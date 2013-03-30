package randori.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import randori.plugin.utils.ProjectUtils;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public class BaseRandoriBuildAction extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent event)
    {
    }

    @Override
    public void update(AnActionEvent e)
    {
        boolean visible = ProjectUtils.hasRandoriModuleType(ProjectUtils.getProject());
        e.getPresentation().setVisible(visible);
    }

}
