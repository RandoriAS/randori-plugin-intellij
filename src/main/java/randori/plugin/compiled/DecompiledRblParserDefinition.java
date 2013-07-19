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

package randori.plugin.compiled;

import com.intellij.lang.javascript.ECMAL4ParserDefinition;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.psi.tree.IFileElementType;
import randori.plugin.components.RandoriApplicationComponent;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 15:52
 */
public class DecompiledRblParserDefinition extends ECMAL4ParserDefinition
{
    private static final IFileElementType FILE_TYPE = new JSFileElementType(RandoriApplicationComponent.DECOMPILED_RBL);

    public IFileElementType getFileNodeType()
    {
        return FILE_TYPE;
    }
}
