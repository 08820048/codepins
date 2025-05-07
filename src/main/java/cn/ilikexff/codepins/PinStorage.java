package cn.ilikexff.codepins;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 图钉存储管理类
 * - 管理图钉数据列表
 * - 支持与 UI 列表模型联动，实时刷新 ToolWindow 展示内容
 */
public class PinStorage {

    // 用于保存所有图钉的内存列表
    private static final List<PinEntry> pins = new ArrayList<>();

    // 当前绑定的 UI 列表模型（由 ToolWindow 设置）
    private static DefaultListModel<PinEntry> model = null;

    /**
     * 设置 UI 使用的列表模型，ToolWindow 初始化时调用
     * @param m 传入的 DefaultListModel，用于 UI 显示
     */
    public static void setModel(DefaultListModel<PinEntry> m) {
        model = m;
        refreshModel(); // 初始化时同步刷新一次
    }

    /**
     * 添加图钉，并刷新 UI
     * @param entry 新的图钉条目
     */
    public static void addPin(PinEntry entry) {
        pins.add(entry);
        refreshModel(); // 每次添加后更新 UI
    }

    /**
     * 获取所有图钉列表（供 ToolWindow 初始渲染使用）
     */
    public static List<PinEntry> getPins() {
        return pins;
    }

    /**
     * 内部方法：将内存中 pins 同步刷新到 UI 列表模型中
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