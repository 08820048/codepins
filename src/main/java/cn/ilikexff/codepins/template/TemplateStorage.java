package cn.ilikexff.codepins.template;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 模板存储服务
 * 负责模板的持久化存储和管理
 */
@Service
@State(name = "CodePinsTemplateStorage", storages = @Storage("codepins-templates.xml"))
public final class TemplateStorage implements PersistentStateComponent<TemplateStorage.State> {
    
    private final Map<String, PinTemplate> templates = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * 状态类，用于持久化
     */
    public static class State {
        public String templatesJson = "";
    }
    
    private State state = new State();
    
    /**
     * 获取服务实例
     */
    public static TemplateStorage getInstance() {
        return ApplicationManager.getApplication().getService(TemplateStorage.class);
    }
    
    /**
     * 初始化，加载内置模板
     */
    public TemplateStorage() {
        initializeBuiltInTemplates();
    }
    
    @Override
    public @Nullable State getState() {
        // 序列化模板到JSON
        List<PinTemplate> templateList = new ArrayList<>(templates.values());
        state.templatesJson = gson.toJson(templateList);
        return state;
    }
    
    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
        
        // 从JSON反序列化模板
        if (state.templatesJson != null && !state.templatesJson.isEmpty()) {
            try {
                Type listType = new TypeToken<List<PinTemplate>>(){}.getType();
                List<PinTemplate> templateList = gson.fromJson(state.templatesJson, listType);
                
                templates.clear();
                if (templateList != null) {
                    for (PinTemplate template : templateList) {
                        if (template != null && template.getId() != null) {
                            templates.put(template.getId(), template);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[CodePins] 加载模板失败: " + e.getMessage());
                templates.clear();
            }
        }
        
        // 确保内置模板存在
        initializeBuiltInTemplates();
    }
    
    /**
     * 初始化内置模板
     */
    private void initializeBuiltInTemplates() {
        // 只有当内置模板不存在时才创建
        if (templates.values().stream().noneMatch(PinTemplate::isBuiltIn)) {
            createBuiltInTemplates();
        }
    }
    
    /**
     * 创建内置模板
     */
    private void createBuiltInTemplates() {
        // TODO模板
        PinTemplate todoTemplate = new PinTemplate(
            "builtin-todo",
            "TODO任务",
            "标记需要完成的任务",
            "TODO: {description} - {author} {date}",
            Arrays.asList("TODO", "任务"),
            PinTemplate.TemplateType.TODO,
            true
        );
        
        // FIXME模板
        PinTemplate fixmeTemplate = new PinTemplate(
            "builtin-fixme",
            "FIXME修复",
            "标记需要修复的问题",
            "FIXME: {description} - {author} {date}",
            Arrays.asList("FIXME", "修复"),
            PinTemplate.TemplateType.FIXME,
            true
        );
        
        // NOTE模板
        PinTemplate noteTemplate = new PinTemplate(
            "builtin-note",
            "NOTE备注",
            "添加重要备注说明",
            "NOTE: {description} - {author} {date}",
            Arrays.asList("NOTE", "备注"),
            PinTemplate.TemplateType.NOTE,
            true
        );
        
        // REVIEW模板
        PinTemplate reviewTemplate = new PinTemplate(
            "builtin-review",
            "REVIEW审查",
            "标记需要代码审查的部分",
            "REVIEW: {description} - {author} {date}",
            Arrays.asList("REVIEW", "审查"),
            PinTemplate.TemplateType.REVIEW,
            true
        );
        
        // HACK模板
        PinTemplate hackTemplate = new PinTemplate(
            "builtin-hack",
            "HACK临时方案",
            "标记临时解决方案",
            "HACK: {description} - {author} {date}",
            Arrays.asList("HACK", "临时"),
            PinTemplate.TemplateType.HACK,
            true
        );
        
        // BUG模板
        PinTemplate bugTemplate = new PinTemplate(
            "builtin-bug",
            "BUG错误",
            "标记发现的错误",
            "BUG: {description} - {author} {date}",
            Arrays.asList("BUG", "错误"),
            PinTemplate.TemplateType.BUG,
            true
        );
        
        // 添加到存储
        templates.put(todoTemplate.getId(), todoTemplate);
        templates.put(fixmeTemplate.getId(), fixmeTemplate);
        templates.put(noteTemplate.getId(), noteTemplate);
        templates.put(reviewTemplate.getId(), reviewTemplate);
        templates.put(hackTemplate.getId(), hackTemplate);
        templates.put(bugTemplate.getId(), bugTemplate);
    }
    
    /**
     * 添加模板
     */
    public boolean addTemplate(PinTemplate template) {
        if (template == null || !template.isValid()) {
            return false;
        }
        
        // 生成ID（如果没有）
        if (template.getId() == null || template.getId().isEmpty()) {
            template.setId(generateTemplateId());
        }
        
        // 检查ID是否已存在
        if (templates.containsKey(template.getId())) {
            return false;
        }
        
        templates.put(template.getId(), new PinTemplate(template));
        return true;
    }
    
    /**
     * 更新模板
     */
    public boolean updateTemplate(PinTemplate template) {
        if (template == null || !template.isValid() || template.getId() == null) {
            return false;
        }
        
        PinTemplate existing = templates.get(template.getId());
        if (existing == null) {
            return false;
        }
        
        // 内置模板不能修改
        if (existing.isBuiltIn()) {
            return false;
        }
        
        templates.put(template.getId(), new PinTemplate(template));
        return true;
    }
    
    /**
     * 删除模板
     */
    public boolean removeTemplate(String templateId) {
        if (templateId == null || templateId.isEmpty()) {
            return false;
        }
        
        PinTemplate template = templates.get(templateId);
        if (template == null) {
            return false;
        }
        
        // 内置模板不能删除
        if (template.isBuiltIn()) {
            return false;
        }
        
        templates.remove(templateId);
        return true;
    }
    
    /**
     * 获取模板
     */
    public PinTemplate getTemplate(String templateId) {
        if (templateId == null || templateId.isEmpty()) {
            return null;
        }
        
        PinTemplate template = templates.get(templateId);
        return template != null ? new PinTemplate(template) : null;
    }
    
    /**
     * 获取所有模板
     */
    public List<PinTemplate> getAllTemplates() {
        return templates.values().stream()
                .map(PinTemplate::new)
                .sorted((t1, t2) -> {
                    // 内置模板排在前面
                    if (t1.isBuiltIn() && !t2.isBuiltIn()) return -1;
                    if (!t1.isBuiltIn() && t2.isBuiltIn()) return 1;
                    // 同类型按名称排序
                    return t1.getName().compareTo(t2.getName());
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 按类型获取模板
     */
    public List<PinTemplate> getTemplatesByType(PinTemplate.TemplateType type) {
        return templates.values().stream()
                .filter(t -> t.getType() == type)
                .map(PinTemplate::new)
                .sorted(Comparator.comparing(PinTemplate::getName))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户自定义模板
     */
    public List<PinTemplate> getCustomTemplates() {
        return templates.values().stream()
                .filter(t -> !t.isBuiltIn())
                .map(PinTemplate::new)
                .sorted(Comparator.comparing(PinTemplate::getName))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取内置模板
     */
    public List<PinTemplate> getBuiltInTemplates() {
        return templates.values().stream()
                .filter(PinTemplate::isBuiltIn)
                .map(PinTemplate::new)
                .sorted(Comparator.comparing(PinTemplate::getName))
                .collect(Collectors.toList());
    }
    
    /**
     * 清空所有自定义模板
     */
    public void clearCustomTemplates() {
        templates.entrySet().removeIf(entry -> !entry.getValue().isBuiltIn());
    }
    
    /**
     * 生成模板ID
     */
    private String generateTemplateId() {
        return "template-" + System.currentTimeMillis() + "-" + Math.random();
    }
    
    /**
     * 获取模板数量
     */
    public int getTemplateCount() {
        return templates.size();
    }
    
    /**
     * 检查模板ID是否存在
     */
    public boolean containsTemplate(String templateId) {
        return templateId != null && templates.containsKey(templateId);
    }
}
