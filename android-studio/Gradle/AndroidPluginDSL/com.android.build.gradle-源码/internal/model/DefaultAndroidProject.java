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

package com.android.build.gradle.internal.model;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.CompileOptions;
import com.android.builder.model.AaptOptions;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.ArtifactMetaData;
import com.android.builder.model.BuildTypeContainer;
import com.android.builder.model.JavaCompileOptions;
import com.android.builder.model.ProductFlavorContainer;
import com.android.builder.model.SigningConfig;
import com.android.builder.model.Variant;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;

/**
 * Implementation of the AndroidProject model object.
 */
class DefaultAndroidProject implements AndroidProject, Serializable {
    private static final long serialVersionUID = 1L;

    @NonNull
    private final String modelVersion;
    @NonNull
    private final String name;
    @NonNull
    private final String compileTarget;
    @NonNull
    private final Collection<String> bootClasspath;
    @NonNull
    private final Collection<File> frameworkSource;
    @NonNull
    private final Collection<SigningConfig> signingConfigs;
    @NonNull
    private final Collection<ArtifactMetaData> extraArtifacts;
    @NonNull
    private final Collection<String> unresolvedDependencies;
    @NonNull
    private final JavaCompileOptions javaCompileOptions;
    private final boolean isLibrary;

    private final Collection<BuildTypeContainer> buildTypes = Lists.newArrayList();
    private final Collection<ProductFlavorContainer> productFlavors = Lists.newArrayList();
    private final Collection<Variant> variants = Lists.newArrayList();

    private ProductFlavorContainer defaultConfig;

    DefaultAndroidProject(@NonNull String modelVersion,
                          @NonNull String name,
                          @NonNull String compileTarget,
                          @NonNull Collection<String> bootClasspath,
                          @NonNull Collection<File> frameworkSource,
                          @NonNull Collection<SigningConfig> signingConfigs,
                          @NonNull Collection<ArtifactMetaData> extraArtifacts,
                          @NonNull Collection<String> unresolvedDependencies,
                          @NonNull CompileOptions compileOptions,
                          boolean isLibrary) {
        this.modelVersion = modelVersion;
        this.name = name;
        this.compileTarget = compileTarget;
        this.bootClasspath = bootClasspath;
        this.frameworkSource = frameworkSource;
        this.signingConfigs = signingConfigs;
        this.extraArtifacts = extraArtifacts;
        this.unresolvedDependencies = unresolvedDependencies;
        javaCompileOptions = new DefaultJavaCompileOptions(compileOptions);
        this.isLibrary = isLibrary;
    }

    @NonNull
    DefaultAndroidProject setDefaultConfig(@NonNull ProductFlavorContainer defaultConfigContainer) {
        defaultConfig = defaultConfigContainer;
        return this;
    }

    @NonNull
    DefaultAndroidProject addBuildType(@NonNull BuildTypeContainer buildTypeContainer) {
        buildTypes.add(buildTypeContainer);
        return this;
    }

    @NonNull
    DefaultAndroidProject addProductFlavors(
            @NonNull ProductFlavorContainer productFlavorContainer) {
        productFlavors.add(productFlavorContainer);
        return this;
    }

    @NonNull
    DefaultAndroidProject addVariant(@NonNull VariantImpl variant) {
        variants.add(variant);
        return this;
    }

    @Override
    @NonNull
    public String getModelVersion() {
        return modelVersion;
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    @NonNull
    public ProductFlavorContainer getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    @NonNull
    public Collection<BuildTypeContainer> getBuildTypes() {
        return buildTypes;
    }

    @Override
    @NonNull
    public Collection<ProductFlavorContainer> getProductFlavors() {
        return productFlavors;
    }

    @Override
    @NonNull
    public Collection<Variant> getVariants() {
        return variants;
    }

    @NonNull
    @Override
    public Collection<ArtifactMetaData> getExtraArtifacts() {
        return extraArtifacts;
    }

    @Override
    public boolean isLibrary() {
        return isLibrary;
    }

    @Override
    @NonNull
    public String getCompileTarget() {
        return compileTarget;
    }

    @Override
    @NonNull
    public Collection<String> getBootClasspath() {
        return bootClasspath;
    }

    @Override
    @NonNull
    public Collection<File> getFrameworkSources() {
        return frameworkSource;
    }

    @Override
    @NonNull
    public Collection<SigningConfig> getSigningConfigs() {
        return signingConfigs;
    }

    @Override
    @NonNull
    public AaptOptions getAaptOptions() {
        return null;
    }

    @Override
    @NonNull
    public Collection<String> getUnresolvedDependencies() {
        return unresolvedDependencies;
    }

    @Override
    @NonNull
    public JavaCompileOptions getJavaCompileOptions() {
        return javaCompileOptions;
    }
}
