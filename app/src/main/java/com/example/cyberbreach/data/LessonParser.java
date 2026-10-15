package com.example.cyberbreach.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class LessonParser {
    private LessonParser() { }

    public static class LessonData {
        public final String id;
        public final String title;
        public final String filename;
        public final String contentHtml;

        public LessonData(String id, String title, String filename, String contentHtml) {
            this.id = id;
            this.title = title;
            this.filename = filename;
            this.contentHtml = contentHtml;
        }
    }

    public static List<LessonData> parsePack(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONArray array = root.getJSONArray("lessons");
        if (array.length() == 0) {
            throw new JSONException("Lesson pack is empty");
        }
        List<LessonData> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);
            list.add(new LessonData(
                    o.getString("id"),
                    o.getString("title"),
                    o.optString("filename", "lesson.html"),
                    o.optString("contentHtml", "")
            ));
        }
        return list;
    }
}
