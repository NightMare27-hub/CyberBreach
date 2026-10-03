package com.example.cyberbreach;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomePagerAdapter extends FragmentStateAdapter {

    public HomePagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new HomeFragment();
            case 1: return new PlayFragment();
            case 2: return new LearnFragment();
            case 3: return new ArmoryFragment();
            default: return new ProfileFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
