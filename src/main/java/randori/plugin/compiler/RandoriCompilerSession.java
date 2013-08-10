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
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.utils.StringUtils;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Frédéric THOMAS Date: 13/04/13 Time: 13:27
 */
public class RandoriCompilerSession {
    private static RandoriProject lastCompiler;

    private final Project project;

    private final Workspace workspace;
    private Module module;
    private List<VirtualFile> modifiedFiles;
    private final RandoriCompilerModel projectModel;
    private RandoriModuleModel moduleModel;
    private RandoriProject compiler;
    private List<Module> usedModules;
    private boolean isRebuild;
    private boolean isMake;
    private boolean isWebModule;
    private List<String> webModuleRblPaths;
    private String moduleBasePath;

    public static void parseAll(@NotNull Project project) {
        if (ProjectUtils.isSDKInstalled(project) && ProjectUtils.hasRandoriModuleType(project)) {
            Module[] sortedModules = ModuleManager.getInstance(project).getSortedModules();

            RandoriCompilerSession compilerSession = null;

            for (Module module : sortedModules) {
                if (compilerSession == null)
                    compilerSession = new RandoriCompilerSession(project);

                if (!compilerSession.parse(module)) {
                    ApplicationManager.getApplication().invokeLater(
                            new ProblemBuildRunnable(project, module, lastCompiler, false));
                    return;
                }
            }
            ApplicationManager.getApplication().invokeLater(new ProblemBuildRunnable(project, null, lastCompiler, false));
        }
    }

    public static RandoriProject getLastCompiler() {
        return lastCompiler;
    }

    public RandoriCompilerSession(@NotNull Project project) {
        this.project = project;
        projectModel = RandoriCompilerModel.getInstance(project).getState();
        workspace = new Workspace();
    }

    //--------------------------------------------------------------------------

    public Workspace getWorkspace() {
        return workspace;
    }

    //--------------------------------------------------------------------------

    boolean parse(@NotNull Module module) {
        return build(module, false, false);
    }

    boolean make(@NotNull Module module) {
        isMake = true;
        return build(module, true, false);
    }

    public boolean build(@NotNull Module module) {
        isRebuild = true;
        return build(module, true, true);
    }

    private boolean build(Module module, boolean doBuild, boolean doExport) {
        boolean success = false;

        isWebModule = ProjectUtils.isRandoriWebModule(module);

        if (prepareCompilation(module)) {
            success = true;

            if (isWebModule || isMake || (isRebuild && moduleModel.isGenerateRbl())) {
                CompilerArguments arguments = new CompilerArguments();
                configureDependencies(arguments);
                final IBundleConfiguration configuration;

                clearProblems();

                if (compiler instanceof RandoriProjectCompiler) {
                    compiler.configure(arguments.toArguments());
                    dumpCompilationInfo("Compiling Web Module: " + module.getName() + "\n\n" +
                            StringUtils.join(arguments.toArguments(), "\n"));
                } else if (compiler instanceof RandoriBundleCompiler) {
                    configuration = createConfiguration(arguments);
                    ((RandoriBundleCompiler) compiler).configure(configuration);
                    dumpCompilationInfo("Compiling Rbl: " + configuration.getBundelName() + "\n\n" +
                            StringUtils.join(((BundleConfiguration) configuration).toArguments(), "\n"));
                }

                // Checked twice because the compiler.compile return true even if it throws errors
                success = compiler.compile(doBuild, doExport) && !lastCompiler.getProblemQuery().hasErrors();

                // If we're compiling a web module, copy the SDK libs to the appropriate location.
                // If we compile a lib module, only copy the compile Rbl to its others web module parents.
                if (success) {
                    if (doExport && isWebModule) {
                        RandoriSdkType.copySdkLibraries(project, moduleBasePath);
                    } else if (webModuleRblPaths != null && webModuleRblPaths.size() > 1) {
                        File rblFile = new File(webModuleRblPaths.get(0));
                        for (int i = 1; i < webModuleRblPaths.size(); i++) {
                            File rblDestination = new File(webModuleRblPaths.get(i));
                            try {
                                FileUtilRt.copy(rblFile, rblDestination);
                                dumpCompilationInfo("Copying " + rblFile.getName() + " to " + rblDestination.getPath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if ((project.isInitialized()) && (!project.isDisposed()) && (project.isOpen()) && (!project.isDefault())) {
                        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                            public void run() {
                                project.getBaseDir().refresh(true, true);
                            }
                        });
                    }
                }
            }
        }

        return success;
    }

    private boolean prepareCompilation(Module module) {
        boolean isCompilationPrepared = false;
        this.module = module;

        //workspace = new Workspace();
        lastCompiler = compiler = isWebModule ? new RandoriProjectCompiler(workspace) : new RandoriBundleCompiler(
                workspace);

        compiler.getPluginFactory().registerPlugin(IPreProcessPlugin.class, DummyPreProcessPlugin.class);

        final RandoriProjectComponent projectComponent = project.getComponent(RandoriProjectComponent.class);
        final RandoriModuleComponent moduleComponent = module.getComponent(RandoriModuleComponent.class);

        usedModules = new ArrayList<Module>();
        moduleModel = null;
        modifiedFiles = null;

        if (moduleComponent != null) {
            modifiedFiles = projectComponent.getModifiedFiles();
            moduleModel = moduleComponent.getState();
            usedModules = moduleComponent.getRecursiveDependencies();

            webModuleRblPaths = new ArrayList<String>();

            final List<VirtualFile> webModuleParentsContentRootFolder = moduleComponent.getWebModuleParentsContentRootFolder();
            if (isWebModule) {
                if (!webModuleParentsContentRootFolder.isEmpty())
                    moduleBasePath = webModuleParentsContentRootFolder.get(0).getCanonicalPath();
            } else
                for (VirtualFile webModuleParentContentRootFolder : webModuleParentsContentRootFolder) {
                    String librariesOutputPath = FileUtil.toSystemDependentName(webModuleParentContentRootFolder.getCanonicalPath()
                            + File.separator + projectModel.getLibraryPath());
                    String libraryOutputPath = librariesOutputPath + File.separator + module.getName();
                    String generatedLibPath = libraryOutputPath + File.separator + module.getName()
                            + RandoriLibraryType.LIBRARY_DOT_EXTENSION;

                    webModuleRblPaths.add(generatedLibPath);
                }
            isCompilationPrepared = true;
        }
        return isCompilationPrepared;
    }

    private void configureDependencies(CompilerArguments arguments) {
        arguments.clear();

        configure(arguments);

        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();

        if (projectSdk != null) {
            arguments.setSDKPath(projectSdk.getHomePath());

            if (usedModules != null)
                for (Module moduleLib : usedModules) {
                    for (String modulePath : ProjectUtils.getAllModuleSourcePaths(moduleLib)) {
                        arguments.addSourcepath(modulePath);
                    }
                }

            for (VirtualFile sourceRoot : ModuleRootManager.getInstance(module).getSourceRoots()) {
                arguments.addSourcepath(sourceRoot.getPath());
            }

            if (!isRebuild && modifiedFiles != null) {
                for (VirtualFile virtualFile : modifiedFiles) {
                    arguments.addIncludedSources(virtualFile.getPath());
                }
            }

            // Add SWCs and RBLs paths.
            final RandoriModuleComponent moduleComponent = module.getComponent(RandoriModuleComponent.class);
            List<VirtualFile> effectiveLibraryRoots = moduleComponent.getLibraryRootsGottenNoModuleSources();

            for (VirtualFile library : effectiveLibraryRoots) {
                final String libraryPath = library.getPath();

                if (FileUtilRt.getExtension(libraryPath).equalsIgnoreCase(
                        RandoriApplicationComponent.RBL_FILE_TYPE.getDefaultExtension())) {
                    arguments.addBundlePath(libraryPath);
                } else if (FileUtilRt.getExtension(libraryPath).equalsIgnoreCase("swc"))
                    arguments.addLibraryPath(libraryPath.replace("!", ""));
            }
        }
    }

    private void configure(CompilerArguments arguments) {
        arguments.setAppName(project.getName());
        arguments.setJsBasePath(projectModel.getBasePath());
        arguments.setJsLibraryPath(projectModel.getLibraryPath());
        arguments.setJsOutputAsFiles(moduleModel.isExportAsFile());

        if (compiler instanceof RandoriProjectCompiler) {
            arguments.setOutput(moduleBasePath);
        } else if (compiler instanceof RandoriBundleCompiler) {
            arguments.setOutput(webModuleRblPaths.get(0));
        }
    }

    private IBundleConfiguration createConfiguration(CompilerArguments arguments) {
        BundleConfiguration configuration = new BundleConfiguration(module.getName(), arguments.getOutput());

        for (String libraryPath : arguments.getLibraries()) {
            configuration.addLibraryPath(libraryPath);
        }

        for (String bundlePath : arguments.getBundles()) {
            configuration.addBundlePath(bundlePath);
        }

        IBundleConfigurationEntry entry = configuration.addEntry(module.getName());

        for (String sourcePath : arguments.getSources()) {
            entry.addSourcePath(sourcePath);
        }

        for (String sourceIncludedPath : arguments.getIncludes()) {
            entry.addIncludeSources(sourceIncludedPath);
        }

        configuration.setSDKPath(arguments.getSDKPath());

        return configuration;
    }

    public static void dumpCompilationInfo(String configuration) {
        System.out.println();
        System.out.println("----------------- Dumping compilation info ----------------");
        System.out.println(configuration);
        System.out.println("-------------- Ends Dumping compilation info --------------");
        System.out.println();
    }

    private void clearProblems() {
        ApplicationManager.getApplication().invokeLater(new ProblemClearRunnable());
    }

    @SuppressWarnings("unused")
    private String toErrorCode(int code) {
        switch (code) {
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

    private class ProblemClearRunnable implements Runnable {
        @Override
        public void run() {
            ProblemsService service = ProblemsService.getInstance(project);
            service.clearProblems();
        }
    }

    public static class ProblemBuildRunnable implements Runnable {
        private final Project project;
        private final Module module;
        private final RandoriProject compiler;
        private final boolean isBuild;

        ProblemBuildRunnable(Project project, Module module, RandoriProject compiler, boolean isBuild) {
            this.project = project;
            this.module = module;
            this.compiler = compiler;
            this.isBuild = isBuild;
        }

        @Override
        public void run() {
            ProblemsService service = ProblemsService.getInstance(project);

            service.addAll(compiler.getProblemQuery().getProblems());

            if (service.hasErrors()) {
                NotificationUtils.sendRandoriError("Error", "Error(s) in project, Check the <a href='"
                        + ProblemsToolWindowFactory.WINDOW_ID + "'>" + ProblemsToolWindowFactory.WINDOW_ID
                        + "</a> for more information", project);
            } else {
                final String successBuildMessage;
                final String projectName = module != null ? project.getName() + "/" + module.getName() : project.getName();

                if (isBuild) {
                    successBuildMessage = "Successfully compiled and built " + projectName;
                } else
                    successBuildMessage = "Successfully parsed " + projectName;

                NotificationUtils.sendRandoriInformation("Success", successBuildMessage, project);
            }

        }

    }
}
