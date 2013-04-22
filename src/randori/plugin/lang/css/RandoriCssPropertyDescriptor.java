package randori.plugin.lang.css;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssPropertyDescriptor;
import com.intellij.psi.css.CssPropertyInfo;
import com.intellij.psi.css.CssPropertyValue;
import randori.compiler.access.IASProjectAccess;
import randori.plugin.compiler.RandoriProjectCompiler;
import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.util.ProjectUtils;

import java.util.Collection;
import java.util.Map;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public class RandoriCssPropertyDescriptor implements CssPropertyDescriptor
{

    public RandoriCssPropertyDescriptor(@NotNull String name, @NotNull Map<String, String> superClassLookup)
    {
        _propertyName = name;
        _superClassLookup = superClassLookup;
    }

    final private String _propertyName;
    final private Map<String, String> _superClassLookup;

    @Nullable
    @Override
    public String generateDoc(PsiElement context)
    {
        return _propertyName + " is a Randori specific CSS property";
    }

    @Override
    public boolean isValidValue(@NotNull PsiElement element)
    {
        return true;
    }

    @Override
    public boolean isMultiValueProperty()
    {
        return true;
    }

    @Override
    public boolean isShorthandValue()
    {
        return false;
    }

    @NotNull
    @Override
    public String[] expand(@NotNull CssDeclaration declaration)
    {
        return new String[0];
    }

    @Nullable
    @Override
    public PsiElement[] getShorthandPsiValue(@NotNull CssDeclaration decl,
            @NotNull String propertyName)
    {
        return new PsiElement[0];
    }

    @Override
    public boolean is4ValueProperty()
    {
        return true;
    }

    @Override
    public CssPropertyValue getReferencedPropertyValue(@NotNull String name)
    {
        return null;
    }

    @NotNull
    @Override
    public CssPropertyInfo[] getInfos()
    {
        return new CssPropertyInfo[0];
    }

    @NotNull
    @Override
    public String getPropertyName()
    {
        return _propertyName;
    }

    @Override
    public boolean allowsLengthes()
    {
        return false;
    }

    @Override
    public boolean allowsPercentages()
    {
        return false;
    }

    @Override
    public boolean hasStringValue()
    {
        return true;
    }

    @Override
    public Object[] getAllVariants()
    {
        return new Object[0];
    }

    @Nullable
    @Override
    public String[] getRefNames()
    {
        return new String[0];
    }

    @NotNull
    @Override
    public Object[] getVariants(@NotNull PsiElement contextElement)
    {
        return getSubClassesForPropertyName(_propertyName, contextElement.getProject());
    }

    private Object[] getSubClassesForPropertyName(@NotNull String propertyName, @NotNull Project project)
    {
        String superClass = getSuperClassNameForCSSDeclaration(propertyName);
        if (superClass != null)
        {
            final RandoriProjectComponent projectComponent = ProjectUtils
                    .getProjectComponent(project);
            final RandoriProjectCompiler compiler = projectComponent
                    .getCompiler();
            if (compiler != null)
            {
                IASProjectAccess projectAccess = compiler
                        .getProjectAccess();

                if (projectAccess != null)
                {
                    Collection<IClassDefinition> subClasses = getSubClasses(
                            superClass,
                            projectAccess);
                    if (subClasses != null)
                    {
                        Object[] result = new Object[subClasses.size()];
                        int i = 0;
                        for(IClassDefinition definition : subClasses)
                        {
                            result[i++] = LookupElementBuilder.create("\"" + definition.getQualifiedName() + "\"").withPresentableText(definition.getQualifiedName());
                        }
                        return result;
                    }
                }
            }
        }
        return null;
    }

    private Collection<IClassDefinition> getSubClasses(String superClass,
                                                       IASProjectAccess projectAccess)
    {
        ITypeDefinition typeDefinition = projectAccess.getType(superClass);
        if ((typeDefinition != null)
                && (typeDefinition instanceof IClassDefinition))
        {
            return projectAccess
                    .getSubClasses((IClassDefinition) typeDefinition);
        }
        return null;
    }

    private String getSuperClassNameForCSSDeclaration(String name)
    {
        if (_superClassLookup.containsKey(name))
        {
            return _superClassLookup.get(name);
        }
        return null;
    }

    @Override
    public boolean getInherited()
    {
        return false;
    }

    @NotNull
    @Override
    public String toCannonicalName(@NotNull String propertyName)
    {
        return propertyName;
    }

    @Override
    public CssPropertyValue getValue()
    {
        return null;
    }

    @NotNull
    @Override
    public PsiElement[] getDeclarations(PsiElement context)
    {
        return new PsiElement[0];
    }

    @Override
    public boolean allowsIntegerWithoutSuffix()
    {
        return false;
    }
}
