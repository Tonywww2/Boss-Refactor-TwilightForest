package com.tonywww.bossrefactortf.boss.snowqueen;

public final class SnowQueenCombatRules {
    private SnowQueenCombatRules() {
    }

    public static int shieldCount(int configuredCount, int capacity) {
        return Math.max(0, Math.min(configuredCount, capacity));
    }

    public static double shieldAngleDegrees(
            int index, int count, int tickCount, double degreesPerTick) {
        if (count <= 0) {
            return 0.0;
        }
        return 360.0 * index / count + degreesPerTick * tickCount;
    }

    public static double shieldWaveOffset(
            int index, int count, int tickCount, double amplitude,
            double degreesPerTick) {
        if (count <= 0) {
            return 0.0;
        }
        double angle = degreesPerTick * tickCount + 360.0 * index / count;
        return Math.sin(Math.toRadians(angle)) * amplitude;
    }

    public static boolean isSecondPhase(
            float health, float maximumHealth, double threshold) {
        return maximumHealth > 0.0F && health / maximumHealth <= threshold;
    }

    public static boolean isInsideCircle(
            double x, double z, double centerX, double centerZ,
            double radius) {
        double offsetX = x - centerX;
        double offsetZ = z - centerZ;
        return offsetX * offsetX + offsetZ * offsetZ <= radius * radius;
    }

    public static float scaledDamage(double attackDamage, double multiplier) {
        return (float) Math.max(0.0, attackDamage * multiplier);
    }

    public static int fallingIceCooldownBeforeWindup(
            int impactIntervalTicks, int windupTicks) {
        return Math.max(1,
                Math.max(1, impactIntervalTicks) - Math.max(0, windupTicks));
    }
}