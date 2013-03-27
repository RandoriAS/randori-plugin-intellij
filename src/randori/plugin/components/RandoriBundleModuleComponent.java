package randori.plugin.components;

import javax.swing.*;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import randori.plugin.forms.RandoriBundleModuleConfigurationForm;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

/**
 * Autopackage module adds new bundle module tab and holds plugin configuration.
 */
@State(name = RandoriModuleComponent.COMPONENT_NAME, storages = { @Storage(id = "randoribundlecompiler", file = "$MODULE_FILE$") })
public class RandoriBundleModuleComponent  implements ModuleComponent, Configurable,
        PersistentStateComponent<RandoriBundleModuleComponent> {

    public static final String COMPONENT_NAME = "RandoriBundleBuilder";
    private RandoriBundleModuleConfigurationForm form;

    @Nls
    @Override
    public String getDisplayName() {
        return "Randori Bundle Compiler";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void projectOpened() {

    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void moduleAdded() {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Nullable
    @Override
    public RandoriBundleModuleComponent getState() {
        return this;
    }

    @Override
    public void loadState(RandoriBundleModuleComponent state) {
        setClassesAsFile(state.isClassesAsFile());
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (form == null)
        {
            form = new RandoriBundleModuleConfigurationForm();
        }
        return form.getComponent();
    }

    @Override
    public boolean isModified() {
        return form.isModified(this);
    }

    @Override
    public void apply() throws ConfigurationException {
        if (form != null)
        {
            form.getData(this);
        }
    }

    @Override
    public void reset() {
        if (form != null)
        {
            form.setData(this);
        }
    }

    @Override
    public void disposeUIResources() {

    }

    private boolean classesAsFile;

    public boolean isClassesAsFile()
    {
        return classesAsFile;
    }

    public void setClassesAsFile(boolean classesAsFile)
    {
        this.classesAsFile = classesAsFile;
    }

}
