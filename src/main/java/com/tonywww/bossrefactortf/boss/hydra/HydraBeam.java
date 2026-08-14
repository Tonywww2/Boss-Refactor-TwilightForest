package com.tonywww.bossrefactortf.boss.hydra;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public final class HydraBeam {
    final UUID id;
    final double x;
    final double y;
    final double z;
    final double radius;
    final int totalWindupTicks;
    int remainingWindupTicks;

    HydraBeam(
            UUID id, double x, double y, double z,
            double radius, int windupTicks) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = Math.max(0.0, radius);
        this.totalWindupTicks = Math.max(1, windupTicks);
        this.remainingWindupTicks = Math.max(1, windupTicks);
    }

    private HydraBeam(
            UUID id, double x, double y, double z, double radius,
            int totalWindupTicks, int remainingWindupTicks) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = Math.max(0.0, radius);
        this.totalWindupTicks = Math.max(1, totalWindupTicks);
        this.remainingWindupTicks = Math.max(1, Math.min(
                this.totalWindupTicks, remainingWindupTicks));
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putDouble("X", x);
        tag.putDouble("Y", y);
        tag.putDouble("Z", z);
        tag.putDouble("Radius", radius);
        tag.putInt("TotalWindupTicks", totalWindupTicks);
        tag.putInt("RemainingWindupTicks", remainingWindupTicks);
        return tag;
    }

    static HydraBeam load(CompoundTag tag) {
        UUID id = tag.hasUUID("Id") ? tag.getUUID("Id") : UUID.randomUUID();
        return new HydraBeam(
                id,
                tag.getDouble("X"),
                tag.getDouble("Y"),
                tag.getDouble("Z"),
                tag.getDouble("Radius"),
                tag.getInt("TotalWindupTicks"),
                tag.getInt("RemainingWindupTicks"));
    }
}