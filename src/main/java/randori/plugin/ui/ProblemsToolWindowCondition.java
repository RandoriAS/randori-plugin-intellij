package randori.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import randori.plugin.util.ProjectUtils;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public class ProblemsToolWindowCondition implements Condition
{
    @Override
    public boolean value(Object o) {
        return o instanceof Project && ProjectUtils.hasRandoriModuleType((Project) o);
    }
}
