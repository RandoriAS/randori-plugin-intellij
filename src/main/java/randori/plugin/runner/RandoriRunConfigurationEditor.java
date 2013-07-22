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

package randori.plugin.runner;

import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.ui.ComboboxSpeedSearch;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.SortedComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import randori.plugin.module.RandoriWebModuleType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Michael Schmalle
 * @author Roland Zwaga
 */
class RandoriRunConfigurationEditor extends
        SettingsEditor<RandoriRunConfiguration> implements ActionListener {
    private JPanel panel;

    private JTextField indexRoot;

    @SuppressWarnings({"rawtypes"})
    private JComboBox modules;
    private JTextField webRoot;
    private JRadioButton useEmbeddedServer;
    private JRadioButton useExistingWebServer;
    private ButtonGroup webServerGroup;

    //private JTextField webRoot;

    private final Project project;

    private RandoriConfigurationModuleSelector moduleSelector;

    public RandoriRunConfigurationEditor(Project project) {
        this.project = project;
    }

    @Override
    protected void applyEditorTo(RandoriRunConfiguration configuration)
            throws ConfigurationException {
        // apply the ui component values to the configuration
        //configuration.webRoot = webRoot.getText();
        configuration.indexRoot = indexRoot.getText();
        configuration.explicitWebroot = webRoot.getText();
        configuration.useExplicitWebroot = (useExistingWebServer.isSelected());
        moduleSelector.applyTo(configuration);
    }

    @Override
    protected void resetEditorFrom(RandoriRunConfiguration configuration) {
        // reset ui components with config data
        //webRoot.setText(configuration.webRoot);
        indexRoot.setText(configuration.indexRoot);
        webRoot.setText(configuration.explicitWebroot);
        if (configuration.useExplicitWebroot) {
            useExistingWebServer.setSelected(true);
            useEmbeddedServer.setSelected(false);
        } else {
            useExistingWebServer.setSelected(false);
            useEmbeddedServer.setSelected(true);
        }
        webRoot.setEnabled(configuration.useExplicitWebroot);
        moduleSelector.reset(configuration);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        webServerGroup = new ButtonGroup();
        webServerGroup.add(useEmbeddedServer);
        webServerGroup.add(useExistingWebServer);

        useEmbeddedServer.setActionCommand("EMBEDDED");
        useExistingWebServer.setActionCommand("EXISTING");

        useEmbeddedServer.addActionListener(this);
        useExistingWebServer.addActionListener(this);

        moduleSelector = new RandoriConfigurationModuleSelector(project, modules);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        webRoot.setEnabled(e.getActionCommand().equals("EXISTING"));
    }

    @Override
    protected void disposeEditor() {
        panel.setVisible(false);
    }

    public class RandoriConfigurationModuleSelector {

        private final String _noModule;
        private final Project _project;
        private final JComboBox _modulesList;
        private final SortedComboBoxModel<Object> myModules = new SortedComboBoxModel<Object>(new Comparator<Object>() {
            public int compare(final Object module, final Object module1) {
                if (module instanceof Module && module1 instanceof Module) {
                    return ((Module) module).getName().compareToIgnoreCase(((Module) module1).getName());
                }
                return -1;
            }
        });

        public RandoriConfigurationModuleSelector(final Project project, final JComboBox modulesList) {
            this(project, modulesList, "<no module>");
        }

        public RandoriConfigurationModuleSelector(final Project project, final JComboBox modulesList, String noModule) {
            _noModule = noModule;
            _project = project;
            _modulesList = modulesList;
            new ComboboxSpeedSearch(modulesList) {
                protected String getElementText(Object element) {
                    if (element instanceof Module && RandoriWebModuleType.isOfType((Module) element)) {
                        return ((Module) element).getName();
                    } else if (element == null) {
                        return _noModule;
                    }
                    return super.getElementText(element);
                }
            };
            _modulesList.setModel(myModules);
            _modulesList.setRenderer(new ListCellRendererWrapper() {
                @Override
                public void customize(final JList list, final Object value, final int index, final boolean selected, final boolean hasFocus) {
                    if (value instanceof Module) {
                        final Module module = (Module) value;
                        setIcon(ModuleType.get(module).getIcon());
                        setText(module.getName());
                    } else if (value == null) {
                        setText(_noModule);
                    }
                }
            });
        }

        public void applyTo(final ModuleBasedConfiguration configurationModule) {
            configurationModule.setModule((Module) _modulesList.getSelectedItem());
        }

        public void reset(final ModuleBasedConfiguration configuration) {
            final Module[] modules = ModuleManager.getInstance(getProject()).getModules();
            final List<Module> list = new ArrayList<Module>();
            for (final Module module : modules) {
                if (isModuleAccepted(module)) list.add(module);
            }
            setModules(list);
            myModules.setSelectedItem(configuration.getConfigurationModule().getModule());
        }

        public boolean isModuleAccepted(final Module module) {
            return ModuleTypeManager.getInstance().isClasspathProvider(ModuleType.get(module)) && RandoriWebModuleType.isOfType(module);
        }

        public Project getProject() {
            return _project;
        }

        public JavaRunConfigurationModule getConfigurationModule() {
            final JavaRunConfigurationModule configurationModule = new JavaRunConfigurationModule(getProject(), false);
            configurationModule.setModule((Module) myModules.getSelectedItem());
            return configurationModule;
        }

        private void setModules(final Collection<Module> modules) {
            myModules.clear();
            myModules.add(null);
            for (Module module : modules) {
                myModules.add(module);
            }
        }

        public Module getModule() {
            return (Module) myModules.getSelectedItem();
        }

        @Nullable
        public PsiClass findClass(final String className) {
            return getConfigurationModule().findClass(className);
        }

        public String getModuleName() {
            final Module module = (Module) myModules.getSelectedItem();
            return module == null ? "" : module.getName();
        }
    }
}
