package com.tonywww.bossrefactortf.boss.naga;

import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;

public interface NagaCombatAccess {
    NagaCombatState bossRefactorTF$getNagaCombatState();

    NagaBodyState bossRefactorTF$getNagaBodyState();

    NagaBodyPose bossRefactorTF$getBodyPose();

    void bossRefactorTF$setBodyPose(NagaBodyPose pose);

    float bossRefactorTF$getBodyPoseProgress();

    void bossRefactorTF$setBodyPoseProgress(float progress);

    double bossRefactorTF$getBodyDirectionX();

    double bossRefactorTF$getBodyDirectionZ();

    void bossRefactorTF$setBodyDirection(double directionX, double directionZ);

    boolean bossRefactorTF$isStunImmunityVisible();

    void bossRefactorTF$setStunImmunityVisible(boolean visible);

    boolean bossRefactorTF$isFireSnakeVisualActive();

    void bossRefactorTF$setFireSnakeVisualActive(boolean active);

    void bossRefactorTF$setPrimaryTelegraph(AttackTelegraph telegraph);

    void bossRefactorTF$setSecondaryTelegraph(AttackTelegraph telegraph);
}