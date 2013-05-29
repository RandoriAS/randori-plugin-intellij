package randori.plugin.compiler;

import java.util.*;
import org.jetbrains.annotations.NotNull;
import com.intellij.compiler.impl.javaCompiler.ModuleChunk;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.util.PathsList;

/**
 * @author Frédéric THOMAS
 */
public abstract class RandoriCompilerExtension {
    public static final ExtensionPointName<RandoriCompilerExtension> EP_NAME = ExtensionPointName.create("randori.compilerExtension");

    public abstract void enhanceCompilationClassPath(@NotNull ModuleChunk paramModuleChunk, @NotNull PathsList paramPathsList);

    @NotNull
    public abstract List<String> getCompilationUnitPatchers(@NotNull ModuleChunk paramModuleChunk);
}
