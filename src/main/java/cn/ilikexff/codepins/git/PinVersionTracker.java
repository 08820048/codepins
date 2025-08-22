package cn.ilikexff.codepins.git;

import cn.ilikexff.codepins.core.PinEntry;
import cn.ilikexff.codepins.core.PinStorage;
import com.intellij.openapi.project.Project;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 图钉版本追踪器
 * 负责保存和管理图钉的版本历史
 */
public class PinVersionTracker {
    
    private final Project project;
    private final Path snapshotDir;
    private final String SNAPSHOT_FILE_PREFIX = "pins_snapshot_";
    private final String SNAPSHOT_FILE_SUFFIX = ".json";
    
    public PinVersionTracker(Project project) {
        this.project = project;
        this.snapshotDir = Paths.get(project.getBasePath(), ".git", "codepins", "snapshots");
    }
    
    /**
     * 初始化版本追踪
     */
    public void initialize() {
        try {
            Files.createDirectories(snapshotDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 保存图钉快照
     */
    public boolean saveSnapshot(String commitHash, String message) {
        try {
            List<PinEntry> currentPins = PinStorage.getPins();
            
            PinSnapshot snapshot = new PinSnapshot(
                commitHash,
                message,
                getCurrentBranch(),
                System.currentTimeMillis(),
                getCurrentUser(),
                currentPins,
                project.getBasePath()
            );
            
            String fileName = SNAPSHOT_FILE_PREFIX + commitHash + SNAPSHOT_FILE_SUFFIX;
            Path snapshotFile = snapshotDir.resolve(fileName);
            
            // 简单的JSON序列化（实际项目中可以使用Jackson等库）
            String jsonContent = serializeSnapshot(snapshot);
            Files.write(snapshotFile, jsonContent.getBytes());
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取历史快照
     */
    public List<PinSnapshot> getHistory() {
        List<PinSnapshot> snapshots = new ArrayList<>();
        
        try {
            if (!Files.exists(snapshotDir)) {
                return snapshots;
            }
            
            Files.list(snapshotDir)
                .filter(path -> path.getFileName().toString().startsWith(SNAPSHOT_FILE_PREFIX))
                .forEach(path -> {
                    try {
                        String content = new String(Files.readAllBytes(path));
                        PinSnapshot snapshot = deserializeSnapshot(content);
                        if (snapshot != null) {
                            snapshots.add(snapshot);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            
            // 按时间戳排序
            snapshots.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return snapshots;
    }
    
    /**
     * 比较两个版本的差异
     */
    public PinDiff compareVersions(String fromCommit, String toCommit) {
        try {
            PinSnapshot fromSnapshot = loadSnapshot(fromCommit);
            PinSnapshot toSnapshot = loadSnapshot(toCommit);
            
            if (fromSnapshot == null || toSnapshot == null) {
                return new PinDiff();
            }
            
            return calculateDiff(fromSnapshot, toSnapshot);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new PinDiff();
        }
    }
    
    /**
     * 加载指定提交的快照
     */
    private PinSnapshot loadSnapshot(String commitHash) {
        try {
            String fileName = SNAPSHOT_FILE_PREFIX + commitHash + SNAPSHOT_FILE_SUFFIX;
            Path snapshotFile = snapshotDir.resolve(fileName);
            
            if (!Files.exists(snapshotFile)) {
                return null;
            }
            
            String content = new String(Files.readAllBytes(snapshotFile));
            return deserializeSnapshot(content);
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 计算两个快照的差异
     */
    private PinDiff calculateDiff(PinSnapshot fromSnapshot, PinSnapshot toSnapshot) {
        PinDiff diff = new PinDiff(fromSnapshot.getCommitHash(), toSnapshot.getCommitHash(), new ArrayList<>());
        
        List<PinEntry> fromPins = fromSnapshot.getPins();
        List<PinEntry> toPins = toSnapshot.getPins();
        
        // 简单的差异计算（实际实现可以更复杂）
        Map<String, PinEntry> fromPinMap = new HashMap<>();
        for (PinEntry pin : fromPins) {
            fromPinMap.put(pin.filePath + ":" + pin.note, pin);
        }
        
        Map<String, PinEntry> toPinMap = new HashMap<>();
        for (PinEntry pin : toPins) {
            toPinMap.put(pin.filePath + ":" + pin.note, pin);
        }
        
        // 查找新增的图钉
        for (String key : toPinMap.keySet()) {
            if (!fromPinMap.containsKey(key)) {
                diff.addDifference(PinDiff.DiffType.ADDED, null, toPinMap.get(key), "新增图钉");
            }
        }
        
        // 查找删除的图钉
        for (String key : fromPinMap.keySet()) {
            if (!toPinMap.containsKey(key)) {
                diff.addDifference(PinDiff.DiffType.REMOVED, fromPinMap.get(key), null, "删除图钉");
            }
        }
        
        return diff;
    }
    
    /**
     * 序列化快照（简单实现）
     */
    private String serializeSnapshot(PinSnapshot snapshot) {
        // 这里是简化的JSON序列化，实际项目中建议使用专业的JSON库
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"commitHash\": \"").append(snapshot.getCommitHash()).append("\",\n");
        json.append("  \"commitMessage\": \"").append(snapshot.getCommitMessage()).append("\",\n");
        json.append("  \"branch\": \"").append(snapshot.getBranch()).append("\",\n");
        json.append("  \"timestamp\": ").append(snapshot.getTimestamp()).append(",\n");
        json.append("  \"author\": \"").append(snapshot.getAuthor()).append("\",\n");
        json.append("  \"totalPins\": ").append(snapshot.getTotalPins()).append(",\n");
        json.append("  \"projectPath\": \"").append(snapshot.getProjectPath()).append("\"\n");
        json.append("}");
        return json.toString();
    }
    
    /**
     * 反序列化快照（简单实现）
     */
    private PinSnapshot deserializeSnapshot(String jsonContent) {
        // 这里是简化的JSON反序列化，实际项目中建议使用专业的JSON库
        try {
            // 简单解析（实际应该用正则或JSON库）
            String commitHash = extractJsonValue(jsonContent, "commitHash");
            String commitMessage = extractJsonValue(jsonContent, "commitMessage");
            String branch = extractJsonValue(jsonContent, "branch");
            long timestamp = Long.parseLong(extractJsonValue(jsonContent, "timestamp"));
            String author = extractJsonValue(jsonContent, "author");
            String projectPath = extractJsonValue(jsonContent, "projectPath");
            
            // 这里简化处理，实际应该保存和恢复完整的图钉数据
            return new PinSnapshot(commitHash, commitMessage, branch, timestamp, author, 
                                 new ArrayList<>(), projectPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 简单的JSON值提取（实际项目中应该使用JSON库）
     */
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        
        // 尝试数字值
        pattern = "\"" + key + "\":\\s*(\\d+)";
        p = java.util.regex.Pattern.compile(pattern);
        m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        
        return "";
    }
    
    /**
     * 获取当前分支（简化实现）
     */
    private String getCurrentBranch() {
        return "main"; // 简化实现
    }
    
    /**
     * 获取当前用户（简化实现）
     */
    private String getCurrentUser() {
        return System.getProperty("user.name", "unknown");
    }
}
