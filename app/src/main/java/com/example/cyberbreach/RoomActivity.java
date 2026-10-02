package com.example.cyberbreach;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cyberbreach.audio.SoundManager;
import com.example.cyberbreach.data.DbHelper;
import com.example.cyberbreach.data.PrefsManager;
import com.example.cyberbreach.databinding.ActivityRoomBinding;
import com.example.cyberbreach.game.GameEngine;
import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LevelStore;
import com.example.cyberbreach.model.LogEntry;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoomActivity extends AppCompatActivity {

    private static final long FEED_DELAY_MS = 1800;

    private ActivityRoomBinding binding;
    private Level level;
    private GameEngine engine;
    private SoundManager sound;
    private CountDownTimer timer;

    private final Handler feedHandler = new Handler(Looper.getMainLooper());
    private final List<TableRow> rows = new ArrayList<>();
    private int revealed = 0;
    private boolean finished = false;
    private GameEngine.Tool selectedTool = GameEngine.Tool.PACKET_ANALYZER;

    private final int[] toolIcons = {
            android.R.drawable.ic_menu_search,
            android.R.drawable.ic_menu_close_clear_cancel,
            android.R.drawable.ic_menu_sort_by_size,
            android.R.drawable.ic_menu_manage
    };

    private final Runnable feedRunnable = new Runnable() {
        @Override
        public void run() {
            if (finished) return;
            if (revealed < level.logs.size()) {
                addRow(level.logs.get(revealed++));
                binding.scrollTerminal.post(() -> binding.scrollTerminal.fullScroll(View.FOCUS_DOWN));
                feedHandler.postDelayed(this, FEED_DELAY_MS);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        level = LevelStore.findById(getIntent().getStringExtra(IntentKeys.LEVEL_ID));
        if (level == null) {
            finish();
            return;
        }
        GameEngine.Mode mode = "LEARN".equals(getIntent().getStringExtra(IntentKeys.MODE))
                ? GameEngine.Mode.LEARN : GameEngine.Mode.CHALLENGE;

        PrefsManager prefs = new PrefsManager(this);
        DbHelper db = DbHelper.get(this);
        engine = new GameEngine(level, mode,
                db.getToolTier("firewall"), db.getToolTier("log_filter"));
        sound = new SoundManager(this, prefs.isMuted(), prefs.isHapticsEnabled());

        binding.tvRoomTitle.setText(level.title + "  |  " + level.org);
        binding.tableAlerts.addView(headerRow());
        for (int i = 0; i < Math.min(3, level.logs.size()); i++) {
            addRow(level.logs.get(revealed++));
        }

        setupToolSpinner();
        setupHintButton();
        setupBackHandling();
        updateHud();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (level != null && !finished) {
            startTimer();
            feedHandler.postDelayed(feedRunnable, FEED_DELAY_MS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLoops();
        if (sound != null) sound.pauseAlarm();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLoops();
        if (sound != null) sound.release();
    }

    // ---------- timer and feed ----------

    private void startTimer() {
        long duration = engine.getMode() == GameEngine.Mode.CHALLENGE
                ? engine.getRemainingSec() * 1000L
                : 24L * 3600L * 1000L;

        timer = new CountDownTimer(duration, 1000) {
            boolean first = true;

            @Override
            public void onTick(long millisUntilFinished) {
                if (first) {
                    first = false;
                } else {
                    engine.tick();
                }
                updateHud();
                if (engine.isBreached()) onGameOver();
            }

            @Override
            public void onFinish() {
                if (engine.getMode() == GameEngine.Mode.CHALLENGE) {
                    engine.tick();
                    updateHud();
                    onGameOver();
                }
            }
        }.start();
    }

    private void stopLoops() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        feedHandler.removeCallbacks(feedRunnable);
    }

    private void updateHud() {
        int secs = engine.getDisplaySeconds();
        int breach = engine.getBreachPercent();
        binding.tvTimer.setText(String.format(Locale.US, "%02d:%02d", secs / 60, secs % 60));
        binding.tvTimer.setTextColor(getColor(breach >= 70 ? R.color.terminal_red : R.color.terminal_green));
        binding.tvBreach.setText(getString(R.string.hud_breach) + " " + breach + "%");
        binding.progressBreach.setProgress(breach);
        if (engine.getMode() == GameEngine.Mode.CHALLENGE && !finished) {
            sound.updateAlarm(breach);
        }
    }

    // ---------- UI setup ----------

    private void setupToolSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tool_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTool.setAdapter(adapter);
        binding.spTool.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTool = GameEngine.Tool.values()[position];
                binding.ivTool.setImageResource(toolIcons[position]);
                sound.click();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupHintButton() {
        binding.btnHint.setOnClickListener(v -> {
            if (finished) return;
            if (engine.getMode() == GameEngine.Mode.CHALLENGE) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.dialog_hint_title)
                        .setMessage(getString(R.string.dialog_hint_message, engine.getHintCostSec()))
                        .setPositiveButton(R.string.dialog_use_hint, (d, w) -> revealHint())
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show();
            } else {
                revealHint();
            }
        });
    }

    private void revealHint() {
        String hint = engine.useHint();
        if (hint == null) {
            Toast.makeText(this, R.string.toast_no_hints, Toast.LENGTH_SHORT).show();
            return;
        }
        binding.tvStatus.setText("HINT: " + hint);
        Toast.makeText(this, hint, Toast.LENGTH_LONG).show();
        updateHud();
    }

    private void setupBackHandling() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (finished) {
                    finish();
                    return;
                }
                new MaterialAlertDialogBuilder(RoomActivity.this)
                        .setTitle(R.string.dialog_exit_title)
                        .setMessage(R.string.dialog_exit_message)
                        .setPositiveButton(R.string.dialog_leave, (d, w) -> finish())
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show();
            }
        });
    }

    // ---------- terminal ----------

    private TableRow headerRow() {
        TableRow row = new TableRow(this);
        row.addView(cell("TIME", R.color.terminal_dim, true));
        row.addView(cell("SOURCE", R.color.terminal_dim, true));
        row.addView(cell("PORT", R.color.terminal_dim, true));
        row.addView(cell("EVENT", R.color.terminal_dim, true));
        return row;
    }

    private void addRow(LogEntry e) {
        TableRow row = new TableRow(this);
        row.setTag(e);
        row.setClickable(true);

        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tv, true);
        row.setBackgroundResource(tv.resourceId);

        row.addView(cell(e.time, R.color.terminal_green, false));
        row.addView(cell(e.src, R.color.terminal_green, false));
        row.addView(cell(String.valueOf(e.port), R.color.terminal_green, false));
        row.addView(cell(e.event, R.color.terminal_green, false));

        row.setOnClickListener(v -> onRowTapped(e));
        row.setOnLongClickListener(v -> {
            showPacketDialog(e);
            return true;
        });

        rows.add(row);
        binding.tableAlerts.addView(row);
    }

    private TextView cell(String text, int colorRes, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTypeface(Typeface.MONOSPACE, bold ? Typeface.BOLD : Typeface.NORMAL);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setTextColor(getColor(colorRes));
        tv.setPadding(10, 8, 10, 8);
        return tv;
    }

    private void highlightSource(String src) {
        for (TableRow row : rows) {
            LogEntry e = (LogEntry) row.getTag();
            int color = e.src.equals(src) ? R.color.terminal_amber : R.color.terminal_green;
            for (int i = 0; i < row.getChildCount(); i++) {
                ((TextView) row.getChildAt(i)).setTextColor(getColor(color));
            }
        }
    }

    // ---------- event handling ----------

    private void onRowTapped(LogEntry entry) {
        if (finished) return;
        sound.click();

        GameEngine.ActionResult result = engine.apply(selectedTool, entry);
        if (selectedTool == GameEngine.Tool.LOG_FILTER) highlightSource(entry.src);
        binding.tvStatus.setText(result.message);

        switch (result.outcome) {
            case INFO:
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                break;
            case WRONG:
                sound.alert();
                sound.vibrate(150);
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                updateHud();
                if (engine.isBreached()) onGameOver();
                break;
            case SOLVED:
                onSolved();
                break;
        }
    }

    private void showPacketDialog(LogEntry e) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(e.event)
                .setMessage("Time: " + e.time + "\nSource: " + e.src + "\nPort: " + e.port + "\n\n" + e.detail)
                .setPositiveButton(R.string.btn_close, null)
                .show();
    }

    private void openResult(boolean solved) {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra(IntentKeys.LEVEL_ID, level.id);
        i.putExtra(IntentKeys.MODE, engine.getMode().name());
        i.putExtra(IntentKeys.SOLVED, solved);
        i.putExtra(IntentKeys.STARS, engine.getStars());
        i.putExtra(IntentKeys.TIME_SEC, engine.getElapsedSeconds());
        i.putExtra(IntentKeys.HINTS, engine.getHintsUsed());
        i.putExtra(IntentKeys.WRONG, engine.getWrongActions());
        startActivity(i);
        finish();
    }

    private void onSolved() {
        finished = true;
        stopLoops();
        sound.pauseAlarm();
        sound.success();
        sound.vibrate(80);
        binding.tvStatus.setText(R.string.dialog_solved_title);
        feedHandler.postDelayed(() -> openResult(true), 900);   // let the success sound play
    }

    private void onGameOver() {
        if (finished) return;
        finished = true;
        stopLoops();
        sound.pauseAlarm();
        sound.alert();
        sound.vibrate(400);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_gameover_title)
                .setMessage(R.string.dialog_gameover_short)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_retry, (d, w) -> recreate())
                .setNegativeButton(R.string.btn_debrief, (d, w) -> openResult(false))
                .show();
    }
}
