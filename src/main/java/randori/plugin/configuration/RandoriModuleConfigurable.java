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

package randori.plugin.configuration;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;

/**
 * @author Michael Schmalle
 */
public class RandoriModuleConfigurable extends SettingsEditor<RandoriModuleModel>
{

    private JPanel panel;
    private JCheckBox exportAsFiles;
    private JCheckBox generateRbl;

    public void getData(RandoriModuleModel model)
    {
        model.setExportAsFile(exportAsFiles.isSelected());
        model.setGenerateRbl(generateRbl.isSelected());
    }

    public void setData(RandoriModuleModel model)
    {
        exportAsFiles.setSelected(model.isExportAsFile());
        generateRbl.setSelected(model.isGenerateRbl());
    }

    @Override
    protected void resetEditorFrom(RandoriModuleModel model)
    {
        exportAsFiles.setSelected(model.isExportAsFile());
        generateRbl.setSelected(model.isGenerateRbl());
    }

    @Override
    protected void applyEditorTo(RandoriModuleModel model) throws ConfigurationException
    {
        model.setExportAsFile(exportAsFiles.isSelected());
        model.setGenerateRbl(generateRbl.isSelected());
    }

    @NotNull
    @Override
    protected JComponent createEditor()
    {
        return panel;
    }

    @Override
    protected void disposeEditor()
    {
    }

    public boolean isModified(RandoriModuleModel model)
    {
        return exportAsFiles.isSelected() != model.isExportAsFile() || generateRbl.isSelected() != model.isGenerateRbl();
    }
}
