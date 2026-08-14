package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.lich.LichCombat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.ai.goal.LichShadowsGoal;
import twilightforest.entity.boss.Lich;

@Mixin(value = LichShadowsGoal.class, remap = false)
public abstract class LichShadowsGoalMixin {
    @Shadow(remap = false)
    @Final
    private Lich lich;

    @Inject(
            method = {"canUse", "m_8036_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$disableForSecondPhaseMaster(
            CallbackInfoReturnable<Boolean> callback) {
        if (LichCombat.shouldSuppressVanillaPhaseLogic(lich)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(
            method = "spawnShadowClone",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$disableVanillaCloneSpawn(
            CallbackInfo callback) {
        if (LichCombat.customCombatEnabled()) {
            callback.cancel();
        }
    }

    @Inject(
            method = {"tick", "m_8037_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$pauseCloneRangedAttack(
            CallbackInfo callback) {
        if (LichCombat.shouldSuppressVanillaCloneAttack(lich)) {
            callback.cancel();
        }
    }

    @Inject(
            method = {"stop", "m_8041_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$preserveFinishingClone(
            CallbackInfo callback) {
        if (LichCombat.shouldSuppressVanillaCloneAttack(lich)) {
            callback.cancel();
        }
    }
}