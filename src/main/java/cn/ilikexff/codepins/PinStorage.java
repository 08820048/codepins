package cn.ilikexff.codepins;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import cn.ilikexff.codepins.PinState;
/**
 * 图钉统一存储管理类（内存 + UI 模型 + 本地持久化）
 */
public class PinStorage {

    // 内存中的图钉对象列表（带备注、支持跳转）
    private static final List<PinEntry> pins = new ArrayList<>();

    // 当前侧边栏 UI 使用的列表模型（用于动态刷新）
    private static DefaultListModel<PinEntry> model = null;

    /**
     * 设置 UI 模型，ToolWindow 初始化时调用
     */
    public static void setModel(DefaultListModel<PinEntry> m) {
        model = m;
        refreshModel();
    }

    /**
     * 添加图钉：保存到内存、刷新 UI、写入持久化存储
     */
    public static void addPin(PinEntry entry) {
        pins.add(entry); // 加入内存列表

        // 加入插件持久化服务（自动写入本地 XML）
        PinStateService.getInstance().addPin(entry);

        refreshModel(); // 同步刷新 UI
    }

    /**
     * 清空所有图钉
     */
    public static void clearAll() {
        pins.clear();
        PinStateService.getInstance().clear(); // 清空持久化
        refreshModel();
    }

    /**
     * 获取当前图钉列表（主要用于 UI 展示）
     */
    public static List<PinEntry> getPins() {
        return pins;
    }

    /**
     * 插件启动时调用：从持久化存储中加载所有图钉到内存和 UI
     */
    public static void initFromSaved() {
        List<PinState> saved = PinStateService.getInstance().getPins();
        pins.clear();

        for (PinState state : saved) {
            pins.add(new PinEntry(state.filePath, state.line, state.note));
        }

        refreshModel(); // 恢复时刷新 UI
    }

    /**
     * 刷新 UI 模型内容
     */
    private static void refreshModel() {
        if (model != null) {
            model.clear();
            for (PinEntry pin : pins) {
                model.addElement(pin);
            }
        }
    }
}