package com.example.cyberbreach.notify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public final class ReminderScheduler {
    private ReminderScheduler() { }

    private static PendingIntent pending(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        return PendingIntent.getBroadcast(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void schedule(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Calendar first = Calendar.getInstance();
        first.set(Calendar.HOUR_OF_DAY, 18);
        first.set(Calendar.MINUTE, 0);
        first.set(Calendar.SECOND, 0);
        if (first.before(Calendar.getInstance())) {
            first.add(Calendar.DAY_OF_YEAR, 1);
        }
        am.setInexactRepeating(AlarmManager.RTC, first.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pending(context));
    }

    public static void cancel(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(pending(context));
    }
}
