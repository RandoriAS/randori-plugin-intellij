/***
 * Copyright 2013 Teoti Graphix, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * @author Michael Schmalle <mschmalle@teotigraphix.com>
 */

package randori.plugin.action;

import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.JavaCreateTemplateInPackageAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import icons.RandoriIcons;

/**
 * @author Michael Schmalle
 */
public class CreateActionScriptClassAction extends
        JavaCreateTemplateInPackageAction<PsiElement>

{
    public CreateActionScriptClassAction()
    {
        super("Create new AS Class", "Creates a new ActionScript class",
                RandoriIcons.Randori16, true);
    }

    @Nullable
    @Override
    protected PsiElement getNavigationElement(@NotNull PsiElement createdElement)
    {
        return createdElement.getNavigationElement();
    }

    @Nullable
    @Override
    protected PsiElement doCreate(PsiDirectory dir, String className,
            String templateName) throws IncorrectOperationException
    {
        // dir
        // className = 'as'
        // templateName 'template'

        final String fileName = className + ".as";
        final PsiFile fromTemplate = null;
        /*final PsiFile fromTemplate = RandoriTemplatesFactory
                .createFromTemplate(dir, className, fileName, templateName);*/

        return fromTemplate;
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory directory,
            CreateFileFromTemplateDialog.Builder builder)
    {
        builder.setTitle("Create new Class").addKind("Class",
                RandoriIcons.Randori16, "RandoriClass.as");

        for (FileTemplate template : FileTemplateManager.getInstance()
                .getAllTemplates())
        {
            @SuppressWarnings("unused")
            FileType fileType = FileTypeManagerEx.getInstanceEx()
                    .getFileTypeByExtension(template.getExtension());
            if (/*fileType.equals(RandoriFileType.RANDORI_FILE_TYPE) && */JavaDirectoryService
                    .getInstance().getPackage(directory) != null)
            {
                builder.addKind(template.getName(),
                        RandoriIcons.Randori16,
                        template.getName());
            }
        }
    }

    @Override
    protected String getActionName(PsiDirectory directory, String newName,
            String templateName)
    {
        return "New AS Class";
    }

    @Override
    protected boolean isAvailable(DataContext dataContext)
    {
        return true; // return super.isAvailable(dataContext) && LibrariesUtil.hasRandoriSdk(LangDataKeys.MODULE.getData(dataContext));
    }
}
