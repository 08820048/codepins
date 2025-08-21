package cn.ilikexff.codepins.template;

import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 模板应用器
 * 负责将模板应用到代码中并创建对应的图钉
 */
public class TemplateApplicator {
    
    /**
     * 应用模板到当前编辑器位置
     * 
     * @param project 项目
     * @param editor 编辑器
     * @param template 要应用的模板
     * @param customVariables 自定义变量（可选）
     * @return 是否成功应用
     */
    public static boolean applyTemplate(Project project, Editor editor, PinTemplate template, 
                                       Map<String, String> customVariables) {
        if (project == null || editor == null || template == null) {
            return false;
        }
        
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null) {
            return false;
        }
        
        // 获取当前光标位置
        int offset = editor.getCaretModel().getOffset();
        int lineNumber = document.getLineNumber(offset);
        
        // 处理模板变量
        String processedContent = processTemplateContent(template, customVariables);
        
        // 创建图钉
        RangeMarker marker = document.createRangeMarker(offset, offset);
        marker.setGreedyToLeft(true);
        marker.setGreedyToRight(true);
        
        PinEntry pin = new PinEntry(
            file.getPath(),
            marker,
            processedContent,
            System.currentTimeMillis(),
            System.getProperty("user.name"),
            false, // 模板应用默认为单行图钉
            template.getTags()
        );
        
        boolean success = PinStorage.addPin(pin);
        
        if (success) {
            // 显示成功消息
            showSuccessMessage(project, template, lineNumber + 1);
        } else {
            // 显示失败消息
            showErrorMessage(project, "应用模板失败");
        }
        
        return success;
    }
    
    /**
     * 应用模板（使用默认变量）
     */
    public static boolean applyTemplate(Project project, Editor editor, PinTemplate template) {
        return applyTemplate(project, editor, template, null);
    }
    
    /**
     * 交互式应用模板
     * 允许用户输入自定义描述
     */
    public static boolean applyTemplateInteractive(Project project, Editor editor, PinTemplate template) {
        if (project == null || editor == null || template == null) {
            return false;
        }
        
        // 检查模板是否包含 {description} 变量
        String templateContent = template.getContent();
        Map<String, String> customVariables = new HashMap<>();
        
        if (templateContent.contains("{description}")) {
            // 提示用户输入描述
            String description = Messages.showInputDialog(
                project,
                "请输入描述信息：",
                "应用模板：" + template.getName(),
                Messages.getQuestionIcon()
            );
            
            if (description == null) {
                // 用户取消了操作
                return false;
            }
            
            customVariables.put("description", description.trim());
        }
        
        return applyTemplate(project, editor, template, customVariables);
    }
    
    /**
     * 处理模板内容，替换变量
     */
    private static String processTemplateContent(PinTemplate template, Map<String, String> customVariables) {
        String content = template.getContent();
        if (content == null || content.isEmpty()) {
            return template.getName();
        }
        
        return TemplateVariableProcessor.processTemplate(content, customVariables);
    }
    
    /**
     * 显示成功消息
     */
    private static void showSuccessMessage(Project project, PinTemplate template, int lineNumber) {
        String message = String.format("已在第 %d 行应用模板：%s", lineNumber, template.getName());
        
        // 使用通知而不是对话框，避免打断用户工作流
        com.intellij.notification.Notifications.Bus.notify(
            new com.intellij.notification.Notification(
                "CodePins",
                "模板应用成功",
                message,
                com.intellij.notification.NotificationType.INFORMATION
            ),
            project
        );
    }
    
    /**
     * 显示错误消息
     */
    private static void showErrorMessage(Project project, String message) {
        com.intellij.notification.Notifications.Bus.notify(
            new com.intellij.notification.Notification(
                "CodePins",
                "模板应用失败",
                message,
                com.intellij.notification.NotificationType.ERROR
            ),
            project
        );
    }
    
    /**
     * 批量应用模板到选中的行
     */
    public static int applyTemplateToSelection(Project project, Editor editor, PinTemplate template) {
        if (project == null || editor == null || template == null) {
            return 0;
        }
        
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null) {
            return 0;
        }
        
        // 获取选中的文本范围
        int selectionStart = editor.getSelectionModel().getSelectionStart();
        int selectionEnd = editor.getSelectionModel().getSelectionEnd();
        
        if (selectionStart == selectionEnd) {
            // 没有选中文本，应用到当前行
            return applyTemplate(project, editor, template) ? 1 : 0;
        }
        
        // 获取选中的行范围
        int startLine = document.getLineNumber(selectionStart);
        int endLine = document.getLineNumber(selectionEnd);
        
        int successCount = 0;
        String processedContent = processTemplateContent(template, null);
        
        // 为每一行创建图钉
        for (int line = startLine; line <= endLine; line++) {
            int lineStartOffset = document.getLineStartOffset(line);
            
            RangeMarker marker = document.createRangeMarker(lineStartOffset, lineStartOffset);
            marker.setGreedyToLeft(true);
            marker.setGreedyToRight(true);
            
            PinEntry pin = new PinEntry(
                file.getPath(),
                marker,
                processedContent + " (第" + (line + 1) + "行)",
                System.currentTimeMillis(),
                System.getProperty("user.name"),
                false,
                template.getTags()
            );
            
            if (PinStorage.addPin(pin)) {
                successCount++;
            }
        }
        
        // 显示批量应用结果
        if (successCount > 0) {
            String message = String.format("已成功应用模板到 %d 行", successCount);
            com.intellij.notification.Notifications.Bus.notify(
                new com.intellij.notification.Notification(
                    "CodePins",
                    "批量应用成功",
                    message,
                    com.intellij.notification.NotificationType.INFORMATION
                ),
                project
            );
        }
        
        return successCount;
    }
    
    /**
     * 检查模板是否可以应用到当前上下文
     */
    public static boolean canApplyTemplate(Project project, Editor editor, PinTemplate template) {
        if (project == null || editor == null || template == null) {
            return false;
        }
        
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        
        return file != null && template.isValid();
    }
}
