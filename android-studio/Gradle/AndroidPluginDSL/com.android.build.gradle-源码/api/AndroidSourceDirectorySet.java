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

package com.android.build.gradle.api;

import com.android.annotations.NonNull;

import java.io.File;
import java.util.Set;

/**
 * An AndroidSourceDirectorySet represents a lit of directory input for an Android project.
 */
public interface AndroidSourceDirectorySet {

    /**
     * A concise name for the source directory (typically used to identify it in a collection).
     */
    @NonNull
    String getName();

    /**
     * Adds the given source directory to this set.
     *
     * @param srcDir The source directory. This is evaluated as for
     *                {@link org.gradle.api.Project#file(Object)}
     * @return this
     */
    @NonNull
    AndroidSourceDirectorySet srcDir(Object srcDir);

    /**
     * Adds the given source directories to this set.
     *
     * @param srcDirs The source directories. These are evaluated as for
     *                {@link org.gradle.api.Project#files(Object...)}
     * @return this
     */
    @NonNull
    AndroidSourceDirectorySet srcDirs(Object... srcDirs);

    /**
     * Sets the source directories for this set.
     *
     * @param srcDirs The source directories. These are evaluated as for
     *                {@link org.gradle.api.Project#files(Object...)}
     * @return this
     */
    @NonNull
    AndroidSourceDirectorySet setSrcDirs(Iterable<?> srcDirs);

    /**
     * Returns the resolved directories.
     * @return a non null set of File objects.
     */
    @NonNull
    Set<File> getSrcDirs();
}
