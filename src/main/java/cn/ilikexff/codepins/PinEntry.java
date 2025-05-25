package cn.ilikexff.codepins;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final List<String> tags;    // 标签列表
    public String name;                 // 图钉名称

    public PinEntry(String filePath, RangeMarker marker, String note, long timestamp, String author, boolean isBlock) {
        this.filePath = filePath;
        this.marker = marker;
        this.note = note;
        this.timestamp = timestamp;
        this.author = author;
        this.isBlock = isBlock;
        this.tags = new ArrayList<>();
    }

    /**
     * 带标签的构造函数
     */
    public PinEntry(String filePath, RangeMarker marker, String note, long timestamp, String author, boolean isBlock, List<String> tags) {
        this.filePath = filePath;
        this.marker = marker;
        this.note = note;
        this.timestamp = timestamp;
        this.author = author;
        this.isBlock = isBlock;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    /**
     * 获取标签列表（只读副本）
     */
    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    /**
     * 添加标签
     */
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty() && !tags.contains(tag.trim())) {
            tags.add(tag.trim());
        }
    }

    /**
     * 移除标签
     */
    public void removeTag(String tag) {
        tags.remove(tag);
    }

    /**
     * 设置标签列表（替换现有标签）
     */
    public void setTags(List<String> newTags) {
        tags.clear();
        if (newTags != null) {
            for (String tag : newTags) {
                if (tag != null && !tag.trim().isEmpty()) {
                    tags.add(tag.trim());
                }
            }
        }
    }

    /**
     * 检查是否包含指定标签
     */
    public boolean hasTag(String tag) {
        return tag != null && tags.contains(tag.trim());
    }

    /**
     * 获取当前行号（从 0 开始），可随代码变化自动更新。
     */
    public int getCurrentLine(Document document) {
        // 验证参数
        if (document == null) {
            System.out.println("[CodePins] getCurrentLine 失败: document 为空");
            return 0;
        }

        if (marker == null) {
            System.out.println("[CodePins] getCurrentLine 失败: marker 为空");
            return 0;
        }

        if (!marker.isValid()) {
            System.out.println("[CodePins] getCurrentLine 失败: marker 无效");
            return 0;
        }

        // 使用 ReadAction 包装文档访问操作，确保线程安全
        return com.intellij.openapi.application.ReadAction.compute(() -> {
            try {
                int startOffset = marker.getStartOffset();
                if (startOffset < 0 || startOffset >= document.getTextLength()) {
                    System.out.println("[CodePins] getCurrentLine 失败: 偏移量超出范围 " + startOffset + ", 文档长度: " + document.getTextLength());
                    return 0;
                }

                int line = document.getLineNumber(startOffset);
                System.out.println("[CodePins] getCurrentLine 成功: " + (line + 1) + ", 文件: " + filePath);
                return line;
            } catch (Exception e) {
                // 如果发生异常，记录错误并返回 0
                System.out.println("[CodePins] getCurrentLine 异常: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        });
    }

    /**
     * 在图钉列表中展示的字符串（用于 JList）
     */
    @Override
    public String toString() {
        // 使用 ReadAction 包装文档访问操作，确保线程安全
        return com.intellij.openapi.application.ReadAction.compute(() -> {
            try {
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
                String tagsStr = "";
                if (!tags.isEmpty()) {
                    tagsStr = " [" + String.join(", ", tags) + "]";
                }
                return typeLabel + " " + filePath + " @ " + lineInfo
                        + (note != null && !note.isEmpty() ? " - " + note : "")
                        + tagsStr;
            } catch (Exception e) {
                // 如果发生异常，返回一个简单的字符串
                String typeLabel = isBlock ? "[代码块]" : "[单行]";
                String tagsStr = "";
                if (!tags.isEmpty()) {
                    tagsStr = " [" + String.join(", ", tags) + "]";
                }
                return typeLabel + " " + filePath +
                       (note != null && !note.isEmpty() ? " - " + note : "") +
                       tagsStr;
            }
        });
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
     * 导航到源代码（供外部调用）
     */
    public void navigateToSource(Project project) {
        navigate(project);
    }
    
    /**
     * 创建新的图钉并添加到存储中
     * 
     * @param project 项目
     * @param filePath 文件路径
     * @param document 文档
     * @param startOffset 起始偏移量
     * @param endOffset 结束偏移量
     * @param note 备注
     * @param isBlock 是否为代码块
     * @return 创建的图钉对象
     */
    public static PinEntry createPin(Project project, String filePath, Document document, int startOffset, int endOffset, String note, boolean isBlock) {
        RangeMarker marker = document.createRangeMarker(startOffset, endOffset);
        marker.setGreedyToLeft(true);
        marker.setGreedyToRight(true);
        
        PinEntry pin = new PinEntry(
                filePath,
                marker,
                note,
                System.currentTimeMillis(),
                System.getProperty("user.name"),
                isBlock
        );
        
        PinStorage.addPin(pin);
        return pin;
    }

    /**
     * 执行跳转：打开文件并定位到当前行号
     * 如果是代码块，则定位到起始行并选中整个代码块
     * 如果代码已被删除，则显示错误消息并询问用户是否删除图钉
     */
    public void navigate(Project project) {
        // 使用 ReadAction 包装文档访问操作，确保线程安全
        com.intellij.openapi.application.ReadAction.run(() -> {
            try {
                VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
                if (file == null || !file.exists()) {
                    // 文件不存在，显示错误消息
                    showNavigationError(project, "无法跳转到图钉位置，文件不存在或已被删除。");
                    return;
                }

                // 检查标记是否有效
                if (marker == null || !marker.isValid()) {
                    // 标记无效，可能是代码已被删除或修改
                    showNavigationError(project, "无法跳转到图钉位置，代码可能已被删除或修改。");
                    return;
                }

                if (isBlock && marker.getStartOffset() != marker.getEndOffset()) {
                    // 如果是代码块图钉，则定位到起始位置并选中整个代码块
                    final int startOffset = marker.getStartOffset();
                    final int endOffset = marker.getEndOffset();

                    // 检查偏移量是否有效
                    Document doc = marker.getDocument();
                    if (doc == null || startOffset < 0 || endOffset > doc.getTextLength()) {
                        showNavigationError(project, "无法跳转到图钉位置，代码可能已被删除或修改。");
                        return;
                    }

                    // 在 EDT 线程上执行导航操作
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                        OpenFileDescriptor descriptor = new OpenFileDescriptor(
                                project,
                                file,
                                startOffset,
                                endOffset - startOffset
                        );
                        if (descriptor.canNavigate()) {
                            descriptor.navigate(true);
                        } else {
                            showNavigationError(project, "无法跳转到图钉位置，请检查文件是否已被修改。");
                        }
                    });
                } else {
                    // 如果是单行图钉，则只定位到当前行
                    Document doc = marker.getDocument();
                    if (doc == null) {
                        showNavigationError(project, "无法跳转到图钉位置，文档不可用。");
                        return;
                    }

                    final int line = getCurrentLine(doc);
                    if (line < 0 || line >= doc.getLineCount()) {
                        showNavigationError(project, "无法跳转到图钉位置，行号超出范围。");
                        return;
                    }

                    // 在 EDT 线程上执行导航操作
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, line, 0);
                        if (descriptor.canNavigate()) {
                            descriptor.navigate(true);
                        } else {
                            showNavigationError(project, "无法跳转到图钉位置，请检查文件是否已被修改。");
                        }
                    });
                }
            } catch (Exception e) {
                // 如果发生异常，记录错误并显示错误消息
                System.out.println("[CodePins] 导航失败: " + e.getMessage());
                showNavigationError(project, "导航失败: " + e.getMessage());
            }
        });
    }

    /**
     * 显示导航错误消息，并询问用户是否删除图钉
     */
    private void showNavigationError(Project project, String message) {
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
            int result = Messages.showYesNoDialog(
                    project,
                    message + "\n\n是否要删除这个图钉？",
                    "跳转失败",
                    "删除图钉",
                    "取消",
                    Messages.getErrorIcon()
            );

            if (result == Messages.YES) {
                // 删除图钉
                PinStorage.removePin(this);
            }
        });
    }
}