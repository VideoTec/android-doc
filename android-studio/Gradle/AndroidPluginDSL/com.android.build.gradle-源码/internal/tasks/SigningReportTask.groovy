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

package com.android.build.gradle.internal.tasks

import com.android.build.gradle.internal.dsl.SigningConfigDsl
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.model.SigningConfig
import com.android.builder.signing.CertificateInfo
import com.android.builder.signing.KeystoreHelper
import com.android.builder.signing.KeytoolException
import com.google.common.collect.Maps
import org.gradle.api.tasks.TaskAction
import org.gradle.logging.StyledTextOutput
import org.gradle.logging.StyledTextOutputFactory

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.text.DateFormat

import static org.gradle.logging.StyledTextOutput.Style.Description
import static org.gradle.logging.StyledTextOutput.Style.Failure
import static org.gradle.logging.StyledTextOutput.Style.Identifier
import static org.gradle.logging.StyledTextOutput.Style.Normal

/**
 * Report tasks displaying the signing information for all variants.
 */
class SigningReportTask extends BaseTask {

    private Set<BaseVariantData> variants = [];

    @TaskAction
    public void generate() throws IOException {

        StyledTextOutput textOutput = getServices().get(
                StyledTextOutputFactory.class).create(getClass())

        Map<SigningConfig, SigningInfo> cache = Maps.newHashMap()

        for (BaseVariantData variant : variants) {
            textOutput.withStyle(Identifier).text("Variant: ")
            textOutput.withStyle(Description).text(variant.name)
            textOutput.println()

            // get the data
            SigningConfigDsl signingConfig = (SigningConfigDsl) variant.variantConfiguration.signingConfig
            if (signingConfig == null) {
                textOutput.withStyle(Identifier).text("Config: ")
                textOutput.withStyle(Normal).text("none")
                textOutput.println()
            } else {
                SigningInfo signingInfo = getSigningInfo(signingConfig, cache)


                textOutput.withStyle(Identifier).text("Config: ")
                textOutput.withStyle(Description).text(signingConfig.name)
                textOutput.println()

                textOutput.withStyle(Identifier).text("Store: ")
                textOutput.withStyle(Description).text(signingConfig.getStoreFile())
                textOutput.println()

                textOutput.withStyle(Identifier).text("Alias: ")
                textOutput.withStyle(Description).text(signingConfig.getKeyAlias())
                textOutput.println()

                if (signingInfo.isValid()) {
                    if (signingInfo.error != null) {
                        textOutput.withStyle(Identifier).text("Error: ")
                        textOutput.withStyle(Failure).text(signingInfo.error)
                        textOutput.println()
                    } else {
                        textOutput.withStyle(Identifier).text("MD5: ")
                        textOutput.withStyle(Description).text(signingInfo.md5)
                        textOutput.println()

                        textOutput.withStyle(Identifier).text("SHA1: ")
                        textOutput.withStyle(Description).text(signingInfo.sha1)
                        textOutput.println()

                        textOutput.withStyle(Identifier).text("Valid until: ")
                        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL)
                        textOutput.withStyle(Description).text(df.format(signingInfo.notAfter))
                        textOutput.println()
                    }
                }
            }

            textOutput.withStyle(Normal).text("----------")
            textOutput.println()
        }
    }

    /**
     * Sets the configurations to generate the report for.
     *
     * @param configurations The configuration. Must not be null.
     */
    public void setVariants(Collection<BaseVariantData> variants) {
        this.variants.addAll(variants);
    }

    private static SigningInfo getSigningInfo(SigningConfig signingConfig,
                                       Map<SigningConfig, SigningInfo> cache) {
        SigningInfo signingInfo = cache.get(signingConfig)

        if (signingInfo == null) {
            signingInfo = new SigningInfo()

            if (signingConfig.isSigningReady()) {
                try {
                    CertificateInfo certificateInfo = KeystoreHelper.getCertificateInfo(
                            signingConfig)
                    if (certificateInfo != null) {
                        signingInfo.md5 = getFingerprint(certificateInfo.certificate, "MD5")
                        signingInfo.sha1 = getFingerprint(certificateInfo.certificate, "SHA1")
                        signingInfo.notAfter = certificateInfo.certificate.notAfter
                    } else {

                    }
                } catch (KeytoolException e) {
                    signingInfo.error = e.getMessage()
                } catch (FileNotFoundException e) {
                    signingInfo.error = "Missing keystore"
                }
            }

            cache.put(signingConfig, signingInfo)
        }

        return signingInfo
    }

    private final static class SigningInfo {
        String md5
        String sha1
        Date notAfter
        String error

        boolean isValid() {
            return md5 != null || error != null
        }
    }

    /**
     * Returns the {@link Certificate} fingerprint as returned by <code>keytool</code>.
     *
     * @param certificate
     * @param hashAlgorithm
     */
    public static String getFingerprint(Certificate cert, String hashAlgorithm) {
        if (cert == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
            return toHexadecimalString(digest.digest(cert.getEncoded()));
        } catch(NoSuchAlgorithmException e) {
            // ignore
        } catch(CertificateEncodingException e) {
            // ignore
        }
        return null;
    }

    private static String toHexadecimalString(byte[] value) {
        StringBuilder sb = new StringBuilder();
        int len = value.length;
        for (int i = 0; i < len; i++) {
            int num = ((int) value[i]) & 0xff;
            if (num < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(num));
            if (i < len - 1) {
                sb.append(':');
            }
        }
        return sb.toString().toUpperCase(Locale.US);
    }
}