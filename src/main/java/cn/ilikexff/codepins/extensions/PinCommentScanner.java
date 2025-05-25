package cn.ilikexff.codepins.extensions;

import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;
import cn.ilikexff.codepins.settings.CodePinsSettings;
import cn.ilikexff.codepins.ui.SimpleTagEditorDialog;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 注释标记扫描器
 * 用于扫描文件中的注释标记并创建图钉
 */
public class PinCommentScanner {
    // 注释标记正则表达式，匹配 @pin: 或 @pin 后面的内容
    private static final Pattern PIN_PATTERN = Pattern.compile("@pin:?\\s*(.*)");
    
    // 代码块注释标记正则表达式，匹配 @pin:block、@pin-block 或 @pin:block: 后面的内容
    private static final Pattern PIN_BLOCK_PATTERN = Pattern.compile("@pin[:-]block:?\\s*(.*)");
    
    // 是否显示通知
    private static final boolean SHOW_NOTIFICATIONS = false;

    /**
     * 扫描文件中的注释标记
     *
     * @param psiFile  PSI 文件
     * @param document 文档
     * @param project  项目
     */
    public static void scanFile(PsiFile psiFile, Document document, Project project) {
        if (psiFile == null || document == null || project == null) {
            return;
        }
        
        // 查找文件中的所有注释
        Collection<PsiComment> comments = PsiTreeUtil.findChildrenOfType(psiFile, PsiComment.class);
        
        if (SHOW_NOTIFICATIONS) {
            // 显示通知，报告找到的注释数量
            Notifications.Bus.notify(new Notification(
                    "CodePins",
                    "CodePins 注释检测",
                    "找到 " + comments.size() + " 个注释",
                    NotificationType.INFORMATION
            ));
        }

        // 检查每个注释
        for (PsiComment comment : comments) {
            checkComment(comment, document, project);
        }
    }

    /**
     * 检查注释是否包含图钉标记
     *
     * @param comment  注释元素
     * @param document 文档
     * @param project  项目
     */
    private static void checkComment(PsiComment comment, Document document, Project project) {
        String commentText = comment.getText();
        
        if (SHOW_NOTIFICATIONS) {
            // 显示通知，确认注释被检测到
            Notifications.Bus.notify(new Notification(
                    "CodePins",
                    "CodePins 注释检测",
                    "检测到注释: " + commentText,
                    NotificationType.INFORMATION
            ));
        }
        
        // 检查是否是代码块标记
        Matcher blockMatcher = PIN_BLOCK_PATTERN.matcher(commentText);
        if (blockMatcher.find()) {
            if (SHOW_NOTIFICATIONS) {
                // 显示通知，确认代码块标记被检测到
                Notifications.Bus.notify(new Notification(
                        "CodePins",
                        "CodePins 代码块标记",
                        "检测到代码块标记: " + blockMatcher.group(1).trim(),
                        NotificationType.INFORMATION
                ));
            }
            
            // 处理代码块标记
            processBlockPin(comment, blockMatcher.group(1).trim(), document, project);
            return;
        }
        
        // 检查是否是普通图钉标记
        Matcher matcher = PIN_PATTERN.matcher(commentText);
        if (matcher.find()) {
            if (SHOW_NOTIFICATIONS) {
                // 显示通知，确认普通图钉标记被检测到
                Notifications.Bus.notify(new Notification(
                        "CodePins",
                        "CodePins 普通图钉标记",
                        "检测到普通图钉标记: " + matcher.group(1).trim(),
                        NotificationType.INFORMATION
                ));
            }
            
            // 提取注释中的备注内容
            String note = matcher.group(1).trim();
            
            // 处理普通图钉标记
            processSingleLinePin(comment, note, document, project);
        }
    }

    /**
     * 处理普通图钉标记
     *
     * @param comment  注释元素
     * @param note     备注内容
     * @param document 文档
     * @param project  项目
     */
    private static void processSingleLinePin(PsiComment comment, String note, Document document, Project project) {
        // 获取注释所在行
        int lineNumber = document.getLineNumber(comment.getTextOffset());
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        int lineEndOffset = document.getLineEndOffset(lineNumber);
        
        // 获取文件
        VirtualFile file = comment.getContainingFile().getVirtualFile();
        if (file == null) {
            return;
        }
        
        // 创建图钉
        createPinWithCheck(file, document, lineStartOffset, lineEndOffset, note, false, project);
    }

    /**
     * 处理代码块标记
     *
     * @param comment  注释元素
     * @param note     备注内容
     * @param document 文档
     * @param project  项目
     */
    private static void processBlockPin(PsiComment comment, String note, Document document, Project project) {
        // 获取注释所在行
        int lineNumber = document.getLineNumber(comment.getTextOffset());
        
        // 获取文件
        VirtualFile file = comment.getContainingFile().getVirtualFile();
        if (file == null) {
            return;
        }
        
        // 尝试找到下一个元素
        PsiElement nextElement = comment.getNextSibling();
        while (nextElement instanceof PsiWhiteSpace) {
            nextElement = nextElement.getNextSibling();
        }
        
        if (nextElement == null) {
            // 如果没有下一个元素，则只标记当前行
            int lineStartOffset = document.getLineStartOffset(lineNumber);
            int lineEndOffset = document.getLineEndOffset(lineNumber);
            createPinWithCheck(file, document, lineStartOffset, lineEndOffset, note, false, project);
            return;
        }
        
        // 获取下一个元素的范围
        int blockStartOffset = document.getLineStartOffset(lineNumber + 1);
        int blockEndOffset;
        
        // 检查下一个元素是否可能是代码块
        String elementText = nextElement.getText();
        if (elementText != null && elementText.startsWith("{") && elementText.endsWith("}")) {
            // 可能是代码块，标记整个元素
            blockEndOffset = nextElement.getTextRange().getEndOffset();
        } else {
            // 否则标记到下一个元素的结束
            blockEndOffset = nextElement.getTextRange().getEndOffset();
        }
        
        // 创建图钉
        createPinWithCheck(file, document, blockStartOffset, blockEndOffset, note, true, project);
    }

    /**
     * 创建图钉，并检查是否已存在
     *
     * @param file         虚拟文件
     * @param document     文档
     * @param startOffset  起始偏移量
     * @param endOffset    结束偏移量
     * @param note         备注内容
     * @param isBlock      是否是代码块
     * @param project      项目
     */
    private static void createPinWithCheck(VirtualFile file, Document document, int startOffset, int endOffset, String note, boolean isBlock, Project project) {
        if (SHOW_NOTIFICATIONS) {
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
        }
        
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
            if (SHOW_NOTIFICATIONS) {
                Notifications.Bus.notify(new Notification(
                        "CodePins",
                        "CodePins 图钉已存在",
                        "该范围已有图钉，不重复添加",
                        NotificationType.WARNING
                ));
            }
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
                    
                    if (SHOW_NOTIFICATIONS) {
                        // 显示通知，确认图钉创建成功
                        Notifications.Bus.notify(new Notification(
                                "CodePins",
                                "CodePins 图钉创建成功",
                                "图钉已成功创建: " + confirmedNote,
                                NotificationType.INFORMATION
                        ));
                    }
                });
            } else {
                // 直接创建图钉，不显示备注框和标签框
                // 在写入操作中创建图钉，确保线程安全
                ApplicationManager.getApplication().runWriteAction(() -> {
                    if (SHOW_NOTIFICATIONS) {
                        // 显示通知，确认正在创建图钉
                        Notifications.Bus.notify(new Notification(
                                "CodePins",
                                "CodePins 正在创建图钉",
                                "正在创建图钉...",
                                NotificationType.INFORMATION
                        ));
                    }
                    
                    try {
                        // 创建图钉
                        PinEntry pin = PinEntry.createPin(project, file.getPath(), document, startOffset, endOffset, note, isBlock);
                        
                        if (SHOW_NOTIFICATIONS) {
                            // 显示通知，确认图钉创建成功
                            Notifications.Bus.notify(new Notification(
                                    "CodePins",
                                    "CodePins 图钉创建成功",
                                    "图钉已成功创建: " + pin.note,
                                    NotificationType.INFORMATION
                            ));
                        }
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
