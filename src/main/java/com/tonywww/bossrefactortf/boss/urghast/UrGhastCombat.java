package com.tonywww.bossrefactortf.boss.urghast;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import twilightforest.entity.boss.UrGhast;
import twilightforest.entity.projectile.UrGhastFireball;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class UrGhastCombat {
    private static final DustParticleOptions CRIMSON_MIST =
            new DustParticleOptions(new Vector3f(0.68F, 0.01F, 0.03F), 2.0F);
        private static final DustParticleOptions TANTRUM_ENERGY =
            new DustParticleOptions(new Vector3f(1.0F, 0.02F, 0.02F), 1.8F);

    private UrGhastCombat() {
    }

    public static boolean customCombatEnabled() {
        return BossRefactorTwilightForestConfig.UR_GHAST.enabled.get();
    }

    public static boolean isOffensive(UrGhast ghast) {
        UrGhastCombatState state = state(ghast);
        return customCombatEnabled()
                && state.mode == UrGhastCombatMode.OFFENSIVE;
    }

    public static boolean shouldHoldPosition(UrGhast ghast) {
        return customCombatEnabled()
                && state(ghast).action == UrGhastActionState.MIST_WINDUP;
    }

    public static void tick(UrGhast ghast) {
        if (ghast.level().isClientSide() || !customCombatEnabled()) {
            return;
        }
        synchronizeAttackDamage(ghast);
        UrGhastCombatState state = state(ghast);
        decrementCooldowns(state);
        if (updateMode(ghast, state)) {
            return;
        }
        setTantrumForMode(ghast, state);
        switch (state.action) {
            case NORMAL -> tickNormal(ghast, state);
            case FIREBALL_WINDUP -> tickFireballWindup(ghast, state);
            case MIST_WINDUP -> tickMistWindup(ghast, state);
            case MIST_DAMAGE -> tickMistDamage(ghast, state);
        }
    }

    public static void stop(UrGhast ghast) {
        ghast.setCharging(false);
        clearMistTelegraphs(ghast);
    }

    public static void restoreVisualState(UrGhast ghast) {
        UrGhastCombatState state = state(ghast);
        if (!ghast.level().isClientSide()) {
            setTantrumForMode(ghast, state);
        }
        ghast.setCharging(state.action == UrGhastActionState.FIREBALL_WINDUP
                && state.actionTicks * 2 >= state.actionDuration);
        if (state.action == UrGhastActionState.MIST_WINDUP
                || state.action == UrGhastActionState.MIST_DAMAGE) {
            updateMistTelegraphs(ghast, state);
        } else {
            clearMistTelegraphs(ghast);
        }
    }

    public static boolean allowHurtSound(UrGhast ghast) {
        if (!customCombatEnabled()) {
            return true;
        }
        UrGhastCombatState state = state(ghast);
        long gameTime = ghast.level().getGameTime();
        if (gameTime < state.nextHurtSoundGameTime) {
            return false;
        }
        state.nextHurtSoundGameTime = gameTime
                + BossRefactorTwilightForestConfig.UR_GHAST
                        .hurtSoundCooldownTicks.get();
        return true;
    }

        public static void tickTantrumVisuals(UrGhast ghast) {
        if (!ghast.level().isClientSide()
            || !customCombatEnabled()
            || !ghast.isInTantrum()
            || ghast.isDeadOrDying()) {
            return;
        }
        BossRefactorTwilightForestConfig.UrGhastConfig config =
            BossRefactorTwilightForestConfig.UR_GHAST;
        for (int index = 0;
            index < config.tantrumRedSmokeParticlesPerTick.get();
            index++) {
            double x = ghast.getX()
                + (ghast.getRandom().nextDouble() - 0.5)
                    * ghast.getBbWidth() * 0.9;
            double y = ghast.getY()
                + ghast.getRandom().nextDouble() * ghast.getBbHeight();
            double z = ghast.getZ()
                + (ghast.getRandom().nextDouble() - 0.5)
                    * ghast.getBbWidth() * 0.9;
            ghast.level().addParticle(
                ParticleTypes.ENTITY_EFFECT,
                x, y, z, 0.85, 0.01, 0.01);
        }
        int interval = config.tantrumEnergyRingIntervalTicks.get();
        if (ghast.tickCount % interval != 0) {
            return;
        }
        int particleCount = config.tantrumEnergyRingParticles.get();
        double radius = ghast.getBbWidth() * 0.65;
        double y = ghast.getY() + ghast.getBbHeight() * 0.5;
        for (int index = 0; index < particleCount; index++) {
            double angle = Math.PI * 2.0 * index / particleCount;
            double cosine = Math.cos(angle);
            double sine = Math.sin(angle);
            ghast.level().addParticle(
                TANTRUM_ENERGY,
                ghast.getX() + cosine * radius,
                y,
                ghast.getZ() + sine * radius,
                cosine * 0.04, 0.03, sine * 0.04);
        }
        }

    public static boolean explodeFireball(
            UrGhastFireball fireball, HitResult hitResult) {
        if (!customCombatEnabled()
                || !(fireball.getOwner() instanceof UrGhast owner)) {
            return false;
        }
        if (fireball.level().isClientSide()) {
            return true;
        }
        Vec3 center = hitResult.getLocation();
        BossRefactorTwilightForestConfig.UrGhastConfig config =
                BossRefactorTwilightForestConfig.UR_GHAST;
        double radius = config.fireballExplosionRadius.get();
        AABB bounds = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);
        for (LivingEntity target : fireball.level().getEntitiesOfClass(
                LivingEntity.class, bounds,
                target -> isFireballTarget(owner, target))) {
            double distance = distanceFromPointToAabb(center, target.getBoundingBox());
            float damage = (float) (UrGhastCombatRules.giantFireballDamage(
                    owner.getAttributeValue(Attributes.ATTACK_DAMAGE),
                    config.fireballDamageMultiplier.get())
                    * UrGhastCombatRules.fireballDamageScale(distance, radius));
            if (damage <= 0.0F || !target.hurt(
                    fireball.damageSources().fireball(fireball, owner), damage)) {
                continue;
            }
            int fireSeconds = Mth.ceil(config.fireballIgniteTicks.get() / 20.0F);
            if (fireSeconds > 0) {
                target.setSecondsOnFire(fireSeconds);
            }
        }
        if (fireball.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    center.x, center.y, center.z,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
        fireball.level().playSound(
                null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE,
                4.0F, 0.8F + owner.getRandom().nextFloat() * 0.2F);
        fireball.gameEvent(GameEvent.PROJECTILE_LAND);
        fireball.discard();
        return true;
    }

    private static void decrementCooldowns(UrGhastCombatState state) {
        if (state.fireballCooldown > 0) {
            state.fireballCooldown--;
        }
        if (state.mistCooldown > 0) {
            state.mistCooldown--;
        }
    }

    private static void synchronizeAttackDamage(UrGhast ghast) {
        var attackDamage = ghast.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return;
        }
        double configuredDamage = BossRefactorTwilightForestConfig.UR_GHAST
                .baseAttackDamage.get();
        if (Double.compare(attackDamage.getBaseValue(), configuredDamage) != 0) {
            attackDamage.setBaseValue(configuredDamage);
        }
    }

    private static boolean updateMode(
            UrGhast ghast, UrGhastCombatState state) {
        BossRefactorTwilightForestConfig.UrGhastConfig config =
                BossRefactorTwilightForestConfig.UR_GHAST;
        if (UrGhastCombatRules.shouldTriggerHalfHealthOffensive(
                state.halfHealthAttackTriggered,
                ghast.getHealth(), ghast.getMaxHealth())) {
            state.halfHealthAttackTriggered = true;
            enterMode(ghast, state, UrGhastCombatMode.OFFENSIVE);
            beginMist(ghast, state);
            return true;
        }

        state.modeTicks++;
        if (!UrGhastCombatRules.shouldSwitchMode(
                state.modeTicks, config.modeDurationTicks.get())) {
            return false;
        }
        enterMode(ghast, state, state.mode.opposite());
        return true;
    }

    private static void enterMode(
            UrGhast ghast, UrGhastCombatState state,
            UrGhastCombatMode mode) {
        state.mode = mode;
        state.modeTicks = 0;
        state.hasPatrolDestination = false;
        state.action = UrGhastActionState.NORMAL;
        state.actionTicks = 0;
        state.actionDuration = 0;
        ghast.setCharging(false);
        clearMistTelegraphs(ghast);
        setTantrumForMode(ghast, state);
    }

    private static void setTantrumForMode(
            UrGhast ghast, UrGhastCombatState state) {
        boolean shouldBeInTantrum = state.mode == UrGhastCombatMode.OFFENSIVE;
        if (ghast.isInTantrum() != shouldBeInTantrum) {
            ghast.setInTantrum(shouldBeInTantrum);
        }
    }

    private static void tickNormal(
            UrGhast ghast, UrGhastCombatState state) {
        if (state.mode == UrGhastCombatMode.PATROL) {
            tickPatrolMovement(ghast, state);
        } else {
            chaseTarget(ghast);
        }

        LivingEntity target = retainableTarget(ghast);
        if (target == null) {
            return;
        }
        if (state.mode == UrGhastCombatMode.OFFENSIVE
                && state.mistCooldown <= 0) {
            beginMist(ghast, state);
        } else if (state.fireballCooldown <= 0
                && ghast.getSensing().hasLineOfSight(target)) {
            beginFireballWindup(ghast, state);
        }
    }

    private static void tickPatrolMovement(
            UrGhast ghast, UrGhastCombatState state) {
        if (!state.hasPatrolDestination) {
            List<BlockPos> points = patrolPoints(ghast);
            BlockPos point = points.get(Math.floorMod(
                    state.patrolIndex++, points.size()));
            state.patrolX = point.getX();
            state.patrolY = point.getY()
                    + (ghast.getRestrictionPoint() == null ? 0.0 : 20.0);
            state.patrolZ = point.getZ();
            state.hasPatrolDestination = true;
        }
        ghast.getMoveControl().setWantedPosition(
                state.patrolX, state.patrolY, state.patrolZ, 1.0);
        double deltaX = ghast.getX() - state.patrolX;
        double deltaY = ghast.getY() - state.patrolY;
        double deltaZ = ghast.getZ() - state.patrolZ;
        if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 4.0) {
            return;
        }
        state.hasPatrolDestination = false;
    }

    private static void chaseTarget(UrGhast ghast) {
        LivingEntity target = retainableTarget(ghast);
        if (target == null) {
            return;
        }
        ghast.getMoveControl().setWantedPosition(
                target.getX(),
                target.getY() + BossRefactorTwilightForestConfig.UR_GHAST
                        .offensiveHeightOffset.get(),
                target.getZ(),
                BossRefactorTwilightForestConfig.UR_GHAST
                        .offensiveSpeedMultiplier.get());
        ghast.getLookControl().setLookAt(
                target, 10.0F, ghast.getMaxHeadXRot());
    }

    private static void beginFireballWindup(
            UrGhast ghast, UrGhastCombatState state) {
        state.action = UrGhastActionState.FIREBALL_WINDUP;
        state.actionTicks = 0;
        state.actionDuration = BossRefactorTwilightForestConfig.UR_GHAST
                .fireballWindupTicks.get();
        ghast.playSound(
                ghast.getWarnSound(), 10.0F, ghast.getVoicePitch());
    }

    private static void tickFireballWindup(
            UrGhast ghast, UrGhastCombatState state) {
        LivingEntity target = retainableTarget(ghast);
        if (target == null) {
            finishAction(ghast, state);
            return;
        }
        ghast.getLookControl().setLookAt(
                target, 10.0F, ghast.getMaxHeadXRot());
        state.actionTicks++;
        ghast.setCharging(state.actionTicks * 2 >= state.actionDuration);
        if (state.actionTicks < state.actionDuration) {
            return;
        }
        fireGiantFireballs(ghast);
        state.fireballCooldown = BossRefactorTwilightForestConfig.UR_GHAST
                .fireballCooldownTicks.get();
        finishAction(ghast, state);
    }

    private static void beginMist(
            UrGhast ghast, UrGhastCombatState state) {
        state.action = UrGhastActionState.MIST_WINDUP;
        state.actionTicks = 0;
        state.actionDuration = BossRefactorTwilightForestConfig.UR_GHAST
                .mistWindupTicks.get();
        state.mistOriginX = ghast.getX();
        state.mistOriginY = findMistBaseY(
                ghast.level(), ghast.getX(), ghast.getY(), ghast.getZ());
        state.mistOriginZ = ghast.getZ();

        BlockPos destination = chooseMistDestination(ghast);
        state.mistDestinationX = destination.getX() + 0.5;
        state.mistDestinationY = destination.getY() + 1.0;
        state.mistDestinationZ = destination.getZ() + 0.5;
        ghast.setDeltaMovement(Vec3.ZERO);
        updateMistTelegraphs(ghast, state);
    }

    private static void tickMistWindup(
            UrGhast ghast, UrGhastCombatState state) {
        ghast.setDeltaMovement(Vec3.ZERO);
        emitMistParticles(ghast, state);
        updateMistTelegraphs(ghast, state);
        state.actionTicks++;
        if (state.actionTicks < state.actionDuration) {
            return;
        }

        ghast.teleportTo(
                state.mistDestinationX,
                state.mistDestinationY
                        + BossRefactorTwilightForestConfig.UR_GHAST
                                .mistTeleportHeight.get(),
                state.mistDestinationZ);
        ghast.setDeltaMovement(Vec3.ZERO);
        LivingEntity target = retainableTarget(ghast);
        if (target != null) {
            fireGiantFireballs(ghast);
            state.fireballCooldown = BossRefactorTwilightForestConfig.UR_GHAST
                    .fireballCooldownTicks.get();
        }
        state.action = UrGhastActionState.MIST_DAMAGE;
        state.actionTicks = 0;
        state.actionDuration = BossRefactorTwilightForestConfig.UR_GHAST
                .mistDamageDurationTicks.get();
        updateMistTelegraphs(ghast, state);
    }

    private static void tickMistDamage(
            UrGhast ghast, UrGhastCombatState state) {
        chaseTarget(ghast);
        emitMistParticles(ghast, state);
        updateMistTelegraphs(ghast, state);
        int interval = BossRefactorTwilightForestConfig.UR_GHAST
                .mistDamageIntervalTicks.get();
        if (UrGhastCombatRules.isIntervalPulse(
            state.actionTicks, interval)) {
            damageMistColumns(ghast, state);
        }
        state.actionTicks++;
        if (state.actionTicks < state.actionDuration) {
            return;
        }
        state.mistCooldown = BossRefactorTwilightForestConfig.UR_GHAST
                .mistCooldownTicks.get();
        finishAction(ghast, state);
    }

    private static void finishAction(
            UrGhast ghast, UrGhastCombatState state) {
        state.action = UrGhastActionState.NORMAL;
        state.actionTicks = 0;
        state.actionDuration = 0;
        ghast.setCharging(false);
        clearMistTelegraphs(ghast);
    }

    private static void fireGiantFireballs(UrGhast ghast) {
        ghast.playSound(
                ghast.getFireSound(), 10.0F, ghast.getVoicePitch());
        ghast.spitFireball();
    }

    private static void damageMistColumns(
            UrGhast ghast, UrGhastCombatState state) {
        double radius = BossRefactorTwilightForestConfig.UR_GHAST
                .mistRadius.get();
        double height = BossRefactorTwilightForestConfig.UR_GHAST
                .mistHeight.get();
        float damage = (float) UrGhastCombatRules.mistDamage(
            ghast.getAttributeValue(Attributes.ATTACK_DAMAGE),
                BossRefactorTwilightForestConfig.UR_GHAST
                        .mistDamageMultiplier.get());
        Set<Integer> damagedPlayers = new HashSet<>();
        damageMistColumn(ghast, state.mistOriginX, state.mistOriginY,
                state.mistOriginZ, radius, height, damage, damagedPlayers);
        damageMistColumn(ghast, state.mistDestinationX,
                state.mistDestinationY, state.mistDestinationZ,
                radius, height, damage, damagedPlayers);
    }

    private static void damageMistColumn(
            UrGhast ghast, double x, double y, double z,
            double radius, double height, float damage,
            Set<Integer> damagedPlayers) {
        AABB bounds = new AABB(
                x - radius, y, z - radius,
                x + radius, y + height, z + radius);
        for (Player player : ghast.level().getEntitiesOfClass(
                Player.class, bounds,
                player -> !player.isCreative() && !player.isSpectator())) {
            double deltaX = player.getX() - x;
            double deltaZ = player.getZ() - z;
            if (deltaX * deltaX + deltaZ * deltaZ > radius * radius
                    || !damagedPlayers.add(player.getId())) {
                continue;
            }
            player.hurt(ghast.damageSources().mobAttack(ghast), damage);
        }
    }

    private static void emitMistParticles(
            UrGhast ghast, UrGhastCombatState state) {
        if (!(ghast.level() instanceof ServerLevel serverLevel)
                || state.actionTicks % 2 != 0) {
            return;
        }
        double progress = state.action == UrGhastActionState.MIST_WINDUP
                ? AttackTelegraph.windupProgress(
                        state.actionTicks, state.actionDuration)
                : 1.0;
        emitMistColumn(serverLevel, ghast, state.mistOriginX,
                state.mistOriginY, state.mistOriginZ, progress);
        emitMistColumn(serverLevel, ghast, state.mistDestinationX,
                state.mistDestinationY, state.mistDestinationZ, progress);
    }

    private static void emitMistColumn(
            ServerLevel level, UrGhast ghast,
            double x, double y, double z, double progress) {
        BossRefactorTwilightForestConfig.UrGhastConfig config =
                BossRefactorTwilightForestConfig.UR_GHAST;
        double radius = config.mistRadius.get() * Math.max(0.05, progress);
        double height = config.mistHeight.get();
        int count = config.mistParticlesPerColumn.get();
        for (int index = 0; index < count; index++) {
            double angle = ghast.getRandom().nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(ghast.getRandom().nextDouble()) * radius;
            level.sendParticles(
                    CRIMSON_MIST,
                    x + Math.cos(angle) * distance,
                    y + ghast.getRandom().nextDouble() * height,
                    z + Math.sin(angle) * distance,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static void updateMistTelegraphs(
            UrGhast ghast, UrGhastCombatState state) {
        float progress = state.action == UrGhastActionState.MIST_WINDUP
                ? AttackTelegraph.windupProgress(
                        state.actionTicks, state.actionDuration)
                : 1.0F;
        UrGhastCombatAccess access = access(ghast);
        access.bossRefactorTF$setMistOriginTelegraph(mistTelegraph(
                state.mistOriginX, state.mistOriginY,
                state.mistOriginZ, progress));
        access.bossRefactorTF$setMistDestinationTelegraph(mistTelegraph(
                state.mistDestinationX, state.mistDestinationY,
                state.mistDestinationZ, progress));
    }

    private static AttackTelegraph mistTelegraph(
            double x, double y, double z, float progress) {
        return new AttackTelegraph(
                AttackTelegraphShape.CYLINDER,
                (float) x, (float) y, (float) z,
                0.0F, 1.0F,
                BossRefactorTwilightForestConfig.UR_GHAST
                        .mistHeight.get().floatValue(),
                0.0F,
                BossRefactorTwilightForestConfig.UR_GHAST
                        .mistRadius.get().floatValue(),
                progress);
    }

    private static void clearMistTelegraphs(UrGhast ghast) {
        UrGhastCombatAccess access = access(ghast);
        access.bossRefactorTF$setMistOriginTelegraph(AttackTelegraph.NONE);
        access.bossRefactorTF$setMistDestinationTelegraph(AttackTelegraph.NONE);
    }

    private static BlockPos chooseMistDestination(UrGhast ghast) {
        List<BlockPos> points = patrolPoints(ghast);
        points.sort(Comparator.comparingDouble(
                point -> -point.distToCenterSqr(
                        ghast.getX(), ghast.getY(), ghast.getZ())));
        int candidateCount = Math.min(2, points.size());
        return points.get(ghast.getRandom().nextInt(candidateCount));
    }

    private static List<BlockPos> patrolPoints(UrGhast ghast) {
        List<BlockPos> points = new ArrayList<>(ghast.getTrapLocations());
        if (points.isEmpty()) {
            BlockPos center = ghast.getLogicalScanPoint();
            points.add(center.offset(20, 0, 0));
            points.add(center.offset(0, 0, -20));
            points.add(center.offset(-20, 0, 0));
            points.add(center.offset(0, 0, 20));
        }
        points.sort(Comparator
            .comparingInt((BlockPos point) -> point.getX())
                .thenComparingInt(BlockPos::getZ)
                .thenComparingInt(BlockPos::getY));
        return points;
    }

    private static double findMistBaseY(
            Level level, double x, double referenceY, double z) {
        int minimumY = Math.max(level.getMinBuildHeight(),
                Mth.floor(referenceY) - 64);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(
                Mth.floor(x), Mth.floor(referenceY), Mth.floor(z));
        while (cursor.getY() > minimumY
                && level.getBlockState(cursor).getCollisionShape(
                        level, cursor).isEmpty()) {
            cursor.move(0, -1, 0);
        }
        return cursor.getY() + 1.0;
    }

    private static LivingEntity retainableTarget(UrGhast ghast) {
        LivingEntity target = ghast.getTarget();
        if (target == null || !target.isAlive() || target.isRemoved()
                || ghast.isAlliedTo(target)
                || target instanceof Player player
                        && (player.isCreative() || player.isSpectator())) {
            return null;
        }
        return target;
    }

        private static boolean isFireballTarget(
            UrGhast owner, LivingEntity target) {
        return target != owner
            && target.isAlive()
            && !owner.isAlliedTo(target)
            && (!(target instanceof Player player)
                || (!player.isCreative() && !player.isSpectator()));
        }

        private static double distanceFromPointToAabb(Vec3 point, AABB box) {
        double deltaX = point.x < box.minX
            ? box.minX - point.x
            : point.x > box.maxX ? point.x - box.maxX : 0.0;
        double deltaY = point.y < box.minY
            ? box.minY - point.y
            : point.y > box.maxY ? point.y - box.maxY : 0.0;
        double deltaZ = point.z < box.minZ
            ? box.minZ - point.z
            : point.z > box.maxZ ? point.z - box.maxZ : 0.0;
        return UrGhastCombatRules.aabbDistanceFromCenter(
            deltaX, deltaY, deltaZ);
        }

    private static UrGhastCombatAccess access(UrGhast ghast) {
        return (UrGhastCombatAccess) ghast;
    }

    private static UrGhastCombatState state(UrGhast ghast) {
        return access(ghast).bossRefactorTF$getUrGhastCombatState();
    }
}