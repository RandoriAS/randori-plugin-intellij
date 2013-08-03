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

package randori.plugin.projectStructure.detection;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.util.ui.UIUtil;
import randori.plugin.ui.RandoriSdkComboBoxWithBrowseButton;

import javax.swing.*;

/**
 * @author Frédéric THOMAS Date: 19/04/13 Time: 20:44
 */
public class RandoriSdkStep extends ModuleWizardStep
{

    private final WizardContext context;
    private final JPanel contentPane;
    private final RandoriSdkComboBoxWithBrowseButton sdkCombo;
    private final JLabel sdkLabel;

    public RandoriSdkStep(WizardContext context)
    {
        this.context = context;

        Sdk sdk = null;
        if (context.getProject() != null)
            sdk = ProjectRootManager.getInstance(context.getProject()).getProjectSdk();

        sdkLabel = new JLabel();
        sdkCombo = new RandoriSdkComboBoxWithBrowseButton();

        if (sdk != null)
            sdkCombo.setSelectedSdkRaw(sdk.getName());

        contentPane = new JPanel();
        contentPane.add(sdkLabel);
        contentPane.add(sdkCombo);
    }

    @Override
    public JComponent getComponent()
    {
        this.sdkLabel.setLabelFor(this.sdkCombo.getChildComponent());
        String text = this.context.getProject() != null ? "Module &SDK:" : "Project &SDK:";
        this.sdkLabel.setText(UIUtil.removeMnemonic(text));
        this.sdkLabel.setDisplayedMnemonicIndex(UIUtil.getDisplayMnemonicIndex(text));

        return this.contentPane;
    }

    @Override
    public void updateDataModel()
    {
        this.context.setProjectJdk(this.sdkCombo.getSelectedSdk());
    }
}
