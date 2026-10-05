package com.example.cyberbreach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.cyberbreach.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        com.example.cyberbreach.data.DbHelper db = com.example.cyberbreach.data.DbHelper.get(requireContext());
        int stars = db.getTotalStars();
        
        binding.tvRankText.setText(com.example.cyberbreach.data.DbHelper.rankForStars(stars));
        binding.tvCredits.setText(String.valueOf(db.getCredits()));

        binding.btnDailyBounties.setOnClickListener(v -> {
            // Tell MainActivity to switch to "Play" tab (index 1)
            ((com.example.cyberbreach.MainActivity) requireActivity()).switchToTab(1);
        });

        binding.btnContinueTraining.setOnClickListener(v -> {
            // Tell MainActivity to switch to "Play" tab (index 1)
            ((com.example.cyberbreach.MainActivity) requireActivity()).switchToTab(1);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        com.example.cyberbreach.data.DbHelper db = com.example.cyberbreach.data.DbHelper.get(requireContext());
        binding.tvRankText.setText(com.example.cyberbreach.data.DbHelper.rankForStars(db.getTotalStars()));
        binding.tvCredits.setText(String.valueOf(db.getCredits()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
