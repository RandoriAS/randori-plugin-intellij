package randori.plugin.configuration;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;

/**
 * @author Michael Schmalle
 */
public class RandoriModuleConfigurable extends SettingsEditor<RandoriModuleModel>
{

    private JPanel panel;
    private JCheckBox exportAsFiles;
    private JCheckBox generateRbl;

    public void getData(RandoriModuleModel data)
    {
        data.setExportAsFile(exportAsFiles.isSelected());
        data.setGenerateRbl(generateRbl.isSelected());
    }

    public void setData(RandoriModuleModel data)
    {
        exportAsFiles.setSelected(data.isExportAsFile());
        generateRbl.setSelected(data.isGenerateRbl());
    }

    @Override
    protected void resetEditorFrom(RandoriModuleModel model)
    {
        exportAsFiles.setSelected(model.isExportAsFile());
        generateRbl.setSelected(model.isGenerateRbl());
    }

    @Override
    protected void applyEditorTo(RandoriModuleModel model) throws ConfigurationException
    {
        model.setExportAsFile(exportAsFiles.isSelected());
        model.setGenerateRbl(generateRbl.isSelected());
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

    public boolean isModified(RandoriModuleModel data)
    {
        return exportAsFiles.isSelected() == data.isExportAsFile() && generateRbl.isSelected() == data.isGenerateRbl();
    }
}
