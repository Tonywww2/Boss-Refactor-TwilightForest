package com.tonywww.bossrefactortf.compat.sendims;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.TwilightBossParryBridge;
import com.tonywww.bossrefactortf.boss.TwilightBossParryIntegration;
import com.tonywww.bossrefactortf.boss.TwilightBossLeaderProfile;
import com.tonywww.slashblade_sendims.api.leader.LeaderApi;
import com.tonywww.slashblade_sendims.api.leader.LeaderParryDecision;
import com.tonywww.slashblade_sendims.api.leader.LeaderProfile;
import com.tonywww.slashblade_sendims.api.leader.event.LeaderParryAttemptEvent;
import com.tonywww.slashblade_sendims.api.leader.event.LeaderParriedEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public final class TwilightForestSenDimsCompat implements TwilightBossParryBridge {
    private static final String CLIENT_INDICATOR_PROVIDER =
            "com.tonywww.bossrefactortf.compat.sendims.client.ExternalLeaderIndicatorProvider";

    private TwilightForestSenDimsCompat() {
    }

    public static void initialize() {
        TwilightForestSenDimsCompat compat = new TwilightForestSenDimsCompat();
        TwilightBossParryIntegration.install(compat);
        MinecraftForge.EVENT_BUS.register(compat);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registerClientIndicatorProvider();
        }
        BossRefactorTwilightForest.LOGGER.info(
                "Enabled SlashBlade SenDimS compatibility with {} external boss adapters",
                TwilightBossParryIntegration.externalLeaderTypes().size());
    }

    private static void registerClientIndicatorProvider() {
        try {
            Class<?> provider = Class.forName(CLIENT_INDICATOR_PROVIDER);
            provider.getMethod("register").invoke(null);
        } catch (ReflectiveOperationException exception) {
            BossRefactorTwilightForest.LOGGER.error(
                    "Unable to register the SenDimS external Leader indicator provider",
                    exception);
            throw new IllegalStateException(
                "Unable to register the SenDimS external Leader indicator provider",
                exception);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean registerLeaderType(
            EntityType<? extends LivingEntity> entityType) {
        boolean registered = LeaderApi.registerLeaderType(
                entityType, LeaderProfile.EXTERNAL);
        if (!registered) {
            BossRefactorTwilightForest.LOGGER.error(
                "A conflicting Leader profile is already registered for {}",
                EntityType.getKey(entityType));
        }
        return registered;
    }

    @Override
    public boolean ensureActive(LivingEntity boss) {
        if (leaderProfile(boss) != TwilightBossLeaderProfile.NONE) {
            return true;
        }
        boolean strictExternal = TwilightBossParryIntegration
            .externalLeaderTypes().contains(boss.getType());
        boolean migrationType = TwilightBossParryIntegration
            .managedOrExternalLeaderTypes().contains(boss.getType());
        if (!strictExternal && !migrationType) {
            return false;
        }
        boolean registered = LeaderApi.registerLeader(
            boss, LeaderProfile.EXTERNAL);
        if (!registered
                && leaderProfile(boss) == TwilightBossLeaderProfile.NONE) {
            BossRefactorTwilightForest.LOGGER.error(
                    "Unable to activate SenDimS Leader mechanics for {}",
                    boss.getUUID());
            return false;
        }
        return leaderProfile(boss) != TwilightBossLeaderProfile.NONE;
    }

    @Override
    public TwilightBossLeaderProfile leaderProfile(LivingEntity boss) {
        return LeaderApi.getSnapshot(boss)
                .map(snapshot -> switch (snapshot.profile()) {
                    case MANAGED -> TwilightBossLeaderProfile.MANAGED;
                    case EXTERNAL -> TwilightBossLeaderProfile.EXTERNAL;
                })
                .orElse(TwilightBossLeaderProfile.NONE);
    }

    @Override
    public boolean isParryable(LivingEntity boss) {
        return ensureActive(boss) && LeaderApi.isParryable(boss);
    }

    @Override
    public boolean isParried(LivingEntity boss) {
        return ensureActive(boss) && LeaderApi.isParried(boss);
    }

    @Override
    public boolean openWindow(LivingEntity boss) {
        return ensureActive(boss) && LeaderApi.openParryWindow(boss);
    }

    @Override
    public boolean closeWindow(LivingEntity boss) {
        return ensureActive(boss) && LeaderApi.closeParryWindow(boss);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onParryAttempt(LeaderParryAttemptEvent event) {
        if (!(event.getTarget() instanceof TwilightBossParryAccess access)
                || !access.bossRefactorTF$isCurrentAttackParryable()) {
            return;
        }
        int feedbackTicks = access.bossRefactorTF$getParryFeedbackTicks();
        if (feedbackTicks <= 0) {
            return;
        }
        event.setDecision(LeaderParryDecision.PARRY);
        event.setParriedTicks(feedbackTicks);
        event.setStunTicks(access.bossRefactorTF$suppressesExternalParryStun()
            ? 0
            : feedbackTicks);
    }

    @SubscribeEvent
    public void onParried(LeaderParriedEvent event) {
        if (event.getTarget() instanceof TwilightBossParryAccess access) {
            access.bossRefactorTF$acceptParry(
                    event.getActor(), event.getSourceId());
        }
    }
}