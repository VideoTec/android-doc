/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.builder.testing;

import com.android.annotations.NonNull;
import com.android.builder.testing.api.DeviceConnector;
import com.android.builder.testing.api.DeviceException;
import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.utils.ILogger;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Local device connected to with ddmlib. This is a wrapper around {@link IDevice}.
 */
public class ConnectedDevice extends DeviceConnector {

    private final IDevice iDevice;

    public ConnectedDevice(@NonNull IDevice iDevice) {
        this.iDevice = iDevice;
    }

    @NonNull
    @Override
    public String getName() {
        String version = iDevice.getProperty(IDevice.PROP_BUILD_VERSION);
        boolean emulator = iDevice.isEmulator();

        String name;
        if (emulator) {
            name = iDevice.getAvdName() != null ?
                    iDevice.getAvdName() + "(AVD)" :
                    iDevice.getSerialNumber();
        } else {
            String model = iDevice.getProperty(IDevice.PROP_DEVICE_MODEL);
            name = model != null ? model : iDevice.getSerialNumber();
        }

        return version != null ? name + " - " + version : name;
    }

    @Override
    public void connect(int timeout, ILogger logger) throws TimeoutException {
        // nothing to do here
    }

    @Override
    public void disconnect(int timeout, ILogger logger) throws TimeoutException {
        // nothing to do here
    }

    @Override
    public void installPackage(@NonNull File apkFile, int timeout, ILogger logger) throws DeviceException {
        try {
            iDevice.installPackage(apkFile.getAbsolutePath(), true /*reinstall*/);
        } catch (Exception e) {
            logger.error(e, "Unable to install " + apkFile.getAbsolutePath());
            throw new DeviceException(e);
        }
    }

    @Override
    public void uninstallPackage(@NonNull String packageName, int timeout, ILogger logger) throws DeviceException {
        try {
            iDevice.uninstallPackage(packageName);
        } catch (Exception e) {
            logger.error(e, "Unable to uninstall " + packageName);
            throw new DeviceException(e);
        }
    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver,
                                    long maxTimeToOutputResponse, TimeUnit maxTimeUnits)
                                    throws TimeoutException, AdbCommandRejectedException,
                                    ShellCommandUnresponsiveException, IOException {
        iDevice.executeShellCommand(command, receiver, maxTimeToOutputResponse, maxTimeUnits);
    }

    @Override
    public int getApiLevel() {
        String sdkVersion = iDevice.getProperty(IDevice.PROP_BUILD_API_LEVEL);
        if (sdkVersion != null) {
            try {
                return Integer.valueOf(sdkVersion);
            } catch (NumberFormatException e) {

            }
        }

        // can't get it, return 0.
        return 0;
    }

    @NonNull
    @Override
    public List<String> getAbis() {
        List<String> abis = Lists.newArrayListWithExpectedSize(2);
        String abi = iDevice.getProperty(IDevice.PROP_DEVICE_CPU_ABI);
        if (abi != null) {
            abis.add(abi);
        }

        abi = iDevice.getProperty(IDevice.PROP_DEVICE_CPU_ABI2);
        if (abi != null) {
            abis.add(abi);
        }

        return abis;
    }

    @Override
    public int getDensity() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getHeight() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getWidth() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
