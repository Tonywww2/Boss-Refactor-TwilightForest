package com.tonywww.bossrefactortf.boss.hydra;

public final class HydraCombatRules {
    private HydraCombatRules() {
    }

    public static boolean isSecondPhase(
            float health, float maximumHealth, double healthFraction) {
        return maximumHealth > 0.0F
                && health < maximumHealth * healthFraction;
    }

    public static HydraSkill ordinarySkill(int randomValue) {
        return switch (Math.floorMod(randomValue, 3)) {
            case 0 -> HydraSkill.BITE;
            case 1 -> HydraSkill.SWEEP;
            default -> HydraSkill.SLAM;
        };
    }

    public static double expandingRadius(
            int elapsedTicks, int durationTicks, double maximumRadius) {
        if (durationTicks <= 0) {
            return Math.max(0.0, maximumRadius);
        }
        double progress = Math.max(0.0, Math.min(
                1.0, elapsedTicks / (double) durationTicks));
        return Math.max(0.0, maximumRadius) * progress;
    }

    public static boolean isInsideCircle(
            double x, double z, double centerX, double centerZ,
            double radius) {
        double deltaX = x - centerX;
        double deltaZ = z - centerZ;
        return deltaX * deltaX + deltaZ * deltaZ <= radius * radius;
    }

    public static boolean isInsideArc(
            double x, double z, double originX, double originZ,
            double directionX, double directionZ,
            double radius, double halfAngleDegrees) {
        double deltaX = x - originX;
        double deltaZ = z - originZ;
        double distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
        if (distanceSquared > radius * radius) {
            return false;
        }
        if (distanceSquared < 1.0E-8) {
            return true;
        }
        double directionLength = Math.sqrt(
                directionX * directionX + directionZ * directionZ);
        if (directionLength < 1.0E-8) {
            return false;
        }
        double dot = (deltaX * directionX + deltaZ * directionZ)
                / (Math.sqrt(distanceSquared) * directionLength);
        double threshold = Math.cos(Math.toRadians(
                Math.max(0.0, Math.min(180.0, halfAngleDegrees))));
        return dot + 1.0E-8 >= threshold;
    }
}