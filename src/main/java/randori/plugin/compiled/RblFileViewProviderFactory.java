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

import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import randori.plugin.components.RandoriApplicationComponent;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 16:25
 */
public class RblFileViewProviderFactory implements FileViewProviderFactory
{
    @Override
    public FileViewProvider createFileViewProvider(@NotNull VirtualFile file, Language language,
            @NotNull PsiManager manager, boolean physical)
    {
        return new SwfFileViewProvider(manager, file, physical);
    }

    static class CompiledJSFile extends JSFileImpl implements PsiCompiledFile
    {
        public CompiledJSFile(FileViewProvider fileViewProvider)
        {
            super(fileViewProvider);
        }

        public PsiElement getMirror()
        {
            return this;
        }

        public boolean isWritable()
        {
            return true;
        }

        public PsiFile getDecompiledPsiFile()
        {
            return this;
        }
    }

    private static class SwfFileViewProvider extends SingleRootFileViewProvider
    {
        public SwfFileViewProvider(PsiManager manager, VirtualFile file, boolean physical)
        {
            super(manager, file, physical);
        }

        protected PsiFile createFile(@NotNull Project project, @NotNull VirtualFile vFile, @NotNull FileType fileType)
        {
            return new RblFileViewProviderFactory.CompiledJSFile(this);
        }

        @NotNull
        public Language getBaseLanguage()
        {
            return RandoriApplicationComponent.DECOMPILED_RBL;
        }

        @NotNull
        public SingleRootFileViewProvider createCopy(@NotNull VirtualFile copy)
        {
            return new SwfFileViewProvider(getManager(), copy, false);
        }
    }
}
