package com.example.cyberbreach.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.cyberbreach.data.PrefsManager;

/**
 * Re-registers the daily reminder alarm after a device reboot,
 * since AlarmManager alarms are cleared by the system on restart.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            PrefsManager prefs = new PrefsManager(context);
            if (prefs.isReminderEnabled()) {
                ReminderScheduler.schedule(context);
            }
        }
    }
}
