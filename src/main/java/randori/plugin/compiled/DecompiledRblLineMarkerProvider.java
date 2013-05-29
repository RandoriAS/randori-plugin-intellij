package randori.plugin.compiled;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.lang.javascript.highlighting.JavaScriptLineMarkerProvider;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.psi.PsiElement;

/**
 * @author: Frédéric THOMAS Date: 27/04/13 Time: 15:55
 */
public class DecompiledRblLineMarkerProvider extends JavaScriptLineMarkerProvider
{
    public DecompiledRblLineMarkerProvider(DaemonCodeAnalyzerSettings daemonSettings, EditorColorsManager colorsManager)
    {
        super(daemonSettings, colorsManager);
    }

    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element)
    {
        return null;
    }
}
