package randori.plugin.library;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.projectRoots.ui.Util;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.*;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.DefaultLibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.UIBundle;
import icons.FlexIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 19:50
 */
class RandoriLibraryRootsComponentDescriptor extends LibraryRootsComponentDescriptor {
    public OrderRootTypePresentation getRootTypePresentation(@NotNull OrderRootType type) {
        if ((type instanceof JavadocOrderRootType)) {
            return new OrderRootTypePresentation("Documentation", FlexIcons.Flex.Documentation);
        }

        return DefaultLibraryRootsComponentDescriptor.getDefaultPresentation(type);
    }

    @NotNull
    public List<? extends RootDetector> getRootDetectors() {
        return Arrays.asList(new RandoriLibraryBinariesRootDetector());
    }

    @NotNull
    public FileChooserDescriptor createAttachFilesChooserDescriptor(String libraryName) {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, true, false, false, true) {
            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return "rbl".equalsIgnoreCase(file.getExtension()) || "swc".equalsIgnoreCase(file.getExtension());
            }

            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                return super.isFileVisible(file, showHiddenFiles) && (file.isDirectory() || isFileSelectable(file));
            }
        };
        fileChooserDescriptor.setTitle(UIBundle.message("file.chooser.default.title"));
        fileChooserDescriptor.setDescription("Select *.rbl or *.swc files");

        return fileChooserDescriptor;
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

    private static class RandoriLibraryBinariesRootDetector extends RootFilter {
        public RandoriLibraryBinariesRootDetector() {
            super(OrderRootType.CLASSES, false, "RBL/SWC library");
        }

        public boolean isAccepted(@NotNull VirtualFile rootCandidate, @NotNull ProgressIndicator progressIndicator) {
            return ("swc".equalsIgnoreCase(rootCandidate.getExtension()) && rootCandidate.getFileSystem() == JarFileSystem.getInstance() && rootCandidate.getParent() == null)
                    || RandoriLibraryType.LIBRARY_EXTENSION.equalsIgnoreCase(rootCandidate.getExtension()) && rootCandidate.getFileSystem() == LocalFileSystem.getInstance();
        }
    }
}
