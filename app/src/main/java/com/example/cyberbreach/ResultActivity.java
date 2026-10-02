package com.example.cyberbreach;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cyberbreach.data.DbHelper;
import com.example.cyberbreach.data.PrefsManager;
import com.example.cyberbreach.databinding.ActivityResultBinding;
import com.example.cyberbreach.engine.LevelEngine;
import com.example.cyberbreach.engine.StarBreakdown;
import com.example.cyberbreach.engine.StatsEngine;
import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LevelStore;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private Level level;
    private boolean solved;
    private int stars;
    private int timeSec;
    private int hints;
    private int wrong;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultBinding binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent in = getIntent();
        level = LevelStore.findById(in.getStringExtra(IntentKeys.LEVEL_ID));
        if (level == null) {
            finish();
            return;
        }
        solved = in.getBooleanExtra(IntentKeys.SOLVED, false);
        stars = in.getIntExtra(IntentKeys.STARS, 0);
        timeSec = in.getIntExtra(IntentKeys.TIME_SEC, 0);
        hints = in.getIntExtra(IntentKeys.HINTS, 0);
        wrong = in.getIntExtra(IntentKeys.WRONG, 0);
        mode = in.getStringExtra(IntentKeys.MODE);
        if (mode == null) mode = "CHALLENGE";

        StarBreakdown breakdown = LevelEngine.evaluatePerformance(solved, wrong, hints, level.timeLimitSec - timeSec, level.timeLimitSec);

        binding.tvHeadline.setText(solved ? R.string.result_contained : R.string.result_breached);
        binding.ratingStars.setRating(breakdown.totalStars);
        binding.tvTime.setText(String.format(Locale.US, "%02d:%02d", timeSec / 60, timeSec % 60));
        binding.tvHints.setText(String.valueOf(hints));
        binding.tvWrong.setText(String.valueOf(wrong));
        binding.tvMode.setText("LEARN".equals(mode) ? "Learn" : "Challenge");
        binding.tvDebrief.setText(level.debrief);

        binding.btnRetry.setOnClickListener(v -> {
            Intent i = new Intent(this, RoomActivity.class);
            i.putExtra(IntentKeys.LEVEL_ID, level.id);
            i.putExtra(IntentKeys.MODE, mode);
            startActivity(i);
            finish();
        });

        final String nextId = LevelStore.nextId(level.id);
        if (!solved || nextId == null) {
            binding.btnNext.setVisibility(View.GONE);
        } else {
            binding.btnNext.setOnClickListener(v -> {
                Intent i = new Intent(this, BriefingActivity.class);
                i.putExtra(IntentKeys.LEVEL_ID, nextId);
                i.putExtra(IntentKeys.MODE, mode);
                startActivity(i);
                finish();
            });
        }

        binding.btnShare.setOnClickListener(v -> shareResult());

        if (solved) {
            binding.btnReport.setVisibility(View.VISIBLE);
            binding.btnReport.setOnClickListener(v -> {
                Intent i = new Intent(this, ReportActivity.class);
                i.putExtra(IntentKeys.LEVEL_ID, level.id);
                i.putExtra(IntentKeys.MODE, mode);
                i.putExtra(IntentKeys.STARS, breakdown.totalStars);
                i.putExtra(IntentKeys.TIME_SEC, timeSec);
                i.putExtra(IntentKeys.HINTS, hints);
                i.putExtra(IntentKeys.WRONG, wrong);
                startActivity(i);
            });
        }

        if (savedInstanceState == null) {
            saveResult(breakdown.totalStars);
        }
    }

    /** Runs once per result. These writes are tiny, so the main thread is acceptable here. */
    private void saveResult(int evaluatedStars) {
        DbHelper db = DbHelper.get(this);
        db.insertAttempt(level.id, mode, timeSec, hints, solved);

        if (!solved) return;

        String nextId = LevelStore.nextId(level.id);
        if (nextId != null) {
            db.unlock(nextId);
        }
        if ("CHALLENGE".equals(mode)) {
            db.recordBest(level.id, timeSec, evaluatedStars);
            int streak = new PrefsManager(this).getStreak();
            int awardedCredits = StatsEngine.calculateAwardedCredits(evaluatedStars, streak);
            db.addCredits(awardedCredits);
        }
        new PrefsManager(this).updateStreak();
    }

    /** Implicit intent: any app that accepts plain text can receive the summary. */
    private void shareResult() {
        String text = getString(R.string.share_text, level.title, stars, timeSec);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(send, getString(R.string.share_chooser)));
    }
}
