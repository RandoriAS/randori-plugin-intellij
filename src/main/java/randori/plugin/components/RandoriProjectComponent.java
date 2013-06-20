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

package randori.plugin.components;

import com.intellij.compiler.CompilerWorkspaceConfiguration;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ClasspathEditor;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import randori.plugin.compiler.RandoriCompilerSession;
import randori.plugin.configuration.RandoriProjectConfigurable;
import randori.plugin.configuration.RandoriProjectModel;
import randori.plugin.module.RandoriModuleType;
import randori.plugin.runner.RandoriRunConfiguration;
import randori.plugin.runner.RandoriServerComponent;
import randori.plugin.ui.ProblemsToolWindowFactory;
import randori.plugin.util.ProjectUtils;
import randori.plugin.util.VFileUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@State(name = RandoriProjectComponent.COMPONENT_NAME, storages = { @Storage(id = "randoriProject", file = "$PROJECT_FILE$") })
/**
 * The project component manages the global state of the current project and wrap the compiler.
 *
 * @author Frédéric THOMAS
 */
public class RandoriProjectComponent implements ProjectComponent, Configurable,
        PersistentStateComponent<RandoriProjectModel>
{

    public static final String COMPONENT_NAME = "RandoriProject";
    private RandoriProjectConfigurable form;
    private final Project project;
    private RandoriProjectModel state;
    private VirtualFileListener fileChangeListener;
    private List<VirtualFile> modifiedFiles;

    public RandoriProjectComponent(Project project)
    {
        this.project = project;
    }

    @Override
    public void initComponent()
    {
    }

    @Override
    public void projectOpened()
    {
        if (ProjectUtils.hasRandoriModuleType(project))
        {
            modifiedFiles = new ArrayList<VirtualFile>();

            CompilerWorkspaceConfiguration workspaceConfiguration = CompilerWorkspaceConfiguration.getInstance(project)
                    .getState();
            if (workspaceConfiguration != null)
            {
                workspaceConfiguration.USE_COMPILE_SERVER = false;
                CompilerWorkspaceConfiguration.getInstance(project).loadState(workspaceConfiguration);
            }

            state = new RandoriProjectModel();

            fileChangeListener = new FileChangeListener(project);
            VirtualFileManager.getInstance().addVirtualFileListener(fileChangeListener);

            RandoriCompilerSession.parseAll(project);
        }
    }

    @Override
    public void projectClosed()
    {
        if (ProjectUtils.hasRandoriModuleType(project))
        {

            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(
                    ProblemsToolWindowFactory.WINDOW_ID);
            if (toolWindow != null)
            {
                toolWindow.hide(new Runnable() {
                    @Override
                    public void run()
                    {
                    }
                });
            }
            modifiedFiles = null;
        }
    }

    @Override
    public void disposeComponent()
    {

        if (!ProjectUtils.hasRandoriModuleType(project))
            return;

        VirtualFileManager.getInstance().removeVirtualFileListener(fileChangeListener);

        state = null;
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
        return "Randori Project";
    }

    @Override
    public String getHelpTopic()
    {
        return null;
    }

    @Override
    public RandoriProjectModel getState()
    {
        return state;
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
            form = new RandoriProjectConfigurable();
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

    public List<VirtualFile> getModifiedFiles()
    {
        return modifiedFiles;
    }

    /**
     * Opens a ICompilerProblem in a new editor, or opens the editor and places the caret a the specific problem.
     * 
     * @param problem The ICompilerProblem to focus.
     */
    public void openFileForProblem(ICompilerProblem problem)
    {
        VirtualFile virtualFile = VFileUtils.getFile(problem.getSourcePath());
        if (virtualFile != null)
        {
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile);
            Editor editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
            if (editor != null)
            {
                LogicalPosition position = new LogicalPosition(problem.getLine(), problem.getColumn());
                editor.getCaretModel().moveToLogicalPosition(position);
            }
        }
    }

    public void run(RandoriRunConfiguration configuration)
    {
        RandoriServerComponent component = project.getComponent(RandoriServerComponent.class);
        String explicitWebRoot = (configuration.useExplicitWebroot) ? configuration.explicitWebroot : "";
        component.openURL(configuration.indexRoot, explicitWebRoot);
    }

    public boolean validateConfiguration(CompileScope scope)
    {
        boolean validated = true;
        String message = null;
        Module module = null;
        
        if (ProjectUtils.isSDKInstalled(project))
        {
            for (final Module affectedModule : scope.getAffectedModules())
            {
                if (ModuleType.get(affectedModule) != RandoriModuleType.getInstance())
                {
                    message = "This module is not a Randori module";
                    module = affectedModule;
                    validated = false;
                    break;
                }
            }
        }
        else {
            message = "This project is not a Randori project, please check your Project SDK settings.";
            validated = false;
        }
        
        if (message != null) {
            Messages.showErrorDialog(project, message, "Can not Compile");
            if (module != null) {
                ModulesConfigurator.showDialog(project, module.getName(), ClasspathEditor.NAME);
            }
        }

        return validated;
    }
}
