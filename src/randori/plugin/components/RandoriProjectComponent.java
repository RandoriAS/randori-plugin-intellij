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

import javax.swing.JComponent;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import randori.compiler.clients.CompilerArguments;
import randori.plugin.forms.RandoriProjectConfigurationForm;
import randori.plugin.ui.ProblemsToolWindowFactory;
import randori.plugin.utils.ProjectUtils;
import randori.plugin.workspaces.IWorkspaceApplication;
import randori.plugin.workspaces.RandoriApplicationComponent;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;

@State(name = RandoriProjectComponent.COMPONENT_NAME, storages = { @Storage(id = "randoriproject", file = "$PROJECT_FILE$") })
/**
 * @author Michael Schmalle
 */
public class RandoriProjectComponent extends BaseRandoriProjectComponent
        implements ProjectComponent, Configurable,
        PersistentStateComponent<RandoriProjectModel>
{
    public static final String COMPONENT_NAME = "RandoriProject";

    private RandoriProjectConfigurationForm form;

    private IWorkspaceApplication workspaceApplication;

    public RandoriProjectComponent(Project project,
            IWorkspaceApplication workspaceApplication)
    {
        super(project);
        this.workspaceApplication = workspaceApplication;
    }

    @Override
    public void projectOpened()
    {
        if (!ProjectUtils.hasRandoriModuleType(getProject()))
            return;

        System.out.println("projectOpened()");
        build(null, false, true);
    }

    public void build(VirtualFile[] files, boolean doClean, boolean sync)
    {
        CompilerArguments arguments = new CompilerArguments();
        configureDependencies(getProject(), arguments, files);

        if (sync)
        {
            workspaceApplication.buildSync(getProject(), doClean, arguments);
        }
        else
        {
            workspaceApplication.build(getProject(), doClean, arguments);
        }
    }

    public void parse(boolean sync)
    {
        CompilerArguments arguments = new CompilerArguments();
        configureDependencies(getProject(), arguments);

        RandoriApplicationComponent component = getProject().getComponent(
                RandoriApplicationComponent.class);

        if (sync)
        {
            component.parseSync(getProject(), arguments);
        }
        else
        {
            component.parse(getProject(), arguments);
        }
    }

    @Override
    public void projectClosed()
    {
        ToolWindow toolWindow = ToolWindowManager.getInstance(getProject())
                .getToolWindow(ProblemsToolWindowFactory.WINDOW_ID);
        if (toolWindow != null)
        {
            toolWindow.hide(new Runnable() {
                @Override
                public void run()
                {

                }
            });
        }
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
        return COMPONENT_NAME;
    }

    @Nls
    @Override
    public String getDisplayName()
    {
        return "Randori";
    }

    @Override
    public String getHelpTopic()
    {
        return null;
    }

    @Override
    public RandoriProjectModel getState()
    {
        return getModel();
    }

    @Override
    public void loadState(RandoriProjectModel state)
    {
    }

    @Override
    public JComponent createComponent()
    {
        if (form == null)
        {
            form = new RandoriProjectConfigurationForm();
        }
        return form.getComponent();
    }

    @Override
    public boolean isModified()
    {
        return form.isModified(getState());
    }

    @Override
    public void apply() throws ConfigurationException
    {
        if (form != null)
        {
            form.getData(getState());
        }
    }

    //------------------------------------------------------
    // TODO get the below in a Model

    @Override
    public void reset()
    {
        if (form != null)
        {
            form.setData(getState());
        }
    }

    @Override
    public void disposeUIResources()
    {
    }

}
