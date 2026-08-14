package com.tonywww.bossrefactortf.boss.naga;

public enum NagaSkill {
    NONE,
    POUNCE,
    TAIL_SWEEP,
    VENOM_BREATH;

    public static NagaSkill byId(int id) {
        NagaSkill[] values = values();
        return id >= 0 && id < values.length ? values[id] : NONE;
    }
}