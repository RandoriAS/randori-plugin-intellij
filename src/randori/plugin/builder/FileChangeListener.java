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

package randori.plugin.builder;

import com.intellij.openapi.vfs.*;
import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.utils.ProjectUtils;

/**
 * @author Michael Schmalle
 */
public class FileChangeListener implements VirtualFileListener
{
    @Override
    public void propertyChanged(VirtualFilePropertyEvent event)
    {
    }

    @Override
    public void contentsChanged(VirtualFileEvent event)
    {
        VirtualFile file = event.getFile();
        if (file != null && file.getExtension() != null
                && file.getExtension().equals("as"))
        {
            System.out.println(event.getFileName());
            RandoriProjectComponent component = ProjectUtils
                    .getProjectComponent(ProjectUtils.getProject());

            component.parse(false);
        }
    }

    @Override
    public void fileCreated(VirtualFileEvent event)
    {
    }

    @Override
    public void fileDeleted(VirtualFileEvent event)
    {
    }

    @Override
    public void fileMoved(VirtualFileMoveEvent event)
    {
    }

    @Override
    public void fileCopied(VirtualFileCopyEvent event)
    {
    }

    @Override
    public void beforePropertyChange(VirtualFilePropertyEvent event)
    {
    }

    @Override
    public void beforeContentsChange(VirtualFileEvent event)
    {
    }

    @Override
    public void beforeFileDeletion(VirtualFileEvent event)
    {
    }

    @Override
    public void beforeFileMovement(VirtualFileMoveEvent event)
    {
    }
}
