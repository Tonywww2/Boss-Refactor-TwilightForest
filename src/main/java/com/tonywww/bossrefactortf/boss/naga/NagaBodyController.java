package com.tonywww.bossrefactortf.boss.naga;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.boss.Naga;
import twilightforest.entity.boss.NagaSegment;

import java.util.ArrayList;
import java.util.List;

public final class NagaBodyController {
    private static final double EPSILON = 1.0E-9;

    private NagaBodyController() {
    }

    public static void tick(
            Naga naga, NagaSegment[] segments, NagaBodyState state,
            NagaBodyPose pose) {
        for (NagaSegment segment : segments) {
            if (segment != null) {
                segment.tick();
            }
        }

        double segmentSpacing = BossRefactorTwilightForestConfig.NAGA
                .bodySegmentSpacing.get();
        double sampleSpacing = BossRefactorTwilightForestConfig.NAGA
                .bodyTrailSampleSpacing.get();
        boolean returningToTrail = pose == NagaBodyPose.TRAIL
                && state.previousPose != NagaBodyPose.TRAIL;
        if (returningToTrail
                || Double.compare(sampleSpacing,
                        state.configuredSampleSpacing) != 0
                || Double.compare(segmentSpacing,
                        state.configuredSegmentSpacing) != 0) {
            state.resetTrail();
            state.configuredSampleSpacing = sampleSpacing;
            state.configuredSegmentSpacing = segmentSpacing;
        }

        updateTrail(naga, segments.length, state,
                segmentSpacing, sampleSpacing);
        Vec3[] trailTargets = trailTargets(
                naga.position(), segments.length, state, segmentSpacing);
        if (returningToTrail) {
            state.blendStartPositions = currentPositions(segments);
            state.blendTicksRemaining = BossRefactorTwilightForestConfig.NAGA
                    .bodyPoseBlendTicks.get();
        }

        Vec3[] targets;
        if (pose == NagaBodyPose.FROZEN) {
            if (state.previousPose != NagaBodyPose.FROZEN
                    || state.fixedPositions == null
                    || state.fixedPositions.length != segments.length) {
                state.fixedPositions = currentPositions(segments);
                state.fixedRotations = currentRotations(segments);
            }
            targets = state.fixedPositions;
        } else {
            targets = blendTrailTargets(state, trailTargets);
        }

        applyTargets(naga.position(), segments, targets,
                pose == NagaBodyPose.FROZEN ? state.fixedRotations : null);
        state.previousPose = pose;
    }

    private static void updateTrail(
            Naga naga, int segmentCount, NagaBodyState state,
            double segmentSpacing, double sampleSpacing) {
        Vec3 head = naga.position();
        int capacity = NagaBodyRules.trailCapacity(
                segmentCount, segmentSpacing, sampleSpacing);
        double resetDistance = BossRefactorTwilightForestConfig.NAGA
                .bodyTrailTeleportResetDistance.get();
        if (state.lastHeadPosition == null
                || NagaBodyRules.shouldResetTrail(
                        state.lastHeadPosition.distanceToSqr(head),
                        resetDistance)) {
            initializeTrail(naga, state, head, capacity, sampleSpacing);
            return;
        }

        Vec3 start = state.lastHeadPosition;
        double stepLength = start.distanceTo(head);
        if (stepLength > EPSILON) {
            double traveled = 0.0;
            double needed = sampleSpacing - state.distanceSinceSample;
            while (stepLength - traveled + EPSILON >= needed) {
                traveled += needed;
                state.trail.addFirst(start.lerp(head, traveled / stepLength));
                state.distanceSinceSample = 0.0;
                needed = sampleSpacing;
            }
            state.distanceSinceSample += stepLength - traveled;
        }
        state.lastHeadPosition = head;
        trimTrail(state, capacity);
    }

    private static void initializeTrail(
            Naga naga, NagaBodyState state, Vec3 head,
            int capacity, double sampleSpacing) {
        state.resetTrail();
        Vec3 forward = naga.getLookAngle().multiply(1.0, 0.0, 1.0);
        if (forward.lengthSqr() < EPSILON) {
            float yaw = naga.getYRot() * Mth.DEG_TO_RAD;
            forward = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
        }
        forward = forward.normalize();
        for (int index = 0; index < capacity; index++) {
            state.trail.addLast(head.subtract(
                    forward.scale(sampleSpacing * index)));
        }
        state.lastHeadPosition = head;
        state.distanceSinceSample = 0.0;
    }

    private static Vec3[] trailTargets(
            Vec3 head, int segmentCount, NagaBodyState state,
            double segmentSpacing) {
        List<NagaBodyRules.Point> points = new ArrayList<>(state.trail.size());
        for (Vec3 point : state.trail) {
            points.add(new NagaBodyRules.Point(point.x, point.y, point.z));
        }
        NagaBodyRules.Point headPoint = new NagaBodyRules.Point(
                head.x, head.y, head.z);
        Vec3[] targets = new Vec3[segmentCount];
        for (int index = 0; index < segmentCount; index++) {
            NagaBodyRules.Point target = NagaBodyRules.sampleTrail(
                    headPoint, points, segmentSpacing * (index + 1));
            targets[index] = new Vec3(target.x(), target.y(), target.z());
        }
        return targets;
    }

    private static Vec3[] blendTrailTargets(
            NagaBodyState state, Vec3[] trailTargets) {
        if (state.blendTicksRemaining <= 0
                || state.blendStartPositions == null
                || state.blendStartPositions.length != trailTargets.length) {
            state.blendStartPositions = null;
            state.blendTicksRemaining = 0;
            return trailTargets;
        }
        int duration = Math.max(1, BossRefactorTwilightForestConfig.NAGA
                .bodyPoseBlendTicks.get());
        double progress = 1.0
                - (double) state.blendTicksRemaining / duration;
        Vec3[] targets = new Vec3[trailTargets.length];
        for (int index = 0; index < targets.length; index++) {
            targets[index] = state.blendStartPositions[index]
                    .lerp(trailTargets[index], Mth.clamp(progress, 0.0, 1.0));
        }
        state.blendTicksRemaining--;
        return targets;
    }

    private static void applyTargets(
            Vec3 head, NagaSegment[] segments, Vec3[] targets,
            float[] fixedRotations) {
        for (int index = 0; index < segments.length; index++) {
            NagaSegment segment = segments[index];
            if (segment == null) {
                continue;
            }
            Vec3 target = targets[index];
            segment.setPos(target.x, target.y, target.z);
            float yaw = fixedRotations != null
                    ? fixedRotations[index]
                    : targetYaw(target,
                            index == 0 ? head : targets[index - 1]);
            segment.setYRot(yaw);
        }
    }

    private static float targetYaw(Vec3 target, Vec3 precedingTarget) {
        Vec3 direction = precedingTarget.subtract(target);
        return (float) (Mth.atan2(direction.z, direction.x)
                * Mth.RAD_TO_DEG) - 90.0F;
    }

    private static Vec3[] currentPositions(NagaSegment[] segments) {
        Vec3[] positions = new Vec3[segments.length];
        for (int index = 0; index < segments.length; index++) {
            positions[index] = segments[index] == null
                    ? Vec3.ZERO
                    : segments[index].position();
        }
        return positions;
    }

    private static float[] currentRotations(NagaSegment[] segments) {
        float[] rotations = new float[segments.length];
        for (int index = 0; index < segments.length; index++) {
            rotations[index] = segments[index] == null
                    ? 0.0F
                    : segments[index].getYRot();
        }
        return rotations;
    }

    private static void trimTrail(NagaBodyState state, int capacity) {
        while (state.trail.size() > capacity) {
            state.trail.removeLast();
        }
    }
}