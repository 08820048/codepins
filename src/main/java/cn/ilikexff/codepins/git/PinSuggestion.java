package cn.ilikexff.codepins.git;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于Git分析的图钉建议
 */
public class PinSuggestion {
    
    /**
     * 建议类型
     */
    public enum SuggestionType {
        NEW_CODE("新增代码"),
        MODIFIED_CODE("修改代码"),
        COMPLEX_CHANGE("复杂变更"),
        POTENTIAL_BUG("潜在问题"),
        REVIEW_NEEDED("需要审查"),
        PERFORMANCE_CONCERN("性能关注"),
        SECURITY_ISSUE("安全问题"),
        REFACTOR("重构建议");
        
        private final String displayName;
        
        SuggestionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    /**
     * 建议优先级
     */
    public enum Priority {
        LOW("低", 1),
        MEDIUM("中", 2),
        HIGH("高", 3),
        CRITICAL("紧急", 4);
        
        private final String displayName;
        private final int level;
        
        Priority(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }
        
        public String getDisplayName() { return displayName; }
        public int getLevel() { return level; }
    }
    
    private final String filePath;
    private final int lineNumber;
    private final SuggestionType type;
    private final Priority priority;
    private final String title;
    private final String description;
    private final String reason;
    private final List<String> suggestedTags;
    private final String codeContext;
    private final String changeType;
    private double confidence;
    
    public PinSuggestion(String filePath, int lineNumber, SuggestionType type, Priority priority,
                        String title, String description, String reason) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.type = type;
        this.priority = priority;
        this.title = title;
        this.description = description;
        this.reason = reason;
        this.suggestedTags = new ArrayList<>();
        this.codeContext = "";
        this.changeType = "";
        this.confidence = 0.8;
    }
    
    public PinSuggestion(String filePath, int lineNumber, SuggestionType type, Priority priority,
                        String title, String description, String reason, String codeContext, String changeType) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.type = type;
        this.priority = priority;
        this.title = title;
        this.description = description;
        this.reason = reason;
        this.suggestedTags = new ArrayList<>();
        this.codeContext = codeContext;
        this.changeType = changeType;
        this.confidence = 0.8;
    }
    
    /**
     * 添加建议标签
     */
    public void addSuggestedTag(String tag) {
        if (!suggestedTags.contains(tag)) {
            suggestedTags.add(tag);
        }
    }
    
    /**
     * 生成建议的图钉内容
     */
    public String generatePinContent() {
        StringBuilder content = new StringBuilder();
        content.append(title);
        
        if (!description.isEmpty()) {
            content.append(" - ").append(description);
        }
        
        if (!reason.isEmpty()) {
            content.append(" (").append(reason).append(")");
        }
        
        return content.toString();
    }
    
    /**
     * 获取建议的标签列表
     */
    public List<String> getRecommendedTags() {
        List<String> tags = new ArrayList<>(suggestedTags);
        
        // 根据类型添加默认标签
        switch (type) {
            case NEW_CODE:
                tags.add("NEW");
                break;
            case MODIFIED_CODE:
                tags.add("MODIFIED");
                break;
            case COMPLEX_CHANGE:
                tags.add("COMPLEX");
                break;
            case POTENTIAL_BUG:
                tags.add("BUG");
                break;
            case REVIEW_NEEDED:
                tags.add("REVIEW");
                break;
            case PERFORMANCE_CONCERN:
                tags.add("PERFORMANCE");
                break;
            case SECURITY_ISSUE:
                tags.add("SECURITY");
                break;
        }
        
        // 根据优先级添加标签
        if (priority == Priority.HIGH || priority == Priority.CRITICAL) {
            tags.add("URGENT");
        }
        
        return tags;
    }
    
    /**
     * 检查是否应该创建图钉
     */
    public boolean shouldCreatePin() {
        return confidence >= 0.6 && priority.getLevel() >= 2;
    }
    
    /**
     * 获取建议的显示文本
     */
    public String getDisplayText() {
        return String.format("[%s] %s - %s (置信度: %.1f%%)", 
            type.getDisplayName(), title, priority.getDisplayName(), confidence * 100);
    }
    
    // Getters and Setters
    public String getFilePath() { return filePath; }
    public int getLineNumber() { return lineNumber; }
    public SuggestionType getType() { return type; }
    public Priority getPriority() { return priority; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getReason() { return reason; }
    public List<String> getSuggestedTags() { return suggestedTags; }
    public String getCodeContext() { return codeContext; }
    public String getChangeType() { return changeType; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    @Override
    public String toString() {
        return String.format("PinSuggestion{file='%s', line=%d, type=%s, priority=%s, title='%s'}", 
            filePath, lineNumber, type, priority, title);
    }
}
