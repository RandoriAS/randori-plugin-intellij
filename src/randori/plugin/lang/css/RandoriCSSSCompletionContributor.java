package randori.plugin.lang.css;

import static com.intellij.patterns.PlatformPatterns.psiElement;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.util.ProcessingContext;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public class RandoriCSSSCompletionContributor extends CompletionContributor {

    private static final ElementPattern<PsiElement> AFTER_DOUBLE_COLON = psiElement().afterLeaf(":");
    public static final String RANDORI_PREFIX = "-randori";

    public RandoriCSSSCompletionContributor()
    {
        extend(CompletionType.BASIC, AFTER_DOUBLE_COLON, new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters,
                                          ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement position = parameters.getPosition();

                /*result.addElement(new LookupElement() {
                    @NotNull
                    @Override
                    public String getLookupString() {
                        return "com.class.MyBehaviour";
                    }
                });
                result.stopHere();*/

                PsiElement elm = position.getParent();
                CssDeclaration decl = null;
                while(elm != null) {
                    if (elm instanceof CssDeclaration) {
                        decl = (CssDeclaration)elm;
                        break;
                    }
                    elm = elm.getParent();
                }
                if (decl != null)
                {
                    String name = decl.getPropertyName();
                    if (name.startsWith(RANDORI_PREFIX))
                    {
                        result.addElement(new LookupElement() {
                            @NotNull
                            @Override
                            public String getLookupString() {
                                return "com.classes.MyBehaviour";
                            }
                        });
                        result.stopHere();
                    }
                }
            }
        });
    }

}
