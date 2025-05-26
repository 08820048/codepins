package cn.ilikexff.codepins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于调试正则表达式的测试类
 */
public class RegexDebugger {
    public static void main(String[] args) {
        // 测试用例
        String[] testComments = {
            "// @cp 这是一个测试 #重要",
            "// @cpb 这是一个代码块测试 #代码块",
            "// @cpb1-20 这是一个范围测试 #范围"
        };
        
        // 测试不同的正则表达式模式
        String[] patterns = {
            "@(cp|pin):?\\s*([^#]*?)(?:\\s+#[\\w\\u4e00-\\u9fa5]+)*",
            "@(cp|pin):?\\s*(.*?)(?:\\s+#[\\w\\u4e00-\\u9fa5]+)*",
            "@(cp|pin):?\\s*([^#]*)(?:\\s+#[\\w\\u4e00-\\u9fa5]+)*",
            "@(cp|pin):?\\s*([^#\\s]+(?:\\s+[^#\\s]+)*)(?:\\s+#[\\w\\u4e00-\\u9fa5]+)*"
        };
        
        for (String comment : testComments) {
            System.out.println("\n测试注释: " + comment);
            
            for (int i = 0; i < patterns.length; i++) {
                System.out.println("\n模式 " + (i + 1) + ": " + patterns[i]);
                Pattern pattern = Pattern.compile(patterns[i]);
                Matcher matcher = pattern.matcher(comment);
                
                if (matcher.find()) {
                    System.out.println("匹配成功!");
                    for (int j = 0; j <= matcher.groupCount(); j++) {
                        System.out.println("  组 " + j + ": [" + matcher.group(j) + "]");
                    }
                    
                    // 提取备注内容
                    String note = matcher.group(2).trim();
                    System.out.println("  提取的备注内容: [" + note + "]");
                } else {
                    System.out.println("匹配失败!");
                }
            }
        }
    }
}
