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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.jetbrains.annotations.NotNull;
import randori.compiler.bundle.BundleConfiguration;
import randori.compiler.bundle.IBundleConfiguration;
import randori.compiler.bundle.IBundleConfigurationEntry;
import randori.compiler.clients.CompilerArguments;
import randori.compiler.internal.projects.RandoriProject;
import randori.compiler.plugin.IPreProcessPlugin;
import randori.plugin.components.DummyPreProcessPlugin;
import randori.plugin.components.RandoriApplicationComponent;
import randori.plugin.components.RandoriModuleComponent;
import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.configuration.RandoriCompilerModel;
import randori.plugin.configuration.RandoriModuleModel;
import randori.plugin.library.RandoriLibraryType;
import randori.plugin.roots.RandoriSdkType;
import randori.plugin.service.ProblemsService;
import randori.plugin.ui.ProblemsToolWindowFactory;
import randori.plugin.util.NotificationUtils;
import randori.plugin.util.ProjectUtils;

import java.io.File;
import java.util.List;

/**
 * @author Frédéric THOMAS Date: 13/04/13 Time: 13:27
 */
public class RandoriCompilerSession
{
    private static RandoriProject lastCompiler;

    private final Project project;

    private final Workspace workspace;
    private Module module;
    private List<VirtualFile> modifiedFiles;
    private final RandoriCompilerModel projectModel;
    private RandoriModuleModel moduleModel;
    private RandoriProject compiler;
    private String rblOutputPath;
    private List<Module> usedModules;
    private boolean isRebuild;
    private boolean isMake;
    private boolean isModuleRoot;

    public static void parseAll(@NotNull Project project)
    {
        if (ProjectUtils.isSDKInstalled(project) && ProjectUtils.hasRandoriModuleType(project))
        {
            Module[] sortedModules = ModuleManager.getInstance(project).getSortedModules();

            RandoriCompilerSession compilerSession = null;

            for (Module module : sortedModules)
            {
                if (compilerSession == null)
                    compilerSession = new RandoriCompilerSession(project);

                if (!compilerSession.parse(module))
                {
                    ApplicationManager.getApplication().invokeLater(
                            new ProblemBuildRunnable(project, lastCompiler, false));
                    return;
                }
            }
            ApplicationManager.getApplication().invokeLater(new ProblemBuildRunnable(project, lastCompiler, false));
        }
    }

    public static RandoriProject getLastCompiler()
    {
        return lastCompiler;
    }

    public RandoriCompilerSession(@NotNull Project project)
    {
        this.project = project;
        projectModel = RandoriCompilerModel.getInstance(project).getState();
        workspace = new Workspace();
    }

    //--------------------------------------------------------------------------

    public Workspace getWorkspace()
    {
        return workspace;
    }

    //--------------------------------------------------------------------------

    boolean parse(@NotNull Module module)
    {
        return build(module, false, false);
    }

    boolean make(@NotNull Module module)
    {
        isMake = true;
        return build(module, true, false);
    }

    public boolean build(@NotNull Module module)
    {
        isRebuild = true;
        return build(module, true, true);
    }

    private boolean build(Module module, boolean doBuild, boolean doExport)
    {
        boolean success = true;

        isModuleRoot = ProjectUtils.isRandoriWebModule(module);

        prepareCompilation(module);

        if (isModuleRoot || isMake || (isRebuild && moduleModel.isGenerateRbl()))
        {
            CompilerArguments arguments = new CompilerArguments();
            configureDependencies(arguments);
            final IBundleConfiguration configuration;

            clearProblems();

            if (compiler instanceof RandoriProjectCompiler)
                compiler.configure(arguments.toArguments());
            else if (compiler instanceof RandoriBundleCompiler)
            {
                configuration = createConfiguration(arguments);
                ((RandoriBundleCompiler) compiler).configure(configuration);
            }

            // Checked twice because the compiler.compile return true even if it throws errors
            success = compiler.compile(doBuild, doExport) && !lastCompiler.getProblemQuery().hasErrors();

            if (success && doExport)
            {
                RandoriSdkType.copySdkLibraries(project);
            }
        }

        return success;
    }

    void prepareCompilation(Module module)
    {
        this.module = module;

        //workspace = new Workspace();
        lastCompiler = compiler = isModuleRoot ? new RandoriProjectCompiler(workspace) : new RandoriBundleCompiler(
                workspace);

        compiler.getPluginFactory().registerPlugin(IPreProcessPlugin.class, DummyPreProcessPlugin.class);

        final RandoriProjectComponent projectComponent = project.getComponent(RandoriProjectComponent.class);
        final RandoriModuleComponent moduleComponent = module.getComponent(RandoriModuleComponent.class);

        if (moduleComponent != null)
        {
            modifiedFiles = projectComponent.getModifiedFiles();
            moduleModel = moduleComponent.getState();
            usedModules = moduleComponent.getDependencies();

            String librariesOutputPath = FileUtil.toSystemDependentName(project.getBasePath() + File.separator
                    + projectModel.getLibraryPath());
            String libraryOutputPath = librariesOutputPath + File.separator + module.getName();
            rblOutputPath = libraryOutputPath + File.separator + module.getName()
                    + RandoriLibraryType.LIBRARY_DOT_EXTENSION;
        }
    }

    //--------------------------------------------------------------------------

    private void configureDependencies(CompilerArguments arguments)
    {
        arguments.clear();

        configure(projectModel, moduleModel, arguments);

        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();

        if (projectSdk != null)
        {
            arguments.setSDKPath(projectSdk.getHomePath());

            if (usedModules != null)
                for (Module moduleLib : usedModules)
                {
                    for (String modulePath : ProjectUtils.getAllModuleSourcePaths(moduleLib))
                    {
                        arguments.addSourcepath(modulePath);
                    }
                }

            for (VirtualFile sourceRoot : ModuleRootManager.getInstance(module).getSourceRoots())
            {
                arguments.addSourcepath(sourceRoot.getPath());
            }

            if (!isRebuild && modifiedFiles != null)
            {
                for (VirtualFile virtualFile : modifiedFiles)
                {
                    arguments.addIncludedSources(virtualFile.getPath());
                }
            }

            Module[] modules = isModuleRoot ? ModuleManager.getInstance(project).getModules() : new Module[] { module };
            final VirtualFile[] libraryRoots = LibraryUtil.getLibraryRoots(modules, false, false);

            for (VirtualFile library : libraryRoots)
            {
                final String libraryPath = library.getPath();

                if (FileUtilRt.getExtension(libraryPath).equals(
                        RandoriApplicationComponent.RBL_FILE_TYPE.getDefaultExtension()))
                {
                    arguments.addBundlePath(libraryPath);
                }
                else
                    arguments.addLibraryPath(libraryPath);
            }
        }
    }

    private void configure(RandoriCompilerModel projectModel, RandoriModuleModel moduleModel,
            CompilerArguments arguments)
    {
        arguments.setAppName(project.getName());
        arguments.setJsBasePath(projectModel.getBasePath());
        arguments.setJsLibraryPath(projectModel.getLibraryPath());
        arguments.setJsOutputAsFiles(moduleModel.isExportAsFile());

        if (compiler instanceof RandoriProjectCompiler)
        {
            arguments.setOutput(project.getBasePath());
        }
        else if (compiler instanceof RandoriBundleCompiler)
        {
            arguments.setOutput(rblOutputPath);
        }
    }

    private IBundleConfiguration createConfiguration(CompilerArguments arguments)
    {
        BundleConfiguration configuration = new BundleConfiguration(module.getName(), arguments.getOutput());

        for (String libraryPath : arguments.getLibraries())
        {
            configuration.addLibraryPath(libraryPath);
        }

        for (String bundlePath : arguments.getBundles())
        {
            configuration.addBundlePath(bundlePath);
        }

        IBundleConfigurationEntry entry = configuration.addEntry(module.getName());

        for (String sourcePath : arguments.getSources())
        {
            entry.addSourcePath(sourcePath);
        }

        for (String sourceIncludedPath : arguments.getIncludes())
        {
            entry.addIncludeSources(sourceIncludedPath);
        }

        configuration.setSDKPath(arguments.getSDKPath());

        return configuration;
    }

    void clearProblems()
    {
        ApplicationManager.getApplication().invokeLater(new ProblemClearRunnable());
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

    private class ProblemClearRunnable implements Runnable
    {
        @Override
        public void run()
        {
            ProblemsService service = ProblemsService.getInstance(project);
            service.clearProblems();
        }
    }

    public static class ProblemBuildRunnable implements Runnable
    {
        private final Project project;
        private final RandoriProject compiler;
        private final boolean isBuild;

        ProblemBuildRunnable(Project project, RandoriProject compiler, boolean isBuild)
        {
            this.project = project;
            this.compiler = compiler;
            this.isBuild = isBuild;
        }

        @Override
        public void run()
        {
            ProblemsService service = ProblemsService.getInstance(project);

            service.addAll(compiler.getProblemQuery().getProblems());

            if (service.hasErrors())
            {
                NotificationUtils.sendRandoriError("Error", "Error(s) in project, Check the <a href='"
                        + ProblemsToolWindowFactory.WINDOW_ID + "'>" + ProblemsToolWindowFactory.WINDOW_ID
                        + "</a> for more information", project);
            }
            else
            {
                final String successBuildMessage;
                if (isBuild)
                    successBuildMessage = "Successfully compiled and built project";
                else
                    successBuildMessage = "Successfully parsed project";

                NotificationUtils.sendRandoriInformation("Success", successBuildMessage, project);
            }

        }

    }
}
