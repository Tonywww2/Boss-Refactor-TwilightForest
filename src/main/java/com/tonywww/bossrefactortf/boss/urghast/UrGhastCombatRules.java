package com.tonywww.bossrefactortf.boss.urghast;

public final class UrGhastCombatRules {
    public static final int MODE_DURATION_TICKS = 400;

    private UrGhastCombatRules() {
    }

    public static boolean shouldTriggerHalfHealthOffensive(
            boolean alreadyTriggered, float health, float maximumHealth) {
        return !alreadyTriggered
                && maximumHealth > 0.0F
                && health < maximumHealth * 0.5F;
    }

    public static boolean shouldSwitchMode(int elapsedTicks) {
        return shouldSwitchMode(elapsedTicks, MODE_DURATION_TICKS);
    }

    public static boolean shouldSwitchMode(
            int elapsedTicks, int modeDurationTicks) {
        return elapsedTicks >= Math.max(1, modeDurationTicks);
    }

    public static boolean crossesMinionSummonThreshold(
            float healthBefore, float healthAfter,
            float maximumHealth, double intervalPercent) {
        if (maximumHealth <= 0.0F || healthAfter >= healthBefore) {
            return false;
        }
        double interval = Math.max(0.1, Math.min(100.0, intervalPercent));
        double beforePercent = Math.max(0.0,
                Math.min(100.0, healthBefore / maximumHealth * 100.0));
        double afterPercent = Math.max(0.0,
                Math.min(100.0, healthAfter / maximumHealth * 100.0));
        int nextThresholdIndex = (int) Math.floor(
                (100.0 - beforePercent) / interval) + 1;
        double nextThreshold = 100.0 - nextThresholdIndex * interval;
        return nextThreshold >= 0.0 && afterPercent <= nextThreshold;
    }

    public static double tantrumIncomingDamage(double incomingDamage) {
        return tantrumIncomingDamage(incomingDamage, 0.75);
    }

    public static double tantrumIncomingDamage(
            double incomingDamage, double damageReduction) {
        double clampedReduction = Math.max(0.0, Math.min(1.0, damageReduction));
        return Math.max(0.0, incomingDamage) * (1.0 - clampedReduction);
    }

    public static double giantFireballDamage(double attackDamage) {
        return giantFireballDamage(attackDamage, 1.5);
    }

    public static double giantFireballDamage(
            double attackDamage, double damageMultiplier) {
        return Math.max(0.0, attackDamage)
                * Math.max(0.0, damageMultiplier);
    }

    public static int giantFireballExtraProjectiles(boolean inTantrum) {
        return inTantrum ? 1 : 0;
    }

    public static double mistDamage(double attackDamage) {
        return mistDamage(attackDamage, 0.2);
    }

    public static double mistDamage(
            double attackDamage, double damageMultiplier) {
        return Math.max(0.0, attackDamage)
                * Math.max(0.0, damageMultiplier);
    }

    public static double fireballDamageScale(
            double distanceFromCenter, double radius) {
        double nonNegativeRadius = Math.max(0.0, radius);
        if (nonNegativeRadius == 0.0) {
            return distanceFromCenter <= 0.0 ? 1.0 : 0.0;
        }
        double normalizedDistance = Math.max(0.0, distanceFromCenter)
                / nonNegativeRadius;
        if (normalizedDistance > 1.0) {
            return 0.0;
        }
        return 1.0 - normalizedDistance * 0.5;
    }

    public static double fireballDamage(
            double attackDamage, double distanceFromCenter, double radius) {
        return giantFireballDamage(attackDamage)
                * fireballDamageScale(distanceFromCenter, radius);
    }

        public static double aabbDistanceFromCenter(
            double deltaX, double deltaY, double deltaZ) {
        return Math.max(Math.abs(deltaX),
            Math.max(Math.abs(deltaY), Math.abs(deltaZ)));
        }

    public static boolean trapAffectsOffensiveMode(
            int trapCounter, int activeStartTick,
            int activeEndTick, double durationMultiplier) {
        int activeDuration = Math.max(0, activeEndTick - activeStartTick);
        int effectiveDuration = (int) Math.ceil(activeDuration
                * Math.max(0.0, Math.min(1.0, durationMultiplier)));
        return trapCounter < activeStartTick + effectiveDuration;
    }

    public static boolean isIntervalPulse(
            int elapsedTicks, int intervalTicks) {
        return (Math.max(0, elapsedTicks) + 1)
                % Math.max(1, intervalTicks) == 0;
    }
}