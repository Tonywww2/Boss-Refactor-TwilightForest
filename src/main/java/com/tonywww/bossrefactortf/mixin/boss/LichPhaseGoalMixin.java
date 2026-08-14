package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.lich.LichCombat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.ai.goal.LichAbsorbMinionsGoal;
import twilightforest.entity.ai.goal.LichMinionsGoal;
import twilightforest.entity.ai.goal.LichPopMobsGoal;
import twilightforest.entity.boss.Lich;

@Mixin(
        value = {
                LichMinionsGoal.class,
                LichAbsorbMinionsGoal.class,
                LichPopMobsGoal.class
        },
        remap = false)
public abstract class LichPhaseGoalMixin {
        @Shadow(remap = false)
    @Final
    private Lich lich;

    @Inject(
            method = {"canUse", "m_8036_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$disableForCustomSecondPhase(
            CallbackInfoReturnable<Boolean> callback) {
        if (LichCombat.shouldSuppressVanillaPhaseLogic(lich)) {
            callback.setReturnValue(false);
        }
    }
}