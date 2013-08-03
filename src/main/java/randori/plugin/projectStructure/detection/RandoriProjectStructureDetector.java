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

package randori.plugin.projectStructure.detection;

import com.intellij.ide.util.DelegatingProgressIndicator;
import com.intellij.ide.util.importProject.LibrariesDetectionStep;
import com.intellij.ide.util.importProject.ModulesDetectionStep;
import com.intellij.ide.util.importProject.ProjectDescriptor;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.ide.util.projectWizard.importSources.util.CommonSourceRootDetectionUtil;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.NullableFunction;
import com.intellij.util.StringBuilderSpinAllocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Frédéric THOMAS Date: 19/04/13 Time: 18:58
 */
public class RandoriProjectStructureDetector extends ProjectStructureDetector
{

    public static final NullableFunction<CharSequence, String> PACKAGE_NAME_FETCHER = new NullableFunction<CharSequence, String>() {
        @Nullable
        @Override
        public String fun(CharSequence charSequence)
        {
            Lexer lexer = LanguageParserDefinitions.INSTANCE.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4)
                    .createLexer(null);
            lexer.start(charSequence);
            return RandoriProjectStructureDetector.readPackageName(charSequence, lexer);
        }
    };

    private static final TokenSet WHITESPACE_AND_COMMENTS = TokenSet.create(JSTokenTypes.WHITE_SPACE,
            JSTokenTypes.DOC_COMMENT, JSTokenTypes.C_STYLE_COMMENT, JSTokenTypes.END_OF_LINE_COMMENT);

    public static boolean isRandoriSourceFile(File file)
    {
        String extension = FileUtilRt.getExtension(file.getName());
        return JavaScriptSupportLoader.ECMA_SCRIPT_L4.equals(getRandoriLanguageDialect(extension));
    }

    @Nullable
    private static JSLanguageDialect getRandoriLanguageDialect(String extension)
    {
        extension = extension.toLowerCase();
        return ActionScriptFileType.INSTANCE.getDefaultExtension().equals(extension) ? JavaScriptSupportLoader.ECMA_SCRIPT_L4 : null;

    }

    @NotNull
    @Override
    public DirectoryProcessingResult detectRoots(@NotNull File dir, @NotNull File[] children, @NotNull File base,
            @NotNull List<DetectedProjectRoot> result)
    {
        for (File child : children)
        {
            if (child.isFile())
            {
                if (isRandoriSourceFile(child))
                {
                    Pair<File, String> root = CommonSourceRootDetectionUtil.IO_FILE
                            .suggestRootForFileWithPackageStatement(child, base, PACKAGE_NAME_FETCHER, false);
                    if (root != null)
                    {
                        result.add(new RandoriWebModuleSourceRoot(root.getFirst()));
                        result.add(new RandoriLibraryModuleSourceRoot(root.getFirst()));
                        return DirectoryProcessingResult.skipChildrenAndParentsUpTo(root.getFirst());
                    }
                    else
                    {
                        return DirectoryProcessingResult.SKIP_CHILDREN;
                    }
                }
            }
        }
        return DirectoryProcessingResult.PROCESS_CHILDREN;
    }

    @Override
    public List<ModuleWizardStep> createWizardSteps(ProjectFromSourcesBuilder builder,
            ProjectDescriptor projectDescriptor, Icon stepIcon)
    {
        if (builder.getContext().getProject() != null)
            builder.getContext().setProjectJdk(
                    ProjectRootManager.getInstance(builder.getContext().getProject()).getProjectSdk());

        RandoriModuleInsight moduleInsight = new RandoriModuleInsight(new DelegatingProgressIndicator(),
                builder.getExistingModuleNames(), builder.getExistingProjectLibraryNames());

        List<ModuleWizardStep> steps = new ArrayList<ModuleWizardStep>();
        steps.add(new LibrariesDetectionStep(builder, projectDescriptor, moduleInsight, stepIcon,
                "reference.dialogs.new.project.fromCode.page1"));

        steps.add(new ModulesDetectionStep(this, builder, projectDescriptor, moduleInsight, stepIcon,
                "reference.dialogs.new.project.fromCode.page2"));

        steps.add(new RandoriSdkStep(builder.getContext()));

        return steps;
    }

    @Nullable
    public static String readPackageName(CharSequence charSequence, Lexer lexer)
    {
        skipWhiteSpaceAndComments(lexer);
        if (!JSTokenTypes.PACKAGE_KEYWORD.equals(lexer.getTokenType()))
        {
            return null;
        }
        lexer.advance();
        skipWhiteSpaceAndComments(lexer);

        return readQualifiedName(charSequence, lexer, false);
    }

    @Nullable
    static String readQualifiedName(CharSequence charSequence, Lexer lexer, boolean allowStar)
    {
        StringBuilder buffer = StringBuilderSpinAllocator.alloc();
        try
        {
            while ((lexer.getTokenType() == JSTokenTypes.IDENTIFIER)
                    || ((allowStar) && (lexer.getTokenType() != JSTokenTypes.MULT)))
            {
                buffer.append(charSequence, lexer.getTokenStart(), lexer.getTokenEnd());
                if (lexer.getTokenType() == JSTokenTypes.MULT)
                    break;
                lexer.advance();
                if (lexer.getTokenType() != JSTokenTypes.DOT)
                    break;
                buffer.append('.');
                lexer.advance();
            }

            String packageName = buffer.toString();
            if (StringUtil.endsWithChar(packageName, '.'))
                return null;
            return packageName;
        }
        finally
        {
            StringBuilderSpinAllocator.dispose(buffer);
        }
    }

    public static void skipWhiteSpaceAndComments(Lexer lexer)
    {
        while (WHITESPACE_AND_COMMENTS.contains(lexer.getTokenType()))
            lexer.advance();
    }
}
