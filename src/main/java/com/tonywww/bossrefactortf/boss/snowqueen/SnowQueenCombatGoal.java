package com.tonywww.bossrefactortf.boss.snowqueen;

import net.minecraft.world.entity.ai.goal.Goal;
import twilightforest.entity.boss.SnowQueen;

import java.util.EnumSet;

public final class SnowQueenCombatGoal extends Goal {
    private final SnowQueen queen;

    public SnowQueenCombatGoal(SnowQueen queen) {
        this.queen = queen;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return SnowQueenCombat.customCombatEnabled()
                && SnowQueenCombat.hasCombatWork(queen);
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        SnowQueenCombat.startIfIdle(queen);
    }

    @Override
    public void stop() {
        SnowQueenCombat.stopWhenUnusable(queen);
    }

    @Override
    public void tick() {
        SnowQueenCombat.tick(queen);
    }
}