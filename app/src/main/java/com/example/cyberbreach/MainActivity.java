package com.example.cyberbreach;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cyberbreach.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        binding.pager.setAdapter(new HomePagerAdapter(this));

        final String[] titles = {
                getString(R.string.tab_play),
                getString(R.string.tab_learn),
                getString(R.string.tab_profile)
        };
        new TabLayoutMediator(binding.tabs, binding.pager,
                (tab, position) -> tab.setText(titles[position])).attach();
    }
}
