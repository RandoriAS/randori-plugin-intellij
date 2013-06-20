package randori.plugin.library;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootFilter;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author AsusFred Date: 20/06/13 Time: 01:55
 */
public class RandoriLibraryBinariesRootDetector extends RootFilter
{
    public RandoriLibraryBinariesRootDetector()
    {
        super(OrderRootType.CLASSES, false, "RBL/SWC library");
    }

    public boolean isAccepted(@NotNull VirtualFile rootCandidate, @NotNull ProgressIndicator progressIndicator)
    {
        return ("swc".equalsIgnoreCase(rootCandidate.getExtension())
                && rootCandidate.getFileSystem() == JarFileSystem.getInstance() && rootCandidate.getParent() == null)
                || RandoriLibraryType.LIBRARY_EXTENSION.equalsIgnoreCase(rootCandidate.getExtension())
                && rootCandidate.getFileSystem() == LocalFileSystem.getInstance();
    }
}
