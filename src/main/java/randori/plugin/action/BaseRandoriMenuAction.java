package randori.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import icons.RandoriIcons;
import randori.plugin.util.ProjectUtils;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
class BaseRandoriMenuAction extends AnAction
{

    BaseRandoriMenuAction()
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
        boolean visible = ProjectUtils.hasRandoriModuleType(LangDataKeys.PROJECT.getData(e.getDataContext()));
        e.getPresentation().setVisible(visible);
    }
}
