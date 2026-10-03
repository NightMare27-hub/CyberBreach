package com.example.cyberbreach.engine;

public class ToolEngine {

    private static final int BASE_FIREWALL_PENALTY = 15;
    private static final int BASE_HINT_COST_SEC = 10;
    private static final int BASE_UPGRADE_COST_CREDITS = 30;

    /** Calculates firewall breach penalty points based on firewall tier (Tier 1 = 15%, Tier 2 = 12%, ..., min 3%). */
    public static int calculateFirewallPenalty(int firewallTier) {
        int tier = Math.max(1, firewallTier);
        return Math.max(3, BASE_FIREWALL_PENALTY - 3 * (tier - 1));
    }

    /** Calculates hint cost in seconds based on log filter tier (Tier 1 = 10s, Tier 2 = 8s, ..., min 2s). */
    public static int calculateHintCost(int filterTier) {
        int tier = Math.max(1, filterTier);
        return Math.max(2, BASE_HINT_COST_SEC - 2 * (tier - 1));
    }

    /** Calculates credit cost to upgrade a tool from its current tier. */
    public static int getUpgradeCost(int currentTier) {
        int tier = Math.max(1, currentTier);
        return BASE_UPGRADE_COST_CREDITS * tier;
    }

    /** Generates a descriptive summary label for tool upgrade buttons in UI. */
    public static String getUpgradeCapabilityDescription(String toolName, int currentTier) {
        int nextTier = currentTier + 1;
        switch (toolName.toLowerCase()) {
            case "firewall":
                return "Tier " + currentTier + " -> " + nextTier + "\n(" + calculateFirewallPenalty(nextTier) + "% mistake penalty)";
            case "log_filter":
            case "log filter":
                return "Tier " + currentTier + " -> " + nextTier + "\n(" + calculateHintCost(nextTier) + "s hint cost)";
            case "packet_analyzer":
                return "Tier " + currentTier + " -> " + nextTier + "\n(Highlights high-risk anomalies)";
            case "patch_manager":
                return "Tier " + currentTier + " -> " + nextTier + "\n(Reduces patch deploy time by " + (nextTier * 10) + "%)";
            case "isolation_framework":
                return "Tier " + currentTier + " -> " + nextTier + "\n(Adds " + (nextTier * 5) + "% breach meter buffer)";
            case "email_gateway":
                return "Tier " + currentTier + " -> " + nextTier + "\n(" + (nextTier * 15) + "% chance to auto-flag phishing)";
            case "crypto_manager":
                return "Tier " + currentTier + " -> " + nextTier + "\n(Reduces crypto-error penalty by " + (nextTier * 20) + "%)";
            default:
                return "Tier " + currentTier + " -> " + nextTier;
        }
    }

    /** Evaluates Packet Analyzer capabilities based on tier. */
    public static String analyzePacketDetail(String rawDetail, int analyzerTier) {
        if (rawDetail == null) return "";
        if (analyzerTier >= 2) {
            if (rawDetail.contains("EXPLOIT") || rawDetail.contains("LOGIN_FAIL") || rawDetail.contains("TELNET")) {
                return "[HIGH RISK] " + rawDetail;
            }
        }
        return rawDetail;
    }
}
