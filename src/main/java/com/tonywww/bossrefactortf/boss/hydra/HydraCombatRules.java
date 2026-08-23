package com.tonywww.bossrefactortf.boss.hydra;

import net.minecraft.world.phys.Vec3;

public final class HydraCombatRules {
    private HydraCombatRules() {
    }

    public static boolean isSecondPhase(
            float health, float maximumHealth, double healthFraction) {
        return maximumHealth > 0.0F
                && health < maximumHealth * healthFraction;
    }

    public static float phaseDamage(
            float baseDamage, boolean secondPhase, float multiplier) {
        return secondPhase ? baseDamage * multiplier : baseDamage;
    }

    public static float fireLordMultiplier(
            float baseMultiplier, float perHeadMultiplier,
            int activeHeadCount) {
        return baseMultiplier + perHeadMultiplier * activeHeadCount;
    }

    public static float physicalFireDamage(
            float totalDamage, double physicalFraction) {
        return (float) (Math.max(0.0F, totalDamage)
                * Math.max(0.0, Math.min(1.0, physicalFraction)));
    }

    public static float elementalFireDamage(
            float totalDamage, double physicalFraction) {
        return Math.max(0.0F, totalDamage)
                - physicalFireDamage(totalDamage, physicalFraction);
    }

    public static HydraSkill customMeleeSkill(int randomValue) {
        return Math.floorMod(randomValue, 2) == 0
                ? HydraSkill.SWEEP
                : HydraSkill.SLAM;
    }

    public static Vec3 turnToward(
            Vec3 currentDirection, Vec3 desiredDirection,
            double maximumTurnDegrees) {
        if (!isFiniteDirection(desiredDirection)
                || desiredDirection.lengthSqr() < 1.0E-8) {
            return normalizedOrDefault(currentDirection);
        }
        Vec3 current = normalizedOrDefault(currentDirection);
        Vec3 desired = desiredDirection.normalize();
        double dot = Math.max(-1.0, Math.min(1.0, current.dot(desired)));
        double angle = Math.acos(dot);
        double maximumTurn = Math.toRadians(
                Math.max(0.0, Math.min(180.0, maximumTurnDegrees)));
        if (angle <= maximumTurn || angle < 1.0E-8) {
            return desired;
        }
        double sine = Math.sin(angle);
        if (Math.abs(sine) < 1.0E-6) {
            Vec3 perpendicular = current.cross(new Vec3(0.0, 1.0, 0.0));
            if (perpendicular.lengthSqr() < 1.0E-8) {
                perpendicular = current.cross(new Vec3(1.0, 0.0, 0.0));
            }
            return current.scale(Math.cos(maximumTurn))
                    .add(perpendicular.normalize().scale(Math.sin(maximumTurn)))
                    .normalize();
        }
        double remainingWeight = Math.sin(angle - maximumTurn) / sine;
        double desiredWeight = Math.sin(maximumTurn) / sine;
        return current.scale(remainingWeight)
                .add(desired.scale(desiredWeight))
                .normalize();
    }

    private static Vec3 normalizedOrDefault(Vec3 direction) {
        return isFiniteDirection(direction) && direction.lengthSqr() >= 1.0E-8
                ? direction.normalize()
                : new Vec3(0.0, 0.0, 1.0);
    }

    private static boolean isFiniteDirection(Vec3 direction) {
        return Double.isFinite(direction.x)
                && Double.isFinite(direction.y)
                && Double.isFinite(direction.z);
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