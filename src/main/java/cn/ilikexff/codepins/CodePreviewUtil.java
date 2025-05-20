package cn.ilikexff.codepins;

import cn.ilikexff.codepins.settings.CodePinsSettings;
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
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

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
            // 创建自定义错误面板
            JPanel errorPanel = new JPanel();
            errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
            errorPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            errorPanel.setBackground(new JBColor(new Color(50, 40, 40, 245), new Color(50, 40, 40, 245)));

            // 添加错误图标和标题
            JPanel headerPanel = new JPanel();
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
            headerPanel.setOpaque(false);
            headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel iconLabel = new JLabel("⚠️"); // 警告图标
            iconLabel.setFont(iconLabel.getFont().deriveFont(18.0f));
            headerPanel.add(iconLabel);
            headerPanel.add(Box.createHorizontalStrut(10));

            JLabel titleLabel = new JLabel("代码预览错误");
            titleLabel.setForeground(new JBColor(new Color(255, 180, 180), new Color(255, 180, 180)));
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14.0f));
            headerPanel.add(titleLabel);
            headerPanel.add(Box.createHorizontalGlue());

            errorPanel.add(headerPanel);
            errorPanel.add(Box.createVerticalStrut(10));

            // 添加分隔线
            JSeparator separator = new JSeparator();
            separator.setForeground(new JBColor(new Color(100, 70, 70), new Color(100, 70, 70)));
            separator.setAlignmentX(Component.LEFT_ALIGNMENT);
            errorPanel.add(separator);
            errorPanel.add(Box.createVerticalStrut(10));

            // 添加错误信息
            JLabel errorLabel = new JLabel(message);
            errorLabel.setForeground(new JBColor(new Color(255, 255, 255), new Color(255, 255, 255)));
            errorLabel.setFont(errorLabel.getFont().deriveFont(13.0f));
            errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            errorPanel.add(errorLabel);

            // 添加确定按钮
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);
            buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton okButton = new JButton("确定");
            okButton.setFocusPainted(false);
            buttonPanel.add(okButton);

            errorPanel.add(Box.createVerticalStrut(15));
            errorPanel.add(buttonPanel);

            // 创建弹窗
            JBPopup popup = JBPopupFactory.getInstance()
                    .createComponentPopupBuilder(errorPanel, null)
                    .setResizable(false)
                    .setMovable(true)
                    .setRequestFocus(true)
                    .setCancelOnClickOutside(true)
                    .setCancelOnWindowDeactivation(true)
                    .createPopup();

            // 添加按钮点击事件
            okButton.addActionListener(e -> popup.cancel());

            // 显示弹窗
            popup.showInFocusCenter();
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
                showErrorMessage("无法预览代码：选区范围无效");
                return;
            }

            String codeSnippet = document.getText().substring(startOffset, endOffset);
            if (codeSnippet.trim().isEmpty()) {
                System.out.println("[CodePins] 无法预览代码：代码片段为空");
                showErrorMessage("无法预览代码：代码片段为空");
                return;
            }

            // 计算代码行数，用于动态调整面板高度
            int lineCount = 0;
            for (int i = 0; i < codeSnippet.length(); i++) {
                if (codeSnippet.charAt(i) == '\n') {
                    lineCount++;
                }
            }
            // 最后一行可能没有\n
            if (codeSnippet.length() > 0 && codeSnippet.charAt(codeSnippet.length() - 1) != '\n') {
                lineCount++;
            }

            // 确保至少显示1行
            lineCount = Math.max(1, lineCount);
            System.out.println("[CodePins] 代码片段行数: " + lineCount);

            // 从设置中获取预览窗口高度
            int previewHeight = 300; // 默认高度
            try {
                previewHeight = Integer.parseInt(CodePinsSettings.getInstance().previewHeight);
            } catch (NumberFormatException e) {
                System.out.println("[CodePins] 解析预览窗口高度设置失败，使用默认值: " + e.getMessage());
            }

            // 创建一个新的文档来显示代码片段
            Document snippetDoc = EditorFactory.getInstance().createDocument(codeSnippet);
            EditorTextField editorField = new EditorTextField(snippetDoc, project, fileType, true, false);

            editorField.setOneLineMode(false);

            // 动态计算面板高度，每行大约20像素，加上边距
            int editorHeight = Math.min(previewHeight, Math.max(100, lineCount * 22 + 30)); // 限制最小高度并使用设置中的最大高度
            editorField.setPreferredSize(new Dimension(650, editorHeight));

            // 创建包装面板，使用现代化设计
            JPanel mainPanel = new JPanel(new BorderLayout()) {
                // 重写绘制方法，添加圆角和阴影效果
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // 设置背景颜色（根据主题自适应）
                    g2.setColor(new JBColor(new Color(250, 250, 250), new Color(43, 43, 46)));

                    // 绘制圆角矩形
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                    // 绘制微妙的边框
                    g2.setColor(new JBColor(new Color(220, 220, 220, 100), new Color(70, 70, 70, 100)));
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                    g2.dispose();
                }
            };
            mainPanel.setOpaque(false);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0)); // 添加底部边距，为阴影留出空间

            // 创建标题面板
            JPanel titlePanel = new JPanel() {
                // 重写绘制方法，添加圆角效果
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // 设置背景颜色（根据主题自适应）
                    g2.setColor(new JBColor(new Color(60, 63, 65), new Color(40, 44, 52)));

                    // 绘制圆角矩形（只有上部圆角）
                    g2.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 12, 12);
                    g2.fillRect(0, getHeight() - 10, getWidth(), 10);

                    g2.dispose();
                }
            };
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
            titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
            titlePanel.setOpaque(false);

            // 文件名和行号信息
            String fileName = pin.filePath;
            int lastSlash = Math.max(pin.filePath.lastIndexOf('/'), pin.filePath.lastIndexOf('\\'));
            if (lastSlash >= 0) {
                fileName = pin.filePath.substring(lastSlash + 1);
            }

            // 创建文件名标签 - 使用现代化图标和颜色
            JLabel fileLabel = new JLabel("📄 " + fileName);
            fileLabel.setFont(fileLabel.getFont().deriveFont(Font.BOLD, 14.0f));
            // 亮色主题使用深色，暗色主题使用浅色
            fileLabel.setForeground(new JBColor(new Color(50, 120, 220), new Color(255, 203, 107)));

            // 创建行号标签 - 使用现代化颜色
            JLabel lineLabel = new JLabel(String.format(" (第 %d-%d 行)", startLine + 1, endLine + 1));
            lineLabel.setFont(lineLabel.getFont().deriveFont(13.0f));
            // 亮色主题使用深色，暗色主题使用浅色
            lineLabel.setForeground(new JBColor(new Color(200, 80, 40), new Color(247, 140, 108)));

            titlePanel.add(fileLabel);
            titlePanel.add(lineLabel);
            titlePanel.add(Box.createHorizontalGlue());

            // 添加标题面板
            mainPanel.add(titlePanel, BorderLayout.NORTH);

            // 创建代码编辑器面板
            JPanel editorPanel = new JPanel(new BorderLayout()) {
                // 重写绘制方法，添加圆角效果
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // 设置背景颜色（根据主题自适应）
                    g2.setColor(new JBColor(new Color(250, 250, 250), new Color(43, 43, 46)));

                    // 绘制圆角矩形（只有下部圆角）
                    g2.fillRoundRect(0, -10, getWidth(), getHeight() + 10, 12, 12);
                    g2.fillRect(0, 0, getWidth(), 10);

                    g2.dispose();
                }
            };
            editorPanel.setOpaque(false);
            editorPanel.setBorder(BorderFactory.createEmptyBorder(0, 1, 1, 1));
            editorPanel.add(editorField, BorderLayout.CENTER);
            mainPanel.add(editorPanel, BorderLayout.CENTER);

            // 设置编辑器属性
            SwingUtilities.invokeLater(() -> {
                EditorEx editor = (EditorEx) editorField.getEditor();
                if (editor != null) {
                    EditorSettings settings = editor.getSettings();
                    settings.setLineNumbersShown(true);
                    settings.setLineMarkerAreaShown(true);
                    settings.setFoldingOutlineShown(false);
                    settings.setAdditionalLinesCount(0); // 减少底部空白
                    settings.setAdditionalColumnsCount(0); // 减少右侧空白
                    editor.setHorizontalScrollbarVisible(true);
                    editor.setVerticalScrollbarVisible(true);

                    // 设置背景颜色 - 根据主题自适应
                    editor.setBackgroundColor(new JBColor(new Color(250, 250, 250), new Color(43, 43, 46)));
                }
            });

            // 创建弹窗 - 添加阴影和动画效果
            JBPopup popup = JBPopupFactory.getInstance()
                    .createComponentPopupBuilder(mainPanel, null)
                    .setResizable(true)
                    .setMovable(true)
                    .setRequestFocus(true)
                    .setCancelOnClickOutside(true)
                    .setCancelOnWindowDeactivation(true)
                    .setShowShadow(true) // 显示阴影
                    .setShowBorder(false) // 不显示边框，使用自定义边框
                    .setFocusable(true)
                    .createPopup();

            popup.showInFocusCenter();
            System.out.println("[CodePins] 成功显示代码预览，长度: " + codeSnippet.length());
        } catch (Exception e) {
            System.out.println("[CodePins] 预览代码时出错: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("预览代码时出错: " + e.getMessage());
        }
    }
}