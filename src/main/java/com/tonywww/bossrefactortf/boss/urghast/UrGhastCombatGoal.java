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

    // 配置里的 tick 按游戏 tick 计算；默认目标只每 2 tick 执行一次。
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}