package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.TwilightBossParryAccess;
import com.tonywww.bossrefactortf.boss.hydra.HydraCombat;
import com.tonywww.bossrefactortf.boss.hydra.HydraCombatAccess;
import com.tonywww.bossrefactortf.boss.hydra.HydraCombatState;
import com.tonywww.bossrefactortf.boss.hydra.HydraSkill;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphAccess;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphNbt;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.boss.Hydra;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = Hydra.class, remap = false)
public abstract class HydraMixin extends Mob
        implements HydraCombatAccess, AttackTelegraphAccess,
        TwilightBossParryAccess {
    @Unique
    private static final EntityDataAccessor<Integer>
            BOSS_REFACTOR_TF$HYDRA_SKILL = SynchedEntityData.defineId(
                    Hydra.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer>
            BOSS_REFACTOR_TF$HYDRA_HEAD = SynchedEntityData.defineId(
                    Hydra.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Float>
            BOSS_REFACTOR_TF$HYDRA_PROGRESS = SynchedEntityData.defineId(
                    Hydra.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Boolean>
            BOSS_REFACTOR_TF$HYDRA_RECOVERING = SynchedEntityData.defineId(
                    Hydra.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Float>
            BOSS_REFACTOR_TF$HYDRA_RECOVERY_START =
                    SynchedEntityData.defineId(
                            Hydra.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<CompoundTag>
            BOSS_REFACTOR_TF$HYDRA_GEOMETRY = SynchedEntityData.defineId(
                    Hydra.class, EntityDataSerializers.COMPOUND_TAG);
    @Unique
    private static final EntityDataAccessor<CompoundTag>
            BOSS_REFACTOR_TF$HYDRA_TELEGRAPHS = SynchedEntityData.defineId(
                    Hydra.class, EntityDataSerializers.COMPOUND_TAG);

    @Unique
    private final HydraCombatState bossRefactorTF$hydraCombatState =
            new HydraCombatState();

    protected HydraMixin(
            EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "registerAttributes",
            at = @At("RETURN"),
            remap = false)
    private static void bossRefactorTF$addAttackDamageAttribute(
            CallbackInfoReturnable<AttributeSupplier.Builder> callback) {
        callback.getReturnValue().add(
                Attributes.ATTACK_DAMAGE,
                BossRefactorTwilightForestConfig.HYDRA
                        .baseAttackDamage.getDefault());
    }

    @Inject(
            method = {"defineSynchedData", "m_8097_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$defineHydraCombatData(CallbackInfo callback) {
        getEntityData().define(
                BOSS_REFACTOR_TF$HYDRA_SKILL, HydraSkill.NONE.ordinal());
        getEntityData().define(BOSS_REFACTOR_TF$HYDRA_HEAD, -1);
        getEntityData().define(BOSS_REFACTOR_TF$HYDRA_PROGRESS, 0.0F);
        getEntityData().define(BOSS_REFACTOR_TF$HYDRA_RECOVERING, false);
        getEntityData().define(BOSS_REFACTOR_TF$HYDRA_RECOVERY_START, 0.0F);
        getEntityData().define(
                BOSS_REFACTOR_TF$HYDRA_GEOMETRY,
                bossRefactorTF$encodeGeometry(
                        getX(), getY(), getZ(), 0.0, 1.0));
        getEntityData().define(
                BOSS_REFACTOR_TF$HYDRA_TELEGRAPHS,
                bossRefactorTF$encodeTelegraphs(
                        AttackTelegraph.NONE, List.of()));
    }

    @Inject(
            method = {"aiStep", "m_8107_"},
            at = @At("HEAD"),
            remap = false)
    private void bossRefactorTF$clearVanillaHeadActions(
            CallbackInfo callback) {
        HydraCombat.suppressVanillaHeadActions(bossRefactorTF$self());
    }

    @Inject(
            method = {"attackEntity"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$disableVanillaPrimaryAttack(
            Entity target, float distance, CallbackInfo callback) {
        if (HydraCombat.customCombatEnabled()) {
            callback.cancel();
        }
    }

    @Inject(
            method = {"secondaryAttacks"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$disableVanillaSecondaryAttacks(
            CallbackInfo callback) {
        if (HydraCombat.customCombatEnabled()) {
            callback.cancel();
        }
    }

    @Inject(
            method = {"customServerAiStep", "m_8024_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$tickHydraCombat(CallbackInfo callback) {
        HydraCombat.tick(bossRefactorTF$self());
    }

    @Inject(
            method = {"addAdditionalSaveData", "m_7380_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$saveHydraCombat(
            CompoundTag tag, CallbackInfo callback) {
        tag.put(
                "BossRefactorTFHydraCombat",
                bossRefactorTF$hydraCombatState.save());
    }

    @Inject(
            method = {"readAdditionalSaveData", "m_7378_"},
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$loadHydraCombat(
            CompoundTag tag, CallbackInfo callback) {
        if (tag.contains("BossRefactorTFHydraCombat", Tag.TAG_COMPOUND)) {
            bossRefactorTF$hydraCombatState.load(
                    tag.getCompound("BossRefactorTFHydraCombat"));
        }
        HydraCombat.restoreVisualState(bossRefactorTF$self());
    }

    @Override
    public HydraCombatState bossRefactorTF$getHydraCombatState() {
        return bossRefactorTF$hydraCombatState;
    }

    @Override
    public HydraSkill bossRefactorTF$getVisibleHydraSkill() {
        return HydraSkill.byId(
                getEntityData().get(BOSS_REFACTOR_TF$HYDRA_SKILL));
    }

    @Override
    public int bossRefactorTF$getVisibleHydraHeadIndex() {
        return getEntityData().get(BOSS_REFACTOR_TF$HYDRA_HEAD);
    }

    @Override
    public float bossRefactorTF$getVisibleHydraActionProgress() {
        return getEntityData().get(BOSS_REFACTOR_TF$HYDRA_PROGRESS);
    }

        @Override
        public boolean bossRefactorTF$isVisibleHydraRecovering() {
                return getEntityData().get(BOSS_REFACTOR_TF$HYDRA_RECOVERING);
        }

        @Override
        public float bossRefactorTF$getVisibleHydraRecoveryStartProgress() {
                return getEntityData().get(BOSS_REFACTOR_TF$HYDRA_RECOVERY_START);
        }

    @Override
    public double bossRefactorTF$getVisibleHydraLockedX() {
        return getEntityData().get(BOSS_REFACTOR_TF$HYDRA_GEOMETRY)
                .getDouble("LockedX");
    }

    @Override
    public double bossRefactorTF$getVisibleHydraLockedY() {
        return getEntityData().get(BOSS_REFACTOR_TF$HYDRA_GEOMETRY)
                .getDouble("LockedY");
    }

    @Override
    public double bossRefactorTF$getVisibleHydraLockedZ() {
        return getEntityData().get(BOSS_REFACTOR_TF$HYDRA_GEOMETRY)
                .getDouble("LockedZ");
    }

    @Override
    public double bossRefactorTF$getVisibleHydraDirectionX() {
        return getEntityData().get(BOSS_REFACTOR_TF$HYDRA_GEOMETRY)
                .getDouble("DirectionX");
    }

    @Override
    public double bossRefactorTF$getVisibleHydraDirectionZ() {
        return getEntityData().get(BOSS_REFACTOR_TF$HYDRA_GEOMETRY)
                .getDouble("DirectionZ");
    }

    @Override
    public void bossRefactorTF$setHydraVisualState(
            HydraSkill skill, int headIndex, float progress,
            boolean recovering, float recoveryStartProgress,
            double lockedX, double lockedY, double lockedZ,
            double directionX, double directionZ) {
        getEntityData().set(
                BOSS_REFACTOR_TF$HYDRA_SKILL, skill.ordinal());
        getEntityData().set(BOSS_REFACTOR_TF$HYDRA_HEAD, headIndex);
        getEntityData().set(
                BOSS_REFACTOR_TF$HYDRA_PROGRESS,
                Math.max(0.0F, Math.min(1.0F, progress)));
        getEntityData().set(BOSS_REFACTOR_TF$HYDRA_RECOVERING, recovering);
        getEntityData().set(
                BOSS_REFACTOR_TF$HYDRA_RECOVERY_START,
                Math.max(0.0F, Math.min(1.0F, recoveryStartProgress)));
        getEntityData().set(
                BOSS_REFACTOR_TF$HYDRA_GEOMETRY,
                bossRefactorTF$encodeGeometry(
                        lockedX, lockedY, lockedZ,
                        directionX, directionZ));
    }

    @Override
    public void bossRefactorTF$setHydraTelegraphs(
            AttackTelegraph primary, List<AttackTelegraph> beams) {
        getEntityData().set(
                BOSS_REFACTOR_TF$HYDRA_TELEGRAPHS,
                bossRefactorTF$encodeTelegraphs(primary, beams));
    }

    @Override
    public AttackTelegraph bossRefactorTF$getAttackTelegraph() {
        List<AttackTelegraph> telegraphs = bossRefactorTF$getAttackTelegraphs();
        return telegraphs.isEmpty() ? AttackTelegraph.NONE : telegraphs.get(0);
    }

    @Override
    public List<AttackTelegraph> bossRefactorTF$getAttackTelegraphs() {
        CompoundTag tag = getEntityData().get(
                BOSS_REFACTOR_TF$HYDRA_TELEGRAPHS);
        List<AttackTelegraph> telegraphs = new ArrayList<>();
        AttackTelegraph primary = AttackTelegraphNbt.decode(
                tag.getCompound("Primary"));
        if (primary.shape() != AttackTelegraphShape.NONE) {
            telegraphs.add(primary);
        }
        ListTag beamTags = tag.getList("Beams", Tag.TAG_COMPOUND);
        for (int index = 0; index < beamTags.size(); index++) {
            AttackTelegraph beam = AttackTelegraphNbt.decode(
                    beamTags.getCompound(index));
            if (beam.shape() != AttackTelegraphShape.NONE) {
                telegraphs.add(beam);
            }
        }
        return telegraphs;
    }

    @Override
    public void bossRefactorTF$setAttackTelegraph(
            AttackTelegraph telegraph) {
        bossRefactorTF$setHydraTelegraphs(telegraph, List.of());
    }

    @Override
    public boolean bossRefactorTF$isCurrentAttackParryable() {
        return HydraCombat.customCombatEnabled()
                && HydraCombat.isCurrentAttackParryable(
                        bossRefactorTF$self());
    }

    @Override
    public int bossRefactorTF$getParryFeedbackTicks() {
        return bossRefactorTF$isCurrentAttackParryable()
                ? BossRefactorTwilightForestConfig.HYDRA
                        .parryFeedbackTicks.get()
                : 0;
    }

        @Override
        public boolean bossRefactorTF$suppressesExternalParryStun() {
                return true;
        }

    @Override
    public int bossRefactorTF$acceptParry(
            @Nullable LivingEntity actor, ResourceLocation sourceId) {
        int feedbackTicks = bossRefactorTF$getParryFeedbackTicks();
        return feedbackTicks > 0
                && HydraCombat.acceptParry(bossRefactorTF$self())
                ? feedbackTicks
                : 0;
    }

    @Unique
    private static CompoundTag bossRefactorTF$encodeGeometry(
            double lockedX, double lockedY, double lockedZ,
            double directionX, double directionZ) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("LockedX", lockedX);
        tag.putDouble("LockedY", lockedY);
        tag.putDouble("LockedZ", lockedZ);
        tag.putDouble("DirectionX", directionX);
        tag.putDouble("DirectionZ", directionZ);
        return tag;
    }

    @Unique
    private static CompoundTag bossRefactorTF$encodeTelegraphs(
            AttackTelegraph primary, List<AttackTelegraph> beams) {
        CompoundTag tag = new CompoundTag();
        tag.put("Primary", AttackTelegraphNbt.encode(primary));
        ListTag beamTags = new ListTag();
        for (AttackTelegraph beam : beams) {
            beamTags.add(AttackTelegraphNbt.encode(beam));
        }
        tag.put("Beams", beamTags);
        return tag;
    }

    @Unique
    private Hydra bossRefactorTF$self() {
        return (Hydra) (Object) this;
    }
}