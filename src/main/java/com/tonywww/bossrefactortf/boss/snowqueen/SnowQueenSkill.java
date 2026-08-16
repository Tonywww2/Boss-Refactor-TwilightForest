package com.tonywww.bossrefactortf.boss.snowqueen;

public enum SnowQueenSkill {
    NONE,
    REPEATED_SLAM,
    SHATTERED_ICE_RAIN;

    public static SnowQueenSkill byId(int id) {
        SnowQueenSkill[] values = values();
        return id >= 0 && id < values.length ? values[id] : NONE;
    }
}