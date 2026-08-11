package com.tonywww.bossrefactortf.boss.alphayeti;

public enum AlphaYetiAttackState {
    IDLE,
    CRUISE,
    RAMPAGE,
    PURSUIT_ROAR,
    PURSUIT,
    STOMP_WINDUP,
    STOMP_ICE,
    REST;

    public static AlphaYetiAttackState byId(int id) {
        AlphaYetiAttackState[] values = values();
        return id >= 0 && id < values.length ? values[id] : IDLE;
    }
}