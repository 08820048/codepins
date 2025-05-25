package cn.codepins;

import java.util.ArrayList;
import java.util.List;

public class PinStorage {
    private static final List<PinEntry> pins = new ArrayList<>();

    public static void addPin(PinEntry entry) {
        pins.add(entry);
    }

    public static List<PinEntry> getPins() {
        return pins;
    }
}