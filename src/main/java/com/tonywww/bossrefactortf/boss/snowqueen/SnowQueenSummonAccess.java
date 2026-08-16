package com.tonywww.bossrefactortf.boss.snowqueen;

import javax.annotation.Nullable;
import java.util.UUID;

public interface SnowQueenSummonAccess {
    int ROLE_NONE = 0;
    int ROLE_RAIN = 1;
    int ROLE_PHASE_TWO_STABLE = 2;

    int STATE_IDLE = 0;
    int STATE_WINDUP = 1;
    int STATE_DASHING = 2;
    int STATE_DYING = 3;

    @Nullable
    UUID bossRefactorTF$getSnowQueenOwnerUuid();

    void bossRefactorTF$setSnowQueenOwnerUuid(@Nullable UUID ownerUuid);

    int bossRefactorTF$getSnowQueenOwnerEntityId();

    void bossRefactorTF$setSnowQueenOwnerEntityId(int ownerEntityId);

    boolean bossRefactorTF$isSnowQueenLinked();

    void bossRefactorTF$setSnowQueenLinked(boolean linked);

    int bossRefactorTF$getSnowQueenSummonRole();

    void bossRefactorTF$setSnowQueenSummonRole(int role);

    int bossRefactorTF$getSnowQueenSummonState();

    void bossRefactorTF$setSnowQueenSummonState(int state);

    int bossRefactorTF$getSnowQueenSummonTicks();

    void bossRefactorTF$setSnowQueenSummonTicks(int ticks);

    int bossRefactorTF$getSnowQueenSummonDuration();

    void bossRefactorTF$setSnowQueenSummonDuration(int duration);

    double bossRefactorTF$getSnowQueenDashX();

    double bossRefactorTF$getSnowQueenDashY();

    double bossRefactorTF$getSnowQueenDashZ();

    void bossRefactorTF$setSnowQueenDash(double x, double y, double z);

    boolean bossRefactorTF$hasReportedSnowQueenDeath();

    void bossRefactorTF$setReportedSnowQueenDeath(boolean reported);

    default boolean bossRefactorTF$isOwnedBy(UUID ownerUuid) {
        return ownerUuid.equals(bossRefactorTF$getSnowQueenOwnerUuid());
    }
}