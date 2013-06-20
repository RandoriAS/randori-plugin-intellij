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
 * @author Frédéric THOMAS
 */
public class RandoriProjectConfigurable extends SettingsEditor<RandoriProjectModel>
{

    private JPanel panel;
    private JTextField basePath;
    private JTextField libraryPath;
    private JCheckBox validateCSSClasses;

    public void getData(RandoriProjectModel model)
    {
        model.setBasePath(basePath.getText());
        model.setLibraryPath(libraryPath.getText());
        model.setValidateCSSClasses(validateCSSClasses.isSelected());
    }

    public void setData(RandoriProjectModel model)
    {
        basePath.setText(model.getBasePath());
        libraryPath.setText(model.getLibraryPath());
        validateCSSClasses.setSelected(model.isValidateCSSClasses());
    }

    @Override
    protected void resetEditorFrom(RandoriProjectModel model)
    {
        basePath.setText(model.getBasePath());
        libraryPath.setText(model.getLibraryPath());
        validateCSSClasses.setSelected(model.isValidateCSSClasses());
    }

    @Override
    protected void applyEditorTo(RandoriProjectModel model) throws ConfigurationException
    {
        model.setBasePath(basePath.getText());
        model.setLibraryPath(libraryPath.getText());
        model.setValidateCSSClasses(validateCSSClasses.isSelected());
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

    public boolean isModified(RandoriProjectModel model)
    {
        return isModified(basePath, model.getBasePath()) || isModified(libraryPath, model.getLibraryPath())
                || validateCSSClasses.isSelected() == model.isValidateCSSClasses();

    }

    private boolean isModified(JTextField component, String value)
    {
        return component.getText() != null ? !component.getText().equals(value) : value != null;
    }
}
