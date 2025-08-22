package cn.ilikexff.codepins.git;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Git差异分析器
 * 分析代码变更并生成图钉建议
 */
public class DiffAnalyzer {
    
    private final Project project;
    
    // 分析规则
    private static final Pattern TODO_PATTERN = Pattern.compile("(?i)\\b(todo|fixme|hack|xxx)\\b");
    private static final Pattern COMPLEX_METHOD_PATTERN = Pattern.compile("(public|private|protected)\\s+\\w+\\s+\\w+\\s*\\([^)]*\\)\\s*\\{");
    private static final Pattern EXCEPTION_PATTERN = Pattern.compile("(throw|catch|try)\\s+");
    private static final Pattern LOOP_PATTERN = Pattern.compile("\\b(for|while|do)\\s*\\(");
    private static final Pattern CONDITION_PATTERN = Pattern.compile("\\bif\\s*\\(");
    
    public DiffAnalyzer(Project project) {
        this.project = project;
    }
    
    /**
     * 初始化分析器
     */
    public void initialize() {
        // 初始化逻辑
    }
    
    /**
     * 分析变更并生成图钉建议
     */
    public List<PinSuggestion> analyzeChanges(Collection<Change> changes) {
        List<PinSuggestion> suggestions = new ArrayList<>();
        
        for (Change change : changes) {
            try {
                List<PinSuggestion> changeSuggestions = analyzeChange(change);
                suggestions.addAll(changeSuggestions);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return suggestions;
    }
    
    /**
     * 分析单个变更
     */
    private List<PinSuggestion> analyzeChange(Change change) {
        List<PinSuggestion> suggestions = new ArrayList<>();
        
        ContentRevision afterRevision = change.getAfterRevision();
        ContentRevision beforeRevision = change.getBeforeRevision();
        
        if (afterRevision == null) {
            // 文件被删除
            return suggestions;
        }
        
        String filePath = afterRevision.getFile().getPath();
        
        try {
            String afterContent = afterRevision.getContent();
            String beforeContent = beforeRevision != null ? beforeRevision.getContent() : "";
            
            if (afterContent != null) {
                suggestions.addAll(analyzeContent(filePath, afterContent, beforeContent));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return suggestions;
    }
    
    /**
     * 分析文件内容
     */
    private List<PinSuggestion> analyzeContent(String filePath, String afterContent, String beforeContent) {
        List<PinSuggestion> suggestions = new ArrayList<>();
        
        String[] afterLines = afterContent.split("\n");
        String[] beforeLines = beforeContent.split("\n");
        
        // 简单的行级差异分析
        Set<Integer> changedLines = findChangedLines(beforeLines, afterLines);
        
        for (int lineNumber : changedLines) {
            if (lineNumber < afterLines.length) {
                String line = afterLines[lineNumber];
                suggestions.addAll(analyzeLine(filePath, line, lineNumber + 1));
            }
        }
        
        return suggestions;
    }
    
    /**
     * 查找变更的行
     */
    private Set<Integer> findChangedLines(String[] beforeLines, String[] afterLines) {
        Set<Integer> changedLines = new HashSet<>();
        
        // 简单的差异检测（实际项目中可以使用更复杂的diff算法）
        int maxLines = Math.max(beforeLines.length, afterLines.length);
        
        for (int i = 0; i < maxLines; i++) {
            String beforeLine = i < beforeLines.length ? beforeLines[i] : "";
            String afterLine = i < afterLines.length ? afterLines[i] : "";
            
            if (!beforeLine.equals(afterLine)) {
                changedLines.add(i);
            }
        }
        
        return changedLines;
    }
    
    /**
     * 分析单行代码
     */
    private List<PinSuggestion> analyzeLine(String filePath, String line, int lineNumber) {
        List<PinSuggestion> suggestions = new ArrayList<>();
        
        String trimmedLine = line.trim();
        
        // 检查TODO注释
        if (TODO_PATTERN.matcher(trimmedLine).find()) {
            suggestions.add(new PinSuggestion(
                filePath, lineNumber,
                PinSuggestion.SuggestionType.REVIEW_NEEDED,
                PinSuggestion.Priority.MEDIUM,
                "发现TODO注释",
                "代码中包含TODO标记，需要后续处理",
                "Git变更分析",
                trimmedLine,
                "新增/修改"
            ));
        }
        
        // 检查复杂方法
        if (COMPLEX_METHOD_PATTERN.matcher(trimmedLine).find()) {
            suggestions.add(new PinSuggestion(
                filePath, lineNumber,
                PinSuggestion.SuggestionType.COMPLEX_CHANGE,
                PinSuggestion.Priority.LOW,
                "新增方法",
                "检测到新的方法定义，建议添加文档注释",
                "Git变更分析",
                trimmedLine,
                "新增方法"
            ));
        }
        
        // 检查异常处理
        if (EXCEPTION_PATTERN.matcher(trimmedLine).find()) {
            suggestions.add(new PinSuggestion(
                filePath, lineNumber,
                PinSuggestion.SuggestionType.REVIEW_NEEDED,
                PinSuggestion.Priority.MEDIUM,
                "异常处理代码",
                "检测到异常处理逻辑，建议仔细审查",
                "Git变更分析",
                trimmedLine,
                "异常处理"
            ));
        }
        
        // 检查循环结构
        if (LOOP_PATTERN.matcher(trimmedLine).find()) {
            suggestions.add(new PinSuggestion(
                filePath, lineNumber,
                PinSuggestion.SuggestionType.PERFORMANCE_CONCERN,
                PinSuggestion.Priority.LOW,
                "循环结构",
                "检测到循环代码，注意性能影响",
                "Git变更分析",
                trimmedLine,
                "循环逻辑"
            ));
        }
        
        // 检查复杂条件
        if (CONDITION_PATTERN.matcher(trimmedLine).find() && trimmedLine.length() > 50) {
            suggestions.add(new PinSuggestion(
                filePath, lineNumber,
                PinSuggestion.SuggestionType.COMPLEX_CHANGE,
                PinSuggestion.Priority.LOW,
                "复杂条件判断",
                "检测到较长的条件判断，考虑简化",
                "Git变更分析",
                trimmedLine,
                "条件逻辑"
            ));
        }
        
        // 检查魔法数字（在变更的代码中）
        if (containsMagicNumber(trimmedLine)) {
            suggestions.add(new PinSuggestion(
                filePath, lineNumber,
                PinSuggestion.SuggestionType.REFACTOR,
                PinSuggestion.Priority.LOW,
                "可能的魔法数字",
                "新增代码中包含数字常量，考虑定义为常量",
                "Git变更分析",
                trimmedLine,
                "代码质量"
            ));
        }
        
        return suggestions;
    }
    
    /**
     * 检查是否包含魔法数字
     */
    private boolean containsMagicNumber(String line) {
        // 简单的魔法数字检测
        Pattern magicNumberPattern = Pattern.compile("\\b(?!0|1|2|10|100|1000)\\d{2,}\\b");
        
        // 排除注解和常量定义
        if (line.contains("@") || line.contains("final") || line.contains("static")) {
            return false;
        }
        
        return magicNumberPattern.matcher(line).find();
    }
    
    /**
     * 分析文件级别的变更
     */
    public List<PinSuggestion> analyzeFileChanges(String filePath, String content) {
        List<PinSuggestion> suggestions = new ArrayList<>();
        
        // 分析文件大小
        String[] lines = content.split("\n");
        if (lines.length > 500) {
            suggestions.add(new PinSuggestion(
                filePath, 1,
                PinSuggestion.SuggestionType.REFACTOR,
                PinSuggestion.Priority.MEDIUM,
                "文件过大",
                String.format("文件包含%d行代码，考虑拆分", lines.length),
                "文件大小分析",
                "",
                "文件结构"
            ));
        }
        
        // 分析方法数量
        long methodCount = Arrays.stream(lines)
            .filter(line -> COMPLEX_METHOD_PATTERN.matcher(line.trim()).find())
            .count();
        
        if (methodCount > 20) {
            suggestions.add(new PinSuggestion(
                filePath, 1,
                PinSuggestion.SuggestionType.REFACTOR,
                PinSuggestion.Priority.LOW,
                "方法过多",
                String.format("类包含%d个方法，考虑重构", methodCount),
                "类结构分析",
                "",
                "类设计"
            ));
        }
        
        return suggestions;
    }
}
