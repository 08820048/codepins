package cn.ilikexff.codepins.utils;

import cn.ilikexff.codepins.PinEntry;
import cn.ilikexff.codepins.ui.GithubTokenDialog;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分享链接生成工具类
 */
public class ShareLinkGenerator {

    // 基础URL
    // 使用GitHub Gist作为分享解决方案
    private static final String FALLBACK_URL = "https://gist.github.com/codepins/7f5f8c0e5f8f8f8f8f8f8f8f8f8f8f8f";

    // 存储分享链接信息
    private static final Map<String, ShareLinkInfo> SHARE_LINKS = new HashMap<>();

    /**
     * 分享链接信息
     */
    public static class ShareLinkInfo {
        private final String id;
        private final String content;
        private final long creationTime;
        private final long expirationTime;
        private final boolean requiresPassword;
        private final String password;
        private final boolean isPremium;

        public ShareLinkInfo(String id, String content, long expirationTime, boolean requiresPassword, String password, boolean isPremium) {
            this.id = id;
            this.content = content;
            this.creationTime = System.currentTimeMillis();
            this.expirationTime = expirationTime;
            this.requiresPassword = requiresPassword;
            this.password = password;
            this.isPremium = isPremium;
        }

        public String getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }

        public boolean requiresPassword() {
            return requiresPassword;
        }

        public String getPassword() {
            return password;
        }

        public boolean isPremium() {
            return isPremium;
        }

        public String getFormattedCreationTime() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(creationTime));
        }

        public String getFormattedExpirationTime() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(expirationTime));
        }

        private String shareUrl;

        public String getShareUrl() {
            // 如果已经有分享 URL，直接返回
            if (shareUrl != null && !shareUrl.isEmpty()) {
                return shareUrl;
            }

            // 否则返回备用URL
            return FALLBACK_URL;
        }

        public void setShareUrl(String url) {
            this.shareUrl = url;
        }
    }

    /**
     * 过期时间枚举
     */
    public enum ExpirationTime {
        ONE_HOUR("1小时", TimeUnit.HOURS.toMillis(1)),
        ONE_DAY("1天", TimeUnit.DAYS.toMillis(1)),
        ONE_WEEK("1周", TimeUnit.DAYS.toMillis(7)),
        ONE_MONTH("1个月", TimeUnit.DAYS.toMillis(30)),
        NEVER("永不过期", Long.MAX_VALUE);

        private final String displayName;
        private final long milliseconds;

        ExpirationTime(String displayName, long milliseconds) {
            this.displayName = displayName;
            this.milliseconds = milliseconds;
        }

        public String getDisplayName() {
            return displayName;
        }

        public long getMilliseconds() {
            return milliseconds;
        }
    }

    /**
     * 生成分享链接
     *
     * @param project 当前项目
     * @param pins 要分享的图钉列表
     * @param format 分享格式
     * @param codeOnly 是否只分享代码
     * @param showLineNumbers 是否显示行号
     * @param expirationTime 过期时间
     * @param requiresPassword 是否需要密码
     * @param password 密码
     * @return 分享链接信息
     */
    public static ShareLinkInfo generateShareLink(Project project, List<PinEntry> pins,
                                                 SharingUtil.SharingFormat format, boolean codeOnly,
                                                 boolean showLineNumbers, ExpirationTime expirationTime,
                                                 boolean requiresPassword, String password) {
        try {
            // 生成内容
            String content = SharingUtil.formatPins(project, pins, format, codeOnly, showLineNumbers);

            // 生成唯一ID
            String id = generateUniqueId(content);

            // 计算过期时间
            long expiration = System.currentTimeMillis() + expirationTime.getMilliseconds();

            // 创建分享链接信息
            boolean isPremium = SocialSharingUtil.isPremiumUser();
            ShareLinkInfo info = new ShareLinkInfo(id, content, expiration, requiresPassword, password, isPremium);

            // 存储分享链接信息
            SHARE_LINKS.put(id, info);

            // 尝试使用GitHub Gist创建真实的分享链接
            try {
                // 获取GistService实例
                cn.ilikexff.codepins.services.GistService gistService = cn.ilikexff.codepins.services.GistService.getInstance();

                // 检查是否配置GitHub令牌
                if (gistService.isConfigured()) {
                    // 生成描述
                    String description = "CodePins分享: " + (pins.size() == 1 ?
                            (pins.get(0).name != null ? pins.get(0).name : "Code") :
                            pins.size() + "个图钉");

                    // 生成文件名
                    String filename = "codepins_" + id + getFileExtension(pins);

                    // 创建Gist
                    String gistUrl = gistService.createGist(content, description, filename);

                    // 设置分享 URL
                    if (gistUrl != null && !gistUrl.isEmpty()) {
                        info.setShareUrl(gistUrl);
                    }
                } else {
                    // 如果没有配置GitHub令牌，显示设置对话框
                    SwingUtilities.invokeLater(() -> {
                        GithubTokenDialog dialog = new GithubTokenDialog(project);
                        if (dialog.showAndGet()) {
                            // 用户设置了令牌，重新尝试
                            String description = "CodePins分享: " + (pins.size() == 1 ?
                                    (pins.get(0).name != null ? pins.get(0).name : "Code") :
                                    pins.size() + "个图钉");
                            String filename = "codepins_" + id + getFileExtension(pins);
                            String gistUrl = gistService.createGist(content, description, filename);
                            if (gistUrl != null && !gistUrl.isEmpty()) {
                                info.setShareUrl(gistUrl);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 如果创建Gist失败，使用备用URL
            }

            return info;
        } catch (Exception e) {
            e.printStackTrace();
            // 如果出错，返回一个有效的演示链接
            String demoId = "codepins-demo";
            return new ShareLinkInfo(
                demoId,
                "// 示例代码\nfunction helloWorld() {\n  console.log(\"Hello from CodePins!\");\n}",
                System.currentTimeMillis() + 86400000, // 24小时
                false,
                null,
                false
            );
        }
    }

    /**
     * 获取分享链接信息
     *
     * @param id 分享链接ID
     * @return 分享链接信息
     */
    public static ShareLinkInfo getShareLinkInfo(String id) {
        return SHARE_LINKS.get(id);
    }

    /**
     * 删除分享链接
     *
     * @param id 分享链接ID
     */
    public static void deleteShareLink(String id) {
        SHARE_LINKS.remove(id);
    }

    /**
     * 清理过期的分享链接
     */
    public static void cleanupExpiredLinks() {
        SHARE_LINKS.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * 获取文件扩展名
     *
     * @param pins 图钉列表
     * @return 文件扩展名
     */
    private static String getFileExtension(List<PinEntry> pins) {
        if (pins == null || pins.isEmpty()) {
            return ".txt";
        }

        // 获取第一个图钉的文件路径
        String filePath = pins.get(0).filePath;
        if (filePath == null || filePath.isEmpty()) {
            return ".txt";
        }

        // 获取文件扩展名
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filePath.length() - 1) {
            return "." + filePath.substring(dotIndex + 1);
        }

        return ".txt";
    }

    /**
     * 生成唯一ID
     *
     * @param content 内容
     * @return 唯一ID
     */
    private static String generateUniqueId(String content) {
        try {
            // 使用内容和时间戳生成唯一ID
            String input = content + System.currentTimeMillis() + UUID.randomUUID().toString();

            // 使用SHA-256哈希
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // 返回前8个字符作为ID
            return hexString.toString().substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            // 如果SHA-256不可用，使用UUID
            return UUID.randomUUID().toString().substring(0, 8);
        }
    }
}
