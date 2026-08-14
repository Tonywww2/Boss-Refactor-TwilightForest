package com.tonywww.bossrefactortf.boss.naga;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;

public final class NagaBodyState {
    final Deque<Vec3> trail = new ArrayDeque<>();
    Vec3 lastHeadPosition;
    double distanceSinceSample;
    double configuredSampleSpacing = Double.NaN;
    double configuredSegmentSpacing = Double.NaN;
    NagaBodyPose previousPose = NagaBodyPose.TRAIL;
    Vec3[] fixedPositions;
    float[] fixedRotations;
    Vec3[] blendStartPositions;
    int blendTicksRemaining;

    public void resetTrail() {
        trail.clear();
        lastHeadPosition = null;
        distanceSinceSample = 0.0;
    }
}