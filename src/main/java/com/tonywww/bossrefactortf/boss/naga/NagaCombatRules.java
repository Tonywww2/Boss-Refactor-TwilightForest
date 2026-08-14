package com.tonywww.bossrefactortf.boss.naga;

import java.util.ArrayList;
import java.util.List;

public final class NagaCombatRules {
    private NagaCombatRules() {
    }

    public static boolean isStunned(NagaActionState actionState) {
        return actionState == NagaActionState.STUNNED;
    }

    public static boolean shouldEnterStunnedState(
            boolean secondPhase, boolean stunImmunityAvailable) {
        return !secondPhase || !stunImmunityAvailable;
    }

    public static boolean isSecondPhase(
            float health, float maximumHealth, double threshold) {
        return maximumHealth > 0.0F && health / maximumHealth < threshold;
    }

    public static boolean suppressesExternalParryStun(
            NagaActionState actionState, boolean secondPhase,
            boolean stunImmunityAvailable) {
        return switch (actionState) {
            case VENOM_WINDUP, VENOM_PROJECTILE -> true;
            case POUNCE_WINDUP, POUNCE_FLIGHT, POUNCE_IMPACT,
                    TAIL_WINDUP, TAIL_STRIKE -> secondPhase
                            && stunImmunityAvailable;
            default -> false;
        };
    }

    public static int cruiseTicks(int minimum, int maximum, double roll) {
        int lower = Math.max(1, Math.min(minimum, maximum));
        int upper = Math.max(lower, Math.max(minimum, maximum));
        double clamped = Math.max(0.0, Math.min(Math.nextDown(1.0), roll));
        return lower + (int) Math.floor(clamped * (upper - lower + 1));
    }

    public static NagaSkill nextSkill(
            NagaSkill previous, boolean pounceReady,
            boolean tailReady, boolean venomReady, int roll) {
        List<NagaSkill> choices = new ArrayList<>(3);
        addIfReady(choices, NagaSkill.POUNCE, previous, pounceReady);
        addIfReady(choices, NagaSkill.TAIL_SWEEP, previous, tailReady);
        addIfReady(choices, NagaSkill.VENOM_BREATH, previous, venomReady);
        if (choices.isEmpty()) {
            return NagaSkill.NONE;
        }
        return choices.get(Math.floorMod(roll, choices.size()));
    }

    public static boolean isInsideCircle(
            double pointX, double pointZ,
            double centerX, double centerZ, double radius) {
        double deltaX = pointX - centerX;
        double deltaZ = pointZ - centerZ;
        return deltaX * deltaX + deltaZ * deltaZ <= radius * radius;
    }

    public static boolean isInsideArc(
            double pointX, double pointZ,
            double originX, double originZ,
            double directionX, double directionZ,
            double radius, double halfAngleDegrees) {
        double deltaX = pointX - originX;
        double deltaZ = pointZ - originZ;
        double distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
        if (distanceSquared > radius * radius || distanceSquared == 0.0) {
            return distanceSquared == 0.0;
        }
        double directionLength = Math.hypot(directionX, directionZ);
        if (directionLength == 0.0) {
            return false;
        }
        double dot = (deltaX * directionX + deltaZ * directionZ)
                / (Math.sqrt(distanceSquared) * directionLength);
        double minimumDot = Math.cos(Math.toRadians(halfAngleDegrees));
        return dot + 1.0E-12 >= minimumDot;
    }

    public static boolean isInsideCorridor(
            double pointX, double pointZ,
            double originX, double originZ,
            double directionX, double directionZ,
            double length, double fullWidth) {
        double directionLength = Math.hypot(directionX, directionZ);
        if (directionLength == 0.0) {
            return false;
        }
        double normalizedX = directionX / directionLength;
        double normalizedZ = directionZ / directionLength;
        double deltaX = pointX - originX;
        double deltaZ = pointZ - originZ;
        double forward = deltaX * normalizedX + deltaZ * normalizedZ;
        double sideways = Math.abs(deltaX * -normalizedZ + deltaZ * normalizedX);
        return forward >= 0.0 && forward <= length
                && sideways <= fullWidth * 0.5;
    }

    public static double shrinkingOrbitRadius(
            int elapsedTicks, int durationTicks,
            double startRadius, double endRadius) {
        double progress = durationTicks <= 0 ? 1.0
                : Math.max(0.0, Math.min(1.0,
                        elapsedTicks / (double) durationTicks));
        return startRadius + (endRadius - startRadius) * progress;
    }

        public static HorizontalPoint blockedCloudCenter(
            double targetCenterX, double targetCenterZ,
            double targetHalfSizeX, double targetHalfSizeZ,
            double directionX, double directionZ, double offset) {
        double length = Math.hypot(directionX, directionZ);
        if (length == 0.0) {
            return new HorizontalPoint(targetCenterX, targetCenterZ);
        }
        double normalizedX = directionX / length;
        double normalizedZ = directionZ / length;
        double shieldSurfaceDistanceX = Math.abs(normalizedX) < 1.0E-12
            ? Double.POSITIVE_INFINITY
            : Math.max(0.0, targetHalfSizeX) / Math.abs(normalizedX);
        double shieldSurfaceDistanceZ = Math.abs(normalizedZ) < 1.0E-12
            ? Double.POSITIVE_INFINITY
            : Math.max(0.0, targetHalfSizeZ) / Math.abs(normalizedZ);
        double shieldSurfaceDistance = Math.min(
            shieldSurfaceDistanceX, shieldSurfaceDistanceZ);
        double safeOffset = Math.max(0.0, offset);
        return new HorizontalPoint(
            targetCenterX - normalizedX
                * (shieldSurfaceDistance + safeOffset),
            targetCenterZ - normalizedZ
                * (shieldSurfaceDistance + safeOffset));
    }

    public static float normalizeColorChannel(int channel) {
        return Math.max(0, Math.min(255, channel)) / 255.0F;
    }

    public static boolean isFireSnakeVisualActive(int fireSnakeTicks) {
        return fireSnakeTicks > 0;
    }

    public record HorizontalPoint(double x, double z) {
    }

    private static void addIfReady(
            List<NagaSkill> choices, NagaSkill skill,
            NagaSkill previous, boolean ready) {
        if (ready && skill != previous) {
            choices.add(skill);
        }
    }
}