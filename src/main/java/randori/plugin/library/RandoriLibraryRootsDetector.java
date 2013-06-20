package randori.plugin.library;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.LibraryRootType;
import com.intellij.openapi.roots.libraries.ui.DetectedLibraryRoot;
import com.intellij.openapi.roots.libraries.ui.impl.LibraryRootsDetectorImpl;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class RandoriLibraryRootsDetector extends LibraryRootsDetectorImpl
{

    private static final Condition<DetectedLibraryRoot> DETECTED_LIBRARY_ROOT_CONDITION = new Condition<DetectedLibraryRoot>() {
        @Override
        public boolean value(DetectedLibraryRoot root)
        {
            LibraryRootType libraryRootType = root.getTypes().get(0);
            return (libraryRootType.getType() == OrderRootType.CLASSES) && (libraryRootType.isJarDirectory());
        }
    };

    public RandoriLibraryRootsDetector()
    {
        super(Arrays.asList(new RandoriLibraryBinariesRootDetector(), new RandoriDocsRootDetector(),
                new RandoriSourcesRootDetector()));
    }

    public Collection<DetectedLibraryRoot> detectRoots(@NotNull VirtualFile rootCandidate,
            @NotNull ProgressIndicator progressIndicator)
    {
        Collection<DetectedLibraryRoot> roots = super.detectRoots(rootCandidate, progressIndicator);
        boolean swcsFoldersFound = ContainerUtil.find(roots, DETECTED_LIBRARY_ROOT_CONDITION) != null;
        final List<LibraryRootType> types = Arrays.asList(new LibraryRootType(OrderRootType.CLASSES, false),
                new LibraryRootType(OrderRootType.SOURCES, false));

        if (swcsFoldersFound)
        {
            Collections.reverse(types);
        }

        return ContainerUtil.map(roots, new Function<DetectedLibraryRoot, DetectedLibraryRoot>() {
            @Override
            public DetectedLibraryRoot fun(DetectedLibraryRoot root)
            {
                if (root.getTypes().get(0).getType() == OrderRootType.SOURCES)
                {
                    return new DetectedLibraryRoot(root.getFile(), types);
                }
                return root;
            }
        });
    }

    public String getRootTypeName(@NotNull LibraryRootType rootType)
    {
        if (rootType.getType() == OrderRootType.SOURCES)
        {
            return "RBL sources";
        }

        if ((rootType.getType() instanceof JavadocOrderRootType))
        {
            return "RBL Documentation";
        }
        return null;
    }
}
