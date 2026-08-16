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
        check(TwilightBossLeaderProfilePolicy.EXTERNAL_ONLY.accepts(
                        TwilightBossLeaderProfile.EXTERNAL),
                "strict boss policies must accept the External profile");
        check(!TwilightBossLeaderProfilePolicy.EXTERNAL_ONLY.accepts(
                        TwilightBossLeaderProfile.MANAGED),
                "strict boss policies must reject the Managed profile");
        check(TwilightBossLeaderProfilePolicy.MANAGED_OR_EXTERNAL.accepts(
                        TwilightBossLeaderProfile.MANAGED),
                "the Minoshroom migration policy must accept Managed");
        check(TwilightBossLeaderProfilePolicy.MANAGED_OR_EXTERNAL.accepts(
                        TwilightBossLeaderProfile.EXTERNAL),
                "the Minoshroom migration policy must accept External");
        check(!TwilightBossLeaderProfilePolicy.MANAGED_OR_EXTERNAL.accepts(
                        TwilightBossLeaderProfile.NONE),
                "the migration policy must still require Leader activation");
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