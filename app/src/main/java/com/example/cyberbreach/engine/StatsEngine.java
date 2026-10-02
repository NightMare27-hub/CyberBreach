package com.example.cyberbreach.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatsEngine {

    /** Calculates total Experience Points (XP) from stars, attempts, and zero-mistake clears. */
    public static int calculateXP(int totalStars, int totalAttempts, int perfectClears) {
        return (totalStars * 50) + (totalAttempts * 10) + (perfectClears * 100);
    }

    /** 6-Tier SOC Analyst Rank Ladder based on total stars or XP. */
    public static String rankForStars(int totalStars) {
        if (totalStars < 3) return "Trainee Analyst";
        if (totalStars < 6) return "Junior Incident Responder";
        if (totalStars < 10) return "SOC Analyst";
        if (totalStars < 15) return "Security Engineer";
        if (totalStars < 20) return "Senior Incident Lead";
        return "Chief Information Security Officer (CISO)";
    }

    /** Calculates streak credit multiplier bonus (1.0x to 1.5x based on daily streak). */
    public static float calculateStreakMultiplier(int currentStreak) {
        int bonusPercent = Math.min(50, Math.max(0, (currentStreak - 1) * 10));
        return 1.0f + (bonusPercent / 100.0f);
    }

    /** Calculates bonus credits awarded after a win including streak multiplier. */
    public static int calculateAwardedCredits(int starsEarned, int streak) {
        int base = starsEarned * 10;
        float multiplier = calculateStreakMultiplier(streak);
        return Math.round(base * multiplier);
    }

    /** Evaluates achievements unlocked for a completed room attempt. */
    public static List<Achievement> evaluateAchievements(boolean solved, int timeSec, int wrongActions, int hintsUsed, boolean usedAnalyzer) {
        List<Achievement> unlocked = new ArrayList<>();
        if (!solved) return unlocked;

        if (wrongActions == 0) {
            unlocked.add(Achievement.IRON_WALL);
        }
        if (timeSec <= 30) {
            unlocked.add(Achievement.SPEED_DEMON);
        }
        if (!usedAnalyzer && hintsUsed == 0) {
            unlocked.add(Achievement.EAGLE_EYE);
        }
        return unlocked;
    }

    /**
     * Evaluates global achievements that require data across all levels.
     * @param allStars map of levelId to star count for each completed level
     * @param totalLevelCount total number of available levels
     */
    public static List<Achievement> evaluateGlobalAchievements(
            Map<String, Integer> allStars, int totalLevelCount) {
        List<Achievement> unlocked = new ArrayList<>();
        if (totalLevelCount <= 0 || allStars.size() < totalLevelCount) return unlocked;
        boolean allThreeStars = true;
        for (int stars : allStars.values()) {
            if (stars < 3) {
                allThreeStars = false;
                break;
            }
        }
        if (allThreeStars) {
            unlocked.add(Achievement.MASTER_ANALYST);
        }
        return unlocked;
    }
}
