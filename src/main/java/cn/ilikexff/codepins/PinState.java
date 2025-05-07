package cn.ilikexff.codepins;

public class PinState {
    public String filePath;
    public int line;
    public String note;

    public PinState() {}

    public PinState(String filePath, int line, String note) {
        this.filePath = filePath;
        this.line = line;
        this.note = note;
    }
}