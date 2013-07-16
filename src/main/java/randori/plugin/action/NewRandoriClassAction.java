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

package randori.plugin.action;

import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.JavaCreateTemplateInPackageAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import icons.RandoriIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import randori.plugin.util.ProjectUtils;

/**
 * @author Michael Schmalle
 * @author Frédéric THOMAS
 */
public class NewRandoriClassAction extends JavaCreateTemplateInPackageAction<PsiElement>

{
    public NewRandoriClassAction()
    {
        super("Randori File", "Creates a new Randori file", RandoriIcons.Randori16, true);
    }

    @Nullable
    @Override
    protected PsiElement getNavigationElement(@NotNull PsiElement createdElement)
    {
        return createdElement.getNavigationElement();
    }

    @Nullable
    @Override
    protected PsiElement doCreate(PsiDirectory dir, String className, String templateName)
            throws IncorrectOperationException
    {
        final String fileName = className + ".as";
        final PsiFile fromTemplate = RandoriTemplatesFactory.createFromTemplate(dir, className, fileName, templateName);

        if (fromTemplate.getFileType().getDefaultExtension()
                .endsWith(ActionScriptFileType.INSTANCE.getDefaultExtension()))
        {
            CodeStyleManager.getInstance(fromTemplate.getManager()).reformat(fromTemplate);
            return fromTemplate.findElementAt(0);
        }
        final String description = fromTemplate.getFileType().getDescription();
        throw new IncorrectOperationException("*.as files are mapped to ''" + description
                + "''.\\nYou can map them to Randori in Settings | File types");
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder)
    {

        builder.setTitle("Create new Randori file")
                .addKind("Class", RandoriIcons.Randori16, RandoriTemplates.RANDORI_CLASS)
                .addKind("Interface", RandoriIcons.Randori16, RandoriTemplates.RANDORI_INTERFACE)
                .addKind("Behavior", RandoriIcons.Randori16, RandoriTemplates.RANDORI_BEHAVIOUR)
                .addKind("Mediator", RandoriIcons.Randori16, RandoriTemplates.RANDORI_MEDIATOR)
                .addKind("Context", RandoriIcons.Randori16, RandoriTemplates.RANDORI_CONTEXT);

        for (FileTemplate template : FileTemplateManager.getInstance().getAllTemplates())
        {
            FileType fileType = FileTypeManagerEx.getInstanceEx().getFileTypeByExtension(template.getExtension());

            if (fileType.getDefaultExtension().endsWith(ActionScriptFileType.INSTANCE.getDefaultExtension())
                    && JavaDirectoryService.getInstance().getPackage(directory) != null)
            {
                builder.addKind(template.getName(), RandoriIcons.Randori16, template.getName());
            }
        }
    }

    @Override
    protected String getActionName(PsiDirectory directory, String newName, String templateName)
    {
        return "Randori File";
    }

    @Override
    protected boolean isAvailable(DataContext dataContext)
    {
        return super.isAvailable(dataContext)
                && ProjectUtils.hasRandoriModuleType(LangDataKeys.PROJECT.getData(dataContext));
    }
}
