package com.example.cyberbreach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.cyberbreach.data.DbHelper;
import com.example.cyberbreach.databinding.FragmentArmoryBinding;
import com.example.cyberbreach.engine.ToolEngine;

public class ArmoryFragment extends Fragment {
    private FragmentArmoryBinding binding;
    private DbHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentArmoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = DbHelper.get(requireContext());
        
        binding.btnUpgradeFirewall.setOnClickListener(v -> upgrade("firewall"));
        binding.btnUpgradeFilter.setOnClickListener(v -> upgrade("log_filter"));
        binding.btnUpgradeAnalyzer.setOnClickListener(v -> upgrade("packet_analyzer"));
        binding.btnUpgradePatch.setOnClickListener(v -> upgrade("patch_manager"));
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        binding.tvCredits.setText("Credits: " + db.getCredits());
        binding.btnUpgradeFirewall.setText(upgradeLabel("Firewall", "firewall"));
        binding.btnUpgradeFilter.setText(upgradeLabel("Log Filter", "log_filter"));
        binding.btnUpgradeAnalyzer.setText(upgradeLabel("Packet Analyzer", "packet_analyzer"));
        binding.btnUpgradePatch.setText(upgradeLabel("Patch Manager", "patch_manager"));
    }

    private String upgradeLabel(String name, String toolId) {
        int tier = db.getToolTier(toolId);
        int cost = ToolEngine.getUpgradeCost(tier);
        return getString(R.string.upgrade_format, name, tier, tier + 1, cost);
    }

    private void upgrade(String toolId) {
        int cost = ToolEngine.getUpgradeCost(db.getToolTier(toolId));
        if (db.upgradeTool(toolId, cost)) {
            Toast.makeText(requireContext(), R.string.toast_upgraded, Toast.LENGTH_SHORT).show();
            refresh();
        } else {
            Toast.makeText(requireContext(), R.string.toast_not_enough_credits, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
