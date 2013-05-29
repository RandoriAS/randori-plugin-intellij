package randori.plugin.service;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

@State(name = "RandoriProjectPreferences", storages = {
        @Storage(id = "default", file = "$PROJECT_FILE$"),
        @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/randori_settings.xml", scheme = StorageScheme.DIRECTORY_BASED) })
public class RandoriProjectPreferences implements
        PersistentStateComponent<RandoriProjectPreferences>
{
    RandoriProjectPreferencesBean bean;

    @Override
    @NotNull
    public RandoriProjectPreferences getState()
    {
        //return bean != null ? bean : new RandoriProjectPreferencesBean();
        return this;
    }

    @Override
    public void loadState(RandoriProjectPreferences settingsBean)
    {
        //this.bean = settingsBean;
        XmlSerializerUtil.copyBean(settingsBean, this);
    }

    @NotNull
    public static RandoriProjectPreferences getInstance(Project project)
    {
        return ServiceManager.getService(project,
                RandoriProjectPreferences.class);
    }

    //public List<Integer> problemWindowColumnsSizes = new ArrayList<Integer>();
    public String foo = "";

    public List<Integer> getProblemWindowColumnSizes()
    {
        //        if (problemWindowColumnsSizes == null)
        //        {
        //            problemWindowColumnsSizes = new ArrayList<Integer>();
        //            problemWindowColumnsSizes.add(-1);
        //            problemWindowColumnsSizes.add(-1);
        //            problemWindowColumnsSizes.add(-1);
        //            problemWindowColumnsSizes.add(-1);
        //            problemWindowColumnsSizes.add(-1);
        //        }
        return new ArrayList<Integer>();
    }

    //    public void setProblemWindowColumnSizes(List<Integer> sizes)
    //    {
    //        problemWindowColumnsSizes = sizes;
    //    }

    static class RandoriProjectPreferencesBean
    {

    }
}
