package cn.ilikexff.codepins.ai;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 建议学习引擎
 * 根据用户行为学习和优化建议准确性
 */
@Service
@State(name = "CodePinsSuggestionLearning", storages = @Storage("codepins-learning.xml"))
public final class SuggestionLearningEngine implements PersistentStateComponent<SuggestionLearningEngine.State> {
    
    /**
     * 状态类，用于持久化
     */
    public static class State {
        public Map<String, UserPreference> preferences = new HashMap<>();
        public Map<String, SuggestionFeedback> feedbacks = new HashMap<>();
        public long totalSuggestions = 0;
        public long appliedSuggestions = 0;
        public long dismissedSuggestions = 0;
    }
    
    /**
     * 用户偏好
     */
    public static class UserPreference {
        public Map<String, Double> typeWeights = new HashMap<>(); // 建议类型权重
        public Map<String, Double> priorityWeights = new HashMap<>(); // 优先级权重
        public double confidenceThreshold = 0.5; // 置信度阈值
        public Set<String> disabledTypes = new HashSet<>(); // 禁用的建议类型
        public long lastUpdated = System.currentTimeMillis();
        
        public UserPreference() {
            // 初始化默认权重
            initializeDefaultWeights();
        }
        
        private void initializeDefaultWeights() {
            // 建议类型默认权重
            typeWeights.put("TODO", 1.0);
            typeWeights.put("FIXME", 1.2);
            typeWeights.put("OPTIMIZE", 0.8);
            typeWeights.put("SECURITY", 1.5);
            typeWeights.put("CODE_SMELL", 0.7);
            typeWeights.put("COMPLEXITY", 0.9);
            typeWeights.put("DOCUMENTATION", 0.6);
            typeWeights.put("REFACTOR", 0.8);
            
            // 优先级默认权重
            priorityWeights.put("LOW", 0.5);
            priorityWeights.put("MEDIUM", 1.0);
            priorityWeights.put("HIGH", 1.5);
            priorityWeights.put("CRITICAL", 2.0);
        }
    }
    
    /**
     * 建议反馈
     */
    public static class SuggestionFeedback {
        public String suggestionType;
        public String priority;
        public boolean wasApplied;
        public long timestamp;
        public double originalConfidence;
        public String reason; // 用户反馈原因
        
        public SuggestionFeedback() {}
        
        public SuggestionFeedback(SmartSuggestion suggestion, boolean applied, String reason) {
            this.suggestionType = suggestion.getType().name();
            this.priority = suggestion.getPriority().name();
            this.wasApplied = applied;
            this.timestamp = System.currentTimeMillis();
            this.originalConfidence = suggestion.getConfidence();
            this.reason = reason;
        }
    }
    
    private State state = new State();
    private final Map<String, UserPreference> runtimePreferences = new ConcurrentHashMap<>();
    
    /**
     * 获取服务实例
     */
    public static SuggestionLearningEngine getInstance() {
        return ApplicationManager.getApplication().getService(SuggestionLearningEngine.class);
    }
    
    @Override
    public @Nullable State getState() {
        // 同步运行时偏好到状态
        state.preferences.putAll(runtimePreferences);
        return state;
    }
    
    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
        this.runtimePreferences.clear();
        this.runtimePreferences.putAll(state.preferences);
        
        // 确保有默认偏好
        if (!runtimePreferences.containsKey("default")) {
            runtimePreferences.put("default", new UserPreference());
        }
    }
    
    /**
     * 记录建议反馈
     */
    public void recordFeedback(SmartSuggestion suggestion, boolean applied, String reason) {
        String feedbackId = generateFeedbackId(suggestion);
        SuggestionFeedback feedback = new SuggestionFeedback(suggestion, applied, reason);
        
        state.feedbacks.put(feedbackId, feedback);
        state.totalSuggestions++;
        
        if (applied) {
            state.appliedSuggestions++;
        } else {
            state.dismissedSuggestions++;
        }
        
        // 更新用户偏好
        updateUserPreferences(suggestion, applied);
    }
    
    /**
     * 更新用户偏好
     */
    private void updateUserPreferences(SmartSuggestion suggestion, boolean applied) {
        UserPreference pref = runtimePreferences.computeIfAbsent("default", k -> new UserPreference());
        
        String type = suggestion.getType().name();
        String priority = suggestion.getPriority().name();
        
        // 根据用户行为调整权重
        double adjustment = applied ? 0.1 : -0.05; // 应用时增加权重，忽略时减少权重
        
        // 更新类型权重
        double currentTypeWeight = pref.typeWeights.getOrDefault(type, 1.0);
        pref.typeWeights.put(type, Math.max(0.1, Math.min(2.0, currentTypeWeight + adjustment)));
        
        // 更新优先级权重
        double currentPriorityWeight = pref.priorityWeights.getOrDefault(priority, 1.0);
        pref.priorityWeights.put(priority, Math.max(0.1, Math.min(3.0, currentPriorityWeight + adjustment)));
        
        // 调整置信度阈值
        if (!applied && suggestion.getConfidence() > pref.confidenceThreshold) {
            pref.confidenceThreshold = Math.min(0.9, pref.confidenceThreshold + 0.02);
        } else if (applied && suggestion.getConfidence() < pref.confidenceThreshold) {
            pref.confidenceThreshold = Math.max(0.1, pref.confidenceThreshold - 0.02);
        }
        
        pref.lastUpdated = System.currentTimeMillis();
    }
    
    /**
     * 优化建议列表
     */
    public List<SmartSuggestion> optimizeSuggestions(List<SmartSuggestion> suggestions) {
        UserPreference pref = runtimePreferences.getOrDefault("default", new UserPreference());
        
        return suggestions.stream()
                .filter(s -> !pref.disabledTypes.contains(s.getType().name()))
                .filter(s -> s.getConfidence() >= pref.confidenceThreshold)
                .peek(s -> adjustSuggestionScore(s, pref))
                .sorted((s1, s2) -> Double.compare(s2.getAdjustedScore(), s1.getAdjustedScore()))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 调整建议分数
     */
    private void adjustSuggestionScore(SmartSuggestion suggestion, UserPreference pref) {
        double typeWeight = pref.typeWeights.getOrDefault(suggestion.getType().name(), 1.0);
        double priorityWeight = pref.priorityWeights.getOrDefault(suggestion.getPriority().name(), 1.0);
        
        double adjustedScore = suggestion.getConfidence() * typeWeight * priorityWeight;
        suggestion.setAdjustedScore(adjustedScore);
    }
    
    /**
     * 获取建议统计
     */
    public LearningStatistics getStatistics() {
        LearningStatistics stats = new LearningStatistics();
        stats.totalSuggestions = state.totalSuggestions;
        stats.appliedSuggestions = state.appliedSuggestions;
        stats.dismissedSuggestions = state.dismissedSuggestions;
        stats.applyRate = state.totalSuggestions > 0 ? 
            (double) state.appliedSuggestions / state.totalSuggestions : 0.0;
        
        // 统计类型偏好
        UserPreference pref = runtimePreferences.getOrDefault("default", new UserPreference());
        stats.typePreferences = new HashMap<>(pref.typeWeights);
        stats.confidenceThreshold = pref.confidenceThreshold;
        
        return stats;
    }
    
    /**
     * 重置学习数据
     */
    public void resetLearning() {
        state.feedbacks.clear();
        state.totalSuggestions = 0;
        state.appliedSuggestions = 0;
        state.dismissedSuggestions = 0;
        runtimePreferences.clear();
        runtimePreferences.put("default", new UserPreference());
    }
    
    /**
     * 禁用建议类型
     */
    public void disableSuggestionType(SmartSuggestion.SuggestionType type) {
        UserPreference pref = runtimePreferences.computeIfAbsent("default", k -> new UserPreference());
        pref.disabledTypes.add(type.name());
    }
    
    /**
     * 启用建议类型
     */
    public void enableSuggestionType(SmartSuggestion.SuggestionType type) {
        UserPreference pref = runtimePreferences.computeIfAbsent("default", k -> new UserPreference());
        pref.disabledTypes.remove(type.name());
    }
    
    /**
     * 设置置信度阈值
     */
    public void setConfidenceThreshold(double threshold) {
        UserPreference pref = runtimePreferences.computeIfAbsent("default", k -> new UserPreference());
        pref.confidenceThreshold = Math.max(0.0, Math.min(1.0, threshold));
    }
    
    /**
     * 生成反馈ID
     */
    private String generateFeedbackId(SmartSuggestion suggestion) {
        return suggestion.getId() + "-" + System.currentTimeMillis();
    }
    
    /**
     * 学习统计信息
     */
    public static class LearningStatistics {
        public long totalSuggestions;
        public long appliedSuggestions;
        public long dismissedSuggestions;
        public double applyRate;
        public Map<String, Double> typePreferences;
        public double confidenceThreshold;
        
        @Override
        public String toString() {
            return String.format("总建议: %d, 已应用: %d, 应用率: %.1f%%, 置信度阈值: %.2f",
                               totalSuggestions, appliedSuggestions, applyRate * 100, confidenceThreshold);
        }
    }
}
