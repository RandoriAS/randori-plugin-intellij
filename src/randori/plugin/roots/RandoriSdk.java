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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.module.RandoriModuleType;
import randori.plugin.utils.SdkUtils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Michael Schmalle
 */
public class RandoriSdk extends SdkType
{
    private static final String SDK_XML = "sdk.xml";

    private static final String BUILTIN = "builtin";

    private static final String RANDORI = "randori-framework";

    private static final String RANDORI_GUICE = "randori-guice-framework";

    private static final String JQUERY = "JQuery";

    private static final String HTML_CORE_LIB = "HTMLCoreLib";

    private final List<String> libraries = new ArrayList<String>();

    public RandoriSdk()
    {
        super("Randori SDK");

        // TODO put these names in the sdk.xml manifest?
        libraries.add(BUILTIN);
        libraries.add(RANDORI);
        libraries.add(RANDORI_GUICE);
        libraries.add(HTML_CORE_LIB);
        libraries.add(JQUERY);
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

        // TODO do we need to check the version?, how would we?
        @SuppressWarnings("unused")
        String sdkVersion = getVersion(sdkRoot);
        // check whether the root selected by the user still exists
        if (sdkRoot == null || !sdkRoot.isValid())
            return;

        loadSDKLibraries(sdk);

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
        return RandoriModuleType.RANDORI_ICON_SMALL;
    }

    @Override
    public Icon getIcon()
    {
        return RandoriModuleType.RANDORI_ICON_SMALL;
    }

    @Override
    public String getVersionString(String sdkHome)
    {
        // need to get the version from the XML file at the root of the SDK
        VirtualFile sdkRoot = sdkHome != null ? VfsUtil.findRelativeFile(
                sdkHome, null) : null;
        return getVersion(sdkRoot);
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

    private void loadSDKLibraries(Sdk sdk)
    {
        // getHomeDirectory() is what the user selected in the file chooser, the root
        VirtualFile sdkRoot = sdk.getHomeDirectory();

        SdkModificator modificator = sdk.getSdkModificator();

        for (String library : libraries)
        {
            addSWC(modificator, getSWC(sdkRoot, library));
        }

        modificator.commitChanges();
    }

    private VirtualFile getSWC(VirtualFile root, String name)
    {
        String extension = ".swc";
        VirtualFile swc = root
                .findFileByRelativePath("bin/" + name + extension);
        if (swc == null || !swc.exists())
            throw new RuntimeException("The SWC in sdk/bin [" + name
                    + "] does not exist");
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

        VirtualFile sdkRoot = sdk.getHomeDirectory();
        if (sdkRoot == null || !sdkRoot.exists())
            throw new RuntimeException(
                    "SDK home directory not found, please check your Project and Module SDK settings.");

        if (SdkUtils.libraryExists(project, RANDORI)
                && SdkUtils.libraryExists(project, RANDORI_GUICE))
        {
            String libPath = project
                    .getComponent(RandoriProjectComponent.class).getState()
                    .getLibraryPath();

            VirtualFile libraryDir = baseDir.findFileByRelativePath(libPath);
            if (libraryDir == null)
            {
                new File(baseDir.getPath(), libPath).mkdirs();
                libraryDir = baseDir.findFileByRelativePath(libPath);
            }

            // copy the files to generated
            VirtualFile randoriJS = sdkRoot.findFileByRelativePath("src/"
                    + "Randori" + ".js");
            VirtualFile guiceJS = sdkRoot.findFileByRelativePath("src/"
                    + "RandoriGuice" + ".js");

            try
            {
                FileUtil.copy(new File(randoriJS.getPath()),
                        new File(baseDir.getPath(), libPath + "/" + "Randori"
                                + ".js"));
                FileUtil.copy(new File(guiceJS.getPath()),
                        new File(baseDir.getPath(), libPath + "/"
                                + "RandoriGuice" + ".js"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }

        // TODO figure out the correct way to refresh the generated dir
        // says this should only be called in a writeAction
        baseDir.refresh(true, true);
    }

    private String getVersion(VirtualFile sdkRoot)
    {
        // TODO Roland, you need to work your majic here.
        VirtualFile versionFile = sdkRoot.findFileByRelativePath(SDK_XML);
        // If the user has selected a directory that does not contain the sdmk.xml
        if (versionFile == null || !versionFile.exists())
            return null;

        if (versionFile.exists())
        {
            File fXmlFile = new File(versionFile.getPath());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            try
            {
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);
                Element element = doc.getDocumentElement();
                NodeList version = element.getElementsByTagName("version");
                String versionId = version.item(0).getTextContent();
                return versionId;
            }
            catch (SAXException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ParserConfigurationException e)
            {
                e.printStackTrace();
            }
        }

        throw new RuntimeException(
                "Cannot get the version of SDK, corrupt sdk.xml");
    }
}
