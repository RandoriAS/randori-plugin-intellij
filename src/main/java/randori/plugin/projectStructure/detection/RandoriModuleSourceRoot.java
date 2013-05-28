package randori.plugin.projectStructure.detection;

import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.DetectedSourceRoot;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Frédéric THOMAS Date: 19/04/13 Time: 19:40
 */
class RandoriModuleSourceRoot extends DetectedSourceRoot
{
    public RandoriModuleSourceRoot(final File directory)
    {
        super(directory, null);
    }

    @NotNull
    @Override
    public String getRootTypeName()
    {
        return "Randori module";
    }

    @Override
    public DetectedProjectRoot combineWith(@NotNull DetectedProjectRoot root)
    {
        if (root instanceof RandoriModuleSourceRoot)
            return this;

        return null;
    }

    @Override
    public boolean canContainRoot(@NotNull DetectedProjectRoot root) {
        return !(root instanceof RandoriModuleSourceRoot);
    }
}
