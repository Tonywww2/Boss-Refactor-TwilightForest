package com.tonywww.bossrefactortf.boss;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = BossRefactorTwilightForest.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TwilightBossSenDimsEvents {
    private static final UUID EXTERNAL_LEADER_HEALTH_MODIFIER_ID = UUID.fromString(
            "0aa462d1-55a5-43ad-93ae-d2819faac415");
    private static final String EXTERNAL_LEADER_HEALTH_MODIFIER_NAME =
            "bossrefactortf.sendims_external_leader_health";

    private TwilightBossSenDimsEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)
                || !(event.getEntity() instanceof LivingEntity boss)
                || !(boss instanceof TwilightBossParryAccess)) {
            return;
        }
        TwilightBossLeaderProfilePolicy policy =
                TwilightBossParryIntegration.leaderProfilePolicy(boss.getType());
        if (policy == TwilightBossLeaderProfilePolicy.MANAGED_OR_EXTERNAL) {
            serverLevel.getServer().execute(() -> {
                if (!boss.isRemoved()) {
                    synchronizeSenDimsTraits(boss);
                }
            });
            return;
        }
        synchronizeSenDimsTraits(boss);
    }

    static void synchronizeSenDimsTraits(LivingEntity boss) {
        TwilightBossParryBridge bridge = TwilightBossParryIntegration.bridge();
        if (!bridge.isAvailable()) {
            removeExternalHealthScale(boss);
            return;
        }
        if (!bridge.ensureActive(boss)) {
            BossRefactorTwilightForest.LOGGER.error(
                "SenDimS did not activate Leader mechanics for parryable entity {}",
                boss.getUUID());
            return;
        }
        TwilightBossLeaderProfile profile = bridge.leaderProfile(boss);
        TwilightBossLeaderProfilePolicy policy =
                TwilightBossParryIntegration.leaderProfilePolicy(boss.getType());
        if (!policy.accepts(profile)) {
            removeProjectExternalHealthScale(boss);
            BossRefactorTwilightForest.LOGGER.error(
                "Parryable entity {} is using SenDimS Leader profile {}, which {} does not allow",
                boss.getUUID(), profile, policy);
            return;
        }
        if (profile == TwilightBossLeaderProfile.EXTERNAL) {
            applyExternalHealthScale(boss);
        } else {
            removeProjectExternalHealthScale(boss);
        }
    }

    private static void applyExternalHealthScale(LivingEntity boss) {
        AttributeInstance maximumHealth = boss.getAttribute(Attributes.MAX_HEALTH);
        if (maximumHealth == null) {
            return;
        }
        AttributeModifier externalScale = maximumHealth.getModifier(
            EXTERNAL_LEADER_HEALTH_MODIFIER_ID);
        java.util.List<AttributeModifier> legacyScales = maximumHealth
            .getModifiers().stream()
            .filter(TwilightBossSenDimsEvents::isLegacySenDimsHealthScale)
            .toList();
        if ((externalScale != null && legacyScales.isEmpty())
            || (externalScale == null && legacyScales.size() == 1)) {
            return;
        }
        boolean firstScale = externalScale == null && legacyScales.isEmpty();
        float previousMaximumHealth = boss.getMaxHealth();
        float previousHealth = boss.getHealth();
        if (externalScale != null) {
            maximumHealth.removeModifier(EXTERNAL_LEADER_HEALTH_MODIFIER_ID);
        }
        for (AttributeModifier legacyScale : legacyScales) {
            maximumHealth.removeModifier(legacyScale.getId());
        }
        maximumHealth.addPermanentModifier(new AttributeModifier(
                EXTERNAL_LEADER_HEALTH_MODIFIER_ID,
                EXTERNAL_LEADER_HEALTH_MODIFIER_NAME,
                TwilightBossSenDimsRules.MAXIMUM_HEALTH_MODIFIER_AMOUNT,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
            boss.setHealth(!firstScale && previousMaximumHealth > 0.0F
            ? TwilightBossSenDimsRules.healthAfterScaleRemoval(
                previousHealth, previousMaximumHealth,
                boss.getMaxHealth())
            : boss.getMaxHealth());
    }

        private static boolean isLegacySenDimsHealthScale(
            AttributeModifier modifier) {
        return TwilightBossSenDimsRules.isSenDimsHealthModifier(
            modifier.getName(), modifier.getAmount(),
            modifier.getOperation()
                == AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static void removeExternalHealthScale(LivingEntity boss) {
        AttributeInstance maximumHealth = boss.getAttribute(Attributes.MAX_HEALTH);
        if (maximumHealth == null) {
            return;
        }
        boolean hasExternalScale = maximumHealth.getModifier(
            EXTERNAL_LEADER_HEALTH_MODIFIER_ID) != null;
        java.util.List<UUID> legacyScaleIds = maximumHealth.getModifiers().stream()
            .filter(TwilightBossSenDimsEvents::isLegacySenDimsHealthScale)
            .map(AttributeModifier::getId)
            .toList();
        if (!hasExternalScale && legacyScaleIds.isEmpty()) {
            return;
        }
        float scaledMaximumHealth = boss.getMaxHealth();
        float currentHealth = boss.getHealth();
        if (hasExternalScale) {
            maximumHealth.removeModifier(EXTERNAL_LEADER_HEALTH_MODIFIER_ID);
        }
        for (UUID modifierId : legacyScaleIds) {
            maximumHealth.removeModifier(modifierId);
        }
        boss.setHealth(TwilightBossSenDimsRules.healthAfterScaleRemoval(
                currentHealth, scaledMaximumHealth, boss.getMaxHealth()));
    }

    private static void removeProjectExternalHealthScale(LivingEntity boss) {
        AttributeInstance maximumHealth = boss.getAttribute(Attributes.MAX_HEALTH);
        if (maximumHealth == null) {
            return;
        }
        if (maximumHealth.getModifier(EXTERNAL_LEADER_HEALTH_MODIFIER_ID)
                == null) {
            return;
        }
        float scaledMaximumHealth = boss.getMaxHealth();
        float currentHealth = boss.getHealth();
        maximumHealth.removeModifier(EXTERNAL_LEADER_HEALTH_MODIFIER_ID);
        boss.setHealth(TwilightBossSenDimsRules.healthAfterScaleRemoval(
                currentHealth, scaledMaximumHealth, boss.getMaxHealth()));
    }
}