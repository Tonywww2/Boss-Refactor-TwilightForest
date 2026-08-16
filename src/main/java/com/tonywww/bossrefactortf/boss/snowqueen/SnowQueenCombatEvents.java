package com.tonywww.bossrefactortf.boss.snowqueen;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.entity.boss.SnowQueen;
import twilightforest.entity.monster.BaseIceMob;
import twilightforest.entity.monster.StableIceCore;
import twilightforest.entity.monster.UnstableIceCore;

import java.util.Objects;

@Mod.EventBusSubscriber(
        modid = BossRefactorTwilightForest.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SnowQueenCombatEvents {
    private SnowQueenCombatEvents() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        BossRefactorTwilightForestConfig.SnowQueenConfig config =
                BossRefactorTwilightForestConfig.SNOW_QUEEN;
        if (!config.enabled.get()) {
            return;
        }

        if (event.getSource().getEntity()
                instanceof SnowQueenSummonAccess summon
                && isSameOwnerTarget(summon, event.getEntity())) {
            event.setCanceled(true);
            return;
        }

        float amount = event.getAmount();
        if (event.getEntity() instanceof SnowQueen queen
                && SnowQueenCombat.hasLivingLink(queen)) {
            amount *= (float) (1.0 - config.linkQueenDamageReduction.get());
        }
        if (event.getSource().getEntity()
                instanceof SnowQueenSummonAccess summon
                && summon.bossRefactorTF$isSnowQueenLinked()) {
            amount *= (float) (1.0 + config.linkSummonDamageBonus.get());
        }
        event.setAmount(amount);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof SnowQueen queen) {
            SnowQueenCombat.cleanupAfterOwnerDeath(queen);
            return;
        }
        if ((event.getEntity() instanceof StableIceCore
                || event.getEntity() instanceof UnstableIceCore)
                && event.getEntity() instanceof BaseIceMob summon
                && event.getEntity() instanceof SnowQueenSummonAccess access
                && access.bossRefactorTF$getSnowQueenOwnerUuid() != null) {
            SnowQueenCombat.onSummonDeath(summon);
        }
    }

    private static boolean isSameOwnerTarget(
            SnowQueenSummonAccess summon, Object target) {
        if (summon.bossRefactorTF$getSnowQueenOwnerUuid() == null) {
            return false;
        }
        if (target instanceof SnowQueen queen) {
            return Objects.equals(
                    summon.bossRefactorTF$getSnowQueenOwnerUuid(),
                    queen.getUUID());
        }
        return target instanceof SnowQueenSummonAccess other
                && Objects.equals(
                        summon.bossRefactorTF$getSnowQueenOwnerUuid(),
                        other.bossRefactorTF$getSnowQueenOwnerUuid());
    }
}