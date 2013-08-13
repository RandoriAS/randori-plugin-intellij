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

package randori.plugin.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import org.jetbrains.annotations.NonNls;
import randori.plugin.ui.icons.RandoriIcons;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

/**
 * @author Michael Schmalle
 * @author Frédéric THOMAS
 */
public class RandoriWebModuleType extends ModuleType<RandoriModuleBuilder>
{

    @NonNls
    private static final String MODULE_ID = "RANDORI_WEB_MODULE";
    @NonNls
    public static final String RANDORI_GROUP = "Randori";
    @NonNls
    public static final String PRESENTABLE_MODULE_NAME = "Randori Web Module";

    public RandoriWebModuleType()
    {
        super(MODULE_ID);
    }

    public static RandoriWebModuleType getInstance()
    {
        return (RandoriWebModuleType) ModuleTypeManager.getInstance().findByID(MODULE_ID);
    }

    public static boolean isOfType(Module module)
    {
        return get(module) instanceof RandoriWebModuleType;
    }

    // create New Project
    @Override
    public RandoriModuleBuilder createModuleBuilder()
    {
        return new RandoriModuleBuilder();
    }

    @Override
    public String getName()
    {
        return "Randori Web Module";
    }

    @Override
    public String getDescription()
    {
        return "This module type is used to create Randori AS3 Web projects using the Randori JavaScript cross compiler";
    }

    @Override
    public Icon getBigIcon()
    {
        return RandoriIcons.Randori24;
    }

    @Override
    public Icon getNodeIcon(boolean isOpened)
    {
        return RandoriIcons.Randori16;
    }
}
