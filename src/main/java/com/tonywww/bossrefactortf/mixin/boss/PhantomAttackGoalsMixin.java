package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.knightphantom.KnightPhantomCombat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.ai.goal.PhantomAttackStartGoal;
import twilightforest.entity.ai.goal.PhantomThrowWeaponGoal;
import twilightforest.entity.ai.goal.PhantomWatchAndAttackGoal;

@Mixin(
        value = {
            PhantomAttackStartGoal.class,
            PhantomThrowWeaponGoal.class,
            PhantomWatchAndAttackGoal.class
        },
        remap = false)
public abstract class PhantomAttackGoalsMixin {
    @Inject(
            method = {"canUse", "m_8036_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$disableVanillaCombatGoals(
            CallbackInfoReturnable<Boolean> callback) {
        if (KnightPhantomCombat.customChargesEnabled()) {
            callback.setReturnValue(false);
        }
    }
}