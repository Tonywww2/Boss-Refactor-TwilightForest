package com.tonywww.bossrefactortf.boss.hydra;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import twilightforest.entity.boss.Hydra;

public final class HydraDamageTypes {
    public static final ResourceKey<DamageType> EVIL_FIRE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(
                    BossRefactorTwilightForest.MOD_ID,
                    "hydra_evil_fire"));

    private HydraDamageTypes() {
    }

    public static DamageSource evilFire(
            HydraEvilFireball fireball, Hydra hydra) {
        var damageType = hydra.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(EVIL_FIRE);
        return new DamageSource(damageType, fireball, hydra);
    }

        public static DamageSource evilFire(Hydra hydra) {
                var damageType = hydra.level().registryAccess()
                                .registryOrThrow(Registries.DAMAGE_TYPE)
                                .getHolderOrThrow(EVIL_FIRE);
                return new DamageSource(damageType, hydra, hydra);
        }
}