package cn.ilikexff.codepins;

public class PinState {
    public String filePath;
    public int line;
    public String note;
    public long timestamp;  // 添加
    public String author;   // 添加
    public PinState() {}

    public PinState(String filePath, int line, String note,long timestamp,String author) {
        this.filePath = filePath;
        this.line = line;
        this.note = note;
        this.timestamp = timestamp;
        this.author = author;
    }
}