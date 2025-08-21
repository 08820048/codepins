package cn.ilikexff.codepins.template.actions;

import cn.ilikexff.codepins.i18n.CodePinsBundle;
import cn.ilikexff.codepins.template.PinTemplate;
import cn.ilikexff.codepins.template.TemplateApplicator;
import cn.ilikexff.codepins.template.ui.TemplateSelectionDialog;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用模板Action
 * 显示模板选择对话框并应用选中的模板
 */
public class ApplyTemplateAction extends AnAction {
    
    public ApplyTemplateAction() {
        super("应用模板", "选择并应用图钉模板", null);
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        
        if (project == null || editor == null) {
            return;
        }
        
        // 显示模板选择对话框
        TemplateSelectionDialog dialog = new TemplateSelectionDialog(project);
        if (dialog.showAndGet()) {
            PinTemplate selectedTemplate = dialog.getSelectedTemplate();
            String description = dialog.getDescription();
            
            if (selectedTemplate != null) {
                // 准备自定义变量
                Map<String, String> customVariables = new HashMap<>();
                if (!description.isEmpty()) {
                    customVariables.put("description", description);
                }
                
                // 应用模板
                TemplateApplicator.applyTemplate(project, editor, selectedTemplate, customVariables);
            }
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
