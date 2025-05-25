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
            
            // 检查该行是否已有图钉
            boolean hasPinAtLine = PinStorage.getPins().stream()
                    .filter(pin -> pin.filePath.equals(virtualFile.getPath()))
                    .anyMatch(pin -> {
                        int pinLine = document.getLineNumber(pin.marker.getStartOffset());
                        return pinLine == lineNumber;
                    });
            
            // 如果该行已有图钉，不重复添加
            if (hasPinAtLine) {
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
                            false,
                            initialTags
                    ));

                    // 声明最终使用的标签列表
                    final List<String> finalTags = tagDialog.showAndGet() ? tagDialog.getTags() : initialTags;

                    // 在写入操作中添加图钉，确保线程安全
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        // 添加图钉
                        PinEntry pinEntry = new PinEntry(
                                virtualFile.getPath(),
                                document.createRangeMarker(lineStartOffset, lineEndOffset),
                                confirmedNote,
                                System.currentTimeMillis(),
                                System.getProperty("user.name"),
                                false,
                                finalTags
                        );
                        PinStorage.addPin(pinEntry);
                    });
                } else {
                    // 直接创建图钉，不显示备注框和标签框
                    // 在写入操作中创建图钉，确保线程安全
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        // 创建图钉
                        PinEntry.createPin(project, virtualFile.getPath(), document, lineStartOffset, lineEndOffset, note, false);
                    });
                }
            });
        }
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
