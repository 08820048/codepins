package cn.ilikexff.codepins.ai;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 智能建议服务
 * 管理AI建议的生成、存储和应用
 */
@Service
public final class SmartSuggestionService {
    
    private final Map<String, List<SmartSuggestion>> fileSuggestions = new ConcurrentHashMap<>();
    private final Map<String, SmartSuggestionEngine> engines = new ConcurrentHashMap<>();
    private final Set<SmartSuggestionListener> listeners = new HashSet<>();
    private final SuggestionLearningEngine learningEngine;
    
    /**
     * 建议监听器接口
     */
    public interface SmartSuggestionListener {
        void onSuggestionsUpdated(String filePath, List<SmartSuggestion> suggestions);
        void onSuggestionApplied(SmartSuggestion suggestion);
    }
    
    /**
     * 构造函数
     */
    public SmartSuggestionService() {
        this.learningEngine = SuggestionLearningEngine.getInstance();
    }

    /**
     * 获取服务实例
     */
    public static SmartSuggestionService getInstance(Project project) {
        return project.getService(SmartSuggestionService.class);
    }
    
    /**
     * 分析文件并生成建议
     */
    public void analyzeFile(Project project, VirtualFile file) {
        if (file == null || !isAnalyzableFile(file)) {
            return;
        }
        
        ApplicationManager.getApplication().runReadAction(() -> {
            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document != null) {
                String filePath = file.getPath();
                System.out.println("[SmartSuggestionService] 开始分析文件: " + filePath);

                SmartSuggestionEngine engine = getOrCreateEngine(project);
                List<SmartSuggestion> suggestions = engine.analyzeFile(file, document);

                System.out.println("[SmartSuggestionService] 原始建议数: " + suggestions.size());

                // 应用学习优化
                List<SmartSuggestion> optimizedSuggestions = learningEngine.optimizeSuggestions(suggestions);
                fileSuggestions.put(filePath, optimizedSuggestions);

                System.out.println("[SmartSuggestionService] 优化后建议数: " + optimizedSuggestions.size());

                // 通知监听器
                notifyListeners(filePath, optimizedSuggestions);
                System.out.println("[SmartSuggestionService] 已通知监听器");
            } else {
                System.out.println("[SmartSuggestionService] 无法获取文档: " + file.getPath());
            }
        });
    }
    
    /**
     * 分析当前编辑器中的文件
     */
    public void analyzeCurrentFile(Project project, Editor editor) {
        if (editor == null) {
            return;
        }
        
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        
        if (file != null) {
            analyzeFile(project, file);
        }
    }
    
    /**
     * 获取文件的建议
     */
    public List<SmartSuggestion> getSuggestions(String filePath) {
        return fileSuggestions.getOrDefault(filePath, new ArrayList<>());
    }
    
    /**
     * 获取文件在指定行的建议
     */
    public List<SmartSuggestion> getSuggestionsAtLine(String filePath, int line) {
        return getSuggestions(filePath).stream()
                .filter(s -> s.getStartLine() <= line && s.getEndLine() >= line)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取高优先级建议
     */
    public List<SmartSuggestion> getHighPrioritySuggestions(String filePath) {
        return getSuggestions(filePath).stream()
                .filter(s -> s.getPriority().getLevel() >= SmartSuggestion.Priority.HIGH.getLevel())
                .filter(s -> !s.isApplied())
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有未应用的建议
     */
    public List<SmartSuggestion> getUnappliedSuggestions(String filePath) {
        return getSuggestions(filePath).stream()
                .filter(s -> !s.isApplied())
                .collect(Collectors.toList());
    }
    
    /**
     * 按类型获取建议
     */
    public List<SmartSuggestion> getSuggestionsByType(String filePath, SmartSuggestion.SuggestionType type) {
        return getSuggestions(filePath).stream()
                .filter(s -> s.getType() == type)
                .filter(s -> !s.isApplied())
                .collect(Collectors.toList());
    }
    
    /**
     * 标记建议为已应用
     */
    public void markSuggestionApplied(String suggestionId, String pinId) {
        markSuggestionApplied(suggestionId, pinId, "applied");
    }

    /**
     * 标记建议为已应用（带原因）
     */
    public void markSuggestionApplied(String suggestionId, String pinId, String reason) {
        for (List<SmartSuggestion> suggestions : fileSuggestions.values()) {
            for (SmartSuggestion suggestion : suggestions) {
                if (suggestionId.equals(suggestion.getId())) {
                    suggestion.setApplied(true);
                    suggestion.setAppliedPinId(pinId);

                    // 记录学习反馈
                    boolean wasApplied = !"dismissed".equals(reason);
                    learningEngine.recordFeedback(suggestion, wasApplied, reason);

                    // 通知监听器
                    notifyAppliedListeners(suggestion);
                    return;
                }
            }
        }
    }
    
    /**
     * 清除文件的建议
     */
    public void clearSuggestions(String filePath) {
        fileSuggestions.remove(filePath);
    }
    
    /**
     * 清除所有建议
     */
    public void clearAllSuggestions() {
        fileSuggestions.clear();
    }
    
    /**
     * 获取建议统计信息
     */
    public SuggestionStatistics getStatistics() {
        SuggestionStatistics stats = new SuggestionStatistics();
        
        for (List<SmartSuggestion> suggestions : fileSuggestions.values()) {
            for (SmartSuggestion suggestion : suggestions) {
                stats.totalCount++;
                
                if (suggestion.isApplied()) {
                    stats.appliedCount++;
                } else {
                    stats.pendingCount++;
                }
                
                // 按类型统计
                SmartSuggestion.SuggestionType type = suggestion.getType();
                stats.typeCount.put(type, stats.typeCount.getOrDefault(type, 0) + 1);
                
                // 按优先级统计
                SmartSuggestion.Priority priority = suggestion.getPriority();
                stats.priorityCount.put(priority, stats.priorityCount.getOrDefault(priority, 0) + 1);
            }
        }
        
        return stats;
    }
    
    /**
     * 添加监听器
     */
    public void addListener(SmartSuggestionListener listener) {
        listeners.add(listener);
    }
    
    /**
     * 移除监听器
     */
    public void removeListener(SmartSuggestionListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 获取或创建分析引擎
     */
    private SmartSuggestionEngine getOrCreateEngine(Project project) {
        String projectPath = project.getBasePath();
        return engines.computeIfAbsent(projectPath, k -> new SmartSuggestionEngine(project));
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
            extension.equals("groovy")
        );
    }
    
    /**
     * 通知监听器建议更新
     */
    private void notifyListeners(String filePath, List<SmartSuggestion> suggestions) {
        for (SmartSuggestionListener listener : listeners) {
            try {
                listener.onSuggestionsUpdated(filePath, suggestions);
            } catch (Exception e) {
                // 忽略监听器异常
            }
        }
    }
    
    /**
     * 通知监听器建议已应用
     */
    private void notifyAppliedListeners(SmartSuggestion suggestion) {
        for (SmartSuggestionListener listener : listeners) {
            try {
                listener.onSuggestionApplied(suggestion);
            } catch (Exception e) {
                // 忽略监听器异常
            }
        }
    }
    
    /**
     * 建议统计信息
     */
    public static class SuggestionStatistics {
        public int totalCount = 0;
        public int appliedCount = 0;
        public int pendingCount = 0;
        public Map<SmartSuggestion.SuggestionType, Integer> typeCount = new HashMap<>();
        public Map<SmartSuggestion.Priority, Integer> priorityCount = new HashMap<>();
        
        public double getAppliedRate() {
            return totalCount > 0 ? (double) appliedCount / totalCount : 0.0;
        }
        
        public int getHighPriorityCount() {
            return priorityCount.getOrDefault(SmartSuggestion.Priority.HIGH, 0) +
                   priorityCount.getOrDefault(SmartSuggestion.Priority.CRITICAL, 0);
        }
    }
}
