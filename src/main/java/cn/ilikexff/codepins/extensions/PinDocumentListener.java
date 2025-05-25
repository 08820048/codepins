package cn.ilikexff.codepins.extensions;

import cn.ilikexff.codepins.PinEntry;
import cn.ilikexff.codepins.PinStorage;
import cn.ilikexff.codepins.settings.CodePinsSettings;
import cn.ilikexff.codepins.ui.SimpleTagEditorDialog;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文档监听器，用于检测注释标记
 */
public class PinDocumentListener implements DocumentListener {
    // 注释标记正则表达式，匹配 @pin: 或 @pin 后面的内容
    private static final Pattern PIN_PATTERN = Pattern.compile("@pin:?\\s*(.*)");
    
    // 代码块注释标记正则表达式，匹配 @pin:block、@pin-block 或 @pin:block: 后面的内容
    private static final Pattern PIN_BLOCK_PATTERN = Pattern.compile("@pin[:-]block:?\\s*(.*)");
    
    private final Project project;
    private final Document document;

    /**
     * 构造函数
     *
     * @param project  项目
     * @param document 文档
     */
    public PinDocumentListener(Project project, Document document) {
        this.project = project;
        this.document = document;
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        // 获取变化的文本
        String newText = event.getNewFragment().toString();
        
        // 显示通知，确认文档变化被检测到
        Notifications.Bus.notify(new Notification(
                "CodePins",
                "CodePins 文档变化",
                "检测到文档变化: " + newText,
                NotificationType.INFORMATION
        ));
        
        // 检查是否包含注释标记
        checkForPinMarkers(newText, event.getOffset());
    }
    
    /**
     * 检查文本是否包含注释标记
     *
     * @param text   文本
     * @param offset 偏移量
     */
    private void checkForPinMarkers(String text, int offset) {
        // 检查是否是代码块标记
        Matcher blockMatcher = PIN_BLOCK_PATTERN.matcher(text);
        if (blockMatcher.find()) {
            // 显示通知，确认代码块标记被检测到
            Notifications.Bus.notify(new Notification(
                    "CodePins",
                    "CodePins 代码块标记",
                    "检测到代码块标记: " + blockMatcher.group(1).trim(),
                    NotificationType.INFORMATION
            ));
            
            // 处理代码块标记
            processBlockPin(blockMatcher.group(1).trim(), offset);
            return;
        }
        
        // 检查是否是普通图钉标记
        Matcher matcher = PIN_PATTERN.matcher(text);
        if (matcher.find()) {
            // 显示通知，确认普通图钉标记被检测到
            Notifications.Bus.notify(new Notification(
                    "CodePins",
                    "CodePins 普通图钉标记",
                    "检测到普通图钉标记: " + matcher.group(1).trim(),
                    NotificationType.INFORMATION
            ));
            
            // 处理普通图钉标记
            processSingleLinePin(matcher.group(1).trim(), offset);
        }
    }
    
    /**
     * 处理普通图钉标记
     *
     * @param note   备注内容
     * @param offset 偏移量
     */
    private void processSingleLinePin(String note, int offset) {
        // 获取文件
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null) {
            return;
        }
        
        // 获取偏移量所在行
        int lineNumber = document.getLineNumber(offset);
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        int lineEndOffset = document.getLineEndOffset(lineNumber);
        
        // 创建图钉
        createPinWithCheck(file, lineStartOffset, lineEndOffset, note, false);
    }
    
    /**
     * 处理代码块标记
     *
     * @param note   备注内容
     * @param offset 偏移量
     */
    private void processBlockPin(String note, int offset) {
        // 获取文件
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null) {
            return;
        }
        
        // 获取偏移量所在行
        int lineNumber = document.getLineNumber(offset);
        
        // 尝试找到下一个代码块的开始和结束
        int blockStartLine = lineNumber + 1;
        if (blockStartLine >= document.getLineCount()) {
            // 如果没有下一行，则只标记当前行
            int lineStartOffset = document.getLineStartOffset(lineNumber);
            int lineEndOffset = document.getLineEndOffset(lineNumber);
            createPinWithCheck(file, lineStartOffset, lineEndOffset, note, false);
            return;
        }
        
        // 获取下一行的文本
        int blockStartOffset = document.getLineStartOffset(blockStartLine);
        String nextLineText = document.getText().substring(
                blockStartOffset,
                Math.min(blockStartOffset + 100, document.getTextLength())
        );
        
        // 检查是否是代码块的开始
        if (nextLineText.trim().startsWith("{")) {
            // 尝试找到代码块的结束
            int openBraces = 1;
            int blockEndLine = blockStartLine;
            
            for (int i = blockStartLine + 1; i < document.getLineCount() && openBraces > 0; i++) {
                int lineStartOffset = document.getLineStartOffset(i);
                int lineEndOffset = document.getLineEndOffset(i);
                String lineText = document.getText().substring(lineStartOffset, lineEndOffset);
                
                for (char c : lineText.toCharArray()) {
                    if (c == '{') openBraces++;
                    if (c == '}') openBraces--;
                    
                    if (openBraces == 0) {
                        blockEndLine = i;
                        break;
                    }
                }
                
                if (openBraces == 0) break;
            }
            
            // 创建图钉
            int blockEndOffset = document.getLineEndOffset(blockEndLine);
            createPinWithCheck(file, blockStartOffset, blockEndOffset, note, true);
        } else {
            // 如果不是代码块，则标记下一行
            int nextLineEndOffset = document.getLineEndOffset(blockStartLine);
            createPinWithCheck(file, blockStartOffset, nextLineEndOffset, note, false);
        }
    }
    
    /**
     * 创建图钉，并检查是否已存在
     *
     * @param file         虚拟文件
     * @param startOffset  起始偏移量
     * @param endOffset    结束偏移量
     * @param note         备注内容
     * @param isBlock      是否是代码块
     */
    private void createPinWithCheck(VirtualFile file, int startOffset, int endOffset, String note, boolean isBlock) {
        // 显示通知，确认准备创建图钉
        Notifications.Bus.notify(new Notification(
                "CodePins",
                "CodePins 准备创建图钉",
                "文件: " + file.getPath() + "\n" +
                "偏移量: " + startOffset + "-" + endOffset + "\n" +
                "备注: " + note + "\n" +
                "是否代码块: " + isBlock,
                NotificationType.INFORMATION
        ));
        
        // 检查该范围是否已有图钉
        boolean hasPinInRange = PinStorage.getPins().stream()
                .filter(pin -> pin.filePath.equals(file.getPath()))
                .anyMatch(pin -> {
                    int pinStartOffset = pin.marker.getStartOffset();
                    int pinEndOffset = pin.marker.getEndOffset();
                    // 检查是否有重叠
                    return (pinStartOffset <= endOffset && pinEndOffset >= startOffset);
                });
        
        // 如果该范围已有图钉，不重复添加
        if (hasPinInRange) {
            Notifications.Bus.notify(new Notification(
                    "CodePins",
                    "CodePins 图钉已存在",
                    "该范围已有图钉，不重复添加",
                    NotificationType.WARNING
            ));
            return;
        }
        
        // 检查用户设置，决定是否显示备注框和标签框
        boolean showNoteDialog = CodePinsSettings.getInstance().showNoteDialogOnQuickAdd;
        
        // 在 UI 线程中创建图钉
        ApplicationManager.getApplication().invokeLater(() -> {
            if (showNoteDialog) {
                // 显示备注框和标签框
                // 注释中已经有备注了，所以这里只请求用户确认或修改
                String confirmedNote = Messages.showInputDialog(
                        project,
                        "请确认或修改图钉备注：",
                        "添加图钉",
                        null,
                        note,
                        null
                );

                // 如果用户取消了输入，不添加图钉
                if (confirmedNote == null) {
                    return;
                }

                // 创建标签对话框，请求用户输入标签
                final List<String> initialTags = new ArrayList<>();
                SimpleTagEditorDialog tagDialog = new SimpleTagEditorDialog(project, new PinEntry(
                        file.getPath(),
                        document.createRangeMarker(0, 0), // 临时标记，仅用于对话框
                        confirmedNote,
                        System.currentTimeMillis(),
                        System.getProperty("user.name"),
                        isBlock,
                        initialTags
                ));

                // 声明最终使用的标签列表
                final List<String> finalTags = tagDialog.showAndGet() ? tagDialog.getTags() : initialTags;

                // 在写入操作中添加图钉，确保线程安全
                ApplicationManager.getApplication().runWriteAction(() -> {
                    // 添加图钉
                    PinEntry pinEntry = new PinEntry(
                            file.getPath(),
                            document.createRangeMarker(startOffset, endOffset),
                            confirmedNote,
                            System.currentTimeMillis(),
                            System.getProperty("user.name"),
                            isBlock,
                            finalTags
                    );
                    PinStorage.addPin(pinEntry);
                });
            } else {
                // 直接创建图钉，不显示备注框和标签框
                // 在写入操作中创建图钉，确保线程安全
                ApplicationManager.getApplication().runWriteAction(() -> {
                    // 显示通知，确认正在创建图钉
                    Notifications.Bus.notify(new Notification(
                            "CodePins",
                            "CodePins 正在创建图钉",
                            "正在创建图钉...",
                            NotificationType.INFORMATION
                    ));
                    
                    try {
                        // 创建图钉
                        PinEntry pin = PinEntry.createPin(project, file.getPath(), document, startOffset, endOffset, note, isBlock);
                        
                        // 显示通知，确认图钉创建成功
                        Notifications.Bus.notify(new Notification(
                                "CodePins",
                                "CodePins 图钉创建成功",
                                "图钉已成功创建: " + pin.note,
                                NotificationType.INFORMATION
                        ));
                    } catch (Exception e) {
                        // 显示通知，报告错误
                        Notifications.Bus.notify(new Notification(
                                "CodePins",
                                "CodePins 图钉创建失败",
                                "创建图钉时发生错误: " + e.getMessage(),
                                NotificationType.ERROR
                        ));
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}
