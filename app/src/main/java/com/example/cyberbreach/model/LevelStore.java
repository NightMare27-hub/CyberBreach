package com.example.cyberbreach.model;

import java.util.ArrayList;
import java.util.List;

public final class LevelStore {
    private LevelStore() { }

    private static volatile List<Level> levels = new ArrayList<>();

    public static void set(List<Level> newLevels) {
        levels = new ArrayList<>(newLevels);
    }

    public static List<Level> getLevels() {
        return levels;
    }

    public static Level findById(String id) {
        if (id == null) return null;
        for (Level l : levels) {
            if (l.id.equals(id)) return l;
        }
        return null;
    }

    public static String nextId(String id) {
        for (int i = 0; i < levels.size() - 1; i++) {
            if (levels.get(i).id.equals(id)) return levels.get(i + 1).id;
        }
        return null;
    }
}
