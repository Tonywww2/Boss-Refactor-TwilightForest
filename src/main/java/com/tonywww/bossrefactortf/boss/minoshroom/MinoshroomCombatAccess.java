package com.tonywww.bossrefactortf.boss.minoshroom;

import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;

public interface MinoshroomCombatAccess {
    MinoshroomCombatState bossRefactorTF$getMinoshroomCombatState();

    boolean bossRefactorTF$isSecondPhase();

    void bossRefactorTF$setSecondPhase(boolean secondPhase);

    void bossRefactorTF$setMinoshroomTelegraph(AttackTelegraph telegraph);
}