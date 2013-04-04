package randori.plugin.forms;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;
import randori.plugin.components.RandoriProjectModel;

import javax.swing.*;

public class RandoriBundleProjectConfigurationForm extends
        SettingsEditor<RandoriBundleProjectConfigurationForm>
{
    private JPanel panel;

    private JCheckBox exportAsFiles;

    public void getData(RandoriProjectModel data)
    {
        data.setClassesAsFile(exportAsFiles.isSelected());
    }

    public void setData(RandoriProjectModel data)
    {
        exportAsFiles.setSelected(data.isClassesAsFile());
    }

    @Override
    protected void resetEditorFrom(RandoriBundleProjectConfigurationForm model)
    {
        // apply the saved model to the components
        //        basePath.setText(model.getBasePath());
        //        libraryPath.setText(model.getLibraryPath());
        //        exportAsFiles.setSelected(model.isClassesAsFile());
    }

    @Override
    protected void applyEditorTo(RandoriBundleProjectConfigurationForm s)
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

    public boolean isModified(RandoriProjectModel data)
    {
        if (isOpposite(exportAsFiles, data.isClassesAsFile()))
        {
            return true;
        }
        return false;
    }

    private boolean isOpposite(JCheckBox component, boolean selected)
    {
        return component.isSelected() != selected;
    }

}
