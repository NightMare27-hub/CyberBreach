package com.example.cyberbreach;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cyberbreach.data.DbHelper;
import com.example.cyberbreach.databinding.ActivityTutorialBinding;
import com.example.cyberbreach.game.GameEngine;

public class TutorialActivity extends AppCompatActivity {

    private ActivityTutorialBinding binding;
    private int currentStep = 1;
    private boolean isThreatFlagged = false;
    private LinearLayout maliciousRow;

    private GameEngine.Tool selectedTool = GameEngine.Tool.PACKET_ANALYZER;

    private final int[] toolIcons = {
            android.R.drawable.ic_menu_search,
            android.R.drawable.ic_menu_close_clear_cancel,
            android.R.drawable.ic_menu_sort_by_size,
            android.R.drawable.ic_menu_manage,
            android.R.drawable.ic_lock_lock,
            android.R.drawable.ic_dialog_email,
            android.R.drawable.ic_secure
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTutorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolSpinner();
        populateLogs();

        binding.btnNextStep.setOnClickListener(v -> onNextStepClicked());
        updateGuidanceUI();
    }

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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void populateLogs() {
        addLogRow("00:01", "192.168.1.10", "HTTP_GET", false);
        addLogRow("00:02", "192.168.1.12", "HTTP_GET", false);
        maliciousRow = addLogRow("00:03", "10.0.0.55", "SYN_FLOOD", true);
        addLogRow("00:04", "192.168.1.15", "DNS_QUERY", false);
    }

    private LinearLayout addLogRow(String time, String src, String event, boolean isMalicious) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TypedValue tvAttr = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tvAttr, true);
        row.setBackgroundResource(tvAttr.resourceId);

        row.addView(cell(time));
        row.addView(cell(src));
        row.addView(cell(event));

        row.setOnClickListener(v -> onLogRowTapped(row, isMalicious, src));
        binding.llLogs.addView(row);
        return row;
    }

    private TextView cell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tv.setTextColor(getColor(R.color.soc_primary));
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setPadding(10, 8, 10, 8);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        return tv;
    }

    private void onLogRowTapped(LinearLayout row, boolean isMalicious, String src) {
        if (currentStep == 1) {
            // Step 1: Must select Packet Analyzer and tap the malicious row
            if (selectedTool == GameEngine.Tool.PACKET_ANALYZER) {
                if (isMalicious) {
                    isThreatFlagged = true;
                    row.setBackgroundColor(getColor(R.color.soc_warning));
                    for (int i = 0; i < row.getChildCount(); i++) {
                        ((TextView) row.getChildAt(i)).setTextColor(getColor(R.color.soc_background));
                    }
                    binding.tvStatus.setText("Threat Flagged: " + src + " (SYN_FLOOD)");
                    Toast.makeText(this, "Target Flagged! Now proceed to Step 2.", Toast.LENGTH_SHORT).show();
                    currentStep = 2;
                    updateGuidanceUI();
                } else {
                    Toast.makeText(this, "Legitimate traffic. Tap the 10.0.0.55 SYN_FLOOD entry!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Select the Packet Analyzer tool first!", Toast.LENGTH_SHORT).show();
            }
        } else if (currentStep == 2) {
            // Step 2: Must select Firewall and tap the flagged row
            if (selectedTool == GameEngine.Tool.FIREWALL) {
                if (isMalicious) {
                    row.setBackgroundColor(getColor(R.color.soc_primary));
                    for (int i = 0; i < row.getChildCount(); i++) {
                        ((TextView) row.getChildAt(i)).setTextColor(getColor(R.color.soc_background));
                    }
                    binding.tvStatus.setText("Incident Contained: " + src + " blocked!");
                    Toast.makeText(this, "Breach Contained!", Toast.LENGTH_SHORT).show();
                    currentStep = 3;
                    updateGuidanceUI();
                } else {
                    Toast.makeText(this, "Tap the flagged 10.0.0.55 entry to block it!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Select the Firewall tool first to mitigate the threat!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onNextStepClicked() {
        if (currentStep == 1 && !isThreatFlagged) {
            Toast.makeText(this, "Select Packet Analyzer and tap the 10.0.0.55 SYN_FLOOD entry!", Toast.LENGTH_SHORT).show();
        } else if (currentStep == 2) {
            Toast.makeText(this, "Select Firewall and tap the flagged row to contain the breach!", Toast.LENGTH_SHORT).show();
        } else if (currentStep == 3) {
            finishTutorial();
        }
    }

    private void updateGuidanceUI() {
        switch (currentStep) {
            case 1:
                binding.tvGuideStep.setText("STEP 1/3: IDENTIFY THREAT");
                binding.tvGuideText.setText("Anomalous SYN_FLOOD traffic detected! Select 'Packet Analyzer' from the tool selector below, then tap the '10.0.0.55' log row to flag it.");
                binding.btnNextStep.setVisibility(View.GONE);
                break;
            case 2:
                binding.tvGuideStep.setText("STEP 2/3: MITIGATE THREAT");
                binding.tvGuideText.setText("Target Flagged! Now switch the tool selector to 'Firewall' and tap the yellow flagged row to block the attacker.");
                binding.btnNextStep.setVisibility(View.GONE);
                break;
            case 3:
                binding.tvGuideStep.setText("STEP 3/3: MISSION COMPLETE");
                binding.tvGuideText.setText("Outstanding job! You've mastered two-stage SOC incident response (Identify -> Mitigate). You are ready for live command.");
                binding.btnNextStep.setText("Enter Command Dashboard");
                binding.btnNextStep.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void finishTutorial() {
        DbHelper db = DbHelper.get(this);
        db.setTutorialCompleted();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
