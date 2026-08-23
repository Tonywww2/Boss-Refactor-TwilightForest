package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.hydra.HydraCombat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.boss.Hydra;
import twilightforest.entity.boss.HydraHeadContainer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(value = HydraHeadContainer.class, remap = false)
public abstract class HydraHeadContainerMixin {
        @Unique
        private static final int BOSS_REFACTOR_TF$VANILLA_STATE_COUNT = 17;

    @Shadow
    @Final
    private Hydra hydra;

    @Shadow
    @Final
    private int headNum;

    @Shadow
    protected abstract void setNeckPosition(
            double x, double y, double z, float yaw);

    @Shadow
    @Final
    private Map<Object, Float>[] stateNeckLength;

    @Shadow
    @Final
    private Map<Object, Float>[] stateXRotations;

    @Shadow
    @Final
    private Map<Object, Float>[] stateYRotations;

    @Shadow
    @Final
    private Map<Object, Float>[] stateMouthOpen;

    @ModifyConstant(
            method = "<init>",
            constant = @Constant(intValue = 7),
            require = 5,
            remap = false)
    private int bossRefactorTF$expandHeadAnimationArrays(int original) {
        return 9;
    }

    @Inject(
            method = "setupStateRotations",
            at = @At("TAIL"),
            remap = false)
    private void bossRefactorTF$addExpandedHeadAnimations(
            CallbackInfo callback) {
        bossRefactorTF$copyCompleteStateMap(
                stateNeckLength, 5, 7, 0.0F);
        bossRefactorTF$copyCompleteStateMap(
                stateXRotations, 5, 7, 0.0F);
        bossRefactorTF$copyCompleteStateMap(
                stateYRotations, 5, 7, 30.0F);
        bossRefactorTF$copyCompleteStateMap(
                stateMouthOpen, 5, 7, 0.0F);
        bossRefactorTF$copyCompleteStateMap(
                stateNeckLength, 6, 8, 0.0F);
        bossRefactorTF$copyCompleteStateMap(
                stateXRotations, 6, 8, 0.0F);
        bossRefactorTF$copyCompleteStateMap(
                stateYRotations, 6, 8, -30.0F);
        bossRefactorTF$copyCompleteStateMap(
                stateMouthOpen, 6, 8, 0.0F);
    }

    @Unique
    private static void bossRefactorTF$copyCompleteStateMap(
            Map<Object, Float>[] maps, int sourceIndex,
            int targetIndex, float offset) {
        Map<Object, Float> source = maps[sourceIndex];
        Map<Object, Float> target = maps[targetIndex];
        if (source == null || target == null) {
            throw new IllegalStateException(
                    "Hydra head animation maps were not initialized");
        }
        Set<Object> states = new HashSet<>();
        for (Map<Object, Float> map : maps) {
            if (map != null) {
                states.addAll(map.keySet());
            }
        }
        if (states.size() != BOSS_REFACTOR_TF$VANILLA_STATE_COUNT) {
            throw new IllegalStateException(
                    "Unexpected Hydra head animation state count: "
                            + states.size());
        }
        for (Object state : states) {
            Float value = source.get(state);
            if (value == null) {
                for (Map<Object, Float> candidates : maps) {
                    if (candidates != null && candidates.containsKey(state)) {
                        value = candidates.get(state);
                        break;
                    }
                }
            }
            if (value != null) {
                target.put(
                        state,
                        offset == 0.0F
                                ? value
                                : bossRefactorTF$wrapDegrees(value + offset));
            }
        }
        if (target.size() != BOSS_REFACTOR_TF$VANILLA_STATE_COUNT) {
            throw new IllegalStateException(
                    "Expanded Hydra head animation map is incomplete");
        }
    }

    @Unique
    private static float bossRefactorTF$wrapDegrees(float value) {
        float wrapped = value % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    @Inject(
            method = "setNeckPosition()V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$positionExpandedHeadNecks(
            CallbackInfo callback) {
        if (headNum != 7 && headNum != 8) {
            return;
        }
        Vec3 rootOffset = headNum == 7
                ? new Vec3(-1.0, 3.0, 5.0)
                : new Vec3(1.0, 3.0, 5.0);
        float angle = headNum == 7 ? 165.0F : -165.0F;
        Vec3 rotatedOffset = rootOffset.yRot(
                -(hydra.yBodyRot + angle) * (float) Math.PI / 180.0F);
        setNeckPosition(
                hydra.getX() + rotatedOffset.x,
                hydra.getY() + rotatedOffset.y,
                hydra.getZ() + rotatedOffset.z,
                hydra.yBodyRot);
        callback.cancel();
    }

        @Group(name = "mixedFlameBreathDamage", min = 1, max = 1)
        @Redirect(
            method = "executeAttacks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    ordinal = 1),
            require = 0,
            remap = false)
    private boolean bossRefactorTF$applyMixedFlameBreathDamageMcp(
            Entity target, DamageSource source, float amount) {
        return bossRefactorTF$applyMixedFlameBreathDamage(
                target, source, amount);
    }

    @Group(name = "mixedFlameBreathDamage", min = 1, max = 1)
    @Redirect(
            method = "executeAttacks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    ordinal = 1),
            require = 0,
            remap = false)
    private boolean bossRefactorTF$applyMixedFlameBreathDamageSrg(
            Entity target, DamageSource source, float amount) {
        return bossRefactorTF$applyMixedFlameBreathDamage(
                target, source, amount);
    }

    @Unique
    private boolean bossRefactorTF$applyMixedFlameBreathDamage(
            Entity target, DamageSource source, float amount) {
        HydraHeadContainer container = (HydraHeadContainer) (Object) this;
        return HydraCombat.customCombatEnabled()
                ? HydraCombat.hurtWithMixedFire(
                        target, container.headEntity, hydra, amount)
                : target.hurt(source, amount);
    }

}