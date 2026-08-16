package com.tonywww.bossrefactortf.boss.snowqueen;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public final class SnowQueenSummonNbt {
    private SnowQueenSummonNbt() {
    }

    public static CompoundTag save(SnowQueenSummonAccess access) {
        CompoundTag tag = new CompoundTag();
        UUID ownerUuid = access.bossRefactorTF$getSnowQueenOwnerUuid();
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        tag.putBoolean("Linked", access.bossRefactorTF$isSnowQueenLinked());
        tag.putInt("Role", access.bossRefactorTF$getSnowQueenSummonRole());
        tag.putInt("State", access.bossRefactorTF$getSnowQueenSummonState());
        tag.putInt("Ticks", access.bossRefactorTF$getSnowQueenSummonTicks());
        tag.putInt("Duration", access.bossRefactorTF$getSnowQueenSummonDuration());
        tag.putDouble("DashX", access.bossRefactorTF$getSnowQueenDashX());
        tag.putDouble("DashY", access.bossRefactorTF$getSnowQueenDashY());
        tag.putDouble("DashZ", access.bossRefactorTF$getSnowQueenDashZ());
        tag.putBoolean("ReportedDeath",
                access.bossRefactorTF$hasReportedSnowQueenDeath());
        return tag;
    }

    public static void load(
            CompoundTag tag, SnowQueenSummonAccess access) {
        access.bossRefactorTF$setSnowQueenOwnerUuid(
                tag.hasUUID("Owner") ? tag.getUUID("Owner") : null);
        access.bossRefactorTF$setSnowQueenOwnerEntityId(0);
        access.bossRefactorTF$setSnowQueenLinked(tag.getBoolean("Linked"));
        access.bossRefactorTF$setSnowQueenSummonRole(tag.getInt("Role"));
        access.bossRefactorTF$setSnowQueenSummonState(tag.getInt("State"));
        access.bossRefactorTF$setSnowQueenSummonTicks(
                Math.max(0, tag.getInt("Ticks")));
        access.bossRefactorTF$setSnowQueenSummonDuration(
                Math.max(0, tag.getInt("Duration")));
        access.bossRefactorTF$setSnowQueenDash(
                tag.getDouble("DashX"),
                tag.getDouble("DashY"),
                tag.getDouble("DashZ"));
        access.bossRefactorTF$setReportedSnowQueenDeath(
                tag.getBoolean("ReportedDeath"));
    }
}