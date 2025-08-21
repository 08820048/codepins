package cn.ilikexff.codepins.extensions;

import cn.ilikexff.codepins.settings.CodePinsSettings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

// 不需要这些导入，因为我们直接使用PinCommentAction类来处理注释标记

/**
 * 完成符号监听器
 * 监听文档变化，检测是否输入了完成符号
 */
public class PinCompletionSymbolListener implements DocumentListener {
    // 注释：我们不需要在这里定义正则表达式，因为我们直接使用PinCommentAction类来处理注释标记
    
    // 是否显示调试通知
    private static final boolean SHOW_NOTIFICATIONS = false;
    private final Project project;
    private final Document document;

    /**
     * 构造函数
     *
     * @param project  项目
     * @param document 文档
     */
    public PinCompletionSymbolListener(Project project, Document document) {
        this.project = project;
        this.document = document;
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        // 获取设置
        CodePinsSettings settings = CodePinsSettings.getInstance();

        // 如果没有启用完成指令符号，则不处理
        if (!settings.useCompletionSymbol) {
            return;
        }

        // 获取完成符号
        String completionSymbol = settings.completionSymbol;
        if (completionSymbol == null || completionSymbol.isEmpty()) {
            return;
        }

        // 获取变化的文本
        String newText = event.getNewFragment().toString();

        // 检查是否输入了完成符号
        if (!newText.contains(completionSymbol)) {
            return;
        }

        // 使用ReadAction包装所有文档和PSI访问操作
        com.intellij.openapi.application.ReadAction.nonBlocking(() -> {
            try {
                // 获取当前光标所在的偏移量
                int offset = event.getOffset();

                // 获取当前行号
                int lineNumber = document.getLineNumber(offset);

                if (SHOW_NOTIFICATIONS) {
                    // 显示通知，确认完成符号被检测到
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                        Notifications.Bus.notify(new Notification(
                                "CodePins",
                                "CodePins 完成符号检测",
                                "检测到完成符号: " + completionSymbol,
                                NotificationType.INFORMATION
                        ));
                    });
                }

                // 获取文件
                VirtualFile file = FileDocumentManager.getInstance().getFile(document);
                if (file == null) {
                    return null;
                }

                // 获取 PSI 文件
                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
                if (psiFile == null) {
                    return null;
                }

                // 在EDT线程上执行注释检测
                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                    // 使用注释检测器处理当前行的指令
                    // 这样只会触发当前行的指令，而不是扫描整个文件
                    PinCommentAction commentAction = new PinCommentAction();
                    commentAction.scanLine(psiFile, document, project, lineNumber);

                    if (SHOW_NOTIFICATIONS) {
                        // 显示通知，确认文件扫描完成
                        Notifications.Bus.notify(new Notification(
                                "CodePins",
                                "CodePins 文件扫描",
                                "文件扫描完成",
                                NotificationType.INFORMATION
                        ));
                    }
                });

                return null;
            } catch (Exception e) {
                System.err.println("[CodePins] 完成符号监听器异常: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }).finishOnUiThread(com.intellij.openapi.application.ModalityState.defaultModalityState(), (result) -> {
            // 完成后的回调，这里不需要做任何事情
        });
    }
}
