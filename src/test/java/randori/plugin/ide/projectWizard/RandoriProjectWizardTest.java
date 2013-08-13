package randori.plugin.ide.projectWizard;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.projectWizard.ProjectWizardTestCase;
import com.intellij.ide.util.importProject.LibrariesDetectionStep;
import com.intellij.ide.util.importProject.RootsDetectionStep;
import com.intellij.ide.util.newProjectWizard.ProjectNameStep;
import com.intellij.ide.util.projectWizard.ImportFromSourcesProvider;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.util.Consumer;
import randori.plugin.RandoriTestUtil;
import randori.plugin.module.RandoriWebModuleType;
import randori.plugin.projectStructure.detection.RandoriSdkStep;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Frédéric THOMAS
 */
public class RandoriProjectWizardTest extends ProjectWizardTestCase {

    public void testCreateProject() throws Exception {
        Sdk randoriSdk = RandoriTestUtil.createRandoriSdk();

        Project project = createProjectFromTemplate(RandoriWebModuleType.RANDORI_GROUP, RandoriWebModuleType.PRESENTABLE_MODULE_NAME, null);

        assertEquals("The project's SDK should be the one registered", randoriSdk.getName(), ProjectRootManager.getInstance(project).getProjectSdkName());

        Module module = ModuleManager.getInstance(project).getModules()[0];
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

        assertTrue("module should be RandoriWebModuleType", RandoriWebModuleType.isOfType(module));
        assertTrue("The module's SDK should be inherited from the project's one", moduleRootManager.isSdkInherited());
    }

    public void testCreateProjectWithoutSdk() throws Exception {
        try {
            createProjectFromTemplate(RandoriWebModuleType.RANDORI_GROUP, RandoriWebModuleType.PRESENTABLE_MODULE_NAME, null);
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertEquals(IdeBundle.message("prompt.confirm.project.no.jdk"), e.getMessage());
        }
    }

    public void testImportSimpleProject() throws Exception {
        final boolean[] isProjectNameStepPassed = {false};
        final boolean[] isRootsDetectionStepPassed = {false};
        final boolean[] isLibrariesDetectionStepPassed = {false};
        final boolean[] isModulesRandoriSdkStepPassed = {false};

        final ImportFromSourcesProvider provider = new ImportFromSourcesProvider();
        final File contentRootFile = new File(RandoriTestUtil.getTestDataPath(this) + "simpleModule");
        final File sourceRootFile = new File(RandoriTestUtil.getTestDataPath(this) + "simpleModule/src");

        final Module simpleModule = importProjectFrom(contentRootFile.getAbsolutePath(), new Consumer<ModuleWizardStep>() {
            @Override
            public void consume(ModuleWizardStep moduleWizardStep) {
                if (moduleWizardStep instanceof ProjectNameStep) {
                    ProjectNameStep projectNameStep = (ProjectNameStep) moduleWizardStep;
                    assertEquals(projectNameStep.getProjectName(), "simpleModule");
                    isProjectNameStepPassed[0] = true;
                } else if (moduleWizardStep instanceof RootsDetectionStep) {
                    isRootsDetectionStepPassed[0] = true;
                } else if (moduleWizardStep instanceof LibrariesDetectionStep) {
                    isLibrariesDetectionStepPassed[0] = true;
                } else if (moduleWizardStep instanceof RandoriSdkStep) {
                    isModulesRandoriSdkStepPassed[0] = true;
                }
            }
        }, provider);

        assertTrue("Wizard should show ProjectNameStep", isProjectNameStepPassed[0]);
        assertTrue("Wizard should show RootsDetectionStep", isRootsDetectionStepPassed[0]);
        assertTrue("Wizard should show LibrariesDetectionStep", isLibrariesDetectionStepPassed[0]);
        assertTrue("Wizard should show RandoriSdkStep", isModulesRandoriSdkStepPassed[0]);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        removeIdeaDir("simpleProject");
        removeIdeaDir("simpleProjectWithNestedModule");
        removeIdeaDir("simpleProjectWithNestedAndCommonModules");
        removeIdeaDir("simpleProjectWithNestedModuleAndCommonRbl");
    }

    private void removeIdeaDir(String dirName) {
        File dir = new File(RandoriTestUtil.getTestDataPath(this) + dirName);
        if (dir.exists()) {
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.equals(".idea");
                }
            });
            for (File file : files) {
                file.deleteOnExit();
            }
        }
    }
}
