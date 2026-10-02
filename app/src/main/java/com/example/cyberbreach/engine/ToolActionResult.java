package com.example.cyberbreach.engine;

import com.example.cyberbreach.game.GameEngine;

public class ToolActionResult {
    public final GameEngine.Outcome outcome;
    public final String message;
    public final int penaltyPoints;

    public ToolActionResult(GameEngine.Outcome outcome, String message, int penaltyPoints) {
        this.outcome = outcome;
        this.message = message;
        this.penaltyPoints = penaltyPoints;
    }
}
