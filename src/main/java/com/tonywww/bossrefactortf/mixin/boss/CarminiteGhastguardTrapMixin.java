package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.urghast.UrGhastCombat;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.boss.UrGhast;
import twilightforest.entity.monster.CarminiteGhastguard;

@Mixin(value = CarminiteGhastguard.class, remap = false)
public abstract class CarminiteGhastguardTrapMixin {
    @Shadow
    private int inTrapCounter;

    @Inject(method = "setInTrap", at = @At("TAIL"), remap = false)
    private void bossRefactorTF$halveOffensiveTrapTail(
            CallbackInfo callback) {
        if ((Object) this instanceof UrGhast ghast
                && UrGhastCombat.isOffensive(ghast)) {
            inTrapCounter = (int) Math.ceil(
                    10.0 * BossRefactorTwilightForestConfig.UR_GHAST
                            .offensiveTrapDurationMultiplier.get());
        }
    }
}