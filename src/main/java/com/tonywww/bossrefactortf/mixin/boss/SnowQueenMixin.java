package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenCombat;
import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenCombatAccess;
import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenCombatRules;
import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenCombatState;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
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
        private static final EntityDataAccessor<Float> BOSS_REFACTOR_TF$SHIELD_WAVE_AMPLITUDE =
            SynchedEntityData.defineId(
                SnowQueen.class, EntityDataSerializers.FLOAT);
        @Unique
        private static final EntityDataAccessor<Float> BOSS_REFACTOR_TF$SHIELD_WAVE_SPEED =
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
        private static final EntityDataAccessor<CompoundTag> BOSS_REFACTOR_TF$DROP_TELEGRAPH =
            SynchedEntityData.defineId(
                SnowQueen.class, EntityDataSerializers.COMPOUND_TAG);

    @Unique
    private final SnowQueenCombatState bossRefactorTF$combatState =
            new SnowQueenCombatState();

    protected SnowQueenMixin(
            EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
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
        getEntityData().define(BOSS_REFACTOR_TF$SHIELD_WAVE_AMPLITUDE, 0.4F);
        getEntityData().define(BOSS_REFACTOR_TF$SHIELD_WAVE_SPEED, 6.0F);
        getEntityData().define(BOSS_REFACTOR_TF$SECOND_PHASE, false);
        getEntityData().define(
                BOSS_REFACTOR_TF$TELEGRAPH,
                AttackTelegraphNbt.encode(AttackTelegraph.NONE));
        getEntityData().define(
            BOSS_REFACTOR_TF$DROP_TELEGRAPH,
            AttackTelegraphNbt.encode(AttackTelegraph.NONE));
    }

    @Inject(
            method = {"tick", "m_8119_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$tickBaseTraits(CallbackInfo callback) {
        SnowQueenCombat.tickBaseTraits(bossRefactorTF$self());
    }

    @Inject(
            method = {"customServerAiStep", "m_8024_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$tickCombatSidecar(CallbackInfo callback) {
        SnowQueenCombat.tickSidecar(bossRefactorTF$self());
    }

    @Inject(
            method = "destroyBlocksInAABB",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$preserveArenaBlocks(
            AABB bounds, CallbackInfo callback) {
        if (SnowQueenCombat.customCombatActive(bossRefactorTF$self())) {
            callback.cancel();
        }
    }

    @Inject(
            method = {"doHurtTarget", "m_7327_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$scaleDropContactDamage(
            Entity target, CallbackInfoReturnable<Boolean> callback) {
        SnowQueen queen = bossRefactorTF$self();
        if (SnowQueenCombat.customCombatActive(queen)
                && queen.getCurrentPhase() == SnowQueen.Phase.DROP) {
            callback.setReturnValue(
                    SnowQueenCombat.attackOriginalDropContact(queen, target));
        }
    }

    @ModifyConstant(
            method = "setCurrentPhase",
            constant = @Constant(intValue = 3),
            require = 1,
            remap = false)
    private int bossRefactorTF$limitDropCount(int originalBound) {
        if (!SnowQueenCombat.customCombatActive(bossRefactorTF$self())) {
            return originalBound;
        }
        return SnowQueenCombatRules.configuredDropRandomBound(
                BossRefactorTwilightForestConfig.SNOW_QUEEN
                        .dropMaximumSuccessfulDrops.get());
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
    public double bossRefactorTF$getSnowQueenShieldWaveAmplitude() {
        return getEntityData().get(BOSS_REFACTOR_TF$SHIELD_WAVE_AMPLITUDE);
    }

    @Override
    public void bossRefactorTF$setSnowQueenShieldWaveAmplitude(
            double amplitude) {
        getEntityData().set(
                BOSS_REFACTOR_TF$SHIELD_WAVE_AMPLITUDE, (float) amplitude);
    }

    @Override
    public double bossRefactorTF$getSnowQueenShieldWaveSpeed() {
        return getEntityData().get(BOSS_REFACTOR_TF$SHIELD_WAVE_SPEED);
    }

    @Override
    public void bossRefactorTF$setSnowQueenShieldWaveSpeed(double speed) {
        getEntityData().set(BOSS_REFACTOR_TF$SHIELD_WAVE_SPEED, (float) speed);
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
    public List<AttackTelegraph> bossRefactorTF$getAttackTelegraphs() {
        List<AttackTelegraph> telegraphs = new ArrayList<>(2);
        AttackTelegraph sidecar = AttackTelegraphNbt.decode(
                getEntityData().get(BOSS_REFACTOR_TF$TELEGRAPH));
        AttackTelegraph drop = AttackTelegraphNbt.decode(
                getEntityData().get(BOSS_REFACTOR_TF$DROP_TELEGRAPH));
        if (sidecar.shape() != AttackTelegraphShape.NONE) {
            telegraphs.add(sidecar);
        }
        if (drop.shape() != AttackTelegraphShape.NONE) {
            telegraphs.add(drop);
        }
        return telegraphs;
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
    public void bossRefactorTF$setSnowQueenDropTelegraph(
            AttackTelegraph telegraph) {
        getEntityData().set(
                BOSS_REFACTOR_TF$DROP_TELEGRAPH,
                AttackTelegraphNbt.encode(telegraph));
    }

    @Override
    public boolean bossRefactorTF$isCurrentAttackParryable() {
        return SnowQueenCombat.isCurrentAttackParryable(
                bossRefactorTF$self());
    }

    @Override
    public boolean bossRefactorTF$isCurrentAttackParryable(
            @Nullable LivingEntity actor) {
        return SnowQueenCombat.isCurrentAttackParryable(
                bossRefactorTF$self(), actor);
    }

    @Override
    public int bossRefactorTF$getParryFeedbackTicks() {
        return bossRefactorTF$isCurrentAttackParryable()
                ? BossRefactorTwilightForestConfig.SNOW_QUEEN
                .iceShieldParryFeedbackTicks.get()
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
            && SnowQueenCombat.acceptParry(
                bossRefactorTF$self(), actor, sourceId)
                ? feedbackTicks
                : 0;
    }

    @Unique
    private SnowQueen bossRefactorTF$self() {
        return (SnowQueen) (Object) this;
    }
}