package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenCombat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.ai.goal.HoverBaseGoal;
import twilightforest.entity.ai.goal.HoverThenDropGoal;
import twilightforest.entity.boss.SnowQueen;

@Mixin(value = HoverThenDropGoal.class, remap = false)
public abstract class HoverThenDropGoalMixin
        extends HoverBaseGoal<SnowQueen> {
    @Shadow
    private int hoverTimer;
    @Shadow
    private int dropTimer;
    @Shadow
    @Final
    private int maxHoverTime;

    protected HoverThenDropGoalMixin(
            SnowQueen attacker, float hoverHeight, float hoverRadius) {
        super(attacker, hoverHeight, hoverRadius);
    }

        @Inject(
            method = {"tick", "m_8037_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$updateDropWarning(CallbackInfo callback) {
        SnowQueenCombat.updateOriginalDropTelegraph(
                attacker, hoverTimer, maxHoverTime, dropTimer > 0);
    }

    @Redirect(
            method = {"canContinueToUse", "m_8045_"},
            at = @At(
                    value = "INVOKE",
                    target = "Ltwilightforest/entity/boss/SnowQueen;incrementSuccessfulDrops()V"),
            remap = false)
    private void bossRefactorTF$completeDrop(SnowQueen queen) {
        SnowQueenCombat.completeOriginalDrop(queen);
        queen.incrementSuccessfulDrops();
    }

        @Inject(
            method = {"stop", "m_8041_"},
            at = @At("HEAD"),
            remap = false)
    private void bossRefactorTF$clearDropWarning(CallbackInfo callback) {
        SnowQueenCombat.clearOriginalDropTelegraph(attacker);
    }
}
