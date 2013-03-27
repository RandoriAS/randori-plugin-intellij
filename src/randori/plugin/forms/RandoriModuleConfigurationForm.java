package randori.plugin.forms;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;
import randori.plugin.components.RandoriModuleComponent;

import javax.swing.*;

/**
 * @author Michael Schmalle
 */
public class RandoriModuleConfigurationForm extends
        SettingsEditor<RandoriModuleModel>
{

    private JPanel panel;

    private JTextField basePath;

    private JTextField libraryPath;

    private JCheckBox exportAsFiles;

    public void getData(RandoriModuleComponent data)
    {
        data.setBasePath(basePath.getText());
        data.setLibraryPath(libraryPath.getText());
        data.setClassesAsFile(exportAsFiles.isSelected());
    }

    public void setData(RandoriModuleComponent data)
    {
        basePath.setText(data.getBasePath());
        libraryPath.setText(data.getLibraryPath());
        exportAsFiles.setSelected(data.isClassesAsFile());
    }

    @Override
    protected void resetEditorFrom(RandoriModuleModel model)
    {
        // apply the saved model to the components
        basePath.setText(model.getBasePath());
        libraryPath.setText(model.getLibraryPath());
        exportAsFiles.setSelected(model.isClassesAsFile());
    }

    @Override
    protected void applyEditorTo(RandoriModuleModel s)
            throws ConfigurationException
    {
        // saved the state of the components to the config state
        // TODO implement the properties in the config
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

    public boolean isModified(RandoriModuleComponent data)
    {
        return isModified(basePath, data.getBasePath())
                || isModified(libraryPath, data.getLibraryPath());

    }

    private boolean isModified(JTextField field, String value)
    {
        return field.getText() != null ? !field.getText().equals(value)
                : value != null;
    }
}
