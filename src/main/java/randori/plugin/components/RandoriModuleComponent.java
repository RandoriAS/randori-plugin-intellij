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

package randori.plugin.components;

import com.intellij.compiler.impl.CompilerContentIterator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import randori.plugin.configuration.RandoriModuleConfigurable;
import randori.plugin.configuration.RandoriModuleModel;
import randori.plugin.module.RandoriWebModuleType;
import randori.plugin.util.ProjectUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Frédéric THOMAS
 */
@State(name = RandoriModuleComponent.COMPONENT_NAME, storages = {@Storage(id = "RandoriModule", file = "$MODULE_FILE$")})
/**
 * @author Michael Schmalle
 * @author Frédéric THOMAS
 */
public class RandoriModuleComponent implements ModuleComponent, Configurable,
        PersistentStateComponent<RandoriModuleModel> {
    public static final String COMPONENT_NAME = "RandoriModule";
    private final Module module;
    private final Project project;

    private RandoriModuleConfigurable form;
    private RandoriModuleModel state;
    private List<Module> dependencies;
    private List<Module> webModulesParents;

    private ModifiableRootModel modifiableRootModel;
    private ArrayList<Library> libraries;

    public RandoriModuleComponent(Module module, Project project) {
        this.module = module;
        this.project = project;
        state = new RandoriModuleModel();
        webModulesParents = new ArrayList<Module>();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Randori Module";
    }

    @Override
    public void initComponent() {
        modifiableRootModel = ModuleRootManager.getInstance(module).getModifiableModel();
        updateDependencies();
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void moduleAdded() {

    }

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {
    }

    @Override
    public void disposeComponent() {
        modifiableRootModel.dispose();
    }

    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public RandoriModuleModel getState() {
        return state;
    }

    @Override
    public void loadState(RandoriModuleModel state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

    @Override
    public JComponent createComponent() {
        if (form == null) {
            form = new RandoriModuleConfigurable();
            if (ProjectUtils.isRandoriWebModule(module)) {
                final JComponent formComponent = form.getComponent();
                for (Component component : formComponent.getComponents()) {
                    component.setEnabled(false);
                }
            }
        }
        return form.getComponent();
    }

    @Override
    public boolean isModified() {
        return form.isModified(getState());
    }

    @Override
    public void apply() throws ConfigurationException {
        if (form != null) {
            form.getData(getState());
        }
    }

    @Override
    public void reset() {
        if (form != null) {
            form.setData(getState());
        }
    }

    @Override
    public void disposeUIResources() {
    }

    @NotNull
    public List<Module> getDependencies() {
        if (ArrayUtils.isEmpty(dependencies.toArray()) || dependencies.get(0) == null)
            updateDependencies();

        return dependencies;
    }

    @NotNull
    public List<Library> getLibraries() {
        if (ArrayUtils.isEmpty(dependencies.toArray()) || dependencies.get(0) == null)
            updateDependencies();

        return libraries;
    }

    public List<Module> getWebModulesParents() {
        return webModulesParents;
    }

    public ModifiableRootModel getModifiableRootModel() {
        return modifiableRootModel;
    }

    /**
     * Compute the direct dependencies of this module except this module and get its parents of RandoriWebModuleType.
     * <p/>
     * Called when the module settings are changed or the
     * first time is called, careful to call it on UI Thread only.
     */
    public void updateDependencies() {
        dependencies = new ArrayList<Module>();
        libraries = new ArrayList<Library>();
        getUsedDependencies();

        if (ApplicationManager.getApplication().isReadAccessAllowed())
            webModulesParents = getRecursivelyWebModuleParents(module);
    }

    public List<Module> getRecursiveDependencies() {
        Module[] result = getDependencies().toArray(new Module[getDependencies().size()]);

        for (Module dependency : result) {
            RandoriModuleComponent moduleComponent = dependency.getComponent(RandoriModuleComponent.class);
            List<Module> dependencies = moduleComponent.getDependencies();
            if (!dependencies.isEmpty()) {
                Module[] moduleDependencies = dependencies.toArray(new Module[dependencies.size()]);
                result = (Module[]) org.apache.commons.lang.ArrayUtils.addAll(result, moduleDependencies);
                List<Module> recursivelyUsedModules = moduleComponent.getRecursiveDependencies();
                result = ArrayUtils.addAll(result, recursivelyUsedModules.toArray(new Module[recursivelyUsedModules.size()]));
            }
        }

        return Arrays.asList(result);
    }


    public List<VirtualFile> getLibraryRootsGottenNoModuleSources() {
        VirtualFile[] result = getLibraryRoots();
        final List<Module> recursiveDependencies = getRecursiveDependencies();

        if (!org.apache.commons.lang.ArrayUtils.isEmpty(result))
            for (VirtualFile libraryRoot : result) {
                if (!libraryRoot.getPath().endsWith(".swc")) {
                    String libraryName = libraryRoot.getNameWithoutExtension();
                    for (Module usedModule : recursiveDependencies) {
                        boolean existsAsModule = libraryName.equalsIgnoreCase(usedModule.getName());
                        boolean isStillInList = Arrays.asList(result).contains(libraryRoot);
                        if (existsAsModule && isStillInList) {
                            result = (VirtualFile[]) org.apache.commons.lang.ArrayUtils.removeElement(result, libraryRoot);
                            break;
                        }
                    }
                }
            }

        return Arrays.asList(result);
    }

    public VirtualFile[] getLibraryRoots() {
        Module[] modules;
        final List<Module> recursiveDependencies = getRecursiveDependencies();
        if (recursiveDependencies != null && !recursiveDependencies.isEmpty())
            modules = (Module[]) org.apache.commons.lang.ArrayUtils.addAll(new Module[]{module}, recursiveDependencies.toArray());
        else modules = new Module[]{module};

        return LibraryUtil.getLibraryRoots(modules, false, false);
    }

    protected FileIndex[] getFileIndices(boolean inMainModule, boolean inSubModules) {
        final List<Module> moduleList = new ArrayList<Module>();

        if (inMainModule)
            moduleList.add(module);

        if (inSubModules)
            moduleList.addAll(getDependencies());

        FileIndex[] indices = null;

        if (moduleList.size() != 0) {
            indices = new FileIndex[moduleList.size()];
            int idx = 0;
            for (final Module module : moduleList) {
                indices[idx++] = ModuleRootManager.getInstance(module).getFileIndex();
            }
        }
        return indices;
    }

    @SuppressWarnings("unused")
    @NotNull
    public VirtualFile[] getFiles(final FileType fileType, final boolean inSourceOnly, boolean inMainModule,
                                  boolean inSubModules) {
        final List<VirtualFile> files = new ArrayList<VirtualFile>();
        final FileIndex[] fileIndices = getFileIndices(inMainModule, inSubModules);
        for (final FileIndex fileIndex : fileIndices) {
            fileIndex.iterateContent(new CompilerContentIterator(fileType, fileIndex, inSourceOnly, files));
        }
        return VfsUtil.toVirtualFileArray(files);
    }

    public List<VirtualFile> getWebModuleParentsContentRootFolder() {
        List<VirtualFile> preferredContentRoots = new ArrayList<VirtualFile>();
        for (Module webModulesParent : getWebModulesParents()) {
            VirtualFile preferredContentEntry = getPreferredContentEntry(webModulesParent);
            if (preferredContentEntry != null)
                preferredContentRoots.add(preferredContentEntry.getCanonicalFile());
        }
        return preferredContentRoots;
    }

    private void getUsedDependencies() {
        for (OrderEntry orderEntry : getModifiableRootModel().getOrderEntries()) {
            if (orderEntry instanceof LibraryOrderEntry)
                libraries.add(((LibraryOrderEntry) orderEntry).getLibrary());
            else if (orderEntry instanceof ModuleOrderEntry)
                dependencies.add(((ModuleOrderEntry) orderEntry).getModule());
        }
    }

    private List<Module> getRecursivelyWebModuleParents(@NotNull Module module) {
        Module[] result;

        if (RandoriWebModuleType.isOfType(module))
            result = new Module[]{module};
        else {
            result = new Module[0];
            List<Module> parents = ModuleManager.getInstance(module.getProject()).getModuleDependentModules(module);
            for (Module parent : parents) {
                if (RandoriWebModuleType.isOfType(parent))
                    result = ArrayUtils.add(result, parent);
                else {
                    final List<Module> webModuleParents = getRecursivelyWebModuleParents(parent);
                    if (!webModuleParents.isEmpty())
                        result = ArrayUtils.addAll(result, webModuleParents.toArray(new Module[webModuleParents.size()]));
                }
            }
        }
        return Arrays.asList(result);
    }

    private VirtualFile getPreferredContentEntry(final Module module) {
        RandoriModuleComponent webModuleComponent = module.getComponent(RandoriModuleComponent.class);
        ModifiableRootModel modifiableRootModel = webModuleComponent.getModifiableRootModel();
        ContentEntry[] contentEntries = modifiableRootModel.getContentEntries();
        ContentEntry preferredContentEntry = null;
        if (contentEntries.length > 0) {
            preferredContentEntry = contentEntries[0];
            for (ContentEntry contentEntry : contentEntries) {
                if (contentEntry.getFile() != null) {
                    //noinspection ConstantConditions
                    String modulePathName = contentEntry.getFile().getCanonicalPath().toLowerCase();
                    String suffix = module.getName().toLowerCase();
                    if (modulePathName.endsWith(suffix)) {
                        preferredContentEntry = contentEntry;
                        break;
                    }
                }
            }
        }
        return (preferredContentEntry == null) ? null : preferredContentEntry.getFile();
    }
}
