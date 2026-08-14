package com.tonywww.bossrefactortf.boss.naga;

import net.minecraft.world.entity.ai.goal.Goal;
import twilightforest.entity.boss.Naga;

public final class NagaStunAwareGoal extends Goal {
    private final Naga naga;
    private final Goal delegate;

    public NagaStunAwareGoal(Naga naga, Goal delegate) {
        this.naga = naga;
        this.delegate = delegate;
        setFlags(delegate.getFlags());
    }

    @Override
    public boolean canUse() {
        return !NagaCombat.isStunned(naga) && delegate.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !NagaCombat.isStunned(naga) && delegate.canContinueToUse();
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
        if (!NagaCombat.isStunned(naga)) {
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