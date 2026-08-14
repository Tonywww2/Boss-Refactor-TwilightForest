package com.tonywww.bossrefactortf.boss.hydra;

public enum HydraActionState {
    IDLE,
    WINDUP,
    FOLLOW_UP_DELAY,
    RECOVERY,
    FIRE_LORD;

    public static HydraActionState byId(int id) {
        HydraActionState[] values = values();
        return id >= 0 && id < values.length ? values[id] : IDLE;
    }
}