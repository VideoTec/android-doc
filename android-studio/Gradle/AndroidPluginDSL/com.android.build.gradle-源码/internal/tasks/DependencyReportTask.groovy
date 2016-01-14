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

package com.android.build.gradle.internal.tasks
import com.android.build.gradle.internal.AndroidAsciiReportRenderer
import com.android.build.gradle.internal.variant.BaseVariantData
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.logging.StyledTextOutputFactory
/**
 */
public class DependencyReportTask extends DefaultTask {

    private AndroidAsciiReportRenderer renderer = new AndroidAsciiReportRenderer();

    private Set<BaseVariantData> variants = [];

    @TaskAction
    public void generate() throws IOException {
        renderer.setOutput(getServices().get(StyledTextOutputFactory.class).create(getClass()));

        SortedSet<BaseVariantData> sortedConfigurations = new TreeSet<BaseVariantData>(
                new Comparator<BaseVariantData>() {
            public int compare(BaseVariantData conf1, BaseVariantData conf2) {
                return conf1.getName().compareTo(conf2.getName());
            }
        });
        sortedConfigurations.addAll(getVariants());
        for (BaseVariantData variant : sortedConfigurations) {
            renderer.startVariant(variant);
            renderer.render(variant);
        }
    }

    /**
     * Returns the configurations to generate the report for. Default to all configurations of
     * this task's containing project.
     *
     * @return the configurations.
     */
    public Set<BaseVariantData> getVariants() {
        return variants;
    }

    /**
     * Sets the configurations to generate the report for.
     *
     * @param configurations The configuration. Must not be null.
     */
    public void setVariants(Collection<BaseVariantData> variants) {
        this.variants.addAll(variants);
    }
}
