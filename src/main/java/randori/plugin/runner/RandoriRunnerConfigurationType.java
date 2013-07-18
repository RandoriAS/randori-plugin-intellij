/*
 * Copyright 2013 original Randori IntelliJ Plugin authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package randori.plugin.runner;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import randori.plugin.ui.icons.RandoriIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Michael Schmalle
 */
public class RandoriRunnerConfigurationType implements ConfigurationType
{

    public static final String TYPE = "#randori.plugin.runner.RandoriRunnerConfigurationType";
    public static final String DISPLAY_NAME = "Randori Runner";

    private final RandoriFactory myConfigurationFactory;

    public static RandoriRunnerConfigurationType getInstance() {
        return Extensions.findExtension(CONFIGURATION_TYPE_EP, RandoriRunnerConfigurationType.class);
    }

    public RandoriRunnerConfigurationType()
    {
        myConfigurationFactory = new RandoriFactory(this);
    }

    @Override
    public String getDisplayName()
    {
        return DISPLAY_NAME;
    }

    @Override
    public String getConfigurationTypeDescription()
    {
        return "Runs a randori application using the Jetty server";
    }

    @Override
    public Icon getIcon()
    {
        return RandoriIcons.Randori16;
    }

    @Override
    @NotNull
    public String getId()
    {
        return TYPE;
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories()
    {
        return new ConfigurationFactory[] { myConfigurationFactory };
    }

    public static class RandoriFactory extends ConfigurationFactory
    {
        public RandoriFactory(ConfigurationType type)
        {
            super(type);
        }

        @Override
        public RunConfiguration createTemplateConfiguration(Project project)
        {
            return new RandoriRunConfiguration("Randori Application", project, getInstance());
        }
    }
}
