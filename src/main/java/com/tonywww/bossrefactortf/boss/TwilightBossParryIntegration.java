package com.tonywww.bossrefactortf.boss;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class TwilightBossParryIntegration {
    private static final TwilightBossParryBridge NOOP = new TwilightBossParryBridge() {
        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public boolean registerLeaderType(
                EntityType<? extends LivingEntity> entityType) {
            return false;
        }

        @Override
        public boolean ensureActive(LivingEntity boss) {
            return false;
        }

        @Override
        public TwilightBossLeaderProfile leaderProfile(LivingEntity boss) {
            return TwilightBossLeaderProfile.NONE;
        }

        @Override
        public boolean isParryable(LivingEntity boss) {
            return false;
        }

        @Override
        public boolean isParried(LivingEntity boss) {
            return false;
        }

        @Override
        public boolean openWindow(LivingEntity boss) {
            return false;
        }

        @Override
        public boolean closeWindow(LivingEntity boss) {
            return false;
        }
    };

    private static final Set<EntityType<? extends LivingEntity>> EXTERNAL_LEADERS =
            new LinkedHashSet<>();
        private static final Set<EntityType<? extends LivingEntity>>
            MANAGED_OR_EXTERNAL_LEADERS = new LinkedHashSet<>();
    private static TwilightBossParryBridge bridge = NOOP;

    private TwilightBossParryIntegration() {
    }

    public static TwilightBossParryBridge bridge() {
        return bridge;
    }

    public static Set<EntityType<? extends LivingEntity>> externalLeaderTypes() {
        return Collections.unmodifiableSet(EXTERNAL_LEADERS);
    }

    public static Set<EntityType<? extends LivingEntity>>
            managedOrExternalLeaderTypes() {
        return Collections.unmodifiableSet(MANAGED_OR_EXTERNAL_LEADERS);
    }

        public static TwilightBossLeaderProfilePolicy leaderProfilePolicy(
            EntityType<?> entityType) {
        return MANAGED_OR_EXTERNAL_LEADERS.contains(entityType)
                ? TwilightBossLeaderProfilePolicy.MANAGED_OR_EXTERNAL
                : TwilightBossLeaderProfilePolicy.EXTERNAL_ONLY;
    }

    public static boolean declareExternalLeaderType(
            EntityType<? extends LivingEntity> entityType) {
        EntityType<? extends LivingEntity> checked =
                Objects.requireNonNull(entityType, "entityType");
        if (MANAGED_OR_EXTERNAL_LEADERS.contains(checked)) {
            throw new IllegalStateException(
                    "Leader type already uses the managed-or-external policy: "
                            + EntityType.getKey(checked));
        }
        if (!EXTERNAL_LEADERS.add(checked)) {
            return false;
        }
        return !bridge.isAvailable() || bridge.registerLeaderType(checked);
    }

    public static boolean declareManagedOrExternalLeaderType(
            EntityType<? extends LivingEntity> entityType) {
        EntityType<? extends LivingEntity> checked =
                Objects.requireNonNull(entityType, "entityType");
        if (EXTERNAL_LEADERS.contains(checked)) {
            throw new IllegalStateException(
                    "Leader type already requires the External profile: "
                            + EntityType.getKey(checked));
        }
        if (!MANAGED_OR_EXTERNAL_LEADERS.add(checked)) {
            return false;
        }
        return true;
    }

    public static void install(TwilightBossParryBridge newBridge) {
        bridge = Objects.requireNonNull(newBridge, "newBridge");
        for (EntityType<? extends LivingEntity> entityType : EXTERNAL_LEADERS) {
            if (!bridge.registerLeaderType(entityType)) {
                throw new IllegalStateException(
                        "Conflicting SenDimS Leader profile for "
                                + EntityType.getKey(entityType));
            }
        }
    }
}