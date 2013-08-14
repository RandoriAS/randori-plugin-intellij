/*
 * Copyright 2013 original Randori IntelliJ Plugin authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package randori.plugin.roots;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import randori.compiler.bundle.*;
import randori.compiler.bundle.io.StAXManifestReader;
import randori.plugin.configuration.RandoriCompilerModel;
import randori.plugin.ui.icons.RandoriIcons;
import randori.plugin.util.LogUtils;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Michael Schmalle
 * @author Roland Zwaga
 * @author Frédéric THOMAS
 */
public class RandoriSdkType extends SdkType {
    private static final Logger logger = Logger.getInstance(RandoriSdkType.class);
    private static final String LAST_SELECTED_RANDORI_SDK_HOME_KEY = "last.selected.randori.sdk.home";

    private IBundleVersion sdkVersion;
    private IBundle sdkBundle;

    public RandoriSdkType() {
        super("Randori SDK");
    }

    public static RandoriSdkType getInstance() {
        return findInstance(RandoriSdkType.class);
    }

    public static void copySdkLibraries(Project project, String webModulePath) {
        // only copy the SDK js libraries if the SWCs exist in the SDK

        final VirtualFile baseDir = VirtualFileManager.getInstance().findFileByUrl(webModulePath);
        final Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();

        if (sdk == null)
            throw new RuntimeException("Tried to copy libraries but SDK was null");

        RandoriSdkType randoriSdk = (RandoriSdkType) sdk.getSdkType();

        VirtualFile sdkRoot = sdk.getHomeDirectory();
        if (sdkRoot == null || !sdkRoot.exists())
            throw new RuntimeException(
                    "SDK home directory not found, please check your Project and Module SDK settings.");

        if (randoriSdk.sdkBundle == null) {
            randoriSdk.sdkBundle = randoriSdk.getBundle(sdkRoot);
        }

        IBundle sdkBundle = randoriSdk.sdkBundle;
        if (sdkBundle == null) {
            throw new RuntimeException("SDK bundle is invalid.");
        }

        final RandoriCompilerModel state = RandoriCompilerModel.getInstance(project).getState();
        String libPath = null;
        if (state != null)
            libPath = state.getLibraryPath();

        if (baseDir != null && libPath != null) {
            VirtualFile libraryDir = baseDir.findFileByRelativePath(libPath);
            if (libraryDir == null) {
                logger.debug("The library path '" + libPath + "' doesn't exist, creating it now.");
                //noinspection ResultOfMethodCallIgnored
                new File(baseDir.getPath(), libPath).mkdirs();
                baseDir.refresh(false, true);
                libraryDir = baseDir.findFileByRelativePath(libPath);
            }
            copyJSFilesFromBundle(libraryDir, sdkRoot, sdkBundle);
            logger.debug("Finished copying JS files from SDK to library path: " + libPath);

            // says this should only be called in a writeAction
            baseDir.refresh(true, true);
        }
    }

    private static void copyJSFilesFromBundle(VirtualFile destinationDir, VirtualFile sdkRoot, IBundle sdkBundle) {
        Collection<IBundleLibrary> libraries = sdkBundle.getLibraries();
        for (IBundleLibrary library : libraries) {
            copyJSFilesFromLibrary(destinationDir, sdkRoot, library);
        }
    }

    private static void copyJSFilesFromLibrary(VirtualFile destinationDir, VirtualFile sdkRoot, IBundleLibrary library) {

        IBundleContainer container = library.getContainer(IBundleContainer.Type.JS);
        if (container != null) {
            copyJSFilesFromContainer(destinationDir, sdkRoot, container);
        }
    }

    private static void copyJSFilesFromContainer(VirtualFile destinationDir, VirtualFile sdkRoot,
                                                 IBundleContainer container) {
        IBundleCategory category = container.getCategory(IBundleCategory.Type.MONOLITHIC);
        if (category != null) {
            copyJSFilesFromCategory(destinationDir, sdkRoot, category);
        }
    }

    private static void copyJSFilesFromCategory(VirtualFile destinationDir, VirtualFile sdkRoot,
                                                IBundleCategory category) {
        Collection<IBundleEntry> entries = category.getEntries();
        for (IBundleEntry entry : entries) {
            copyJSEntry(destinationDir, sdkRoot, entry);
        }
    }

    private static void copyJSEntry(VirtualFile destinationDir, VirtualFile sdkRoot, IBundleEntry entry) {
        File sourceFile = new File(sdkRoot.getPath() + File.separator + entry.getPath());
        File destinationFile = new File(destinationDir.getPath() + File.separator + sourceFile.getName());
        try {
            FileUtil.copy(sourceFile, destinationFile);
            logger.debug("Copied JS file " + sourceFile.getName() + " to library path " + destinationDir.getPath());
        } catch (IOException e) {
            String stackTrace = LogUtils.dumpStackTrace(Thread.currentThread().getStackTrace());
            logger.error("Error copying '" + sourceFile.getAbsolutePath() + "' to '" + destinationDir.getPath()
                    + "':\n" + e.getMessage() + ":\n" + stackTrace);
            e.printStackTrace();
        }
    }

    public Sdk createSdk(@NotNull String sdkName, @NotNull String home) {
        ProjectJdkImpl sdk = new ProjectJdkImpl(sdkName, this, home.replace(File.separatorChar, '/'), null);
        setupSdkPaths(sdk);
        return sdk;
    }

    // called by SdkType.setupSdkPaths()
    @Override
    public void setupSdkPaths(Sdk sdk) {
        VirtualFile sdkRoot = sdk.getHomeDirectory();

        // check whether the root selected by the user still exists
        if (sdkRoot == null || !sdkRoot.isValid()) {
            logger.debug("SDK paths setup failed");
            return;
        }
        sdkRoot.refresh(false, true);

        PropertiesComponent.getInstance().setValue(LAST_SELECTED_RANDORI_SDK_HOME_KEY, sdkRoot.getPath());

        if (sdkBundle == null) {
            sdkBundle = getBundle(sdkRoot);
        }

        if ((sdkVersion == null) && (sdkBundle != null)) {
            sdkVersion = sdkBundle.getVersion();
        }

        if (sdkBundle != null) {
            loadSDKLibraries(sdk, sdkBundle);
            logger.debug("SDK paths setup successfully");
        } else
            logger.debug("SDK paths setup failed");
    }

    @Override
    public String suggestHomePath() {
        VirtualFile userHomeDir = VfsUtil.getUserHomeDir();
        String path = PropertiesComponent.getInstance().getValue(LAST_SELECTED_RANDORI_SDK_HOME_KEY);
        return (path != null) ? PathUtil.getParentPath(path) : ((userHomeDir != null) ? userHomeDir.getPath() : null);
    }

    // called when the directory is selected when creating a new SDK
    // 'path' is the selected directory from the file chooser
    @Override
    public boolean isValidSdkHome(String path) {
        return getVersionString(path) != null;
    }

    @Override
    public String suggestSdkName(String currentSdkName, String sdkHome) {
        return "Randori SDK " + getVersionString(sdkHome);
    }

    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(SdkModel sdkModel, SdkModificator sdkModificator) {
        return null;
    }

    @Override
    public void saveAdditionalData(SdkAdditionalData additionalData, org.jdom.Element additional) {
    }

    @Override
    public Icon getIconForAddAction() {
        return RandoriIcons.Randori16;
    }

    @Override
    public Icon getIcon() {
        return RandoriIcons.Randori16;
    }

    @Override
    public String getVersionString(String sdkHome) {
        if (sdkBundle == null) {
            VirtualFile sdkRoot = VfsUtil.findRelativeFile(sdkHome, null);
            if (sdkRoot == null) {
                logger.error("SDK path does not exist: " + sdkHome);
                return null;
            }
            sdkBundle = getBundle(sdkRoot);
        }
        if ((sdkVersion == null) && (sdkBundle != null)) {
            sdkVersion = sdkBundle.getVersion();
        }
        return (sdkVersion != null) ? sdkVersion.getRandoriVersion() : null;
    }

    @Override
    public String getPresentableName() {
        return "Randori SDK";
    }

    @Override
    public boolean isRootTypeApplicable(OrderRootType type) {
        // called after an sdk has been chosen and when a project is starting up OR the IDE is starting with ProjectStructure
        return type == OrderRootType.CLASSES || type == OrderRootType.SOURCES
                || type == JavadocOrderRootType.getInstance();
    }

    private void loadSDKLibraries(Sdk sdk, IBundle bundle) {
        // getHomeDirectory() is what the user selected in the file chooser, the root
        VirtualFile sdkRoot = sdk.getHomeDirectory();

        SdkModificator modificator = sdk.getSdkModificator();

        List<VirtualFile> libraries = getSWCs(bundle, sdkRoot);

        for (VirtualFile library : libraries) {
            addSWC(modificator, library);
        }

        modificator.commitChanges();
    }

    private List<VirtualFile> getSWCs(IBundle bundle, VirtualFile sdkRoot) {
        ArrayList<VirtualFile> result = new ArrayList<VirtualFile>();
        Collection<IBundleLibrary> libraries = bundle.getLibraries();
        for (IBundleLibrary library : libraries) {
            result.addAll(getSWCPathsFromBundleLibrary(library, sdkRoot));
        }
        return result;
    }

    private Collection<VirtualFile> getSWCPathsFromBundleLibrary(IBundleLibrary bundleLibrary, VirtualFile sdkRoot) {
        ArrayList<VirtualFile> result = new ArrayList<VirtualFile>();
        IBundleContainer container = bundleLibrary.getContainer(IBundleContainer.Type.BIN);
        if (container != null) {
            getEntriesFromCategories(sdkRoot, result, container);
        }
        return result;
    }

    private void getEntriesFromCategories(VirtualFile sdkRoot, ArrayList<VirtualFile> result, IBundleContainer container) {
        IBundleCategory category = container.getCategory(IBundleCategory.Type.SWC);
        if (category != null) {
            getSWCEntries(sdkRoot, result, category);
        }
    }

    private void getSWCEntries(VirtualFile sdkRoot, ArrayList<VirtualFile> result, IBundleCategory category) {
        Collection<IBundleEntry> entries = category.getEntries();
        for (IBundleEntry entry : entries) {
            result.add(getSWC(sdkRoot, entry.getPath()));
        }
    }

    private VirtualFile getSWC(VirtualFile root, String relativePath) {
        VirtualFile swc = root.findFileByRelativePath(relativePath);
        if (swc == null || !swc.exists())
            throw new RuntimeException("The SWC " + relativePath + " does not exist");
        return swc;
    }

    private void addSWC(SdkModificator modificator, VirtualFile swc) {
        final VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(swc);
        if (jarRoot != null) {
            modificator.addRoot(jarRoot, OrderRootType.CLASSES);
        }
    }

    IBundle getBundle(VirtualFile sdkRoot) {
        VirtualFile manifestFile = sdkRoot.findFileByRelativePath("manifest.xml");
        if (manifestFile == null) {
            logger.error("manifest.xml does not exist in SDK path:" + sdkRoot.getPath());
            return null;
        }
        IMutableBundle bundle = new Bundle(null);
        populateBundle(manifestFile, bundle);
        return bundle;
    }

    private void populateBundle(VirtualFile manifestFile, IMutableBundle bundle) {
        StAXManifestReader manifestReader = null;
        try {
            manifestReader = new StAXManifestReader(new BufferedInputStream(manifestFile.getInputStream()), bundle);
            manifestReader.parse();
        } catch (IOException e) {
            logger.error(LogUtils.dumpStackTrace(Thread.currentThread().getStackTrace()));
            e.printStackTrace();
        } catch (XMLStreamException e) {
            logger.error(LogUtils.dumpStackTrace(Thread.currentThread().getStackTrace()));
            e.printStackTrace();
        } finally {
            if (manifestReader != null) {
                try {
                    manifestReader.close();
                } catch (IOException e) {
                    logger.error(LogUtils.dumpStackTrace(Thread.currentThread().getStackTrace()));
                    e.printStackTrace();
                }
            }
        }
    }
}
