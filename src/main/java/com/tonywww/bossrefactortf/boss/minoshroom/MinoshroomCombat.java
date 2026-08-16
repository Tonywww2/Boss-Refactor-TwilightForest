package com.tonywww.bossrefactortf.boss.minoshroom;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.TwilightBossParryIntegration;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import twilightforest.entity.boss.Minoshroom;

import java.util.List;

public final class MinoshroomCombat {
    private MinoshroomCombat() {
    }

    public static boolean customCombatEnabled() {
        return BossRefactorTwilightForestConfig.MINOSHROOM.enabled.get();
    }

    public static boolean hasCombatWork(Minoshroom minoshroom) {
        return state(minoshroom).attackState != MinoshroomAttackState.IDLE
                || isRetainableTarget(minoshroom, minoshroom.getTarget());
    }

    public static void startIfIdle(Minoshroom minoshroom) {
        if (state(minoshroom).attackState == MinoshroomAttackState.IDLE
                && isRetainableTarget(minoshroom, minoshroom.getTarget())) {
            beginCruise(minoshroom);
        }
    }

    public static void stopWhenUnusable(Minoshroom minoshroom) {
        if (!customCombatEnabled()) {
            resetCombat(minoshroom);
            return;
        }
        if (state(minoshroom).attackState == MinoshroomAttackState.CRUISE
                && !isRetainableTarget(minoshroom, minoshroom.getTarget())) {
            enterIdle(minoshroom);
        }
    }

    public static void tick(Minoshroom minoshroom) {
        if (minoshroom.level().isClientSide() || !customCombatEnabled()) {
            return;
        }
        MinoshroomCombatState state = state(minoshroom);
        switch (state.attackState) {
            case IDLE -> startIfIdle(minoshroom);
            case CRUISE -> tickCruise(minoshroom, state);
            case SWEEP_WINDUP -> tickSweepWindup(minoshroom, state);
            case SWEEP_DASH -> tickSweepDash(minoshroom, state);
            case SWEEP_STRIKE_WINDUP -> tickSweepStrikeWindup(
                    minoshroom, state);
            case STOMP_WINDUP -> tickStompWindup(minoshroom, state);
            case STOMP_INTERVAL -> tickStompInterval(minoshroom, state);
            case STUNNED -> tickStunned(minoshroom, state);
        }
        synchronizeParryWindow(minoshroom);
    }

    public static void tickBaseTraits(Minoshroom minoshroom) {
        if (minoshroom.level().isClientSide()) {
            return;
        }
        access(minoshroom).bossRefactorTF$setSecondPhase(
                isSecondPhase(minoshroom));
        if (!customCombatEnabled() || !minoshroom.isAlive()) {
            return;
        }
        int interval = BossRefactorTwilightForestConfig.MINOSHROOM
                .blockDestructionIntervalTicks.get();
        if (minoshroom.tickCount % interval == 0) {
            destroyNearbyBlocks(minoshroom);
        }
    }

    public static boolean isCurrentAttackParryable(Minoshroom minoshroom) {
        if (!customCombatEnabled()) {
            return false;
        }
        return switch (state(minoshroom).attackState) {
            case SWEEP_WINDUP, SWEEP_DASH, SWEEP_STRIKE_WINDUP -> true;
            default -> false;
        };
    }

    public static boolean acceptParry(Minoshroom minoshroom) {
        if (!isCurrentAttackParryable(minoshroom)) {
            return false;
        }
        MinoshroomCombatState state = state(minoshroom);
        long gameTime = minoshroom.level().getGameTime();
        if (state.lastParryGameTime == gameTime) {
            return true;
        }
        state.lastParryGameTime = gameTime;
        state.sweepRoundsCompleted = 0;
        state.dashHits.clear();
        enterState(state, MinoshroomAttackState.STUNNED,
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .sweepParryStunTicks.get());
        stopHorizontalMovement(minoshroom);
        minoshroom.setGroundAttackCharge(false);
        setTelegraph(minoshroom, AttackTelegraph.NONE);
        closeParryWindow(minoshroom);
        return true;
    }

    public static void restoreVisualState(Minoshroom minoshroom) {
        minoshroom.setGroundAttackCharge(false);
        access(minoshroom).bossRefactorTF$setSecondPhase(
                isSecondPhase(minoshroom));
        setTelegraph(minoshroom, AttackTelegraph.NONE);
        closeParryWindow(minoshroom);
    }

    private static void tickCruise(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        LivingEntity target = minoshroom.getTarget();
        if (!isRetainableTarget(minoshroom, target)) {
            enterIdle(minoshroom);
            return;
        }
        minoshroom.getNavigation().moveTo(target, navigationSpeed(
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .cruiseNavigationSpeed.get(), minoshroom));
        minoshroom.getLookControl().setLookAt(target);
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        if (MinoshroomCombatRules.shouldUseStomp(
                horizontalDistance(minoshroom, target),
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .stompRadius.get())) {
            beginStomp(minoshroom, state);
        } else {
            state.sweepRoundsCompleted = 0;
            beginSweepRound(minoshroom, state);
        }
    }

    private static void tickSweepWindup(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        LivingEntity target = minoshroom.getTarget();
        if (!isRetainableTarget(minoshroom, target)) {
            enterIdle(minoshroom);
            return;
        }
        stopHorizontalMovement(minoshroom);
        lockDirectionToward(minoshroom, target, state);
        setTelegraph(minoshroom, dashTelegraph(
                minoshroom, state,
                AttackTelegraph.windupProgress(
                        state.stateTicks, state.stateDuration)));
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        state.originX = minoshroom.getX();
        state.originY = minoshroom.getY();
        state.originZ = minoshroom.getZ();
        state.dashDistance = 0.0;
        state.dashHits.clear();
        enterState(state, MinoshroomAttackState.SWEEP_DASH,
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .sweepDashDurationTicks.get());
    }

    private static void tickSweepDash(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        BossRefactorTwilightForestConfig.MinoshroomConfig config =
                BossRefactorTwilightForestConfig.MINOSHROOM;
        double maximumDistance = config.sweepDashMaximumDistance.get();
        minoshroom.getNavigation().moveTo(
                state.originX + state.directionX * maximumDistance,
                state.originY,
                state.originZ + state.directionZ * maximumDistance,
                navigationSpeed(config.sweepDashNavigationSpeed.get(),
                        minoshroom));
        faceLockedDirection(minoshroom, state);
        setTelegraph(minoshroom, dashTelegraph(minoshroom, state, 1.0F));
        damageDashContacts(minoshroom, state);
        state.stateTicks++;
        state.dashDistance = horizontalDistance(
                minoshroom.getX(), minoshroom.getZ(),
                state.originX, state.originZ);
        if (state.stateTicks < state.stateDuration
                && state.dashDistance < maximumDistance) {
            return;
        }
        stopHorizontalMovement(minoshroom);
        state.originX = minoshroom.getX();
        state.originY = minoshroom.getY();
        state.originZ = minoshroom.getZ();
        enterState(state, MinoshroomAttackState.SWEEP_STRIKE_WINDUP,
                windupTicks(config.sweepStrikeWindupTicks.get(), minoshroom));
    }

    private static void tickSweepStrikeWindup(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        stopHorizontalMovement(minoshroom);
        faceLockedDirection(minoshroom, state);
        setTelegraph(minoshroom, sweepTelegraph(
                state,
                AttackTelegraph.windupProgress(
                        state.stateTicks, state.stateDuration)));
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        performSweepStrike(minoshroom, state);
        state.sweepRoundsCompleted++;
        if (MinoshroomCombatRules.shouldRepeatSweep(
                state.sweepRoundsCompleted,
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .sweepMaximumRepeats.get(),
                isSecondPhase(minoshroom),
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .secondPhaseMinimumSweepRounds.get(),
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .sweepRepeatProbability.get(),
                minoshroom.getRandom().nextDouble())) {
            beginSweepRound(minoshroom, state);
        } else {
            beginCruise(minoshroom);
        }
    }

    private static void tickStompWindup(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        stopHorizontalMovement(minoshroom);
        minoshroom.setGroundAttackCharge(true);
        setTelegraph(minoshroom, circleTelegraph(
                state.originX, state.originY, state.originZ,
                AttackTelegraph.windupProgress(
                        state.stateTicks, state.stateDuration)));
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        minoshroom.setGroundAttackCharge(false);
        performStomp(minoshroom, state);
        state.stompImpactsCompleted++;
        continueOrFinishStomp(minoshroom, state);
    }

    private static void tickStompInterval(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        stopHorizontalMovement(minoshroom);
        setTelegraph(minoshroom, circleTelegraph(
                state.originX, state.originY, state.originZ,
                AttackTelegraph.windupProgress(
                        state.stateTicks, state.stateDuration)));
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        performStomp(minoshroom, state);
        state.stompImpactsCompleted++;
        continueOrFinishStomp(minoshroom, state);
    }

    private static void tickStunned(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        stopHorizontalMovement(minoshroom);
        setTelegraph(minoshroom, AttackTelegraph.NONE);
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        if (isRetainableTarget(minoshroom, minoshroom.getTarget())) {
            beginCruise(minoshroom);
        } else {
            enterIdle(minoshroom);
        }
    }

    private static void beginCruise(Minoshroom minoshroom) {
        MinoshroomCombatState state = state(minoshroom);
        enterState(state, MinoshroomAttackState.CRUISE,
                MinoshroomCombatRules.cruiseTicks(
                        BossRefactorTwilightForestConfig.MINOSHROOM
                                .cruiseMinimumTicks.get(),
                        BossRefactorTwilightForestConfig.MINOSHROOM
                                .cruiseMaximumTicks.get(),
                        minoshroom.getRandom().nextDouble()));
        state.sweepRoundsCompleted = 0;
        state.stompImpactsCompleted = 0;
        state.dashHits.clear();
        minoshroom.setGroundAttackCharge(false);
        setTelegraph(minoshroom, AttackTelegraph.NONE);
        closeParryWindow(minoshroom);
    }

    private static void beginSweepRound(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        enterState(state, MinoshroomAttackState.SWEEP_WINDUP,
                windupTicks(BossRefactorTwilightForestConfig.MINOSHROOM
                        .sweepInitialWindupTicks.get(), minoshroom));
        state.dashHits.clear();
        minoshroom.getNavigation().stop();
        LivingEntity target = minoshroom.getTarget();
        if (isRetainableTarget(minoshroom, target)) {
            lockDirectionToward(minoshroom, target, state);
        }
        setTelegraph(minoshroom, dashTelegraph(minoshroom, state, 0.0F));
    }

    private static void beginStomp(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        state.originX = minoshroom.getX();
        state.originY = minoshroom.getY();
        state.originZ = minoshroom.getZ();
        state.stompImpactsCompleted = 0;
        int duration = windupTicks(
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .stompWindupTicks.get(), minoshroom);
        enterState(state, MinoshroomAttackState.STOMP_WINDUP, duration);
        minoshroom.setMaxCharge(duration);
        minoshroom.setGroundAttackCharge(true);
        setTelegraph(minoshroom, circleTelegraph(
                state.originX, state.originY, state.originZ, 0.0F));
    }

    private static void continueOrFinishStomp(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        int impacts = MinoshroomCombatRules.stompCount(
                isSecondPhase(minoshroom),
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .secondPhaseStompCount.get());
        if (state.stompImpactsCompleted >= impacts) {
            beginCruise(minoshroom);
            return;
        }
        enterState(state, MinoshroomAttackState.STOMP_INTERVAL,
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .secondPhaseStompIntervalTicks.get());
        setTelegraph(minoshroom, circleTelegraph(
                state.originX, state.originY, state.originZ, 0.0F));
    }

    private static void damageDashContacts(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        double inflation = BossRefactorTwilightForestConfig.MINOSHROOM
                .sweepDashHitboxInflation.get();
        float damage = skillDamage(
                minoshroom,
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .sweepDashDamageMultiplier.get());
        DamageSource source = minoshroom.damageSources().mobAttack(minoshroom);
        for (LivingEntity target : combatTargets(
                minoshroom,
                minoshroom.getBoundingBox().inflate(inflation))) {
            if (state.dashHits.add(target.getUUID())) {
                target.hurt(source, damage);
            }
        }
    }

    private static void performSweepStrike(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        BossRefactorTwilightForestConfig.MinoshroomConfig config =
                BossRefactorTwilightForestConfig.MINOSHROOM;
        double radius = config.sweepStrikeRadius.get();
        double halfAngle = MinoshroomCombatRules.halfAngleDegrees(
                config.sweepStrikeArcDegrees.get());
        float damage = skillDamage(
                minoshroom, config.sweepStrikeDamageMultiplier.get());
        DamageSource source = minoshroom.damageSources().mobAttack(minoshroom);
        for (LivingEntity target : combatTargets(
                minoshroom,
                new AABB(
                        state.originX - radius, state.originY - radius,
                        state.originZ - radius,
                        state.originX + radius, state.originY + radius,
                        state.originZ + radius))) {
            if (MinoshroomCombatRules.isInsideArc(
                    state.originX, state.originZ,
                    state.directionX, state.directionZ,
                    target.getX(), target.getZ(), radius, halfAngle)) {
                target.hurt(source, damage);
            }
        }
        minoshroom.swing(minoshroom.getUsedItemHand());
        minoshroom.playSound(SoundEvents.PLAYER_ATTACK_SWEEP);
    }

    private static void performStomp(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        BossRefactorTwilightForestConfig.MinoshroomConfig config =
                BossRefactorTwilightForestConfig.MINOSHROOM;
        double radius = config.stompRadius.get();
        float damage = skillDamage(minoshroom,
                config.stompDamageMultiplier.get());
        DamageSource source = MinoshroomDamageTypes.stomp(minoshroom);
        for (LivingEntity target : combatTargets(
                minoshroom,
                new AABB(
                        state.originX - radius, state.originY - radius,
                        state.originZ - radius,
                        state.originX + radius, state.originY + radius,
                        state.originZ + radius))) {
                if (!MinoshroomCombatRules.shouldDamageWithStomp(
                        target.onGround(),
                        horizontalDistance(
                            target.getX(), target.getZ(),
                            state.originX, state.originZ),
                        radius)
                    || !target.hurt(source, damage)) {
                continue;
            }
            target.knockback(config.stompHorizontalKnockback.get(),
                    state.originX - target.getX(),
                    state.originZ - target.getZ());
            Vec3 movement = target.getDeltaMovement();
            target.setDeltaMovement(
                    movement.x,
                    Math.max(movement.y,
                            config.stompMinimumVerticalVelocity.get()),
                    movement.z);
        }
        minoshroom.playSound(SoundEvents.GENERIC_EXPLODE);
    }

    private static void destroyNearbyBlocks(Minoshroom minoshroom) {
        if (!minoshroom.level().getGameRules().getBoolean(
                GameRules.RULE_MOBGRIEFING)) {
            return;
        }
        BossRefactorTwilightForestConfig.MinoshroomConfig config =
                BossRefactorTwilightForestConfig.MINOSHROOM;
        AABB bounds = minoshroom.getBoundingBox().inflate(
                config.blockDestructionHorizontalRange.get(),
                config.blockDestructionVerticalRange.get(),
                config.blockDestructionHorizontalRange.get());
        for (BlockPos position : BlockPos.betweenClosed(
                BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
                BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ))) {
            BlockState blockState = minoshroom.level().getBlockState(position);
            if (blockState.isAir()
                    || minoshroom.level().getBlockEntity(position) != null
                    || blockState.getDestroySpeed(
                        minoshroom.level(), position) < 0.0F
                    || !blockState.canEntityDestroy(
                        minoshroom.level(), position, minoshroom)
                    || !ForgeEventFactory.onEntityDestroyBlock(
                        minoshroom, position, blockState)) {
                continue;
            }
            minoshroom.level().destroyBlock(position, true, minoshroom);
        }
    }

    private static AttackTelegraph dashTelegraph(
            Minoshroom minoshroom, MinoshroomCombatState state,
            float progress) {
        return new AttackTelegraph(
                AttackTelegraphShape.CORRIDOR,
                (float) state.originX,
                (float) state.originY,
                (float) state.originZ,
                (float) state.directionX,
                (float) state.directionZ,
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .sweepDashMaximumDistance.get().floatValue(),
                minoshroom.getBbWidth() * 0.5F
                        + BossRefactorTwilightForestConfig.MINOSHROOM
                                .sweepDashHitboxInflation.get().floatValue(),
                0.0F,
                progress);
    }

    private static AttackTelegraph sweepTelegraph(
            MinoshroomCombatState state, float progress) {
        return new AttackTelegraph(
                AttackTelegraphShape.ARC,
                (float) state.originX,
                (float) state.originY,
                (float) state.originZ,
                (float) state.directionX,
                (float) state.directionZ,
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .sweepStrikeRadius.get().floatValue(),
                (float) MinoshroomCombatRules.halfAngleDegrees(
                        BossRefactorTwilightForestConfig.MINOSHROOM
                                .sweepStrikeArcDegrees.get()),
                0.0F,
                progress);
    }

    private static AttackTelegraph circleTelegraph(
            double x, double y, double z, float progress) {
        return new AttackTelegraph(
                AttackTelegraphShape.CIRCLE,
                (float) x, (float) y, (float) z,
                0.0F, 1.0F, 0.0F, 0.0F,
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .stompRadius.get().floatValue(),
                progress);
    }

    private static void lockDirectionToward(
            Minoshroom minoshroom, LivingEntity target,
            MinoshroomCombatState state) {
        Vec3 direction = target.position().subtract(minoshroom.position());
        direction = new Vec3(direction.x, 0.0, direction.z);
        if (direction.lengthSqr() == 0.0) {
            direction = minoshroom.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        if (direction.lengthSqr() == 0.0) {
            direction = new Vec3(0.0, 0.0, 1.0);
        }
        direction = direction.normalize();
        state.directionX = direction.x;
        state.directionZ = direction.z;
        state.originX = minoshroom.getX();
        state.originY = minoshroom.getY();
        state.originZ = minoshroom.getZ();
        minoshroom.getLookControl().setLookAt(target);
    }

    private static void faceLockedDirection(
            Minoshroom minoshroom, MinoshroomCombatState state) {
        minoshroom.getLookControl().setLookAt(
                minoshroom.getX() + state.directionX,
                minoshroom.getEyeY(),
                minoshroom.getZ() + state.directionZ);
    }

    private static List<LivingEntity> combatTargets(
            Minoshroom minoshroom, AABB bounds) {
        return minoshroom.level().getEntitiesOfClass(
                LivingEntity.class, bounds,
                target -> isCombatTarget(minoshroom, target));
    }

    private static boolean isCombatTarget(
            Minoshroom minoshroom, LivingEntity target) {
        return target != minoshroom
                && target.isAlive()
                && !minoshroom.isAlliedTo(target)
                && (!(target instanceof Player player)
                        || (!player.isCreative() && !player.isSpectator()));
    }

    private static boolean isRetainableTarget(
            Minoshroom minoshroom, LivingEntity target) {
        return target != null
                && isCombatTarget(minoshroom, target)
                && !target.isRemoved();
    }

    private static boolean isSecondPhase(Minoshroom minoshroom) {
        return MinoshroomCombatRules.isSecondPhase(
                minoshroom.getHealth(), minoshroom.getMaxHealth(),
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .secondPhaseHealthFraction.get());
    }

    private static int windupTicks(
            int configuredTicks, Minoshroom minoshroom) {
        return MinoshroomCombatRules.windupTicks(
                configuredTicks, isSecondPhase(minoshroom),
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .secondPhaseWindupMultiplier.get());
    }

    private static double navigationSpeed(
            double configuredSpeed, Minoshroom minoshroom) {
        return MinoshroomCombatRules.navigationSpeed(
                configuredSpeed, isSecondPhase(minoshroom),
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .secondPhaseSpeedMultiplier.get());
    }

    private static float skillDamage(
            Minoshroom minoshroom, double skillMultiplier) {
        return (float) MinoshroomCombatRules.skillDamage(
                minoshroom.getAttributeValue(Attributes.ATTACK_DAMAGE),
                skillMultiplier, isSecondPhase(minoshroom),
                BossRefactorTwilightForestConfig.MINOSHROOM
                        .secondPhaseDamageMultiplier.get());
    }

    private static double horizontalDistance(
            LivingEntity first, LivingEntity second) {
        return horizontalDistance(
                first.getX(), first.getZ(), second.getX(), second.getZ());
    }

    private static double horizontalDistance(
            double firstX, double firstZ, double secondX, double secondZ) {
        double deltaX = firstX - secondX;
        double deltaZ = firstZ - secondZ;
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

    private static void stopHorizontalMovement(Minoshroom minoshroom) {
        minoshroom.getNavigation().stop();
        Vec3 movement = minoshroom.getDeltaMovement();
        minoshroom.setDeltaMovement(0.0, movement.y, 0.0);
    }

    private static void enterState(
            MinoshroomCombatState state,
            MinoshroomAttackState attackState, int duration) {
        state.attackState = attackState;
        state.stateTicks = 0;
        state.stateDuration = Math.max(0, duration);
    }

    private static void enterIdle(Minoshroom minoshroom) {
        MinoshroomCombatState state = state(minoshroom);
        enterState(state, MinoshroomAttackState.IDLE, 0);
        state.sweepRoundsCompleted = 0;
        state.stompImpactsCompleted = 0;
        state.dashHits.clear();
        stopHorizontalMovement(minoshroom);
        minoshroom.setGroundAttackCharge(false);
        setTelegraph(minoshroom, AttackTelegraph.NONE);
        closeParryWindow(minoshroom);
    }

    private static void resetCombat(Minoshroom minoshroom) {
        MinoshroomCombatState state = state(minoshroom);
        state.attackState = MinoshroomAttackState.IDLE;
        state.stateTicks = 0;
        state.stateDuration = 0;
        state.sweepRoundsCompleted = 0;
        state.stompImpactsCompleted = 0;
        state.dashHits.clear();
        minoshroom.setGroundAttackCharge(false);
        setTelegraph(minoshroom, AttackTelegraph.NONE);
        closeParryWindow(minoshroom);
    }

    private static void synchronizeParryWindow(Minoshroom minoshroom) {
        if (!(minoshroom instanceof TwilightBossParryAccess parryAccess)) {
            return;
        }
        var bridge = TwilightBossParryIntegration.bridge();
        if (!bridge.isAvailable()) {
            return;
        }
        if (!parryAccess.bossRefactorTF$isCurrentAttackParryable()) {
            closeParryWindow(minoshroom);
        } else if (!bridge.isParryable(minoshroom)
                && !bridge.isParried(minoshroom)
                && !bridge.openWindow(minoshroom)) {
            BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to open the SenDimS parry window for Minoshroom {}",
                    minoshroom.getUUID());
        }
    }

    private static void closeParryWindow(Minoshroom minoshroom) {
        var bridge = TwilightBossParryIntegration.bridge();
        if (bridge.isAvailable() && bridge.isParryable(minoshroom)
                && !bridge.closeWindow(minoshroom)) {
            BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to close the SenDimS parry window for Minoshroom {}",
                    minoshroom.getUUID());
        }
    }

    private static void setTelegraph(
            Minoshroom minoshroom, AttackTelegraph telegraph) {
        access(minoshroom).bossRefactorTF$setMinoshroomTelegraph(telegraph);
    }

    private static MinoshroomCombatAccess access(Minoshroom minoshroom) {
        return (MinoshroomCombatAccess) minoshroom;
    }

    private static MinoshroomCombatState state(Minoshroom minoshroom) {
        return access(minoshroom).bossRefactorTF$getMinoshroomCombatState();
    }
}