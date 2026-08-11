package com.tonywww.bossrefactortf.telegraph;

import java.util.List;

public interface AttackTelegraphAccess {
    AttackTelegraph bossRefactorTF$getAttackTelegraph();

    void bossRefactorTF$setAttackTelegraph(AttackTelegraph telegraph);

    default List<AttackTelegraph> bossRefactorTF$getAttackTelegraphs() {
        AttackTelegraph telegraph = bossRefactorTF$getAttackTelegraph();
        return telegraph.shape() == AttackTelegraphShape.NONE
                ? List.of()
                : List.of(telegraph);
    }
}