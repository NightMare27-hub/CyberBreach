package com.example.cyberbreach.export;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LogEntry;

public final class ReportRenderer {
    private ReportRenderer() { }

    public static final int WIDTH = 595;
    public static final int HEIGHT = 842;

    public static class Data {
        public Level level;
        public String mode;
        public String rank;
        public String date;
        public int stars;
        public int timeSec;
        public int hints;
        public int wrong;
    }

    public static void draw(Canvas c, Data d) {
        c.drawColor(Color.WHITE);

        Paint band = new Paint();
        band.setColor(0xFF0B1220);
        c.drawRect(0, 0, WIDTH, 110, band);
        c.drawText("INCIDENT RESPONSE REPORT", 36, 56, paint(0xFF3DDC84, 22, true, false));
        c.drawText("Cyber Breach  |  Security Operations Training", 36, 84,
                paint(0xFF9FB0C8, 11, false, false));

        int y = 146;
        y = field(c, "Incident", d.level.title, y);
        y = field(c, "Organization", d.level.org, y);
        y = field(c, "Track", d.level.track, y);
        y = field(c, "Attack type", d.level.vulnType.replace('_', ' '), y);
        y = field(c, "Date", d.date, y);
        y = field(c, "Analyst rank", d.rank, y);

        y = section(c, "ATTACK TIMELINE", y + 10);
        TextPaint mono = paint(0xFF222222, 9, false, true);
        int shown = Math.min(7, d.level.logs.size());
        for (int i = 0; i < shown; i++) {
            LogEntry e = d.level.logs.get(i);
            c.drawText(e.time + "  " + e.src + ":" + e.port + "  " + e.event, 40, y, mono);
            y += 15;
        }

        y = section(c, "ACTIONS TAKEN", y + 14);
        String action;
        switch (d.level.fix) {
            case "block_ip":
                action = "Blocked source address " + d.level.target + " at the firewall.";
                break;
            case "close_port":
                action = "Closed port " + d.level.target + " at the firewall.";
                break;
            default:
                action = "Applied the missing security patch.";
                break;
        }
        c.drawText(action, 40, y, paint(0xFF222222, 11, false, false));
        y += 20;
        c.drawText("Mode: " + d.mode + "   Time: " + (d.timeSec / 60) + "m " + (d.timeSec % 60) + "s"
                + "   Hints: " + d.hints + "   Mistakes: " + d.wrong, 40, y,
                paint(0xFF222222, 11, false, false));
        y += 20;
        c.drawText("Performance: " + d.stars + " of 3 stars", 40, y, paint(0xFF1F3A5F, 12, true, false));

        y = section(c, "LESSONS LEARNED", y + 24);
        TextPaint body = new TextPaint(paint(0xFF222222, 11, false, false));
        StaticLayout layout = StaticLayout.Builder
                .obtain(d.level.debrief, 0, d.level.debrief.length(), body, WIDTH - 80)
                .setLineSpacing(3f, 1f)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .build();
        c.save();
        c.translate(40, y - 10);
        layout.draw(c);
        c.restore();

        c.drawText("All traffic, addresses and systems in this report are simulated for education.",
                36, HEIGHT - 30, paint(0xFF777777, 8, false, false));
    }

    private static int field(Canvas c, String label, String value, int y) {
        c.drawText(label, 40, y, paint(0xFF777777, 11, false, false));
        c.drawText(value, 160, y, paint(0xFF111111, 12, true, false));
        return y + 22;
    }

    private static int section(Canvas c, String title, int y) {
        Paint line = new Paint();
        line.setColor(0xFF1F3A5F);
        c.drawText(title, 40, y, paint(0xFF1F3A5F, 11, true, false));
        c.drawLine(40, y + 6, WIDTH - 40, y + 6, line);
        return y + 26;
    }

    private static TextPaint paint(int color, float sizePt, boolean bold, boolean mono) {
        TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTextSize(sizePt);
        Typeface base = mono ? Typeface.MONOSPACE : Typeface.SANS_SERIF;
        p.setTypeface(Typeface.create(base, bold ? Typeface.BOLD : Typeface.NORMAL));
        return p;
    }
}
