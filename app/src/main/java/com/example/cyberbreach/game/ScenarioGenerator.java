package com.example.cyberbreach.game;

import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LogEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ScenarioGenerator {

    private static final String[] ORGS = {
            "OmniCorp Global", "NeoTech Startups", "Global Bank Inc.", 
            "Federal Archive", "City Power Grid", "St. Mary's Hospital"
    };

    private static final String[] IPS = {
            "192.168.1.", "10.0.0.", "172.16.0.", "203.0.113.", "198.51.100."
    };

    /**
     * Generates a procedural list of daily bounties.
     * @param seed The seed for the random number generator (e.g., today's date).
     * @param count The number of levels to generate.
     * @return List of generated Levels.
     */
    public static List<Level> generateDailyBounties(long seed, int count) {
        Random random = new Random(seed);
        List<Level> levels = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            levels.add(generateScenario(random, "daily_" + i, "Daily Incident 0" + i, Level.Difficulty.HARD));
        }
        return levels;
    }

    private static Level generateScenario(Random random, String id, String title, Level.Difficulty difficulty) {
        String org = ORGS[random.nextInt(ORGS.length)];
        
        // Define possible vulnerability concepts based on phase 1 additions
        String[] concepts = {"bruteforce", "sql_injection", "xss", "ddos", "open_port", "ransomware", "phishing", "zero_day", "mitm"};
        String[] tracks = {"Access", "Web App", "Web App", "Networking", "Networking", "Malware", "Social Eng", "Defense", "Networking"};
        String[] fixes = {"block_ip", "patch", "patch", "block_ip", "close_port", "isolate_host", "quarantine_email", "patch", "enable_encryption"};
        
        int typeIdx = random.nextInt(concepts.length);
        String concept = concepts[typeIdx];
        String track = tracks[typeIdx];
        String fix = fixes[typeIdx];
        
        int targetPort = (concept.equals("bruteforce") || concept.equals("open_port")) ? 22 : 
                         (concept.equals("sql_injection") || concept.equals("xss") ? 80 : 
                         (random.nextBoolean() ? 443 : 80));
                         
        String maliciousIp = randomIp(random);
        String target = fix.equals("block_ip") || fix.equals("isolate_host") ? maliciousIp : 
                        (fix.equals("close_port") ? String.valueOf(targetPort) : 
                        (fix.equals("patch") ? "EXPLOIT_ATTEMPT" : "SMTP_IN"));

        int timeLimitSec = 90 - (random.nextInt(30)); // 60 to 90 seconds
        
        List<LogEntry> logs = generateStreamingLogs(random, concept, maliciousIp, targetPort);

        List<String> hints = new ArrayList<>();
        hints.add("Look for anomalies related to " + concept + ".");
        hints.add("Check traffic from " + maliciousIp + " or on port " + targetPort + ".");
        hints.add("Use the appropriate tool to mitigate.");

        return new Level(id, title, org, track, concept, timeLimitSec, concept, fix, target, 
                "Procedural Training Scenario based on " + concept + ".", hints, logs, difficulty);
    }

    private static List<LogEntry> generateStreamingLogs(Random random, String concept, String maliciousIp, int targetPort) {
        List<LogEntry> logs = new ArrayList<>();
        int baseHour = 14;
        int baseMin = random.nextInt(50);
        
        // Generate 30 logs (mostly noise)
        for (int i = 0; i < 30; i++) {
            int sec = i * 2 + random.nextInt(3);
            String time = String.format(Locale.US, "%02d:%02d:%02d", baseHour, baseMin, sec % 60);
            
            boolean isMalicious = random.nextDouble() < 0.25; // 25% chance of being the attack vector
            
            if (isMalicious) {
                logs.add(new LogEntry(time, maliciousIp, targetPort, getMaliciousEvent(concept), getMaliciousDetail(concept)));
            } else {
                int noisePort = random.nextBoolean() ? 443 : (random.nextBoolean() ? 80 : 53);
                String noiseEvent = noisePort == 443 ? "HTTPS_OK" : (noisePort == 80 ? "HTTP_OK" : "DNS_QUERY");
                String noiseDetail = noisePort == 443 ? "Encrypted web session" : "Standard background sync";
                logs.add(new LogEntry(time, randomIp(random), noisePort, noiseEvent, noiseDetail));
            }
        }
        
        // Ensure at least one attack log exists
        boolean hasAttack = false;
        for (LogEntry log : logs) {
            if (log.src.equals(maliciousIp)) {
                hasAttack = true;
                break;
            }
        }
        
        if (!hasAttack) {
            logs.add(new LogEntry(String.format(Locale.US, "%02d:%02d:59", baseHour, baseMin), maliciousIp, targetPort, getMaliciousEvent(concept), getMaliciousDetail(concept)));
        }
        
        return logs;
    }

    private static String randomIp(Random random) {
        return IPS[random.nextInt(IPS.length)] + (random.nextInt(253) + 1);
    }

    private static String getMaliciousEvent(String concept) {
        switch (concept) {
            case "bruteforce": return "LOGIN_FAIL";
            case "sql_injection": return "HTTP_500";
            case "xss": return "HTTP_REQ";
            case "ddos": return "SYN_FLOOD";
            case "open_port": return "TELNET_OPEN";
            case "ransomware": return "SMB_WRITE";
            case "phishing": return "SMTP_IN";
            case "zero_day": return "EXPLOIT_ATTEMPT";
            case "mitm": return "ARP_REPLY";
            default: return "MALICIOUS";
        }
    }

    private static String getMaliciousDetail(String concept) {
        switch (concept) {
            case "bruteforce": return "User admin, wrong password";
            case "sql_injection": return "SQL Syntax error in query parameter";
            case "xss": return "Payload contains <script> tag";
            case "ddos": return "Abnormal SYN requests detected";
            case "open_port": return "Unencrypted remote login";
            case "ransomware": return "Mass file encryption detected";
            case "phishing": return "Suspicious sender domain mimicking IT";
            case "zero_day": return "Unknown heap spray pattern";
            case "mitm": return "Unexpected ARP broadcast for gateway";
            default: return "Unknown threat signature";
        }
    }
}
