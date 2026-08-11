package com.tonywww.bossrefactortf.boss.urghast;

public final class UrGhastCombatRules {
    public static final int MODE_DURATION_TICKS = 400;
    public static final int PATROL_POINTS_PER_MODE = 4;

    private UrGhastCombatRules() {
    }

    public static boolean offensiveModeUnlocked(
            float health, float maximumHealth) {
        return maximumHealth > 0.0F && health < maximumHealth * 0.8F;
    }

    public static boolean shouldSwitchMode(
            int elapsedTicks, int visitedPatrolPoints) {
        return shouldSwitchMode(
                elapsedTicks, visitedPatrolPoints,
                MODE_DURATION_TICKS, PATROL_POINTS_PER_MODE);
    }

    public static boolean shouldSwitchMode(
            int elapsedTicks, int visitedPatrolPoints,
            int modeDurationTicks, int patrolPointsPerMode) {
        return elapsedTicks >= Math.max(1, modeDurationTicks)
                || visitedPatrolPoints >= Math.max(1, patrolPointsPerMode);
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