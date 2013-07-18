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

import com.intellij.lang.javascript.flex.importer.FlexImporter;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.stubs.BinaryFileStubBuilder;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.stubs.Stub;
import org.jetbrains.annotations.Nullable;
import randori.plugin.components.RandoriApplicationComponent;

import java.io.InputStream;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 16:16
 */
public class RblFileStubBuilder implements BinaryFileStubBuilder
{
    private static final Logger LOG = Logger.getInstance(RblFileStubBuilder.class);

    @Override
    public boolean acceptsFile(VirtualFile file)
    {
        return (file.getFileType().equals(RandoriApplicationComponent.RBL_FILE_TYPE));
    }

    @Nullable
    @Override
    public Stub buildStubTree(VirtualFile file, byte[] content, Project project)
    {
        return buildFileStub(file, content);
    }

    @Override
    public int getStubVersion()
    {
        return JSFileElementType.getVersion() + 1;
    }

    private static PsiFileStub buildFileStub(final VirtualFile file, final byte[] content)
    {
        final PsiFileStubImpl stub = new PsiFileStubImpl(null);

        try
        {
            InputStream librariesInputStream = RblFileDecompiler.extractLibraries(file, content);

            if (librariesInputStream != null)
                FlexImporter.buildStubsInterfaceFromStream(librariesInputStream, stub);
        }
        catch (Exception e)
        {
            LOG.warn(file.getPath(), e);
        }

        return stub;
    }
}
