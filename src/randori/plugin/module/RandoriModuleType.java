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

package randori.plugin.module;

import javax.swing.*;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import icons.RandoriIcons;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.projectRoots.Sdk;

/**
 * @author Michael Schmalle
 */
public class RandoriModuleType extends ModuleType<RandoriModuleBuilder>
{

    @NonNls
    private static final String MODULE_ID = "RANDORI_MODULE";

    public RandoriModuleType()
    {
        super(MODULE_ID);
    }

    public static RandoriModuleType getInstance()
    {
        return (RandoriModuleType) ModuleTypeManager.getInstance().findByID(
                MODULE_ID);
    }

    public static boolean isOfType(Module module)
    {
        return get(module) instanceof RandoriModuleType;
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
        return "Randori Module";
    }

    @Override
    public String getDescription()
    {
        return "This module type is used to create Randori AS3 projects using the Randori JavaScript cross compiler";
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

    @Override
    public boolean isValidSdk(Module module, @Nullable Sdk projectSdk)
    {
        return super.isValidSdk(module, projectSdk);
    }
}
