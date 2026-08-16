package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenCombat;
import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenCombatAccess;
import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenCombatGoal;
import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenCombatState;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphAccess;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.ai.goal.HoverBeamGoal;
import twilightforest.entity.ai.goal.HoverSummonGoal;
import twilightforest.entity.ai.goal.HoverThenDropGoal;
import twilightforest.entity.boss.SnowQueen;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = SnowQueen.class, remap = false)
public abstract class SnowQueenMixin extends Monster
        implements SnowQueenCombatAccess, AttackTelegraphAccess,
        TwilightBossParryAccess {
    @Unique
    private static final EntityDataAccessor<Boolean> BOSS_REFACTOR_TF$CUSTOM_COMBAT_ACTIVE =
            SynchedEntityData.defineId(
                SnowQueen.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Integer> BOSS_REFACTOR_TF$ACTIVE_SHIELD_COUNT =
            SynchedEntityData.defineId(
                SnowQueen.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Float> BOSS_REFACTOR_TF$SHIELD_ORBIT_RADIUS =
            SynchedEntityData.defineId(
                SnowQueen.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BOSS_REFACTOR_TF$SHIELD_ANGULAR_SPEED =
            SynchedEntityData.defineId(
                SnowQueen.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Float> BOSS_REFACTOR_TF$SHIELD_VERTICAL_OFFSET =
            SynchedEntityData.defineId(
                SnowQueen.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Boolean> BOSS_REFACTOR_TF$SECOND_PHASE =
            SynchedEntityData.defineId(
                    SnowQueen.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<CompoundTag> BOSS_REFACTOR_TF$TELEGRAPH =
            SynchedEntityData.defineId(
                    SnowQueen.class, EntityDataSerializers.COMPOUND_TAG);

    @Unique
    private final SnowQueenCombatState bossRefactorTF$combatState =
            new SnowQueenCombatState();

    protected SnowQueenMixin(
            EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyConstant(
            method = "<init>",
            constant = @Constant(intValue = 7),
            require = 1,
            remap = false)
    private int bossRefactorTF$expandIceShieldArray(int originalCapacity) {
        return 8;
    }

    @Inject(
            method = {"registerGoals", "m_8099_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$replaceCombatGoals(CallbackInfo callback) {
        if (!BossRefactorTwilightForestConfig.SNOW_QUEEN.enabled.get()) {
            return;
        }
        List<Goal> replacedGoals = new ArrayList<>();
        for (WrappedGoal wrappedGoal : goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            if (goal instanceof HoverSummonGoal
                    || goal instanceof HoverThenDropGoal
                    || goal instanceof HoverBeamGoal) {
                replacedGoals.add(goal);
            }
        }
        replacedGoals.forEach(goalSelector::removeGoal);
        goalSelector.addGoal(0,
                new SnowQueenCombatGoal(bossRefactorTF$self()));
    }

    @Inject(
            method = {"defineSynchedData", "m_8097_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$defineCombatData(CallbackInfo callback) {
        getEntityData().define(BOSS_REFACTOR_TF$CUSTOM_COMBAT_ACTIVE, false);
        getEntityData().define(BOSS_REFACTOR_TF$ACTIVE_SHIELD_COUNT, 7);
        getEntityData().define(BOSS_REFACTOR_TF$SHIELD_ORBIT_RADIUS, 1.5F);
        getEntityData().define(BOSS_REFACTOR_TF$SHIELD_ANGULAR_SPEED, 5.0F);
        getEntityData().define(BOSS_REFACTOR_TF$SHIELD_VERTICAL_OFFSET, 0.0F);
        getEntityData().define(BOSS_REFACTOR_TF$SECOND_PHASE, false);
        getEntityData().define(
                BOSS_REFACTOR_TF$TELEGRAPH,
                AttackTelegraphNbt.encode(AttackTelegraph.NONE));
    }

    @Inject(
            method = {"aiStep", "m_8107_"},
            at = @At("HEAD"),
            remap = false)
    private void bossRefactorTF$disableOriginalCombatState(
            CallbackInfo callback) {
        if (!SnowQueenCombat.customCombatActive(bossRefactorTF$self())) {
            return;
        }
        SnowQueen queen = bossRefactorTF$self();
        if (queen.getCurrentPhase() != SnowQueen.Phase.SUMMON) {
            queen.setCurrentPhase(SnowQueen.Phase.SUMMON);
        }
        if (queen.isBreathing()) {
            queen.setBreathing(false);
        }
    }

    @Inject(
            method = {"tick", "m_8119_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$tickBaseTraits(CallbackInfo callback) {
        SnowQueenCombat.tickBaseTraits(bossRefactorTF$self());
    }

        @Group(name = "disableOriginalPhaseTransitions", min = 1, max = 1)
        @Inject(
                method = {"customServerAiStep", "m_8024_"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Monster;customServerAiStep()V",
                    shift = At.Shift.AFTER),
            cancellable = true,
            require = 0,
            remap = false)
        private void bossRefactorTF$disableOriginalPhaseTransitionsMcp(
            CallbackInfo callback) {
        bossRefactorTF$disableOriginalPhaseTransitions(callback);
        }

        @Group(name = "disableOriginalPhaseTransitions", min = 1, max = 1)
        @Inject(
                method = {"customServerAiStep", "m_8024_"},
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/monster/Monster;m_8024_()V",
                shift = At.Shift.AFTER),
            cancellable = true,
            require = 0,
            remap = false)
        private void bossRefactorTF$disableOriginalPhaseTransitionsSrg(
            CallbackInfo callback) {
        bossRefactorTF$disableOriginalPhaseTransitions(callback);
        }

        @Unique
        private void bossRefactorTF$disableOriginalPhaseTransitions(
            CallbackInfo callback) {
        if (BossRefactorTwilightForestConfig.SNOW_QUEEN.enabled.get()) {
            callback.cancel();
        }
    }

    @Inject(
            method = "getIceShieldPosition(I)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$getConfiguredIceShieldPosition(
            int index, CallbackInfoReturnable<Vec3> callback) {
        SnowQueen queen = bossRefactorTF$self();
        if (SnowQueenCombat.customCombatActive(queen)) {
            callback.setReturnValue(SnowQueenCombat.iceShieldPosition(
                    queen, index));
        } else if (index >= 7) {
            callback.setReturnValue(SnowQueenCombat.hiddenIceShieldPosition(
                    queen));
        }
    }

    @Inject(
            method = "applyShieldCollisions",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$disableOriginalShieldCollisions(
            Entity shield, CallbackInfo callback) {
        SnowQueen queen = bossRefactorTF$self();
        if (SnowQueenCombat.customCombatActive(queen)
                || bossRefactorTF$isExtraIceShield(queen, shield)) {
            callback.cancel();
        }
    }

    @Unique
    private boolean bossRefactorTF$isExtraIceShield(
            SnowQueen queen, Entity shield) {
        for (int index = 7; index < queen.iceArray.length; index++) {
            if (queen.iceArray[index] == shield) {
                return true;
            }
        }
        return false;
    }

    @Inject(
            method = {"addAdditionalSaveData", "m_7380_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$saveCombatState(
            CompoundTag tag, CallbackInfo callback) {
        tag.put("BossRefactorTFSnowQueenCombat",
                bossRefactorTF$combatState.save());
    }

    @Inject(
            method = {"readAdditionalSaveData", "m_7378_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$loadCombatState(
            CompoundTag tag, CallbackInfo callback) {
        if (tag.contains("BossRefactorTFSnowQueenCombat", Tag.TAG_COMPOUND)) {
            bossRefactorTF$combatState.load(
                    tag.getCompound("BossRefactorTFSnowQueenCombat"));
        }
        SnowQueenCombat.restoreVisualState(bossRefactorTF$self());
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason.shouldDestroy()) {
            SnowQueenCombat.cleanupAfterOwnerDeath(bossRefactorTF$self());
        }
        super.remove(reason);
    }

    @Override
    public SnowQueenCombatState bossRefactorTF$getSnowQueenCombatState() {
        return bossRefactorTF$combatState;
    }

    @Override
    public boolean bossRefactorTF$isSnowQueenCustomCombatActive() {
        return getEntityData().get(BOSS_REFACTOR_TF$CUSTOM_COMBAT_ACTIVE);
    }

    @Override
    public void bossRefactorTF$setSnowQueenCustomCombatActive(boolean active) {
        getEntityData().set(BOSS_REFACTOR_TF$CUSTOM_COMBAT_ACTIVE, active);
    }

    @Override
    public int bossRefactorTF$getSnowQueenActiveShieldCount() {
        return getEntityData().get(BOSS_REFACTOR_TF$ACTIVE_SHIELD_COUNT);
    }

    @Override
    public void bossRefactorTF$setSnowQueenActiveShieldCount(int count) {
        getEntityData().set(BOSS_REFACTOR_TF$ACTIVE_SHIELD_COUNT, count);
    }

    @Override
    public double bossRefactorTF$getSnowQueenShieldOrbitRadius() {
        return getEntityData().get(BOSS_REFACTOR_TF$SHIELD_ORBIT_RADIUS);
    }

    @Override
    public void bossRefactorTF$setSnowQueenShieldOrbitRadius(double radius) {
        getEntityData().set(BOSS_REFACTOR_TF$SHIELD_ORBIT_RADIUS,
                (float) radius);
    }

    @Override
    public double bossRefactorTF$getSnowQueenShieldAngularSpeed() {
        return getEntityData().get(BOSS_REFACTOR_TF$SHIELD_ANGULAR_SPEED);
    }

    @Override
    public void bossRefactorTF$setSnowQueenShieldAngularSpeed(double speed) {
        getEntityData().set(BOSS_REFACTOR_TF$SHIELD_ANGULAR_SPEED,
                (float) speed);
    }

    @Override
    public double bossRefactorTF$getSnowQueenShieldVerticalOffset() {
        return getEntityData().get(BOSS_REFACTOR_TF$SHIELD_VERTICAL_OFFSET);
    }

    @Override
    public void bossRefactorTF$setSnowQueenShieldVerticalOffset(double offset) {
        getEntityData().set(BOSS_REFACTOR_TF$SHIELD_VERTICAL_OFFSET,
                (float) offset);
    }

    @Override
    public boolean bossRefactorTF$isSnowQueenSecondPhase() {
        return getEntityData().get(BOSS_REFACTOR_TF$SECOND_PHASE);
    }

    @Override
    public void bossRefactorTF$setSnowQueenSecondPhase(boolean secondPhase) {
        getEntityData().set(BOSS_REFACTOR_TF$SECOND_PHASE, secondPhase);
    }

    @Override
    public AttackTelegraph bossRefactorTF$getAttackTelegraph() {
        return AttackTelegraphNbt.decode(
                getEntityData().get(BOSS_REFACTOR_TF$TELEGRAPH));
    }

    @Override
    public void bossRefactorTF$setAttackTelegraph(
            AttackTelegraph telegraph) {
        bossRefactorTF$setSnowQueenTelegraph(telegraph);
    }

    @Override
    public void bossRefactorTF$setSnowQueenTelegraph(
            AttackTelegraph telegraph) {
        getEntityData().set(
                BOSS_REFACTOR_TF$TELEGRAPH,
                AttackTelegraphNbt.encode(telegraph));
    }

    @Override
    public boolean bossRefactorTF$isCurrentAttackParryable() {
        return SnowQueenCombat.isCurrentAttackParryable(
                bossRefactorTF$self());
    }

    @Override
    public int bossRefactorTF$getParryFeedbackTicks() {
        return bossRefactorTF$isCurrentAttackParryable()
                ? BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .slamParryFeedbackTicks.get()
                : 0;
    }

    @Override
    public boolean bossRefactorTF$suppressesExternalParryStun() {
        return true;
    }

    @Override
    public int bossRefactorTF$acceptParry(
            @Nullable LivingEntity actor, ResourceLocation sourceId) {
        int feedbackTicks = bossRefactorTF$getParryFeedbackTicks();
        return feedbackTicks > 0
                && SnowQueenCombat.acceptParry(bossRefactorTF$self())
                ? feedbackTicks
                : 0;
    }

    @Unique
    private SnowQueen bossRefactorTF$self() {
        return (SnowQueen) (Object) this;
    }
}