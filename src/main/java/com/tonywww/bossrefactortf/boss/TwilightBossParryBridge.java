package com.tonywww.bossrefactortf.boss;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public interface TwilightBossParryBridge {
    boolean isAvailable();

    boolean registerLeaderType(EntityType<? extends LivingEntity> entityType);

    boolean ensureActive(LivingEntity boss);

    boolean usesExternalProfile(LivingEntity boss);

    boolean isParryable(LivingEntity boss);

    boolean isParried(LivingEntity boss);

    boolean openWindow(LivingEntity boss);

    boolean closeWindow(LivingEntity boss);
}