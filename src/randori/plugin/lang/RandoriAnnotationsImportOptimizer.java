package randori.plugin.lang;

import com.intellij.lang.javascript.psi.ecmal4.impl.JSImportStatementImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ImportOptimizer;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.psi.PsiFile;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public class RandoriAnnotationsImportOptimizer implements ImportOptimizer
{
    @Override
    public boolean supports(PsiFile psiFile)
    {
        return true;//psiFile instanceof JSFile;
    }

    @NotNull
    @Override
    public Runnable processFile(PsiFile psiFile)
    {
        psiFile.accept(new PsiRecursiveElementVisitor()
        {
            @Override
            public void visitElement(PsiElement element)
            {
                if (element instanceof JSImportStatementImpl)
                {
                    JSImportStatementImpl importStatement = (JSImportStatementImpl)element;
                    String text = importStatement.getImportText();
                    text = null;
                }
            }
        });
        return null;
    }
}
