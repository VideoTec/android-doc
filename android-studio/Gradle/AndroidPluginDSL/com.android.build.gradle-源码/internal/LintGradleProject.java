package com.android.build.gradle.internal;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.BuildTypeContainer;
import com.android.builder.model.Dependencies;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.ProductFlavorContainer;
import com.android.builder.model.SourceProvider;
import com.android.builder.model.Variant;
import com.android.sdklib.AndroidTargetHash;
import com.android.sdklib.AndroidVersion;
import com.android.tools.lint.detector.api.Project;
import com.android.utils.Pair;
import com.android.utils.XmlUtils;

import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.io.File.separatorChar;

/**
 * An implementation of Lint's {@link Project} class wrapping a Gradle model (project or
 * library)
 */
public class LintGradleProject extends Project {
    private LintGradleProject(
            @NonNull LintGradleClient client,
            @NonNull File dir,
            @NonNull File referenceDir,
            @NonNull File manifest) {
        super(client, dir, referenceDir);
        mGradleProject = true;
        mMergeManifests = true;
        mDirectLibraries = Lists.newArrayList();
        readManifest(manifest);
    }

    /**
     * Creates a {@link com.android.build.gradle.internal.LintGradleProject} from
     * the given {@link com.android.builder.model.AndroidProject} definition for
     * a given {@link com.android.builder.model.Variant}, and returns it along with
     * a set of lint custom rule jars applicable for the given model project.
     *
     * @param client the client
     * @param project the model project
     * @param variant the variant
     * @param gradleProject the gradle project
     * @return a pair of new project and list of custom rule jars
     */
    @NonNull
    public static Pair<LintGradleProject, List<File>> create(
            @NonNull LintGradleClient client,
            @NonNull AndroidProject project,
            @NonNull Variant variant,
            @NonNull org.gradle.api.Project gradleProject) {
        File dir = gradleProject.getRootDir();
        AppGradleProject lintProject = new AppGradleProject(client, dir,
                dir, project, variant);

        List<File> customRules = Lists.newArrayList();
        File appLintJar = new File(gradleProject.getBuildDir(),
                "lint" + separatorChar + "lint.jar");
        if (appLintJar.exists()) {
            customRules.add(appLintJar);
        }

        Set<AndroidLibrary> libraries = Sets.newHashSet();
        Dependencies dependencies = variant.getMainArtifact().getDependencies();
        for (AndroidLibrary library : dependencies.getLibraries()) {
            lintProject.addDirectLibrary(createLibrary(client, library, libraries, customRules));
        }

        return Pair.<LintGradleProject,List<File>>of(lintProject, customRules);
    }


    @Override
    protected void initialize() {
        // Deliberately not calling super; that code is for ADT compatibility
    }

    protected void readManifest(File manifest) {
        if (manifest.exists()) {
            try {
                String xml = Files.toString(manifest, Charsets.UTF_8);
                Document document = XmlUtils.parseDocumentSilently(xml, true);
                readManifest(document);
            } catch (IOException e) {
                mClient.log(e, "Could not read manifest %1$s", manifest);
            }
        }
    }

    @Override
    public boolean isGradleProject() {
        return true;
    }

    void addDirectLibrary(@NonNull Project project) {
        mDirectLibraries.add(project);
    }

    @NonNull
    private static LibraryProject createLibrary(@NonNull LintGradleClient client,
            @NonNull AndroidLibrary library,
            @NonNull Set<AndroidLibrary> seen, List<File> customRules) {
        seen.add(library);
        File dir = library.getFolder();
        LibraryProject project = new LibraryProject(client, dir, dir, library);

        File ruleJar = library.getLintJar();
        if (ruleJar.exists()) {
            customRules.add(ruleJar);
        }

        for (AndroidLibrary dependent : library.getLibraryDependencies()) {
            if (!seen.contains(dependent)) {
                project.addDirectLibrary(createLibrary(client, dependent, seen, customRules));
            }
        }

        return project;
    }

    private static class AppGradleProject extends LintGradleProject {
        private AndroidProject mProject;
        private Variant mVariant;
        private List<SourceProvider> mProviders;

        private AppGradleProject(
                @NonNull LintGradleClient client,
                @NonNull File dir,
                @NonNull File referenceDir,
                @NonNull AndroidProject project,
                @NonNull Variant variant) {
            super(client, dir, referenceDir, variant.getMainArtifact().getGeneratedManifest());

            mProject = project;
            mVariant = variant;
        }

        @Override
        public boolean isLibrary() {
            return mProject.isLibrary();
        }

        @Override
        public AndroidProject getGradleProjectModel() {
            return mProject;
        }

        @Override
        public Variant getCurrentVariant() {
            return mVariant;
        }

        private List<SourceProvider> getSourceProviders() {
            if (mProviders == null) {
                List<SourceProvider> providers = Lists.newArrayList();
                AndroidArtifact mainArtifact = mVariant.getMainArtifact();

                SourceProvider defaultProvider = mProject.getDefaultConfig().getSourceProvider();
                if (defaultProvider != null) {
                    providers.add(defaultProvider);
                }

                for (String flavorName : mVariant.getProductFlavors()) {
                    for (ProductFlavorContainer flavor : mProject.getProductFlavors()) {
                        if (flavorName.equals(flavor.getProductFlavor().getName())) {
                            SourceProvider provider = flavor.getSourceProvider();
                            if (provider != null) {
                                providers.add(provider);
                            }
                            break;
                        }
                    }
                }

                SourceProvider multiProvider = mainArtifact.getMultiFlavorSourceProvider();
                if (multiProvider != null) {
                    providers.add(multiProvider);
                }

                String buildTypeName = mVariant.getBuildType();
                for (BuildTypeContainer buildType : mProject.getBuildTypes()) {
                    if (buildTypeName.equals(buildType.getBuildType().getName())) {
                        SourceProvider provider = buildType.getSourceProvider();
                        if (provider != null) {
                            providers.add(provider);
                        }
                        break;
                    }
                }

                SourceProvider variantProvider =  mainArtifact.getVariantSourceProvider();
                if (variantProvider != null) {
                    providers.add(variantProvider);
                }

                mProviders = providers;
            }

            return mProviders;
        }

        @NonNull
        @Override
        public List<File> getManifestFiles() {
            if (mManifestFiles == null) {
                mManifestFiles = Lists.newArrayList();
                for (SourceProvider provider : getSourceProviders()) {
                    File manifestFile = provider.getManifestFile();
                    if (manifestFile.exists()) { // model returns path whether or not it exists
                        mManifestFiles.add(manifestFile);
                    }
                }
            }

            return mManifestFiles;
        }

        @Override
        public List<File> getProguardFiles() {
            if (mProguardFiles == null) {
                ProductFlavor flavor = mProject.getDefaultConfig().getProductFlavor();
                mProguardFiles = Lists.newArrayList();
                mProguardFiles.addAll(flavor.getProguardFiles());
                // TODO: This is currently broken:
                //mProguardFiles.addAll(flavor.getConsumerProguardFiles());
                // It will throw
                //org.gradle.tooling.model.UnsupportedMethodException: Unsupported method: BaseConfig.getConsumerProguardFiles().
                //  The version of Gradle you connect to does not support that method.
                //  To resolve the problem you can change/upgrade the target version of Gradle you connect to.
                //  Alternatively, you can ignore this exception and read other information from the model.
                //at org.gradle.tooling.model.internal.Exceptions.unsupportedMethod(Exceptions.java:33)
                //at org.gradle.tooling.internal.adapter.ProtocolToModelAdapter$InvocationHandlerImpl.invoke(ProtocolToModelAdapter.java:239)
                //at com.sun.proxy.$Proxy129.getConsumerProguardFiles(Unknown Source)
            }

            return mProguardFiles;
        }

        @NonNull
        @Override
        public List<File> getResourceFolders() {
            if (mResourceFolders == null) {
                mResourceFolders = Lists.newArrayList();
                for (SourceProvider provider : getSourceProviders()) {
                    Collection<File> resDirs = provider.getResDirectories();
                    for (File res : resDirs) {
                        if (res.exists()) { // model returns path whether or not it exists
                            mResourceFolders.add(res);
                        }
                    }
                }
            }

            return mResourceFolders;
        }

        @NonNull
        @Override
        public List<File> getJavaSourceFolders() {
            if (mJavaSourceFolders == null) {
                mJavaSourceFolders = Lists.newArrayList();
                for (SourceProvider provider : getSourceProviders()) {
                    Collection<File> resDirs = provider.getJavaDirectories();
                    for (File res : resDirs) {
                        if (res.exists()) { // model returns path whether or not it exists
                            mJavaSourceFolders.add(res);
                        }
                    }
                }
            }

            return mJavaSourceFolders;
        }

        @NonNull
        @Override
        public List<File> getJavaClassFolders() {
            if (mJavaClassFolders == null) {
                mJavaClassFolders = new ArrayList<File>(1);
                File outputClassFolder = mVariant.getMainArtifact().getClassesFolder();
                if (outputClassFolder != null && outputClassFolder.exists()) {
                    mJavaClassFolders.add(outputClassFolder);
                }
            }

            return mJavaClassFolders;
        }

        @Override
        public List<File> getJavaLibraries() {
            if (mJavaLibraries == null) {
                Collection<File> jars = mVariant.getMainArtifact().getDependencies().getJars();
                mJavaLibraries = Lists.newArrayListWithExpectedSize(jars.size());
                for (File jar : jars) {
                    if (jar.exists()) {
                        mJavaLibraries.add(jar);
                    }
                }
            }
            return mJavaLibraries;
        }

        @Nullable
        @Override
        public String getPackage() {
            // For now, lint only needs the manifest package; not the potentially variant specific
            // package. As part of the Gradle work on the Lint API we should make two separate
            // package lookup methods -- one for the manifest package, one for the build package
            if (mPackage == null) { // only used as a fallback in case manifest somehow is null
                String packageName = mProject.getDefaultConfig().getProductFlavor().getPackageName();
                if (packageName != null) {
                    return packageName;
                }
            }

            return mPackage; // from manifest
        }

        @Override
        public int getMinSdk() {
            int minSdk = mProject.getDefaultConfig().getProductFlavor().getMinSdkVersion();
            if (minSdk != -1) {
                return minSdk;
            }

            return mMinSdk; // from manifest
        }

        @Override
        public int getTargetSdk() {
            int targetSdk = mProject.getDefaultConfig().getProductFlavor().getTargetSdkVersion();
            if (targetSdk != -1) {
                return targetSdk;
            }

            return targetSdk; // from manifest
        }

        @Override
        public int getBuildSdk() {
            String compileTarget = mProject.getCompileTarget();
            if (compileTarget != null) {
                AndroidVersion version = AndroidTargetHash.getPlatformVersion(compileTarget);
                if (version != null) {
                    return version.getApiLevel();
                }
            }

            return super.getBuildSdk();
        }
    }

    private static class LibraryProject extends LintGradleProject {
        private AndroidLibrary mLibrary;

        private LibraryProject(
                @NonNull LintGradleClient client,
                @NonNull File dir,
                @NonNull File referenceDir,
                @NonNull AndroidLibrary library) {
            super(client, dir, referenceDir, library.getManifest());
            mLibrary = library;

            // TODO: Make sure we don't use this project for any source library projects!
            mReportIssues = false;
        }

        @Override
        public boolean isLibrary() {
            return true;
        }

        @Override
        public AndroidLibrary getGradleLibraryModel() {
            return mLibrary;
        }

        @Override
        public Variant getCurrentVariant() {
            return null;
        }

        @NonNull
        @Override
        public List<File> getManifestFiles() {
            if (mManifestFiles == null) {
                File manifest = mLibrary.getManifest();
                if (manifest.exists()) {
                    mManifestFiles = Collections.singletonList(manifest);
                } else {
                    mManifestFiles = Collections.emptyList();
                }
            }

            return mManifestFiles;
        }

        @NonNull
        @Override
        public List<File> getResourceFolders() {
            if (mResourceFolders == null) {
                File folder = mLibrary.getResFolder();
                if (folder.exists()) {
                    mResourceFolders = Collections.singletonList(folder);
                } else {
                    mResourceFolders = Collections.emptyList();
                }
            }

            return mResourceFolders;
        }

        @NonNull
        @Override
        public List<File> getJavaSourceFolders() {
            return Collections.emptyList();
        }

        @NonNull
        @Override
        public List<File> getJavaClassFolders() {
            return Collections.emptyList();
        }

        @NonNull
        @Override
        public List<File> getJavaLibraries() {
            if (mJavaLibraries == null) {
                File jarFile = mLibrary.getJarFile();
                if (jarFile.exists()) {
                    mJavaLibraries = Collections.singletonList(jarFile);
                } else {
                    mJavaLibraries = Collections.emptyList();
                }
            }

            return mJavaLibraries;
        }
    }
}
