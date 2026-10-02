package com.example.cyberbreach.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrefsManager {

    private static final String FILE = "cyberbreach_prefs";
    private static final String KEY_MUTED = "muted";
    private static final String KEY_HAPTICS = "haptics";
    private static final String KEY_REMINDER = "reminder";
    private static final String KEY_FONT_SCALE = "font_scale";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_LAST_PLAY = "last_play_date";

    private final SharedPreferences sp;

    public PrefsManager(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public boolean isMuted() { return sp.getBoolean(KEY_MUTED, false); }
    public void setMuted(boolean v) { sp.edit().putBoolean(KEY_MUTED, v).apply(); }

    public boolean isHapticsEnabled() { return sp.getBoolean(KEY_HAPTICS, true); }
    public void setHapticsEnabled(boolean v) { sp.edit().putBoolean(KEY_HAPTICS, v).apply(); }

    public boolean isReminderEnabled() { return sp.getBoolean(KEY_REMINDER, false); }
    public void setReminderEnabled(boolean v) { sp.edit().putBoolean(KEY_REMINDER, v).apply(); }

    public float getFontScale() { return sp.getFloat(KEY_FONT_SCALE, 1.0f); }
    public void setFontScale(float v) { sp.edit().putFloat(KEY_FONT_SCALE, v).apply(); }

    public int getStreak() { return sp.getInt(KEY_STREAK, 0); }

    public void updateStreak() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String today = fmt.format(new Date());
        String last = sp.getString(KEY_LAST_PLAY, "");
        if (today.equals(last)) return;

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        int streak = fmt.format(yesterday.getTime()).equals(last)
                ? sp.getInt(KEY_STREAK, 0) + 1
                : 1;
        sp.edit().putString(KEY_LAST_PLAY, today).putInt(KEY_STREAK, streak).apply();
    }
}
