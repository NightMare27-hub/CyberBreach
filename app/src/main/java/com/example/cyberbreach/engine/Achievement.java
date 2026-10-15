package com.example.cyberbreach.engine;

public enum Achievement {
    SPEED_DEMON("Speed Demon", "Clear a Challenge room in under 30 seconds", android.R.drawable.ic_media_play),
    EAGLE_EYE("Eagle Eye", "Clear a room without using Packet Analyzer or hints", android.R.drawable.ic_menu_search),
    IRON_WALL("Iron Wall", "Contain a breach with zero wrong actions", android.R.drawable.ic_lock_lock),
    SCHOLAR("Scholar", "Read all security lessons in the Learn tab", android.R.drawable.ic_menu_compass),
    MASTER_ANALYST("Master Analyst", "Earn 3 stars across all available missions", android.R.drawable.btn_star_big_on);

    public final String title;
    public final String description;
    public final int iconResId;

    Achievement(String title, String description, int iconResId) {
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
    }
}
