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
import java.util.Date;
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

    /**
     * 无参构造函数，用于创建空的PinEntry对象
     */
    public PinEntry() {
        this.filePath = "";
        this.marker = null;
        this.note = "";
        this.timestamp = new Date().getTime();
        this.author = System.getProperty("user.name");
        this.isBlock = false;
        this.tags = new ArrayList<>();
    }

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
                    // 对于单行图钉，获取整行内容而不仅仅是一个字符
                    int end;
                    if (isBlock) {
                        end = marker.getEndOffset();
                    } else {
                        // 获取行的开始和结束
                        int line = doc.getLineNumber(start);
                        start = doc.getLineStartOffset(line);
                        end = doc.getLineEndOffset(line);
                    }
                    
                    // 确保不越界
                    if (start >= 0 && end <= doc.getTextLength()) {
                        this.originalCode = doc.getText(new TextRange(start, end));
                        System.out.println("[CodePins] 保存原始代码：" + this.originalCode);
                        
                        // 记录行号信息，用于调试
                        int startLine = doc.getLineNumber(start) + 1;
                        int endLine = doc.getLineNumber(end) + 1;
                        System.out.println("[CodePins] 原始代码位置：行 " + startLine + 
                                (startLine != endLine ? "-" + endLine : ""));
                    }
                } catch (Exception e) {
                    System.out.println("[CodePins] 保存原始代码失败：" + e.getMessage());
                    e.printStackTrace();
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
                if (marker == null || !marker.isValid()) {
                    // marker无效时，显示警告信息
                    String typeLabel = isBlock ? "[代码块]" : "[单行]";
                    String tagsStr = "";
                    if (!tags.isEmpty()) {
                        tagsStr = " [" + String.join(", ", tags) + "]";
                    }
                    return typeLabel + " " + filePath + " @ Line ? (无效标记)" 
                           + (note != null && !note.isEmpty() ? " - " + note : "")
                           + tagsStr;
                }
                
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
                return typeLabel + " " + filePath + " @ Line ? (异常: " + e.getMessage() + ")" +
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

                // 检查RangeMarker状态，尝试自动恢复
                boolean markerValid = marker != null && marker.isValid();
                boolean codeExists = checkIfCodeExists(file);
                
                // 简化恢复逻辑: 如果marker无效但代码存在，直接尝试恢复而不弹窗
                if (!markerValid && codeExists) {
                    boolean recovered = tryRecoverMarker(project, file);
                    if (!recovered) {
                        // 仅当恢复失败时才显示一次恢复对话框
                        showCodeRestoredDialog(project, file);
                        return;
                    }
                } else if (!markerValid) {
                    // 如果marker无效且代码不存在，显示错误
                    showNavigationError(project, "无法跳转到图钉位置，代码可能已被删除或修改。");
                    return;
                }

                // 此时图钉标记应该有效，继续导航
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
                                startOffset
                        );
                        if (descriptor.canNavigate()) {
                            descriptor.navigate(true);
                            
                            // 如果需要选中代码块，可以在导航后获取编辑器并设置选择
                            try {
                                FileEditorManager manager = FileEditorManager.getInstance(project);
                                Editor editor = manager.getSelectedTextEditor();
                                if (editor != null && endOffset > startOffset) {
                                    editor.getSelectionModel().setSelection(startOffset, endOffset);
                                    System.out.println("[CodePins] 代码块选中成功");
                                }
                            } catch (Exception e) {
                                System.out.println("[CodePins] 选中代码块失败: " + e.getMessage());
                            }
                            
                            System.out.println("[CodePins] 导航成功");
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

                    // 使用标记的起始偏移量而不是行号
                    final int startOffset = marker.getStartOffset();
                    
                    if (startOffset < 0 || startOffset >= doc.getTextLength()) {
                        showNavigationError(project, "无法跳转到图钉位置，代码位置已被修改。");
                        return;
                    }

                    // 在 EDT 线程上执行导航操作
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                        // 直接使用偏移量创建描述符，而不是行号和列
                        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, startOffset);
                        
                        if (descriptor.canNavigate()) {
                            descriptor.navigate(true);
                        } else {
                            showNavigationError(project, "无法跳转到图钉位置，请检查文件是否已被修改。");
                        }
                    });
                }
            } catch (Exception e) {
                showNavigationError(project, "导航失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 检查文件中是否存在原始代码
     */
    private boolean checkIfCodeExists(VirtualFile file) {
        if (originalCode == null || originalCode.isEmpty()) {
            return false;
        }
        
        try {
            Document document = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(file);
            if (document == null) {
                return false;
            }
            
            String docText = document.getText();
            return docText.contains(originalCode) || docText.contains(originalCode.trim());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 显示导航错误消息，并询问用户是否删除图钉或尝试恢复
     */
    private void showNavigationError(Project project, String message) {
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
            // 检查是否能通过恢复解决问题
            boolean showRecoverOption = false;
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
            if (file != null && file.exists() && originalCode != null && !originalCode.isEmpty()) {
                // 检查文件中是否存在原始代码，以决定是否显示恢复选项
                showRecoverOption = checkIfCodeExists(file);
            }
            
            String[] options;
            if (showRecoverOption) {
                options = new String[]{"删除图钉", "尝试恢复", "取消"};
            } else {
                options = new String[]{"删除图钉", "取消"};
            }
            
            int result = Messages.showDialog(
                    project,
                    message + "\n\n您想要执行什么操作？",
                    "跳转失败",
                    options,
                    0, // 默认选项
                    Messages.getErrorIcon()
            );

            if (result == 0) {
                // 删除图钉
                PinStorage.removePin(this);
            } else if (showRecoverOption && result == 1) {
                // 尝试恢复
                VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
                if (virtualFile != null && virtualFile.exists()) {
                    boolean recovered = tryRecoverMarkerWithNewRangeMarker(project, virtualFile);
                    if (recovered) {
                        // 恢复成功后重新刷新UI显示并尝试导航
                        PinStorage.refreshUI();
                        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                            navigate(project);
                        });
                    } else {
                        Messages.showErrorDialog(project, "无法恢复图钉位置，请确保代码已恢复。", "恢复失败");
                    }
                } else {
                    Messages.showErrorDialog(project, "文件不存在或无法访问。", "恢复失败");
                }
            }
            // 如果选择"取消"，则不执行任何操作
        });
    }

    /**
     * 显示代码已恢复对话框
     */
    private void showCodeRestoredDialog(Project project, VirtualFile file) {
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
            String[] options = {"恢复图钉", "取消"};
            int result = Messages.showDialog(
                    project,
                    "检测到图钉位置的代码已恢复，但图钉标记仍然无效。\n是否要恢复图钉位置？",
                    "代码已恢复",
                    options,
                    0, // 默认选项
                    Messages.getInformationIcon()
            );
            
            if (result == 0) {
                // 直接尝试使用新方法恢复，避免使用可能失败的反射方法
                boolean recovered = tryRecoverMarkerWithNewRangeMarker(project, file);
                if (recovered) {
                    // 成功恢复后立即重新刷新UI显示并尝试导航
                    PinStorage.refreshUI();
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                        navigate(project);
                    });
                } else {
                    Messages.showErrorDialog(project, "无法恢复图钉位置，尝试取消再重做您的操作。", "恢复失败");
                }
            }
        });
    }

    /**
     * 尝试通过创建新的RangeMarker来恢复图钉
     * 这种方法适用于反射失败的情况
     */
    private boolean tryRecoverMarkerWithNewRangeMarker(Project project, VirtualFile file) {
        try {
            return com.intellij.openapi.application.ReadAction.compute(() -> {
                try {
                    // 获取文件的文档
                    com.intellij.openapi.editor.Document document = 
                        com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(file);
                    
                    if (document == null || originalCode == null || originalCode.isEmpty()) {
                        System.out.println("[CodePins] 恢复失败: 文档或原始代码为空");
                        return false;
                    }
                    
                    // 在整个文档中查找原始代码
                    String docText = document.getText();
                    int foundOffset = docText.indexOf(originalCode);
                    boolean useOriginalCode = true;
                    
                    // 如果找不到原始代码，尝试使用修剪后的代码
                    if (foundOffset < 0) {
                        String trimmedCode = originalCode.trim();
                        if (!trimmedCode.isEmpty()) {
                            foundOffset = docText.indexOf(trimmedCode);
                            if (foundOffset >= 0) {
                                useOriginalCode = false;
                                System.out.println("[CodePins] 使用修剪后的代码匹配成功");
                            }
                        }
                    }
                    
                    // 如果仍然找不到，尝试使用更宽松的匹配策略
                    if (foundOffset < 0) {
                        // 尝试匹配代码的前几行（对于多行代码块）
                        String[] lines = originalCode.split("\n");
                        if (lines.length > 1) {
                            String firstTwoLines = lines[0];
                            if (lines.length > 1) {
                                firstTwoLines += "\n" + lines[1];
                            }
                            
                            if (!firstTwoLines.trim().isEmpty()) {
                                foundOffset = docText.indexOf(firstTwoLines.trim());
                                if (foundOffset >= 0) {
                                    useOriginalCode = false;
                                    System.out.println("[CodePins] 使用代码前几行匹配成功");
                                }
                            }
                        }
                    }
                    
                    if (foundOffset >= 0) {
                        // 找到匹配的代码
                        System.out.println("[CodePins] 在文档中找到匹配的" + 
                                          (useOriginalCode ? "原始" : "部分") + 
                                          "代码，位置: " + foundOffset);
                        
                        // 创建新的RangeMarker
                        int newEndOffset;
                        if (useOriginalCode) {
                            newEndOffset = foundOffset + originalCode.length();
                        } else {
                            // 尝试找到合适的结束位置
                            String[] lines = originalCode.split("\n");
                            if (lines.length > 1) {
                                // 对于多行代码，尝试找到最后一行
                                String lastLine = lines[lines.length - 1].trim();
                                if (!lastLine.isEmpty()) {
                                    int lastLinePos = docText.indexOf(lastLine, foundOffset);
                                    if (lastLinePos > foundOffset) {
                                        newEndOffset = lastLinePos + lastLine.length();
                                    } else {
                                        // 如果找不到最后一行，至少包含找到的部分
                                        newEndOffset = foundOffset + originalCode.trim().length();
                                    }
                                } else {
                                    newEndOffset = foundOffset + originalCode.trim().length();
                                }
                            } else {
                                newEndOffset = foundOffset + originalCode.trim().length();
                            }
                        }
                        
                        // 确保偏移量不超出文档范围
                        if (newEndOffset > document.getTextLength()) {
                            newEndOffset = document.getTextLength();
                        }
                        
                        // 创建一个新的可靠的RangeMarker
                        RangeMarker newMarker = document.createRangeMarker(foundOffset, newEndOffset);
                        newMarker.setGreedyToLeft(true);
                        newMarker.setGreedyToRight(true);
                        
                        // 更新原始代码为当前找到的代码段，确保下次比较能够匹配
                        if (!useOriginalCode) {
                            // 如果使用修剪后的代码匹配成功，更新原始代码
                            originalCode = document.getText(new TextRange(foundOffset, newEndOffset));
                        }
                        
                        // 创建一个新的PinEntry替换当前的
                        PinEntry newPin = new PinEntry(
                            filePath, 
                            newMarker, 
                            note, 
                            timestamp, 
                            author, 
                            isBlock, 
                            getTags()
                        );
                        
                        // 如果有名称，也复制过来
                        if (name != null) {
                            newPin.name = name;
                        }
                        
                        // 使用PinStorage的replacePin方法替换图钉
                        if (PinStorage.replacePin(this, newPin)) {
                            int newLineNumber = document.getLineNumber(foundOffset) + 1;
                            System.out.println("[CodePins] 图钉标记已成功恢复到行 " + newLineNumber);
                            return true;
                        } else {
                            System.out.println("[CodePins] 无法在存储中替换图钉");
                            return false;
                        }
                    } else {
                        System.out.println("[CodePins] 恢复失败: 在文档中找不到匹配的代码");
                        return false;
                    }
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
                        return false;
                    }
                    
                    // 先检查marker是否仍然有效
                    if (marker != null && marker.isValid()) {
                        // 验证当前位置的代码是否与原始代码匹配
                        int startOffset = marker.getStartOffset();
                        int endOffset = marker.getEndOffset();
                        
                        // 确保不越界
                        if (endOffset <= document.getTextLength()) {
                            String currentCode = document.getText(new TextRange(startOffset, endOffset));
                            if (originalCode.equals(currentCode)) {
                                // 当前位置的代码与原始代码匹配，不需要恢复
                                System.out.println("[CodePins] 图钉位置有效且代码匹配，无需恢复");
                                return true;
                            }
                        }
                    }
                    
                    // 由于无法使用反射修改marker内部状态，直接调用新方法创建新的marker
                    System.out.println("[CodePins] 尝试通过创建新marker替换恢复图钉");
                    return tryRecoverMarkerWithNewRangeMarker(project, file);
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
}