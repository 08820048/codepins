package cn.ilikexff.codepins.git;

import cn.ilikexff.codepins.core.PinEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * 图钉版本快照
 */
public class PinSnapshot {
    
    private final String commitHash;
    private final String commitMessage;
    private final String branch;
    private final long timestamp;
    private final String author;
    private final List<PinEntry> pins;
    private final int totalPins;
    private final String projectPath;
    
    public PinSnapshot(String commitHash, String commitMessage, String branch, 
                      long timestamp, String author, List<PinEntry> pins, String projectPath) {
        this.commitHash = commitHash;
        this.commitMessage = commitMessage;
        this.branch = branch;
        this.timestamp = timestamp;
        this.author = author;
        this.pins = new ArrayList<>(pins);
        this.totalPins = pins.size();
        this.projectPath = projectPath;
    }
    
    /**
     * 获取快照摘要
     */
    public String getSummary() {
        return String.format("Commit %s: %d pins", 
            commitHash.substring(0, Math.min(8, commitHash.length())), totalPins);
    }
    
    /**
     * 获取显示文本
     */
    public String getDisplayText() {
        return String.format("[%s] %s - %d pins by %s", 
            branch, commitMessage, totalPins, author);
    }
    
    // Getters
    public String getCommitHash() { return commitHash; }
    public String getCommitMessage() { return commitMessage; }
    public String getBranch() { return branch; }
    public long getTimestamp() { return timestamp; }
    public String getAuthor() { return author; }
    public List<PinEntry> getPins() { return new ArrayList<>(pins); }
    public int getTotalPins() { return totalPins; }
    public String getProjectPath() { return projectPath; }
}
