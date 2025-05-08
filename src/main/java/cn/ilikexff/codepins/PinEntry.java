package cn.ilikexff.codepins;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Objects;

/**
 * 图钉数据模型类，支持单行与代码块图钉类型区分，使用 RangeMarker 动态追踪代码位置。
 */
public class PinEntry {

    public final String filePath;       // 文件路径（绝对路径）
    public final RangeMarker marker;    // 可变行位置追踪
    public String note;                 // 用户备注
    public final long timestamp;        // 创建时间戳
    public final String author;         // 创建者（可用于团队协作）
    public final boolean isBlock;       // 是否为代码块图钉

    public PinEntry(String filePath, RangeMarker marker, String note, long timestamp, String author, boolean isBlock) {
        this.filePath = filePath;
        this.marker = marker;
        this.note = note;
        this.timestamp = timestamp;
        this.author = author;
        this.isBlock = isBlock;
    }

    /**
     * 获取当前行号（从 0 开始），可随代码变化自动更新。
     */
    public int getCurrentLine(Document document) {
        return document.getLineNumber(marker.getStartOffset());
    }

    /**
     * 在图钉列表中展示的字符串（用于 JList）
     */
    @Override
    public String toString() {
        Document doc = marker.getDocument();
        String lineInfo;

        if (isBlock) {
            // 如果是代码块，显示起始行号到结束行号
            int startLine = doc.getLineNumber(marker.getStartOffset()) + 1; // 转为从1开始的行号
            int endLine = doc.getLineNumber(marker.getEndOffset()) + 1;     // 转为从1开始的行号

            // 如果起始行和结束行相同，则只显示一个行号
            if (startLine == endLine) {
                lineInfo = "Line " + startLine;
            } else {
                lineInfo = "Line " + startLine + "-" + endLine;
            }
        } else {
            // 如果是单行图钉，只显示当前行号
            int line = doc.getLineNumber(marker.getStartOffset()) + 1; // 转为从1开始的行号
            lineInfo = "Line " + line;
        }

        String typeLabel = isBlock ? "[代码块]" : "[单行]";
        return typeLabel + " " + filePath + " @ " + lineInfo
                + (note != null && !note.isEmpty() ? " - " + note : "");
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
        return Objects.hash(filePath, marker.getStartOffset());
    }

    /**
     * 执行跳转：打开文件并定位到当前行号
     * 如果是代码块，则定位到起始行并选中整个代码块
     */
    public void navigate(Project project) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (file != null) {
            if (isBlock && marker.getStartOffset() != marker.getEndOffset()) {
                // 如果是代码块图钉，则定位到起始位置并选中整个代码块
                OpenFileDescriptor descriptor = new OpenFileDescriptor(
                        project,
                        file,
                        marker.getStartOffset(),
                        marker.getEndOffset() - marker.getStartOffset()
                );
                if (descriptor.canNavigate()) {
                    descriptor.navigate(true);
                }
            } else {
                // 如果是单行图钉，则只定位到当前行
                int line = getCurrentLine(marker.getDocument());
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, line);
                if (descriptor.canNavigate()) {
                    descriptor.navigate(true);
                }
            }
        }
    }
}