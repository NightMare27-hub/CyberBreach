package com.example.cyberbreach.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LogEntry;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class LevelEngineTest {

    private Level sampleLevel() {
        return new Level("t1", "Test Level", "Org", "Networking basics", "ports", 100,
                "open_port", "close_port", "23", "debrief",
                Arrays.asList("hint1", "hint2"),
                Collections.singletonList(new LogEntry("09:00:00", "192.0.2.1", 23, "TELNET_OPEN", "detail")),
                Level.Difficulty.EASY);
    }

    @Test
    public void evaluatePerformanceUnsolved() {
        StarBreakdown sb = LevelEngine.evaluatePerformance(false, 0, 0, 100, 100);
        assertEquals(0, sb.totalStars);
        assertFalse(sb.star1Contained);
    }

    @Test
    public void evaluatePerformancePerfectSolve() {
        StarBreakdown sb = LevelEngine.evaluatePerformance(true, 0, 0, 80, 100);
        assertEquals(3, sb.totalStars);
        assertTrue(sb.star1Contained);
        assertTrue(sb.star2Precision);
        assertTrue(sb.star3Mastery);
    }

    @Test
    public void evaluatePerformanceManyMistakes() {
        StarBreakdown sb = LevelEngine.evaluatePerformance(true, 3, 0, 80, 100);
        assertEquals(2, sb.totalStars);
        assertFalse(sb.star2Precision);
    }

    @Test
    public void masteryStatusEvaluations() {
        assertEquals(MasteryStatus.LOCKED, LevelEngine.getMasteryStatus(false, false, 0, 0, 0));
        assertEquals(MasteryStatus.UNLOCKED, LevelEngine.getMasteryStatus(true, false, 0, 0, 0));
        assertEquals(MasteryStatus.COMPLETED, LevelEngine.getMasteryStatus(true, true, 2, 1, 1));
        assertEquals(MasteryStatus.MASTERED, LevelEngine.getMasteryStatus(true, true, 3, 0, 0));
    }

    @Test
    public void filterByConceptAndTrack() {
        Level l = sampleLevel();
        assertEquals(1, LevelEngine.filterByConcept(Collections.singletonList(l), "ports").size());
        assertEquals(0, LevelEngine.filterByConcept(Collections.singletonList(l), "bruteforce").size());

        assertEquals(1, LevelEngine.filterByTrack(Collections.singletonList(l), "Networking basics").size());
        assertEquals(0, LevelEngine.filterByTrack(Collections.singletonList(l), "Defense").size());
    }
}
