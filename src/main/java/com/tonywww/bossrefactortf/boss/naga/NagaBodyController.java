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
            NagaBodyPose pose, float poseProgress,
            double lockedDirectionX, double lockedDirectionZ) {
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
        } else if (pose == NagaBodyPose.TAIL_WINDUP
                || pose == NagaBodyPose.TAIL_STRIKE) {
            double curveDegrees = BossRefactorTwilightForestConfig.NAGA
                .bodyTailCurveDegrees.get();
            Vec3[] startTargets = curvedTailTargets(
                    naga.position(), trailTargets, segmentSpacing,
                lockedDirectionX, lockedDirectionZ, curveDegrees);
            if (pose == NagaBodyPose.TAIL_WINDUP) {
            targets = constrainSpacing(naga.position(), lerpTargets(
                trailTargets, startTargets, poseProgress),
                segmentSpacing);
            } else {
            Vec3[] frontTargets = straightTailTargets(
                naga.position(), trailTargets, segmentSpacing,
                lockedDirectionX, lockedDirectionZ);
            Vec3[] endTargets = curvedTailTargets(
                naga.position(), trailTargets, segmentSpacing,
                lockedDirectionX, lockedDirectionZ, -curveDegrees);
            targets = constrainSpacing(naga.position(), weightedTargets(
                startTargets, frontTargets, endTargets,
                NagaBodyRules.tailPoseWeights(poseProgress)),
                segmentSpacing);
            }
            state.blendTicksRemaining = 0;
            state.blendStartPositions = null;
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

    private static Vec3[] curvedTailTargets(
            Vec3 head, Vec3[] trailTargets, double segmentSpacing,
            double directionX, double directionZ, double bendDegrees) {
        Vec3 forward = new Vec3(directionX, 0.0, directionZ);
        if (forward.lengthSqr() < EPSILON) {
            forward = new Vec3(0.0, 0.0, 1.0);
        }
        forward = forward.normalize();
        double forwardAngle = Math.atan2(forward.z, forward.x);
        Vec3[] targets = new Vec3[trailTargets.length];
        Vec3 previous = head;
        double previousTrailY = head.y;
        for (int index = 0; index < targets.length; index++) {
            double alongBody = (double) (index + 1) / targets.length;
            double angle = forwardAngle + Math.PI
                    + Math.toRadians(bendDegrees) * alongBody;
            double targetY = previous.y
                    + trailTargets[index].y - previousTrailY;
            targets[index] = new Vec3(
                    previous.x + Math.cos(angle) * segmentSpacing,
                    targetY,
                    previous.z + Math.sin(angle) * segmentSpacing);
            previous = targets[index];
            previousTrailY = trailTargets[index].y;
        }
        return targets;
    }

    private static Vec3[] straightTailTargets(
            Vec3 head, Vec3[] trailTargets, double segmentSpacing,
            double directionX, double directionZ) {
        Vec3 forward = horizontalDirection(directionX, directionZ);
        Vec3[] targets = new Vec3[trailTargets.length];
        for (int index = 0; index < targets.length; index++) {
            targets[index] = head.add(forward.scale(
                    segmentSpacing * (index + 1))).multiply(
                            1.0, 0.0, 1.0).add(
                                    0.0, trailTargets[index].y, 0.0);
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

    private static Vec3[] lerpTargets(
            Vec3[] start, Vec3[] end, double progress) {
        Vec3[] targets = new Vec3[start.length];
        double clampedProgress = Mth.clamp(progress, 0.0, 1.0);
        for (int index = 0; index < targets.length; index++) {
            targets[index] = start[index].lerp(end[index], clampedProgress);
        }
        return targets;
    }

    private static Vec3[] weightedTargets(
            Vec3[] start, Vec3[] front, Vec3[] end,
            NagaBodyRules.TailPoseWeights weights) {
        Vec3[] targets = new Vec3[start.length];
        for (int index = 0; index < targets.length; index++) {
            targets[index] = start[index].scale(weights.start())
                    .add(front[index].scale(weights.front()))
                    .add(end[index].scale(weights.end()));
        }
        return targets;
    }

    private static Vec3[] constrainSpacing(
            Vec3 head, Vec3[] targets, double segmentSpacing) {
        Vec3 previous = head;
        for (int index = 0; index < targets.length; index++) {
            Vec3 offset = targets[index].subtract(previous);
            if (offset.lengthSqr() < EPSILON) {
                offset = index == 0
                        ? new Vec3(0.0, 0.0, 1.0)
                        : previous.subtract(targets[index - 1]);
            }
            targets[index] = previous.add(
                    offset.normalize().scale(segmentSpacing));
            previous = targets[index];
        }
        return targets;
    }

    private static Vec3 horizontalDirection(
            double directionX, double directionZ) {
        Vec3 direction = new Vec3(directionX, 0.0, directionZ);
        return direction.lengthSqr() < EPSILON
                ? new Vec3(0.0, 0.0, 1.0)
                : direction.normalize();
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