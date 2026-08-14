package com.tonywww.bossrefactortf.boss.lich;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;

final class LichCombatConfig {
    private LichCombatConfig() {
    }

    static boolean enabled() {
        return BossRefactorTwilightForestConfig.LICH.enabled.get();
    }

    static int secondPhaseShieldLayers() {
        return BossRefactorTwilightForestConfig.LICH
                .secondPhaseShieldLayers.get();
    }

    static double shieldDamageReductionPerLayer() {
        return BossRefactorTwilightForestConfig.LICH
                .shieldDamageReductionPerLayer.get();
    }

    static double meleeMagicDamageMultiplier() {
        return BossRefactorTwilightForestConfig.LICH
                .meleeMagicDamageMultiplier.get();
    }

    static int meleeCooldownTicks() {
        return BossRefactorTwilightForestConfig.LICH
                .meleeCooldownTicks.get();
    }

    static int shieldBashWindupTicks() {
        return BossRefactorTwilightForestConfig.LICH
                .shieldBashWindupTicks.get();
    }

    static int shieldBashCooldownTicks() {
        return BossRefactorTwilightForestConfig.LICH
                .shieldBashCooldownTicks.get();
    }

    static double shieldBashLength() {
        return BossRefactorTwilightForestConfig.LICH
                .shieldBashLength.get();
    }

    static double shieldBashHalfWidth() {
        return BossRefactorTwilightForestConfig.LICH
                .shieldBashHalfWidth.get();
    }

    static double shieldBashHeight() {
        return BossRefactorTwilightForestConfig.LICH
                .shieldBashHeight.get();
    }

    static double shieldBashDamageMultiplier() {
        return BossRefactorTwilightForestConfig.LICH
                .shieldBashDamageMultiplier.get();
    }

    static double shieldBashShieldMultiplier() {
        return BossRefactorTwilightForestConfig.LICH
                .shieldBashShieldMultiplier.get();
    }

    static double shieldBashHorizontalKnockback() {
        return BossRefactorTwilightForestConfig.LICH
                .shieldBashHorizontalKnockback.get();
    }

    static double shieldBashVerticalVelocity() {
        return BossRefactorTwilightForestConfig.LICH
                .shieldBashVerticalVelocity.get();
    }

    static int shieldBashStunTicks() {
        return BossRefactorTwilightForestConfig.LICH
                .shieldBashStunTicks.get();
    }

    static int shieldBashParryFeedbackTicks() {
        return BossRefactorTwilightForestConfig.LICH
                .shieldBashParryFeedbackTicks.get();
    }

    static int summonWindupTicks() {
        return BossRefactorTwilightForestConfig.LICH
                .summonWindupTicks.get();
    }

    static int summonCooldownTicks() {
        return BossRefactorTwilightForestConfig.LICH
                .summonCooldownTicks.get();
    }

    static int cloneLifetimeTicks() {
        return BossRefactorTwilightForestConfig.LICH
                .cloneLifetimeTicks.get();
    }

    static int cruiseMinimumTicks() {
        return BossRefactorTwilightForestConfig.LICH
                .cruiseMinimumTicks.get();
    }

    static int cruiseMaximumTicks() {
        return BossRefactorTwilightForestConfig.LICH
                .cruiseMaximumTicks.get();
    }

    static double meleeSpeed() {
        return BossRefactorTwilightForestConfig.LICH.meleeSpeed.get();
    }
}