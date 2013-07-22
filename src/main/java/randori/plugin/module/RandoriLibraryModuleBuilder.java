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

package randori.plugin.module;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import randori.plugin.roots.RandoriSdkType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Frédéric THOMAS
 */
public class RandoriLibraryModuleBuilder extends JavaModuleBuilder
{
    // Pair<Source Path, Package Prefix>
    private List<Pair<String, String>> mySourcePaths;

    //private final List<Pair<String, String>> myModuleLibraries = new ArrayList<Pair<String, String>>();

    @Override
    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException
    {
        // adds the project/module's root as a content entry, this allows /generated
        // etc. to be seen in the project explorer
        ContentEntry contentEntry = doAddContentEntry(rootModel);

        if (contentEntry != null)
        {
            final List<Pair<String, String>> sourcePaths = getSourcePaths();

            if (sourcePaths != null)
                for (final Pair<String, String> sourcePath : sourcePaths)
                {
                    String first = sourcePath.first;
                    //noinspection ResultOfMethodCallIgnored
                    new File(first).mkdirs();
                    final VirtualFile sourceRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(
                            FileUtil.toSystemIndependentName(first));
                    if (sourceRoot != null)
                        contentEntry.addSourceFolder(sourceRoot, false, sourcePath.second);
                }
        }

        List<Sdk> sdks = ProjectJdkTable.getInstance().getSdksOfType(RandoriSdkType.getInstance());
        if (sdks.size() > 0)
        {
            rootModel.setSdk(sdks.get(0));
        }

        //        // copy the files to generated
        //        VirtualFile randoriJS = sdkRoot.findFileByRelativePath("src/Randori.js");
        //        VirtualFile guiceJS = sdkRoot.findFileByRelativePath("src/RandoriGuiceJS.js");
        //        VirtualFile newRandoriJS = newFile;
        //        VirtualFile newRandoriGuiceJS = newFile;
        //        randoriJS.copy(this, newRandoriJS, "Randori.js");
        //        randoriJS.copy(this, newRandoriGuiceJS, "RandoriGuiceJS.js");
    }

    @Override
    public List<Pair<String, String>> getSourcePaths()
    {
        if (mySourcePaths == null)
        {
            final List<Pair<String, String>> paths = new ArrayList<Pair<String, String>>();
            @NonNls
            final String path = getContentEntryPath() + File.separator + "src";
            //noinspection ResultOfMethodCallIgnored
            new File(path).mkdirs();
            paths.add(Pair.create(path, ""));
            return paths;
        }
        return mySourcePaths;
    }

    @Override
    public void setSourcePaths(List<Pair<String, String>> sourcePaths)
    {
        mySourcePaths = sourcePaths != null ? new ArrayList<Pair<String, String>>(sourcePaths) : null;
    }

    @Override
    public void addSourcePath(Pair<String, String> sourcePathInfo)
    {
        if (mySourcePaths == null)
        {
            mySourcePaths = new ArrayList<Pair<String, String>>();
        }
        mySourcePaths.add(sourcePathInfo);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ModuleType getModuleType()
    {
        return RandoriLibraryModuleType.getInstance();
    }

    @Override
    public boolean isSuitableSdkType(SdkTypeId sdkType)
    {
        return RandoriSdkType.getInstance() == sdkType;
    }
}
