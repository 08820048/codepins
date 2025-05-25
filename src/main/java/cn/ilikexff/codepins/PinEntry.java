package cn.ilikexff.codepins;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.util.TextRange;

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
    private String originalCode;        // 原始代码内容，用于恢复验证

    public PinEntry(String filePath, RangeMarker marker, String note, long timestamp, String author, boolean isBlock) {
        this.filePath = filePath;
        this.marker = marker;
        this.note = note;
        this.timestamp = timestamp;
        this.author = author;
        this.isBlock = isBlock;
        this.tags = new ArrayList<>();
        
        // 保存原始代码内容
        saveOriginalCode();
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
        
        // 保存原始代码内容
        saveOriginalCode();
    }

    /**
     * 保存原始代码内容
     */
    private void saveOriginalCode() {
        if (marker != null && marker.isValid()) {
            Document doc = marker.getDocument();
            if (doc != null) {
                try {
                    int start = marker.getStartOffset();
                    int end = isBlock ? marker.getEndOffset() : start + 1;
                    
                    // 确保不越界
                    if (start >= 0 && end <= doc.getTextLength()) {
                        this.originalCode = doc.getText(new TextRange(start, end));
                        System.out.println("[CodePins] 保存原始代码：" + this.originalCode);
                    }
                } catch (Exception e) {
                    System.out.println("[CodePins] 保存原始代码失败：" + e.getMessage());
                }
            }
        }
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
     * 执行跳转：打开文件并定位到当前行号
     * 如果是代码块，则定位到起始行并选中整个代码块
     * 如果代码已被删除，则显示错误消息并询问用户是否删除图钉
     */
    public void navigate(Project project) {
        // 使用 ReadAction 包装文档访问操作，确保线程安全
        com.intellij.openapi.application.ReadAction.run(() -> {
            try {
                System.out.println("[CodePins] 开始导航到图钉: " + filePath);
                
                VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
                if (file == null || !file.exists()) {
                    // 文件不存在，显示错误消息
                    showNavigationError(project, "无法跳转到图钉位置，文件不存在或已被删除。");
                    return;
                }

                // 在任何操作前尝试恢复图钉标记 - 即使它当前是有效的，也尝试更新位置
                boolean recovered = tryRecoverMarker(project, file);
                
                // 如果恢复失败且标记无效，显示错误
                if (!recovered && (marker == null || !marker.isValid())) {
                    showNavigationError(project, "无法跳转到图钉位置，代码可能已被删除或修改。");
                    return;
                }

                // 获取文档对象
                com.intellij.openapi.editor.Document doc = 
                    com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(file);
                
                if (doc == null) {
                    showNavigationError(project, "无法跳转到图钉位置，文档不可用。");
                    return;
                }
                
                final int startOffset = marker.getStartOffset();
                final int endOffset = marker.getEndOffset();
                
                // 验证偏移量是否有效
                if (startOffset < 0 || startOffset >= doc.getTextLength()) {
                    System.out.println("[CodePins] 偏移量无效: " + startOffset + ", 文档长度: " + doc.getTextLength());
                    // 尝试再次修复
                    if (tryRecoverMarker(project, file)) {
                        // 递归调用导航，使用修复后的marker
                        navigate(project);
                        return;
                    } else {
                        showNavigationError(project, "无法跳转到图钉位置，代码位置已被修改。");
                        return;
                    }
                }
                
                // 在 EDT 线程上执行导航操作
                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                    OpenFileDescriptor descriptor = new OpenFileDescriptor(
                            project,
                            file,
                            startOffset
                    );
                    
                    if (descriptor.canNavigate()) {
                        descriptor.navigate(true);
                        
                        // 如果是代码块且有选择范围，则设置选择
                        if (isBlock && endOffset > startOffset) {
                            try {
                                FileEditorManager manager = FileEditorManager.getInstance(project);
                                Editor editor = manager.getSelectedTextEditor();
                                if (editor != null) {
                                    editor.getSelectionModel().setSelection(startOffset, endOffset);
                                    System.out.println("[CodePins] 代码块选中成功");
                                }
                            } catch (Exception e) {
                                System.out.println("[CodePins] 选中代码块失败: " + e.getMessage());
                            }
                        }
                        
                        System.out.println("[CodePins] 导航成功");
                    } else {
                        System.out.println("[CodePins] 导航描述符无法导航");
                        // 尝试通过FileEditorManager直接打开文件
                        try {
                            FileEditorManager.getInstance(project).openFile(file, true);
                            System.out.println("[CodePins] 已打开文件，但无法定位到指定位置");
                        } catch (Exception e) {
                            showNavigationError(project, "无法跳转到图钉位置，请检查文件是否已被修改。");
                        }
                    }
                });
            } catch (Exception e) {
                System.out.println("[CodePins] 导航异常: " + e.getMessage());
                e.printStackTrace();
                showNavigationError(project, "导航失败: " + e.getMessage());
            }
        });
    }

    /**
     * 显示导航错误消息，并询问用户是否删除图钉
     */
    private void showNavigationError(Project project, String message) {
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
            String[] options = {"删除图钉", "保留图钉", "尝试恢复"};
            int result = Messages.showDialog(
                    project,
                    message + "\n\n您想要执行什么操作？",
                    "跳转失败",
                    options,
                    0, // 默认选项：删除图钉
                    Messages.getErrorIcon()
            );

            if (result == 0) {
                // 删除图钉
                System.out.println("[CodePins] 用户选择删除图钉");
                PinStorage.removePin(this);
            } else if (result == 2) {
                // 尝试恢复
                System.out.println("[CodePins] 用户选择尝试恢复图钉");
                VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
                if (file != null && file.exists()) {
                    boolean recovered = tryRecoverMarker(project, file);
                    if (recovered) {
                        // 恢复成功，再次尝试导航
                        System.out.println("[CodePins] 恢复成功，再次尝试导航");
                        navigate(project);
                    } else {
                        // 恢复失败
                        Messages.showErrorDialog(
                                project,
                                "无法恢复图钉位置。\n可能的原因：\n1. 代码已被完全删除\n2. 代码已经被大幅修改\n3. 文件结构变化过大",
                                "恢复失败"
                        );
                    }
                }
            }
            // 用户选择保留图钉不做任何操作
        });
    }

    /**
     * 尝试恢复无效的图钉标记
     * 当代码被删除后又撤销恢复时，通过文件重新创建标记
     * 
     * @param project 项目对象
     * @param file 文件对象
     * @return 是否成功恢复
     */
    private boolean tryRecoverMarker(Project project, VirtualFile file) {
        try {
            // 使用ReadAction包装文档访问，确保线程安全
            return com.intellij.openapi.application.ReadAction.compute(() -> {
                try {
                    System.out.println("[CodePins] 开始尝试恢复图钉: " + filePath);
                    
                    // 获取文件的文档
                    com.intellij.openapi.editor.Document document = 
                        com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(file);
                    
                    if (document == null) {
                        System.out.println("[CodePins] 恢复失败: 无法获取文档");
                        return false;
                    }
                    
                    // 判断文档内容长度是否足够
                    if (document.getTextLength() == 0) {
                        System.out.println("[CodePins] 恢复失败: 文档内容为空");
                        return false;
                    }
                    
                    // 如果没有保存原始代码，无法进行内容比较
                    if (originalCode == null || originalCode.isEmpty()) {
                        System.out.println("[CodePins] 恢复失败: 没有原始代码记录");
                        // 尝试先刷新原始代码
                        saveOriginalCode();
                        if (originalCode == null || originalCode.isEmpty()) {
                            return false;
                        }
                    }

                    // 尝试几种不同的恢复策略
                    
                    // 策略1: 原位置检查
                    int originalOffset = marker.getStartOffset();
                    if (originalOffset >= 0 && originalOffset < document.getTextLength()) {
                        int endOffset = Math.min(originalOffset + originalCode.length(), document.getTextLength());
                        String currentCode = document.getText(new TextRange(originalOffset, endOffset));
                        
                        if (originalCode.equals(currentCode) || 
                            (currentCode.length() > 0 && originalCode.startsWith(currentCode))) {
                            System.out.println("[CodePins] 原位置代码匹配，直接恢复");
                            updateMarkerState(originalOffset, endOffset);
                            return true;
                        }
                    }

                    // 策略2: 完全匹配搜索
                    int exactMatchPos = document.getText().indexOf(originalCode);
                    if (exactMatchPos >= 0) {
                        System.out.println("[CodePins] 找到完全匹配的代码，位置: " + exactMatchPos);
                        int endPos = exactMatchPos + originalCode.length();
                        updateMarkerState(exactMatchPos, endPos);
                        return true;
                    }

                    // 策略3: 部分匹配搜索 (只匹配前10个字符，适用于长代码段被部分修改的情况)
                    if (originalCode.length() > 10) {
                        String codePrefix = originalCode.substring(0, 10);
                        int prefixPos = document.getText().indexOf(codePrefix);
                        if (prefixPos >= 0) {
                            System.out.println("[CodePins] 找到部分匹配的代码前缀，位置: " + prefixPos);
                            int estimatedEnd = Math.min(prefixPos + originalCode.length(), document.getTextLength());
                            updateMarkerState(prefixPos, estimatedEnd);
                            return true;
                        }
                    }

                    // 策略4: 行号恢复 (根据保存的行号信息尝试恢复)
                    try {
                        java.lang.reflect.Field lineField = this.getClass().getDeclaredField("line");
                        lineField.setAccessible(true);
                        Integer savedLine = (Integer) lineField.get(this);
                        
                        if (savedLine != null && savedLine >= 0 && savedLine < document.getLineCount()) {
                            int lineStart = document.getLineStartOffset(savedLine);
                            int lineEnd = document.getLineEndOffset(savedLine);
                            System.out.println("[CodePins] 尝试通过行号恢复: " + savedLine + ", 位置: " + lineStart);
                            updateMarkerState(lineStart, lineEnd);
                            return true;
                        }
                    } catch (Exception e) {
                        // 没有保存行号信息，继续使用其他恢复策略
                        System.out.println("[CodePins] 无法通过行号恢复: " + e.getMessage());
                    }
                    
                    System.out.println("[CodePins] 恢复失败: 无法找到匹配的代码内容");
                    return false;
                } catch (Exception e) {
                    System.out.println("[CodePins] 恢复失败: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            });
        } catch (Exception e) {
            System.out.println("[CodePins] 恢复操作异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 更新标记状态
     */
    private void updateMarkerState(int startOffset, int endOffset) {
        try {
            // 使用反射修改marker的内部状态
            java.lang.reflect.Field validField = marker.getClass().getDeclaredField("valid");
            validField.setAccessible(true);
            validField.set(marker, true);
            
            // 更新偏移量
            java.lang.reflect.Field startOffsetField = marker.getClass().getDeclaredField("startOffset");
            startOffsetField.setAccessible(true);
            startOffsetField.set(marker, startOffset);
            
            java.lang.reflect.Field endOffsetField = marker.getClass().getDeclaredField("endOffset");
            endOffsetField.setAccessible(true);
            endOffsetField.set(marker, endOffset);
            
            System.out.println("[CodePins] 图钉标记已成功恢复到位置: " + startOffset + "-" + endOffset);
        } catch (Exception e) {
            System.out.println("[CodePins] 恢复失败: 无法重置标记状态: " + e.getMessage());
            e.printStackTrace();
        }
    }
}