package com.tonywww.bossrefactortf.boss;

public enum TwilightBossLeaderProfilePolicy {
    EXTERNAL_ONLY {
        @Override
        public boolean accepts(TwilightBossLeaderProfile profile) {
            return profile == TwilightBossLeaderProfile.EXTERNAL;
        }
    },
    MANAGED_OR_EXTERNAL {
        @Override
        public boolean accepts(TwilightBossLeaderProfile profile) {
            return profile == TwilightBossLeaderProfile.MANAGED
                    || profile == TwilightBossLeaderProfile.EXTERNAL;
        }
    };

    public abstract boolean accepts(TwilightBossLeaderProfile profile);
}