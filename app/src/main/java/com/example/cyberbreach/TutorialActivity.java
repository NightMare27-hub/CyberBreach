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

        binding.overlayLayer.setOnClickListener(v -> advanceStep());
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
                maliciousRow = addLog("00:03", "10.0.0.55", "SYN_FLOOD");
                binding.tvOverlay.setText("Anomalous traffic detected!\n\nStep 1: Identify\n[ TAP ANYWHERE TO SELECT PACKET ANALYZER ]");
                binding.overlayLayer.setVisibility(View.VISIBLE);
                break;
            case 3:
                binding.ivTool.setImageResource(android.R.drawable.ic_menu_search);
                binding.tvTool.setText("Packet Analyzer");
                binding.tvOverlay.setText("Analyzer selected.\n\n[ TAP ANYWHERE TO FLAG THE ATTACKER ]");
                break;
            case 4:
                if (maliciousRow != null) {
                    maliciousRow.setBackgroundColor(getColor(R.color.soc_warning));
                    for (int i = 0; i < maliciousRow.getChildCount(); i++) {
                        ((TextView) maliciousRow.getChildAt(i)).setTextColor(getColor(R.color.soc_background));
                    }
                }
                binding.tvOverlay.setText("Target Flagged!\n\nStep 2: Mitigate\n[ TAP ANYWHERE TO SELECT FIREWALL ]");
                break;
            case 5:
                binding.ivTool.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                binding.tvTool.setText("Firewall");
                binding.tvOverlay.setText("Firewall selected.\n\n[ TAP ANYWHERE TO BLOCK THE IP ]");
                break;
            case 6:
                if (maliciousRow != null) {
                    maliciousRow.setBackgroundColor(getColor(R.color.soc_primary));
                    for (int i = 0; i < maliciousRow.getChildCount(); i++) {
                        ((TextView) maliciousRow.getChildAt(i)).setTextColor(getColor(R.color.soc_background));
                    }
                }
                binding.tvOverlay.setText("Incident Contained!\n\nYou're ready to deploy.\n[ TAP TO ENTER THE DASHBOARD ]");
                break;
            case 7:
                finishTutorial();
                break;
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
