package com.example.cyberbreach.game;

import com.example.cyberbreach.engine.ToolEngine;
import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LogEntry;

public class GameEngine {

    public enum Mode { LEARN, CHALLENGE }

    public enum Tool { PACKET_ANALYZER, FIREWALL, LOG_FILTER, PATCH_MANAGER }

    public enum Outcome { INFO, WRONG, SOLVED }

    public static class ActionResult {
        public final Outcome outcome;
        public final String message;

        ActionResult(Outcome outcome, String message) {
            this.outcome = outcome;
            this.message = message;
        }
    }

    private final Level level;
    private final Mode mode;
    private final int penaltyPerMistake;
    private final int hintCostSec;

    private int remainingSec;
    private int elapsedSec;
    private int hintPenaltySec;
    private int penaltyPoints;
    private int hintsUsed;
    private int wrongActions;
    private boolean solved;

    public GameEngine(Level level, Mode mode, int firewallTier, int filterTier) {
        this.level = level;
        this.mode = mode;
        this.remainingSec = level.timeLimitSec;
        this.penaltyPerMistake = ToolEngine.calculateFirewallPenalty(firewallTier);
        this.hintCostSec = ToolEngine.calculateHintCost(filterTier);
    }

    /** Called once per second by the UI timer. */
    public void tick() {
        if (solved) return;
        elapsedSec++;
        if (mode == Mode.CHALLENGE && remainingSec > 0) {
            remainingSec--;
        }
    }

    public ActionResult apply(Tool tool, LogEntry e) {
        if (solved) {
            return new ActionResult(Outcome.INFO, "Incident already contained.");
        }
        switch (tool) {
            case PACKET_ANALYZER:
                return new ActionResult(Outcome.INFO, e.src + ":" + e.port + " - " + e.detail);
            case LOG_FILTER:
                return new ActionResult(Outcome.INFO, "Filter applied: traffic from " + e.src);
            case FIREWALL:
                if (matches("block_ip", e.src) || matches("close_port", String.valueOf(e.port))) {
                    return markSolved();
                }
                return mistake("Firewall rule hit legitimate traffic!");
            case PATCH_MANAGER:
                if (matches("patch", e.event)) {
                    return markSolved();
                }
                return mistake("That service did not need patching.");
            default:
                return new ActionResult(Outcome.INFO, "");
        }
    }

    private boolean matches(String fixType, String value) {
        return level.fix.equals(fixType) && level.target.equals(value);
    }

    private ActionResult markSolved() {
        solved = true;
        return new ActionResult(Outcome.SOLVED, "Incident contained.");
    }

    private ActionResult mistake(String message) {
        wrongActions++;
        if (mode == Mode.CHALLENGE) {
            penaltyPoints += penaltyPerMistake;
            return new ActionResult(Outcome.WRONG, message + " (+" + penaltyPerMistake + "% breach)");
        }
        return new ActionResult(Outcome.WRONG, message + " Try again.");
    }

    /** Returns the next hint, or null when none are left. */
    public String useHint() {
        if (hintsUsed >= level.hints.size()) return null;
        String hint = level.hints.get(hintsUsed++);
        if (mode == Mode.CHALLENGE) {
            remainingSec = Math.max(0, remainingSec - hintCostSec);
            hintPenaltySec += hintCostSec;
        }
        return hint;
    }

    public int getBreachPercent() {
        if (mode == Mode.LEARN) return 0;
        int used = level.timeLimitSec - remainingSec;
        int base = (int) Math.round(100.0 * used / level.timeLimitSec);
        return Math.min(100, base + penaltyPoints);
    }

    public boolean isBreached() {
        return mode == Mode.CHALLENGE && !solved && getBreachPercent() >= 100;
    }

    public int getStars() {
        if (!solved || mode == Mode.LEARN) return 0;
        int stars = 3;
        if (wrongActions >= 2) stars--;
        if (hintsUsed >= 2) stars--;
        if (remainingSec * 4 < level.timeLimitSec) stars--;
        return Math.max(1, stars);
    }

    public Mode getMode() { return mode; }
    public boolean isSolved() { return solved; }
    public int getRemainingSec() { return remainingSec; }
    public int getHintsUsed() { return hintsUsed; }
    public int getWrongActions() { return wrongActions; }
    public int getHintCostSec() { return hintCostSec; }

    /** Challenge shows time left; Learn shows time spent. */
    public int getDisplaySeconds() {
        return mode == Mode.CHALLENGE ? remainingSec : elapsedSec;
    }

    /** Time spent including hint penalties. Used for best-time records. */
    public int getElapsedSeconds() {
        return elapsedSec + hintPenaltySec;
    }
}
