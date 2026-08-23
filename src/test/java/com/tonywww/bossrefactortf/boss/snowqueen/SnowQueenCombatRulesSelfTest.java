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
        check(SnowQueenCombatRules.shieldCount(-1, 6) == 0,
                "shield counts must clamp below zero");
        check(SnowQueenCombatRules.shieldCount(9, 6) == 6,
                "shield counts must clamp to available capacity");
        checkClose(SnowQueenCombatRules.shieldAngleDegrees(1, 6, 0, 0.0),
                60.0, "six shields must be sixty degrees apart");
        checkClose(SnowQueenCombatRules.shieldWaveOffset(1, 6, 0, 0.4, 6.0),
                0.2 * Math.sqrt(3.0),
                "adjacent shields must use staggered wave phases");
        checkClose(SnowQueenCombatRules.shieldWaveOffset(0, 6, 15, 0.4, 6.0),
                0.4, "shield waves must advance with tick count");
        checkClose(SnowQueenCombatRules.shieldWaveOffset(0, 0, 15, 0.4, 6.0),
                0.0, "empty shield waves must remain at zero");
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
        check(SnowQueenCombatRules.fallingIceCooldownBeforeWindup(
                        160, 20) == 140,
                "falling ice windup must fit inside the impact interval");
        check(SnowQueenCombatRules.fallingIceCooldownBeforeWindup(
                        10, 20) == 1,
                "falling ice cooldowns must remain positive");
        check(SnowQueenCombatRules.configuredDropRandomBound(2) == 1,
                "two drops must use a one-value random bound");
        check(SnowQueenCombatRules.configuredDropRandomBound(3) == 2,
                "three drops must use a two-value random bound");

        checkConfigDefaults();
        checkStateRoundTrip();
        checkSummonDataRoundTrip();
    }

    private static void checkConfigDefaults() {
        BossRefactorTwilightForestConfig.SnowQueenConfig config =
                BossRefactorTwilightForestConfig.SNOW_QUEEN;
        check(config.enabled.getDefault(), "Snow Queen combat must be enabled");
        check(config.firstPhaseIceShieldCount.getDefault() == 6,
                "first phase must use six ice shields");
        check(config.secondPhaseIceShieldCount.getDefault() == 6,
                "second phase must use six ice shields");
        checkClose(config.iceShieldOrbitRadius.getDefault(), 1.5,
                "ice shields must use the default orbit radius");
        checkClose(config.iceShieldAngularSpeedDegreesPerTick.getDefault(), 5.0,
                "ice shields must use the default angular speed");
        checkClose(config.iceShieldWaveAmplitude.getDefault(), 0.4,
                "ice shields must use the default wave amplitude");
        checkClose(config.iceShieldWaveSpeedDegreesPerTick.getDefault(), 6.0,
                "ice shields must use the default wave speed");
        checkClose(config.iceShieldVerticalOffset.getDefault(), 0.0,
                "ice shields must use no vertical offset by default");
        checkClose(config.iceShieldCollisionDamageMultiplier.getDefault(), 1.0,
                "shield damage must default to base attack damage");
        checkClose(config.iceShieldCollisionHitboxInflation.getDefault(), 0.0,
                "shield collision inflation must default to zero");
        check(config.iceShieldCollisionDamageCooldownTicks.getDefault() == 10,
                "shield collision cooldown must default to ten ticks");
        check(config.iceShieldCollisionHitsNonPlayers.getDefault(),
                "shield collisions must hit non-players by default");
        check(config.iceShieldShieldCooldownEnabled.getDefault(),
                "shield blocks must apply the configured cooldown");
        check(config.iceShieldShieldCooldownTicks.getDefault() == 60,
                "shield block cooldown must default to sixty ticks");
        check(config.iceShieldSlashBladeParryEnabled.getDefault(),
                "shield SlashBlade parries must be enabled by default");
        check(config.iceShieldParryFeedbackTicks.getDefault() == 20,
                "shield parry feedback must default to twenty ticks");

        checkClose(config.chaseSpeedMultiplier.getDefault(), 1.3,
                "original hover speed must default to one point three");
        checkClose(config.dropTelegraphRadius.getDefault(), 3.0,
                "drop warning radius must default to three blocks");
        checkClose(config.dropDamageMultiplier.getDefault(), 1.0,
                "drop impact must default to base attack damage");
        check(config.dropMaximumSuccessfulDrops.getDefault() == 2,
                "DROP phases must default to two successful drops");
        check(config.dropHitsNonPlayers.getDefault(),
                "drop impacts must hit non-players by default");
        check(config.dropWarningSoundEnabled.getDefault(),
                "drop warning sound must be enabled by default");
        checkClose(config.dropWarningSoundVolume.getDefault(), 2.0,
                "drop warning sound volume must default to two");
        check(config.dropSnowflakeParticlesPerTick.getDefault() == 8,
                "drop warnings must emit eight snowflakes per tick");

        check(config.shatteredIceRainQueenWindupTicks.getDefault() == 30,
                "rain windup must default to thirty ticks");
        check(config.shatteredIceRainCooldownTicks.getDefault() == 240,
                "rain cooldown must default to twelve seconds");
        check(config.shatteredIceRainFirstPhaseMinimumCount.getDefault() == 2,
                "rain minimum count must default to two");
        check(config.shatteredIceRainFirstPhaseMaximumCount.getDefault() == 4,
                "rain maximum count must default to four");
        check(config.shatteredIceRainSecondPhaseMinimumCount.getDefault() == 3,
                "phase-two rain minimum count must default to three");
        check(config.shatteredIceRainSecondPhaseMaximumCount.getDefault() == 4,
                "phase-two rain maximum count must default to four");
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

        check(config.maximumLinks.getDefault() == 2,
                "maximum links must default to two");
        checkClose(config.linkSummonDamageBonus.getDefault(), 0.3,
                "link summon damage bonus must default to zero point three");
        checkClose(config.linkQueenDamageReduction.getDefault(), 0.3,
                "link queen damage reduction must default to zero point three");
        check(config.linkEstablishWindupTicks.getDefault() == 20,
                "link establishment windup must default to twenty ticks");
        check(config.linkTeleportDelayTicks.getDefault() == 20,
                "link teleport delay must default to twenty ticks");
        checkClose(config.linkBurstRadius.getDefault(), 5.0,
                "link burst radius must default to five");
        checkClose(config.linkBurstDamageMultiplier.getDefault(), 1.5,
                "link burst damage must default to one and a half");
        check(config.linkBurstHitsNonPlayers.getDefault(),
                "link bursts must hit non-players by default");

        checkClose(config.phaseTwoHealthThreshold.getDefault(), 0.5,
                "phase-two threshold must default to fifty percent");
        checkClose(config.phaseTwoDamageMultiplier.getDefault(), 1.2,
                "phase-two damage must default to one point two");
        checkClose(config.phaseTwoSpeedMultiplier.getDefault(), 1.2,
                "phase-two speed must default to one point two");
        check(config.fallingIceIntervalTicks.getDefault() == 160,
                "falling ice interval must default to one hundred sixty ticks");
        check(config.fallingIceWindupTicks.getDefault() == 20,
                "falling ice windup must default to twenty ticks");
        checkClose(config.fallingIceRadius.getDefault(), 2.5,
                "falling ice radius must default to two point five");
        check(config.fallingIceCount.getDefault() == 6,
                "falling ice count must default to six");
        checkClose(config.fallingIceSpawnHeight.getDefault(), 10.0,
                "falling ice spawn height must default to ten");
        checkClose(config.fallingIceDamageMultiplier.getDefault(), 1.0,
                "falling ice damage must default to base attack damage");
        check(config.fallingIceHitsNonPlayers.getDefault(),
                "falling ice must hit non-players by default");

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
        UUID preferredLink = UUID.randomUUID();
        UUID parryActor = UUID.randomUUID();
        UUID directHit = UUID.randomUUID();
        AttackTelegraph telegraph = new AttackTelegraph(
                AttackTelegraphShape.CIRCLE,
                1.0F, 2.0F, 3.0F,
                0.0F, 1.0F, 4.0F, 0.0F, 4.0F, 0.75F);
        SnowQueenCombatState savedState = new SnowQueenCombatState();
        savedState.secondPhase = true;
        savedState.rainRoundActive = true;
        savedState.rainVolleySpawned = true;
        savedState.rainQueenWindupRemaining = 8;
        savedState.rainCooldownRemaining = 123;
        savedState.linkEstablishTicks = 7;
        savedState.preferredLinkUuid = preferredLink;
        savedState.linkTeleportRemaining = 9;
        savedState.fallingIceCooldown = 160;
        savedState.fallingIcePending = true;
        savedState.fallingIceRemaining = 12;
        savedState.fallingIceTotal = 20;
        savedState.fallingIceVisualSpawned = true;
        savedState.fallingIceX = 4.5;
        savedState.fallingIceY = 64.0;
        savedState.fallingIceZ = -7.25;
        savedState.lockedX = 10.5;
        savedState.lockedY = 64.0;
        savedState.lockedZ = -3.25;
        savedState.lastParryGameTime = 1234L;
        savedState.lastParryActorUuid = parryActor;
        savedState.currentTelegraph = telegraph;
        savedState.dropTelegraph = telegraph.withProgress(1.0F);
        savedState.linkedSummons.add(linked);
        savedState.ownedSummons.add(owned);
        savedState.rainVolley.add(rain);
        savedState.dropDirectHits.add(directHit);
        savedState.linkDeathPositions.addLast(new Vec3(1.0, 2.0, 3.0));
        savedState.linkDeathPositions.addLast(new Vec3(-4.0, 5.5, 6.0));
        savedState.shieldCollisionCooldowns.put(cooldownTarget, 4321L);

        CompoundTag savedTag = savedState.save();
        SnowQueenCombatState loadedState = new SnowQueenCombatState();
        loadedState.load(savedTag);
        check(!savedTag.contains("ActionState")
                        && !savedTag.contains("LastSkill")
                        && !savedTag.contains("StateTicks")
                        && !savedTag.contains("StateDuration")
                        && !savedTag.contains("SlamsCompleted"),
                "obsolete action-state data must not be written to NBT");
        check(loadedState.secondPhase,
                "second-phase state must survive an NBT round-trip");
        check(loadedState.rainRoundActive
                        && loadedState.rainVolleySpawned
                        && loadedState.rainQueenWindupRemaining == 8
                        && loadedState.rainCooldownRemaining == 123,
                "rain round state must survive an NBT round-trip");
        check(loadedState.linkEstablishTicks == 7
                        && preferredLink.equals(loadedState.preferredLinkUuid)
                        && loadedState.linkTeleportRemaining == 9,
                "link timing and preferred summon must survive an NBT round-trip");
        check(loadedState.fallingIceCooldown == 160
                        && loadedState.fallingIcePending
                        && loadedState.fallingIceRemaining == 12
                        && loadedState.fallingIceTotal == 20
                        && loadedState.fallingIceVisualSpawned,
                "falling ice timing state must survive an NBT round-trip");
        checkClose(loadedState.fallingIceX, 4.5,
                "falling ice X must survive an NBT round-trip");
        checkClose(loadedState.fallingIceY, 64.0,
                "falling ice Y must survive an NBT round-trip");
        checkClose(loadedState.fallingIceZ, -7.25,
                "falling ice Z must survive an NBT round-trip");
        checkClose(loadedState.lockedX, 10.5,
                "locked X must survive an NBT round-trip");
        checkClose(loadedState.lockedY, 64.0,
                "locked Y must survive an NBT round-trip");
        checkClose(loadedState.lockedZ, -3.25,
                "locked Z must survive an NBT round-trip");
        check(loadedState.lastParryGameTime == 1234L,
                "parry timestamp must survive an NBT round-trip");
        check(parryActor.equals(loadedState.lastParryActorUuid),
                "parry actor must survive an NBT round-trip");
        check(loadedState.linkedSummons.contains(linked)
                        && loadedState.ownedSummons.contains(owned)
                        && loadedState.rainVolley.contains(rain)
                        && loadedState.dropDirectHits.contains(directHit),
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
        check(loadedState.dropTelegraph.equals(
                        telegraph.withProgress(1.0F)),
                "drop telegraph state must survive an NBT round-trip");
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
