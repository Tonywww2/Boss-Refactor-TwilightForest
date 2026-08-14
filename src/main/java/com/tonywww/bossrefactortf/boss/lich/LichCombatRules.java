package com.tonywww.bossrefactortf.boss.lich;

public final class LichCombatRules {
    public record ShieldBashHitDecision(
            boolean dealsDamage, boolean appliesKnockback) {
    }

    private LichCombatRules() {
    }

    public static boolean shouldEnterSecondPhase(
            boolean secondPhase, boolean shieldJustBroke) {
        return !secondPhase && shieldJustBroke;
    }

    public static boolean shouldEnterSecondPhase(
            boolean secondPhase, boolean customClone,
            int previousShieldLayers, int currentShieldLayers) {
        return !customClone && !secondPhase
                && previousShieldLayers >= 0
                && currentShieldLayers <= 0;
    }

    public static double shieldDamageMultiplier(
            int shieldLayers, double reductionPerLayer) {
        double reduction = Math.max(0.0, reductionPerLayer)
                * Math.max(0, shieldLayers);
        return 1.0 - Math.min(1.0, reduction);
    }

    public static double damageAfterShieldReduction(
            double damage, int shieldLayers, double reductionPerLayer) {
        return Math.max(0.0, damage)
                * shieldDamageMultiplier(shieldLayers, reductionPerLayer);
    }

    public static double meleeMagicDamage(
            double attackDamage, double multiplier) {
        return Math.max(0.0, attackDamage) * Math.max(0.0, multiplier);
    }

    public static double shieldBashDamage(
            double attackDamage, double damageMultiplier,
            double shieldMultiplier, boolean empowered) {
        double damage = Math.max(0.0, attackDamage)
                * Math.max(0.0, damageMultiplier);
        return empowered ? damage * Math.max(0.0, shieldMultiplier) : damage;
    }

        public static double shieldBashDamage(
                        double attackDamage, double damageMultiplier,
                        boolean empowered, double shieldMultiplier) {
                return shieldBashDamage(
                                attackDamage, damageMultiplier,
                                shieldMultiplier, empowered);
        }

        public static ShieldBashHitDecision shieldBashHitDecision(
                        boolean shieldBlocked, boolean parried) {
                return new ShieldBashHitDecision(
                                !shieldBlocked && !parried, true);
        }

    public static int cruiseTicks(int minimum, int maximum, double roll) {
        int lower = Math.max(1, Math.min(minimum, maximum));
        int upper = Math.max(lower, Math.max(minimum, maximum));
        double clamped = Math.max(0.0, Math.min(Math.nextDown(1.0), roll));
        return lower + (int) Math.floor(clamped * (upper - lower + 1));
    }

    public static boolean shouldSummonClone(
            boolean secondPhase, boolean customClone,
            boolean hasLivingClone, int summonCooldown) {
        return secondPhase && !customClone
                && !hasLivingClone && summonCooldown <= 0;
    }

    public static boolean isCurrentAttackParryable(
            LichActionState actionState) {
        return actionState == LichActionState.SHIELD_BASH_WINDUP;
    }

    public static boolean intersectsLockedCorridor(
            double targetX, double targetMinY, double targetMaxY,
            double targetZ, double targetWidth,
            double originX, double originY, double originZ,
            double directionX, double directionZ,
            double length, double halfWidth, double height) {
        double directionLength = Math.hypot(directionX, directionZ);
        if (directionLength < 1.0E-8) {
            return false;
        }
        double normalizedX = directionX / directionLength;
        double normalizedZ = directionZ / directionLength;
        double deltaX = targetX - originX;
        double deltaZ = targetZ - originZ;
        double forward = deltaX * normalizedX + deltaZ * normalizedZ;
        double sideways = Math.abs(
                deltaX * -normalizedZ + deltaZ * normalizedX);
        double targetRadius = Math.max(0.0, targetWidth) * 0.5
                * (Math.abs(normalizedX) + Math.abs(normalizedZ));
        double corridorLength = Math.max(0.0, length);
        double corridorHalfWidth = Math.max(0.0, halfWidth);
        double corridorHeight = Math.max(0.0, height);
        return forward + targetRadius >= 0.0
                && forward - targetRadius <= corridorLength
                && sideways <= corridorHalfWidth + targetRadius
                && targetMaxY >= originY
                && targetMinY <= originY + corridorHeight;
    }

        public static boolean isInsideShieldBashRectangle(
                        double targetX, double targetY, double targetZ,
                        double originX, double originY, double originZ,
                        double directionX, double directionZ,
                        double length, double halfWidth, double height) {
                return intersectsLockedCorridor(
                                targetX, targetY, targetY, targetZ, 0.0,
                                originX, originY, originZ,
                                directionX, directionZ, length, halfWidth, height);
        }
}