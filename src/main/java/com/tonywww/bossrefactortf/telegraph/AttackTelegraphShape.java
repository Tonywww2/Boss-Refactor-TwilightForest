package com.tonywww.bossrefactortf.telegraph;

public enum AttackTelegraphShape {
    NONE,
    ARC,
    CIRCLE,
    RING,
    CORRIDOR,
    CORRIDOR_WITH_END_CIRCLE,
    BOX,
    CYLINDER;

    public static AttackTelegraphShape byId(int id) {
        AttackTelegraphShape[] values = values();
        return id >= 0 && id < values.length ? values[id] : NONE;
    }
}