package com.tonywww.bossrefactortf.boss.snowqueen;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.TwilightBossParryIntegration;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.boss.SnowQueen;
import twilightforest.entity.boss.SnowQueenIceShield;
import twilightforest.entity.monster.BaseIceMob;
import twilightforest.entity.monster.StableIceCore;
import twilightforest.entity.monster.UnstableIceCore;
import twilightforest.init.TFEntities;
import twilightforest.init.TFMobEffects;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public final class SnowQueenCombat {
    private static final UUID PHASE_ATTACK_MODIFIER_ID = UUID.fromString(
            "d2b8161b-46df-4059-a6e8-ad22cfe2841b");
    private static final UUID PHASE_MOVEMENT_MODIFIER_ID = UUID.fromString(
            "0e1e5dfa-e497-4f58-8029-20968a38f563");
    private static final UUID PHASE_FLYING_MODIFIER_ID = UUID.fromString(
            "66e2fa9f-60c7-4dfa-bd0a-b26595b8315d");
    private static final String PHASE_ATTACK_MODIFIER_NAME =
            "BossRefactorTF Snow Queen phase attack";
    private static final String PHASE_MOVEMENT_MODIFIER_NAME =
            "BossRefactorTF Snow Queen phase movement";
    private static final String PHASE_FLYING_MODIFIER_NAME =
            "BossRefactorTF Snow Queen phase flying";

    private SnowQueenCombat() {
    }

    public static boolean customCombatEnabled() {
        return BossRefactorTwilightForestConfig.SNOW_QUEEN.enabled.get();
    }

    public static boolean customCombatActive(SnowQueen queen) {
        return queen.level().isClientSide()
                ? access(queen).bossRefactorTF$isSnowQueenCustomCombatActive()
                : customCombatEnabled();
    }

    public static boolean hasCombatWork(SnowQueen queen) {
        SnowQueenCombatState state = state(queen);
        return state.actionState != SnowQueenActionState.IDLE
                || !state.linkDeathPositions.isEmpty()
                || isRetainableTarget(queen, queen.getTarget())
                || acquireTarget(queen) != null;
    }

    public static void startIfIdle(SnowQueen queen) {
        SnowQueenCombatState state = state(queen);
        if (state.actionState != SnowQueenActionState.IDLE) {
            return;
        }
        if (!state.linkDeathPositions.isEmpty()) {
            beginLinkTeleportDelay(queen, state);
            return;
        }
        if (ensureTarget(queen) != null) {
            beginCruise(queen);
        }
    }

    public static void stopWhenUnusable(SnowQueen queen) {
        if (!customCombatEnabled()) {
            resetCombat(queen);
            return;
        }
        if (state(queen).actionState == SnowQueenActionState.CRUISE
                && ensureTarget(queen) == null) {
            enterIdle(queen);
        }
    }

    public static void tick(SnowQueen queen) {
        if (queen.level().isClientSide() || !customCombatEnabled()) {
            return;
        }
        SnowQueenCombatState state = state(queen);
        checkSecondPhase(queen, state);
        pruneSummonReferences(queen, state);
        if (!state.linkDeathPositions.isEmpty()
                && state.actionState != SnowQueenActionState.LINK_TELEPORT_DELAY
                && state.actionState != SnowQueenActionState.LINK_BURST_WINDUP) {
            beginLinkTeleportDelay(queen, state);
        }

        switch (state.actionState) {
            case IDLE -> startIfIdle(queen);
            case CRUISE -> tickCruise(queen, state);
            case SLAM_WINDUP -> tickSlamWindup(queen, state);
            case SLAM_DESCENT -> tickSlamDescent(queen, state);
            case SLAM_INTERVAL -> tickSlamInterval(queen, state);
            case CHASE -> tickChase(queen, state);
            case RAIN_WINDUP -> tickRainWindup(queen, state);
            case RAIN_ACTIVE -> tickRainActive(queen, state);
            case LINK_TELEPORT_DELAY -> tickLinkTeleportDelay(queen, state);
            case LINK_BURST_WINDUP -> tickLinkBurstWindup(queen, state);
        }
        synchronizeParryWindow(queen);
    }

    public static void tickBaseTraits(SnowQueen queen) {
        SnowQueenCombatState state = state(queen);
        if (!queen.level().isClientSide()) {
            if (customCombatEnabled()) {
                checkSecondPhase(queen, state);
            }
            synchronizeShieldData(queen, state);
            synchronizePhaseModifiers(
                    queen, customCombatEnabled() && state.secondPhase);
            access(queen).bossRefactorTF$setSnowQueenSecondPhase(
                    state.secondPhase);
        }
        if (customCombatActive(queen)) {
            positionIceShields(queen, state.secondPhase);
        } else {
            restoreOriginalIceShields(queen);
        }
    }

    private static void synchronizeShieldData(
            SnowQueen queen, SnowQueenCombatState state) {
        BossRefactorTwilightForestConfig.SnowQueenConfig config =
            BossRefactorTwilightForestConfig.SNOW_QUEEN;
        boolean active = config.enabled.get();
        SnowQueenCombatAccess access = access(queen);
        access.bossRefactorTF$setSnowQueenCustomCombatActive(active);
        int configuredCount = active
                ? (state.secondPhase
                        ? config.secondPhaseIceShieldCount.get()
                        : config.firstPhaseIceShieldCount.get())
                : 7;
        access.bossRefactorTF$setSnowQueenActiveShieldCount(
                SnowQueenCombatRules.shieldCount(
                        configuredCount, queen.iceArray.length));
        access.bossRefactorTF$setSnowQueenShieldOrbitRadius(
                config.iceShieldOrbitRadius.get());
        access.bossRefactorTF$setSnowQueenShieldAngularSpeed(
                config.iceShieldAngularSpeedDegreesPerTick.get());
        access.bossRefactorTF$setSnowQueenShieldVerticalOffset(
                config.iceShieldVerticalOffset.get());
    }

    public static Vec3 iceShieldPosition(SnowQueen queen, int index) {
        int count = activeShieldCount(queen);
        if (index < 0 || index >= count) {
            return hiddenIceShieldPosition(queen);
        }
        SnowQueenCombatAccess access = access(queen);
        double angle = Math.toRadians(SnowQueenCombatRules.shieldAngleDegrees(
                index, count, queen.tickCount,
            access.bossRefactorTF$getSnowQueenShieldAngularSpeed()));
        double radius = access.bossRefactorTF$getSnowQueenShieldOrbitRadius();
        return new Vec3(
                queen.getX() + Math.cos(angle) * radius,
                queen.getY()
                        + access.bossRefactorTF$getSnowQueenShieldVerticalOffset(),
                queen.getZ() + Math.sin(angle) * radius);
    }

    public static Vec3 hiddenIceShieldPosition(SnowQueen queen) {
        return new Vec3(
                queen.getX(),
                queen.getY() - queen.getBbHeight(),
                queen.getZ());
    }

    public static boolean isCurrentAttackParryable(SnowQueen queen) {
        if (!customCombatActive(queen)
                || !BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .slamSlashBladeParryEnabled.get()) {
            return false;
        }
        SnowQueenActionState actionState = state(queen).actionState;
        return actionState == SnowQueenActionState.SLAM_WINDUP
                || actionState == SnowQueenActionState.SLAM_DESCENT;
    }

    public static boolean acceptParry(SnowQueen queen) {
        if (!isCurrentAttackParryable(queen)) {
            return false;
        }
        SnowQueenCombatState state = state(queen);
        long gameTime = queen.level().getGameTime();
        if (state.lastParryGameTime == gameTime) {
            return true;
        }
        state.lastParryGameTime = gameTime;
        beginChase(queen, state);
        return true;
    }

    public static void restoreVisualState(SnowQueen queen) {
        SnowQueenCombatState state = state(queen);
        access(queen).bossRefactorTF$setSnowQueenSecondPhase(
                state.secondPhase);
        access(queen).bossRefactorTF$setSnowQueenTelegraph(
                state.currentTelegraph);
        if (!queen.level().isClientSide()) {
            synchronizePhaseModifiers(
                queen, customCombatEnabled() && state.secondPhase);
        }
    }

    public static boolean hasLivingLink(SnowQueen queen) {
        if (!customCombatEnabled() || queen.level().isClientSide()) {
            return false;
        }
        ServerLevel level = (ServerLevel) queen.level();
        SnowQueenCombatState state = state(queen);
        SnowQueenSummonData summonData = SnowQueenSummonData.get(level);
        pruneDeadSummonReferences(state, summonData);
        for (UUID summonUuid : state.linkedSummons) {
            Entity summon = level.getEntity(summonUuid);
            if (summon == null) {
                return true;
            }
            if (summon instanceof LivingEntity living && living.isAlive()) {
                return true;
            }
        }
        return false;
    }

    public static void tickSummon(BaseIceMob summon) {
        if (summon.level().isClientSide()
                || !(summon instanceof SnowQueenSummonAccess access)) {
            return;
        }
        SnowQueen owner = resolveOwner(summon, access);
        if (owner == null) {
            access.bossRefactorTF$setSnowQueenOwnerEntityId(0);
            UUID ownerUuid = access.bossRefactorTF$getSnowQueenOwnerUuid();
            if (ownerUuid != null
                    && SnowQueenSummonData.get((ServerLevel) summon.level())
                            .isOwnerDead(ownerUuid)) {
                access.bossRefactorTF$setSnowQueenLinked(false);
                summon.discard();
            }
            return;
        }
        if (!owner.isAlive() || !customCombatEnabled()) {
            return;
        }
        access.bossRefactorTF$setSnowQueenOwnerEntityId(owner.getId());
        if (summon instanceof StableIceCore stable) {
            stable.setTarget(owner.getTarget());
        }
        if (summon instanceof UnstableIceCore unstable
                && access.bossRefactorTF$getSnowQueenSummonRole()
                        == SnowQueenSummonAccess.ROLE_RAIN) {
            tickRainSummon(unstable, owner, access);
        }
    }

    public static void onSummonDeath(BaseIceMob summon) {
        if (summon.level().isClientSide()
                || !(summon instanceof SnowQueenSummonAccess access)
                || access.bossRefactorTF$getSnowQueenOwnerUuid() == null
                || access.bossRefactorTF$hasReportedSnowQueenDeath()) {
            return;
        }
        UUID ownerUuid = access.bossRefactorTF$getSnowQueenOwnerUuid();
        boolean linked = access.bossRefactorTF$isSnowQueenLinked();
        SnowQueenSummonData summonData = SnowQueenSummonData.get(
                (ServerLevel) summon.level());
        access.bossRefactorTF$setReportedSnowQueenDeath(true);
        summonData.markSummonDead(summon.getUUID());
        SnowQueen owner = resolveOwner(summon, access);
        if (owner == null) {
            if (linked && !summonData.isOwnerDead(ownerUuid)) {
                summonData.enqueue(ownerUuid, summon.position());
            }
            return;
        }
        SnowQueenCombatState state = state(owner);
        state.ownedSummons.remove(summon.getUUID());
        state.rainVolley.remove(summon.getUUID());
        linked = state.linkedSummons.remove(summon.getUUID());
        summonData.clearSummonDead(summon.getUUID());
        access.bossRefactorTF$setSnowQueenLinked(false);
        if (linked && owner.isAlive() && !state.suppressLinkDeathQueue) {
            state.linkDeathPositions.addLast(summon.position());
            if (state.actionState != SnowQueenActionState.LINK_TELEPORT_DELAY
                    && state.actionState
                            != SnowQueenActionState.LINK_BURST_WINDUP) {
                beginLinkTeleportDelay(owner, state);
            }
        }
    }

    public static void cleanupOwnedSummons(SnowQueen queen) {
        if (queen.level().isClientSide()) {
            return;
        }
        SnowQueenCombatState state = state(queen);
        state.suppressLinkDeathQueue = true;
        ServerLevel level = (ServerLevel) queen.level();
        for (UUID summonUuid : new ArrayList<>(state.ownedSummons)) {
            Entity summon = level.getEntity(summonUuid);
            if (summon instanceof SnowQueenSummonAccess access) {
                access.bossRefactorTF$setSnowQueenLinked(false);
                access.bossRefactorTF$setReportedSnowQueenDeath(true);
            }
            if (summon != null) {
                summon.discard();
            }
        }
        state.ownedSummons.clear();
        state.linkedSummons.clear();
        state.rainVolley.clear();
        state.linkDeathPositions.clear();
        clearTelegraph(queen);
        closeParryWindow(queen);
    }

    public static void cleanupAfterOwnerDeath(SnowQueen queen) {
        if (queen.level() instanceof ServerLevel level) {
            SnowQueenSummonData.get(level).markOwnerDead(queen.getUUID());
        }
        cleanupOwnedSummons(queen);
    }

    private static void tickCruise(
            SnowQueen queen, SnowQueenCombatState state) {
        LivingEntity target = ensureTarget(queen);
        if (target == null) {
            enterIdle(queen);
            return;
        }
        queen.getMoveControl().setWantedPosition(
                target.getX(), target.getY(), target.getZ(), 1.0);
        queen.getLookControl().setLookAt(target);
        clearTelegraph(queen);
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        SnowQueenSkill skill = SnowQueenCombatRules.chooseSkill(
                state.lastSkill,
                BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .avoidImmediateSkillRepeat.get(),
                queen.getRandom().nextBoolean());
        state.lastSkill = skill;
        if (skill == SnowQueenSkill.REPEATED_SLAM) {
            state.slamsCompleted = 0;
            beginSlamWindup(queen, state, target);
        } else {
            beginRainWindup(queen, state, target);
        }
    }

    private static void tickSlamWindup(
            SnowQueen queen, SnowQueenCombatState state) {
        LivingEntity target = ensureTarget(queen);
        if (target == null) {
            enterIdle(queen);
            return;
        }
        BossRefactorTwilightForestConfig.SnowQueenConfig config =
                BossRefactorTwilightForestConfig.SNOW_QUEEN;
        queen.getMoveControl().setWantedPosition(
                state.lockedX,
                state.lockedY + config.slamPreparationHeight.get(),
                state.lockedZ, 1.0);
        queen.getLookControl().setLookAt(
                state.lockedX, state.lockedY, state.lockedZ);
        setCircleTelegraph(
                queen, state,
                AttackTelegraph.windupProgress(
                        state.stateTicks, state.stateDuration),
                config.slamRadius.get().floatValue());
        state.stateTicks++;
        if (state.stateTicks >= state.stateDuration) {
            queen.getNavigation().stop();
            queen.setPos(
                state.lockedX,
                state.lockedY + config.slamPreparationHeight.get(),
                state.lockedZ);
            enterState(state, SnowQueenActionState.SLAM_DESCENT,
                    config.slamMaximumDescentTicks.get());
        }
    }

    private static void tickSlamDescent(
            SnowQueen queen, SnowQueenCombatState state) {
        BossRefactorTwilightForestConfig.SnowQueenConfig config =
                BossRefactorTwilightForestConfig.SNOW_QUEEN;
        queen.getNavigation().stop();
        queen.setDeltaMovement(0.0, -config.slamDescentSpeed.get(), 0.0);
        setCircleTelegraph(
                queen, state, 1.0F,
                config.slamRadius.get().floatValue());
        state.stateTicks++;
        if (queen.getY() > state.lockedY
                && state.stateTicks < state.stateDuration) {
            return;
        }
        queen.setDeltaMovement(Vec3.ZERO);
        boolean defended = damageCircle(
                queen,
                state.lockedX, state.lockedY, state.lockedZ,
                config.slamRadius.get(),
                skillDamage(queen, config.slamDamageMultiplier.get()),
                config.slamHitsNonPlayers.get(), true);
        state.slamsCompleted++;
        if (defended || state.slamsCompleted >= config.slamCount.get()) {
            beginChase(queen, state);
            return;
        }
        enterState(state, SnowQueenActionState.SLAM_INTERVAL,
                config.betweenSlamsTicks.get());
        clearTelegraph(queen);
    }

    private static void tickSlamInterval(
            SnowQueen queen, SnowQueenCombatState state) {
        stopMovement(queen);
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        LivingEntity target = ensureTarget(queen);
        if (target == null) {
            enterIdle(queen);
        } else {
            beginSlamWindup(queen, state, target);
        }
    }

    private static void tickChase(
            SnowQueen queen, SnowQueenCombatState state) {
        LivingEntity target = ensureTarget(queen);
        if (target == null) {
            enterIdle(queen);
            return;
        }
        BossRefactorTwilightForestConfig.SnowQueenConfig config =
                BossRefactorTwilightForestConfig.SNOW_QUEEN;
        queen.getMoveControl().setWantedPosition(
                target.getX(),
                target.getY() + config.chaseHeightOffset.get(),
                target.getZ(),
                config.chaseSpeedMultiplier.get());
        queen.getLookControl().setLookAt(target);
        if (state.stateTicks % config.chaseContactDamageIntervalTicks.get()
                == 0) {
            damageContacts(
                    queen,
                    queen.getBoundingBox().inflate(
                            config.chaseHitboxInflation.get()),
                    skillDamage(
                            queen, config.chaseContactDamageMultiplier.get()),
                    config.chaseHitsNonPlayers.get());
        }
        state.stateTicks++;
        if (state.stateTicks >= state.stateDuration) {
            beginCruise(queen);
        }
    }

    private static void tickRainWindup(
            SnowQueen queen, SnowQueenCombatState state) {
        LivingEntity target = ensureTarget(queen);
        if (target == null) {
            enterIdle(queen);
            return;
        }
        stopMovement(queen);
        queen.getLookControl().setLookAt(target);
        state.stateTicks++;
        if (state.stateTicks >= state.stateDuration) {
            spawnRainVolley(queen, state, target);
            enterState(state, SnowQueenActionState.RAIN_ACTIVE, 0);
        }
    }

    private static void tickRainActive(
            SnowQueen queen, SnowQueenCombatState state) {
        stopMovement(queen);
        state.rainVolley.removeIf(uuid -> {
            Entity summon = ((ServerLevel) queen.level()).getEntity(uuid);
            return !(summon instanceof LivingEntity living) || !living.isAlive();
        });
        if (state.rainVolley.isEmpty()) {
            beginCruiseOrIdle(queen);
        }
    }

    private static void tickLinkTeleportDelay(
            SnowQueen queen, SnowQueenCombatState state) {
        stopMovement(queen);
        clearTelegraph(queen);
        if (state.linkDeathPositions.isEmpty()) {
            beginCruiseOrIdle(queen);
            return;
        }
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        Vec3 position = state.linkDeathPositions.peekFirst();
        state.lockedX = position.x;
        state.lockedY = position.y;
        state.lockedZ = position.z;
        queen.teleportTo(position.x, position.y, position.z);
        enterState(state, SnowQueenActionState.LINK_BURST_WINDUP,
                BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .linkBurstWindupTicks.get());
    }

    private static void tickLinkBurstWindup(
            SnowQueen queen, SnowQueenCombatState state) {
        BossRefactorTwilightForestConfig.SnowQueenConfig config =
                BossRefactorTwilightForestConfig.SNOW_QUEEN;
        stopMovement(queen);
        setCircleTelegraph(
                queen, state,
                AttackTelegraph.windupProgress(
                        state.stateTicks, state.stateDuration),
                config.linkBurstRadius.get().floatValue());
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        damageCircle(
                queen,
                state.lockedX, state.lockedY, state.lockedZ,
                config.linkBurstRadius.get(),
                skillDamage(queen, config.linkBurstDamageMultiplier.get()),
                config.linkBurstHitsNonPlayers.get(), false);
        state.linkDeathPositions.pollFirst();
        if (state.linkDeathPositions.isEmpty()) {
            beginCruiseOrIdle(queen);
        } else {
            beginLinkTeleportDelay(queen, state);
        }
    }

    private static void beginCruise(SnowQueen queen) {
        SnowQueenCombatState state = state(queen);
        enterState(state, SnowQueenActionState.CRUISE,
                SnowQueenCombatRules.cruiseTicks(
                        BossRefactorTwilightForestConfig.SNOW_QUEEN
                                .cruiseMinimumTicks.get(),
                        BossRefactorTwilightForestConfig.SNOW_QUEEN
                                .cruiseMaximumTicks.get(),
                        queen.getRandom().nextDouble()));
        state.slamsCompleted = 0;
        clearTelegraph(queen);
        closeParryWindow(queen);
    }

    private static void beginSlamWindup(
            SnowQueen queen, SnowQueenCombatState state, LivingEntity target) {
        state.lockedX = target.getX();
        state.lockedY = target.getY();
        state.lockedZ = target.getZ();
        enterState(state, SnowQueenActionState.SLAM_WINDUP,
                BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .slamWindupTicks.get());
        setCircleTelegraph(
                queen, state, 0.0F,
                BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .slamRadius.get().floatValue());
    }

    private static void beginChase(
            SnowQueen queen, SnowQueenCombatState state) {
        enterState(state, SnowQueenActionState.CHASE,
                BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .chaseDurationTicks.get());
        state.slamsCompleted = 0;
        clearTelegraph(queen);
        closeParryWindow(queen);
    }

    private static void beginRainWindup(
            SnowQueen queen, SnowQueenCombatState state, LivingEntity target) {
        state.lockedX = target.getX();
        state.lockedY = target.getY();
        state.lockedZ = target.getZ();
        enterState(state, SnowQueenActionState.RAIN_WINDUP,
                BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .shatteredIceRainQueenWindupTicks.get());
        clearTelegraph(queen);
        closeParryWindow(queen);
    }

    private static void beginLinkTeleportDelay(
            SnowQueen queen, SnowQueenCombatState state) {
        enterState(state, SnowQueenActionState.LINK_TELEPORT_DELAY,
                BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .linkTeleportDelayTicks.get());
        clearTelegraph(queen);
        closeParryWindow(queen);
    }

    private static void beginCruiseOrIdle(SnowQueen queen) {
        if (ensureTarget(queen) == null) {
            enterIdle(queen);
        } else {
            beginCruise(queen);
        }
    }

    private static void spawnRainVolley(
            SnowQueen queen, SnowQueenCombatState state, LivingEntity target) {
        BossRefactorTwilightForestConfig.SnowQueenConfig config =
                BossRefactorTwilightForestConfig.SNOW_QUEEN;
        int count = state.secondPhase
                ? config.shatteredIceRainSecondPhaseCount.get()
                : randomInclusive(
                        queen,
                        config.shatteredIceRainFirstPhaseMinimumCount.get(),
                        config.shatteredIceRainFirstPhaseMaximumCount.get());
        Vec3 direction = horizontalDirection(queen.position(), target.position());
        Vec3 side = new Vec3(-direction.z, 0.0, direction.x);
        Vec3 base = queen.position()
                .subtract(direction.scale(
                        config.shatteredIceRainSpawnDistanceBehind.get()))
                .add(0.0, config.shatteredIceRainSpawnVerticalOffset.get(), 0.0);
        state.rainVolley.clear();
        for (int index = 0; index < count; index++) {
            UnstableIceCore core = TFEntities.UNSTABLE_ICE_CORE.get()
                    .create(queen.level());
            if (core == null) {
                continue;
            }
            double centeredIndex = index - (count - 1) * 0.5;
            Vec3 spawn = base.add(side.scale(
                    centeredIndex * config.shatteredIceRainSpawnSpacing.get()));
            core.moveTo(spawn.x, spawn.y, spawn.z,
                    queen.getYRot(), queen.getXRot());
            configureOwnedSummon(
                    queen, core, SnowQueenSummonAccess.ROLE_RAIN);
            SnowQueenSummonAccess summonAccess = (SnowQueenSummonAccess) core;
            summonAccess.bossRefactorTF$setSnowQueenSummonState(
                    SnowQueenSummonAccess.STATE_WINDUP);
            summonAccess.bossRefactorTF$setSnowQueenSummonTicks(0);
            summonAccess.bossRefactorTF$setSnowQueenSummonDuration(
                    config.shatteredIceRainCrystalWindupTicks.get()
                            + index
                            * config.shatteredIceRainLaunchIntervalTicks.get());
            queen.level().addFreshEntity(core);
            state.ownedSummons.add(core.getUUID());
            state.rainVolley.add(core.getUUID());
            if (!state.secondPhase) {
                linkIfSlotAvailable(queen, core);
            }
        }
    }

    private static void spawnPhaseTwoStableCores(
            SnowQueen queen, SnowQueenCombatState state) {
        BossRefactorTwilightForestConfig.SnowQueenConfig config =
                BossRefactorTwilightForestConfig.SNOW_QUEEN;
        int count = config.phaseTwoStableIceCoreCount.get();
        for (int index = 0; index < count; index++) {
            StableIceCore core = TFEntities.STABLE_ICE_CORE.get()
                    .create(queen.level());
            if (core == null) {
                continue;
            }
            double angle = Math.PI * 2.0 * index / Math.max(1, count);
            double radius = config.stableIceCoreSpawnRadius.get();
            core.moveTo(
                    queen.getX() + Math.cos(angle) * radius,
                    queen.getY() + config.stableIceCoreSpawnVerticalOffset.get(),
                    queen.getZ() + Math.sin(angle) * radius,
                    queen.getYRot(), queen.getXRot());
            configureOwnedSummon(
                    queen, core,
                    SnowQueenSummonAccess.ROLE_PHASE_TWO_STABLE);
            core.setTarget(queen.getTarget());
            queen.level().addFreshEntity(core);
            state.ownedSummons.add(core.getUUID());
            linkIfSlotAvailable(queen, core);
        }
    }

    private static void configureOwnedSummon(
            SnowQueen queen, BaseIceMob summon, int role) {
        SnowQueenSummonAccess access = (SnowQueenSummonAccess) summon;
        summon.setNoAi(role == SnowQueenSummonAccess.ROLE_RAIN);
        access.bossRefactorTF$setSnowQueenOwnerUuid(queen.getUUID());
        access.bossRefactorTF$setSnowQueenOwnerEntityId(queen.getId());
        access.bossRefactorTF$setSnowQueenSummonRole(role);
        access.bossRefactorTF$setSnowQueenSummonState(
                SnowQueenSummonAccess.STATE_IDLE);
        access.bossRefactorTF$setReportedSnowQueenDeath(false);
    }

    private static void linkIfSlotAvailable(
            SnowQueen queen, BaseIceMob summon) {
        SnowQueenCombatState state = state(queen);
        if (state.linkedSummons.size()
                >= BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .maximumLinks.get()) {
            return;
        }
        state.linkedSummons.add(summon.getUUID());
        ((SnowQueenSummonAccess) summon)
                .bossRefactorTF$setSnowQueenLinked(true);
    }

    private static void tickRainSummon(
            UnstableIceCore core, SnowQueen owner,
            SnowQueenSummonAccess access) {
        switch (access.bossRefactorTF$getSnowQueenSummonState()) {
            case SnowQueenSummonAccess.STATE_WINDUP -> {
                core.setDeltaMovement(Vec3.ZERO);
                int ticks = access.bossRefactorTF$getSnowQueenSummonTicks() + 1;
                access.bossRefactorTF$setSnowQueenSummonTicks(ticks);
                if (ticks >= access.bossRefactorTF$getSnowQueenSummonDuration()) {
                    LivingEntity target = owner.getTarget();
                    Vec3 direction = target != null
                            ? target.position().subtract(core.position()).normalize()
                            : owner.getLookAngle().normalize();
                    access.bossRefactorTF$setSnowQueenDash(
                            direction.x, direction.y, direction.z);
                    access.bossRefactorTF$setSnowQueenSummonTicks(0);
                    access.bossRefactorTF$setSnowQueenSummonState(
                            SnowQueenSummonAccess.STATE_DASHING);
                }
            }
            case SnowQueenSummonAccess.STATE_DASHING ->
                    tickRainDash(core, owner, access);
            default -> {
            }
        }
    }

    private static void tickRainDash(
            UnstableIceCore core, SnowQueen owner,
            SnowQueenSummonAccess access) {
        BossRefactorTwilightForestConfig.SnowQueenConfig config =
                BossRefactorTwilightForestConfig.SNOW_QUEEN;
        core.setDeltaMovement(
                access.bossRefactorTF$getSnowQueenDashX()
                        * config.shatteredIceRainDashSpeed.get(),
                access.bossRefactorTF$getSnowQueenDashY()
                        * config.shatteredIceRainDashSpeed.get(),
                access.bossRefactorTF$getSnowQueenDashZ()
                        * config.shatteredIceRainDashSpeed.get());
        boolean hit = false;
        AABB bounds = core.getBoundingBox().inflate(
                config.shatteredIceRainDashHitboxInflation.get());
        for (LivingEntity target : core.level().getEntitiesOfClass(
                LivingEntity.class, bounds,
                candidate -> isRainTarget(owner, candidate,
                        config.shatteredIceRainDashHitsNonPlayers.get()))) {
            if (target.hurt(owner.damageSources().mobAttack(core),
                    skillDamage(
                            owner,
                            config.shatteredIceRainDashDamageMultiplier.get()))) {
                target.addEffect(new MobEffectInstance(
                        TFMobEffects.FROSTY.get(),
                        config.shatteredIceRainFrostedDurationTicks.get(),
                        config.shatteredIceRainFrostedAmplifier.get()));
            }
            hit = true;
            break;
        }
        int ticks = access.bossRefactorTF$getSnowQueenSummonTicks() + 1;
        access.bossRefactorTF$setSnowQueenSummonTicks(ticks);
        if (hit || ticks >= config.shatteredIceRainMaximumDashTicks.get()) {
            access.bossRefactorTF$setSnowQueenSummonState(
                    SnowQueenSummonAccess.STATE_DYING);
            core.hurt(owner.damageSources().mobAttack(owner), Float.MAX_VALUE);
        }
    }

    private static void checkSecondPhase(
            SnowQueen queen, SnowQueenCombatState state) {
        if (state.secondPhase || !SnowQueenCombatRules.isSecondPhase(
                queen.getHealth(), queen.getMaxHealth(),
                BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .phaseTwoHealthThreshold.get())) {
            return;
        }
        state.secondPhase = true;
        access(queen).bossRefactorTF$setSnowQueenSecondPhase(true);
        synchronizePhaseModifiers(queen, true);
        clearExistingLinks(queen, state);
        spawnPhaseTwoStableCores(queen, state);
    }

    private static void clearExistingLinks(
            SnowQueen queen, SnowQueenCombatState state) {
        ServerLevel level = (ServerLevel) queen.level();
        for (UUID summonUuid : state.linkedSummons) {
            Entity summon = level.getEntity(summonUuid);
            if (summon instanceof SnowQueenSummonAccess summonAccess) {
                summonAccess.bossRefactorTF$setSnowQueenLinked(false);
            }
        }
        state.linkedSummons.clear();
    }

    private static void positionIceShields(
            SnowQueen queen, boolean secondPhase) {
        int count = activeShieldCount(queen);
        for (int index = 0; index < queen.iceArray.length; index++) {
            SnowQueenIceShield shield = queen.iceArray[index];
            boolean visible = index < count;
            shield.setInvisible(!visible);
            Vec3 position = visible
                    ? iceShieldPosition(queen, index)
                    : hiddenIceShieldPosition(queen);
            shield.setPos(position.x, position.y, position.z);
            if (!queen.level().isClientSide() && secondPhase && visible) {
                damageShieldContacts(queen, shield);
            }
        }
    }

    private static void restoreOriginalIceShields(SnowQueen queen) {
        for (int index = 0; index < queen.iceArray.length; index++) {
            SnowQueenIceShield shield = queen.iceArray[index];
            boolean originalShield = index < 7;
            shield.setInvisible(!originalShield);
            if (!originalShield) {
                Vec3 position = hiddenIceShieldPosition(queen);
                shield.setPos(position.x, position.y, position.z);
            }
        }
    }

    private static void damageShieldContacts(
            SnowQueen queen, SnowQueenIceShield shield) {
        BossRefactorTwilightForestConfig.SnowQueenConfig config =
                BossRefactorTwilightForestConfig.SNOW_QUEEN;
        SnowQueenCombatState state = state(queen);
        long gameTime = queen.level().getGameTime();
        state.shieldCollisionCooldowns.entrySet().removeIf(
                entry -> entry.getValue() <= gameTime);
        for (LivingEntity target : queen.level().getEntitiesOfClass(
                LivingEntity.class,
                shield.getBoundingBox().inflate(
                        config.iceShieldCollisionHitboxInflation.get()),
                candidate -> isSkillTarget(
                        queen, candidate,
                        config.iceShieldCollisionHitsNonPlayers.get()))) {
            long expiresAt = state.shieldCollisionCooldowns.getOrDefault(
                    target.getUUID(), Long.MIN_VALUE);
            if (gameTime < expiresAt) {
                continue;
            }
            target.hurt(
                    queen.damageSources().mobAttack(queen),
                    skillDamage(
                            queen,
                            config.secondPhaseIceShieldCollisionDamageMultiplier
                                    .get()));
            state.shieldCollisionCooldowns.put(
                    target.getUUID(),
                    gameTime + config.iceShieldCollisionDamageCooldownTicks
                            .get());
        }
    }

    private static int activeShieldCount(SnowQueen queen) {
        return SnowQueenCombatRules.shieldCount(
            access(queen).bossRefactorTF$getSnowQueenActiveShieldCount(),
            queen.iceArray.length);
    }

    private static void synchronizePhaseModifiers(
            SnowQueen queen, boolean active) {
        double damageAmount = BossRefactorTwilightForestConfig.SNOW_QUEEN
                .phaseTwoDamageMultiplier.get() - 1.0;
        double speedAmount = BossRefactorTwilightForestConfig.SNOW_QUEEN
                .phaseTwoSpeedMultiplier.get() - 1.0;
        synchronizeModifier(
                queen.getAttribute(Attributes.ATTACK_DAMAGE),
                PHASE_ATTACK_MODIFIER_ID, PHASE_ATTACK_MODIFIER_NAME,
                damageAmount, active);
        synchronizeModifier(
                queen.getAttribute(Attributes.MOVEMENT_SPEED),
                PHASE_MOVEMENT_MODIFIER_ID, PHASE_MOVEMENT_MODIFIER_NAME,
                speedAmount, active);
        synchronizeModifier(
                queen.getAttribute(Attributes.FLYING_SPEED),
                PHASE_FLYING_MODIFIER_ID, PHASE_FLYING_MODIFIER_NAME,
                speedAmount, active);
    }

    private static void synchronizeModifier(
            @Nullable AttributeInstance attribute, UUID id,
            String name, double amount, boolean active) {
        if (attribute == null) {
            return;
        }
        AttributeModifier existing = attribute.getModifier(id);
        if (!active) {
            if (existing != null) {
                attribute.removeModifier(id);
            }
            return;
        }
        if (existing != null && Double.compare(existing.getAmount(), amount) == 0) {
            return;
        }
        if (existing != null) {
            attribute.removeModifier(id);
        }
        attribute.addTransientModifier(new AttributeModifier(
                id, name, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static boolean damageCircle(
            SnowQueen queen, double x, double y, double z,
            double radius, float damage, boolean hitsNonPlayers,
            boolean checksShield) {
        AABB bounds = new AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius);
        DamageSource source = queen.damageSources().mobAttack(queen);
        boolean anyDefended = false;
        for (LivingEntity target : queen.level().getEntitiesOfClass(
                LivingEntity.class, bounds,
                candidate -> isSkillTarget(
                        queen, candidate, hitsNonPlayers))) {
            if (!SnowQueenCombatRules.isInsideCircle(
                    target.getX(), target.getZ(), x, z, radius)) {
                continue;
            }
            if (checksShield
                    && hurtWithShieldCheck(target, source, damage)) {
                anyDefended = true;
            }
            if (!checksShield) {
                target.hurt(source, damage);
            }
        }
        return anyDefended;
    }

    private static void damageContacts(
            SnowQueen queen, AABB bounds, float damage,
            boolean hitsNonPlayers) {
        DamageSource source = queen.damageSources().mobAttack(queen);
        for (LivingEntity target : queen.level().getEntitiesOfClass(
                LivingEntity.class, bounds,
                candidate -> isSkillTarget(
                        queen, candidate, hitsNonPlayers))) {
            target.hurt(source, damage);
        }
    }

    private static boolean hurtWithShieldCheck(
            LivingEntity target, DamageSource source, float damage) {
        ItemStack usedShield = target instanceof ServerPlayer player
                ? player.getUseItem().copy()
                : ItemStack.EMPTY;
        boolean shieldBlocked = target instanceof ServerPlayer player
                && usedShield.getItem() instanceof ShieldItem
                && player.isDamageSourceBlocked(source);
        target.hurt(source, damage);
        if (shieldBlocked
                && BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .slamShieldCooldownEnabled.get()) {
            int cooldown = BossRefactorTwilightForestConfig.SNOW_QUEEN
                    .slamShieldCooldownTicks.get();
            if (!usedShield.isEmpty() && cooldown > 0) {
                ((ServerPlayer) target).getCooldowns().addCooldown(
                        usedShield.getItem(), cooldown);
            }
            ((ServerPlayer) target).stopUsingItem();
        }
        return shieldBlocked;
    }

    private static boolean isSkillTarget(
            SnowQueen queen, LivingEntity target, boolean hitsNonPlayers) {
        return isCombatTarget(queen, target)
                && (hitsNonPlayers || target instanceof Player);
    }

    private static boolean isRainTarget(
            SnowQueen queen, LivingEntity target, boolean hitsNonPlayers) {
        return isSkillTarget(queen, target, hitsNonPlayers)
                && !isOwnedSummon(queen, target);
    }

    private static boolean isCombatTarget(
            SnowQueen queen, LivingEntity target) {
        return target != queen
                && target.isAlive()
                && !queen.isAlliedTo(target)
                && !isOwnedSummon(queen, target)
                && (!(target instanceof Player player)
                        || (!player.isCreative() && !player.isSpectator()));
    }

    private static boolean isOwnedSummon(
            SnowQueen queen, Entity entity) {
        return entity instanceof SnowQueenSummonAccess summon
                && summon.bossRefactorTF$isOwnedBy(queen.getUUID());
    }

    @Nullable
    private static LivingEntity ensureTarget(SnowQueen queen) {
        LivingEntity target = queen.getTarget();
        if (isRetainableTarget(queen, target)) {
            return target;
        }
        target = acquireTarget(queen);
        queen.setTarget(target);
        return target;
    }

    @Nullable
    private static LivingEntity acquireTarget(SnowQueen queen) {
        double range = queen.getAttributeValue(Attributes.FOLLOW_RANGE);
        return queen.level().getEntitiesOfClass(
                        Player.class,
                        queen.getBoundingBox().inflate(range),
                target -> isValidPlayerTarget(queen, target)
                    && queen.distanceToSqr(target)
                        <= range * range)
                .stream()
                .min(Comparator.comparingDouble(queen::distanceToSqr))
                .orElse(null);
    }

    private static boolean isRetainableTarget(
            SnowQueen queen, @Nullable LivingEntity target) {
        if (!(target instanceof Player player)
                || target.isRemoved()
            || !isValidPlayerTarget(queen, player)) {
            return false;
        }
        double range = queen.getAttributeValue(Attributes.FOLLOW_RANGE);
        return queen.distanceToSqr(target) <= range * range;
    }

    private static boolean isValidPlayerTarget(
            SnowQueen queen, Player player) {
        return player.isAlive()
                && !queen.isAlliedTo(player)
                && !player.isCreative()
                && !player.isSpectator();
    }

    @Nullable
    private static SnowQueen resolveOwner(
            BaseIceMob summon, SnowQueenSummonAccess access) {
        UUID ownerUuid = access.bossRefactorTF$getSnowQueenOwnerUuid();
        if (ownerUuid == null || !(summon.level() instanceof ServerLevel level)) {
            return null;
        }
        Entity owner = level.getEntity(ownerUuid);
        return owner instanceof SnowQueen queen ? queen : null;
    }

    private static void pruneSummonReferences(
            SnowQueen queen, SnowQueenCombatState state) {
        ServerLevel level = (ServerLevel) queen.level();
        SnowQueenSummonData summonData = SnowQueenSummonData.get(level);
        for (Vec3 position : summonData.drain(queen.getUUID())) {
            state.linkDeathPositions.addLast(position);
        }
        pruneDeadSummonReferences(state, summonData);
        state.ownedSummons.removeIf(uuid -> {
            Entity entity = level.getEntity(uuid);
            return entity != null && entity.isRemoved();
        });
        state.linkedSummons.removeIf(uuid -> {
            Entity entity = level.getEntity(uuid);
            return entity != null && entity.isRemoved();
        });
    }

    private static void pruneDeadSummonReferences(
            SnowQueenCombatState state, SnowQueenSummonData summonData) {
        for (UUID summonUuid : new ArrayList<>(state.ownedSummons)) {
            if (!summonData.isSummonDead(summonUuid)) {
                continue;
            }
            state.ownedSummons.remove(summonUuid);
            state.linkedSummons.remove(summonUuid);
            state.rainVolley.remove(summonUuid);
            summonData.clearSummonDead(summonUuid);
        }
    }

    private static int randomInclusive(
            SnowQueen queen, int first, int second) {
        int minimum = Math.min(first, second);
        int maximum = Math.max(first, second);
        return minimum + queen.getRandom().nextInt(maximum - minimum + 1);
    }

    private static Vec3 horizontalDirection(Vec3 from, Vec3 to) {
        Vec3 direction = to.subtract(from).multiply(1.0, 0.0, 1.0);
        if (direction.lengthSqr() < 1.0E-6) {
            return new Vec3(0.0, 0.0, 1.0);
        }
        return direction.normalize();
    }

    private static float skillDamage(
            SnowQueen queen, double multiplier) {
        return SnowQueenCombatRules.scaledDamage(
                queen.getAttributeValue(Attributes.ATTACK_DAMAGE), multiplier);
    }

    private static void setCircleTelegraph(
            SnowQueen queen, SnowQueenCombatState state,
            float progress, float radius) {
        AttackTelegraph telegraph = new AttackTelegraph(
                AttackTelegraphShape.CIRCLE,
                (float) state.lockedX,
                (float) state.lockedY,
                (float) state.lockedZ,
                0.0F, 1.0F, 0.0F, 0.0F, radius, progress);
        state.currentTelegraph = telegraph;
        access(queen).bossRefactorTF$setSnowQueenTelegraph(telegraph);
    }

    private static void clearTelegraph(SnowQueen queen) {
        state(queen).currentTelegraph = AttackTelegraph.NONE;
        access(queen).bossRefactorTF$setSnowQueenTelegraph(
                AttackTelegraph.NONE);
    }

    private static void synchronizeParryWindow(SnowQueen queen) {
        if (!(queen instanceof TwilightBossParryAccess parryAccess)) {
            return;
        }
        var bridge = TwilightBossParryIntegration.bridge();
        if (!bridge.isAvailable()) {
            return;
        }
        if (!parryAccess.bossRefactorTF$isCurrentAttackParryable()) {
            closeParryWindow(queen);
        } else if (!bridge.isParryable(queen) && !bridge.isParried(queen)
                && !bridge.openWindow(queen)) {
            BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to open the SenDimS parry window for Snow Queen {}",
                    queen.getUUID());
        }
    }

    private static void closeParryWindow(SnowQueen queen) {
        var bridge = TwilightBossParryIntegration.bridge();
        if (bridge.isAvailable() && bridge.isParryable(queen)
                && !bridge.closeWindow(queen)) {
            BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to close the SenDimS parry window for Snow Queen {}",
                    queen.getUUID());
        }
    }

    private static void stopMovement(SnowQueen queen) {
        queen.getNavigation().stop();
        queen.setDeltaMovement(Vec3.ZERO);
    }

    private static void enterState(
            SnowQueenCombatState state,
            SnowQueenActionState actionState, int duration) {
        state.actionState = actionState;
        state.stateTicks = 0;
        state.stateDuration = Math.max(0, duration);
    }

    private static void enterIdle(SnowQueen queen) {
        enterState(state(queen), SnowQueenActionState.IDLE, 0);
        stopMovement(queen);
        clearTelegraph(queen);
        closeParryWindow(queen);
    }

    private static void resetCombat(SnowQueen queen) {
        SnowQueenCombatState state = state(queen);
        enterState(state, SnowQueenActionState.IDLE, 0);
        state.slamsCompleted = 0;
        state.rainVolley.clear();
        state.linkDeathPositions.clear();
        synchronizePhaseModifiers(queen, false);
        clearTelegraph(queen);
        closeParryWindow(queen);
    }

    private static SnowQueenCombatState state(SnowQueen queen) {
        return access(queen).bossRefactorTF$getSnowQueenCombatState();
    }

    private static SnowQueenCombatAccess access(SnowQueen queen) {
        return (SnowQueenCombatAccess) queen;
    }
}