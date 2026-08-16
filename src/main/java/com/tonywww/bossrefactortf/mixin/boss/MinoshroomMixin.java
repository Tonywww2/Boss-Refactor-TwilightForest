package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.minoshroom.MinoshroomCombat;
import com.tonywww.bossrefactortf.boss.minoshroom.MinoshroomCombatAccess;
import com.tonywww.bossrefactortf.boss.minoshroom.MinoshroomCombatGoal;
import com.tonywww.bossrefactortf.boss.minoshroom.MinoshroomCombatState;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.boss.Minoshroom;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = Minoshroom.class, remap = false)
public abstract class MinoshroomMixin extends Monster
        implements MinoshroomCombatAccess, AttackTelegraphAccess,
        TwilightBossParryAccess {
    @Unique
    private static final EntityDataAccessor<Boolean> BOSS_REFACTOR_TF$SECOND_PHASE =
            SynchedEntityData.defineId(
                    Minoshroom.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<CompoundTag> BOSS_REFACTOR_TF$TELEGRAPH =
            SynchedEntityData.defineId(
                    Minoshroom.class, EntityDataSerializers.COMPOUND_TAG);

    @Unique
    private final MinoshroomCombatState bossRefactorTF$combatState =
            new MinoshroomCombatState();

    protected MinoshroomMixin(
            EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = {"registerGoals", "m_8099_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$replaceCombatGoals(CallbackInfo callback) {
        if (!BossRefactorTwilightForestConfig.MINOSHROOM.enabled.get()) {
            return;
        }
        String minotaurInnerPrefix =
                Minoshroom.class.getSuperclass().getName() + "$";
        List<Goal> replacedGoals = new ArrayList<>();
        for (WrappedGoal wrappedGoal : goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            String className = goal.getClass().getName();
            String simpleName = goal.getClass().getSimpleName();
            if (simpleName.equals("GroundAttackGoal")
                    || simpleName.equals("ChargeAttackGoal")
                    || className.startsWith(minotaurInnerPrefix)) {
                replacedGoals.add(goal);
            }
        }
        replacedGoals.forEach(goalSelector::removeGoal);
        goalSelector.addGoal(0,
                new MinoshroomCombatGoal(bossRefactorTF$self()));
    }

    @Inject(
            method = {"defineSynchedData", "m_8097_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$defineCombatData(CallbackInfo callback) {
        getEntityData().define(BOSS_REFACTOR_TF$SECOND_PHASE, false);
        getEntityData().define(
                BOSS_REFACTOR_TF$TELEGRAPH,
                AttackTelegraphNbt.encode(AttackTelegraph.NONE));
    }

    @Inject(
            method = {"tick", "m_8119_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$tickBaseTraits(CallbackInfo callback) {
        MinoshroomCombat.tickBaseTraits(bossRefactorTF$self());
    }

    @Inject(
            method = {"addAdditionalSaveData", "m_7380_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$saveCombatState(
            CompoundTag tag, CallbackInfo callback) {
        tag.put("BossRefactorTFMinoshroomCombat",
                bossRefactorTF$combatState.save());
    }

    @Inject(
            method = {"readAdditionalSaveData", "m_7378_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$loadCombatState(
            CompoundTag tag, CallbackInfo callback) {
        if (tag.contains("BossRefactorTFMinoshroomCombat", Tag.TAG_COMPOUND)) {
            bossRefactorTF$combatState.load(
                    tag.getCompound("BossRefactorTFMinoshroomCombat"));
        }
        MinoshroomCombat.restoreVisualState(bossRefactorTF$self());
    }

    @Override
    public MinoshroomCombatState bossRefactorTF$getMinoshroomCombatState() {
        return bossRefactorTF$combatState;
    }

    @Override
    public boolean bossRefactorTF$isSecondPhase() {
        return getEntityData().get(BOSS_REFACTOR_TF$SECOND_PHASE);
    }

    @Override
    public void bossRefactorTF$setSecondPhase(boolean secondPhase) {
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
        bossRefactorTF$setMinoshroomTelegraph(telegraph);
    }

    @Override
    public void bossRefactorTF$setMinoshroomTelegraph(
            AttackTelegraph telegraph) {
        getEntityData().set(
                BOSS_REFACTOR_TF$TELEGRAPH,
                AttackTelegraphNbt.encode(telegraph));
    }

    @Override
    public boolean bossRefactorTF$isCurrentAttackParryable() {
        return MinoshroomCombat.isCurrentAttackParryable(
                bossRefactorTF$self());
    }

    @Override
    public int bossRefactorTF$getParryFeedbackTicks() {
        return bossRefactorTF$isCurrentAttackParryable()
                ? BossRefactorTwilightForestConfig.MINOSHROOM
                        .sweepParryStunTicks.get()
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
                && MinoshroomCombat.acceptParry(bossRefactorTF$self())
                ? feedbackTicks
                : 0;
    }

    @Unique
    private Minoshroom bossRefactorTF$self() {
        return (Minoshroom) (Object) this;
    }
}