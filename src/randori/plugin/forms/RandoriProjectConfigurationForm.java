package randori.plugin.forms;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.NotNull;

import randori.plugin.components.RandoriProjectModel;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;

/**
 * @author Michael Schmalle
 */
public class RandoriProjectConfigurationForm extends
        SettingsEditor<RandoriProjectConfigurationForm>
{

    private JPanel panel;

    private JTextField basePath;

    private JTextField libraryPath;
    private JTextField webRoot;

    //private JCheckBox exportAsFiles;

    //private JTextField webRoot;

    //private JTextField port;

    public void getData(RandoriProjectModel data)
    {
        //data.setWebRoot(webRoot.getText());
        //data.setPort(Integer.valueOf(port.getText()));
        data.setBasePath(basePath.getText());
        data.setLibraryPath(libraryPath.getText());
        //data.setClassesAsFile(exportAsFiles.isSelected());
    }

    public void setData(RandoriProjectModel data)
    {
        //webRoot.setText(data.getWebRoot());
        //port.setText(Integer.toString(data.getPort()));
        basePath.setText(data.getBasePath());
        libraryPath.setText(data.getLibraryPath());
        //exportAsFiles.setSelected(data.isClassesAsFile());
    }

    @Override
    protected void resetEditorFrom(RandoriProjectConfigurationForm model)
    {
        // apply the saved model to the components
        //        basePath.setText(model.getBasePath());
        //        libraryPath.setText(model.getLibraryPath());
        //        exportAsFiles.setSelected(model.isClassesAsFile());
    }

    @Override
    protected void applyEditorTo(RandoriProjectConfigurationForm s)
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
        //if (isOpposite(exportAsFiles, data.isClassesAsFile()))
        //    return true;
        return isModified(basePath, data.getBasePath())
                || isModified(libraryPath, data.getLibraryPath());

    }

    @SuppressWarnings("unused")
    private boolean isModified(JTextField component, int value)
    {
        return component.getText() != null ? !component.getText().equals(value)
                : false;
    }

    @SuppressWarnings("unused")
    private boolean isOpposite(JCheckBox component, boolean selected)
    {
        return component.isSelected() != selected;
    }

    private boolean isModified(JTextField component, String value)
    {
        return component.getText() != null ? !component.getText().equals(value)
                : value != null;
    }
}
