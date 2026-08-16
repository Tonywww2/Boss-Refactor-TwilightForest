package com.tonywww.bossrefactortf.boss.minoshroom;

public enum MinoshroomAttackState {
    IDLE,
    CRUISE,
    SWEEP_WINDUP,
    SWEEP_DASH,
    SWEEP_STRIKE_WINDUP,
    STOMP_WINDUP,
    STOMP_INTERVAL,
    STUNNED;

    public static MinoshroomAttackState byId(int id) {
        MinoshroomAttackState[] values = values();
        return id >= 0 && id < values.length ? values[id] : IDLE;
    }
}