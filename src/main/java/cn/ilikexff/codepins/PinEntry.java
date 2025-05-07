package cn.ilikexff.codepins;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * 图钉数据模型类，使用 RangeMarker 实时追踪代码行位置。
 */
public class PinEntry {

    public final String filePath;       // 文件路径（绝对路径）
    public final RangeMarker marker;    // 可变行位置追踪
    public String note;                 // 用户备注
    public final long timestamp;  // 创建时间（毫秒）
    public final String author;   // 创建者（可选）

    public PinEntry(String filePath, RangeMarker marker, String note, long timestamp, String author) {
        this.filePath = filePath;
        this.marker = marker;
        this.note = note;
        this.timestamp = timestamp;
        this.author = author;
    }

    /**
     * 获取当前行号（从 0 开始），会随代码变化动态更新。
     */
    public int getCurrentLine(Document document) {
        return document.getLineNumber(marker.getStartOffset());
    }

    /**
     * 在图钉列表中展示的字符串（用于 JList）
     */
    @Override
    public String toString() {
        int line = getCurrentLine(marker.getDocument());
        return filePath + " @ Line " + (line + 1) + (note != null && !note.isEmpty() ? " - " + note : "");
    }

    /**
     * 判断是否为同一个图钉（基于路径和初始偏移）
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PinEntry other)) return false;
        return filePath.equals(other.filePath)
                && marker.getStartOffset() == other.marker.getStartOffset();
    }

    @Override
    public int hashCode() {
        return filePath.hashCode() * 31 + marker.getStartOffset();
    }

    /**
     * 执行跳转：打开文件并定位到当前行号
     */
    public void navigate(Project project) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (file != null) {
            int line = getCurrentLine(marker.getDocument());
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, line);
            if (descriptor.canNavigate()) {
                descriptor.navigate(true);
            }
        }
    }
}