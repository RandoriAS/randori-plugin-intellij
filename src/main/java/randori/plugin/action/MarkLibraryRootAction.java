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
package randori.plugin.action;

import com.intellij.ide.projectView.actions.CreateLibraryFromFilesDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.ui.OrderRoot;
import com.intellij.openapi.roots.libraries.ui.impl.RootDetectionUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import randori.plugin.components.RandoriApplicationComponent;
import randori.plugin.library.RandoriLibraryRootsComponentDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Frédéric THOMAS Date: 12/08/13 Time: 21:53
 */
public class MarkLibraryRootAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = getEventProject(e);
        if (project == null) return;

        final List<VirtualFile> rbls = getRoots(e);
        if (rbls.isEmpty()) return;

        final List<OrderRoot> roots = RootDetectionUtil.detectRoots(rbls, null, project, new RandoriLibraryRootsComponentDescriptor());
        new CreateLibraryFromFilesDialog(project, roots).show();
    }

    @NotNull
    private static List<VirtualFile> getRoots(AnActionEvent e) {
        final Project project = getEventProject(e);
        final VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (project == null || files == null || files.length == 0) return Collections.emptyList();

        List<VirtualFile> roots = new ArrayList<VirtualFile>();
        for (VirtualFile file : files) {
            if (file.isDirectory()) {
                roots.add(file);
            } else {
                final VirtualFile root = file.getFileType().equals(RandoriApplicationComponent.RBL_FILE_TYPE) ? file : null;
                if (root != null) {
                    roots.add(root);
                }
            }
        }
        return roots;
    }

    @Override
    public void update(AnActionEvent e) {
        final Project project = getEventProject(e);
        boolean visible = false;
        if (project != null && ModuleManager.getInstance(project).getModules().length > 0) {
            final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
            for (VirtualFile root : getRoots(e)) {
                if (root.isInLocalFileSystem() && !fileIndex.isInLibraryClasses(root)) {
                    visible = true;
                    break;
                }
                if (root.isInLocalFileSystem() && root.isDirectory()) {
                    for (VirtualFile child : root.getChildren()) {
                        final VirtualFile rblRoot =
                                child.getFileType().equals(RandoriApplicationComponent.RBL_FILE_TYPE) ? child : null;
                        if (rblRoot != null && !fileIndex.isInLibraryClasses(child)) {
                            visible = true;
                            break;
                        }
                    }
                }
            }
        }

        e.getPresentation().setVisible(visible);
        e.getPresentation().setEnabled(visible);
    }
}

