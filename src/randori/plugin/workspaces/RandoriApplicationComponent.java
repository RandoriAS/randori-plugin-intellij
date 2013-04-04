package randori.plugin.workspaces;

import org.jetbrains.annotations.NotNull;

import randori.compiler.clients.CompilerArguments;
import randori.compiler.clients.Randori;
import randori.compiler.internal.driver.RandoriBackend;
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
        if (!ProjectUtils.hasRandoriModuleType(project))
            return;

        randoriApplication = (IRandoriApplicationProject) workspaceApplication
                .addProject(project);
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
        // called when project is opened
    }

    public void projectClosed()
    {
        // called when project is being closed
    }

    //--------------------------------------------------------------------------

    public void parseSync(final Project project,
            final CompilerArguments arguments)
    {
        // final String name = project.getName();
        final ProblemsService service = ProblemsService.getInstance(project);

        // clear the problems for the next parse
        service.clearProblems();

        randoriApplication.configure(arguments.toArguments());
        boolean success = randoriApplication.compile(false);

        service.addAll(randoriApplication.getProblemQuery().getProblems());

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
                                + ProblemsToolWindowFactory.WINDOW_ID + "'>"
                                + ProblemsToolWindowFactory.WINDOW_ID
                                + "</a> for more information", project);
            }
        }
    }

    public void parse(final Project project, final CompilerArguments arguments)
    {
        // final String name = project.getName();
        final ProblemsService service = ProblemsService.getInstance(project);

        // clear the problems for the next parse
        service.clearProblems();

        ProgressManager.getInstance().run(
                new Task.Backgroundable(project,
                        "Randori compiler building project", true, null) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator)
                    {
                        //problems.clear();

                        RandoriBackend backend = new RandoriBackend();
                        backend.parseOnly(true);
                        final Randori randori = new Randori(backend);

                        // need to only parse not generate
                        final int code = randori.mainNoExit(
                                arguments.toArguments(), service.getProblems());

                        if (code == 0)
                        {
                            service.clearProblems();
                        }
                        else
                        {
                            // all this needs to be run on the UI thread
                            ApplicationManager.getApplication().invokeLater(
                                    new Runnable() {
                                        @Override
                                        public void run()
                                        {
                                            service.filter();

                                            if (service.hasErrors())
                                            {
                                                NotificationUtils
                                                        .sendRandoriError(
                                                                "Error",
                                                                "Error(s) in project, Check the <a href='"
                                                                        + ProblemsToolWindowFactory.WINDOW_ID
                                                                        + "'>"
                                                                        + ProblemsToolWindowFactory.WINDOW_ID
                                                                        + "</a> for more information '"
                                                                        + toErrorCode(code)
                                                                        + "'",
                                                                project);
                                            }
                                        }
                                    });
                        }
                    }
                });
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
