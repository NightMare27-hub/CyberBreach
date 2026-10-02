package com.example.cyberbreach.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ToolEngineTest {

    @Test
    public void firewallPenaltyScaling() {
        assertEquals(15, ToolEngine.calculateFirewallPenalty(1));
        assertEquals(12, ToolEngine.calculateFirewallPenalty(2));
        assertEquals(9, ToolEngine.calculateFirewallPenalty(3));
        assertEquals(6, ToolEngine.calculateFirewallPenalty(4));
        assertEquals(3, ToolEngine.calculateFirewallPenalty(5));
        assertEquals(3, ToolEngine.calculateFirewallPenalty(10)); // minimum 3
    }

    @Test
    public void hintCostScaling() {
        assertEquals(10, ToolEngine.calculateHintCost(1));
        assertEquals(8, ToolEngine.calculateHintCost(2));
        assertEquals(6, ToolEngine.calculateHintCost(3));
        assertEquals(4, ToolEngine.calculateHintCost(4));
        assertEquals(2, ToolEngine.calculateHintCost(5));
        assertEquals(2, ToolEngine.calculateHintCost(10)); // minimum 2
    }

    @Test
    public void upgradeCostCurve() {
        assertEquals(30, ToolEngine.getUpgradeCost(1));
        assertEquals(60, ToolEngine.getUpgradeCost(2));
        assertEquals(90, ToolEngine.getUpgradeCost(3));
    }

    @Test
    public void upgradeCapabilityDescription() {
        String firewallDesc = ToolEngine.getUpgradeCapabilityDescription("Firewall", 1);
        assertTrue(firewallDesc.contains("Tier 1 -> 2"));
        assertTrue(firewallDesc.contains("12% mistake penalty"));

        String filterDesc = ToolEngine.getUpgradeCapabilityDescription("Log Filter", 1);
        assertTrue(filterDesc.contains("8s hint cost"));
    }

    @Test
    public void packetAnalyzerHighlighting() {
        String raw = "User tried EXPLOIT on checkout";
        String analyzed = ToolEngine.analyzePacketDetail(raw, 2);
        assertTrue(analyzed.contains("[HIGH RISK]"));

        String normal = "User logged in";
        assertEquals("User logged in", ToolEngine.analyzePacketDetail(normal, 2));
    }
}
