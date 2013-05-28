package randori.plugin.compiled;

import com.google.common.io.Files;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.fs.archive.zip.ZipDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import junit.framework.Assert;
import randori.plugin.RandoriLightCodeInsightFixtureTestCase;

import java.io.File;
import java.io.IOException;

/**
 * @author Frédéric THOMAS Date: 17/05/13 Time: 14:52
 */
abstract class CompiledTestCase extends RandoriLightCodeInsightFixtureTestCase
{
    static
    {
        TConfig.get().setArchiveDetector(new TArchiveDetector("rbl|swc", new ZipDriver(IOPoolLocator.SINGLETON)));
    }

    byte[] content;

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        content = null;
    }

    VirtualFile getTestedRblFile(String rblPath, Complexity complexity)
    {
        File simpleRblFile = new File(getTestDataPath() + rblPath);
        VirtualFile rblFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(simpleRblFile);

        try
        {
            content = Files.toByteArray(simpleRblFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Assert.fail(complexity.getName() + " Rbl should have content");
        }

        Assert.assertNotNull(complexity.getName() + " Rbl should exists", rblFile);
        Assert.assertNotNull(complexity.getName() + " Rbl should have content", content);
        Assert.assertTrue("Virtual " + complexity.getName() + " Rbl should exists", rblFile.exists());

        return rblFile;
    }

    protected static enum Complexity {
        SIMPLE, COMPLEX;
        public String getName() {
            return this == SIMPLE ? "Simple" : "Complex";
        }
    }
}
