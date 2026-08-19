package com.tonywww.bossrefactortf.boss.naga;

public enum NagaActionState {
    IDLE("idle"),
    CRUISING("cruising"),
    CHARGE("charge"),
    POUNCE_WINDUP("pounce_windup"),
    POUNCE_FLIGHT("pounce_flight"),
    POUNCE_IMPACT("pounce_impact"),
    VENOM_WINDUP("venom_windup"),
    VENOM_PROJECTILE("venom_projectile"),
    STUNNED("stunned");

    private final String id;

    NagaActionState(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static NagaActionState byId(String id) {
        if (id.equals("tail_windup") || id.equals("tail_strike")) {
            return CRUISING;
        }
        for (NagaActionState state : values()) {
            if (state.id.equals(id)) {
                return state;
            }
        }
        return IDLE;
    }

    public static NagaActionState fromLegacyOrdinal(int ordinal) {
        return switch (ordinal) {
            case 1 -> CRUISING;
            case 2 -> POUNCE_WINDUP;
            case 3 -> POUNCE_FLIGHT;
            case 4 -> POUNCE_IMPACT;
            case 5, 6 -> CRUISING;
            case 7 -> VENOM_WINDUP;
            case 8 -> VENOM_PROJECTILE;
            case 9 -> STUNNED;
            default -> IDLE;
        };
    }
}