package cn.ilikexff.codepins.ai;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码质量分析器
 * 基于静态分析规则检测代码质量问题
 */
public class CodeQualityAnalyzer {
    
    /**
     * 质量规则定义
     */
    private static final List<QualityRule> QUALITY_RULES = Arrays.asList(
        // 命名规范
        new QualityRule(
            "NAMING_CONVENTION",
            "命名规范",
            Pattern.compile("\\b[a-z][a-zA-Z0-9]*\\s*="),
            SmartSuggestion.SuggestionType.BEST_PRACTICE,
            SmartSuggestion.Priority.LOW,
            "变量命名应使用驼峰命名法",
            0.6
        ),
        
        // 魔法数字 - 使用更智能的检测逻辑
        new QualityRule(
            "MAGIC_NUMBER",
            "魔法数字",
            Pattern.compile("\\b(?!0|1|2|10|100|1000)\\d{2,}\\b"),
            SmartSuggestion.SuggestionType.REFACTOR,
            SmartSuggestion.Priority.MEDIUM,
            "避免使用魔法数字，建议定义为常量",
            0.7
        ) {
            @Override
            public boolean matches(String line) {
                // 使用智能上下文检测
                return isActualMagicNumber(line, pattern);
            }
        },
        
        // 重复字符串
        new QualityRule(
            "DUPLICATE_STRING",
            "重复字符串",
            Pattern.compile("\"([^\"]{5,})\".*\"\\1\""),
            SmartSuggestion.SuggestionType.REFACTOR,
            SmartSuggestion.Priority.MEDIUM,
            "重复的字符串应提取为常量",
            0.8
        ),
        
        // 空方法体
        new QualityRule(
            "EMPTY_METHOD",
            "空方法体",
            Pattern.compile("\\{\\s*\\}"),
            SmartSuggestion.SuggestionType.CODE_SMELL,
            SmartSuggestion.Priority.LOW,
            "空方法体可能表示未完成的实现",
            0.5
        ),
        
        // 过长的方法参数
        new QualityRule(
            "LONG_PARAMETER_LIST",
            "参数列表过长",
            Pattern.compile("\\([^)]{80,}\\)"),
            SmartSuggestion.SuggestionType.REFACTOR,
            SmartSuggestion.Priority.MEDIUM,
            "参数列表过长，考虑使用参数对象",
            0.6
        ),
        
        // 深度嵌套
        new QualityRule(
            "DEEP_NESTING",
            "深度嵌套",
            Pattern.compile("(\\s{12,})(if|for|while|try)"),
            SmartSuggestion.SuggestionType.COMPLEXITY,
            SmartSuggestion.Priority.HIGH,
            "嵌套层次过深，影响代码可读性",
            0.8
        ),
        
        // 未使用的导入
        new QualityRule(
            "UNUSED_IMPORT",
            "未使用的导入",
            Pattern.compile("^import\\s+[^;]+;$"),
            SmartSuggestion.SuggestionType.CODE_SMELL,
            SmartSuggestion.Priority.LOW,
            "可能存在未使用的导入语句",
            0.4
        ),
        
        // 异常处理
        new QualityRule(
            "GENERIC_EXCEPTION",
            "通用异常捕获",
            Pattern.compile("catch\\s*\\(\\s*Exception\\s+"),
            SmartSuggestion.SuggestionType.BEST_PRACTICE,
            SmartSuggestion.Priority.MEDIUM,
            "避免捕获通用Exception，应捕获具体异常类型",
            0.7
        ),
        
        // 性能问题
        new QualityRule(
            "STRING_CONCATENATION",
            "字符串拼接",
            Pattern.compile("\\+\\s*\"[^\"]*\"\\s*\\+"),
            SmartSuggestion.SuggestionType.OPTIMIZE,
            SmartSuggestion.Priority.MEDIUM,
            "频繁的字符串拼接，建议使用StringBuilder",
            0.6
        ),
        
        // 安全问题
        new QualityRule(
            "SQL_INJECTION_RISK",
            "SQL注入风险",
            Pattern.compile("(SELECT|INSERT|UPDATE|DELETE).*\\+.*"),
            SmartSuggestion.SuggestionType.SECURITY,
            SmartSuggestion.Priority.CRITICAL,
            "SQL语句拼接可能存在注入风险，使用参数化查询",
            0.9
        )
    );
    
    /**
     * 复杂度分析规则
     */
    private static final Map<String, ComplexityRule> COMPLEXITY_RULES = new HashMap<>();
    
    static {
        COMPLEXITY_RULES.put("CYCLOMATIC", new ComplexityRule(
            "圈复杂度", 10, "方法的圈复杂度过高，建议拆分方法"
        ));
        COMPLEXITY_RULES.put("LINE_COUNT", new ComplexityRule(
            "方法行数", 50, "方法行数过多，建议拆分为更小的方法"
        ));
        COMPLEXITY_RULES.put("PARAMETER_COUNT", new ComplexityRule(
            "参数数量", 5, "参数数量过多，考虑使用参数对象"
        ));
    }
    
    /**
     * 分析代码质量
     */
    public List<SmartSuggestion> analyzeQuality(String filePath, String content) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        String[] lines = content.split("\n");
        
        // 逐行分析
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            suggestions.addAll(analyzeLineQuality(filePath, line, i));
        }
        
        // 整体分析
        suggestions.addAll(analyzeOverallQuality(filePath, content, lines));
        
        // 方法级分析
        suggestions.addAll(analyzeMethodComplexity(filePath, content, lines));
        
        return suggestions;
    }
    
    /**
     * 分析单行代码质量
     */
    private List<SmartSuggestion> analyzeLineQuality(String filePath, String line, int lineNumber) {
        List<SmartSuggestion> suggestions = new ArrayList<>();

        // 跳过CodePins特殊注释指令
        if (isCodePinsComment(line)) {
            return suggestions;
        }

        for (QualityRule rule : QUALITY_RULES) {
            if (rule.matches(line)) {
                Matcher matcher = rule.pattern.matcher(line);
                SmartSuggestion suggestion = new SmartSuggestion(
                    rule.type,
                    rule.priority,
                    rule.name,
                    rule.description,
                    filePath,
                    lineNumber,
                    lineNumber
                );
                suggestion.setConfidence(rule.confidence);
                suggestion.setReason("代码质量检测: " + rule.name);
                suggestion.setCodeSnippet(line.trim());
                suggestions.add(suggestion);
            }
        }
        
        return suggestions;
    }

    /**
     * 检查是否为CodePins特殊注释指令
     */
    private boolean isCodePinsComment(String line) {
        String trimmed = line.trim();

        // 检查CodePins的特殊注释格式
        // 格式: //@cp... 或 //@cpb... 或 //@cpr...
        if (trimmed.matches("^//\\s*@cp[br]?\\d+.*")) {
            return true;
        }

        // 检查包含CodePins标签的注释
        if (trimmed.startsWith("//") && (
            trimmed.contains("#") ||  // 包含标签
            trimmed.contains("@cp") || // CodePins指令
            trimmed.matches(".*\\d+-\\d+.*") // 包含范围格式
        )) {
            return true;
        }

        return false;
    }

    /**
     * 智能检测是否为真正的魔法数字
     */
    private static boolean isActualMagicNumber(String line, Pattern pattern) {
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) {
            return false;
        }

        String trimmed = line.trim();

        // 排除注解参数
        if (trimmed.contains("@") && (trimmed.contains("(") || trimmed.contains("="))) {
            return false;
        }

        // 排除配置常量定义
        if (trimmed.contains("final") || trimmed.contains("static") || trimmed.contains("const")) {
            return false;
        }

        // 排除枚举值
        if (trimmed.matches(".*[A-Z_]+\\s*\\(.*\\d+.*\\).*")) {
            return false;
        }

        // 排除数组索引和长度
        if (trimmed.contains("[") && trimmed.contains("]")) {
            return false;
        }

        // 排除时间相关的常见数字
        String number = matcher.group();
        if (isTimeRelatedNumber(number)) {
            return false;
        }

        // 排除HTTP状态码
        if (isHttpStatusCode(number)) {
            return false;
        }

        // 排除端口号
        if (isPortNumber(number)) {
            return false;
        }

        // 排除版本号
        if (trimmed.matches(".*version.*\\d+.*") || trimmed.matches(".*v\\d+.*")) {
            return false;
        }

        // 排除测试数据
        if (trimmed.toLowerCase().contains("test") || trimmed.toLowerCase().contains("mock")) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否为时间相关数字
     */
    private static boolean isTimeRelatedNumber(String number) {
        int num = Integer.parseInt(number);
        // 常见时间数字：60(秒/分), 24(小时), 7(天), 30(天), 365(天), 1000(毫秒), 3600(秒)
        return num == 60 || num == 24 || num == 7 || num == 30 || num == 365 ||
               num == 1000 || num == 3600 || num == 86400;
    }

    /**
     * 检查是否为HTTP状态码
     */
    private static boolean isHttpStatusCode(String number) {
        int num = Integer.parseInt(number);
        return (num >= 100 && num < 600);
    }

    /**
     * 检查是否为端口号
     */
    private static boolean isPortNumber(String number) {
        int num = Integer.parseInt(number);
        return (num >= 1024 && num <= 65535);
    }

    /**
     * 分析整体代码质量
     */
    private List<SmartSuggestion> analyzeOverallQuality(String filePath, String content, String[] lines) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        // 检查文件长度
        if (lines.length > 500) {
            SmartSuggestion suggestion = new SmartSuggestion(
                SmartSuggestion.SuggestionType.REFACTOR,
                SmartSuggestion.Priority.MEDIUM,
                "文件过大",
                "文件包含" + lines.length + "行代码，建议拆分为多个文件",
                filePath, 0, lines.length - 1
            );
            suggestion.setConfidence(0.7);
            suggestion.setReason("文件行数超过建议阈值");
            suggestions.add(suggestion);
        }
        
        // 检查注释密度
        int commentLines = 0;
        int codeLines = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                commentLines++;
            } else if (!trimmed.isEmpty()) {
                codeLines++;
            }
        }
        
        if (codeLines > 50 && commentLines < codeLines * 0.1) {
            SmartSuggestion suggestion = new SmartSuggestion(
                SmartSuggestion.SuggestionType.DOCUMENTATION,
                SmartSuggestion.Priority.LOW,
                "注释不足",
                "代码注释密度较低，建议增加必要的注释",
                filePath, 0, lines.length - 1
            );
            suggestion.setConfidence(0.5);
            suggestion.setReason("注释密度低于10%");
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * 分析方法复杂度
     */
    private List<SmartSuggestion> analyzeMethodComplexity(String filePath, String content, String[] lines) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        // 简单的方法检测（基于缩进和关键字）
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            // 检测方法定义
            if (isMethodDefinition(line)) {
                MethodInfo method = extractMethodInfo(lines, i);
                if (method != null) {
                    suggestions.addAll(analyzeMethodComplexity(filePath, method));
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * 检查是否为方法定义
     */
    private boolean isMethodDefinition(String line) {
        return line.matches(".*\\b(public|private|protected|static).*\\(.*\\).*\\{?") &&
               !line.contains("=") && !line.startsWith("//");
    }
    
    /**
     * 提取方法信息
     */
    private MethodInfo extractMethodInfo(String[] lines, int startLine) {
        MethodInfo method = new MethodInfo();
        method.startLine = startLine;
        method.name = extractMethodName(lines[startLine]);
        
        // 计算方法行数和复杂度
        int braceCount = 0;
        int lineCount = 0;
        int complexity = 1; // 基础复杂度
        
        for (int i = startLine; i < lines.length; i++) {
            String line = lines[i];
            lineCount++;
            
            // 计算大括号
            for (char c : line.toCharArray()) {
                if (c == '{') braceCount++;
                if (c == '}') braceCount--;
            }
            
            // 计算圈复杂度
            if (line.contains("if") || line.contains("while") || line.contains("for") ||
                line.contains("case") || line.contains("catch")) {
                complexity++;
            }
            
            // 方法结束
            if (braceCount == 0 && i > startLine) {
                method.endLine = i;
                method.lineCount = lineCount;
                method.complexity = complexity;
                break;
            }
        }
        
        return method.endLine > method.startLine ? method : null;
    }
    
    /**
     * 提取方法名
     */
    private String extractMethodName(String line) {
        Pattern pattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
        Matcher matcher = pattern.matcher(line);
        return matcher.find() ? matcher.group(1) : "unknown";
    }
    
    /**
     * 分析方法复杂度
     */
    private List<SmartSuggestion> analyzeMethodComplexity(String filePath, MethodInfo method) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        // 检查行数
        if (method.lineCount > COMPLEXITY_RULES.get("LINE_COUNT").threshold) {
            SmartSuggestion suggestion = new SmartSuggestion(
                SmartSuggestion.SuggestionType.REFACTOR,
                SmartSuggestion.Priority.MEDIUM,
                "方法过长",
                "方法 " + method.name + " 包含 " + method.lineCount + " 行代码，" +
                COMPLEXITY_RULES.get("LINE_COUNT").description,
                filePath, method.startLine, method.endLine
            );
            suggestion.setConfidence(0.8);
            suggestions.add(suggestion);
        }
        
        // 检查圈复杂度
        if (method.complexity > COMPLEXITY_RULES.get("CYCLOMATIC").threshold) {
            SmartSuggestion suggestion = new SmartSuggestion(
                SmartSuggestion.SuggestionType.COMPLEXITY,
                SmartSuggestion.Priority.HIGH,
                "复杂度过高",
                "方法 " + method.name + " 的圈复杂度为 " + method.complexity + "，" +
                COMPLEXITY_RULES.get("CYCLOMATIC").description,
                filePath, method.startLine, method.endLine
            );
            suggestion.setConfidence(0.9);
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * 质量规则定义
     */
    private static class QualityRule {
        final String id;
        final String name;
        final Pattern pattern;
        final SmartSuggestion.SuggestionType type;
        final SmartSuggestion.Priority priority;
        final String description;
        final double confidence;

        QualityRule(String id, String name, Pattern pattern, SmartSuggestion.SuggestionType type,
                   SmartSuggestion.Priority priority, String description, double confidence) {
            this.id = id;
            this.name = name;
            this.pattern = pattern;
            this.type = type;
            this.priority = priority;
            this.description = description;
            this.confidence = confidence;
        }

        /**
         * 检查是否匹配，子类可以重写此方法实现自定义逻辑
         */
        public boolean matches(String line) {
            return pattern.matcher(line).find();
        }
    }
    
    /**
     * 复杂度规则定义
     */
    private static class ComplexityRule {
        final String name;
        final int threshold;
        final String description;
        
        ComplexityRule(String name, int threshold, String description) {
            this.name = name;
            this.threshold = threshold;
            this.description = description;
        }
    }
    
    /**
     * 方法信息
     */
    private static class MethodInfo {
        String name;
        int startLine;
        int endLine;
        int lineCount;
        int complexity;
    }
}
