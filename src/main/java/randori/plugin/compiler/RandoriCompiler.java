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

package randori.plugin.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.configuration.RandoriProjectModel;

import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerBundle;
import com.intellij.openapi.compiler.TranslatingCompiler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Chunk;
import com.intellij.util.ThrowableRunnable;
import randori.plugin.util.ProjectUtils;

/**
 * IDEA Compiler class for calling the internal compiler API.
 * 
 * @author Frédéric THOMAS
 */
class RandoriCompiler implements TranslatingCompiler
{

    private static final Logger LOG = Logger.getInstance("#randori.compiler.RandoriCompiler");
    private final Project project;
    private final RandoriProjectComponent projectComponent;
    private int sessionId;
    private RandoriCompilerSession compilerSession;

    public RandoriCompiler(Project project)
    {
        this.project = project;

        projectComponent = project.getComponent(RandoriProjectComponent.class);
    }

    @Override
    public boolean isCompilableFile(VirtualFile file, CompileContext context)
    {
        return FileUtilRt.extensionEquals(file.getPath(), ActionScriptFileType.INSTANCE.getDefaultExtension());
    }

    @Override
    public void compile(CompileContext context, Chunk<Module> moduleChunk, VirtualFile[] files, OutputSink sink)
    {
        context.getProgressIndicator().checkCanceled();
        int moduleCount = moduleChunk.getNodes().size();

        for (Module module : moduleChunk.getNodes()) {
            moduleCount -= 1;
            List<VirtualFile> modifiedFiles = project.getComponent(RandoriProjectComponent.class).getModifiedFiles();

            boolean doClean = true;
            boolean success;

            if (context.hashCode() != sessionId) {
                sessionId = context.hashCode();
                compilerSession = new RandoriCompilerSession(project);
            } else
                doClean = false;

            if (context.isMake() && modifiedFiles.size() > 0) {
                LOG.info("Starting Randori compiler... Make " + module.getName());
                context.getProgressIndicator().setText("Starting Randori compiler... Make " + module.getName());

                success = compilerSession.make(module);
            } else if (context.isRebuild()) {
                if (doClean)
                    clearAffectedOutputPathsIfPossible(context);

                LOG.info("Starting Randori compiler... Rebuild " + module.getName());
                context.getProgressIndicator().setText("Starting Randori compiler... Rebuild " + module.getName());

                success = compilerSession.build(module);
            } else
                return;

            final boolean isLastModule = moduleCount == 0;
            if (success) {
                if (context.isMake() && modifiedFiles.size() > 0 && isLastModule)
                    modifiedFiles.removeAll(modifiedFiles);
            } else {
                context.getProgressIndicator().cancel();
            }

            // If compilation failed or it is the last module, Show the problem window
            if (!success || isLastModule)
                ApplicationManager.getApplication().invokeLater(
                        new RandoriCompilerSession.ProblemBuildRunnable(project, RandoriCompilerSession
                                .getLastCompiler(), true));
        }
    }

    @NotNull
    @Override
    public String getDescription()
    {
        return "Randori Compiler";
    }

    @Override
    public boolean validateConfiguration(CompileScope scope)
    {
        return projectComponent.validateConfiguration(scope);
    }

    private void clearAffectedOutputPathsIfPossible(final CompileContext context)
    {
        final List<File> outPutDirs = new ReadAction<List<File>>() {
            protected void run(final Result<List<File>> result)
            {
                final List<File> dirs = new ArrayList<File>();
                final RandoriProjectModel projectModel = project.getComponent(RandoriProjectComponent.class).getState();
                final VirtualFile baseDir = project.getBaseDir();
                assert projectModel != null;
                baseDir.findFileByRelativePath(projectModel.getBasePath());
                File outPutDir = new File(FileUtil.toSystemDependentName(baseDir.getPath() + File.separator
                        + projectModel.getBasePath()));
                dirs.add(outPutDir);
                result.setResult(dirs);
            }
        }.execute().getResultObject();
        if (outPutDirs.size() > 0)
        {
            CompilerUtil.runInContext(context, CompilerBundle.message("progress.clearing.output"),
                    new ThrowableRunnable<RuntimeException>() {
                        public void run()
                        {
                            CompilerUtil.clearOutputDirectories(outPutDirs);
                        }
                    });
        }
    }
}
