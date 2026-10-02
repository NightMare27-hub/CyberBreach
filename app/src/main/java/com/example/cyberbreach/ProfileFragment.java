package com.example.cyberbreach;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.cyberbreach.data.DbHelper;
import com.example.cyberbreach.data.PrefsManager;
import com.example.cyberbreach.databinding.FragmentProfileBinding;
import com.example.cyberbreach.engine.ToolEngine;
import com.example.cyberbreach.notify.ReminderScheduler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private DbHelper db;
    private PrefsManager prefs;
    private boolean ignoreSwitch = false;

    private final ActivityResultLauncher<String> notificationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    enableReminder();
                } else {
                    prefs.setReminderEnabled(false);
                    setReminderSwitch(false);
                    Toast.makeText(requireContext(), R.string.perm_notif_denied, Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = DbHelper.get(requireContext());
        prefs = new PrefsManager(requireContext());

        binding.switchMute.setChecked(prefs.isMuted());
        binding.switchMute.setOnCheckedChangeListener((b, checked) -> prefs.setMuted(checked));
        binding.switchHaptics.setChecked(prefs.isHapticsEnabled());
        binding.switchHaptics.setOnCheckedChangeListener((b, checked) -> prefs.setHapticsEnabled(checked));

        binding.switchReminder.setChecked(prefs.isReminderEnabled());
        binding.switchReminder.setOnCheckedChangeListener((b, checked) -> onReminderToggled(checked));

        binding.btnUpgradeFirewall.setOnClickListener(v -> upgrade("firewall"));
        binding.btnUpgradeFilter.setOnClickListener(v -> upgrade("log_filter"));

        binding.btnResetProgress.setOnClickListener(v -> confirm(
                R.string.dialog_reset_title, R.string.dialog_reset_message, () -> {
                    db.resetProgress();
                    Toast.makeText(requireContext(), R.string.toast_reset_done, Toast.LENGTH_SHORT).show();
                    refresh();
                }));
        binding.btnClearHistory.setOnClickListener(v -> confirm(
                R.string.dialog_clear_title, R.string.dialog_clear_message, () -> {
                    db.clearHistory();
                    Toast.makeText(requireContext(), R.string.toast_history_cleared, Toast.LENGTH_SHORT).show();
                    refresh();
                }));
    }

    private void setReminderSwitch(boolean checked) {
        if (binding == null) return;
        ignoreSwitch = true;
        binding.switchReminder.setChecked(checked);
        ignoreSwitch = false;
    }

    private void onReminderToggled(boolean checked) {
        if (ignoreSwitch) return;
        if (!checked) {
            prefs.setReminderEnabled(false);
            ReminderScheduler.cancel(requireContext());
            Toast.makeText(requireContext(), R.string.toast_reminder_off, Toast.LENGTH_SHORT).show();
            return;
        }
        boolean needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
        if (!needsPermission) {
            enableReminder();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.perm_notif_title)
                    .setMessage(R.string.perm_notif_message)
                    .setPositiveButton(R.string.perm_continue,
                            (d, w) -> notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS))
                    .setNegativeButton(R.string.dialog_cancel, (d, w) -> setReminderSwitch(false))
                    .setOnCancelListener(d -> setReminderSwitch(false))
                    .show();
        } else {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void enableReminder() {
        prefs.setReminderEnabled(true);
        setReminderSwitch(true);
        ReminderScheduler.schedule(requireContext());
        Toast.makeText(requireContext(), R.string.toast_reminder_on, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        int stars = db.getTotalStars();
        binding.tvRank.setText(DbHelper.rankForStars(stars));
        binding.tvTotalStars.setText(String.valueOf(stars));
        binding.tvCredits.setText(String.valueOf(db.getCredits()));
        binding.tvAttempts.setText(String.valueOf(db.getAttemptCount()));
        binding.ivRank.setImageResource(android.R.drawable.btn_star_big_on);

        binding.btnUpgradeFirewall.setText(upgradeLabel("Firewall", "firewall"));
        binding.btnUpgradeFilter.setText(upgradeLabel("Log Filter", "log_filter"));
    }

    private String upgradeLabel(String name, String toolId) {
        int tier = db.getToolTier(toolId);
        return getString(R.string.upgrade_format, name, tier, tier + 1, upgradeCost(tier));
    }

    private int upgradeCost(int tier) {
        return ToolEngine.getUpgradeCost(tier);
    }

    private void upgrade(String toolId) {
        int cost = upgradeCost(db.getToolTier(toolId));
        if (db.upgradeTool(toolId, cost)) {
            Toast.makeText(requireContext(), R.string.toast_upgraded, Toast.LENGTH_SHORT).show();
            refresh();
        } else {
            Toast.makeText(requireContext(), R.string.toast_not_enough_credits, Toast.LENGTH_SHORT).show();
        }
    }

    private void confirm(int titleRes, int messageRes, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setPositiveButton(R.string.dialog_confirm, (d, w) -> onConfirm.run())
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
