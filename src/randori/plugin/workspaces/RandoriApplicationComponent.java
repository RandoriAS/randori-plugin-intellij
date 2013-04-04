package randori.plugin.workspaces;

import java.io.File;

import org.jetbrains.annotations.NotNull;

import randori.compiler.clients.CompilerArguments;
import randori.compiler.projects.IRandoriApplicationProject;
import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.components.RandoriProjectModel;
import randori.plugin.module.RandoriModuleType;
import randori.plugin.roots.RandoriSdk;
import randori.plugin.service.ProblemsService;
import randori.plugin.ui.ProblemsToolWindowFactory;
import randori.plugin.utils.NotificationUtils;
import randori.plugin.utils.ProjectUtils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
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

        System.out.println("projectOpened()");
        build(null, false, false);
    }

    public void projectClosed()
    {
        // called when project is being closed
    }

    public void build(VirtualFile[] files, boolean doClean, boolean sync)
    {
        CompilerArguments arguments = new CompilerArguments();
        configureDependencies(project, arguments, files);

        if (sync)
        {
            buildSync(project, doClean, arguments);
        }
        else
        {
            build(project, doClean, arguments);
        }
    }

    public void parse(boolean sync)
    {
        CompilerArguments arguments = new CompilerArguments();
        configureDependencies(project, arguments);

        if (sync)
        {
            parseSync(project, arguments);
        }
        else
        {
            parse(project, arguments);
        }
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

    //--------------------------------------------------------------------------

    public void buildSync(final Project project, boolean doClean,
            final CompilerArguments arguments)
    {
        final String name = project.getName();
        // final VirtualFile file = project.getBaseDir();

        final ProblemsService service = ProblemsService.getInstance(project);

        if (doClean)
        {
            clean(project);
        }

        randoriApplication.configure(arguments.toArguments());
        boolean success = randoriApplication.compile(true);

        ApplicationManager.getApplication().invokeLater(
                new ProblemUpdateRunnable(randoriApplication));

        if (success)
        {
            NotificationUtils.sendRandoriInformation("Success",
                    "Successfully compiled and built project '" + name + "'",
                    project);
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
            else
            {
                NotificationUtils.sendRandoriWarning("Success",
                        "Successfully compiled and built project with warnings '"
                                + name + "'", project);
            }
        }

        RandoriSdk.copySdkLibraries(project);
    }

    public void build(final Project project, boolean doClean,
            final CompilerArguments arguments)
    {
        final String name = project.getName();
        final ProblemsService service = ProblemsService.getInstance(project);

        // clear the problems for the next parse
        service.clearProblems();

        if (doClean)
        {
            clean(project);
        }

        ProgressManager.getInstance().run(
                new Task.Backgroundable(project,
                        "Randori compiler building project", true, null) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator)
                    {
                        randoriApplication.configure(arguments.toArguments());
                        boolean success = randoriApplication.compile(true);

                        if (success)
                        {
                            NotificationUtils.sendRandoriInformation("Success",
                                    "Successfully compiled and built project '"
                                            + name + "'", project);
                        }
                        else
                        {
                            // all this needs to be run on the UI thread
                            ApplicationManager.getApplication().invokeLater(
                                    new Runnable() {
                                        @Override
                                        public void run()
                                        {
                                            service.addAll(randoriApplication
                                                    .getProblemQuery()
                                                    .getProblems());

                                            if (service.hasErrors())
                                            {
                                                NotificationUtils
                                                        .sendRandoriError(
                                                                "Error",
                                                                "Error(s) in project, Check the <a href='"
                                                                        + ProblemsToolWindowFactory.WINDOW_ID
                                                                        + "'>"
                                                                        + ProblemsToolWindowFactory.WINDOW_ID
                                                                        + "</a> for more information",
                                                                project);
                                            }
                                            else
                                            {
                                                // XXX This is temp until I get the ProblemQuery
                                                // yanked out of the compiler
                                                // this would hit here if there are still
                                                // Warnings but the build passed
                                                NotificationUtils
                                                        .sendRandoriWarning(
                                                                "Success",
                                                                "Successfully compiled and built project with warnings '"
                                                                        + name
                                                                        + "'",
                                                                project);
                                            }
                                        }
                                    });
                        }

                        RandoriSdk.copySdkLibraries(project);
                    }
                });
    }

    //--------------------------------------------------------------------------

    public void configureDependencies(Project project,
            CompilerArguments arguments)
    {
        configureDependencies(project, arguments, null);
    }

    public void configureDependencies(Project project,
            CompilerArguments arguments, VirtualFile[] virtualFiles)
    {
        RandoriProjectComponent component = project
                .getComponent(RandoriProjectComponent.class);

        arguments.clear();

        configure(project, component.getModel(), arguments);

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
            if (virtualFiles != null)
            {
                for (VirtualFile virtualFile : virtualFiles)
                {
                    arguments.addIncludedSources(virtualFile.getPath());
                }
            }
        }
    }

    public void configure(Project project, RandoriProjectModel model,
            CompilerArguments arguments)
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

    private void clean(Project project)
    {
        final VirtualFile baseDir = project.getBaseDir();
        final RandoriProjectComponent component = ProjectUtils
                .getProjectComponent(project);

        // wipe the generated directory
        VirtualFile virtualFile = baseDir.findFileByRelativePath(component
                .getModel().getBasePath());
        if (virtualFile != null && virtualFile.exists())
        {
            File fsFile = new File(virtualFile.getPath());
            FileUtil.asyncDelete(fsFile);
        }
    }

    class ProblemUpdateRunnable implements Runnable
    {
        private final IRandoriApplicationProject application;

        ProblemUpdateRunnable(IRandoriApplicationProject application)
        {
            this.application = application;
        }

        @Override
        public void run()
        {
            ProblemsService service = ProblemsService.getInstance(project);
            service.clearProblems();
            service.addAll(application.getProblemQuery().getProblems());
        }
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
