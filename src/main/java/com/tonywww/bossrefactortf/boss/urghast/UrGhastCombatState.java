package com.tonywww.bossrefactortf.boss.urghast;

import net.minecraft.nbt.CompoundTag;

public final class UrGhastCombatState {
    UrGhastCombatMode mode = UrGhastCombatMode.PATROL;
    UrGhastActionState action = UrGhastActionState.NORMAL;
    boolean offensiveModeUnlocked;
    int modeTicks;
    int visitedPatrolPoints;
    int patrolIndex;
    boolean hasPatrolDestination;
    double patrolX;
    double patrolY;
    double patrolZ;
    int actionTicks;
    int actionDuration;
    int fireballCooldown;
    int mistCooldown;
    double mistOriginX;
    double mistOriginY;
    double mistOriginZ;
    double mistDestinationX;
    double mistDestinationY;
    double mistDestinationZ;
    long nextHurtSoundGameTime;

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Mode", mode.ordinal());
        tag.putInt("Action", action.ordinal());
        tag.putBoolean("OffensiveModeUnlocked", offensiveModeUnlocked);
        tag.putInt("ModeTicks", modeTicks);
        tag.putInt("VisitedPatrolPoints", visitedPatrolPoints);
        tag.putInt("PatrolIndex", patrolIndex);
        tag.putBoolean("HasPatrolDestination", hasPatrolDestination);
        tag.putDouble("PatrolX", patrolX);
        tag.putDouble("PatrolY", patrolY);
        tag.putDouble("PatrolZ", patrolZ);
        tag.putInt("ActionTicks", actionTicks);
        tag.putInt("ActionDuration", actionDuration);
        tag.putInt("FireballCooldown", fireballCooldown);
        tag.putInt("MistCooldown", mistCooldown);
        tag.putDouble("MistOriginX", mistOriginX);
        tag.putDouble("MistOriginY", mistOriginY);
        tag.putDouble("MistOriginZ", mistOriginZ);
        tag.putDouble("MistDestinationX", mistDestinationX);
        tag.putDouble("MistDestinationY", mistDestinationY);
        tag.putDouble("MistDestinationZ", mistDestinationZ);
        tag.putLong("NextHurtSoundGameTime", nextHurtSoundGameTime);
        return tag;
    }

    public void load(CompoundTag tag) {
        mode = UrGhastCombatMode.byId(tag.getInt("Mode"));
        action = UrGhastActionState.byId(tag.getInt("Action"));
        offensiveModeUnlocked = tag.getBoolean("OffensiveModeUnlocked");
        modeTicks = Math.max(0, tag.getInt("ModeTicks"));
        visitedPatrolPoints = Math.max(0, tag.getInt("VisitedPatrolPoints"));
        patrolIndex = Math.max(0, tag.getInt("PatrolIndex"));
        hasPatrolDestination = tag.getBoolean("HasPatrolDestination");
        patrolX = tag.getDouble("PatrolX");
        patrolY = tag.getDouble("PatrolY");
        patrolZ = tag.getDouble("PatrolZ");
        actionTicks = Math.max(0, tag.getInt("ActionTicks"));
        actionDuration = Math.max(0, tag.getInt("ActionDuration"));
        fireballCooldown = Math.max(0, tag.getInt("FireballCooldown"));
        mistCooldown = Math.max(0, tag.getInt("MistCooldown"));
        mistOriginX = tag.getDouble("MistOriginX");
        mistOriginY = tag.getDouble("MistOriginY");
        mistOriginZ = tag.getDouble("MistOriginZ");
        mistDestinationX = tag.getDouble("MistDestinationX");
        mistDestinationY = tag.getDouble("MistDestinationY");
        mistDestinationZ = tag.getDouble("MistDestinationZ");
        nextHurtSoundGameTime = Math.max(0L,
                tag.getLong("NextHurtSoundGameTime"));
    }
}