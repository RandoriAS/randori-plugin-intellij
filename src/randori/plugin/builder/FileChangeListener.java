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

import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.util.ProjectUtils;
import randori.plugin.util.VFileUtils;

import java.util.List;

/**
 * @author Michael Schmalle
 * @author Frédéric THOMAS
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
        if (project == ProjectUtils.getProject())
        {
            List<String> projectSourcePaths = ProjectUtils
                    .getAllProjectSourcePaths(project);

            for (String sourcePath : projectSourcePaths)
            {
                if (VFileUtils.extensionEquals(file.getPath(), "as")
                        && file.getPath().startsWith(sourcePath))
                {
                    return true;
                }
            }
        }

        return false;
    }

    protected void validateAndParse(final VirtualFile file, final boolean add,
            final boolean remove)
    {

        if (isValidFile(file))
        {
            List<VirtualFile> modifiedFiles = projectComponent
                    .getModifiedFiles();

            if (add && !modifiedFiles.contains(file))
                modifiedFiles.add(file);

            if (remove && modifiedFiles.contains(file))
                modifiedFiles.remove(file);

            final ReadonlyStatusHandler.OperationStatus operationStatus = ReadonlyStatusHandler
                    .getInstance(project).ensureFilesWritable(modifiedFiles);
            if (!operationStatus.hasReadonlyFiles())
            {
                new OptimizeImportsProcessor(project,
                        ReformatCodeAction.convertToPsiFiles(
                                modifiedFiles
                                        .toArray(new VirtualFile[modifiedFiles
                                                .size()]), project), null)
                        .run();
            }

            /*projectComponent.parse(false);
            modifiedFiles.removeAll(modifiedFiles);*/
        }
    }

    @Override
    public void propertyChanged(VirtualFilePropertyEvent event)
    {
        VirtualFile file = event.getFile();

        if (event.getPropertyName() == VirtualFile.PROP_NAME
                && !projectComponent.getModifiedFiles().contains(file))
        {
            validateAndParse(file, true, false);
        }
    }

    @Override
    public void contentsChanged(VirtualFileEvent event)
    {
        validateAndParse(event.getFile(), true, false);
    }

    @Override
    public void fileCreated(VirtualFileEvent event)
    {
        validateAndParse(event.getFile(), true, false);
    }

    @Override
    public void fileDeleted(VirtualFileEvent event)
    {
        validateAndParse(event.getFile(), false, true);
    }

    @Override
    public void fileMoved(VirtualFileMoveEvent event)
    {
        validateAndParse(event.getFile(), false, false);
    }

    @Override
    public void fileCopied(VirtualFileCopyEvent event)
    {
        validateAndParse(event.getFile(), true, false);
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
