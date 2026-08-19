package com.tonywww.bossrefactortf.boss.lich;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;

public final class LichCombatRulesSelfTest {
    private LichCombatRulesSelfTest() {
    }

    public static void main(String[] args) {
        check(LichCombatRules.shouldEnterSecondPhase(
                        false, false, 1, 0),
                "the first exhausted vanilla shield must enter phase two");
        check(!LichCombatRules.shouldEnterSecondPhase(
                        true, false, 1, 0),
                "phase two must never initialize twice");
        check(!LichCombatRules.shouldEnterSecondPhase(
                        false, true, 1, 0),
                "custom clones must not initialize phase two");
        check(LichCombatRules.shouldEnterSecondPhase(
                        false, false, 0, 0),
                "a legacy zero-shield Lich must migrate into phase two");

        checkClose(LichCombatRules.damageAfterShieldReduction(
                        20.0, 5, 0.1),
                10.0, "five shield layers must prevent fifty percent");
        checkClose(LichCombatRules.damageAfterShieldReduction(
                        20.0, 2, 0.1),
                16.0, "two shield layers must prevent twenty percent");
        checkClose(LichCombatRules.damageAfterShieldReduction(
                        20.0, 11, 0.1),
                0.0, "shield reduction must clamp at one hundred percent");
        checkClose(LichCombatRules.meleeMagicDamage(10.0, 0.2),
                2.0, "melee magic must deal twenty percent attack damage");
        checkClose(LichCombatRules.shieldBashDamage(
                        10.0, 1.5, 1.5, false),
                15.0, "ordinary shield bash must deal 150 percent");
        checkClose(LichCombatRules.shieldBashDamage(
                        10.0, 1.5, 1.5, true),
                22.5, "empowered shield bash must deal 225 percent");

        LichCombatRules.ShieldBashHitDecision blocked =
                LichCombatRules.shieldBashHitDecision(true, false);
        check(!blocked.dealsDamage() && blocked.appliesKnockback(),
                "blocked shield bashes must knock back without damage");
        LichCombatRules.ShieldBashHitDecision parried =
                LichCombatRules.shieldBashHitDecision(false, true);
        check(!parried.dealsDamage() && parried.appliesKnockback(),
                "parried shield bashes must knock back without damage");

        check(LichCombatRules.cruiseTicks(40, 80, 0.0) == 40,
                "minimum cruise roll must use forty ticks");
        check(LichCombatRules.cruiseTicks(40, 80, 1.0) == 80,
                "maximum cruise roll must use eighty ticks");
        check(LichCombatRules.shouldSummonClone(
                        true, false, false, 0),
                "a phase-two master without a clone must summon when ready");
        check(!LichCombatRules.shouldSummonClone(
                        true, false, true, 0),
                "a living custom clone must suppress another summon");
        check(LichCombatRules.isCurrentAttackParryable(
                        LichActionState.SHIELD_BASH_WINDUP),
                "shield bash windup must be parryable");
        check(!LichCombatRules.isCurrentAttackParryable(
                        LichActionState.CRUISE),
                "ordinary cruise must not be parryable");

        check(LichCombatRules.intersectsLockedCorridor(
                        8.4, 0.0, 2.0, 3.4, 1.0,
                        0.0, 0.0, 0.0, 1.0, 0.0,
                        8.0, 3.0, 4.0),
                "target radius must extend the far and side boundaries");
        check(!LichCombatRules.intersectsLockedCorridor(
                        8.6, 0.0, 2.0, 3.6, 1.0,
                        0.0, 0.0, 0.0, 1.0, 0.0,
                        8.0, 3.0, 4.0),
                "target outside both expanded boundaries must miss");
        check(LichCombatRules.intersectsLockedCorridor(
                        2.0, 3.5, 4.5, 0.0, 0.6,
                        0.0, 0.0, 0.0, 1.0, 0.0,
                        8.0, 3.0, 4.0),
                "vertical AABB overlap must count as a hit");
        check(!LichCombatRules.intersectsLockedCorridor(
                        2.0, 4.01, 5.0, 0.0, 0.6,
                        0.0, 0.0, 0.0, 1.0, 0.0,
                        8.0, 3.0, 4.0),
                "target entirely above the configured height must miss");
        double diagonal = Math.sqrt(0.5);
        check(LichCombatRules.intersectsLockedCorridor(
                        0.283, 0.0, 2.0, 5.374, 1.0,
                        0.0, 0.0, 0.0, diagonal, diagonal,
                        8.0, 3.0, 4.0),
                "diagonal corridors must use the target AABB projection");

        LichCombatState savedState = new LichCombatState();
        savedState.actionState = LichActionState.SHIELD_BASH_WINDUP;
        savedState.stateTicks = 4;
        savedState.stateDuration = 30;
        savedState.secondPhase = true;
        savedState.shieldTracked = true;
        savedState.shieldLayers = 3;
        savedState.shieldBashCooldown = 75;
        savedState.summonCooldown = 320;
        savedState.cloneLifetimeTicks = 180;
        savedState.shieldBashEmpowered = true;
        savedState.shieldBashParried = true;
        savedState.secondPhasePending = true;
        LichCombatState loadedState = new LichCombatState();
        loadedState.load(savedState.save());
        check(loadedState.actionState == LichActionState.SHIELD_BASH_WINDUP,
                "active shield bash state must survive an NBT round-trip");
        check(loadedState.secondPhase && loadedState.shieldTracked,
                "phase and shield tracking must survive an NBT round-trip");
        check(loadedState.shieldLayers == 3,
                "shield layers must survive an NBT round-trip");
        check(loadedState.shieldBashCooldown == 75,
                "shield bash cooldown must survive an NBT round-trip");
        check(loadedState.summonCooldown == 320,
                "summon cooldown must survive an NBT round-trip");
        check(loadedState.cloneLifetimeTicks == 180,
                "clone lifetime must survive an NBT round-trip");
        check(loadedState.shieldBashEmpowered && loadedState.shieldBashParried,
                "shield bash flags must survive an NBT round-trip");
        check(loadedState.secondPhasePending,
                "deferred phase transition must survive an NBT round-trip");

        BossRefactorTwilightForestConfig.LichConfig config =
                BossRefactorTwilightForestConfig.LICH;
        check(config.enabled.getDefault(),
                "Lich combat takeover must be enabled by default");
        check(config.secondPhaseShieldLayers.getDefault() == 5,
                "phase two must begin with five shield layers");
        checkClose(config.shieldDamageReductionPerLayer.getDefault(), 0.1,
                "each shield layer must reduce damage by ten percent");
        checkClose(config.meleeMagicDamageMultiplier.getDefault(), 0.2,
                "melee magic must default to twenty percent attack damage");
        check(config.meleeCooldownTicks.getDefault() == 20,
                "ordinary melee cooldown must default to twenty ticks");
        check(config.shieldBashWindupTicks.getDefault() == 30,
                "shield bash windup must last thirty ticks");
        check(config.shieldBashCooldownTicks.getDefault() == 120,
                "shield bash cooldown must last 120 ticks");
        checkClose(config.shieldBashLength.getDefault(), 8.0,
                "shield bash length must be eight blocks");
        checkClose(config.shieldBashHalfWidth.getDefault(), 3.0,
                "shield bash half-width must be three blocks");
        checkClose(config.shieldBashHeight.getDefault(), 4.0,
                "shield bash height must be four blocks");
        checkClose(config.shieldBashDamageMultiplier.getDefault(), 1.5,
                "shield bash must default to 150 percent");
        checkClose(config.shieldBashShieldMultiplier.getDefault(), 1.5,
                "shield-consumed bash multiplier must be 150 percent");
        checkClose(config.shieldBashHorizontalKnockback.getDefault(), 1.5,
                "shield bash horizontal knockback must be 1.5");
        checkClose(config.shieldBashVerticalVelocity.getDefault(), 0.5,
                "shield bash vertical velocity must be 0.5");
        check(config.shieldBashStunTicks.getDefault() == 40,
                "shield bash stun must last forty ticks");
        check(config.shieldBashParryFeedbackTicks.getDefault() == 40,
                "shield bash parry feedback must last forty ticks");
        check(config.summonWindupTicks.getDefault() == 40,
                "clone summon windup must last forty ticks");
        check(config.summonCooldownTicks.getDefault() == 400,
                "clone summon cooldown must last 400 ticks");
        check(config.cloneLifetimeTicks.getDefault() == 300,
                "clones must live for 300 ticks");
        check(config.cruiseMinimumTicks.getDefault() == 40,
                "minimum melee cruise must last forty ticks");
        check(config.cruiseMaximumTicks.getDefault() == 80,
                "maximum melee cruise must last eighty ticks");
        checkClose(config.meleeSpeed.getDefault(), 0.75,
                "melee cruise speed must be 0.75");
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