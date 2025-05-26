package cn.ilikexff.codepins;

import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;
import cn.ilikexff.codepins.i18n.CodePinsBundle;

import cn.ilikexff.codepins.ui.SimpleTagEditorDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
// Removed unused import: org.jetbrains.annotations.NotNull

import java.util.ArrayList;
import java.util.List;

/**
 * Action: Add a pin to the current line or selection with optional note.
 */
public class PinAction extends AnAction {

    public PinAction() {
        // Note: Icon is set in plugin.xml, no need to set it here
        // Use empty constructor to avoid overriding plugin.xml settings
        System.out.println("[CodePins] PinAction registered"); // Output when plugin loads
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (editor == null || project == null) return;

        Caret caret = editor.getCaretModel().getPrimaryCaret();
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null) return;

        String note = Messages.showInputDialog(
                project,
                CodePinsBundle.message("note.placeholder"),
                CodePinsBundle.message("pin.add"),
                Messages.getQuestionIcon()
        );

        // 如果用户点击“取消”按钮，则中止添加图钉
        if (note == null) {
            return; // 用户取消了操作，直接返回
        }

        // 如果用户没有输入备注，则使用空字符串
        if (note.trim().isEmpty()) {
            note = "";
        }

        // 创建标签对话框，请求用户输入标签
        List<String> tags = new ArrayList<>();
        SimpleTagEditorDialog tagDialog = new SimpleTagEditorDialog(project, new PinEntry(
                file.getPath(),
                document.createRangeMarker(0, 0), // 临时标记，仅用于对话框
                note,
                System.currentTimeMillis(),
                System.getProperty("user.name"),
                false,
                tags
        ));

        if (tagDialog.showAndGet()) {
            // 如果用户点击了确定，获取标签
            tags = tagDialog.getTags();
        }

        boolean isBlock = caret.hasSelection();

        // Log debug information
        if (isBlock) {
            int startLine = document.getLineNumber(caret.getSelectionStart()) + 1;
            int endLine = document.getLineNumber(caret.getSelectionEnd()) + 1;
            System.out.println("[CodePins] Creating block pin, line range: " + startLine + "-" + endLine);
        } else {
            System.out.println("[CodePins] Creating single line pin, line: " + (document.getLineNumber(caret.getOffset()) + 1));
        }

        TextRange range = isBlock
                ? new TextRange(caret.getSelectionStart(), caret.getSelectionEnd())
                : new TextRange(caret.getOffset(), caret.getOffset());

        RangeMarker marker = document.createRangeMarker(range);
        marker.setGreedyToLeft(true);
        marker.setGreedyToRight(true);

        PinEntry pin = new PinEntry(
                file.getPath(),
                marker,
                note,
                System.currentTimeMillis(),
                System.getProperty("user.name"),
                isBlock,
                tags
        );

        boolean success = PinStorage.addPin(pin);

        // Status bar and notification tips
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (success) {
            // Add success
            if (statusBar != null) {
                StatusBar.Info.set("✅ " + CodePinsBundle.message("pin.added", document.getLineNumber(caret.getOffset()) + 1), project);
            }
            Notifications.Bus.notify(new Notification(
                    "CodePins",
                    CodePinsBundle.message("notification.success"),
                    isBlock ? CodePinsBundle.message("pin.type.block") + " " + CodePinsBundle.message("pin.added", document.getLineNumber(caret.getOffset()) + 1) : 
                             CodePinsBundle.message("pin.type.single") + " " + CodePinsBundle.message("pin.added", document.getLineNumber(caret.getOffset()) + 1),
                    NotificationType.INFORMATION
            ), project);
        } else {
            // Add failure
            if (statusBar != null) {
                StatusBar.Info.set("❌ " + CodePinsBundle.message("pin.invalid"), project);
            }

            // Plugin is now completely free, this should not show any limitation errors
            String failureReason = CodePinsBundle.message("notification.error");

            // Create notification
            Notification notification = new Notification(
                    "CodePins",
                    CodePinsBundle.message("notification.error"),
                    failureReason,
                    NotificationType.WARNING
            );

            Notifications.Bus.notify(notification, project);
        }
    }
}