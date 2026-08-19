package com.tonywww.bossrefactortf.boss;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public interface TwilightBossParryAccess {
    boolean bossRefactorTF$isCurrentAttackParryable();

    default boolean bossRefactorTF$isCurrentAttackParryable(
            @Nullable LivingEntity actor) {
        return bossRefactorTF$isCurrentAttackParryable();
    }

    int bossRefactorTF$getParryFeedbackTicks();

    default boolean bossRefactorTF$suppressesExternalParryStun() {
        return false;
    }

    int bossRefactorTF$acceptParry(
            @Nullable LivingEntity actor, ResourceLocation sourceId);
}