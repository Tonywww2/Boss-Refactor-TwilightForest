package com.tonywww.bossrefactortf.boss.lich;

import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;

public interface LichCombatAccess {
    LichCombatState bossRefactorTF$getLichCombatState();

    void bossRefactorTF$setPrimaryTelegraph(AttackTelegraph telegraph);
}