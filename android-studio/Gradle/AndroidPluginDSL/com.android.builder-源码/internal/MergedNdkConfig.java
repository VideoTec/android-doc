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

package com.android.builder.internal;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.NdkConfig;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Implementation of NdkConfig used to merge multiple configs together.
 */
public class MergedNdkConfig implements NdkConfig {

    private String moduleName;
    private String cFlags;
    private Set<String> ldLibs;
    private Set<String> abiFilters;
    private String stl;

    public void reset() {
        moduleName = null;
        cFlags = null;
        ldLibs = null;
        abiFilters = null;
    }

    @Override
    @Nullable
    public String getModuleName() {
        return moduleName;
    }

    @Override
    @Nullable
    public String getcFlags() {
        return cFlags;
    }

    @Override
    @Nullable
    public Set<String> getLdLibs() {
        return ldLibs;
    }

    @Override
    @Nullable
    public Set<String> getAbiFilters() {
        return abiFilters;
    }

    @Override
    @Nullable
    public String getStl() {
        return stl;
    }

    public void append(@NonNull NdkConfig ndkConfig) {
        // override
        if (ndkConfig.getModuleName() != null) {
            moduleName = ndkConfig.getModuleName();
        }

        if (ndkConfig.getStl() != null) {
            stl = ndkConfig.getStl();
        }

        // append
        if (ndkConfig.getAbiFilters() != null) {
            if (abiFilters == null) {
                abiFilters = Sets.newHashSetWithExpectedSize(ndkConfig.getAbiFilters().size());
            } else {
                abiFilters.clear();
            }
            abiFilters.addAll(ndkConfig.getAbiFilters());
        }

        if (cFlags == null) {
            cFlags = ndkConfig.getcFlags();
        } else if (ndkConfig.getcFlags() != null) {
            cFlags = cFlags + " " + ndkConfig.getcFlags();
        }

        if (ndkConfig.getLdLibs() != null) {
            if (ldLibs == null) {
                ldLibs = Sets.newHashSetWithExpectedSize(ndkConfig.getLdLibs().size());
            } else {
                ldLibs.clear();
            }
            ldLibs.addAll(ndkConfig.getLdLibs());
        }
    }
}
