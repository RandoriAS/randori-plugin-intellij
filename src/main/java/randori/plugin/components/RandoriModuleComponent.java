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
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
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
        dependencies = null;
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

    public List<Module> getDependencies() {
        if (dependencies == null)
            updateDependencies();

        return dependencies;
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
        getUsedDependencies(module);
        dependencies.remove(0);

        webModulesParents = RandoriWebModuleType.isOfType(module) ?
                Arrays.asList(module) :
                ModuleUtil.getParentModulesOfType(RandoriWebModuleType.getInstance(), module);
    }

    private void getUsedDependencies(Module module) {
        dependencies.add(module);
        final Module[] usedDependencies = ModuleRootManager.getInstance(module).getDependencies();
        for (Module dependency : usedDependencies) {
            if (!dependencies.contains(dependency)) {
                getUsedDependencies(dependency);
            }
        }
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

    private VirtualFile getPreferredContentEntry(final Module module) {
        RandoriModuleComponent webModuleComponent = module.getComponent(RandoriModuleComponent.class);
        ModifiableRootModel modifiableRootModel = webModuleComponent.getModifiableRootModel();
        ContentEntry[] contentEntries = modifiableRootModel.getContentEntries();
        ContentEntry preferredContentEntry = contentEntries[0];
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
        return preferredContentEntry.getFile();
    }
}
