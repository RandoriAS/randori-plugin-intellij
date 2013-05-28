package randori.plugin.lang.css;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.util.ProcessingContext;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.jetbrains.annotations.NotNull;
import randori.compiler.access.IASProjectAccess;
import randori.compiler.internal.projects.RandoriProject;
import randori.plugin.compiler.RandoriCompilerSession;
import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.util.ProjectUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public class RandoriCSSSCompletionContributor extends CompletionContributor
{
    private static final Logger logger = Logger.getInstance(RandoriCSSSCompletionContributor.class);

    private static final ElementPattern<PsiElement> AFTER_DOUBLE_COLON = psiElement().afterLeaf(":");
    private static final String RANDORI_PREFIX = "-randori";
    private static final Map<String, String> superClassLookup;
    static
    {
        Map<String, String> tempMap = new HashMap<String, String>();
        tempMap.put(RANDORI_PREFIX + "-behavior", "randori.behaviors.AbstractBehavior");
        tempMap.put(RANDORI_PREFIX + "-mediator", "randori.behaviors.AbstractMediator");
        tempMap.put(RANDORI_PREFIX + "-context", "guice.GuiceModule");
        superClassLookup = Collections.unmodifiableMap(tempMap);
    }

    public RandoriCSSSCompletionContributor()
    {
        final Project project = ProjectUtils.getProject();

        if (project != null)
        {
            final RandoriProjectComponent projectComponent = ProjectUtils.getProjectComponent(project);

            extend(CompletionType.BASIC, AFTER_DOUBLE_COLON, new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                        @NotNull CompletionResultSet result)
                {

                    if (!ProjectUtils.hasRandoriModuleType(project))
                    {
                        logger.debug("No Randori project active, bailing out");
                        return;
                    }

                    final PsiElement position = parameters.getPosition();
                    final CssDeclaration declaration = getCssDeclaration(position);
                    if (declaration != null)
                    {
                        String name = declaration.getPropertyName();
                        if (isRandoriCSSDeclaration(name))
                        {
                            String superClass = getSuperClassNameForCSSDeclaration(name);
                            if (superClass != null)
                            {
                                RandoriProject compiler = RandoriCompilerSession.getLastCompiler();
                                if (compiler != null)
                                {
                                    IASProjectAccess projectAccess = compiler.getProjectAccess();

                                    if (projectAccess != null)
                                    {
                                        Collection<IClassDefinition> subClasses = getSubClasses(superClass,
                                                projectAccess);
                                        if (subClasses != null)
                                        {
                                            addLookupElements(subClasses, result);
                                        }
                                        result.stopHere();
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    private void addLookupElements(Collection<IClassDefinition> subClasses, CompletionResultSet result)
    {
        for (final IClassDefinition subClass : subClasses)
        {
            result.addElement(new LookupElement() {
                @NotNull
                @Override
                public String getLookupString()
                {
                    return '"' + subClass.getQualifiedName() + '"';
                }

                @NotNull
                @Override
                public void renderElement(LookupElementPresentation presentation)
                {
                    presentation.setItemText(subClass.getQualifiedName());
                }

            });
        }
    }

    private Collection<IClassDefinition> getSubClasses(String superClass, IASProjectAccess projectAccess)
    {
        ITypeDefinition typeDefinition = projectAccess.getType(superClass);
        if ((typeDefinition != null) && (typeDefinition instanceof IClassDefinition))
        {
            return projectAccess.getSubClasses((IClassDefinition) typeDefinition);
        }
        return null;
    }

    private String getSuperClassNameForCSSDeclaration(String name)
    {
        if (superClassLookup.containsKey(name))
        {
            return superClassLookup.get(name);
        }
        logger.error("Unrecognized Randori CSS declaration: " + name);
        return null;
    }

    private boolean isRandoriCSSDeclaration(String name)
    {
        return name.startsWith(RANDORI_PREFIX);
    }

    private CssDeclaration getCssDeclaration(PsiElement position)
    {
        CssDeclaration decl = null;
        PsiElement elm = position.getParent();
        while (elm != null)
        {
            if (elm instanceof CssDeclaration)
            {
                decl = (CssDeclaration) elm;
                break;
            }
            elm = elm.getParent();
        }
        return decl;
    }

}
