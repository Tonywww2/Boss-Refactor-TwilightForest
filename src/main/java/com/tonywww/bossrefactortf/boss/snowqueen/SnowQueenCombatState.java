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
    boolean secondPhase;
    boolean suppressLinkDeathQueue;
    boolean rainRoundActive;
    boolean rainVolleySpawned;
    int rainQueenWindupRemaining;
    int rainCooldownRemaining;
    int linkEstablishTicks;
    UUID preferredLinkUuid;
    int linkTeleportRemaining;
    int fallingIceCooldown;
    boolean fallingIcePending;
    int fallingIceRemaining;
    int fallingIceTotal;
    boolean fallingIceVisualSpawned;
    double fallingIceX;
    double fallingIceY;
    double fallingIceZ;
    double lockedX;
    double lockedY;
    double lockedZ;
    long lastParryGameTime = Long.MIN_VALUE;
    UUID lastParryActorUuid;
    long iceShieldParryWindowStart = Long.MIN_VALUE;
    boolean iceShieldParryWindowActive;
    AttackTelegraph currentTelegraph = AttackTelegraph.NONE;
    AttackTelegraph dropTelegraph = AttackTelegraph.NONE;
    final Set<UUID> linkedSummons = new LinkedHashSet<>();
    final Set<UUID> ownedSummons = new LinkedHashSet<>();
    final Set<UUID> rainVolley = new LinkedHashSet<>();
    final Set<UUID> dropDirectHits = new LinkedHashSet<>();
    final Deque<Vec3> linkDeathPositions = new ArrayDeque<>();
    final Map<UUID, Long> shieldCollisionCooldowns = new HashMap<>();

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("SecondPhase", secondPhase);
        tag.putBoolean("RainRoundActive", rainRoundActive);
        tag.putBoolean("RainVolleySpawned", rainVolleySpawned);
        tag.putInt("RainQueenWindupRemaining", rainQueenWindupRemaining);
        tag.putInt("RainCooldownRemaining", rainCooldownRemaining);
        tag.putInt("LinkEstablishTicks", linkEstablishTicks);
        if (preferredLinkUuid != null) {
            tag.putUUID("PreferredLinkUuid", preferredLinkUuid);
        }
        tag.putInt("LinkTeleportRemaining", linkTeleportRemaining);
        tag.putInt("FallingIceCooldown", fallingIceCooldown);
        tag.putBoolean("FallingIcePending", fallingIcePending);
        tag.putInt("FallingIceRemaining", fallingIceRemaining);
        tag.putInt("FallingIceTotal", fallingIceTotal);
        tag.putBoolean("FallingIceVisualSpawned", fallingIceVisualSpawned);
        tag.putDouble("FallingIceX", fallingIceX);
        tag.putDouble("FallingIceY", fallingIceY);
        tag.putDouble("FallingIceZ", fallingIceZ);
        tag.putDouble("LockedX", lockedX);
        tag.putDouble("LockedY", lockedY);
        tag.putDouble("LockedZ", lockedZ);
        tag.putLong("LastParryGameTime", lastParryGameTime);
        if (lastParryActorUuid != null) {
            tag.putUUID("LastParryActorUuid", lastParryActorUuid);
        }
        tag.put("CurrentTelegraph", AttackTelegraphNbt.encode(currentTelegraph));
        tag.put("DropTelegraph", AttackTelegraphNbt.encode(dropTelegraph));
        tag.put("LinkedSummons", saveUuids(linkedSummons));
        tag.put("OwnedSummons", saveUuids(ownedSummons));
        tag.put("RainVolley", saveUuids(rainVolley));
        tag.put("DropDirectHits", saveUuids(dropDirectHits));

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
        secondPhase = tag.getBoolean("SecondPhase");
        suppressLinkDeathQueue = false;
        rainRoundActive = tag.getBoolean("RainRoundActive");
        rainVolleySpawned = tag.getBoolean("RainVolleySpawned");
        rainQueenWindupRemaining = Math.max(
            0, tag.getInt("RainQueenWindupRemaining"));
        rainCooldownRemaining = Math.max(
            0, tag.getInt("RainCooldownRemaining"));
        linkEstablishTicks = Math.max(0, tag.getInt("LinkEstablishTicks"));
        preferredLinkUuid = tag.hasUUID("PreferredLinkUuid")
            ? tag.getUUID("PreferredLinkUuid")
            : null;
        linkTeleportRemaining = Math.max(
            0, tag.getInt("LinkTeleportRemaining"));
        fallingIceCooldown = Math.max(0, tag.getInt("FallingIceCooldown"));
        fallingIcePending = tag.getBoolean("FallingIcePending");
        fallingIceRemaining = Math.max(0, tag.getInt("FallingIceRemaining"));
        fallingIceTotal = Math.max(0, tag.getInt("FallingIceTotal"));
        fallingIceVisualSpawned = tag.getBoolean("FallingIceVisualSpawned");
        fallingIceX = tag.getDouble("FallingIceX");
        fallingIceY = tag.getDouble("FallingIceY");
        fallingIceZ = tag.getDouble("FallingIceZ");
        lockedX = tag.getDouble("LockedX");
        lockedY = tag.getDouble("LockedY");
        lockedZ = tag.getDouble("LockedZ");
        lastParryGameTime = tag.contains("LastParryGameTime")
                ? tag.getLong("LastParryGameTime")
                : Long.MIN_VALUE;
        lastParryActorUuid = tag.hasUUID("LastParryActorUuid")
            ? tag.getUUID("LastParryActorUuid")
            : null;
        currentTelegraph = tag.contains("CurrentTelegraph", Tag.TAG_COMPOUND)
            ? AttackTelegraphNbt.decode(tag.getCompound("CurrentTelegraph"))
            : AttackTelegraph.NONE;
        dropTelegraph = tag.contains("DropTelegraph", Tag.TAG_COMPOUND)
            ? AttackTelegraphNbt.decode(tag.getCompound("DropTelegraph"))
            : AttackTelegraph.NONE;

        loadUuids(tag.getList("LinkedSummons", Tag.TAG_COMPOUND), linkedSummons);
        loadUuids(tag.getList("OwnedSummons", Tag.TAG_COMPOUND), ownedSummons);
        loadUuids(tag.getList("RainVolley", Tag.TAG_COMPOUND), rainVolley);
        loadUuids(tag.getList("DropDirectHits", Tag.TAG_COMPOUND),
            dropDirectHits);

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