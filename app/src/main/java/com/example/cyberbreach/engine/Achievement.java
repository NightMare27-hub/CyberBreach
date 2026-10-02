package com.example.cyberbreach.engine;

public enum Achievement {
    SPEED_DEMON("Speed Demon", "Clear a Challenge room in under 30 seconds"),
    EAGLE_EYE("Eagle Eye", "Clear a room without using Packet Analyzer or hints"),
    IRON_WALL("Iron Wall", "Contain a breach with zero wrong actions"),
    MASTER_ANALYST("Master Analyst", "Earn 3 stars across all available levels");

    public final String title;
    public final String description;

    Achievement(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
