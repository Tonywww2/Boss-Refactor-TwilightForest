package com.tonywww.bossrefactortf.boss.naga;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class NagaAreaEffectData extends SavedData {
    private static final String DATA_NAME = "bossrefactortf_naga_area_effects";
    private static final String PROJECTILES_TAG = "VenomProjectiles";
    private static final String CLOUDS_TAG = "VenomClouds";
    private static final String FIRE_POINTS_TAG = "FirePoints";

    private final List<VenomProjectile> projectiles = new ArrayList<>();
    private final List<VenomCloud> clouds = new ArrayList<>();
    private final List<FirePoint> firePoints = new ArrayList<>();

    public static NagaAreaEffectData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                NagaAreaEffectData::load,
                NagaAreaEffectData::new,
                DATA_NAME);
    }

    public static NagaAreaEffectData load(CompoundTag tag) {
        NagaAreaEffectData data = new NagaAreaEffectData();
        readList(tag, PROJECTILES_TAG,
                value -> data.projectiles.add(VenomProjectile.load(value)));
        readList(tag, CLOUDS_TAG,
                value -> data.clouds.add(VenomCloud.load(value)));
        readList(tag, FIRE_POINTS_TAG,
                value -> data.firePoints.add(FirePoint.load(value)));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put(PROJECTILES_TAG, writeList(projectiles));
        tag.put(CLOUDS_TAG, writeList(clouds));
        tag.put(FIRE_POINTS_TAG, writeList(firePoints));
        return tag;
    }

    public UUID spawnVenomProjectile(
            ServerLevel level, LivingEntity owner,
            Vec3 origin, Vec3 direction, float attackDamage,
            boolean fieryLanding) {
        BossRefactorTwilightForestConfig.NagaConfig config =
                BossRefactorTwilightForestConfig.NAGA;
        UUID id = UUID.randomUUID();
        projectiles.add(new VenomProjectile(
                id, owner.getUUID(), teamName(owner),
                origin.x, origin.y, origin.z,
                direction.x, direction.z,
                config.venomProjectileSpeed.get(),
                config.venomLength.get(),
                config.venomProjectileRadius.get(),
                attackDamage,
                config.venomInitialDamageMultiplier.get().floatValue(),
                config.venomCloudDurationTicks.get(),
                config.venomCloudRadius.get().floatValue(),
                config.venomDamageIntervalTicks.get(),
                config.venomDamageMultiplier.get().floatValue(),
                config.venomPoisonDurationTicks.get(),
                config.venomPoisonAmplifier.get(),
                config.venomProjectileParticles.get(),
                config.venomCloudParticles.get(),
                fieryLanding,
                config.firePointDurationTicks.get(),
                config.fireDamageRadius.get().floatValue(),
                config.fireDamageIntervalTicks.get(),
                config.fireDamageMultiplier.get().floatValue(),
                config.fireIgniteTicks.get(),
                config.fireParticlesPerPoint.get()));
        setDirty();
        return id;
    }

    public void addFirePoint(
            LivingEntity owner, Vec3 position, float attackDamage) {
        BossRefactorTwilightForestConfig.NagaConfig config =
                BossRefactorTwilightForestConfig.NAGA;
        addFirePoint(new FirePoint(
                UUID.randomUUID(), owner.getUUID(), teamName(owner),
                position.x, position.y, position.z,
                config.firePointDurationTicks.get(),
                config.fireDamageRadius.get().floatValue(),
                Math.max(0.0F, attackDamage
                        * config.fireDamageMultiplier.get().floatValue()),
                config.fireDamageIntervalTicks.get(),
                config.fireIgniteTicks.get(),
                config.fireParticlesPerPoint.get()));
    }

    public boolean removeVenomProjectile(UUID id) {
        boolean removed = projectiles.removeIf(projectile -> projectile.id.equals(id));
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public boolean hasVenomProjectile(UUID id) {
        return projectiles.stream().anyMatch(projectile -> projectile.id.equals(id));
    }

    public void tick(ServerLevel level) {
        boolean changed = tickProjectiles(level);
        changed |= tickClouds(level);
        changed |= tickFirePoints(level);
        if (changed) {
            setDirty();
        }
    }

    private boolean tickProjectiles(ServerLevel level) {
        if (projectiles.isEmpty()) {
            return false;
        }
        Iterator<VenomProjectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            VenomProjectile projectile = iterator.next();
            Vec3 start = projectile.position();
            Vec3 movement = new Vec3(
                    projectile.directionX, 0.0, projectile.directionZ)
                    .scale(projectile.speed);
            Vec3 end = start.add(movement);
            Entity owner = level.getEntity(projectile.ownerId);
            BlockHitResult blockHit = level.clip(new ClipContext(
                    start, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    owner));
            boolean blockedByTerrain = blockHit.getType() != HitResult.Type.MISS;
            Vec3 pathEnd = blockedByTerrain ? blockHit.getLocation() : end;
            Contact contact = firstContact(
                    level, projectile, start, pathEnd);
            Vec3 resolvedPosition = contact == null
                    ? pathEnd
                    : contact.position;
            projectile.x = resolvedPosition.x;
            projectile.y = resolvedPosition.y;
            projectile.z = resolvedPosition.z;
            projectile.travelled += start.distanceTo(resolvedPosition);
            sendVenomParticles(level, projectile);

            if (contact != null) {
                boolean defended = damageVenomContact(
                    level, projectile, contact.target, contact.position);
                iterator.remove();
                landVenom(projectile, defended
                        ? blockedCloudPosition(
                                contact.target, contact.position, projectile)
                        : projectile.position());
                NagaCombat.onVenomProjectileResolved(
                        level, projectile.ownerId, projectile.id);
                continue;
            }
            if (blockedByTerrain || projectile.travelled >= projectile.maximumDistance) {
                iterator.remove();
                landVenom(projectile);
                NagaCombat.onVenomProjectileResolved(
                        level, projectile.ownerId, projectile.id);
            }
        }
        return true;
    }

    private boolean tickClouds(ServerLevel level) {
        if (clouds.isEmpty()) {
            return false;
        }
        Iterator<VenomCloud> iterator = clouds.iterator();
        while (iterator.hasNext()) {
            VenomCloud cloud = iterator.next();
            cloud.age++;
            sendCloudParticles(level, cloud);
            if (cloud.age % cloud.damageInterval == 0) {
                for (LivingEntity target : targetsInCircle(
                    level, cloud, cloud.position(), cloud.radius,
                    BossRefactorTwilightForestConfig.NAGA
                        .venomCloudVerticalRange.get())) {
                    target.hurt(damageSource(level, cloud, false), cloud.damage);
                    target.addEffect(new MobEffectInstance(
                            MobEffects.POISON,
                            cloud.poisonDuration,
                            cloud.poisonAmplifier));
                }
            }
            cloud.remainingTicks--;
            if (cloud.remainingTicks <= 0) {
                iterator.remove();
            }
        }
        return true;
    }

    private boolean tickFirePoints(ServerLevel level) {
        if (firePoints.isEmpty()) {
            return false;
        }
        Map<FireHitKey, PendingFireHit> pendingHits = new HashMap<>();
        Iterator<FirePoint> iterator = firePoints.iterator();
        while (iterator.hasNext()) {
            FirePoint point = iterator.next();
            point.age++;
            sendFireParticles(level, point);
                if (point.age >= point.damageInterval
                    && level.getGameTime() % point.damageInterval == 0) {
                for (LivingEntity target : targetsInCircle(
                        level, point, point.position(), point.radius,
                        BossRefactorTwilightForestConfig.NAGA
                                .firePointVerticalRange.get())) {
                FireHitKey key = new FireHitKey(
                    point.ownerId, target.getUUID());
                PendingFireHit candidate = new PendingFireHit(
                    point, target);
                pendingHits.merge(key, candidate,
                    NagaAreaEffectData::strongerFireHit);
                }
            }
            point.remainingTicks--;
            if (point.remainingTicks <= 0) {
                iterator.remove();
            }
        }
        for (PendingFireHit hit : pendingHits.values()) {
            FirePoint point = hit.point;
            LivingEntity target = hit.target;
            target.hurt(damageSource(level, point, true), point.damage);
            target.setRemainingFireTicks(Math.max(
                    target.getRemainingFireTicks(), point.igniteTicks));
        }
        return true;
    }

    private static PendingFireHit strongerFireHit(
            PendingFireHit first, PendingFireHit second) {
        return second.point.damage > first.point.damage ? second : first;
    }

    @Nullable
    private Contact firstContact(
            ServerLevel level, VenomProjectile projectile,
            Vec3 start, Vec3 end) {
        double radius = projectile.radius;
        AABB bounds = new AABB(start, end).inflate(radius, radius, radius);
        Vec3 direction = end.subtract(start).normalize();
        return level.getEntitiesOfClass(
                LivingEntity.class, bounds,
                target -> isValidTarget(level, projectile, target))
            .stream()
            .map(target -> target.getBoundingBox().inflate(radius)
                .clip(start, end)
                .map(position -> new Contact(target, position))
                .orElse(null))
            .filter(java.util.Objects::nonNull)
            .min(Comparator.comparingDouble(contact ->
                contact.position.subtract(start).dot(direction)))
                .orElse(null);
    }

    private boolean damageVenomContact(
            ServerLevel level, VenomProjectile projectile,
            LivingEntity target, Vec3 contactPosition) {
        DamageSource source = damageSource(
            level, projectile, false, contactPosition);
        ItemStack usedShield = target instanceof ServerPlayer player
                ? player.getUseItem().copy()
                : ItemStack.EMPTY;
        boolean shieldBlocked = target instanceof ServerPlayer player
                && usedShield.getItem() instanceof ShieldItem
                && player.isDamageSourceBlocked(source);
        target.hurt(source, projectile.attackDamage
                * projectile.initialDamageMultiplier);
        if (shieldBlocked) {
            applyShieldCooldown((ServerPlayer) target, usedShield);
        }
        return shieldBlocked;
    }

        private static Vec3 blockedCloudPosition(
            LivingEntity target, Vec3 contactPosition,
            VenomProjectile projectile) {
        AABB bounds = target.getBoundingBox();
        NagaCombatRules.HorizontalPoint center =
                NagaCombatRules.blockedCloudCenter(
                (bounds.minX + bounds.maxX) * 0.5,
                (bounds.minZ + bounds.maxZ) * 0.5,
                (bounds.maxX - bounds.minX) * 0.5,
                (bounds.maxZ - bounds.minZ) * 0.5,
                        projectile.directionX, projectile.directionZ,
                        BossRefactorTwilightForestConfig.NAGA
                                .venomBlockedCloudOffset.get());
        return new Vec3(center.x(), contactPosition.y, center.z());
    }

    private void landVenom(VenomProjectile projectile) {
        landVenom(projectile, projectile.position());
    }

    private void landVenom(VenomProjectile projectile, Vec3 position) {
        clouds.add(new VenomCloud(
                UUID.randomUUID(), projectile.ownerId, projectile.teamName,
                position.x, position.y, position.z,
                projectile.cloudDuration,
                projectile.cloudRadius,
                projectile.attackDamage * projectile.cloudDamageMultiplier,
                projectile.cloudDamageInterval,
                projectile.poisonDuration,
                projectile.poisonAmplifier,
                projectile.cloudParticles));
        if (projectile.fieryLanding) {
            double spacing = BossRefactorTwilightForestConfig.NAGA
                    .firePointSpacing.get();
            int rings = Math.max(1, (int) Math.ceil(
                    projectile.cloudRadius / spacing));
            for (int ring = 0; ring <= rings; ring++) {
                double radius = ring * projectile.cloudRadius / rings;
                int count = ring == 0 ? 1
                        : Math.max(6, (int) Math.ceil(
                                Math.PI * 2.0 * radius / spacing));
                for (int index = 0; index < count; index++) {
                    double angle = Math.PI * 2.0 * index / count;
                    addFirePoint(new FirePoint(
                            UUID.randomUUID(), projectile.ownerId,
                            projectile.teamName,
                            position.x + Math.cos(angle) * radius,
                            position.y,
                            position.z + Math.sin(angle) * radius,
                            projectile.fireDuration,
                            projectile.fireRadius,
                            projectile.attackDamage
                                    * projectile.fireDamageMultiplier,
                            projectile.fireDamageInterval,
                            projectile.fireIgniteTicks,
                            projectile.fireParticles));
                }
            }
        }
    }

    private void addFirePoint(FirePoint point) {
        firePoints.add(point);
        setDirty();
    }

    private static void applyShieldCooldown(
            ServerPlayer player, ItemStack shield) {
        int cooldown = BossRefactorTwilightForestConfig.NAGA
                .shieldCooldownTicks.get();
        if (!shield.isEmpty() && cooldown > 0) {
            player.getCooldowns().addCooldown(shield.getItem(), cooldown);
        }
        player.stopUsingItem();
    }

    private static List<LivingEntity> targetsInCircle(
            ServerLevel level, OwnedEffect effect,
            Vec3 center, double radius, double verticalRange) {
        AABB bounds = new AABB(
                center.x - radius, center.y - verticalRange, center.z - radius,
                center.x + radius, center.y + verticalRange, center.z + radius);
        return level.getEntitiesOfClass(
                LivingEntity.class, bounds,
                target -> isValidTarget(level, effect, target)
                        && NagaCombatRules.isInsideCircle(
                                target.getX(), target.getZ(),
                                center.x, center.z, radius));
    }

    private static boolean isValidTarget(
            ServerLevel level, OwnedEffect effect, LivingEntity target) {
        if (!target.isAlive() || target.getUUID().equals(effect.ownerId)) {
            return false;
        }
        if (target instanceof Player player
                && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        Entity ownerEntity = level.getEntity(effect.ownerId);
        if (ownerEntity instanceof LivingEntity owner) {
            return !owner.isAlliedTo(target);
        }
        return effect.teamName.isEmpty()
                || target.getTeam() == null
                || !effect.teamName.equals(target.getTeam().getName());
    }

    private static DamageSource damageSource(
            ServerLevel level, OwnedEffect effect, boolean fire) {
        return damageSource(level, effect, fire, effect.position());
        }

        private static DamageSource damageSource(
            ServerLevel level, OwnedEffect effect,
            boolean fire, Vec3 sourcePosition) {
        Entity owner = level.getEntity(effect.ownerId);
        var damageTypeRegistry = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE);
        var damageType = damageTypeRegistry.getHolderOrThrow(
                fire ? DamageTypes.IN_FIRE
                        : owner instanceof LivingEntity
                                ? DamageTypes.MOB_ATTACK
                                : DamageTypes.MAGIC);
        if (owner instanceof LivingEntity) {
            return new DamageSource(
                    damageType, owner, owner, sourcePosition);
        }
        return new DamageSource(damageType, sourcePosition);
    }

    private static String teamName(LivingEntity owner) {
        return owner.getTeam() == null ? "" : owner.getTeam().getName();
    }

    private static void sendVenomParticles(
            ServerLevel level, VenomProjectile projectile) {
        BossRefactorTwilightForestConfig.NagaConfig config =
            BossRefactorTwilightForestConfig.NAGA;
        double coreSpread = config.venomProjectileCoreSpread.get();
        double mistSpread = config.venomProjectileMistSpread.get();
        double speed = config.venomProjectileParticleSpeed.get();
        level.sendParticles(
            new DustColorTransitionOptions(
                primaryColor(config), secondaryColor(config),
                config.venomPrimaryParticleScale.get().floatValue()),
                projectile.x, projectile.y, projectile.z,
                projectile.particles,
            coreSpread, coreSpread, coreSpread, speed);
        level.sendParticles(
            new DustParticleOptions(
                secondaryColor(config),
                config.venomSecondaryParticleScale.get().floatValue()),
            projectile.x, projectile.y, projectile.z,
            config.venomProjectileMistParticles.get(),
            mistSpread, mistSpread, mistSpread, speed);
        level.sendParticles(
            ParticleTypes.ITEM_SLIME,
            projectile.x, projectile.y, projectile.z,
            config.venomProjectileSplashParticles.get(),
            mistSpread, mistSpread, mistSpread, speed);
        sendColoredEffectParticles(
            level,
            projectile.x, projectile.y, projectile.z,
            config.venomProjectileGlowParticles.get(),
            mistSpread,
            primaryChannel(config.venomPrimaryRed.get()),
            primaryChannel(config.venomPrimaryGreen.get()),
            primaryChannel(config.venomPrimaryBlue.get()));
    }

    private static void sendCloudParticles(
            ServerLevel level, VenomCloud cloud) {
        BossRefactorTwilightForestConfig.NagaConfig config =
            BossRefactorTwilightForestConfig.NAGA;
        double y = cloud.y + config.venomCloudParticleHeight.get();
        double coreSpread = config.venomCloudCoreSpread.get();
        double mistSpread = config.venomCloudMistSpread.get();
        double speed = config.venomCloudParticleSpeed.get();
        level.sendParticles(
            new DustColorTransitionOptions(
                primaryColor(config), secondaryColor(config),
                config.venomPrimaryParticleScale.get().floatValue()),
            cloud.x, y, cloud.z,
                cloud.particles,
            coreSpread, coreSpread, coreSpread, speed);
        level.sendParticles(
            new DustParticleOptions(
                secondaryColor(config),
                config.venomSecondaryParticleScale.get().floatValue()),
            cloud.x, y, cloud.z,
            config.venomCloudMistParticles.get(),
            mistSpread, mistSpread, mistSpread, speed);
        level.sendParticles(
            ParticleTypes.BUBBLE_POP,
            cloud.x, y, cloud.z,
            config.venomCloudBubbleParticles.get(),
            mistSpread, mistSpread, mistSpread, speed);
        level.sendParticles(
            ParticleTypes.ITEM_SLIME,
            cloud.x, y, cloud.z,
            config.venomCloudSplashParticles.get(),
            mistSpread, mistSpread, mistSpread, speed);
        sendColoredEffectParticles(
            level,
            cloud.x, y, cloud.z,
            config.venomCloudGlowParticles.get(),
            mistSpread,
            primaryChannel(config.venomPrimaryRed.get()),
            primaryChannel(config.venomPrimaryGreen.get()),
            primaryChannel(config.venomPrimaryBlue.get()));
    }

        private static void sendColoredEffectParticles(
            ServerLevel level, double x, double y, double z,
            int count, double spread,
            float red, float green, float blue) {
        for (int index = 0; index < count; index++) {
            double particleX = x
                + (level.random.nextDouble() * 2.0 - 1.0) * spread;
            double particleY = y
                + (level.random.nextDouble() * 2.0 - 1.0) * spread;
            double particleZ = z
                + (level.random.nextDouble() * 2.0 - 1.0) * spread;
            level.sendParticles(
                ParticleTypes.ENTITY_EFFECT,
                particleX, particleY, particleZ,
                0, red, green, blue, 1.0);
        }
        }

        private static Vector3f primaryColor(
            BossRefactorTwilightForestConfig.NagaConfig config) {
        return new Vector3f(
            primaryChannel(config.venomPrimaryRed.get()),
            primaryChannel(config.venomPrimaryGreen.get()),
            primaryChannel(config.venomPrimaryBlue.get()));
        }

        private static Vector3f secondaryColor(
            BossRefactorTwilightForestConfig.NagaConfig config) {
        return new Vector3f(
            primaryChannel(config.venomSecondaryRed.get()),
            primaryChannel(config.venomSecondaryGreen.get()),
            primaryChannel(config.venomSecondaryBlue.get()));
        }

        private static float primaryChannel(int channel) {
        return NagaCombatRules.normalizeColorChannel(channel);
        }

    private static void sendFireParticles(
            ServerLevel level, FirePoint point) {
        level.sendParticles(
                ParticleTypes.FLAME,
                point.x, point.y + 0.15, point.z,
                point.particles,
                point.radius * 0.3, 0.2,
                point.radius * 0.3, 0.01);
    }

    private static ListTag writeList(List<? extends OwnedEffect> effects) {
        ListTag list = new ListTag();
        for (OwnedEffect effect : effects) {
            list.add(effect.save());
        }
        return list;
    }

    private static void readList(
            CompoundTag tag, String key,
            java.util.function.Consumer<CompoundTag> reader) {
        ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
        for (Tag value : list) {
            reader.accept((CompoundTag) value);
        }
    }

    private record Contact(LivingEntity target, Vec3 position) {
    }

    private record FireHitKey(UUID ownerId, UUID targetId) {
    }

    private record PendingFireHit(FirePoint point, LivingEntity target) {
    }

    private abstract static class OwnedEffect {
        final UUID id;
        final UUID ownerId;
        final String teamName;
        double x;
        double y;
        double z;

        OwnedEffect(
                UUID id, UUID ownerId, String teamName,
                double x, double y, double z) {
            this.id = id;
            this.ownerId = ownerId;
            this.teamName = teamName;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        Vec3 position() {
            return new Vec3(x, y, z);
        }

        CompoundTag saveBase() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Id", id);
            tag.putUUID("OwnerId", ownerId);
            tag.putString("Team", teamName);
            tag.putDouble("X", x);
            tag.putDouble("Y", y);
            tag.putDouble("Z", z);
            return tag;
        }

        abstract CompoundTag save();
    }

    private static final class VenomProjectile extends OwnedEffect {
        final double directionX;
        final double directionZ;
        final double speed;
        final double maximumDistance;
        final double radius;
        final float attackDamage;
        final float initialDamageMultiplier;
        final int cloudDuration;
        final float cloudRadius;
        final int cloudDamageInterval;
        final float cloudDamageMultiplier;
        final int poisonDuration;
        final int poisonAmplifier;
        final int particles;
        final int cloudParticles;
        final boolean fieryLanding;
        final int fireDuration;
        final float fireRadius;
        final int fireDamageInterval;
        final float fireDamageMultiplier;
        final int fireIgniteTicks;
        final int fireParticles;
        double travelled;

        VenomProjectile(
                UUID id, UUID ownerId, String teamName,
                double x, double y, double z,
                double directionX, double directionZ,
                double speed, double maximumDistance, double radius,
                float attackDamage, float initialDamageMultiplier,
                int cloudDuration, float cloudRadius,
                int cloudDamageInterval, float cloudDamageMultiplier,
                int poisonDuration, int poisonAmplifier,
                int particles, int cloudParticles,
                boolean fieryLanding, int fireDuration,
                float fireRadius, int fireDamageInterval,
                float fireDamageMultiplier, int fireIgniteTicks,
                int fireParticles) {
            super(id, ownerId, teamName, x, y, z);
            double length = Math.hypot(directionX, directionZ);
            this.directionX = length == 0.0 ? 0.0 : directionX / length;
            this.directionZ = length == 0.0 ? 0.0 : directionZ / length;
            this.speed = Math.max(0.01, speed);
            this.maximumDistance = Math.max(0.0, maximumDistance);
            this.radius = Math.max(0.0, radius);
            this.attackDamage = Math.max(0.0F, attackDamage);
            this.initialDamageMultiplier = Math.max(0.0F, initialDamageMultiplier);
            this.cloudDuration = Math.max(1, cloudDuration);
            this.cloudRadius = Math.max(0.0F, cloudRadius);
            this.cloudDamageInterval = Math.max(1, cloudDamageInterval);
            this.cloudDamageMultiplier = Math.max(0.0F, cloudDamageMultiplier);
            this.poisonDuration = Math.max(1, poisonDuration);
            this.poisonAmplifier = Math.max(0, poisonAmplifier);
            this.particles = Math.max(0, particles);
            this.cloudParticles = Math.max(0, cloudParticles);
            this.fieryLanding = fieryLanding;
            this.fireDuration = Math.max(1, fireDuration);
            this.fireRadius = Math.max(0.0F, fireRadius);
            this.fireDamageInterval = Math.max(1, fireDamageInterval);
            this.fireDamageMultiplier = Math.max(0.0F, fireDamageMultiplier);
            this.fireIgniteTicks = Math.max(0, fireIgniteTicks);
            this.fireParticles = Math.max(0, fireParticles);
        }

        @Override
        CompoundTag save() {
            CompoundTag tag = saveBase();
            tag.putDouble("DirectionX", directionX);
            tag.putDouble("DirectionZ", directionZ);
            tag.putDouble("Speed", speed);
            tag.putDouble("MaximumDistance", maximumDistance);
            tag.putDouble("Radius", radius);
            tag.putDouble("Travelled", travelled);
            tag.putFloat("AttackDamage", attackDamage);
            tag.putFloat("InitialDamageMultiplier", initialDamageMultiplier);
            tag.putInt("CloudDuration", cloudDuration);
            tag.putFloat("CloudRadius", cloudRadius);
            tag.putInt("CloudDamageInterval", cloudDamageInterval);
            tag.putFloat("CloudDamageMultiplier", cloudDamageMultiplier);
            tag.putInt("PoisonDuration", poisonDuration);
            tag.putInt("PoisonAmplifier", poisonAmplifier);
            tag.putInt("Particles", particles);
            tag.putInt("CloudParticles", cloudParticles);
            tag.putBoolean("FieryLanding", fieryLanding);
            tag.putInt("FireDuration", fireDuration);
            tag.putFloat("FireRadius", fireRadius);
            tag.putInt("FireDamageInterval", fireDamageInterval);
            tag.putFloat("FireDamageMultiplier", fireDamageMultiplier);
            tag.putInt("FireIgniteTicks", fireIgniteTicks);
            tag.putInt("FireParticles", fireParticles);
            return tag;
        }

        static VenomProjectile load(CompoundTag tag) {
            VenomProjectile projectile = new VenomProjectile(
                    tag.getUUID("Id"), tag.getUUID("OwnerId"),
                    tag.getString("Team"),
                    tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"),
                    tag.getDouble("DirectionX"), tag.getDouble("DirectionZ"),
                    tag.getDouble("Speed"), tag.getDouble("MaximumDistance"),
                    tag.getDouble("Radius"), tag.getFloat("AttackDamage"),
                    tag.getFloat("InitialDamageMultiplier"),
                    tag.getInt("CloudDuration"), tag.getFloat("CloudRadius"),
                    tag.getInt("CloudDamageInterval"),
                    tag.getFloat("CloudDamageMultiplier"),
                    tag.getInt("PoisonDuration"), tag.getInt("PoisonAmplifier"),
                    tag.getInt("Particles"), tag.getInt("CloudParticles"),
                    tag.getBoolean("FieryLanding"), tag.getInt("FireDuration"),
                    tag.getFloat("FireRadius"), tag.getInt("FireDamageInterval"),
                    tag.getFloat("FireDamageMultiplier"),
                    tag.getInt("FireIgniteTicks"), tag.getInt("FireParticles"));
            projectile.travelled = Math.max(0.0, tag.getDouble("Travelled"));
            return projectile;
        }
    }

    private static final class VenomCloud extends OwnedEffect {
        int remainingTicks;
        final float radius;
        final float damage;
        final int damageInterval;
        final int poisonDuration;
        final int poisonAmplifier;
        final int particles;
        int age;

        VenomCloud(
                UUID id, UUID ownerId, String teamName,
                double x, double y, double z,
                int remainingTicks, float radius, float damage,
                int damageInterval, int poisonDuration,
                int poisonAmplifier, int particles) {
            super(id, ownerId, teamName, x, y, z);
            this.remainingTicks = Math.max(1, remainingTicks);
            this.radius = Math.max(0.0F, radius);
            this.damage = Math.max(0.0F, damage);
            this.damageInterval = Math.max(1, damageInterval);
            this.poisonDuration = Math.max(1, poisonDuration);
            this.poisonAmplifier = Math.max(0, poisonAmplifier);
            this.particles = Math.max(0, particles);
        }

        @Override
        CompoundTag save() {
            CompoundTag tag = saveBase();
            tag.putInt("RemainingTicks", remainingTicks);
            tag.putFloat("Radius", radius);
            tag.putFloat("Damage", damage);
            tag.putInt("DamageInterval", damageInterval);
            tag.putInt("PoisonDuration", poisonDuration);
            tag.putInt("PoisonAmplifier", poisonAmplifier);
            tag.putInt("Particles", particles);
            tag.putInt("Age", age);
            return tag;
        }

        static VenomCloud load(CompoundTag tag) {
            VenomCloud cloud = new VenomCloud(
                    tag.getUUID("Id"), tag.getUUID("OwnerId"),
                    tag.getString("Team"),
                    tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"),
                    tag.getInt("RemainingTicks"), tag.getFloat("Radius"),
                    tag.getFloat("Damage"), tag.getInt("DamageInterval"),
                    tag.getInt("PoisonDuration"), tag.getInt("PoisonAmplifier"),
                    tag.getInt("Particles"));
            cloud.age = Math.max(0, tag.getInt("Age"));
            return cloud;
        }
    }

    private static final class FirePoint extends OwnedEffect {
        int remainingTicks;
        final float radius;
        final float damage;
        final int damageInterval;
        final int igniteTicks;
        final int particles;
        int age;

        FirePoint(
                UUID id, UUID ownerId, String teamName,
                double x, double y, double z,
                int remainingTicks, float radius, float damage,
                int damageInterval, int igniteTicks, int particles) {
            super(id, ownerId, teamName, x, y, z);
            this.remainingTicks = Math.max(1, remainingTicks);
            this.radius = Math.max(0.0F, radius);
            this.damage = Math.max(0.0F, damage);
            this.damageInterval = Math.max(1, damageInterval);
            this.igniteTicks = Math.max(0, igniteTicks);
            this.particles = Math.max(0, particles);
        }

        @Override
        CompoundTag save() {
            CompoundTag tag = saveBase();
            tag.putInt("RemainingTicks", remainingTicks);
            tag.putFloat("Radius", radius);
            tag.putFloat("Damage", damage);
            tag.putInt("DamageInterval", damageInterval);
            tag.putInt("IgniteTicks", igniteTicks);
            tag.putInt("Particles", particles);
            tag.putInt("Age", age);
            return tag;
        }

        static FirePoint load(CompoundTag tag) {
            FirePoint point = new FirePoint(
                    tag.getUUID("Id"), tag.getUUID("OwnerId"),
                    tag.getString("Team"),
                    tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"),
                    tag.getInt("RemainingTicks"), tag.getFloat("Radius"),
                    tag.getFloat("Damage"), tag.getInt("DamageInterval"),
                    tag.getInt("IgniteTicks"), tag.getInt("Particles"));
            point.age = Math.max(0, tag.getInt("Age"));
            return point;
        }
    }
}