package com.tonywww.bossrefactortf.boss;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public interface TwilightBossParryAccess {
    boolean bossRefactorTF$isCurrentAttackParryable();

    int bossRefactorTF$getParryFeedbackTicks();

    int bossRefactorTF$acceptParry(
            @Nullable LivingEntity actor, ResourceLocation sourceId);
}