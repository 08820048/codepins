package cn.ilikexff.codepins.extensions;

import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;
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
    // 同时支持标签语法：@cp 备注内容 #标签名
    private static final Pattern PIN_PATTERN = Pattern.compile("@(cp|pin):?\\s+([^#]*)(?:\\s+#[\\w\\u4e00-\\u9fa5]+)*");
    
    // 代码块注释标记正则表达式，匹配 @cpb: 或 @cpb 后面的内容（也兼容原来的 @pin-block 指令）
    // 同时支持标签语法：@cpb 备注内容 #标签名
    private static final Pattern PIN_BLOCK_PATTERN = Pattern.compile("@(cpb|pin[:-]block):?\\s+([^#]*)(?:\\s+#[\\w\\u4e00-\\u9fa5]+)*");
    
    // 带行号范围的代码块标记正则表达式，匹配 @cpb1-20 这样的格式
    // 同时支持标签语法：@cpb1-20 备注内容 #标签名
    private static final Pattern PIN_BLOCK_RANGE_PATTERN = Pattern.compile("@cpb(\\d+)-(\\d+)\\s+([^#]*)(?:\\s+#[\\w\\u4e00-\\u9fa5]+)*");
    
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
     * 从注释文本中提取标签
     * 
     * @param commentText 注释文本
     * @return 提取的标签列表
     */
    private List<String> extractTags(String commentText) {
        List<String> tags = new ArrayList<>();
        Pattern tagPattern = Pattern.compile("#([\\w\\u4e00-\\u9fa5]+)");
        Matcher tagMatcher = tagPattern.matcher(commentText);
        
        while (tagMatcher.find()) {
            tags.add(tagMatcher.group(1));
        }
        
        return tags;
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
        
        // 提取标签
        List<String> tags = extractTags(commentText);
        
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
            processBlockPinWithRange(comment, note, document, project, startLine, endLine, tags);
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
            processBlockPin(comment, note, document, project, tags);
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
            processSingleLinePin(comment, note, document, project, tags);
        }
    }

    /**
     * 处理普通图钉标记
     *
     * @param comment  注释元素
     * @param note     备注内容
     * @param document 文档
     * @param project  项目
     * @param tags     标签列表
     */
    private void processSingleLinePin(PsiComment comment, String note, Document document, Project project, List<String> tags) {
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
        createPinWithCheck(file, document, lineStartOffset, lineEndOffset, note, false, project, tags);
    }

    /**
     * 处理代码块标记
     *
     * @param comment  注释元素
     * @param note     备注内容
     * @param document 文档
     * @param project  项目
     * @param tags     标签列表
     */
    private void processBlockPin(PsiComment comment, String note, Document document, Project project, List<String> tags) {
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
            createPinWithCheck(file, document, lineStartOffset, lineEndOffset, note, false, project, tags);
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
        createPinWithCheck(file, document, blockStartOffset, blockEndOffset, note, true, project, tags);
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
     * @param tags      标签列表
     */
    private void processBlockPinWithRange(PsiComment comment, String note, Document document, Project project, int startLine, int endLine, List<String> tags) {
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
        createPinWithCheck(file, document, startOffset, endOffset, note, true, project, tags);
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
     * @param tags         标签列表
     */
    private void createPinWithCheck(VirtualFile file, Document document, int startOffset, int endOffset, String note, boolean isBlock, Project project, List<String> tags) {
        // 获取设置
        CodePinsSettings settings = CodePinsSettings.getInstance();
        // 使用注释指令添加图钉时显示备注框和标签对话框的设置
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
        
        // 准备标签列表
        final List<String> initialTags = new ArrayList<>();
        
        // 如果有指令中的标签，先添加这些标签
        if (tags != null && !tags.isEmpty()) {
            initialTags.addAll(tags);
        }
        
        // 如果设置了自动添加"快捷添加"标签，则添加该标签
        if (autoAddQuickTag) {
            initialTags.add("快捷添加");
        }
        
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
                    // 显示通知，确认正在创建图钉
                    // 始终显示这个通知，不受 SHOW_NOTIFICATIONS 控制
                    Notifications.Bus.notify(new Notification(
                            "CodePins",
                            "CodePins 图钉已添加",
                            "根据注释指令自动添加了图钉: " + note,
                            NotificationType.INFORMATION
                    ));
                    
                    // 调试通知，只在开启调试模式时显示
                    if (SHOW_NOTIFICATIONS) {
                        Notifications.Bus.notify(new Notification(
                                "CodePins",
                                "CodePins 正在创建图钉",
                                "正在创建图钉...",
                                NotificationType.INFORMATION
                        ));
                    }
                    
                    try {
                        // 创建图钉，使用从注释指令中提取的标签
                        PinEntry pinEntry = new PinEntry(
                                file.getPath(),
                                document.createRangeMarker(startOffset, endOffset),
                                note,
                                System.currentTimeMillis(),
                                System.getProperty("user.name"),
                                isBlock,
                                initialTags  // 使用初始标签列表，包含从注释中提取的标签和可能的快捷添加标签
                        );
                        PinStorage.addPin(pinEntry);
                        
                        // 不再显示成功创建的通知，因为我们已经在开始时显示了通知
                        // 调试通知，只在开启调试模式时显示
                        if (SHOW_NOTIFICATIONS) {
                            Notifications.Bus.notify(new Notification(
                                    "CodePins",
                                    "CodePins 图钉创建成功",
                                    "图钉已成功创建: " + pinEntry.note,
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
