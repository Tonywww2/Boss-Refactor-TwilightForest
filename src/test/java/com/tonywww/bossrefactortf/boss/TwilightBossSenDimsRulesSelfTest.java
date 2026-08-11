package com.tonywww.bossrefactortf.boss;

public final class TwilightBossSenDimsRulesSelfTest {
    private TwilightBossSenDimsRulesSelfTest() {
    }

    public static void main(String[] args) {
        checkClose(TwilightBossSenDimsRules.MAXIMUM_HEALTH_MODIFIER_AMOUNT,
                5.0, "SenDimS uses a +5 MULTIPLY_TOTAL health modifier");
        checkClose(TwilightBossSenDimsRules.scaledMaximumHealth(100.0),
                600.0, "parryable bosses must have six times maximum health");
        checkClose(TwilightBossSenDimsRules.parriedDamage(12.0),
                60.0, "parried bosses must receive five times damage");
        check(TwilightBossSenDimsRules.isSenDimsHealthModifier(
                        "sbsd.leader.health", 5.0, true),
                "legacy SenDimS health modifiers must be recognized");
        check(!TwilightBossSenDimsRules.isSenDimsHealthModifier(
                        "other.health", 5.0, true),
                "unrelated health modifiers must not suppress scaling");
        checkClose(TwilightBossSenDimsRules.healthAfterScaleRemoval(
                        300.0F, 600.0F, 100.0F),
                50.0, "removing SenDimS must preserve the health fraction");
    }

    private static void checkClose(double actual, double expected, String message) {
        if (Math.abs(actual - expected) >= 1.0E-6) {
            throw new AssertionError(
                    message + ": expected " + expected + ", got " + actual);
        }
    }

        private static void check(boolean condition, String message) {
                if (!condition) {
                        throw new AssertionError(message);
                }
        }
}