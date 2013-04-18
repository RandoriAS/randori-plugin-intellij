/***
 * Copyright 2013 Teoti Graphix, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * @author Michael Schmalle <mschmalle@teotigraphix.com>
 */

package randori.plugin.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import randori.plugin.components.RandoriProjectComponent;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Michael Schmalle
 */
@SuppressWarnings({ "rawtypes" })
public class RandoriRunConfiguration extends
        ModuleBasedConfiguration<RandoriApplicationModuleBasedConfiguration>
{
    public boolean useExplicitWebroot = false;
    public String explicitWebroot = "";
    public String indexRoot;
    private ExecutionEnvironment myEnvironment;

    public RandoriRunConfiguration(String name, Project project,
            RandoriRunnerConfigurationType configurationType)
    {
        super(name, new RandoriApplicationModuleBasedConfiguration(project),
                configurationType.getConfigurationFactories()[0]);
    }

    @Override
    public Collection<Module> getValidModules()
    {
        Module[] modules = ModuleManager.getInstance(getProject()).getModules();
        return Arrays.asList(modules);
    }

    @Override
    protected ModuleBasedConfiguration createInstance()
    {
        return new RandoriRunConfiguration(getName(), getProject(),
                RandoriRunnerConfigurationType.getInstance());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException
    {
        super.checkConfiguration();

        if (indexRoot == null)
            throw new RuntimeConfigurationException("An index is required");

        if (getModule() == null)
            throw new RuntimeConfigurationException("A module is required");
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
    {
        return new RandoriRunConfigurationEditor(getProject());
    }

    @Override
    public void readExternal(final Element element) throws InvalidDataException
    {
        PathMacroManager.getInstance(getProject()).expandPaths(element);
        XmlSerializer.deserializeInto(this, element);
        readModule(element);
    }

    @Override
    public void writeExternal(final Element element)
            throws WriteExternalException
    {
        super.writeExternal(element);
        XmlSerializer.serializeInto(this, element);
        writeModule(element);
        PathMacroManager.getInstance(getProject()).collapsePathsRecursively(
                element);
    }

    @Override
    public RunProfileState getState(@NotNull Executor executor,
            @NotNull ExecutionEnvironment environment)
            throws ExecutionException
    {
        myEnvironment = environment;

        RunProfileState state = new ApplicationServerRunState();
        return state;
    }

    protected Module getModule()
    {
        return getConfigurationModule().getModule();
    }

    @Override
    public boolean isGeneratedName()
    {
        return true;
    }

    @Override
    public String suggestedName()
    {
        String name = getName();
        int pos = name.lastIndexOf('.');
        if (pos == -1)
            return name;

        return name.substring(pos + 1);
    }

    //--------------------------------------------------------------------------

    public class ApplicationServerRunState implements RunProfileState
    {
        @Override
        public ExecutionResult execute(Executor executor, ProgramRunner runner)
                throws ExecutionException
        {
            RandoriProjectComponent component = getProject().getComponent(
                    RandoriProjectComponent.class);
            component.run(RandoriRunConfiguration.this);
            return null;
        }

        @Override
        public ConfigurationPerRunnerSettings getConfigurationSettings()
        {
            return myEnvironment.getConfigurationSettings();
        }

        @Override
        public RunnerSettings getRunnerSettings()
        {
            return myEnvironment.getRunnerSettings();
        }
    }

}
