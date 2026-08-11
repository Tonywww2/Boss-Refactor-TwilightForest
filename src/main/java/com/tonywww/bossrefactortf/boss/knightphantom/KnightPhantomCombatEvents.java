package com.tonywww.bossrefactortf.boss.knightphantom;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.entity.boss.KnightPhantom;

@Mod.EventBusSubscriber(
        modid = BossRefactorTwilightForest.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class KnightPhantomCombatEvents {
    private KnightPhantomCombatEvents() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        BossRefactorTwilightForestConfig.KnightPhantomConfig config =
                BossRefactorTwilightForestConfig.KNIGHT_PHANTOM;
        if (!config.enabled.get()) {
            return;
        }

        float amount = event.getAmount();
        if (event.getEntity() instanceof KnightPhantom knight
                && knight instanceof KnightPhantomCombatAccess access) {
            boolean fireDamage = config.fireVulnerabilityEnabled.get()
                    && event.getSource().is(DamageTypeTags.IS_FIRE);
            boolean lastKnight = config.lastKnightEnabled.get()
                    && config.lastKnightDamageReductionEnabled.get()
                    && access.bossRefactorTF$isLastKnightPhaseActive();
            amount = KnightPhantomCombatRules.incomingDamage(
                    amount,
                    fireDamage,
                    config.fireDamageMultiplier.get(),
                    lastKnight,
                    config.lastKnightDamageReduction.get());
        }

        if (event.getSource().getEntity() instanceof KnightPhantom knight
                && knight instanceof KnightPhantomCombatAccess access
                && config.fallenKnightScalingEnabled.get()) {
            amount *= (float) KnightPhantomCombatRules.fallenKnightMultiplier(
                    access.bossRefactorTF$getFallenKnightCount(),
                    config.damageBonusPerFallenKnight.get());
        }
        event.setAmount(amount);
    }
}