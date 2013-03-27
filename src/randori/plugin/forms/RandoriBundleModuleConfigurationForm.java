package randori.plugin.forms;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

import randori.plugin.components.RandoriBundleModuleComponent;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;

public class RandoriBundleModuleConfigurationForm extends
        SettingsEditor<RandoriBundleModuleModel>
{
    private JPanel panel;
    private JCheckBox exportAsFiles;

    @Override
    protected void resetEditorFrom(RandoriBundleModuleModel model)
    {
        exportAsFiles.setSelected(model.isClassesAsFile());
    }

    @Override
    protected void applyEditorTo(
            RandoriBundleModuleModel randoriBundleModuleModel)
            throws ConfigurationException
    {

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

    public boolean isModified(RandoriBundleModuleComponent data)
    {
        return (exportAsFiles.isSelected() == data.isClassesAsFile());
    }

    public void getData(RandoriBundleModuleComponent data)
    {
        data.setClassesAsFile(exportAsFiles.isSelected());
    }

    public void setData(RandoriBundleModuleComponent data)
    {
        exportAsFiles.setSelected(data.isClassesAsFile());
    }

}
