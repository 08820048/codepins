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
 * 导航到上一个图钉的快捷键 Action
 */
public class NavigatePrevPinAction extends AnAction {

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
        
        // 如果没有打开的编辑器，直接导航到最后一个图钉
        if (editor == null || currentFile == null) {
            navigateToPin(project, pins.get(pins.size() - 1));
            return;
        }

        // 获取当前光标位置
        int currentOffset = editor.getCaretModel().getOffset();
        String currentFilePath = currentFile.getPath();

        // 查找上一个图钉
        PinEntry prevPin = findPrevPin(pins, currentFilePath, currentOffset);
        if (prevPin != null) {
            navigateToPin(project, prevPin);
        }
    }

    /**
     * 查找上一个图钉
     * 
     * @param pins 所有图钉
     * @param currentFilePath 当前文件路径
     * @param currentOffset 当前光标位置
     * @return 上一个图钉，如果没有则返回最后一个图钉
     */
    private PinEntry findPrevPin(List<PinEntry> pins, String currentFilePath, int currentOffset) {
        PinEntry lastPin = pins.get(pins.size() - 1);
        PinEntry prevPin = null;
        
        // 首先查找同一文件中的上一个图钉
        for (int i = pins.size() - 1; i >= 0; i--) {
            PinEntry pin = pins.get(i);
            
            // 如果是同一个文件，且位置在当前光标之前
            if (pin.filePath.equals(currentFilePath) && pin.marker.getStartOffset() < currentOffset) {
                prevPin = pin;
                break;
            }
        }
        
        // 如果同一文件中没有找到上一个图钉，查找其他文件中的图钉
        if (prevPin == null) {
            for (int i = pins.size() - 1; i >= 0; i--) {
                PinEntry pin = pins.get(i);
                if (!pin.filePath.equals(currentFilePath)) {
                    prevPin = pin;
                    break;
                }
            }
        }
        
        // 如果仍然没有找到上一个图钉，返回最后一个图钉（循环导航）
        return prevPin != null ? prevPin : lastPin;
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
            OpenFileDescriptor descriptor = new OpenFileDescriptor(
                    project,
                    file,
                    pin.marker.getStartOffset()
            );
            FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只有在有项目打开时才启用此操作
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null && !PinStorage.getPins().isEmpty());
    }
}
