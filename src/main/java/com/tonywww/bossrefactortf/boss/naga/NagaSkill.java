package com.tonywww.bossrefactortf.boss.naga;

public enum NagaSkill {
    NONE("none"),
    CHARGE("charge"),
    POUNCE("pounce"),
    VENOM_BREATH("venom_breath");

    private final String id;

    NagaSkill(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static NagaSkill byId(String id) {
        if (id.equals("tail_sweep")) {
            return NONE;
        }
        for (NagaSkill skill : values()) {
            if (skill.id.equals(id)) {
                return skill;
            }
        }
        return NONE;
    }

    public static NagaSkill fromLegacyOrdinal(int ordinal) {
        return switch (ordinal) {
            case 1 -> POUNCE;
            case 3 -> VENOM_BREATH;
            default -> NONE;
        };
    }
}