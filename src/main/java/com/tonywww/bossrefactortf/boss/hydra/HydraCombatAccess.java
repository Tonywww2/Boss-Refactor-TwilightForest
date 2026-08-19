package com.tonywww.bossrefactortf.boss.hydra;

import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;

import java.util.List;

public interface HydraCombatAccess {
    HydraCombatState bossRefactorTF$getHydraCombatState();

    void bossRefactorTF$activateNumberOfHeads(int desiredHeadCount);

    HydraSkill bossRefactorTF$getVisibleHydraSkill();

    int bossRefactorTF$getVisibleHydraHeadIndex();

    float bossRefactorTF$getVisibleHydraActionProgress();

        boolean bossRefactorTF$isVisibleHydraRecovering();

        float bossRefactorTF$getVisibleHydraRecoveryStartProgress();

    double bossRefactorTF$getVisibleHydraLockedX();

    double bossRefactorTF$getVisibleHydraLockedY();

    double bossRefactorTF$getVisibleHydraLockedZ();

    double bossRefactorTF$getVisibleHydraDirectionX();

    double bossRefactorTF$getVisibleHydraDirectionZ();

    void bossRefactorTF$setHydraVisualState(
            HydraSkill skill, int headIndex, float progress,
            boolean recovering, float recoveryStartProgress,
            double lockedX, double lockedY, double lockedZ,
            double directionX, double directionZ);

    void bossRefactorTF$setHydraTelegraphs(
            AttackTelegraph primary, List<AttackTelegraph> beams);
}