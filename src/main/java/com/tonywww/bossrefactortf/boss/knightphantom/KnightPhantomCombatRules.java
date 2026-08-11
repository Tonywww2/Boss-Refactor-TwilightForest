package com.tonywww.bossrefactortf.boss.knightphantom;

public final class KnightPhantomCombatRules {
    public static final double ARENA_RADIUS = 10.0;
    private static final double EPSILON = 1.0E-8;

    private KnightPhantomCombatRules() {
    }

    public record HorizontalPoint(double x, double z) {
    }

    public static int fallenKnightCount(int maximumObserved, int livingKnights) {
        return Math.max(0, maximumObserved - Math.max(0, livingKnights));
    }

    public static double fallenKnightMultiplier(
            int fallenKnights, double bonusPerFallenKnight) {
        return 1.0 + Math.max(0, fallenKnights)
                * Math.max(0.0, bonusPerFallenKnight);
    }

    public static float incomingDamage(
            float amount, boolean fireDamage, double fireMultiplier,
            boolean lastKnight, double lastKnightDamageReduction) {
        double adjusted = Math.max(0.0F, amount);
        if (fireDamage) {
            adjusted *= Math.max(0.0, fireMultiplier);
        }
        if (lastKnight) {
            adjusted *= 1.0 - Math.max(0.0,
                    Math.min(1.0, lastKnightDamageReduction));
        }
        return (float) adjusted;
    }

    public static float chargeDamage(double attackDamage, double chargeMultiplier) {
        return (float) (Math.max(0.0, attackDamage)
                * Math.max(0.0, chargeMultiplier));
    }

    public static int chargeWindupTicks(
            boolean lastKnight, int regularTicks, int lastKnightTicks) {
        return Math.max(0, lastKnight ? lastKnightTicks : regularTicks);
    }

    public static int lastKnightRemainingChargeSegments(
            int currentRemainingSegments, int configuredChargeCount) {
        return Math.max(Math.max(0, currentRemainingSegments),
                Math.max(0, configuredChargeCount - 1));
    }

    public static boolean chargeHandoffReady(
            long currentGameTime, long readyGameTime) {
        return currentGameTime >= readyGameTime;
    }

    public static boolean shouldQueueChargeRoundAfterDeath(
            boolean chargesEnabled, int livingKnights,
            boolean roundInProgress) {
        return chargesEnabled && livingKnights > 0 && !roundInProgress;
    }

    public static boolean shouldMoveToDestination(
            double currentX, double currentY, double currentZ,
            double destinationX, double destinationY, double destinationZ) {
        if (!Double.isFinite(currentX) || !Double.isFinite(currentY)
                || !Double.isFinite(currentZ)
                || !Double.isFinite(destinationX)
                || !Double.isFinite(destinationY)
                || !Double.isFinite(destinationZ)) {
            return false;
        }
        double offsetX = destinationX - currentX;
        double offsetY = destinationY - currentY;
        double offsetZ = destinationZ - currentZ;
        double distanceSquared = offsetX * offsetX
                + offsetY * offsetY + offsetZ * offsetZ;
        return Double.isFinite(distanceSquared) && distanceSquared >= EPSILON;
    }

    public static HorizontalPoint clampToArena(
            double x, double z, double centerX, double centerZ,
            double maximumRadius) {
        double radius = Math.max(0.0, maximumRadius);
        double offsetX = x - centerX;
        double offsetZ = z - centerZ;
        double distanceSquared = offsetX * offsetX + offsetZ * offsetZ;
        if (distanceSquared <= radius * radius || distanceSquared < EPSILON) {
            return new HorizontalPoint(x, z);
        }
        double scale = radius / Math.sqrt(distanceSquared);
        return new HorizontalPoint(
                centerX + offsetX * scale,
                centerZ + offsetZ * scale);
    }

    public static HorizontalPoint straightChargeDestination(
            double startX, double startZ,
            double directionX, double directionZ,
            double maximumDistance,
            double centerX, double centerZ, double arenaRadius) {
        double directionLengthSquared = directionX * directionX
                + directionZ * directionZ;
        if (directionLengthSquared < EPSILON) {
            return clampToArena(
                    startX, startZ, centerX, centerZ, arenaRadius);
        }

        double inverseLength = 1.0 / Math.sqrt(directionLengthSquared);
        double normalizedX = directionX * inverseLength;
        double normalizedZ = directionZ * inverseLength;
        double offsetX = startX - centerX;
        double offsetZ = startZ - centerZ;
        double radius = Math.max(0.0, arenaRadius);
        double projection = offsetX * normalizedX + offsetZ * normalizedZ;
        double discriminant = projection * projection + radius * radius
                - offsetX * offsetX - offsetZ * offsetZ;
        double boundaryDistance = discriminant <= 0.0
                ? 0.0
                : Math.max(0.0, -projection + Math.sqrt(discriminant));
        double travelDistance = Math.min(
                Math.max(0.0, maximumDistance), boundaryDistance);
        return new HorizontalPoint(
                startX + normalizedX * travelDistance,
                startZ + normalizedZ * travelDistance);
    }

    public static HorizontalPoint avoidanceDestination(
            double knightX, double knightZ,
            double playerX, double playerZ,
            double originalX, double originalZ,
            double centerX, double centerZ,
            double avoidanceDistance, double maximumRadius,
            int sideSeed) {
        double distance = Math.max(0.0, avoidanceDistance);
        double awayX = knightX - playerX;
        double awayZ = knightZ - playerZ;
        double awayLengthSquared = awayX * awayX + awayZ * awayZ;
        if (awayLengthSquared < EPSILON) {
            awayX = originalX - playerX;
            awayZ = originalZ - playerZ;
            awayLengthSquared = awayX * awayX + awayZ * awayZ;
        }
        if (awayLengthSquared < EPSILON) {
            awayX = centerX - playerX;
            awayZ = centerZ - playerZ;
            awayLengthSquared = awayX * awayX + awayZ * awayZ;
        }
        if (awayLengthSquared < EPSILON) {
            double angle = sideSeed * Math.PI * 2.0 / 6.0;
            awayX = Math.cos(angle);
            awayZ = Math.sin(angle);
            awayLengthSquared = 1.0;
        }

        double inverseLength = 1.0 / Math.sqrt(awayLengthSquared);
        awayX *= inverseLength;
        awayZ *= inverseLength;
        HorizontalPoint retreat = clampToArena(
                playerX + awayX * distance,
                playerZ + awayZ * distance,
                centerX, centerZ, maximumRadius);
        double unclampedX = playerX + awayX * distance;
        double unclampedZ = playerZ + awayZ * distance;
        if (Math.abs(retreat.x() - unclampedX) < EPSILON
                && Math.abs(retreat.z() - unclampedZ) < EPSILON) {
            return retreat;
        }

        double tangentX = -awayZ;
        double tangentZ = awayX;
        double originalDirectionX = originalX - knightX;
        double originalDirectionZ = originalZ - knightZ;
        double sidePreference = originalDirectionX * tangentX
                + originalDirectionZ * tangentZ;
        if (Math.abs(sidePreference) < EPSILON) {
            sidePreference = (sideSeed & 1) == 0 ? 1.0 : -1.0;
        }
        if (sidePreference < 0.0) {
            tangentX = -tangentX;
            tangentZ = -tangentZ;
        }
        return clampToArena(
                knightX + tangentX * distance,
                knightZ + tangentZ * distance,
                centerX, centerZ, maximumRadius);
    }

    public static boolean movementSegmentReaches(
            double previousX, double previousY, double previousZ,
            double currentX, double currentY, double currentZ,
            double destinationX, double destinationY, double destinationZ,
            double completionRadius) {
        double segmentX = currentX - previousX;
        double segmentY = currentY - previousY;
        double segmentZ = currentZ - previousZ;
        double segmentLengthSquared = segmentX * segmentX
                + segmentY * segmentY + segmentZ * segmentZ;
        double interpolation = 0.0;
        if (segmentLengthSquared >= EPSILON) {
            interpolation = ((destinationX - previousX) * segmentX
                    + (destinationY - previousY) * segmentY
                    + (destinationZ - previousZ) * segmentZ)
                    / segmentLengthSquared;
            interpolation = Math.max(0.0, Math.min(1.0, interpolation));
        }
        double closestX = previousX + segmentX * interpolation;
        double closestY = previousY + segmentY * interpolation;
        double closestZ = previousZ + segmentZ * interpolation;
        double offsetX = destinationX - closestX;
        double offsetY = destinationY - closestY;
        double offsetZ = destinationZ - closestZ;
        double radius = Math.max(0.0, completionRadius);
        return offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ
                <= radius * radius;
    }
}