package randori.plugin.lang.css;

import java.awt.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.CssPropertyDescriptor;
import com.intellij.psi.css.CssSimpleSelector;
import com.intellij.psi.css.CssTerm;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public class RandoriCssElementDescriptorProvider extends
        CssElementDescriptorProvider
{

    public RandoriCssElementDescriptorProvider()
    {
    }

    private CssElementDescriptorProvider _base;

    private CssElementDescriptorProvider getBaseProvider()
    {
        if (_base == null)
        {
            CssElementDescriptorProvider[] providers = CssElementDescriptorProvider.EP_NAME.getExtensions();
            _base = providers.length > 0 ? providers[providers.length - 1] : null;
        }
        return _base;
    }

    @Override
    public boolean isMyContext(@Nullable PsiElement context)
    {
        return true;
    }

    @Nullable
    @Override
    public CssPropertyDescriptor getPropertyDescriptor(
            @NotNull String propertyName, @Nullable PsiElement context)
    {
        return null; // To change body of implemented methods use File |
                     // Settings | File Templates.
    }

    @Override
    public boolean isPossibleSelector(@NotNull String selector,
            @NotNull PsiElement context)
    {
        return getBaseProvider().isPossibleSelector(selector, context);
    }

    @Override
    public boolean isPossiblePseudoClass(@NotNull String pseudoClass,
            @NotNull PsiElement context)
    {
        return getBaseProvider().isPossiblePseudoClass(pseudoClass, context);
    }

    @NotNull
    @Override
    public String[] getPossiblePseudoClasses(@NotNull PsiElement context)
    {
        return getBaseProvider().getPossiblePseudoClasses(context);
    }

    @NotNull
    @Override
    public String[] getPropertyNames(@Nullable PsiElement context)
    {
        return new String[0];
    }

    @NotNull
    @Override
    public String[] getSimpleSelectors(@Nullable PsiElement context)
    {
        return getBaseProvider().getSimpleSelectors(context);
    }

    @NotNull
    @Override
    public PsiElement[] getDeclarationsForSimpleSelector(
            @NotNull CssSimpleSelector selector)
    {
        return getBaseProvider().getDeclarationsForSimpleSelector(selector);
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForSelector(@NotNull String text,
            @Nullable PsiElement context)
    {
        return getBaseProvider().getDocumentationElementForSelector(text, context);
    }

    @Override
    public boolean providesClassicCss()
    {
        return getBaseProvider().providesClassicCss();
    }

    @Nullable
    @Override
    public String generateDocForSelector(@NotNull String s,
            @NotNull PsiElement context)
    {
        return getBaseProvider().generateDocForSelector(s, context);
    }

    @NotNull
    @Override
    public PsiReference getStyleReference(PsiElement element, int start,
            int end, boolean caseSensitive)
    {
        return getBaseProvider().getStyleReference(element, start, end, caseSensitive);
    }

    @Nullable
    @Override
    public Color getColorByValue(@NotNull String value)
    {
        return getBaseProvider().getColorByValue(value);
    }

    @Override
    public boolean isColorTerm(@NotNull CssTerm term)
    {
        return getBaseProvider().isColorTerm(term);
    }
}
