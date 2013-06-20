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

package randori.plugin.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.projectRoots.ui.ProjectJdksEditor;
import com.intellij.openapi.roots.ui.configuration.ProjectJdksConfigurable;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.JdkListConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import org.jetbrains.annotations.Nullable;
import randori.plugin.roots.RandoriSdkType;

/**
 * @author Frédéric THOMAS
 * Date: 20/04/13
 * Time: 08:39
 */
public class RandoriSdkComboBoxWithBrowseButton extends ComboboxWithBrowseButton
{
    private static final Condition<Sdk> RANDORI_SDK = new Condition<Sdk>() {
        @Override
        public boolean value(Sdk sdk) {
            return (sdk != null) && ((sdk.getSdkType() instanceof RandoriSdkType));
        }
    };

    public static final String BC_SDK_KEY = "BC SDK";
    private final Condition<Sdk> sdkFilter;
    private final BCSdk bcSdk = new BCSdk();
    private boolean showBCSdk = false;

    public RandoriSdkComboBoxWithBrowseButton() {
        this.sdkFilter = RandoriSdkComboBoxWithBrowseButton.RANDORI_SDK;
        rebuildSdkListAndSelectSdk(null);

        final JComboBox sdkCombo = getComboBox();
        sdkCombo.setRenderer(new ListCellRendererWrapper()
        {
            public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                if ((value instanceof RandoriSdkComboBoxWithBrowseButton.BCSdk)) {
                    Sdk sdk = ((RandoriSdkComboBoxWithBrowseButton.BCSdk)value).mySdk;
                    if (sdk == null) {
                        if (sdkCombo.isEnabled()) {
                            setText("<html>SDK set for the build configuration <font color='red'>[not set]</font></html>");
                            setIcon(null);
                        }
                        else {
                            setText("SDK set for the build configuration [not set]");
                            setIcon(null);
                        }
                    }
                    else {
                        setText("SDK set for the build configuration [" + sdk.getName() + "]");
                        setIcon(((SdkType)((RandoriSdkComboBoxWithBrowseButton.BCSdk)value).mySdk.getSdkType()).getIcon());
                    }
                }
                else if ((value instanceof String)) {
                    if (sdkCombo.isEnabled()) {
                        setText("<html><font color='red'>" + value + " [Invalid]</font></html>");
                        setIcon(null);
                    }
                    else {
                        setText(value + " [Invalid]");
                        setIcon(null);
                    }
                }
                else if ((value instanceof Sdk)) {
                    setText(((Sdk)value).getName());
                    setIcon(((SdkType)((Sdk)value).getSdkType()).getIcon());
                }
                else if (sdkCombo.isEnabled()) {
                    setText("<html><font color='red'>[none]</font></html>");
                }
                else {
                    setText("[none]");
                }
            }
        });
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Project project = PlatformDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
                if (project == null) {
                    project = ProjectManager.getInstance().getDefaultProject();
                }

                ProjectSdksModel sdksModel = ProjectStructureConfigurable.getInstance(project).getProjectJdksModel();
                sdksModel = new RandoriSdkComboBoxWithBrowseButton.NonCommittingWrapper(sdksModel, JdkListConfigurable.getInstance(project));

                ProjectJdksEditor editor = new ProjectJdksEditor(null, RandoriSdkComboBoxWithBrowseButton.this, new ProjectJdksConfigurable(project, sdksModel));

                editor.show();
                if (editor.isOK()) {
                    Sdk selectedSdk = editor.getSelectedJdk();
                    if (RandoriSdkComboBoxWithBrowseButton.this.sdkFilter.value(selectedSdk)) {
                        RandoriSdkComboBoxWithBrowseButton.this.rebuildSdkListAndSelectSdk(selectedSdk);
                    }
                    else {
                        RandoriSdkComboBoxWithBrowseButton.this.rebuildSdkListAndSelectSdk(null);
                        if (selectedSdk != null)
                            Messages.showErrorDialog(RandoriSdkComboBoxWithBrowseButton.this, "SDK '" + selectedSdk.getName() + "' can not be selected here.\\nPlease select a Randori SDK.", "Select a Randori SDK");
                    }
                }
            }
        });
    }

    private void rebuildSdkListAndSelectSdk(@Nullable Sdk selectedSdk)
    {
        String previousSelectedSdkName = getSelectedSdkRaw();
        List<Object> sdkList = new ArrayList<Object>();

        if (this.showBCSdk) {
            sdkList.add(this.bcSdk);
        }

        Sdk[] sdks = ProjectJdkTable.getInstance().getAllJdks();
        for (Sdk sdk : sdks) {
            if (this.sdkFilter.value(sdk)) {
                sdkList.add(sdk);
            }
        }

        if (!sdkList.isEmpty())
        {
            Collections.sort(sdkList, new Comparator<Object>() {
                public int compare(Object sdk1, Object sdk2) {
                    if ((sdk1 == RandoriSdkComboBoxWithBrowseButton.this.bcSdk) && (sdk2 != RandoriSdkComboBoxWithBrowseButton.this.bcSdk)) return -1;
                    if ((sdk1 != RandoriSdkComboBoxWithBrowseButton.this.bcSdk) && (sdk2 == RandoriSdkComboBoxWithBrowseButton.this.bcSdk)) return 1;

                    if (((sdk1 instanceof Sdk)) && ((sdk2 instanceof Sdk))) {
                        SdkTypeId type1 = ((Sdk)sdk1).getSdkType();
                        SdkTypeId type2 = ((Sdk)sdk2).getSdkType();

                        if (type1 == type2) return -StringUtil.compareVersionNumbers(((Sdk)sdk1).getVersionString(), ((Sdk)sdk2).getVersionString());
                        if (type1 == RandoriSdkType.getInstance()) return -1;
                        if (type2 == RandoriSdkType.getInstance()) return 1;
                    }

                    return 0;
                }
            });
            getComboBox().setModel(new DefaultComboBoxModel(ArrayUtil.toObjectArray(sdkList)));
            if (selectedSdk != null) {
                setSelectedSdkRaw(selectedSdk.getName(), false);
            }
            else if (previousSelectedSdkName != null)
                setSelectedSdkRaw(previousSelectedSdkName, false);
        }
        else
        {
            getComboBox().setModel(new DefaultComboBoxModel(new Object[] { null }));
        }
    }

    public void addComboboxListener(final Listener listener) {
        getComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listener.stateChanged();
            }
        });
        getComboBox().addPropertyChangeListener("model", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                listener.stateChanged();
            }
        });
    }

    @Nullable
    public Sdk getSelectedSdk() {
        Object selectedItem = getComboBox().getSelectedItem();

        if ((selectedItem instanceof BCSdk)) {
            return ((BCSdk)selectedItem).mySdk;
        }
        if ((selectedItem instanceof Sdk)) {
            return (Sdk)selectedItem;
        }

        return null;
    }

    String getSelectedSdkRaw()
    {
        Object selectedItem = getComboBox().getSelectedItem();

        if ((selectedItem instanceof BCSdk)) {
            return "BC SDK";
        }
        if ((selectedItem instanceof Sdk)) {
            return ((Sdk)selectedItem).getName();
        }
        if ((selectedItem instanceof String)) {
            return (String)selectedItem;
        }

        return "";
    }

    public void setSelectedSdkRaw(String sdkName)
    {
        setSelectedSdkRaw(sdkName, true);
    }

    private void setSelectedSdkRaw(String sdkName, boolean addErrorItemIfSdkNotFound) {
        JComboBox combo = getComboBox();

        if ("BC SDK".equals(sdkName)) {
            combo.setSelectedItem(this.bcSdk);
            return;
        }

        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (((item instanceof Sdk)) && (((Sdk)item).getName().equals(sdkName))) {
                combo.setSelectedItem(item);
                return;
            }

        }

        if (addErrorItemIfSdkNotFound) {
            List<Object> items = new ArrayList<Object>();
            items.add(sdkName);
            for (int i = 0; i < combo.getItemCount(); i++) {
                Object item = combo.getItemAt(i);
                if (!(item instanceof String)) {
                    items.add(item);
                }
            }
            combo.setModel(new DefaultComboBoxModel(ArrayUtil.toObjectArray(items)));
        }
    }

    public void showBCSdk(boolean showBCSdk) {
        if (this.showBCSdk != showBCSdk) {
            this.showBCSdk = showBCSdk;
            Object selectedItem = getComboBox().getSelectedItem();
            rebuildSdkListAndSelectSdk(null);
            if ((selectedItem instanceof String))
                setSelectedSdkRaw((String)selectedItem, true);
        }
    }

    public void setBCSdk(Sdk sdk)
    {
        if (sdk != this.bcSdk.mySdk)
            this.bcSdk.mySdk = sdk;
    }

    private static class NonCommittingWrapper extends ProjectSdksModel {
        private final ProjectSdksModel myOriginal;
        private final JdkListConfigurable myConfigurable;

        public NonCommittingWrapper(ProjectSdksModel original, JdkListConfigurable configurable) {
            this.myOriginal = original;
            this.myConfigurable = configurable;
        }

        public void apply() throws ConfigurationException {
            apply(null);
        }

        public void apply(@Nullable MasterDetailsComponent configurable) throws ConfigurationException {
            this.myConfigurable.reset();
        }

        public void reset(@Nullable Project project)
        {
        }

        public void addListener(SdkModel.Listener listener) {
            this.myOriginal.addListener(listener);
        }

        public void removeListener(SdkModel.Listener listener) {
            this.myOriginal.removeListener(listener);
        }

        public SdkModel.Listener getMulticaster() {
            return this.myOriginal.getMulticaster();
        }

        public Sdk[] getSdks() {
            return this.myOriginal.getSdks();
        }

        public Sdk findSdk(String sdkName) {
            return this.myOriginal.findSdk(sdkName);
        }

        public void disposeUIResources()
        {
        }

        public HashMap<Sdk, Sdk> getProjectSdks() {
            return this.myOriginal.getProjectSdks();
        }

        public boolean isModified() {
            return this.myOriginal.isModified();
        }

        public void removeSdk(Sdk editableObject) {
            this.myOriginal.removeSdk(editableObject);
        }

        public void createAddActions(DefaultActionGroup group, JComponent parent, Consumer<Sdk> updateTree, @Nullable Condition<SdkTypeId> filter)
        {
            this.myOriginal.createAddActions(group, parent, updateTree, filter);
        }

        public void doAdd(JComponent parent, SdkType type, Consumer<Sdk> updateTree) {
            this.myOriginal.doAdd(parent, type, updateTree);
        }

        public void addSdk(Sdk sdk) {
            this.myOriginal.addSdk(sdk);
        }

        public void doAdd(Sdk newSdk, @Nullable Consumer<Sdk> updateTree) {
            this.myOriginal.doAdd(newSdk, updateTree);
        }

        public Sdk findSdk(@Nullable Sdk modelJdk) {
            return this.myOriginal.findSdk(modelJdk);
        }

        public Sdk getProjectSdk() {
            return this.myOriginal.getProjectSdk();
        }

        public void setProjectSdk(Sdk projectSdk) {
            this.myOriginal.setProjectSdk(projectSdk);
        }

        public boolean isInitialized() {
            return this.myOriginal.isInitialized();
        }
    }

    private static class BCSdk
    {
        private Sdk mySdk;
    }

    public static abstract interface Listener
    {
        public abstract void stateChanged();
    }
}
