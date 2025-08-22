package cn.ilikexff.codepins.ai;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 智能分析管理器
 * 负责监听文件变化并触发智能分析
 */
@Service
public final class SmartAnalysisManager implements FileEditorManagerListener {
    
    private final Project project;
    private final SmartSuggestionService suggestionService;
    private final ScheduledExecutorService executor;
    private final ConcurrentHashMap<String, Long> lastAnalysisTime;
    
    // 分析间隔（毫秒）
    private static final long ANALYSIS_INTERVAL = 5000; // 5秒
    
    public SmartAnalysisManager(Project project) {
        this.project = project;
        this.suggestionService = SmartSuggestionService.getInstance(project);
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.lastAnalysisTime = new ConcurrentHashMap<>();
        
        // 注册监听器
        registerListeners();
    }
    
    /**
     * 获取服务实例
     */
    public static SmartAnalysisManager getInstance(Project project) {
        return project.getService(SmartAnalysisManager.class);
    }
    
    /**
     * 注册监听器
     */
    private void registerListeners() {
        // 监听文件编辑器变化
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
    }
    
    /**
     * 文件选择变化时触发
     */
    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        VirtualFile newFile = event.getNewFile();
        if (newFile != null && isAnalyzableFile(newFile)) {
            scheduleAnalysis(newFile);
        }
    }
    
    /**
     * 手动触发文件分析
     */
    public void analyzeFile(VirtualFile file) {
        if (file != null && isAnalyzableFile(file)) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                suggestionService.analyzeFile(project, file);
            });
        }
    }
    
    /**
     * 分析当前活动文件
     */
    public void analyzeCurrentFile() {
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        VirtualFile[] selectedFiles = editorManager.getSelectedFiles();
        
        if (selectedFiles.length > 0) {
            analyzeFile(selectedFiles[0]);
        }
    }
    
    /**
     * 分析当前编辑器中的文件
     */
    public void analyzeCurrentEditor() {
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        Editor editor = editorManager.getSelectedTextEditor();
        
        if (editor != null) {
            suggestionService.analyzeCurrentFile(project, editor);
        }
    }
    
    /**
     * 计划分析任务
     */
    private void scheduleAnalysis(VirtualFile file) {
        String filePath = file.getPath();
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastAnalysisTime.get(filePath);
        
        // 如果距离上次分析时间不足间隔，则延迟分析
        if (lastTime != null && (currentTime - lastTime) < ANALYSIS_INTERVAL) {
            long delay = ANALYSIS_INTERVAL - (currentTime - lastTime);
            executor.schedule(() -> performAnalysis(file), delay, TimeUnit.MILLISECONDS);
        } else {
            // 立即分析
            performAnalysis(file);
        }
    }
    
    /**
     * 执行分析
     */
    private void performAnalysis(VirtualFile file) {
        if (file.isValid() && isAnalyzableFile(file)) {
            lastAnalysisTime.put(file.getPath(), System.currentTimeMillis());
            
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                suggestionService.analyzeFile(project, file);
            });
        }
    }
    
    /**
     * 检查文件是否可分析
     */
    private boolean isAnalyzableFile(VirtualFile file) {
        if (file.isDirectory()) {
            return false;
        }
        
        String extension = file.getExtension();
        return extension != null && (
            extension.equals("java") ||
            extension.equals("kt") ||
            extension.equals("scala") ||
            extension.equals("groovy") ||
            extension.equals("js") ||
            extension.equals("ts") ||
            extension.equals("py") ||
            extension.equals("cpp") ||
            extension.equals("c") ||
            extension.equals("h")
        );
    }
    
    /**
     * 启用自动分析
     */
    public void enableAutoAnalysis() {
        // 分析当前打开的所有文件
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        VirtualFile[] openFiles = editorManager.getOpenFiles();
        
        for (VirtualFile file : openFiles) {
            if (isAnalyzableFile(file)) {
                scheduleAnalysis(file);
            }
        }
    }
    
    /**
     * 禁用自动分析
     */
    public void disableAutoAnalysis() {
        // 清除所有计划的分析任务
        lastAnalysisTime.clear();
    }
    
    /**
     * 清理资源
     */
    public void dispose() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * 获取分析统计信息
     */
    public AnalysisStatistics getStatistics() {
        AnalysisStatistics stats = new AnalysisStatistics();
        stats.analyzedFilesCount = lastAnalysisTime.size();
        stats.totalSuggestions = suggestionService.getStatistics().totalCount;
        stats.pendingSuggestions = suggestionService.getStatistics().pendingCount;
        return stats;
    }
    
    /**
     * 分析统计信息
     */
    public static class AnalysisStatistics {
        public int analyzedFilesCount = 0;
        public int totalSuggestions = 0;
        public int pendingSuggestions = 0;
        
        @Override
        public String toString() {
            return String.format("已分析文件: %d, 总建议: %d, 待处理: %d", 
                               analyzedFilesCount, totalSuggestions, pendingSuggestions);
        }
    }
}
