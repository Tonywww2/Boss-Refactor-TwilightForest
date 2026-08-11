package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.urghast.UrGhastCombat;
import com.tonywww.bossrefactortf.boss.urghast.UrGhastCombatAccess;
import com.tonywww.bossrefactortf.boss.urghast.UrGhastCombatGoal;
import com.tonywww.bossrefactortf.boss.urghast.UrGhastCombatRules;
import com.tonywww.bossrefactortf.boss.urghast.UrGhastCombatState;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.ai.goal.GhastguardAttackGoal;
import twilightforest.entity.ai.goal.UrGhastFlightGoal;
import twilightforest.entity.boss.UrGhast;
import twilightforest.entity.monster.CarminiteGhastguard;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = UrGhast.class, remap = false)
public abstract class UrGhastMixin extends CarminiteGhastguard
        implements UrGhastCombatAccess, AttackTelegraphAccess {
    @Unique
    private static final EntityDataAccessor<CompoundTag> BOSS_REFACTOR_TF$MIST_ORIGIN =
            SynchedEntityData.defineId(
                    UrGhast.class, EntityDataSerializers.COMPOUND_TAG);
    @Unique
    private static final EntityDataAccessor<CompoundTag> BOSS_REFACTOR_TF$MIST_DESTINATION =
            SynchedEntityData.defineId(
                    UrGhast.class, EntityDataSerializers.COMPOUND_TAG);

    @Shadow
    private float damageUntilNextPhase;

    @Unique
    private final UrGhastCombatState bossRefactorTF$combatState =
            new UrGhastCombatState();
    @Unique
    private float bossRefactorTF$healthBeforeDamage;

    protected UrGhastMixin(
            EntityType<? extends CarminiteGhastguard> entityType, Level level) {
        super(entityType, level);
    }

        @Inject(
            method = "registerAttributes",
            at = @At("RETURN"),
            remap = false)
        private static void bossRefactorTF$addAttackDamageAttribute(
            CallbackInfoReturnable<AttributeSupplier.Builder> callback) {
        callback.getReturnValue().add(
            Attributes.ATTACK_DAMAGE,
            BossRefactorTwilightForestConfig.UR_GHAST
                .baseAttackDamage.getDefault());
        }

    @Inject(
            method = {"registerGoals", "m_8099_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$replaceCombatGoals(CallbackInfo callback) {
        if (!BossRefactorTwilightForestConfig.UR_GHAST.enabled.get()) {
            return;
        }
        List<Goal> replacedGoals = new ArrayList<>();
        for (WrappedGoal wrappedGoal : goalSelector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            if (goal instanceof UrGhastFlightGoal
                    || goal instanceof GhastguardAttackGoal) {
                replacedGoals.add(goal);
            }
        }
        replacedGoals.forEach(goalSelector::removeGoal);
        goalSelector.addGoal(5, new UrGhastCombatGoal(bossRefactorTF$self()));
    }

    @Inject(
            method = {"defineSynchedData", "m_8097_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$defineCombatData(CallbackInfo callback) {
        getEntityData().define(
                BOSS_REFACTOR_TF$MIST_ORIGIN,
                AttackTelegraphNbt.encode(AttackTelegraph.NONE));
        getEntityData().define(
                BOSS_REFACTOR_TF$MIST_DESTINATION,
                AttackTelegraphNbt.encode(AttackTelegraph.NONE));
    }

    @Inject(
            method = {"aiStep", "m_8107_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$enhanceTantrumVisuals(CallbackInfo callback) {
        UrGhastCombat.tickTantrumVisuals(bossRefactorTF$self());
    }

    @Inject(
            method = {"hurt", "m_6469_"},
            at = @At("HEAD"),
            remap = false)
    private void bossRefactorTF$disableVanillaDamagePhaseSwitch(
            DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> callback) {
        bossRefactorTF$healthBeforeDamage = getHealth();
        if (BossRefactorTwilightForestConfig.UR_GHAST.enabled.get()) {
            damageUntilNextPhase = Float.POSITIVE_INFINITY;
        }
    }

    @Inject(
            method = {"hurt", "m_6469_"},
            at = @At("RETURN"),
            remap = false)
    private void bossRefactorTF$summonMinionsByHealthPercentage(
            DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> callback) {
        if (!BossRefactorTwilightForestConfig.UR_GHAST.enabled.get()
                || level().isClientSide()
                || isDeadOrDying()
                || !Boolean.TRUE.equals(callback.getReturnValue())) {
            return;
        }
        if (UrGhastCombatRules.crossesMinionSummonThreshold(
                bossRefactorTF$healthBeforeDamage, getHealth(), getMaxHealth(),
                BossRefactorTwilightForestConfig.UR_GHAST
                        .minionSummonHealthIntervalPercent.get())) {
            bossRefactorTF$self().spawnGhastsAtTraps();
        }
    }

    @ModifyConstant(
            method = {"hurt", "m_6469_"},
            constant = @Constant(floatValue = 10.0F),
            remap = false)
    private float bossRefactorTF$replaceTantrumDamageDivisor(float original) {
        if (!BossRefactorTwilightForestConfig.UR_GHAST.enabled.get()) {
            return original;
        }
        double reduction = BossRefactorTwilightForestConfig.UR_GHAST
                .tantrumDamageReduction.get();
        return reduction >= 1.0
                ? Float.POSITIVE_INFINITY
                : (float) (1.0 / (1.0 - reduction));
    }

    @ModifyConstant(
            method = "spitFireball",
            constant = @Constant(intValue = 2),
            require = 1,
            remap = false)
    private int bossRefactorTF$limitExtraFireballs(int original) {
        if (!BossRefactorTwilightForestConfig.UR_GHAST.enabled.get()) {
            return original;
        }
        return UrGhastCombatRules.giantFireballExtraProjectiles(
                bossRefactorTF$self().isInTantrum());
    }

    @Inject(
            method = "resetDamageUntilNextPhase",
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$keepVanillaDamagePhaseDisabled(
            CallbackInfo callback) {
        if (BossRefactorTwilightForestConfig.UR_GHAST.enabled.get()) {
            damageUntilNextPhase = Float.POSITIVE_INFINITY;
        }
    }

    @Inject(
            method = {"getHurtSound", "m_7975_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$applyHurtSoundCooldown(
            DamageSource source,
            CallbackInfoReturnable<SoundEvent> callback) {
        if (!UrGhastCombat.allowHurtSound(bossRefactorTF$self())) {
            callback.setReturnValue(null);
        }
    }

        @Group(name = "preserveOffensiveTarget", min = 1, max = 1)
        @Redirect(
            method = {"customServerAiStep", "m_8024_"},
            at = @At(
                    value = "INVOKE",
                    target = "Ltwilightforest/entity/boss/UrGhast;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"),
            require = 0,
            remap = false)
        private void bossRefactorTF$preserveOffensiveTargetMcp(
            UrGhast ghast, @Nullable LivingEntity target) {
        bossRefactorTF$preserveOffensiveTarget(ghast, target);
        }

        @Group(name = "preserveOffensiveTarget", min = 1, max = 1)
        @Redirect(
            method = {"customServerAiStep", "m_8024_"},
            at = @At(
                value = "INVOKE",
                target = "Ltwilightforest/entity/boss/UrGhast;m_6710_(Lnet/minecraft/world/entity/LivingEntity;)V"),
            require = 0,
            remap = false)
        private void bossRefactorTF$preserveOffensiveTargetSrg(
            UrGhast ghast, @Nullable LivingEntity target) {
        bossRefactorTF$preserveOffensiveTarget(ghast, target);
        }

        @Unique
        private void bossRefactorTF$preserveOffensiveTarget(
            UrGhast ghast, @Nullable LivingEntity target) {
        if (!UrGhastCombat.isOffensive(ghast) || target != null) {
            ghast.setTarget(target);
        }
    }

    @Inject(
            method = {"addAdditionalSaveData", "m_7380_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$saveCombatState(
            CompoundTag tag, CallbackInfo callback) {
        tag.put("BossRefactorTFUrGhastCombat", bossRefactorTF$combatState.save());
    }

    @Inject(
            method = {"readAdditionalSaveData", "m_7378_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$loadCombatState(
            CompoundTag tag, CallbackInfo callback) {
        if (tag.contains("BossRefactorTFUrGhastCombat", Tag.TAG_COMPOUND)) {
            bossRefactorTF$combatState.load(
                    tag.getCompound("BossRefactorTFUrGhastCombat"));
        }
        if (BossRefactorTwilightForestConfig.UR_GHAST.enabled.get()) {
            damageUntilNextPhase = Float.POSITIVE_INFINITY;
        }
        UrGhastCombat.restoreVisualState(bossRefactorTF$self());
    }

    @Override
    public UrGhastCombatState bossRefactorTF$getUrGhastCombatState() {
        return bossRefactorTF$combatState;
    }

    @Override
    public void bossRefactorTF$setMistOriginTelegraph(
            AttackTelegraph telegraph) {
        getEntityData().set(
                BOSS_REFACTOR_TF$MIST_ORIGIN,
                AttackTelegraphNbt.encode(telegraph));
    }

    @Override
    public void bossRefactorTF$setMistDestinationTelegraph(
            AttackTelegraph telegraph) {
        getEntityData().set(
                BOSS_REFACTOR_TF$MIST_DESTINATION,
                AttackTelegraphNbt.encode(telegraph));
    }

    @Override
    public AttackTelegraph bossRefactorTF$getAttackTelegraph() {
        AttackTelegraph origin = AttackTelegraphNbt.decode(
                getEntityData().get(BOSS_REFACTOR_TF$MIST_ORIGIN));
        if (origin.shape() != AttackTelegraphShape.NONE) {
            return origin;
        }
        return AttackTelegraphNbt.decode(
                getEntityData().get(BOSS_REFACTOR_TF$MIST_DESTINATION));
    }

    @Override
    public List<AttackTelegraph> bossRefactorTF$getAttackTelegraphs() {
        List<AttackTelegraph> telegraphs = new ArrayList<>(2);
        AttackTelegraph origin = AttackTelegraphNbt.decode(
                getEntityData().get(BOSS_REFACTOR_TF$MIST_ORIGIN));
        AttackTelegraph destination = AttackTelegraphNbt.decode(
                getEntityData().get(BOSS_REFACTOR_TF$MIST_DESTINATION));
        if (origin.shape() != AttackTelegraphShape.NONE) {
            telegraphs.add(origin);
        }
        if (destination.shape() != AttackTelegraphShape.NONE) {
            telegraphs.add(destination);
        }
        return telegraphs;
    }

    @Override
    public void bossRefactorTF$setAttackTelegraph(AttackTelegraph telegraph) {
        bossRefactorTF$setMistOriginTelegraph(telegraph);
    }

    @Unique
    private UrGhast bossRefactorTF$self() {
        return (UrGhast) (Object) this;
    }
}