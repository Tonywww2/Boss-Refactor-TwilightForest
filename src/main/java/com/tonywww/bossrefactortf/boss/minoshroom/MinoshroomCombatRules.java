package com.tonywww.bossrefactortf.boss.minoshroom;

public final class MinoshroomCombatRules {
    private static final String STOMP_DAMAGE_TYPE_PATH = "minoshroom_stomp";

    private MinoshroomCombatRules() {
    }

    public static boolean isSecondPhase(
            float health, float maximumHealth, double healthFraction) {
        return maximumHealth > 0.0F
                && health < maximumHealth * clamp(healthFraction, 0.0, 1.0);
    }

    public static int windupTicks(
            int configuredTicks, boolean secondPhase, double phaseMultiplier) {
        int ticks = Math.max(0, configuredTicks);
        return secondPhase
                ? (int) Math.ceil(ticks * Math.max(0.0, phaseMultiplier))
                : ticks;
    }

    public static double skillDamage(
            double attackDamage, double skillMultiplier,
            boolean secondPhase, double phaseMultiplier) {
        double damage = Math.max(0.0, attackDamage)
                * Math.max(0.0, skillMultiplier);
        return secondPhase ? damage * Math.max(0.0, phaseMultiplier) : damage;
    }

    public static double navigationSpeed(
            double configuredSpeed, boolean secondPhase,
            double phaseMultiplier) {
        double speed = Math.max(0.0, configuredSpeed);
        return secondPhase ? speed * Math.max(0.0, phaseMultiplier) : speed;
    }

    public static double reducedDamage(
            double amount, double sourceDistance,
            double reductionRadius, double reductionFraction) {
        double nonNegativeAmount = Math.max(0.0, amount);
        if (sourceDistance <= Math.max(0.0, reductionRadius)) {
            return nonNegativeAmount;
        }
        return nonNegativeAmount
                * (1.0 - clamp(reductionFraction, 0.0, 1.0));
    }

    public static int cruiseTicks(int minimumTicks, int maximumTicks, double roll) {
        int minimum = Math.max(0, minimumTicks);
        int maximum = Math.max(minimum, maximumTicks);
        double clampedRoll = clamp(roll, 0.0, Math.nextDown(1.0));
        return minimum + (int) Math.floor(
                clampedRoll * (maximum - minimum + 1));
    }

    public static boolean shouldUseStomp(
            double targetDistance, double stompRadius) {
        return Math.max(0.0, targetDistance) <= Math.max(0.0, stompRadius);
    }

    public static boolean shouldDamageWithStomp(
            boolean targetOnGround, double targetDistance,
            double stompRadius) {
        return targetOnGround && shouldUseStomp(targetDistance, stompRadius);
    }

    public static String stompDamageTypePath() {
        return STOMP_DAMAGE_TYPE_PATH;
    }

    public static boolean shouldRepeatSweep(
            int completedRounds, int maximumRepeats,
            boolean secondPhase, int secondPhaseMinimumRounds,
            double repeatProbability, double roll) {
        int completed = Math.max(0, completedRounds);
        int maximumRounds = Math.max(1, Math.max(0, maximumRepeats) + 1);
        if (completed >= maximumRounds) {
            return false;
        }
        int minimumRounds = secondPhase
                ? Math.max(1, secondPhaseMinimumRounds)
                : 1;
        if (completed < Math.min(minimumRounds, maximumRounds)) {
            return true;
        }
        return clamp(roll, 0.0, Math.nextDown(1.0))
                < clamp(repeatProbability, 0.0, 1.0);
    }

    public static int stompCount(
            boolean secondPhase, int secondPhaseCount) {
        return secondPhase ? Math.max(1, secondPhaseCount) : 1;
    }

    public static double halfAngleDegrees(double fullArcDegrees) {
        return clamp(fullArcDegrees, 0.0, 360.0) * 0.5;
    }

    public static boolean isInsideArc(
            double originX, double originZ,
            double directionX, double directionZ,
            double targetX, double targetZ,
            double radius, double halfAngleDegrees) {
        double offsetX = targetX - originX;
        double offsetZ = targetZ - originZ;
        double distanceSquared = offsetX * offsetX + offsetZ * offsetZ;
        double nonNegativeRadius = Math.max(0.0, radius);
        if (distanceSquared > nonNegativeRadius * nonNegativeRadius) {
            return false;
        }
        if (distanceSquared <= 1.0E-12) {
            return true;
        }
        double directionLength = Math.sqrt(
                directionX * directionX + directionZ * directionZ);
        if (directionLength <= 1.0E-12) {
            return false;
        }
        double targetLength = Math.sqrt(distanceSquared);
        double dot = (offsetX * directionX + offsetZ * directionZ)
                / (targetLength * directionLength);
        double minimumDot = Math.cos(Math.toRadians(
                clamp(halfAngleDegrees, 0.0, 180.0)));
        return dot + 1.0E-12 >= minimumDot;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}