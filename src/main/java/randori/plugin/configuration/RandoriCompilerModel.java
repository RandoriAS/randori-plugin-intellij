package randori.plugin.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * @author Frédéric THOMAS
 */

@State(name = RandoriCompilerModel.COMPONENT_NAME, storages = {
        @Storage(id = "default", file = "$PROJECT_FILE$"),
        @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/randoriCompiler.xml", scheme = StorageScheme.DIRECTORY_BASED) })
public class RandoriCompilerModel implements PersistentStateComponent<RandoriCompilerModel>
{

    public static final String COMPONENT_NAME = "RandoriCompilerModel";

    private static final int CORES_COUNT = Runtime.getRuntime().availableProcessors();
    public static final boolean makeProjectOnSaveEnabled = CORES_COUNT > 2;

    private boolean makeOnSave;

    @NotNull
    public static RandoriCompilerModel getInstance(Project project)
    {
        return ServiceManager.getService(project, RandoriCompilerModel.class);
    }

    public RandoriCompilerModel()
    {
        makeOnSave = true;
    }

    public boolean isMakeOnSave()
    {
        return makeOnSave && makeProjectOnSaveEnabled;
    }

    public void setMakeOnSave(boolean makeOnSave)
    {
        this.makeOnSave = makeOnSave;
    }

    @Nullable
    @Override
    public RandoriCompilerModel getState()
    {
        return this;
    }

    @Override
    public void loadState(RandoriCompilerModel state)
    {
        XmlSerializerUtil.copyBean(state, this);
    }
}
