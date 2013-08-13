package randori.plugin;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NonNls;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Frédéric THOMAS
 */
public class RandoriLightPlatformCodeInsightFixtureTestCase extends LightPlatformCodeInsightFixtureTestCase {

    /**
     * Return relative path to the test data.
     *
     * @return relative path to the test data.
     */
    @NonNls
    @Override
    protected String getBasePath() {
        return RandoriTestUtil.TEST_BASE_PATH;
    }

    /**
     * Return absolute path to the test data. Not intended to be overridden.
     *
     * @return absolute path to the test data.
     */
    @NonNls
    @Override
    protected String getTestDataPath() {
        return RandoriTestUtil.getTestDataPath(this);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new RandoriTestUtil.DefaultRandoriLightProjectDescriptor();
    }

    protected void writeTestFile(String fileName, String content) {
        try {
            FileWriter file = new FileWriter(getTestDataPath() + fileName);
            BufferedWriter out = new BufferedWriter(file);
            out.write(content);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
