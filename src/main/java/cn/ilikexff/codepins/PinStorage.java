package cn.ilikexff.codepins;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
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
        if (entry.isBlock) {
            // 如果是代码块图钉，保存偏移量范围
            PinStateService.getInstance().addPin(
                    new PinState(
                            entry.filePath,
                            currentLine,
                            entry.note,
                            entry.timestamp,
                            entry.author,
                            entry.isBlock,
                            entry.marker.getStartOffset(),
                            entry.marker.getEndOffset()
                    )
            );
            System.out.println("[CodePins] 保存代码块图钉，范围: " + entry.marker.getStartOffset() + "-" + entry.marker.getEndOffset());
        } else {
            // 如果是单行图钉，使用简化的构造函数
            PinStateService.getInstance().addPin(
                    new PinState(entry.filePath, currentLine, entry.note, entry.timestamp, entry.author, entry.isBlock)
            );
        }

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

            // 创建 RangeMarker
            int line = Math.min(state.line, doc.getLineCount() - 1); // 防止越界
            int startOffset, endOffset;
            int docLength = doc.getTextLength();

            if (state.isBlock && state.startOffset >= 0 && state.endOffset >= 0) {
                // 如果是代码块图钉，并且有保存的偏移量范围，则使用保存的范围
                startOffset = Math.max(0, Math.min(state.startOffset, docLength));
                endOffset = Math.max(0, Math.min(state.endOffset, docLength));
                System.out.println("[CodePins] 恢复代码块图钉，使用保存的范围: " + startOffset + "-" + endOffset);
            } else if (state.isBlock) {
                // 如果是代码块图钉，但没有保存范围，则使用整行作为范围
                startOffset = doc.getLineStartOffset(line);
                endOffset = doc.getLineEndOffset(line);
                System.out.println("[CodePins] 恢复代码块图钉，使用行范围: " + startOffset + "-" + endOffset);
            } else {
                // 如果是单行图钉，则使用行起始位置
                startOffset = doc.getLineStartOffset(line);
                endOffset = startOffset;
            }

            // 确保范围有效0
            if (startOffset > endOffset) {
                startOffset = endOffset;
            }

            RangeMarker marker = doc.createRangeMarker(startOffset, endOffset);
            marker.setGreedyToLeft(true);
            marker.setGreedyToRight(true);

            PinEntry entry = new PinEntry(
                    state.filePath,
                    marker,
                    state.note,
                    state.timestamp,
                    state.author,
                    state.isBlock
            );
            pins.add(entry);
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
