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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTutorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.overlayLayer.setOnClickListener(v -> advanceStep());
        binding.bottomBar.setOnClickListener(v -> handleToolClick());
    }

    private void advanceStep() {
        step++;
        switch (step) {
            case 1:
                binding.overlayLayer.setVisibility(View.GONE);
                addLog("00:01", "192.168.1.10", "HTTP_GET");
                addLog("00:02", "192.168.1.12", "HTTP_GET");
                binding.overlayLayer.postDelayed(this::advanceStep, 1000);
                break;
            case 2:
                addLog("00:03", "10.0.0.55", "SYN_FLOOD");
                binding.tvOverlay.setText("Anomalous traffic detected!\n\nStep 1: Identify\nTap the Tool bar below to select your Packet Analyzer.");
                binding.overlayLayer.setVisibility(View.VISIBLE);
                break;
            case 3:
                // Expected user to tap tool bar
                break;
            case 4:
                // User tapped malicious row with Analyzer
                binding.tvOverlay.setText("Target Flagged!\n\nStep 2: Mitigate\nTap the Tool bar again to select your Firewall.");
                binding.overlayLayer.setVisibility(View.VISIBLE);
                break;
            case 5:
                // Expected user to tap tool bar
                break;
            case 6:
                // User tapped malicious row with Firewall
                binding.tvOverlay.setText("Incident Contained!\n\nYou're ready to deploy.\nTap to enter the Dashboard.");
                binding.overlayLayer.setVisibility(View.VISIBLE);
                break;
            case 7:
                finishTutorial();
                break;
        }
    }

    private void handleToolClick() {
        if (step == 2) {
            binding.ivTool.setImageResource(android.R.drawable.ic_menu_search);
            binding.tvTool.setText("Packet Analyzer");
            binding.tvOverlay.setText("Analyzer ready.\nNow tap the SYN_FLOOD log in the list to FLAG the attacker.");
            step = 3;
        } else if (step == 4) {
            binding.ivTool.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            binding.tvTool.setText("Firewall");
            binding.tvOverlay.setText("Firewall ready.\nNow tap the flagged orange log to BLOCK the IP.");
            step = 5;
        } else {
            Toast.makeText(this, "Follow the instructions on screen!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRowClick(View row, String event) {
        if (step == 3 && event.equals("SYN_FLOOD")) {
            row.setBackgroundColor(getColor(R.color.soc_warning));
            
            // set text color to background for all child views
            if (row instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) row;
                for (int i=0; i<ll.getChildCount(); i++) {
                    ((TextView)ll.getChildAt(i)).setTextColor(getColor(R.color.soc_background));
                }
            }
            
            step = 3; 
            advanceStep();
        } else if (step == 5 && event.equals("SYN_FLOOD")) {
            // Restore styling to indicate success/blocked
            row.setBackgroundColor(getColor(R.color.soc_primary)); 
            if (row instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) row;
                for (int i=0; i<ll.getChildCount(); i++) {
                    ((TextView)ll.getChildAt(i)).setTextColor(getColor(R.color.soc_background));
                }
            }
            step = 5; 
            advanceStep();
        } else {
            Toast.makeText(this, "That's not the right target!", Toast.LENGTH_SHORT).show();
        }
    }

    private void addLog(String time, String src, String event) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        
        TypedValue tvAttr = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tvAttr, true);
        row.setBackgroundResource(tvAttr.resourceId);
        
        TextView t1 = cell(time);
        TextView t2 = cell(src);
        TextView t3 = cell(event);
        
        row.addView(t1);
        row.addView(t2);
        row.addView(t3);
        
        row.setOnClickListener(v -> handleRowClick(row, event));
        binding.llLogs.addView(row);
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
