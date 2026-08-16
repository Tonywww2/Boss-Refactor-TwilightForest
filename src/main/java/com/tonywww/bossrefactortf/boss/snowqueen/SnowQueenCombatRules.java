package com.tonywww.bossrefactortf.boss.snowqueen;

public final class SnowQueenCombatRules {
    private SnowQueenCombatRules() {
    }

    public static int cruiseTicks(int minimum, int maximum, double random) {
        int lower = Math.max(0, Math.min(minimum, maximum));
        int upper = Math.max(lower, Math.max(minimum, maximum));
        return lower + (int) Math.floor(
                Math.max(0.0, Math.min(Math.nextDown(1.0), random))
                        * (upper - lower + 1));
    }

    public static SnowQueenSkill chooseSkill(
            SnowQueenSkill previous, boolean avoidRepeat, boolean chooseRain) {
        SnowQueenSkill selected = chooseRain
                ? SnowQueenSkill.SHATTERED_ICE_RAIN
                : SnowQueenSkill.REPEATED_SLAM;
        if (avoidRepeat && selected == previous) {
            return selected == SnowQueenSkill.REPEATED_SLAM
                    ? SnowQueenSkill.SHATTERED_ICE_RAIN
                    : SnowQueenSkill.REPEATED_SLAM;
        }
        return selected;
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
}