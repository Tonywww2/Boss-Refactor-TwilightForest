package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.knightphantom.KnightPhantomCombat;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.boss.KnightPhantom;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class KnightPhantomNearestTargetGoalMixin extends TargetGoal {
    protected KnightPhantomNearestTargetGoalMixin(
            Mob mob, boolean mustSee) {
        super(mob, mustSee);
    }

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void bossRefactorTF$disableVanillaTargetSelection(
            CallbackInfoReturnable<Boolean> callback) {
        if (mob instanceof KnightPhantom
                && KnightPhantomCombat.customChargesEnabled()) {
            callback.setReturnValue(false);
        }
    }
}