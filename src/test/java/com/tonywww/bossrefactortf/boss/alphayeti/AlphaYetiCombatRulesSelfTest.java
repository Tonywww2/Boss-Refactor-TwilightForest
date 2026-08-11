package com.tonywww.bossrefactortf.boss.alphayeti;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;

public final class AlphaYetiCombatRulesSelfTest {
    private AlphaYetiCombatRulesSelfTest() {
    }

    public static void main(String[] args) {
        check(AlphaYetiCombatRules.fatigueAfterSkill(0) == 1,
                "every completed skill must add one fatigue");
        check(AlphaYetiCombatRules.fatigueAfterPursuit(1, false) == 2,
                "an ordinary pursuit must add one fatigue");
        check(AlphaYetiCombatRules.fatigueAfterPursuit(1, true) == 3,
                "an imbalance-stopped pursuit must add one extra fatigue");
        check(!AlphaYetiCombatRules.shouldRest(2),
                "two fatigue must not trigger rest");
        check(AlphaYetiCombatRules.shouldRest(3),
                "three fatigue must trigger rest");

        check(!AlphaYetiCombatRules.reachesImbalanceThreshold(2),
                "two imbalance stacks must not stop pursuit");
        check(AlphaYetiCombatRules.reachesImbalanceThreshold(3),
                "three imbalance stacks must stop pursuit");

        check(!AlphaYetiCombatRules.isSecondPhase(50.0F, 100.0F),
                "exactly half health must remain in phase one");
        check(AlphaYetiCombatRules.isSecondPhase(49.99F, 100.0F),
                "health below half must enter phase two");
        check(AlphaYetiCombatRules.restTicks(200, false) == 200,
                "phase-one rest must last ten seconds");
        check(AlphaYetiCombatRules.restTicks(200, true) == 100,
                "phase-two rest must be halved");

        checkClose(AlphaYetiCombatRules.pursuitDamage(12.0), 12.0,
                "pursuit damage must use 100% attack damage");
        checkClose(AlphaYetiCombatRules.stompDamage(12.0), 18.0,
                "stomp damage must use 150% attack damage");
        checkClose(AlphaYetiCombatRules.fallingIceDamage(12.0), 12.0,
                "falling ice damage must use 100% attack damage");

        check(AlphaYetiCombatRules.cruiseTicks(15, 35, 0.0) == 15,
                "the minimum cruise roll must use the configured minimum");
        check(AlphaYetiCombatRules.cruiseTicks(15, 35, 1.0) == 35,
                "the maximum cruise roll must use the configured maximum");

        for (AlphaYetiSkill previous : new AlphaYetiSkill[] {
                AlphaYetiSkill.RAMPAGE,
                AlphaYetiSkill.PURSUIT,
                AlphaYetiSkill.STOMP}) {
            check(AlphaYetiCombatRules.nextSkill(previous, 0) != previous,
                    "skill selection must not immediately repeat the previous skill");
            check(AlphaYetiCombatRules.nextSkill(previous, 1) != previous,
                    "both random branches must exclude the previous skill");
        }
        check(AlphaYetiCombatRules.nextSkill(AlphaYetiSkill.NONE, 0)
                        == AlphaYetiSkill.RAMPAGE,
                "the first skill roll must include rampage");
        check(AlphaYetiCombatRules.nextSkill(AlphaYetiSkill.NONE, 2)
                        == AlphaYetiSkill.STOMP,
                "the first skill roll must include stomp");

        checkClose(AlphaYetiCombatRules.fallingIceRingRadius(1, 4, 12.0),
                3.0, "the first falling-ice ring must begin at three blocks");
        checkClose(AlphaYetiCombatRules.fallingIceRingRadius(4, 4, 12.0),
                12.0, "the fourth falling-ice ring must reach twelve blocks");
        checkClose(AlphaYetiCombatRules.fallingIceRingHalfWidth(4, 12.0),
                1.5, "adjacent falling-ice hit bands must meet without gaps");
        check(AlphaYetiCombatRules.isInsideRing(4.5, 3.0, 1.5),
                "a ring must include its outer boundary");
        check(AlphaYetiCombatRules.isInsideRing(4.5, 6.0, 1.5),
                "adjacent rings must share their boundary");
        check(!AlphaYetiCombatRules.isInsideRing(4.6, 3.0, 1.5),
                "a ring must not damage outside its marked band");
        check(!AlphaYetiCombatRules.isIntervalPulse(18, 20),
                "pursuit damage must not occur before one complete second");
        check(AlphaYetiCombatRules.isIntervalPulse(19, 20),
                "pursuit damage must occur after one complete second");
        check(AlphaYetiCombatRules.isIntervalPulse(39, 20),
                "pursuit damage must repeat once per second");
        checkClose(AlphaYetiCombatRules.blockDestructionMinimumY(64.0),
                64.0,
                "block destruction must begin above the support block");
        checkClose(AlphaYetiCombatRules.blockDestructionMinimumY(63.999999),
                64.0,
                "floating-point ground contact must not expose the support block");
        checkClose(AlphaYetiCombatRules.blockDestructionMinimumY(64.25),
                64.0,
                "blocks intersecting the yeti body may still be destroyed");
        double fallingIceVelocity =
                AlphaYetiCombatRules.fallingIceInitialVelocity(10.0, 20);
        checkClose(AlphaYetiCombatRules.fallingIceTravelDistance(
                        fallingIceVelocity, 20),
                10.0,
                "falling-ice visuals must reach the impact height on the damage tick");

        AlphaYetiCombatState savedState = new AlphaYetiCombatState();
        savedState.attackState = AlphaYetiAttackState.STOMP_ICE;
        savedState.lastSkill = AlphaYetiSkill.STOMP;
        savedState.stateTicks = 7;
        savedState.stateDuration = 20;
        savedState.fatigue = 2;
        savedState.ringIndex = 3;
        AlphaYetiCombatState.IceStrike savedStrike =
                new AlphaYetiCombatState.IceStrike(
                        1.0, 2.0, 3.0, 2.5F, 20);
        savedStrike.remainingTicks = 8;
        savedStrike.visualsSpawned = true;
        savedState.secondaryIce.add(savedStrike);
        AlphaYetiCombatState loadedState = new AlphaYetiCombatState();
        loadedState.load(savedState.save());
        check(loadedState.attackState == AlphaYetiAttackState.STOMP_ICE,
                "the active skill must survive an NBT round-trip");
        check(loadedState.lastSkill == AlphaYetiSkill.STOMP,
                "the previous-skill exclusion must survive an NBT round-trip");
        check(loadedState.stateTicks == 7 && loadedState.stateDuration == 20,
                "the active skill timer must survive an NBT round-trip");
        check(loadedState.fatigue == 2 && loadedState.ringIndex == 3,
                "fatigue and falling-ice ring progress must survive reload");
        AlphaYetiCombatState.IceStrike loadedStrike =
                loadedState.secondaryIce.peekFirst();
        check(loadedStrike != null && loadedStrike.remainingTicks == 8,
                "pending second-phase ice must survive reload");
        check(loadedStrike.visualsSpawned,
                "saved visual ice must not be duplicated after reload");

        BossRefactorTwilightForestConfig.AlphaYetiConfig config =
                BossRefactorTwilightForestConfig.ALPHA_YETI;
        check(config.cruiseMinimumTicks.getDefault() == 15,
                "default minimum cruise must be 0.75 seconds");
        check(config.cruiseMaximumTicks.getDefault() == 35,
                "default maximum cruise must be 1.75 seconds");
        check(config.restDurationTicks.getDefault() == 200,
                "default rest must be ten seconds");
        check(config.pursuitDurationTicks.getDefault() == 120,
                "default pursuit must be six seconds");
        checkClose(config.pursuitSpeedMultiplier.getDefault(), 1.5,
                "default pursuit navigation speed must be 1.5 times");
        check(config.pursuitKnockbackIntervalTicks.getDefault() == 5,
                "pursuit knockback must pulse every quarter second");
        checkClose(config.pursuitKnockbackStrength.getDefault(), 0.6,
                "default pursuit knockback must match the approved value");
        check(config.stompWindupTicks.getDefault() == 30,
                "default stomp windup must be 1.5 seconds");
        checkClose(config.stompRadius.getDefault(), 10.0,
                "default stomp radius must be ten blocks");
        checkClose(config.stompHorizontalKnockback.getDefault(), 0.8,
                "default stomp horizontal knockback must match the approved value");
        checkClose(config.stompVerticalVelocity.getDefault(), 1.0,
                "default stomp vertical velocity must match the approved value");
        check(config.fallingIceRingCount.getDefault() == 4,
                "default stomp must emit four falling-ice rings");
        checkClose(config.fallingIceMaximumRadius.getDefault(), 12.0,
                "the final falling-ice ring must reach twelve blocks");
        check(config.secondPhaseIceWindupTicks.getDefault() == 20,
                "second-phase falling ice must have a one-second warning");
        checkClose(config.secondPhaseIceRadius.getDefault(), 2.5,
                "second-phase falling ice must mark a 2.5-block radius");
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