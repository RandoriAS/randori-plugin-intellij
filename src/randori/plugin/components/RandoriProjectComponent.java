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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import randori.compiler.clients.CompilerArguments;
import randori.compiler.plugin.IPreProcessPlugin;
import randori.plugin.builder.FileChangeListener;
import randori.plugin.compiler.RandoriProjectCompiler;
import randori.plugin.forms.RandoriProjectConfigurationForm;
import randori.plugin.module.RandoriModuleType;
import randori.plugin.roots.RandoriSdk;
import randori.plugin.runner.RandoriRunConfiguration;
import randori.plugin.runner.RandoriServerComponent;
import randori.plugin.service.ProblemsService;
import randori.plugin.ui.ProblemsToolWindowFactory;
import randori.plugin.util.NotificationUtils;
import randori.plugin.util.ProjectUtils;
import randori.plugin.util.VFileUtils;
import randori.plugin.workspace.IRandoriWorkspace;

import com.intellij.openapi.application.ApplicationManager;
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
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ui.configuration.ClasspathEditor;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;

@State(name = RandoriProjectComponent.COMPONENT_NAME, storages = { @Storage(id = "randoriProject", file = "$PROJECT_FILE$") })
/**
 * The project component manages the global state of the current project and wrap the compiler.
 *
 * @author Frédéric THOMAS
 */
public class RandoriProjectComponent implements ProjectComponent, Configurable,
        PersistentStateComponent<RandoriProjectModel>, IRandoriWorkspace
{

    public static final String COMPONENT_NAME = "RandoriProject";
    private List<VirtualFile> modifiedFiles;
    private Workspace workspace;
    private RandoriProjectConfigurationForm form;
    private Project project;
    private RandoriProjectModel state;
    private VirtualFileListener fileChangeListener;
    private RandoriProjectCompiler compiler;

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

        if (!ProjectUtils.hasRandoriModuleType(project))
            return;

        workspace = new Workspace();
        compiler = addProjectCompiler(this.project);
        state = new RandoriProjectModel();
        modifiedFiles = new ArrayList<VirtualFile>();

        fileChangeListener = new FileChangeListener(project);
        VirtualFileManager.getInstance().addVirtualFileListener(
                fileChangeListener);

        parse(false);
    }

    @Override
    public RandoriProjectCompiler addProjectCompiler(Project project)
    {

        if (!ProjectUtils.hasRandoriModuleType(project))
            return null;

        RandoriProjectCompiler compiler = new RandoriProjectCompiler(new Workspace());

        compiler.getPluginFactory().registerPlugin(IPreProcessPlugin.class, DummyPreProcessPlugin.class);

        return compiler;
    }

    @Override
    public void projectClosed()
    {

        if (!ProjectUtils.hasRandoriModuleType(project))
            return;

        ToolWindow toolWindow = ToolWindowManager.getInstance(project)
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
    public void disposeComponent()
    {

        if (!ProjectUtils.hasRandoriModuleType(project))
            return;

        VirtualFileManager.getInstance().removeVirtualFileListener(
                fileChangeListener);

        compiler = null;
        workspace = null;
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

    /**
     * Returns the {@link Project} instance open.
     */
    public Project getProject()
    {
        return project;
    }

    @Override
    public Workspace getWorkspace()
    {
        return workspace;
    }

    /**
     * Returns the {@link ProblemsService} that manages {@link ICompilerProblem}
     * s from the compiler.
     */
    public ProblemsService getProblemsService()
    {
        return ProblemsService.getInstance(project);
    }

    public List<VirtualFile> getModifiedFiles()
    {
        return modifiedFiles;
    }

    /**
     * Opens a ICompilerProblem in a new editor, or opens the editor and places
     * the caret a the specific problem.
     * 
     * @param problem The ICompilerProblem to focus.
     */
    public void openFileForProblem(ICompilerProblem problem)
    {
        VirtualFile virtualFile = VFileUtils.getFile(problem.getSourcePath());
        if (virtualFile != null)
        {
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project,
                    virtualFile);
            if (descriptor != null)
            {
                Editor editor = FileEditorManager.getInstance(project)
                        .openTextEditor(descriptor, true);
                if (editor != null)
                {
                    LogicalPosition position = new LogicalPosition(
                            problem.getLine(), problem.getColumn());
                    editor.getCaretModel().moveToLogicalPosition(position);
                }
            }
        }
    }

    public void build(boolean doClean, boolean sync)
    {
        compiler = addProjectCompiler(project);
        CompilerArguments arguments = new CompilerArguments();
        configureDependencies(arguments);

        if (sync)
        {
            buildSync(doClean, arguments);
        }
        else
        {
            build(doClean, arguments);
        }
    }

    public void parse(boolean sync)
    {
        compiler = addProjectCompiler(project);
        CompilerArguments arguments = new CompilerArguments();
        configureDependencies(arguments);

        if (sync)
        {
            parseSync(arguments);
        }
        else
        {
            parse(arguments);
        }
    }

    public void run(RandoriRunConfiguration configuration)
    {
        RandoriServerComponent component = project
                .getComponent(RandoriServerComponent.class);
        String explicitWebRoot = (configuration.useExplicitWebroot) ? configuration.explicitWebroot
                : "";
        component.openURL(configuration.indexRoot, explicitWebRoot);
    }

    //--------------------------------------------------------------------------

    void parseSync(final CompilerArguments arguments)
    {
        clearProblems();

        compiler.configure(arguments.toArguments());
        boolean success = compiler.compile(false);

        ApplicationManager.getApplication().invokeLater(
                new ProblemRunnable(success));
    }

    void parse(final CompilerArguments arguments)
    {
        clearProblems();

        ProgressManager.getInstance().run(
                new Task.Backgroundable(project,
                        "Randori compiler building project", true, null) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator)
                    {
                        compiler.configure(arguments.toArguments());
                        boolean success = compiler.compile(false);

                        ApplicationManager.getApplication().invokeLater(
                                new ProblemRunnable(success));
                    }
                });
    }

    //--------------------------------------------------------------------------

    void buildSync(boolean doClean, final CompilerArguments arguments)
    {
        if (doClean)
        {
            clean();
        }

        compiler.configure(arguments.toArguments());
        boolean success = compiler.compile(true);

        ApplicationManager.getApplication().invokeLater(
                new ProblemUpdateRunnable());

        // only run if success, the compile will return false
        // with any errors or mis configurations
        if (success)
        {
            buildComplete();
        }

        // this will take care of success and failure notices
        ApplicationManager.getApplication().invokeLater(
                new ProblemBuildRunnable());
    }

    void build(boolean doClean, final CompilerArguments arguments)
    {
        clearProblems();

        if (doClean)
        {
            clean();
        }

        ProgressManager.getInstance().run(
                new Task.Backgroundable(project,
                        "Randori compiler building project", true, null) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator)
                    {
                        compiler.configure(arguments.toArguments());
                        boolean success = compiler.compile(true);

                        // only run if success, the compile will return false
                        // with any errors or mis configurations
                        if (success)
                        {
                            buildComplete();
                        }

                        // this will take care of success and failure notices
                        ApplicationManager.getApplication().invokeLater(
                                new ProblemBuildRunnable());
                    }
                });
    }

    //--------------------------------------------------------------------------

    void configureDependencies(CompilerArguments arguments)
    {

        arguments.clear();

        configure(getState(), arguments);

        for (String library : ProjectUtils.getAllProjectSWCs(project))
        {
            arguments.addLibraryPath(library);
        }

        for (String library : ProjectUtils.getAllProjectSourcePaths(project))
        {
            arguments.addSourcepath(library);
        }

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules)
        {
            // RandoriFlash/src
            for (VirtualFile sourceRoot : ModuleRootManager.getInstance(module)
                    .getSourceRoots())
            {
                arguments.addSourcepath(sourceRoot.getPath());
            }
            if (modifiedFiles != null)
            {
                for (VirtualFile virtualFile : modifiedFiles)
                {
                    arguments.addIncludedSources(virtualFile.getPath());
                }
            }
        }
    }

    void configure(RandoriProjectModel model, CompilerArguments arguments)
    {
        arguments.setAppName(project.getName());
        arguments.setJsBasePath(model.getBasePath());
        arguments.setJsLibraryPath(model.getLibraryPath());
        arguments.setJsOutputAsFiles(model.isClassesAsFile());
        arguments.setOutput(project.getBasePath());
    }

    public boolean validateConfiguration(CompileScope scope)
    {
        if (!ProjectUtils.isSDKInstalled(project))
            return false;

        // TODO Implement the Randori facet for modules.
        for (final Module module : scope.getAffectedModules())
        {
            if (ModuleType.get(module) != RandoriModuleType.getInstance())
            {
                Messages.showErrorDialog(module.getProject(),
                        "This module is not a Randori module",
                        "Can not compile");
                ModulesConfigurator.showDialog(module.getProject(),
                        module.getName(), ClasspathEditor.NAME);
                return false;
            }
        }

        return true;
    }

    private void clean()
    {
        final VirtualFile baseDir = project.getBaseDir();

        // wipe the generated directory
        VirtualFile virtualFile = baseDir.findFileByRelativePath(this
                .getState().getBasePath());
        if (virtualFile != null && virtualFile.exists())
        {
            File fsFile = new File(virtualFile.getPath());
            FileUtil.asyncDelete(fsFile);
        }
    }

    protected void buildComplete()
    {
        RandoriSdk.copySdkLibraries(project);
    }

    protected void clearProblems()
    {
        ApplicationManager.getApplication().invokeLater(
                new ProblemClearRunnable());
    }

    @SuppressWarnings("unused")
    private String toErrorCode(int code)
    {
        switch (code)
        {
        case 1:
            return "Unknown";
        case 2:
            return "Compiler problems";
        case 3:
            return "Compiler Exceptions";
        case 4:
            return "Configuration Problems";
        }

        return "Unknown error code";
    }

    public RandoriProjectCompiler getCompiler() {
        return compiler;
    }

    class ProblemClearRunnable implements Runnable
    {
        @Override
        public void run()
        {
            ProblemsService service = ProblemsService.getInstance(project);
            service.clearProblems();
        }
    }

    class ProblemUpdateRunnable implements Runnable
    {

        ProblemUpdateRunnable()
        {
        }

        @Override
        public void run()
        {
            ProblemsService service = ProblemsService.getInstance(project);
            service.clearProblems();
            service.addAll(compiler.getProblemQuery().getProblems());
        }
    }

    class ProblemRunnable implements Runnable
    {
        private final boolean success;

        ProblemRunnable(boolean success)
        {
            this.success = success;
        }

        @Override
        public void run()
        {
            ProblemsService service = ProblemsService.getInstance(project);

            service.addAll(compiler.getProblemQuery().getProblems());

            if (success)
            {
                service.clearProblems();
                NotificationUtils.sendRandoriInformation("Success",
                        "Successfully compiled project", project);
            }
            else
            {
                if (service.hasErrors())
                {
                    NotificationUtils.sendRandoriError("Error",
                            "Error(s) in project, Check the <a href='"
                                    + ProblemsToolWindowFactory.WINDOW_ID
                                    + "'>"
                                    + ProblemsToolWindowFactory.WINDOW_ID
                                    + "</a> for more information", project);
                }
            }
        }
    }

    class ProblemBuildRunnable implements Runnable
    {
        ProblemBuildRunnable()
        {
        }

        @Override
        public void run()
        {
            ProblemsService service = ProblemsService.getInstance(project);

            service.addAll(compiler.getProblemQuery().getProblems());

            if (service.hasErrors())
            {
                NotificationUtils.sendRandoriError("Error",
                        "Error(s) in project, Check the <a href='"
                                + ProblemsToolWindowFactory.WINDOW_ID + "'>"
                                + ProblemsToolWindowFactory.WINDOW_ID
                                + "</a> for more information", project);
            }
            else
            {
                NotificationUtils.sendRandoriInformation("Success",
                        "Successfully compiled and built project", project);
            }

        }

    }

}
