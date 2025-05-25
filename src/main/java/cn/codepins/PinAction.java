package cn.codepins;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;

public class PinAction extends AnAction {

    public PinAction() {
        super("📌 Pin This Line");
        System.out.println("[CodePins] PinAction registered"); // 插件加载时输出
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        System.out.println("[CodePins] PinAction triggered"); // 每次点击输出

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            int line = editor.getCaretModel().getLogicalPosition().line;
            VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
            if (file != null) {
                System.out.printf("[CodePins] Pin added: %s @ line %d%n", file.getPath(), line + 1);
                PinStorage.addPin(new PinEntry(file.getPath(), line));
            } else {
                System.out.println("[CodePins] Could not get VirtualFile from editor.");
            }
        } else {
            System.out.println("[CodePins] No editor context available.");
        }
    }
}