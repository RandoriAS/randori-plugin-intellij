package randori.plugin.library;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author AsusFred Date: 17/06/13 Time: 16:37
 */
public class RandoriDocsRootDetector extends RootDetector
{

    protected RandoriDocsRootDetector()
    {
        super(OrderRootType.DOCUMENTATION, false, "RBL documentation detector");
    }

    /**
     * Find suitable roots in {@code rootCandidate} or its descendants.
     * 
     * @param rootCandidate file selected in the file chooser by user
     * @param progressIndicator can be used to show information about the progress and to abort searching if process is
     * cancelled
     * @return suitable roots
     */
    @NotNull
    @Override
    public Collection<VirtualFile> detectRoots(@NotNull VirtualFile rootCandidate,
            @NotNull ProgressIndicator progressIndicator)
    {
        Collection<VirtualFile> result = new ArrayList<VirtualFile>();
        collectRoots(rootCandidate, result, progressIndicator);

        return result;
    }

    private void collectRoots(final VirtualFile startFile, final Collection<VirtualFile> result,
            final ProgressIndicator progressIndicator)
    {
        VfsUtilCore.visitChildrenRecursively(startFile, new VirtualFileVisitor() {
            public boolean visitFile(@NotNull VirtualFile file)
            {
                if (!file.isDirectory())
                    return false;
                progressIndicator.setText2(file.getPresentableUrl());
                if (file.findChild("all-classes.html") != null)
                {
                    result.add(file);
                    return false;
                }
                return true;
            }
        });
    }
}
