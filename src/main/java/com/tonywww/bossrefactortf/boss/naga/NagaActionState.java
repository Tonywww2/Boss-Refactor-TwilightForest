package com.tonywww.bossrefactortf.boss.naga;

public enum NagaActionState {
    IDLE,
    CRUISING,
    POUNCE_WINDUP,
    POUNCE_FLIGHT,
    POUNCE_IMPACT,
    TAIL_WINDUP,
    TAIL_STRIKE,
    VENOM_WINDUP,
    VENOM_PROJECTILE,
    STUNNED;

    public static NagaActionState byId(int id) {
        NagaActionState[] values = values();
        return id >= 0 && id < values.length ? values[id] : IDLE;
    }
}