package com.tonywww.bossrefactortf;

import com.mojang.logging.LogUtils;
import com.tonywww.bossrefactortf.boss.TwilightBossParryIntegration;
import com.tonywww.bossrefactortf.compat.OptionalCompatBootstrap;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.registry.BossRefactorTwilightForestEntities;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import twilightforest.init.TFEntities;

@Mod(BossRefactorTwilightForest.MOD_ID)
public final class BossRefactorTwilightForest {
    public static final String MOD_ID = "bossrefactortf";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal")
    public BossRefactorTwilightForest(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        BossRefactorTwilightForestEntities.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                BossRefactorTwilightForestConfig.COMMON_SPEC);
        LOGGER.info("{} loading", MOD_ID);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            TwilightBossParryIntegration.declareExternalLeaderType(
                    TFEntities.KNIGHT_PHANTOM.get());
            TwilightBossParryIntegration.declareExternalLeaderType(
                TFEntities.ALPHA_YETI.get());
            TwilightBossParryIntegration.declareExternalLeaderType(
                    TFEntities.NAGA.get());
            TwilightBossParryIntegration.declareExternalLeaderType(
                    TFEntities.SNOW_QUEEN.get());
                TwilightBossParryIntegration.declareExternalLeaderType(
                    TFEntities.HYDRA.get());
                TwilightBossParryIntegration.declareExternalLeaderType(
                    TFEntities.LICH.get());
                    TwilightBossParryIntegration.declareManagedOrExternalLeaderType(
                        TFEntities.MINOSHROOM.get());
            OptionalCompatBootstrap.initialize();
        });
    }
}