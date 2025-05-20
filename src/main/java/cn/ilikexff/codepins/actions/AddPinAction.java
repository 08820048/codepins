package cn.ilikexff.codepins.actions;

import cn.ilikexff.codepins.PinAction;
import cn.ilikexff.codepins.PinEntry;
import cn.ilikexff.codepins.PinStorage;
import cn.ilikexff.codepins.ui.SimpleTagEditorDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
        PinStorage.addPin(pinEntry);

        // 显示成功消息
        Messages.showInfoMessage(
                project,
                "已成功添加图钉到第 " + lineNumber + " 行",
                "添加图钉"
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只有在编辑器中有文件打开时才启用此操作
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabled(project != null && editor != null);
    }
}
