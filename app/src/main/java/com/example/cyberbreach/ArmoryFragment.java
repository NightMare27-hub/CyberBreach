package com.example.cyberbreach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        binding.tvCredits.setText(String.valueOf(db.getCredits()));
        
        setupToolCard(binding.cardAnalyzer.getRoot(), "Packet Analyzer", "packet_analyzer", android.R.drawable.ic_menu_search);
        setupToolCard(binding.cardFirewall.getRoot(), "Firewall", "firewall", android.R.drawable.ic_menu_close_clear_cancel);
        setupToolCard(binding.cardFilter.getRoot(), "Log Filter", "log_filter", android.R.drawable.ic_menu_sort_by_size);
        setupToolCard(binding.cardPatch.getRoot(), "Patch Manager", "patch_manager", android.R.drawable.ic_menu_manage);
        setupToolCard(binding.cardIsolation.getRoot(), "Isolation Framework", "isolation_framework", android.R.drawable.ic_lock_lock);
        setupToolCard(binding.cardEmail.getRoot(), "Email Gateway", "email_gateway", android.R.drawable.ic_dialog_email);
        setupToolCard(binding.cardCrypto.getRoot(), "Crypto Manager", "crypto_manager", android.R.drawable.ic_secure);
    }

    private void setupToolCard(View cardRoot, String name, String toolId, int iconResId) {
        ImageView ivIcon = cardRoot.findViewById(R.id.ivIcon);
        TextView tvToolName = cardRoot.findViewById(R.id.tvToolName);
        TextView tvToolDesc = cardRoot.findViewById(R.id.tvToolDesc);
        Button btnUpgrade = cardRoot.findViewById(R.id.btnUpgrade);

        ivIcon.setImageResource(iconResId);
        tvToolName.setText(name);

        int tier = db.getToolTier(toolId);
        int cost = ToolEngine.getUpgradeCost(tier);
        
        tvToolDesc.setText(ToolEngine.getUpgradeCapabilityDescription(toolId, tier));
        btnUpgrade.setText(cost + " C");

        btnUpgrade.setOnClickListener(v -> {
            if (db.upgradeTool(toolId, cost)) {
                Toast.makeText(requireContext(), R.string.toast_upgraded, Toast.LENGTH_SHORT).show();
                refresh();
            } else {
                Toast.makeText(requireContext(), R.string.toast_not_enough_credits, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
