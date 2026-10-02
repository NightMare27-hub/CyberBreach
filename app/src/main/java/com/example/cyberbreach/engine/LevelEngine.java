package com.example.cyberbreach.engine;

import com.example.cyberbreach.model.Level;

import java.util.ArrayList;
import java.util.List;

public class LevelEngine {

    public static StarBreakdown evaluatePerformance(boolean solved, int wrongActions, int hintsUsed, int remainingSec, int totalLimitSec) {
        if (!solved) {
            return new StarBreakdown(
                    false, false, false,
                    "Breach not contained",
                    "Breach not contained",
                    "Breach not contained"
            );
        }

        boolean star1 = true;
        String reason1 = "Incident successfully contained!";

        boolean star2 = wrongActions < 2;
        String reason2 = star2 ? "High precision: fewer than 2 mistakes!" : "Too many mistakes made (2 or more)";

        boolean star3 = (hintsUsed == 0) && (remainingSec * 4 >= totalLimitSec);
        String reason3 = star3 ? "Mastery achieved: 0 hints under target time!" : "Mastery missed: used hints or ran low on time";

        return new StarBreakdown(star1, star2, star3, reason1, reason2, reason3);
    }

    public static MasteryStatus getMasteryStatus(boolean unlocked, boolean solved, int stars, int hintsUsed, int wrongActions) {
        if (!unlocked) return MasteryStatus.LOCKED;
        if (!solved) return MasteryStatus.UNLOCKED;
        if (stars == 3 && hintsUsed == 0 && wrongActions == 0) {
            return MasteryStatus.MASTERED;
        }
        return MasteryStatus.COMPLETED;
    }

    public static List<Level> filterByConcept(List<Level> levels, String concept) {
        List<Level> result = new ArrayList<>();
        if (levels == null || concept == null) return result;
        for (Level l : levels) {
            if (concept.equalsIgnoreCase(l.concept)) {
                result.add(l);
            }
        }
        return result;
    }

    public static List<Level> filterByTrack(List<Level> levels, String track) {
        List<Level> result = new ArrayList<>();
        if (levels == null || track == null) return result;
        for (Level l : levels) {
            if (track.equalsIgnoreCase(l.track)) {
                result.add(l);
            }
        }
        return result;
    }
}
