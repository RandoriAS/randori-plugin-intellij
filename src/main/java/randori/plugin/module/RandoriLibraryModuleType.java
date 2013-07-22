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
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NonNls;
import randori.plugin.ui.icons.RandoriIcons;

import javax.swing.*;

/**
 * @author Frédéric THOMAS
 */
public class RandoriLibraryModuleType extends ModuleType<RandoriLibraryModuleBuilder>
{

    @NonNls
    private static final String MODULE_ID = "RANDORI_LIBRARY_MODULE";

    public RandoriLibraryModuleType()
    {
        super(MODULE_ID);
    }

    public static RandoriLibraryModuleType getInstance()
    {
        return (RandoriLibraryModuleType) ModuleTypeManager.getInstance().findByID(MODULE_ID);
    }

    public static boolean isOfType(Module module)
    {
        return get(module) instanceof RandoriLibraryModuleType;
    }

    // create New Project
    @Override
    public RandoriLibraryModuleBuilder createModuleBuilder()
    {
        return new RandoriLibraryModuleBuilder();
    }

    @Override
    public String getName()
    {
        return "Randori Library Module";
    }

    @Override
    public String getDescription()
    {
        return "This module type is used to create Randori AS3 library.";
    }

    @Override
    public Icon getBigIcon()
    {
        return RandoriIcons.RandoriLibModule24;
    }

    @Override
    public Icon getNodeIcon(boolean isOpened)
    {
        return RandoriIcons.RandoriLibModule16;
    }
}
