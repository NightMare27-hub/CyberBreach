package com.example.cyberbreach.data;

import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LogEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class LevelParser {
    private LevelParser() { }

    public static List<Level> parsePack(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONArray array = root.getJSONArray("levels");
        if (array.length() == 0) {
            throw new JSONException("Pack has no levels");
        }
        List<Level> levels = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            levels.add(parseLevel(array.getJSONObject(i)));
        }
        return levels;
    }

    private static Level parseLevel(JSONObject o) throws JSONException {
        JSONObject vuln = o.getJSONObject("vulnerability");

        List<String> hints = new ArrayList<>();
        JSONArray hintArray = o.optJSONArray("hints");
        if (hintArray != null) {
            for (int i = 0; i < hintArray.length(); i++) {
                hints.add(hintArray.getString(i));
            }
        }

        JSONArray logArray = o.getJSONArray("logs");
        if (logArray.length() == 0) {
            throw new JSONException("Level " + o.optString("id") + " has no logs");
        }
        List<LogEntry> logs = new ArrayList<>();
        for (int i = 0; i < logArray.length(); i++) {
            JSONObject e = logArray.getJSONObject(i);
            logs.add(new LogEntry(
                    e.getString("time"),
                    e.getString("src"),
                    e.getInt("port"),
                    e.getString("event"),
                    e.optString("detail", "")));
        }

        return new Level(
                o.getString("id"),
                o.getString("title"),
                o.optString("org", "Unknown organization"),
                o.optString("track", "General"),
                o.optString("concept", "ports"),
                o.optInt("timeLimitSec", 120),
                vuln.getString("type"),
                vuln.getString("fix"),
                vuln.getString("target"),
                o.optString("debrief", ""),
                hints,
                logs);
    }
}
