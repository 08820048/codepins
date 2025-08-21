package cn.ilikexff.codepins.template;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板变量处理器
 * 负责处理模板中的变量替换
 */
public class TemplateVariableProcessor {
    
    // 变量匹配模式：{变量名}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    // 预定义变量
    private static final String VAR_DATE = "date";
    private static final String VAR_TIME = "time";
    private static final String VAR_DATETIME = "datetime";
    private static final String VAR_AUTHOR = "author";
    private static final String VAR_USER = "user";
    private static final String VAR_YEAR = "year";
    private static final String VAR_MONTH = "month";
    private static final String VAR_DAY = "day";
    private static final String VAR_HOUR = "hour";
    private static final String VAR_MINUTE = "minute";
    private static final String VAR_SECOND = "second";
    
    /**
     * 处理模板内容，替换所有变量
     * 
     * @param template 原始模板内容
     * @param customVariables 自定义变量映射
     * @return 处理后的内容
     */
    public static String processTemplate(String template, Map<String, String> customVariables) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        
        // 合并预定义变量和自定义变量
        Map<String, String> allVariables = new HashMap<>();
        allVariables.putAll(getBuiltInVariables());
        if (customVariables != null) {
            allVariables.putAll(customVariables);
        }
        
        // 替换变量
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String replacement = allVariables.getOrDefault(variableName, matcher.group(0));
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 处理模板内容，仅使用预定义变量
     * 
     * @param template 原始模板内容
     * @return 处理后的内容
     */
    public static String processTemplate(String template) {
        return processTemplate(template, null);
    }
    
    /**
     * 获取预定义变量映射
     * 
     * @return 变量名到值的映射
     */
    public static Map<String, String> getBuiltInVariables() {
        Map<String, String> variables = new HashMap<>();
        Date now = new Date();
        
        // 日期时间相关
        variables.put(VAR_DATE, new SimpleDateFormat("yyyy-MM-dd").format(now));
        variables.put(VAR_TIME, new SimpleDateFormat("HH:mm:ss").format(now));
        variables.put(VAR_DATETIME, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now));
        variables.put(VAR_YEAR, new SimpleDateFormat("yyyy").format(now));
        variables.put(VAR_MONTH, new SimpleDateFormat("MM").format(now));
        variables.put(VAR_DAY, new SimpleDateFormat("dd").format(now));
        variables.put(VAR_HOUR, new SimpleDateFormat("HH").format(now));
        variables.put(VAR_MINUTE, new SimpleDateFormat("mm").format(now));
        variables.put(VAR_SECOND, new SimpleDateFormat("ss").format(now));
        
        // 用户相关
        String userName = System.getProperty("user.name", "Unknown");
        variables.put(VAR_AUTHOR, userName);
        variables.put(VAR_USER, userName);
        
        return variables;
    }
    
    /**
     * 获取模板中使用的所有变量
     * 
     * @param template 模板内容
     * @return 变量名列表
     */
    public static java.util.List<String> extractVariables(String template) {
        java.util.List<String> variables = new java.util.ArrayList<>();
        if (template == null || template.isEmpty()) {
            return variables;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            String variableName = matcher.group(1);
            if (!variables.contains(variableName)) {
                variables.add(variableName);
            }
        }
        
        return variables;
    }
    
    /**
     * 验证模板中的变量是否都有对应的值
     * 
     * @param template 模板内容
     * @param customVariables 自定义变量映射
     * @return 未定义的变量列表
     */
    public static java.util.List<String> validateTemplate(String template, Map<String, String> customVariables) {
        java.util.List<String> undefinedVariables = new java.util.ArrayList<>();
        java.util.List<String> usedVariables = extractVariables(template);
        
        Map<String, String> allVariables = new HashMap<>();
        allVariables.putAll(getBuiltInVariables());
        if (customVariables != null) {
            allVariables.putAll(customVariables);
        }
        
        for (String variable : usedVariables) {
            if (!allVariables.containsKey(variable)) {
                undefinedVariables.add(variable);
            }
        }
        
        return undefinedVariables;
    }
    
    /**
     * 获取所有可用的预定义变量名称
     * 
     * @return 变量名称列表
     */
    public static java.util.List<String> getAvailableVariables() {
        java.util.List<String> variables = new java.util.ArrayList<>();
        variables.add(VAR_DATE);
        variables.add(VAR_TIME);
        variables.add(VAR_DATETIME);
        variables.add(VAR_AUTHOR);
        variables.add(VAR_USER);
        variables.add(VAR_YEAR);
        variables.add(VAR_MONTH);
        variables.add(VAR_DAY);
        variables.add(VAR_HOUR);
        variables.add(VAR_MINUTE);
        variables.add(VAR_SECOND);
        return variables;
    }
    
    /**
     * 获取变量的描述信息
     * 
     * @param variableName 变量名
     * @return 变量描述
     */
    public static String getVariableDescription(String variableName) {
        switch (variableName) {
            case VAR_DATE:
                return "当前日期 (yyyy-MM-dd)";
            case VAR_TIME:
                return "当前时间 (HH:mm:ss)";
            case VAR_DATETIME:
                return "当前日期时间 (yyyy-MM-dd HH:mm:ss)";
            case VAR_AUTHOR:
            case VAR_USER:
                return "当前用户名";
            case VAR_YEAR:
                return "当前年份";
            case VAR_MONTH:
                return "当前月份";
            case VAR_DAY:
                return "当前日期";
            case VAR_HOUR:
                return "当前小时";
            case VAR_MINUTE:
                return "当前分钟";
            case VAR_SECOND:
                return "当前秒数";
            default:
                return "自定义变量";
        }
    }
}
