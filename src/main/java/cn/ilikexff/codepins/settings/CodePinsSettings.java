package cn.ilikexff.codepins.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * CodePins 设置状态类
 * 用于保存和加载 CodePins 的设置
 */
@State(
        name = "cn.ilikexff.codepins.settings.CodePinsSettings",
        storages = {@Storage("CodePinsSettings.xml")}
)
public class CodePinsSettings implements PersistentStateComponent<CodePinsSettings> {
    // 常规设置
    public String previewHeight = "300";
    public boolean confirmDelete = true;

    // 开发测试设置
    public boolean testPremiumMode = false;

    /**
     * 获取设置实例
     */
    public static CodePinsSettings getInstance() {
        return ApplicationManager.getApplication().getService(CodePinsSettings.class);
    }

    @Nullable
    @Override
    public CodePinsSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CodePinsSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
