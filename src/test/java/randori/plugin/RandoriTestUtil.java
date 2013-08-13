package randori.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.testFramework.LightProjectDescriptor;
import junit.framework.TestCase;
import randori.plugin.module.RandoriWebModuleType;
import randori.plugin.roots.RandoriSdkType;

import java.io.File;

/**
 * @author Frédéric THOMAS
 */
public class RandoriTestUtil {
    public static final String TEST_BASE_PATH = "src/test/resources/";


    public static Sdk createRandoriSdk() {
        final Sdk defaultJdk = RandoriTestUtil.getMockRandoriSdk();
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();
                jdkTable.addJdk(defaultJdk);

                Project defaultProject = ProjectManager.getInstance().getDefaultProject();
                ProjectRootManager.getInstance(defaultProject).setProjectSdk(defaultJdk);
            }
        });
        return defaultJdk;
    }

    public static Sdk getMockRandoriSdk() {
        return getMockRandoriSdk("Randori SDK");
    }

    public static Sdk getMockRandoriSdk(String name) {
        File sdkPath = getMockRandoriSdkPath();
        return sdkPath.exists() ? RandoriSdkType.getInstance().createSdk(name, sdkPath.getPath()) : null;
    }

    public static File getMockRandoriSdkPath() {
        return getPathForRandoriSdkNamed("mockRandoriSDK");
    }

    private static File getPathForRandoriSdkNamed(String name) {
        return new File(TEST_BASE_PATH, "sdk/" + name);
    }

    public static String getTestDataPath(TestCase testCase) {
        return TEST_BASE_PATH + testCase.getClass().getPackage().getName().replace(".", "/") + "/";
    }

    public static class DefaultRandoriLightProjectDescriptor implements LightProjectDescriptor {

        @Override
        public ModuleType getModuleType() {
            return RandoriWebModuleType.getInstance();
        }

        @Override
        public Sdk getSdk() {
            return RandoriTestUtil.getMockRandoriSdk();
        }

        @Override
        public void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {

        }
    }
}
