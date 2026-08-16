package com.tonywww.bossrefactortf.boss.snowqueen;

import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SnowQueenCombatState {
    SnowQueenActionState actionState = SnowQueenActionState.IDLE;
    SnowQueenSkill lastSkill = SnowQueenSkill.NONE;
    int stateTicks;
    int stateDuration;
    int slamsCompleted;
    boolean secondPhase;
    boolean suppressLinkDeathQueue;
    double lockedX;
    double lockedY;
    double lockedZ;
    long lastParryGameTime = Long.MIN_VALUE;
    AttackTelegraph currentTelegraph = AttackTelegraph.NONE;
    final Set<UUID> linkedSummons = new LinkedHashSet<>();
    final Set<UUID> ownedSummons = new LinkedHashSet<>();
    final Set<UUID> rainVolley = new LinkedHashSet<>();
    final Deque<Vec3> linkDeathPositions = new ArrayDeque<>();
    final Map<UUID, Long> shieldCollisionCooldowns = new HashMap<>();

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ActionState", actionState.ordinal());
        tag.putInt("LastSkill", lastSkill.ordinal());
        tag.putInt("StateTicks", stateTicks);
        tag.putInt("StateDuration", stateDuration);
        tag.putInt("SlamsCompleted", slamsCompleted);
        tag.putBoolean("SecondPhase", secondPhase);
        tag.putDouble("LockedX", lockedX);
        tag.putDouble("LockedY", lockedY);
        tag.putDouble("LockedZ", lockedZ);
        tag.putLong("LastParryGameTime", lastParryGameTime);
        tag.put("CurrentTelegraph", AttackTelegraphNbt.encode(currentTelegraph));
        tag.put("LinkedSummons", saveUuids(linkedSummons));
        tag.put("OwnedSummons", saveUuids(ownedSummons));
        tag.put("RainVolley", saveUuids(rainVolley));

        ListTag positionTags = new ListTag();
        for (Vec3 position : linkDeathPositions) {
            CompoundTag positionTag = new CompoundTag();
            positionTag.putDouble("X", position.x);
            positionTag.putDouble("Y", position.y);
            positionTag.putDouble("Z", position.z);
            positionTags.add(positionTag);
        }
        tag.put("LinkDeathPositions", positionTags);

        ListTag cooldownTags = new ListTag();
        shieldCollisionCooldowns.forEach((target, expiresAt) -> {
            CompoundTag cooldownTag = new CompoundTag();
            cooldownTag.putUUID("Target", target);
            cooldownTag.putLong("ExpiresAt", expiresAt);
            cooldownTags.add(cooldownTag);
        });
        tag.put("ShieldCollisionCooldowns", cooldownTags);
        return tag;
    }

    public void load(CompoundTag tag) {
        actionState = SnowQueenActionState.byId(tag.getInt("ActionState"));
        lastSkill = SnowQueenSkill.byId(tag.getInt("LastSkill"));
        stateTicks = Math.max(0, tag.getInt("StateTicks"));
        stateDuration = Math.max(0, tag.getInt("StateDuration"));
        slamsCompleted = Math.max(0, tag.getInt("SlamsCompleted"));
        secondPhase = tag.getBoolean("SecondPhase");
        suppressLinkDeathQueue = false;
        lockedX = tag.getDouble("LockedX");
        lockedY = tag.getDouble("LockedY");
        lockedZ = tag.getDouble("LockedZ");
        lastParryGameTime = tag.contains("LastParryGameTime")
                ? tag.getLong("LastParryGameTime")
                : Long.MIN_VALUE;
        currentTelegraph = tag.contains("CurrentTelegraph", Tag.TAG_COMPOUND)
                ? AttackTelegraphNbt.decode(tag.getCompound("CurrentTelegraph"))
                : AttackTelegraph.NONE;

        loadUuids(tag.getList("LinkedSummons", Tag.TAG_COMPOUND), linkedSummons);
        loadUuids(tag.getList("OwnedSummons", Tag.TAG_COMPOUND), ownedSummons);
        loadUuids(tag.getList("RainVolley", Tag.TAG_COMPOUND), rainVolley);

        linkDeathPositions.clear();
        ListTag positionTags = tag.getList(
                "LinkDeathPositions", Tag.TAG_COMPOUND);
        for (int index = 0; index < positionTags.size(); index++) {
            CompoundTag positionTag = positionTags.getCompound(index);
            linkDeathPositions.addLast(new Vec3(
                    positionTag.getDouble("X"),
                    positionTag.getDouble("Y"),
                    positionTag.getDouble("Z")));
        }

        shieldCollisionCooldowns.clear();
        ListTag cooldownTags = tag.getList(
                "ShieldCollisionCooldowns", Tag.TAG_COMPOUND);
        for (int index = 0; index < cooldownTags.size(); index++) {
            CompoundTag cooldownTag = cooldownTags.getCompound(index);
            if (cooldownTag.hasUUID("Target")) {
                shieldCollisionCooldowns.put(
                        cooldownTag.getUUID("Target"),
                        cooldownTag.getLong("ExpiresAt"));
            }
        }
    }

    private static ListTag saveUuids(Iterable<UUID> uuids) {
        ListTag tags = new ListTag();
        for (UUID uuid : uuids) {
            CompoundTag uuidTag = new CompoundTag();
            uuidTag.putUUID("Uuid", uuid);
            tags.add(uuidTag);
        }
        return tags;
    }

    private static void loadUuids(ListTag tags, Set<UUID> destination) {
        destination.clear();
        for (int index = 0; index < tags.size(); index++) {
            CompoundTag uuidTag = tags.getCompound(index);
            if (uuidTag.hasUUID("Uuid")) {
                destination.add(uuidTag.getUUID("Uuid"));
            }
        }
    }
}