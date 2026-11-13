package com.tonywww.bossrefactortf.boss;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
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
    private static boolean disabled;

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
        if (disabled) {
            return;
        }
        bridge = new GuardedBridge(Objects.requireNonNull(newBridge, "newBridge"));
        for (EntityType<? extends LivingEntity> entityType : EXTERNAL_LEADERS) {
            if (!bridge.registerLeaderType(entityType) && !disabled) {
                BossRefactorTwilightForest.LOGGER.warn(
                        "SenDimS already uses another Leader profile for {}; its parry integration stays off",
                        EntityType.getKey(entityType));
            }
        }
    }

    // 联动失败（特供模组版本不兼容、API 变化）时退回无联动状态，Boss 照常按本模组的规则战斗。
    public static void disable(String reason, Throwable cause) {
        bridge = NOOP;
        if (disabled) {
            return;
        }
        disabled = true;
        BossRefactorTwilightForest.LOGGER.warn(
                "SenDimS boss integration disabled: {}", reason, cause);
    }

    private static final class GuardedBridge implements TwilightBossParryBridge {
        private static final String INCOMPATIBLE_API = "incompatible SenDimS Leader API";

        private final TwilightBossParryBridge delegate;

        private GuardedBridge(TwilightBossParryBridge delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean isAvailable() {
            try {
                return delegate.isAvailable();
            } catch (LinkageError error) {
                disable(INCOMPATIBLE_API, error);
                return false;
            }
        }

        @Override
        public boolean registerLeaderType(
                EntityType<? extends LivingEntity> entityType) {
            try {
                return delegate.registerLeaderType(entityType);
            } catch (LinkageError error) {
                disable(INCOMPATIBLE_API, error);
                return false;
            }
        }

        @Override
        public boolean ensureActive(LivingEntity boss) {
            try {
                return delegate.ensureActive(boss);
            } catch (LinkageError error) {
                disable(INCOMPATIBLE_API, error);
                return false;
            }
        }

        @Override
        public TwilightBossLeaderProfile leaderProfile(LivingEntity boss) {
            try {
                return delegate.leaderProfile(boss);
            } catch (LinkageError error) {
                disable(INCOMPATIBLE_API, error);
                return TwilightBossLeaderProfile.NONE;
            }
        }

        @Override
        public boolean isParryable(LivingEntity boss) {
            try {
                return delegate.isParryable(boss);
            } catch (LinkageError error) {
                disable(INCOMPATIBLE_API, error);
                return false;
            }
        }

        @Override
        public boolean isParried(LivingEntity boss) {
            try {
                return delegate.isParried(boss);
            } catch (LinkageError error) {
                disable(INCOMPATIBLE_API, error);
                return false;
            }
        }

        @Override
        public boolean openWindow(LivingEntity boss) {
            try {
                return delegate.openWindow(boss);
            } catch (LinkageError error) {
                disable(INCOMPATIBLE_API, error);
                return false;
            }
        }

        @Override
        public boolean closeWindow(LivingEntity boss) {
            try {
                return delegate.closeWindow(boss);
            } catch (LinkageError error) {
                disable(INCOMPATIBLE_API, error);
                return false;
            }
        }
    }
}