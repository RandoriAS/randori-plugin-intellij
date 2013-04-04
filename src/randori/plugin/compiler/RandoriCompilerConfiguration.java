package randori.plugin.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.compiler.AnnotationProcessingConfiguration;
import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.MalformedPatternException;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.compiler.options.ExcludedEntriesConfiguration;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Frédéric THOMAS
 */
@State(name = RandoriCompilerConfiguration.COMPONENT_NAME, storages = {@Storage(id = RandoriCompilerConfiguration.COMPONENT_NAME, file = "$WORKSPACE_FILE$")})
public class RandoriCompilerConfiguration extends CompilerConfiguration implements PersistentStateComponent<RandoriCompilerConfiguration.MyStateBean>, Disposable {

    public static final String COMPONENT_NAME = "RandoriCompilerConfiguration";

    private String myHeapSize;

    public static RandoriCompilerConfiguration getInstance(Project project) {
        return ServiceManager.getService(project, RandoriCompilerConfiguration.class);
    }

    @Override
    public boolean isExcludedFromCompilation(VirtualFile virtualFile) {
        return false;  // TODO implement method
    }

    @Override
    public boolean isResourceFile(VirtualFile virtualFile) {
        return false;  // TODO implement method
    }

    @Override
    public boolean isResourceFile(String path) {
        return false;  // TODO implement method
    }

    @Override
    public void addResourceFilePattern(String namePattern) throws MalformedPatternException {
        // TODO implement method
    }

    @Override
    public boolean isAddNotNullAssertions() {
        return false;  // TODO implement method
    }

    @Override
    public void setAddNotNullAssertions(boolean enabled) {
        // TODO implement method
    }

    @Override
    public ExcludedEntriesConfiguration getExcludedEntriesConfiguration() {
        return null;  // TODO implement method
    }

    public RandoriCompilerConfiguration() {
        myHeapSize = "400";
    }

    @Nullable
    @Override
    public String getProjectBytecodeTarget() {
        return null;  // TODO implement method
    }

    @Nullable
    @Override
    public String getBytecodeTargetLevel(Module module) {
        return null;  // TODO implement method
    }

    @Override
    public void setBytecodeTargetLevel(Module module, String level) {
        // TODO implement method
    }

    @NotNull
    @Override
    public AnnotationProcessingConfiguration getAnnotationProcessingConfiguration(Module module) {
        return null;  // TODO implement method
    }

    @Override
    public boolean isAnnotationProcessorsEnabled() {
        return false;  // TODO implement method
    }

    public String getHeapSize() {
        return this.myHeapSize;
    }

    public void setHeapSize(String heapSize) {
        this.myHeapSize = heapSize;
    }

    @Override
    public void dispose() {
        // TODO implement method
    }

    @Nullable
    @Override
    public MyStateBean getState() {
        MyStateBean bean = new MyStateBean();
        bean.heapSize = this.myHeapSize;
        return bean;
    }

    @Override
    public void loadState(MyStateBean state) {
        this.myHeapSize = state.heapSize;
    }

    public static class MyStateBean {
        public String heapSize = "400";
    }
}
