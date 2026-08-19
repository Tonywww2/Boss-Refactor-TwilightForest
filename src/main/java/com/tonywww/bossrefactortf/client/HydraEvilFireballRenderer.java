package com.tonywww.bossrefactortf.client;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.boss.hydra.HydraEvilFireball;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.registry.BossRefactorTwilightForestEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = BossRefactorTwilightForest.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class HydraEvilFireballRenderer {
    private HydraEvilFireballRenderer() {
    }

    @SubscribeEvent
    public static void registerRenderers(
            EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                BossRefactorTwilightForestEntities.HYDRA_EVIL_FIREBALL.get(),
                context -> new ThrownItemRenderer<HydraEvilFireball>(
                        context,
                        BossRefactorTwilightForestConfig.HYDRA
                                .evilFireballVisualScale.get().floatValue(),
                        true));
    }
}