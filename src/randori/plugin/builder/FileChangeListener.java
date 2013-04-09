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

import java.util.List;

import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.util.ProjectUtils;
import randori.plugin.util.VFileUtils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;

/**
 * @author Michael Schmalle
 */
public class FileChangeListener implements VirtualFileListener
{

    private final Project project;
    private final RandoriProjectComponent projectComponent;

    public FileChangeListener(Project project)
    {
        this.project = project;
        projectComponent = project.getComponent(RandoriProjectComponent.class);
    }

    protected boolean isValidFile(VirtualFile file)
    {

        if (project == ProjectUtils.getProject()
                && VFileUtils.extensionEquals(file.getPath(), "as"))
            return true;

        return false;
    }

    @Override
    public void propertyChanged(VirtualFilePropertyEvent event)
    {
        VirtualFile file = event.getFile();

        if (isValidFile(file)
                && event.getPropertyName() == VirtualFile.PROP_NAME
                && !projectComponent.getModifiedFiles().contains(file))
        {
            projectComponent.getModifiedFiles().add(file);
        }
    }

    @Override
    public void contentsChanged(VirtualFileEvent event)
    {

        VirtualFile file = event.getFile();

        if (isValidFile(file))
        {
            List<VirtualFile> modifiedFiles = projectComponent
                    .getModifiedFiles();

            if (!modifiedFiles.contains(file))
                modifiedFiles.add(file);

            Document[] unsavedDocuments = (Document[]) FileDocumentManager
                    .getInstance().getUnsavedDocuments();

            if (unsavedDocuments.length == 1)
            {
                projectComponent.parse(false);
                modifiedFiles.removeAll(modifiedFiles);
            }
        }
    }

    @Override
    public void fileCreated(VirtualFileEvent event)
    {

        VirtualFile file = event.getFile();

        if (isValidFile(file)
                && !projectComponent.getModifiedFiles().contains(file))
        {
            projectComponent.getModifiedFiles().add(file);
        }
    }

    @Override
    public void fileDeleted(VirtualFileEvent event)
    {
        VirtualFile file = event.getFile();

        if (isValidFile(file)
                && projectComponent.getModifiedFiles().contains(file))
        {
            projectComponent.getModifiedFiles().remove(file);
        }
    }

    @Override
    public void fileMoved(VirtualFileMoveEvent event)
    {
        VirtualFile file = event.getFile();

        if (isValidFile(file)
                && !projectComponent.getModifiedFiles().contains(file))
        {
            projectComponent.getModifiedFiles().add(file);
        }
    }

    @Override
    public void fileCopied(VirtualFileCopyEvent event)
    {
        VirtualFile file = event.getFile();

        if (isValidFile(file)
                && !projectComponent.getModifiedFiles().contains(file))
        {
            projectComponent.getModifiedFiles().add(file);
        }
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
