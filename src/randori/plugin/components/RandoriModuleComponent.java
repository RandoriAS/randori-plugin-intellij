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

package randori.plugin.components;

import javax.swing.*;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import randori.plugin.forms.RandoriModuleConfigurationForm;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

/**
 * Autopackage module adds new module tab and holds plugin configuration.
 */
@State(name = RandoriModuleComponent.COMPONENT_NAME, storages = { @Storage(id = "randoricompiler", file = "$MODULE_FILE$") })
/**
 * @author Michael Schmalle
 */
public class RandoriModuleComponent implements ModuleComponent, Configurable,
        PersistentStateComponent<RandoriModuleComponent>
{

    public static final String COMPONENT_NAME = "RandoriBuilder";

    private RandoriModuleConfigurationForm form;

    @Nls
    @Override
    public String getDisplayName()
    {
        return "Randori Compiler";
    }

    //@Override
    public Icon getIcon()
    {
        return null;
    }

    @Override
    public String getHelpTopic()
    {
        return null;
    }

    @Override
    public void projectOpened()
    {
    }

    @Override
    public void projectClosed()
    {
    }

    @Override
    public void moduleAdded()
    {
    }

    @Override
    public void initComponent()
    {
    }

    @Override
    public void disposeComponent()
    {
    }

    @NotNull
    @Override
    public String getComponentName()
    {
        return COMPONENT_NAME;
    }

    @Override
    public RandoriModuleComponent getState()
    {
        return this;
    }

    @Override
    public void loadState(RandoriModuleComponent state)
    {
        setBasePath(state.getBasePath());
        setLibraryPath(state.getLibraryPath());
        setClassesAsFile(state.isClassesAsFile());
    }

    @Override
    public JComponent createComponent()
    {
        if (form == null)
        {
            form = new RandoriModuleConfigurationForm();
        }
        return form.getComponent();
    }

    @Override
    public boolean isModified()
    {
        return form.isModified(this);
    }

    @Override
    public void apply() throws ConfigurationException
    {
        if (form != null)
        {
            form.getData(this);
        }
    }

    @Override
    public void reset()
    {
        if (form != null)
        {
            form.setData(this);
        }
    }

    @Override
    public void disposeUIResources()
    {
    }

    //---------------------------------------------------------------

    private String basePath;

    private String libraryPath;

    private boolean classesAsFile;

    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public String getLibraryPath()
    {
        return libraryPath;
    }

    public void setLibraryPath(String libraryPath)
    {
        this.libraryPath = libraryPath;
    }

    public boolean isClassesAsFile()
    {
        return classesAsFile;
    }

    public void setClassesAsFile(boolean classesAsFile)
    {
        this.classesAsFile = classesAsFile;
    }
}
