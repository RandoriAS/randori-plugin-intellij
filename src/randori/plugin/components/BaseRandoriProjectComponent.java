package randori.plugin.components;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.flex.compiler.problems.ICompilerProblem;
import randori.plugin.execution.CompilerArguments;
import randori.plugin.runner.RandoriRunConfiguration;
import randori.plugin.runner.RandoriServerComponent;
import randori.plugin.service.ProblemsService;
import randori.plugin.utils.ProjectUtils;
import randori.plugin.utils.VFileUtils;

public class BaseRandoriProjectComponent {
    protected RandoriProjectModel model;
    private Project project;
    private final RandoriBundleApplicationComponent applicationComponent;

    public BaseRandoriProjectComponent(Project project, RandoriBundleApplicationComponent applicationComponent) {
        this.project = project;
        this.applicationComponent = applicationComponent;
        this.model = new RandoriProjectModel();
    }

    public Project getProject()
    {
        return project;
    }
    public ProblemsService getProblemsService()
    {
        return ProblemsService.getInstance(project);
    }

    public void run(RandoriRunConfiguration configuration)
    {
        RandoriServerComponent component = getProject().getComponent(
                RandoriServerComponent.class);
        String explicitWebroot = (configuration.useExplicitWebroot) ? configuration.explicitWebroot : "";
        component.openURL(configuration.indexRoot, explicitWebroot);
    }

    public void reparse(VirtualFile file)
    {
        // parses a file and its dependencies
        // TEMP
        parse();
    }

    public void parse()
    {
        CompilerArguments arguments = applicationComponent
                .getCompilerArguments();
        configureDependencies(project, arguments);

        applicationComponent.getBuildSourceCommand().parse(project, arguments);
    }

    /**
     * Builds the current Project by doing a full parse and output render.
     *
     * @param doClean
     */
    public void build(boolean doClean)
    {
        CompilerArguments arguments = applicationComponent
                .getCompilerArguments();
        configureDependencies(project, arguments);

        applicationComponent.getBuildSourceCommand().build(project, doClean,
                arguments);
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

    public void configureDependencies(Project project,
            CompilerArguments arguments)
    {
        arguments.clear();

        arguments.configure(project, getModel());

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
            for (VirtualFile virtualFile : ModuleRootManager
                    .getInstance(module).getSourceRoots())
            {
                arguments.addSourcepath(virtualFile.getPath());
            }
        }
    }

    public RandoriProjectModel getModel()
    {
        return model;
    }
}
