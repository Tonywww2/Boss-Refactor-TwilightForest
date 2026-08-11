package com.tonywww.bossrefactortf.boss.alphayeti;

import net.minecraft.world.entity.ai.goal.Goal;
import twilightforest.entity.boss.AlphaYeti;

import java.util.EnumSet;

public final class AlphaYetiCombatGoal extends Goal {
    private final AlphaYeti yeti;

    public AlphaYetiCombatGoal(AlphaYeti yeti) {
        this.yeti = yeti;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return AlphaYetiCombat.customCombatEnabled()
                && AlphaYetiCombat.hasCombatWork(yeti);
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
        AlphaYetiCombat.startIfIdle(yeti);
    }

    @Override
    public void stop() {
        AlphaYetiCombat.stopWhenUnusable(yeti);
    }

    @Override
    public void tick() {
        AlphaYetiCombat.tick(yeti);
    }
}