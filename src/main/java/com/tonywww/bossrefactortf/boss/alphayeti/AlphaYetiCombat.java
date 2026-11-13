package com.tonywww.bossrefactortf.boss.alphayeti;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.TwilightBossParryIntegration;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.boss.AlphaYeti;
import twilightforest.entity.projectile.FallingIce;
import twilightforest.entity.projectile.IceBomb;
import twilightforest.init.TFEntities;
import twilightforest.init.TFSounds;

import java.util.List;

public final class AlphaYetiCombat {
    public static final String VISUAL_ONLY_FALLING_ICE_TAG =
            "BossRefactorTFVisualOnly";

    private static final int MINIMUM_PARRY_WINDOW_TICKS = 20;

    private AlphaYetiCombat() {
    }

    public static boolean customCombatEnabled() {
        return BossRefactorTwilightForestConfig.ALPHA_YETI.enabled.get();
    }

    public static boolean hasCombatWork(AlphaYeti yeti) {
        AlphaYetiCombatState state = state(yeti);
        return state.attackState != AlphaYetiAttackState.IDLE
                || !state.secondaryIce.isEmpty()
                || isRetainableTarget(yeti, yeti.getTarget());
    }

    public static void startIfIdle(AlphaYeti yeti) {
        if (state(yeti).attackState == AlphaYetiAttackState.IDLE
                && isRetainableTarget(yeti, yeti.getTarget())) {
            beginCruise(yeti);
        }
    }

    public static void stopWhenUnusable(AlphaYeti yeti) {
        if (!customCombatEnabled()) {
            resetCombat(yeti);
            return;
        }
        AlphaYetiCombatState state = state(yeti);
        if (state.attackState == AlphaYetiAttackState.CRUISE
                && !isRetainableTarget(yeti, yeti.getTarget())) {
            enterIdle(yeti);
        }
    }

    public static void tick(AlphaYeti yeti) {
        if (yeti.level().isClientSide() || !customCombatEnabled()) {
            return;
        }
        AlphaYetiCombatState state = state(yeti);
        switch (state.attackState) {
            case IDLE -> startIfIdle(yeti);
            case CRUISE -> tickCruise(yeti, state);
            case RAMPAGE -> tickRampage(yeti, state);
            case PURSUIT_ROAR -> tickPursuitRoar(yeti, state);
            case PURSUIT -> tickPursuit(yeti, state);
            case STOMP_WINDUP -> tickStompWindup(yeti, state);
            case STOMP_ICE -> tickStompIce(yeti, state);
            case REST -> tickRest(yeti, state);
        }
        synchronizeParryWindow(yeti);
        synchronizeIndicators(yeti);
    }

    public static void tickIndependentIce(AlphaYeti yeti) {
        if (yeti.level().isClientSide() || !customCombatEnabled()) {
            return;
        }
        AlphaYetiCombatState state = state(yeti);
        AlphaYetiCombatState.IceStrike strike = state.secondaryIce.peekFirst();
        if (strike == null) {
            access(yeti).bossRefactorTF$setSecondaryTelegraph(AttackTelegraph.NONE);
            return;
        }
        if (!strike.visualsSpawned) {
            spawnIceCluster(yeti, strike.x, strike.y, strike.z,
                    strike.radius,
                    BossRefactorTwilightForestConfig.ALPHA_YETI
                            .secondPhaseIceCount.get(),
                    strike.remainingTicks);
            strike.visualsSpawned = true;
            access(yeti).bossRefactorTF$setSecondaryTelegraph(circleAt(
                    strike.x, strike.y, strike.z, strike.radius,
                    AttackTelegraph.windupProgress(
                            strike.totalTicks - strike.remainingTicks,
                            strike.totalTicks)));
            return;
        }
        int elapsedTicks = strike.totalTicks - strike.remainingTicks;
        access(yeti).bossRefactorTF$setSecondaryTelegraph(circleAt(
                strike.x, strike.y, strike.z, strike.radius,
                AttackTelegraph.windupProgress(
                        elapsedTicks, strike.totalTicks)));
        strike.remainingTicks--;
        if (strike.remainingTicks > 0) {
            return;
        }
        damageCircle(yeti, strike.x, strike.y, strike.z,
                strike.radius,
                (float) AlphaYetiCombatRules.fallingIceDamage(attackDamage(yeti)),
                4.0);
        state.secondaryIce.removeFirst();
        access(yeti).bossRefactorTF$setSecondaryTelegraph(AttackTelegraph.NONE);
    }

    public static boolean isPursuing(AlphaYeti yeti) {
        return customCombatEnabled()
                && state(yeti).attackState == AlphaYetiAttackState.PURSUIT;
    }

    public static boolean acceptParry(AlphaYeti yeti) {
        AlphaYetiCombatState state = state(yeti);
        if (!isPursuing(yeti)) {
            return false;
        }
        long gameTime = yeti.level().getGameTime();
        if (state.lastParryGameTime == gameTime) {
            return true;
        }
        state.lastParryGameTime = gameTime;
        addImbalance(yeti, state);
        if (state.attackState == AlphaYetiAttackState.PURSUIT) {
            state.stateDuration = Math.max(state.stateDuration,
                    state.stateTicks + BossRefactorTwilightForestConfig.ALPHA_YETI
                            .parryFeedbackTicks.get()
                            + MINIMUM_PARRY_WINDOW_TICKS);
        }
        return true;
    }

    public static void restoreVisualState(AlphaYeti yeti) {
        AlphaYetiCombatState state = state(yeti);
        yeti.setRampaging(state.attackState == AlphaYetiAttackState.RAMPAGE);
        yeti.setTired(state.attackState == AlphaYetiAttackState.REST);
        access(yeti).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
        access(yeti).bossRefactorTF$setSecondaryTelegraph(AttackTelegraph.NONE);
        synchronizeIndicators(yeti);
    }

    private static void tickCruise(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        LivingEntity target = yeti.getTarget();
        if (!isRetainableTarget(yeti, target)) {
            enterIdle(yeti);
            return;
        }
        state.stateTicks++;
        if (state.stateTicks >= state.stateDuration) {
            beginNextSkill(yeti, target, state);
            return;
        }
        yeti.getNavigation().moveTo(target, 1.0);
        yeti.getLookControl().setLookAt(
                target, 30.0F, yeti.getMaxHeadXRot());
        if (state.meleeCooldown > 0) {
            state.meleeCooldown--;
            return;
        }
        double reach = yeti.getBbWidth() * 2.0F;
        double reachSquared = reach * reach + target.getBbWidth();
        if (yeti.distanceToSqr(target) > reachSquared
                || !yeti.getSensing().hasLineOfSight(target)) {
            return;
        }
        yeti.swing(InteractionHand.MAIN_HAND);
        yeti.doHurtTarget(target);
        state.meleeCooldown = 20;
        queueSecondPhaseIce(yeti, target);
    }

    private static void tickRampage(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        LivingEntity target = yeti.getTarget();
        if (isRetainableTarget(yeti, target)) {
            yeti.getLookControl().setLookAt(
                    target, 10.0F, yeti.getMaxHeadXRot());
        }
        if (yeti.onGround()) {
            yeti.setDeltaMovement(0.0, 0.4, 0.0);
            yeti.gameEvent(GameEvent.HIT_GROUND);
        }
        yeti.destroyBlocksInAABB(
                blockDestructionBounds(yeti, 1.0, 4.0));
        int remainingTicks = state.stateDuration - state.stateTicks;
        int markedRange = Mth.floor(
                BossRefactorTwilightForestConfig.ALPHA_YETI.rampageRange.get());
        int randomBlockRange = Math.max(1,
                Mth.floor(markedRange / Math.sqrt(2.0)));
        if (remainingTicks % 10 == 0) {
            yeti.makeRandomBlockFall(randomBlockRange, 80);
        }
        if (remainingTicks % 20 == 0) {
            yeti.makeBlockAboveTargetFall();
            spawnRampageIceBomb(yeti);
        }
        if (remainingTicks < 40 && remainingTicks % 10 == 0) {
            yeti.makeRandomBlockFall(
                    Math.max(1, randomBlockRange / 2), 40);
        }
        access(yeti).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
        state.stateTicks++;
        if (state.stateTicks >= state.stateDuration) {
            finishSkill(yeti, false);
        }
    }

    private static void tickPursuitRoar(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        stopHorizontalMovement(yeti);
        LivingEntity target = yeti.getTarget();
        if (isRetainableTarget(yeti, target)) {
            yeti.getLookControl().setLookAt(
                    target, 30.0F, yeti.getMaxHeadXRot());
        }
        access(yeti).bossRefactorTF$setPrimaryTelegraph(new AttackTelegraph(
                AttackTelegraphShape.CIRCLE,
                0.0F, 1.0F, 0.0F, 0.0F,
                pursuitRadius(yeti),
                AttackTelegraph.windupProgress(
                        state.stateTicks, state.stateDuration)));
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        enterState(state, AlphaYetiAttackState.PURSUIT,
                BossRefactorTwilightForestConfig.ALPHA_YETI
                        .pursuitDurationTicks.get());
        state.imbalance = 0;
        access(yeti).bossRefactorTF$setPursuing(true);
    }

    private static void tickPursuit(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        LivingEntity target = yeti.getTarget();
        if (!isRetainableTarget(yeti, target)) {
            finishSkill(yeti, false);
            return;
        }
        yeti.getNavigation().moveTo(
                target,
                BossRefactorTwilightForestConfig.ALPHA_YETI
                        .pursuitSpeedMultiplier.get());
        yeti.getLookControl().setLookAt(
                target, 30.0F, yeti.getMaxHeadXRot());
        double inflation = BossRefactorTwilightForestConfig.ALPHA_YETI
                .pursuitHitboxInflation.get();
        yeti.destroyBlocksInAABB(
                blockDestructionBounds(yeti, inflation, 1.0));
        List<LivingEntity> contacts = combatTargets(
                yeti, yeti.getBoundingBox().inflate(inflation, 0.5, inflation));
        int knockbackInterval = BossRefactorTwilightForestConfig.ALPHA_YETI
                .pursuitKnockbackIntervalTicks.get();
        if (state.stateTicks % knockbackInterval == 0) {
            double strength = BossRefactorTwilightForestConfig.ALPHA_YETI
                    .pursuitKnockbackStrength.get();
            for (LivingEntity contact : contacts) {
                contact.knockback(strength,
                        yeti.getX() - contact.getX(),
                        yeti.getZ() - contact.getZ());
            }
        }
        int damageInterval = BossRefactorTwilightForestConfig.ALPHA_YETI
                .pursuitDamageIntervalTicks.get();
        if (AlphaYetiCombatRules.isIntervalPulse(
                state.stateTicks, damageInterval)
                && damagePursuitContacts(yeti, state, contacts)) {
            return;
        }
        access(yeti).bossRefactorTF$setPrimaryTelegraph(new AttackTelegraph(
                AttackTelegraphShape.CIRCLE,
                0.0F, 1.0F, 0.0F, 0.0F,
                pursuitRadius(yeti), 1.0F));
        state.stateTicks++;
        if (state.stateTicks >= state.stateDuration) {
            finishSkill(yeti, false);
        }
    }

    private static void tickStompWindup(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        stopHorizontalMovement(yeti);
        access(yeti).bossRefactorTF$setPrimaryTelegraph(circleAt(
                state.originX, state.originY, state.originZ,
                BossRefactorTwilightForestConfig.ALPHA_YETI
                        .stompRadius.get().floatValue(),
                AttackTelegraph.windupProgress(
                        state.stateTicks, state.stateDuration)));
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        performStomp(yeti, state);
        state.ringIndex = 1;
        beginFallingIceRing(yeti, state);
    }

    private static void tickStompIce(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        stopHorizontalMovement(yeti);
        setRingTelegraph(yeti, state);
        double radius = currentRingRadius(state);
        double halfWidth = currentRingHalfWidth();
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        damageRing(yeti, state.originX, state.originY, state.originZ,
                radius, halfWidth,
                (float) AlphaYetiCombatRules.fallingIceDamage(
                        attackDamage(yeti)));
        int ringCount = BossRefactorTwilightForestConfig.ALPHA_YETI
                .fallingIceRingCount.get();
        if (state.ringIndex >= ringCount) {
            finishSkill(yeti, false);
            return;
        }
        state.ringIndex++;
        beginFallingIceRing(yeti, state);
    }

    private static void tickRest(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        stopHorizontalMovement(yeti);
                if (state.stateTicks % 10 == 0) {
                        yeti.playSound(
                                        TFSounds.ALPHA_YETI_PANT.get(), 4.0F,
                                        0.5F + yeti.getRandom().nextFloat() * 0.5F);
                }
        int restTicks = AlphaYetiCombatRules.restTicks(
                BossRefactorTwilightForestConfig.ALPHA_YETI
                        .restDurationTicks.get(),
                AlphaYetiCombatRules.isSecondPhase(
                        yeti.getHealth(), yeti.getMaxHealth()));
        state.stateTicks++;
        if (state.stateTicks < restTicks) {
            return;
        }
        state.fatigue = 0;
        yeti.setTired(false);
        if (isRetainableTarget(yeti, yeti.getTarget())) {
            beginCruise(yeti);
        } else {
            enterIdle(yeti);
        }
    }

    private static void beginCruise(AlphaYeti yeti) {
        AlphaYetiCombatState state = state(yeti);
        int duration = AlphaYetiCombatRules.cruiseTicks(
                BossRefactorTwilightForestConfig.ALPHA_YETI
                        .cruiseMinimumTicks.get(),
                BossRefactorTwilightForestConfig.ALPHA_YETI
                        .cruiseMaximumTicks.get(),
                yeti.getRandom().nextDouble());
        enterState(state, AlphaYetiAttackState.CRUISE, duration);
        state.meleeCooldown = 0;
        state.imbalance = 0;
        yeti.setRampaging(false);
        yeti.setTired(false);
        access(yeti).bossRefactorTF$setPursuing(false);
        access(yeti).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
    }

    private static void beginNextSkill(
            AlphaYeti yeti, LivingEntity target, AlphaYetiCombatState state) {
        AlphaYetiSkill nextSkill = AlphaYetiCombatRules.nextSkill(
                state.lastSkill, yeti.getRandom().nextInt());
        state.lastSkill = nextSkill;
        yeti.getNavigation().stop();
        switch (nextSkill) {
            case RAMPAGE -> beginRampage(yeti, state);
            case PURSUIT -> beginPursuitRoar(yeti, state);
            case STOMP -> beginStompWindup(yeti, state);
            case NONE -> beginCruise(yeti);
        }
        queueSecondPhaseIce(yeti, target);
    }

    private static void beginRampage(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        enterState(state, AlphaYetiAttackState.RAMPAGE,
                BossRefactorTwilightForestConfig.ALPHA_YETI
                        .rampageDurationTicks.get());
        yeti.setRampaging(true);
        yeti.setTired(false);
        playRoar(yeti);
    }

    private static void beginPursuitRoar(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        enterState(state, AlphaYetiAttackState.PURSUIT_ROAR,
                BossRefactorTwilightForestConfig.ALPHA_YETI
                        .pursuitRoarTicks.get());
        state.imbalance = 0;
        yeti.setRampaging(false);
        yeti.setTired(false);
        playRoar(yeti);
    }

    private static void beginStompWindup(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        enterState(state, AlphaYetiAttackState.STOMP_WINDUP,
                BossRefactorTwilightForestConfig.ALPHA_YETI
                        .stompWindupTicks.get());
        state.originX = yeti.getX();
        state.originY = yeti.getY();
        state.originZ = yeti.getZ();
        yeti.setRampaging(false);
        yeti.setTired(false);
        playRoar(yeti);
    }

    private static void beginFallingIceRing(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        enterState(state, AlphaYetiAttackState.STOMP_ICE,
                BossRefactorTwilightForestConfig.ALPHA_YETI
                        .fallingIceRingWindupTicks.get());
        double radius = currentRingRadius(state);
        int count = BossRefactorTwilightForestConfig.ALPHA_YETI
                .fallingIcePerRing.get();
        spawnIceRing(yeti, state.originX, state.originY, state.originZ,
                radius, count, state.stateDuration);
        setRingTelegraph(yeti, state);
    }

    private static void finishSkill(
            AlphaYeti yeti, boolean stoppedByImbalance) {
        AlphaYetiCombatState state = state(yeti);
        state.fatigue = state.lastSkill == AlphaYetiSkill.PURSUIT
                ? AlphaYetiCombatRules.fatigueAfterPursuit(
                        state.fatigue, stoppedByImbalance)
                : AlphaYetiCombatRules.fatigueAfterSkill(state.fatigue);
        state.fatigue = Math.min(
                AlphaYetiCombatRules.FATIGUE_THRESHOLD, state.fatigue);
        state.imbalance = 0;
        yeti.setRampaging(false);
        yeti.setTired(false);
        yeti.getNavigation().stop();
        access(yeti).bossRefactorTF$setPursuing(false);
        access(yeti).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
        closeParryWindow(yeti);
        if (AlphaYetiCombatRules.shouldRest(state.fatigue)) {
            enterState(state, AlphaYetiAttackState.REST,
                    BossRefactorTwilightForestConfig.ALPHA_YETI
                            .restDurationTicks.get());
            yeti.setTired(true);
        } else {
            beginCruise(yeti);
        }
    }

    private static boolean damagePursuitContacts(
            AlphaYeti yeti, AlphaYetiCombatState state,
            List<LivingEntity> contacts) {
        DamageSource source = yeti.damageSources().mobAttack(yeti);
        float damage = (float) AlphaYetiCombatRules.pursuitDamage(
                attackDamage(yeti));
        for (LivingEntity contact : contacts) {
            boolean shieldBlocked = contact instanceof ServerPlayer player
                    && player.isDamageSourceBlocked(source)
                    && player.getUseItem().getItem() instanceof ShieldItem;
            contact.hurt(source, damage);
            if (shieldBlocked) {
                addImbalance(yeti, state);
                if (state.attackState != AlphaYetiAttackState.PURSUIT) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void addImbalance(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        state.imbalance++;
        synchronizeIndicators(yeti);
        if (AlphaYetiCombatRules.reachesImbalanceThreshold(state.imbalance)) {
            finishSkill(yeti, true);
        }
    }

    private static void performStomp(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        double radius = BossRefactorTwilightForestConfig.ALPHA_YETI
                .stompRadius.get();
        float damage = (float) AlphaYetiCombatRules.stompDamage(
                attackDamage(yeti));
        double horizontal = BossRefactorTwilightForestConfig.ALPHA_YETI
                .stompHorizontalKnockback.get();
        double vertical = BossRefactorTwilightForestConfig.ALPHA_YETI
                .stompVerticalVelocity.get();
        DamageSource source = yeti.damageSources().mobAttack(yeti);
        for (LivingEntity target : combatTargets(yeti,
                new AABB(
                        state.originX - radius, state.originY - 4.0,
                        state.originZ - radius,
                        state.originX + radius, state.originY + 4.0,
                        state.originZ + radius))) {
            if (horizontalDistance(target, state.originX, state.originZ)
                    > radius) {
                continue;
            }
            if (!target.hurt(source, damage)) {
                continue;
            }
            target.knockback(horizontal,
                    state.originX - target.getX(),
                    state.originZ - target.getZ());
            Vec3 movement = target.getDeltaMovement();
            target.setDeltaMovement(
                    movement.x, Math.max(movement.y, vertical), movement.z);
        }
        playIceImpact(yeti);
    }

    private static void damageRing(
            AlphaYeti yeti, double x, double y, double z,
            double radius, double halfWidth, float damage) {
        double outerRadius = radius + halfWidth;
        DamageSource source = yeti.damageSources().mobAttack(yeti);
        for (LivingEntity target : combatTargets(yeti,
                new AABB(
                        x - outerRadius, y - 6.0, z - outerRadius,
                        x + outerRadius, y + 6.0, z + outerRadius))) {
            if (AlphaYetiCombatRules.isInsideRing(
                    horizontalDistance(target, x, z), radius, halfWidth)) {
                target.hurt(source, damage);
            }
        }
    }

    private static void damageCircle(
            AlphaYeti yeti, double x, double y, double z,
            double radius, float damage, double verticalRange) {
        DamageSource source = yeti.damageSources().mobAttack(yeti);
        for (LivingEntity target : combatTargets(yeti,
                new AABB(
                        x - radius, y - verticalRange, z - radius,
                        x + radius, y + verticalRange, z + radius))) {
            if (horizontalDistance(target, x, z) <= radius) {
                target.hurt(source, damage);
            }
        }
    }

    private static void queueSecondPhaseIce(
            AlphaYeti yeti, LivingEntity target) {
        if (!AlphaYetiCombatRules.isSecondPhase(
                yeti.getHealth(), yeti.getMaxHealth())
                || !isRetainableTarget(yeti, target)) {
            return;
        }
        int windupTicks = BossRefactorTwilightForestConfig.ALPHA_YETI
                .secondPhaseIceWindupTicks.get();
        float radius = BossRefactorTwilightForestConfig.ALPHA_YETI
                .secondPhaseIceRadius.get().floatValue();
        double y = findGroundY(yeti, target.getX(), target.getY(), target.getZ());
        state(yeti).secondaryIce.addLast(new AlphaYetiCombatState.IceStrike(
                target.getX(), y, target.getZ(), radius, windupTicks));
    }

    private static void spawnIceRing(
            AlphaYeti yeti, double x, double y, double z,
            double radius, int count, int impactTicks) {
        for (int index = 0; index < count; index++) {
            double angle = Math.PI * 2.0 * index / count;
            spawnVisualFallingIce(
                    yeti,
                    x + Math.cos(angle) * radius,
                    y,
                    z + Math.sin(angle) * radius,
                    impactTicks);
        }
    }

    private static void spawnIceCluster(
            AlphaYeti yeti, double x, double y, double z,
            double radius, int count, int impactTicks) {
        for (int index = 0; index < count; index++) {
            double angle = Math.PI * 2.0 * index / count
                    + yeti.getRandom().nextDouble() * 0.35;
            double distance = radius * Math.sqrt(yeti.getRandom().nextDouble());
            spawnVisualFallingIce(
                    yeti,
                    x + Math.cos(angle) * distance,
                    y,
                    z + Math.sin(angle) * distance,
                    impactTicks);
        }
    }

    private static void spawnVisualFallingIce(
            AlphaYeti yeti, double x, double referenceY, double z,
            int impactTicks) {
        double groundY = findGroundY(yeti, x, referenceY, z);
        double spawnY = findIceSpawnY(yeti, x, groundY, z);
        FallingIce fallingIce = new FallingIce(
                yeti.level(), x, spawnY, z,
                Blocks.PACKED_ICE.defaultBlockState(), 0);
        ((AlphaYetiFallingIceAccess) fallingIce)
                .bossRefactorTF$markVisualOnly();
        fallingIce.setDeltaMovement(0.0,
                AlphaYetiCombatRules.fallingIceInitialVelocity(
                        spawnY - groundY, impactTicks),
                0.0);
        yeti.level().addFreshEntity(fallingIce);
    }

    private static double findGroundY(
            AlphaYeti yeti, double x, double referenceY, double z) {
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        int minimumY = Math.max(yeti.level().getMinBuildHeight(),
                Mth.floor(referenceY) - 16);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(
                blockX, Mth.floor(referenceY) + 3, blockZ);
        while (cursor.getY() > minimumY
                && yeti.level().getBlockState(cursor).getCollisionShape(
                        yeti.level(), cursor).isEmpty()) {
            cursor.move(0, -1, 0);
        }
        return cursor.getY() + 1.0;
    }

    private static double findIceSpawnY(
            AlphaYeti yeti, double x, double groundY, double z) {
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        double preferredHeight = BossRefactorTwilightForestConfig.ALPHA_YETI
                .fallingIceSpawnHeight.get();
        int maximumY = Math.min(yeti.level().getMaxBuildHeight() - 1,
                Mth.floor(groundY + preferredHeight));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(
                blockX, Mth.floor(groundY) + 1, blockZ);
        while (cursor.getY() < maximumY) {
            if (!yeti.level().getBlockState(cursor).getCollisionShape(
                    yeti.level(), cursor).isEmpty()) {
                return Math.max(groundY + 1.5, cursor.getY() - 0.5);
            }
            cursor.move(0, 1, 0);
        }
        return maximumY;
    }

    private static void spawnRampageIceBomb(AlphaYeti yeti) {
        IceBomb iceBomb = new IceBomb(
                TFEntities.THROWN_ICE.get(), yeti.level(), yeti);
        Vec3 direction = new Vec3(
                0.5F + yeti.getRandom().nextFloat() * 0.5F,
                0.5F + yeti.getRandom().nextFloat() * 0.3F,
                0.0).yRot(yeti.getRandom().nextFloat() * 360.0F);
        iceBomb.shoot(
                direction.x, direction.y, direction.z,
                0.4F + yeti.getRandom().nextFloat() * 0.3F, 0.0F);
        yeti.playSound(
                TFSounds.ALPHA_YETI_ICE.get(), 1.0F,
                1.0F / (yeti.getRandom().nextFloat() * 0.4F + 0.8F));
        yeti.gameEvent(GameEvent.PROJECTILE_SHOOT);
        yeti.level().addFreshEntity(iceBomb);
    }

    private static List<LivingEntity> combatTargets(
            AlphaYeti yeti, AABB bounds) {
        return yeti.level().getEntitiesOfClass(
                LivingEntity.class, bounds,
                target -> isCombatTarget(yeti, target));
    }

    private static boolean isCombatTarget(
            AlphaYeti yeti, LivingEntity target) {
        return target != yeti
                && target.isAlive()
                && !yeti.isAlliedTo(target)
                && (!(target instanceof Player player)
                        || (!player.isCreative() && !player.isSpectator()));
    }

    private static boolean isRetainableTarget(
            AlphaYeti yeti, LivingEntity target) {
        return target != null && isCombatTarget(yeti, target)
                && !target.isRemoved();
    }

    private static float attackDamage(AlphaYeti yeti) {
        return (float) yeti.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private static float pursuitRadius(AlphaYeti yeti) {
        return yeti.getBbWidth() * 0.5F
                + BossRefactorTwilightForestConfig.ALPHA_YETI
                        .pursuitHitboxInflation.get().floatValue();
    }

    private static double currentRingRadius(AlphaYetiCombatState state) {
        BossRefactorTwilightForestConfig.AlphaYetiConfig config =
                BossRefactorTwilightForestConfig.ALPHA_YETI;
        return AlphaYetiCombatRules.fallingIceRingRadius(
                state.ringIndex,
                config.fallingIceRingCount.get(),
                config.fallingIceMaximumRadius.get());
    }

    private static double currentRingHalfWidth() {
        BossRefactorTwilightForestConfig.AlphaYetiConfig config =
                BossRefactorTwilightForestConfig.ALPHA_YETI;
        return AlphaYetiCombatRules.fallingIceRingHalfWidth(
                config.fallingIceRingCount.get(),
                config.fallingIceMaximumRadius.get());
    }

    private static void setRingTelegraph(
            AlphaYeti yeti, AlphaYetiCombatState state) {
        access(yeti).bossRefactorTF$setPrimaryTelegraph(new AttackTelegraph(
                AttackTelegraphShape.RING,
                (float) state.originX,
                (float) state.originY,
                (float) state.originZ,
                0.0F, 1.0F, 0.0F,
                (float) currentRingHalfWidth(),
                (float) currentRingRadius(state),
                AttackTelegraph.windupProgress(
                        state.stateTicks, state.stateDuration)));
    }

    private static double horizontalDistance(
            LivingEntity entity, double x, double z) {
        double deltaX = entity.getX() - x;
        double deltaZ = entity.getZ() - z;
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

        private static AABB blockDestructionBounds(
                        AlphaYeti yeti, double horizontalInflation,
                        double upwardInflation) {
                AABB bounds = yeti.getBoundingBox();
                return new AABB(
                                bounds.minX - horizontalInflation,
                                AlphaYetiCombatRules.blockDestructionMinimumY(bounds.minY),
                                bounds.minZ - horizontalInflation,
                                bounds.maxX + horizontalInflation,
                                bounds.maxY + upwardInflation,
                                bounds.maxZ + horizontalInflation);
        }

    private static AttackTelegraph circleAt(
            double x, double y, double z, float radius, float progress) {
        return new AttackTelegraph(
                AttackTelegraphShape.CIRCLE,
                (float) x, (float) y, (float) z,
                0.0F, 1.0F, 0.0F, 0.0F, radius, progress);
    }

    private static void playRoar(AlphaYeti yeti) {
        SoundEvent roar = TFSounds.ALPHA_YETI_ROAR.get();
        yeti.playSound(roar, 4.0F,
                0.5F + yeti.getRandom().nextFloat() * 0.5F);
        yeti.gameEvent(GameEvent.ENTITY_ROAR);
    }

    private static void playIceImpact(AlphaYeti yeti) {
        yeti.playSound(
                TFSounds.ALPHA_YETI_ICE.get(), 1.0F,
                1.0F / (yeti.getRandom().nextFloat() * 0.4F + 0.8F));
        yeti.gameEvent(GameEvent.HIT_GROUND);
    }

    private static void stopHorizontalMovement(AlphaYeti yeti) {
        yeti.getNavigation().stop();
        Vec3 movement = yeti.getDeltaMovement();
        yeti.setDeltaMovement(0.0, movement.y, 0.0);
    }

    private static void enterState(
            AlphaYetiCombatState state,
            AlphaYetiAttackState attackState, int duration) {
        state.attackState = attackState;
        state.stateTicks = 0;
        state.stateDuration = Math.max(0, duration);
    }

    private static void enterIdle(AlphaYeti yeti) {
        AlphaYetiCombatState state = state(yeti);
        enterState(state, AlphaYetiAttackState.IDLE, 0);
        state.imbalance = 0;
        yeti.getNavigation().stop();
        yeti.setRampaging(false);
        yeti.setTired(false);
        access(yeti).bossRefactorTF$setPursuing(false);
        access(yeti).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
        closeParryWindow(yeti);
    }

    private static void resetCombat(AlphaYeti yeti) {
        AlphaYetiCombatState state = state(yeti);
        state.attackState = AlphaYetiAttackState.IDLE;
        state.stateTicks = 0;
        state.stateDuration = 0;
        state.fatigue = 0;
        state.imbalance = 0;
        state.secondaryIce.clear();
        yeti.setRampaging(false);
        yeti.setTired(false);
        access(yeti).bossRefactorTF$setPursuing(false);
        access(yeti).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
        access(yeti).bossRefactorTF$setSecondaryTelegraph(AttackTelegraph.NONE);
        closeParryWindow(yeti);
        synchronizeIndicators(yeti);
    }

    private static void synchronizeIndicators(AlphaYeti yeti) {
        AlphaYetiCombatState state = state(yeti);
        AlphaYetiCombatAccess access = access(yeti);
        access.bossRefactorTF$setVisibleFatigue(Math.min(
                AlphaYetiCombatRules.FATIGUE_THRESHOLD, state.fatigue));
        access.bossRefactorTF$setVisibleImbalance(
                state.attackState == AlphaYetiAttackState.PURSUIT
                        ? Math.min(AlphaYetiCombatRules.IMBALANCE_THRESHOLD,
                                state.imbalance)
                        : 0);
        access.bossRefactorTF$setPursuing(
                state.attackState == AlphaYetiAttackState.PURSUIT);
    }

    private static void synchronizeParryWindow(AlphaYeti yeti) {
        if (!(yeti instanceof TwilightBossParryAccess parryAccess)) {
            return;
        }
        var bridge = TwilightBossParryIntegration.bridge();
        if (!bridge.isAvailable()) {
            return;
        }
        if (!parryAccess.bossRefactorTF$isCurrentAttackParryable()) {
            closeParryWindow(yeti);
        } else if (!bridge.isParryable(yeti) && !bridge.isParried(yeti)
                && !bridge.openWindow(yeti)) {
            BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to open the SenDimS parry window for Alpha Yeti {}",
                    yeti.getUUID());
        }
    }

    private static void closeParryWindow(AlphaYeti yeti) {
        var bridge = TwilightBossParryIntegration.bridge();
        if (bridge.isAvailable() && bridge.isParryable(yeti)
                && !bridge.closeWindow(yeti)) {
            BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to close the SenDimS parry window for Alpha Yeti {}",
                    yeti.getUUID());
        }
    }

    private static AlphaYetiCombatAccess access(AlphaYeti yeti) {
        return (AlphaYetiCombatAccess) yeti;
    }

    private static AlphaYetiCombatState state(AlphaYeti yeti) {
        return access(yeti).bossRefactorTF$getAlphaYetiCombatState();
    }
}