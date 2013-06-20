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

package randori.plugin.ui;

import randori.plugin.service.ProblemsService;
import randori.plugin.service.RandoriProjectPreferences;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import randori.plugin.util.ProjectUtils;

/**
 * @author Michael Schmalle
 */
public class ProblemsToolWindowFactory implements ToolWindowFactory
{
    public static final String WINDOW_ID = "Randori Problems";

    @SuppressWarnings("unused")
    private ProblemsToolWindow window;

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow)
    {
        ProblemsService service = ProblemsService.getInstance(project);

        RandoriProjectPreferences preferences = RandoriProjectPreferences
                .getInstance(project);

        window = new ProblemsToolWindow(toolWindow, service,
                preferences.getProblemWindowColumnSizes());
    }
}
