package com.tonywww.bossrefactortf.boss.naga;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = BossRefactorTwilightForest.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NagaAreaEffectEvents {
    private NagaAreaEffectEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
            || !(event.level instanceof ServerLevel level)) {
            return;
        }
        NagaAreaEffectData.get(level).tick(level);
    }
}