package com.tonywww.bossrefactortf.boss.hydra;

public enum HydraSkill {
    NONE,
    BITE,
    SWEEP,
    SLAM,
    FIRE_LORD;

    public static HydraSkill byId(int id) {
        HydraSkill[] values = values();
        return id >= 0 && id < values.length ? values[id] : NONE;
    }

    public boolean isOrdinaryMelee() {
        return this == BITE || this == SWEEP || this == SLAM;
    }
}