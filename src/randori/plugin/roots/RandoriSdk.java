/***
 * Copyright 2013 Teoti Graphix, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * @author Michael Schmalle <mschmalle@teotigraphix.com>
 */

package randori.plugin.roots;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;

import randori.compiler.bundle.*;
import randori.compiler.bundle.io.StAXManifestReader;
import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.icons.RandoriIcons;
import randori.plugin.module.RandoriModuleType;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Michael Schmalle
 * @author Roland Zwaga
 */
public class RandoriSdk extends SdkType
{
    private final List<String> libraries = new ArrayList<String>();
    private static final String RANDORI = "randori-framework";
    private static final String RANDORI_GUICE = "randori-guice-framework";
    private IBundleVersion sdkVersion;
    private IBundle sdkBundle;

    public RandoriSdk()
    {
        super("Randori SDK");
    }

    public static SdkType getInstance()
    {
        return findInstance(RandoriSdk.class);
    }

    // called by SdkType.setupSdkPaths()
    @Override
    public void setupSdkPaths(Sdk sdk)
    {
        VirtualFile sdkRoot = sdk.getHomeDirectory();

        // check whether the root selected by the user still exists
        if (sdkRoot == null || !sdkRoot.isValid())
            return;

        // TODO do we need to check the version?, how would we?
        if (sdkBundle == null)
        {
            sdkBundle = getBundle(sdkRoot);
        }
        if (sdkVersion == null)
        {
            sdkVersion = sdkBundle.getVersion();
        }

        loadSDKLibraries(sdk, sdkBundle);

        // setup project SDK and Module SDKs
        setupDependentSdks(sdk);
    }

    @Override
    public String suggestHomePath()
    {
        return VfsUtil.getUserHomeDir().getPath();
    }

    // called when the directory is selected when creating a new SDK
    // 'path' is the selected directory from the file chooser
    @Override
    public boolean isValidSdkHome(String path)
    {
        return getVersionString(path) != null;
    }

    @Override
    public String suggestSdkName(String currentSdkName, String sdkHome)
    {
        return "Randori SDK " + getVersionString(sdkHome);
    }

    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(
            SdkModel sdkModel, SdkModificator sdkModificator)
    {
        return null;
    }

    @Override
    public void saveAdditionalData(SdkAdditionalData additionalData,
            org.jdom.Element additional)
    {
    }

    @Override
    public Icon getIconForAddAction()
    {
        return RandoriIcons.Randori16;
    }

    @Override
    public Icon getIcon()
    {
        return RandoriIcons.Randori16;
    }

    @Override
    public String getVersionString(String sdkHome)
    {
        if (sdkBundle == null)
        {
            VirtualFile sdkRoot = null;
            sdkRoot = VfsUtil.findRelativeFile(sdkHome, null);
            sdkBundle = getBundle(sdkRoot);
        }
        if (sdkVersion == null)
        {
            sdkVersion = sdkBundle.getVersion();
        }
        return sdkVersion.getRandoriVersion().toString();
    }

    @Override
    public String getPresentableName()
    {
        return "Randori SDK";
    }

    @Override
    public boolean isRootTypeApplicable(OrderRootType type)
    {
        // called after an sdk has been choosen and when a project is starting up OR the IDE is starting with ProjectStructure
        return type == OrderRootType.CLASSES || type == OrderRootType.SOURCES
                || type == JavadocOrderRootType.getInstance();
    }

    private void loadSDKLibraries(Sdk sdk, IBundle bundle)
    {
        // getHomeDirectory() is what the user selected in the file chooser, the root
        VirtualFile sdkRoot = sdk.getHomeDirectory();

        SdkModificator modificator = sdk.getSdkModificator();

        List<VirtualFile> libraries = getSWCs(bundle, sdkRoot);

        for (VirtualFile library : libraries)
        {
            addSWC(modificator, library);
        }

        modificator.commitChanges();
    }

    private List<VirtualFile> getSWCs(IBundle bundle, VirtualFile sdkRoot)
    {
        ArrayList<VirtualFile> result = new ArrayList<VirtualFile>();
        Collection<IBundleLibrary> libraries = bundle.getLibraries();
        for(IBundleLibrary library : libraries)
        {
            result.addAll(getSWCPathsFromBundleLibrary(library, sdkRoot));
        }
        return result;
    }

    private Collection<VirtualFile> getSWCPathsFromBundleLibrary(IBundleLibrary bundleLibrary, VirtualFile sdkRoot) {
        ArrayList<VirtualFile> result = new ArrayList<VirtualFile>();
        IBundleContainer container = bundleLibrary.getContainer(IBundleContainer.Type.BIN);
        if (container != null)
        {
            getEntriesFromCategories(sdkRoot, result, container);
        }
        return result;
    }

    private void getEntriesFromCategories(VirtualFile sdkRoot, ArrayList<VirtualFile> result, IBundleContainer container) {
        IBundleCategory category = container.getCategory(IBundleCategory.Type.SWC);
        if (category != null)
        {
            getSWCEntries(sdkRoot, result, category);
        }
    }

    private void getSWCEntries(VirtualFile sdkRoot, ArrayList<VirtualFile> result, IBundleCategory category) {
        Collection<IBundleEntry> entries = category.getEntries();
        for(IBundleEntry entry : entries)
        {
            result.add(getSWC(sdkRoot, entry.getPath()));
        }
    }

    private VirtualFile getSWC(VirtualFile root, String relativePath)
    {
        VirtualFile swc = root
                .findFileByRelativePath(relativePath);
        if (swc == null || !swc.exists())
            throw new RuntimeException("The SWC " + relativePath + " does not exist");
        return swc;
    }

    private void addSWC(SdkModificator modificator, VirtualFile swc)
    {
        final VirtualFile jarRoot = JarFileSystem.getInstance()
                .getJarRootForLocalFile(swc);
        if (jarRoot != null)
        {
            modificator.addRoot(jarRoot, OrderRootType.CLASSES);
        }
    }

    private void setupDependentSdks(Sdk sdk)
    {
        // until future understanding, anytime an sdk is setup, we make sure
        // to sync the project and modules sdks to the new setup.
        // NO, now that I think about this, we might have a dev and production sdk and
        // changing people's settings after the fact is going to piss them off.
        // We need to make this clear in the docs then about making sure SDKs are synced
        // on the project and modules.
    }

    public static void copySdkLibraries(Project project)
    {
        // only copy the SDK js libraries if the SWCs exist in the SDK
        final VirtualFile baseDir = project.getBaseDir();

        final Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();

        if (sdk == null)
            throw new RuntimeException(
                    "Tried to copy libraries but SDK was null");

        RandoriSdk randoriSdk = (RandoriSdk) sdk.getSdkType();


        VirtualFile sdkRoot = sdk.getHomeDirectory();
        if (sdkRoot == null || !sdkRoot.exists())
            throw new RuntimeException(
                    "SDK home directory not found, please check your Project and Module SDK settings.");

        if (randoriSdk.sdkBundle == null)
        {
            randoriSdk.sdkBundle = randoriSdk.getBundle(sdkRoot);
        }
        IBundle sdkBundle = randoriSdk.sdkBundle;

        String libPath = project
                .getComponent(RandoriProjectComponent.class).getState()
                .getLibraryPath();

        VirtualFile libraryDir = baseDir.findFileByRelativePath(libPath);
        if (libraryDir == null)
        {
            new File(baseDir.getPath(), libPath).mkdirs();
            baseDir.refresh(false, true);
            libraryDir = baseDir.findFileByRelativePath(libPath);
        }
        copyJSFilesFromBundle(libraryDir, sdkRoot, sdkBundle);

        // TODO figure out the correct way to refresh the generated dir
        // says this should only be called in a writeAction
        baseDir.refresh(true, true);
    }

    private static void copyJSFilesFromBundle(VirtualFile baseDir, VirtualFile sdkRoot, IBundle sdkBundle) {
        Collection<IBundleLibrary> libraries = sdkBundle.getLibraries();
        for(IBundleLibrary library : libraries)
        {
            copyJSFilesFromLibrary(baseDir, sdkRoot, library);
        }
    }

    private static void copyJSFilesFromLibrary(VirtualFile baseDir, VirtualFile sdkRoot, IBundleLibrary library) {

        IBundleContainer container = library.getContainer(IBundleContainer.Type.JS);
        if (container != null)
        {
            copyJSFilesFromContainer(baseDir, sdkRoot, container);
        }
   }

    private static void copyJSFilesFromContainer(VirtualFile baseDir, VirtualFile sdkRoot, IBundleContainer container) {
        IBundleCategory category = container.getCategory(IBundleCategory.Type.MONO);
        if (category != null)
        {
            copyJSFilesFromCategory(baseDir, sdkRoot, category);
        }
    }

    private static void copyJSFilesFromCategory(VirtualFile baseDir, VirtualFile sdkRoot, IBundleCategory category) {
        Collection<IBundleEntry> entries = category.getEntries();
        for(IBundleEntry entry : entries)
        {
            copyJSEntry(baseDir, sdkRoot, entry);
        }
    }

    private static void copyJSEntry(VirtualFile baseDir, VirtualFile sdkRoot, IBundleEntry entry) {
        File sourceFile = new File(sdkRoot.getPath() + File.separator + entry.getPath());
        File destinationFile = new File(baseDir.getPath() + File.separator + sourceFile.getName());
        try
        {
            FileUtil.copy(sourceFile, destinationFile);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public IBundle getBundle(VirtualFile sdkRoot)
    {
        VirtualFile manifestFile = sdkRoot.findFileByRelativePath("manifest.xml");
        IMutableBundle bundle = new Bundle(null);
        populateBundle(manifestFile, bundle);
        return bundle;
    }

    private void populateBundle(VirtualFile manifestFile, IMutableBundle bundle) {
        StAXManifestReader manifestReader = null;
        try
        {
            manifestReader = new StAXManifestReader(new BufferedInputStream(manifestFile.getInputStream()), bundle);
            manifestReader.parse();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(XMLStreamException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (manifestReader != null)
            {
                try
                {
                    manifestReader.close();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
