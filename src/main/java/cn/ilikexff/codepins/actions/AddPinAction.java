package cn.ilikexff.codepins.actions;

import cn.ilikexff.codepins.PinAction;
import cn.ilikexff.codepins.PinEntry;
import cn.ilikexff.codepins.PinStorage;
import cn.ilikexff.codepins.services.LicenseService;
import cn.ilikexff.codepins.ui.SimpleTagEditorDialog;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 添加图钉的快捷键 Action
 */
public class AddPinAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) {
            return;
        }

        Document document = editor.getDocument();
        int offset = editor.getCaretModel().getOffset();
        int lineNumber = document.getLineNumber(offset) + 1;

        // 创建标记
        int lineStartOffset = document.getLineStartOffset(lineNumber - 1);
        int lineEndOffset = document.getLineEndOffset(lineNumber - 1);
        RangeMarker marker = document.createRangeMarker(lineStartOffset, lineEndOffset);

        // 请求用户输入备注
        String note = Messages.showInputDialog(
                project,
                "请输入图钉备注（可选）：",
                "添加图钉",
                null
        );

        // 如果用户取消了输入，不添加图钉
        if (note == null) {
            return;
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

        // 添加图钉
        PinEntry pinEntry = new PinEntry(
                file.getPath(),
                marker,
                note,
                System.currentTimeMillis(),
                System.getProperty("user.name"),
                false,
                tags
        );
        boolean success = PinStorage.addPin(pinEntry);

        // 状态栏和通知提示
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (success) {
            // 添加成功
            if (statusBar != null) {
                StatusBar.Info.set("✅ 图钉已添加", project);
            }

            // 显示成功消息
            Messages.showInfoMessage(
                    project,
                    "已成功添加图钉到第 " + lineNumber + " 行",
                    "添加图钉"
            );
        } else {
            // 添加失败
            if (statusBar != null) {
                StatusBar.Info.set("❌ 图钉添加失败", project);
            }

            // 获取图钉数量信息
            Map<String, Integer> pinsInfo = PinStorage.getPinsCountInfo();
            int currentPins = pinsInfo.get("current");
            int maxPins = pinsInfo.get("max");

            // 获取标签数量信息
            Map<String, Integer> tagsInfo = PinStorage.getTagsCountInfo();
            int currentTagTypes = tagsInfo.get("current");
            int maxTagTypes = tagsInfo.get("max");
            int maxTagsPerPin = tagsInfo.get("perPin");

            // 确定失败原因
            String failureReason;
            String featureName;

            if (currentPins >= maxPins && maxPins != -1) {
                failureReason = "免费版限制" + maxPins + "个图钉，升级到专业版可获得无限图钉";
                featureName = "无限图钉";
            } else if (tags.size() > maxTagsPerPin && maxTagsPerPin != -1) {
                failureReason = "免费版每个图钉最多只能添加" + maxTagsPerPin + "个标签，升级到专业版可获得无限标签";
                featureName = "无限标签";
            } else if (currentTagTypes >= maxTagTypes && maxTagTypes != -1) {
                failureReason = "免费版最多只能创建" + maxTagTypes + "种不同标签，升级到专业版可获得无限标签";
                featureName = "无限标签";
            } else {
                failureReason = "添加图钉失败，请稍后重试";
                featureName = "专业版功能";
            }

            // 显示错误消息
            Messages.showWarningDialog(
                    project,
                    failureReason,
                    "添加图钉失败"
            );

            // 创建带有升级链接的通知
            Notification notification = new Notification(
                    "CodePins",
                    "图钉添加失败",
                    failureReason,
                    NotificationType.WARNING
            );

            // 添加升级按钮
            final String finalFeatureName = featureName;
            notification.addAction(new AnAction("立即升级") {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    // 显示升级对话框
                    LicenseService.getInstance().showUpgradeDialogIfNeeded(project, finalFeatureName);
                }
            });

            Notifications.Bus.notify(notification, project);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只有在编辑器中有文件打开时才启用此操作
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabled(project != null && editor != null);
    }
}
