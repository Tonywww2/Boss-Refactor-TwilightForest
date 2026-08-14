package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.hydra.HydraCombat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.boss.Hydra;
import twilightforest.entity.boss.HydraHeadContainer;

@Mixin(value = HydraHeadContainer.class, remap = false)
public abstract class HydraHeadContainerMixin {
    @Shadow
    @Final
    private Hydra hydra;

    @Inject(
            method = "executeAttacks",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$disableVanillaAttackExecution(
            CallbackInfo callback) {
        if (HydraCombat.customCombatEnabled()) {
            callback.cancel();
        }
    }
}