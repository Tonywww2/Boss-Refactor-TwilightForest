package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.urghast.UrGhastCombat;
import com.tonywww.bossrefactortf.boss.urghast.UrGhastCombatRules;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Ghast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.block.entity.GhastTrapBlockEntity;
import twilightforest.entity.boss.UrGhast;
import twilightforest.entity.monster.CarminiteGhastguard;

@Mixin(value = GhastTrapBlockEntity.class, remap = false)
public abstract class GhastTrapBlockEntityMixin {
    @Shadow
    private int counter;

    @Redirect(
            method = "tickActive",
            at = @At(
                    value = "INVOKE",
                    target = "Ltwilightforest/entity/boss/UrGhast;setInTantrum(Z)V"),
            remap = false)
    private void bossRefactorTF$preserveOffensiveTantrum(
            UrGhast ghast, boolean inTantrum) {
        if (!UrGhastCombat.isOffensive(ghast)) {
            ghast.setInTantrum(inTantrum);
        }
    }

    @Redirect(
            method = "tickActive",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Ghast;setDeltaMovement(DDD)V"),
            remap = false)
    private void bossRefactorTF$limitOffensivePull(
            Ghast ghast, double x, double y, double z) {
        if (bossRefactorTF$trapAffects(ghast)) {
            ghast.setDeltaMovement(x, y, z);
        }
    }

    @Redirect(
            method = "tickActive",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Ghast;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            remap = false)
    private boolean bossRefactorTF$limitOffensiveDamage(
            Ghast ghast, DamageSource source, float amount) {
        return bossRefactorTF$trapAffects(ghast)
                && ghast.hurt(source, amount);
    }

    @Redirect(
            method = "tickActive",
            at = @At(
                    value = "INVOKE",
                    target = "Ltwilightforest/entity/monster/CarminiteGhastguard;setInTrap()V"),
            remap = false)
    private void bossRefactorTF$limitOffensiveTargetSuppression(
            CarminiteGhastguard ghast) {
        if (bossRefactorTF$trapAffects(ghast)) {
            ghast.setInTrap();
        }
    }

    private boolean bossRefactorTF$trapAffects(Ghast ghast) {
        if (!(ghast instanceof UrGhast urGhast)
                || !UrGhastCombat.isOffensive(urGhast)) {
            return true;
        }
        return UrGhastCombatRules.trapAffectsOffensiveMode(
                counter, 80, 120,
                BossRefactorTwilightForestConfig.UR_GHAST
                        .offensiveTrapDurationMultiplier.get());
    }
}