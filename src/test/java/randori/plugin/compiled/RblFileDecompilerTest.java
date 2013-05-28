package randori.plugin.compiled;

import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.Assert;

import java.io.InputStream;

/**
 * @author Frédéric THOMAS Date: 16/05/13 Time: 19:03
 */
public class RblFileDecompilerTest extends CompiledTestCase
{
    private static final String SIMPLE_RBL_PATH = "simple.rbl";
    private static final String COMPLEX_RBL_PATH = "complex.rbl";

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
        VirtualFile rblFile = getTestedRblFile(SIMPLE_RBL_PATH, Complexity.SIMPLE);

        RblFileDecompiler decompiler = new RblFileDecompiler();
        CharSequence decompiled = decompiler.decompile(rblFile);

        Assert.assertNotNull("Simple Rbl file should be decompiled", decompiled);
    }

    public void testShouldDecompileComplexRbl()
    {
        VirtualFile rblFile = getTestedRblFile(COMPLEX_RBL_PATH, Complexity.COMPLEX);

        RblFileDecompiler decompiler = new RblFileDecompiler();
        CharSequence decompiled = decompiler.decompile(rblFile);

        Assert.assertNotNull("Complex Rbl file should be decompiled", decompiled);
    }
}
