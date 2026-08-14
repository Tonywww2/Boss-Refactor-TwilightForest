package com.tonywww.bossrefactortf.boss.lich;

import net.minecraft.world.entity.ai.goal.Goal;
import twilightforest.entity.boss.Lich;

import java.util.EnumSet;

public final class LichCombatGoal extends Goal {
    private final Lich lich;

    public LichCombatGoal(Lich lich) {
        this.lich = lich;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return LichCombat.customCombatEnabled()
                && LichCombat.hasCombatWork(lich)
                && LichCombat.requiresExclusiveControl(lich);
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
        LichCombat.restoreVisualState(lich);
    }

    @Override
    public void stop() {
        LichCombat.stopWhenUnusable(lich);
    }

    @Override
    public void tick() {
        LichCombat.tick(lich);
    }
}