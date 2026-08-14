package com.tonywww.bossrefactortf.boss.naga;

import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public final class NagaCombatRulesSelfTest {
    private NagaCombatRulesSelfTest() {
    }

    public static void main(String[] args) {
        check(!NagaCombatRules.isSecondPhase(50.0F, 100.0F, 0.5),
                "exactly half health must remain in phase one");
        check(NagaCombatRules.isSecondPhase(49.99F, 100.0F, 0.5),
                "health strictly below half must enter phase two");
        check(NagaCombatRules.suppressesExternalParryStun(
                        NagaActionState.VENOM_PROJECTILE, false, false),
                "venom parry must not apply external boss stun");
        check(NagaCombatRules.suppressesExternalParryStun(
                        NagaActionState.POUNCE_WINDUP, true, true),
                "an available second-phase token must suppress pounce stun");
        check(!NagaCombatRules.suppressesExternalParryStun(
                        NagaActionState.TAIL_STRIKE, true, false),
                "a spent second-phase token must not suppress tail stun");
        check(NagaCombatRules.isStunned(NagaActionState.STUNNED),
                "stunned action state must suppress movement and attacks");
        check(!NagaCombatRules.isStunned(NagaActionState.CRUISING),
                "ordinary cruise must not be treated as stunned");
        check(NagaCombatRules.shouldEnterStunnedState(false, false),
                "phase one defense must enter the stunned state");
        check(!NagaCombatRules.shouldEnterStunnedState(true, true),
                "an available phase-two token must interrupt without stun");
        check(NagaCombatRules.shouldEnterStunnedState(true, false),
                "phase two must become stunnable after spending its token");

        check(NagaCombatRules.cruiseTicks(20, 40, 0.0) == 20,
                "minimum cruise roll must use twenty ticks");
        check(NagaCombatRules.cruiseTicks(20, 40, 1.0) == 40,
                "maximum cruise roll must use forty ticks");
        for (NagaSkill previous : new NagaSkill[] {
                NagaSkill.POUNCE,
                NagaSkill.TAIL_SWEEP,
                NagaSkill.VENOM_BREATH}) {
            for (int roll = 0; roll < 6; roll++) {
                check(NagaCombatRules.nextSkill(
                                previous, true, true, true, roll) != previous,
                        "skill selection must exclude the previous skill");
            }
        }
        check(NagaCombatRules.nextSkill(
                        NagaSkill.POUNCE, true, false, false, 0)
                        == NagaSkill.NONE,
                "cooldowns and repeat exclusion may force another cruise");

        check(NagaCombatRules.isInsideCircle(
                        4.0, 0.0, 0.0, 0.0, 4.0),
                "pounce circle must include its boundary");
        check(!NagaCombatRules.isInsideCircle(
                        4.01, 0.0, 0.0, 0.0, 4.0),
                "pounce circle must exclude points outside the mark");
        check(NagaCombatRules.isInsideArc(
                        0.0, 6.0, 0.0, 0.0,
                        1.0, 0.0, 6.0, 90.0),
                "tail semicircle must include its side boundary");
        check(!NagaCombatRules.isInsideArc(
                        -1.0, 0.0, 0.0, 0.0,
                        1.0, 0.0, 6.0, 90.0),
                "tail semicircle must exclude the rear side");
        check(NagaCombatRules.isInsideCorridor(
                        16.0, 2.0, 0.0, 0.0,
                        1.0, 0.0, 16.0, 4.0),
                "venom corridor must include its far corner");
        check(!NagaCombatRules.isInsideCorridor(
                        16.01, 0.0, 0.0, 0.0,
                        1.0, 0.0, 16.0, 4.0),
                "venom corridor must stop at maximum range");
        check(!NagaCombatRules.isInsideCorridor(
                        8.0, 2.01, 0.0, 0.0,
                        1.0, 0.0, 16.0, 4.0),
                "venom corridor must use configured full width");

        checkClose(NagaCombatRules.shrinkingOrbitRadius(
                        0, 300, 12.0, 4.0),
                12.0, "fire snake must begin on the outer ring");
        checkClose(NagaCombatRules.shrinkingOrbitRadius(
                        150, 300, 12.0, 4.0),
                8.0, "fire snake must contract linearly");
        checkClose(NagaCombatRules.shrinkingOrbitRadius(
                        300, 300, 12.0, 4.0),
                4.0, "fire snake must finish on the inner ring");
        NagaCombatRules.HorizontalPoint blockedCloud =
                NagaCombatRules.blockedCloudCenter(
                        10.0, 20.0, 0.5, 0.5,
                        3.0, 4.0, 0.75);
        checkClose(blockedCloud.x(), 9.175,
                "blocked venom cloud must sit outside the diagonal shield face");
        checkClose(blockedCloud.z(), 18.9,
                "blocked venom cloud must include the configured outward offset");
        NagaCombatRules.HorizontalPoint zeroDirectionCloud =
                NagaCombatRules.blockedCloudCenter(
                        10.0, 20.0, 0.5, 0.5,
                        0.0, 0.0, 0.75);
        checkClose(zeroDirectionCloud.x(), 10.0,
                "zero-direction venom must retain its contact x coordinate");
        checkClose(zeroDirectionCloud.z(), 20.0,
                "zero-direction venom must retain its contact z coordinate");
        checkClose(NagaCombatRules.normalizeColorChannel(-1), 0.0,
                "negative color channels must clamp to black");
        checkClose(NagaCombatRules.normalizeColorChannel(128), 128.0 / 255.0,
                "color channels must normalize from the 0-255 config range");
        checkClose(NagaCombatRules.normalizeColorChannel(256), 1.0,
                "oversized color channels must clamp to white");
        check(!NagaCombatRules.isFireSnakeVisualActive(0),
                "expired fire snake must clear its visual state immediately");
        check(NagaCombatRules.isFireSnakeVisualActive(1),
                "positive fire snake duration must activate its visual state");

        NagaBodyRules.Point trailHead = new NagaBodyRules.Point(3.0, 1.0, 0.0);
        List<NagaBodyRules.Point> trail = List.of(
                new NagaBodyRules.Point(2.0, 1.0, 0.0),
                new NagaBodyRules.Point(1.0, 0.0, 0.0),
                new NagaBodyRules.Point(0.0, 0.0, 0.0));
        NagaBodyRules.Point sampled = NagaBodyRules.sampleTrail(
                trailHead, trail, 2.0);
        checkClose(sampled.x(), 1.2928932188134525,
                "trail sampling must interpolate by traveled distance");
        checkClose(sampled.y(), 0.29289321881345254,
                "trail sampling must retain vertical head motion");
        check(NagaBodyRules.trailCapacity(12, 2.0, 0.25) == 99,
                "trail capacity must be bounded from segment count and spacing");
        check(!NagaBodyRules.shouldResetTrail(32.0 * 32.0, 32.0),
                "the teleport reset boundary must remain continuous");
        check(NagaBodyRules.shouldResetTrail(32.01 * 32.01, 32.0),
                "movement beyond the reset distance must rebuild the trail");
        checkClose(NagaBodyRules.tailSweepProgress(0, 8), 0.0,
                "tail sweep must begin at the starting side");
        checkClose(NagaBodyRules.tailSweepProgress(7, 8), 1.0,
                "tail sweep must reach the opposite side on tick eight");
        int tailDamageApplications = 0;
        boolean tailDamageApplied = false;
        for (int tick = 0; tick < 8; tick++) {
            if (NagaBodyRules.shouldApplyTailDamage(
                    tick, 8, tailDamageApplied)) {
                tailDamageApplications++;
                tailDamageApplied = true;
            }
        }
        check(tailDamageApplications == 1,
                "tail sweep damage must be applied exactly once");
        checkClose(NagaBodyRules.sweepAngleRadians(0.0, 90.0),
                Math.PI * 0.5,
                "tail sweep must begin on one side of the forward arc");
        checkClose(NagaBodyRules.sweepAngleRadians(1.0, 90.0),
                -Math.PI * 0.5,
                "tail sweep must finish on the other side of the forward arc");
        NagaBodyRules.TailPoseWeights startPose =
                NagaBodyRules.tailPoseWeights(0.0);
        NagaBodyRules.TailPoseWeights frontPose =
                NagaBodyRules.tailPoseWeights(0.5);
        NagaBodyRules.TailPoseWeights endPose =
                NagaBodyRules.tailPoseWeights(1.0);
        checkClose(startPose.start(), 1.0,
                "tail pose must begin entirely on the starting side");
        checkClose(frontPose.front(), 1.0,
                "tail pose midpoint must point entirely forward");
        checkClose(endPose.end(), 1.0,
                "tail pose must finish entirely on the opposite side");

        NagaCombatState savedState = new NagaCombatState();
        savedState.actionState = NagaActionState.VENOM_PROJECTILE;
        savedState.lastSkill = NagaSkill.VENOM_BREATH;
        savedState.stateTicks = 4;
        savedState.stateDuration = 21;
        savedState.secondPhase = true;
        savedState.stunImmunityAvailable = false;
        savedState.stunImmunityRecoveryTicks = 200;
        savedState.fireSnakeTicks = 250;
        savedState.venomProjectileId = java.util.UUID.randomUUID();
        NagaCombatState loadedState = new NagaCombatState();
        loadedState.load(savedState.save());
        check(loadedState.actionState == NagaActionState.VENOM_PROJECTILE,
                "active venom state must survive reload");
        check(loadedState.lastSkill == NagaSkill.VENOM_BREATH,
                "repeat exclusion must survive reload");
        check(loadedState.secondPhase && !loadedState.stunImmunityAvailable,
                "phase and current immunity token state must survive reload");
        check(loadedState.stunImmunityRecoveryTicks == 200,
                "immunity recovery must survive reload");
        check(loadedState.fireSnakeTicks == 250,
                "active fire snake must survive reload");
        check(savedState.venomProjectileId.equals(
                        loadedState.venomProjectileId),
                "moving venom identity must survive reload");

        BossRefactorTwilightForestConfig.NagaConfig config =
                BossRefactorTwilightForestConfig.NAGA;
        check(config.pounceWindupTicks.getDefault() == 30,
                "default pounce windup must be thirty ticks");
        check(config.stunDurationTicks.getDefault() == 60,
                "default defensive stun must last sixty ticks");
        check(config.pounceFlightTicks.getDefault() == 20,
                "default pounce flight must be twenty ticks");
        checkClose(config.pounceImpactRadius.getDefault(), 4.0,
                "default pounce radius must be four blocks");
        checkClose(config.pounceVerticalRange.getDefault(), 4.0,
                "default pounce vertical range must be four blocks");
        check(config.tailWindupTicks.getDefault() == 20,
                "default tail windup must be twenty ticks");
        check(config.tailDurationTicks.getDefault() == 8,
                "default visible tail sweep must last eight ticks");
        checkClose(config.tailRadius.getDefault(), 6.0,
                "default tail radius must be six blocks");
        checkClose(config.tailVerticalRange.getDefault(), 3.0,
                "default tail vertical range must be three blocks");
        checkClose(config.tailHalfAngleDegrees.getDefault(), 90.0,
                "default tail half-angle must be ninety degrees");
        checkClose(config.bodySegmentSpacing.getDefault(), 2.0,
                "default body segment spacing must be two blocks");
        checkClose(config.bodyTrailSampleSpacing.getDefault(), 0.25,
                "default body trail samples must be a quarter block apart");
        checkClose(config.bodyTrailTeleportResetDistance.getDefault(), 32.0,
                "default body teleport reset distance must be thirty-two blocks");
        checkClose(config.bodyTailCurveDegrees.getDefault(), 270.0,
                "default tail pose must sweep through the forward half-circle");
        check(config.venomWindupTicks.getDefault() == 30,
                "default venom windup must be thirty ticks");
        checkClose(config.venomLength.getDefault(), 16.0,
                "default venom range must be sixteen blocks");
        checkClose(config.venomWidth.getDefault(), 4.0,
                "default venom width must be four blocks");
        checkClose(config.venomProjectileRadius.getDefault(), 2.0,
                "default venom collision must cover the marked corridor width");
        check(config.venomCloudDurationTicks.getDefault() == 140,
                "default venom cloud must last 140 ticks");
        checkClose(config.venomCloudVerticalRange.getDefault(), 3.0,
                "default venom cloud vertical range must be three blocks");
        checkClose(config.venomBlockedCloudOffset.getDefault(), 0.75,
                "shield-blocked venom must land three quarters of a block outward");
        check(config.venomPrimaryRed.getDefault() == 190
                        && config.venomPrimaryGreen.getDefault() == 255
                        && config.venomPrimaryBlue.getDefault() == 35,
                "default venom core must be fluorescent yellow-green");
        check(config.venomSecondaryRed.getDefault() == 18
                        && config.venomSecondaryGreen.getDefault() == 105
                        && config.venomSecondaryBlue.getDefault() == 18,
                "default venom mist must be deep green");
        checkClose(config.venomPrimaryParticleScale.getDefault(), 1.4,
                "default venom core particle scale must be configurable");
        checkClose(config.venomSecondaryParticleScale.getDefault(), 2.2,
                "default venom mist particle scale must be configurable");
        check(config.fireSnakeDurationTicks.getDefault() == 300,
                "default fire snake must last 300 ticks");
        checkClose(config.fireSnakeRedMultiplier.getDefault(), 1.0,
                "fire snake must retain its full red model channel");
        checkClose(config.fireSnakeGreenMultiplier.getDefault(), 0.25,
                "fire snake must strongly reduce its green model channel");
        checkClose(config.fireSnakeBlueMultiplier.getDefault(), 0.2,
                "fire snake must strongly reduce its blue model channel");
        check(config.firePointDurationTicks.getDefault() == 300,
                "default fire points must last 300 ticks");
        checkClose(config.firePointVerticalRange.getDefault(), 3.0,
                "default fire point vertical range must be three blocks");
        check(config.stunImmunityRecoveryTicks.getDefault() == 300,
                "default stun ward recovery must last 300 ticks");

        TwilightBossParryAccess defaultParryAccess = new TwilightBossParryAccess() {
            @Override
            public boolean bossRefactorTF$isCurrentAttackParryable() {
                return false;
            }

            @Override
            public int bossRefactorTF$getParryFeedbackTicks() {
                return 0;
            }

            @Override
            public int bossRefactorTF$acceptParry(
                    LivingEntity actor, ResourceLocation sourceId) {
                return 0;
            }
        };
        check(!defaultParryAccess.bossRefactorTF$suppressesExternalParryStun(),
                "boss parry access must preserve external stun by default");
    }

    private static void checkClose(
            double actual, double expected, String message) {
        check(Math.abs(actual - expected) < 1.0E-6,
                message + ": expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}