package randori.plugin.compiler;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;

/**
 * @author Frédéric THOMAS
 */
public class RandoriCompilerLoader extends AbstractProjectComponent
{

    private static final String COMPONENT_NAME = "RandoriCompilerLoader";

    public RandoriCompilerLoader(Project project)
    {
        super(project);
    }

    @Override
    public void projectOpened()
    {
        CompilerManager compilerManager = CompilerManager
                .getInstance(this.myProject);

        compilerManager.addCompiler(new RandoriCompiler(this.myProject));
    }

    @NotNull
    @Override
    public String getComponentName()
    {
        return COMPONENT_NAME;
    }
}
