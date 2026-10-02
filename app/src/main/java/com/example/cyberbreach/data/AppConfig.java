package com.example.cyberbreach.data;

public final class AppConfig {
    private AppConfig() { }

    // Replace YOUR-USERNAME with your GitHub user name
    public static final String LEVELS_URL =
            "https://YOUR-USERNAME.github.io/cyberbreach-content/levels.json";

    public static final int CONNECT_TIMEOUT_MS = 8000;
    public static final int READ_TIMEOUT_MS = 8000;

    public static final String CACHE_FILE = "levels_cache.json";
    public static final String BUNDLED_ASSET = "levels.json";
}
