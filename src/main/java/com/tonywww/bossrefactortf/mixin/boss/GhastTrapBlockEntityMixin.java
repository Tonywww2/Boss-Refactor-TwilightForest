package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.urghast.UrGhastCombat;
import com.tonywww.bossrefactortf.boss.urghast.UrGhastCombatRules;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Ghast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
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

    @Group(name = "limitOffensivePull", min = 2, max = 2)
    @Redirect(
            method = "tickActive",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Ghast;setDeltaMovement(DDD)V"),
            require = 0,
            remap = false)
    private void bossRefactorTF$limitOffensivePullMcp(
            Ghast ghast, double x, double y, double z) {
        bossRefactorTF$limitOffensivePull(ghast, x, y, z);
    }

    @Group(name = "limitOffensivePull", min = 2, max = 2)
    @Redirect(
            method = "tickActive",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Ghast;m_20334_(DDD)V"),
            require = 0,
            remap = false)
    private void bossRefactorTF$limitOffensivePullSrg(
            Ghast ghast, double x, double y, double z) {
        bossRefactorTF$limitOffensivePull(ghast, x, y, z);
    }

    @Unique
    private void bossRefactorTF$limitOffensivePull(
            Ghast ghast, double x, double y, double z) {
        if (bossRefactorTF$trapAffects(ghast)) {
            ghast.setDeltaMovement(x, y, z);
        }
    }

    @Group(name = "limitOffensiveDamage", min = 2, max = 2)
    @Redirect(
            method = "tickActive",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Ghast;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            require = 0,
            remap = false)
    private boolean bossRefactorTF$limitOffensiveDamageMcp(
            Ghast ghast, DamageSource source, float amount) {
        return bossRefactorTF$limitOffensiveDamage(ghast, source, amount);
    }

    @Group(name = "limitOffensiveDamage", min = 2, max = 2)
    @Redirect(
            method = "tickActive",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Ghast;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            require = 0,
            remap = false)
    private boolean bossRefactorTF$limitOffensiveDamageSrg(
            Ghast ghast, DamageSource source, float amount) {
        return bossRefactorTF$limitOffensiveDamage(ghast, source, amount);
    }

    @Unique
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