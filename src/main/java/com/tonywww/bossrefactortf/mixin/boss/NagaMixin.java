package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.naga.NagaCombat;
import com.tonywww.bossrefactortf.boss.naga.NagaCombatAccess;
import com.tonywww.bossrefactortf.boss.naga.NagaCombatGoal;
import com.tonywww.bossrefactortf.boss.naga.NagaCombatState;
import com.tonywww.bossrefactortf.boss.naga.NagaBodyController;
import com.tonywww.bossrefactortf.boss.naga.NagaBodyPose;
import com.tonywww.bossrefactortf.boss.naga.NagaBodyState;
import com.tonywww.bossrefactortf.boss.naga.NagaStunAwareGoal;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphAccess;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphNbt;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.boss.Naga;
import twilightforest.entity.boss.NagaSegment;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = Naga.class, remap = false)
public abstract class NagaMixin extends Monster
        implements NagaCombatAccess, AttackTelegraphAccess,
        TwilightBossParryAccess {
    @Shadow
    @Final
    private NagaSegment[] bodySegments;

    @Unique
    private static final EntityDataAccessor<Boolean>
            BOSS_REFACTOR_TF$STUN_IMMUNITY = SynchedEntityData.defineId(
                    Naga.class, EntityDataSerializers.BOOLEAN);
        @Unique
        private static final EntityDataAccessor<Boolean>
            BOSS_REFACTOR_TF$FIRE_SNAKE_ACTIVE = SynchedEntityData.defineId(
                Naga.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<CompoundTag>
            BOSS_REFACTOR_TF$PRIMARY_TELEGRAPH = SynchedEntityData.defineId(
                    Naga.class, EntityDataSerializers.COMPOUND_TAG);
    @Unique
    private static final EntityDataAccessor<Integer>
            BOSS_REFACTOR_TF$BODY_POSE = SynchedEntityData.defineId(
                Naga.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Float>
            BOSS_REFACTOR_TF$BODY_POSE_PROGRESS = SynchedEntityData.defineId(
                Naga.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float>
            BOSS_REFACTOR_TF$BODY_DIRECTION_X = SynchedEntityData.defineId(
                Naga.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float>
            BOSS_REFACTOR_TF$BODY_DIRECTION_Z = SynchedEntityData.defineId(
                Naga.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<CompoundTag>
            BOSS_REFACTOR_TF$SECONDARY_TELEGRAPH = SynchedEntityData.defineId(
                    Naga.class, EntityDataSerializers.COMPOUND_TAG);

    @Unique
    private final NagaCombatState bossRefactorTF$combatState =
            new NagaCombatState();
    @Unique
    private final NagaBodyState bossRefactorTF$bodyState =
            new NagaBodyState();

    protected NagaMixin(
            EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = {"registerGoals", "m_8099_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$replaceMovementPattern(CallbackInfo callback) {
        if (!BossRefactorTwilightForestConfig.NAGA.enabled.get()) {
            return;
        }
        List<Goal> replacedGoals = new ArrayList<>();
        List<WrappedGoal> guardedGoals = new ArrayList<>();
        for (WrappedGoal wrappedGoal : goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            String goalName = goal.getClass().getSimpleName();
            if (goalName.equals("NagaMovementPattern")
                    || goalName.equals("SimplifiedAttackGoal")
                    || goalName.equals("SimplifiedMeleeAttackGoal")) {
                replacedGoals.add(goal);
            } else if (goalName.equals("NagaSmashGoal")) {
                guardedGoals.add(wrappedGoal);
            }
        }
        replacedGoals.forEach(goalSelector::removeGoal);
        for (WrappedGoal guardedGoal : guardedGoals) {
            goalSelector.removeGoal(guardedGoal.getGoal());
            goalSelector.addGoal(guardedGoal.getPriority(),
                    new NagaStunAwareGoal(
                            bossRefactorTF$self(), guardedGoal.getGoal()));
        }
        goalSelector.addGoal(1, new NagaCombatGoal(bossRefactorTF$self()));
    }

    @Inject(
            method = {"defineSynchedData", "m_8097_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$defineCombatData(CallbackInfo callback) {
        getEntityData().define(BOSS_REFACTOR_TF$STUN_IMMUNITY, false);
        getEntityData().define(BOSS_REFACTOR_TF$FIRE_SNAKE_ACTIVE, false);
        getEntityData().define(
                BOSS_REFACTOR_TF$PRIMARY_TELEGRAPH,
                AttackTelegraphNbt.encode(AttackTelegraph.NONE));
        getEntityData().define(
                BOSS_REFACTOR_TF$SECONDARY_TELEGRAPH,
                AttackTelegraphNbt.encode(AttackTelegraph.NONE));
        getEntityData().define(BOSS_REFACTOR_TF$BODY_POSE,
            NagaBodyPose.TRAIL.ordinal());
        getEntityData().define(BOSS_REFACTOR_TF$BODY_POSE_PROGRESS, 0.0F);
        getEntityData().define(BOSS_REFACTOR_TF$BODY_DIRECTION_X, 0.0F);
        getEntityData().define(BOSS_REFACTOR_TF$BODY_DIRECTION_Z, 1.0F);
    }

    @Inject(method = "moveSegments", at = @At("HEAD"),
            cancellable = true, remap = false)
    private void bossRefactorTF$controlBody(CallbackInfo callback) {
        if (!NagaCombat.customCombatEnabled() || !isAlive()) {
            return;
        }
        NagaBodyController.tick(
                bossRefactorTF$self(), bodySegments, bossRefactorTF$bodyState,
                bossRefactorTF$getBodyPose(),
                bossRefactorTF$getBodyPoseProgress(),
                bossRefactorTF$getBodyDirectionX(),
                bossRefactorTF$getBodyDirectionZ());
        callback.cancel();
    }

    @Inject(
            method = {"doHurtTarget", "m_7327_"},
            at = @At("HEAD"), cancellable = true, remap = false)
    private void bossRefactorTF$blockAttackWhileStunned(
            Entity target, CallbackInfoReturnable<Boolean> callback) {
        if (NagaCombat.isStunned(bossRefactorTF$self())) {
            callback.setReturnValue(false);
        }
    }

    @Inject(
            method = {"addAdditionalSaveData", "m_7380_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$saveCombatState(
            CompoundTag tag, CallbackInfo callback) {
        tag.put("BossRefactorTFNagaCombat", bossRefactorTF$combatState.save());
    }

    @Inject(
            method = {"readAdditionalSaveData", "m_7378_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$loadCombatState(
            CompoundTag tag, CallbackInfo callback) {
        if (tag.contains("BossRefactorTFNagaCombat", Tag.TAG_COMPOUND)) {
            bossRefactorTF$combatState.load(
                    tag.getCompound("BossRefactorTFNagaCombat"));
        }
        NagaCombat.restoreVisualState(bossRefactorTF$self());
    }

    @Override
    public NagaCombatState bossRefactorTF$getNagaCombatState() {
        return bossRefactorTF$combatState;
    }

    @Override
    public NagaBodyState bossRefactorTF$getNagaBodyState() {
        return bossRefactorTF$bodyState;
    }

    @Override
    public NagaBodyPose bossRefactorTF$getBodyPose() {
        return NagaBodyPose.byId(getEntityData().get(
                BOSS_REFACTOR_TF$BODY_POSE));
    }

    @Override
    public void bossRefactorTF$setBodyPose(NagaBodyPose pose) {
        getEntityData().set(BOSS_REFACTOR_TF$BODY_POSE, pose.ordinal());
    }

    @Override
    public float bossRefactorTF$getBodyPoseProgress() {
        return getEntityData().get(BOSS_REFACTOR_TF$BODY_POSE_PROGRESS);
    }

    @Override
    public void bossRefactorTF$setBodyPoseProgress(float progress) {
        getEntityData().set(BOSS_REFACTOR_TF$BODY_POSE_PROGRESS, progress);
    }

    @Override
    public double bossRefactorTF$getBodyDirectionX() {
        return getEntityData().get(BOSS_REFACTOR_TF$BODY_DIRECTION_X);
    }

    @Override
    public double bossRefactorTF$getBodyDirectionZ() {
        return getEntityData().get(BOSS_REFACTOR_TF$BODY_DIRECTION_Z);
    }

    @Override
    public void bossRefactorTF$setBodyDirection(
            double directionX, double directionZ) {
        getEntityData().set(
                BOSS_REFACTOR_TF$BODY_DIRECTION_X, (float) directionX);
        getEntityData().set(
                BOSS_REFACTOR_TF$BODY_DIRECTION_Z, (float) directionZ);
    }

    @Override
    public boolean bossRefactorTF$isStunImmunityVisible() {
        return getEntityData().get(BOSS_REFACTOR_TF$STUN_IMMUNITY);
    }

    @Override
    public void bossRefactorTF$setStunImmunityVisible(boolean visible) {
        getEntityData().set(BOSS_REFACTOR_TF$STUN_IMMUNITY, visible);
    }

    @Override
    public boolean bossRefactorTF$isFireSnakeVisualActive() {
        return getEntityData().get(BOSS_REFACTOR_TF$FIRE_SNAKE_ACTIVE);
    }

    @Override
    public void bossRefactorTF$setFireSnakeVisualActive(boolean active) {
        getEntityData().set(BOSS_REFACTOR_TF$FIRE_SNAKE_ACTIVE, active);
    }

    @Override
    public AttackTelegraph bossRefactorTF$getAttackTelegraph() {
        AttackTelegraph primary = AttackTelegraphNbt.decode(
                getEntityData().get(BOSS_REFACTOR_TF$PRIMARY_TELEGRAPH));
        if (primary.shape() != AttackTelegraphShape.NONE) {
            return primary;
        }
        return AttackTelegraphNbt.decode(
                getEntityData().get(BOSS_REFACTOR_TF$SECONDARY_TELEGRAPH));
    }

    @Override
    public List<AttackTelegraph> bossRefactorTF$getAttackTelegraphs() {
        List<AttackTelegraph> telegraphs = new ArrayList<>(2);
        AttackTelegraph primary = AttackTelegraphNbt.decode(
                getEntityData().get(BOSS_REFACTOR_TF$PRIMARY_TELEGRAPH));
        AttackTelegraph secondary = AttackTelegraphNbt.decode(
                getEntityData().get(BOSS_REFACTOR_TF$SECONDARY_TELEGRAPH));
        if (primary.shape() != AttackTelegraphShape.NONE) {
            telegraphs.add(primary);
        }
        if (secondary.shape() != AttackTelegraphShape.NONE) {
            telegraphs.add(secondary);
        }
        return telegraphs;
    }

    @Override
    public void bossRefactorTF$setAttackTelegraph(AttackTelegraph telegraph) {
        bossRefactorTF$setPrimaryTelegraph(telegraph);
    }

    @Override
    public void bossRefactorTF$setPrimaryTelegraph(AttackTelegraph telegraph) {
        getEntityData().set(
                BOSS_REFACTOR_TF$PRIMARY_TELEGRAPH,
                AttackTelegraphNbt.encode(telegraph));
    }

    @Override
    public void bossRefactorTF$setSecondaryTelegraph(AttackTelegraph telegraph) {
        getEntityData().set(
                BOSS_REFACTOR_TF$SECONDARY_TELEGRAPH,
                AttackTelegraphNbt.encode(telegraph));
    }

    @Override
    public boolean bossRefactorTF$isCurrentAttackParryable() {
        return NagaCombat.customCombatEnabled()
                && NagaCombat.isCurrentAttackParryable(bossRefactorTF$self());
    }

    @Override
    public int bossRefactorTF$getParryFeedbackTicks() {
        return bossRefactorTF$isCurrentAttackParryable()
                ? BossRefactorTwilightForestConfig.NAGA
                        .parryFeedbackTicks.get()
                : 0;
    }

    @Override
    public boolean bossRefactorTF$suppressesExternalParryStun() {
        return NagaCombat.suppressesExternalParryStun(bossRefactorTF$self());
    }

    @Override
    public int bossRefactorTF$acceptParry(
            @Nullable LivingEntity actor, ResourceLocation sourceId) {
        int feedbackTicks = bossRefactorTF$getParryFeedbackTicks();
        return feedbackTicks > 0
                && NagaCombat.acceptParry(bossRefactorTF$self())
                ? feedbackTicks
                : 0;
    }

    @Unique
    private Naga bossRefactorTF$self() {
        return (Naga) (Object) this;
    }
}