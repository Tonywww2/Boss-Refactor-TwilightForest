package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.alphayeti.AlphaYetiCombat;
import com.tonywww.bossrefactortf.boss.alphayeti.AlphaYetiCombatAccess;
import com.tonywww.bossrefactortf.boss.alphayeti.AlphaYetiCombatGoal;
import com.tonywww.bossrefactortf.boss.alphayeti.AlphaYetiCombatState;
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
import twilightforest.entity.ai.goal.YetiRampageGoal;
import twilightforest.entity.ai.goal.YetiTiredGoal;
import twilightforest.entity.boss.AlphaYeti;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = AlphaYeti.class, remap = false)
public abstract class AlphaYetiMixin extends Monster
        implements AlphaYetiCombatAccess, AttackTelegraphAccess,
        TwilightBossParryAccess {
    @Unique
    private static final EntityDataAccessor<Integer> BOSS_REFACTOR_TF$FATIGUE =
            SynchedEntityData.defineId(AlphaYeti.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> BOSS_REFACTOR_TF$IMBALANCE =
            SynchedEntityData.defineId(AlphaYeti.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Boolean> BOSS_REFACTOR_TF$PURSUING =
            SynchedEntityData.defineId(AlphaYeti.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<CompoundTag> BOSS_REFACTOR_TF$PRIMARY_TELEGRAPH =
            SynchedEntityData.defineId(
                    AlphaYeti.class, EntityDataSerializers.COMPOUND_TAG);
    @Unique
    private static final EntityDataAccessor<CompoundTag> BOSS_REFACTOR_TF$SECONDARY_TELEGRAPH =
            SynchedEntityData.defineId(
                    AlphaYeti.class, EntityDataSerializers.COMPOUND_TAG);

    @Unique
    private final AlphaYetiCombatState bossRefactorTF$combatState =
            new AlphaYetiCombatState();

    protected AlphaYetiMixin(
            EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = {"registerGoals", "m_8099_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$replaceCombatGoals(CallbackInfo callback) {
        if (!BossRefactorTwilightForestConfig.ALPHA_YETI.enabled.get()) {
            return;
        }
        List<Goal> replacedGoals = new ArrayList<>();
        for (WrappedGoal wrappedGoal : goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            if (goal instanceof YetiTiredGoal
                    || goal instanceof YetiRampageGoal
                    || goal.getClass().getName().startsWith(
                            AlphaYeti.class.getName() + "$")) {
                replacedGoals.add(goal);
            }
        }
        replacedGoals.forEach(goalSelector::removeGoal);
        goalSelector.addGoal(1, new AlphaYetiCombatGoal(bossRefactorTF$self()));
    }

    @Inject(
            method = {"defineSynchedData", "m_8097_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$defineCombatData(CallbackInfo callback) {
        getEntityData().define(BOSS_REFACTOR_TF$FATIGUE, 0);
        getEntityData().define(BOSS_REFACTOR_TF$IMBALANCE, 0);
        getEntityData().define(BOSS_REFACTOR_TF$PURSUING, false);
        getEntityData().define(
                BOSS_REFACTOR_TF$PRIMARY_TELEGRAPH,
                AttackTelegraphNbt.encode(AttackTelegraph.NONE));
        getEntityData().define(
                BOSS_REFACTOR_TF$SECONDARY_TELEGRAPH,
                AttackTelegraphNbt.encode(AttackTelegraph.NONE));
    }

    @Inject(
            method = {"aiStep", "m_8107_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$tickIndependentIce(CallbackInfo callback) {
        AlphaYetiCombat.tickIndependentIce(bossRefactorTF$self());
    }

    @Inject(
            method = {"addAdditionalSaveData", "m_7380_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$saveCombatState(
            CompoundTag tag, CallbackInfo callback) {
        tag.put("BossRefactorTFAlphaYetiCombat", bossRefactorTF$combatState.save());
    }

    @Inject(
            method = {"readAdditionalSaveData", "m_7378_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$loadCombatState(
            CompoundTag tag, CallbackInfo callback) {
        if (tag.contains("BossRefactorTFAlphaYetiCombat", Tag.TAG_COMPOUND)) {
            bossRefactorTF$combatState.load(
                    tag.getCompound("BossRefactorTFAlphaYetiCombat"));
        }
        AlphaYetiCombat.restoreVisualState(bossRefactorTF$self());
    }

    @Override
    public AlphaYetiCombatState bossRefactorTF$getAlphaYetiCombatState() {
        return bossRefactorTF$combatState;
    }

    @Override
    public int bossRefactorTF$getVisibleFatigue() {
        return getEntityData().get(BOSS_REFACTOR_TF$FATIGUE);
    }

    @Override
    public void bossRefactorTF$setVisibleFatigue(int fatigue) {
        getEntityData().set(BOSS_REFACTOR_TF$FATIGUE, Math.max(0, fatigue));
    }

    @Override
    public int bossRefactorTF$getVisibleImbalance() {
        return getEntityData().get(BOSS_REFACTOR_TF$IMBALANCE);
    }

    @Override
    public void bossRefactorTF$setVisibleImbalance(int imbalance) {
        getEntityData().set(BOSS_REFACTOR_TF$IMBALANCE, Math.max(0, imbalance));
    }

    @Override
    public boolean bossRefactorTF$isPursuing() {
        return getEntityData().get(BOSS_REFACTOR_TF$PURSUING);
    }

    @Override
    public void bossRefactorTF$setPursuing(boolean pursuing) {
        getEntityData().set(BOSS_REFACTOR_TF$PURSUING, pursuing);
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
        return AlphaYetiCombat.isPursuing(bossRefactorTF$self())
                && BossRefactorTwilightForestConfig.ALPHA_YETI
                        .slashBladeParryEnabled.get();
    }

    @Override
    public int bossRefactorTF$getParryFeedbackTicks() {
        return bossRefactorTF$isCurrentAttackParryable()
                ? BossRefactorTwilightForestConfig.ALPHA_YETI
                        .parryFeedbackTicks.get()
                : 0;
    }

    @Override
    public int bossRefactorTF$acceptParry(
            @Nullable LivingEntity actor, ResourceLocation sourceId) {
        int feedbackTicks = bossRefactorTF$getParryFeedbackTicks();
        return feedbackTicks > 0
                && AlphaYetiCombat.acceptParry(bossRefactorTF$self())
                ? feedbackTicks
                : 0;
    }

    @Unique
    private AlphaYeti bossRefactorTF$self() {
        return (AlphaYeti) (Object) this;
    }
}