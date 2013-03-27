package randori.plugin.components;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import randori.plugin.forms.RandoriBundleProjectConfigurationForm;

import javax.swing.*;

@State(name = RandoriBundleProjectComponent.COMPONENT_NAME, storages = { @Storage(id = "randoribundleproject", file = "$PROJECT_FILE$") })
public class RandoriBundleProjectComponent implements ProjectComponent, Configurable,
        PersistentStateComponent<RandoriProjectModel> {

    public static final String COMPONENT_NAME = "RandoriBundleProject";

    private Project project;

    private RandoriBundleProjectConfigurationForm form;

    private RandoriProjectModel model;

    private final RandoriBundleApplicationComponent applicationComponent;

    public RandoriBundleProjectComponent(Project project,
                                   RandoriBundleApplicationComponent applicationComponent)
    {
        this.project = project;
        this.applicationComponent = applicationComponent;
        this.model = new RandoriProjectModel();
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @Nls
    @Override
    public String getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Nullable
    @Override
    public RandoriProjectModel getState() {
        return model;
    }

    @Override
    public void loadState(RandoriProjectModel model) {
    }

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
    }

    @Override
    public void reset() {
    }

    @Override
    public void disposeUIResources() {
    }
}
