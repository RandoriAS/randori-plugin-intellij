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

import com.intellij.openapi.module.Module;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import randori.plugin.module.RandoriLibraryModuleType;
import randori.plugin.module.RandoriWebModuleType;

import javax.swing.*;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 20:50
 */
public class RandoriLibraryType extends LibraryType<RandoriLibraryProperties> {
    private static final String LIBRARY_KIND = "randori.rbl";
    public static final String LIBRARY_EXTENSION = "rbl";
    public static final String LIBRARY_DOT_EXTENSION = ".rbl";

    public static RandoriLibraryType getInstance() {
        return LibraryType.EP_NAME.findExtension(RandoriLibraryType.class);
    }

    public static final PersistentLibraryKind<RandoriLibraryProperties> RANDORI_LIBRARY =
            new PersistentLibraryKind<RandoriLibraryProperties>(LIBRARY_KIND) {

                @NotNull
                public RandoriLibraryProperties createDefaultProperties() {
                    return new RandoriLibraryProperties();
                }
            };

    protected RandoriLibraryType() {
        super(RANDORI_LIBRARY);
    }

    /**
     * @return text to show in 'New Library' popup. Return {@code null} if the type should not be shown in the 'New
     *         Library' popup
     */
    @Nullable
    @Override
    public String getCreateActionName() {
        return "Randori Library";
    }

    /**
     * Called when a new library of this type is created in Project Structure dialog
     */
    @Nullable
    @Override
    public NewLibraryConfiguration createNewLibrary(@NotNull JComponent parentComponent,
                                                    @Nullable VirtualFile contextDirectory, @NotNull Project project) {
        return LibraryTypeService.getInstance().createLibraryFromFiles(createLibraryRootsComponentDescriptor(),
                parentComponent, contextDirectory, this, project);
    }

    @Nullable
    @Override
    public LibraryPropertiesEditor createPropertiesEditor(@NotNull LibraryEditorComponent editorComponent) {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return PlatformIcons.LIBRARY_ICON;
    }

    public boolean isSuitableModule(@NotNull Module module, @NotNull FacetsProvider facetsProvider) {
        return RandoriWebModuleType.isOfType(module) || RandoriLibraryModuleType.isOfType(module);
    }

    @NotNull
    public LibraryRootsComponentDescriptor createLibraryRootsComponentDescriptor() {
        return new RandoriLibraryRootsComponentDescriptor();
    }
}
