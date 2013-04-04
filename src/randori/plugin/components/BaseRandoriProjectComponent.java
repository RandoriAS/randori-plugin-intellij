package randori.plugin.components;

import org.apache.flex.compiler.problems.ICompilerProblem;

import randori.plugin.runner.RandoriRunConfiguration;
import randori.plugin.runner.RandoriServerComponent;
import randori.plugin.service.ProblemsService;
import randori.plugin.utils.VFileUtils;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class BaseRandoriProjectComponent
{

    protected RandoriProjectModel model;
    private Project project;

    public BaseRandoriProjectComponent(Project project)
    {
        this.project = project;
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
        String explicitWebroot = (configuration.useExplicitWebroot) ? configuration.explicitWebroot
                : "";
        component.openURL(configuration.indexRoot, explicitWebroot);
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

    public RandoriProjectModel getModel()
    {
        return model;
    }

}
