package cn.ilikexff.codepins;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图钉统一存储管理类（内存 + UI 模型 + 本地持久化）
 */
public class PinStorage {

    private static final List<PinEntry> pins = new ArrayList<>();
    private static DefaultListModel<PinEntry> model = null;

    /**
     * 设置 UI 模型，用于同步刷新列表
     */
    public static void setModel(DefaultListModel<PinEntry> m) {
        model = m;
        refreshModel();
    }

    /**
     * 添加图钉（包括 UI 显示 + 持久化）
     */
    public static void addPin(PinEntry entry) {
        pins.add(entry);

        // 获取 Document 对象，计算当前行号（用于持久化）
        Document doc = entry.marker.getDocument();
        int currentLine = entry.getCurrentLine(doc);

        // 存入持久化服务中（静态快照）
        PinStateService.getInstance().addPin(new PinState(entry.filePath, currentLine, entry.note));

        refreshModel();
    }

    /**
     * 删除指定图钉（内存 + 持久化）
     */
    public static void removePin(PinEntry entry) {
        pins.remove(entry);

        // 同时从持久化列表中删除（路径 + 行号匹配）
        Document doc = entry.marker.getDocument();
        int currentLine = entry.getCurrentLine(doc);

        PinStateService.getInstance().getPins().removeIf(
                p -> p.filePath.equals(entry.filePath) && p.line == currentLine
        );

        refreshModel();
    }

    /**
     * 删除所有图钉（清空内存和本地）
     */
    public static void clearAll() {
        pins.clear();
        PinStateService.getInstance().clear();
        refreshModel();
    }

    /**
     * 获取当前图钉列表（内存）
     */
    public static List<PinEntry> getPins() {
        return pins;
    }

    /**
     * 从本地持久化数据恢复图钉（将 line 转为 RangeMarker）
     */
    public static void initFromSaved() {
        List<PinState> saved = PinStateService.getInstance().getPins();
        pins.clear();

        for (PinState state : saved) {
            // 先通过路径获取 VirtualFile
            VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(state.filePath);
            if (vFile == null) continue;

            // 再通过 VirtualFile 获取 Document
            Document doc = FileDocumentManager.getInstance().getDocument(vFile);
            if (doc == null) continue;

            // 将行号转换为 offset 并创建 RangeMarker（保持在该行起始位置）
            int line = Math.min(state.line, doc.getLineCount() - 1); // 防止越界
            int offset = doc.getLineStartOffset(line);
            RangeMarker marker = doc.createRangeMarker(offset, offset);

            // 添加为 PinEntry
            pins.add(new PinEntry(state.filePath, marker, state.note));
        }

        refreshModel();
    }

    /**
     * 更新图钉备注内容
     */
    public static void updateNote(PinEntry entry, String newNote) {
        entry.note = newNote;

        Document doc = entry.marker.getDocument();
        int currentLine = entry.getCurrentLine(doc);

        for (PinState p : PinStateService.getInstance().getPins()) {
            if (p.filePath.equals(entry.filePath) && p.line == currentLine) {
                p.note = newNote;
                break;
            }
        }

        refreshModel();
    }

    /**
     * 通知 UI 刷新 JList 内容
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