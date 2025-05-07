package cn.ilikexff.codepins;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * 图钉数据模型类，包含文件路径、行号和备注信息。
 */
public class PinEntry {

    public final String filePath; // 文件路径（绝对路径）
    public final int line;        // 行号（从 0 开始）
    public final String note;     // 用户备注

    public PinEntry(String filePath, int line, String note) {
        this.filePath = filePath;
        this.line = line;
        this.note = note;
    }

    /**
     * 在图钉列表中展示的字符串（用于 JList）
     */
    @Override
    public String toString() {
        return filePath + " @ Line " + (line + 1) + (note != null && !note.isEmpty() ? " - " + note : "");
    }

    /**
     * 用于比较图钉是否相等（路径和行号）
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PinEntry other)) return false;
        return filePath.equals(other.filePath) && line == other.line;
    }

    @Override
    public int hashCode() {
        return filePath.hashCode() * 31 + line;
    }

    /**
     * 执行跳转：打开文件并定位到指定行
     */
    public void navigate(Project project) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (file != null) {
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, line);
            if (descriptor.canNavigate()) {
                descriptor.navigate(true);
            }
        }
    }
}