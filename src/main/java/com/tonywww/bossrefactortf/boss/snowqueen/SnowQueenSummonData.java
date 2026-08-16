package com.tonywww.bossrefactortf.boss.snowqueen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SnowQueenSummonData extends SavedData {
    private static final String DATA_NAME =
            "bossrefactortf_snow_queen_summons";
    private static final String DEAD_OWNERS_TAG = "DeadOwners";
    private static final String DEAD_SUMMONS_TAG = "DeadSummons";
    private static final String PENDING_LINK_DEATHS_TAG = "PendingLinkDeaths";
    private static final String OWNER_TAG = "Owner";
    private static final String SUMMON_TAG = "Summon";
    private static final String POSITIONS_TAG = "Positions";
    private static final String X_TAG = "X";
    private static final String Y_TAG = "Y";
    private static final String Z_TAG = "Z";

    private final Set<UUID> deadOwners = new HashSet<>();
    private final Set<UUID> deadSummons = new HashSet<>();
    private final Map<UUID, Deque<Vec3>> pendingLinkDeaths = new HashMap<>();

    public static SnowQueenSummonData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                SnowQueenSummonData::load,
                SnowQueenSummonData::new,
                DATA_NAME);
    }

    public static SnowQueenSummonData load(CompoundTag tag) {
        SnowQueenSummonData data = new SnowQueenSummonData();
        ListTag owners = tag.getList(DEAD_OWNERS_TAG, Tag.TAG_COMPOUND);
        for (Tag value : owners) {
            CompoundTag ownerTag = (CompoundTag) value;
            if (ownerTag.hasUUID(OWNER_TAG)) {
                data.deadOwners.add(ownerTag.getUUID(OWNER_TAG));
            }
        }
        ListTag summons = tag.getList(DEAD_SUMMONS_TAG, Tag.TAG_COMPOUND);
        for (Tag value : summons) {
            CompoundTag summonTag = (CompoundTag) value;
            if (summonTag.hasUUID(SUMMON_TAG)) {
                data.deadSummons.add(summonTag.getUUID(SUMMON_TAG));
            }
        }
        ListTag pendingDeaths = tag.getList(
                PENDING_LINK_DEATHS_TAG, Tag.TAG_COMPOUND);
        for (Tag value : pendingDeaths) {
            CompoundTag ownerTag = (CompoundTag) value;
            if (!ownerTag.hasUUID(OWNER_TAG)) {
                continue;
            }
            Deque<Vec3> positions = new ArrayDeque<>();
            ListTag positionTags = ownerTag.getList(
                    POSITIONS_TAG, Tag.TAG_COMPOUND);
            for (Tag positionValue : positionTags) {
                CompoundTag positionTag = (CompoundTag) positionValue;
                positions.addLast(new Vec3(
                        positionTag.getDouble(X_TAG),
                        positionTag.getDouble(Y_TAG),
                        positionTag.getDouble(Z_TAG)));
            }
            if (!positions.isEmpty()) {
                data.pendingLinkDeaths.put(
                        ownerTag.getUUID(OWNER_TAG), positions);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag owners = new ListTag();
        for (UUID ownerUuid : deadOwners) {
            CompoundTag ownerTag = new CompoundTag();
            ownerTag.putUUID(OWNER_TAG, ownerUuid);
            owners.add(ownerTag);
        }
        tag.put(DEAD_OWNERS_TAG, owners);
        ListTag summons = new ListTag();
        for (UUID summonUuid : deadSummons) {
            CompoundTag summonTag = new CompoundTag();
            summonTag.putUUID(SUMMON_TAG, summonUuid);
            summons.add(summonTag);
        }
        tag.put(DEAD_SUMMONS_TAG, summons);
        ListTag pendingDeaths = new ListTag();
        pendingLinkDeaths.forEach((ownerUuid, positions) -> {
            CompoundTag ownerTag = new CompoundTag();
            ownerTag.putUUID(OWNER_TAG, ownerUuid);
            ListTag positionTags = new ListTag();
            for (Vec3 position : positions) {
                CompoundTag positionTag = new CompoundTag();
                positionTag.putDouble(X_TAG, position.x);
                positionTag.putDouble(Y_TAG, position.y);
                positionTag.putDouble(Z_TAG, position.z);
                positionTags.add(positionTag);
            }
            ownerTag.put(POSITIONS_TAG, positionTags);
            pendingDeaths.add(ownerTag);
        });
        tag.put(PENDING_LINK_DEATHS_TAG, pendingDeaths);
        return tag;
    }

    public void markOwnerDead(UUID ownerUuid) {
        if (deadOwners.add(ownerUuid)) {
            setDirty();
        }
    }

    public boolean isOwnerDead(UUID ownerUuid) {
        return deadOwners.contains(ownerUuid);
    }

    public void markSummonDead(UUID summonUuid) {
        if (deadSummons.add(summonUuid)) {
            setDirty();
        }
    }

    public boolean isSummonDead(UUID summonUuid) {
        return deadSummons.contains(summonUuid);
    }

    public void enqueue(UUID ownerUuid, Vec3 position) {
        pendingLinkDeaths.computeIfAbsent(
                ownerUuid, ignored -> new ArrayDeque<>()).addLast(position);
        setDirty();
    }

    public List<Vec3> drain(UUID ownerUuid) {
        Deque<Vec3> positions = pendingLinkDeaths.remove(ownerUuid);
        if (positions == null) {
            return List.of();
        }
        setDirty();
        return new ArrayList<>(positions);
    }

    public void clearSummonDead(UUID summonUuid) {
        if (deadSummons.remove(summonUuid)) {
            setDirty();
        }
    }
}