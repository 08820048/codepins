package cn.ilikexff.codepins.template.actions;

import cn.ilikexff.codepins.template.PinTemplate;
import cn.ilikexff.codepins.template.TemplateApplicator;
import cn.ilikexff.codepins.template.TemplateStorage;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 快速模板应用Action
 * 直接应用指定类型的模板，无需选择对话框
 */
public class QuickTemplateAction extends AnAction {
    
    private final PinTemplate.TemplateType templateType;
    
    public QuickTemplateAction(PinTemplate.TemplateType templateType) {
        super(templateType.getDisplayName(), 
              "快速应用 " + templateType.getDisplayName() + " 模板", 
              null);
        this.templateType = templateType;
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        
        if (project == null || editor == null) {
            return;
        }
        
        // 查找对应类型的内置模板
        TemplateStorage storage = TemplateStorage.getInstance();
        PinTemplate template = storage.getTemplatesByType(templateType)
                .stream()
                .filter(PinTemplate::isBuiltIn)
                .findFirst()
                .orElse(null);
        
        if (template != null) {
            // 交互式应用模板（如果包含{description}变量会提示用户输入）
            TemplateApplicator.applyTemplateInteractive(project, editor, template);
        } else {
            // 如果没有找到内置模板，显示错误消息
            com.intellij.notification.Notifications.Bus.notify(
                new com.intellij.notification.Notification(
                    "CodePins",
                    "模板不存在",
                    "未找到 " + templateType.getDisplayName() + " 类型的内置模板",
                    com.intellij.notification.NotificationType.WARNING
                ),
                project
            );
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只有在编辑器中有文件打开时才启用此操作
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabled(project != null && editor != null);
    }
    
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
