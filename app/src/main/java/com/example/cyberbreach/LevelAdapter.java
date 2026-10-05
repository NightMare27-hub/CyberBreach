package com.example.cyberbreach;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cyberbreach.data.DbHelper;
import com.example.cyberbreach.databinding.ItemLevelBinding;
import com.example.cyberbreach.engine.LevelEngine;
import com.example.cyberbreach.engine.MasteryStatus;
import com.example.cyberbreach.model.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.VH> {

    public interface OnLevelClick {
        void onClick(Level level);
    }

    private List<Level> levels;
    private Map<String, DbHelper.Progress> progress = new HashMap<>();
    private final OnLevelClick listener;

    public LevelAdapter(List<Level> levels, OnLevelClick listener) {
        this.levels = new ArrayList<>(levels);
        this.listener = listener;
    }

    public void setLevels(List<Level> newLevels) {
        this.levels = new ArrayList<>(newLevels);
        notifyDataSetChanged();
    }

    public void setProgress(Map<String, DbHelper.Progress> newProgress) {
        this.progress = newProgress;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemLevelBinding b;

        VH(ItemLevelBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemLevelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Level level = levels.get(position);
        
        if (level.id != null && level.id.startsWith("HEADER_")) {
            h.b.tvTitle.setText(level.title);
            h.b.tvTitle.setTextColor(h.itemView.getContext().getColor(R.color.soc_accent_cyan));
            h.b.tvTitle.setTextSize(16f);
            h.b.tvMeta.setVisibility(View.GONE);
            h.b.tvStars.setVisibility(View.GONE);
            h.b.tvBest.setVisibility(View.GONE);
            h.b.ivLock.setVisibility(View.GONE);
            h.b.getRoot().setAlpha(1f);
            h.b.getRoot().setOnClickListener(null);
            h.b.getRoot().setClickable(false);
            
            // Modify card visuals for header
            h.b.getRoot().setCardBackgroundColor(0x00000000);
            h.b.getRoot().setStrokeColor(0x00000000);
            h.b.getRoot().setCardElevation(0f);
            return;
        }

        h.b.tvMeta.setVisibility(View.VISIBLE);
        h.b.tvStars.setVisibility(View.VISIBLE);
        h.b.tvBest.setVisibility(View.VISIBLE);
        h.b.tvTitle.setTextColor(h.itemView.getContext().getColor(R.color.soc_text_primary));
        h.b.tvTitle.setTextSize(18f);

        DbHelper.Progress p = progress.get(level.id);
        
        // Find previous actual level to determine unlock status (not header)
        boolean unlocked = false;
        if (position == 0 || (position == 1 && levels.get(0).id.startsWith("HEADER"))) {
            unlocked = true;
        } else {
            // Find last level
            unlocked = (p != null && p.unlocked);
        }

        int stars = p != null ? p.stars : 0;
        int best = p != null ? p.bestTimeSec : 0;

        MasteryStatus status = LevelEngine.getMasteryStatus(unlocked, p != null && p.stars > 0, stars, 0, 0);

        h.b.tvTitle.setText(level.title);
        h.b.tvMeta.setText(level.track + "  |  " + level.timeLimitSec + " s");
        if (status == MasteryStatus.MASTERED) {
            h.b.tvStars.setText("\u2605 MASTERED");
        } else {
            h.b.tvStars.setText(starText(stars));
        }
        h.b.tvBest.setText(best > 0
                ? String.format(Locale.US, "Best %02d:%02d", best / 60, best % 60) : "--");
        h.b.ivLock.setVisibility(unlocked ? View.GONE : View.VISIBLE);
        h.b.getRoot().setAlpha(unlocked ? 1f : 0.55f);

        // Restore card visuals for regular item
        h.b.getRoot().setCardBackgroundColor(h.itemView.getContext().getColor(R.color.soc_surface));
        h.b.getRoot().setStrokeColor(h.itemView.getContext().getColor(R.color.soc_surface_stroke));
        h.b.getRoot().setCardElevation(2f);

        final boolean isUnlocked = unlocked;
        h.b.getRoot().setOnClickListener(v -> {
            if (isUnlocked) {
                listener.onClick(level);
            } else {
                Toast.makeText(v.getContext(), R.string.toast_level_locked, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    private static String starText(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < stars ? "\u2605" : "\u2606");
        }
        return sb.toString();
    }
}
