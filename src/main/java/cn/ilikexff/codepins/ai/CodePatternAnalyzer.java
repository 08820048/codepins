package cn.ilikexff.codepins.ai;

// PSI imports temporarily removed to fix compilation issues
// import com.intellij.psi.*;
// import com.intellij.psi.util.PsiTreeUtil;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 代码模式分析器
 * 识别常见的代码模式和潜在问题
 */
public class CodePatternAnalyzer {
    
    /**
     * 代码异味模式
     */
    private static final Map<String, CodeSmellPattern> CODE_SMELL_PATTERNS = new HashMap<>();
    
    static {
        // 长参数列表
        CODE_SMELL_PATTERNS.put("LONG_PARAMETER_LIST", new CodeSmellPattern(
            "长参数列表", "方法参数过多，建议使用参数对象", 5, 0.7
        ));
        
        // 长方法
        CODE_SMELL_PATTERNS.put("LONG_METHOD", new CodeSmellPattern(
            "长方法", "方法过长，建议拆分为更小的方法", 50, 0.8
        ));
        
        // 大类
        CODE_SMELL_PATTERNS.put("LARGE_CLASS", new CodeSmellPattern(
            "大类", "类过大，建议拆分职责", 20, 0.6
        ));
        
        // 重复代码
        CODE_SMELL_PATTERNS.put("DUPLICATE_CODE", new CodeSmellPattern(
            "重复代码", "发现重复的代码块，建议提取公共方法", 3, 0.9
        ));
        
        // 深度嵌套
        CODE_SMELL_PATTERNS.put("DEEP_NESTING", new CodeSmellPattern(
            "深度嵌套", "嵌套层次过深，影响代码可读性", 4, 0.7
        ));
    }
    
    /**
     * 性能模式
     */
    private static final List<PerformancePattern> PERFORMANCE_PATTERNS = Arrays.asList(
        new PerformancePattern("字符串拼接", Pattern.compile("\\+\\s*\""), "使用StringBuilder进行字符串拼接", 0.6),
        new PerformancePattern("循环中的对象创建", Pattern.compile("for\\s*\\([^)]*\\)\\s*\\{[^}]*new\\s+"), "避免在循环中创建对象", 0.7),
        new PerformancePattern("未关闭资源", Pattern.compile("new\\s+(FileInputStream|FileOutputStream|BufferedReader)"), "使用try-with-resources确保资源关闭", 0.8),
        new PerformancePattern("同步方法", Pattern.compile("synchronized\\s+[^{]*\\{"), "考虑使用更细粒度的同步", 0.5)
    );
    
    /**
     * 安全模式
     */
    private static final List<SecurityPattern> SECURITY_PATTERNS = Arrays.asList(
        new SecurityPattern("硬编码密码", Pattern.compile("(?i)(password|pwd|secret)\\s*=\\s*\"[^\"]+\""), "避免硬编码敏感信息", 0.9),
        new SecurityPattern("SQL注入风险", Pattern.compile("\"\\s*\\+\\s*[^\"]*\\s*\\+\\s*\""), "使用参数化查询防止SQL注入", 0.7),
        new SecurityPattern("随机数生成", Pattern.compile("new\\s+Random\\s*\\(\\s*\\)"), "使用SecureRandom生成安全随机数", 0.6),
        new SecurityPattern("反序列化", Pattern.compile("ObjectInputStream|readObject"), "反序列化可能存在安全风险", 0.8)
    );
    
    /**
     * 分析文本内容 (简化版本，PSI分析暂时禁用)
     */
    public List<SmartSuggestion> analyzeText(String filePath, String content) {
        List<SmartSuggestion> suggestions = new ArrayList<>();

        String[] lines = content.split("\n");

        // 基本的文本模式分析
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // 检查长行
            if (line.length() > 120) {
                SmartSuggestion suggestion = new SmartSuggestion(
                    SmartSuggestion.SuggestionType.REFACTOR,
                    SmartSuggestion.Priority.LOW,
                    "长行代码",
                    "这行代码过长，建议拆分以提高可读性",
                    filePath, i, i
                );
                suggestion.setConfidence(0.6);
                suggestions.add(suggestion);
            }
        }

        return suggestions;
    }
    
    // PSI相关的分析方法暂时移除，避免编译问题
    // 后续可以在解决依赖问题后重新添加
    
    /**
     * 代码异味模式
     */
    private static class CodeSmellPattern {
        final String name;
        final String description;
        final int threshold;
        final double confidence;
        
        CodeSmellPattern(String name, String description, int threshold, double confidence) {
            this.name = name;
            this.description = description;
            this.threshold = threshold;
            this.confidence = confidence;
        }
    }
    
    /**
     * 性能模式
     */
    private static class PerformancePattern {
        final String name;
        final Pattern pattern;
        final String description;
        final double confidence;
        
        PerformancePattern(String name, Pattern pattern, String description, double confidence) {
            this.name = name;
            this.pattern = pattern;
            this.description = description;
            this.confidence = confidence;
        }
    }
    
    /**
     * 安全模式
     */
    private static class SecurityPattern {
        final String name;
        final Pattern pattern;
        final String description;
        final double confidence;
        
        SecurityPattern(String name, Pattern pattern, String description, double confidence) {
            this.name = name;
            this.pattern = pattern;
            this.description = description;
            this.confidence = confidence;
        }
    }
}
