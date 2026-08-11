package com.tonywww.bossrefactortf.boss.urghast;

import net.minecraft.world.entity.ai.goal.Goal;
import twilightforest.entity.boss.UrGhast;

import java.util.EnumSet;

public final class UrGhastCombatGoal extends Goal {
    private final UrGhast ghast;

    public UrGhastCombatGoal(UrGhast ghast) {
        this.ghast = ghast;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return UrGhastCombat.customCombatEnabled() && ghast.isAlive();
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
        UrGhastCombat.restoreVisualState(ghast);
    }

    @Override
    public void stop() {
        UrGhastCombat.stop(ghast);
    }

    @Override
    public void tick() {
        UrGhastCombat.tick(ghast);
    }
}