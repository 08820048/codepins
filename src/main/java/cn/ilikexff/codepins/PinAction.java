package cn.ilikexff.codepins;

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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 动作：在当前行或选区添加一个图钉，并可附加备注。
 */
public class PinAction extends AnAction {

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
                "请输入图钉备注（可选）：",
                "添加图钉",
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

        // 记录调试信息
        if (isBlock) {
            int startLine = document.getLineNumber(caret.getSelectionStart()) + 1;
            int endLine = document.getLineNumber(caret.getSelectionEnd()) + 1;
            System.out.println("[CodePins] 创建代码块图钉，行范围: " + startLine + "-" + endLine);
        } else {
            System.out.println("[CodePins] 创建单行图钉，行号: " + (document.getLineNumber(caret.getOffset()) + 1));
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

        // 状态栏和通知提示
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (success) {
            // 添加成功
            if (statusBar != null) {
                StatusBar.Info.set("✅ 图钉已添加", project);
            }
            Notifications.Bus.notify(new Notification(
                    "CodePins",
                    "图钉添加成功",
                    isBlock ? "已添加一段代码块图钉" : "已添加单行图钉",
                    NotificationType.INFORMATION
            ), project);
        } else {
            // 添加失败
            if (statusBar != null) {
                StatusBar.Info.set("❌ 图钉添加失败", project);
            }
            // 创建带有升级链接的通知
            Notification notification = new Notification(
                    "CodePins",
                    "图钉添加失败",
                    "免费版限制100个图钉，升级到专业版可获得无限图钉",
                    NotificationType.WARNING
            );

            // 添加升级按钮
            notification.addAction(new AnAction("升级到专业版") {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    // 显示升级对话框
                    cn.ilikexff.codepins.services.LicenseService.getInstance().showUpgradeDialogIfNeeded(project, "无限图钉");
                }
            });

            Notifications.Bus.notify(notification, project);
        }
    }
}