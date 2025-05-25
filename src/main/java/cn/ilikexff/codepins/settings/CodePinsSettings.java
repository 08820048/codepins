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
    
    // 图钉添加设置
    public boolean showNoteDialogOnQuickAdd = false; // 默认不弹出备注框和标签框
    
    // 注释指令添加图钉设置
    public boolean showNoteDialogOnCommentPin = false; // 默认不弹出备注框和标签框
    public boolean autoAddQuickTag = true; // 默认自动添加“快捷添加”标签
    public boolean useCompletionSymbol = true; // 默认使用完成指令符号
    public String completionSymbol = ";"; // 默认完成指令符号为分号

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
