package com.example.cyberbreach.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class StatsEngineTest {

    @Test
    public void xpCalculation() {
        assertEquals(270, StatsEngine.calculateXP(3, 2, 1));
    }

    @Test
    public void rankLadderProgression() {
        assertEquals("Trainee Analyst", StatsEngine.rankForStars(0));
        assertEquals("Junior Incident Responder", StatsEngine.rankForStars(4));
        assertEquals("SOC Analyst", StatsEngine.rankForStars(8));
        assertEquals("Security Engineer", StatsEngine.rankForStars(12));
        assertEquals("Senior Incident Lead", StatsEngine.rankForStars(18));
        assertEquals("Chief Information Security Officer (CISO)", StatsEngine.rankForStars(25));
    }

    @Test
    public void streakMultiplierAndCredits() {
        assertEquals(1.0f, StatsEngine.calculateStreakMultiplier(1), 0.01f);
        assertEquals(1.1f, StatsEngine.calculateStreakMultiplier(2), 0.01f);
        assertEquals(1.5f, StatsEngine.calculateStreakMultiplier(6), 0.01f);

        assertEquals(30, StatsEngine.calculateAwardedCredits(3, 1)); // 30 * 1.0 = 30
        assertEquals(33, StatsEngine.calculateAwardedCredits(3, 2)); // 30 * 1.1 = 33
    }

    @Test
    public void achievementEvaluations() {
        List<Achievement> achievements = StatsEngine.evaluateAchievements(true, 25, 0, 0, false);
        assertTrue(achievements.contains(Achievement.IRON_WALL));
        assertTrue(achievements.contains(Achievement.SPEED_DEMON));
        assertTrue(achievements.contains(Achievement.EAGLE_EYE));
    }
}
