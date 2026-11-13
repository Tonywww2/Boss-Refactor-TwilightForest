package com.tonywww.bossrefactortf.boss.minoshroom;

import net.minecraft.world.entity.ai.goal.Goal;
import twilightforest.entity.boss.Minoshroom;

import java.util.EnumSet;

public final class MinoshroomCombatGoal extends Goal {
    private final Minoshroom minoshroom;

    public MinoshroomCombatGoal(Minoshroom minoshroom) {
        this.minoshroom = minoshroom;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return MinoshroomCombat.customCombatEnabled()
                && MinoshroomCombat.hasCombatWork(minoshroom);
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
        MinoshroomCombat.startIfIdle(minoshroom);
    }

    @Override
    public void stop() {
        MinoshroomCombat.stopWhenUnusable(minoshroom);
    }

    @Override
    public void tick() {
        MinoshroomCombat.tick(minoshroom);
    }

    // 配置里的 tick 按游戏 tick 计算；默认目标只每 2 tick 执行一次。
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}