package com.tonywww.bossrefactortf.boss.knightphantom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class KnightPhantomEncounterData extends SavedData {
    private static final String DATA_NAME = "bossrefactortf_knight_phantom_encounters";
    private static final String ENCOUNTERS_TAG = "Encounters";
    private static final String ENCOUNTER_ID_TAG = "EncounterId";
    private static final String OWNER_ID_TAG = "OwnerId";
    private static final String NEXT_CLAIM_GAME_TIME_TAG = "NextClaimGameTime";

    private final Map<UUID, Ownership> ownershipByEncounter = new HashMap<>();

    public static KnightPhantomEncounterData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                KnightPhantomEncounterData::load,
                KnightPhantomEncounterData::new,
                DATA_NAME);
    }

    public static KnightPhantomEncounterData load(CompoundTag tag) {
        KnightPhantomEncounterData data = new KnightPhantomEncounterData();
        ListTag encounters = tag.getList(ENCOUNTERS_TAG, Tag.TAG_COMPOUND);
        for (Tag value : encounters) {
            CompoundTag encounterTag = (CompoundTag) value;
            if (!encounterTag.hasUUID(ENCOUNTER_ID_TAG)) {
                continue;
            }
            UUID encounterId = encounterTag.getUUID(ENCOUNTER_ID_TAG);
            UUID ownerId = encounterTag.hasUUID(OWNER_ID_TAG)
                    ? encounterTag.getUUID(OWNER_ID_TAG)
                    : null;
            long nextClaimGameTime = Math.max(0L,
                    encounterTag.getLong(NEXT_CLAIM_GAME_TIME_TAG));
            data.ownershipByEncounter.put(
                    encounterId, new Ownership(ownerId, nextClaimGameTime));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag encounters = new ListTag();
        for (Map.Entry<UUID, Ownership> entry : ownershipByEncounter.entrySet()) {
            CompoundTag encounterTag = new CompoundTag();
            encounterTag.putUUID(ENCOUNTER_ID_TAG, entry.getKey());
            Ownership ownership = entry.getValue();
            if (ownership.ownerId != null) {
                encounterTag.putUUID(OWNER_ID_TAG, ownership.ownerId);
            }
            encounterTag.putLong(
                    NEXT_CLAIM_GAME_TIME_TAG, ownership.nextClaimGameTime);
            encounters.add(encounterTag);
        }
        tag.put(ENCOUNTERS_TAG, encounters);
        return tag;
    }

    @Nullable
    public UUID owner(UUID encounterId) {
        Ownership ownership = ownershipByEncounter.get(encounterId);
        return ownership == null ? null : ownership.ownerId;
    }

    public boolean tryClaim(
            UUID encounterId, UUID candidateId, long currentGameTime) {
        Ownership ownership = ownershipByEncounter.computeIfAbsent(
                encounterId, ignored -> new Ownership(null, 0L));
        if (ownership.ownerId != null
                || currentGameTime < ownership.nextClaimGameTime) {
            return false;
        }
        ownership.ownerId = candidateId;
        setDirty();
        return true;
    }

    public boolean releaseCompleted(
            UUID encounterId, UUID expectedOwnerId, long currentGameTime) {
        return release(encounterId, expectedOwnerId, currentGameTime);
    }

    public boolean releaseAfterDeath(
            UUID encounterId, UUID expectedOwnerId, long nextClaimGameTime) {
        return release(encounterId, expectedOwnerId, nextClaimGameTime);
    }

    public void clear(UUID encounterId) {
        if (ownershipByEncounter.remove(encounterId) != null) {
            setDirty();
        }
    }

    private boolean release(
            UUID encounterId, UUID expectedOwnerId, long nextClaimGameTime) {
        Ownership ownership = ownershipByEncounter.get(encounterId);
        if (ownership == null
                || !expectedOwnerId.equals(ownership.ownerId)) {
            return false;
        }
        ownership.ownerId = null;
        ownership.nextClaimGameTime = Math.max(0L, nextClaimGameTime);
        setDirty();
        return true;
    }

    private static final class Ownership {
        @Nullable
        private UUID ownerId;
        private long nextClaimGameTime;

        private Ownership(@Nullable UUID ownerId, long nextClaimGameTime) {
            this.ownerId = ownerId;
            this.nextClaimGameTime = nextClaimGameTime;
        }
    }
}