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
 * 快速FIXME模板Action
 */
public class QuickFixmeAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        
        if (project == null || editor == null) {
            return;
        }
        
        // 查找FIXME类型的内置模板
        TemplateStorage storage = TemplateStorage.getInstance();
        PinTemplate template = storage.getTemplatesByType(PinTemplate.TemplateType.FIXME)
                .stream()
                .filter(PinTemplate::isBuiltIn)
                .findFirst()
                .orElse(null);
        
        if (template != null) {
            TemplateApplicator.applyTemplateInteractive(project, editor, template);
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabled(project != null && editor != null);
    }
    
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
