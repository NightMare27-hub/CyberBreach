package com.example.cyberbreach;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cyberbreach.data.DbHelper;
import com.example.cyberbreach.databinding.ActivityTutorialBinding;

public class TutorialActivity extends AppCompatActivity {

    private ActivityTutorialBinding binding;
    private int step = 0;
    private LinearLayout maliciousRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTutorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.overlayLayer.setOnClickListener(v -> {
            if (step == 0) advanceStep();
        });
        
        binding.bottomBar.setOnClickListener(v -> handleToolClick());
    }

    private void advanceStep() {
        step++;
        switch (step) {
            case 1:
                binding.overlayLayer.setVisibility(View.GONE);
                addLog("00:01", "192.168.1.10", "HTTP_GET");
                addLog("00:02", "192.168.1.12", "HTTP_GET");
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::advanceStep, 1000);
                break;
            case 2:
                maliciousRow = addLog("00:03", "10.0.0.55", "SYN_FLOOD");
                binding.tvOverlay.setText("Anomalous traffic detected!\n\nStep 1: Identify\nTap the Tool bar below to select your Packet Analyzer.");
                binding.overlayLayer.setVisibility(View.VISIBLE);
                // Allow clicks to pass through to the real UI
                binding.overlayLayer.setClickable(false);
                binding.overlayLayer.setFocusable(false);
                break;
            case 3:
                binding.ivTool.setImageResource(android.R.drawable.ic_menu_search);
                binding.tvTool.setText("Packet Analyzer");
                binding.tvOverlay.setText("Analyzer ready.\nNow tap the SYN_FLOOD log in the list to FLAG the attacker.");
                break;
            case 4:
                if (maliciousRow != null) {
                    maliciousRow.setBackgroundColor(getColor(R.color.soc_warning));
                    for (int i = 0; i < maliciousRow.getChildCount(); i++) {
                        ((TextView) maliciousRow.getChildAt(i)).setTextColor(getColor(R.color.soc_background));
                    }
                }
                binding.tvOverlay.setText("Target Flagged!\n\nStep 2: Mitigate\nTap the Tool bar again to select your Firewall.");
                break;
            case 5:
                binding.ivTool.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                binding.tvTool.setText("Firewall");
                binding.tvOverlay.setText("Firewall ready.\nNow tap the flagged orange log to BLOCK the IP.");
                break;
            case 6:
                if (maliciousRow != null) {
                    maliciousRow.setBackgroundColor(getColor(R.color.soc_primary));
                    for (int i = 0; i < maliciousRow.getChildCount(); i++) {
                        ((TextView) maliciousRow.getChildAt(i)).setTextColor(getColor(R.color.soc_background));
                    }
                }
                binding.tvOverlay.setText("Incident Contained!\n\nYou're ready to deploy.\nTap anywhere to enter the Dashboard.");
                binding.overlayLayer.setClickable(true);
                binding.overlayLayer.setFocusable(true);
                binding.overlayLayer.setOnClickListener(v -> finishTutorial());
                break;
        }
    }

    private void handleToolClick() {
        if (step == 2) {
            advanceStep();
        } else if (step == 4) {
            advanceStep();
        } else {
            Toast.makeText(this, "Follow the instructions on screen!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRowClick(View row, String event) {
        if (step == 3 && event.equals("SYN_FLOOD")) {
            advanceStep();
        } else if (step == 5 && event.equals("SYN_FLOOD")) {
            advanceStep();
        } else {
            Toast.makeText(this, "That's not the right target!", Toast.LENGTH_SHORT).show();
        }
    }

    private LinearLayout addLog(String time, String src, String event) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        
        TypedValue tvAttr = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tvAttr, true);
        row.setBackgroundResource(tvAttr.resourceId);
        
        row.addView(cell(time));
        row.addView(cell(src));
        row.addView(cell(event));
        
        row.setOnClickListener(v -> handleRowClick(row, event));
        
        binding.llLogs.addView(row);
        return row;
    }
    
    private TextView cell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tv.setTextColor(getColor(R.color.soc_primary));
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
        tv.setPadding(10, 8, 10, 8);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        return tv;
    }

    private void finishTutorial() {
        DbHelper db = DbHelper.get(this);
        db.setTutorialCompleted();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
