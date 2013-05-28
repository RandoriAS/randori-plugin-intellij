package randori.plugin.library;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.*;
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent;
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor;
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.ui.configuration.FacetsProvider;
import com.intellij.openapi.vfs.VirtualFile;
import icons.RandoriIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import randori.plugin.module.RandoriModuleType;

import javax.swing.*;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 20:50
 */
public class RandoriLibraryType extends LibraryType<DummyLibraryProperties> {
    private static final String LIBRARY_KIND = "randori.rbl";
    public static final String LIBRARY_EXTENSION = "rbl";
    public static final String LIBRARY_DOT_EXTENSION = ".rbl";

    public static RandoriLibraryType getInstance() {
        return LibraryType.EP_NAME.findExtension(RandoriLibraryType.class);
    }

    private static final PersistentLibraryKind<DummyLibraryProperties> RANDORI_LIBRARY =
            new PersistentLibraryKind<DummyLibraryProperties>(LIBRARY_KIND) {
                @NotNull
                @Override
                public DummyLibraryProperties createDefaultProperties() {
                    return DummyLibraryProperties.INSTANCE;
                }
            };

    public RandoriLibraryType() {
        super(RANDORI_LIBRARY);
    }

    public String getCreateActionName() {
        return "Randori Library";
    }

    @Nullable
    @Override
    public NewLibraryConfiguration createNewLibrary(@NotNull JComponent parentComponent, VirtualFile contextDirectory,
                                                    @NotNull Project project) {
        return LibraryTypeService.getInstance().createLibraryFromFiles(createLibraryRootsComponentDescriptor(),
                parentComponent, contextDirectory, this, project);
    }

    public LibraryPropertiesEditor createPropertiesEditor(@NotNull LibraryEditorComponent properties) {
        return null;
    }

    public boolean isSuitableModule(@NotNull Module module, @NotNull FacetsProvider facetsProvider) {
        return ModuleType.get(module).equals(RandoriModuleType.getInstance());
    }

    @NotNull
    public LibraryRootsComponentDescriptor createLibraryRootsComponentDescriptor() {
        return new RandoriLibraryRootsComponentDescriptor();
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return RandoriIcons.Randori16;
    }
}
