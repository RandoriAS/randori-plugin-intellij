/***
 * Copyright 2013 Teoti Graphix, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * @author Michael Schmalle <mschmalle@teotigraphix.com>
 */

package randori.plugin.runner;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;

/**
 * @author Michael Schmalle
 */
public class RandoriRunConfigurationEditor extends
        SettingsEditor<RandoriRunConfiguration>
{
    private JPanel panel;

    private JTextField indexRoot;

    @SuppressWarnings({ "rawtypes" })
    private JComboBox modules;
    private JTextField webRoot;

    //private JTextField webRoot;

    private Project project;

    private ConfigurationModuleSelector moduleSelector;

    public RandoriRunConfigurationEditor(Project project)
    {
        this.project = project;
    }

    @Override
    protected void applyEditorTo(RandoriRunConfiguration configuration)
            throws ConfigurationException
    {
        // apply the ui component values to the configuration
        //configuration.webRoot = webRoot.getText();
        configuration.indexRoot = indexRoot.getText();
        configuration.explicitWebroot = webRoot.getText();
        moduleSelector.applyTo(configuration);
    }

    @Override
    protected void resetEditorFrom(RandoriRunConfiguration configuration)
    {
        // reset ui components with config data
        //webRoot.setText(configuration.webRoot);
        indexRoot.setText(configuration.indexRoot);
        webRoot.setText(configuration.explicitWebroot);
        moduleSelector.reset(configuration);
    }

    @Override
    protected JComponent createEditor()
    {
        moduleSelector = new ConfigurationModuleSelector(project, modules);
        return panel;
    }

    @Override
    protected void disposeEditor()
    {
        panel.setVisible(false);
    }

}
