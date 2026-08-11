package com.tonywww.bossrefactortf.boss.knightphantom;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public final class KnightPhantomCombatRulesSelfTest {
    private KnightPhantomCombatRulesSelfTest() {
    }

    public static void main(String[] args) {
        check(KnightPhantomCombatRules.fallenKnightCount(6, 6) == 0,
                "a full group must have no fallen knights");
        check(KnightPhantomCombatRules.fallenKnightCount(6, 1) == 5,
                "the final member of a six-knight group must count five losses");
        check(KnightPhantomCombatRules.fallenKnightCount(1, 2) == 0,
                "fallen count must not become negative");

        checkClose(KnightPhantomCombatRules.fallenKnightMultiplier(5, 0.05),
                1.25, "five losses must grant a 25% bonus");
        checkClose(KnightPhantomCombatRules.fallenKnightMultiplier(-1, 0.05),
                1.0, "negative losses must not reduce the multiplier");

        checkClose(KnightPhantomCombatRules.incomingDamage(
                        10.0F, true, 2.0, false, 0.5),
                20.0, "fire damage must double");
        checkClose(KnightPhantomCombatRules.incomingDamage(
                        10.0F, false, 2.0, true, 0.5),
                5.0, "the last knight must prevent half of incoming damage");
        checkClose(KnightPhantomCombatRules.incomingDamage(
                        10.0F, true, 2.0, true, 0.5),
                10.0, "fire vulnerability and last-knight reduction must stack");
        checkClose(KnightPhantomCombatRules.chargeDamage(10.0, 1.2),
                12.0, "charge damage must use 120% attack damage");

        checkClose(BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .playerAvoidanceDistance.getDefault(),
                KnightPhantomCombatRules.ARENA_RADIUS,
                "default player avoidance must match the arena radius");
        checkClose(BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .chargeSpeedMultiplier.getDefault(),
                3.0, "default charge speed must be 3.0");
        check(BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .postChargePossessionTicks.getDefault() == 60,
                "post-charge possession must last 60 ticks by default");
        check(BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .deathHandoffDelayTicks.getDefault() == 30,
                "death handoff must wait 1.5 seconds by default");
        check(BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .lastKnightChargeWindupTicks.getDefault() == 15,
                "the final knight windup must last 0.75 seconds by default");
        check(BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .parryFeedbackTicks.getDefault() == 40,
                "a parried knight must remain vulnerable for two seconds by default");
        checkClose(BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .chargeMaximumDistance.getDefault(),
                30.0, "a charge must travel at most 30 blocks by default");

        check(KnightPhantomCombatRules.chargeWindupTicks(
                        false, 30, 15) == 30,
                "regular knights must keep the regular windup");
        check(KnightPhantomCombatRules.chargeWindupTicks(
                        true, 30, 15) == 15,
                "the final knight must use its shorter windup");
        check(KnightPhantomCombatRules.lastKnightRemainingChargeSegments(
                        0, 3) == 2,
                "activating the final phase mid-round must add two remaining charges");
        check(KnightPhantomCombatRules.lastKnightRemainingChargeSegments(
                        4, 3) == 4,
                "activating the final phase must not discard existing charge segments");
        check(!KnightPhantomCombatRules.chargeHandoffReady(129L, 130L),
                "death handoff must not complete one tick early");
        check(KnightPhantomCombatRules.chargeHandoffReady(130L, 130L),
                "death handoff must complete at its deadline");

        UUID encounterId = UUID.randomUUID();
        UUID firstOwnerId = UUID.randomUUID();
        UUID secondOwnerId = UUID.randomUUID();
        KnightPhantomEncounterData encounterData =
                new KnightPhantomEncounterData();
        check(encounterData.tryClaim(encounterId, firstOwnerId, 100L),
                "the first queued knight must claim an empty encounter");
        check(!encounterData.tryClaim(encounterId, secondOwnerId, 100L),
                "a second knight must never claim an occupied encounter");
        check(!encounterData.tryClaim(encounterId, secondOwnerId, 10_000L),
                "an unloaded owner must never be replaced by a timeout");
        check(firstOwnerId.equals(encounterData.owner(encounterId)),
                "the first owner must remain authoritative");
        check(!encounterData.releaseCompleted(
                        encounterId, secondOwnerId, 100L),
                "a non-owner must not release possession");
        check(encounterData.releaseCompleted(
                        encounterId, firstOwnerId, 100L),
                "the owner must release possession after completion");
        check(encounterData.tryClaim(encounterId, secondOwnerId, 100L),
                "the next knight may claim only after completion");
        check(encounterData.releaseAfterDeath(
                        encounterId, secondOwnerId, 130L),
                "a dead owner must release possession with a delay");
        check(!encounterData.tryClaim(encounterId, firstOwnerId, 129L),
                "death handoff must not transfer before its deadline");
        check(encounterData.tryClaim(encounterId, firstOwnerId, 130L),
                "death handoff may transfer at its deadline");
        CompoundTag savedEncounterData = encounterData.save(new CompoundTag());
        KnightPhantomEncounterData loadedEncounterData =
                KnightPhantomEncounterData.load(savedEncounterData);
        check(firstOwnerId.equals(loadedEncounterData.owner(encounterId)),
                "the unique owner must survive a world-data reload");
        check(!loadedEncounterData.tryClaim(
                        encounterId, secondOwnerId, Long.MAX_VALUE),
                "a reloaded owner must remain exclusive");

        KnightPhantomCombatRules.HorizontalPoint boundaryCharge =
                KnightPhantomCombatRules.straightChargeDestination(
                        -5.0, 0.0, 1.0, 0.0,
                        30.0, 0.0, 0.0, 9.0);
        checkClose(boundaryCharge.x(), 9.0,
                "a long charge must stop at the arena boundary");
        checkClose(boundaryCharge.z(), 0.0,
                "arena clipping must preserve the locked charge line");
        KnightPhantomCombatRules.HorizontalPoint distanceCharge =
                KnightPhantomCombatRules.straightChargeDestination(
                        0.0, 0.0, 3.0, 4.0,
                        5.0, 0.0, 0.0, 20.0);
        checkClose(distanceCharge.x(), 3.0,
                "a charge inside the arena must respect its maximum distance");
        checkClose(distanceCharge.z(), 4.0,
                "a charge must preserve its normalized locked direction");

        check(KnightPhantomCombatRules.shouldQueueChargeRoundAfterDeath(
                        true, 5, false),
                "a death between rounds must queue the survivors");
        check(!KnightPhantomCombatRules.shouldQueueChargeRoundAfterDeath(
                        true, 5, true),
                "a death must not replace an existing charge round");
        check(!KnightPhantomCombatRules.shouldQueueChargeRoundAfterDeath(
                        true, 0, false),
                "a death cannot start a round without survivors");
        check(!KnightPhantomCombatRules.shouldQueueChargeRoundAfterDeath(
                        false, 5, false),
                "disabled custom charges must not start after a death");

        check(!KnightPhantomCombatRules.shouldMoveToDestination(
                        1.0, 2.0, 3.0,
                        1.0, 2.0, 3.0),
                "a waiting knight must not move toward its current position");
        check(!KnightPhantomCombatRules.shouldMoveToDestination(
                        1.0, 2.0, 3.0,
                        Double.NaN, 2.0, 3.0),
                "a non-finite movement target must be rejected");
        check(KnightPhantomCombatRules.shouldMoveToDestination(
                        1.0, 2.0, 3.0,
                        2.0, 2.0, 3.0),
                "a finite separated movement target must remain valid");

        KnightPhantomCombatRules.HorizontalPoint openRetreat =
                KnightPhantomCombatRules.avoidanceDestination(
                        0.0, 0.0, 1.0, 0.0,
                        0.0, 5.0, 0.0, 0.0,
                        10.0, 20.0, 0);
        checkClose(openRetreat.x(), -9.0,
                "avoidance must retreat directly when arena space is available");
        checkClose(openRetreat.z(), 0.0,
                "an unobstructed retreat must not add lateral movement");

        KnightPhantomCombatRules.HorizontalPoint wallEscape =
                KnightPhantomCombatRules.avoidanceDestination(
                        9.0, 0.0, 8.0, 0.0,
                        12.0, 0.0, 0.0, 0.0,
                        10.0, 9.0, 0);
        check(wallEscape.x() * wallEscape.x()
                        + wallEscape.z() * wallEscape.z() <= 81.0 + 1.0E-6,
                "wall avoidance destination must remain inside the arena");
        check(Math.abs(wallEscape.z()) > 1.0E-6,
                "a cornered knight must escape laterally instead of retreating into the wall");
        check(wallEscape.x() < 9.0,
                "wall-following avoidance must turn back into the arena");

        check(KnightPhantomCombatRules.movementSegmentReaches(
                        0.0, 0.0, 0.0,
                        12.0, 0.0, 0.0,
                        10.0, 0.0, 0.0, 0.5),
                "a high-speed charge must finish when it crosses its destination");
        check(!KnightPhantomCombatRules.movementSegmentReaches(
                        0.0, 0.0, 0.0,
                        8.0, 0.0, 0.0,
                        10.0, 0.0, 0.0, 0.5),
                "a charge must continue before reaching its destination");
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