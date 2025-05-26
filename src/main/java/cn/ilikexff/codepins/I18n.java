package cn.ilikexff.codepins;

import cn.ilikexff.codepins.i18n.CodePinsBundle;
import cn.ilikexff.codepins.settings.LanguageSettings;
import com.intellij.openapi.application.ApplicationManager;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Internationalization utility class: automatically loads messages/CodePinsBundle based on selected language.
 * Supports fallback to default English values when a key is not found.
 */
public class I18n {
    private static final String BUNDLE_NAME = "messages.CodePinsBundle";
    private static ResourceBundle BUNDLE;

    /**
     * Get the current resource bundle based on user's language settings
     * @return The resource bundle for the current language
     */
    private static ResourceBundle getBundle() {
        if (BUNDLE == null) {
            try {
                // Try to get language settings from application service
                LanguageSettings settings = null;
                try {
                    if (ApplicationManager.getApplication() != null) {
                        settings = LanguageSettings.getInstance();
                    }
                } catch (Exception e) {
                    // Ignore, might be in unit tests or during startup
                }
                
                // Get locale from settings or use system default
                Locale locale = settings != null ? settings.getSelectedLocale() : Locale.getDefault();
                BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            } catch (MissingResourceException e) {
                // Fallback to default locale if bundle not found
                try {
                    BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
                } catch (MissingResourceException ex) {
                    // If still not found, return null (will use default values)
                    BUNDLE = null;
                }
            }
        }
        return BUNDLE;
    }
    
    /**
     * Reset the cached bundle to force reload on next access
     * Call this when language settings change
     */
    public static void resetBundle() {
        BUNDLE = null;
    }

    /**
     * Get localized text with fallback to default English value
     *
     * @param key          Resource key
     * @param defaultValue Default value if key not found
     * @return Localized string
     */
    public static String get(String key, String defaultValue) {
        ResourceBundle bundle = getBundle();
        if (bundle != null && bundle.containsKey(key)) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Get localized text with parameters
     *
     * @param key          Resource key
     * @param defaultValue Default value if key not found
     * @param params       Parameters for message formatting
     * @return Formatted localized string
     */
    public static String get(String key, String defaultValue, Object... params) {
        String pattern = get(key, defaultValue);
        if (params.length == 0) {
            return pattern;
        }
        try {
            return MessageFormat.format(pattern, params);
        } catch (Exception e) {
            return pattern;
        }
    }
    
    /**
     * Get localized text using CodePinsBundle
     * This is a convenience method that delegates to CodePinsBundle.message
     *
     * @param key    Resource key
     * @param params Parameters for message formatting
     * @return Localized string
     */
    public static String message(String key, Object... params) {
        return CodePinsBundle.message(key, params);
    }
}