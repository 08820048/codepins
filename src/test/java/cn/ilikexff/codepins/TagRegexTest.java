package cn.ilikexff.codepins;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这个类用于测试标签正则表达式
 */
public class TagRegexTest {

    private static final Pattern TAG_PATTERN = Pattern.compile("#([\\w\\u4e00-\\u9fa5]+)");
    private static final Pattern PIN_PATTERN = Pattern.compile("@(cp|pin):?\\s*(.*?)(?:\\s+#[\\w\\u4e00-\\u9fa5]+)*");
    private static final Pattern PIN_BLOCK_PATTERN = Pattern.compile("@(cpb|pin[:-]block):?\\s*(.*?)(?:\\s+#[\\w\\u4e00-\\u9fa5]+)*");
    private static final Pattern PIN_BLOCK_RANGE_PATTERN = Pattern.compile("@cpb(\\d+)-(\\d+)\\s*(.*?)(?:\\s+#[\\w\\u4e00-\\u9fa5]+)*");

    public static void main(String[] args) {
        // 测试用例
        String[] testCases = {
            "// @cp 这是一个单行图钉测试 #测试 #单行",
            "// @cpb 这是一个代码块图钉测试 #测试 #代码块",
            "// @cpb1-10 这是一个带行号范围的代码块图钉测试 #测试 #行号范围",
            "// @cp 这是一个多标签测试 #重要 #待办 #高优先级",
            "// @cp 这是一个中文标签测试 #中文标签 #测试",
            "// @pin 这是旧格式测试 #测试 #兼容性",
            "// @pin-block 这是旧格式代码块测试 #测试 #代码块 #兼容性"
        };

        for (String testCase : testCases) {
            System.out.println("\n测试: " + testCase);
            
            // 提取标签
            List<String> tags = extractTags(testCase);
            System.out.println("提取的标签: " + tags);
            
            // 测试单行图钉正则
            testRegex(PIN_PATTERN, testCase, "单行图钉");
            
            // 测试代码块图钉正则
            testRegex(PIN_BLOCK_PATTERN, testCase, "代码块图钉");
            
            // 测试带行号范围的代码块图钉正则
            testRegex(PIN_BLOCK_RANGE_PATTERN, testCase, "带行号范围的代码块图钉");
        }
    }
    
    private static List<String> extractTags(String text) {
        List<String> tags = new ArrayList<>();
        Matcher tagMatcher = TAG_PATTERN.matcher(text);
        
        while (tagMatcher.find()) {
            tags.add(tagMatcher.group(1));
        }
        
        return tags;
    }
    
    private static void testRegex(Pattern pattern, String text, String patternName) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            System.out.println(patternName + " 匹配成功:");
            for (int i = 0; i <= matcher.groupCount(); i++) {
                System.out.println("  组 " + i + ": " + matcher.group(i));
            }
        } else {
            System.out.println(patternName + " 不匹配");
        }
    }
}
