package com.tonywww.bossrefactortf.boss.alphayeti;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayDeque;
import java.util.Deque;

public final class AlphaYetiCombatState {
    AlphaYetiAttackState attackState = AlphaYetiAttackState.IDLE;
    AlphaYetiSkill lastSkill = AlphaYetiSkill.NONE;
    int stateTicks;
    int stateDuration;
    int fatigue;
    int imbalance;
    int meleeCooldown;
    int ringIndex;
    double originX;
    double originY;
    double originZ;
    long lastParryGameTime = Long.MIN_VALUE;
    final Deque<IceStrike> secondaryIce = new ArrayDeque<>();

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("AttackState", attackState.ordinal());
        tag.putInt("LastSkill", lastSkill.ordinal());
        tag.putInt("StateTicks", stateTicks);
        tag.putInt("StateDuration", stateDuration);
        tag.putInt("Fatigue", fatigue);
        tag.putInt("Imbalance", imbalance);
        tag.putInt("MeleeCooldown", meleeCooldown);
        tag.putInt("RingIndex", ringIndex);
        tag.putDouble("OriginX", originX);
        tag.putDouble("OriginY", originY);
        tag.putDouble("OriginZ", originZ);
        tag.putLong("LastParryGameTime", lastParryGameTime);
        ListTag iceTags = new ListTag();
        for (IceStrike strike : secondaryIce) {
            iceTags.add(strike.save());
        }
        tag.put("SecondaryIce", iceTags);
        return tag;
    }

    public void load(CompoundTag tag) {
        attackState = AlphaYetiAttackState.byId(tag.getInt("AttackState"));
        lastSkill = AlphaYetiSkill.byId(tag.getInt("LastSkill"));
        stateTicks = Math.max(0, tag.getInt("StateTicks"));
        stateDuration = Math.max(0, tag.getInt("StateDuration"));
        fatigue = Math.max(0, tag.getInt("Fatigue"));
        imbalance = Math.max(0, tag.getInt("Imbalance"));
        meleeCooldown = Math.max(0, tag.getInt("MeleeCooldown"));
        ringIndex = Math.max(0, tag.getInt("RingIndex"));
        originX = tag.getDouble("OriginX");
        originY = tag.getDouble("OriginY");
        originZ = tag.getDouble("OriginZ");
        lastParryGameTime = tag.contains("LastParryGameTime")
                ? tag.getLong("LastParryGameTime")
                : Long.MIN_VALUE;
        secondaryIce.clear();
        ListTag iceTags = tag.getList("SecondaryIce", Tag.TAG_COMPOUND);
        for (int index = 0; index < iceTags.size(); index++) {
            secondaryIce.addLast(IceStrike.load(iceTags.getCompound(index)));
        }
    }

    static final class IceStrike {
        final double x;
        final double y;
        final double z;
        final float radius;
        final int totalTicks;
        int remainingTicks;
        boolean visualsSpawned;

        IceStrike(double x, double y, double z, float radius, int totalTicks) {
            this(x, y, z, radius, totalTicks, totalTicks, false);
        }

        private IceStrike(
                double x, double y, double z, float radius,
                int totalTicks, int remainingTicks, boolean visualsSpawned) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = Math.max(0.0F, radius);
            this.totalTicks = Math.max(1, totalTicks);
            this.remainingTicks = Math.max(0, remainingTicks);
            this.visualsSpawned = visualsSpawned;
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("X", x);
            tag.putDouble("Y", y);
            tag.putDouble("Z", z);
            tag.putFloat("Radius", radius);
            tag.putInt("TotalTicks", totalTicks);
            tag.putInt("RemainingTicks", remainingTicks);
            tag.putBoolean("VisualsSpawned", visualsSpawned);
            return tag;
        }

        private static IceStrike load(CompoundTag tag) {
            return new IceStrike(
                    tag.getDouble("X"),
                    tag.getDouble("Y"),
                    tag.getDouble("Z"),
                    tag.getFloat("Radius"),
                    tag.getInt("TotalTicks"),
                    tag.getInt("RemainingTicks"),
                    tag.getBoolean("VisualsSpawned"));
        }
    }
}