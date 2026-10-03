package com.example.cyberbreach;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.cyberbreach.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.pager.setAdapter(new HomePagerAdapter(this));
        
        // Prevent swipe if you want to rely only on bottom nav, or leave it. We'll leave it for now.
        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0: binding.bottomNav.setSelectedItemId(R.id.nav_home); break;
                    case 1: binding.bottomNav.setSelectedItemId(R.id.nav_missions); break;
                    case 2: binding.bottomNav.setSelectedItemId(R.id.nav_learn); break;
                    case 3: binding.bottomNav.setSelectedItemId(R.id.nav_armory); break;
                    case 4: binding.bottomNav.setSelectedItemId(R.id.nav_profile); break;
                }
            }
        });

        binding.bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) { binding.pager.setCurrentItem(0); return true; }
                else if (id == R.id.nav_missions) { binding.pager.setCurrentItem(1); return true; }
                else if (id == R.id.nav_learn) { binding.pager.setCurrentItem(2); return true; }
                else if (id == R.id.nav_armory) { binding.pager.setCurrentItem(3); return true; }
                else if (id == R.id.nav_profile) { binding.pager.setCurrentItem(4); return true; }
                return false;
            }
        });
    }
}
