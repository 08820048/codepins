package cn.ilikexff.codepins;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 图钉统一存储管理类（内存 + UI 模型 + 本地持久化）
 */
public class PinStorage {

    private static final List<PinEntry> pins = new ArrayList<>();
    private static DefaultListModel<PinEntry> model = null;

    public static void setModel(DefaultListModel<PinEntry> m) {
        model = m;
        refreshModel();
    }

    public static void addPin(PinEntry entry) {
        pins.add(entry);
        PinStateService.getInstance().addPin(entry);
        refreshModel();
    }

    /**
     * ✅ 删除指定图钉：从内存 + 持久化中移除
     */
    public static void removePin(PinEntry entry) {
        pins.remove(entry);

        // 同时从持久化数据中移除（按路径 + 行号匹配）
        PinStateService.getInstance().getPins().removeIf(
                p -> p.filePath.equals(entry.filePath) && p.line == entry.line
        );

        refreshModel();
    }

    public static void clearAll() {
        pins.clear();
        PinStateService.getInstance().clear();
        refreshModel();
    }

    public static List<PinEntry> getPins() {
        return pins;
    }

    public static void initFromSaved() {
        List<PinState> saved = PinStateService.getInstance().getPins();
        pins.clear();

        for (PinState state : saved) {
            pins.add(new PinEntry(state.filePath, state.line, state.note));
        }

        refreshModel();
    }

    private static void refreshModel() {
        if (model != null) {
            model.clear();
            for (PinEntry pin : pins) {
                model.addElement(pin);
            }
        }
    }
}