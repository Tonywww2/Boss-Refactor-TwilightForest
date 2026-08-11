package com.tonywww.bossrefactortf.boss.urghast;

public enum UrGhastCombatMode {
    PATROL,
    OFFENSIVE;

    public static UrGhastCombatMode byId(int id) {
        UrGhastCombatMode[] values = values();
        return id >= 0 && id < values.length ? values[id] : PATROL;
    }

    public UrGhastCombatMode opposite() {
        return this == PATROL ? OFFENSIVE : PATROL;
    }
}