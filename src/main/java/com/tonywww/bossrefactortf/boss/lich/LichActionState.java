package com.tonywww.bossrefactortf.boss.lich;

public enum LichActionState {
    IDLE,
    CRUISE,
    SHIELD_BASH_WINDUP,
    SUMMON_WINDUP,
    STUNNED;

    public static LichActionState byId(int id) {
        LichActionState[] values = values();
        return id >= 0 && id < values.length ? values[id] : IDLE;
    }
}