package com.tonywww.bossrefactortf.boss.snowqueen;

import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;

public interface SnowQueenCombatAccess {
    SnowQueenCombatState bossRefactorTF$getSnowQueenCombatState();

    boolean bossRefactorTF$isSnowQueenCustomCombatActive();

    void bossRefactorTF$setSnowQueenCustomCombatActive(boolean active);

    int bossRefactorTF$getSnowQueenActiveShieldCount();

    void bossRefactorTF$setSnowQueenActiveShieldCount(int count);

    double bossRefactorTF$getSnowQueenShieldOrbitRadius();

    void bossRefactorTF$setSnowQueenShieldOrbitRadius(double radius);

    double bossRefactorTF$getSnowQueenShieldAngularSpeed();

    void bossRefactorTF$setSnowQueenShieldAngularSpeed(double speed);

    double bossRefactorTF$getSnowQueenShieldVerticalOffset();

    void bossRefactorTF$setSnowQueenShieldVerticalOffset(double offset);

    double bossRefactorTF$getSnowQueenShieldWaveAmplitude();

    void bossRefactorTF$setSnowQueenShieldWaveAmplitude(double amplitude);

    double bossRefactorTF$getSnowQueenShieldWaveSpeed();

    void bossRefactorTF$setSnowQueenShieldWaveSpeed(double speed);

    boolean bossRefactorTF$isSnowQueenSecondPhase();

    void bossRefactorTF$setSnowQueenSecondPhase(boolean secondPhase);

    void bossRefactorTF$setSnowQueenTelegraph(AttackTelegraph telegraph);

    void bossRefactorTF$setSnowQueenDropTelegraph(AttackTelegraph telegraph);
}