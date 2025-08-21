package cn.ilikexff.codepins.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 图钉模板数据模型
 * 定义图钉模板的结构和属性
 */
public class PinTemplate {
    private String id;              // 模板唯一标识
    private String name;            // 模板名称
    private String description;     // 模板描述
    private String content;         // 模板内容（支持变量）
    private List<String> tags;      // 预设标签
    private TemplateType type;      // 模板类型
    private boolean isBuiltIn;      // 是否为内置模板
    private long createdTime;       // 创建时间
    private long modifiedTime;      // 修改时间
    private String author;          // 创建者

    /**
     * 模板类型枚举
     */
    public enum TemplateType {
        TODO("TODO", "待办事项"),
        FIXME("FIXME", "需要修复"),
        NOTE("NOTE", "备注说明"),
        REVIEW("REVIEW", "需要审查"),
        HACK("HACK", "临时解决方案"),
        BUG("BUG", "错误标记"),
        OPTIMIZE("OPTIMIZE", "性能优化"),
        REFACTOR("REFACTOR", "重构标记"),
        CUSTOM("CUSTOM", "自定义");

        private final String code;
        private final String displayName;

        TemplateType(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 默认构造函数
     */
    public PinTemplate() {
        this.tags = new ArrayList<>();
        this.createdTime = System.currentTimeMillis();
        this.modifiedTime = this.createdTime;
        this.author = System.getProperty("user.name");
    }

    /**
     * 完整构造函数
     */
    public PinTemplate(String id, String name, String description, String content, 
                      List<String> tags, TemplateType type, boolean isBuiltIn) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.content = content;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.type = type;
        this.isBuiltIn = isBuiltIn;
    }

    /**
     * 复制构造函数
     */
    public PinTemplate(PinTemplate other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.content = other.content;
        this.tags = new ArrayList<>(other.tags);
        this.type = other.type;
        this.isBuiltIn = other.isBuiltIn;
        this.createdTime = other.createdTime;
        this.modifiedTime = other.modifiedTime;
        this.author = other.author;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateModifiedTime();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        updateModifiedTime();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        updateModifiedTime();
    }

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        updateModifiedTime();
    }

    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty() && !this.tags.contains(tag)) {
            this.tags.add(tag);
            updateModifiedTime();
        }
    }

    public void removeTag(String tag) {
        if (this.tags.remove(tag)) {
            updateModifiedTime();
        }
    }

    public TemplateType getType() {
        return type;
    }

    public void setType(TemplateType type) {
        this.type = type;
        updateModifiedTime();
    }

    public boolean isBuiltIn() {
        return isBuiltIn;
    }

    public void setBuiltIn(boolean builtIn) {
        isBuiltIn = builtIn;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 更新修改时间
     */
    private void updateModifiedTime() {
        this.modifiedTime = System.currentTimeMillis();
    }

    /**
     * 验证模板是否有效
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               content != null && !content.trim().isEmpty() &&
               type != null;
    }

    /**
     * 获取显示名称（包含类型前缀）
     */
    public String getDisplayName() {
        if (type != null && type != TemplateType.CUSTOM) {
            return "[" + type.getDisplayName() + "] " + name;
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PinTemplate that = (PinTemplate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PinTemplate{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", isBuiltIn=" + isBuiltIn +
                '}';
    }
}
