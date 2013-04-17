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

package randori.plugin.builder;

import java.util.*;

import org.jetbrains.annotations.NotNull;

import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.util.ProjectUtils;
import randori.plugin.util.VFileUtils;

import com.intellij.analysis.AnalysisScopeBundle;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeAction;
import com.intellij.compiler.impl.ModuleCompileScope;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeListImpl;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.util.ui.UIUtil;

/**
 * @author Michael Schmalle
 * @author Frédéric THOMAS
 */
public class FileChangeListener implements VirtualFileListener
{
    private static final int CORES_COUNT = Runtime.getRuntime().availableProcessors();
    private boolean makeProjectOnSave = CORES_COUNT > 2;

    private final Project project;
    private final RandoriProjectComponent projectComponent;

    public FileChangeListener(Project project)
    {
        this.project = project;
        projectComponent = project.getComponent(RandoriProjectComponent.class);
    }

    public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext)
    {
        // does nothing but apparently needed for write access.
    }

    protected boolean isValidFile(VirtualFile file)
    {
        if (project == ProjectUtils.getProject() && VFileUtils.extensionEquals(file.getPath(), "as"))
        {
            Module rootModule = (Module) ModuleManager.getInstance(project).getSortedModules()[0];

            VirtualFile[] sourceRoots = ModuleRootManager.getInstance(rootModule).getSourceRoots();

            for (VirtualFile sourceRoot : sourceRoots)
            {
                if (file.getPath().startsWith(sourceRoot.getPath()))
                    return true;
            }
        }

        return false;
    }

    protected void validateAndParse(final VirtualFileEvent event, final boolean add, final boolean remove)
    {
        final VirtualFile file = event.getFile();

        if (isValidFile(file))
        {
            List<VirtualFile> modifiedFiles = projectComponent.getModifiedFiles();

            if (add && !modifiedFiles.contains(file))
                modifiedFiles.add(file);

            if (remove && modifiedFiles.contains(file))
                modifiedFiles.remove(file);

            final ReadonlyStatusHandler.OperationStatus operationStatus = ReadonlyStatusHandler.getInstance(project)
                    .ensureFilesWritable(modifiedFiles);

            if (!operationStatus.hasReadonlyFiles())
            {
                new OptimizeImportsProcessor(project, ReformatCodeAction.convertToPsiFiles(
                        modifiedFiles.toArray(new VirtualFile[modifiedFiles.size()]), project), new Runnable() {
                    @Override
                    public void run()
                    {
                        importAnnotations(file);

                        if ((event.isFromSave()) && makeProjectOnSave)
                            executeMake(event);
                    }
                }).run();
            }
        }
    }

    private void importAnnotations(VirtualFile file)
    {
        final List<PsiElement> annotations = new ArrayList<PsiElement>();

        FileViewProvider viewProvider = PsiManager.getInstance(project).findFile(file).getViewProvider();
        Set<Language> languages = viewProvider.getLanguages();
        JSFile asFile = (JSFile) viewProvider.getPsi(languages.iterator().next());

        for (PsiElement psiElement : asFile.getChildren())
        {
            findAnnotations(annotations, psiElement);
        }

        Map<JSAttribute, String> importStatements = getImportStatementsFromAnnotations(annotations);

        if (importStatements.size() > 0)
        {
            AccessToken accessToken = ApplicationManager.getApplication().acquireWriteActionLock(
                    FileChangeListener.class);
            try
            {
                for (Map.Entry<JSAttribute, String> entry : importStatements.entrySet())
                {
                    ImportUtils.doImport(entry.getKey(), entry.getValue(), true);
                }
            }
            finally
            {
                accessToken.finish();
            }
        }
    }

    private static void findAnnotations(List<PsiElement> annotations, @NotNull PsiElement aChild)
    {
        if (aChild instanceof JSAttributeListImpl)
        {
            for (PsiElement psiElement : aChild.getChildren())
            {
                if (psiElement instanceof JSAttribute && !annotations.contains(psiElement))
                {
                    annotations.add(psiElement);
                }
            }
        }

        for (PsiElement child : aChild.getChildren())
        {
            findAnnotations(annotations, child);
        }
    }

    private Map<JSAttribute, String> getImportStatementsFromAnnotations(List<PsiElement> annotations)
    {

        Map<JSAttribute, String> importStatements = new HashMap<JSAttribute, String>();

        //AnnotationManager annotationManager = new AnnotationManager(projectComponent.getCompiler());
        //TODO implement and maybe keep a map at class level and don't try to get what is already in the map.

        return importStatements;
    }

    private void executeMake(final VirtualFileEvent event)
    {
        ProgressManager.getInstance()
                .run(new Task.Backgroundable(project, AnalysisScopeBundle.message("analyzing.project", new Object[0]),
                        true) {
                    public void run(@NotNull ProgressIndicator indicator)
                    {
                        if (indicator == null)
                            throw new IllegalArgumentException(
                                    "Argument 0 for @NotNull parameter of randori/plugin/builder/FileChangeListener$1.run must not be null");
                        FileChangeListener.this.executeMakeInUIThread(event);
                    }
                });
    }

    private void executeMakeInUIThread(VirtualFileEvent event)
    {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : projects)
            if ((project.isInitialized()) && (!project.isDisposed()) && (project.isOpen()) && (!project.isDefault()))
            {
                ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
                final Module module = projectFileIndex.getModuleForFile(event.getFile());
                if (module != null)
                {
                    final CompilerManager compilerManager = CompilerManager.getInstance(project);
                    if ((!compilerManager.isCompilationActive())
                            && (!compilerManager.isExcludedFromCompilation(event.getFile()))
                            && (!compilerManager.isUpToDate(new ModuleCompileScope(module, false))))
                    {
                        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                            public void run()
                            {
                                compilerManager.make(module, null);
                            }
                        });
                    }
                }
            }
    }

    @Override
    public void propertyChanged(VirtualFilePropertyEvent event)
    {
        if (event.getPropertyName() == VirtualFile.PROP_NAME
                && !projectComponent.getModifiedFiles().contains(event.getFile()))
        {
            validateAndParse(event, true, false);
        }
    }

    @Override
    public void contentsChanged(VirtualFileEvent event)
    {
        validateAndParse(event, true, false);
    }

    @Override
    public void fileCreated(VirtualFileEvent event)
    {
        validateAndParse(event, true, false);
    }

    @Override
    public void fileDeleted(VirtualFileEvent event)
    {
        validateAndParse(event, false, true);
    }

    @Override
    public void fileMoved(VirtualFileMoveEvent event)
    {
        validateAndParse(event, false, false);
    }

    @Override
    public void fileCopied(VirtualFileCopyEvent event)
    {
        validateAndParse(event, true, false);
    }

    @Override
    public void beforePropertyChange(VirtualFilePropertyEvent event)
    {
    }

    @Override
    public void beforeContentsChange(VirtualFileEvent event)
    {
    }

    @Override
    public void beforeFileDeletion(VirtualFileEvent event)
    {
    }

    @Override
    public void beforeFileMovement(VirtualFileMoveEvent event)
    {
    }
}
