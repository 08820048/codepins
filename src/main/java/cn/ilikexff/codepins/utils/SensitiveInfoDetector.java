package cn.ilikexff.codepins.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感信息检测工具类
 * 用于检测代码中的敏感信息
 */
public class SensitiveInfoDetector {

    // 敏感信息类型
    public enum SensitiveType {
        API_KEY("API密钥", "可能包含API密钥"),
        PASSWORD("密码", "可能包含密码"),
        TOKEN("令牌", "可能包含访问令牌"),
        SECRET_KEY("密钥", "可能包含密钥"),
        CREDENTIAL("凭证", "可能包含凭证信息"),
        PRIVATE_KEY("私钥", "可能包含私钥"),
        ACCESS_KEY("访问密钥", "可能包含访问密钥"),
        DATABASE_CONNECTION("数据库连接", "可能包含数据库连接信息"),
        EMAIL("电子邮件", "可能包含电子邮件地址"),
        PHONE("电话号码", "可能包含电话号码"),
        IP_ADDRESS("IP地址", "可能包含IP地址"),
        PERSONAL_ID("个人ID", "可能包含个人身份信息");

        private final String displayName;
        private final String description;

        SensitiveType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    // 敏感信息结果
    public static class SensitiveResult {
        private final SensitiveType type;
        private final String content;
        private final int lineNumber;
        private final String suggestion;

        public SensitiveResult(SensitiveType type, String content, int lineNumber, String suggestion) {
            this.type = type;
            this.content = content;
            this.lineNumber = lineNumber;
            this.suggestion = suggestion;
        }

        public SensitiveType getType() {
            return type;
        }

        public String getContent() {
            return content;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getSuggestion() {
            return suggestion;
        }

        @Override
        public String toString() {
            return "第" + lineNumber + "行: " + type.getDisplayName() + " - " + content + "\n建议: " + suggestion;
        }
    }

    // 敏感信息正则表达式
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(?i)(api[_-]?key|apikey)[\\s]*[=:][\\s]*[\"|']([\\w\\d]{16,})[\"|']");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password|passwd|pwd)[\\s]*[=:][\\s]*[\"|']([^\"']{4,})[\"|']");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(?i)(token|access[_-]?token|auth[_-]?token)[\\s]*[=:][\\s]*[\"|']([\\w\\d._-]{10,})[\"|']");
    private static final Pattern SECRET_KEY_PATTERN = Pattern.compile("(?i)(secret|secret[_-]?key)[\\s]*[=:][\\s]*[\"|']([\\w\\d._-]{10,})[\"|']");
    private static final Pattern CREDENTIAL_PATTERN = Pattern.compile("(?i)(credential|cred)[\\s]*[=:][\\s]*[\"|']([\\w\\d._-]{8,})[\"|']");
    private static final Pattern PRIVATE_KEY_PATTERN = Pattern.compile("(?i)(BEGIN PRIVATE KEY|BEGIN RSA PRIVATE KEY)");
    private static final Pattern ACCESS_KEY_PATTERN = Pattern.compile("(?i)(access[_-]?key|access[_-]?id)[\\s]*[=:][\\s]*[\"|']([\\w\\d]{10,})[\"|']");
    private static final Pattern DATABASE_CONNECTION_PATTERN = Pattern.compile("(?i)(jdbc:[a-z]+://[\\w\\d.-]+:[\\d]+/[\\w\\d]+)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?i)(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");
    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile("(?i)(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");
    private static final Pattern PERSONAL_ID_PATTERN = Pattern.compile("(?i)(\\d{17}[0-9Xx]|\\d{15})"); // 中国身份证号

    /**
     * 检测代码中的敏感信息
     *
     * @param code 代码内容
     * @return 敏感信息结果列表
     */
    public static List<SensitiveResult> detect(String code) {
        List<SensitiveResult> results = new ArrayList<>();
        String[] lines = code.split("\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            // 检测API密钥
            Matcher apiKeyMatcher = API_KEY_PATTERN.matcher(line);
            if (apiKeyMatcher.find()) {
                results.add(new SensitiveResult(
                        SensitiveType.API_KEY,
                        apiKeyMatcher.group(0),
                        lineNumber,
                        "使用环境变量或配置文件存储API密钥，避免硬编码"
                ));
            }

            // 检测密码
            Matcher passwordMatcher = PASSWORD_PATTERN.matcher(line);
            if (passwordMatcher.find()) {
                results.add(new SensitiveResult(
                        SensitiveType.PASSWORD,
                        passwordMatcher.group(0),
                        lineNumber,
                        "使用环境变量或安全的密码管理工具存储密码，避免硬编码"
                ));
            }

            // 检测令牌
            Matcher tokenMatcher = TOKEN_PATTERN.matcher(line);
            if (tokenMatcher.find()) {
                results.add(new SensitiveResult(
                        SensitiveType.TOKEN,
                        tokenMatcher.group(0),
                        lineNumber,
                        "使用环境变量或配置文件存储令牌，避免硬编码"
                ));
            }

            // 检测密钥
            Matcher secretKeyMatcher = SECRET_KEY_PATTERN.matcher(line);
            if (secretKeyMatcher.find()) {
                results.add(new SensitiveResult(
                        SensitiveType.SECRET_KEY,
                        secretKeyMatcher.group(0),
                        lineNumber,
                        "使用环境变量或配置文件存储密钥，避免硬编码"
                ));
            }

            // 检测凭证
            Matcher credentialMatcher = CREDENTIAL_PATTERN.matcher(line);
            if (credentialMatcher.find()) {
                results.add(new SensitiveResult(
                        SensitiveType.CREDENTIAL,
                        credentialMatcher.group(0),
                        lineNumber,
                        "使用环境变量或配置文件存储凭证，避免硬编码"
                ));
            }

            // 检测私钥
            Matcher privateKeyMatcher = PRIVATE_KEY_PATTERN.matcher(line);
            if (privateKeyMatcher.find()) {
                results.add(new SensitiveResult(
                        SensitiveType.PRIVATE_KEY,
                        privateKeyMatcher.group(0),
                        lineNumber,
                        "不要在代码中包含私钥，使用密钥管理服务或配置文件"
                ));
            }

            // 检测访问密钥
            Matcher accessKeyMatcher = ACCESS_KEY_PATTERN.matcher(line);
            if (accessKeyMatcher.find()) {
                results.add(new SensitiveResult(
                        SensitiveType.ACCESS_KEY,
                        accessKeyMatcher.group(0),
                        lineNumber,
                        "使用环境变量或配置文件存储访问密钥，避免硬编码"
                ));
            }

            // 检测数据库连接
            Matcher dbConnectionMatcher = DATABASE_CONNECTION_PATTERN.matcher(line);
            if (dbConnectionMatcher.find()) {
                results.add(new SensitiveResult(
                        SensitiveType.DATABASE_CONNECTION,
                        dbConnectionMatcher.group(0),
                        lineNumber,
                        "使用环境变量或配置文件存储数据库连接信息，避免硬编码"
                ));
            }

            // 检测电子邮件
            Matcher emailMatcher = EMAIL_PATTERN.matcher(line);
            if (emailMatcher.find()) {
                results.add(new SensitiveResult(
                        SensitiveType.EMAIL,
                        emailMatcher.group(0),
                        lineNumber,
                        "考虑是否需要在代码中包含电子邮件地址，可能泄露个人信息"
                ));
            }

            // 检测电话号码
            Matcher phoneMatcher = PHONE_PATTERN.matcher(line);
            if (phoneMatcher.find()) {
                results.add(new SensitiveResult(
                        SensitiveType.PHONE,
                        phoneMatcher.group(0),
                        lineNumber,
                        "考虑是否需要在代码中包含电话号码，可能泄露个人信息"
                ));
            }

            // 检测IP地址
            Matcher ipAddressMatcher = IP_ADDRESS_PATTERN.matcher(line);
            if (ipAddressMatcher.find()) {
                results.add(new SensitiveResult(
                        SensitiveType.IP_ADDRESS,
                        ipAddressMatcher.group(0),
                        lineNumber,
                        "考虑使用环境变量或配置文件存储IP地址，避免硬编码"
                ));
            }

            // 检测个人ID
            Matcher personalIdMatcher = PERSONAL_ID_PATTERN.matcher(line);
            if (personalIdMatcher.find()) {
                results.add(new SensitiveResult(
                        SensitiveType.PERSONAL_ID,
                        personalIdMatcher.group(0),
                        lineNumber,
                        "不要在代码中包含个人身份信息，这是严重的隐私泄露"
                ));
            }
        }

        return results;
    }

    /**
     * 检测代码中是否包含敏感信息
     *
     * @param code 代码内容
     * @return 是否包含敏感信息
     */
    public static boolean containsSensitiveInfo(String code) {
        return !detect(code).isEmpty();
    }

    /**
     * 获取敏感信息警告消息
     *
     * @param results 敏感信息结果列表
     * @return 警告消息
     */
    public static String getWarningMessage(List<SensitiveResult> results) {
        if (results.isEmpty()) {
            return null;
        }

        StringBuilder message = new StringBuilder("检测到可能的敏感信息：\n\n");
        for (SensitiveResult result : results) {
            message.append("- ").append(result.toString()).append("\n\n");
        }
        message.append("请确认是否要分享这些信息。");

        return message.toString();
    }
}
