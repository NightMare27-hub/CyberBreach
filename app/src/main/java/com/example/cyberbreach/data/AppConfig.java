package com.example.cyberbreach.data;

public final class AppConfig {
    private AppConfig() { }

    public static final String LEVELS_URL =
            "https://nightmare27-hub.github.io/CyberBreach/levels.json";

    public static final String LESSONS_URL =
            "https://nightmare27-hub.github.io/CyberBreach/lessons.json";

    public static final int CONNECT_TIMEOUT_MS = 8000;
    public static final int READ_TIMEOUT_MS = 8000;

    public static final String CACHE_FILE = "levels_cache.json";
    public static final String LESSONS_CACHE_FILE = "lessons_cache.json";
    public static final String BUNDLED_ASSET = "levels.json";
}
