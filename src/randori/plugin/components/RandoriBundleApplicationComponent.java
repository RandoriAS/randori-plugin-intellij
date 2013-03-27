package randori.plugin.components;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import randori.plugin.builder.FileChangeListener;
import randori.plugin.execution.BuildSourceCommand;
import randori.plugin.execution.CompilerArguments;

public class RandoriBundleApplicationComponent  implements ApplicationComponent {
    private CompilerArguments compilerArguments;

    private VirtualFileListener fileChangeListener;
    private BuildSourceCommand buildSourceCommand;

    public BuildSourceCommand getBuildSourceCommand()
    {
        return buildSourceCommand;
    }

    public CompilerArguments getCompilerArguments()
    {
        return compilerArguments;
    }

    @Override
    public void initComponent()
    {
        fileChangeListener = new FileChangeListener();
        VirtualFileManager.getInstance().addVirtualFileListener(
                fileChangeListener);

        compilerArguments = new CompilerArguments();
        buildSourceCommand = new BuildSourceCommand();
    }

    @Override
    public void disposeComponent()
    {
        VirtualFileManager.getInstance().removeVirtualFileListener(
                fileChangeListener);
    }

    @NotNull
    @Override
    public String getComponentName()
    {
        return "RandoriBundleApplicationComponent";
    }
}
