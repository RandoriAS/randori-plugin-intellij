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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Michael Schmalle
 */
public class SdkUtils
{

    public static boolean libraryFileExists(Project project, String name)
    {
        final Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
        final VirtualFile sdkRoot = sdk.getHomeDirectory();
        final VirtualFile swc = sdkRoot.findFileByRelativePath("bin/" + name
                + ".swc");
        return swc != null && swc.exists();
    }

    public static boolean libraryExists(Project project, String name)
    {
        final Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
        VirtualFile[] files = sdk.getRootProvider().getFiles(
                OrderRootType.CLASSES);
        for (VirtualFile file : files)
        {
            String fileName = file.getName();
            if (fileName.equals(name + ".swc"))
                return true;
        }
        return false;
    }
}
