package com.example.cyberbreach.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.example.cyberbreach.R;

public class SoundManager {

    private final Context ctx;
    private final SoundPool pool;
    private final int clickId;
    private final int alertId;
    private final int successId;
    private final boolean muted;
    private final boolean haptics;
    private MediaPlayer alarm;

    public SoundManager(Context context, boolean muted, boolean haptics) {
        this.ctx = context.getApplicationContext();
        this.muted = muted;
        this.haptics = haptics;

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        pool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build();
        clickId = pool.load(ctx, R.raw.click, 1);
        alertId = pool.load(ctx, R.raw.alert, 1);
        successId = pool.load(ctx, R.raw.success, 1);

        alarm = MediaPlayer.create(ctx, R.raw.alarm_loop);
        if (alarm != null) {
            alarm.setLooping(true);
            alarm.setVolume(0f, 0f);
        }
    }

    private void play(int soundId) {
        if (!muted) pool.play(soundId, 1f, 1f, 1, 0, 1f);
    }

    public void click() { play(clickId); }

    public void alert() { play(alertId); }

    public void success() { play(successId); }

    /** Starts or adjusts the alarm according to the breach percentage. */
    public void updateAlarm(int breachPercent) {
        if (alarm == null || muted) return;
        if (breachPercent < 50) {
            pauseAlarm();
            return;
        }
        float volume = Math.min(1f, 0.2f + (breachPercent - 50) / 50f);
        alarm.setVolume(volume, volume);
        if (!alarm.isPlaying()) alarm.start();
    }

    public void pauseAlarm() {
        if (alarm != null && alarm.isPlaying()) alarm.pause();
    }

    public void vibrate(long millis) {
        if (!haptics) return;
        Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null || !v.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(millis);
        }
    }

    public void release() {
        pool.release();
        if (alarm != null) {
            alarm.release();
            alarm = null;
        }
    }
}
