package cn.ilikexff.codepins.i18n;

import cn.ilikexff.codepins.settings.LanguageSettings;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * CodePins 国际化消息束类
 */
public class CodePinsBundle {
    private static final String BUNDLE_NAME = "messages.CodePinsBundle";
    private static ResourceBundle bundle;
    private static Locale cachedLocale; // 缓存的语言设置，用于检测语言变化

    // 移除静态的 currentLocale，改为动态获取

    /**
     * 获取当前语言设置
     * @return 当前语言区域设置
     */
    public static Locale getCurrentLocale() {
        try {
            if (ApplicationManager.getApplication() != null) {
                LanguageSettings settings = LanguageSettings.getInstance();
                if (settings != null) {
                    return settings.getSelectedLocale();
                }
            }
        } catch (Exception e) {
            // 在启动过程中或测试环境中可能会出现异常，使用默认语言
            System.out.println("[CodePins] 无法获取语言设置，使用默认语言: " + e.getMessage());
        }
        return Locale.ENGLISH; // 默认使用英文
    }

    /**
     * 重置资源束，强制在下次访问时重新加载
     */
    public static void resetBundle() {
        bundle = null;
        cachedLocale = null;
    }

    /**
     * 获取当前语言设置的资源束
     * @return 资源束
     */
    private static ResourceBundle getBundle() {
        try {
            Locale currentLocale = getCurrentLocale();

            // 检查语言是否发生变化，如果变化了就重新加载资源束
            if (bundle == null || !currentLocale.equals(cachedLocale)) {
                bundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
                cachedLocale = currentLocale;
            }
            return bundle;
        } catch (MissingResourceException e) {
            // 如果找不到资源，尝试使用英文
            try {
                bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
                cachedLocale = Locale.ENGLISH;
                return bundle;
            } catch (MissingResourceException ex) {
                System.err.println("[CodePins] 无法加载资源束: " + ex.getMessage());
                return null;
            }
        }
    }

    /**
     * 获取国际化消息
     * @param key 消息键
     * @param params 格式化参数
     * @return 国际化消息
     */
    public static String message(@NotNull String key, @NotNull Object... params) {
        try {
            ResourceBundle resourceBundle = getBundle();
            if (resourceBundle != null && resourceBundle.containsKey(key)) {
                String pattern = resourceBundle.getString(key);
                if (params.length > 0) {
                    return MessageFormat.format(pattern, params);
                } else {
                    return pattern;
                }
            }
        } catch (Exception e) {
            System.err.println("[CodePins] 获取消息失败: " + key + ", " + e.getMessage());
        }

        // 如果找不到消息，返回键名
        return key;
    }

}
