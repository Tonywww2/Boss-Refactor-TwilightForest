package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.lich.LichCombat;
import com.tonywww.bossrefactortf.boss.lich.LichCombatAccess;
import com.tonywww.bossrefactortf.boss.lich.LichCombatGoal;
import com.tonywww.bossrefactortf.boss.lich.LichCombatState;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphAccess;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.data.tags.DamageTypeTagGenerator;
import twilightforest.entity.boss.Lich;
import twilightforest.init.TFSounds;

import javax.annotation.Nullable;

@Mixin(value = Lich.class, remap = false)
public abstract class LichMixin extends Monster
        implements LichCombatAccess, AttackTelegraphAccess,
        TwilightBossParryAccess {
    @Unique
    private static final EntityDataAccessor<CompoundTag>
            BOSS_REFACTOR_TF$PRIMARY_TELEGRAPH = SynchedEntityData.defineId(
                    Lich.class, EntityDataSerializers.COMPOUND_TAG);

    @Unique
    private final LichCombatState bossRefactorTF$combatState =
            new LichCombatState();
        @Unique
        private boolean bossRefactorTF$bypassDamageReduction;

    protected LichMixin(
            EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = {"registerGoals", "m_8099_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$addCombatGoal(CallbackInfo callback) {
        goalSelector.addGoal(0, new LichCombatGoal(bossRefactorTF$self()));
    }

    @Inject(
            method = {"defineSynchedData", "m_8097_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$defineCombatData(CallbackInfo callback) {
        bossRefactorTF$defineCombatDataOnce();
    }

    // 其他模组可能在 defineSynchedData 开头取消整个方法，末尾的注入就不会执行；构造完成后补登记一次。
    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
            at = @At("RETURN"),
            remap = false)
    private void bossRefactorTF$ensureCombatData(CallbackInfo callback) {
        bossRefactorTF$defineCombatDataOnce();
    }

    @Unique
    private void bossRefactorTF$defineCombatDataOnce() {
        SynchedEntityData data = getEntityData();
        if (!data.hasItem(BOSS_REFACTOR_TF$PRIMARY_TELEGRAPH)) {
            data.define(
                    BOSS_REFACTOR_TF$PRIMARY_TELEGRAPH,
                    AttackTelegraphNbt.encode(AttackTelegraph.NONE));
        }
    }

    @Inject(
            method = {"aiStep", "m_8107_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$observeCombatState(CallbackInfo callback) {
        if (level().isClientSide()) {
            return;
        }
        Lich lich = bossRefactorTF$self();
                if (!LichCombat.customCombatEnabled()) {
                        if (LichCombat.hasManagedCloneState(lich)) {
                                LichCombat.stopWhenUnusable(lich);
                        }
                        return;
                }
        LichCombat.observeShieldStrength(lich);
        if (LichCombat.isCustomClone(lich)
                && !LichCombat.requiresExclusiveControl(lich)) {
            LichCombat.tick(lich);
                } else if (!LichCombat.isSecondPhase(lich)
                                && !LichCombat.requiresExclusiveControl(lich)) {
                        LichCombat.tickFirstPhaseBackground(lich);
        }
    }

        @Inject(
                        method = "setShieldStrength",
                        at = @At("TAIL"),
                        remap = false)
        private void bossRefactorTF$observeShieldWrite(
                        int shieldStrength, CallbackInfo callback) {
                if (!level().isClientSide()
                                && bossRefactorTF$combatState != null) {
                        LichCombat.observeShieldStrength(bossRefactorTF$self());
        }
        }

    @Inject(
            method = {"hurt", "m_6469_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$consumeSecondPhaseShield(
            DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> callback) {
        Lich lich = bossRefactorTF$self();
        bossRefactorTF$bypassDamageReduction =
                source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (!LichCombat.isSecondPhase(lich)
                || bossRefactorTF$bypassDamageReduction
                || !source.is(DamageTypeTagGenerator.BREAKS_LICH_SHIELDS)
                || amount <= 2.0F || lich.getShieldStrength() <= 0) {
            return;
        }
        lich.setShieldStrength(lich.getShieldStrength() - 1);
        lich.playSound(
                TFSounds.SHIELD_BREAK.get(),
                1.0F, lich.getVoicePitch() * 2.0F);
        lich.gameEvent(GameEvent.ENTITY_DAMAGE);
        callback.setReturnValue(false);
    }

    @Redirect(
            method = {"hurt", "m_6469_"},
            at = @At(
                    value = "INVOKE",
                    target = "Ltwilightforest/entity/boss/Lich;getShieldStrength()I",
                    ordinal = 0),
            require = 1,
            remap = false)
    private int bossRefactorTF$bypassVanillaShieldImmunity(Lich lich) {
        return LichCombat.isSecondPhase(lich)
                ? 0
                : lich.getShieldStrength();
    }

    @Group(name = "reduceSecondPhaseDamage", min = 1, max = 1)
    @ModifyArg(
            method = {"hurt", "m_6469_"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Monster;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            index = 1,
            require = 0,
            remap = false)
    private float bossRefactorTF$reduceSecondPhaseDamageMcp(float amount) {
        return bossRefactorTF$reduceSecondPhaseDamage(amount);
    }

    @Group(name = "reduceSecondPhaseDamage", min = 1, max = 1)
    @ModifyArg(
            method = {"hurt", "m_6469_"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Monster;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            index = 1,
            require = 0,
            remap = false)
    private float bossRefactorTF$reduceSecondPhaseDamageSrg(float amount) {
        return bossRefactorTF$reduceSecondPhaseDamage(amount);
    }

    @Unique
    private float bossRefactorTF$reduceSecondPhaseDamage(float amount) {
        return LichCombat.isSecondPhase(bossRefactorTF$self())
                                && !bossRefactorTF$bypassDamageReduction
                ? LichCombat.modifyIncomingDamage(
                        bossRefactorTF$self(), amount)
                : amount;
    }

    @Inject(
            method = {"die", "m_6667_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$cleanupAfterDeath(
            DamageSource source, CallbackInfo callback) {
        LichCombat.onRemoved(bossRefactorTF$self());
    }

    @Inject(
            method = {"remove", "m_142687_"},
            at = @At("HEAD"),
            remap = false)
    private void bossRefactorTF$cleanupAfterRemoval(
            Entity.RemovalReason reason, CallbackInfo callback) {
        LichCombat.onRemoved(bossRefactorTF$self());
    }

    @Inject(
            method = {"addAdditionalSaveData", "m_7380_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$saveCombatState(
            CompoundTag tag, CallbackInfo callback) {
        tag.put("BossRefactorTFLichCombat", bossRefactorTF$combatState.save());
    }

    @Inject(
            method = {"readAdditionalSaveData", "m_7378_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$loadCombatState(
            CompoundTag tag, CallbackInfo callback) {
        if (tag.contains("BossRefactorTFLichCombat", Tag.TAG_COMPOUND)) {
            bossRefactorTF$combatState.load(
                    tag.getCompound("BossRefactorTFLichCombat"));
        }
        LichCombat.restoreVisualState(bossRefactorTF$self());
    }

    @Override
    public LichCombatState bossRefactorTF$getLichCombatState() {
        return bossRefactorTF$combatState;
    }

    @Override
    public AttackTelegraph bossRefactorTF$getAttackTelegraph() {
        return AttackTelegraphNbt.decode(
                getEntityData().get(BOSS_REFACTOR_TF$PRIMARY_TELEGRAPH));
    }

    @Override
    public void bossRefactorTF$setAttackTelegraph(
            AttackTelegraph telegraph) {
        bossRefactorTF$setPrimaryTelegraph(telegraph);
    }

    @Override
    public void bossRefactorTF$setPrimaryTelegraph(
            AttackTelegraph telegraph) {
        getEntityData().set(
                BOSS_REFACTOR_TF$PRIMARY_TELEGRAPH,
                AttackTelegraphNbt.encode(telegraph));
    }

    @Override
    public boolean bossRefactorTF$isCurrentAttackParryable() {
        return LichCombat.isCurrentAttackParryable(bossRefactorTF$self());
    }

    @Override
    public int bossRefactorTF$getParryFeedbackTicks() {
        return bossRefactorTF$isCurrentAttackParryable()
                ? LichCombat.parryFeedbackTicks()
                : 0;
    }

    @Override
    public boolean bossRefactorTF$suppressesExternalParryStun() {
        return LichCombat.suppressesExternalParryStun(bossRefactorTF$self());
    }

    @Override
    public int bossRefactorTF$acceptParry(
            @Nullable LivingEntity actor, ResourceLocation sourceId) {
        int feedbackTicks = bossRefactorTF$getParryFeedbackTicks();
        return feedbackTicks > 0
                && LichCombat.acceptParry(bossRefactorTF$self(), actor)
                ? feedbackTicks
                : 0;
    }

    @Unique
    private Lich bossRefactorTF$self() {
        return (Lich) (Object) this;
    }
}