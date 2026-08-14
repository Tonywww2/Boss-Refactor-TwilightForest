package com.tonywww.bossrefactortf.boss.hydra;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.nbt.CompoundTag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class HydraCombatRulesSelfTest {
    private HydraCombatRulesSelfTest() {
    }

    public static void main(String[] args) {
        check(!HydraCombatRules.isSecondPhase(50.0F, 100.0F, 0.5),
                "exactly half health must remain in phase one");
        check(HydraCombatRules.isSecondPhase(49.99F, 100.0F, 0.5),
                "health strictly below half must enter phase two");

        Set<HydraSkill> ordinarySkills = new HashSet<>();
        for (int value = -100; value <= 100; value++) {
            HydraSkill skill = HydraCombatRules.ordinarySkill(value);
            check(skill == HydraCombatRules.ordinarySkill(value),
                    "ordinary skill mapping must be stable");
            check(skill.isOrdinaryMelee(),
                    "ordinary skill mapping must only return melee skills");
            ordinarySkills.add(skill);
        }
        check(HydraCombatRules.ordinarySkill(Integer.MIN_VALUE)
                        .isOrdinaryMelee()
                        && HydraCombatRules.ordinarySkill(Integer.MAX_VALUE)
                        .isOrdinaryMelee(),
                "ordinary skill mapping must support all integer values");
        check(ordinarySkills.contains(HydraSkill.BITE)
                        && ordinarySkills.contains(HydraSkill.SWEEP)
                        && ordinarySkills.contains(HydraSkill.SLAM),
                "all ordinary melee skills must be reachable");

        checkClose(HydraCombatRules.expandingRadius(0, 10, 20.0), 0.0,
                "Fire Lord radius must start at zero");
        checkClose(HydraCombatRules.expandingRadius(5, 10, 20.0), 10.0,
                "Fire Lord radius must expand linearly at the midpoint");
        checkClose(HydraCombatRules.expandingRadius(10, 10, 20.0), 20.0,
                "Fire Lord radius must reach its maximum at the endpoint");
        checkClose(HydraCombatRules.expandingRadius(-5, 10, 20.0), 0.0,
                "negative elapsed ticks must clamp to the starting radius");
        checkClose(HydraCombatRules.expandingRadius(5, 0, 20.0), 20.0,
                "zero duration must use the completed radius");
        checkClose(HydraCombatRules.expandingRadius(5, -1, -20.0), 0.0,
                "negative maximum radius must never produce a negative mark");

        check(HydraCombatRules.isInsideCircle(
                        4.0, 0.0, 0.0, 0.0, 4.0),
                "circle damage must include the radius boundary");
        check(!HydraCombatRules.isInsideCircle(
                        4.01, 0.0, 0.0, 0.0, 4.0),
                "circle damage must exclude points outside the radius");
        check(HydraCombatRules.isInsideArc(
                        0.0, 6.0, 0.0, 0.0,
                        1.0, 0.0, 6.0, 90.0),
                "arc damage must include the half-circle boundary");
        check(!HydraCombatRules.isInsideArc(
                        -1.0, 0.0, 0.0, 0.0,
                        1.0, 0.0, 6.0, 90.0),
                "arc damage must exclude targets behind the head");
        check(!HydraCombatRules.isInsideArc(
                        1.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 6.0, 90.0),
                "a zero attack direction must not hit an offset target");
        check(HydraCombatRules.isInsideArc(
                        0.0, 0.0, 0.0, 0.0,
                        0.0, 0.0, 6.0, 90.0),
                "the attack origin must remain inside an arc");

        check(HydraSkill.byId(HydraSkill.SLAM.ordinal()) == HydraSkill.SLAM,
                "valid Hydra skill ids must resolve normally");
        check(HydraSkill.byId(-1) == HydraSkill.NONE
                        && HydraSkill.byId(999) == HydraSkill.NONE,
                "invalid Hydra skill ids must fall back to NONE");
        check(HydraActionState.byId(HydraActionState.RECOVERY.ordinal())
                        == HydraActionState.RECOVERY,
                "valid Hydra action ids must resolve normally");
        check(HydraActionState.byId(-1) == HydraActionState.IDLE
                        && HydraActionState.byId(999) == HydraActionState.IDLE,
                "invalid Hydra action ids must fall back to IDLE");

        verifyConfigDefaults();
        verifyStateRoundTrip();
        verifyBeamLoadClamping();
    }

    private static void verifyConfigDefaults() {
        BossRefactorTwilightForestConfig.HydraConfig config =
                BossRefactorTwilightForestConfig.HYDRA;
        check(config.enabled.getDefault(),
                "Hydra combat takeover must default to enabled");
        checkClose(config.baseAttackDamage.getDefault(), 16.0,
                "Hydra base attack damage must default to sixteen");
        check(config.ordinaryAttackIntervalTicks.getDefault() == 40,
                "ordinary attacks must default to a forty-tick interval");
        check(config.biteWindupTicks.getDefault() == 20
                        && config.biteRange.getDefault() == 6.0
                        && config.biteDamageMultiplier.getDefault() == 1.0,
                "bite defaults must remain 20 ticks, 6 range, and 1x damage");
        check(config.sweepWindupTicks.getDefault() == 30
                        && config.sweepRadius.getDefault() == 8.0
                        && config.sweepHalfAngleDegrees.getDefault() == 90.0
                        && config.sweepDamageMultiplier.getDefault() == 1.0,
                "sweep defaults must remain 30 ticks, 8 range, 90 degrees, and 1x damage");
        check(config.slamWindupTicks.getDefault() == 40
                        && config.slamRadius.getDefault() == 5.0
                        && config.slamDamageMultiplier.getDefault() == 1.5,
                "slam defaults must remain 40 ticks, 5 range, and 1.5x damage");
        check(config.recoveryTicks.getDefault() == 20
                        && config.followUpDelayTicks.getDefault() == 20,
                "recovery and follow-up delay must default to twenty ticks");
        check(config.fireLordInitialDelayTicks.getDefault() == 200
                        && config.fireLordCooldownTicks.getDefault() == 600
                        && config.fireLordDurationTicks.getDefault() == 200
                        && config.fireLordMaximumRadius.getDefault() == 20.0,
                "Fire Lord cadence must remain 200 initial, 600 cooldown, 200 duration, and radius 20");
        check(config.fireLordDamageIntervalTicks.getDefault() == 20
                        && config.fireLordDamageMultiplier.getDefault() == 0.5
                        && config.fireIgniteTicks.getDefault() == 100,
                "Fire Lord damage must remain every 20 ticks at 0.5x with 100 ignite ticks");
        check(config.fireLordBeamIntervalTicks.getDefault() == 20
                        && config.fireLordBeamCount.getDefault() == 2
                        && config.fireLordBeamScatter.getDefault() == 14.0
                        && config.fireLordBeamRadius.getDefault() == 2.0
                        && config.fireLordBeamWindupTicks.getDefault() == 20,
                "Fire Lord beams must retain the configured default cadence and geometry");
        check(config.secondPhaseBeamIntervalTicks.getDefault() == 160
                        && config.secondPhaseBeamCount.getDefault() == 3
                        && config.secondPhaseBeamScatter.getDefault() == 16.0
                        && config.secondPhaseBeamRadius.getDefault() == 2.0
                        && config.secondPhaseBeamWindupTicks.getDefault() == 20,
                "phase-two beams must retain the configured default cadence and geometry");
        checkClose(config.beamDamageMultiplier.getDefault(), 1.0,
                "shared flame beams must default to 1x damage");
        check(config.parryFeedbackTicks.getDefault() == 10
                        && config.shieldCooldownTicks.getDefault() == 100,
                "Hydra parry feedback and shield cooldown defaults must remain stable");
        checkClose(config.meleeVerticalRange.getDefault(), 4.0,
                "ordinary melee vertical range must default to four blocks");
        checkClose(config.fireLordGroundDistance.getDefault(), 10.0,
                "Fire Lord ground marker distance must default to ten blocks");
    }

    private static void verifyStateRoundTrip() {
        UUID firstId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        UUID secondId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        HydraCombatState savedState = new HydraCombatState();
        savedState.actionState = HydraActionState.FOLLOW_UP_DELAY;
        savedState.skill = HydraSkill.SWEEP;
        savedState.pendingFollowUpSkill = HydraSkill.SLAM;
        savedState.actionTicks = 7;
        savedState.actionDuration = 20;
        savedState.activeHeadIndex = 2;
        savedState.pendingFollowUpHeadIndex = 5;
        savedState.ordinaryAttackCooldown = 40;
        savedState.fireLordCooldown = 600;
        savedState.secondPhaseBeamCooldown = 160;
        savedState.recoveryVisualProgress = 0.75F;
        savedState.followUpAttack = true;
        savedState.secondPhase = true;
        savedState.combatInitialized = true;
        savedState.lockedX = 10.5;
        savedState.lockedY = 64.0;
        savedState.lockedZ = -7.25;
        savedState.directionX = -0.6;
        savedState.directionZ = 0.8;
        savedState.lastParryGameTime = 987654321L;
        HydraBeam firstBeam = new HydraBeam(firstId, 1.5, 65.0, -2.5, 3.0, 8);
        firstBeam.remainingWindupTicks = 4;
        HydraBeam secondBeam = new HydraBeam(secondId, -4.0, 70.5, 6.25, 2.0, 3);
        secondBeam.remainingWindupTicks = 1;
        savedState.beams.add(firstBeam);
        savedState.beams.add(secondBeam);

        HydraCombatState loadedState = new HydraCombatState();
        loadedState.load(savedState.save());
        check(loadedState.actionState == HydraActionState.FOLLOW_UP_DELAY
                        && loadedState.skill == HydraSkill.SWEEP
                        && loadedState.pendingFollowUpSkill == HydraSkill.SLAM,
                "action and pending follow-up skills must survive an NBT round-trip");
        check(loadedState.actionTicks == 7 && loadedState.actionDuration == 20
                        && loadedState.activeHeadIndex == 2
                        && loadedState.pendingFollowUpHeadIndex == 5,
                "action timing and head indices must survive an NBT round-trip");
        check(loadedState.ordinaryAttackCooldown == 40
                        && loadedState.fireLordCooldown == 600
                        && loadedState.secondPhaseBeamCooldown == 160,
                "all Hydra cooldowns must survive an NBT round-trip");
        checkClose(loadedState.recoveryVisualProgress, 0.75,
                "recovery progress must survive an NBT round-trip");
        check(loadedState.followUpAttack && loadedState.secondPhase
                        && loadedState.combatInitialized,
                "phase and combat flags must survive an NBT round-trip");
        checkClose(loadedState.lockedX, 10.5,
                "locked x geometry must survive an NBT round-trip");
        checkClose(loadedState.lockedY, 64.0,
                "locked y geometry must survive an NBT round-trip");
        checkClose(loadedState.lockedZ, -7.25,
                "locked z geometry must survive an NBT round-trip");
        checkClose(loadedState.directionX, -0.6,
                "locked x direction must survive an NBT round-trip");
        checkClose(loadedState.directionZ, 0.8,
                "locked z direction must survive an NBT round-trip");
        check(loadedState.lastParryGameTime == 987654321L,
                "last parry time must survive an NBT round-trip");
        check(loadedState.beams.size() == 2,
                "every pending beam must survive an NBT round-trip");
        verifyBeam(loadedState.beams.get(0), firstId,
                1.5, 65.0, -2.5, 3.0, 8, 4);
        verifyBeam(loadedState.beams.get(1), secondId,
                -4.0, 70.5, 6.25, 2.0, 3, 1);
    }

    private static void verifyBeamLoadClamping() {
        HydraBeam pendingBeam = new HydraBeam(
                UUID.fromString("01234567-89ab-cdef-0123-456789abcdef"),
                0.0, 64.0, 0.0, 2.0, 5);
        pendingBeam.remainingWindupTicks = 1;
        HydraBeam restoredPendingBeam = HydraBeam.load(pendingBeam.save());
        check(restoredPendingBeam.remainingWindupTicks >= 1,
                "normally saved pending beams must keep at least one windup tick");

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putDouble("Radius", -3.0);
        HydraBeam legacyBeam = HydraBeam.load(legacyTag);
        check(legacyBeam.totalWindupTicks == 1
                        && legacyBeam.remainingWindupTicks == 1
                        && legacyBeam.radius == 0.0,
                "missing legacy windup data must safely fall back to one tick");

        CompoundTag malformedTag = new CompoundTag();
        malformedTag.putInt("TotalWindupTicks", 3);
        malformedTag.putInt("RemainingWindupTicks", 99);
        HydraBeam malformedBeam = HydraBeam.load(malformedTag);
        check(malformedBeam.totalWindupTicks == 3
                        && malformedBeam.remainingWindupTicks == 3,
                "remaining windup must clamp to the saved total windup");
        malformedTag.putInt("RemainingWindupTicks", 0);
        HydraBeam expiredMalformedBeam = HydraBeam.load(malformedTag);
        check(expiredMalformedBeam.remainingWindupTicks == 1,
                "expired malformed pending beams must clamp to one remaining tick");
    }

    private static void verifyBeam(
            HydraBeam beam, UUID id, double x, double y, double z,
            double radius, int totalWindupTicks, int remainingWindupTicks) {
        check(beam.id.equals(id), "beam UUID must survive an NBT round-trip");
        checkClose(beam.x, x, "beam x coordinate must survive an NBT round-trip");
        checkClose(beam.y, y, "beam y coordinate must survive an NBT round-trip");
        checkClose(beam.z, z, "beam z coordinate must survive an NBT round-trip");
        checkClose(beam.radius, radius,
                "beam radius must survive an NBT round-trip");
        check(beam.totalWindupTicks == totalWindupTicks
                        && beam.remainingWindupTicks == remainingWindupTicks,
                "beam windup timing must survive an NBT round-trip");
    }

    private static void checkClose(double actual, double expected, String message) {
        check(Math.abs(actual - expected) < 1.0E-6,
                message + ": expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}