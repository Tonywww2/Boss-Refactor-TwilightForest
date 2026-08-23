package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.hydra.HydraCombat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.entity.boss.Hydra;
import twilightforest.entity.boss.HydraMortar;

@Mixin(value = HydraMortar.class, remap = false)
public abstract class HydraMortarMixin {
        @Group(name = "mixedMortarDamage", min = 1, max = 1)
        @Redirect(
            method = "detonate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            require = 0,
            remap = false)
    private boolean bossRefactorTF$applyMixedMortarDamageMcp(
            Entity target, DamageSource source, float amount) {
        return bossRefactorTF$applyMixedMortarDamage(target, source, amount);
    }

    @Group(name = "mixedMortarDamage", min = 1, max = 1)
    @Redirect(
            method = "detonate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            require = 0,
            remap = false)
    private boolean bossRefactorTF$applyMixedMortarDamageSrg(
            Entity target, DamageSource source, float amount) {
        return bossRefactorTF$applyMixedMortarDamage(target, source, amount);
    }

    private boolean bossRefactorTF$applyMixedMortarDamage(
            Entity target, DamageSource source, float amount) {
        HydraMortar mortar = (HydraMortar) (Object) this;
        return HydraCombat.customCombatEnabled()
                && mortar.getOwner() instanceof Hydra hydra
                ? HydraCombat.hurtWithMixedFire(target, mortar, hydra, amount)
                : target.hurt(source, amount);
    }
}