package randori.plugin.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created with IntelliJ IDEA.
 * User: DoubleFx
 * Date: 27/07/13
 * Time: 21:55
 * To change this template use File | Settings | File Templates.
 */
public class RandoriBaseRunner extends GenericProgramRunner {
    @Nullable
    @Override
    protected RunContentDescriptor doExecute(Project project, Executor executor, RunProfileState state, RunContentDescriptor contentToReuse, ExecutionEnvironment env) throws ExecutionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public String getRunnerId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
