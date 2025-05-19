package cn.ilikexff.codepins;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.EditorTextField;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.EditorSettings;

import javax.swing.*;
import java.awt.*;

/**
 * 工具类：用于显示代码预览弹窗（支持代码块内容）
 */
public class CodePreviewUtil {

    /**
     * 展示代码块图钉的上下文代码（直接从 RangeMarker 获取选中片段）
     */
    public static void showPreviewPopup(Project project, PinEntry pin) {
        // 添加调试信息
        System.out.println("[CodePins] 尝试预览代码，图钉信息: " +
                          (pin != null ? (pin.filePath + ", isBlock=" + pin.isBlock) : "null"));

        // 使用 ReadAction 包装文档访问操作，确保线程安全
        com.intellij.openapi.application.ReadAction.run(() -> {
            try {
                if (pin == null) {
                    System.out.println("[CodePins] 无法预览代码：图钉为空");
                    showErrorMessage("无法预览代码：图钉为空");
                    return;
                }

                if (pin.marker == null) {
                    System.out.println("[CodePins] 无法预览代码：图钉标记为空");
                    showErrorMessage("无法预览代码：图钉标记为空");
                    return;
                }

                if (!pin.marker.isValid()) {
                    System.out.println("[CodePins] 无法预览代码：图钉标记无效");
                    showErrorMessage("无法预览代码：图钉标记无效");
                    return;
                }

                Document document = pin.marker.getDocument();
                if (document == null) {
                    System.out.println("[CodePins] 无法预览代码：无法获取文档");
                    showErrorMessage("无法预览代码：无法获取文档");
                    return;
                }

                FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(pin.filePath);
                if (fileType == null) {
                    System.out.println("[CodePins] 无法预览代码：无法确定文件类型");
                    showErrorMessage("无法预览代码：无法确定文件类型");
                    return;
                }

                // 继续处理预览逻辑
                processPreview(project, pin, document, fileType);
            } catch (Exception e) {
                System.out.println("[CodePins] ReadAction 中预览代码时出错: " + e.getMessage());
                e.printStackTrace();
                showErrorMessage("预览代码时出错: " + e.getMessage());
            }
        });
    }

    /**
     * 显示错误消息对话框
     */
    private static void showErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, "预览错误", JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * 处理预览逻辑（在 ReadAction 中调用）
     */
    private static void processPreview(Project project, PinEntry pin, Document document, FileType fileType) {

        try {
            int startOffset, endOffset;
            // 定义行号变量，用于标题显示
            int startLine, endLine;

            if (pin.isBlock && pin.marker.getStartOffset() != pin.marker.getEndOffset()) {
                // 如果是代码块图钉，并且有有效范围，则直接使用该范围
                startOffset = pin.marker.getStartOffset();
                endOffset = pin.marker.getEndOffset();
                // 计算行号范围
                startLine = document.getLineNumber(startOffset);
                endLine = document.getLineNumber(endOffset);
                System.out.println("[CodePins] 使用代码块范围预览: " + startOffset + "-" + endOffset);
            } else {
                // 否则，显示当前行及前后共3行
                int line = document.getLineNumber(pin.marker.getStartOffset());
                int contextLines = 3;
                startLine = Math.max(0, line - contextLines);
                endLine = Math.min(document.getLineCount() - 1, line + contextLines);

                startOffset = document.getLineStartOffset(startLine);
                endOffset = document.getLineEndOffset(endLine);
                System.out.println("[CodePins] 使用上下文范围预览: " + startOffset + "-" + endOffset);
            }

            // 确保偏移量在文档范围内
            int docLength = document.getTextLength();
            startOffset = Math.max(0, Math.min(startOffset, docLength));
            endOffset = Math.max(0, Math.min(endOffset, docLength));

            if (startOffset >= endOffset) {
                System.out.println("[CodePins] 无法预览代码：选区范围无效 " + startOffset + "-" + endOffset);
                JOptionPane.showMessageDialog(null,
                    "无法预览代码：选区范围无效",
                    "预览错误",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            String codeSnippet = document.getText().substring(startOffset, endOffset);
            if (codeSnippet.trim().isEmpty()) {
                System.out.println("[CodePins] 无法预览代码：代码片段为空");
                JOptionPane.showMessageDialog(null,
                    "无法预览代码：代码片段为空",
                    "预览错误",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 创建一个新的文档来显示代码片段
            Document snippetDoc = EditorFactory.getInstance().createDocument(codeSnippet);
            EditorTextField editorField = new EditorTextField(snippetDoc, project, fileType, true, false);

            editorField.setOneLineMode(false);
            editorField.setPreferredSize(new Dimension(600, 300));

            SwingUtilities.invokeLater(() -> {
                EditorEx editor = (EditorEx) editorField.getEditor();
                if (editor != null) {
                    EditorSettings settings = editor.getSettings();
                    settings.setLineNumbersShown(true);
                    settings.setLineMarkerAreaShown(true);
                    settings.setFoldingOutlineShown(false);
                    editor.setHorizontalScrollbarVisible(true);
                    editor.setVerticalScrollbarVisible(true);
                }
            });

            // 创建带有文件名和行号的标题
            String fileName = pin.filePath;
            int lastSlash = Math.max(pin.filePath.lastIndexOf('/'), pin.filePath.lastIndexOf('\\'));
            if (lastSlash >= 0) {
                fileName = pin.filePath.substring(lastSlash + 1);
            }
            String title = String.format("🪄 代码预览: %s (第 %d-%d 行)", fileName, startLine + 1, endLine + 1);

            JBPopup popup = JBPopupFactory.getInstance()
                    .createComponentPopupBuilder(editorField, null)
                    .setTitle(title)
                    .setResizable(true)
                    .setMovable(true)
                    .setRequestFocus(true)
                    // 移除 setDimensionServiceKey 调用，因为参数不匹配
                    .createPopup();

            popup.showInFocusCenter();
            System.out.println("[CodePins] 成功显示代码预览，长度: " + codeSnippet.length());
        } catch (Exception e) {
            System.out.println("[CodePins] 预览代码时出错: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "预览代码时出错: " + e.getMessage(),
                "预览错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}