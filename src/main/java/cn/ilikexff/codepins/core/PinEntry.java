package cn.ilikexff.codepins.core;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import cn.ilikexff.codepins.core.PinStorage;

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
     * 创建一个新的图钉并添加到存储中
     */
    public static PinEntry createPin(Project project, String filePath, Document document, int startOffset, int endOffset, String note, boolean isBlock) {
        RangeMarker marker = document.createRangeMarker(startOffset, endOffset);
        marker.setGreedyToLeft(true);
        marker.setGreedyToRight(true);
        PinEntry pin = new PinEntry(filePath, marker, note, System.currentTimeMillis(), System.getProperty("user.name"), isBlock);
        PinStorage.addPin(pin);
        return pin;
    }

    /**
     * 导航到图钉位置
     */
    public void navigate(Project project) {
        try {
            VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(filePath);
            if (vFile != null && vFile.exists()) {
                Document document = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(vFile);
                if (document != null && marker.isValid()) {
                    int offset = marker.getStartOffset();
                    new OpenFileDescriptor(project, vFile, offset).navigate(true);
                } else {
                    Messages.showErrorDialog("无法打开文件或标记已失效", "导航错误");
                }
            } else {
                Messages.showErrorDialog("找不到文件: " + filePath, "导航错误");
            }
        } catch (Exception e) {
            Messages.showErrorDialog("导航时发生错误: " + e.getMessage(), "导航错误");
            e.printStackTrace();
        }
    }

    /**
     * 获取图钉的代码内容
     */
    public String getCodeContent() {
        if (!marker.isValid()) {
            return "代码标记已失效";
        }

        try {
            Document doc = marker.getDocument();
            if (doc != null) {
                int startOffset = marker.getStartOffset();
                int endOffset = marker.getEndOffset();
                if (startOffset >= 0 && endOffset <= doc.getTextLength() && startOffset <= endOffset) {
                    return doc.getText(new com.intellij.openapi.util.TextRange(startOffset, endOffset));
                }
            }
            return "无法获取代码内容";
        } catch (Exception e) {
            e.printStackTrace();
            return "获取代码内容时发生错误: " + e.getMessage();
        }
    }

    /**
     * 获取图钉的行号范围（从1开始）
     */
    public String getLineRange() {
        if (!marker.isValid()) {
            return "未知";
        }

        try {
            Document doc = marker.getDocument();
            if (doc != null) {
                int startLine = doc.getLineNumber(marker.getStartOffset()) + 1; // 转为从1开始的行号
                int endLine = doc.getLineNumber(marker.getEndOffset()) + 1;     // 转为从1开始的行号

                if (startLine == endLine) {
                    return String.valueOf(startLine);
                } else {
                    return startLine + "-" + endLine;
                }
            }
            return "未知";
        } catch (Exception e) {
            return "未知";
        }
    }

    /**
     * 获取图钉的简短描述（用于工具提示等）
     */
    public String getShortDescription() {
        String typeLabel = isBlock ? "代码块" : "单行";
        String lineInfo = getLineRange();
        return typeLabel + " @ " + lineInfo + (note != null && !note.isEmpty() ? " - " + note : "");
    }

    /**
     * 创建一个带标签的图钉
     */
    public static PinEntry createPinWithTags(Project project, String filePath, Document document, int startOffset, int endOffset, String note, boolean isBlock, List<String> tags) {
        RangeMarker marker = document.createRangeMarker(startOffset, endOffset);
        marker.setGreedyToLeft(true);
        marker.setGreedyToRight(true);
        PinEntry pin = new PinEntry(filePath, marker, note, System.currentTimeMillis(), System.getProperty("user.name"), isBlock, tags);
        PinStorage.addPin(pin);
        return pin;
    }
}
