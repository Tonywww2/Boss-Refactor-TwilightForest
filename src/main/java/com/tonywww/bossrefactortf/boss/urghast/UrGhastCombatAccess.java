package com.tonywww.bossrefactortf.boss.urghast;

import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;

public interface UrGhastCombatAccess {
    UrGhastCombatState bossRefactorTF$getUrGhastCombatState();

    void bossRefactorTF$setMistOriginTelegraph(AttackTelegraph telegraph);

    void bossRefactorTF$setMistDestinationTelegraph(AttackTelegraph telegraph);
}