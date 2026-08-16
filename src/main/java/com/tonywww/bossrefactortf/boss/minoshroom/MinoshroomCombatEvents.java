package com.tonywww.bossrefactortf.boss.minoshroom;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.entity.boss.Minoshroom;

@Mod.EventBusSubscriber(
        modid = BossRefactorTwilightForest.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MinoshroomCombatEvents {
    private MinoshroomCombatEvents() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Minoshroom minoshroom)
                || !MinoshroomCombat.customCombatEnabled()) {
            return;
        }
        Vec3 sourcePosition = event.getSource().getSourcePosition();
        if (sourcePosition == null) {
            return;
        }
        BossRefactorTwilightForestConfig.MinoshroomConfig config =
                BossRefactorTwilightForestConfig.MINOSHROOM;
        event.setAmount((float) MinoshroomCombatRules.reducedDamage(
                event.getAmount(),
                minoshroom.position().distanceTo(sourcePosition),
                config.rangedDamageReductionRadius.get(),
                config.rangedDamageReduction.get()));
    }
}