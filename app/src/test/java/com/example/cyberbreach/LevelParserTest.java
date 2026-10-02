package com.example.cyberbreach;

import static org.junit.Assert.assertEquals;

import com.example.cyberbreach.data.LevelParser;
import com.example.cyberbreach.model.Level;

import org.json.JSONException;
import org.junit.Test;

import java.util.List;

public class LevelParserTest {

    private static final String VALID = "{ \"levels\": [ {"
            + "\"id\": \"a1\", \"title\": \"T\", \"timeLimitSec\": 60,"
            + "\"vulnerability\": { \"type\": \"open_port\", \"fix\": \"close_port\", \"target\": \"23\" },"
            + "\"hints\": [\"one\", \"two\"],"
            + "\"logs\": [ { \"time\": \"09:00:00\", \"src\": \"192.0.2.1\", \"port\": 23, \"event\": \"TELNET_OPEN\" } ]"
            + "} ] }";

    @Test
    public void parsesValidPack() throws JSONException {
        List<Level> levels = LevelParser.parsePack(VALID);
        assertEquals(1, levels.size());
        Level l = levels.get(0);
        assertEquals("a1", l.id);
        assertEquals(60, l.timeLimitSec);
        assertEquals("23", l.target);
        assertEquals(2, l.hints.size());
        assertEquals(23, l.logs.get(0).port);
        assertEquals("General", l.track);   // optional field falls back to its default
    }

    @Test(expected = JSONException.class)
    public void rejectsEmptyLevelList() throws JSONException {
        LevelParser.parsePack("{ \"levels\": [] }");
    }

    @Test(expected = JSONException.class)
    public void rejectsMalformedJson() throws JSONException {
        LevelParser.parsePack("{ \"levels\": [ ");
    }

    @Test(expected = JSONException.class)
    public void rejectsLevelWithoutVulnerability() throws JSONException {
        LevelParser.parsePack("{ \"levels\": [ { \"id\": \"x\", \"title\": \"T\","
                + "\"logs\": [ { \"time\": \"1\", \"src\": \"s\", \"port\": 1, \"event\": \"e\" } ] } ] }");
    }
}
