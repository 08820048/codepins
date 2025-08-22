package cn.ilikexff.codepins.ai;

import cn.ilikexff.codepins.template.PinTemplate;

import java.util.List;

/**
 * 智能建议数据模型
 * 表示AI分析代码后生成的建议
 */
public class SmartSuggestion {
    
    /**
     * 建议类型枚举
     */
    public enum SuggestionType {
        TODO("TODO", "待办事项", "需要完成的任务或功能"),
        FIXME("FIXME", "修复问题", "需要修复的错误或问题"),
        OPTIMIZE("OPTIMIZE", "性能优化", "可以优化性能的代码"),
        REFACTOR("REFACTOR", "重构建议", "建议重构的代码结构"),
        SECURITY("SECURITY", "安全问题", "潜在的安全风险"),
        CODE_SMELL("CODE_SMELL", "代码异味", "代码质量问题"),
        COMPLEXITY("COMPLEXITY", "复杂度", "代码复杂度过高"),
        DOCUMENTATION("DOCUMENTATION", "文档缺失", "缺少必要的文档或注释"),
        DEPRECATED("DEPRECATED", "过时代码", "使用了过时的API或方法"),
        BEST_PRACTICE("BEST_PRACTICE", "最佳实践", "不符合最佳实践的代码");
        
        private final String code;
        private final String displayName;
        private final String description;
        
        SuggestionType(String code, String displayName, String description) {
            this.code = code;
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getCode() { return code; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 建议优先级
     */
    public enum Priority {
        LOW(1, "低", "#4CAF50"),
        MEDIUM(2, "中", "#FF9800"),
        HIGH(3, "高", "#F44336"),
        CRITICAL(4, "紧急", "#9C27B0");
        
        private final int level;
        private final String displayName;
        private final String color;
        
        Priority(int level, String displayName, String color) {
            this.level = level;
            this.displayName = displayName;
            this.color = color;
        }
        
        public int getLevel() { return level; }
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    private String id;                          // 建议唯一标识
    private SuggestionType type;                // 建议类型
    private Priority priority;                  // 优先级
    private String title;                       // 建议标题
    private String description;                 // 详细描述
    private String reason;                      // 建议原因
    private String filePath;                    // 文件路径
    private int startLine;                      // 开始行号
    private int endLine;                        // 结束行号
    private int startOffset;                    // 开始偏移量
    private int endOffset;                      // 结束偏移量
    private String codeSnippet;                 // 相关代码片段
    private List<String> suggestedActions;      // 建议的操作
    private double confidence;                  // 置信度 (0.0 - 1.0)
    private long createdTime;                   // 创建时间
    private boolean isApplied;                  // 是否已应用
    private String appliedPinId;                // 应用后的图钉ID
    private double adjustedScore;               // 调整后的分数（用于学习优化）
    
    public SmartSuggestion() {
        this.createdTime = System.currentTimeMillis();
        this.isApplied = false;
        this.confidence = 0.5;
    }
    
    public SmartSuggestion(SuggestionType type, Priority priority, String title, 
                          String description, String filePath, int startLine, int endLine) {
        this();
        this.type = type;
        this.priority = priority;
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        this.id = generateId();
    }
    
    /**
     * 生成建议ID
     */
    private String generateId() {
        return "suggestion-" + System.currentTimeMillis() + "-" + Math.random();
    }
    
    /**
     * 获取建议的严重程度分数
     */
    public int getSeverityScore() {
        return priority.getLevel() * 10 + (int)(confidence * 10);
    }
    
    /**
     * 检查建议是否适用于创建图钉
     */
    public boolean isApplicableForPin() {
        return confidence >= 0.3 && !isApplied;
    }
    
    /**
     * 转换为图钉模板类型
     */
    public PinTemplate.TemplateType toTemplateType() {
        switch (type) {
            case TODO:
                return PinTemplate.TemplateType.TODO;
            case FIXME:
                return PinTemplate.TemplateType.FIXME;
            case OPTIMIZE:
                return PinTemplate.TemplateType.OPTIMIZE;
            case REFACTOR:
                return PinTemplate.TemplateType.REFACTOR;
            case SECURITY:
            case CODE_SMELL:
            case COMPLEXITY:
                return PinTemplate.TemplateType.BUG;
            case DOCUMENTATION:
                return PinTemplate.TemplateType.NOTE;
            default:
                return PinTemplate.TemplateType.CUSTOM;
        }
    }
    
    /**
     * 获取建议的显示文本
     */
    public String getDisplayText() {
        return String.format("[%s] %s", type.getDisplayName(), title);
    }
    
    /**
     * 获取详细信息
     */
    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append("类型: ").append(type.getDisplayName()).append("\n");
        info.append("优先级: ").append(priority.getDisplayName()).append("\n");
        info.append("置信度: ").append(String.format("%.1f%%", confidence * 100)).append("\n");
        info.append("位置: ").append(filePath).append(":").append(startLine + 1).append("\n");
        if (reason != null && !reason.isEmpty()) {
            info.append("原因: ").append(reason).append("\n");
        }
        info.append("描述: ").append(description);
        return info.toString();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public SuggestionType getType() { return type; }
    public void setType(SuggestionType type) { this.type = type; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public int getStartLine() { return startLine; }
    public void setStartLine(int startLine) { this.startLine = startLine; }
    
    public int getEndLine() { return endLine; }
    public void setEndLine(int endLine) { this.endLine = endLine; }
    
    public int getStartOffset() { return startOffset; }
    public void setStartOffset(int startOffset) { this.startOffset = startOffset; }
    
    public int getEndOffset() { return endOffset; }
    public void setEndOffset(int endOffset) { this.endOffset = endOffset; }
    
    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }
    
    public List<String> getSuggestedActions() { return suggestedActions; }
    public void setSuggestedActions(List<String> suggestedActions) { this.suggestedActions = suggestedActions; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = Math.max(0.0, Math.min(1.0, confidence)); }
    
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    public boolean isApplied() { return isApplied; }
    public void setApplied(boolean applied) { isApplied = applied; }
    
    public String getAppliedPinId() { return appliedPinId; }
    public void setAppliedPinId(String appliedPinId) { this.appliedPinId = appliedPinId; }

    public double getAdjustedScore() { return adjustedScore; }
    public void setAdjustedScore(double adjustedScore) { this.adjustedScore = adjustedScore; }
    
    @Override
    public String toString() {
        return "SmartSuggestion{" +
                "type=" + type +
                ", priority=" + priority +
                ", title='" + title + '\'' +
                ", filePath='" + filePath + '\'' +
                ", line=" + startLine +
                ", confidence=" + confidence +
                '}';
    }
}
