package com.tonywww.bossrefactortf.boss;

public final class TwilightBossSenDimsRules {
    public static final String SENDIMS_HEALTH_MODIFIER_NAME =
            "sbsd.leader.health";
    public static final double MAXIMUM_HEALTH_MULTIPLIER = 6.0;
    public static final double MAXIMUM_HEALTH_MODIFIER_AMOUNT =
            MAXIMUM_HEALTH_MULTIPLIER - 1.0;
    public static final double PARRIED_DAMAGE_MULTIPLIER = 5.0;

    private TwilightBossSenDimsRules() {
    }

    public static double scaledMaximumHealth(double unscaledMaximumHealth) {
        return Math.max(0.0, unscaledMaximumHealth)
                * MAXIMUM_HEALTH_MULTIPLIER;
    }

    public static double parriedDamage(double incomingDamage) {
        return Math.max(0.0, incomingDamage) * PARRIED_DAMAGE_MULTIPLIER;
    }

    public static boolean isSenDimsHealthModifier(
            String name, double amount, boolean multiplyTotal) {
        return SENDIMS_HEALTH_MODIFIER_NAME.equals(name)
                && Math.abs(amount - MAXIMUM_HEALTH_MODIFIER_AMOUNT) < 1.0E-9
                && multiplyTotal;
    }

    public static float healthAfterScaleRemoval(
            float currentHealth, float scaledMaximumHealth,
            float restoredMaximumHealth) {
        if (scaledMaximumHealth <= 0.0F || restoredMaximumHealth <= 0.0F) {
            return 0.0F;
        }
        double healthFraction = Math.max(0.0,
                Math.min(1.0, currentHealth / scaledMaximumHealth));
        return (float) (restoredMaximumHealth * healthFraction);
    }
}