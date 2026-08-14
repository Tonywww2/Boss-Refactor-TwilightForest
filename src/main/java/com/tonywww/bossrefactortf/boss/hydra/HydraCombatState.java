package com.tonywww.bossrefactortf.boss.hydra;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public final class HydraCombatState {
    HydraActionState actionState = HydraActionState.IDLE;
    HydraSkill skill = HydraSkill.NONE;
    HydraSkill pendingFollowUpSkill = HydraSkill.NONE;
    int actionTicks;
    int actionDuration;
    int activeHeadIndex = -1;
    int pendingFollowUpHeadIndex = -1;
    int ordinaryAttackCooldown;
    int fireLordCooldown;
    int secondPhaseBeamCooldown;
    float recoveryVisualProgress;
    boolean followUpAttack;
    boolean secondPhase;
    boolean combatInitialized;
    double lockedX;
    double lockedY;
    double lockedZ;
    double directionX;
    double directionZ = 1.0;
    long lastParryGameTime = Long.MIN_VALUE;
    final List<HydraBeam> beams = new ArrayList<>();

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ActionState", actionState.ordinal());
        tag.putInt("Skill", skill.ordinal());
        tag.putInt("PendingFollowUpSkill", pendingFollowUpSkill.ordinal());
        tag.putInt("ActionTicks", actionTicks);
        tag.putInt("ActionDuration", actionDuration);
        tag.putInt("ActiveHeadIndex", activeHeadIndex);
        tag.putInt("PendingFollowUpHeadIndex", pendingFollowUpHeadIndex);
        tag.putInt("OrdinaryAttackCooldown", ordinaryAttackCooldown);
        tag.putInt("FireLordCooldown", fireLordCooldown);
        tag.putInt("SecondPhaseBeamCooldown", secondPhaseBeamCooldown);
        tag.putFloat("RecoveryVisualProgress", recoveryVisualProgress);
        tag.putBoolean("FollowUpAttack", followUpAttack);
        tag.putBoolean("SecondPhase", secondPhase);
        tag.putBoolean("CombatInitialized", combatInitialized);
        tag.putDouble("LockedX", lockedX);
        tag.putDouble("LockedY", lockedY);
        tag.putDouble("LockedZ", lockedZ);
        tag.putDouble("DirectionX", directionX);
        tag.putDouble("DirectionZ", directionZ);
        tag.putLong("LastParryGameTime", lastParryGameTime);
        ListTag beamTags = new ListTag();
        for (HydraBeam beam : beams) {
            beamTags.add(beam.save());
        }
        tag.put("Beams", beamTags);
        return tag;
    }

    public void load(CompoundTag tag) {
        actionState = HydraActionState.byId(tag.getInt("ActionState"));
        skill = HydraSkill.byId(tag.getInt("Skill"));
        pendingFollowUpSkill = HydraSkill.byId(
                tag.getInt("PendingFollowUpSkill"));
        actionTicks = nonNegative(tag.getInt("ActionTicks"));
        actionDuration = nonNegative(tag.getInt("ActionDuration"));
        activeHeadIndex = tag.getInt("ActiveHeadIndex");
        pendingFollowUpHeadIndex = tag.getInt("PendingFollowUpHeadIndex");
        ordinaryAttackCooldown = nonNegative(
                tag.getInt("OrdinaryAttackCooldown"));
        fireLordCooldown = nonNegative(tag.getInt("FireLordCooldown"));
        secondPhaseBeamCooldown = nonNegative(
                tag.getInt("SecondPhaseBeamCooldown"));
        recoveryVisualProgress = Math.max(0.0F, Math.min(
            1.0F, tag.getFloat("RecoveryVisualProgress")));
        followUpAttack = tag.getBoolean("FollowUpAttack");
        secondPhase = tag.getBoolean("SecondPhase");
        combatInitialized = tag.getBoolean("CombatInitialized");
        lockedX = tag.getDouble("LockedX");
        lockedY = tag.getDouble("LockedY");
        lockedZ = tag.getDouble("LockedZ");
        directionX = tag.getDouble("DirectionX");
        directionZ = tag.getDouble("DirectionZ");
        lastParryGameTime = tag.contains("LastParryGameTime")
                ? tag.getLong("LastParryGameTime")
                : Long.MIN_VALUE;
        beams.clear();
        ListTag beamTags = tag.getList("Beams", Tag.TAG_COMPOUND);
        for (int index = 0; index < beamTags.size(); index++) {
            beams.add(HydraBeam.load(beamTags.getCompound(index)));
        }
    }

    private static int nonNegative(int value) {
        return Math.max(0, value);
    }
}