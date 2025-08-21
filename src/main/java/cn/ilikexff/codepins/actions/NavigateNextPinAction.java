package cn.ilikexff.codepins.actions;

import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 导航到下一个图钉的快捷键 Action
 */
public class NavigateNextPinAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 获取所有图钉
        List<PinEntry> pins = PinStorage.getPins();
        if (pins.isEmpty()) {
            return;
        }

        // 获取当前编辑器和文件
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        VirtualFile currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        
        // 如果没有打开的编辑器，直接导航到第一个图钉
        if (editor == null || currentFile == null) {
            navigateToPin(project, pins.get(0));
            return;
        }

        // 获取当前光标位置
        int currentOffset = editor.getCaretModel().getOffset();
        String currentFilePath = currentFile.getPath();

        // 使用ReadAction包装查找操作
        com.intellij.openapi.application.ReadAction.run(() -> {
            try {
                // 查找下一个图钉
                PinEntry nextPin = findNextPin(pins, currentFilePath, currentOffset);
                if (nextPin != null) {
                    navigateToPin(project, nextPin);
                }
            } catch (Exception ex) {
                System.err.println("[CodePins] 查找下一个图钉异常: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    /**
     * 查找下一个图钉
     * 
     * @param pins 所有图钉
     * @param currentFilePath 当前文件路径
     * @param currentOffset 当前光标位置
     * @return 下一个图钉，如果没有则返回第一个图钉
     */
    private PinEntry findNextPin(List<PinEntry> pins, String currentFilePath, int currentOffset) {
        PinEntry firstPin = null;
        PinEntry nextPin = null;
        
        // 首先查找同一文件中的下一个图钉
        for (PinEntry pin : pins) {
            // 记录第一个图钉，用于循环导航
            if (firstPin == null) {
                firstPin = pin;
            }
            
            // 如果是同一个文件，且位置在当前光标之后
            if (pin.filePath.equals(currentFilePath) && pin.marker.getStartOffset() > currentOffset) {
                nextPin = pin;
                break;
            }
        }
        
        // 如果同一文件中没有找到下一个图钉，查找其他文件中的图钉
        if (nextPin == null) {
            for (PinEntry pin : pins) {
                if (!pin.filePath.equals(currentFilePath)) {
                    nextPin = pin;
                    break;
                }
            }
        }
        
        // 如果仍然没有找到下一个图钉，返回第一个图钉（循环导航）
        return nextPin != null ? nextPin : firstPin;
    }
    
    /**
     * 导航到指定图钉
     *
     * @param project 项目
     * @param pin 图钉
     */
    private void navigateToPin(Project project, PinEntry pin) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(pin.filePath);
        if (file != null && file.exists()) {
            // 使用ReadAction包装marker访问操作
            com.intellij.openapi.application.ReadAction.run(() -> {
                try {
                    int startOffset = pin.marker.getStartOffset();
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                        OpenFileDescriptor descriptor = new OpenFileDescriptor(
                                project,
                                file,
                                startOffset
                        );
                        FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
                    });
                } catch (Exception ex) {
                    System.err.println("[CodePins] 导航到图钉异常: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只有在有项目打开时才启用此操作
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null && !PinStorage.getPins().isEmpty());
    }
}
