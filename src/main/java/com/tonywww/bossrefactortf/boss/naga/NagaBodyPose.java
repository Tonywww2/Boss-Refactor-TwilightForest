package com.tonywww.bossrefactortf.boss.naga;

public enum NagaBodyPose {
    TRAIL,
    FROZEN;

    public static NagaBodyPose byId(int id) {
        NagaBodyPose[] values = values();
        return id >= 0 && id < values.length ? values[id] : TRAIL;
    }
}