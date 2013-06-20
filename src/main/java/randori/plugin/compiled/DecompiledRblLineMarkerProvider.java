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

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.lang.javascript.highlighting.JavaScriptLineMarkerProvider;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.psi.PsiElement;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 15:55
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
