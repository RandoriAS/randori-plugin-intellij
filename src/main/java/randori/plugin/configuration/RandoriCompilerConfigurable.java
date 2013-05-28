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
