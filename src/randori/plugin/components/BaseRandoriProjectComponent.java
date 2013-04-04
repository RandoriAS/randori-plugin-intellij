package randori.plugin.components;

import com.intellij.openapi.application.ApplicationManager;
import org.apache.flex.compiler.problems.ICompilerProblem;

import randori.compiler.clients.CompilerArguments;
import randori.plugin.module.RandoriModuleType;
import randori.plugin.runner.RandoriRunConfiguration;
import randori.plugin.runner.RandoriServerComponent;
import randori.plugin.service.ProblemsService;
import randori.plugin.utils.ProjectUtils;
import randori.plugin.utils.VFileUtils;
import randori.plugin.workspaces.IWorkspaceApplication;

import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ui.configuration.ClasspathEditor;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

public class BaseRandoriProjectComponent {

    protected RandoriProjectModel model;
    private Project project;

    public BaseRandoriProjectComponent(Project project) {
        this.project = project;
        this.model = new RandoriProjectModel();
    }

    public Project getProject() {
        return project;
    }

    public ProblemsService getProblemsService() {
        return ProblemsService.getInstance(project);
    }

    public void run(RandoriRunConfiguration configuration) {
        RandoriServerComponent component = getProject().getComponent(RandoriServerComponent.class);
        String explicitWebroot = (configuration.useExplicitWebroot) ? configuration.explicitWebroot : "";
        component.openURL(configuration.indexRoot, explicitWebroot);
    }

    public void reparse(VirtualFile file, boolean sync) {
        // parses a file and its dependencies
        // TEMP
        parse(sync);
    }

    public void parse(boolean sync) {
        CompilerArguments arguments = new CompilerArguments();
        configureDependencies(project, arguments);

        IWorkspaceApplication component = ApplicationManager.getApplication().getComponent(
                IWorkspaceApplication.class);
        
        if (sync) {
            component.parseSync(project, arguments);
        } else {
            component.parse(project, arguments);
        }
    }

    /**
     * Builds the current Project by doing a full parse and output render.
     *
     * @param doClean
     */
    public void build(boolean doClean) {
        build(null, doClean, true);
    }

    public void build(VirtualFile[] files, boolean doClean, boolean sync) {
        CompilerArguments arguments = new CompilerArguments();
        configureDependencies(project, arguments, files);

        IWorkspaceApplication component = ApplicationManager.getApplication().getComponent(
                        IWorkspaceApplication.class);

        if (sync) {
            component.buildSync(project, doClean, arguments);
        } else {
            component.build(project, doClean, arguments);
        }
    }

    /**
     * Opens a ICompilerProblem in a new editor, or opens the editor and places
     * the caret a the specific problem.
     *
     * @param problem The ICompilerProblem to focus.
     */
    public void openFileForProblem(ICompilerProblem problem) {
        VirtualFile virtualFile = VFileUtils.getFile(problem.getSourcePath());
        if (virtualFile != null) {
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile);
            if (descriptor != null) {
                Editor editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
                if (editor != null) {
                    LogicalPosition position = new LogicalPosition(problem.getLine(), problem.getColumn());
                    editor.getCaretModel().moveToLogicalPosition(position);
                }
            }
        }
    }

    public void configureDependencies(Project project, CompilerArguments arguments) {
        configureDependencies(project, arguments, null);
    }

    public void configureDependencies(Project project, CompilerArguments arguments, VirtualFile[] virtualFiles) {
        arguments.clear();

        configure(project, getModel(), arguments);

        for (String library : ProjectUtils.getAllProjectSWCs(project)) {
            arguments.addLibraryPath(library);
        }

        for (String library : ProjectUtils.getAllProjectSourcePaths(project)) {
            arguments.addSourcepath(library);
        }

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            // RandoriFlash/src
            for (VirtualFile sourceRoot : ModuleRootManager.getInstance(module).getSourceRoots()) {
                arguments.addSourcepath(sourceRoot.getPath());
            }
            if (virtualFiles != null) {
                for (VirtualFile virtualFile : virtualFiles) {
                    arguments.addIncludedSources(virtualFile.getPath());
                }
            }
        }
    }

    public RandoriProjectModel getModel() {
        return model;
    }

    public boolean validateConfiguration(CompileScope scope) {

        if (!ProjectUtils.isSDKInstalled(project)) {
            return false;
        }

        // TODO Implement the Randori facet for modules.
        for (final Module module : scope.getAffectedModules()) {
            if (ModuleType.get(module) != RandoriModuleType.getInstance()) {
                Messages.showErrorDialog(module.getProject(), "This module is not a Randori module", "Can not compile");
                ModulesConfigurator.showDialog(module.getProject(), module.getName(), ClasspathEditor.NAME);
                return false;
            }
        }

        return true;
    }

    public void configure(Project project, RandoriProjectModel model, CompilerArguments arguments) {
        arguments.setAppName(project.getName());
        arguments.setJsBasePath(model.getBasePath());
        arguments.setJsLibraryPath(model.getLibraryPath());
        arguments.setJsOutputAsFiles(model.isClassesAsFile());
        arguments.setOutput(project.getBasePath());
    }
}
