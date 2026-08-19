package com.tonywww.bossrefactortf.boss.naga;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.UUID;

public final class NagaCombatState {
    NagaActionState actionState = NagaActionState.IDLE;
    NagaSkill lastSkill = NagaSkill.NONE;
    int stateTicks;
    int stateDuration;
    int chargeCooldown;
    int pounceCooldown;
    int venomCooldown;
    double lockedX;
    double lockedY;
    double lockedZ;
    double directionX;
    double directionZ;
    boolean secondPhase;
    boolean stunImmunityAvailable;
    int stunImmunityRecoveryTicks;
    int fireSnakeTicks;
    int fireSnakeElapsedTicks;
    int fireSnakeCooldownTicks;
    boolean hasLastFirePoint;
    double lastFirePointX;
    double lastFirePointY;
    double lastFirePointZ;
    long lastParryGameTime = Long.MIN_VALUE;
    @Nullable
    UUID venomProjectileId;

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("SchemaVersion", 2);
        tag.putString("ActionStateId", actionState.id());
        tag.putString("LastSkillId", lastSkill.id());
        tag.putInt("StateTicks", stateTicks);
        tag.putInt("StateDuration", stateDuration);
        tag.putInt("ChargeCooldown", chargeCooldown);
        tag.putInt("PounceCooldown", pounceCooldown);
        tag.putInt("VenomCooldown", venomCooldown);
        tag.putDouble("LockedX", lockedX);
        tag.putDouble("LockedY", lockedY);
        tag.putDouble("LockedZ", lockedZ);
        tag.putDouble("DirectionX", directionX);
        tag.putDouble("DirectionZ", directionZ);
        tag.putBoolean("SecondPhase", secondPhase);
        tag.putBoolean("StunImmunityAvailable", stunImmunityAvailable);
        tag.putInt("StunImmunityRecoveryTicks", stunImmunityRecoveryTicks);
        tag.putInt("FireSnakeTicks", fireSnakeTicks);
        tag.putInt("FireSnakeElapsedTicks", fireSnakeElapsedTicks);
        tag.putInt("FireSnakeCooldownTicks", fireSnakeCooldownTicks);
        tag.putBoolean("HasLastFirePoint", hasLastFirePoint);
        tag.putDouble("LastFirePointX", lastFirePointX);
        tag.putDouble("LastFirePointY", lastFirePointY);
        tag.putDouble("LastFirePointZ", lastFirePointZ);
        tag.putLong("LastParryGameTime", lastParryGameTime);
        if (venomProjectileId != null) {
            tag.putUUID("VenomProjectileId", venomProjectileId);
        }
        return tag;
    }

    public void load(CompoundTag tag, int configuredFireSnakeCooldownTicks) {
        actionState = tag.contains("ActionStateId", Tag.TAG_STRING)
                ? NagaActionState.byId(tag.getString("ActionStateId"))
                : NagaActionState.fromLegacyOrdinal(tag.getInt("ActionState"));
        lastSkill = tag.contains("LastSkillId", Tag.TAG_STRING)
                ? NagaSkill.byId(tag.getString("LastSkillId"))
                : NagaSkill.fromLegacyOrdinal(tag.getInt("LastSkill"));
        stateTicks = nonNegative(tag.getInt("StateTicks"));
        stateDuration = nonNegative(tag.getInt("StateDuration"));
        chargeCooldown = nonNegative(tag.getInt("ChargeCooldown"));
        pounceCooldown = nonNegative(tag.getInt("PounceCooldown"));
        venomCooldown = nonNegative(tag.getInt("VenomCooldown"));
        lockedX = tag.getDouble("LockedX");
        lockedY = tag.getDouble("LockedY");
        lockedZ = tag.getDouble("LockedZ");
        directionX = tag.getDouble("DirectionX");
        directionZ = tag.getDouble("DirectionZ");
        secondPhase = tag.getBoolean("SecondPhase");
        stunImmunityAvailable = tag.getBoolean("StunImmunityAvailable");
        stunImmunityRecoveryTicks = nonNegative(
                tag.getInt("StunImmunityRecoveryTicks"));
        fireSnakeTicks = nonNegative(tag.getInt("FireSnakeTicks"));
        fireSnakeElapsedTicks = nonNegative(
                tag.getInt("FireSnakeElapsedTicks"));
        fireSnakeCooldownTicks = tag.contains("FireSnakeCooldownTicks")
                ? nonNegative(tag.getInt("FireSnakeCooldownTicks"))
                : secondPhase && fireSnakeTicks == 0
                        ? Math.max(0, configuredFireSnakeCooldownTicks)
                        : 0;
        hasLastFirePoint = tag.getBoolean("HasLastFirePoint");
        lastFirePointX = tag.getDouble("LastFirePointX");
        lastFirePointY = tag.getDouble("LastFirePointY");
        lastFirePointZ = tag.getDouble("LastFirePointZ");
        lastParryGameTime = tag.contains("LastParryGameTime")
                ? tag.getLong("LastParryGameTime")
                : Long.MIN_VALUE;
        venomProjectileId = tag.hasUUID("VenomProjectileId")
                ? tag.getUUID("VenomProjectileId")
                : null;
    }

    private static int nonNegative(int value) {
        return Math.max(0, value);
    }
}