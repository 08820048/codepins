package cn.ilikexff.codepins;

public class PinEntry {
    public final String filePath;
    public final int line;
    public final  String note;

    public PinEntry(String filePath, int line, String note) {
        this.filePath = filePath;
        this.line = line;
        this.note = note;
    }

    @Override
    public String toString() {
        return filePath + " @ Line " + (line + 1) + (note.isEmpty() ? "" : " - " + note);
    }
}