/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.builder.packaging;

import com.android.annotations.NonNull;
import com.android.builder.signing.SignedJarBuilder.IZipEntryFilter.ZipAbortException;

import java.io.File;

/**
 * An exception thrown during packaging of an APK file.
 */
public final class DuplicateFileException extends ZipAbortException {
    private static final long serialVersionUID = 1L;
    private final String mArchivePath;
    private final File mFile1;
    private final File mFile2;

    public DuplicateFileException(@NonNull String archivePath, @NonNull File file1,
                                  @NonNull File file2) {
        super();
        mArchivePath = archivePath;
        mFile1 = file1;
        mFile2 = file2;
    }

    public String getArchivePath() {
        return mArchivePath;
    }

    public File getFile1() {
        return mFile1;
    }

    public File getFile2() {
        return mFile2;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append("Duplicate files copied in APK ").append(mArchivePath).append('\n');
        sb.append("\tFile 1: ").append(mFile1).append('\n');
        sb.append("\tFile 2: ").append(mFile1).append('\n');

        return sb.toString();
    }
}