package com.tonywww.bossrefactortf.boss.lich;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.TwilightBossParryIntegration;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.boss.Lich;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public final class LichCombat {
    private static final double CLONE_SEARCH_RANGE = 128.0;
    private static final ResourceKey<DamageType> MELEE_MAGIC_DAMAGE =
        ResourceKey.create(
            Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(
                BossRefactorTwilightForest.MOD_ID,
                "lich_melee_magic"));

    private LichCombat() {
    }

    public static boolean customCombatEnabled() {
        return LichCombatConfig.enabled();
    }

    public static boolean hasCombatWork(Lich lich) {
        LichCombatState state = state(lich);
        return state.customClone
                || state.actionState != LichActionState.IDLE
                || state.secondPhase
                && isRetainableTarget(lich, lich.getTarget());
    }

    public static boolean requiresExclusiveControl(Lich lich) {
        LichCombatState state = state(lich);
        return state.secondPhase && !state.customClone
                || state.customClone
                && state.actionState == LichActionState.SHIELD_BASH_WINDUP
                || !state.customClone && !state.secondPhase
                && (state.actionState == LichActionState.SHIELD_BASH_WINDUP
                || state.actionState == LichActionState.STUNNED);
    }

    public static boolean isSecondPhase(Lich lich) {
        return customCombatEnabled() && state(lich).secondPhase;
    }

    public static boolean isCustomClone(Lich lich) {
        return customCombatEnabled() && state(lich).customClone;
    }

    public static boolean hasManagedCloneState(Lich lich) {
        return state(lich).customClone;
    }

    public static boolean shouldSuppressVanillaPhaseLogic(Lich lich) {
        return isSecondPhase(lich) && !isCustomClone(lich);
    }

    public static boolean shouldSuppressVanillaShadowsGoal(Lich lich) {
        LichCombatState state = state(lich);
        return !state.customClone
                && (state.secondPhase
                || state.actionState == LichActionState.SHIELD_BASH_WINDUP
                || state.actionState == LichActionState.STUNNED);
    }

    public static boolean shouldSuppressVanillaCloneAttack(Lich lich) {
        return isCustomClone(lich)
                && state(lich).actionState
                == LichActionState.SHIELD_BASH_WINDUP;
    }

    public static void observeShieldStrength(Lich lich) {
        LichCombatState state = state(lich);
        int currentLayers = Math.max(0, lich.getShieldStrength());
        if (!state.shieldTracked) {
            state.shieldTracked = true;
            state.shieldLayers = currentLayers;
            if (customCombatEnabled()
                    && !lich.isShadowClone()
                    && LichCombatRules.shouldEnterSecondPhase(
                            state.secondPhase, state.customClone,
                            currentLayers, currentLayers)) {
                enterSecondPhase(lich, state);
            }
            return;
        }
        onShieldStrengthChanged(
                lich, state.shieldLayers, currentLayers);
    }

    public static void onShieldStrengthChanged(
            Lich lich, int previousLayers, int currentLayers) {
        LichCombatState state = state(lich);
        state.shieldTracked = true;
        state.shieldLayers = Math.max(0, currentLayers);
        if (!customCombatEnabled()
                || lich.isShadowClone() || state.customClone
                || !LichCombatRules.shouldEnterSecondPhase(
                        state.secondPhase, state.customClone,
                        previousLayers, currentLayers)) {
            return;
        }
        if (state.actionState == LichActionState.SHIELD_BASH_WINDUP) {
            state.secondPhasePending = true;
            return;
        }
        enterSecondPhase(lich, state);
    }

    public static float modifyIncomingDamage(Lich lich, float damage) {
        if (!isSecondPhase(lich) || isCustomClone(lich)) {
            return damage;
        }
        return (float) LichCombatRules.damageAfterShieldReduction(
                damage, lich.getShieldStrength(),
                LichCombatConfig.shieldDamageReductionPerLayer());
    }

    public static void onSuccessfulMeleeHit(
            Lich lich, LivingEntity target) {
        LichCombatState state = state(lich);
        if (!isSecondPhase(lich) || state.customClone
                || !isCombatTarget(lich, target)) {
            return;
        }
        long gameTime = lich.level().getGameTime();
        if (state.lastMeleeMagicGameTime == gameTime
                && target.getUUID().equals(state.lastMeleeMagicTargetId)) {
            return;
        }
        state.lastMeleeMagicGameTime = gameTime;
        state.lastMeleeMagicTargetId = target.getUUID();
        float damage = (float) LichCombatRules.meleeMagicDamage(
                attackDamage(lich),
                LichCombatConfig.meleeMagicDamageMultiplier());
        if (damage > 0.0F) {
            target.hurt(meleeMagicDamageSource(lich), damage);
        }
    }

    public static boolean isCurrentAttackParryable(Lich lich) {
        LichCombatState state = state(lich);
        return customCombatEnabled()
                && !state.shieldBashParried
                && LichCombatRules.isCurrentAttackParryable(state.actionState);
    }

    public static boolean suppressesExternalParryStun(Lich lich) {
        return isCurrentAttackParryable(lich);
    }

    public static boolean acceptParry(
            Lich lich, @Nullable LivingEntity actor) {
        LichCombatState state = state(lich);
        if (!isCurrentAttackParryable(lich)) {
            return false;
        }
        long gameTime = lich.level().getGameTime();
        if (state.lastParryGameTime == gameTime) {
            return true;
        }
        state.lastParryGameTime = gameTime;
        state.shieldBashParried = true;
        state.parryingEntityId = actor == null ? null : actor.getUUID();
        return true;
    }

    public static void synchronizeParryWindow(Lich lich) {
        if (!(lich instanceof TwilightBossParryAccess parryAccess)) {
            return;
        }
        var bridge = TwilightBossParryIntegration.bridge();
        if (!bridge.isAvailable()) {
            return;
        }
        if (!parryAccess.bossRefactorTF$isCurrentAttackParryable()) {
            closeParryWindow(lich);
        } else if (!bridge.isParryable(lich) && !bridge.isParried(lich)
                && !bridge.openWindow(lich)) {
            BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to open the SenDimS parry window for Lich {}",
                    lich.getUUID());
        }
    }

    public static int parryFeedbackTicks() {
        return Math.max(1, LichCombatConfig.shieldBashParryFeedbackTicks());
    }

    public static void tick(Lich lich) {
        if (lich.level().isClientSide() || !customCombatEnabled()) {
            return;
        }
        LichCombatState state = state(lich);
        if (state.customClone) {
            tickCustomClone(lich, state);
        } else if (state.secondPhase) {
            tickSecondPhaseMaster(lich, state);
        } else {
            tickFirstPhaseMaster(lich, state);
        }
        synchronizeParryWindow(lich);
    }

    public static void tickFirstPhaseBackground(Lich lich) {
        if (lich.level().isClientSide() || !customCombatEnabled()) {
            return;
        }
        LichCombatState state = state(lich);
        if (state.customClone || state.secondPhase
                || state.actionState != LichActionState.IDLE) {
            return;
        }
        tickFirstPhaseMaster(lich, state);
    }

    public static void restoreVisualState(Lich lich) {
        access(lich).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
        if (state(lich).actionState == LichActionState.SHIELD_BASH_WINDUP) {
            setShieldBashTelegraph(lich, state(lich));
        }
    }

    public static void stopWhenUnusable(Lich lich) {
        if (!customCombatEnabled()) {
            LichCombatState state = state(lich);
            if (state.customClone) {
                clearMasterCloneReference(lich, state);
                lich.discard();
                return;
            }
            cleanupCustomClone(lich);
            enterIdle(lich);
        }
    }

    public static void onRemoved(Lich lich) {
        LichCombatState state = state(lich);
        if (state.customClone) {
            clearMasterCloneReference(lich, state);
        } else {
            cleanupCustomClone(lich);
        }
        closeParryWindow(lich);
    }

    public static void cleanupCustomClone(Lich master) {
        if (!(master.level() instanceof ServerLevel)) {
            return;
        }
        LichCombatState state = state(master);
        Lich clone = findLivingCustomClone(master);
        if (clone != null) {
            clone.discard();
        }
        state.customCloneId = null;
    }

    private static void enterSecondPhase(
            Lich lich, LichCombatState state) {
        state.secondPhase = true;
        state.secondPhasePending = false;
        state.shieldBashCooldown = 0;
        state.summonCooldown = 0;
        state.meleeCooldown = 0;
        int shieldLayers = Math.max(
                0, LichCombatConfig.secondPhaseShieldLayers());
        lich.setShieldStrength(shieldLayers);
        state.shieldLayers = shieldLayers;
        lich.setMinionsToSummon(0);
        enterIdle(lich);
    }

    private static void tickFirstPhaseMaster(
            Lich lich, LichCombatState state) {
        decrementCooldowns(state);
        switch (state.actionState) {
            case IDLE -> {
                LivingEntity target = lich.getTarget();
                if (!state.secondPhase && !state.customClone
                        && isRetainableTarget(lich, target)
                        && state.shieldBashCooldown <= 0) {
                    beginShieldBash(lich, state, target);
                }
            }
            case SHIELD_BASH_WINDUP -> tickShieldBashWindup(lich, state);
            case STUNNED -> tickStunned(lich, state);
            default -> enterIdle(lich);
        }
    }

    private static void tickSecondPhaseMaster(
            Lich lich, LichCombatState state) {
        decrementCooldowns(state);
        switch (state.actionState) {
            case IDLE -> beginNextMasterAction(lich, state);
            case CRUISE -> tickCruise(lich, state);
            case SHIELD_BASH_WINDUP -> tickShieldBashWindup(lich, state);
            case SUMMON_WINDUP -> tickSummonWindup(lich, state);
            case STUNNED -> tickStunned(lich, state);
        }
    }

    private static void tickCustomClone(
            Lich clone, LichCombatState state) {
        clone.setInvulnerable(true);
        Lich master = findMaster(clone, state);
        if (!isValidMaster(master)
            || !clone.getUUID().equals(state(master).customCloneId)) {
            clone.discard();
            return;
        }
        LivingEntity masterTarget = master.getTarget();
        if (!isRetainableTarget(clone, clone.getTarget())
                && isRetainableTarget(clone, masterTarget)) {
            clone.setTarget(masterTarget);
        }
        if (state.actionState == LichActionState.SHIELD_BASH_WINDUP) {
            tickShieldBashWindup(clone, state);
            return;
        }
        state.cloneLifetimeTicks++;
        if (state.cloneLifetimeTicks >= LichCombatConfig.cloneLifetimeTicks()) {
            beginShieldBash(clone, state, clone.getTarget());
        }
    }

    private static void beginNextMasterAction(
            Lich lich, LichCombatState state) {
        if (!isRetainableTarget(lich, lich.getTarget())) {
            enterIdle(lich);
            return;
        }
        if (LichCombatRules.shouldSummonClone(
                state.secondPhase, false,
                findLivingCustomClone(lich) != null,
                state.summonCooldown)) {
            beginSummon(lich, state);
        } else {
            beginCruise(lich, state);
        }
    }

    private static void tickCruise(
            Lich lich, LichCombatState state) {
        LivingEntity target = lich.getTarget();
        if (!isRetainableTarget(lich, target)) {
            enterIdle(lich);
            return;
        }
        lich.getNavigation().moveTo(target, LichCombatConfig.meleeSpeed());
        lich.getLookControl().setLookAt(
                target, 30.0F, lich.getMaxHeadXRot());
        tickOrdinaryMelee(lich, target, state);
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        if (LichCombatRules.shouldSummonClone(
                true, false, findLivingCustomClone(lich) != null,
                state.summonCooldown)) {
            beginSummon(lich, state);
        } else if (state.shieldBashCooldown <= 0) {
            beginShieldBash(lich, state, target);
        } else {
            beginCruise(lich, state);
        }
    }

    private static void tickOrdinaryMelee(
            Lich lich, LivingEntity target, LichCombatState state) {
        if (state.meleeCooldown > 0) {
            return;
        }
        double reach = lich.getBbWidth() * 2.0 + target.getBbWidth();
        if (lich.distanceToSqr(target) > reach * reach
                || !lich.getSensing().hasLineOfSight(target)) {
            return;
        }
        lich.swing(InteractionHand.MAIN_HAND);
        if (lich.doHurtTarget(target)) {
            onSuccessfulMeleeHit(lich, target);
        }
        state.meleeCooldown = Math.max(
            0, LichCombatConfig.meleeCooldownTicks());
    }

    private static void beginCruise(
            Lich lich, LichCombatState state) {
        enterState(state, LichActionState.CRUISE,
                LichCombatRules.cruiseTicks(
                        LichCombatConfig.cruiseMinimumTicks(),
                        LichCombatConfig.cruiseMaximumTicks(),
                        lich.getRandom().nextDouble()));
        clearAttackState(state);
        access(lich).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
        closeParryWindow(lich);
    }

    private static void beginSummon(
            Lich lich, LichCombatState state) {
        enterState(state, LichActionState.SUMMON_WINDUP,
                LichCombatConfig.summonWindupTicks());
        stopHorizontalMovement(lich);
        access(lich).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
    }

    private static void tickSummonWindup(
            Lich lich, LichCombatState state) {
        stopHorizontalMovement(lich);
        LivingEntity target = lich.getTarget();
        if (isRetainableTarget(lich, target)) {
            lich.getLookControl().setLookAt(
                    target, 30.0F, lich.getMaxHeadXRot());
        }
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        if (findLivingCustomClone(lich) == null) {
            spawnCustomClone(lich);
        }
        state.summonCooldown = Math.max(
                0, LichCombatConfig.summonCooldownTicks());
        beginCruise(lich, state);
    }

    private static void spawnCustomClone(Lich master) {
        if (!(master.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Lich clone = new Lich(serverLevel, master);
        double angle = master.getRandom().nextDouble() * Math.PI * 2.0;
        double distance = 3.0;
        clone.moveTo(
                master.getX() + Math.cos(angle) * distance,
                master.getY(),
                master.getZ() + Math.sin(angle) * distance,
                master.getYRot(), master.getXRot());
        clone.setMaster(master);
        clone.setShadowClone(true);
        clone.setInvulnerable(true);
        clone.setTarget(master.getTarget());
        LichCombatState cloneState = state(clone);
        cloneState.customClone = true;
        cloneState.masterId = master.getUUID();
        cloneState.cloneLifetimeTicks = 0;
        enterState(cloneState, LichActionState.IDLE, 0);
        if (serverLevel.addFreshEntity(clone)) {
            state(master).customCloneId = clone.getUUID();
        }
    }

    private static void beginShieldBash(
            Lich lich, LichCombatState state,
            @Nullable LivingEntity target) {
        enterState(state, LichActionState.SHIELD_BASH_WINDUP,
                LichCombatConfig.shieldBashWindupTicks());
        state.originX = lich.getX();
        state.originY = lich.getY();
        state.originZ = lich.getZ();
        lockDirection(lich, target, state);
        state.secondPhasePending = false;
        state.shieldBashEmpowered = lich.getShieldStrength() > 0;
        if (state.shieldBashEmpowered) {
            lich.setShieldStrength(lich.getShieldStrength() - 1);
            state.shieldLayers = lich.getShieldStrength();
        }
        state.shieldBashParried = false;
        state.parryingEntityId = null;
        stopHorizontalMovement(lich);
        setShieldBashTelegraph(lich, state);
    }

    private static void tickShieldBashWindup(
            Lich lich, LichCombatState state) {
        stopHorizontalMovement(lich);
        lich.getLookControl().setLookAt(
                state.originX + state.directionX,
                state.originY + lich.getEyeHeight(),
                state.originZ + state.directionZ,
                30.0F, lich.getMaxHeadXRot());
        setShieldBashTelegraph(lich, state);
        state.stateTicks++;
        if (state.stateTicks < state.stateDuration) {
            return;
        }
        boolean defended = performShieldBash(lich, state);
        access(lich).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
        closeParryWindow(lich);
        if (state.customClone) {
            clearMasterCloneReference(lich, state);
            lich.discard();
            return;
        }
        if (state.secondPhasePending) {
            enterSecondPhase(lich, state);
            return;
        }
        state.shieldBashCooldown = Math.max(
                0, LichCombatConfig.shieldBashCooldownTicks());
        if (defended) {
            beginStun(lich, state);
        } else if (state.secondPhase) {
            beginCruise(lich, state);
        } else {
            enterIdle(lich);
        }
    }

    private static boolean performShieldBash(
            Lich lich, LichCombatState state) {
        double length = Math.max(0.0, LichCombatConfig.shieldBashLength());
        double halfWidth = Math.max(
                0.0, LichCombatConfig.shieldBashHalfWidth());
        double height = Math.max(0.0, LichCombatConfig.shieldBashHeight());
        double endX = state.originX + state.directionX * length;
        double endZ = state.originZ + state.directionZ * length;
        AABB candidates = new AABB(
                Math.min(state.originX, endX) - halfWidth,
                state.originY,
                Math.min(state.originZ, endZ) - halfWidth,
                Math.max(state.originX, endX) + halfWidth,
                state.originY + height,
                Math.max(state.originZ, endZ) + halfWidth);
        DamageSource source = lich.damageSources().mobAttack(lich);
        float damage = (float) LichCombatRules.shieldBashDamage(
                attackDamage(lich),
                LichCombatConfig.shieldBashDamageMultiplier(),
                LichCombatConfig.shieldBashShieldMultiplier(),
                state.shieldBashEmpowered);
        boolean defended = state.shieldBashParried;
        for (LivingEntity target : combatTargets(lich, candidates)) {
            AABB targetBounds = target.getBoundingBox();
            if (!LichCombatRules.intersectsLockedCorridor(
                    target.getX(), targetBounds.minY, targetBounds.maxY,
                    target.getZ(), target.getBbWidth(),
                    state.originX, state.originY, state.originZ,
                    state.directionX, state.directionZ,
                    length, halfWidth, height)) {
                continue;
            }
            boolean parried = state.shieldBashParried
                    && (state.parryingEntityId == null
                    || target.getUUID().equals(state.parryingEntityId));
            boolean blocked = isShieldBlocking(target, source);
            LichCombatRules.ShieldBashHitDecision decision =
                    LichCombatRules.shieldBashHitDecision(blocked, parried);
            if (decision.dealsDamage()) {
                target.hurt(source, damage);
            }
            if (decision.appliesKnockback()) {
                applyShieldBashKnockback(target, state);
            }
            defended |= parried || blocked;
        }
        return defended;
    }

    private static boolean isShieldBlocking(
            LivingEntity target, DamageSource source) {
        return target instanceof ServerPlayer player
                && player.getUseItem().getItem() instanceof ShieldItem
                && player.isDamageSourceBlocked(source);
    }

    private static void applyShieldBashKnockback(
            LivingEntity target, LichCombatState state) {
        target.knockback(
                LichCombatConfig.shieldBashHorizontalKnockback(),
                -state.directionX, -state.directionZ);
        Vec3 movement = target.getDeltaMovement();
        target.setDeltaMovement(
                movement.x,
                Math.max(movement.y,
                        LichCombatConfig.shieldBashVerticalVelocity()),
                movement.z);
        target.hurtMarked = true;
    }

    private static void beginStun(
            Lich lich, LichCombatState state) {
        enterState(state, LichActionState.STUNNED,
                LichCombatConfig.shieldBashStunTicks());
        clearAttackState(state);
        stopHorizontalMovement(lich);
        access(lich).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
    }

    private static void tickStunned(
            Lich lich, LichCombatState state) {
        stopHorizontalMovement(lich);
        state.stateTicks++;
        if (state.stateTicks >= state.stateDuration) {
            if (state.secondPhase) {
                beginCruise(lich, state);
            } else {
                enterIdle(lich);
            }
        }
    }

    private static void decrementCooldowns(LichCombatState state) {
        if (state.shieldBashCooldown > 0) {
            state.shieldBashCooldown--;
        }
        if (state.summonCooldown > 0) {
            state.summonCooldown--;
        }
        if (state.meleeCooldown > 0) {
            state.meleeCooldown--;
        }
    }

    private static void lockDirection(
            Lich lich, @Nullable LivingEntity target,
            LichCombatState state) {
        Vec3 direction = target == null
                ? lich.getLookAngle()
                : target.position().subtract(lich.position());
        direction = direction.multiply(1.0, 0.0, 1.0);
        if (direction.lengthSqr() < 1.0E-8) {
            direction = lich.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        if (direction.lengthSqr() < 1.0E-8) {
            direction = new Vec3(0.0, 0.0, 1.0);
        }
        direction = direction.normalize();
        state.directionX = direction.x;
        state.directionZ = direction.z;
    }

    private static void setShieldBashTelegraph(
            Lich lich, LichCombatState state) {
        access(lich).bossRefactorTF$setPrimaryTelegraph(new AttackTelegraph(
                AttackTelegraphShape.CORRIDOR,
                (float) state.originX,
                (float) state.originY,
                (float) state.originZ,
                (float) state.directionX,
                (float) state.directionZ,
                (float) LichCombatConfig.shieldBashLength(),
                (float) LichCombatConfig.shieldBashHalfWidth(),
                0.0F,
                AttackTelegraph.windupProgress(
                        state.stateTicks, state.stateDuration)));
    }

    private static List<LivingEntity> combatTargets(
            Lich lich, AABB bounds) {
        return lich.level().getEntitiesOfClass(
                LivingEntity.class, bounds,
                target -> isCombatTarget(lich, target));
    }

    private static boolean isCombatTarget(
            Lich lich, LivingEntity target) {
        return target != lich
                && !(target instanceof Lich)
                && target.isAlive()
                && !target.isRemoved()
                && !lich.isAlliedTo(target)
                && (!(target instanceof Player player)
                || (!player.isCreative() && !player.isSpectator()));
    }

    private static boolean isRetainableTarget(
            Lich lich, @Nullable LivingEntity target) {
        return target != null && isCombatTarget(lich, target);
    }

    @Nullable
    private static Lich findLivingCustomClone(Lich master) {
        if (!(master.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        UUID cloneId = state(master).customCloneId;
        if (cloneId != null
                && serverLevel.getEntity(cloneId) instanceof Lich clone
                && isOwnedLivingCustomClone(master, clone)) {
            return clone;
        }
        List<Lich> nearby = master.level().getEntitiesOfClass(
                Lich.class,
                master.getBoundingBox().inflate(CLONE_SEARCH_RANGE),
                candidate -> isOwnedLivingCustomClone(master, candidate));
        if (nearby.isEmpty()) {
            state(master).customCloneId = null;
            return null;
        }
        Lich clone = nearby.get(0);
        state(master).customCloneId = clone.getUUID();
        return clone;
    }

    private static boolean isOwnedLivingCustomClone(
            Lich master, Lich candidate) {
        LichCombatState candidateState = state(candidate);
        return candidate != master
                && candidate.isAlive() && !candidate.isRemoved()
                && candidateState.customClone
                && master.getUUID().equals(candidateState.masterId);
    }

    @Nullable
    private static Lich findMaster(
            Lich clone, LichCombatState state) {
        Lich directMaster = clone.getMasterLich();
        if (directMaster != null) {
            state.masterId = directMaster.getUUID();
            return directMaster;
        }
        if (state.masterId != null
                && clone.level() instanceof ServerLevel serverLevel
                && serverLevel.getEntity(state.masterId) instanceof Lich master) {
            clone.setMaster(master);
            return master;
        }
        return null;
    }

    private static boolean isValidMaster(@Nullable Lich master) {
        return master != null && master.isAlive() && !master.isRemoved()
                && state(master).secondPhase;
    }

    private static void clearMasterCloneReference(
            Lich clone, LichCombatState cloneState) {
        Lich master = findMaster(clone, cloneState);
        if (master != null
                && clone.getUUID().equals(state(master).customCloneId)) {
            state(master).customCloneId = null;
        }
    }

    private static void clearAttackState(LichCombatState state) {
        state.shieldBashEmpowered = false;
        state.shieldBashParried = false;
        state.parryingEntityId = null;
    }

    private static void enterState(
            LichCombatState state, LichActionState actionState,
            int duration) {
        state.actionState = actionState;
        state.stateTicks = 0;
        state.stateDuration = Math.max(0, duration);
    }

    private static void enterIdle(Lich lich) {
        LichCombatState state = state(lich);
        enterState(state, LichActionState.IDLE, 0);
        clearAttackState(state);
        lich.getNavigation().stop();
        access(lich).bossRefactorTF$setPrimaryTelegraph(AttackTelegraph.NONE);
        closeParryWindow(lich);
    }

    private static void stopHorizontalMovement(Lich lich) {
        lich.getNavigation().stop();
        Vec3 movement = lich.getDeltaMovement();
        lich.setDeltaMovement(0.0, movement.y, 0.0);
    }

    private static void closeParryWindow(Lich lich) {
        var bridge = TwilightBossParryIntegration.bridge();
        if (bridge.isAvailable() && bridge.isParryable(lich)
                && !bridge.closeWindow(lich)) {
            BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to close the SenDimS parry window for Lich {}",
                    lich.getUUID());
        }
    }

    private static double attackDamage(Lich lich) {
        return lich.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private static DamageSource meleeMagicDamageSource(Lich lich) {
        var damageType = lich.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(MELEE_MAGIC_DAMAGE);
        return new DamageSource(damageType, lich, lich);
    }

    private static LichCombatState state(Lich lich) {
        return access(lich).bossRefactorTF$getLichCombatState();
    }

    private static LichCombatAccess access(Lich lich) {
        return (LichCombatAccess) lich;
    }
}