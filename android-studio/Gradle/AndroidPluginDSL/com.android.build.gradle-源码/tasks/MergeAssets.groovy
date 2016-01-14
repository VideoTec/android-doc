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
package com.android.build.gradle.tasks
import com.android.build.gradle.internal.tasks.IncrementalTask
import com.android.ide.common.res2.AssetMerger
import com.android.ide.common.res2.AssetSet
import com.android.ide.common.res2.FileStatus
import com.android.ide.common.res2.FileValidity
import com.android.ide.common.res2.MergedAssetWriter
import com.android.ide.common.res2.MergingException
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory

public class MergeAssets extends IncrementalTask {

    // ----- PUBLIC TASK API -----

    @OutputDirectory
    File outputDir

    // ----- PRIVATE TASK API -----

    // fake input to detect changes. Not actually used by the task
    @InputFiles
    Iterable<File> getRawInputFolders() {
        return flattenSourceSets(getInputAssetSets())
    }

    // actual inputs
    List<AssetSet> inputAssetSets

    private final FileValidity<AssetSet> fileValidity = new FileValidity<AssetSet>();

    @Override
    protected boolean isIncremental() {
        return true
    }

    @Override
    protected void doFullTaskAction() {
        // this is full run, clean the previous output
        File destinationDir = getOutputDir()
        emptyFolder(destinationDir)

        List<AssetSet> assetSets = getInputAssetSets()

        // create a new merger and populate it with the sets.
        AssetMerger merger = new AssetMerger()

        try {
            for (AssetSet assetSet : assetSets) {
                // set needs to be loaded.
                assetSet.loadFromFiles(plugin.logger)
                merger.addDataSet(assetSet)
            }

            // get the merged set and write it down.
            MergedAssetWriter writer = new MergedAssetWriter(destinationDir)

            merger.mergeData(writer, false /*doCleanUp*/)

            // No exception? Write the known state.
            merger.writeBlobTo(getIncrementalFolder(), writer)
        } catch (MergingException e) {
            println e.getMessage()
            merger.cleanBlob(getIncrementalFolder())
            throw new ResourceException(e.getMessage(), e)
        }
    }

    @Override
    protected void doIncrementalTaskAction(Map<File, FileStatus> changedInputs) {
        // create a merger and load the known state.
        AssetMerger merger = new AssetMerger()
        try {
            if (!merger.loadFromBlob(getIncrementalFolder(), true /*incrementalState*/)) {
                doFullTaskAction()
                return
            }

            // compare the known state to the current sets to detect incompatibility.
            // This is in case there's a change that's too hard to do incrementally. In this case
            // we'll simply revert to full build.
            List<AssetSet> assetSets = getInputAssetSets()

            if (!merger.checkValidUpdate(assetSets)) {
                project.logger.info("Changed Asset sets: full task run!")
                doFullTaskAction()
                return
            }

            // The incremental process is the following:
            // Loop on all the changed files, find which ResourceSet it belongs to, then ask
            // the resource set to update itself with the new file.
            for (Map.Entry<File, FileStatus> entry : changedInputs.entrySet()) {
                File changedFile = entry.getKey()

                merger.findDataSetContaining(changedFile, fileValidity)
                if (fileValidity.status == FileValidity.FileStatus.UNKNOWN_FILE) {
                    doFullTaskAction()
                    return
                } else if (fileValidity.status == FileValidity.FileStatus.VALID_FILE) {
                    if (!fileValidity.dataSet.updateWith(
                            fileValidity.sourceFile, changedFile, entry.getValue(),
                            plugin.logger)) {
                        project.logger.info(
                                String.format("Failed to process %s event! Full task run",
                                        entry.getValue()))
                        doFullTaskAction()
                        return
                    }
                }
            }

            MergedAssetWriter writer = new MergedAssetWriter(getOutputDir())

            merger.mergeData(writer, false /*doCleanUp*/)

            // No exception? Write the known state.
            merger.writeBlobTo(getIncrementalFolder(), writer)
        } catch (MergingException e) {
            println e.getMessage()
            merger.cleanBlob(getIncrementalFolder())
            throw new ResourceException(e.getMessage(), e)
        }
    }
}
