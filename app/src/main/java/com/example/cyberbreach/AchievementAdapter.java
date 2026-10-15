package com.example.cyberbreach;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cyberbreach.databinding.ItemAchievementBadgeBinding;
import com.example.cyberbreach.engine.Achievement;

import java.util.List;
import java.util.Set;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.VH> {

    private final List<Achievement> achievements;
    private Set<String> unlockedIds;

    public AchievementAdapter(List<Achievement> achievements, Set<String> unlockedIds) {
        this.achievements = achievements;
        this.unlockedIds = unlockedIds;
    }

    public void setUnlockedIds(Set<String> unlockedIds) {
        this.unlockedIds = unlockedIds;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAchievementBadgeBinding b;

        VH(ItemAchievementBadgeBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAchievementBadgeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Achievement a = achievements.get(position);
        boolean isUnlocked = unlockedIds != null && unlockedIds.contains(a.name());

        h.b.ivBadgeIcon.setImageResource(a.iconResId);
        h.b.tvBadgeTitle.setText(a.title);

        if (isUnlocked) {
            h.b.tvBadgeStatus.setText("UNLOCKED");
            h.b.tvBadgeStatus.setTextColor(h.itemView.getContext().getColor(R.color.soc_accent_cyan));
            h.b.ivBadgeIcon.setColorFilter(h.itemView.getContext().getColor(R.color.soc_accent_cyan));
            h.b.getRoot().setAlpha(1.0f);
        } else {
            h.b.tvBadgeStatus.setText("LOCKED");
            h.b.tvBadgeStatus.setTextColor(h.itemView.getContext().getColor(R.color.soc_text_secondary));
            h.b.ivBadgeIcon.setColorFilter(h.itemView.getContext().getColor(R.color.soc_text_secondary));
            h.b.getRoot().setAlpha(0.5f);
        }

        h.b.getRoot().setOnClickListener(v -> {
            String status = isUnlocked ? "[UNLOCKED] " : "[LOCKED] ";
            Toast.makeText(v.getContext(), status + a.title + ": " + a.description, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }
}
