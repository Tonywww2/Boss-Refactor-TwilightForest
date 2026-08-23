package com.tonywww.bossrefactortf.boss.hydra;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import twilightforest.entity.boss.Hydra;

public final class HydraDamageTypes {
    public static final ResourceKey<DamageType> EVIL_FIRE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(
                    BossRefactorTwilightForest.MOD_ID,
                    "hydra_evil_fire"));
    public static final ResourceKey<DamageType> PHYSICAL = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(
                    BossRefactorTwilightForest.MOD_ID,
                    "hydra_physical"));

    private HydraDamageTypes() {
    }

    public static DamageSource evilFire(Entity directEntity, Hydra hydra) {
        return source(EVIL_FIRE, directEntity, hydra);
    }

    public static DamageSource evilFire(Hydra hydra) {
        return evilFire(hydra, hydra);
    }

    public static DamageSource physical(Entity directEntity, Hydra hydra) {
        return source(PHYSICAL, directEntity, hydra);
    }

    public static DamageSource physical(Hydra hydra) {
        return physical(hydra, hydra);
    }

    public static boolean hurtMixedFire(
            Entity target, DamageSource physicalSource,
            DamageSource fireSource, float totalDamage,
            double physicalFraction) {
        float physicalDamage = HydraCombatRules.physicalFireDamage(
                totalDamage, physicalFraction);
        float fireDamage = HydraCombatRules.elementalFireDamage(
                totalDamage, physicalFraction);
        boolean physicalHit = physicalDamage > 0.0F
                && target.hurt(physicalSource, physicalDamage);
        int physicalInvulnerableTime = target instanceof LivingEntity living
                ? living.invulnerableTime
                : 0;
        if (physicalHit && target instanceof LivingEntity living) {
            living.invulnerableTime = 0;
        }
        boolean fireHit = fireDamage > 0.0F
                && target.hurt(fireSource, fireDamage);
        if (physicalHit && !fireHit && target instanceof LivingEntity living) {
            living.invulnerableTime = physicalInvulnerableTime;
        }
        return physicalHit || fireHit;
    }

    private static DamageSource source(
            ResourceKey<DamageType> key,
            Entity directEntity, Hydra hydra) {
        var damageType = hydra.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key);
        return new DamageSource(damageType, directEntity, hydra);
    }
}