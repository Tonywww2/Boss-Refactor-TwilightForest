package com.tonywww.bossrefactortf.boss.urghast;

public enum UrGhastActionState {
    NORMAL,
    FIREBALL_WINDUP,
    MIST_WINDUP,
    MIST_DAMAGE;

    public static UrGhastActionState byId(int id) {
        UrGhastActionState[] values = values();
        return id >= 0 && id < values.length ? values[id] : NORMAL;
    }
}