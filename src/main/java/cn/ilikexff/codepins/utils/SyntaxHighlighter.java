package cn.ilikexff.codepins.utils;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 语法高亮工具类
 * 用于对代码进行语法高亮分析
 */
public class SyntaxHighlighter {

    // 语言关键字映射
    private static final Map<String, String[]> LANGUAGE_KEYWORDS = new HashMap<>();
    
    // 语言注释符号映射
    private static final Map<String, String[]> LANGUAGE_COMMENTS = new HashMap<>();
    
    // 语言操作符映射
    private static final Map<String, String[]> LANGUAGE_OPERATORS = new HashMap<>();
    
    // 语言字符串定界符映射
    private static final Map<String, String[]> LANGUAGE_STRING_DELIMITERS = new HashMap<>();
    
    // 语言数字正则表达式映射
    private static final Map<String, String> LANGUAGE_NUMBER_REGEX = new HashMap<>();
    
    // 初始化语言定义
    static {
        // Java
        LANGUAGE_KEYWORDS.put("java", new String[]{
                "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
                "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
                "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
                "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
                "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
                "true", "false", "null"
        });
        LANGUAGE_COMMENTS.put("java", new String[]{"//", "/*", "*/"});
        LANGUAGE_OPERATORS.put("java", new String[]{
                "+", "-", "*", "/", "%", "=", "+=", "-=", "*=", "/=", "%=", "++", "--", "==", "!=", ">", "<",
                ">=", "<=", "&&", "||", "!", "&", "|", "^", "~", "<<", ">>", ">>>", "?", ":", "."
        });
        LANGUAGE_STRING_DELIMITERS.put("java", new String[]{"\"", "'"});
        LANGUAGE_NUMBER_REGEX.put("java", "\\b\\d+(\\.\\d+)?([fFdDlL])?\\b");
        
        // Python
        LANGUAGE_KEYWORDS.put("python", new String[]{
                "and", "as", "assert", "async", "await", "break", "class", "continue", "def", "del", "elif",
                "else", "except", "False", "finally", "for", "from", "global", "if", "import", "in", "is",
                "lambda", "None", "nonlocal", "not", "or", "pass", "raise", "return", "True", "try", "while",
                "with", "yield"
        });
        LANGUAGE_COMMENTS.put("python", new String[]{"#"});
        LANGUAGE_OPERATORS.put("python", new String[]{
                "+", "-", "*", "/", "//", "%", "**", "=", "+=", "-=", "*=", "/=", "//=", "%=", "**=", "==",
                "!=", ">", "<", ">=", "<=", "and", "or", "not", "is", "is not", "in", "not in"
        });
        LANGUAGE_STRING_DELIMITERS.put("python", new String[]{"\"", "'", "\"\"\"", "'''"});
        LANGUAGE_NUMBER_REGEX.put("python", "\\b\\d+(\\.\\d+)?([jJ])?\\b");
        
        // JavaScript
        LANGUAGE_KEYWORDS.put("javascript", new String[]{
                "await", "break", "case", "catch", "class", "const", "continue", "debugger", "default", "delete",
                "do", "else", "enum", "export", "extends", "false", "finally", "for", "function", "if", "implements",
                "import", "in", "instanceof", "interface", "let", "new", "null", "package", "private", "protected",
                "public", "return", "super", "switch", "static", "this", "throw", "try", "true", "typeof", "var",
                "void", "while", "with", "yield"
        });
        LANGUAGE_COMMENTS.put("javascript", new String[]{"//", "/*", "*/"});
        LANGUAGE_OPERATORS.put("javascript", new String[]{
                "+", "-", "*", "/", "%", "=", "+=", "-=", "*=", "/=", "%=", "++", "--", "==", "===", "!=", "!==",
                ">", "<", ">=", "<=", "&&", "||", "!", "&", "|", "^", "~", "<<", ">>", ">>>", "?", ":", "."
        });
        LANGUAGE_STRING_DELIMITERS.put("javascript", new String[]{"\"", "'", "`"});
        LANGUAGE_NUMBER_REGEX.put("javascript", "\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b");
        
        // C/C++
        LANGUAGE_KEYWORDS.put("cpp", new String[]{
                "auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else", "enum",
                "extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short", "signed",
                "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while",
                "class", "namespace", "try", "catch", "new", "delete", "this", "friend", "using", "public",
                "protected", "private", "virtual", "inline", "template", "typename", "true", "false", "nullptr"
        });
        LANGUAGE_COMMENTS.put("cpp", new String[]{"//", "/*", "*/"});
        LANGUAGE_OPERATORS.put("cpp", new String[]{
                "+", "-", "*", "/", "%", "=", "+=", "-=", "*=", "/=", "%=", "++", "--", "==", "!=", ">", "<",
                ">=", "<=", "&&", "||", "!", "&", "|", "^", "~", "<<", ">>", "?", ":", "::", "."
        });
        LANGUAGE_STRING_DELIMITERS.put("cpp", new String[]{"\"", "'"});
        LANGUAGE_NUMBER_REGEX.put("cpp", "\\b\\d+(\\.\\d+)?([uUlLfF])?\\b");
        
        // HTML
        LANGUAGE_KEYWORDS.put("html", new String[]{
                "!DOCTYPE", "html", "head", "title", "body", "h1", "h2", "h3", "h4", "h5", "h6", "p", "br", "hr",
                "div", "span", "a", "img", "ul", "ol", "li", "table", "tr", "td", "th", "form", "input", "button",
                "select", "option", "textarea", "script", "style", "link", "meta", "header", "footer", "nav", "section",
                "article", "aside", "main", "figure", "figcaption", "canvas", "audio", "video", "source"
        });
        LANGUAGE_COMMENTS.put("html", new String[]{"<!--", "-->"});
        LANGUAGE_OPERATORS.put("html", new String[]{"=", "<", ">", "/"});
        LANGUAGE_STRING_DELIMITERS.put("html", new String[]{"\"", "'"});
        LANGUAGE_NUMBER_REGEX.put("html", "\\b\\d+(\\.\\d+)?\\b");
        
        // CSS
        LANGUAGE_KEYWORDS.put("css", new String[]{
                "@media", "@import", "@charset", "@keyframes", "@font-face", "@page", "@supports", "!important",
                "color", "background", "margin", "padding", "font", "border", "width", "height", "display", "position",
                "top", "right", "bottom", "left", "float", "clear", "z-index", "overflow", "text-align", "line-height",
                "flex", "grid", "transition", "animation", "transform", "opacity", "visibility"
        });
        LANGUAGE_COMMENTS.put("css", new String[]{"/*", "*/"});
        LANGUAGE_OPERATORS.put("css", new String[]{":", ";", ",", ".", "#", ">", "+", "~", "*", "="});
        LANGUAGE_STRING_DELIMITERS.put("css", new String[]{"\"", "'"});
        LANGUAGE_NUMBER_REGEX.put("css", "\\b\\d+(\\.\\d+)?(px|em|rem|%|vh|vw|pt|pc|in|cm|mm|ex|ch)?\\b");
    }
    
    // 颜色定义
    public static class SyntaxColors {
        public final Color keywordColor;
        public final Color commentColor;
        public final Color stringColor;
        public final Color numberColor;
        public final Color operatorColor;
        public final Color defaultColor;
        
        public SyntaxColors(Color defaultColor) {
            this.keywordColor = new Color(198, 120, 221); // 紫色
            this.commentColor = new Color(92, 99, 112);   // 灰色
            this.stringColor = new Color(152, 195, 121);  // 绿色
            this.numberColor = new Color(209, 154, 102);  // 橙色
            this.operatorColor = new Color(86, 182, 194); // 青色
            this.defaultColor = defaultColor;             // 默认颜色
        }
        
        public SyntaxColors(Color keywordColor, Color commentColor, Color stringColor, 
                           Color numberColor, Color operatorColor, Color defaultColor) {
            this.keywordColor = keywordColor;
            this.commentColor = commentColor;
            this.stringColor = stringColor;
            this.numberColor = numberColor;
            this.operatorColor = operatorColor;
            this.defaultColor = defaultColor;
        }
    }
    
    /**
     * 获取行的语法高亮颜色
     * 
     * @param line 代码行
     * @param language 编程语言
     * @param colors 语法颜色
     * @return 行的颜色
     */
    public static Color getLineColor(String line, String language, SyntaxColors colors) {
        // 如果语言不支持，返回默认颜色
        if (!LANGUAGE_COMMENTS.containsKey(language)) {
            return colors.defaultColor;
        }
        
        // 检查是否是注释
        String[] comments = LANGUAGE_COMMENTS.get(language);
        for (String comment : comments) {
            if (comment.length() == 2 && line.trim().startsWith(comment)) {
                return colors.commentColor;
            }
        }
        
        // 简单检查是否包含字符串
        String[] stringDelimiters = LANGUAGE_STRING_DELIMITERS.get(language);
        for (String delimiter : stringDelimiters) {
            if (line.contains(delimiter) && line.indexOf(delimiter) != line.lastIndexOf(delimiter)) {
                return colors.stringColor;
            }
        }
        
        // 检查是否包含关键字
        String[] keywords = LANGUAGE_KEYWORDS.get(language);
        for (String keyword : keywords) {
            // 使用正则表达式确保关键字是独立的单词
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                return colors.keywordColor;
            }
        }
        
        // 检查是否包含数字
        String numberRegex = LANGUAGE_NUMBER_REGEX.get(language);
        Pattern numberPattern = Pattern.compile(numberRegex);
        Matcher numberMatcher = numberPattern.matcher(line);
        if (numberMatcher.find()) {
            return colors.numberColor;
        }
        
        // 检查是否包含操作符
        String[] operators = LANGUAGE_OPERATORS.get(language);
        for (String operator : operators) {
            if (line.contains(operator)) {
                return colors.operatorColor;
            }
        }
        
        // 默认颜色
        return colors.defaultColor;
    }
    
    /**
     * 对代码行进行语法高亮分析
     * 
     * @param line 代码行
     * @param language 编程语言
     * @param colors 语法颜色
     * @return 代码行的语法高亮信息
     */
    public static Map<String, Object> analyzeLine(String line, String language, SyntaxColors colors) {
        Map<String, Object> result = new HashMap<>();
        result.put("line", line);
        result.put("color", getLineColor(line, language, colors));
        return result;
    }
    
    /**
     * 判断语言是否支持
     * 
     * @param language 编程语言
     * @return 是否支持
     */
    public static boolean isLanguageSupported(String language) {
        return LANGUAGE_KEYWORDS.containsKey(language);
    }
    
    /**
     * 获取支持的语言列表
     * 
     * @return 支持的语言列表
     */
    public static String[] getSupportedLanguages() {
        return LANGUAGE_KEYWORDS.keySet().toArray(new String[0]);
    }
}
