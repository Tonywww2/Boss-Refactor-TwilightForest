package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.alphayeti.AlphaYetiCombat;
import com.tonywww.bossrefactortf.boss.alphayeti.AlphaYetiFallingIceAccess;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.projectile.FallingIce;

@Mixin(value = FallingIce.class, remap = false)
public abstract class FallingIceMixin extends Entity
    implements AlphaYetiFallingIceAccess {
    @Unique
    private static final EntityDataAccessor<Boolean> BOSS_REFACTOR_TF$VISUAL_ONLY =
        SynchedEntityData.defineId(
            FallingIce.class, EntityDataSerializers.BOOLEAN);

    protected FallingIceMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
        method = {"defineSynchedData", "m_8097_"},
        at = @At("TAIL"),
        remap = false)
    private void bossRefactorTF$defineVisualOnlyData(CallbackInfo callback) {
    getEntityData().define(BOSS_REFACTOR_TF$VISUAL_ONLY, false);
    }

    @Inject(
        method = {"tick", "m_8119_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$tickVisualOnly(CallbackInfo callback) {
    if (!getEntityData().get(BOSS_REFACTOR_TF$VISUAL_ONLY)) {
        return;
    }
    super.tick();
    if (!isNoGravity()) {
        setDeltaMovement(getDeltaMovement().add(0.0, -0.04, 0.0));
    }
    move(MoverType.SELF, getDeltaMovement());
    if (level().isClientSide()) {
        level().addParticle(
            new BlockParticleOption(
                ParticleTypes.FALLING_DUST,
                Blocks.PACKED_ICE.defaultBlockState()),
            getX(), getY() + 0.5, getZ(),
            0.0, -0.25, 0.0);
    } else if (onGround() || verticalCollision) {
            level().levelEvent(
                2001, blockPosition(),
                Block.getId(Blocks.PACKED_ICE.defaultBlockState()));
        discard();
        callback.cancel();
        return;
    } else if (tickCount > 2400
        || getY() < level().getMinBuildHeight() - 64) {
        discard();
        }
    setDeltaMovement(getDeltaMovement().scale(0.98));
    callback.cancel();
    }

    @Inject(
        method = {"addAdditionalSaveData", "m_7380_"},
        at = @At("TAIL"),
        remap = false)
    private void bossRefactorTF$saveVisualOnly(
        CompoundTag tag, CallbackInfo callback) {
    tag.putBoolean(
        AlphaYetiCombat.VISUAL_ONLY_FALLING_ICE_TAG,
        getEntityData().get(BOSS_REFACTOR_TF$VISUAL_ONLY));
    }

    @Inject(
        method = {"readAdditionalSaveData", "m_7378_"},
        at = @At("TAIL"),
        remap = false)
    private void bossRefactorTF$loadVisualOnly(
        CompoundTag tag, CallbackInfo callback) {
    getEntityData().set(
        BOSS_REFACTOR_TF$VISUAL_ONLY,
        tag.getBoolean(AlphaYetiCombat.VISUAL_ONLY_FALLING_ICE_TAG));
    }

    @Override
    public void bossRefactorTF$markVisualOnly() {
    getEntityData().set(BOSS_REFACTOR_TF$VISUAL_ONLY, true);
    }
}