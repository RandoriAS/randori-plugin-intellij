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

package randori.plugin.projectStructure.detection;

import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.DetectedSourceRoot;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Frédéric THOMAS Date: 19/04/13 Time: 19:40
 */
class RandoriWebModuleSourceRoot extends DetectedSourceRoot
{
    public RandoriWebModuleSourceRoot(final File directory)
    {
        super(directory, null);
    }

    @NotNull
    @Override
    public String getRootTypeName()
    {
        return "Randori Web Module";
    }

    @Override
    public DetectedProjectRoot combineWith(@NotNull DetectedProjectRoot root)
    {
        return root instanceof RandoriWebModuleSourceRoot ? this : null;

    }

    @Override
    public boolean canContainRoot(@NotNull DetectedProjectRoot root) {
        return !(root instanceof RandoriWebModuleSourceRoot);
    }
}
