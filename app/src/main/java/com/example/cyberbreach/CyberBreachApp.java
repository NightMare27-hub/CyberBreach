package com.example.cyberbreach;

import android.app.Application;

import com.example.cyberbreach.data.LevelRepository;
import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LevelStore;

import java.util.List;

public class CyberBreachApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Reads the saved copy or the bundled file (a few KB), so a main-thread read is fine.
        List<Level> local = new LevelRepository(this).loadLocal();
        LevelStore.set(local);
    }
}
