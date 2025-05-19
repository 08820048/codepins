package ilikexff.codepins;

public class PinEntry {
    public final String filePath;
    public final int line;

    public PinEntry(String filePath, int line) {
        this.filePath = filePath;
        this.line = line;
    }

    @Override
    public String toString() {
        return filePath + " @ Line " + (line + 1);
    }
}