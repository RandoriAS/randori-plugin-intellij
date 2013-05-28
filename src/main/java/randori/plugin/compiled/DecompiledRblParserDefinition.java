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
