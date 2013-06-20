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

package randori.plugin.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.intellij.openapi.roots.ModuleRootManager;
import org.jetbrains.annotations.Nullable;

import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.module.RandoriModuleType;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * A set of utilities for working with Projects and Modules.
 * 
 * @author Michael Schmalle
 */
public class ProjectUtils
{
    public static RandoriProjectComponent getProjectComponent(Project project)
    {
        return project.getComponent(RandoriProjectComponent.class);
    }

    public static Project getProject()
    {
        // TODO: Temporary try catch to remove once dealt with project/module instead of application setup.
        try
        {
            AsyncResult<DataContext> dataContext = DataManager.getInstance().getDataContextFromFocus();
            return PlatformDataKeys.PROJECT.getData(dataContext.getResult());
        }
        catch (IllegalArgumentException e)
        {
            // Happens when the project is closing.
        }

        return null;
    }

    private static Project getProject(Component component)
    {
        DataContext dataContext = DataManager.getInstance().getDataContext(component);
        return PlatformDataKeys.PROJECT.getData(dataContext);
    }

    public static <T> T findProjectComponent(Component component, Class<T> type)
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
    public static <T> T findProjectComponent(DataContext context, Class<T> type)
    {
        final Project project = PlatformDataKeys.PROJECT.getData(context);
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
    public static boolean isSDKInstalled(Project project)
    {
        return ProjectRootManager.getInstance(project).getProjectSdk() != null;
    }

    /**
     * Return the Sdk base path.
     * 
     * @param project The project
     */
    public static String getSDKBasePath(Project project)
    {
        Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
        return sdk != null ? sdk.getHomePath() : null;
    }

    /**
     * Return a List of all swcs found within the Project.
     * 
     * @param project The project.
     */
    public static List<String> getAllProjectSWCs(Project project)
    {
        ArrayList<String> result = new ArrayList<String>();
        for (VirtualFile virtualFile : LibraryUtil.getLibraryRoots(project))
        {
            String path = virtualFile.getPath();
            if (path.contains("swc!/"))
            {
                result.add(virtualFile.getPath().replace("!/", ""));
            }
            else if (virtualFile.getName().contains("builtin"))
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
    public static List<String> getAllProjectSourcePaths(Project project)
    {
        ArrayList<String> result = new ArrayList<String>();
        // Randori/src, RandoriGuice/src, RandorFlash, RandoriFlash
        VirtualFile[] roots = ProjectRootManager.getInstance(project).getContentSourceRoots();

        for (VirtualFile root : roots)
        {
            if (isModuleRoot(project, root.getName()))
                result.add(root.getPath());
        }
        return result;
    }
    
    public static List<String> getAllModuleSourcePaths(Module module)
    {
        ArrayList<String> result = new ArrayList<String>();
        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();

        for (VirtualFile sourceRoot : sourceRoots) {
            result.add(sourceRoot.getPath());
        }

        return result;
    }

    public static boolean hasRandoriModuleType(Project project)
    {
        if (project == null)
            return false;

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

    public static boolean isModuleRoot(Project project, Module module)
    {
        return module.getName().equals(project.getName());
    }
}
