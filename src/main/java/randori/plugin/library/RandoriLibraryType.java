package randori.plugin.library;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.roots.libraries.LibraryTypeService;
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent;
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor;
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.ui.configuration.FacetsProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformIcons;
import icons.RandoriIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import randori.plugin.module.RandoriModuleType;

import javax.swing.*;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 20:50
 */
public class RandoriLibraryType extends LibraryType
{
    public static final String LIBRARY_EXTENSION = "rbl";
    public static final String LIBRARY_DOT_EXTENSION = ".rbl";

    public static RandoriLibraryType getInstance()
    {
        return LibraryType.EP_NAME.findExtension(RandoriLibraryType.class);
    }

    public static final PersistentLibraryKind RANDORI_LIBRARY = new PersistentLibraryKind("randori") {

        @NotNull
        public RandoriLibraryProperties createDefaultProperties()
        {
            return new RandoriLibraryProperties();
        }
    };

    protected RandoriLibraryType()
    {
        super(RANDORI_LIBRARY);
    }

    /**
     * @return text to show in 'New Library' popup. Return {@code null} if the type should not be shown in the 'New
     * Library' popup
     */
    @Nullable
    @Override
    public String getCreateActionName()
    {
        return "Randori Library";
    }

    /**
     * Called when a new library of this type is created in Project Structure dialog
     */
    @Nullable
    @Override
    public NewLibraryConfiguration createNewLibrary(@NotNull JComponent parentComponent,
            @Nullable VirtualFile contextDirectory, @NotNull Project project)
    {
        return LibraryTypeService.getInstance().createLibraryFromFiles(createLibraryRootsComponentDescriptor(),
                parentComponent, contextDirectory, this, project);
    }

    @Nullable
    @Override
    public LibraryPropertiesEditor createPropertiesEditor(@NotNull LibraryEditorComponent editorComponent)
    {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon()
    {
        return PlatformIcons.LIBRARY_ICON;
    }

    public boolean isSuitableModule(@NotNull Module module, @NotNull FacetsProvider facetsProvider)
    {
        return ModuleType.get(module).equals(RandoriModuleType.getInstance());
    }

    @NotNull
    public LibraryRootsComponentDescriptor createLibraryRootsComponentDescriptor()
    {
        return new RandoriLibraryRootsComponentDescriptor();
    }
}
