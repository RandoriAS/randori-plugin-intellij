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

    private final RandoriCompilerModel myConfig;

    private JCheckBox makeOnSave;
    private JPanel myMainPanel;
    private JPanel panel;
    private JTextField basePath;
    private JTextField libraryPath;

    public RandoriCompilerConfigurable (Project project) {
        myConfig = RandoriCompilerModel.getInstance(project);
    }
    @Nullable
    @Override
    public JComponent createComponent() {
        makeOnSave.setEnabled(RandoriCompilerModel.makeProjectOnSaveEnabled);
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        return !Comparing.equal(myConfig.isMakeOnSave(), makeOnSave.isSelected());
    }

    @Override
    public void apply() throws ConfigurationException {
        myConfig.setMakeOnSave(makeOnSave.isSelected());
    }

    @Override
    public void reset() {
        makeOnSave.setSelected(myConfig.isMakeOnSave());
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
