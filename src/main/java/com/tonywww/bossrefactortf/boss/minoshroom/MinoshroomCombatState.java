package com.tonywww.bossrefactortf.boss.minoshroom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class MinoshroomCombatState {
    MinoshroomAttackState attackState = MinoshroomAttackState.IDLE;
    int stateTicks;
    int stateDuration;
    int sweepRoundsCompleted;
    int stompImpactsCompleted;
    double originX;
    double originY;
    double originZ;
    double directionX;
    double directionZ;
    double dashDistance;
    long lastParryGameTime = Long.MIN_VALUE;
    final Set<UUID> dashHits = new HashSet<>();

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("AttackState", attackState.ordinal());
        tag.putInt("StateTicks", stateTicks);
        tag.putInt("StateDuration", stateDuration);
        tag.putInt("SweepRoundsCompleted", sweepRoundsCompleted);
        tag.putInt("StompImpactsCompleted", stompImpactsCompleted);
        tag.putDouble("OriginX", originX);
        tag.putDouble("OriginY", originY);
        tag.putDouble("OriginZ", originZ);
        tag.putDouble("DirectionX", directionX);
        tag.putDouble("DirectionZ", directionZ);
        tag.putDouble("DashDistance", dashDistance);
        tag.putLong("LastParryGameTime", lastParryGameTime);
        ListTag hitTags = new ListTag();
        for (UUID hit : dashHits) {
            CompoundTag hitTag = new CompoundTag();
            hitTag.putUUID("Target", hit);
            hitTags.add(hitTag);
        }
        tag.put("DashHits", hitTags);
        return tag;
    }

    public void load(CompoundTag tag) {
        attackState = MinoshroomAttackState.byId(tag.getInt("AttackState"));
        stateTicks = Math.max(0, tag.getInt("StateTicks"));
        stateDuration = Math.max(0, tag.getInt("StateDuration"));
        sweepRoundsCompleted = Math.max(0, tag.getInt("SweepRoundsCompleted"));
        stompImpactsCompleted = Math.max(0, tag.getInt("StompImpactsCompleted"));
        originX = tag.getDouble("OriginX");
        originY = tag.getDouble("OriginY");
        originZ = tag.getDouble("OriginZ");
        directionX = tag.getDouble("DirectionX");
        directionZ = tag.getDouble("DirectionZ");
        dashDistance = Math.max(0.0, tag.getDouble("DashDistance"));
        lastParryGameTime = tag.contains("LastParryGameTime")
                ? tag.getLong("LastParryGameTime")
                : Long.MIN_VALUE;
        dashHits.clear();
        ListTag hitTags = tag.getList("DashHits", Tag.TAG_COMPOUND);
        for (int index = 0; index < hitTags.size(); index++) {
            CompoundTag hitTag = hitTags.getCompound(index);
            if (hitTag.hasUUID("Target")) {
                dashHits.add(hitTag.getUUID("Target"));
            }
        }
    }
}