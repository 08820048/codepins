package cn.ilikexff.codepins.git;

import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Git图钉创建器
 * 负责将Git建议转换为实际的图钉
 */
public class GitPinCreator {
    
    private final Project project;
    
    public GitPinCreator(Project project) {
        this.project = project;
    }
    
    /**
     * 从建议创建图钉
     */
    public boolean createPinFromSuggestion(PinSuggestion suggestion) {
        try {
            // 获取文件
            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl("file://" + suggestion.getFilePath());
            if (file == null) {
                return false;
            }
            
            // 获取文档
            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document == null) {
                return false;
            }
            
            // 创建范围标记
            int lineNumber = Math.max(0, suggestion.getLineNumber() - 1); // 转换为0基索引
            if (lineNumber >= document.getLineCount()) {
                lineNumber = document.getLineCount() - 1;
            }
            
            int startOffset = document.getLineStartOffset(lineNumber);
            int endOffset = document.getLineEndOffset(lineNumber);
            
            RangeMarker marker = document.createRangeMarker(startOffset, endOffset);
            marker.setGreedyToLeft(true);
            marker.setGreedyToRight(true);
            
            // 创建图钉
            PinEntry pin = new PinEntry(
                suggestion.getFilePath(),
                marker,
                suggestion.generatePinContent(),
                System.currentTimeMillis(),
                System.getProperty("user.name", "unknown"),
                false // 默认为单行图钉
            );
            
            // 添加建议的标签
            List<String> tags = suggestion.getRecommendedTags();
            for (String tag : tags) {
                pin.addTag(tag);
            }
            
            // 添加Git相关标签
            pin.addTag("GIT_SUGGESTION");
            pin.addTag(suggestion.getType().name());
            
            // 保存图钉
            PinStorage.addPin(pin);
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 批量创建图钉
     */
    public List<PinEntry> createPinsFromSuggestions(List<PinSuggestion> suggestions) {
        List<PinEntry> createdPins = new ArrayList<>();
        
        for (PinSuggestion suggestion : suggestions) {
            if (suggestion.shouldCreatePin()) {
                if (createPinFromSuggestion(suggestion)) {
                    // 获取刚创建的图钉（简化实现）
                    List<PinEntry> allPins = PinStorage.getPins();
                    if (!allPins.isEmpty()) {
                        createdPins.add(allPins.get(allPins.size() - 1));
                    }
                }
            }
        }
        
        return createdPins;
    }
    
    /**
     * 创建Git提交相关的图钉
     */
    public boolean createCommitPin(String filePath, int lineNumber, String commitHash, String commitMessage) {
        try {
            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl("file://" + filePath);
            if (file == null) {
                return false;
            }
            
            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document == null) {
                return false;
            }
            
            int adjustedLineNumber = Math.max(0, lineNumber - 1);
            if (adjustedLineNumber >= document.getLineCount()) {
                adjustedLineNumber = document.getLineCount() - 1;
            }
            
            int startOffset = document.getLineStartOffset(adjustedLineNumber);
            int endOffset = document.getLineEndOffset(adjustedLineNumber);
            
            RangeMarker marker = document.createRangeMarker(startOffset, endOffset);
            marker.setGreedyToLeft(true);
            marker.setGreedyToRight(true);
            
            String pinContent = String.format("Git提交: %s - %s", 
                commitHash.substring(0, Math.min(8, commitHash.length())), commitMessage);
            
            PinEntry pin = new PinEntry(filePath, marker, pinContent, System.currentTimeMillis(),
                System.getProperty("user.name", "unknown"), false);
            pin.addTag("GIT_COMMIT");
            pin.addTag("HISTORY");
            
            PinStorage.addPin(pin);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 为代码变更创建审查图钉
     */
    public boolean createReviewPin(String filePath, int lineNumber, String changeType, String reviewNote) {
        try {
            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl("file://" + filePath);
            if (file == null) {
                return false;
            }
            
            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document == null) {
                return false;
            }
            
            int adjustedLineNumber = Math.max(0, lineNumber - 1);
            if (adjustedLineNumber >= document.getLineCount()) {
                adjustedLineNumber = document.getLineCount() - 1;
            }
            
            int startOffset = document.getLineStartOffset(adjustedLineNumber);
            int endOffset = document.getLineEndOffset(adjustedLineNumber);
            
            RangeMarker marker = document.createRangeMarker(startOffset, endOffset);
            marker.setGreedyToLeft(true);
            marker.setGreedyToRight(true);
            
            String pinContent = String.format("[%s] %s", changeType, reviewNote);
            
            PinEntry pin = new PinEntry(filePath, marker, pinContent, System.currentTimeMillis(),
                System.getProperty("user.name", "unknown"), false);
            pin.addTag("REVIEW");
            pin.addTag("GIT_CHANGE");
            pin.addTag(changeType.toUpperCase());
            
            PinStorage.addPin(pin);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取建议的图钉内容预览
     */
    public String getPreviewContent(PinSuggestion suggestion) {
        StringBuilder preview = new StringBuilder();
        preview.append("文件: ").append(suggestion.getFilePath()).append("\n");
        preview.append("行号: ").append(suggestion.getLineNumber()).append("\n");
        preview.append("类型: ").append(suggestion.getType().getDisplayName()).append("\n");
        preview.append("优先级: ").append(suggestion.getPriority().getDisplayName()).append("\n");
        preview.append("内容: ").append(suggestion.generatePinContent()).append("\n");
        preview.append("标签: ").append(String.join(", ", suggestion.getRecommendedTags()));
        
        return preview.toString();
    }
}
