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

import com.google.common.base.Charsets
import com.google.common.io.Files
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task to merge files. This appends all the files together into an output file.
 */
class MergeFileTask extends DefaultTask {

    @InputFiles
    Set<File> inputFiles

    @OutputFile
    File outputFile

    @TaskAction
    void mergeFiles() {

        Set<File> files = getInputFiles();
        File output = getOutputFile()

        if (files.size() == 1) {
            Files.copy(files.iterator().next(), output);
            return
        }

        // first delete the current file
        output.delete();

        // no input? done.
        if (files.isEmpty()) {
            return
        }

        // otherwise put the all the files together
        for (File file : files) {
            String content = Files.toString(file, Charsets.UTF_8);
            Files.append(content, output, Charsets.UTF_8);
            Files.append("\n", output, Charsets.UTF_8);
        }
    }
}
