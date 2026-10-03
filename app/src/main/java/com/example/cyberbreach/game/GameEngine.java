package com.example.cyberbreach.game;

import com.example.cyberbreach.engine.ToolEngine;
import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LogEntry;

public class GameEngine {

    public enum Mode { LEARN, CHALLENGE }

    public enum Tool { PACKET_ANALYZER, FIREWALL, LOG_FILTER, PATCH_MANAGER, ISOLATION_FRAMEWORK, EMAIL_GATEWAY, CRYPTO_MANAGER }

    public enum Outcome { INFO, WRONG, FLAGGED, SOLVED }

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
    private final int analyzerTier;

    private int remainingSec;
    private int elapsedSec;
    private int hintPenaltySec;
    private int penaltyPoints;
    private int hintsUsed;
    private int wrongActions;
    private boolean threatFlagged;
    private boolean solved;

    public GameEngine(Level level, Mode mode, int firewallTier, int filterTier, int analyzerTier) {
        this.level = level;
        this.mode = mode;
        this.remainingSec = level.timeLimitSec;
        this.penaltyPerMistake = ToolEngine.calculateFirewallPenalty(firewallTier);
        this.hintCostSec = ToolEngine.calculateHintCost(filterTier);
        this.analyzerTier = analyzerTier;
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
        
        // Stage 1: Identify
        if (tool == Tool.PACKET_ANALYZER) {
            boolean isMalicious = matches("block_ip", e.src) || matches("isolate_host", e.src) 
                || matches("close_port", String.valueOf(e.port)) || matches("patch", e.event) 
                || matches("quarantine_email", e.event) || matches("enable_encryption", e.event) 
                || matches("enable_encryption", String.valueOf(e.port));
                
            if (isMalicious) {
                threatFlagged = true;
                return new ActionResult(Outcome.FLAGGED, "Threat Flagged. Awaiting Mitigation Strategy...");
            } else {
                return mistake("Legitimate traffic flagged as malicious.");
            }
        }
        
        if (tool == Tool.LOG_FILTER) {
            return new ActionResult(Outcome.INFO, "Filter applied: traffic from " + e.src);
        }

        // Stage 2: Mitigate
        if (!threatFlagged) {
            return mistake("RECKLESS ACTION: You must identify and flag the threat with the Packet Analyzer first!");
        }

        switch (tool) {
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
            case ISOLATION_FRAMEWORK:
                if (matches("isolate_host", e.src)) {
                    return markSolved();
                }
                return mistake("Wrong host isolated.");
            case EMAIL_GATEWAY:
                if (matches("quarantine_email", e.event)) {
                    return markSolved();
                }
                return mistake("Legitimate traffic quarantined.");
            case CRYPTO_MANAGER:
                if (matches("enable_encryption", e.event) || matches("enable_encryption", String.valueOf(e.port))) {
                    return markSolved();
                }
                return mistake("Encryption applied to wrong target.");
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

    /**
     * @deprecated Use {@link com.example.cyberbreach.engine.LevelEngine#evaluatePerformance} instead.
     * Kept temporarily for backward compatibility with intent extras.
     */
    @Deprecated
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
