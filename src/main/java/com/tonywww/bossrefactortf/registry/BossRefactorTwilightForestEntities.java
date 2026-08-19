package com.tonywww.bossrefactortf.registry;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.boss.hydra.HydraEvilFireball;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BossRefactorTwilightForestEntities {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(
                    ForgeRegistries.ENTITY_TYPES,
                    BossRefactorTwilightForest.MOD_ID);

    public static final RegistryObject<EntityType<HydraEvilFireball>>
            HYDRA_EVIL_FIREBALL = ENTITY_TYPES.register(
                    "hydra_evil_fireball",
                    () -> EntityType.Builder.<HydraEvilFireball>of(
                                    HydraEvilFireball::new,
                                    MobCategory.MISC)
                            .sized(1.5F, 1.5F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("hydra_evil_fireball"));

    private BossRefactorTwilightForestEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}