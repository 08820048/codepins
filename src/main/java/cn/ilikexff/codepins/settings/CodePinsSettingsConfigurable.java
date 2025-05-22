package cn.ilikexff.codepins.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * CodePins 设置配置类
 * 用于在 IDE 的设置页面中显示 CodePins 的设置
 */
public class CodePinsSettingsConfigurable implements Configurable {
    private CodePinsSettingsComponent mySettingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "CodePins";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new CodePinsSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        CodePinsSettings settings = CodePinsSettings.getInstance();
        boolean modified = !mySettingsComponent.getPreviewHeight().equals(settings.previewHeight);
        modified |= mySettingsComponent.getConfirmDelete() != settings.confirmDelete;
        return modified;
    }

    @Override
    public void apply() {
        CodePinsSettings settings = CodePinsSettings.getInstance();
        settings.previewHeight = mySettingsComponent.getPreviewHeight();
        settings.confirmDelete = mySettingsComponent.getConfirmDelete();
    }

    @Override
    public void reset() {
        CodePinsSettings settings = CodePinsSettings.getInstance();
        mySettingsComponent.setPreviewHeight(settings.previewHeight);
        mySettingsComponent.setConfirmDelete(settings.confirmDelete);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}
