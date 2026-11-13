package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.knightphantom.KnightPhantomCombat;
import com.tonywww.bossrefactortf.boss.knightphantom.KnightPhantomCombatAccess;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.ai.goal.PhantomUpdateFormationAndMoveGoal;
import twilightforest.entity.boss.KnightPhantom;

import java.util.List;

@Mixin(value = PhantomUpdateFormationAndMoveGoal.class, remap = false)
public abstract class PhantomUpdateFormationAndMoveGoalMixin {
    @Shadow
    @Final
    private KnightPhantom boss;

    @Inject(
            method = {"canUse", "m_8036_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$disableVanillaFormationGoal(
            CallbackInfoReturnable<Boolean> callback) {
        if (KnightPhantomCombat.customChargesEnabled()) {
            callback.setReturnValue(false);
        }
    }

    @Inject(
            method = {"tick", "m_8037_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$runCustomFormationTick(
            CallbackInfo callback) {
        if (KnightPhantomCombat.customChargesEnabled()) {
            callback.cancel();
        }
    }

    @Inject(
            method = "makeARandomKnightCharge",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$startSequentialChargeRound(
            List<KnightPhantom> nearbyKnights, CallbackInfo callback) {
        if (KnightPhantomCombat.replaceRandomChargeSelection(boss, nearbyKnights)) {
            callback.cancel();
        }
    }

    @Inject(
            method = "switchToNextFormation",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$advanceSequentialCharge(CallbackInfo callback) {
        if (KnightPhantomCombat.advanceCustomChargeFormation(boss)) {
            callback.cancel();
        }
    }

    @Inject(
            method = "getAttackPlayerPosition",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$makeEveryKnightCharge(
            CallbackInfoReturnable<Vec3> callback) {
        if (KnightPhantomCombat.customChargesEnabled()
                && boss instanceof KnightPhantomCombatAccess access
            && KnightPhantomCombat.isPossessionOwner(boss)
                && boss.getCurrentFormation()
                == KnightPhantom.Formation.ATTACK_PLAYER_ATTACK) {
            callback.setReturnValue(access.bossRefactorTF$isPostChargePossessionActive()
                    ? KnightPhantomCombat.postChargePossessionDestination(boss)
                    : Vec3.atLowerCornerOf(boss.getChargePos()));
        }
    }

    @Inject(
            method = "getDestination",
            at = @At("RETURN"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$avoidPlayerInsideArena(
            CallbackInfoReturnable<Vec3> callback) {
        callback.setReturnValue(KnightPhantomCombat.avoidPlayerWithinArena(
                boss, KnightPhantomCombat.clampToFlightCeiling(
                        boss, callback.getReturnValue())));
    }
}