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

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.jetbrains.annotations.NotNull;
import randori.plugin.workspace.IRandoriWorkspace;

/**
 * @author Michael Schmalle
 */
public class ASProjectComponent implements ProjectComponent
{
    private final IRandoriWorkspace workspace;
    private final Project project;
    private FlexProject asProject;

    public ASProjectComponent(Project project, IRandoriWorkspace workspace)
    {
        this.project = project;
        this.workspace = workspace;
    }

    public FlexProject getASProject()
    {
        return asProject;
    }

    @Override
    public void initComponent()
    {
        asProject = (FlexProject) workspace.addProjectCompiler(project);
    }

    @Override
    public void disposeComponent()
    {
        workspace.getWorkspace().deleteProject(asProject);
    }

    @Override
    @NotNull
    public String getComponentName()
    {
        return "ASProjectComponent";
    }

    @Override
    public void projectOpened()
    {
        // called when project is opened
    }

    @Override
    public void projectClosed()
    {
        // called when project is being closed
    }
}
