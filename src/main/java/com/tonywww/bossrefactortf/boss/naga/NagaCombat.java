package com.tonywww.bossrefactortf.boss.naga;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.TwilightBossParryIntegration;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.boss.Naga;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public final class NagaCombat {
    private static final UUID PHASE_ATTACK_MODIFIER_ID = UUID.fromString(
            "60f46585-4e47-4d0b-9bea-e520a9c4fe15");
    private static final UUID PHASE_SPEED_MODIFIER_ID = UUID.fromString(
            "8cf21532-23e2-43ec-b89b-bf74564f8ca0");
    private static final UUID FIRE_SNAKE_SPEED_MODIFIER_ID = UUID.fromString(
            "12331942-fbb7-43f4-b818-c30159062e46");
    private static final String PHASE_ATTACK_MODIFIER_NAME =
            "bossrefactortf.naga_phase_attack";
    private static final String PHASE_SPEED_MODIFIER_NAME =
            "bossrefactortf.naga_phase_speed";
    private static final String FIRE_SNAKE_SPEED_MODIFIER_NAME =
            "bossrefactortf.naga_fire_snake_speed";

    private NagaCombat() {
    }

    public static boolean customCombatEnabled() {
        return BossRefactorTwilightForestConfig.NAGA.enabled.get();
    }

    public static boolean isStunned(Naga naga) {
        return customCombatEnabled()
                && NagaCombatRules.isStunned(state(naga).actionState);
    }

    public static boolean hasCombatWork(Naga naga) {
        NagaCombatState state = state(naga);
        return state.actionState != NagaActionState.IDLE
            || state.fireSnakeTicks > 0
            || isRetainableTarget(naga, naga.getTarget())
            || (!state.secondPhase && NagaCombatRules.isSecondPhase(
                naga.getHealth(), naga.getMaxHealth(),
                BossRefactorTwilightForestConfig.NAGA
                    .secondPhaseHealthFraction.get()));
    }

    public static void startIfIdle(Naga naga) {
        if (state(naga).actionState == NagaActionState.IDLE
                && isRetainableTarget(naga, naga.getTarget())) {
            beginCruise(naga);
        }
    }

    public static void stopWhenUnusable(Naga naga) {
        if (!customCombatEnabled()) {
            enterIdle(naga);
            return;
        }
        if (state(naga).actionState == NagaActionState.CRUISING
                && !isRetainableTarget(naga, naga.getTarget())) {
            enterIdle(naga);
        }
    }

    public static void tick(Naga naga) {
        if (naga.level().isClientSide() || !customCombatEnabled()) {
            return;
        }
        NagaCombatState state = state(naga);
        updateSecondPhase(naga, state);
        decrementCooldowns(state);
        tickStunImmunity(naga, state);
        tickFireSnake(naga, state);
        synchronizeFireSnakeVisualState(naga, state);
        synchronizeBodyState(naga, state);

        switch (state.actionState) {
            case IDLE -> startIfIdle(naga);
            case CRUISING -> tickCruise(naga, state);
            case POUNCE_WINDUP -> tickPounceWindup(naga, state);
            case POUNCE_FLIGHT -> tickPounceFlight(naga, state);
            case POUNCE_IMPACT -> performPounceImpact(naga, state);
            case TAIL_WINDUP -> tickTailWindup(naga, state);
            case TAIL_STRIKE -> performTailStrike(naga, state);
            case VENOM_WINDUP -> tickVenomWindup(naga, state);
            case VENOM_PROJECTILE -> tickVenomProjectile(naga, state);
            case STUNNED -> tickStunned(naga, state);
        }
        synchronizeParryWindow(naga);
        synchronizeIndicator(naga);
        synchronizeFireSnakeVisualState(naga, state);
        synchronizeBodyState(naga, state);
    }

    public static boolean isCurrentAttackParryable(Naga naga) {
        return switch (state(naga).actionState) {
            case POUNCE_WINDUP, POUNCE_FLIGHT, POUNCE_IMPACT,
                    TAIL_WINDUP, TAIL_STRIKE,
                    VENOM_WINDUP, VENOM_PROJECTILE -> true;
            default -> false;
        };
    }

    public static boolean suppressesExternalParryStun(Naga naga) {
        NagaCombatState state = state(naga);
        return NagaCombatRules.suppressesExternalParryStun(
            state.actionState, state.secondPhase,
            state.stunImmunityAvailable);
    }

    public static boolean acceptParry(Naga naga) {
        NagaCombatState state = state(naga);
        if (!isCurrentAttackParryable(naga)) {
            return false;
        }
        long gameTime = naga.level().getGameTime();
        if (state.lastParryGameTime == gameTime) {
            return true;
        }
        state.lastParryGameTime = gameTime;
        if (state.actionState == NagaActionState.VENOM_WINDUP
                || state.actionState == NagaActionState.VENOM_PROJECTILE) {
            cancelVenom(naga, state);
        } else {
            interruptWithPossibleStun(naga, state);
        }
        return true;
    }

    public static void onVenomProjectileResolved(
            ServerLevel level, UUID ownerId, UUID projectileId) {
        if (!(level.getEntity(ownerId) instanceof Naga naga)
                || !(naga instanceof NagaCombatAccess access)) {
            return;
        }
        NagaCombatState state = access.bossRefactorTF$getNagaCombatState();
        if (projectileId.equals(state.venomProjectileId)) {
            state.venomProjectileId = null;
            if (state.actionState == NagaActionState.VENOM_PROJECTILE) {
                beginCruise(naga);
            }
        }
    }

    public static void restoreVisualState(Naga naga) {
        NagaCombatState state = state(naga);
        access(naga).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
        access(naga).bossRefactorTF$setSecondaryTelegraph(AttackTelegraph.NONE);
        synchronizeIndicator(naga);
        synchronizeAttributeModifiers(naga, state);
        synchronizeFireSnakeVisualState(naga, state);
        synchronizeBodyState(naga, state);
    }

    private static void tickCruise(Naga naga, NagaCombatState state) {
        LivingEntity target = naga.getTarget();
        if (!isRetainableTarget(naga, target)) {
            enterIdle(naga);
            return;
        }
        naga.getLookControl().setLookAt(target, 30.0F, naga.getMaxHeadXRot());
        if (state.fireSnakeTicks <= 0) {
            naga.getNavigation().moveTo(
                    target,
                    BossRefactorTwilightForestConfig.NAGA.cruiseSpeed.get());
        }
        state.stateTicks++;
        if (state.stateTicks >= state.stateDuration) {
            beginNextSkill(naga, target, state);
        }
    }

    private static void tickPounceWindup(Naga naga, NagaCombatState state) {
        stopHorizontalMovement(naga);
        lookAtLockedPoint(naga, state);
        setPounceTelegraph(naga, state);
        sendWindupParticles(naga, ParticleTypes.CLOUD);
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        enterState(state, NagaActionState.POUNCE_FLIGHT,
                BossRefactorTwilightForestConfig.NAGA.pounceFlightTicks.get());
        double horizontalSpeed = BossRefactorTwilightForestConfig.NAGA
                .pounceHorizontalSpeed.get();
        double verticalSpeed = BossRefactorTwilightForestConfig.NAGA
                .pounceVerticalSpeed.get();
        naga.setDeltaMovement(
                state.directionX * horizontalSpeed,
                verticalSpeed,
                state.directionZ * horizontalSpeed);
        naga.setOnGround(false);
    }

    private static void tickPounceFlight(Naga naga, NagaCombatState state) {
        setPounceTelegraph(naga, state);
        lookAtLockedPoint(naga, state);
        double horizontalSpeed = BossRefactorTwilightForestConfig.NAGA
                .pounceHorizontalSpeed.get();
        Vec3 movement = naga.getDeltaMovement();
        naga.setDeltaMovement(
                state.directionX * horizontalSpeed,
                movement.y,
                state.directionZ * horizontalSpeed);
        state.stateTicks++;
        if (state.stateTicks >= state.stateDuration
                || (state.stateTicks > 2 && naga.onGround())) {
            enterState(state, NagaActionState.POUNCE_IMPACT, 1);
        }
    }

    private static void performPounceImpact(
            Naga naga, NagaCombatState state) {
        stopHorizontalMovement(naga);
        float damage = attackDamage(naga)
                * BossRefactorTwilightForestConfig.NAGA
                        .pounceDamageMultiplier.get().floatValue();
        double radius = BossRefactorTwilightForestConfig.NAGA
                .pounceImpactRadius.get();
        boolean defended = damageCircle(
                naga, state.lockedX, state.lockedY, state.lockedZ,
                radius, damage, true);
        naga.playSound(SoundEvents.GENERIC_EXPLODE, 2.0F, 0.8F);
        ((ServerLevel) naga.level()).sendParticles(
                ParticleTypes.EXPLOSION,
                state.lockedX, state.lockedY + 0.2, state.lockedZ,
                16, radius * 0.5, 0.3, radius * 0.5, 0.05);
        if (defended) {
            interruptWithPossibleStun(naga, state);
            return;
        }
        beginFollowUpTail(naga, state);
    }

    private static void tickTailWindup(Naga naga, NagaCombatState state) {
        stopHorizontalMovement(naga);
        setTailTelegraph(naga, state);
        sendWindupParticles(naga, ParticleTypes.SWEEP_ATTACK);
        state.stateTicks++;
        if (state.stateTicks >= state.stateDuration) {
            enterState(state, NagaActionState.TAIL_STRIKE,
                    BossRefactorTwilightForestConfig.NAGA
                            .tailDurationTicks.get());
        }
    }

    private static void performTailStrike(Naga naga, NagaCombatState state) {
        stopHorizontalMovement(naga);
        setTailTelegraph(naga, state);
        if (NagaBodyRules.shouldApplyTailDamage(
                state.stateTicks, state.stateDuration,
                state.tailDamageApplied)) {
            state.tailDamageApplied = true;
            if (applyTailDamage(naga, state)) {
                interruptWithPossibleStun(naga, state);
                return;
            }
        }
        state.stateTicks++;
        if (state.stateTicks >= state.stateDuration) {
            beginCruise(naga);
        }
    }

    private static boolean applyTailDamage(Naga naga, NagaCombatState state) {
        double radius = BossRefactorTwilightForestConfig.NAGA.tailRadius.get();
        double halfAngle = BossRefactorTwilightForestConfig.NAGA
                .tailHalfAngleDegrees.get();
        float damage = attackDamage(naga)
                * BossRefactorTwilightForestConfig.NAGA
                        .tailDamageMultiplier.get().floatValue();
        DamageSource source = naga.damageSources().mobAttack(naga);
        boolean defended = false;
        for (LivingEntity target : combatTargets(
            naga, naga.getBoundingBox().inflate(
                radius,
                BossRefactorTwilightForestConfig.NAGA
                    .tailVerticalRange.get(),
                radius))) {
            if (!NagaCombatRules.isInsideArc(
                    target.getX(), target.getZ(),
                    naga.getX(), naga.getZ(),
                    state.directionX, state.directionZ,
                    radius, halfAngle)) {
                continue;
            }
            if (hurtWithShieldCheck(target, source, damage)) {
                defended = true;
                break;
            }
        }
        naga.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0F, 0.65F);
        return defended;
    }

    private static void tickVenomWindup(Naga naga, NagaCombatState state) {
        stopHorizontalMovement(naga);
        setVenomTelegraphs(naga, state);
        sendWindupParticles(naga, ParticleTypes.ITEM_SLIME);
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        ServerLevel level = (ServerLevel) naga.level();
        Vec3 origin = new Vec3(
                naga.getX(), naga.getEyeY() - 0.2, naga.getZ())
                .add(state.directionX * naga.getBbWidth() * 0.5,
                        0.0,
                        state.directionZ * naga.getBbWidth() * 0.5);
        state.venomProjectileId = NagaAreaEffectData.get(level)
                .spawnVenomProjectile(
                        level, naga, origin,
                        new Vec3(state.directionX, 0.0, state.directionZ),
                        attackDamage(naga), state.secondPhase);
        enterState(state, NagaActionState.VENOM_PROJECTILE,
                Math.max(1, (int) Math.ceil(
                        BossRefactorTwilightForestConfig.NAGA
                                .venomLength.get()
                        / BossRefactorTwilightForestConfig.NAGA
                                .venomProjectileSpeed.get()) + 5));
        access(naga).bossRefactorTF$setSecondaryTelegraph(AttackTelegraph.NONE);
        naga.playSound(SoundEvents.SLIME_SQUISH, 2.0F, 0.55F);
    }

    private static void tickVenomProjectile(
            Naga naga, NagaCombatState state) {
        stopHorizontalMovement(naga);
        access(naga).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
        state.stateTicks++;
        if (state.venomProjectileId == null
                || !NagaAreaEffectData.get((ServerLevel) naga.level())
                        .hasVenomProjectile(state.venomProjectileId)
                || state.stateTicks >= state.stateDuration) {
            state.venomProjectileId = null;
            beginCruise(naga);
        }
    }

    private static void tickStunned(Naga naga, NagaCombatState state) {
        naga.getNavigation().stop();
        naga.setDeltaMovement(Vec3.ZERO);
        naga.setYRot(naga.yRotO);
        ((ServerLevel) naga.level()).sendParticles(
                ParticleTypes.CRIT,
                naga.getX(), naga.getY() + naga.getBbHeight(), naga.getZ(),
                3, naga.getBbWidth() * 0.35, 0.2,
                naga.getBbWidth() * 0.35, 0.02);
        state.stateTicks++;
        if (state.stateTicks >= state.stateDuration) {
            beginCruise(naga);
        }
    }

    private static void beginNextSkill(
            Naga naga, LivingEntity target, NagaCombatState state) {
        NagaSkill next = NagaCombatRules.nextSkill(
                state.lastSkill,
                state.pounceCooldown <= 0,
                state.tailCooldown <= 0,
                state.venomCooldown <= 0,
                naga.getRandom().nextInt());
        if (next == NagaSkill.NONE) {
            beginCruise(naga);
            return;
        }
        state.lastSkill = next;
        switch (next) {
            case POUNCE -> beginPounce(naga, target, state);
            case TAIL_SWEEP -> beginTail(naga, target, state);
            case VENOM_BREATH -> beginVenom(naga, target, state);
            case NONE -> beginCruise(naga);
        }
    }

    private static void beginPounce(
            Naga naga, LivingEntity target, NagaCombatState state) {
        lockTargetPoint(naga, target, state);
        state.pounceCooldown = BossRefactorTwilightForestConfig.NAGA
                .pounceCooldownTicks.get();
        enterState(state, NagaActionState.POUNCE_WINDUP,
                BossRefactorTwilightForestConfig.NAGA.pounceWindupTicks.get());
        naga.getNavigation().stop();
        setPounceTelegraph(naga, state);
    }

    private static void beginTail(
            Naga naga, LivingEntity target, NagaCombatState state) {
        lockDirection(naga, target.position(), state);
        state.tailCooldown = BossRefactorTwilightForestConfig.NAGA
                .tailCooldownTicks.get();
        enterState(state, NagaActionState.TAIL_WINDUP,
                BossRefactorTwilightForestConfig.NAGA.tailWindupTicks.get());
        naga.getNavigation().stop();
        setTailTelegraph(naga, state);
    }

    private static void beginFollowUpTail(
            Naga naga, NagaCombatState state) {
        state.tailCooldown = BossRefactorTwilightForestConfig.NAGA
                .tailCooldownTicks.get();
        enterState(state, NagaActionState.TAIL_WINDUP,
            BossRefactorTwilightForestConfig.NAGA.tailWindupTicks.get());
        setTailTelegraph(naga, state);
    }

    private static void beginVenom(
            Naga naga, LivingEntity target, NagaCombatState state) {
        lockDirection(naga, target.position(), state);
        state.venomCooldown = BossRefactorTwilightForestConfig.NAGA
                .venomCooldownTicks.get();
        enterState(state, NagaActionState.VENOM_WINDUP,
                BossRefactorTwilightForestConfig.NAGA.venomWindupTicks.get());
        naga.getNavigation().stop();
        setVenomTelegraphs(naga, state);
    }

    private static void beginCruise(Naga naga) {
        NagaCombatState state = state(naga);
        int duration = NagaCombatRules.cruiseTicks(
                BossRefactorTwilightForestConfig.NAGA.cruiseMinimumTicks.get(),
                BossRefactorTwilightForestConfig.NAGA.cruiseMaximumTicks.get(),
                naga.getRandom().nextDouble());
        enterState(state, NagaActionState.CRUISING, duration);
        naga.setDazed(false);
        state.venomProjectileId = null;
        clearTelegraphs(naga);
        closeParryWindow(naga);
    }

    private static void enterIdle(Naga naga) {
        NagaCombatState state = state(naga);
        if (state.venomProjectileId != null
                && naga.level() instanceof ServerLevel level) {
            NagaAreaEffectData.get(level)
                    .removeVenomProjectile(state.venomProjectileId);
        }
        state.venomProjectileId = null;
        enterState(state, NagaActionState.IDLE, 0);
        naga.setDazed(false);
        naga.getNavigation().stop();
        clearTelegraphs(naga);
        closeParryWindow(naga);
    }

    private static void interruptWithPossibleStun(
            Naga naga, NagaCombatState state) {
        if (state.venomProjectileId != null) {
            NagaAreaEffectData.get((ServerLevel) naga.level())
                    .removeVenomProjectile(state.venomProjectileId);
            state.venomProjectileId = null;
        }
        clearTelegraphs(naga);
        closeParryWindow(naga);
        naga.getNavigation().stop();
        if (!NagaCombatRules.shouldEnterStunnedState(
            state.secondPhase, state.stunImmunityAvailable)) {
            state.stunImmunityAvailable = false;
            state.stunImmunityRecoveryTicks =
                    BossRefactorTwilightForestConfig.NAGA
                            .stunImmunityRecoveryTicks.get();
            beginCruise(naga);
            return;
        }
        enterState(state, NagaActionState.STUNNED,
                BossRefactorTwilightForestConfig.NAGA.stunDurationTicks.get());
        naga.setDazed(true);
    }

    private static void cancelVenom(Naga naga, NagaCombatState state) {
        if (state.venomProjectileId != null) {
            NagaAreaEffectData.get((ServerLevel) naga.level())
                    .removeVenomProjectile(state.venomProjectileId);
            state.venomProjectileId = null;
        }
        beginCruise(naga);
    }

    private static void updateSecondPhase(
            Naga naga, NagaCombatState state) {
        if (!state.secondPhase && NagaCombatRules.isSecondPhase(
                naga.getHealth(), naga.getMaxHealth(),
                BossRefactorTwilightForestConfig.NAGA
                        .secondPhaseHealthFraction.get())) {
            state.secondPhase = true;
            state.stunImmunityAvailable = true;
            state.stunImmunityRecoveryTicks = 0;
            state.fireSnakeTicks = BossRefactorTwilightForestConfig.NAGA
                    .fireSnakeDurationTicks.get();
            state.fireSnakeElapsedTicks = 0;
            state.hasLastFirePoint = false;
            naga.playSound(SoundEvents.ENDER_DRAGON_GROWL, 3.0F, 0.7F);
        }
        synchronizeAttributeModifiers(naga, state);
    }

    private static void synchronizeAttributeModifiers(
            Naga naga, NagaCombatState state) {
        double phaseAttackAmount = BossRefactorTwilightForestConfig.NAGA
                .secondPhaseAttackMultiplier.get() - 1.0;
        double phaseSpeedAmount = BossRefactorTwilightForestConfig.NAGA
                .secondPhaseSpeedMultiplier.get() - 1.0;
        synchronizeModifier(
                naga.getAttribute(Attributes.ATTACK_DAMAGE),
                PHASE_ATTACK_MODIFIER_ID,
                PHASE_ATTACK_MODIFIER_NAME,
                phaseAttackAmount,
                state.secondPhase);
        synchronizeModifier(
                naga.getAttribute(Attributes.MOVEMENT_SPEED),
                PHASE_SPEED_MODIFIER_ID,
                PHASE_SPEED_MODIFIER_NAME,
                phaseSpeedAmount,
                state.secondPhase);
        synchronizeModifier(
                naga.getAttribute(Attributes.MOVEMENT_SPEED),
                FIRE_SNAKE_SPEED_MODIFIER_ID,
                FIRE_SNAKE_SPEED_MODIFIER_NAME,
                BossRefactorTwilightForestConfig.NAGA
                        .fireSnakeSpeedMultiplier.get() - 1.0,
                state.fireSnakeTicks > 0);
    }

    private static void synchronizeModifier(
            @Nullable AttributeInstance attribute, UUID id,
            String name, double amount, boolean active) {
        if (attribute == null) {
            return;
        }
        AttributeModifier existing = attribute.getModifier(id);
        if (!active) {
            if (existing != null) {
                attribute.removeModifier(id);
            }
            return;
        }
        if (existing != null && Double.compare(existing.getAmount(), amount) == 0) {
            return;
        }
        if (existing != null) {
            attribute.removeModifier(id);
        }
        attribute.addTransientModifier(new AttributeModifier(
                id, name, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void tickStunImmunity(
            Naga naga, NagaCombatState state) {
        if (!state.secondPhase || state.stunImmunityAvailable
                || state.stunImmunityRecoveryTicks <= 0) {
            return;
        }
        state.stunImmunityRecoveryTicks--;
        if (state.stunImmunityRecoveryTicks == 0) {
            state.stunImmunityAvailable = true;
            synchronizeIndicator(naga);
        }
    }

    private static void tickFireSnake(
            Naga naga, NagaCombatState state) {
        if (state.fireSnakeTicks <= 0) {
            state.hasLastFirePoint = false;
            return;
        }
        LivingEntity target = naga.getTarget();
        if (isRetainableTarget(naga, target)) {
            if (state.actionState == NagaActionState.CRUISING) {
            double radius = NagaCombatRules.shrinkingOrbitRadius(
                state.fireSnakeElapsedTicks,
                BossRefactorTwilightForestConfig.NAGA
                    .fireSnakeDurationTicks.get(),
                BossRefactorTwilightForestConfig.NAGA
                    .fireStartOrbitRadius.get(),
                BossRefactorTwilightForestConfig.NAGA
                    .fireEndOrbitRadius.get());
            double angle = state.fireSnakeElapsedTicks
                * BossRefactorTwilightForestConfig.NAGA
                    .fireOrbitAngularSpeed.get();
            naga.getNavigation().moveTo(
                target.getX() + Math.cos(angle) * radius,
                target.getY(),
                target.getZ() + Math.sin(angle) * radius,
                BossRefactorTwilightForestConfig.NAGA.cruiseSpeed.get());
            }
            appendFireTrail(naga, state);
        } else {
            state.hasLastFirePoint = false;
        }
        state.fireSnakeTicks--;
        state.fireSnakeElapsedTicks++;
        if (state.fireSnakeTicks == 0) {
            synchronizeAttributeModifiers(naga, state);
        }
    }

    private static void appendFireTrail(
            Naga naga, NagaCombatState state) {
        Vec3 position = naga.position();
        double spacing = BossRefactorTwilightForestConfig.NAGA
                .firePointSpacing.get();
        if (!state.hasLastFirePoint) {
            addFirePoint(naga, state, position);
            return;
        }
        Vec3 previous = new Vec3(
                state.lastFirePointX,
                state.lastFirePointY,
                state.lastFirePointZ);
        double distance = previous.distanceTo(position);
        if (distance < spacing) {
            return;
        }
        Vec3 direction = position.subtract(previous).normalize();
        int count = (int) Math.floor(distance / spacing);
        for (int index = 1; index <= count; index++) {
            addFirePoint(naga, state,
                    previous.add(direction.scale(spacing * index)));
        }
    }

    private static void addFirePoint(
            Naga naga, NagaCombatState state, Vec3 position) {
        NagaAreaEffectData.get((ServerLevel) naga.level())
                .addFirePoint(naga, position, attackDamage(naga));
        state.hasLastFirePoint = true;
        state.lastFirePointX = position.x;
        state.lastFirePointY = position.y;
        state.lastFirePointZ = position.z;
    }

    private static boolean damageCircle(
            Naga naga, double x, double y, double z,
            double radius, float damage, boolean shieldStuns) {
        DamageSource source = naga.damageSources().mobAttack(naga);
        boolean defended = false;
        double verticalRange = BossRefactorTwilightForestConfig.NAGA
            .pounceVerticalRange.get();
        AABB bounds = new AABB(
            x - radius, y - verticalRange, z - radius,
            x + radius, y + verticalRange, z + radius);
        for (LivingEntity target : combatTargets(naga, bounds)) {
            if (!NagaCombatRules.isInsideCircle(
                    target.getX(), target.getZ(), x, z, radius)) {
                continue;
            }
            boolean blocked = hurtWithShieldCheck(target, source, damage);
            if (shieldStuns && blocked) {
                return true;
            }
        }
        return defended;
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
            applyShieldCooldown((ServerPlayer) target, usedShield);
        }
        return shieldBlocked;
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

    private static List<LivingEntity> combatTargets(Naga naga, AABB bounds) {
        return naga.level().getEntitiesOfClass(
                LivingEntity.class, bounds,
                target -> isCombatTarget(naga, target));
    }

    private static boolean isCombatTarget(
            Naga naga, LivingEntity target) {
        return target != naga
                && target.isAlive()
                && !naga.isAlliedTo(target)
                && (!(target instanceof Player player)
                        || (!player.isCreative() && !player.isSpectator()));
    }

    private static boolean isRetainableTarget(
            Naga naga, @Nullable LivingEntity target) {
        return target != null
                && !target.isRemoved()
                && isCombatTarget(naga, target);
    }

    private static void lockTargetPoint(
            Naga naga, LivingEntity target, NagaCombatState state) {
        state.lockedX = target.getX();
        state.lockedY = target.getY();
        state.lockedZ = target.getZ();
        lockDirection(naga, target.position(), state);
    }

    private static void lockDirection(
            Naga naga, Vec3 target, NagaCombatState state) {
        Vec3 direction = target.subtract(naga.position())
                .multiply(1.0, 0.0, 1.0);
        if (direction.lengthSqr() < 1.0E-6) {
            direction = naga.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        direction = direction.normalize();
        state.directionX = direction.x;
        state.directionZ = direction.z;
    }

    private static void lookAtLockedPoint(
            Naga naga, NagaCombatState state) {
        naga.getLookControl().setLookAt(
                state.lockedX, state.lockedY, state.lockedZ,
                30.0F, naga.getMaxHeadXRot());
    }

    private static void setPounceTelegraph(
            Naga naga, NagaCombatState state) {
        access(naga).bossRefactorTF$setPrimaryTelegraph(new AttackTelegraph(
                AttackTelegraphShape.CIRCLE,
                (float) state.lockedX,
                (float) state.lockedY,
                (float) state.lockedZ,
                0.0F, 1.0F, 0.0F, 0.0F,
                BossRefactorTwilightForestConfig.NAGA
                        .pounceImpactRadius.get().floatValue(),
                state.actionState == NagaActionState.POUNCE_WINDUP
                        ? AttackTelegraph.windupProgress(
                                state.stateTicks, state.stateDuration)
                        : 1.0F));
        access(naga).bossRefactorTF$setSecondaryTelegraph(AttackTelegraph.NONE);
    }

    private static void setTailTelegraph(
            Naga naga, NagaCombatState state) {
        access(naga).bossRefactorTF$setPrimaryTelegraph(new AttackTelegraph(
                AttackTelegraphShape.ARC,
                (float) state.directionX,
                (float) state.directionZ,
                BossRefactorTwilightForestConfig.NAGA
                        .tailRadius.get().floatValue(),
                BossRefactorTwilightForestConfig.NAGA
                        .tailHalfAngleDegrees.get().floatValue(),
                0.0F,
                state.actionState == NagaActionState.TAIL_WINDUP
                        ? AttackTelegraph.windupProgress(
                                state.stateTicks, state.stateDuration)
                        : 1.0F));
        access(naga).bossRefactorTF$setSecondaryTelegraph(AttackTelegraph.NONE);
    }

    private static void setVenomTelegraphs(
            Naga naga, NagaCombatState state) {
        float progress = AttackTelegraph.windupProgress(
                state.stateTicks, state.stateDuration);
        float length = BossRefactorTwilightForestConfig.NAGA
                .venomLength.get().floatValue();
        access(naga).bossRefactorTF$setPrimaryTelegraph(new AttackTelegraph(
                AttackTelegraphShape.CORRIDOR,
                (float) state.directionX,
                (float) state.directionZ,
                length,
                BossRefactorTwilightForestConfig.NAGA
                        .venomWidth.get().floatValue() * 0.5F,
                0.0F,
                progress));
        access(naga).bossRefactorTF$setSecondaryTelegraph(new AttackTelegraph(
                AttackTelegraphShape.CIRCLE,
                (float) (naga.getX() + state.directionX * length),
                (float) naga.getY(),
                (float) (naga.getZ() + state.directionZ * length),
                0.0F, 1.0F, 0.0F, 0.0F,
                BossRefactorTwilightForestConfig.NAGA
                        .venomCloudRadius.get().floatValue(),
                progress));
    }

    private static void sendWindupParticles(
            Naga naga, ParticleOptions particle) {
        int count = BossRefactorTwilightForestConfig.NAGA
                .windupParticlesPerTick.get();
        if (count <= 0) {
            return;
        }
        ((ServerLevel) naga.level()).sendParticles(
                particle,
                naga.getX(), naga.getY() + naga.getBbHeight() * 0.6,
                naga.getZ(), count,
                naga.getBbWidth() * 0.5,
                naga.getBbHeight() * 0.35,
                naga.getBbWidth() * 0.5,
                0.03);
    }

    private static void decrementCooldowns(NagaCombatState state) {
        state.pounceCooldown = Math.max(0, state.pounceCooldown - 1);
        state.tailCooldown = Math.max(0, state.tailCooldown - 1);
        state.venomCooldown = Math.max(0, state.venomCooldown - 1);
    }

    private static void enterState(
            NagaCombatState state,
            NagaActionState actionState, int duration) {
        state.actionState = actionState;
        state.stateTicks = 0;
        state.stateDuration = Math.max(0, duration);
        if (actionState == NagaActionState.TAIL_STRIKE) {
            state.tailDamageApplied = false;
        }
    }

    private static void stopHorizontalMovement(Naga naga) {
        naga.getNavigation().stop();
        Vec3 movement = naga.getDeltaMovement();
        naga.setDeltaMovement(0.0, movement.y, 0.0);
    }

    private static void clearTelegraphs(Naga naga) {
        access(naga).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
        access(naga).bossRefactorTF$setSecondaryTelegraph(AttackTelegraph.NONE);
    }

    private static void synchronizeIndicator(Naga naga) {
        NagaCombatState state = state(naga);
        access(naga).bossRefactorTF$setStunImmunityVisible(
                state.secondPhase && state.stunImmunityAvailable);
    }

    private static void synchronizeFireSnakeVisualState(
            Naga naga, NagaCombatState state) {
        access(naga).bossRefactorTF$setFireSnakeVisualActive(
                NagaCombatRules.isFireSnakeVisualActive(state.fireSnakeTicks));
    }

    private static void synchronizeBodyState(
            Naga naga, NagaCombatState state) {
        NagaCombatAccess access = access(naga);
        NagaBodyPose pose = switch (state.actionState) {
            case TAIL_WINDUP -> NagaBodyPose.TAIL_WINDUP;
            case TAIL_STRIKE -> NagaBodyPose.TAIL_STRIKE;
            case STUNNED -> NagaBodyPose.FROZEN;
            default -> NagaBodyPose.TRAIL;
        };
        float progress = switch (state.actionState) {
            case TAIL_WINDUP -> AttackTelegraph.windupProgress(
                    state.stateTicks, state.stateDuration);
            case TAIL_STRIKE -> (float) NagaBodyRules.tailSweepProgress(
                    state.stateTicks, state.stateDuration);
            default -> 0.0F;
        };
        access.bossRefactorTF$setBodyPose(pose);
        access.bossRefactorTF$setBodyPoseProgress(progress);
        access.bossRefactorTF$setBodyDirection(
                state.directionX, state.directionZ);
        naga.setDazed(NagaCombatRules.isStunned(state.actionState));
    }

    private static void synchronizeParryWindow(Naga naga) {
        if (!(naga instanceof TwilightBossParryAccess parryAccess)) {
            return;
        }
        var bridge = TwilightBossParryIntegration.bridge();
        if (!bridge.isAvailable()) {
            return;
        }
        if (!parryAccess.bossRefactorTF$isCurrentAttackParryable()) {
            closeParryWindow(naga);
        } else if (!bridge.isParryable(naga)
                && !bridge.isParried(naga)
                && !bridge.openWindow(naga)) {
            BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to open the SenDimS parry window for Naga {}",
                    naga.getUUID());
        }
    }

    private static void closeParryWindow(Naga naga) {
        var bridge = TwilightBossParryIntegration.bridge();
        if (bridge.isAvailable() && bridge.isParryable(naga)
                && !bridge.closeWindow(naga)) {
            BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to close the SenDimS parry window for Naga {}",
                    naga.getUUID());
        }
    }

    private static float attackDamage(Naga naga) {
        return (float) naga.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private static NagaCombatState state(Naga naga) {
        return access(naga).bossRefactorTF$getNagaCombatState();
    }

    private static NagaCombatAccess access(Naga naga) {
        return (NagaCombatAccess) naga;
    }
}