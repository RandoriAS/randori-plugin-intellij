package randori.plugin.compiled;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighterProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 16:25
 */
public class RblSyntaxHighlighterProvider implements SyntaxHighlighterProvider
{
    public SyntaxHighlighter create(FileType fileType, Project project, VirtualFile file)
    {
        return SyntaxHighlighterFactory.getSyntaxHighlighter(JavaScriptSupportLoader.ECMA_SCRIPT_L4, project, file);
    }
}
