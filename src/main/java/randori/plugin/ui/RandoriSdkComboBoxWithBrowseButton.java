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
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.projectRoots.ui.ProjectJdksEditor;
import com.intellij.openapi.roots.ui.configuration.ProjectJdksConfigurable;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nullable;
import randori.plugin.roots.RandoriSdkType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Frédéric THOMAS
 *         Date: 20/04/13
 *         Time: 08:39
 */
public class RandoriSdkComboBoxWithBrowseButton extends ComboboxWithBrowseButton {
    private static final Condition<Sdk> RANDORI_SDK = new Condition<Sdk>() {
        @Override
        public boolean value(Sdk sdk) {
            return (sdk != null) && ((sdk.getSdkType() instanceof RandoriSdkType));
        }
    };

    private final Condition<Sdk> sdkFilter;

    public RandoriSdkComboBoxWithBrowseButton() {
        this.sdkFilter = RANDORI_SDK;
        rebuildSdkListAndSelectSdk(null);

        final JComboBox sdkCombo = getComboBox();
        sdkCombo.setRenderer(new ListCellRendererWrapper() {
            public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                if ((value instanceof Sdk)) {
                    setText(((Sdk) value).getName());
                    setIcon(((SdkType) ((Sdk) value).getSdkType()).getIcon());
                } else if (sdkCombo.isEnabled()) {
                    setText("<html><font color='red'>[none]</font></html>");
                } else {
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

                ProjectJdksEditor editor = new ProjectJdksEditor(null, RandoriSdkComboBoxWithBrowseButton.this, new ProjectJdksConfigurable(project, sdksModel));

                editor.show();
                if (editor.isOK()) {
                    Sdk selectedSdk = editor.getSelectedJdk();
                    if (sdkFilter.value(selectedSdk)) {
                        rebuildSdkListAndSelectSdk(selectedSdk);
                    } else {
                        rebuildSdkListAndSelectSdk(null);
                        if (selectedSdk != null)
                            Messages.showErrorDialog(RandoriSdkComboBoxWithBrowseButton.this, "SDK '" + selectedSdk.getName() + "' can not be selected here.\\nPlease select a Randori SDK.", "Select a Randori SDK");
                    }
                }
            }
        });
    }

    private void rebuildSdkListAndSelectSdk(@Nullable final Sdk selectedSdk) {
        String previousSelectedSdkName = getSelectedSdkRaw();
        List<Object> sdkList = new ArrayList<Object>();

        final ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
        if (selectedSdk != null) {
            Sdk jdk = projectJdkTable.findJdk(selectedSdk.getName(), selectedSdk.getSdkType().getName());
            if (jdk == null)
                projectJdkTable.addJdk(selectedSdk);
        }

        Sdk[] sdks = projectJdkTable.getAllJdks();
        for (Sdk sdk : sdks) {
            if (this.sdkFilter.value(sdk)) {
                sdkList.add(sdk);
            }
        }

        if (!sdkList.isEmpty()) {
            Collections.sort(sdkList, new Comparator<Object>() {
                public int compare(Object sdk1, Object sdk2) {

                    if (((sdk1 instanceof Sdk)) && ((sdk2 instanceof Sdk))) {
                        SdkTypeId type1 = ((Sdk) sdk1).getSdkType();
                        SdkTypeId type2 = ((Sdk) sdk2).getSdkType();

                        if (type1 == type2)
                            return -StringUtil.compareVersionNumbers(((Sdk) sdk1).getVersionString(), ((Sdk) sdk2).getVersionString());
                        if (type1 == RandoriSdkType.getInstance()) return -1;
                        if (type2 == RandoriSdkType.getInstance()) return 1;
                    }

                    return 0;
                }
            });
            getComboBox().setModel(new DefaultComboBoxModel(ArrayUtil.toObjectArray(sdkList)));
            if (selectedSdk != null) {
                setSelectedSdkRaw(selectedSdk.getName(), false);
            } else if (previousSelectedSdkName != null)
                setSelectedSdkRaw(previousSelectedSdkName, false);
        } else {
            getComboBox().setModel(new DefaultComboBoxModel(new Object[]{null}));
        }
    }

    public void addComboBoxListener(final Listener listener) {
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

        if ((selectedItem instanceof Sdk)) {
            return (Sdk) selectedItem;
        }

        return null;
    }

    String getSelectedSdkRaw() {
        Object selectedItem = getComboBox().getSelectedItem();

        if ((selectedItem instanceof Sdk)) {
            return ((Sdk) selectedItem).getName();
        }
        if ((selectedItem instanceof String)) {
            return (String) selectedItem;
        }

        return "";
    }

    public void setSelectedSdkRaw(String sdkName) {
        setSelectedSdkRaw(sdkName, true);
    }

    private void setSelectedSdkRaw(String sdkName, boolean addErrorItemIfSdkNotFound) {
        JComboBox combo = getComboBox();

        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (((item instanceof Sdk)) && (((Sdk) item).getName().equals(sdkName))) {
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

    public static abstract interface Listener {
        public abstract void stateChanged();
    }
}
