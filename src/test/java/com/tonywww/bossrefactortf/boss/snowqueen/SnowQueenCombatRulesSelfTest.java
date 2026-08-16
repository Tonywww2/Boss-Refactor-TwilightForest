package com.tonywww.bossrefactortf.boss.snowqueen;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public final class SnowQueenCombatRulesSelfTest {
    private SnowQueenCombatRulesSelfTest() {
    }

    public static void main(String[] args) {
        check(SnowQueenCombatRules.cruiseTicks(20, 40, 0.0) == 20,
                "a zero cruise roll must select the minimum");
        check(SnowQueenCombatRules.cruiseTicks(20, 40, 1.0) == 40,
                "a one cruise roll must select the maximum");
        check(SnowQueenCombatRules.cruiseTicks(40, 20, 0.5) == 30,
                "cruise bounds must be normalized before rolling");
        check(SnowQueenCombatRules.chooseSkill(
                        SnowQueenSkill.REPEATED_SLAM, true, false)
                        == SnowQueenSkill.SHATTERED_ICE_RAIN,
                "repeat prevention must select the other skill");
        check(SnowQueenCombatRules.chooseSkill(
                        SnowQueenSkill.REPEATED_SLAM, false, false)
                        == SnowQueenSkill.REPEATED_SLAM,
                "disabled repeat prevention must allow a repeated skill");
        check(SnowQueenCombatRules.shieldCount(-1, 8) == 0,
                "shield counts must clamp below zero");
        check(SnowQueenCombatRules.shieldCount(9, 8) == 8,
                "shield counts must clamp to available capacity");
        checkClose(SnowQueenCombatRules.shieldAngleDegrees(1, 8, 0, 0.0),
                45.0, "eight shields must be forty-five degrees apart");
        checkClose(SnowQueenCombatRules.shieldAngleDegrees(1, 4, 0, 0.0),
                90.0, "four shields must be ninety degrees apart");
        checkClose(SnowQueenCombatRules.shieldAngleDegrees(1, 8, 3, 5.0),
                60.0, "shield angular speed must advance every tick");
        check(SnowQueenCombatRules.isSecondPhase(50.0F, 100.0F, 0.5),
                "exactly half health must enter the second phase");
        check(!SnowQueenCombatRules.isSecondPhase(50.01F, 100.0F, 0.5),
                "health above half must remain in the first phase");
        check(SnowQueenCombatRules.isInsideCircle(3.0, 4.0, 0.0, 0.0, 5.0),
                "the circle boundary must be included");
        check(!SnowQueenCombatRules.isInsideCircle(
                        3.01, 4.0, 0.0, 0.0, 5.0),
                "points outside the circle must be excluded");
        checkClose(SnowQueenCombatRules.scaledDamage(10.0, 1.5), 15.0,
                "damage must scale by the configured multiplier");
        checkClose(SnowQueenCombatRules.scaledDamage(-10.0, 1.5), 0.0,
                "scaled damage must not be negative");

        checkConfigDefaults();
        checkStateRoundTrip();
        checkSummonDataRoundTrip();
    }

    private static void checkConfigDefaults() {
        BossRefactorTwilightForestConfig.SnowQueenConfig config =
                BossRefactorTwilightForestConfig.SNOW_QUEEN;
        check(config.enabled.getDefault(), "Snow Queen combat must be enabled");
        check(config.cruiseMinimumTicks.getDefault() == 20,
                "minimum cruise must default to twenty ticks");
        check(config.cruiseMaximumTicks.getDefault() == 40,
                "maximum cruise must default to forty ticks");
        check(config.avoidImmediateSkillRepeat.getDefault(),
                "skill repeats must be avoided by default");

        check(config.firstPhaseIceShieldCount.getDefault() == 8,
                "first phase must use eight ice shields");
        check(config.secondPhaseIceShieldCount.getDefault() == 4,
                "second phase must use four ice shields");
        checkClose(config.iceShieldOrbitRadius.getDefault(), 1.5,
                "ice shields must use the default orbit radius");
        checkClose(config.iceShieldAngularSpeedDegreesPerTick.getDefault(), 5.0,
                "ice shields must use the default angular speed");
        checkClose(config.iceShieldVerticalOffset.getDefault(), 0.0,
                "ice shields must use no vertical offset by default");
        checkClose(config.secondPhaseIceShieldCollisionDamageMultiplier.getDefault(), 0.5,
                "second-phase shield damage must default to one half");
        checkClose(config.iceShieldCollisionHitboxInflation.getDefault(), 0.0,
                "shield collision inflation must default to zero");
        check(config.iceShieldCollisionDamageCooldownTicks.getDefault() == 10,
                "shield collision cooldown must default to ten ticks");
        check(config.iceShieldCollisionHitsNonPlayers.getDefault(),
                "shield collisions must hit non-players by default");

        check(config.slamCount.getDefault() == 2,
                "slams must default to two impacts");
        check(config.slamWindupTicks.getDefault() == 20,
                "slam windup must default to twenty ticks");
        checkClose(config.slamPreparationHeight.getDefault(), 6.0,
                "slam preparation height must default to six");
        checkClose(config.slamDescentSpeed.getDefault(), 1.5,
                "slam descent speed must default to one and a half");
        check(config.slamMaximumDescentTicks.getDefault() == 30,
                "slam descent must cap at thirty ticks");
        checkClose(config.slamRadius.getDefault(), 4.0,
                "slam radius must default to four");
        checkClose(config.slamDamageMultiplier.getDefault(), 1.5,
                "slam damage must default to one and a half");
        check(config.slamHitsNonPlayers.getDefault(),
                "slams must hit non-players by default");
        check(config.betweenSlamsTicks.getDefault() == 10,
                "the delay between slams must default to ten ticks");
        check(config.slamShieldCooldownEnabled.getDefault(),
                "slam shield cooldowns must be enabled by default");
        check(config.slamShieldCooldownTicks.getDefault() == 60,
                "slam shield cooldown must default to sixty ticks");
        check(config.slamSlashBladeParryEnabled.getDefault(),
                "slam SlashBlade parries must be enabled by default");
        check(config.slamParryFeedbackTicks.getDefault() == 20,
                "slam parry feedback must default to twenty ticks");

        check(config.chaseDurationTicks.getDefault() == 100,
                "chase duration must default to one hundred ticks");
        checkClose(config.chaseHeightOffset.getDefault(), 0.0,
                "chase height offset must default to zero");
        checkClose(config.chaseSpeedMultiplier.getDefault(), 1.3,
                "chase speed must default to one point three");
        checkClose(config.chaseContactDamageMultiplier.getDefault(), 1.0,
                "chase damage must default to base attack damage");
        check(config.chaseHitsNonPlayers.getDefault(),
                "chase contact must hit non-players by default");
        check(config.chaseContactDamageIntervalTicks.getDefault() == 10,
                "chase contact interval must default to ten ticks");
        checkClose(config.chaseHitboxInflation.getDefault(), 0.5,
                "chase hitbox inflation must default to one half");

        check(config.shatteredIceRainQueenWindupTicks.getDefault() == 30,
                "rain windup must default to thirty ticks");
        check(config.shatteredIceRainFirstPhaseMinimumCount.getDefault() == 2,
                "rain minimum count must default to two");
        check(config.shatteredIceRainFirstPhaseMaximumCount.getDefault() == 4,
                "rain maximum count must default to four");
        check(config.shatteredIceRainSecondPhaseCount.getDefault() == 4,
                "phase-two rain count must default to four");
        checkClose(config.shatteredIceRainSpawnDistanceBehind.getDefault(), 3.0,
                "rain spawn distance must default to three");
        checkClose(config.shatteredIceRainSpawnSpacing.getDefault(), 1.5,
                "rain spawn spacing must default to one and a half");
        checkClose(config.shatteredIceRainSpawnVerticalOffset.getDefault(), 1.0,
                "rain vertical offset must default to one");
        check(config.shatteredIceRainCrystalWindupTicks.getDefault() == 20,
                "crystal windup must default to twenty ticks");
        check(config.shatteredIceRainLaunchIntervalTicks.getDefault() == 10,
                "rain launch interval must default to ten ticks");
        checkClose(config.shatteredIceRainDashSpeed.getDefault(), 1.5,
                "rain dash speed must default to one and a half");
        check(config.shatteredIceRainMaximumDashTicks.getDefault() == 30,
                "rain dash duration must cap at thirty ticks");
        checkClose(config.shatteredIceRainDashHitboxInflation.getDefault(), 0.25,
                "rain dash hitbox inflation must default to one quarter");
        checkClose(config.shatteredIceRainDashDamageMultiplier.getDefault(), 1.0,
                "rain dash damage must default to base attack damage");
        check(config.shatteredIceRainDashHitsNonPlayers.getDefault(),
                "rain dashes must hit non-players by default");
        check(config.shatteredIceRainFrostedDurationTicks.getDefault() == 140,
                "rain frosted duration must default to one hundred forty ticks");
        check(config.shatteredIceRainFrostedAmplifier.getDefault() == 0,
                "rain frosted amplifier must default to zero");
        check(config.shatteredIceRainExplosionDelayTicks.getDefault() == 60,
                "rain explosion delay must default to sixty ticks");
        checkClose(config.shatteredIceRainExplosionRadius.getDefault(), 1.0,
                "rain explosion radius must default to one");
        check(config.shatteredIceRainPreserveOriginalBlockInteraction.getDefault(),
                "rain block interaction must be preserved by default");

        check(config.maximumLinks.getDefault() == 2,
                "maximum links must default to two");
        checkClose(config.linkSummonDamageBonus.getDefault(), 0.3,
                "link summon damage bonus must default to zero point three");
        checkClose(config.linkQueenDamageReduction.getDefault(), 0.3,
                "link queen damage reduction must default to zero point three");
        check(config.linkTeleportDelayTicks.getDefault() == 20,
                "link teleport delay must default to twenty ticks");
        check(config.linkBurstWindupTicks.getDefault() == 20,
                "link burst windup must default to twenty ticks");
        checkClose(config.linkBurstRadius.getDefault(), 5.0,
                "link burst radius must default to five");
        checkClose(config.linkBurstDamageMultiplier.getDefault(), 1.5,
                "link burst damage must default to one and a half");
        check(config.linkBurstHitsNonPlayers.getDefault(),
                "link bursts must hit non-players by default");
        check(config.phaseTwoStableIceCoreCount.getDefault() == 2,
                "phase two must spawn two stable ice cores");
        checkClose(config.stableIceCoreSpawnRadius.getDefault(), 3.0,
                "stable core spawn radius must default to three");
        checkClose(config.stableIceCoreSpawnVerticalOffset.getDefault(), 0.0,
                "stable core vertical offset must default to zero");

        checkClose(config.phaseTwoHealthThreshold.getDefault(), 0.5,
                "phase-two threshold must default to fifty percent");
        checkClose(config.phaseTwoDamageMultiplier.getDefault(), 1.2,
                "phase-two damage must default to one point two");
        checkClose(config.phaseTwoSpeedMultiplier.getDefault(), 1.2,
                "phase-two speed must default to one point two");

        check(config.linkRed.getDefault() == 80,
                "link red must default to eighty");
        check(config.linkGreen.getDefault() == 180,
                "link green must default to one hundred eighty");
        check(config.linkBlue.getDefault() == 255,
                "link blue must default to two hundred fifty-five");
        check(config.linkAlpha.getDefault() == 220,
                "link alpha must default to two hundred twenty");
        checkClose(config.linkLineWidth.getDefault(), 3.0,
                "link line width must default to three");
        checkClose(config.linkMaximumRenderDistance.getDefault(), 96.0,
                "link render distance must default to ninety-six");
    }

    private static void checkStateRoundTrip() {
        UUID linked = UUID.randomUUID();
        UUID owned = UUID.randomUUID();
        UUID rain = UUID.randomUUID();
        UUID cooldownTarget = UUID.randomUUID();
        AttackTelegraph telegraph = new AttackTelegraph(
                AttackTelegraphShape.CIRCLE,
                1.0F, 2.0F, 3.0F,
                0.0F, 1.0F, 4.0F, 0.0F, 4.0F, 0.75F);
        SnowQueenCombatState savedState = new SnowQueenCombatState();
        savedState.actionState = SnowQueenActionState.LINK_BURST_WINDUP;
        savedState.lastSkill = SnowQueenSkill.SHATTERED_ICE_RAIN;
        savedState.stateTicks = 7;
        savedState.stateDuration = 20;
        savedState.slamsCompleted = 1;
        savedState.secondPhase = true;
        savedState.lockedX = 10.5;
        savedState.lockedY = 64.0;
        savedState.lockedZ = -3.25;
        savedState.lastParryGameTime = 1234L;
        savedState.currentTelegraph = telegraph;
        savedState.linkedSummons.add(linked);
        savedState.ownedSummons.add(owned);
        savedState.rainVolley.add(rain);
        savedState.linkDeathPositions.addLast(new Vec3(1.0, 2.0, 3.0));
        savedState.linkDeathPositions.addLast(new Vec3(-4.0, 5.5, 6.0));
        savedState.shieldCollisionCooldowns.put(cooldownTarget, 4321L);

        SnowQueenCombatState loadedState = new SnowQueenCombatState();
        loadedState.load(savedState.save());
        check(loadedState.actionState == SnowQueenActionState.LINK_BURST_WINDUP,
                "action state must survive an NBT round-trip");
        check(loadedState.lastSkill == SnowQueenSkill.SHATTERED_ICE_RAIN,
                "last skill must survive an NBT round-trip");
        check(loadedState.stateTicks == 7 && loadedState.stateDuration == 20
                        && loadedState.slamsCompleted == 1,
                "combat ticks must survive an NBT round-trip");
        check(loadedState.secondPhase,
                "second-phase state must survive an NBT round-trip");
        checkClose(loadedState.lockedX, 10.5,
                "locked X must survive an NBT round-trip");
        checkClose(loadedState.lockedY, 64.0,
                "locked Y must survive an NBT round-trip");
        checkClose(loadedState.lockedZ, -3.25,
                "locked Z must survive an NBT round-trip");
        check(loadedState.lastParryGameTime == 1234L,
                "parry timestamp must survive an NBT round-trip");
        check(loadedState.linkedSummons.contains(linked)
                        && loadedState.ownedSummons.contains(owned)
                        && loadedState.rainVolley.contains(rain),
                "summon UUID sets must survive an NBT round-trip");
        check(loadedState.linkDeathPositions.size() == 2
                        && loadedState.linkDeathPositions.contains(
                                new Vec3(1.0, 2.0, 3.0))
                        && loadedState.linkDeathPositions.contains(
                                new Vec3(-4.0, 5.5, 6.0)),
                "link death positions must survive an NBT round-trip");
        check(loadedState.shieldCollisionCooldowns.get(cooldownTarget) == 4321L,
                "shield collision cooldowns must survive an NBT round-trip");
        check(loadedState.currentTelegraph.equals(telegraph),
                "telegraph state must survive an NBT round-trip");
    }

    private static void checkSummonDataRoundTrip() {
        UUID owner = UUID.randomUUID();
        UUID summon = UUID.randomUUID();
        Vec3 firstPosition = new Vec3(1.25, 64.0, -3.5);
        Vec3 secondPosition = new Vec3(-8.0, 70.5, 12.0);
        SnowQueenSummonData savedData = new SnowQueenSummonData();
        savedData.markOwnerDead(owner);
        savedData.markSummonDead(summon);
        savedData.enqueue(owner, firstPosition);
        savedData.enqueue(owner, secondPosition);

        SnowQueenSummonData loadedData = SnowQueenSummonData.load(
                savedData.save(new CompoundTag()));
        check(loadedData.isOwnerDead(owner),
                "dead owner tombstones must survive an NBT round-trip");
        check(loadedData.isSummonDead(summon),
                "dead summon records must survive an NBT round-trip");
        List<Vec3> positions = loadedData.drain(owner);
        check(positions.equals(List.of(firstPosition, secondPosition)),
                "pending link deaths must preserve their NBT queue order");
        check(loadedData.drain(owner).isEmpty(),
                "draining pending link deaths must consume the queue");
        loadedData.clearSummonDead(summon);
        check(!loadedData.isSummonDead(summon),
                "dead summon records must be consumable");
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
