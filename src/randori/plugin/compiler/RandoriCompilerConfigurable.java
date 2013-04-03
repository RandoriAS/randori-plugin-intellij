package randori.plugin.compiler;

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

    public static final String COMPONENT_LABEL = "Randori compiler";

    private final RandoriCompilerConfiguration myConfig;

    private JTextField myHeapSize;
    private JPanel myMainPanel;

    public RandoriCompilerConfigurable (Project project) {
        myConfig = RandoriCompilerConfiguration.getInstance(project);
    }
    @Nullable
    @Override
    public JComponent createComponent() {
        return myMainPanel;
    }

    @Override
    public boolean isModified() {
        return !Comparing.equal(myConfig.getHeapSize(), myHeapSize.getText());
    }

    @Override
    public void apply() throws ConfigurationException {
        myConfig.setHeapSize(myHeapSize.getText());
    }

    @Override
    public void reset() {
        myHeapSize.setText(myConfig.getHeapSize());
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
