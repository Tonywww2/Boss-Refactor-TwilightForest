package com.tonywww.bossrefactortf.boss.hydra;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.TwilightBossParryIntegration;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.registry.BossRefactorTwilightForestEntities;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.boss.Hydra;
import twilightforest.entity.boss.HydraHeadContainer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class HydraCombat {
    private HydraCombat() {
    }

    public static boolean customCombatEnabled() {
        return BossRefactorTwilightForestConfig.HYDRA.enabled.get();
    }

    public static void enforceCustomAttackOwnership(Hydra hydra) {
        if (hydra.level().isClientSide() || !customCombatEnabled()) {
            return;
        }
        HydraCombatState state = state(hydra);
        if (state.actionState == HydraActionState.IDLE) {
            return;
        }
        for (HydraHeadContainer head : activeContainers(hydra)) {
            head.isSecondaryAttacking = false;
            if (head.isAttacking()) {
                head.endCurrentAction();
            }
        }
    }

    public static boolean shouldSuppressVanillaAttackSelection(Hydra hydra) {
        if (!customCombatEnabled()) {
            return false;
        }
        HydraCombatState state = state(hydra);
        if (state.actionState != HydraActionState.IDLE) {
            return true;
        }
        if (!state.combatInitialized || hasVanillaAttackInProgress(hydra)) {
            return false;
        }
        return state.fireLordCooldown <= 1
            || state.evilFireballCooldown <= 1
            || state.ordinaryAttackCooldown <= 1;
    }

    public static void tick(Hydra hydra) {
        if (hydra.level().isClientSide()) {
            return;
        }
        if (!customCombatEnabled()) {
            clearWhenDisabled(hydra);
            return;
        }

        HydraCombatState state = state(hydra);
        initializeCombat(state);
        synchronizeBaseAttackDamage(hydra);
        decrementCooldowns(state);
        updateSecondPhase(hydra, state);
        tickBeams(hydra, state);
        tickSecondPhaseBeams(hydra, state);

        switch (state.actionState) {
            case IDLE -> tickIdle(hydra, state);
            case WINDUP -> tickWindup(hydra, state);
            case RECOVERY -> tickRecovery(hydra, state);
            case FIRE_LORD -> tickFireLord(hydra, state);
            case EVIL_FIREBALL_WINDUP -> tickEvilFireballWindup(
                hydra, state);
        }

        synchronizeHeadTargets(hydra, state);
        synchronizeVisualState(hydra, state);
        synchronizeTelegraphs(hydra, state);
        synchronizeParryWindow(hydra);
    }

    public static boolean isCurrentAttackParryable(Hydra hydra) {
        HydraCombatState state = state(hydra);
        return state.actionState == HydraActionState.WINDUP
            && state.skill.isCustomMelee();
    }

    public static boolean acceptParry(Hydra hydra) {
        if (!isCurrentAttackParryable(hydra)) {
            return false;
        }
        HydraCombatState state = state(hydra);
        long gameTime = hydra.level().getGameTime();
        if (state.lastParryGameTime == gameTime) {
            return true;
        }
        state.lastParryGameTime = gameTime;
        interruptCurrentHead(hydra, state);
        return true;
    }

    public static void restoreVisualState(Hydra hydra) {
        if (!customCombatEnabled()) {
            clearWhenDisabled(hydra);
            return;
        }
        HydraCombatState state = state(hydra);
        synchronizeVisualState(hydra, state);
        synchronizeTelegraphs(hydra, state);
    }

    private static void initializeCombat(HydraCombatState state) {
        if (state.combatInitialized) {
            return;
        }
        state.combatInitialized = true;
        state.ordinaryAttackCooldown = BossRefactorTwilightForestConfig.HYDRA
                .ordinaryAttackIntervalTicks.get();
        state.evilFireballCooldown = BossRefactorTwilightForestConfig.HYDRA
            .evilFireballCooldownTicks.get();
        state.fireLordCooldown = BossRefactorTwilightForestConfig.HYDRA
                .fireLordInitialDelayTicks.get();
        state.secondPhaseBeamCooldown = BossRefactorTwilightForestConfig.HYDRA
                .secondPhaseBeamIntervalTicks.get();
    }

    private static void tickIdle(Hydra hydra, HydraCombatState state) {
        LivingEntity target = hydra.getTarget();
        if (!isRetainableTarget(hydra, target)) {
            return;
        }
        if (hasVanillaAttackInProgress(hydra)) {
            return;
        }
        if (state.fireLordCooldown <= 0) {
            beginFireLord(hydra, target, state);
            return;
        }
        if (state.evilFireballCooldown <= 0) {
            int headIndex = randomIdleHead(hydra);
            if (headIndex >= 0) {
                beginEvilFireball(hydra, target, state, headIndex);
            }
            return;
        }
        if (state.ordinaryAttackCooldown <= 0) {
            int headIndex = randomIdleHead(hydra);
            if (headIndex >= 0) {
                beginOrdinaryAttack(
                        hydra, target, state,
                        HydraCombatRules.customMeleeSkill(
                                hydra.getRandom().nextInt()),
                        headIndex);
            }
        }
    }

    private static void tickWindup(Hydra hydra, HydraCombatState state) {
        if (!hasUsableHead(hydra, state.activeHeadIndex)
                || !isRetainableTarget(hydra, hydra.getTarget())) {
            beginRecovery(hydra, state);
            return;
        }
        state.actionTicks++;
        if (state.actionTicks < state.actionDuration) {
            return;
        }
        boolean defended = performOrdinaryAttack(hydra, state);
        if (defended) {
            interruptCurrentHead(hydra, state);
            return;
        }
        beginRecovery(hydra, state);
    }

    private static void tickRecovery(Hydra hydra, HydraCombatState state) {
        state.actionTicks++;
        if (state.actionTicks >= state.actionDuration) {
            enterIdle(state);
        }
    }

    private static void tickEvilFireballWindup(
            Hydra hydra, HydraCombatState state) {
        if (!hasUsableHead(hydra, state.activeHeadIndex)) {
            beginRecovery(hydra, state);
            return;
        }
        state.actionTicks++;
        if (state.actionTicks < state.actionDuration) {
            return;
        }
        Vec3 origin = attackOrigin(hydra, state.activeHeadIndex);
        Vec3 direction = new Vec3(
            state.lockedX - origin.x,
            state.lockedY + 1.0 - origin.y,
            state.lockedZ - origin.z);
        if (direction.lengthSqr() < 1.0E-6) {
            direction = new Vec3(
                state.directionX, 0.0, state.directionZ);
        }
        direction = direction.normalize();
        if (!(hydra.getTarget() instanceof Player target)
                || !isRetainableTarget(hydra, target)) {
            beginRecovery(hydra, state);
            return;
        }
        HydraEvilFireball fireball = new HydraEvilFireball(
            BossRefactorTwilightForestEntities.HYDRA_EVIL_FIREBALL.get(),
            hydra.level());
        fireball.initialize(
            hydra, target, origin.add(direction.scale(2.0)), direction,
            state.secondPhase);
        hydra.level().addFreshEntity(fireball);
        hydra.playSound(SoundEvents.FIRECHARGE_USE, 3.0F, 0.55F);
        beginRecovery(hydra, state);
    }

    private static void tickFireLord(Hydra hydra, HydraCombatState state) {
        state.actionTicks++;
        double radius = fireLordRadius(state);
        emitFireLordParticles(hydra, state, radius);
        int damageInterval = BossRefactorTwilightForestConfig.HYDRA
                .fireLordDamageIntervalTicks.get();
        if (state.actionTicks % damageInterval == 0) {
            float multiplier = HydraCombatRules.fireLordMultiplier(
                BossRefactorTwilightForestConfig.HYDRA
                    .fireLordDamageMultiplier.get().floatValue(),
                BossRefactorTwilightForestConfig.HYDRA
                    .fireLordDamagePerHead.get().floatValue(),
                countActiveHeads(hydra));
            damageFireCircle(
                    hydra,
                    new Vec3(state.lockedX, state.lockedY, state.lockedZ),
                radius, multiplier);
        }
        int beamInterval = BossRefactorTwilightForestConfig.HYDRA
                .fireLordBeamIntervalTicks.get();
        if (state.actionTicks % beamInterval == 0) {
            spawnTargetedBeams(
                    hydra, state,
                    new Vec3(state.lockedX, state.lockedY, state.lockedZ),
                    BossRefactorTwilightForestConfig.HYDRA
                            .fireLordBeamCount.get(),
                    BossRefactorTwilightForestConfig.HYDRA
                            .fireLordBeamScatter.get(),
                    BossRefactorTwilightForestConfig.HYDRA
                            .fireLordBeamRadius.get(),
                    BossRefactorTwilightForestConfig.HYDRA
                            .fireLordBeamWindupTicks.get());
        }
        if (state.actionTicks >= state.actionDuration) {
            beginRecovery(hydra, state);
        }
    }

    private static void beginOrdinaryAttack(
            Hydra hydra, LivingEntity target, HydraCombatState state,
            HydraSkill skill, int headIndex) {
        if (!skill.isCustomMelee() || headIndex < 0) {
            beginRecovery(hydra, state);
            return;
        }
        state.skill = skill;
        state.activeHeadIndex = headIndex;
        state.recoveryVisualProgress = 0.0F;
        lockTarget(hydra, target, state);
        hydra.hc[headIndex].setTargetEntity(target);
        enterState(state, HydraActionState.WINDUP, windupTicks(skill));
    }

    private static void beginFireLord(
            Hydra hydra, LivingEntity target, HydraCombatState state) {
        lockDirection(hydra, target.position(), state);
        double distance = BossRefactorTwilightForestConfig.HYDRA
                .fireLordGroundDistance.get();
        double centerX = hydra.getX() + state.directionX * distance;
        double centerZ = hydra.getZ() + state.directionZ * distance;
        state.lockedX = centerX;
        state.lockedY = groundY(
            hydra, centerX, centerZ, target.getY());
        state.lockedZ = centerZ;
        state.skill = HydraSkill.FIRE_LORD;
        state.activeHeadIndex = -1;
        state.recoveryVisualProgress = 0.0F;
        state.fireLordCooldown = BossRefactorTwilightForestConfig.HYDRA
                .fireLordCooldownTicks.get();
        enterState(
                state, HydraActionState.FIRE_LORD,
                BossRefactorTwilightForestConfig.HYDRA
                        .fireLordDurationTicks.get());
        hydra.playSound(SoundEvents.ENDER_DRAGON_GROWL, 3.0F, 0.7F);
    }

    private static void beginEvilFireball(
            Hydra hydra, LivingEntity target, HydraCombatState state,
            int headIndex) {
        if (headIndex < 0) {
            beginRecovery(hydra, state);
            return;
        }
        state.skill = HydraSkill.EVIL_FIREBALL;
        state.activeHeadIndex = headIndex;
        state.recoveryVisualProgress = 0.0F;
        state.evilFireballCooldown = BossRefactorTwilightForestConfig.HYDRA
                .evilFireballCooldownTicks.get();
        lockTarget(hydra, target, state);
        hydra.hc[headIndex].setTargetEntity(target);
        enterState(
                state, HydraActionState.EVIL_FIREBALL_WINDUP,
                BossRefactorTwilightForestConfig.HYDRA
                        .evilFireballWindupTicks.get());
        hydra.playSound(SoundEvents.BLAZE_SHOOT, 2.0F, 0.45F);
    }

    private static void beginRecovery(
            Hydra hydra, HydraCombatState state) {
        beginRecovery(hydra, state, BossRefactorTwilightForestConfig.HYDRA
            .recoveryTicks.get());
    }

    private static void beginRecovery(
            Hydra hydra, HydraCombatState state, int recoveryTicks) {
        state.recoveryVisualProgress = state.skill == HydraSkill.NONE
            ? 0.0F
            : AttackTelegraph.windupProgress(
                state.actionTicks, state.actionDuration);
        clearHeadTarget(hydra, state.activeHeadIndex);
        state.ordinaryAttackCooldown = BossRefactorTwilightForestConfig.HYDRA
                .ordinaryAttackIntervalTicks.get();
        if (recoveryTicks <= 0) {
            enterIdle(state);
        } else {
            enterState(state, HydraActionState.RECOVERY, recoveryTicks);
        }
    }

    private static void enterIdle(HydraCombatState state) {
        enterState(state, HydraActionState.IDLE, 0);
        state.skill = HydraSkill.NONE;
        state.activeHeadIndex = -1;
        state.recoveryVisualProgress = 0.0F;
    }

    private static void interruptCurrentHead(
            Hydra hydra, HydraCombatState state) {
        clearHeadTarget(hydra, state.activeHeadIndex);
        hydra.playSound(SoundEvents.SHIELD_BLOCK, 2.0F, 0.8F);
        beginRecovery(hydra, state, Math.max(
            BossRefactorTwilightForestConfig.HYDRA.recoveryTicks.get(),
            BossRefactorTwilightForestConfig.HYDRA.parryFeedbackTicks.get()));
        synchronizeVisualState(hydra, state);
        synchronizeTelegraphs(hydra, state);
        closeParryWindow(hydra);
    }

    private static boolean performOrdinaryAttack(
            Hydra hydra, HydraCombatState state) {
        Vec3 origin = attackOrigin(hydra, state.activeHeadIndex);
        double radius = attackRadius(state.skill);
        double verticalRange = BossRefactorTwilightForestConfig.HYDRA
                .meleeVerticalRange.get();
        AABB bounds = new AABB(
                origin.x - radius, origin.y - verticalRange,
                origin.z - radius,
                origin.x + radius, origin.y + verticalRange,
                origin.z + radius);
        float damage = attackDamage(hydra) * damageMultiplier(state.skill);
        DamageSource source = hydra.damageSources().mobAttack(hydra);
        for (LivingEntity target : combatTargets(hydra, bounds)) {
            if (!isInsideOrdinaryArea(target, origin, state, radius)) {
                continue;
            }
            if (hurtWithShieldCheck(target, source, damage)) {
                return true;
            }
        }
        if (state.skill == HydraSkill.SLAM) {
            hydra.playSound(SoundEvents.GENERIC_EXPLODE, 2.0F, 0.8F);
            ((ServerLevel) hydra.level()).sendParticles(
                    ParticleTypes.EXPLOSION,
                    origin.x, origin.y, origin.z,
                    BossRefactorTwilightForestConfig.HYDRA
                        .slamImpactParticles.get(),
                    radius * 0.4, 0.3, radius * 0.4, 0.04);
        } else {
            hydra.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0F, 0.7F);
        }
        return false;
    }

    private static boolean isInsideOrdinaryArea(
            LivingEntity target, Vec3 origin,
            HydraCombatState state, double radius) {
        if (state.skill == HydraSkill.SLAM) {
            return HydraCombatRules.isInsideCircle(
                    target.getX(), target.getZ(),
                    origin.x, origin.z, radius);
        }
        return HydraCombatRules.isInsideArc(
                target.getX(), target.getZ(),
                origin.x, origin.z,
                state.directionX, state.directionZ,
                radius, halfAngle(state.skill));
    }

    private static void updateSecondPhase(
            Hydra hydra, HydraCombatState state) {
        if (!state.secondPhase && HydraCombatRules.isSecondPhase(
            hydra.getHealth(), hydra.getMaxHealth(), 0.5)) {
            state.secondPhase = true;
            state.secondPhaseBeamCooldown = BossRefactorTwilightForestConfig
                    .HYDRA.secondPhaseBeamIntervalTicks.get();
        }
        if (state.secondPhase && !state.secondPhaseHeadsActivated) {
            access(hydra).bossRefactorTF$activateNumberOfHeads(
                Math.min(9, countActiveHeads(hydra) + 2));
            state.secondPhaseHeadsActivated = true;
        }
    }

    private static void tickSecondPhaseBeams(
            Hydra hydra, HydraCombatState state) {
        if (!state.secondPhase || state.secondPhaseBeamCooldown > 0
                || !isRetainableTarget(hydra, hydra.getTarget())) {
            return;
        }
        Vec3 center = beamCenter(hydra, state);
        spawnTargetedBeams(
                hydra, state, center,
                BossRefactorTwilightForestConfig.HYDRA
                        .secondPhaseBeamCount.get(),
                BossRefactorTwilightForestConfig.HYDRA
                        .secondPhaseBeamScatter.get(),
                BossRefactorTwilightForestConfig.HYDRA
                        .secondPhaseBeamRadius.get(),
                BossRefactorTwilightForestConfig.HYDRA
                        .secondPhaseBeamWindupTicks.get());
        state.secondPhaseBeamCooldown = BossRefactorTwilightForestConfig.HYDRA
                .secondPhaseBeamIntervalTicks.get();
    }

        private static void spawnTargetedBeams(
            Hydra hydra, HydraCombatState state, Vec3 center,
            int count, double scatter, double radius, int windupTicks) {
        double verticalRange = BossRefactorTwilightForestConfig.HYDRA
            .fireVerticalRange.get();
        AABB bounds = new AABB(
            center.x - scatter, center.y - verticalRange,
            center.z - scatter,
            center.x + scatter, center.y + verticalRange,
            center.z + scatter);
        List<LivingEntity> targets = combatTargets(hydra, bounds);
        LivingEntity primaryTarget = hydra.getTarget();
        if (isRetainableTarget(hydra, primaryTarget)
            && !targets.contains(primaryTarget)) {
            targets.add(primaryTarget);
        }
        for (LivingEntity target : targets) {
            addBeam(
                hydra, state, target.getX(), target.getZ(), target.getY(),
                radius, windupTicks);
        }
        int extraCount = Math.max(0, count - targets.size());
        for (int index = 0; index < extraCount; index++) {
            Vec3 anchor = targets.isEmpty()
                ? center
                : targets.get(index % targets.size()).position();
            double angle = hydra.getRandom().nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(hydra.getRandom().nextDouble())
                    * scatter;
            addBeam(
                hydra, state,
                anchor.x + Math.cos(angle) * distance,
                anchor.z + Math.sin(angle) * distance,
                anchor.y, radius, windupTicks);
        }
    }

        private static void spawnScatteredBeams(
            Hydra hydra, HydraCombatState state, Vec3 center,
            int count, double scatter, double radius, int windupTicks) {
        for (int index = 0; index < count; index++) {
            double angle = hydra.getRandom().nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(hydra.getRandom().nextDouble())
                * scatter;
            addBeam(
                hydra, state,
                center.x + Math.cos(angle) * distance,
                center.z + Math.sin(angle) * distance,
                center.y, radius, windupTicks);
        }
        }

        private static void addBeam(
            Hydra hydra, HydraCombatState state,
            double x, double z, double preferredY,
            double radius, int windupTicks) {
        state.beams.add(new HydraBeam(
            UUID.randomUUID(), x, groundY(hydra, x, z, preferredY), z,
            radius, windupTicks));
        }

        public static void damageEvilFireballAura(
            HydraEvilFireball fireball, Hydra hydra) {
        BossRefactorTwilightForestConfig.HydraConfig config =
            BossRefactorTwilightForestConfig.HYDRA;
        damageEvilFireArea(
            fireball, hydra, fireball.position(),
            config.evilFireballAuraRadius.get(),
            config.evilFireballAuraDamageMultiplier.get().floatValue());
        }

        public static void explodeEvilFireball(
            HydraEvilFireball fireball, Hydra hydra,
            Vec3 center, boolean phaseTwo) {
        if (fireball.isRemoved()) {
            return;
        }
        BossRefactorTwilightForestConfig.HydraConfig config =
            BossRefactorTwilightForestConfig.HYDRA;
        damageEvilFireArea(
            fireball, hydra, center,
            config.evilFireballExplosionRadius.get(),
            config.evilFireballExplosionDamageMultiplier.get()
                .floatValue());
        ServerLevel level = (ServerLevel) hydra.level();
        level.sendParticles(
            ParticleTypes.EXPLOSION_EMITTER,
            center.x, center.y, center.z,
            1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(
            null, center.x, center.y, center.z,
            SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE,
            4.0F, 0.75F + hydra.getRandom().nextFloat() * 0.15F);
        fireball.gameEvent(GameEvent.PROJECTILE_LAND);
        if (phaseTwo) {
            HydraCombatState state = state(hydra);
            spawnScatteredBeams(
                hydra, state, center,
                config.evilFireballPhaseTwoBeamCount.get(),
                config.evilFireballPhaseTwoBeamScatter.get(),
                config.evilFireballPhaseTwoBeamRadius.get(),
                config.evilFireballPhaseTwoBeamWindupTicks.get());
        }
        fireball.discard();
        }

        private static void damageEvilFireArea(
            HydraEvilFireball fireball, Hydra hydra,
            Vec3 center, double radius, float damageMultiplier) {
        AABB bounds = new AABB(
            center.x - radius, center.y - radius, center.z - radius,
            center.x + radius, center.y + radius, center.z + radius);
        float damage = attackDamage(hydra) * damageMultiplier;
        int igniteTicks = BossRefactorTwilightForestConfig.HYDRA
            .evilFireballIgniteTicks.get();
        for (LivingEntity target : combatTargets(hydra, bounds)) {
            if (distanceFromPointToAabb(
                center, target.getBoundingBox()) > radius) {
            continue;
            }
            hurtWithMixedFire(target, fireball, hydra, damage);
            target.setRemainingFireTicks(Math.max(
                target.getRemainingFireTicks(), igniteTicks));
        }
        }

        private static double distanceFromPointToAabb(
            Vec3 point, AABB bounds) {
        double deltaX = Math.max(
            Math.max(bounds.minX - point.x, 0.0),
            point.x - bounds.maxX);
        double deltaY = Math.max(
            Math.max(bounds.minY - point.y, 0.0),
            point.y - bounds.maxY);
        double deltaZ = Math.max(
            Math.max(bounds.minZ - point.z, 0.0),
            point.z - bounds.maxZ);
        return Math.sqrt(
            deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        }

    private static void tickBeams(Hydra hydra, HydraCombatState state) {
        if (state.beams.isEmpty()) {
            return;
        }
        Iterator<HydraBeam> iterator = state.beams.iterator();
        while (iterator.hasNext()) {
            HydraBeam beam = iterator.next();
            emitBeamParticles((ServerLevel) hydra.level(), beam);
            beam.remainingWindupTicks--;
            if (beam.remainingWindupTicks > 0) {
                continue;
            }
            damageFireCircle(
                    hydra, new Vec3(beam.x, beam.y, beam.z), beam.radius,
                    BossRefactorTwilightForestConfig.HYDRA
                            .beamDamageMultiplier.get().floatValue());
            ((ServerLevel) hydra.level()).sendParticles(
                    ParticleTypes.FLAME,
                    beam.x, beam.y + 1.0, beam.z,
                    BossRefactorTwilightForestConfig.HYDRA
                        .beamImpactParticles.get(),
                    beam.radius * 0.7, 2.5,
                    beam.radius * 0.7, 0.08);
            hydra.playSound(SoundEvents.FIRECHARGE_USE, 1.5F, 0.8F);
            iterator.remove();
        }
    }

    private static void damageFireCircle(
            Hydra hydra, Vec3 center, double radius,
            float damageMultiplier) {
        double verticalRange = BossRefactorTwilightForestConfig.HYDRA
                .fireVerticalRange.get();
        AABB bounds = new AABB(
                center.x - radius, center.y - verticalRange,
                center.z - radius,
                center.x + radius, center.y + verticalRange,
                center.z + radius);
        float damage = attackDamage(hydra) * damageMultiplier;
        int igniteTicks = BossRefactorTwilightForestConfig.HYDRA
                .fireIgniteTicks.get();
        for (LivingEntity target : combatTargets(hydra, bounds)) {
            if (!HydraCombatRules.isInsideCircle(
                    target.getX(), target.getZ(),
                    center.x, center.z, radius)) {
                continue;
            }
            hurtWithMixedFire(target, hydra, hydra, damage);
            target.setRemainingFireTicks(Math.max(
                    target.getRemainingFireTicks(), igniteTicks));
        }
    }

    public static boolean hurtWithMixedFire(
            net.minecraft.world.entity.Entity target,
            net.minecraft.world.entity.Entity directEntity,
            Hydra hydra, float totalDamage) {
        return HydraDamageTypes.hurtMixedFire(
                target,
                HydraDamageTypes.physical(directEntity, hydra),
                HydraDamageTypes.evilFire(directEntity, hydra),
                totalDamage,
                BossRefactorTwilightForestConfig.HYDRA
                        .firePhysicalDamageFraction.get());
    }

    private static void emitFireLordParticles(
            Hydra hydra, HydraCombatState state, double radius) {
        int count = BossRefactorTwilightForestConfig.HYDRA
                .fireLordParticlesPerHead.get();
        if (count <= 0) {
            return;
        }
        ServerLevel level = (ServerLevel) hydra.level();
        for (HydraHeadContainer head : activeContainers(hydra)) {
            Vec3 start = head.headEntity.position().add(
                    0.0, head.headEntity.getBbHeight() * 0.5, 0.0);
            double angle = hydra.getRandom().nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(hydra.getRandom().nextDouble())
                    * radius;
            Vec3 end = new Vec3(
                    state.lockedX + Math.cos(angle) * distance,
                    state.lockedY + 0.2,
                    state.lockedZ + Math.sin(angle) * distance);
            Vec3 velocity = end.subtract(start).normalize().scale(0.08);
            for (int index = 1; index <= count; index++) {
                Vec3 point = start.lerp(end, index / (double) count);
                level.sendParticles(
                        ParticleTypes.FLAME,
                        point.x, point.y, point.z,
                        0, velocity.x, velocity.y, velocity.z, 1.0);
            }
            if (state.actionTicks % 3 == 0) {
                Vec3 core = start.lerp(end, 0.7);
                level.sendParticles(
                        ParticleTypes.LAVA,
                        core.x, core.y, core.z,
                        1, 0.08, 0.08, 0.08, 0.0);
            }
        }
    }

    private static void emitBeamParticles(
            ServerLevel level, HydraBeam beam) {
        int count = BossRefactorTwilightForestConfig.HYDRA
                .beamParticlesPerTick.get();
        double height = BossRefactorTwilightForestConfig.HYDRA
            .beamVisualHeight.get();
        double elapsed = beam.totalWindupTicks
            - beam.remainingWindupTicks;
        double progress = beam.totalWindupTicks <= 1
            ? 1.0
            : Mth.clamp(
                elapsed / (beam.totalWindupTicks - 1.0),
                0.0, 1.0);
        double rise = 1.0 - Math.pow(1.0 - progress, 3.0);
        double frontY = beam.y + height * rise;
        double trailLength = Math.min(height * 0.28, 5.0);
        for (int index = 0; index < count; index++) {
            double trail = count <= 1
                ? 0.0 : index / (double) (count - 1);
            double angle = trail * Math.PI * 4.0
                + progress * Math.PI * 2.0;
            double spiralRadius = beam.radius * (0.18 + trail * 0.3);
            double particleY = Math.max(
                beam.y,
                frontY - trail * trailLength);
            level.sendParticles(
                    ParticleTypes.FLAME,
                    beam.x + Math.cos(angle) * spiralRadius,
                particleY,
                    beam.z + Math.sin(angle) * spiralRadius,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
        int lavaCount = BossRefactorTwilightForestConfig.HYDRA
            .beamLavaParticles.get();
        if (lavaCount > 0 && beam.remainingWindupTicks % 3 == 0) {
            level.sendParticles(
                    ParticleTypes.LAVA,
                beam.x, frontY, beam.z,
                lavaCount, beam.radius * 0.22, 0.15,
                beam.radius * 0.22, 0.0);
        }
    }

    private static void synchronizeHeadTargets(
            Hydra hydra, HydraCombatState state) {
        LivingEntity target = hydra.getTarget();
        if (state.actionState == HydraActionState.FIRE_LORD) {
            for (HydraHeadContainer head : activeContainers(hydra)) {
                head.setTargetEntity(isRetainableTarget(hydra, target)
                        ? target : null);
            }
        } else if ((state.actionState == HydraActionState.WINDUP
            || state.actionState
                == HydraActionState.EVIL_FIREBALL_WINDUP)
                && hasUsableHead(hydra, state.activeHeadIndex)) {
            hydra.hc[state.activeHeadIndex].setTargetEntity(target);
        }
    }

    private static void synchronizeVisualState(
            Hydra hydra, HydraCombatState state) {
        float progress = state.actionState == HydraActionState.RECOVERY
            ? state.recoveryVisualProgress * (1.0F
                - AttackTelegraph.windupProgress(
                    state.actionTicks, state.actionDuration))
            : AttackTelegraph.windupProgress(
                state.actionTicks, state.actionDuration);
        access(hydra).bossRefactorTF$setHydraVisualState(
                state.skill, state.activeHeadIndex, progress,
            state.actionState == HydraActionState.RECOVERY,
            state.recoveryVisualProgress,
                state.lockedX, state.lockedY, state.lockedZ,
                state.directionX, state.directionZ);
    }

    private static void synchronizeTelegraphs(
            Hydra hydra, HydraCombatState state) {
        AttackTelegraph primary = primaryTelegraph(hydra, state);
        List<AttackTelegraph> beamTelegraphs = new ArrayList<>(
                state.beams.size());
        for (HydraBeam beam : state.beams) {
            beamTelegraphs.add(new AttackTelegraph(
                    AttackTelegraphShape.CIRCLE,
                    (float) beam.x, (float) beam.y, (float) beam.z,
                    0.0F, 1.0F, 0.0F, 0.0F,
                    (float) beam.radius,
                    AttackTelegraph.windupProgress(
                            beam.totalWindupTicks - beam.remainingWindupTicks,
                            beam.totalWindupTicks)));
        }
        access(hydra).bossRefactorTF$setHydraTelegraphs(
                primary, beamTelegraphs);
    }

    private static AttackTelegraph primaryTelegraph(
            Hydra hydra, HydraCombatState state) {
        if (state.actionState == HydraActionState.FIRE_LORD) {
            return new AttackTelegraph(
                    AttackTelegraphShape.CIRCLE,
                    (float) state.lockedX,
                    (float) state.lockedY,
                    (float) state.lockedZ,
                    (float) state.directionX,
                    (float) state.directionZ,
                    0.0F, 0.0F,
                    (float) fireLordRadius(state),
                    AttackTelegraph.windupProgress(
                            state.actionTicks, state.actionDuration));
        }
        if (state.actionState != HydraActionState.WINDUP
            || !state.skill.isCustomMelee()) {
            return AttackTelegraph.NONE;
        }
        Vec3 origin = attackOrigin(hydra, state.activeHeadIndex);
        float progress = AttackTelegraph.windupProgress(
                state.actionTicks, state.actionDuration);
        if (state.skill == HydraSkill.SLAM) {
            return new AttackTelegraph(
                    AttackTelegraphShape.CIRCLE,
                    (float) origin.x, (float) origin.y, (float) origin.z,
                    (float) state.directionX, (float) state.directionZ,
                    0.0F, 0.0F,
                    (float) attackRadius(state.skill), progress);
        }
        return new AttackTelegraph(
                AttackTelegraphShape.ARC,
                (float) origin.x, (float) origin.y, (float) origin.z,
                (float) state.directionX, (float) state.directionZ,
                (float) attackRadius(state.skill),
                (float) halfAngle(state.skill), 0.0F, progress);
    }

    private static void synchronizeParryWindow(Hydra hydra) {
        if (!(hydra instanceof TwilightBossParryAccess parryAccess)) {
            return;
        }
        var bridge = TwilightBossParryIntegration.bridge();
        if (!bridge.isAvailable()) {
            return;
        }
        if (!parryAccess.bossRefactorTF$isCurrentAttackParryable()) {
            closeParryWindow(hydra);
        } else if (!bridge.isParryable(hydra)
                && !bridge.isParried(hydra)
                && !bridge.openWindow(hydra)) {
            BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to open the SenDimS parry window for Hydra {}",
                    hydra.getUUID());
        }
    }

    private static void closeParryWindow(Hydra hydra) {
        var bridge = TwilightBossParryIntegration.bridge();
        if (bridge.isAvailable() && bridge.isParryable(hydra)
                && !bridge.closeWindow(hydra)) {
            BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to close the SenDimS parry window for Hydra {}",
                    hydra.getUUID());
        }
    }

    private static void clearWhenDisabled(Hydra hydra) {
        HydraCombatState state = state(hydra);
        boolean hadCustomState = state.combatInitialized
                || state.actionState != HydraActionState.IDLE
                || state.skill != HydraSkill.NONE
                || !state.beams.isEmpty();
        if (!hadCustomState) {
            return;
        }
        state.actionState = HydraActionState.IDLE;
        state.skill = HydraSkill.NONE;
        state.actionTicks = 0;
        state.actionDuration = 0;
        state.activeHeadIndex = -1;
        state.combatInitialized = false;
        state.recoveryVisualProgress = 0.0F;
        state.beams.clear();
        access(hydra).bossRefactorTF$setHydraVisualState(
                HydraSkill.NONE, -1, 0.0F,
            false, 0.0F,
                hydra.getX(), hydra.getY(), hydra.getZ(), 0.0, 1.0);
        access(hydra).bossRefactorTF$setHydraTelegraphs(
                AttackTelegraph.NONE, List.of());
        closeParryWindow(hydra);
    }

    private static void synchronizeBaseAttackDamage(Hydra hydra) {
        var attackDamage = hydra.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return;
        }
        double configuredDamage = BossRefactorTwilightForestConfig.HYDRA
                .baseAttackDamage.get();
        if (Double.compare(
                attackDamage.getBaseValue(), configuredDamage) != 0) {
            attackDamage.setBaseValue(configuredDamage);
        }
    }

    private static boolean hurtWithShieldCheck(
            LivingEntity target, DamageSource source, float damage) {
        ItemStack usedShield = target instanceof ServerPlayer player
                ? player.getUseItem().copy()
                : ItemStack.EMPTY;
        boolean shieldBlocked = target instanceof ServerPlayer player
                && usedShield.getItem() instanceof ShieldItem
                && player.isDamageSourceBlocked(source);
        target.hurt(source, damage);
        if (shieldBlocked) {
            int cooldown = BossRefactorTwilightForestConfig.HYDRA
                    .shieldCooldownTicks.get();
            if (cooldown > 0) {
                ((ServerPlayer) target).getCooldowns().addCooldown(
                        usedShield.getItem(), cooldown);
            }
            ((ServerPlayer) target).stopUsingItem();
        }
        return shieldBlocked;
    }

    private static List<LivingEntity> combatTargets(
            Hydra hydra, AABB bounds) {
        return hydra.level().getEntitiesOfClass(
                LivingEntity.class, bounds,
                target -> isCombatTarget(hydra, target));
    }

    private static boolean isCombatTarget(
            Hydra hydra, LivingEntity target) {
        return target != hydra
                && target.isAlive()
                && !hydra.isAlliedTo(target)
                && (!(target instanceof Player player)
                        || (!player.isCreative() && !player.isSpectator()));
    }

    private static boolean isRetainableTarget(
            Hydra hydra, @Nullable LivingEntity target) {
        return target != null
                && !target.isRemoved()
                && isCombatTarget(hydra, target);
    }

    private static void lockTarget(
            Hydra hydra, LivingEntity target, HydraCombatState state) {
        state.lockedX = target.getX();
        state.lockedY = target.getY();
        state.lockedZ = target.getZ();
        lockDirection(hydra, target.position(), state);
    }

    private static void lockDirection(
            Hydra hydra, Vec3 target, HydraCombatState state) {
        Vec3 direction = target.subtract(hydra.position())
                .multiply(1.0, 0.0, 1.0);
        if (direction.lengthSqr() < 1.0E-6) {
            direction = hydra.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        if (direction.lengthSqr() < 1.0E-6) {
            direction = new Vec3(0.0, 0.0, 1.0);
        }
        direction = direction.normalize();
        state.directionX = direction.x;
        state.directionZ = direction.z;
    }

    private static Vec3 beamCenter(
            Hydra hydra, HydraCombatState state) {
        LivingEntity target = hydra.getTarget();
        if (isRetainableTarget(hydra, target)) {
            return target.position();
        }
        double distance = BossRefactorTwilightForestConfig.HYDRA
                .fireLordGroundDistance.get();
        return new Vec3(
                hydra.getX() + state.directionX * distance,
                hydra.getY(),
                hydra.getZ() + state.directionZ * distance);
    }

    private static double groundY(
            Hydra hydra, double x, double z, double preferredY) {
        ServerLevel level = (ServerLevel) hydra.level();
        double startY = Math.max(hydra.getEyeY() + 8.0, preferredY + 16.0);
        BlockHitResult hit = level.clip(new ClipContext(
                new Vec3(x, startY, z),
                new Vec3(x, level.getMinBuildHeight(), z),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                hydra));
        if (hit.getType() != HitResult.Type.MISS) {
            return hit.getLocation().y;
        }
        return level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mth.floor(x), Mth.floor(z));
    }

    private static Vec3 attackOrigin(Hydra hydra, int headIndex) {
        if (hasUsableHead(hydra, headIndex)) {
            HydraHeadContainer head = hydra.hc[headIndex];
            return head.headEntity.position().add(
                    0.0, head.headEntity.getBbHeight() * 0.25, 0.0);
        }
        return hydra.position().add(0.0, hydra.getBbHeight() * 0.5, 0.0);
    }

    private static int randomActiveHead(Hydra hydra, int excludedIndex) {
        List<Integer> candidates = new ArrayList<>();
        int count = Math.min(hydra.numHeads, hydra.hc.length);
        for (int index = 0; index < count; index++) {
            if (index != excludedIndex && hasUsableHead(hydra, index)) {
                candidates.add(index);
            }
        }
        if (candidates.isEmpty()) {
            return -1;
        }
        return candidates.get(hydra.getRandom().nextInt(candidates.size()));
    }

    private static int countActiveHeads(Hydra hydra) {
        return activeContainers(hydra).size();
    }

    private static List<HydraHeadContainer> activeContainers(Hydra hydra) {
        List<HydraHeadContainer> active = new ArrayList<>();
        int count = Math.min(hydra.numHeads, hydra.hc.length);
        for (int index = 0; index < count; index++) {
            if (hydra.hc[index].isActive()) {
                active.add(hydra.hc[index]);
            }
        }
        return active;
    }

    private static boolean hasVanillaAttackInProgress(Hydra hydra) {
        return activeContainers(hydra).stream()
                .anyMatch(HydraHeadContainer::isAttacking);
    }

    private static int randomIdleHead(Hydra hydra) {
        List<Integer> candidates = new ArrayList<>();
        int count = Math.min(hydra.numHeads, hydra.hc.length);
        for (int index = 0; index < count; index++) {
            if (hydra.hc[index].isActive() && hydra.hc[index].isIdle()) {
                candidates.add(index);
            }
        }
        if (candidates.isEmpty()) {
            return -1;
        }
        return candidates.get(hydra.getRandom().nextInt(candidates.size()));
    }

    private static boolean hasUsableHead(Hydra hydra, int index) {
        return index >= 0
                && index < Math.min(hydra.numHeads, hydra.hc.length)
                && hydra.hc[index].isActive();
    }

    private static void clearHeadTarget(Hydra hydra, int index) {
        if (index >= 0 && index < hydra.hc.length) {
            hydra.hc[index].setTargetEntity(null);
        }
    }

    private static void decrementCooldowns(HydraCombatState state) {
        if (state.actionState == HydraActionState.IDLE) {
            state.ordinaryAttackCooldown = Math.max(
                    0, state.ordinaryAttackCooldown - 1);
        }
        state.fireLordCooldown = Math.max(0, state.fireLordCooldown - 1);
        state.evilFireballCooldown = Math.max(
            0, state.evilFireballCooldown - 1);
        if (state.secondPhase) {
            state.secondPhaseBeamCooldown = Math.max(
                    0, state.secondPhaseBeamCooldown - 1);
        }
    }

    private static void enterState(
            HydraCombatState state,
            HydraActionState actionState, int duration) {
        state.actionState = actionState;
        state.actionTicks = 0;
        state.actionDuration = Math.max(0, duration);
    }

    private static int windupTicks(HydraSkill skill) {
        return switch (skill) {
            case SWEEP -> BossRefactorTwilightForestConfig.HYDRA
                    .sweepWindupTicks.get();
            case SLAM -> BossRefactorTwilightForestConfig.HYDRA
                    .slamWindupTicks.get();
            default -> 1;
        };
    }

    private static double attackRadius(HydraSkill skill) {
        return switch (skill) {
            case SWEEP -> BossRefactorTwilightForestConfig.HYDRA
                    .sweepRadius.get();
            case SLAM -> BossRefactorTwilightForestConfig.HYDRA
                    .slamRadius.get();
            default -> 0.0;
        };
    }

    private static double halfAngle(HydraSkill skill) {
        return switch (skill) {
            case SWEEP -> BossRefactorTwilightForestConfig.HYDRA
                    .sweepHalfAngleDegrees.get();
            default -> 180.0;
        };
    }

    private static float damageMultiplier(HydraSkill skill) {
        return switch (skill) {
            case SWEEP -> BossRefactorTwilightForestConfig.HYDRA
                    .sweepDamageMultiplier.get().floatValue();
            case SLAM -> BossRefactorTwilightForestConfig.HYDRA
                    .slamDamageMultiplier.get().floatValue();
            default -> 0.0F;
        };
    }

    private static double fireLordRadius(HydraCombatState state) {
        return HydraCombatRules.expandingRadius(
                state.actionTicks, state.actionDuration,
                BossRefactorTwilightForestConfig.HYDRA
                        .fireLordMaximumRadius.get());
    }

    private static float attackDamage(Hydra hydra) {
        return HydraCombatRules.phaseDamage(
                (float) hydra.getAttributeValue(Attributes.ATTACK_DAMAGE),
                state(hydra).secondPhase,
                BossRefactorTwilightForestConfig.HYDRA
                        .secondPhaseDamageMultiplier.get().floatValue());
    }

    private static HydraCombatState state(Hydra hydra) {
        return access(hydra).bossRefactorTF$getHydraCombatState();
    }

    private static HydraCombatAccess access(Hydra hydra) {
        return (HydraCombatAccess) hydra;
    }
}