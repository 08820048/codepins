package cn.ilikexff.codepins.ai;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 智能建议引擎
 * 基于代码分析生成智能建议
 */
public class SmartSuggestionEngine {
    
    // 常见的问题模式
    private static final Map<Pattern, SmartSuggestion.SuggestionType> PATTERN_SUGGESTIONS = new HashMap<>();
    
    static {
        // TODO相关模式
        PATTERN_SUGGESTIONS.put(Pattern.compile("(?i)\\b(todo|fixme|hack|xxx)\\b"), SmartSuggestion.SuggestionType.TODO);
        
        // 性能问题模式
        PATTERN_SUGGESTIONS.put(Pattern.compile("(?i)\\b(sleep|thread\\.sleep|wait)\\s*\\("), SmartSuggestion.SuggestionType.OPTIMIZE);
        
        // 安全问题模式
        PATTERN_SUGGESTIONS.put(Pattern.compile("(?i)\\b(password|secret|key)\\s*=\\s*[\"'][^\"']*[\"']"), SmartSuggestion.SuggestionType.SECURITY);
        
        // 过时API模式
        PATTERN_SUGGESTIONS.put(Pattern.compile("(?i)\\b(deprecated|obsolete)\\b"), SmartSuggestion.SuggestionType.DEPRECATED);
    }
    
    private final Project project;
    private final CodeQualityAnalyzer qualityAnalyzer;
    
    public SmartSuggestionEngine(Project project) {
        this.project = project;
        this.qualityAnalyzer = new CodeQualityAnalyzer();
    }
    
    /**
     * 分析文件并生成建议
     */
    public List<SmartSuggestion> analyzeFile(VirtualFile file, Document document) {
        List<SmartSuggestion> suggestions = new ArrayList<>();

        if (file == null || document == null) {
            System.out.println("[SmartSuggestionEngine] 文件或文档为空");
            return suggestions;
        }

        String filePath = file.getPath();
        String content = document.getText();
        String[] lines = content.split("\n");

        System.out.println("[SmartSuggestionEngine] 开始分析文件: " + filePath + ", 行数: " + lines.length);

        // 基于文本模式的分析
        List<SmartSuggestion> textSuggestions = analyzeTextPatterns(filePath, lines);
        suggestions.addAll(textSuggestions);
        System.out.println("[SmartSuggestionEngine] 文本模式分析完成，建议数: " + textSuggestions.size());

        // 代码质量分析
        List<SmartSuggestion> qualitySuggestions = qualityAnalyzer.analyzeQuality(filePath, content);
        suggestions.addAll(qualitySuggestions);
        System.out.println("[SmartSuggestionEngine] 代码质量分析完成，建议数: " + qualitySuggestions.size());

        // 基于PSI的代码结构分析 (暂时禁用，避免编译问题)
        // PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        // if (psiFile != null) {
        //     suggestions.addAll(analyzePsiStructure(psiFile));
        // }

        // 如果没有找到建议，添加一个测试建议
        if (suggestions.isEmpty()) {
            SmartSuggestion testSuggestion = new SmartSuggestion(
                SmartSuggestion.SuggestionType.DOCUMENTATION,
                SmartSuggestion.Priority.LOW,
                "测试建议",
                "这是一个测试建议，用于验证系统正常工作",
                filePath, 0, 0
            );
            testSuggestion.setConfidence(0.8);
            testSuggestion.setReason("系统测试");
            suggestions.add(testSuggestion);
            System.out.println("[SmartSuggestionEngine] 添加了测试建议");
        }

        // 按优先级和置信度排序
        suggestions.sort((s1, s2) -> Integer.compare(s2.getSeverityScore(), s1.getSeverityScore()));

        System.out.println("[SmartSuggestionEngine] 分析完成，总建议数: " + suggestions.size());
        return suggestions;
    }
    
    /**
     * 基于文本模式分析
     */
    private List<SmartSuggestion> analyzeTextPatterns(String filePath, String[] lines) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // 检查各种模式
            for (Map.Entry<Pattern, SmartSuggestion.SuggestionType> entry : PATTERN_SUGGESTIONS.entrySet()) {
                Matcher matcher = entry.getKey().matcher(line);
                if (matcher.find()) {
                    SmartSuggestion suggestion = createSuggestionFromPattern(
                        entry.getValue(), filePath, i, line, matcher.group()
                    );
                    suggestions.add(suggestion);
                }
            }
            
            // 检查长行（可能需要重构）
            if (line.length() > 120) {
                SmartSuggestion suggestion = new SmartSuggestion(
                    SmartSuggestion.SuggestionType.REFACTOR,
                    SmartSuggestion.Priority.LOW,
                    "长行代码",
                    "这行代码过长，建议拆分以提高可读性",
                    filePath, i, i
                );
                suggestion.setConfidence(0.6);
                suggestion.setReason("代码行长度超过120字符");
                suggestions.add(suggestion);
            }
            
            // 检查空的catch块
            if (line.trim().equals("} catch") || line.contains("catch") && line.contains("{}")) {
                SmartSuggestion suggestion = new SmartSuggestion(
                    SmartSuggestion.SuggestionType.CODE_SMELL,
                    SmartSuggestion.Priority.MEDIUM,
                    "空的异常处理",
                    "空的catch块可能隐藏重要错误",
                    filePath, i, i
                );
                suggestion.setConfidence(0.8);
                suggestion.setReason("发现空的异常处理块");
                suggestions.add(suggestion);
            }
            
            // 检查硬编码字符串
            if (line.contains("\"") && !line.trim().startsWith("//") && !line.trim().startsWith("*")) {
                Pattern hardcodedPattern = Pattern.compile("\"[^\"]{10,}\"");
                Matcher matcher = hardcodedPattern.matcher(line);
                if (matcher.find()) {
                    SmartSuggestion suggestion = new SmartSuggestion(
                        SmartSuggestion.SuggestionType.BEST_PRACTICE,
                        SmartSuggestion.Priority.LOW,
                        "硬编码字符串",
                        "考虑将长字符串提取为常量",
                        filePath, i, i
                    );
                    suggestion.setConfidence(0.4);
                    suggestion.setReason("发现较长的硬编码字符串");
                    suggestions.add(suggestion);
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * 基于PSI结构分析 (暂时禁用)
     */
    /*
    private List<SmartSuggestion> analyzePsiStructure(PsiFile psiFile) {
        List<SmartSuggestion> suggestions = new ArrayList<>();

        // 分析方法复杂度
        Collection<PsiMethod> methods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class);
        for (PsiMethod method : methods) {
            suggestions.addAll(analyzeMethod(method));
        }

        // 分析类结构
        Collection<PsiClass> classes = PsiTreeUtil.findChildrenOfType(psiFile, PsiClass.class);
        for (PsiClass psiClass : classes) {
            suggestions.addAll(analyzeClass(psiClass));
        }

        return suggestions;
    }
    */
    
    // PSI相关的分析方法暂时禁用，避免编译问题
    // 后续可以在解决依赖问题后重新启用
    
    /**
     * 从模式创建建议
     */
    private SmartSuggestion createSuggestionFromPattern(SmartSuggestion.SuggestionType type, 
                                                       String filePath, int line, String content, String match) {
        SmartSuggestion suggestion = new SmartSuggestion();
        suggestion.setType(type);
        suggestion.setFilePath(filePath);
        suggestion.setStartLine(line);
        suggestion.setEndLine(line);
        suggestion.setCodeSnippet(content.trim());
        
        switch (type) {
            case TODO:
                suggestion.setTitle("发现TODO标记");
                suggestion.setDescription("代码中包含TODO标记: " + match);
                suggestion.setPriority(SmartSuggestion.Priority.MEDIUM);
                suggestion.setConfidence(0.9);
                break;
            case OPTIMIZE:
                suggestion.setTitle("性能优化机会");
                suggestion.setDescription("发现可能影响性能的代码: " + match);
                suggestion.setPriority(SmartSuggestion.Priority.HIGH);
                suggestion.setConfidence(0.7);
                break;
            case SECURITY:
                suggestion.setTitle("安全风险");
                suggestion.setDescription("发现潜在的安全问题: " + match);
                suggestion.setPriority(SmartSuggestion.Priority.CRITICAL);
                suggestion.setConfidence(0.8);
                break;
            case DEPRECATED:
                suggestion.setTitle("过时代码");
                suggestion.setDescription("使用了过时的API或方法: " + match);
                suggestion.setPriority(SmartSuggestion.Priority.MEDIUM);
                suggestion.setConfidence(0.6);
                break;
        }
        
        return suggestion;
    }
    
    // PSI相关的建议创建方法暂时移除
    
    // PSI相关的辅助方法暂时移除
}
