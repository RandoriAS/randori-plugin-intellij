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
import randori.plugin.icons.RandoriIcons;
import randori.plugin.module.RandoriModuleType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public class RandoriTemplatesFactory implements
        FileTemplateGroupDescriptorFactory
{

    private static final String RANDORI_BEHAVIOUR_TEMPLATE = "Randori Behavior.as";
    private static final String RANDORI_CLASS_TEMPLATE = "Randori Class.as";
    private static final String RANDORI_CONTEXT_TEMPLATE = "Randori Context.as";
    private static final String RANDORI_INTERFACE_TEMPLATE = "Randori Interface.as";
    private static final String RANDORI_MEDIATOR_TEMPLATE = "Randori Mediator.as";

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("Randori", RandoriIcons.Randori16);

        addTemplate(group, RANDORI_BEHAVIOUR_TEMPLATE);
        addTemplate(group, RANDORI_CLASS_TEMPLATE);
        addTemplate(group, RANDORI_CONTEXT_TEMPLATE);
        addTemplate(group, RANDORI_INTERFACE_TEMPLATE);
        addTemplate(group, RANDORI_MEDIATOR_TEMPLATE);

        return group;
    }

    private void addTemplate(FileTemplateGroupDescriptor group, String templateFileName) {
        group.addTemplate(new FileTemplateDescriptor(templateFileName, RandoriIcons.Randori16));
    }
}
