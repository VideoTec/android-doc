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

package com.android.builder;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.io.FileWrapper;
import com.android.io.StreamException;
import com.android.xml.AndroidManifest;
import com.android.xml.AndroidXPathFactory;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class DefaultManifestParser implements ManifestParser {

    @Nullable
    @Override
    public String getPackage(@NonNull File manifestFile) {
        XPath xpath = AndroidXPathFactory.newXPath();

        try {
            return xpath.evaluate("/manifest/@package",
                    new InputSource(new FileInputStream(manifestFile)));
        } catch (XPathExpressionException e) {
            // won't happen.
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Nullable
    @Override
    public String getVersionName(@NonNull File manifestFile) {
        XPath xpath = AndroidXPathFactory.newXPath();

        try {
            return xpath.evaluate("/manifest/@android:versionName",
                    new InputSource(new FileInputStream(manifestFile)));
        } catch (XPathExpressionException e) {
            // won't happen.
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public int getVersionCode(@NonNull File manifestFile) {
        XPath xpath = AndroidXPathFactory.newXPath();

        try {
            String value= xpath.evaluate("/manifest/@android:versionCode",
                    new InputSource(new FileInputStream(manifestFile)));
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (XPathExpressionException e) {
            // won't happen.
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NumberFormatException e) {
            // return -1 below.
        }

        return -1;
    }

    @Override
    public int getMinSdkVersion(@NonNull File manifestFile) {
        try {
            Object value = AndroidManifest.getMinSdkVersion(new FileWrapper(manifestFile));
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                // TODO: support codename
            }

        } catch (XPathExpressionException e) {
            // won't happen.
        } catch (StreamException e) {
            throw new RuntimeException(e);
        }

        return 1;
    }

    @Override
    public int getTargetSdkVersion(@NonNull File manifestFile) {
        try {
            Integer value = AndroidManifest.getTargetSdkVersion(new FileWrapper(manifestFile));
            if (value != null) {
                return value;
            } else {
                return -1;
            }

        } catch (XPathExpressionException e) {
            // won't happen.
        } catch (StreamException e) {
            throw new RuntimeException(e);
        }

        return -1;
    }
}
