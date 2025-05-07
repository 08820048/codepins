package cn.ilikexff.codepins;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

public class PinAction extends AnAction {
    public PinAction() {
        super("📌 Pin This Line");
        System.out.println("[CodePins] PinAction registered");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        System.out.println("[CodePins] PinAction triggered");

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            int line = editor.getCaretModel().getLogicalPosition().line;
            VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
            if (file != null) {
                // 弹出备注输入框
                String note = Messages.showInputDialog(
                        editor.getProject(),
                        "Enter a note for this pin (optional):",
                        "Add Code Pin",
                        Messages.getQuestionIcon()
                );

                if (note == null) {
                    note = ""; // 用户点击取消
                }

                PinStorage.addPin(new PinEntry(file.getPath(), line, note));
                System.out.printf("[CodePins] Pin added: %s @ line %d - %s%n", file.getPath(), line + 1, note);
            } else {
                System.out.println("[CodePins] Could not get VirtualFile from editor.");
            }
        } else {
            System.out.println("[CodePins] No editor context available.");
        }
    }
}