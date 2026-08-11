package com.tonywww.bossrefactortf.boss.alphayeti;

import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;

public interface AlphaYetiCombatAccess {
    AlphaYetiCombatState bossRefactorTF$getAlphaYetiCombatState();

    int bossRefactorTF$getVisibleFatigue();

    void bossRefactorTF$setVisibleFatigue(int fatigue);

    int bossRefactorTF$getVisibleImbalance();

    void bossRefactorTF$setVisibleImbalance(int imbalance);

    boolean bossRefactorTF$isPursuing();

    void bossRefactorTF$setPursuing(boolean pursuing);

    void bossRefactorTF$setPrimaryTelegraph(AttackTelegraph telegraph);

    void bossRefactorTF$setSecondaryTelegraph(AttackTelegraph telegraph);
}