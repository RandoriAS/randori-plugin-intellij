package randori.plugin.configuration;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Michael Schmalle
 * @author Frédéric THOMAS
 */
public class RandoriProjectConfigurable extends SettingsEditor<RandoriProjectModel>
{

    private JPanel panel;
    private JTextField basePath;
    private JTextField libraryPath;

    public void getData(RandoriProjectModel data)
    {
        data.setBasePath(basePath.getText());
        data.setLibraryPath(libraryPath.getText());
    }

    public void setData(RandoriProjectModel data)
    {
        basePath.setText(data.getBasePath());
        libraryPath.setText(data.getLibraryPath());
    }

    @Override
    protected void resetEditorFrom(RandoriProjectModel model)
    {
        basePath.setText(model.getBasePath());
        libraryPath.setText(model.getLibraryPath());
    }

    @Override
    protected void applyEditorTo(RandoriProjectModel model) throws ConfigurationException
    {
        model.setBasePath(basePath.getText());
        model.setLibraryPath(libraryPath.getText());
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
        return isModified(basePath, data.getBasePath()) || isModified(libraryPath, data.getLibraryPath());

    }

    private boolean isModified(JTextField component, String value)
    {
        return component.getText() != null ? !component.getText().equals(value) : value != null;
    }
}
