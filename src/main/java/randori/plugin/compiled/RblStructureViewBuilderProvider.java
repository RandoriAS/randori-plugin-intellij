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

package randori.plugin.compiled;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewBuilderProvider;
import com.intellij.lang.javascript.structureView.JSStructureViewBuilderFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 16:33
 */
public class RblStructureViewBuilderProvider implements StructureViewBuilderProvider
{

    private final JSStructureViewBuilderFactory factory = new JSStructureViewBuilderFactory();

    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder(@NotNull FileType fileType, @NotNull VirtualFile file,
            @NotNull Project project)
    {
        return null;
    }
}
