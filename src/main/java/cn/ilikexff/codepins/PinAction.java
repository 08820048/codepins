package cn.ilikexff.codepins;

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
        if (note == null) note = "";

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
                isBlock
        );

        PinStorage.addPin(pin);

        // 状态栏和通知提示
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            StatusBar.Info.set("✅ 图钉已添加", project);
        }
        Notifications.Bus.notify(new Notification(
                "CodePins",
                "图钉添加成功",
                isBlock ? "已添加一段代码块图钉" : "已添加单行图钉",
                NotificationType.INFORMATION
        ), project);
    }
}