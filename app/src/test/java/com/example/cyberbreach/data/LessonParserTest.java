package com.example.cyberbreach.data;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.junit.Test;

import java.util.List;

public class LessonParserTest {

    private static final String VALID_LESSONS_JSON = "{\n" +
            "  \"version\": 1,\n" +
            "  \"pack\": \"Test Lessons\",\n" +
            "  \"lessons\": [\n" +
            "    {\n" +
            "      \"id\": \"les_1\",\n" +
            "      \"title\": \"Test Ports\",\n" +
            "      \"filename\": \"ports.html\",\n" +
            "      \"contentHtml\": \"<h2>Test</h2>\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Test
    public void parseValidLessons() throws JSONException {
        List<LessonParser.LessonData> list = LessonParser.parsePack(VALID_LESSONS_JSON);
        assertEquals(1, list.size());
        LessonParser.LessonData l = list.get(0);
        assertEquals("les_1", l.id);
        assertEquals("Test Ports", l.title);
        assertEquals("ports.html", l.filename);
        assertEquals("<h2>Test</h2>", l.contentHtml);
    }

    @Test(expected = JSONException.class)
    public void rejectEmptyLessons() throws JSONException {
        LessonParser.parsePack("{\"lessons\": []}");
    }
}
