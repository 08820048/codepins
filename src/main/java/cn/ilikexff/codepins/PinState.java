package cn.ilikexff.codepins;

import java.util.ArrayList;
import java.util.List;

/**
 * 图钉持久化快照数据类
 * 用于保存最小必要信息以持久化到 XML 文件
 */
public class PinState {
    public String filePath;       // 文件路径
    public int line;              // 图钉所在行号（静态保存）
    public String note;           // 图钉备注
    public long timestamp;        // 创建时间戳
    public String author;         // 创建者
    public boolean isBlock;       // 是否为代码块图钉
    public int startOffset;       // 代码块开始偏移量（仅对代码块图钉有效）
    public int endOffset;         // 代码块结束偏移量（仅对代码块图钉有效）
    public List<String> tags = new ArrayList<>();     // 图钉标签列表

    public PinState() {
        // 默认构造函数（必须有）
        // tags 已在字段声明中初始化
    }

    public PinState(String filePath, int line, String note, long timestamp, String author, boolean isBlock) {
        this.filePath = filePath;
        this.line = line;
        this.note = note;
        this.timestamp = timestamp;
        this.author = author;
        this.isBlock = isBlock;
        this.startOffset = -1; // 默认值，表示未设置
        this.endOffset = -1;  // 默认值，表示未设置
        this.tags = new ArrayList<>();
    }

    public PinState(String filePath, int line, String note, long timestamp, String author, boolean isBlock, int startOffset, int endOffset) {
        this.filePath = filePath;
        this.line = line;
        this.note = note;
        this.timestamp = timestamp;
        this.author = author;
        this.isBlock = isBlock;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.tags = new ArrayList<>();
    }

    /**
     * 带标签的构造函数（单行图钉）
     */
    public PinState(String filePath, int line, String note, long timestamp, String author, boolean isBlock, List<String> tags) {
        this.filePath = filePath;
        this.line = line;
        this.note = note;
        this.timestamp = timestamp;
        this.author = author;
        this.isBlock = isBlock;
        this.startOffset = -1;
        this.endOffset = -1;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    /**
     * 带标签的构造函数（代码块图钉）
     */
    public PinState(String filePath, int line, String note, long timestamp, String author, boolean isBlock, int startOffset, int endOffset, List<String> tags) {
        this.filePath = filePath;
        this.line = line;
        this.note = note;
        this.timestamp = timestamp;
        this.author = author;
        this.isBlock = isBlock;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }
}