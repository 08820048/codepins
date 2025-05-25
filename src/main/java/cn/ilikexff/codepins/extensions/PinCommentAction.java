package cn.ilikexff.codepins.extensions;

import cn.ilikexff.codepins.PinEntry;
import cn.ilikexff.codepins.PinStorage;
import cn.ilikexff.codepins.settings.CodePinsSettings;
import cn.ilikexff.codepins.ui.SimpleTagEditorDialog;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 注释标记动作
 * 在编辑器中检测特定格式的注释，并将其添加为图钉
 */
public class PinCommentAction extends AnAction {
    // 注释标记正则表达式，匹配 @cp: 或 @cp 后面的内容（也兼容原来的 @pin 指令）
    private static final Pattern PIN_PATTERN = Pattern.compile("@(cp|pin):?\\s*(.*)");
    
    // 代码块注释标记正则表达式，匹配 @cpb: 或 @cpb 后面的内容（也兼容原来的 @pin-block 指令）
    private static final Pattern PIN_BLOCK_PATTERN = Pattern.compile("@(cpb|pin[:-]block):?\\s*(.*)");
    
    // 带行号范围的代码块标记正则表达式，匹配 @cpb1-20 这样的格式
    private static final Pattern PIN_BLOCK_RANGE_PATTERN = Pattern.compile("@cpb(\\d+)-(\\d+)\\s*(.*)");
    
    // 是否显示调试通知
    private static final boolean SHOW_NOTIFICATIONS = false;

    /**
     * 扫描文件中的注释标记
     *
     * @param psiFile  PSI 文件
     * @param document 文档
     * @param project  项目
     */
    public void scanFile(PsiFile psiFile, Document document, Project project) {
        if (psiFile == null || document == null || project == null) {
            return;
        }
        
        // 查找文件中的所有注释
        PsiComment[] comments = PsiTreeUtil.findChildrenOfType(psiFile, PsiComment.class).toArray(new PsiComment[0]);
        
        // 检查每个注释
        for (PsiComment comment : comments) {
            checkComment(comment, document, project);
        }
    }
    
    /**
     * 扫描指定行的注释标记
     *
     * @param psiFile    PSI 文件
     * @param document   文档
     * @param project    项目
     * @param lineNumber 行号
     */
    public void scanLine(PsiFile psiFile, Document document, Project project, int lineNumber) {
        if (psiFile == null || document == null || project == null || lineNumber < 0 || lineNumber >= document.getLineCount()) {
            return;
        }
        
        // 获取指定行的起始和结束偏移量
        int startOffset = document.getLineStartOffset(lineNumber);
        int endOffset = document.getLineEndOffset(lineNumber);
        
        // 查找该行中的注释元素
        
        // 使用替代方法找到该行的注释元素
        PsiElement currentElement = psiFile.findElementAt(startOffset);
        while (currentElement != null && currentElement.getTextOffset() <= endOffset) {
            if (currentElement instanceof PsiComment) {
                checkComment((PsiComment) currentElement, document, project);
            }
            currentElement = PsiTreeUtil.nextLeaf(currentElement);
        }
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        
        if (editor == null || psiFile == null) {
            return;
        }

        // 显示通知，确认动作被触发
        Notifications.Bus.notify(new Notification(
                "CodePins",
                "CodePins 注释检测",
                "正在检测文件中的注释标记...",
                NotificationType.INFORMATION
        ));

        // 扫描文件
        scanFile(psiFile, editor.getDocument(), project);
        
        // 显示通知，报告检测完成
        Notifications.Bus.notify(new Notification(
                "CodePins",
                "CodePins 注释检测",
                "注释标记检测完成",
                NotificationType.INFORMATION
        ));
    }

    /**
     * 检查注释是否包含图钉标记
     *
     * @param comment  注释元素
     * @param document 文档
     * @param project  项目
     */
    private void checkComment(PsiComment comment, Document document, Project project) {
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
        
        // 获取设置
        CodePinsSettings settings = CodePinsSettings.getInstance();
        boolean useCompletionSymbol = settings.useCompletionSymbol;
        String completionSymbol = settings.completionSymbol;
        
        // 如果启用了完成指令符号，检查注释中是否包含完成符号
        // 如果没有包含完成符号，则不触发图钉添加
        // 这确保只有在用户输入完成符号或手动触发时才会创建图钉
        if (useCompletionSymbol && !completionSymbol.isEmpty() && !commentText.contains(completionSymbol)) {
            return;
        }
        
        // 检查是否是带行号范围的代码块标记
        Matcher blockRangeMatcher = PIN_BLOCK_RANGE_PATTERN.matcher(commentText);
        if (blockRangeMatcher.find()) {
            if (SHOW_NOTIFICATIONS) {
                // 显示通知，确认代码块标记被检测到
                Notifications.Bus.notify(new Notification(
                        "CodePins",
                        "CodePins 代码块标记",
                        "检测到带行号范围的代码块标记: " + blockRangeMatcher.group(3).trim(),
                        NotificationType.INFORMATION
                ));
            }
            
            // 提取行号范围
            int startLine = Integer.parseInt(blockRangeMatcher.group(1));
            int endLine = Integer.parseInt(blockRangeMatcher.group(2));
            
            // 提取备注内容，如果有完成符号，则去除完成符号
            String note = blockRangeMatcher.group(3).trim();
            if (useCompletionSymbol && !completionSymbol.isEmpty()) {
                note = note.replace(completionSymbol, "").trim();
            }
            
            // 处理带行号范围的代码块标记
            processBlockPinWithRange(comment, note, document, project, startLine, endLine);
            return;
        }
        
        // 检查是否是普通代码块标记
        Matcher blockMatcher = PIN_BLOCK_PATTERN.matcher(commentText);
        if (blockMatcher.find()) {
            if (SHOW_NOTIFICATIONS) {
                // 显示通知，确认代码块标记被检测到
                Notifications.Bus.notify(new Notification(
                        "CodePins",
                        "CodePins 代码块标记",
                        "检测到代码块标记: " + blockMatcher.group(2).trim(),
                        NotificationType.INFORMATION
                ));
            }
            
            // 提取备注内容，如果有完成符号，则去除完成符号
            String note = blockMatcher.group(2).trim();
            if (useCompletionSymbol && !completionSymbol.isEmpty()) {
                note = note.replace(completionSymbol, "").trim();
            }
            
            // 处理代码块标记
            processBlockPin(comment, note, document, project);
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
                        "检测到普通图钉标记: " + matcher.group(2).trim(),
                        NotificationType.INFORMATION
                ));
            }
            
            // 提取备注内容，如果有完成符号，则去除完成符号
            String note = matcher.group(2).trim();
            if (useCompletionSymbol && !completionSymbol.isEmpty()) {
                note = note.replace(completionSymbol, "").trim();
            }
            
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
    private void processSingleLinePin(PsiComment comment, String note, Document document, Project project) {
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
    private void processBlockPin(PsiComment comment, String note, Document document, Project project) {
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
     * 处理带行号范围的代码块标记
     *
     * @param comment   注释元素
     * @param note      备注内容
     * @param document  文档
     * @param project   项目
     * @param startLine 起始行号
     * @param endLine   结束行号
     */
    private void processBlockPinWithRange(PsiComment comment, String note, Document document, Project project, int startLine, int endLine) {
        // 获取文件
        VirtualFile file = comment.getContainingFile().getVirtualFile();
        if (file == null) {
            return;
        }
        
        // 调整行号范围，确保在文档范围内
        startLine = Math.max(0, startLine - 1); // 转换为基于0的行号
        endLine = Math.min(document.getLineCount() - 1, endLine - 1); // 转换为基于0的行号
        
        // 如果结束行小于起始行，则交换它们
        if (endLine < startLine) {
            int temp = startLine;
            startLine = endLine;
            endLine = temp;
        }
        
        // 获取起始和结束偏移量
        int startOffset = document.getLineStartOffset(startLine);
        int endOffset = document.getLineEndOffset(endLine);
        
        // 创建图钉
        createPinWithCheck(file, document, startOffset, endOffset, note, true, project);
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
    private void createPinWithCheck(VirtualFile file, Document document, int startOffset, int endOffset, String note, boolean isBlock, Project project) {
        // 获取设置
        CodePinsSettings settings = CodePinsSettings.getInstance();
        boolean showNoteDialog = settings.showNoteDialogOnCommentPin;
        boolean autoAddQuickTag = settings.autoAddQuickTag;
        // 准备创建图钉
        // 不显示调试通知，避免干扰用户体验
        if (SHOW_NOTIFICATIONS) {
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
            // 不显示警告通知，避免干扰用户体验
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
        // 默认不显示备注框和标签框，只有用户在设置中明确开启才会显示
        
        // 在 UI 线程中创建图钉
        ApplicationManager.getApplication().invokeLater(() -> {
            // 如果用户在设置中明确开启了显示备注框，则创建备注框
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
                
                // 如果设置了自动添加“快捷添加”标签，则添加该标签
                if (autoAddQuickTag) {
                    initialTags.add("快捷添加");
                }
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
                    
                    // 显示通知，确认图钉创建成功
                    if (SHOW_NOTIFICATIONS) {
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
                    // 如果设置了自动添加“快捷添加”标签，则创建带标签的图钉
                    if (autoAddQuickTag) {
                        List<String> quickTags = new ArrayList<>();
                        quickTags.add("快捷添加");
                        
                        // 创建图钉
                        PinEntry pinEntry = new PinEntry(
                                file.getPath(),
                                document.createRangeMarker(startOffset, endOffset),
                                note,
                                System.currentTimeMillis(),
                                System.getProperty("user.name"),
                                isBlock,
                                quickTags
                        );
                        PinStorage.addPin(pinEntry);
                        
                        if (SHOW_NOTIFICATIONS) {
                            // 显示通知，确认图钉创建成功
                            Notifications.Bus.notify(new Notification(
                                    "CodePins",
                                    "CodePins 图钉创建成功",
                                    "图钉已成功创建: " + pinEntry.note,
                                    NotificationType.INFORMATION
                            ));
                        }
                    } else {
                        // 显示通知，确认正在创建图钉
                        if (SHOW_NOTIFICATIONS) {
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
                            
                            // 显示通知，确认图钉创建成功
                            if (SHOW_NOTIFICATIONS) {
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
                    }
                });
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只在有编辑器和 PSI 文件时启用
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        
        e.getPresentation().setEnabledAndVisible(
                project != null && editor != null && psiFile != null
        );
    }
}
