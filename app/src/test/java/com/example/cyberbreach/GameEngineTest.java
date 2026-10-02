package com.example.cyberbreach;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.cyberbreach.game.GameEngine;
import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LogEntry;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class GameEngineTest {

    private static final LogEntry ATTACKER =
            new LogEntry("02:14:01", "203.0.113.45", 22, "LOGIN_FAIL", "wrong password");
    private static final LogEntry INNOCENT =
            new LogEntry("02:14:02", "198.51.100.7", 443, "HTTPS_OK", "normal traffic");

    private Level level() {
        return new Level("t1", "Test", "Org", "Track", "ports", 100,
                "brute_force", "block_ip", "203.0.113.45", "debrief",
                Arrays.asList("h1", "h2", "h3"), Collections.singletonList(ATTACKER));
    }

    @Test
    public void wrongFirewallActionAddsPenalty() {
        GameEngine e = new GameEngine(level(), GameEngine.Mode.CHALLENGE, 1, 1, 1);
        GameEngine.ActionResult r = e.apply(GameEngine.Tool.FIREWALL, INNOCENT);
        assertEquals(GameEngine.Outcome.WRONG, r.outcome);
        assertEquals(15, e.getBreachPercent());
        assertEquals(1, e.getWrongActions());
    }

    @Test
    public void correctFirewallActionSolvesWithThreeStars() {
        GameEngine e = new GameEngine(level(), GameEngine.Mode.CHALLENGE, 1, 1, 1);
        GameEngine.ActionResult r = e.apply(GameEngine.Tool.FIREWALL, ATTACKER);
        assertEquals(GameEngine.Outcome.SOLVED, r.outcome);
        assertTrue(e.isSolved());
        assertEquals(3, e.getStars());
    }

    @Test
    public void repeatedMistakesCauseBreach() {
        GameEngine e = new GameEngine(level(), GameEngine.Mode.CHALLENGE, 1, 1, 1);
        for (int i = 0; i < 7; i++) {
            e.apply(GameEngine.Tool.FIREWALL, INNOCENT);   // 7 x 15 = 105
        }
        assertEquals(100, e.getBreachPercent());
        assertTrue(e.isBreached());
    }

    @Test
    public void learnModeHasNoBreachAndNoStars() {
        GameEngine e = new GameEngine(level(), GameEngine.Mode.LEARN, 1, 1, 1);
        e.apply(GameEngine.Tool.FIREWALL, INNOCENT);
        assertEquals(0, e.getBreachPercent());
        assertFalse(e.isBreached());
        e.apply(GameEngine.Tool.FIREWALL, ATTACKER);
        assertEquals(0, e.getStars());
    }

    @Test
    public void hintCostsTimeInChallengeMode() {
        GameEngine e = new GameEngine(level(), GameEngine.Mode.CHALLENGE, 1, 1, 1);
        assertEquals("h1", e.useHint());
        assertEquals(90, e.getRemainingSec());
        assertEquals(10, e.getElapsedSeconds());
        assertEquals(1, e.getHintsUsed());
    }

    @Test
    public void hintsRunOut() {
        GameEngine e = new GameEngine(level(), GameEngine.Mode.LEARN, 1, 1, 1);
        e.useHint();
        e.useHint();
        e.useHint();
        assertNull(e.useHint());
    }

    @Test
    public void upgradesReducePenaltyAndHintCost() {
        GameEngine e = new GameEngine(level(), GameEngine.Mode.CHALLENGE, 3, 3, 1);
        e.apply(GameEngine.Tool.FIREWALL, INNOCENT);
        assertEquals(9, e.getBreachPercent());
        assertEquals(6, e.getHintCostSec());
    }

    @Test
    public void tickAdvancesBreachMeter() {
        GameEngine e = new GameEngine(level(), GameEngine.Mode.CHALLENGE, 1, 1, 1);
        for (int i = 0; i < 50; i++) e.tick();
        assertEquals(50, e.getBreachPercent());
        assertEquals(50, e.getRemainingSec());
    }

    @Test
    public void manyHintsAndMistakesLowerStars() {
        GameEngine e = new GameEngine(level(), GameEngine.Mode.CHALLENGE, 1, 1, 1);
        e.apply(GameEngine.Tool.FIREWALL, INNOCENT);
        e.apply(GameEngine.Tool.FIREWALL, INNOCENT);
        e.useHint();
        e.useHint();
        e.apply(GameEngine.Tool.FIREWALL, ATTACKER);
        assertEquals(1, e.getStars());   // 3 minus mistakes minus hints, never below 1
    }
}
