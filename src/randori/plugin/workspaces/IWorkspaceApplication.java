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

package randori.plugin.workspaces;

import com.intellij.openapi.project.Project;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.workspaces.IWorkspace;

import randori.compiler.clients.CompilerArguments;

/**
 * The single instance of the IWorkspace in the IDE that manages compilation
 * units for
 * 
 * @author Michael Schmalle
 */
public interface IWorkspaceApplication
{

    /**
     * Returns the workspace of the application.
     */
    Workspace getWorkspace();

    /**
     * Adds a project to the workspace, returns the Randori project that was
     * added to the workspace.
     * <p>
     * Note; adding a project to the {@link IWorkspace} will not make a
     * dependency of other projects until explicitly added.
     * 
     * @param project The project to add.
     */
    ICompilerProject addProject(Project project);

    void buildSync(Project project, boolean doClean, CompilerArguments arguments);

    void build(Project project, boolean doClean, CompilerArguments arguments);

}
