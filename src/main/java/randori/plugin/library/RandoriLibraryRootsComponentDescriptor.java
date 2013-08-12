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

package randori.plugin.library;

import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.ui.Util;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.*;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.DefaultLibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import icons.FlexIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 19:50
 */
public class RandoriLibraryRootsComponentDescriptor extends LibraryRootsComponentDescriptor {
    final static String CHOOSE_LIBRARY_FILE_DESCRIPTION = "<html>Select *.swc or *.rbl files and/or folders containing *.swc, *.rbl files.<br>"
            + ApplicationNamesInfo.getInstance().getFullProductName()
            + " will analyze the contents of the selected folders and automatically assign the files contained therein to the appropriate categories (Classes, Sources and Documentation).";

    public OrderRootTypePresentation getRootTypePresentation(@NotNull OrderRootType type) {
        if ((type instanceof JavadocOrderRootType)) {
            return new OrderRootTypePresentation("Documentation", FlexIcons.Flex.Documentation);
        }

        return DefaultLibraryRootsComponentDescriptor.getDefaultPresentation(type);
    }

    /**
     * Provides root detector for 'Attach Files' button. It will be used to automatically assign
     * {@link com.intellij.openapi.roots.OrderRootType}s for selected files. Also this detector is used when a new
     * library is created
     *
     * @return {@link com.intellij.openapi.roots.libraries.ui.LibraryRootsDetector}'s implementation
     */
    @NotNull
    @Override
    public LibraryRootsDetector getRootsDetector() {
        return super.getRootsDetector();
    }

    @NotNull
    public List<? extends RootDetector> getRootDetectors() {
        return Arrays.asList(new RandoriLibraryBinariesRootDetector(), new RandoriLibraryDocsRootDetector(),
                new RandoriLibrarySourcesRootDetector(), new RandoriLibraryFoldersRootDetector());
    }

    @NotNull
    public FileChooserDescriptor createAttachFilesChooserDescriptor(String libraryName) {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, true, false, false, true) {
            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return file.isDirectory() || "rbl".equalsIgnoreCase(file.getExtension()) || "swc".equalsIgnoreCase(file.getExtension());
            }

            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                return super.isFileVisible(file, showHiddenFiles) || file.isDirectory() || isFileSelectable(file);
            }
        };

        descriptor.setTitle(StringUtil.isEmpty(libraryName) ? ProjectBundle.message("library.attach.files.action")
                : ProjectBundle.message("library.attach.files.to.library.action", libraryName));
        descriptor.setDescription(CHOOSE_LIBRARY_FILE_DESCRIPTION);

        return descriptor;
    }

    public String getAttachFilesActionName() {
        return "Add &Library Components...";
    }

    @NotNull
    public List<? extends AttachRootButtonDescriptor> createAttachButtons() {
        return Arrays.asList(new AddDocUrlDescriptor());
    }

    private static class AddDocUrlDescriptor extends AttachRootButtonDescriptor {
        private AddDocUrlDescriptor() {
            super(JavadocOrderRootType.getInstance(), "Add Documentation &URL...");
        }

        public VirtualFile[] selectFiles(@NotNull JComponent parent, @Nullable VirtualFile initialSelection,
                                         @Nullable Module contextModule, @Nullable LibraryEditor libraryEditor) {
            VirtualFile vFile = Util.showSpecifyJavadocUrlDialog(parent);
            if (vFile != null) {
                return new VirtualFile[]{vFile};
            }
            return VirtualFile.EMPTY_ARRAY;
        }
    }
}
