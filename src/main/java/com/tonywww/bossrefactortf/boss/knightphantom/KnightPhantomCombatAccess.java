package com.tonywww.bossrefactortf.boss.knightphantom;

import twilightforest.entity.boss.KnightPhantom;

import javax.annotation.Nullable;
import java.util.UUID;

public interface KnightPhantomCombatAccess {
    @Nullable
    UUID bossRefactorTF$getEncounterId();

    void bossRefactorTF$setEncounterId(@Nullable UUID encounterId);

    boolean bossRefactorTF$isQueuedForChargeRound();

    void bossRefactorTF$setQueuedForChargeRound(boolean queued);

    boolean bossRefactorTF$isChargeRoundActive();

    void bossRefactorTF$setPossessionVisible(boolean visible);

    void bossRefactorTF$beginChargeRound(int segmentCount);

        void bossRefactorTF$switchToFormation(
            KnightPhantom.Formation formation);

    long bossRefactorTF$getChargeReadyGameTime();

    void bossRefactorTF$delayChargeUntil(long readyGameTime);

    boolean bossRefactorTF$isParryStunActive();

    long bossRefactorTF$getParryStunEndGameTime();

    void bossRefactorTF$beginParryStun(long endGameTime);

    void bossRefactorTF$finishParryStun();

    boolean bossRefactorTF$isPostChargePossessionActive();

    void bossRefactorTF$beginPostChargePossession();

    boolean bossRefactorTF$consumeAdditionalChargeSegment();

    void bossRefactorTF$finishChargeRound();

    void bossRefactorTF$cancelRemainingChargeSegments();

    void bossRefactorTF$clearChargeSegmentHits();

    boolean bossRefactorTF$markChargeSegmentHit(UUID targetId);

    int bossRefactorTF$getFallenKnightCount();

    boolean bossRefactorTF$isLastKnightPhaseActive();

    void bossRefactorTF$activateLastKnightPhase();
}