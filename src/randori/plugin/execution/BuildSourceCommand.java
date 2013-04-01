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

package randori.plugin.execution;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;

import randori.compiler.clients.Randori;
import randori.compiler.driver.IBackend;
import randori.compiler.internal.driver.RandoriBackend;
import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.roots.RandoriSdk;
import randori.plugin.service.ProblemsService;
import randori.plugin.ui.ProblemsToolWindowFactory;
import randori.plugin.utils.NotificationUtils;
import randori.plugin.utils.ProjectUtils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Michael Schmalle
 */
public class BuildSourceCommand
{

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

    public void build(final Project project, boolean doClean,
            final CompilerArguments arguments)
    {
        final String name = project.getName();
        // final VirtualFile file = project.getBaseDir();

        final ProblemsService service = ProblemsService.getInstance(project);

        // clear the problems for the next parse
        service.clearProblems();

        if (doClean)
            clean(project);

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
        final VirtualFile file = project.getBaseDir();
        final RandoriProjectComponent component = ProjectUtils
                .getProjectComponent(project);

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run()
            {
                // wipe the generated directory
                VirtualFile virtualFile = file.findFileByRelativePath(component
                        .getModel().getBasePath());
                try
                {
                    if (virtualFile != null && virtualFile.exists())
                    {
                        virtualFile.delete(this);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
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
