package randori.plugin.workspaces;

import org.jetbrains.annotations.NotNull;

import randori.compiler.clients.CompilerArguments;
import randori.compiler.projects.IRandoriApplicationProject;
import randori.plugin.service.ProblemsService;
import randori.plugin.ui.ProblemsToolWindowFactory;
import randori.plugin.utils.NotificationUtils;
import randori.plugin.utils.ProjectUtils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

/**
 * @author Michael Schmalle
 */
public class RandoriApplicationComponent implements ProjectComponent
{
    private Project project;

    private IWorkspaceApplication workspaceApplication;

    private IRandoriApplicationProject randoriApplication;

    public RandoriApplicationComponent(Project project,
            IWorkspaceApplication workspaceApplication)
    {
        this.project = project;
        this.workspaceApplication = workspaceApplication;
    }

    public void initComponent()
    {
    }

    public void disposeComponent()
    {
        if (!ProjectUtils.hasRandoriModuleType(project))
            return;

        randoriApplication = null;
    }

    @NotNull
    public String getComponentName()
    {
        return "RandoriApplicationComponent";
    }

    public void projectOpened()
    {
        if (!ProjectUtils.hasRandoriModuleType(project))
            return;

        randoriApplication = (IRandoriApplicationProject) workspaceApplication
                .addProject(project);
    }

    public void projectClosed()
    {
        // called when project is being closed
    }

    //--------------------------------------------------------------------------

    public void parseSync(final Project project,
            final CompilerArguments arguments)
    {
        final ProblemsService service = ProblemsService.getInstance(project);

        // clear the problems for the next parse
        service.clearProblems();

        randoriApplication.configure(arguments.toArguments());
        boolean success = randoriApplication.compile(false);

        ApplicationManager.getApplication().invokeLater(
                new ProblemRunnable(success, randoriApplication));
    }

    public void parse(final Project project, final CompilerArguments arguments)
    {
        final ProblemsService service = ProblemsService.getInstance(project);

        service.clearProblems();

        ProgressManager.getInstance().run(
                new Task.Backgroundable(project,
                        "Randori compiler building project", true, null) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator)
                    {
                        randoriApplication.configure(arguments.toArguments());
                        boolean success = randoriApplication.compile(false);

                        ApplicationManager.getApplication()
                                .invokeLater(
                                        new ProblemRunnable(success,
                                                randoriApplication));
                    }
                });
    }

    class ProblemRunnable implements Runnable
    {
        private final boolean success;

        private final IRandoriApplicationProject application;

        ProblemRunnable(boolean success, IRandoriApplicationProject application)
        {
            this.success = success;
            this.application = application;
        }

        @Override
        public void run()
        {
            ProblemsService service = ProblemsService.getInstance(project);

            service.addAll(application.getProblemQuery().getProblems());

            if (success)
            {
                service.clearProblems();
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
}
