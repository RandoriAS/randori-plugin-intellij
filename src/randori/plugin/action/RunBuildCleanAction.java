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

package randori.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.utils.ProjectUtils;

/**
 * @author Michael Schmalle
 */
public class RunBuildCleanAction extends BaseRandoriBuildAction
{

    @Override
    public void actionPerformed(AnActionEvent event)
    {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        RandoriProjectComponent component = ProjectUtils
                .getProjectComponent(project);
        component.build(true);
    }

}
