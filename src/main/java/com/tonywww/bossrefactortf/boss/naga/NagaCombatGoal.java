package com.tonywww.bossrefactortf.boss.naga;

import net.minecraft.world.entity.ai.goal.Goal;
import twilightforest.entity.boss.Naga;

import java.util.EnumSet;

public final class NagaCombatGoal extends Goal {
    private final Naga naga;

    public NagaCombatGoal(Naga naga) {
        this.naga = naga;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return NagaCombat.customCombatEnabled()
                && NagaCombat.hasCombatWork(naga);
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
        NagaCombat.startIfIdle(naga);
    }

    @Override
    public void stop() {
        NagaCombat.stopWhenUnusable(naga);
    }

    @Override
    public void tick() {
        NagaCombat.tick(naga);
    }

    // 配置里的 tick 按游戏 tick 计算；默认目标只每 2 tick 执行一次。
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}