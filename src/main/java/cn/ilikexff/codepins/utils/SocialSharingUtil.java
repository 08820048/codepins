package cn.ilikexff.codepins.utils;

import cn.ilikexff.codepins.services.LicenseService;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.awt.Desktop;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 社交媒体分享工具类
 * 用于将代码分享到社交媒体平台
 */
public class SocialSharingUtil {

    /**
     * 社交媒体平台枚举
     */
    public enum SocialPlatform {
        // 国际平台
        TWITTER("Twitter", "https://twitter.com/intent/tweet?text=%s&url=%s"),
        LINKEDIN("LinkedIn", "https://www.linkedin.com/sharing/share-offsite/?url=%s"),
        FACEBOOK("Facebook", "https://www.facebook.com/sharer/sharer.php?u=%s"),
        REDDIT("Reddit", "https://www.reddit.com/submit?url=%s&title=%s"),
        TELEGRAM("Telegram", "https://t.me/share/url?url=%s&text=%s"),
        HACKER_NEWS("Hacker News", "https://news.ycombinator.com/submitlink?u=%s&t=%s"),
        GITHUB("GitHub", "https://github.com/08820048/CodePins");

        private final String displayName;
        private final String shareUrlTemplate;

        SocialPlatform(String displayName, String shareUrlTemplate) {
            this.displayName = displayName;
            this.shareUrlTemplate = shareUrlTemplate;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getShareUrlTemplate() {
            return shareUrlTemplate;
        }
    }

    /**
     * 分享类型枚举
     */
    public enum ShareType {
        DIRECT_LINK("直接链接", "分享链接到社交媒体"),
        IMAGE_UPLOAD("图片上传", "上传图片到图床并分享链接"),
        CODE_SNIPPET("代码片段", "分享代码片段到代码分享平台");

        private final String displayName;
        private final String description;

        ShareType(String displayName, String description) {
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

    // 免费版支持的平台
    private static final SocialPlatform[] FREE_PLATFORMS = {
            SocialPlatform.TWITTER,
            SocialPlatform.LINKEDIN,
            SocialPlatform.FACEBOOK
    };

    // 付费版支持的平台
    private static final SocialPlatform[] PREMIUM_PLATFORMS = {
            SocialPlatform.TWITTER,
            SocialPlatform.LINKEDIN,
            SocialPlatform.FACEBOOK,
            SocialPlatform.REDDIT,
            SocialPlatform.TELEGRAM,
            SocialPlatform.HACKER_NEWS,
            SocialPlatform.GITHUB
    };

    /**
     * 获取支持的社交媒体平台
     *
     * @param isPremium 是否为付费版
     * @return 支持的社交媒体平台数组
     */
    public static SocialPlatform[] getSupportedPlatforms(boolean isPremium) {
        return isPremium ? PREMIUM_PLATFORMS : FREE_PLATFORMS;
    }

    /**
     * 分享到社交媒体
     *
     * @param project 当前项目
     * @param platform 社交媒体平台
     * @param title 分享标题
     * @param url 分享链接
     * @return 是否成功
     */
    public static boolean shareToSocialMedia(Project project, SocialPlatform platform, String title, String url) {
        try {
            // 验证URL是否为空
            if (url == null || url.trim().isEmpty()) {
                // 使用默认演示链接
                url = "https://gist.github.com/codepins/7f5f8c0e5f8f8f8f8f8f8f8f8f8f8f8f";
            }

            // 验证标题是否为空
            if (title == null || title.trim().isEmpty()) {
                title = "CodePins代码分享";
            }

            // 编码URL和标题
            String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString());

            // 构建分享URL
            String shareUrl;
            switch (platform) {
                // 只需要URL的平台
                case LINKEDIN:
                case FACEBOOK:
                    shareUrl = String.format(platform.getShareUrlTemplate(), encodedUrl);
                    break;

                // 需要标题和URL，顺序是标题在前
                case TWITTER:
                    shareUrl = String.format(platform.getShareUrlTemplate(), encodedTitle, encodedUrl);
                    break;

                // 需要URL和标题，顺序是URL在前
                case REDDIT:
                case TELEGRAM:
                case HACKER_NEWS:
                    shareUrl = String.format(platform.getShareUrlTemplate(), encodedUrl, encodedTitle);
                    break;

                // GitHub特殊处理
                case GITHUB:
                    shareUrl = platform.getShareUrlTemplate();
                    break;

                default:
                    Messages.showErrorDialog(
                            project,
                            "不支持的社交媒体平台: " + platform.getDisplayName(),
                            "分享错误"
                    );
                    return false;
            }

            // 打开浏览器
            BrowserUtil.browse(shareUrl);
            return true;
        } catch (UnsupportedEncodingException e) {
            Messages.showErrorDialog(
                    project,
                    "编码URL失败: " + e.getMessage(),
                    "分享错误"
            );
            return false;
        }
    }

    /**
     * 显示微信二维码
     *
     * @param project 当前项目
     * @param url 分享链接
     */
    private static void showWeChatQRCode(Project project, String url) {
        // 生成二维码
        try {
            // 创建临时文件
            File qrCodeFile = File.createTempFile("codepins_wechat_qrcode_", ".png");

            // 生成二维码
            QRCodeGenerator.generateQRCode(url, qrCodeFile.getAbsolutePath(), 300, 300);

            // 显示二维码
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(qrCodeFile);
            } else {
                Messages.showInfoMessage(
                        project,
                        "请使用微信扫描二维码分享: " + qrCodeFile.getAbsolutePath(),
                        "微信分享"
                );
            }
        } catch (Exception e) {
            Messages.showErrorDialog(
                    project,
                    "生成微信二维码失败: " + e.getMessage(),
                    "分享错误"
            );
        }
    }

    /**
     * 检查是否为付费用户
     *
     * @return 是否为付费用户
     */
    public static boolean isPremiumUser() {
        // 使用LicenseService检查用户是否为付费用户
        try {
            // 注释掉开发环境检查，确保在测试时也能正确显示免费/付费状态
            // if (isDevEnvironment()) {
            //     return true;
            // }
            return LicenseService.getInstance().isPremiumUser();
        } catch (Exception e) {
            // 如果出错，返回false
            return false;
        }
    }

    /**
     * 检查是否为开发环境
     *
     * @return 是否为开发环境
     */
    private static boolean isDevEnvironment() {
        try {
            // 检查是否存在特定的开发环境标志
            return Boolean.getBoolean("codepins.dev") ||
                   System.getProperty("idea.platform.prefix", "").contains("Idea") ||
                   System.getProperty("idea.paths.selector", "").contains("IdeaIC");
        } catch (Exception e) {
            return false;
        }
    }
}
