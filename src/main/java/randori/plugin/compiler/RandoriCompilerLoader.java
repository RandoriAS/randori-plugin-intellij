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

package randori.plugin.compiler;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;

/**
 * @author Frédéric THOMAS
 */
public class RandoriCompilerLoader extends AbstractProjectComponent
{

    private static final String COMPONENT_NAME = "RandoriCompilerLoader";

    public RandoriCompilerLoader(Project project)
    {
        super(project);
    }

    @Override
    public void projectOpened()
    {
        CompilerManager compilerManager = CompilerManager
                .getInstance(this.myProject);

        compilerManager.addCompiler(new RandoriCompiler(this.myProject));
    }

    @NotNull
    @Override
    public String getComponentName()
    {
        return COMPONENT_NAME;
    }
}
