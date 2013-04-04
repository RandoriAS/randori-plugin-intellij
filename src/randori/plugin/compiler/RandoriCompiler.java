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

package randori.plugin.compiler;

import java.util.*;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.TranslatingCompiler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Chunk;
import randori.plugin.AsFileType;
import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.utils.ProjectUtils;

/**
 * IDEA Compiler class for calling the internal compiler API.
 * 
 * @author Frédéric THOMAS
 */
public class RandoriCompiler implements TranslatingCompiler
{

    private static final Logger LOG = Logger
            .getInstance("#randori.compiler.RandoriCompiler");
    protected final Project myProject;
    private FileDocumentManager m_documentManager;
    private RandoriProjectComponent m_projectComponent;
    private List<VirtualFile> m_unsavedFiles;

    public RandoriCompiler(Project project)
    {
        myProject = project;

        m_projectComponent = ProjectUtils.getProjectComponent(project);
        m_documentManager = ApplicationManager.getApplication().getComponent(
                FileDocumentManager.class);
    }

    @Override
    public boolean isCompilableFile(VirtualFile file, CompileContext context)
    {
        FileType fileType = file.getFileType();
        FileType asFileType = AsFileType.AS_FILE_TYPE;
        boolean b = file.getPath().endsWith('.' + AsFileType.DEFAULT_EXTENSION);
        return b;
    }

    @Override
    public void compile(CompileContext context, Chunk<Module> moduleChunk,
            VirtualFile[] files, OutputSink sink)
    {
        context.getProgressIndicator().checkCanceled();
        context.getProgressIndicator().setText("Starting Randori compiler...");

        if (context.isMake() && m_unsavedFiles != null
                && m_unsavedFiles.size() > 0)
        {
            m_projectComponent.build((m_unsavedFiles
                    .toArray(new VirtualFile[m_unsavedFiles.size()])), false,
                    true);
            m_unsavedFiles = null;
        }
        else
        {
            m_projectComponent.build(null, !context.isMake(), true);
        }
    }

    @NotNull
    @Override
    public String getDescription()
    {
        return "Randori Compiler";
    }

    @Override
    public boolean validateConfiguration(CompileScope scope)
    {
        boolean isConfigurationValidated = m_projectComponent
                .validateConfiguration(scope);

        if (isConfigurationValidated)
        {
            final Document[] unsavedDocuments = m_documentManager
                    .getUnsavedDocuments();

            if (unsavedDocuments.length > 0)
            {
                m_unsavedFiles = new ArrayList<VirtualFile>();

                for (Document unsavedDocument : unsavedDocuments)
                {
                    VirtualFile file = m_documentManager
                            .getFile(unsavedDocument);
                    if (file.getPath().endsWith(
                            '.' + AsFileType.DEFAULT_EXTENSION))
                    {
                        m_unsavedFiles.add(file);
                    }
                }
            }
        }

        return isConfigurationValidated;
    }
}
