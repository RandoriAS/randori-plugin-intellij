package randori.plugin;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NonNls;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Frédéric THOMAS Date: 17/05/13 Time: 13:12
 */
public abstract class RandoriLightCodeInsightFixtureTestCase extends LightCodeInsightFixtureTestCase
{
    private static final String TEST_BASE_PATH = "src/test/resources/";

    /**
     * Return relative path to the test data. Not intended to be overridden.
     * 
     * @return Project relative path to the test data.
     */
    @NonNls
    protected String getTestDataPath()
    {
        return TEST_BASE_PATH + this.getClass().getPackage().getName().replace(".", "/") + "/";
    }

    protected void writeTestFile(String fileName, String content)
    {
        try
        {
            FileWriter file = new FileWriter(getTestDataPath() + fileName);
            BufferedWriter out = new BufferedWriter(file);
            out.write(content);
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
