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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.stubs.Stub;
import junit.framework.Assert;

import java.io.InputStream;

/**
 * @author Frédéric THOMAS Date: 16/05/13 Time: 19:03
 */
public class RblFileDecompilerTest extends CompiledTestCase
{
    private static final String SIMPLE_RBL_PATH = "simple.rbl";
    private static final String COMPLEX_RBL_PATH = "complex.rbl";

    private static final String SIMPLE_DECOMPILED_RBL_PATH = "simpleRblDecompiled.txt";
    private static final String COMPLEX_DECOMPILED_RBL_PATH = "complexRblDecompiled.txt";

    public void testShouldExtractSimpleLibraryFromContent()
    {
        VirtualFile rblFile = getTestedRblFile(SIMPLE_RBL_PATH, Complexity.SIMPLE);

        InputStream inputStream = null;
        try
        {
            inputStream = RblFileDecompiler.extractLibraries(rblFile, content);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("RblFileDecompiler.extractLibraries should read input stream");
        }

        Assert.assertNotNull("RblFileDecompiler.extractLibraries should read input stream", inputStream);
    }

    public void testShouldExtractComplexLibraryFromContent()
    {
        VirtualFile rblFile = getTestedRblFile(COMPLEX_RBL_PATH, Complexity.COMPLEX);

        InputStream inputStream = null;
        try
        {
            inputStream = RblFileDecompiler.extractLibraries(rblFile, content);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("RblFileDecompiler.extractLibraries should read input stream");
        }

        Assert.assertNotNull("RblFileDecompiler.extractLibraries should read input stream", inputStream);
    }

    public void testShouldDecompileSimpleRbl()
    {
        final VirtualFile rblFile = getTestedRblFile(SIMPLE_RBL_PATH, Complexity.SIMPLE);
        final VirtualFile decompiledRblFile = getTestedRblFile(SIMPLE_DECOMPILED_RBL_PATH, Complexity.SIMPLE);

        final Project project = myFixture.getProject();
        PsiFileImpl decompiledRblData = (PsiFileImpl) PsiManager.getInstance(project).findFile(decompiledRblFile);
        assert decompiledRblData != null;
        String decompiledRblDataText = decompiledRblData.getText();

        RblFileDecompiler decompiler = new RblFileDecompiler();
        CharSequence decompiled = decompiler.decompile(rblFile);

        Assert.assertNotNull("Simple Rbl file should be decompiled", decompiled);
        Assert.assertEquals("Simple Rbl file should be decompiled correctly", decompiledRblDataText, decompiled);
    }

    public void testShouldDecompileComplexRbl()
    {
        final VirtualFile rblFile = getTestedRblFile(COMPLEX_RBL_PATH, Complexity.COMPLEX);
        final VirtualFile decompiledRblFile = getTestedRblFile(COMPLEX_DECOMPILED_RBL_PATH, Complexity.COMPLEX);

        final Project project = myFixture.getProject();
        PsiFileImpl decompiledRblData = (PsiFileImpl) PsiManager.getInstance(project).findFile(decompiledRblFile);
        assert decompiledRblData != null;
        String decompiledRblDataText = decompiledRblData.getText();

        RblFileDecompiler decompiler = new RblFileDecompiler();
        CharSequence decompiled = decompiler.decompile(rblFile);

        Assert.assertNotNull("Complex Rbl file should be decompiled", decompiled);
        Assert.assertEquals("Simple Rbl file should be decompiled correctly", decompiledRblDataText, decompiled);
    }
}
