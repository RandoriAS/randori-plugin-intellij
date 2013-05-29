package randori.plugin.compiled;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewBuilderProvider;
import com.intellij.lang.javascript.structureView.JSStructureViewBuilderFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 16:33
 */
public class RblStructureViewBuilderProvider implements StructureViewBuilderProvider
{

    private final JSStructureViewBuilderFactory factory = new JSStructureViewBuilderFactory();

    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder(@NotNull FileType fileType, @NotNull VirtualFile file,
            @NotNull Project project)
    {
        return null;
    }
}
