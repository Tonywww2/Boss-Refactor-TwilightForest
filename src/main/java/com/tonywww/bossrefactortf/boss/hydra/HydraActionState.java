package com.tonywww.bossrefactortf.boss.hydra;

public enum HydraActionState {
    IDLE(0),
    WINDUP(1),
    RECOVERY(3),
    FIRE_LORD(4),
    EVIL_FIREBALL_WINDUP(6);

    private final int serializedId;

    HydraActionState(int serializedId) {
        this.serializedId = serializedId;
    }

    public int serializedId() {
        return serializedId;
    }

    public static HydraActionState byId(int id) {
        return switch (id) {
            case 1 -> WINDUP;
            case 2, 3 -> RECOVERY;
            case 4 -> FIRE_LORD;
            case 5 -> RECOVERY;
            case 6 -> EVIL_FIREBALL_WINDUP;
            default -> IDLE;
        };
    }
}