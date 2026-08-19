package com.tonywww.bossrefactortf.boss.naga;

import net.minecraft.nbt.CompoundTag;

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
        check(NagaCombatRules.suppressesExternalParryStun(
                        NagaActionState.CHARGE, true, false),
                "local charge stun must replace SenDimS external stun");
        check(NagaCombatRules.suppressesExternalParryStun(
                        NagaActionState.POUNCE_WINDUP, false, false),
                "local pounce stun must replace SenDimS external stun");
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
        check(NagaCombatRules.isChargeShieldBlock(
                        NagaActionState.CHARGE, true, true),
                "an actively raised shield during charge must interrupt");
        check(!NagaCombatRules.isChargeShieldBlock(
                        NagaActionState.POUNCE_FLIGHT, true, true),
                "a pounce shield block must not enter the charge hook");
        check(!NagaCombatRules.isChargeShieldBlock(
                        NagaActionState.CHARGE, false, true),
                "non-shield blocking must not enter the charge hook");
        check(!NagaCombatRules.isChargeShieldBlock(
                        NagaActionState.CHARGE, true, false),
                "an inactive shield must not enter the charge hook");

        check(NagaCombatRules.cruiseTicks(20, 40, 0.0) == 20,
                "minimum cruise roll must use twenty ticks");
        check(NagaCombatRules.cruiseTicks(20, 40, 1.0) == 40,
                "maximum cruise roll must use forty ticks");
        check(List.of(NagaSkill.values()).equals(List.of(
                        NagaSkill.NONE, NagaSkill.CHARGE,
                        NagaSkill.POUNCE, NagaSkill.VENOM_BREATH)),
                "skill pool must contain only charge, pounce, and venom");
        for (NagaSkill previous : new NagaSkill[] {
                NagaSkill.CHARGE,
                NagaSkill.POUNCE,
                NagaSkill.VENOM_BREATH}) {
            for (int roll = 0; roll < 6; roll++) {
                check(NagaCombatRules.nextSkill(
                                previous, true, true, true, roll) != previous,
                        "skill selection must exclude the previous skill");
            }
        }
        check(NagaCombatRules.nextSkill(
                        NagaSkill.POUNCE, false, true, false, 0)
                        == NagaSkill.NONE,
                "cooldowns and repeat exclusion may force another cruise");

        check(NagaCombatRules.isInsideCircle(
                        4.0, 0.0, 0.0, 0.0, 4.0),
                "pounce circle must include its boundary");
        check(!NagaCombatRules.isInsideCircle(
                        4.01, 0.0, 0.0, 0.0, 4.0),
                "pounce circle must exclude points outside the mark");
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
        NagaCombatRules.FireSnakeCycle firstFireSnake =
                NagaCombatRules.advanceFireSnakeCycle(
                        true, 0, 0, 300, 600);
        check(firstFireSnake.started() && firstFireSnake.activeTicks() == 300,
                "phase two must start its first fire snake immediately");
        NagaCombatRules.FireSnakeCycle endedFireSnake =
                NagaCombatRules.advanceFireSnakeCycle(
                        true, 1, 0, 300, 600);
        check(endedFireSnake.ended()
                        && endedFireSnake.cooldownTicks() == 600,
                "an ended fire snake must enter the configured cooldown");
        NagaCombatRules.FireSnakeCycle repeatedFireSnake =
                NagaCombatRules.advanceFireSnakeCycle(
                        true, 0, 1, 300, 600);
        check(repeatedFireSnake.started()
                        && repeatedFireSnake.activeTicks() == 300,
                "an expired cooldown must start another fire snake");

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
        NagaCombatState savedState = new NagaCombatState();
        savedState.actionState = NagaActionState.VENOM_PROJECTILE;
        savedState.lastSkill = NagaSkill.VENOM_BREATH;
        savedState.stateTicks = 4;
        savedState.stateDuration = 21;
        savedState.secondPhase = true;
        savedState.stunImmunityAvailable = false;
        savedState.stunImmunityRecoveryTicks = 200;
        savedState.fireSnakeTicks = 250;
        savedState.fireSnakeCooldownTicks = 42;
        savedState.venomProjectileId = java.util.UUID.randomUUID();
        NagaCombatState loadedState = new NagaCombatState();
        loadedState.load(savedState.save(), 600);
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
        check(loadedState.fireSnakeCooldownTicks == 42,
                "fire snake cooldown must survive reload");
        check(savedState.venomProjectileId.equals(
                        loadedState.venomProjectileId),
                "moving venom identity must survive reload");

        NagaActionState[] legacyActions = {
                NagaActionState.IDLE,
                NagaActionState.CRUISING,
                NagaActionState.POUNCE_WINDUP,
                NagaActionState.POUNCE_FLIGHT,
                NagaActionState.POUNCE_IMPACT,
                NagaActionState.CRUISING,
                NagaActionState.CRUISING,
                NagaActionState.VENOM_WINDUP,
                NagaActionState.VENOM_PROJECTILE,
                NagaActionState.STUNNED
        };
        for (int ordinal = 0; ordinal < legacyActions.length; ordinal++) {
            CompoundTag legacyState = new CompoundTag();
            legacyState.putInt("ActionState", ordinal);
            NagaCombatState migratedState = new NagaCombatState();
            migratedState.load(legacyState, 600);
            check(migratedState.actionState == legacyActions[ordinal],
                    "legacy action ordinal " + ordinal
                            + " must migrate exactly");
        }
        NagaSkill[] legacySkills = {
                NagaSkill.NONE, NagaSkill.POUNCE,
                NagaSkill.NONE, NagaSkill.VENOM_BREATH
        };
        for (int ordinal = 0; ordinal < legacySkills.length; ordinal++) {
            CompoundTag legacyState = new CompoundTag();
            legacyState.putInt("LastSkill", ordinal);
            NagaCombatState migratedState = new NagaCombatState();
            migratedState.load(legacyState, 600);
            check(migratedState.lastSkill == legacySkills[ordinal],
                    "legacy skill ordinal " + ordinal
                            + " must migrate exactly");
        }
        CompoundTag legacySecondPhase = new CompoundTag();
        legacySecondPhase.putBoolean("SecondPhase", true);
        NagaCombatState migratedSecondPhase = new NagaCombatState();
        migratedSecondPhase.load(legacySecondPhase, 600);
        check(migratedSecondPhase.fireSnakeCooldownTicks == 600,
                "legacy inactive phase-two saves must initialize cooldown once");
        CompoundTag stableCharge = new CompoundTag();
        stableCharge.putString("ActionStateId", "charge");
        stableCharge.putString("LastSkillId", "charge");
        NagaCombatState loadedCharge = new NagaCombatState();
        loadedCharge.load(stableCharge, 600);
        check(loadedCharge.actionState == NagaActionState.CHARGE
                        && loadedCharge.lastSkill == NagaSkill.CHARGE,
                "charge state and skill must use stable string identifiers");
        CompoundTag removedTail = new CompoundTag();
        removedTail.putString("ActionStateId", "tail_strike");
        removedTail.putString("LastSkillId", "tail_sweep");
        NagaCombatState migratedTail = new NagaCombatState();
        migratedTail.load(removedTail, 600);
        check(migratedTail.actionState == NagaActionState.CRUISING
                        && migratedTail.lastSkill == NagaSkill.NONE,
                "removed tail string identifiers must migrate safely");

        check(NagaCombatDefaults.CHARGE_COOLDOWN_TICKS == 100,
                "default vanilla charge cooldown must be 100 ticks");
        check(NagaCombatDefaults.FIRE_SNAKE_DURATION_TICKS == 300,
                "default fire snake must last 300 ticks");
        check(NagaCombatDefaults.FIRE_SNAKE_COOLDOWN_TICKS == 600,
                "default fire snake cooldown must last 600 ticks");
        check(NagaCombatDefaults.FIRE_POINT_DURATION_TICKS == 200,
                "default fire points must last 200 ticks");
        checkClose(NagaCombatDefaults.FIRE_DAMAGE_MULTIPLIER, 0.3,
                "default fire point damage must be attack damage times 0.3");
        check(NagaCombatDefaults.FIRE_DAMAGE_INTERVAL_TICKS == 20,
                "default fire point damage interval must be 20 ticks");
        check(NagaCombatDefaults.FIRE_IGNITE_TICKS == 100,
                "default fire point ignition must last 100 ticks");
        checkClose(NagaCombatDefaults.SECOND_PHASE_ATTACK_MULTIPLIER, 1.2,
                "phase two attack damage must be multiplied by 1.2");
        checkClose(NagaCombatDefaults.SECOND_PHASE_SPEED_MULTIPLIER, 1.2,
                "phase two movement speed must be multiplied by 1.2");
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