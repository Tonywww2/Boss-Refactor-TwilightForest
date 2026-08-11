package com.tonywww.bossrefactortf.boss.alphayeti;

public enum AlphaYetiSkill {
    NONE,
    RAMPAGE,
    PURSUIT,
    STOMP;

    public static AlphaYetiSkill byId(int id) {
        AlphaYetiSkill[] values = values();
        return id >= 0 && id < values.length ? values[id] : NONE;
    }
}