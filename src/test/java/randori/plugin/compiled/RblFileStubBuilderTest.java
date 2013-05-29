package randori.plugin.compiled;

import junit.framework.Assert;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.stubs.Stub;

/**
 * @author Frédéric THOMAS Date: 17/05/13 Time: 14:16
 */
public class RblFileStubBuilderTest extends CompiledTestCase
{
    private static final int RBL_STUB_VERSION = -2030841460;

    private static final String SIMPLE_RBL_PATH = "simple.rbl";
    private static final String COMPLEX_RBL_PATH = "complex.rbl";

    private static final String SIMPLE_STUB_PATH = "simpleRblPsiStub.txt";
    private static final String COMPLEX_STUB_PATH = "complexRblPsiStub.txt";

    private RblFileStubBuilder builder;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        builder = new RblFileStubBuilder();
    }

    public void testShouldAcceptsFile()
    {
        final VirtualFile file = getTestedRblFile(SIMPLE_RBL_PATH, Complexity.SIMPLE);
        Assert.assertTrue("Should accept Rbl file", builder.acceptsFile(file));
    }

    public void testShouldNotAcceptsFile()
    {
        final VirtualFile file = getTestedRblFile(SIMPLE_STUB_PATH, Complexity.SIMPLE);
        Assert.assertFalse("Should Not accept no Rbl file", builder.acceptsFile(file));
    }

    public void testBuildSimpleStubTree()
    {
        final VirtualFile rblFile = getTestedRblFile(SIMPLE_RBL_PATH, Complexity.SIMPLE);
        final VirtualFile stubFile = getTestedRblFile(SIMPLE_STUB_PATH, Complexity.SIMPLE);

        final Project project = myFixture.getProject();
        PsiFileImpl stubData = (PsiFileImpl) PsiManager.getInstance(project).findFile(stubFile);

        final Stub stub = builder.buildStubTree(rblFile, content, project);

        assert stub != null;
        assert stubData != null;

        // TODO Test better finding a way to compare both
    }

    public void testBuildComplexStubTree()
    {
        final VirtualFile rblFile = getTestedRblFile(COMPLEX_RBL_PATH, Complexity.COMPLEX);
        final VirtualFile stubFile = getTestedRblFile(COMPLEX_STUB_PATH, Complexity.COMPLEX);

        final Project project = myFixture.getProject();
        PsiFileImpl stubData = (PsiFileImpl) PsiManager.getInstance(project).findFile(stubFile);

        final Stub stub = builder.buildStubTree(rblFile, content, project);

        assert stub != null;
        assert stubData != null;

        // TODO Test better finding a way to compare both
    }

    public void testGetStubVersion()
    {
        final int stubVersion = builder.getStubVersion();
        Assert.assertTrue("The stub version should be equal to RBL_STUB_VERSION", stubVersion == RBL_STUB_VERSION);
    }
}
