package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.naga.NagaMovementPatternAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import twilightforest.entity.ai.goal.NagaMovementPattern;

@Mixin(value = NagaMovementPattern.class, remap = false)
public interface NagaMovementPatternAccessMixin
        extends NagaMovementPatternAccess {
    @Override
    @Invoker("doCharge")
    void bossRefactorTF$doCharge(boolean stunless);
}