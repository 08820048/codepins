package cn.ilikexff.codepins.git;

import cn.ilikexff.codepins.core.PinEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * 图钉版本差异
 */
public class PinDiff {
    
    /**
     * 差异类型
     */
    public enum DiffType {
        ADDED("新增"),
        REMOVED("删除"),
        MODIFIED("修改"),
        MOVED("移动");
        
        private final String displayName;
        
        DiffType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    /**
     * 图钉差异项
     */
    public static class PinDiffItem {
        private final DiffType type;
        private final PinEntry oldPin;
        private final PinEntry newPin;
        private final String description;
        
        public PinDiffItem(DiffType type, PinEntry oldPin, PinEntry newPin, String description) {
            this.type = type;
            this.oldPin = oldPin;
            this.newPin = newPin;
            this.description = description;
        }
        
        public DiffType getType() { return type; }
        public PinEntry getOldPin() { return oldPin; }
        public PinEntry getNewPin() { return newPin; }
        public String getDescription() { return description; }
        
        public String getDisplayText() {
            switch (type) {
                case ADDED:
                    return String.format("+ %s", newPin.note);
                case REMOVED:
                    return String.format("- %s", oldPin.note);
                case MODIFIED:
                    return String.format("~ %s -> %s", oldPin.note, newPin.note);
                case MOVED:
                    return String.format("→ %s (moved)", newPin.note);
                default:
                    return description;
            }
        }
    }
    
    private final String fromCommit;
    private final String toCommit;
    private final List<PinDiffItem> differences;
    private final long timestamp;
    
    public PinDiff() {
        this("", "", new ArrayList<>());
    }
    
    public PinDiff(String fromCommit, String toCommit, List<PinDiffItem> differences) {
        this.fromCommit = fromCommit;
        this.toCommit = toCommit;
        this.differences = new ArrayList<>(differences);
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 添加差异项
     */
    public void addDifference(DiffType type, PinEntry oldPin, PinEntry newPin, String description) {
        differences.add(new PinDiffItem(type, oldPin, newPin, description));
    }
    
    /**
     * 获取指定类型的差异数量
     */
    public int getCountByType(DiffType type) {
        return (int) differences.stream().filter(diff -> diff.getType() == type).count();
    }
    
    /**
     * 获取摘要
     */
    public String getSummary() {
        int added = getCountByType(DiffType.ADDED);
        int removed = getCountByType(DiffType.REMOVED);
        int modified = getCountByType(DiffType.MODIFIED);
        int moved = getCountByType(DiffType.MOVED);
        
        return String.format("Changes: +%d -%d ~%d →%d", added, removed, modified, moved);
    }
    
    /**
     * 检查是否有变化
     */
    public boolean hasChanges() {
        return !differences.isEmpty();
    }
    
    // Getters
    public String getFromCommit() { return fromCommit; }
    public String getToCommit() { return toCommit; }
    public List<PinDiffItem> getDifferences() { return new ArrayList<>(differences); }
    public long getTimestamp() { return timestamp; }
}
