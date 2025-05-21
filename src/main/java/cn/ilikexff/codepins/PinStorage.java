package cn.ilikexff.codepins;

import cn.ilikexff.codepins.settings.CodePinsSettings;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 图钉统一存储管理类（内存 + UI 模型 + 本地持久化）
 */
public class PinStorage {

    private static final List<PinEntry> pins = new ArrayList<>();
    private static DefaultListModel<PinEntry> model = null;
    private static final Set<String> allTags = new HashSet<>(); // 所有标签的集合

    // 免费版用户固定为100个图钉
    private static final int FREE_USER_MAX_PINS = 100;
    // 免费版标签限制常量
    private static final int FREE_USER_MAX_TAG_TYPES = 10; // 最多10种不同标签
    private static final int FREE_USER_MAX_TAGS_PER_PIN = 3; // 每个图钉最多3个标签

    /**
     * 设置 UI 模型，用于同步刷新列表
     */
    public static void setModel(DefaultListModel<PinEntry> m) {
        model = m;
        refreshModel();
    }

    /**
     * 添加图钉（包括 UI 显示 + 持久化）
     *
     * @return 是否添加成功
     */
    public static boolean addPin(PinEntry entry) {
        // 检查是否为专业版用户
        boolean isPremiumUser = cn.ilikexff.codepins.services.LicenseService.getInstance().isPremiumUser();

        // 如果不是专业版用户，检查是否超过最大图钉数量限制
        if (!isPremiumUser) {
            if (pins.size() >= FREE_USER_MAX_PINS) {
                System.out.println("[CodePins] 添加图钉失败：免费版用户已达到最大图钉数量限制 (" + FREE_USER_MAX_PINS + ")");
                return false;
            }

            // 检查标签数量限制
            if (entry.getTags().size() > FREE_USER_MAX_TAGS_PER_PIN) {
                System.out.println("[CodePins] 添加图钉失败：免费版用户每个图钉最多只能添加 " + FREE_USER_MAX_TAGS_PER_PIN + " 个标签");
                return false;
            }

            // 检查新标签是否会超出总标签种类限制
            Set<String> newTags = new HashSet<>(entry.getTags());
            newTags.removeAll(allTags); // 只保留尚未存在的新标签

            if (!newTags.isEmpty() && (allTags.size() + newTags.size()) > FREE_USER_MAX_TAG_TYPES) {
                System.out.println("[CodePins] 添加图钉失败：免费版用户最多只能创建 " + FREE_USER_MAX_TAG_TYPES + " 种不同标签");
                return false;
            }
        }

        pins.add(entry);

        // 获取 Document 对象，计算当前行号（用于持久化）
        Document doc = entry.marker.getDocument();
        int currentLine = entry.getCurrentLine(doc);

        // 更新标签集合
        allTags.addAll(entry.getTags());

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
                            entry.marker.getEndOffset(),
                            entry.getTags()
                    )
            );
            System.out.println("[CodePins] 保存代码块图钉，范围: " + entry.marker.getStartOffset() + "-" + entry.marker.getEndOffset());
        } else {
            // 如果是单行图钉，使用带标签的构造函数
            PinStateService.getInstance().addPin(
                    new PinState(entry.filePath, currentLine, entry.note, entry.timestamp, entry.author, entry.isBlock, entry.getTags())
            );
        }

        refreshModel();
        return true;
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

        // 更新标签集合
        refreshAllTags();

        refreshModel();
    }

    /**
     * 删除所有图钉（清空内存和本地）
     */
    public static void clearAll() {
        pins.clear();
        PinStateService.getInstance().clear();
        allTags.clear();
        refreshModel();
    }

    /**
     * 获取当前图钉列表（内存）
     */
    public static List<PinEntry> getPins() {
        return pins;
    }

    /**
     * 获取图钉数量信息
     *
     * @return 包含当前图钉数量和最大限制的Map
     */
    public static Map<String, Integer> getPinsCountInfo() {
        Map<String, Integer> info = new HashMap<>();

        // 当前图钉数量
        info.put("current", pins.size());

        // 最大图钉数量限制
        boolean isPremiumUser = cn.ilikexff.codepins.services.LicenseService.getInstance().isPremiumUser();
        if (isPremiumUser) {
            info.put("max", -1); // -1 表示无限制
        } else {
            info.put("max", FREE_USER_MAX_PINS); // 免费版限制
        }

        return info;
    }

    /**
     * 获取标签数量信息
     *
     * @return 包含当前标签种类数量和最大限制的Map
     */
    public static Map<String, Integer> getTagsCountInfo() {
        Map<String, Integer> info = new HashMap<>();

        // 当前标签种类数量
        info.put("current", allTags.size());

        // 最大标签种类数量限制
        boolean isPremiumUser = cn.ilikexff.codepins.services.LicenseService.getInstance().isPremiumUser();
        if (isPremiumUser) {
            info.put("max", -1); // -1 表示无限制
        } else {
            info.put("max", FREE_USER_MAX_TAG_TYPES); // 免费版限制
        }

        // 每个图钉的标签数量限制
        info.put("perPin", isPremiumUser ? -1 : FREE_USER_MAX_TAGS_PER_PIN);

        return info;
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
                    state.isBlock,
                    state.tags
            );
            pins.add(entry);

            // 更新标签集合
            if (state.tags != null) {
                allTags.addAll(state.tags);
            }
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
     * 更新图钉标签
     *
     * @return 是否更新成功
     */
    public static boolean updateTags(PinEntry entry, List<String> newTags) {
        // 检查是否为专业版用户
        boolean isPremiumUser = cn.ilikexff.codepins.services.LicenseService.getInstance().isPremiumUser();

        // 如果不是专业版用户，检查标签数量限制
        if (!isPremiumUser) {
            // 检查每个图钉的标签数量限制
            if (newTags != null && newTags.size() > FREE_USER_MAX_TAGS_PER_PIN) {
                System.out.println("[CodePins] 更新标签失败：免费版用户每个图钉最多只能添加 " + FREE_USER_MAX_TAGS_PER_PIN + " 个标签");
                return false;
            }

            // 检查新标签是否会超出总标签种类限制
            if (newTags != null) {
                Set<String> existingTags = new HashSet<>(allTags);
                // 移除当前图钉的标签（因为要被替换）
                for (String tag : entry.getTags()) {
                    // 检查其他图钉是否也使用了这个标签
                    boolean tagUsedElsewhere = false;
                    for (PinEntry pin : pins) {
                        if (pin != entry && pin.hasTag(tag)) {
                            tagUsedElsewhere = true;
                            break;
                        }
                    }
                    // 如果没有其他图钉使用这个标签，从现有标签集合中移除
                    if (!tagUsedElsewhere) {
                        existingTags.remove(tag);
                    }
                }

                // 计算新增的标签种类
                Set<String> newTagsSet = new HashSet<>(newTags);
                newTagsSet.removeAll(existingTags);

                // 检查是否超出限制
                if (!newTagsSet.isEmpty() && (existingTags.size() + newTagsSet.size()) > FREE_USER_MAX_TAG_TYPES) {
                    System.out.println("[CodePins] 更新标签失败：免费版用户最多只能创建 " + FREE_USER_MAX_TAG_TYPES + " 种不同标签");
                    return false;
                }
            }
        }

        // 更新内存中的图钉标签
        entry.setTags(newTags);

        // 更新持久化存储中的标签
        Document doc = entry.marker.getDocument();
        int currentLine = entry.getCurrentLine(doc);

        for (PinState p : PinStateService.getInstance().getPins()) {
            if (p.filePath.equals(entry.filePath) && p.line == currentLine) {
                p.tags.clear();
                if (newTags != null) {
                    p.tags.addAll(newTags);
                }
                break;
            }
        }

        // 更新所有标签集合
        refreshAllTags();

        // 刷新UI
        refreshModel();

        return true;
    }

    /**
     * 获取所有标签
     */
    public static Set<String> getAllTags() {
        return new HashSet<>(allTags); // 返回副本，避免外部修改
    }

    /**
     * 刷新所有标签集合
     */
    private static void refreshAllTags() {
        allTags.clear();
        for (PinEntry pin : pins) {
            allTags.addAll(pin.getTags());
        }
    }

    /**
     * 根据标签筛选图钉
     */
    public static List<PinEntry> filterByTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>(pins); // 返回所有图钉
        }

        List<PinEntry> filtered = new ArrayList<>();
        for (PinEntry pin : pins) {
            boolean match = false;
            for (String tag : tags) {
                if (pin.hasTag(tag)) {
                    match = true;
                    break;
                }
            }
            if (match) {
                filtered.add(pin);
            }
        }

        return filtered;
    }

    /**
     * 添加图钉状态（用于导入功能）
     * 从 PinState 创建 PinEntry 并添加到存储中
     *
     * @param state 图钉状态
     * @return 是否添加成功
     */
    public static boolean addPinState(PinState state) {
        // 检查文件是否存在
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(state.filePath);
        if (file == null || !file.exists()) {
            System.out.println("[CodePins] 文件不存在: " + state.filePath);
            return false;
        }

        try {
            // 获取文件内容
            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document == null) {
                System.out.println("[CodePins] 无法获取文件内容: " + state.filePath);
                return false;
            }

            // 创建标记
            RangeMarker marker;
            if (state.isBlock && state.startOffset >= 0 && state.endOffset >= 0) {
                // 代码块图钉
                int startOffset = Math.min(state.startOffset, document.getTextLength());
                int endOffset = Math.min(state.endOffset, document.getTextLength());
                marker = document.createRangeMarker(startOffset, endOffset);
            } else {
                // 单行图钉
                int line = Math.min(state.line - 1, document.getLineCount() - 1);
                int lineStartOffset = document.getLineStartOffset(line);
                int lineEndOffset = document.getLineEndOffset(line);
                marker = document.createRangeMarker(lineStartOffset, lineEndOffset);
            }

            // 创建 PinEntry 并添加到存储
            PinEntry entry = new PinEntry(
                    state.filePath,
                    marker,
                    state.note,
                    state.timestamp,
                    state.author,
                    state.isBlock,
                    state.tags
            );

            pins.add(entry);
            allTags.addAll(state.tags);

            // 添加到持久化存储
            PinStateService.getInstance().addPin(state);

            refreshModel();
            return true;
        } catch (Exception e) {
            System.out.println("[CodePins] 添加图钉失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 交换两个图钉的位置
     *
     * @param fromIndex 起始索引
     * @param toIndex 目标索引
     */
    public static void movePinPosition(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= pins.size() || toIndex < 0 || toIndex >= pins.size() || fromIndex == toIndex) {
            return;
        }

        // 交换内存中的图钉位置
        PinEntry pin = pins.remove(fromIndex);
        pins.add(toIndex, pin);

        // 刷新 UI 模型
        refreshModel();

        // 保存自定义排序
        saveCustomOrder();
    }

    /**
     * 保存自定义排序
     */
    private static void saveCustomOrder() {
        // 将当前图钉顺序保存到持久化存储
        List<PinState> states = PinStateService.getInstance().getPins();

        // 清空并重新添加所有图钉，保持当前顺序
        PinStateService.getInstance().clear();

        for (PinEntry pin : pins) {
            Document doc = pin.marker.getDocument();
            int currentLine = pin.getCurrentLine(doc);

            // 创建新的 PinState
            PinState state;
            if (pin.isBlock) {
                state = new PinState(
                        pin.filePath,
                        currentLine,
                        pin.note,
                        pin.timestamp,
                        pin.author,
                        pin.isBlock,
                        pin.marker.getStartOffset(),
                        pin.marker.getEndOffset(),
                        pin.getTags()
                );
            } else {
                state = new PinState(
                        pin.filePath,
                        currentLine,
                        pin.note,
                        pin.timestamp,
                        pin.author,
                        pin.isBlock,
                        pin.getTags()
                );
            }

            PinStateService.getInstance().addPin(state);
        }
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
