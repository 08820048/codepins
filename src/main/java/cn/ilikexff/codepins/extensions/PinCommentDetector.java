package cn.ilikexff.codepins.extensions;

import cn.ilikexff.codepins.PinEntry;
import cn.ilikexff.codepins.PinStorage;
import cn.ilikexff.codepins.settings.CodePinsSettings;
import cn.ilikexff.codepins.ui.SimpleTagEditorDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
 * 注释标记自动识别
 * 识别特定格式的注释，自动将其添加为图钉
 */
public class PinCommentDetector implements PsiTreeChangeListener {
    // 注释标记正则表达式，匹配 @pin: 或 @pin 后面的内容
    private static final Pattern PIN_PATTERN = Pattern.compile("@pin:?\\s*(.*)");
    
    // 代码块注释标记正则表达式，匹配 @pin:block、@pin-block 或 @pin:block: 后面的内容
    private static final Pattern PIN_BLOCK_PATTERN = Pattern.compile("@pin[:-]block:?\\s*(.*)");
    
    private final Project project;

    /**
     * 构造函数
     *
     * @param project 项目
     */
    public PinCommentDetector(Project project) {
        this.project = project;
    }

    /**
     * 安装注释标记检测器
     *
     * @param project 项目
     */
    public static void installOn(Project project) {
        PsiManager.getInstance(project).addPsiTreeChangeListener(
                new PinCommentDetector(project),
                project
        );
    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent event) {
        processEvent(event);
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        processEvent(event);
    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        processEvent(event);
    }

    /**
     * 处理 PSI 树变化事件
     *
     * @param event PSI 树变化事件
     */
    private void processEvent(PsiTreeChangeEvent event) {
        PsiElement element = event.getChild();
        if (element == null) {
            element = event.getParent();
        }
        
        if (element == null) {
            return;
        }

        // 检查是否是注释元素
        if (element instanceof PsiComment) {
            checkComment((PsiComment) element);
        } else {
            // 查找元素中的所有注释
            PsiComment[] comments = PsiTreeUtil.findChildrenOfType(element, PsiComment.class).toArray(new PsiComment[0]);
            for (PsiComment comment : comments) {
                checkComment(comment);
            }
        }
    }

    /**
     * 检查注释是否包含图钉标记
     *
     * @param comment 注释元素
     */
    private void checkComment(PsiComment comment) {
        String commentText = comment.getText();
        
        // 检查是否是代码块标记
        Matcher blockMatcher = PIN_BLOCK_PATTERN.matcher(commentText);
        if (blockMatcher.find()) {
            // 处理代码块标记
            processBlockPin(comment, blockMatcher.group(1).trim());
            return;
        }
        
        // 检查是否是普通图钉标记
        Matcher matcher = PIN_PATTERN.matcher(commentText);
        if (matcher.find()) {
            // 提取注释中的备注内容
            String note = matcher.group(1).trim();
            
            // 获取注释所在文件
            PsiFile file = comment.getContainingFile();
            if (file == null) {
                return;
            }
            
            VirtualFile virtualFile = file.getVirtualFile();
            if (virtualFile == null) {
                return;
            }
            
            // 获取文档
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if (document == null) {
                return;
            }
            
            // 获取注释的文本范围
            int startOffset = comment.getTextRange().getStartOffset();
            
            // 获取注释所在行
            int lineNumber = document.getLineNumber(startOffset);
            int lineStartOffset = document.getLineStartOffset(lineNumber);
            int lineEndOffset = document.getLineEndOffset(lineNumber);
            
            // 使用通用方法创建图钉
            createPinWithCheck(virtualFile, document, lineStartOffset, lineEndOffset, note, false);
        }
    }

    /**
     * 处理代码块图钉标记
     *
     * @param comment 注释元素
     * @param note    备注内容
     */
    private void processBlockPin(PsiComment comment, String note) {
        // 获取注释所在文件
        PsiFile file = comment.getContainingFile();
        if (file == null) {
            return;
        }
        
        VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null) {
            return;
        }
        
        // 获取文档
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document == null) {
            return;
        }
        
        // 获取注释的文本范围
        int startOffset = comment.getTextRange().getStartOffset();
        
        // 获取注释所在行
        int lineNumber = document.getLineNumber(startOffset);
        
        // 尝试获取下一个非注释元素，以确定代码块的范围
        PsiElement nextElement = comment.getNextSibling();
        while (nextElement != null && (nextElement instanceof PsiComment || nextElement instanceof PsiWhiteSpace)) {
            nextElement = nextElement.getNextSibling();
        }
        
        if (nextElement == null) {
            // 如果没有下一个元素，则只标记注释所在行
            int lineStartOffset = document.getLineStartOffset(lineNumber);
            int lineEndOffset = document.getLineEndOffset(lineNumber);
            createPinWithCheck(virtualFile, document, lineStartOffset, lineEndOffset, note, false);
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
        
        // 创建图钉，并检查是否已存在
        createPinWithCheck(virtualFile, document, blockStartOffset, blockEndOffset, note, true);
    }
    
    /**
     * 创建图钉，并检查是否已存在
     *
     * @param virtualFile  虚拟文件
     * @param document     文档
     * @param startOffset  起始偏移量
     * @param endOffset    结束偏移量
     * @param note         备注内容
     * @param isBlock      是否是代码块
     */
    private void createPinWithCheck(VirtualFile virtualFile, Document document, int startOffset, int endOffset, String note, boolean isBlock) {
        // 检查该范围是否已有图钉
        boolean hasPinInRange = PinStorage.getPins().stream()
                .filter(pin -> pin.filePath.equals(virtualFile.getPath()))
                .anyMatch(pin -> {
                    int pinStartOffset = pin.marker.getStartOffset();
                    int pinEndOffset = pin.marker.getEndOffset();
                    // 检查是否有重叠
                    return (pinStartOffset <= endOffset && pinEndOffset >= startOffset);
                });
        
        // 如果该范围已有图钉，不重复添加
        if (hasPinInRange) {
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
                        virtualFile.getPath(),
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
                            virtualFile.getPath(),
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
                    // 创建图钉
                    PinEntry.createPin(project, virtualFile.getPath(), document, startOffset, endOffset, note, isBlock);
                });
            }
        });
    }
    
    // 以下是未使用的接口方法实现
    @Override
    public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
    }
}
