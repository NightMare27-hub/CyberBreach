package com.example.cyberbreach.engine;

import com.example.cyberbreach.data.DbHelper;
import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LevelStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CertificationEngine {

    public static class Certification {
        public String id;
        public String name;
        public String description;
        public boolean unlocked;
        public int progress;
        public int maxProgress;

        public Certification(String id, String name, String description, int maxProgress) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.maxProgress = maxProgress;
        }
    }

    public static List<Certification> getCertifications(DbHelper db) {
        List<Certification> certs = new ArrayList<>();
        
        Certification cba = new Certification("cert_cba", "CBA-I (CyberBreach Analyst)", "Complete 10 challenges with at least 1 star.", 10);
        Certification wda = new Certification("cert_wda", "WDA (Web Defense Associate)", "Resolve 5 Web App vulnerabilities (SQLi or XSS).", 5);
        Certification mi = new Certification("cert_mi", "MI-PRO (Malware Investigator)", "Mitigate 5 Malware incidents (Ransomware or Zero-Day).", 5);
        
        Map<String, DbHelper.Progress> progressMap = db.getAllProgress();
        
        int totalSolved = 0;
        int webAppSolved = 0;
        int malwareSolved = 0;

        for (Level level : LevelStore.getLevels()) {
            DbHelper.Progress p = progressMap.get(level.id);
            if (p != null && p.stars > 0) {
                totalSolved++;
                
                if (level.track != null && level.track.equals("Web App")) {
                    webAppSolved++;
                } else if (level.concept != null && (level.concept.equals("sql_injection") || level.concept.equals("xss"))) {
                    webAppSolved++; // Fallback
                }
                
                if (level.track != null && level.track.equals("Malware")) {
                    malwareSolved++;
                } else if (level.concept != null && (level.concept.equals("ransomware") || level.concept.equals("zero_day"))) {
                    malwareSolved++; // Fallback
                }
            }
        }
        
        cba.progress = Math.min(totalSolved, cba.maxProgress);
        cba.unlocked = cba.progress >= cba.maxProgress;
        
        wda.progress = Math.min(webAppSolved, wda.maxProgress);
        wda.unlocked = wda.progress >= wda.maxProgress;
        
        mi.progress = Math.min(malwareSolved, mi.maxProgress);
        mi.unlocked = mi.progress >= mi.maxProgress;

        certs.add(cba);
        certs.add(wda);
        certs.add(mi);

        // Sync with Database Achievements table
        for (Certification c : certs) {
            if (c.unlocked) {
                db.unlockAchievement(c.id);
            }
        }

        return certs;
    }
}
