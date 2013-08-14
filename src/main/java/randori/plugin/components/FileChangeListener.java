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

package randori.plugin.components;

import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.*;
import com.intellij.util.ui.UIUtil;
import randori.plugin.util.ProjectUtils;

import java.util.List;

/**
 * @author Michael Schmalle
 * @author Frédéric THOMAS
 */
class FileChangeListener implements VirtualFileListener {
    private final Project project;

    public FileChangeListener(Project project) {
        this.project = project;
    }

    @SuppressWarnings("ConstantConditions")
    void validateAndParse(final VirtualFileEvent event, final boolean add, final boolean remove) {
        final VirtualFile file = event.getFile();
        final boolean isActionScriptFile = FileUtilRt.extensionEquals(file.getPath(),
                ActionScriptFileType.INSTANCE.getDefaultExtension());
        final Module moduleForFile = ModuleUtilCore.findModuleForFile(file, project);
        final boolean isBelongModule = moduleForFile != null;

        if (project == ProjectUtils.getProject() && isActionScriptFile && isBelongModule) {
            List<VirtualFile> modifiedFiles;
            final RandoriModuleComponent moduleComponent = moduleForFile.getComponent(RandoriModuleComponent.class);
            modifiedFiles = moduleComponent.getModifiedFiles();

            if (modifiedFiles == null)
                return;

            if (add && !modifiedFiles.contains(file))
                modifiedFiles.add(file);

            if (remove && modifiedFiles.contains(file))
                modifiedFiles.remove(file);

        } else if (isBelongModule && moduleForFile.getModuleFile() != null && moduleForFile.getModuleFile().equals(file)) {
            updateDependenciesOnUIThread(moduleForFile);
        }
    }

    private void updateDependenciesOnUIThread(final Module module) {

        if ((project.isInitialized()) && (!project.isDisposed()) && (project.isOpen()) && (!project.isDefault())) {
            UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                public void run() {
                    final RandoriModuleComponent moduleComponent = module.getComponent(RandoriModuleComponent.class);
                    moduleComponent.updateDependencies();
                }
            });
        }
    }

    @Override
    public void propertyChanged(VirtualFilePropertyEvent event) {
        if (event.getPropertyName().equals(VirtualFile.PROP_NAME)
                && VirtualFile.isValidName(event.getNewValue().toString()))
            validateAndParse(event, true, false);
    }

    @Override
    public void contentsChanged(VirtualFileEvent event) {
        validateAndParse(event, true, false);
    }

    @Override
    public void fileCreated(VirtualFileEvent event) {
        validateAndParse(event, true, false);
    }

    @Override
    public void fileDeleted(VirtualFileEvent event) {
        validateAndParse(event, false, true);
    }

    @Override
    public void fileMoved(VirtualFileMoveEvent event) {
        validateAndParse(event, false, false);
    }

    @Override
    public void fileCopied(VirtualFileCopyEvent event) {
        validateAndParse(event, true, false);
    }

    @Override
    public void beforePropertyChange(VirtualFilePropertyEvent event) {
    }

    @Override
    public void beforeContentsChange(VirtualFileEvent event) {
    }

    @Override
    public void beforeFileDeletion(VirtualFileEvent event) {
    }

    @Override
    public void beforeFileMovement(VirtualFileMoveEvent event) {
    }
}
