package randori.plugin.action;

import icons.RandoriIcons;
import randori.plugin.utils.ProjectUtils;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public class BaseRandoriMenuAction extends AnAction
{

    public BaseRandoriMenuAction()
    {
        super(RandoriIcons.Randori16);
    }

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
