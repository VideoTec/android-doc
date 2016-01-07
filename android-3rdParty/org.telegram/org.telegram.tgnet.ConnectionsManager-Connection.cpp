/*
 * This is the source code of tgnet library v. 1.0
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2015.
 */

#include "Connection.h"
#include "ConnectionsManager.h"
#include "BuffersStorage.h"
#include "FileLog.h"
#include "Timer.h"
#include "Datacenter.h"
#include "NativeByteBuffer.h"

static uint32_t lastConnectionToken = 1;

Connection::Connection(Datacenter *datacenter, ConnectionType type) {
    currentDatacenter = datacenter;
    connectionType = type;
    genereateNewSessionId();
    connectionState = TcpConnectionStageIdle;
    reconnectTimer = new Timer([&] {
        reconnectTimer->stop();
        connect();
    });
}

Connection::~Connection() {
    if (reconnectTimer != nullptr) {
        reconnectTimer->stop();
        delete reconnectTimer;
        reconnectTimer = nullptr;
    }
}

void Connection::suspendConnection() {
    reconnectTimer->stop();
    if (connectionState == TcpConnectionStageIdle || connectionState == TcpConnectionStageSuspended) {
        return;
    }
    DEBUG_D("connection(%p, dc%u, type %d) suspend", this, currentDatacenter->getDatacenterId(), connectionType);
    connectionState = TcpConnectionStageSuspended;
    dropConnection();
    ConnectionsManager::getInstance().onConnectionClosed(this);
    firstPacketSent = false;
    if (restOfTheData != nullptr) {
        restOfTheData->reuse();
        restOfTheData = nullptr;
    }
    lastPacketLength = 0;
    connectionToken = 0;
    wasConnected = false;
}

void Connection::onReceivedData(NativeByteBuffer *buffer) {
    failedConnectionCount = 0;

    NativeByteBuffer *parseLaterBuffer = nullptr;
    if (restOfTheData != nullptr) {
        if (lastPacketLength == 0) {
            if (restOfTheData->capacity() - restOfTheData->position() >= buffer->limit()) {
                restOfTheData->limit(restOfTheData->position() + buffer->limit());
                restOfTheData->writeBytes(buffer);
                buffer = restOfTheData;
            } else {
                NativeByteBuffer *newBuffer = BuffersStorage::getInstance().getFreeBuffer(restOfTheData->limit() + buffer->limit());
                restOfTheData->rewind();
                newBuffer->writeBytes(restOfTheData);
                newBuffer->writeBytes(buffer);
                buffer = newBuffer;
                restOfTheData->reuse();
                restOfTheData = newBuffer;
            }
        } else {
            uint32_t len;
            if (lastPacketLength - restOfTheData->position() <= buffer->limit()) {
                len = lastPacketLength - restOfTheData->position();
            } else {
                len = buffer->limit();
            }
            uint32_t oldLimit = buffer->limit();
            buffer->limit(len);
            restOfTheData->writeBytes(buffer);
            buffer->limit(oldLimit);
            if (restOfTheData->position() == lastPacketLength) {
                parseLaterBuffer = buffer->hasRemaining() ? buffer : nullptr;
                buffer = restOfTheData;
            } else {
                return;
            }
        }
    }

    buffer->rewind();

    while (buffer->hasRemaining()) {
        if (!hasSomeDataSinceLastConnect) {
            currentDatacenter->storeCurrentAddressAndPortNum();
            isTryingNextPort = false;
            if (connectionType == ConnectionTypePush) {
                setTimeout(60 * 15);
            } else {
                setTimeout(25);
            }
        }
        hasSomeDataSinceLastConnect = true;

        uint32_t currentPacketLength = 0;
        uint32_t mark = buffer->position();
        uint8_t fByte = buffer->readByte(nullptr);

        if ((fByte & (1 << 7)) != 0) {
            buffer->position(mark);
            if (buffer->remaining() < 4) {
                NativeByteBuffer *reuseLater = restOfTheData;
                restOfTheData = BuffersStorage::getInstance().getFreeBuffer(16384);
                restOfTheData->writeBytes(buffer);
                restOfTheData->limit(restOfTheData->position());
                lastPacketLength = 0;
                if (reuseLater != nullptr) {
                    reuseLater->reuse();
                }
                break;
            }
            int32_t ackId = buffer->readBigInt32(nullptr) & (~(1 << 31));
            ConnectionsManager::getInstance().onConnectionQuickAckReceived(this, ackId);
            continue;
        }

        if (fByte != 0x7f) {
            currentPacketLength = ((uint32_t) fByte) * 4;
        } else {
            buffer->position(mark);
            if (buffer->remaining() < 4) {
                if (restOfTheData == nullptr || (restOfTheData != nullptr && restOfTheData->position() != 0)) {
                    NativeByteBuffer *reuseLater = restOfTheData;
                    restOfTheData = BuffersStorage::getInstance().getFreeBuffer(16384);
                    restOfTheData->writeBytes(buffer);
                    restOfTheData->limit(restOfTheData->position());
                    lastPacketLength = 0;
                    if (reuseLater != nullptr) {
                        reuseLater->reuse();
                    }
                } else {
                    restOfTheData->position(restOfTheData->limit());
                }
                break;
            }
            currentPacketLength = ((uint32_t) buffer->readInt32(nullptr) >> 8) * 4;
        }

        if (currentPacketLength % 4 != 0 || currentPacketLength > 2 * 1024 * 1024) {
            DEBUG_E("connection(%p, dc%u, type %d) received invalid packet length", this, currentDatacenter->getDatacenterId(), connectionType);
            reconnect();
            return;
        }

        if (currentPacketLength < buffer->remaining()) {
            DEBUG_E("connection(%p, dc%u, type %d) received message len %u but packet larger %u", this, currentDatacenter->getDatacenterId(), connectionType, currentPacketLength, buffer->remaining());
        } else if (currentPacketLength == buffer->remaining()) {
            DEBUG_E("connection(%p, dc%u, type %d) received message len %u equal to packet size", this, currentDatacenter->getDatacenterId(), connectionType, currentPacketLength);
        } else {
            DEBUG_E("connection(%p, dc%u, type %d) received packet size less(%u) then message size(%u)", this, currentDatacenter->getDatacenterId(), connectionType, buffer->remaining(), currentPacketLength);

            NativeByteBuffer *reuseLater = nullptr;
            uint32_t len = currentPacketLength + (fByte != 0x7f ? 1 : 4);
            if (restOfTheData != nullptr && restOfTheData->capacity() < len) {
                reuseLater = restOfTheData;
                restOfTheData = nullptr;
            }
            if (restOfTheData == nullptr) {
                buffer->position(mark);
                restOfTheData = BuffersStorage::getInstance().getFreeBuffer(len);
                restOfTheData->writeBytes(buffer);
            } else {
                restOfTheData->position(restOfTheData->limit());
                restOfTheData->limit(len);
            }
            lastPacketLength = len;
            if (reuseLater != nullptr) {
                reuseLater->reuse();
            }
            return;
        }

        uint32_t old = buffer->limit();
        buffer->limit(buffer->position() + currentPacketLength);
        ConnectionsManager::getInstance().onConnectionDataReceived(this, buffer, currentPacketLength);
        buffer->position(buffer->limit());
        buffer->limit(old);

        if (restOfTheData != nullptr) {
            if ((lastPacketLength != 0 && restOfTheData->position() == lastPacketLength) || (lastPacketLength == 0 && !restOfTheData->hasRemaining())) {
                restOfTheData->reuse();
                restOfTheData = nullptr;
            } else {
                DEBUG_E("compact occured");
                restOfTheData->compact();
                restOfTheData->limit(restOfTheData->position());
                restOfTheData->position(0);
            }
        }

        if (parseLaterBuffer != nullptr) {
            buffer = parseLaterBuffer;
            parseLaterBuffer = nullptr;
        }
    }
}

void Connection::connect() {
    if (!ConnectionsManager::getInstance().isNetworkAvailable()) {
        ConnectionsManager::getInstance().onConnectionClosed(this);
        return;
    }
    if ((connectionState == TcpConnectionStageConnected || connectionState == TcpConnectionStageConnecting)) {
        return;
    }
    connectionState = TcpConnectionStageConnecting;
    bool ipv6 = ConnectionsManager::getInstance().isIpv6Enabled();
    if (connectionType == ConnectionTypeDownload) {
        currentAddressFlags = 2;
        hostAddress = currentDatacenter->getCurrentAddress(currentAddressFlags | (ipv6 ? 1 : 0));
        if (hostAddress.empty()) {
            currentAddressFlags = 0;
            hostAddress = currentDatacenter->getCurrentAddress(currentAddressFlags | (ipv6 ? 1 : 0));
        }
        if (hostAddress.empty() && ipv6) {
            currentAddressFlags = 2;
            hostAddress = currentDatacenter->getCurrentAddress(currentAddressFlags);
            if (hostAddress.empty()) {
                currentAddressFlags = 0;
                hostAddress = currentDatacenter->getCurrentAddress(currentAddressFlags);
            }
        }
    } else {
        currentAddressFlags = 0;
        hostAddress = currentDatacenter->getCurrentAddress(currentAddressFlags | (ipv6 ? 1 : 0));
        if (ipv6 && hostAddress.empty()) {
            hostAddress = currentDatacenter->getCurrentAddress(currentAddressFlags);
        }
    }
    hostPort = (uint16_t) currentDatacenter->getCurrentPort(currentAddressFlags);

    reconnectTimer->stop();

    DEBUG_D("connection(%p, dc%u, type %d) connecting (%s:%hu)", this, currentDatacenter->getDatacenterId(), connectionType, hostAddress.c_str(), hostPort);
    firstPacketSent = false;
    if (restOfTheData != nullptr) {
        restOfTheData->reuse();
        restOfTheData = nullptr;
    }
    lastPacketLength = 0;
    wasConnected = false;
    hasSomeDataSinceLastConnect = false;
    openConnection(hostAddress, hostPort, ipv6);
    if (connectionType == ConnectionTypePush) {
        if (isTryingNextPort) {
            setTimeout(20);
        } else {
            setTimeout(30);
        }
    } else {
        if (isTryingNextPort) {
            setTimeout(8);
        } else {
            if (connectionType == ConnectionTypeUpload) {
                setTimeout(25);
            } else {
                setTimeout(15);
            }
        }
    }
}

void Connection::reconnect() {
    suspendConnection();
    connectionState = TcpConnectionStageReconnecting;
    connect();
}

void Connection::sendData(NativeByteBuffer *buff, bool reportAck) {
    if (buff == nullptr) {
        return;
    }
    buff->rewind();
    if (connectionState == TcpConnectionStageIdle || connectionState == TcpConnectionStageReconnecting || connectionState == TcpConnectionStageSuspended) {
        connect();
    }

    if (isDisconnected()) {
        buff->reuse();
        DEBUG_D("connection(%p, dc%u, type %d) disconnected, don't send data", this, currentDatacenter->getDatacenterId(), connectionType);
        return;
    }

    uint32_t bufferLen = 0;
    uint32_t packetLength = buff->limit() / 4;

    if (packetLength < 0x7f) {
        bufferLen++;
    } else {
        bufferLen += 4;
    }
    if (!firstPacketSent) {
        bufferLen++;
    }

    NativeByteBuffer *buffer = BuffersStorage::getInstance().getFreeBuffer(bufferLen);
    if (!firstPacketSent) {
        buffer->writeByte(0xef);
        firstPacketSent = true;
    }
    if (packetLength < 0x7f) {
        if (reportAck) {
            packetLength |= (1 << 7);
        }
        buffer->writeByte((uint8_t) packetLength);
    } else {
        packetLength = (packetLength << 8) + 0x7f;
        if (reportAck) {
            packetLength |= (1 << 7);
        }
        buffer->writeInt32(packetLength);
    }

    buffer->rewind();
    writeBuffer(buffer);
    buff->rewind();
    writeBuffer(buff);
}

void Connection::onDisconnected(int reason) {
    reconnectTimer->stop();
    DEBUG_D("connection(%p, dc%u, type %d) disconnected with reason %d", this, currentDatacenter->getDatacenterId(), connectionType, reason);
    bool switchToNextPort = wasConnected && !hasSomeDataSinceLastConnect && reason == 2;
    firstPacketSent = false;
    if (restOfTheData != nullptr) {
        restOfTheData->reuse();
        restOfTheData = nullptr;
    }
    connectionToken = 0;
    lastPacketLength = 0;
    wasConnected = false;
    if (connectionState != TcpConnectionStageSuspended && connectionState != TcpConnectionStageIdle) {
        connectionState = TcpConnectionStageIdle;
    }
    ConnectionsManager::getInstance().onConnectionClosed(this);

    uint32_t datacenterId = currentDatacenter->getDatacenterId();
    if (connectionState == TcpConnectionStageIdle && connectionType == ConnectionTypeGeneric && (datacenterId == ConnectionsManager::getInstance().currentDatacenterId || datacenterId == ConnectionsManager::getInstance().movingToDatacenterId)) {
        connectionState = TcpConnectionStageReconnecting;
        failedConnectionCount++;
        if (failedConnectionCount == 1) {
            if (hasSomeDataSinceLastConnect) {
                willRetryConnectCount = 5;
            } else {
                willRetryConnectCount = 1;
            }
        }
        if (ConnectionsManager::getInstance().isNetworkAvailable()) {
            isTryingNextPort = true;
            if (failedConnectionCount > willRetryConnectCount || switchToNextPort) {
                currentDatacenter->nextAddressOrPort(currentAddressFlags);
                failedConnectionCount = 0;
            }
        }
        DEBUG_D("connection(%p, dc%u, type %d) reconnect %s:%hu", this, currentDatacenter->getDatacenterId(), connectionType, hostAddress.c_str(), hostPort);
        reconnectTimer->setTimeout(1000, false);
        reconnectTimer->start();
    }
}

void Connection::onConnected() {
    connectionState = TcpConnectionStageConnected;
    connectionToken = lastConnectionToken++;
    wasConnected = true;
    DEBUG_D("connection(%p, dc%u, type %d) connected to %s:%hu", this, currentDatacenter->getDatacenterId(), connectionType, hostAddress.c_str(), hostPort);
    ConnectionsManager::getInstance().onConnectionConnected(this);
}

Datacenter *Connection::getDatacenter() {
    return currentDatacenter;
}

ConnectionType Connection::getConnectionType() {
    return connectionType;
}

uint32_t Connection::getConnectionToken() {
    return connectionToken;
}
