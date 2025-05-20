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
        boolean modified = !mySettingsComponent.getMaxPinsCount().equals(settings.maxPinsCount);
        modified |= !mySettingsComponent.getPreviewHeight().equals(settings.previewHeight);
        modified |= mySettingsComponent.getAutoShowPreview() != settings.autoShowPreview;
        modified |= mySettingsComponent.getConfirmDelete() != settings.confirmDelete;
        return modified;
    }

    @Override
    public void apply() {
        CodePinsSettings settings = CodePinsSettings.getInstance();
        settings.maxPinsCount = mySettingsComponent.getMaxPinsCount();
        settings.previewHeight = mySettingsComponent.getPreviewHeight();
        settings.autoShowPreview = mySettingsComponent.getAutoShowPreview();
        settings.confirmDelete = mySettingsComponent.getConfirmDelete();
    }

    @Override
    public void reset() {
        CodePinsSettings settings = CodePinsSettings.getInstance();
        mySettingsComponent.setMaxPinsCount(settings.maxPinsCount);
        mySettingsComponent.setPreviewHeight(settings.previewHeight);
        mySettingsComponent.setAutoShowPreview(settings.autoShowPreview);
        mySettingsComponent.setConfirmDelete(settings.confirmDelete);
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}
