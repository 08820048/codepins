package cn.ilikexff.codepins.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

/**
 * 切换图钉工具窗口显示状态的快捷键 Action
 */
public class TogglePinsToolWindowAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("CodePins");
        
        if (toolWindow != null) {
            if (toolWindow.isVisible()) {
                toolWindow.hide();
            } else {
                toolWindow.show();
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只有在有项目打开时才启用此操作
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null);
    }
}
