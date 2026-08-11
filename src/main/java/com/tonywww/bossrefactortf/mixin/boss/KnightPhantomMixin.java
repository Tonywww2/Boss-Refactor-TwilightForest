package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.knightphantom.KnightPhantomCombat;
import com.tonywww.bossrefactortf.boss.knightphantom.KnightPhantomCombatAccess;
import com.tonywww.bossrefactortf.boss.knightphantom.KnightPhantomCombatRules;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphAccess;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.boss.KnightPhantom;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(value = KnightPhantom.class, remap = false)
public abstract class KnightPhantomMixin extends FlyingMob
        implements AttackTelegraphAccess, TwilightBossParryAccess,
        KnightPhantomCombatAccess {
    @Unique
    private static final EntityDataAccessor<Boolean> BOSS_REFACTOR_TF$TELEGRAPH_ACTIVE =
            SynchedEntityData.defineId(
                    KnightPhantom.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Float> BOSS_REFACTOR_TF$TELEGRAPH_ORIGIN_X =
            SynchedEntityData.defineId(
                    KnightPhantom.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BOSS_REFACTOR_TF$TELEGRAPH_ORIGIN_Y =
            SynchedEntityData.defineId(
                    KnightPhantom.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BOSS_REFACTOR_TF$TELEGRAPH_ORIGIN_Z =
            SynchedEntityData.defineId(
                    KnightPhantom.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BOSS_REFACTOR_TF$TELEGRAPH_DIRECTION_X =
            SynchedEntityData.defineId(
                    KnightPhantom.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BOSS_REFACTOR_TF$TELEGRAPH_DIRECTION_Z =
            SynchedEntityData.defineId(
                    KnightPhantom.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BOSS_REFACTOR_TF$TELEGRAPH_LENGTH =
            SynchedEntityData.defineId(
                    KnightPhantom.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BOSS_REFACTOR_TF$TELEGRAPH_PROGRESS =
            SynchedEntityData.defineId(
                    KnightPhantom.class, EntityDataSerializers.FLOAT);
        @Unique
        private static final EntityDataAccessor<Boolean> BOSS_REFACTOR_TF$POSSESSION_ACTIVE =
            SynchedEntityData.defineId(
                KnightPhantom.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private final Set<UUID> bossRefactorTF$chargeSegmentHits = new HashSet<>();
    @Unique
    @Nullable
    private UUID bossRefactorTF$encounterId;
    @Unique
    private boolean bossRefactorTF$queuedForChargeRound;
    @Unique
    private boolean bossRefactorTF$chargeRoundActive;
    @Unique
    private int bossRefactorTF$remainingChargeSegments;
    @Unique
    private long bossRefactorTF$chargeReadyGameTime;
    @Unique
    private boolean bossRefactorTF$parryStunActive;
    @Unique
    private long bossRefactorTF$parryStunEndGameTime;
    @Unique
    private boolean bossRefactorTF$postChargePossessionActive;
    @Unique
    private boolean bossRefactorTF$formationControlActive;
    @Unique
    private boolean bossRefactorTF$customFormationWrite;
    @Unique
    private int bossRefactorTF$maximumObservedKnights = 1;
    @Unique
    private boolean bossRefactorTF$lastKnightPhaseActive;

    protected KnightPhantomMixin(
            EntityType<? extends FlyingMob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = {"defineSynchedData", "m_8097_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$defineSynchedData(CallbackInfo callback) {
        getEntityData().define(BOSS_REFACTOR_TF$TELEGRAPH_ACTIVE, false);
        getEntityData().define(BOSS_REFACTOR_TF$TELEGRAPH_ORIGIN_X, 0.0F);
        getEntityData().define(BOSS_REFACTOR_TF$TELEGRAPH_ORIGIN_Y, 0.0F);
        getEntityData().define(BOSS_REFACTOR_TF$TELEGRAPH_ORIGIN_Z, 0.0F);
        getEntityData().define(BOSS_REFACTOR_TF$TELEGRAPH_DIRECTION_X, 0.0F);
        getEntityData().define(BOSS_REFACTOR_TF$TELEGRAPH_DIRECTION_Z, 1.0F);
        getEntityData().define(BOSS_REFACTOR_TF$TELEGRAPH_LENGTH, 0.0F);
        getEntityData().define(BOSS_REFACTOR_TF$TELEGRAPH_PROGRESS, 0.0F);
        getEntityData().define(BOSS_REFACTOR_TF$POSSESSION_ACTIVE, false);
    }

    @Inject(
            method = {"aiStep", "m_8107_"},
            at = @At("HEAD"),
            remap = false)
    private void bossRefactorTF$beforeAiStep(CallbackInfo callback) {
        if (level().isClientSide()) {
            return;
        }
        bossRefactorTF$formationControlActive =
                KnightPhantomCombat.customChargesEnabled();
        if (bossRefactorTF$formationControlActive) {
            KnightPhantom knight = bossRefactorTF$self();
            KnightPhantomCombat.ensureChargeRoundProgress(knight);
            KnightPhantomCombat.tickCustomFormation(knight);
        }
    }

    @Inject(
            method = {"aiStep", "m_8107_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$afterAiStep(CallbackInfo callback) {
        if (level().isClientSide()) {
            return;
        }

        KnightPhantom knight = bossRefactorTF$self();
        BossRefactorTwilightForestConfig.KnightPhantomConfig config =
                BossRefactorTwilightForestConfig.KNIGHT_PHANTOM;
        if (!config.enabled.get()) {
            KnightPhantomCombat.stopCustomCombat(knight);
            return;
        }

        int livingKnights = KnightPhantomCombat.livingKnightCount(knight);
        bossRefactorTF$maximumObservedKnights = Math.max(
                bossRefactorTF$maximumObservedKnights, livingKnights);
        if (!bossRefactorTF$lastKnightPhaseActive
                && config.lastKnightEnabled.get()
                && bossRefactorTF$maximumObservedKnights > 1
                && livingKnights == 1
                && isAlive() && !isDeadOrDying()) {
            bossRefactorTF$activateLastKnightPhase();
        }

        if (KnightPhantomCombat.customChargesEnabled()) {
            if (KnightPhantomCombat.updateParryStun(knight)) {
                KnightPhantomCombat.confineKnightToArena(knight);
                return;
            }
            KnightPhantomCombat.refreshChargeDestination(knight);
            KnightPhantomCombat.synchronizeParryWindow(knight);
            bossRefactorTF$updateTelegraph();
            KnightPhantomCombat.attackChargePath(knight);
            KnightPhantomCombat.finishChargeAtDestination(knight);
        } else {
            KnightPhantomCombat.stopCustomCombat(knight);
        }
        KnightPhantomCombat.confineKnightToArena(knight);
    }

    @Inject(
            method = "switchToFormation",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$guardFormationWrites(
            KnightPhantom.Formation formation, CallbackInfo callback) {
        if (bossRefactorTF$formationControlActive
                && KnightPhantomCombat.customChargesEnabled()
                && !bossRefactorTF$customFormationWrite) {
            callback.cancel();
        }
    }

    @Inject(
            method = "getMaxTicksForFormation",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$getConfiguredFormationTicks(
            CallbackInfoReturnable<Integer> callback) {
        if (!KnightPhantomCombat.customChargesEnabled()
            || !KnightPhantomCombat.isPossessionOwner(
                bossRefactorTF$self())) {
            return;
        }
        KnightPhantom.Formation formation = bossRefactorTF$self()
                .getCurrentFormation();
        if (formation == KnightPhantom.Formation.ATTACK_PLAYER_START) {
            callback.setReturnValue(KnightPhantomCombat.chargeWindupTicks(
                bossRefactorTF$self()));
        } else if (formation == KnightPhantom.Formation.ATTACK_PLAYER_ATTACK) {
            callback.setReturnValue(bossRefactorTF$postChargePossessionActive
                    ? BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                            .postChargePossessionTicks.get()
                    : BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                            .chargeDurationTicks.get());
        }
    }

    @Inject(
            method = {"doHurtTarget", "m_7327_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$replaceChargeMeleeAttack(
            Entity target, CallbackInfoReturnable<Boolean> callback) {
        KnightPhantom knight = bossRefactorTF$self();
        if (KnightPhantomCombat.interceptsCurrentMeleeAttack(knight)) {
            callback.setReturnValue(
                    KnightPhantomCombat.attackChargeTarget(knight, target));
        }
    }

    @Inject(
            method = {"die", "m_6667_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$finishChargeWhenKilled(
            DamageSource source, CallbackInfo callback) {
        KnightPhantomCombat.finishDeadKnight(bossRefactorTF$self());
    }

    @Inject(
            method = {"addAdditionalSaveData", "m_7380_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$saveCombatState(
            CompoundTag tag, CallbackInfo callback) {
        tag.putInt("BossRefactorTFMaximumObservedKnights",
                bossRefactorTF$maximumObservedKnights);
        if (bossRefactorTF$encounterId != null) {
            tag.putUUID("BossRefactorTFEncounterId",
                bossRefactorTF$encounterId);
        }
        tag.putBoolean("BossRefactorTFLastKnightPhase",
                bossRefactorTF$lastKnightPhaseActive);
        tag.putBoolean("BossRefactorTFQueuedCharge",
                bossRefactorTF$queuedForChargeRound);
        tag.putBoolean("BossRefactorTFActiveCharge",
                bossRefactorTF$chargeRoundActive);
        tag.putBoolean("BossRefactorTFPostChargePossession",
            bossRefactorTF$postChargePossessionActive);
        tag.putInt("BossRefactorTFRemainingChargeSegments",
                bossRefactorTF$remainingChargeSegments);
        tag.putLong("BossRefactorTFChargeReadyGameTime",
            bossRefactorTF$chargeReadyGameTime);
        tag.putBoolean("BossRefactorTFParryStunActive",
            bossRefactorTF$parryStunActive);
        tag.putLong("BossRefactorTFParryStunEndGameTime",
            bossRefactorTF$parryStunEndGameTime);
        BlockPos chargePosition = bossRefactorTF$self().getChargePos();
        tag.putInt("BossRefactorTFChargeX", chargePosition.getX());
        tag.putInt("BossRefactorTFChargeY", chargePosition.getY());
        tag.putInt("BossRefactorTFChargeZ", chargePosition.getZ());
    }

    @Inject(
            method = {"readAdditionalSaveData", "m_7378_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$loadCombatState(
            CompoundTag tag, CallbackInfo callback) {
        bossRefactorTF$maximumObservedKnights = Math.max(1,
                tag.getInt("BossRefactorTFMaximumObservedKnights"));
        bossRefactorTF$encounterId = tag.hasUUID("BossRefactorTFEncounterId")
            ? tag.getUUID("BossRefactorTFEncounterId")
            : null;
        bossRefactorTF$lastKnightPhaseActive =
                tag.getBoolean("BossRefactorTFLastKnightPhase");
        bossRefactorTF$queuedForChargeRound =
                tag.getBoolean("BossRefactorTFQueuedCharge");
        bossRefactorTF$chargeRoundActive =
                tag.getBoolean("BossRefactorTFActiveCharge");
        bossRefactorTF$postChargePossessionActive =
            bossRefactorTF$chargeRoundActive
                && tag.getBoolean("BossRefactorTFPostChargePossession");
        bossRefactorTF$remainingChargeSegments = Math.max(0,
                tag.getInt("BossRefactorTFRemainingChargeSegments"));
        bossRefactorTF$chargeReadyGameTime = Math.max(0L,
            tag.getLong("BossRefactorTFChargeReadyGameTime"));
        bossRefactorTF$parryStunActive = bossRefactorTF$chargeRoundActive
            && tag.getBoolean("BossRefactorTFParryStunActive");
        bossRefactorTF$parryStunEndGameTime = bossRefactorTF$parryStunActive
            ? Math.max(0L, tag.getLong("BossRefactorTFParryStunEndGameTime"))
            : 0L;
        if (tag.contains("BossRefactorTFChargeX")
            && tag.contains("BossRefactorTFChargeY")
            && tag.contains("BossRefactorTFChargeZ")) {
            bossRefactorTF$self().setChargePos(new BlockPos(
                tag.getInt("BossRefactorTFChargeX"),
                tag.getInt("BossRefactorTFChargeY"),
                tag.getInt("BossRefactorTFChargeZ")));
        }
        bossRefactorTF$chargeSegmentHits.clear();
        bossRefactorTF$setPossessionVisible(false);
        bossRefactorTF$setTelegraphActive(false);
        bossRefactorTF$formationControlActive =
                KnightPhantomCombat.customChargesEnabled();
    }

    @Override
    public AttackTelegraph bossRefactorTF$getAttackTelegraph() {
        SynchedEntityData data = getEntityData();
        if (!data.get(BOSS_REFACTOR_TF$TELEGRAPH_ACTIVE)) {
            return AttackTelegraph.NONE;
        }
        return new AttackTelegraph(
                AttackTelegraphShape.CORRIDOR,
                data.get(BOSS_REFACTOR_TF$TELEGRAPH_ORIGIN_X),
                data.get(BOSS_REFACTOR_TF$TELEGRAPH_ORIGIN_Y),
                data.get(BOSS_REFACTOR_TF$TELEGRAPH_ORIGIN_Z),
                data.get(BOSS_REFACTOR_TF$TELEGRAPH_DIRECTION_X),
                data.get(BOSS_REFACTOR_TF$TELEGRAPH_DIRECTION_Z),
                data.get(BOSS_REFACTOR_TF$TELEGRAPH_LENGTH),
                getBbWidth() * 0.5F
                    + BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .chargeHitboxInflation.get().floatValue(),
                0.0F,
                data.get(BOSS_REFACTOR_TF$TELEGRAPH_PROGRESS));
    }

    @Override
    public void bossRefactorTF$setAttackTelegraph(AttackTelegraph telegraph) {
        if (telegraph.shape() == AttackTelegraphShape.NONE) {
            bossRefactorTF$setTelegraphActive(false);
            return;
        }
        SynchedEntityData data = getEntityData();
        data.set(BOSS_REFACTOR_TF$TELEGRAPH_ORIGIN_X, telegraph.originX());
        data.set(BOSS_REFACTOR_TF$TELEGRAPH_ORIGIN_Y, telegraph.originY());
        data.set(BOSS_REFACTOR_TF$TELEGRAPH_ORIGIN_Z, telegraph.originZ());
        data.set(BOSS_REFACTOR_TF$TELEGRAPH_DIRECTION_X, telegraph.directionX());
        data.set(BOSS_REFACTOR_TF$TELEGRAPH_DIRECTION_Z, telegraph.directionZ());
        data.set(BOSS_REFACTOR_TF$TELEGRAPH_LENGTH, telegraph.length());
        data.set(BOSS_REFACTOR_TF$TELEGRAPH_PROGRESS, telegraph.progress());
        bossRefactorTF$setTelegraphActive(true);
    }

    @Override
    public boolean bossRefactorTF$isCurrentAttackParryable() {
        if (!KnightPhantomCombat.isPossessionOwner(
                bossRefactorTF$self())
                || !KnightPhantomCombat.hasRetainableChargeTarget(
                        bossRefactorTF$self())
            || bossRefactorTF$parryStunActive
                || bossRefactorTF$postChargePossessionActive
                || !KnightPhantomCombat.customChargesEnabled()
                || !BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                .slashBladeParryEnabled.get()) {
            return false;
        }
        KnightPhantom.Formation formation = bossRefactorTF$self()
                .getCurrentFormation();
        return formation == KnightPhantom.Formation.ATTACK_PLAYER_START
                || formation == KnightPhantom.Formation.ATTACK_PLAYER_ATTACK;
    }

    @Override
    public int bossRefactorTF$getParryFeedbackTicks() {
        if (!bossRefactorTF$isCurrentAttackParryable()) {
            return 0;
        }
        return BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                .parryFeedbackTicks.get();
    }

    @Override
    public int bossRefactorTF$acceptParry(
            @Nullable LivingEntity actor, ResourceLocation sourceId) {
        int feedbackTicks = bossRefactorTF$getParryFeedbackTicks();
        if (feedbackTicks <= 0) {
            return 0;
        }
        KnightPhantomCombat.beginParryStun(bossRefactorTF$self());
        return feedbackTicks;
    }

    @Override
    @Nullable
    public UUID bossRefactorTF$getEncounterId() {
        return bossRefactorTF$encounterId;
    }

    @Override
    public void bossRefactorTF$setEncounterId(
            @Nullable UUID encounterId) {
        bossRefactorTF$encounterId = encounterId;
    }

    @Override
    public boolean bossRefactorTF$isQueuedForChargeRound() {
        return bossRefactorTF$queuedForChargeRound;
    }

    @Override
    public void bossRefactorTF$setQueuedForChargeRound(boolean queued) {
        bossRefactorTF$queuedForChargeRound = queued;
    }

    @Override
    public void bossRefactorTF$switchToFormation(
            KnightPhantom.Formation formation) {
        boolean previous = bossRefactorTF$customFormationWrite;
        bossRefactorTF$customFormationWrite = true;
        try {
            bossRefactorTF$self().switchToFormation(formation);
        } finally {
            bossRefactorTF$customFormationWrite = previous;
        }
    }

    @Override
    public boolean bossRefactorTF$isChargeRoundActive() {
        return level().isClientSide()
                ? getEntityData().get(BOSS_REFACTOR_TF$POSSESSION_ACTIVE)
                : bossRefactorTF$chargeRoundActive;
    }

    @Override
    public void bossRefactorTF$setPossessionVisible(boolean visible) {
        getEntityData().set(BOSS_REFACTOR_TF$POSSESSION_ACTIVE, visible);
    }

    @Override
    public void bossRefactorTF$beginChargeRound(int segmentCount) {
        bossRefactorTF$queuedForChargeRound = false;
        bossRefactorTF$chargeRoundActive = true;
        bossRefactorTF$setPossessionVisible(true);
        bossRefactorTF$chargeReadyGameTime = 0L;
        bossRefactorTF$parryStunActive = false;
        bossRefactorTF$parryStunEndGameTime = 0L;
        bossRefactorTF$postChargePossessionActive = false;
        bossRefactorTF$remainingChargeSegments = Math.max(0, segmentCount - 1);
        bossRefactorTF$clearChargeSegmentHits();
        bossRefactorTF$initializeTelegraph();
    }

    @Override
    public long bossRefactorTF$getChargeReadyGameTime() {
        return bossRefactorTF$chargeReadyGameTime;
    }

    @Override
    public void bossRefactorTF$delayChargeUntil(long readyGameTime) {
        bossRefactorTF$chargeReadyGameTime = Math.max(
                bossRefactorTF$chargeReadyGameTime, readyGameTime);
    }

    @Override
    public boolean bossRefactorTF$isParryStunActive() {
        return bossRefactorTF$parryStunActive;
    }

    @Override
    public long bossRefactorTF$getParryStunEndGameTime() {
        return bossRefactorTF$parryStunEndGameTime;
    }

    @Override
    public void bossRefactorTF$beginParryStun(long endGameTime) {
        KnightPhantomCombat.closeParryWindow(bossRefactorTF$self());
        bossRefactorTF$parryStunActive = true;
        bossRefactorTF$parryStunEndGameTime = Math.max(
                level().getGameTime(), endGameTime);
        bossRefactorTF$chargeSegmentHits.clear();
        bossRefactorTF$setTelegraphActive(false);
    }

    @Override
    public void bossRefactorTF$finishParryStun() {
        bossRefactorTF$parryStunActive = false;
        bossRefactorTF$parryStunEndGameTime = 0L;
    }

    @Override
    public boolean bossRefactorTF$isPostChargePossessionActive() {
        return bossRefactorTF$postChargePossessionActive;
    }

    @Override
    public void bossRefactorTF$beginPostChargePossession() {
        KnightPhantomCombat.closeParryWindow(bossRefactorTF$self());
        bossRefactorTF$postChargePossessionActive = true;
        bossRefactorTF$remainingChargeSegments = 0;
        bossRefactorTF$chargeSegmentHits.clear();
        bossRefactorTF$setTelegraphActive(false);
    }

    @Override
    public boolean bossRefactorTF$consumeAdditionalChargeSegment() {
        if (bossRefactorTF$remainingChargeSegments <= 0) {
            return false;
        }
        bossRefactorTF$remainingChargeSegments--;
        return true;
    }

    @Override
    public void bossRefactorTF$finishChargeRound() {
        KnightPhantomCombat.closeParryWindow(bossRefactorTF$self());
        bossRefactorTF$queuedForChargeRound = false;
        bossRefactorTF$chargeRoundActive = false;
        bossRefactorTF$setPossessionVisible(false);
        bossRefactorTF$chargeReadyGameTime = 0L;
        bossRefactorTF$parryStunActive = false;
        bossRefactorTF$parryStunEndGameTime = 0L;
        bossRefactorTF$postChargePossessionActive = false;
        bossRefactorTF$remainingChargeSegments = 0;
        bossRefactorTF$chargeSegmentHits.clear();
        bossRefactorTF$setTelegraphActive(false);
    }

    @Override
    public void bossRefactorTF$cancelRemainingChargeSegments() {
        bossRefactorTF$remainingChargeSegments = 0;
    }

    @Override
    public void bossRefactorTF$clearChargeSegmentHits() {
        bossRefactorTF$chargeSegmentHits.clear();
    }

    @Override
    public boolean bossRefactorTF$markChargeSegmentHit(UUID targetId) {
        return bossRefactorTF$chargeSegmentHits.add(targetId);
    }

    @Override
    public int bossRefactorTF$getFallenKnightCount() {
        return KnightPhantomCombatRules.fallenKnightCount(
                bossRefactorTF$maximumObservedKnights,
                KnightPhantomCombat.livingKnightCount(bossRefactorTF$self()));
    }

    @Override
    public boolean bossRefactorTF$isLastKnightPhaseActive() {
        return bossRefactorTF$lastKnightPhaseActive;
    }

    @Override
    public void bossRefactorTF$activateLastKnightPhase() {
        if (bossRefactorTF$lastKnightPhaseActive
                || !isAlive() || isDeadOrDying()) {
            return;
        }
        bossRefactorTF$lastKnightPhaseActive = true;
        bossRefactorTF$maximumObservedKnights = Math.max(
                2, bossRefactorTF$maximumObservedKnights);
        BossRefactorTwilightForestConfig.KnightPhantomConfig config =
            BossRefactorTwilightForestConfig.KNIGHT_PHANTOM;
        if (KnightPhantomCombat.isPossessionOwner(bossRefactorTF$self())
            && !bossRefactorTF$postChargePossessionActive
            && config.lastKnightMultiChargeEnabled.get()) {
            bossRefactorTF$remainingChargeSegments =
                KnightPhantomCombatRules.lastKnightRemainingChargeSegments(
                    bossRefactorTF$remainingChargeSegments,
                    config.lastKnightChargeCount.get());
        }
        if (config.lastKnightFullHealEnabled.get()) {
            setHealth(getMaxHealth());
        }
    }

    @Unique
    private void bossRefactorTF$initializeTelegraph() {
        KnightPhantom knight = bossRefactorTF$self();
        Vec3 origin = knight.position();
        Vec3 destination = Vec3.atLowerCornerOf(knight.getChargePos());
        Vec3 direction = destination.subtract(origin).multiply(1.0, 0.0, 1.0);
        if (direction.lengthSqr() < 1.0E-8) {
            direction = knight.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        if (direction.lengthSqr() < 1.0E-8) {
            direction = new Vec3(0.0, 0.0, 1.0);
        }
        float length = (float) Math.max(2.0,
                destination.multiply(1.0, 0.0, 1.0)
                        .distanceTo(origin.multiply(1.0, 0.0, 1.0)));
        bossRefactorTF$setAttackTelegraph(new AttackTelegraph(
                AttackTelegraphShape.CORRIDOR,
                (float) origin.x, (float) origin.y, (float) origin.z,
                (float) direction.normalize().x,
                (float) direction.normalize().z,
                length, getBbWidth() * 0.5F
                    + BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .chargeHitboxInflation.get().floatValue(),
                0.0F, 0.0F));
    }

    @Unique
    private void bossRefactorTF$updateTelegraph() {
        KnightPhantom knight = bossRefactorTF$self();
        if (!KnightPhantomCombat.isPossessionOwner(knight)
                || !KnightPhantomCombat.hasRetainableChargeTarget(knight)
                || knight.getCurrentFormation()
                != KnightPhantom.Formation.ATTACK_PLAYER_START) {
            bossRefactorTF$setTelegraphActive(false);
            return;
        }
        float progress = AttackTelegraph.windupProgress(
            knight.getTicksProgress(),
            KnightPhantomCombat.chargeWindupTicks(knight));
        bossRefactorTF$initializeTelegraph();
        getEntityData().set(BOSS_REFACTOR_TF$TELEGRAPH_PROGRESS, progress);
    }

    @Unique
    private void bossRefactorTF$setTelegraphActive(boolean active) {
        getEntityData().set(BOSS_REFACTOR_TF$TELEGRAPH_ACTIVE, active);
    }

    @Unique
    private KnightPhantom bossRefactorTF$self() {
        return (KnightPhantom) (Object) this;
    }
}