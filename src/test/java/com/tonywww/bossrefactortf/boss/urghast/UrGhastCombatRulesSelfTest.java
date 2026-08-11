package com.tonywww.bossrefactortf.boss.urghast;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;

public final class UrGhastCombatRulesSelfTest {
    private UrGhastCombatRulesSelfTest() {
    }

    public static void main(String[] args) {
        check(!UrGhastCombatRules.shouldTriggerHalfHealthOffensive(
                        false, 50.0F, 100.0F),
                "exactly 50% health must remain in patrol mode");
        check(UrGhastCombatRules.shouldTriggerHalfHealthOffensive(
                        false, 49.99F, 100.0F),
                "health below 50% must force offensive mode");
        check(!UrGhastCombatRules.shouldTriggerHalfHealthOffensive(
                        true, 25.0F, 100.0F),
                "the half-health offensive attack must trigger only once");

        check(!UrGhastCombatRules.shouldSwitchMode(399),
                "a mode must not switch before twenty seconds");
        check(UrGhastCombatRules.shouldSwitchMode(400),
                "a mode must switch after twenty seconds");

        check(UrGhastCombatRules.crossesMinionSummonThreshold(
                        250.0F, 235.0F, 250.0F, 6.0),
                "crossing 94% health must summon one minion wave");
        check(!UrGhastCombatRules.crossesMinionSummonThreshold(
                        235.0F, 232.0F, 250.0F, 6.0),
                "damage below an already crossed threshold must not summon");
        check(UrGhastCombatRules.crossesMinionSummonThreshold(
                        240.0F, 232.0F, 250.0F, 6.0),
                "healing above 94% must allow that threshold to trigger again");
        check(UrGhastCombatRules.crossesMinionSummonThreshold(
                        250.0F, 200.0F, 250.0F, 6.0),
                "one hit crossing several thresholds must request one wave");
        check(!UrGhastCombatRules.crossesMinionSummonThreshold(
                        220.0F, 221.0F, 250.0F, 6.0),
                "healing must never summon minions by itself");

        checkClose(UrGhastCombatRules.tantrumIncomingDamage(20.0), 5.0,
                "75% tantrum reduction must leave 25% incoming damage");
        checkClose(UrGhastCombatRules.giantFireballDamage(12.0), 18.0,
                "giant fireballs must use 150% attack damage");
        check(UrGhastCombatRules.giantFireballExtraProjectiles(false) == 0,
                "patrol mode must add no projectile to the primary fireball");
        check(UrGhastCombatRules.giantFireballExtraProjectiles(true) == 1,
                "tantrum mode must add one projectile to the primary fireball");
        checkClose(UrGhastCombatRules.mistDamage(12.0), 2.4,
                "mist must deal 20% attack damage each second");

        checkClose(UrGhastCombatRules.fireballDamageScale(0.0, 4.0), 1.0,
                "fireball damage must be 100% at the center");
        checkClose(UrGhastCombatRules.fireballDamageScale(2.0, 4.0), 0.75,
                "fireball damage must fall linearly inside the radius");
        checkClose(UrGhastCombatRules.fireballDamageScale(4.0, 4.0), 0.5,
                "fireball damage must be 50% at the edge");
        checkClose(UrGhastCombatRules.fireballDamageScale(4.01, 4.0), 0.0,
                "fireball damage must stop outside the marked radius");
        checkClose(UrGhastCombatRules.fireballDamage(12.0, 4.0, 4.0), 9.0,
                "edge damage must be half of the 150% base damage");
        checkClose(UrGhastCombatRules.aabbDistanceFromCenter(
                        4.0, 4.0, 4.0),
                4.0, "AABB corners must use Chebyshev distance");
        checkClose(UrGhastCombatRules.fireballDamageScale(
                        UrGhastCombatRules.aabbDistanceFromCenter(
                                4.0, 4.0, 4.0),
                        4.0),
                0.5, "every marked AABB corner must receive 50% damage");

        check(UrGhastCombatRules.trapAffectsOffensiveMode(
                        99, 80, 120, 0.5),
                "the first half of active trap control must affect offensive mode");
        check(!UrGhastCombatRules.trapAffectsOffensiveMode(
                        100, 80, 120, 0.5),
                "the second half of active trap control must be ignored");
        check(!UrGhastCombatRules.isIntervalPulse(18, 20),
                "mist must not damage before one complete second");
        check(UrGhastCombatRules.isIntervalPulse(19, 20),
                "mist must damage after one complete second");
        check(UrGhastCombatRules.isIntervalPulse(39, 20),
                "mist damage must repeat once per second");

        check(UrGhastCombatMode.PATROL.opposite()
                        == UrGhastCombatMode.OFFENSIVE,
                "patrol must alternate to offensive mode");
        check(UrGhastCombatMode.OFFENSIVE.opposite()
                        == UrGhastCombatMode.PATROL,
                "offensive mode must alternate back to patrol");

        BossRefactorTwilightForestConfig.UrGhastConfig config =
                BossRefactorTwilightForestConfig.UR_GHAST;
        checkClose(config.baseAttackDamage.getDefault(), 10.0,
                "default Ur-Ghast attack damage must be ten");
        check(config.hurtSoundCooldownTicks.getDefault() == 20,
                "hurt sounds must have a one-second cooldown");
        checkClose(config.tantrumDamageReduction.getDefault(), 0.75,
                "enraged damage reduction must default to 75%");
        checkClose(config.minionSummonHealthIntervalPercent.getDefault(), 6.0,
                "minion waves must default to six-percent health intervals");
        check(config.modeDurationTicks.getDefault() == 400,
                "mode duration must default to twenty seconds");
        checkClose(config.offensiveSpeedMultiplier.getDefault(), 1.3,
                "offensive flight speed must increase by 30%");
        checkClose(config.offensiveHeightOffset.getDefault(), 4.0,
                "offensive chase height must remain melee-accessible");
        checkClose(config.offensiveTrapDurationMultiplier.getDefault(), 0.5,
                "offensive trap control must be halved");
        check(config.fireballWindupTicks.getDefault() == 20,
                "fireball windup must default to one second");
        check(config.fireballCooldownTicks.getDefault() == 60,
                "ordinary fireballs must default to a three-second cooldown");
        checkClose(config.fireballVisualScale.getDefault(), 9.0,
                "Ur-Ghast fireballs must render at triple vanilla size");
        checkClose(config.fireballExplosionRadius.getDefault(), 4.0,
                "fireball explosion AABBs must have four-block half-size");
        check(config.fireballIgniteTicks.getDefault() == 100,
                "fireballs must ignite for five seconds");
        check(config.mistWindupTicks.getDefault() == 60,
                "mist must have a three-second harmless warning");
        check(config.mistDamageDurationTicks.getDefault() == 100,
                "mist damage must last five seconds");
        check(config.mistCooldownTicks.getDefault() == 200,
                "mist cooldown must default to ten seconds");
        checkClose(config.mistRadius.getDefault(), 6.0,
                "mist columns must have six-block radius");
        checkClose(config.mistHeight.getDefault(), 24.0,
                "mist columns must be twenty-four blocks high");
        check(config.tantrumRedSmokeParticlesPerTick.getDefault() == 4,
                "tantrum mode must add four red smoke particles per tick");
        check(config.tantrumEnergyRingIntervalTicks.getDefault() == 10,
                "tantrum energy rings must repeat every ten ticks");
        check(config.tantrumEnergyRingParticles.getDefault() == 24,
                "tantrum energy rings must contain twenty-four particles");
        checkClose(UrGhastCombatRules.giantFireballDamage(
                        config.baseAttackDamage.getDefault(),
                        config.fireballDamageMultiplier.getDefault()),
                15.0, "configured fireball center damage must be fifteen");
        checkClose(UrGhastCombatRules.mistDamage(
                        config.baseAttackDamage.getDefault(),
                        config.mistDamageMultiplier.getDefault()),
                2.0, "configured mist pulse damage must be two");

        UrGhastCombatState savedState = new UrGhastCombatState();
        savedState.mode = UrGhastCombatMode.OFFENSIVE;
        savedState.action = UrGhastActionState.MIST_DAMAGE;
        savedState.halfHealthAttackTriggered = true;
        savedState.modeTicks = 123;
        savedState.actionTicks = 45;
        savedState.actionDuration = 100;
        savedState.mistDestinationX = 10.5;
        savedState.mistDestinationY = 20.0;
        savedState.mistDestinationZ = -4.5;
        UrGhastCombatState loadedState = new UrGhastCombatState();
        loadedState.load(savedState.save());
        check(loadedState.mode == UrGhastCombatMode.OFFENSIVE,
                "combat mode must survive an NBT round-trip");
        check(loadedState.action == UrGhastActionState.MIST_DAMAGE,
                "active mist must survive an NBT round-trip");
        check(loadedState.halfHealthAttackTriggered
                        && loadedState.modeTicks == 123,
                "half-health attack state and timer must survive reload");
        checkClose(loadedState.mistDestinationX, 10.5,
                "mist destination must survive reload");
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