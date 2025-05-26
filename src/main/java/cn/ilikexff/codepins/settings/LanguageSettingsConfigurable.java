package cn.ilikexff.codepins.settings;

import cn.ilikexff.codepins.I18n;
import cn.ilikexff.codepins.i18n.CodePinsBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Configurable for CodePins language settings
 */
public class LanguageSettingsConfigurable implements Configurable {
    private JComboBox<LanguageItem> languageComboBox;
    private final LanguageSettings settings = LanguageSettings.getInstance();

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return CodePinsBundle.message("settings.language");
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JBPanel<?> panel = new JBPanel<>(new BorderLayout());

        // Create language combo box
        languageComboBox = new ComboBox<>();
        for (LanguageSettings.Language language : LanguageSettings.Language.values()) {
            languageComboBox.addItem(new LanguageItem(language));
        }

        // Set current language
        for (int i = 0; i < languageComboBox.getItemCount(); i++) {
            LanguageItem item = languageComboBox.getItemAt(i);
            if (item.getLanguage() == settings.getLanguage()) {
                languageComboBox.setSelectedIndex(i);
                break;
            }
        }

        // Build form
        JBLabel languageLabel = new JBLabel(CodePinsBundle.message("settings.language") + ":");
        panel.add(FormBuilder.createFormBuilder()
                .addLabeledComponent(languageLabel, languageComboBox)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel());

        return panel;
    }

    @Override
    public boolean isModified() {
        LanguageItem selectedItem = (LanguageItem) languageComboBox.getSelectedItem();
        return selectedItem != null && selectedItem.getLanguage() != settings.getLanguage();
    }

    @Override
    public void apply() throws ConfigurationException {
        LanguageItem selectedItem = (LanguageItem) languageComboBox.getSelectedItem();
        if (selectedItem != null) {
            // 保存语言设置
            settings.setLanguage(selectedItem.getLanguage());

            // 重置资源束，强制重新加载
            I18n.resetBundle();
            CodePinsBundle.resetBundle();

            // 显示重启提示
            Messages.showInfoMessage(
                    CodePinsBundle.message("settings.language.change.message"),
                    CodePinsBundle.message("settings.language.change.title")
            );
        }
    }

    /**
     * Helper class to display language items in combo box
     */
    private static class LanguageItem {
        private final LanguageSettings.Language language;

        public LanguageItem(LanguageSettings.Language language) {
            this.language = language;
        }

        public LanguageSettings.Language getLanguage() {
            return language;
        }

        @Override
        public String toString() {
            return CodePinsBundle.message(language.getDisplayNameKey());
        }
    }
}
