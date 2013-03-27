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

import com.intellij.ide.fileTemplates.*;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import randori.plugin.module.RandoriModuleType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

/**
 * @author Michael Schmalle
 */
public class RandoriTemplatesFactory implements
        FileTemplateGroupDescriptorFactory
{

    @NonNls
    public static final String[] TEMPLATES = { RandoriTemplates.RANDORI_CLASS,
            RandoriTemplates.RANDORI_INTERFACE };

    @NonNls
    static final String NAME_TEMPLATE_PROPERTY = "NAME";

    static final String LOW_CASE_NAME_TEMPLATE_PROPERTY = "lowCaseName";

    private final ArrayList<String> myCustomTemplates = new ArrayList<String>();

    public static PsiFile createFromTemplate(
            @NotNull final PsiDirectory directory, @NotNull final String name,
            @NotNull String fileName, @NotNull String templateName,
            @NonNls String... parameters) throws IncorrectOperationException
    {
        final FileTemplate template = FileTemplateManager.getInstance()
                .getTemplate(templateName);

        Properties properties = new Properties(FileTemplateManager
                .getInstance().getDefaultProperties(directory.getProject()));
        JavaTemplateUtil.setPackageNameAttribute(properties, directory);
        properties.setProperty(NAME_TEMPLATE_PROPERTY, name);
        properties.setProperty(LOW_CASE_NAME_TEMPLATE_PROPERTY,
                name.substring(0, 1).toLowerCase() + name.substring(1));
        for (int i = 0; i < parameters.length; i += 2)
        {
            properties.setProperty(parameters[i], parameters[i + 1]);
        }
        String text;
        try
        {
            text = template.getText(properties);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to load template for "
                    + FileTemplateManager.getInstance()
                            .internalTemplateToSubject(templateName), e);
        }

        final PsiFileFactory factory = PsiFileFactory.getInstance(directory
                .getProject());
        @SuppressWarnings("deprecation")
        final PsiFile file = factory.createFileFromText(fileName, text);

        return (PsiFile) directory.add(file);
    }

    public static RandoriTemplatesFactory getInstance()
    {
        return RandoriTemplatesFactoryHolder.myInstance;
    }

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor()
    {
        final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(
                "Templates", RandoriModuleType.RANDORI_ICON_SMALL);
        final FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        for (String template : TEMPLATES)
        {
            group.addTemplate(new FileTemplateDescriptor(template,
                    fileTypeManager.getFileTypeByFileName(template).getIcon()));
        }
        //        //add GSP Template
        //        group.addTemplate(new FileTemplateDescriptor(
        //                RandoriTemplates.GROOVY_SERVER_PAGE, fileTypeManager.getFileTypeByFileName(GroovyTemplates.GROOVY_SERVER_PAGE).getIcon()));

        // register custom templates
        for (String template : getInstance().getCustomTemplates())
        {
            group.addTemplate(new FileTemplateDescriptor(template,
                    fileTypeManager.getFileTypeByFileName(template).getIcon()));
        }
        return group;
    }

    public void registerCustomTemplates(String... templates)
    {
        Collections.addAll(myCustomTemplates, templates);
    }

    public String[] getCustomTemplates()
    {
        return ArrayUtil.toStringArray(myCustomTemplates);
    }

    private static class RandoriTemplatesFactoryHolder
    {
        private static final RandoriTemplatesFactory myInstance = new RandoriTemplatesFactory();
    }
}
