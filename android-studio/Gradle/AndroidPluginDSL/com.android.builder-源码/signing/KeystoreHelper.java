/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.builder.signing;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.SigningConfig;
import com.android.prefs.AndroidLocation;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.utils.GrabProcessOutput;
import com.android.utils.GrabProcessOutput.IProcessOutput;
import com.android.utils.GrabProcessOutput.Wait;
import com.android.utils.ILogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * A Helper to create and read keystore/keys.
 */
public final class KeystoreHelper {

    // Certificate CN value. This is a hard-coded value for the debug key.
    // Android Market checks against this value in order to refuse applications signed with
    // debug keys.
    private static final String CERTIFICATE_DESC = "CN=Android Debug,O=Android,C=US";


    /**
     * Returns the location of the default debug keystore.
     *
     * @return The location of the default debug keystore.
     * @throws AndroidLocationException if the location cannot be computed
     */
    @NonNull
    public static String defaultDebugKeystoreLocation() throws AndroidLocationException {
        //this is guaranteed to either return a non null value (terminated with a platform
        // specific separator), or throw.
        String folder = AndroidLocation.getFolder();
        return folder + "debug.keystore";
    }

    /**
     * Creates a new debug store with the location, keyalias, and passwords specified in the
     * config.
     *
     * @param signingConfig The signing config
     * @param logger a logger object to receive the log of the creation.
     * @throws KeytoolException
     */
    public static boolean createDebugStore(@NonNull SigningConfig signingConfig,
                                           @NonNull ILogger logger) throws KeytoolException {

        return createNewStore(signingConfig, CERTIFICATE_DESC, 30 /* validity*/, logger);
    }

    /**
     * Creates a new store
     *
     * @param signingConfig the Signing Configuration
     * @param description description
     * @param validityYears
     * @param logger
     * @throws KeytoolException
     */
    private static boolean createNewStore(
            @NonNull SigningConfig signingConfig,
            @NonNull String description,
            int validityYears,
            @NonNull final ILogger logger)
            throws KeytoolException {

        // get the executable name of keytool depending on the platform.
        String os = System.getProperty("os.name");

        String keytoolCommand;
        if (os.startsWith("Windows")) {
            keytoolCommand = "keytool.exe";
        } else {
            keytoolCommand = "keytool";
        }

        String javaHome = System.getProperty("java.home");

        if (javaHome != null && javaHome.length() > 0) {
            keytoolCommand = javaHome + File.separator + "bin" + File.separator + keytoolCommand;
        }

        // create the command line to call key tool to build the key with no user input.
        ArrayList<String> commandList = new ArrayList<String>();
        commandList.add(keytoolCommand);
        commandList.add("-genkey");
        commandList.add("-alias");
        commandList.add(signingConfig.getKeyAlias());
        commandList.add("-keyalg");
        commandList.add("RSA");
        commandList.add("-dname");
        commandList.add(description);
        commandList.add("-validity");
        commandList.add(Integer.toString(validityYears * 365));
        commandList.add("-keypass");
        commandList.add(signingConfig.getKeyPassword());
        commandList.add("-keystore");
        commandList.add(signingConfig.getStoreFile().getAbsolutePath());
        commandList.add("-storepass");
        commandList.add(signingConfig.getStorePassword());
        if (signingConfig.getStoreType() != null) {
            commandList.add("-storetype");
            commandList.add(signingConfig.getStoreType());
        }

        String[] commandArray = commandList.toArray(new String[commandList.size()]);

        // launch the command line process
        int result = 0;
        try {
            Process process = Runtime.getRuntime().exec(commandArray);
            result = GrabProcessOutput.grabProcessOutput(
                    process,
                    Wait.WAIT_FOR_READERS,
                    new IProcessOutput() {
                        @Override
                        public void out(@Nullable String line) {
                            if (line != null) {
                                logger.info(line);
                            }
                        }

                        @Override
                        public void err(@Nullable String line) {
                            if (line != null) {
                                logger.error(null /*throwable*/, line);
                            }
                        }
                    });
        } catch (Exception e) {
            // create the command line as one string for debugging purposes
            StringBuilder builder = new StringBuilder();
            boolean firstArg = true;
            for (String arg : commandArray) {
                boolean hasSpace = arg.indexOf(' ') != -1;

                if (firstArg) {
                    firstArg = false;
                } else {
                    builder.append(' ');
                }

                if (hasSpace) {
                    builder.append('"');
                }

                builder.append(arg);

                if (hasSpace) {
                    builder.append('"');
                }
            }

            throw new KeytoolException("Failed to create key: " + e.getMessage(),
                    javaHome, builder.toString());
        }

        return result == 0;
    }

    /**
     * Returns the CertificateInfo for the given signing configuration.
     *
     * Returns null if the key could not be found. If the passwords are wrong,
     * it throws an exception
     *
     * @param signingConfig the signing configuration
     * @return the certificate info if it could be loaded.
     * @throws KeytoolException
     * @throws FileNotFoundException
     */
    public static CertificateInfo getCertificateInfo(@NonNull SigningConfig signingConfig)
            throws KeytoolException, FileNotFoundException {

        try {
            KeyStore keyStore = KeyStore.getInstance(
                    signingConfig.getStoreType() != null ?
                            signingConfig.getStoreType() : KeyStore.getDefaultType());

            FileInputStream fis = new FileInputStream(signingConfig.getStoreFile());
            //noinspection ConstantConditions
            keyStore.load(fis, signingConfig.getStorePassword().toCharArray());
            fis.close();

            //noinspection ConstantConditions
            char[] keyPassword = signingConfig.getKeyPassword().toCharArray();
            PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(
                    signingConfig.getKeyAlias(),
                    new KeyStore.PasswordProtection(keyPassword));

            if (entry != null) {
                return new CertificateInfo(entry.getPrivateKey(),
                        (X509Certificate) entry.getCertificate());
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new KeytoolException(
                    String.format("Failed to read key %1$s from store \"%2$s\": %3$s",
                            signingConfig.getKeyAlias(), signingConfig.getStoreFile(),
                            e.getMessage()),
                    e);
        }

        return null;
    }
}
