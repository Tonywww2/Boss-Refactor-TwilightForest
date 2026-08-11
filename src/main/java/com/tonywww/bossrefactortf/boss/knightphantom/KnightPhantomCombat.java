package com.tonywww.bossrefactortf.boss.knightphantom;

import com.tonywww.bossrefactortf.boss.TwilightBossParryIntegration;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.boss.KnightPhantom;
import twilightforest.entity.ai.goal.PhantomThrowWeaponGoal;
import twilightforest.init.TFDamageTypes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class KnightPhantomCombat {
    private static final Comparator<KnightPhantom> CHARGE_ORDER =
            Comparator.comparingInt(KnightPhantom::getNumber)
                    .thenComparing(KnightPhantom::getUUID);

    private KnightPhantomCombat() {
    }

    public static boolean customChargesEnabled() {
        BossRefactorTwilightForestConfig.KnightPhantomConfig config =
                BossRefactorTwilightForestConfig.KNIGHT_PHANTOM;
        return config.enabled.get() && config.chargeEnabled.get();
    }

    public static boolean replaceRandomChargeSelection(
            KnightPhantom initiator, List<KnightPhantom> knights) {
        if (!customChargesEnabled()) {
            return false;
        }

        List<KnightPhantom> livingKnights = encounterKnights(initiator);
        if (livingKnights.isEmpty()) {
            return true;
        }
        LivingEntity sharedTarget = findChargeTarget(livingKnights, initiator);
        if (!hasChargeRound(initiator, livingKnights)) {
            queueChargeRound(livingKnights, sharedTarget);
        }
        startNextQueuedKnight(initiator, livingKnights);
        return true;
    }

    public static void ensureChargeRoundProgress(KnightPhantom reference) {
        if (!customChargesEnabled()) {
            return;
        }
        List<KnightPhantom> livingKnights = encounterKnights(reference);
        reconcilePossessionOwner(reference, livingKnights);
        LivingEntity target = findChargeTarget(livingKnights, reference);
        for (KnightPhantom livingKnight : livingKnights) {
            if (livingKnight.getTarget() != target) {
                livingKnight.setTarget(target);
            }
        }
        if (!hasChargeRound(reference, livingKnights)) {
            if (target != null) {
                queueChargeRound(livingKnights, target);
            }
        }
        startNextQueuedKnight(reference, livingKnights);
    }

    public static boolean tickCustomFormation(KnightPhantom knight) {
        if (!customChargesEnabled()) {
            return false;
        }
        List<KnightPhantom> livingKnights = encounterKnights(knight);
        startNextQueuedKnight(knight, livingKnights);
        KnightPhantomCombatAccess access = access(knight);
        knight.stopUsingItem();
        knight.noPhysics = true;
        if (!hasChargeRound(knight, livingKnights)) {
            if (isAttackFormation(knight)) {
                switchFormation(knight, KnightPhantom.Formation.HOVER);
            }
            moveWaitingKnight(knight);
            return true;
        }
        if (!isPossessionOwner(knight)) {
            moveWaitingKnight(knight);
            return true;
        }
        if (!isRetainableTarget(knight, knight.getTarget())) {
            knight.setDeltaMovement(Vec3.ZERO);
            return true;
        }
        if (access.bossRefactorTF$isParryStunActive()) {
            knight.setDeltaMovement(Vec3.ZERO);
            return true;
        }

        knight.setTicksProgress(knight.getTicksProgress() + 1);
        if (knight.getTicksProgress() >= knight.getMaxTicksForFormation()) {
            advanceCustomChargeFormation(knight);
        }
        if (!isPossessionOwner(knight)) {
            moveWaitingKnight(knight);
            return true;
        }

        KnightPhantom.Formation formation = knight.getCurrentFormation();
        if (formation == KnightPhantom.Formation.ATTACK_PLAYER_START) {
            knight.setDeltaMovement(Vec3.ZERO);
            return true;
        }
        if (formation != KnightPhantom.Formation.ATTACK_PLAYER_ATTACK) {
            return true;
        }

        boolean postCharge = access.bossRefactorTF$isPostChargePossessionActive();
        Vec3 destination = postCharge
                ? postChargePossessionDestination(knight)
                : Vec3.atLowerCornerOf(knight.getChargePos());
        knight.getMoveControl().setWantedPosition(
                destination.x, destination.y, destination.z,
                postCharge ? 1.0 : 2.0);
        if (postCharge) {
            attackDuringPostChargePossession(knight);
        } else {
            useVanillaChargeSkill(knight);
        }
        return true;
    }

    public static boolean advanceCustomChargeFormation(KnightPhantom knight) {
        KnightPhantomCombatAccess access = access(knight);
        if (!customChargesEnabled()) {
            return false;
        }
        if (!isPossessionOwner(knight)
                && !isChargeRoundInProgress(knight)) {
            List<KnightPhantom> livingKnights = encounterKnights(knight);
            LivingEntity target = findChargeTarget(livingKnights, knight);
            if (target == null) {
                knight.setTicksProgress(0);
                return true;
            }
            queueChargeRound(livingKnights, target);
            startNextQueuedKnight(knight, livingKnights);
            return true;
        }
        if (access.bossRefactorTF$isParryStunActive()) {
            knight.setDeltaMovement(Vec3.ZERO);
            return true;
        }
        if (!isPossessionOwner(knight)) {
            switchFormation(knight,
                    KnightPhantom.Formation.WAITING_FOR_LEADER);
            return true;
        }
        if (access.bossRefactorTF$isPostChargePossessionActive()) {
            knight.setDeltaMovement(Vec3.ZERO);
            finishCurrentKnight(knight);
            switchFormation(knight,
                    KnightPhantom.Formation.WAITING_FOR_LEADER);
            List<KnightPhantom> livingKnights = encounterKnights(knight);
            if (!startNextQueuedKnight(knight, livingKnights)) {
                releaseCompletedRound(knight, livingKnights);
            }
            return true;
        }
        if (knight.getCurrentFormation()
                == KnightPhantom.Formation.ATTACK_PLAYER_START) {
            knight.setDeltaMovement(Vec3.ZERO);
            lockChargePosition(knight);
            access.bossRefactorTF$clearChargeSegmentHits();
            switchFormation(knight,
                    KnightPhantom.Formation.ATTACK_PLAYER_ATTACK);
            return true;
        }
        if (knight.getCurrentFormation()
                != KnightPhantom.Formation.ATTACK_PLAYER_ATTACK) {
            return false;
        }

        if (access.bossRefactorTF$consumeAdditionalChargeSegment()) {
            knight.setDeltaMovement(Vec3.ZERO);
            access.bossRefactorTF$clearChargeSegmentHits();
            switchFormation(knight,
                    KnightPhantom.Formation.ATTACK_PLAYER_START);
            return true;
        }

        knight.setDeltaMovement(Vec3.ZERO);
        access.bossRefactorTF$beginPostChargePossession();
        switchFormation(knight,
                KnightPhantom.Formation.ATTACK_PLAYER_ATTACK);
        return true;
    }

    public static void beginParryStun(KnightPhantom knight) {
        KnightPhantomCombatAccess access = access(knight);
        if (BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                .parryCancelsRemainingCharges.get()) {
            access.bossRefactorTF$cancelRemainingChargeSegments();
        }
        knight.setDeltaMovement(Vec3.ZERO);
        access.bossRefactorTF$beginParryStun(
                knight.level().getGameTime()
                        + BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                                .parryFeedbackTicks.get());
    }

    public static boolean updateParryStun(KnightPhantom knight) {
        KnightPhantomCombatAccess access = access(knight);
        if (!isPossessionOwner(knight)
                || !access.bossRefactorTF$isParryStunActive()) {
            return false;
        }
        knight.setDeltaMovement(Vec3.ZERO);
        if (!KnightPhantomCombatRules.chargeHandoffReady(
                knight.level().getGameTime(),
                access.bossRefactorTF$getParryStunEndGameTime())) {
            return true;
        }

        access.bossRefactorTF$finishParryStun();
        if (access.bossRefactorTF$consumeAdditionalChargeSegment()) {
            access.bossRefactorTF$clearChargeSegmentHits();
            switchFormation(knight,
                    KnightPhantom.Formation.ATTACK_PLAYER_START);
        } else {
            knight.setDeltaMovement(Vec3.ZERO);
            access.bossRefactorTF$beginPostChargePossession();
            switchFormation(knight,
                    KnightPhantom.Formation.ATTACK_PLAYER_ATTACK);
        }
        return true;
    }

    public static void finishDeadKnight(KnightPhantom knight) {
        KnightPhantomCombatAccess deadAccess = access(knight);
        List<KnightPhantom> livingKnights = encounterKnights(knight);
        UUID encounterId = encounterId(knight);
        KnightPhantomEncounterData encounterData = encounterData(knight);
        UUID registeredOwnerId = encounterData.owner(encounterId);
        if (registeredOwnerId == null
            && deadAccess.bossRefactorTF$isChargeRoundActive()) {
            encounterData.tryClaim(
                encounterId, knight.getUUID(), knight.level().getGameTime());
            registeredOwnerId = encounterData.owner(encounterId);
        }
        boolean possessedKnightDied = knight.getUUID().equals(registeredOwnerId);
        livingKnights.remove(knight);
        activateLastKnightIfNeeded(livingKnights);
        deadAccess.bossRefactorTF$finishChargeRound();
        long readyGameTime = knight.level().getGameTime();
        if (possessedKnightDied) {
            readyGameTime += BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                .deathHandoffDelayTicks.get();
            encounterData.releaseAfterDeath(
                encounterId, knight.getUUID(), readyGameTime);
        }
        boolean roundInProgress = hasChargeRound(knight, livingKnights);
        if (KnightPhantomCombatRules.shouldQueueChargeRoundAfterDeath(
                customChargesEnabled(), livingKnights.size(), roundInProgress)) {
            queueChargeRound(livingKnights, findChargeTarget(livingKnights, knight));
        }
        if (!customChargesEnabled()) {
            return;
        }
        if (possessedKnightDied) {
            for (KnightPhantom livingKnight : livingKnights) {
                access(livingKnight).bossRefactorTF$delayChargeUntil(
                        readyGameTime);
            }
        }
        if (!startNextQueuedKnight(knight, livingKnights)) {
            releaseCompletedRound(knight, livingKnights);
        }
    }

    public static int chargeWindupTicks(KnightPhantom knight) {
        BossRefactorTwilightForestConfig.KnightPhantomConfig config =
                BossRefactorTwilightForestConfig.KNIGHT_PHANTOM;
        return KnightPhantomCombatRules.chargeWindupTicks(
            config.lastKnightEnabled.get()
                && access(knight).bossRefactorTF$isLastKnightPhaseActive(),
                config.chargeWindupTicks.get(),
                config.lastKnightChargeWindupTicks.get());
    }

    public static double adjustedMoveSpeed(KnightPhantom knight, double original) {
        if (!BossRefactorTwilightForestConfig.KNIGHT_PHANTOM.enabled.get()) {
            return original;
        }
        KnightPhantomCombatAccess access = access(knight);
        double baseSpeed = customChargesEnabled()
            && isPossessionOwner(knight)
                && !access.bossRefactorTF$isPostChargePossessionActive()
                && knight.isChargingAtPlayer()
                ? BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .chargeSpeedMultiplier.get()
                : original;
        if (!BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                .fallenKnightScalingEnabled.get()) {
            return baseSpeed;
        }
        return baseSpeed * KnightPhantomCombatRules.fallenKnightMultiplier(
                access.bossRefactorTF$getFallenKnightCount(),
                BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .speedBonusPerFallenKnight.get());
    }

    public static int livingKnightCount(KnightPhantom reference) {
        return encounterKnights(reference).size();
    }

    public static void stopCustomCombat(KnightPhantom reference) {
        if (!(reference.level() instanceof ServerLevel)) {
            return;
        }
        List<KnightPhantom> livingKnights = encounterKnights(reference);
        UUID encounterId = access(reference).bossRefactorTF$getEncounterId();
        boolean controlled = encounterId != null
                && encounterData(reference).owner(encounterId) != null;
        if (!controlled) {
            controlled = livingKnights.stream()
                    .map(KnightPhantomCombat::access)
                    .anyMatch(access ->
                            access.bossRefactorTF$isChargeRoundActive()
                                    || access.bossRefactorTF$isQueuedForChargeRound());
        }
        if (!controlled) {
            return;
        }
        if (encounterId != null) {
            encounterData(reference).clear(encounterId);
        }
        for (KnightPhantom knight : livingKnights) {
            access(knight).bossRefactorTF$finishChargeRound();
            if (isAttackFormation(knight)
                    || knight.getCurrentFormation()
                    == KnightPhantom.Formation.WAITING_FOR_LEADER) {
                switchFormation(knight, KnightPhantom.Formation.HOVER);
            }
        }
    }

    public static boolean isPossessionOwner(KnightPhantom knight) {
        KnightPhantomCombatAccess access = access(knight);
        if (!access.bossRefactorTF$isChargeRoundActive()) {
            return false;
        }
        if (!(knight.level() instanceof ServerLevel)) {
            return true;
        }
        UUID encounterId = access.bossRefactorTF$getEncounterId();
        return encounterId != null
                && knight.getUUID().equals(
                        encounterData(knight).owner(encounterId));
    }

    public static boolean hasRetainableChargeTarget(KnightPhantom knight) {
        return isRetainableTarget(knight, knight.getTarget());
    }

    public static boolean interceptsCurrentMeleeAttack(KnightPhantom knight) {
        KnightPhantomCombatAccess access = access(knight);
        if (!customChargesEnabled()
            || !isPossessionOwner(knight)
            || access.bossRefactorTF$isPostChargePossessionActive()) {
            return false;
        }
        KnightPhantom.Formation formation = knight.getCurrentFormation();
        return formation == KnightPhantom.Formation.ATTACK_PLAYER_START
                || formation == KnightPhantom.Formation.ATTACK_PLAYER_ATTACK;
    }

    public static void attackChargePath(KnightPhantom knight) {
        KnightPhantomCombatAccess access = access(knight);
        if (!customChargesEnabled()
            || !isPossessionOwner(knight)
                || !hasRetainableChargeTarget(knight)
            || access.bossRefactorTF$isParryStunActive()
                || access.bossRefactorTF$isPostChargePossessionActive()
                || knight.getCurrentFormation()
                != KnightPhantom.Formation.ATTACK_PLAYER_ATTACK) {
            return;
        }

        AABB sweptBox = chargeHitBox(knight);
        for (LivingEntity target : knight.level().getEntitiesOfClass(
                LivingEntity.class, sweptBox,
                candidate -> isValidChargeTarget(knight, candidate))) {
            attackChargeTarget(knight, target);
        }
    }

    public static void finishChargeAtDestination(KnightPhantom knight) {
        KnightPhantomCombatAccess access = access(knight);
        if (!customChargesEnabled()
            || !isPossessionOwner(knight)
                || !hasRetainableChargeTarget(knight)
            || access.bossRefactorTF$isParryStunActive()
                || access.bossRefactorTF$isPostChargePossessionActive()
                || knight.getCurrentFormation()
                != KnightPhantom.Formation.ATTACK_PLAYER_ATTACK) {
            return;
        }
        Vec3 destination = Vec3.atLowerCornerOf(knight.getChargePos());
        double completionRadius = Math.max(0.5, knight.getBbWidth() * 0.5);
        if (KnightPhantomCombatRules.movementSegmentReaches(
                knight.xOld, knight.yOld, knight.zOld,
                knight.getX(), knight.getY(), knight.getZ(),
                destination.x, destination.y, destination.z,
                completionRadius)) {
            knight.setPos(destination.x, destination.y, destination.z);
            knight.setDeltaMovement(Vec3.ZERO);
            advanceCustomChargeFormation(knight);
        }
    }

    public static void refreshChargeDestination(KnightPhantom knight) {
        KnightPhantomCombatAccess access = access(knight);
        if (customChargesEnabled()
            && isPossessionOwner(knight)
                && hasRetainableChargeTarget(knight)
                && !access.bossRefactorTF$isParryStunActive()
                && knight.getCurrentFormation()
                == KnightPhantom.Formation.ATTACK_PLAYER_START) {
            lockChargePosition(knight);
        }
    }

    public static Vec3 postChargePossessionDestination(KnightPhantom knight) {
        LivingEntity target = knight.getTarget();
        if (target != null && target.isAlive()) {
            return target.position();
        }
        Vec3 direction = knight.getLookAngle();
        if (direction.lengthSqr() < 1.0E-8) {
            direction = new Vec3(0.0, 0.0, 1.0);
        }
        return knight.position().add(direction.normalize());
    }

    public static boolean shouldHoldPostChargePossession(
            KnightPhantom knight) {
        KnightPhantomCombatAccess access = access(knight);
        LivingEntity target = knight.getTarget();
        return customChargesEnabled()
            && isPossessionOwner(knight)
                && access.bossRefactorTF$isPostChargePossessionActive()
                && target != null && target.isAlive()
                && knight.distanceToSqr(target) < 1.75 * 1.75;
    }

    public static Vec3 avoidPlayerWithinArena(
            KnightPhantom knight, Vec3 originalDestination) {
        BossRefactorTwilightForestConfig.KnightPhantomConfig config =
                BossRefactorTwilightForestConfig.KNIGHT_PHANTOM;
        if (!config.enabled.get() || !config.playerAvoidanceEnabled.get()
                || knight.isChargingAtPlayer()
                || !(knight.getTarget() instanceof Player player)
                || !player.isAlive()) {
            return originalDestination;
        }

        double avoidanceDistance = config.playerAvoidanceDistance.get();
        Vec3 horizontalOffset = new Vec3(
                knight.getX() - player.getX(), 0.0,
                knight.getZ() - player.getZ());
        if (horizontalOffset.lengthSqr()
                >= avoidanceDistance * avoidanceDistance) {
            return originalDestination;
        }

        GlobalPos restrictionPoint = knight.getRestrictionPoint();
        if (restrictionPoint == null
                || restrictionPoint.dimension() != knight.level().dimension()) {
            return originalDestination;
        }
        Vec3 arenaCenter = Vec3.atCenterOf(restrictionPoint.pos());
        double maximumRadius = arenaRadius(knight);
        KnightPhantomCombatRules.HorizontalPoint desired =
                KnightPhantomCombatRules.avoidanceDestination(
                        knight.getX(), knight.getZ(),
                        player.getX(), player.getZ(),
                        originalDestination.x, originalDestination.z,
                        arenaCenter.x, arenaCenter.z,
                        avoidanceDistance, maximumRadius,
                        knight.getNumber());
        return new Vec3(desired.x(), originalDestination.y, desired.z());
    }

    public static void confineKnightToArena(KnightPhantom knight) {
        if (!BossRefactorTwilightForestConfig.KNIGHT_PHANTOM.enabled.get()) {
            return;
        }
        GlobalPos restrictionPoint = knight.getRestrictionPoint();
        if (restrictionPoint == null
                || !restrictionPoint.dimension().equals(knight.level().dimension())) {
            return;
        }

        Vec3 arenaCenter = Vec3.atCenterOf(restrictionPoint.pos());
        double maximumRadius = arenaRadius(knight);
        KnightPhantomCombatRules.HorizontalPoint confined =
            KnightPhantomCombatRules.clampToArena(
                knight.getX(), knight.getZ(),
                arenaCenter.x, arenaCenter.z, maximumRadius);
        if (confined.x() == knight.getX() && confined.z() == knight.getZ()) {
            return;
        }

        Vec3 outwardDirection = knight.position().subtract(arenaCenter)
            .multiply(1.0, 0.0, 1.0).normalize();
        knight.setPos(confined.x(), knight.getY(), confined.z());
        Vec3 movement = knight.getDeltaMovement();
        double outwardSpeed = movement.dot(outwardDirection);
        if (outwardSpeed > 0.0) {
            knight.setDeltaMovement(movement.subtract(
                    outwardDirection.scale(outwardSpeed)));
        }
    }

    public static boolean attackChargeTarget(KnightPhantom knight, Entity entity) {
        if (!(entity instanceof LivingEntity target)
                || !isPossessionOwner(knight)
                || !hasRetainableChargeTarget(knight)
                || knight.getCurrentFormation()
                != KnightPhantom.Formation.ATTACK_PLAYER_ATTACK
                || !isValidChargeTarget(knight, target)
                || !chargePathIntersects(knight, target.getBoundingBox())) {
            return false;
        }
        KnightPhantomCombatAccess access = access(knight);
        if (access.bossRefactorTF$isParryStunActive()) {
            return false;
        }
        if (!access.bossRefactorTF$markChargeSegmentHit(target.getUUID())) {
            return false;
        }

        float damage = KnightPhantomCombatRules.chargeDamage(
                knight.getAttributeValue(Attributes.ATTACK_DAMAGE),
                BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .chargeDamageMultiplier.get());
        var source = TFDamageTypes.getEntityDamageSource(
                knight.level(), TFDamageTypes.HAUNT, knight);
        boolean shieldBlocked = target instanceof ServerPlayer player
                && player.isDamageSourceBlocked(source)
                && player.getUseItem().getItem() instanceof ShieldItem;
        boolean hurt = target.hurt(source, damage);
        if (shieldBlocked
                && BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .shieldCooldownEnabled.get()) {
            ServerPlayer player = (ServerPlayer) target;
            ItemStack shield = player.getUseItem();
            int cooldownTicks = BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                    .shieldCooldownTicks.get();
            if (!shield.isEmpty() && cooldownTicks > 0) {
                player.getCooldowns().addCooldown(shield.getItem(), cooldownTicks);
            }
            player.stopUsingItem();
        }
        return hurt || shieldBlocked;
    }

    public static void closeParryWindow(KnightPhantom knight) {
        var bridge = TwilightBossParryIntegration.bridge();
        if (bridge.isAvailable() && bridge.isParryable(knight)
                && !bridge.closeWindow(knight)) {
            com.tonywww.bossrefactortf.BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to close the SenDimS parry window for Knight Phantom {}",
                    knight.getUUID());
        }
    }

    public static void synchronizeParryWindow(KnightPhantom knight) {
        if (!(knight instanceof com.tonywww.bossrefactortf.boss.TwilightBossParryAccess access)) {
            return;
        }
        var bridge = TwilightBossParryIntegration.bridge();
        if (!bridge.isAvailable()) {
            return;
        }
        boolean shouldBeOpen = access.bossRefactorTF$isCurrentAttackParryable();
        if (!shouldBeOpen) {
            closeParryWindow(knight);
            return;
        }
        if (!bridge.isParryable(knight) && !bridge.isParried(knight)
                && !bridge.openWindow(knight)) {
            com.tonywww.bossrefactortf.BossRefactorTwilightForest.LOGGER.warn(
                    "Unable to open the SenDimS parry window for Knight Phantom {}",
                    knight.getUUID());
        }
    }

    private static boolean startNextQueuedKnight(
            KnightPhantom reference, List<KnightPhantom> livingKnights) {
        UUID ownerId = reconcilePossessionOwner(reference, livingKnights);
        if (ownerId != null) {
            return true;
        }
        UUID encounterId = encounterId(reference);
        KnightPhantomEncounterData encounterData = encounterData(reference);
        for (KnightPhantom knight : livingKnights) {
            KnightPhantomCombatAccess access = access(knight);
            if (!access.bossRefactorTF$isQueuedForChargeRound()) {
                continue;
            }
            if (!KnightPhantomCombatRules.chargeHandoffReady(
                    knight.level().getGameTime(),
                    access.bossRefactorTF$getChargeReadyGameTime())) {
                return true;
            }
            if (!encounterData.tryClaim(
                    encounterId, knight.getUUID(),
                    knight.level().getGameTime())) {
                return true;
            }
            lockChargePosition(knight);
            knight.setDeltaMovement(Vec3.ZERO);
                access.bossRefactorTF$beginChargeRound(
                    chargeSegmentCount(knight));
            switchFormation(knight,
                    KnightPhantom.Formation.ATTACK_PLAYER_START);
            return true;
        }
        return false;
    }

            private static UUID reconcilePossessionOwner(
                KnightPhantom reference, List<KnightPhantom> livingKnights) {
            UUID encounterId = encounterId(reference);
            KnightPhantomEncounterData encounterData = encounterData(reference);
            UUID ownerId = encounterData.owner(encounterId);
            if (ownerId == null) {
                for (KnightPhantom knight : livingKnights) {
                KnightPhantomCombatAccess access = access(knight);
                if (!access.bossRefactorTF$isChargeRoundActive()) {
                    continue;
                }
                if (encounterData.tryClaim(
                    encounterId, knight.getUUID(),
                    knight.level().getGameTime())) {
                    ownerId = knight.getUUID();
                    break;
                }
                }
            }

            for (KnightPhantom knight : livingKnights) {
                KnightPhantomCombatAccess access = access(knight);
                if (knight.getUUID().equals(ownerId)) {
                    access.bossRefactorTF$setPossessionVisible(true);
                if (!access.bossRefactorTF$isChargeRoundActive()) {
                    access.bossRefactorTF$beginChargeRound(
                        chargeSegmentCount(knight));
                    switchFormation(knight,
                        KnightPhantom.Formation.ATTACK_PLAYER_START);
                }
                continue;
                }
                if (access.bossRefactorTF$isChargeRoundActive()
                    || isAttackFormation(knight)) {
                clearDuplicatePossession(knight, true);
                }
            }
            return ownerId;
            }

            private static int chargeSegmentCount(KnightPhantom knight) {
            KnightPhantomCombatAccess access = access(knight);
            return access.bossRefactorTF$isLastKnightPhaseActive()
                && BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                    .lastKnightEnabled.get()
                && BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                    .lastKnightMultiChargeEnabled.get()
                ? BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                    .lastKnightChargeCount.get()
                : 1;
            }

    private static void clearDuplicatePossession(
            KnightPhantom knight, boolean keepQueued) {
        KnightPhantomCombatAccess access = access(knight);
        long readyGameTime = access.bossRefactorTF$getChargeReadyGameTime();
        knight.setDeltaMovement(Vec3.ZERO);
        access.bossRefactorTF$finishChargeRound();
        if (keepQueued) {
            access.bossRefactorTF$setQueuedForChargeRound(true);
            access.bossRefactorTF$delayChargeUntil(readyGameTime);
        }
        switchFormation(knight,
                KnightPhantom.Formation.WAITING_FOR_LEADER);
    }

    private static boolean isAttackFormation(KnightPhantom knight) {
        KnightPhantom.Formation formation = knight.getCurrentFormation();
        return formation == KnightPhantom.Formation.ATTACK_PLAYER_START
                || formation == KnightPhantom.Formation.ATTACK_PLAYER_ATTACK;
    }

    private static void finishCurrentKnight(KnightPhantom knight) {
        access(knight).bossRefactorTF$finishChargeRound();
        UUID encounterId = access(knight).bossRefactorTF$getEncounterId();
        if (encounterId != null && knight.level() instanceof ServerLevel) {
            encounterData(knight).releaseCompleted(
                    encounterId, knight.getUUID(),
                    knight.level().getGameTime());
        }
    }

    private static void lockChargePosition(KnightPhantom knight) {
        LivingEntity target = knight.getTarget();
        if (target != null && target.isAlive()) {
            Vec3 destination = chargeDestination(knight, target);
            knight.setChargePos(BlockPos.containing(
                destination.x, destination.y, destination.z));
        }
    }

    private static Vec3 chargeDestination(
            KnightPhantom knight, LivingEntity target) {
        Vec3 direction = target.position().subtract(knight.position())
                .multiply(1.0, 0.0, 1.0);
        if (direction.lengthSqr() < 1.0E-8) {
            direction = knight.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        double maximumDistance = BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                .chargeMaximumDistance.get();
        GlobalPos restrictionPoint = knight.getRestrictionPoint();
        if (restrictionPoint == null
                || !restrictionPoint.dimension().equals(knight.level().dimension())) {
            KnightPhantomCombatRules.HorizontalPoint destination =
                    KnightPhantomCombatRules.straightChargeDestination(
                            knight.getX(), knight.getZ(),
                            direction.x, direction.z, maximumDistance,
                            knight.getX(), knight.getZ(), maximumDistance);
            return new Vec3(destination.x(), target.getY(), destination.z());
        }

        Vec3 arenaCenter = Vec3.atCenterOf(restrictionPoint.pos());
        double maximumRadius = arenaRadius(knight);
        KnightPhantomCombatRules.HorizontalPoint destination =
            KnightPhantomCombatRules.straightChargeDestination(
                knight.getX(), knight.getZ(),
                direction.x, direction.z,
                maximumDistance,
                arenaCenter.x, arenaCenter.z, maximumRadius);
        return new Vec3(destination.x(), target.getY(), destination.z());
    }

    private static double arenaRadius(KnightPhantom knight) {
        return Math.max(0.0, Math.min(
                KnightPhantomCombatRules.ARENA_RADIUS,
                knight.getHomeRadius()) - knight.getBbWidth() * 0.5);
    }

    private static LivingEntity findChargeTarget(
            List<KnightPhantom> livingKnights, KnightPhantom initiator) {
        LivingEntity target = initiator.getTarget();
        if (isRetainableTarget(initiator, target)) {
            return target;
        }
        for (KnightPhantom knight : livingKnights) {
            target = knight.getTarget();
            if (isRetainableTarget(knight, target)) {
                return target;
            }
        }
        target = nearestAttackablePlayer(initiator);
        if (target != null) {
            return target;
        }
        for (KnightPhantom knight : livingKnights) {
            if (knight != initiator) {
                target = nearestAttackablePlayer(knight);
                if (target != null) {
                    return target;
                }
            }
        }
        return null;
    }

    private static boolean isRetainableTarget(
            KnightPhantom knight, LivingEntity target) {
        return target instanceof Player
                && TargetingConditions.forCombat()
                        .range(knight.getAttributeValue(Attributes.FOLLOW_RANGE))
                        .ignoreLineOfSight()
                        .test(knight, target);
    }

    private static Player nearestAttackablePlayer(KnightPhantom knight) {
        return knight.level().getNearestPlayer(
                TargetingConditions.forCombat().range(
                        knight.getAttributeValue(Attributes.FOLLOW_RANGE)),
                knight);
    }

    private static AABB chargeHitBox(KnightPhantom knight) {
        Vec3 currentPosition = knight.position();
        Vec3 previousPosition = new Vec3(knight.xOld, knight.yOld, knight.zOld);
        AABB currentBox = knight.getBoundingBox();
        AABB previousBox = currentBox.move(previousPosition.subtract(currentPosition));
        return currentBox.minmax(previousBox).inflate(
                BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .chargeHitboxInflation.get());
    }

    private static boolean chargePathIntersects(
            KnightPhantom knight, AABB targetBox) {
        AABB currentBox = knight.getBoundingBox();
        Vec3 currentCenter = currentBox.getCenter();
        Vec3 previousCenter = currentCenter.add(
            knight.xOld - knight.getX(),
            knight.yOld - knight.getY(),
            knight.zOld - knight.getZ());
        double inflation = BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                .chargeHitboxInflation.get();
        double halfWidth = (currentBox.maxX - currentBox.minX) * 0.5 + inflation;
        double halfHeight = (currentBox.maxY - currentBox.minY) * 0.5 + inflation;
        AABB expandedTarget = targetBox.inflate(
                halfWidth, halfHeight, halfWidth);
        return expandedTarget.contains(previousCenter)
                || expandedTarget.contains(currentCenter)
                || expandedTarget.clip(previousCenter, currentCenter).isPresent();
    }

    private static boolean isChargeRoundInProgress(KnightPhantom reference) {
        return hasChargeRound(reference, encounterKnights(reference));
    }

    private static boolean hasChargeRound(
            KnightPhantom reference, List<KnightPhantom> livingKnights) {
        UUID encounterId = access(reference).bossRefactorTF$getEncounterId();
        return (encounterId != null
            && encounterData(reference).owner(encounterId) != null)
                || livingKnights.stream()
                .map(KnightPhantomCombat::access)
                .anyMatch(KnightPhantomCombatAccess::bossRefactorTF$isQueuedForChargeRound);
    }

    private static void queueChargeRound(
            List<KnightPhantom> livingKnights, LivingEntity sharedTarget) {
        for (KnightPhantom knight : livingKnights) {
            if (sharedTarget != null) {
                knight.setTarget(sharedTarget);
            }
            access(knight).bossRefactorTF$setQueuedForChargeRound(true);
            switchFormation(knight,
                    KnightPhantom.Formation.WAITING_FOR_LEADER);
        }
    }

    private static void releaseCompletedRound(
            KnightPhantom reference, List<KnightPhantom> livingKnights) {
        for (KnightPhantom knight : livingKnights) {
            access(knight).bossRefactorTF$finishChargeRound();
            if (knight.getCurrentFormation()
                    == KnightPhantom.Formation.WAITING_FOR_LEADER) {
                switchFormation(knight, KnightPhantom.Formation.HOVER);
            }
        }
        UUID encounterId = access(reference).bossRefactorTF$getEncounterId();
        if (encounterId != null
                && encounterData(reference).owner(encounterId) == null) {
            encounterData(reference).clear(encounterId);
        }
    }

    private static void moveWaitingKnight(KnightPhantom knight) {
        Vec3 destination = avoidPlayerWithinArena(knight, knight.position());
        if (!KnightPhantomCombatRules.shouldMoveToDestination(
                knight.getX(), knight.getY(), knight.getZ(),
                destination.x, destination.y, destination.z)) {
            knight.setDeltaMovement(Vec3.ZERO);
            return;
        }
        knight.getMoveControl().setWantedPosition(
                destination.x, destination.y, destination.z, 1.0);
    }

    private static void attackDuringPostChargePossession(
            KnightPhantom knight) {
        LivingEntity target = knight.getTarget();
        if (target == null || !target.isAlive()
                || knight.getTicksProgress() % 20 != 0
                || knight.distanceTo(target) >= 2.0F
                || !knight.getSensing().hasLineOfSight(target)) {
            return;
        }
        knight.lookAt(target, 10.0F, 500.0F);
        knight.doHurtTarget(target);
    }

    private static void useVanillaChargeSkill(KnightPhantom knight) {
        LivingEntity target = knight.getTarget();
        if (!isPossessionOwner(knight)
                || target == null || !target.isAlive()
                || knight.getTicksProgress() % 4 != 0) {
            return;
        }
        PhantomThrowWeaponGoal goal = new PhantomThrowWeaponGoal(knight);
        PhantomThrowWeaponGoalAccess access =
                (PhantomThrowWeaponGoalAccess) goal;
        if (knight.isAxeKnight()) {
            access.bossRefactorTF$launchAxeAt(target);
        } else if (knight.isPickKnight()) {
            access.bossRefactorTF$launchPicks();
        }
    }

    private static void activateLastKnightIfNeeded(
            List<KnightPhantom> livingKnights) {
        if (livingKnights.size() != 1
                || !BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .lastKnightEnabled.get()) {
            return;
        }
        access(livingKnights.get(0)).bossRefactorTF$activateLastKnightPhase();
    }

    private static boolean isValidChargeTarget(
            KnightPhantom knight, LivingEntity target) {
        if (target == knight || target instanceof KnightPhantom
                || !target.isAlive() || target.isSpectator()
                || knight.isAlliedTo(target)) {
            return false;
        }
        return target instanceof Player
                || BossRefactorTwilightForestConfig.KNIGHT_PHANTOM
                        .chargeHitsNonPlayers.get();
    }

    private static List<KnightPhantom> orderedLivingKnights(
            List<KnightPhantom> candidates, KnightPhantom required) {
        List<KnightPhantom> livingKnights = new ArrayList<>();
        for (KnightPhantom candidate : candidates) {
            if (candidate.isAlive() && !candidate.isRemoved()
                    && candidate instanceof KnightPhantomCombatAccess) {
                livingKnights.add(candidate);
            }
        }
        if (required != null && required.isAlive() && !required.isRemoved()
                && required instanceof KnightPhantomCombatAccess
                && !livingKnights.contains(required)) {
            livingKnights.add(required);
        }
        livingKnights.sort(CHARGE_ORDER);
        return livingKnights;
    }

    private static List<KnightPhantom> encounterKnights(
            KnightPhantom reference) {
        KnightPhantomCombatAccess referenceAccess = access(reference);
        UUID encounterId = referenceAccess.bossRefactorTF$getEncounterId();
        GlobalPos home = reference.getRestrictionPoint();
        if (home != null) {
            UUID homeEncounterId = homeEncounterId(home);
            if (!homeEncounterId.equals(encounterId)) {
                referenceAccess.bossRefactorTF$setEncounterId(homeEncounterId);
            }
            encounterId = homeEncounterId;
        } else if (encounterId == null) {
            List<KnightPhantom> nearbyKnights = orderedLivingKnights(
                    reference.getNearbyKnights(),
                    reference.isAlive() && !reference.isRemoved()
                            ? reference : null);
            nearbyKnights.removeIf(candidate ->
                    candidate.getRestrictionPoint() != null);
            encounterId = nearbyKnights.stream()
                    .map(KnightPhantomCombat::access)
                    .map(KnightPhantomCombatAccess::bossRefactorTF$getEncounterId)
                    .filter(java.util.Objects::nonNull)
                    .min(UUID::compareTo)
                    .orElseGet(UUID::randomUUID);
            referenceAccess.bossRefactorTF$setEncounterId(encounterId);
            for (KnightPhantom nearbyKnight : nearbyKnights) {
                KnightPhantomCombatAccess nearbyAccess = access(nearbyKnight);
                if (nearbyAccess.bossRefactorTF$getEncounterId() == null) {
                    nearbyAccess.bossRefactorTF$setEncounterId(encounterId);
                }
            }
        }

        if (!(reference.level() instanceof ServerLevel serverLevel)) {
            List<KnightPhantom> nearbyKnights = orderedLivingKnights(
                    reference.getNearbyKnights(), reference);
            if (home != null) {
            nearbyKnights.removeIf(candidate ->
                !home.equals(candidate.getRestrictionPoint()));
            }
            return nearbyKnights;
        }
        UUID requiredEncounterId = encounterId;
        GlobalPos requiredHome = home;
        List<KnightPhantom> livingKnights = new ArrayList<>(
                serverLevel.getEntities(
                        EntityTypeTest.forClass(KnightPhantom.class),
                        knight -> knight.isAlive() && !knight.isRemoved()
                                && knight instanceof KnightPhantomCombatAccess
                                        knightAccess
                    && (requiredHome != null
                        ? requiredHome.equals(
                            knight.getRestrictionPoint())
                        : requiredEncounterId.equals(
                            knightAccess
                                .bossRefactorTF$getEncounterId()))));
        if (requiredHome != null) {
            for (KnightPhantom livingKnight : livingKnights) {
            access(livingKnight).bossRefactorTF$setEncounterId(
                requiredEncounterId);
            }
        }
        livingKnights.sort(CHARGE_ORDER);
        return livingKnights;
    }

    private static UUID homeEncounterId(GlobalPos home) {
        String key = home.dimension().location() + ":" + home.pos().asLong();
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    private static UUID encounterId(KnightPhantom reference) {
        UUID encounterId = access(reference).bossRefactorTF$getEncounterId();
        if (encounterId == null) {
            throw new IllegalStateException(
                    "Knight Phantom encounter must be initialized before combat");
        }
        return encounterId;
    }

    private static KnightPhantomEncounterData encounterData(
            KnightPhantom reference) {
        return KnightPhantomEncounterData.get(
                (ServerLevel) reference.level());
    }

    private static KnightPhantomCombatAccess access(KnightPhantom knight) {
        return (KnightPhantomCombatAccess) knight;
    }

    private static void switchFormation(
            KnightPhantom knight, KnightPhantom.Formation formation) {
        access(knight).bossRefactorTF$switchToFormation(formation);
    }
}