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


package com.android.builder.internal.compiler;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.internal.CommandLineRunner;
import com.android.ide.common.internal.LoggedErrorException;
import com.android.ide.common.internal.WaitableExecutor;
import com.android.sdklib.BuildToolInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.android.SdkConstants.EXT_BC;
import static com.android.SdkConstants.FN_RENDERSCRIPT_V8_JAR;

/**
 * Compiles Renderscript files.
 */
public class RenderScriptProcessor {

    // ABI list, as pairs of (android-ABI, toolchain-ABI)
    private static final class Abi {

        @NonNull
        private final String mDevice;
        @NonNull
        private final String mToolchain;
        @NonNull
        private final BuildToolInfo.PathId mLinker;
        @NonNull
        private final String[] mLinkerArgs;

        Abi(@NonNull String device,
            @NonNull String toolchain,
            @NonNull BuildToolInfo.PathId linker,
            @NonNull String... linkerArgs) {

            mDevice = device;
            mToolchain = toolchain;
            mLinker = linker;
            mLinkerArgs = linkerArgs;
        }
    }

    private static final Abi[] ABIS = {
            new Abi("armeabi-v7a", "armv7-none-linux-gnueabi", BuildToolInfo.PathId.LD_ARM,
                    "-dynamic-linker", "/system/bin/linker", "-X", "-m", "armelf_linux_eabi"),
            new Abi("mips", "mipsel-unknown-linux", BuildToolInfo.PathId.LD_MIPS, "-EL"),
            new Abi("x86", "i686-unknown-linux", BuildToolInfo.PathId.LD_X86, "-m", "elf_i386") };

    public static final String RS_DEPS = "rsDeps";

    @NonNull
    private final List<File> mSourceFolders;

    @NonNull
    private final List<File> mImportFolders;

    @NonNull
    private final File mSourceOutputDir;

    @NonNull
    private final File mResOutputDir;

    @NonNull
    private final File mObjOutputDir;

    @NonNull
    private final File mLibOutputDir;

    @NonNull
    private final BuildToolInfo mBuildToolInfo;

    private final int mTargetApi;

    private final boolean mDebugBuild;

    private final int mOptimLevel;

    private final boolean mNdkMode;

    private final boolean mSupportMode;
    private final Set<String> mAbiFilters;

    private final File mRsLib;
    private final File mLibClCore;

    public RenderScriptProcessor(
            @NonNull List<File> sourceFolders,
            @NonNull List<File> importFolders,
            @NonNull File sourceOutputDir,
            @NonNull File resOutputDir,
            @NonNull File objOutputDir,
            @NonNull File libOutputDir,
            @NonNull BuildToolInfo buildToolInfo,
            int targetApi,
            boolean debugBuild,
            int optimLevel,
            boolean ndkMode,
            boolean supportMode,
            @Nullable Set<String> abiFilters) {
        mSourceFolders = sourceFolders;
        mImportFolders = importFolders;
        mSourceOutputDir = sourceOutputDir;
        mResOutputDir = resOutputDir;
        mObjOutputDir = objOutputDir;
        mLibOutputDir = libOutputDir;
        mBuildToolInfo = buildToolInfo;
        mTargetApi = targetApi;
        mDebugBuild = debugBuild;
        mOptimLevel = optimLevel;
        mNdkMode = ndkMode;
        mSupportMode = supportMode;
        mAbiFilters = abiFilters;

        if (supportMode) {
            File rs = new File(mBuildToolInfo.getLocation(), "renderscript");
            mRsLib = new File(rs, "lib");
            mLibClCore = new File(mRsLib, "libclcore.bc");
        } else {
            mLibClCore = null;
            mRsLib = null;
        }
    }

    public static File getSupportJar(String buildToolsFolder) {
        return new File(buildToolsFolder, "renderscript/lib/" + FN_RENDERSCRIPT_V8_JAR);
    }

    public static File getSupportNativeLibFolder(String buildToolsFolder) {
        File rs = new File(buildToolsFolder, "renderscript");
        File lib = new File(rs, "lib");
        return new File(lib, "packaged");
    }

    public void build(@NonNull CommandLineRunner launcher)
            throws IOException, InterruptedException, LoggedErrorException {

        // gather the files to compile
        FileGatherer fileGatherer = new FileGatherer();
        SourceSearcher searcher = new SourceSearcher(mSourceFolders, "rs", "fs");
        searcher.setUseExecutor(false);
        searcher.search(fileGatherer);

        List<File> renderscriptFiles = fileGatherer.getFiles();

        if (renderscriptFiles.isEmpty()) {
            return;
        }

        // get the env var
        Map<String, String> env = Maps.newHashMap();
        if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_DARWIN) {
            env.put("DYLD_LIBRARY_PATH", mBuildToolInfo.getLocation().getAbsolutePath());
        } else if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_LINUX) {
            env.put("LD_LIBRARY_PATH", mBuildToolInfo.getLocation().getAbsolutePath());
        }

        doMainCompilation(renderscriptFiles, launcher, env);

        if (mSupportMode) {
            createSupportFiles(launcher, env);
        }
    }

    private void doMainCompilation(
            @NonNull List<File> inputFiles,
            @NonNull CommandLineRunner launcher,
            @NonNull Map<String, String> env)
            throws IOException, InterruptedException, LoggedErrorException {

        String renderscript = mBuildToolInfo.getPath(BuildToolInfo.PathId.LLVM_RS_CC);
        if (renderscript == null || !new File(renderscript).isFile()) {
            throw new IllegalStateException(BuildToolInfo.PathId.LLVM_RS_CC + " is missing");
        }

        String rsPath = mBuildToolInfo.getPath(BuildToolInfo.PathId.ANDROID_RS);
        String rsClangPath = mBuildToolInfo.getPath(BuildToolInfo.PathId.ANDROID_RS_CLANG);

        // the renderscript compiler doesn't expect the top res folder,
        // but the raw folder directly.
        File rawFolder = new File(mResOutputDir, SdkConstants.FD_RES_RAW);

        // compile all the files in a single pass
        ArrayList<String> command = Lists.newArrayListWithExpectedSize(26);

        command.add(renderscript);

        // Due to a device side bug, let's not enable this at this time.
//        if (mDebugBuild) {
//            command.add("-g");
//        }

        command.add("-O");
        command.add(Integer.toString(mOptimLevel));

        // add all import paths
        command.add("-I");
        command.add(rsPath);
        command.add("-I");
        command.add(rsClangPath);

        for (File importPath : mImportFolders) {
            if (importPath.isDirectory()) {
                command.add("-I");
                command.add(importPath.getAbsolutePath());
            }
        }

        if (mSupportMode) {
            command.add("-rs-package-name=android.support.v8.renderscript");
        }

        // source output
        command.add("-p");
        command.add(mSourceOutputDir.getAbsolutePath());

        if (mNdkMode) {
            command.add("-reflect-c++");
        }

        // res output
        command.add("-o");
        command.add(rawFolder.getAbsolutePath());

        command.add("-target-api");
        int targetApi = mTargetApi < 11 ? 11 : mTargetApi;
        targetApi = (mSupportMode && targetApi < 18) ? 18 : targetApi;
        command.add(Integer.toString(targetApi));

        // input files
        for (File sourceFile : inputFiles) {
            command.add(sourceFile.getAbsolutePath());
        }

        launcher.runCmdLine(command, env);
    }

    private void createSupportFiles(@NonNull final CommandLineRunner launcher,
            @NonNull final Map<String, String> env)
            throws IOException, InterruptedException, LoggedErrorException {
        // get the generated BC files.
        File rawFolder = new File(mResOutputDir, SdkConstants.FD_RES_RAW);

        SourceSearcher searcher = new SourceSearcher(
                Collections.singletonList(rawFolder), EXT_BC);
        FileGatherer fileGatherer = new FileGatherer();
        searcher.search(fileGatherer);

        WaitableExecutor<Void> mExecutor  = new WaitableExecutor<Void>();

        for (final File bcFile : fileGatherer.getFiles()) {
            String name = bcFile.getName();
            final String objName = name.replaceAll("\\.bc", ".o");
            final String soName = "librs." + name.replaceAll("\\.bc", ".so");

            for (final Abi abi : ABIS) {
                if (mAbiFilters != null && !mAbiFilters.contains(abi.mDevice)) {
                    continue;
                }

                // make sure the dest folders exist
                final File objAbiFolder = new File(mObjOutputDir, abi.mDevice);
                if (!objAbiFolder.isDirectory() && !objAbiFolder.mkdirs()) {
                    throw new IOException("Unable to create dir " + objAbiFolder.getAbsolutePath());
                }

                final File libAbiFolder = new File(mLibOutputDir, abi.mDevice);
                if (!libAbiFolder.isDirectory() && !libAbiFolder.mkdirs()) {
                    throw new IOException("Unable to create dir " + libAbiFolder.getAbsolutePath());
                }

                mExecutor.execute(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        File objFile = createSupportObjFile(bcFile, abi, objName, objAbiFolder,
                                launcher, env);
                        createSupportLibFile(objFile, abi, soName, libAbiFolder, launcher, env);
                        return null;
                    }
                });
            }
        }

        mExecutor.waitForTasksWithQuickFail(true /*cancelRemaining*/);
    }

    private File createSupportObjFile(
            @NonNull File bcFile,
            @NonNull Abi abi,
            @NonNull String objName,
            @NonNull File objAbiFolder,
            @NonNull CommandLineRunner launcher,
            @NonNull Map<String, String> env)
            throws IOException, InterruptedException, LoggedErrorException {

        List<String> args = Lists.newArrayListWithExpectedSize(10);

        args.add(mBuildToolInfo.getPath(BuildToolInfo.PathId.BCC_COMPAT));

        args.add("-O" + Integer.toString(mOptimLevel));

        File outFile = new File(objAbiFolder, objName);
        args.add("-o");
        args.add(outFile.getAbsolutePath());

        args.add("-fPIC");
        args.add("-shared");

        args.add("-rt-path");
        args.add(mLibClCore.getAbsolutePath());

        args.add("-mtriple");
        args.add(abi.mToolchain);

        args.add(bcFile.getAbsolutePath());

        launcher.runCmdLine(args, env);

        return outFile;
    }

    private void createSupportLibFile(
            @NonNull File objFile,
            @NonNull Abi abi,
            @NonNull String soName,
            @NonNull File libAbiFolder,
            @NonNull CommandLineRunner launcher,
            @NonNull Map<String, String> env)
            throws IOException, InterruptedException, LoggedErrorException {

        File intermediatesFolder = new File(mRsLib, "intermediates");
        File intermediatesAbiFolder = new File(intermediatesFolder, abi.mDevice);
        File packagedFolder = new File(mRsLib, "packaged");
        File packagedAbiFolder = new File(packagedFolder, abi.mDevice);

        List<String> args = Lists.newArrayListWithExpectedSize(26);

        args.add(mBuildToolInfo.getPath(abi.mLinker));

        args.add("--eh-frame-hdr");
        Collections.addAll(args, abi.mLinkerArgs);
        args.add("-shared");
        args.add("-Bsymbolic");
        args.add("-z");
        args.add("noexecstack");
        args.add("-z");
        args.add("relro");
        args.add("-z");
        args.add("now");

        File outFile = new File(libAbiFolder, soName);
        args.add("-o");
        args.add(outFile.getAbsolutePath());

        args.add("-L" + intermediatesAbiFolder.getAbsolutePath());
        args.add("-L" + packagedAbiFolder.getAbsolutePath());

        args.add("-soname");
        args.add(soName);

        args.add(objFile.getAbsolutePath());
        args.add(new File(intermediatesAbiFolder, "libcompiler_rt.a").getAbsolutePath());

        args.add("-lRSSupport");
        args.add("-lm");
        args.add("-lc");

        launcher.runCmdLine(args, env);
    }
}
