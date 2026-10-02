package com.example.cyberbreach.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cyberbreach.engine.StatsEngine;

import java.util.HashMap;
import java.util.Map;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cyberbreach.db";
    private static final int DB_VERSION = 2;

    private static DbHelper instance;

    public static synchronized DbHelper get(Context context) {
        if (instance == null) {
            instance = new DbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static class Progress {
        public boolean unlocked;
        public int bestTimeSec;
        public int stars;
    }

    // ---------- create / upgrade ----------

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE progress ("
                + "level_id TEXT PRIMARY KEY, "
                + "unlocked INTEGER DEFAULT 0, "
                + "best_time_sec INTEGER DEFAULT 0, "
                + "stars INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE attempts ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "level_id TEXT, mode TEXT, time_sec INTEGER, "
                + "hints_used INTEGER, success INTEGER, played_at INTEGER)");
        db.execSQL("CREATE TABLE tool_upgrades ("
                + "tool_id TEXT PRIMARY KEY, tier INTEGER DEFAULT 1)");
        db.execSQL("CREATE TABLE player ("
                + "id INTEGER PRIMARY KEY CHECK (id = 1), credits INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE achievements ("
                + "achievement_id TEXT PRIMARY KEY, "
                + "unlocked_at INTEGER NOT NULL)");

        db.execSQL("INSERT INTO player (id, credits) VALUES (1, 0)");
        db.execSQL("INSERT INTO tool_upgrades (tool_id, tier) VALUES ('firewall', 1), ('log_filter', 1), ('packet_analyzer', 1), ('patch_manager', 1)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add missing tool seeds
            db.execSQL("INSERT OR IGNORE INTO tool_upgrades (tool_id, tier) VALUES ('packet_analyzer', 1)");
            db.execSQL("INSERT OR IGNORE INTO tool_upgrades (tool_id, tier) VALUES ('patch_manager', 1)");
            // Create achievements table
            db.execSQL("CREATE TABLE IF NOT EXISTS achievements ("
                    + "achievement_id TEXT PRIMARY KEY, "
                    + "unlocked_at INTEGER NOT NULL)");
        }
    }

    // ---------- INSERT ----------

    public long insertAttempt(String levelId, String mode, int timeSec, int hints, boolean success) {
        ContentValues cv = new ContentValues();
        cv.put("level_id", levelId);
        cv.put("mode", mode);
        cv.put("time_sec", timeSec);
        cv.put("hints_used", hints);
        cv.put("success", success ? 1 : 0);
        cv.put("played_at", System.currentTimeMillis());
        return getWritableDatabase().insert("attempts", null, cv);
    }

    /** Inserts the row if missing, then sets unlocked = 1. */
    public void unlock(String levelId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues insert = new ContentValues();
        insert.put("level_id", levelId);
        insert.put("unlocked", 1);
        db.insertWithOnConflict("progress", null, insert, SQLiteDatabase.CONFLICT_IGNORE);

        ContentValues update = new ContentValues();
        update.put("unlocked", 1);
        db.update("progress", update, "level_id = ?", new String[]{levelId});
    }

    // ---------- UPDATE ----------

    /** Keeps the fastest time and the highest star count for a level. */
    public void recordBest(String levelId, int timeSec, int stars) {
        unlock(levelId);
        Progress p = getProgress(levelId);

        ContentValues cv = new ContentValues();
        if (p.bestTimeSec == 0 || timeSec < p.bestTimeSec) {
            cv.put("best_time_sec", timeSec);
        }
        if (stars > p.stars) {
            cv.put("stars", stars);
        }
        if (cv.size() > 0) {
            getWritableDatabase().update("progress", cv, "level_id = ?", new String[]{levelId});
        }
    }

    public void addCredits(int amount) {
        getWritableDatabase().execSQL(
                "UPDATE player SET credits = credits + ? WHERE id = 1", new Object[]{amount});
    }

    /** Deducts credits and raises the tier together, or does nothing. */
    public boolean upgradeTool(String toolId, int cost) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (getCredits() < cost) {
                return false;   // no setTransactionSuccessful(), so nothing is committed
            }
            db.execSQL("UPDATE player SET credits = credits - ? WHERE id = 1", new Object[]{cost});
            db.execSQL("UPDATE tool_upgrades SET tier = tier + 1 WHERE tool_id = ?", new Object[]{toolId});
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    // ---------- QUERY ----------

    public Progress getProgress(String levelId) {
        Progress p = new Progress();
        try (Cursor c = getReadableDatabase().query("progress",
                new String[]{"unlocked", "best_time_sec", "stars"},
                "level_id = ?", new String[]{levelId}, null, null, null)) {
            if (c.moveToFirst()) {
                p.unlocked = c.getInt(0) == 1;
                p.bestTimeSec = c.getInt(1);
                p.stars = c.getInt(2);
            }
        }
        return p;
    }

    public Map<String, Progress> getAllProgress() {
        Map<String, Progress> map = new HashMap<>();
        try (Cursor c = getReadableDatabase().query("progress",
                new String[]{"level_id", "unlocked", "best_time_sec", "stars"},
                null, null, null, null, null)) {
            while (c.moveToNext()) {
                Progress p = new Progress();
                p.unlocked = c.getInt(1) == 1;
                p.bestTimeSec = c.getInt(2);
                p.stars = c.getInt(3);
                map.put(c.getString(0), p);
            }
        }
        return map;
    }

    public int getTotalStars() {
        return queryInt("SELECT COALESCE(SUM(stars), 0) FROM progress", null);
    }

    public int getAttemptCount() {
        return queryInt("SELECT COUNT(*) FROM attempts", null);
    }

    public int getCredits() {
        return queryInt("SELECT credits FROM player WHERE id = 1", null);
    }

    public int getToolTier(String toolId) {
        int tier = queryInt("SELECT tier FROM tool_upgrades WHERE tool_id = ?", new String[]{toolId});
        return Math.max(1, tier);
    }

    private int queryInt(String sql, String[] args) {
        try (Cursor c = getReadableDatabase().rawQuery(sql, args)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    // ---------- ACHIEVEMENTS ----------

    /** Unlocks an achievement if not already unlocked. Returns true if newly unlocked. */
    public boolean unlockAchievement(String achievementId) {
        ContentValues cv = new ContentValues();
        cv.put("achievement_id", achievementId);
        cv.put("unlocked_at", System.currentTimeMillis());
        long result = getWritableDatabase().insertWithOnConflict(
                "achievements", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    /** Returns set of unlocked achievement IDs. */
    public java.util.Set<String> getUnlockedAchievements() {
        java.util.Set<String> set = new java.util.HashSet<>();
        try (Cursor c = getReadableDatabase().query("achievements",
                new String[]{"achievement_id"}, null, null, null, null, null)) {
            while (c.moveToNext()) {
                set.add(c.getString(0));
            }
        }
        return set;
    }

    /** Checks if a specific achievement is unlocked. */
    public boolean isAchievementUnlocked(String achievementId) {
        return queryInt("SELECT COUNT(*) FROM achievements WHERE achievement_id = ?",
                new String[]{achievementId}) > 0;
    }

    // ---------- DELETE ----------

    public void clearHistory() {
        getWritableDatabase().delete("attempts", null, null);
    }

    public void resetProgress() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("progress", null, null);
            db.delete("attempts", null, null);
            db.delete("achievements", null, null);
            db.execSQL("UPDATE tool_upgrades SET tier = 1");
            db.execSQL("UPDATE player SET credits = 0 WHERE id = 1");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ---------- helpers ----------

    public static String rankForStars(int totalStars) {
        return StatsEngine.rankForStars(totalStars);
    }
}
