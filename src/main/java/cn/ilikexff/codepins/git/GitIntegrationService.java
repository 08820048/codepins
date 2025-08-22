package cn.ilikexff.codepins.git;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Git集成服务
 * 提供CodePins与Git的集成功能
 */
@Service
public final class GitIntegrationService {
    
    /**
     * Git集成功能类型
     */
    public enum IntegrationType {
        PIN_VERSIONING("图钉版本追踪"),
        DIFF_ANALYSIS("差异分析建议"),
        TEAM_SYNC("团队同步"),
        COMMIT_QUALITY("提交质量分析");
        
        private final String displayName;
        
        IntegrationType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    /**
     * Git集成配置
     */
    public static class GitIntegrationConfig {
        public boolean enablePinVersioning = true;
        public boolean enableDiffAnalysis = true;
        public boolean enableTeamSync = false;
        public boolean enableCommitQuality = true;
        public String syncBranch = "codepins-data";
        public int maxHistoryEntries = 100;
    }
    
    private final Project project;
    private final GitIntegrationConfig config;
    private final PinVersionTracker versionTracker;
    private final DiffAnalyzer diffAnalyzer;
    
    public GitIntegrationService(Project project) {
        this.project = project;
        this.config = new GitIntegrationConfig();
        this.versionTracker = new PinVersionTracker(project);
        this.diffAnalyzer = new DiffAnalyzer(project);
    }
    
    /**
     * 获取服务实例
     */
    public static GitIntegrationService getInstance(Project project) {
        return project.getService(GitIntegrationService.class);
    }
    
    /**
     * 初始化Git集成
     */
    public CompletableFuture<Boolean> initialize() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 检查Git仓库
                if (!isGitRepository()) {
                    return false;
                }
                
                // 初始化版本追踪
                if (config.enablePinVersioning) {
                    versionTracker.initialize();
                }
                
                // 初始化差异分析
                if (config.enableDiffAnalysis) {
                    diffAnalyzer.initialize();
                }
                
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
    
    /**
     * 检查是否为Git仓库
     */
    public boolean isGitRepository() {
        // 简化实现：检查.git目录是否存在
        java.io.File gitDir = new java.io.File(project.getBasePath(), ".git");
        return gitDir.exists() && gitDir.isDirectory();
    }

    /**
     * 获取当前Git仓库（简化实现）
     */
    public String getCurrentRepository() {
        return project.getBasePath();
    }
    
    /**
     * 分析当前变更并建议图钉
     */
    public CompletableFuture<List<PinSuggestion>> analyzeChangesForPins() {
        return CompletableFuture.supplyAsync(() -> {
            if (!config.enableDiffAnalysis) {
                return new ArrayList<>();
            }
            
            try {
                // 获取当前变更
                ChangeListManager changeManager = ChangeListManager.getInstance(project);
                Collection<Change> changes = changeManager.getAllChanges();
                
                // 分析变更并生成建议
                return diffAnalyzer.analyzeChanges(changes);
                
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }
    
    /**
     * 保存图钉版本快照
     */
    public CompletableFuture<Boolean> savePinSnapshot(String commitHash, String message) {
        return CompletableFuture.supplyAsync(() -> {
            if (!config.enablePinVersioning) {
                return false;
            }
            
            try {
                return versionTracker.saveSnapshot(commitHash, message);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
    
    /**
     * 获取图钉历史
     */
    public CompletableFuture<List<PinSnapshot>> getPinHistory() {
        return CompletableFuture.supplyAsync(() -> {
            if (!config.enablePinVersioning) {
                return new ArrayList<>();
            }
            
            try {
                return versionTracker.getHistory();
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }
    
    /**
     * 比较两个版本的图钉差异
     */
    public CompletableFuture<PinDiff> comparePinVersions(String fromCommit, String toCommit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return versionTracker.compareVersions(fromCommit, toCommit);
            } catch (Exception e) {
                e.printStackTrace();
                return new PinDiff();
            }
        });
    }
    
    /**
     * 获取当前分支名（简化实现）
     */
    public String getCurrentBranch() {
        try {
            // 简化实现：读取.git/HEAD文件
            java.io.File headFile = new java.io.File(project.getBasePath(), ".git/HEAD");
            if (headFile.exists()) {
                String content = new String(java.nio.file.Files.readAllBytes(headFile.toPath()));
                if (content.startsWith("ref: refs/heads/")) {
                    return content.substring("ref: refs/heads/".length()).trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "main";
    }

    /**
     * 获取最新提交哈希（简化实现）
     */
    public String getLatestCommitHash() {
        try {
            // 简化实现：返回时间戳作为伪哈希
            return String.valueOf(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检查文件是否在Git跟踪中（简化实现）
     */
    public boolean isFileTracked(String filePath) {
        // 简化实现：假设所有项目文件都被跟踪
        return filePath.startsWith(project.getBasePath());
    }
    
    /**
     * 获取文件的Git状态
     */
    public FileGitStatus getFileStatus(String filePath) {
        try {
            ChangeListManager changeManager = ChangeListManager.getInstance(project);
            // 这里可以实现具体的文件状态检查逻辑
            return FileGitStatus.UNMODIFIED;
        } catch (Exception e) {
            e.printStackTrace();
            return FileGitStatus.UNKNOWN;
        }
    }
    
    /**
     * 文件Git状态枚举
     */
    public enum FileGitStatus {
        UNMODIFIED("未修改"),
        MODIFIED("已修改"),
        ADDED("新增"),
        DELETED("删除"),
        RENAMED("重命名"),
        UNKNOWN("未知");
        
        private final String displayName;
        
        FileGitStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    // Getters and Setters
    public GitIntegrationConfig getConfig() { return config; }
    public PinVersionTracker getVersionTracker() { return versionTracker; }
    public DiffAnalyzer getDiffAnalyzer() { return diffAnalyzer; }
}
