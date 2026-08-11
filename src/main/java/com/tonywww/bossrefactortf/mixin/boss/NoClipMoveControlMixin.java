package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.knightphantom.KnightPhantomCombat;
import com.tonywww.bossrefactortf.boss.knightphantom.KnightPhantomCombatAccess;
import com.tonywww.bossrefactortf.boss.knightphantom.KnightPhantomCombatRules;
import com.tonywww.bossrefactortf.boss.urghast.UrGhastCombat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.ai.control.NoClipMoveControl;
import twilightforest.entity.boss.KnightPhantom;
import twilightforest.entity.boss.UrGhast;

@Mixin(value = NoClipMoveControl.class, remap = false)
public abstract class NoClipMoveControlMixin extends MoveControl {
    @Shadow
    @Final
    private LivingEntity parentEntity;

    protected NoClipMoveControlMixin(Mob mob) {
        super(mob);
    }

    @Inject(
            method = {"tick", "m_8126_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$holdDuringChargeWindup(CallbackInfo callback) {
        if (parentEntity instanceof UrGhast ghast
                && UrGhastCombat.shouldHoldPosition(ghast)) {
            operation = Operation.WAIT;
            ghast.setDeltaMovement(0.0, 0.0, 0.0);
            callback.cancel();
            return;
        }
        if (!(parentEntity instanceof KnightPhantom knight)
                || !(knight instanceof KnightPhantomCombatAccess access)) {
            return;
        }
        if (KnightPhantomCombat.customChargesEnabled()
                && operation == Operation.MOVE_TO
                && !KnightPhantomCombatRules.shouldMoveToDestination(
                        knight.getX(), knight.getY(), knight.getZ(),
                        wantedX, wantedY, wantedZ)) {
            operation = Operation.WAIT;
            knight.setDeltaMovement(0.0, 0.0, 0.0);
            callback.cancel();
            return;
        }
        if (KnightPhantomCombat.isPossessionOwner(knight)
                && (access.bossRefactorTF$isParryStunActive()
                    || knight.getCurrentFormation()
                    == KnightPhantom.Formation.ATTACK_PLAYER_START
                    || KnightPhantomCombat.shouldHoldPostChargePossession(knight))) {
            knight.setDeltaMovement(0.0, 0.0, 0.0);
            callback.cancel();
        }
    }

    @ModifyConstant(
            method = {"tick", "m_8126_"},
            constant = @Constant(doubleValue = 0.1D),
            remap = false)
    private double bossRefactorTF$adjustKnightPhantomAcceleration(double original) {
        if (!(parentEntity instanceof KnightPhantom knight)
                || speedModifier == 0.0D) {
            return original;
        }
        double adjustedSpeed = KnightPhantomCombat.adjustedMoveSpeed(
                knight, speedModifier);
        return original * adjustedSpeed / speedModifier;
    }
}