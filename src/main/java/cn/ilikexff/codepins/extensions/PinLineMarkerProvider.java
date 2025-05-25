package cn.ilikexff.codepins.extensions;

import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;


/**
 * 图钉行标记提供者
 * 在编辑器行号槽区域显示图钉图标，用户可以直接点击添加图钉
 */
public class PinLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        // 只处理叶子节点，以确保每行只有一个标记
        if (element.getFirstChild() == null) {
            // 继续处理
        } else {
            return null;
        }

        PsiFile file = element.getContainingFile();
        if (file == null) {
            return null;
        }

        VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }

        Project project = element.getProject();
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document == null) {
            return null;
        }

        int lineNumber = document.getLineNumber(element.getTextOffset());
        
        // 检查该行是否已有图钉
        boolean hasPinAtLine = PinStorage.getPins().stream()
                .filter(pin -> pin.filePath.equals(virtualFile.getPath()))
                .anyMatch(pin -> {
                    int pinLine = document.getLineNumber(pin.marker.getStartOffset());
                    return pinLine == lineNumber;
                });

        // 创建行标记信息
        return new LineMarkerInfo<>(
                element,
                element.getTextRange(),
                hasPinAtLine ? getPinIcon(true) : getPinIcon(false),
                psiElement -> hasPinAtLine ? "移除图钉" : "添加图钉",
                (e, elt) -> {
                    if (hasPinAtLine) {
                        removePinAtLine(project, document, virtualFile, lineNumber);
                    } else {
                        addPinAtLine(project, document, virtualFile, lineNumber);
                    }
                },
                GutterIconRenderer.Alignment.LEFT,
                () -> "CodePin"
        );
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
        // 不需要收集慢速行标记
    }

    /**
     * 获取图钉图标
     *
     * @param isPinned 是否已添加图钉
     * @return 图标
     */
    private Icon getPinIcon(boolean isPinned) {
        return isPinned
                ? IconLoader.getIcon("/icons/pin-filled.svg", PinLineMarkerProvider.class)
                : IconLoader.getIcon("/icons/pin-outline.svg", PinLineMarkerProvider.class);
    }

    /**
     * 在指定行添加图钉
     *
     * @param project     项目
     * @param document    文档
     * @param virtualFile 虚拟文件
     * @param lineNumber  行号
     */
    private void addPinAtLine(Project project, Document document, VirtualFile virtualFile, int lineNumber) {
        int startOffset = document.getLineStartOffset(lineNumber);
        int endOffset = document.getLineEndOffset(lineNumber);

        // 创建图钉
        PinEntry.createPin(project, virtualFile.getPath(), document, startOffset, endOffset, "", false);

        // 刷新行标记
        refreshLineMarkers(project, virtualFile);
    }

    /**
     * 移除指定行的图钉
     *
     * @param project     项目
     * @param document    文档
     * @param virtualFile 虚拟文件
     * @param lineNumber  行号
     */
    private void removePinAtLine(Project project, Document document, VirtualFile virtualFile, int lineNumber) {
        int offset = document.getLineStartOffset(lineNumber);

        // 查找并移除图钉
        PinStorage.getPins().stream()
                .filter(pin -> pin.filePath.equals(virtualFile.getPath()) &&
                        pin.marker.getStartOffset() <= offset &&
                        pin.marker.getEndOffset() >= offset)
                .findFirst()
                .ifPresent(PinStorage::removePin);

        // 刷新行标记
        refreshLineMarkers(project, virtualFile);
    }

    /**
     * 刷新行标记
     *
     * @param project     项目
     * @param virtualFile 虚拟文件
     */
    private void refreshLineMarkers(Project project, VirtualFile virtualFile) {
        // 获取PSI文件
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile != null) {
            // 请求重新计算行标记
            com.intellij.codeInsight.daemon.DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
        }
    }
}
