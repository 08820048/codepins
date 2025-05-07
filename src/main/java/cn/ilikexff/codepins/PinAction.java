package cn.ilikexff.codepins;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;

/**
 * 动作：在当前行添加一个图钉，并可附加备注。
 */
public class PinAction extends AnAction {

    public PinAction() {
        System.out.println("[CodePins] PinAction registered");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        System.out.println("[CodePins] PinAction triggered");

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = e.getProject();

        if (editor != null && project != null) {
            Document document = editor.getDocument();
            int line = editor.getCaretModel().getLogicalPosition().line;
            VirtualFile file = FileDocumentManager.getInstance().getFile(document);

            if (file != null) {
                // 弹出备注输入框
                String note = Messages.showInputDialog(
                        project,
                        "请输入备注内容（可选）：",
                        "添加图钉",
                        Messages.getQuestionIcon()
                );

                if (note == null) {
                    note = ""; // 用户取消输入
                }

                // 创建 RangeMarker（追踪当前行的范围）
                int start = document.getLineStartOffset(line);
                int end = document.getLineEndOffset(line);
                RangeMarker marker = document.createRangeMarker(start, end);

                // 添加图钉（动态位置）
                long now = System.currentTimeMillis();
                String author = System.getProperty("user.name");
                PinStorage.addPin(new PinEntry(file.getPath(), marker, note, now, author));
                System.out.printf("[CodePins] Pin added: %s @ line %d - %s%n", file.getPath(), line + 1, note);

                // 状态栏提示 ✅
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                if (statusBar != null) {
                    StatusBar.Info.set("👀 钉针 +1", project);
                }

                // 弹出通知气泡 ✅
                Notifications.Bus.notify(new Notification(
                        "CodePins",
                        "图钉添加成功",
                        "📌 当前行钉针 +1",
                        NotificationType.INFORMATION
                ), project);
            } else {
                System.out.println("[CodePins] Could not get VirtualFile from editor.");
            }

        } else {
            System.out.println("[CodePins] No editor or project context available.");
        }
    }
}