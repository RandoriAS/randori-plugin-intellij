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

package randori.plugin.workspaces;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.jetbrains.annotations.NotNull;

import randori.compiler.clients.CompilerArguments;
import randori.compiler.clients.Randori;
import randori.compiler.driver.IBackend;
import randori.compiler.internal.driver.RandoriBackend;
import randori.plugin.builder.FileChangeListener;
import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.roots.RandoriSdk;
import randori.plugin.service.ProblemsService;
import randori.plugin.ui.ProblemsToolWindowFactory;
import randori.plugin.utils.NotificationUtils;
import randori.plugin.utils.ProjectUtils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;

/**
 * XXX I am really thinking this needs to be a service and only be instantiated
 * when a Project with a Randori module type is opened. (will get there) The the
 * Workspace only exists in the proper context, where here it gets instantiated
 * in every project.
 * 
 * @author Michael Schmalle
 */
public class WorkspaceApplicationComponent implements ApplicationComponent,
        IWorkspaceApplication
{
    private Workspace workspace;

    private Map<String, FlexProject> map = new HashMap<String, FlexProject>();

    // XXX I have moved this here for clarity in later refactoring
    // if a file changes with 2+ projects opened, how is that handled?
    private VirtualFileListener fileChangeListener;

    public WorkspaceApplicationComponent()
    {
    }

    @Override
    public Workspace getWorkspace()
    {
        return workspace;
    }

    @Override
    public ICompilerProject addProject(Project project)
    {
        if (map.containsKey(project.getName()))
            return null;

        FlexProject result = new FlexProject(workspace);
        map.put(project.getName(), result);
        return result;
    }

    @Override
    public void initComponent()
    {
        workspace = new Workspace();

        fileChangeListener = new FileChangeListener();
        VirtualFileManager.getInstance().addVirtualFileListener(
                fileChangeListener);
    }

    @Override
    public void disposeComponent()
    {
        VirtualFileManager.getInstance().removeVirtualFileListener(
                fileChangeListener);
    }

    @Override
    @NotNull
    public String getComponentName()
    {
        return "WorkspaceApplicationComponent";
    }

    @SuppressWarnings("unused")
    private void startupApplication()
    {

        //FlexProjectConfigurator.configure(project);

        //List<File> sourcePath = new ArrayList<File>();
        //sourcePath.add(new File(tempDir));
        //project.setSourcePath(sourcePath);

        //List<File> libraries = new ArrayList<File>();
        //project.addSourcePathFile();
        //project.setLibraries();
        //        compilationSuccess = application.build(
        //                (IRandoriBackend) backend, problems);
    }

    //--------------------------------------------------------------------------
    // TEMP UNTIL the build refactor
    //--------------------------------------------------------------------------

    public void build(final Project project, boolean doClean,
            final CompilerArguments arguments)
    {
        final String name = project.getName();
        // final VirtualFile file = project.getBaseDir();

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
                        IBackend backend = new RandoriBackend();
                        final Randori randori = new Randori(backend);
                        final int code = randori.mainNoExit(
                                arguments.toArguments(), service.getProblems());

                        if (code == 0)
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

    public void buildSync(final Project project, boolean doClean,
            final CompilerArguments arguments)
    {
        final String name = project.getName();
        // final VirtualFile file = project.getBaseDir();

        final ProblemsService service = ProblemsService.getInstance(project);

        // clear the problems for the next parse
        service.clearProblems();

        if (doClean)
        {
            clean(project);
        }

        IBackend backend = new RandoriBackend();
        final Randori randori = new Randori(backend);
        final int code = randori.mainNoExit(arguments.toArguments(),
                service.getProblems());

        if (code == 0)
        {
            NotificationUtils.sendRandoriInformation("Success",
                    "Successfully compiled and built project '" + name + "'",
                    project);
        }
        else
        {

            service.filter();

            if (service.hasErrors())
            {
                NotificationUtils.sendRandoriError("Error",
                        "Error(s) in project, Check the <a href='"
                                + ProblemsToolWindowFactory.WINDOW_ID + "'>"
                                + ProblemsToolWindowFactory.WINDOW_ID
                                + "</a> for more information '"
                                + toErrorCode(code) + "'", project);
            }
            else
            {
                // XXX This is temp until I get the ProblemQuery
                // yanked out of the compiler
                // this would hit here if there are still
                // Warnings but the build passed
                NotificationUtils.sendRandoriWarning("Success",
                        "Successfully compiled and built project with warnings '"
                                + name + "'", project);
            }
        }

        RandoriSdk.copySdkLibraries(project);
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
