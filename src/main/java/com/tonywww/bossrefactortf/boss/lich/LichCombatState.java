package com.tonywww.bossrefactortf.boss.lich;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.UUID;

public final class LichCombatState {
    LichActionState actionState = LichActionState.IDLE;
    int stateTicks;
    int stateDuration;
    int shieldBashCooldown;
    int summonCooldown;
    int meleeCooldown;
    int cloneLifetimeTicks;
    boolean secondPhase;
    boolean shieldTracked;
    int shieldLayers;
    boolean shieldBashEmpowered;
    boolean shieldBashParried;
    boolean customClone;
    double originX;
    double originY;
    double originZ;
    double directionX;
    double directionZ;
    long lastParryGameTime = Long.MIN_VALUE;
    long lastMeleeMagicGameTime = Long.MIN_VALUE;
    @Nullable
    UUID customCloneId;
    @Nullable
    UUID masterId;
    @Nullable
    UUID parryingEntityId;
    @Nullable
    UUID lastMeleeMagicTargetId;

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ActionState", actionState.ordinal());
        tag.putInt("StateTicks", stateTicks);
        tag.putInt("StateDuration", stateDuration);
        tag.putInt("ShieldBashCooldown", shieldBashCooldown);
        tag.putInt("SummonCooldown", summonCooldown);
        tag.putInt("MeleeCooldown", meleeCooldown);
        tag.putInt("CloneLifetimeTicks", cloneLifetimeTicks);
        tag.putBoolean("SecondPhase", secondPhase);
        tag.putBoolean("ShieldTracked", shieldTracked);
        tag.putInt("ShieldLayers", shieldLayers);
        tag.putBoolean("ShieldBashEmpowered", shieldBashEmpowered);
        tag.putBoolean("ShieldBashParried", shieldBashParried);
        tag.putBoolean("CustomClone", customClone);
        tag.putDouble("OriginX", originX);
        tag.putDouble("OriginY", originY);
        tag.putDouble("OriginZ", originZ);
        tag.putDouble("DirectionX", directionX);
        tag.putDouble("DirectionZ", directionZ);
        tag.putLong("LastParryGameTime", lastParryGameTime);
        tag.putLong("LastMeleeMagicGameTime", lastMeleeMagicGameTime);
        putUuid(tag, "CustomCloneId", customCloneId);
        putUuid(tag, "MasterId", masterId);
        putUuid(tag, "ParryingEntityId", parryingEntityId);
        putUuid(tag, "LastMeleeMagicTargetId", lastMeleeMagicTargetId);
        return tag;
    }

    public void load(CompoundTag tag) {
        actionState = LichActionState.byId(tag.getInt("ActionState"));
        stateTicks = nonNegative(tag.getInt("StateTicks"));
        stateDuration = nonNegative(tag.getInt("StateDuration"));
        shieldBashCooldown = nonNegative(tag.getInt("ShieldBashCooldown"));
        summonCooldown = nonNegative(tag.getInt("SummonCooldown"));
        meleeCooldown = nonNegative(tag.getInt("MeleeCooldown"));
        cloneLifetimeTicks = nonNegative(tag.getInt("CloneLifetimeTicks"));
        secondPhase = tag.getBoolean("SecondPhase");
        shieldTracked = tag.getBoolean("ShieldTracked");
        shieldLayers = nonNegative(tag.getInt("ShieldLayers"));
        shieldBashEmpowered = tag.getBoolean("ShieldBashEmpowered");
        shieldBashParried = tag.getBoolean("ShieldBashParried");
        customClone = tag.getBoolean("CustomClone");
        originX = tag.getDouble("OriginX");
        originY = tag.getDouble("OriginY");
        originZ = tag.getDouble("OriginZ");
        directionX = tag.getDouble("DirectionX");
        directionZ = tag.getDouble("DirectionZ");
        lastParryGameTime = tag.contains("LastParryGameTime")
                ? tag.getLong("LastParryGameTime")
                : Long.MIN_VALUE;
        lastMeleeMagicGameTime = tag.contains("LastMeleeMagicGameTime")
                ? tag.getLong("LastMeleeMagicGameTime")
                : Long.MIN_VALUE;
        customCloneId = getUuid(tag, "CustomCloneId");
        masterId = getUuid(tag, "MasterId");
        parryingEntityId = getUuid(tag, "ParryingEntityId");
        lastMeleeMagicTargetId = getUuid(tag, "LastMeleeMagicTargetId");
    }

    private static int nonNegative(int value) {
        return Math.max(0, value);
    }

    private static void putUuid(
            CompoundTag tag, String key, @Nullable UUID value) {
        if (value != null) {
            tag.putUUID(key, value);
        }
    }

    @Nullable
    private static UUID getUuid(CompoundTag tag, String key) {
        return tag.hasUUID(key) ? tag.getUUID(key) : null;
    }
}