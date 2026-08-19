package com.tonywww.bossrefactortf.boss.minoshroom;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;

import java.util.UUID;

public final class MinoshroomCombatRulesSelfTest {
    private MinoshroomCombatRulesSelfTest() {
    }

    public static void main(String[] args) {
        check(!MinoshroomCombatRules.isSecondPhase(50.0F, 100.0F, 0.5),
                "exactly half health must remain in phase one");
        check(MinoshroomCombatRules.isSecondPhase(49.99F, 100.0F, 0.5),
                "health strictly below half must enter phase two");
        check(MinoshroomCombatRules.windupTicks(5, true, 0.5) == 3,
                "phase-two windups must round upward");
        checkClose(MinoshroomCombatRules.skillDamage(
                10.0, 1.5, true, 1.2), 18.0,
                "phase-two sweep damage must apply both multipliers");
        checkClose(MinoshroomCombatRules.navigationSpeed(
                1.5, true, 1.2), 1.8,
                "phase-two navigation must apply the speed multiplier");
        checkClose(MinoshroomCombatRules.reducedDamage(
                10.0, 4.0, 4.0, 0.2), 10.0,
                "damage at the configured boundary must not be reduced");
        checkClose(MinoshroomCombatRules.reducedDamage(
                10.0, 4.01, 4.0, 0.2), 8.0,
                "damage beyond the configured boundary must be reduced");
        check(MinoshroomCombatRules.cruiseTicks(20, 40, 0.0) == 20,
                "the minimum cruise roll must use twenty ticks");
        check(MinoshroomCombatRules.cruiseTicks(20, 40, 1.0) == 40,
                "the maximum cruise roll must use forty ticks");
        check(MinoshroomCombatRules.shouldUseStomp(5.0, 5.0),
                "targets on the stomp boundary must select stomp");
        check(!MinoshroomCombatRules.shouldUseStomp(5.01, 5.0),
                "targets outside stomp range must select sweep");
        check(MinoshroomCombatRules.shouldSelectStomp(
                        5.0, 5.0, 0.25, 0.249),
                "a roll below the stomp probability must select stomp");
        check(!MinoshroomCombatRules.shouldSelectStomp(
                        5.0, 5.0, 0.25, 0.25),
                "a roll on the stomp probability boundary must select sweep");
        check(!MinoshroomCombatRules.shouldSelectStomp(
                        5.01, 5.0, 1.0, 0.0),
                "targets outside stomp range must select sweep regardless of probability");
        check(MinoshroomCombatRules.shouldDamageWithStomp(true, 5.0, 5.0),
                "grounded targets on the stomp boundary must take damage");
        check(!MinoshroomCombatRules.shouldDamageWithStomp(false, 0.0, 5.0),
                "jumping targets must avoid stomp damage");
        check(MinoshroomCombatRules.blockDestructionMinimumY(64.0) == 64,
                "an integer feet height must preserve the floor below it");
        check(MinoshroomCombatRules.blockDestructionMinimumY(63.999999) == 64,
                "floating-point drift must not expose the floor to destruction");
        check(MinoshroomCombatRules.blockDestructionMinimumY(64.5) == 65,
                "a partial-block floor must preserve the block containing the feet plane");
        check(MinoshroomCombatRules.stompDamageTypePath()
                        .equals("minoshroom_stomp"),
                "stomp damage must use its dedicated data-driven damage type");

        check(MinoshroomCombatRules.shouldRepeatSweep(
                1, 4, true, 2, 0.0, 1.0),
                "phase two must guarantee a second total round");
        check(!MinoshroomCombatRules.shouldRepeatSweep(
                1, 4, false, 2, 0.5, 0.5),
                "a roll on the repeat boundary must stop");
        check(MinoshroomCombatRules.shouldRepeatSweep(
                4, 4, false, 2, 0.5, 0.49),
                "the fifth total round may be selected");
        check(!MinoshroomCombatRules.shouldRepeatSweep(
                5, 4, true, 2, 1.0, 0.0),
                "the sweep sequence must never exceed five rounds");
        check(MinoshroomCombatRules.stompCount(false, 2) == 1,
                "phase one must stomp once");
        check(MinoshroomCombatRules.stompCount(true, 2) == 2,
                "phase two must stomp twice");
        checkClose(MinoshroomCombatRules.halfAngleDegrees(180.0), 90.0,
                "a front 180-degree sweep must use a ninety-degree half-angle");
        check(MinoshroomCombatRules.isInsideArc(
                0.0, 0.0, 1.0, 0.0, 0.0, 4.0, 4.0, 90.0),
                "the sweep must include its side boundary");
        check(!MinoshroomCombatRules.isInsideArc(
                0.0, 0.0, 1.0, 0.0, -0.01, 4.0, 4.0, 90.0),
                "the sweep must exclude targets behind the side boundary");

        UUID hitTarget = UUID.randomUUID();
        MinoshroomCombatState savedState = new MinoshroomCombatState();
        savedState.attackState = MinoshroomAttackState.SWEEP_DASH;
        savedState.stateTicks = 3;
        savedState.stateDuration = 8;
        savedState.sweepRoundsCompleted = 2;
        savedState.directionX = 1.0;
        savedState.dashDistance = 2.5;
        savedState.dashHits.add(hitTarget);
        MinoshroomCombatState loadedState = new MinoshroomCombatState();
        loadedState.load(savedState.save());
        check(loadedState.attackState == MinoshroomAttackState.SWEEP_DASH,
                "the active attack state must survive an NBT round-trip");
        check(loadedState.stateTicks == 3 && loadedState.stateDuration == 8,
                "the active timer must survive an NBT round-trip");
        check(loadedState.sweepRoundsCompleted == 2,
                "completed sweep rounds must survive an NBT round-trip");
        checkClose(loadedState.directionX, 1.0,
                "the locked direction must survive an NBT round-trip");
        check(loadedState.dashHits.contains(hitTarget),
                "per-round dash contacts must survive an NBT round-trip");

        BossRefactorTwilightForestConfig.MinoshroomConfig config =
                BossRefactorTwilightForestConfig.MINOSHROOM;
        check(config.blockDestructionIntervalTicks.getDefault() == 5,
                "block destruction must run every five ticks by default");
        checkClose(config.blockDestructionHorizontalRange.getDefault(), 0.5,
                "default horizontal destruction range must be half a block");
        checkClose(config.rangedDamageReduction.getDefault(), 0.2,
                "default distant damage reduction must be twenty percent");
        check(config.sweepInitialWindupTicks.getDefault() == 20,
                "the initial sweep windup must be twenty ticks");
        checkClose(config.sweepDashMaximumDistance.getDefault(), 6.0,
                "the dash must be capped at six blocks");
        check(config.sweepDashDurationTicks.getDefault() == 8,
                "the dash must be capped at eight ticks");
        check(config.sweepStrikeWindupTicks.getDefault() == 5,
                "the strike windup must be five ticks");
        checkClose(config.sweepStrikeArcDegrees.getDefault(), 180.0,
                "the strike must cover the forward hemisphere");
        check(config.sweepMaximumRepeats.getDefault() == 4,
                "the initial round may be followed by four repeats");
        check(config.stompWindupTicks.getDefault() == 30,
                "the stomp windup must be thirty ticks");
        checkClose(config.stompSelectionProbability.getDefault(), 0.25,
                "stomp selection must default to twenty-five percent");
        check(config.secondPhaseStompCount.getDefault() == 2,
                "phase two must stomp twice");
        checkClose(config.secondPhaseWindupMultiplier.getDefault(), 0.5,
                "phase-two windups must be halved");
        checkClose(config.secondPhaseDamageMultiplier.getDefault(), 1.2,
                "phase-two custom damage must gain twenty percent");
        checkClose(config.secondPhaseSpeedMultiplier.getDefault(), 1.2,
                "phase-two navigation must gain twenty percent");
        checkClose(config.secondPhaseRedTint.getDefault(), 1.0,
                "phase-two tint must preserve the red channel by default");
        checkClose(config.secondPhaseGreenTint.getDefault(), 0.35,
                "phase-two tint must reduce the green channel by default");
        checkClose(config.secondPhaseBlueTint.getDefault(), 0.35,
                "phase-two tint must reduce the blue channel by default");
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