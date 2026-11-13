package com.tonywww.bossrefactortf.compat.sendims.client;

import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphAccess;
import com.tonywww.bossrefactortf.telegraph.ParryIndicatorStyle;
import com.tonywww.slashblade_sendims.api.leader.client.ClientLeaderIndicatorApi;
import net.minecraft.world.entity.LivingEntity;

import java.util.OptionalDouble;

public final class ExternalLeaderIndicatorProvider {
    private ExternalLeaderIndicatorProvider() {
    }

    public static void register() {
        ClientLeaderIndicatorApi.registerExternalWarningProvider(
                ExternalLeaderIndicatorProvider::warningProgress);
    }

    private static OptionalDouble warningProgress(LivingEntity entity) {
        if (!(entity instanceof TwilightBossParryAccess)) {
            return OptionalDouble.empty();
        }
        if (entity instanceof AttackTelegraphAccess telegraphAccess) {
            AttackTelegraph telegraph =
                    telegraphAccess.bossRefactorTF$getAttackTelegraph();
            if (ParryIndicatorStyle.isVisible(true, telegraph)) {
                return OptionalDouble.of(telegraph.progress());
            }
        }
        return OptionalDouble.of(1.0);
    }
}