package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenCombat;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.ai.goal.HoverBaseGoal;
import twilightforest.entity.ai.goal.HoverThenDropGoal;
import twilightforest.entity.boss.SnowQueen;

@Mixin(value = HoverBaseGoal.class, remap = false)
public abstract class HoverBaseGoalMixin<T extends Mob> {
    @Shadow
    @Final
    protected T attacker;
    @Shadow
    @Final
    protected float hoverHeight;
    @Shadow
    protected double hoverPosX;
    @Shadow
    protected double hoverPosY;
    @Shadow
    protected double hoverPosZ;

        @Inject(
            method = {"start", "m_8056_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$beginDropWarning(CallbackInfo callback) {
        if ((Object) this instanceof HoverThenDropGoal
                && attacker instanceof SnowQueen queen) {
            SnowQueenCombat.beginOriginalDropTelegraph(
                    queen,
                    hoverPosX,
                    hoverPosY - hoverHeight,
                    hoverPosZ);
        }
    }
}
