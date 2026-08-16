package com.tonywww.bossrefactortf.boss.minoshroom;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import twilightforest.entity.boss.Minoshroom;

public final class MinoshroomDamageTypes {
    public static final ResourceKey<DamageType> STOMP = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(
                    BossRefactorTwilightForest.MOD_ID,
                    MinoshroomCombatRules.stompDamageTypePath()));

    private MinoshroomDamageTypes() {
    }

    public static DamageSource stomp(Minoshroom minoshroom) {
        var damageType = minoshroom.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(STOMP);
        return new DamageSource(damageType, minoshroom, minoshroom);
    }
}