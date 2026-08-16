package com.tonywww.bossrefactortf.boss.snowqueen;

public enum SnowQueenActionState {
    IDLE,
    CRUISE,
    SLAM_WINDUP,
    SLAM_DESCENT,
    SLAM_INTERVAL,
    CHASE,
    RAIN_WINDUP,
    RAIN_ACTIVE,
    LINK_TELEPORT_DELAY,
    LINK_BURST_WINDUP;

    public static SnowQueenActionState byId(int id) {
        SnowQueenActionState[] values = values();
        return id >= 0 && id < values.length ? values[id] : IDLE;
    }
}