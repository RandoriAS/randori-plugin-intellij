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

package randori.plugin.components;

import java.util.HashMap;
import java.util.Map;

import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;

/**
 * @author Michael Schmalle
 */
public class WorkspaceApplicationComponent implements ApplicationComponent,
        IWorkspaceApplication
{
    private Workspace workspace;

    private Map<String, FlexProject> map = new HashMap<String, FlexProject>();

    public WorkspaceApplicationComponent()
    {
    }

    @Override
    public Workspace getWorkspace()
    {
        return workspace;
    }

    @Override
    public ICompilerProject addProject(Project project)
    {
        if (map.containsKey(project.getName()))
            return null;
        FlexProject result = new FlexProject(workspace);
        map.put(project.getName(), result);
        return result;
    }

    @Override
    public void initComponent()
    {
        workspace = new Workspace();
    }

    @Override
    public void disposeComponent()
    {
        // TODO: insert component disposal logic here
    }

    @Override
    @NotNull
    public String getComponentName()
    {
        return "WorkspaceApplicationComponent";
    }

    @SuppressWarnings("unused")
    private void startupApplication()
    {

        //FlexProjectConfigurator.configure(project);

        //List<File> sourcePath = new ArrayList<File>();
        //sourcePath.add(new File(tempDir));
        //project.setSourcePath(sourcePath);

        //List<File> libraries = new ArrayList<File>();
        //project.addSourcePathFile();
        //project.setLibraries();
        //        compilationSuccess = application.build(
        //                (IRandoriBackend) backend, problems);
    }
}
