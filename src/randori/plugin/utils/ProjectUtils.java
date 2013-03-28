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

package randori.plugin.utils;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.Nullable;

import randori.plugin.components.RandoriApplicationComponent;
import randori.plugin.components.RandoriProjectComponent;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;
import randori.plugin.module.RandoriModuleType;

/**
 * A set of utilities for working with Projects and Modules.
 * 
 * @author Michael Schmalle
 */
public class ProjectUtils
{
    public static RandoriApplicationComponent getApplicationComponent()
    {
        RandoriApplicationComponent component = ApplicationManager
                .getApplication().getComponent(
                        RandoriApplicationComponent.class);
        return component;
    }

    public static RandoriProjectComponent getProjectComponent(Project project)
    {
        RandoriProjectComponent component = project
                .getComponent(RandoriProjectComponent.class);
        return component;
    }

    public static Project getProject()
    {
        AsyncResult<DataContext> dataContext = DataManager.getInstance()
                .getDataContextFromFocus();
        Project project = PlatformDataKeys.PROJECT.getData(dataContext
                .getResult());
        return project;
    }

    public static Project getProject(Component component)
    {
        DataContext dataContext = DataManager.getInstance().getDataContext(
                component);
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        return project;
    }

    public static final <T> T findProjectComponent(Component component,
            Class<T> type)
    {
        final Project project = getProject(component);
        if (null != project)
        {
            return project.getComponent(type);
        }
        else
        {
            return null;
        }
    }

    @Nullable
    public static final <T> T findProjectComponent(DataContext ctxt,
            Class<T> type)
    {
        final Project project = PlatformDataKeys.PROJECT.getData(ctxt);
        if (null != project)
        {
            return project.getComponent(type);
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns whether there is an installed Sdk within the project.
     * 
     * @param project The project.
     */
    public static final boolean isSDKInstalled(Project project)
    {
        return ProjectRootManager.getInstance(project).getProjectSdk() != null;
    }

    /**
     * Return the Sdk base path.
     * 
     * @param project The project
     */
    public static final String getSDKBasePath(Project project)
    {
        Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
        String path = sdk.getHomePath();
        return path;
    }

    /**
     * Return the current <code>playerglobal.swc</code> for the current Sdk.
     * 
     * @param project The project.
     */
    public static final String getPlayerGloablPath(Project project)
    {
        String path = getSDKBasePath(project);
        path = path + "/bin/builtin.swc";
        return path;
    }

    /**
     * Return a List of all swcs found within the Project.
     * 
     * @param project The project.
     */
    public static final List<String> getAllProjectSWCs(Project project)
    {
        ArrayList<String> result = new ArrayList<String>();
        for (VirtualFile virtualFile : LibraryUtil.getLibraryRoots(project))
        {
            String path = virtualFile.getPath();
            if (path.indexOf("swc!/") != -1)
            {
                result.add(virtualFile.getPath().replace("!/", ""));
            }
            else if (virtualFile.getName().indexOf("builtin") != -1)
            {
                result.add(virtualFile.getPath().replace("!/", ""));
            }
        }
        return result;
    }

    /**
     * Return a List of all source paths found in the Project.
     * 
     * @param project The project.
     */
    public static final List<String> getAllProjectSourcePaths(Project project)
    {
        ArrayList<String> result = new ArrayList<String>();
        // Randori/src, RandoriGuice/src, RandorFlash, RandoriFlash
        VirtualFile[] roots = ProjectRootManager.getInstance(project)
                .getContentRoots();
        for (VirtualFile virtualFile : roots)
        {
            if (!isModuleRoot(project, virtualFile.getName()))
            {
                //String name = virtualFile.getName();
                result.add(virtualFile.getPath());
            }
        }
        return result;
    }

    public static boolean hasRandoriModuleType(Project project)
    {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules)
        {
            if (ModuleType.get(module) instanceof RandoriModuleType)
                return true;
        }
        return false;
    }

    /**
     * Return whether the name is a Module root name in the project.
     * <p/>
     * I'm not sure this method works as desired yet.
     * 
     * @param project The project.
     * @param name A name to compare with all project Module names
     */
    public static boolean isModuleRoot(Project project, String name)
    {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules)
        {
            if (module.getName().equals(name))
                return true;
        }
        return false;
    }
}

/*

Get current project, when many frames could be open;

DataContext dataContext = DataManager.getInstance().getDataContext();
Project project = DataKeys.PROJECT.getData(dataContext);

Folder of currently selected file;

VirtualFile file = (VirtualFile) e.getDataContext().getData(DataConstants.VIRTUAL_FILE);
VirtualFile folder = file.getParent();

Add custom components to statusbar

StatusBar.addCustomIndicationComponent()

*/

