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

package com.android.build.gradle.api

import com.android.annotations.NonNull
import org.gradle.api.file.SourceDirectorySet

/**
 * A {@code AndroidSourceSet} represents a logical group of Java, aidl, renderscript source
 * as well as Android and non-Android resources.
 */
public interface AndroidSourceSet {

    /**
     * Returns the name of this source set.
     *
     * @return The name. Never returns null.
     */
    @NonNull
    String getName();

    /**
     * Returns the Java resources which are to be copied into the javaResources output directory.
     *
     * @return the java resources. Never returns null.
     */
    @NonNull
    SourceDirectorySet getResources();

    /**
     * Configures the Java resources for this set.
     *
     * <p>The given closure is used to configure the {@link SourceDirectorySet} which contains the
     * java resources.
     *
     * @param configureClosure The closure to use to configure the javaResources.
     * @return this
     */
    @NonNull
    AndroidSourceSet resources(Closure configureClosure);

    /**
     * Returns the Java source which is to be compiled by the Java compiler into the class output
     * directory.
     *
     * @return the Java source. Never returns null.
     */
    @NonNull
    SourceDirectorySet getJava();

    /**
     * Configures the Java source for this set.
     *
     * <p>The given closure is used to configure the {@link SourceDirectorySet} which contains the
     * Java source.
     *
     * @param configureClosure The closure to use to configure the Java source.
     * @return this
     */
    @NonNull
    AndroidSourceSet java(Closure configureClosure);

    /**
     * All Java source files for this source set. This includes, for example, source which is
     * directly compiled, and source which is indirectly compiled through joint compilation.
     *
     * @return the Java source. Never returns null.
     */
    @NonNull
    SourceDirectorySet getAllJava();

    /**
     * All source files for this source set.
     *
     * @return the source. Never returns null.
     */
    @NonNull
    SourceDirectorySet getAllSource();

    /**
     * Returns the name of the compile configuration for this source set.
     * @return The configuration name
     */
    @NonNull
    String getCompileConfigurationName();

    /**
     * Returns the name of the runtime configuration for this source set.
     * @return The runtime configuration name
     */
    @NonNull
    String getPackageConfigurationName();

    /**
     * The Android Manifest file for this source set.
     *
     * @return the manifest. Never returns null.
     */
    @NonNull
    AndroidSourceFile getManifest();

    /**
     * Configures the location of the Android Manifest for this set.
     *
     * <p>The given closure is used to configure the {@link AndroidSourceFile} which contains the
     * manifest.
     *
     * @param configureClosure The closure to use to configure the Android Manifest.
     * @return this
     */
    @NonNull
    AndroidSourceSet manifest(Closure configureClosure);

    /**
     * The Android Resources directory for this source set.
     *
     * @return the resources. Never returns null.
     */
    @NonNull
    AndroidSourceDirectorySet getRes();

    /**
     * Configures the location of the Android Resources for this set.
     *
     * <p>The given closure is used to configure the {@link AndroidSourceDirectorySet}
     * which contains the resources.
     *
     * @param configureClosure The closure to use to configure the Resources.
     * @return this
     */
    @NonNull
    AndroidSourceSet res(Closure configureClosure);

    /**
     * The Android Assets directory for this source set.
     *
     * @return the assets. Never returns null.
     */
    @NonNull
    AndroidSourceDirectorySet getAssets();

    /**
     * Configures the location of the Android Assets for this set.
     *
     * <p>The given closure is used to configure the {@link AndroidSourceDirectorySet}
     * which contains the assets.
     *
     * @param configureClosure The closure to use to configure the Assets.
     * @return this
     */
    @NonNull
    AndroidSourceSet assets(Closure configureClosure);

    /**
     * The Android AIDL source directory for this source set.
     *
     * @return the source. Never returns null.
     */
    @NonNull
    AndroidSourceDirectorySet getAidl();

    /**
     * Configures the location of the Android AIDL source for this set.
     *
     * <p>The given closure is used to configure the {@link AndroidSourceDirectorySet}
     * which contains the AIDL source.
     *
     * @param configureClosure The closure to use to configure the AIDL source.
     * @return this
     */
    @NonNull
    AndroidSourceSet aidl(Closure configureClosure);

    /**
     * The Android Renderscript source directory for this source set.
     *
     * @return the source. Never returns null.
     */
    @NonNull
    AndroidSourceDirectorySet getRenderscript();

    /**
     * Configures the location of the Android Renderscript source for this set.
     *
     * <p>The given closure is used to configure the {@link AndroidSourceDirectorySet}
     * which contains the Renderscript source.
     *
     * @param configureClosure The closure to use to configure the Renderscript source.
     * @return this
     */
    @NonNull
    AndroidSourceSet renderscript(Closure configureClosure);

    /**
     * The Android JNI source directory for this source set.
     *
     * @return the source. Never returns null.
     */
    @NonNull
    AndroidSourceDirectorySet getJni();

    /**
     * Configures the location of the Android JNI source for this set.
     *
     * <p>The given closure is used to configure the {@link AndroidSourceDirectorySet}
     * which contains the JNI source.
     *
     * @param configureClosure The closure to use to configure the JNI source.
     * @return this
     */
    @NonNull
    AndroidSourceSet jni(Closure configureClosure);

    /**
     * Sets the root of the source sets to a given path.
     *
     * All entries of the source set are located under this root directory.
     *
     * @param path the root directory.
     * @return this
     */
    @NonNull
    AndroidSourceSet setRoot(String path);
}
