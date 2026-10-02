package com.example.cyberbreach.model;

import java.util.List;

public class Level {

    public enum Difficulty { EASY, MEDIUM, HARD, EXPERT }

    public final String id;
    public final String title;
    public final String org;
    public final String track;
    public final String concept;      // name of the briefing page in assets
    public final int timeLimitSec;
    public final String vulnType;     // e.g. brute_force
    public final String fix;          // block_ip | close_port | patch
    public final String target;       // the value the correct fix must match
    public final String debrief;
    public final List<String> hints;
    public final List<LogEntry> logs;
    public final Difficulty difficulty;

    public Level(String id, String title, String org, String track, String concept,
                 int timeLimitSec, String vulnType, String fix, String target,
                 String debrief, List<String> hints, List<LogEntry> logs) {
        this(id, title, org, track, concept, timeLimitSec, vulnType, fix, target, debrief, hints, logs, Difficulty.MEDIUM);
    }

    public Level(String id, String title, String org, String track, String concept,
                 int timeLimitSec, String vulnType, String fix, String target,
                 String debrief, List<String> hints, List<LogEntry> logs, Difficulty difficulty) {
        this.id = id;
        this.title = title;
        this.org = org;
        this.track = track;
        this.concept = concept;
        this.timeLimitSec = timeLimitSec;
        this.vulnType = vulnType;
        this.fix = fix;
        this.target = target;
        this.debrief = debrief;
        this.hints = hints;
        this.logs = logs;
        this.difficulty = difficulty != null ? difficulty : Difficulty.MEDIUM;
    }
}
