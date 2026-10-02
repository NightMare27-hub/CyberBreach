package com.example.cyberbreach.model;

public class LogEntry {
    public final String time;
    public final String src;
    public final int port;
    public final String event;
    public final String detail;

    public LogEntry(String time, String src, int port, String event, String detail) {
        this.time = time;
        this.src = src;
        this.port = port;
        this.event = event;
        this.detail = detail;
    }
}
