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
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;

/**
 * @author Frédéric THOMAS
 */
public class RandoriCompilerConfigurable implements SearchableConfigurable, Configurable.NoScroll {

    private static final String COMPONENT_LABEL = "Randori compiler";

    private final RandoriCompilerModel model;

    private JCheckBox makeOnSave;
    private JPanel myMainPanel;
    private JPanel panel;
    private JTextField basePath;
    private JTextField libraryPath;
    private JCheckBox validateCSSClasses;

    public RandoriCompilerConfigurable (Project project) {
        model = RandoriCompilerModel.getInstance(project);
    }
    @Nullable
    @Override
    public JComponent createComponent() {
        makeOnSave.setEnabled(RandoriCompilerModel.makeProjectOnSaveEnabled);
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        return !Comparing.equal(model.isMakeOnSave(), makeOnSave.isSelected()) || isModified(basePath, model.getBasePath()) || isModified(libraryPath, model.getLibraryPath())
                || validateCSSClasses.isSelected() != model.isValidateCSSClasses();
    }

    private boolean isModified(JTextField component, String value)
    {
        return component.getText() != null ? !component.getText().equals(value) : value != null;
    }

    @Override
    public void apply() throws ConfigurationException {
        model.setMakeOnSave(makeOnSave.isSelected());
        model.setBasePath(basePath.getText());
        model.setLibraryPath(libraryPath.getText());
        model.setValidateCSSClasses(validateCSSClasses.isSelected());
    }

    @Override
    public void reset() {
        makeOnSave.setSelected(model.isMakeOnSave());
        basePath.setText(model.getBasePath());
        libraryPath.setText(model.getLibraryPath());
        validateCSSClasses.setSelected(model.isValidateCSSClasses());
    }

    @Override
    public void disposeUIResources() {
        // TODO implement method
    }

    @NotNull
    @Override
    public String getId() {
        return COMPONENT_LABEL;
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return COMPONENT_LABEL;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;  // TODO implement method
    }
}
