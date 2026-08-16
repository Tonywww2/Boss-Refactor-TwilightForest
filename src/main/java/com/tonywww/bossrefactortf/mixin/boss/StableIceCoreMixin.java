package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenCombat;
import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenSummonAccess;
import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenSummonNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import twilightforest.entity.monster.BaseIceMob;
import twilightforest.entity.monster.StableIceCore;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(value = StableIceCore.class, remap = false)
public abstract class StableIceCoreMixin extends BaseIceMob
        implements SnowQueenSummonAccess {
    @Unique
    private static final EntityDataAccessor<Integer> BOSS_REFACTOR_TF$OWNER_ID =
            SynchedEntityData.defineId(
                    StableIceCore.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Boolean> BOSS_REFACTOR_TF$LINKED =
            SynchedEntityData.defineId(
                    StableIceCore.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Integer> BOSS_REFACTOR_TF$ROLE =
            SynchedEntityData.defineId(
                    StableIceCore.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> BOSS_REFACTOR_TF$STATE =
            SynchedEntityData.defineId(
                    StableIceCore.class, EntityDataSerializers.INT);

    @Unique private UUID bossRefactorTF$ownerUuid;
    @Unique private int bossRefactorTF$summonTicks;
    @Unique private int bossRefactorTF$summonDuration;
    @Unique private double bossRefactorTF$dashX;
    @Unique private double bossRefactorTF$dashY;
    @Unique private double bossRefactorTF$dashZ;
    @Unique private boolean bossRefactorTF$reportedDeath;

    protected StableIceCoreMixin(
            EntityType<? extends BaseIceMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(BOSS_REFACTOR_TF$OWNER_ID, 0);
        getEntityData().define(BOSS_REFACTOR_TF$LINKED, false);
        getEntityData().define(BOSS_REFACTOR_TF$ROLE, ROLE_NONE);
        getEntityData().define(BOSS_REFACTOR_TF$STATE, STATE_IDLE);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        SnowQueenCombat.tickSummon(bossRefactorTF$self());
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason.shouldDestroy()) {
            SnowQueenCombat.onSummonDeath(bossRefactorTF$self());
        }
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("BossRefactorTFSnowQueenSummon",
                SnowQueenSummonNbt.save(this));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BossRefactorTFSnowQueenSummon", Tag.TAG_COMPOUND)) {
            SnowQueenSummonNbt.load(
                    tag.getCompound("BossRefactorTFSnowQueenSummon"), this);
        }
    }

    @Override public @Nullable UUID bossRefactorTF$getSnowQueenOwnerUuid() { return bossRefactorTF$ownerUuid; }
    @Override public void bossRefactorTF$setSnowQueenOwnerUuid(@Nullable UUID ownerUuid) { bossRefactorTF$ownerUuid = ownerUuid; }
    @Override public int bossRefactorTF$getSnowQueenOwnerEntityId() { return getEntityData().get(BOSS_REFACTOR_TF$OWNER_ID); }
    @Override public void bossRefactorTF$setSnowQueenOwnerEntityId(int ownerEntityId) { getEntityData().set(BOSS_REFACTOR_TF$OWNER_ID, ownerEntityId); }
    @Override public boolean bossRefactorTF$isSnowQueenLinked() { return getEntityData().get(BOSS_REFACTOR_TF$LINKED); }
    @Override public void bossRefactorTF$setSnowQueenLinked(boolean linked) { getEntityData().set(BOSS_REFACTOR_TF$LINKED, linked); }
    @Override public int bossRefactorTF$getSnowQueenSummonRole() { return getEntityData().get(BOSS_REFACTOR_TF$ROLE); }
    @Override public void bossRefactorTF$setSnowQueenSummonRole(int role) { getEntityData().set(BOSS_REFACTOR_TF$ROLE, role); }
    @Override public int bossRefactorTF$getSnowQueenSummonState() { return getEntityData().get(BOSS_REFACTOR_TF$STATE); }
    @Override public void bossRefactorTF$setSnowQueenSummonState(int state) { getEntityData().set(BOSS_REFACTOR_TF$STATE, state); }
    @Override public int bossRefactorTF$getSnowQueenSummonTicks() { return bossRefactorTF$summonTicks; }
    @Override public void bossRefactorTF$setSnowQueenSummonTicks(int ticks) { bossRefactorTF$summonTicks = Math.max(0, ticks); }
    @Override public int bossRefactorTF$getSnowQueenSummonDuration() { return bossRefactorTF$summonDuration; }
    @Override public void bossRefactorTF$setSnowQueenSummonDuration(int duration) { bossRefactorTF$summonDuration = Math.max(0, duration); }
    @Override public double bossRefactorTF$getSnowQueenDashX() { return bossRefactorTF$dashX; }
    @Override public double bossRefactorTF$getSnowQueenDashY() { return bossRefactorTF$dashY; }
    @Override public double bossRefactorTF$getSnowQueenDashZ() { return bossRefactorTF$dashZ; }
    @Override public void bossRefactorTF$setSnowQueenDash(double x, double y, double z) { bossRefactorTF$dashX = x; bossRefactorTF$dashY = y; bossRefactorTF$dashZ = z; }
    @Override public boolean bossRefactorTF$hasReportedSnowQueenDeath() { return bossRefactorTF$reportedDeath; }
    @Override public void bossRefactorTF$setReportedSnowQueenDeath(boolean reported) { bossRefactorTF$reportedDeath = reported; }

    @Unique
    private StableIceCore bossRefactorTF$self() {
        return (StableIceCore) (Object) this;
    }
}