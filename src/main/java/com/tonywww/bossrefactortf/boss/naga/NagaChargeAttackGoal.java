package com.tonywww.bossrefactortf.boss.naga;

import net.minecraft.world.entity.ai.goal.Goal;
import twilightforest.entity.boss.Naga;

public final class NagaChargeAttackGoal extends Goal {
    private final Naga naga;
    private final Goal delegate;

    public NagaChargeAttackGoal(Naga naga, Goal delegate) {
        this.naga = naga;
        this.delegate = delegate;
        setFlags(delegate.getFlags());
    }

    @Override
    public boolean canUse() {
        return NagaCombat.isCharging(naga) && delegate.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return NagaCombat.isCharging(naga) && delegate.canContinueToUse();
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public void tick() {
        if (NagaCombat.isCharging(naga)) {
            delegate.tick();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return delegate.requiresUpdateEveryTick();
    }

    @Override
    public boolean isInterruptable() {
        return delegate.isInterruptable();
    }
}