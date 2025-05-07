package cn.ilikexff.codepins;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 国际化工具类：自动加载 messages/CodePinsBundle，根据系统 locale 显示。
 * 支持 fallback，当找不到 key 时返回默认英文值。
 */
public class I18n {
    private static final String BUNDLE_NAME = "messages.CodePinsBundle";
    private static final ResourceBundle BUNDLE;

    static {
        ResourceBundle temp;
        try {
            temp = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
        } catch (MissingResourceException e) {
            temp = null;
        }
        BUNDLE = temp;
    }

    /**
     * 获取国际化文本（带默认 fallback 英文）
     *
     * @param key          资源 key
     * @param defaultValue 找不到时默认值
     * @return 本地化字符串
     */
    public static String get(String key, String defaultValue) {
        if (BUNDLE != null && BUNDLE.containsKey(key)) {
            return BUNDLE.getString(key);
        }
        return defaultValue;
    }
}