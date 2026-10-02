package com.example.cyberbreach.engine;

public class StarBreakdown {
    public final boolean star1Contained;
    public final boolean star2Precision;
    public final boolean star3Mastery;
    public final int totalStars;
    public final String star1Reason;
    public final String star2Reason;
    public final String star3Reason;

    public StarBreakdown(boolean star1Contained, boolean star2Precision, boolean star3Mastery,
                         String star1Reason, String star2Reason, String star3Reason) {
        this.star1Contained = star1Contained;
        this.star2Precision = star2Precision;
        this.star3Mastery = star3Mastery;
        this.star1Reason = star1Reason;
        this.star2Reason = star2Reason;
        this.star3Reason = star3Reason;
        int count = 0;
        if (star1Contained) count++;
        if (star2Precision) count++;
        if (star3Mastery) count++;
        this.totalStars = count;
    }
}
