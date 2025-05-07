package cn.ilikexff.codepins;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.EditorTextField;

import javax.swing.*;
import java.awt.*;

/**
 * 工具类：用于显示代码预览弹窗（上下文片段）
 */
public class CodePreviewUtil {

    /**
     * 显示代码片段弹窗（基于上下文行号提取代码）
     */
    public static void showPreviewPopup(Project project, PinEntry entry) {
        Document doc = entry.marker.getDocument();
        int line = entry.getCurrentLine(doc);

        // 提取目标行 ±2 行的上下文片段
        String codeSnippet = extractContextCode(doc, line, 2);
        String filePath = entry.filePath;
        FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(filePath);
        String title = new java.io.File(filePath).getName();

        // 创建 Document 和 Editor 视图
        Document previewDoc = EditorFactory.getInstance().createDocument(codeSnippet);
        EditorTextField editorField = new EditorTextField(previewDoc, project, fileType, true, false);
        editorField.setOneLineMode(false);
        editorField.setPreferredSize(new Dimension(520, 160));

        // 设置语法高亮与显示参数（延迟确保 editor 不为 null）
        SwingUtilities.invokeLater(() -> {
            EditorEx editor = (EditorEx) editorField.getEditor();
            if (editor != null) {
                EditorSettings settings = editor.getSettings();
                settings.setLineNumbersShown(true);
                settings.setLineMarkerAreaShown(true);
                settings.setFoldingOutlineShown(false);
                editor.setHorizontalScrollbarVisible(true);
            }
        });

        // 构建弹出窗口
        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(editorField, null)
                .setTitle("🪄 上下文代码预览 - " + title)
                .setResizable(true)
                .setMovable(true)
                .setRequestFocus(true)
                .createPopup();

        popup.showInFocusCenter(); // 或 .showUnderneathOf(parentComponent)
    }

    /**
     * 提取指定行上下 contextRadius 行的代码片段
     */
    public static String extractContextCode(Document doc, int targetLine, int contextRadius) {
        int startLine = Math.max(0, targetLine - contextRadius);
        int endLine = Math.min(doc.getLineCount() - 1, targetLine + contextRadius);

        StringBuilder snippet = new StringBuilder();
        for (int line = startLine; line <= endLine; line++) {
            int startOffset = doc.getLineStartOffset(line);
            int endOffset = doc.getLineEndOffset(line);
            String lineText = doc.getText().substring(startOffset, endOffset);
            snippet.append(lineText).append("\n");
        }
        return snippet.toString();
    }
}