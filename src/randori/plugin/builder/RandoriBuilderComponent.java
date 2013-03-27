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

package randori.plugin.builder;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Michael Schmalle
 */
public class RandoriBuilderComponent implements ProjectComponent
{

    // Could provide the public API here to start the build on background thread

    private Project project;

    public Project getProject()
    {
        return project;
    }

    public RandoriBuilderComponent(Project project)
    {
        this.project = project;
    }

    @Override
    public void projectOpened()
    {
    }

    @Override
    public void projectClosed()
    {
    }

    @Override
    public void initComponent()
    {
    }

    @Override
    public void disposeComponent()
    {
    }

    @NotNull
    @Override
    public String getComponentName()
    {
        return "RandoriBuilderComponent";
    }
}
