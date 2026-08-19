package com.tonywww.bossrefactortf.boss.hydra;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import twilightforest.entity.boss.Hydra;
import twilightforest.entity.boss.HydraPart;

public final class HydraEvilFireball extends Projectile
        implements ItemSupplier {
    private static final EntityDataAccessor<Float> DIRECTION_X =
            SynchedEntityData.defineId(
                    HydraEvilFireball.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIRECTION_Y =
            SynchedEntityData.defineId(
                    HydraEvilFireball.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIRECTION_Z =
            SynchedEntityData.defineId(
                    HydraEvilFireball.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> PHASE_TWO =
            SynchedEntityData.defineId(
                    HydraEvilFireball.class, EntityDataSerializers.BOOLEAN);

    private int age;

    public HydraEvilFireball(
            EntityType<? extends HydraEvilFireball> entityType,
            Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        getEntityData().define(DIRECTION_X, 0.0F);
        getEntityData().define(DIRECTION_Y, 0.0F);
        getEntityData().define(DIRECTION_Z, 1.0F);
        getEntityData().define(PHASE_TWO, false);
    }

    public void initialize(
            Hydra owner, Vec3 origin, Vec3 direction,
            boolean phaseTwo) {
        setOwner(owner);
        setPos(origin);
        setLockedDirection(direction);
        getEntityData().set(PHASE_TWO, phaseTwo);
        updateLockedVelocity();
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && age >= BossRefactorTwilightForestConfig
                .HYDRA.evilFireballLifetimeTicks.get()) {
            discard();
            return;
        }
        updateLockedVelocity();
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(
                this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            onHit(hitResult);
        }
        if (isRemoved()) {
            return;
        }
        setPos(position().add(getDeltaMovement()));
        age++;
        if (level().isClientSide()) {
            level().addParticle(
                    ParticleTypes.FLAME,
                    getX(), getY() + getBbHeight() * 0.5, getZ(),
                    0.0, 0.0, 0.0);
            return;
        }
        int auraInterval = BossRefactorTwilightForestConfig.HYDRA
            .evilFireballAuraIntervalTicks.get();
        auraInterval = Math.max(20, auraInterval);
        if (age % auraInterval == 0 && getOwner() instanceof Hydra hydra) {
            HydraCombat.damageEvilFireballAura(this, hydra);
        }
        if (age >= BossRefactorTwilightForestConfig.HYDRA
                .evilFireballLifetimeTicks.get()) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (level().isClientSide()) {
            return;
        }
        if (getOwner() instanceof Hydra hydra && !hydra.isRemoved()) {
            HydraCombat.explodeEvilFireball(
                    this, hydra, hitResult.getLocation(), isPhaseTwo());
        } else {
            discard();
        }
    }

    @Override
    protected boolean canHitEntity(net.minecraft.world.entity.Entity target) {
        return target != getOwner()
            && !(target instanceof HydraPart part
                && part.getParent() == getOwner())
            && super.canHitEntity(target);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Age", age);
        tag.putFloat("DirectionX", getEntityData().get(DIRECTION_X));
        tag.putFloat("DirectionY", getEntityData().get(DIRECTION_Y));
        tag.putFloat("DirectionZ", getEntityData().get(DIRECTION_Z));
        tag.putBoolean("PhaseTwo", isPhaseTwo());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        age = Math.max(0, tag.getInt("Age"));
        setLockedDirection(new Vec3(
            tag.getFloat("DirectionX"),
            tag.getFloat("DirectionY"),
            tag.getFloat("DirectionZ")));
        getEntityData().set(PHASE_TWO, tag.getBoolean("PhaseTwo"));
        updateLockedVelocity();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }

    private boolean isPhaseTwo() {
        return getEntityData().get(PHASE_TWO);
    }

    private void setLockedDirection(Vec3 direction) {
        if (!Double.isFinite(direction.x)
                || !Double.isFinite(direction.y)
                || !Double.isFinite(direction.z)
                || direction.lengthSqr() < 1.0E-6) {
            direction = new Vec3(0.0, 0.0, 1.0);
        }
        Vec3 normalized = direction.normalize();
        getEntityData().set(DIRECTION_X, (float) normalized.x);
        getEntityData().set(DIRECTION_Y, (float) normalized.y);
        getEntityData().set(DIRECTION_Z, (float) normalized.z);
    }

    private void updateLockedVelocity() {
        double speed = BossRefactorTwilightForestConfig.HYDRA
                .evilFireballSpeed.get();
        setDeltaMovement(
                getEntityData().get(DIRECTION_X) * speed,
                getEntityData().get(DIRECTION_Y) * speed,
                getEntityData().get(DIRECTION_Z) * speed);
    }
}