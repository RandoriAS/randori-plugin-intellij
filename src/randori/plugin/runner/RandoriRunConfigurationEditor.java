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

import javax.swing.*;

import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Schmalle
 * @author Roland Zwaga
 */
public class RandoriRunConfigurationEditor extends
        SettingsEditor<RandoriRunConfiguration> implements ActionListener
{
    private JPanel panel;

    private JTextField indexRoot;

    @SuppressWarnings({ "rawtypes" })
    private JComboBox modules;
    private JTextField webRoot;
    private JRadioButton useEmbeddedServer;
    private JRadioButton useExistingWebserver;
    private ButtonGroup webserverGroup;

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
        configuration.useExplicitWebroot = (useExistingWebserver.isSelected());
        moduleSelector.applyTo(configuration);
    }

    @Override
    protected void resetEditorFrom(RandoriRunConfiguration configuration)
    {
        // reset ui components with config data
        //webRoot.setText(configuration.webRoot);
        indexRoot.setText(configuration.indexRoot);
        webRoot.setText(configuration.explicitWebroot);
        if (configuration.useExplicitWebroot)
        {
            useExistingWebserver.setSelected(true);
            useEmbeddedServer.setSelected(false);
        }
        else
        {
            useExistingWebserver.setSelected(false);
            useEmbeddedServer.setSelected(true);
        }
        webRoot.setEnabled(configuration.useExplicitWebroot);
        moduleSelector.reset(configuration);
    }

    @Override
    protected JComponent createEditor()
    {
        webserverGroup = new ButtonGroup();
        webserverGroup.add(useEmbeddedServer);
        webserverGroup.add(useExistingWebserver);

        useEmbeddedServer.setActionCommand("EMBEDDED");
        useExistingWebserver.setActionCommand("EXISTING");

        useEmbeddedServer.addActionListener(this);
        useExistingWebserver.addActionListener(this);

        moduleSelector = new ConfigurationModuleSelector(project, modules);
        return panel;
    }

    public void actionPerformed(ActionEvent e) {
        webRoot.setEnabled(e.getActionCommand() == "EXISTING");
    }

    @Override
    protected void disposeEditor()
    {
        panel.setVisible(false);
    }

}
