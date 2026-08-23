package com.tonywww.bossrefactortf.client;

import com.tonywww.bossrefactortf.boss.hydra.HydraCombatAccess;
import com.tonywww.bossrefactortf.boss.hydra.HydraSkill;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.boss.Hydra;
import twilightforest.entity.boss.HydraHead;
import twilightforest.entity.boss.HydraHeadContainer;

import java.util.Map;
import java.util.WeakHashMap;

public final class HydraHeadAnimation {
    private static final float DEGREES_TO_RADIANS = Mth.PI / 180.0F;
    private static final Map<Hydra, ProgressSample> PROGRESS_SAMPLES =
            new WeakHashMap<>();

    private HydraHeadAnimation() {
    }

    public static void apply(
            HydraHead headEntity, ModelPart head, ModelPart mouth,
            float baseX, float baseY, float baseZ, float baseZRot,
            float partialTick) {
        head.x = baseX;
        head.y = baseY;
        head.z = baseZ;
        head.zRot = baseZRot;
        head.yRot = interpolatedHeadYaw(headEntity, partialTick);

        Hydra hydra = headEntity.getParent();
        if (hydra == null || !(hydra instanceof HydraCombatAccess access)) {
            return;
        }
        int headIndex = findHeadIndex(hydra, headEntity);
        if (headIndex < 0) {
            return;
        }
        HydraHeadContainer container = hydra.hc[headIndex];
        HydraSkill skill = access.bossRefactorTF$getVisibleHydraSkill();
        if (skill == HydraSkill.NONE) {
            PROGRESS_SAMPLES.remove(hydra);
            return;
        }
        if (!container.isActive() || !animatesHead(
                skill, headIndex,
                access.bossRefactorTF$getVisibleHydraHeadIndex())) {
            return;
        }

        float animationProgress = interpolatedProgress(
                hydra, skill,
                access.bossRefactorTF$getVisibleHydraHeadIndex(),
                access.bossRefactorTF$getVisibleHydraActionProgress(),
                partialTick);
        boolean recovering = access.bossRefactorTF$isVisibleHydraRecovering();
        float recoveryStart = access
            .bossRefactorTF$getVisibleHydraRecoveryStartProgress();
        float progress = recovering ? recoveryStart : animationProgress;
        float blend = recovering && recoveryStart > 1.0E-4F
            ? smoothStep(
                0.0F, 1.0F,
                animationProgress / recoveryStart)
            : 1.0F;
        float aimWeight = aimWeight(skill, progress) * blend;
        if (skill == HydraSkill.SWEEP) {
            aimAtLockedDirection(head, access, aimWeight);
        } else {
            aimAtLockedPoint(headEntity, head, access, aimWeight);
        }
        switch (skill) {
            case BITE -> applyBite(head, mouth, progress, blend);
            case SWEEP -> applySweep(head, mouth, progress, blend);
            case SLAM -> applySlam(head, mouth, progress, blend);
            case FIRE_LORD -> applyFireLord(head, mouth, progress, blend);
                case EVIL_FIREBALL -> applyEvilFireball(
                    head, mouth, progress, blend);
            default -> {
            }
        }
    }

    private static int findHeadIndex(Hydra hydra, HydraHead headEntity) {
        for (int index = 0; index < hydra.hc.length; index++) {
            if (hydra.hc[index].headEntity == headEntity) {
                return index;
            }
        }
        return -1;
    }

    private static boolean animatesHead(
            HydraSkill skill, int headIndex, int visibleHeadIndex) {
        return skill == HydraSkill.FIRE_LORD
            || (skill.isCustomMelee()
                || skill == HydraSkill.EVIL_FIREBALL)
            && headIndex == visibleHeadIndex;
    }

    private static float interpolatedProgress(
            Hydra hydra, HydraSkill skill, int headIndex,
            float synchronizedProgress, float partialTick) {
        ProgressSample sample = PROGRESS_SAMPLES.computeIfAbsent(
                hydra, ignored -> new ProgressSample());
        if (sample.skill != skill || sample.headIndex != headIndex) {
            sample.skill = skill;
            sample.headIndex = headIndex;
            sample.previous = synchronizedProgress;
            sample.current = synchronizedProgress;
        } else if (Float.compare(sample.current, synchronizedProgress) != 0) {
            sample.previous = sample.current;
            sample.current = synchronizedProgress;
        }
            return Mth.lerp(
                Mth.clamp(partialTick, 0.0F, 1.0F),
                sample.previous, sample.current);
    }

    private static void aimAtLockedPoint(
            HydraHead headEntity, ModelPart head,
            HydraCombatAccess access, float weight) {
        if (weight <= 0.0F) {
            return;
        }
        Vec3 offset = new Vec3(
                access.bossRefactorTF$getVisibleHydraLockedX()
                        - headEntity.getX(),
                access.bossRefactorTF$getVisibleHydraLockedY()
                        - headEntity.getY(),
                access.bossRefactorTF$getVisibleHydraLockedZ()
                        - headEntity.getZ());
        double horizontalDistance = Math.sqrt(
                offset.x * offset.x + offset.z * offset.z);
        if (horizontalDistance < 1.0E-4) {
            return;
        }
        float desiredYaw = (float) Math.atan2(-offset.x, offset.z);
        float desiredPitch = (float) Math.atan2(
                -offset.y, horizontalDistance);
        head.yRot = lerpRadians(head.yRot, desiredYaw, weight);
        head.xRot = Mth.lerp(
                weight, head.xRot,
                Mth.clamp(desiredPitch, -1.15F, 1.15F));
    }

    private static void aimAtLockedDirection(
            ModelPart head, HydraCombatAccess access, float weight) {
        if (weight <= 0.0F) {
            return;
        }
        double directionX = access.bossRefactorTF$getVisibleHydraDirectionX();
        double directionZ = access.bossRefactorTF$getVisibleHydraDirectionZ();
        if (directionX * directionX + directionZ * directionZ < 1.0E-6) {
            return;
        }
        float desiredYaw = (float) Math.atan2(-directionX, directionZ);
        head.yRot = lerpRadians(head.yRot, desiredYaw, weight);
    }

    private static void applyBite(
            ModelPart head, ModelPart mouth, float progress, float blend) {
        float open = smoothStep(0.02F, 0.38F, progress);
        float lunge = smoothStep(0.45F, 0.92F, progress);
        float close = smoothStep(0.82F, 1.0F, progress);
        float mouthOpen = open * (1.0F - close * 0.45F);
        head.xRot -= mouthOpen * 0.22F * blend;
        mouth.xRot += mouthOpen * 1.25F * blend;
        head.z -= lunge * 9.0F * blend;
    }

    private static void applySweep(
            ModelPart head, ModelPart mouth, float progress, float blend) {
        float pose = smoothStep(0.0F, 0.2F, progress);
        float sweep = smoothStep(0.2F, 1.0F, progress);
        head.yRot += Mth.lerp(75.0F, -75.0F, sweep)
            * DEGREES_TO_RADIANS * pose * blend;
        head.xRot += Mth.lerp(-38.0F, 30.0F, sweep)
            * DEGREES_TO_RADIANS * pose * blend;
        head.zRot += Mth.lerp(-18.0F, 14.0F, sweep)
            * DEGREES_TO_RADIANS * pose * blend;
        head.z -= pose * 2.0F * blend;
        mouth.xRot += pose * 0.42F * blend;
    }

    private static void applySlam(
            ModelPart head, ModelPart mouth, float progress, float blend) {
        float pose = smoothStep(0.0F, 0.18F, progress);
        float drop = smoothStep(0.55F, 1.0F, progress);
        head.xRot += Mth.lerp(-68.0F, 66.0F, drop)
            * DEGREES_TO_RADIANS * pose * blend;
        head.y += Mth.lerp(-4.0F, 5.0F, drop) * pose * blend;
        head.z -= Mth.lerp(1.0F, 3.0F, drop) * pose * blend;
        mouth.xRot += Mth.lerp(0.25F, 0.7F, drop) * pose * blend;
    }

    private static void applyFireLord(
            ModelPart head, ModelPart mouth, float progress, float blend) {
        float pose = smoothStep(0.0F, 0.18F, progress);
        float pulse = 0.08F * Mth.sin(progress * Mth.PI * 12.0F);
        head.xRot += pose * 0.42F * blend;
        head.y += pose * 2.0F * blend;
        head.z -= pose * 2.0F * blend;
        mouth.xRot += pose * (0.92F + pulse) * blend;
    }

    private static void applyEvilFireball(
            ModelPart head, ModelPart mouth, float progress, float blend) {
        float pose = smoothStep(0.0F, 0.28F, progress);
        float pulse = 0.06F * Mth.sin(progress * Mth.PI * 8.0F);
        head.xRot += pose * 0.18F * blend;
        head.z -= pose * 2.5F * blend;
        mouth.xRot += pose * (1.05F + pulse) * blend;
    }

    private static float aimWeight(HydraSkill skill, float progress) {
        return switch (skill) {
            case BITE -> smoothStep(0.25F, 0.55F, progress);
            case SWEEP -> smoothStep(0.0F, 0.2F, progress);
            case FIRE_LORD -> smoothStep(0.0F, 0.18F, progress);
            case EVIL_FIREBALL -> smoothStep(0.0F, 0.28F, progress);
            default -> 0.0F;
        };
    }

    private static float smoothStep(float start, float end, float value) {
        float normalized = Mth.clamp(
                (value - start) / (end - start), 0.0F, 1.0F);
        return normalized * normalized * (3.0F - 2.0F * normalized);
    }

    private static float lerpRadians(
            float start, float end, float progress) {
        float delta = Mth.wrapDegrees(
                (end - start) / DEGREES_TO_RADIANS)
                * DEGREES_TO_RADIANS;
        return start + delta * progress;
    }

    private static float interpolatedHeadYaw(
            HydraHead headEntity, float partialTick) {
        float previousYaw = headEntity.yRotO;
        float yawDelta = Mth.wrapDegrees(
                headEntity.getYRot() - previousYaw);
        return (previousYaw + yawDelta
                * Mth.clamp(partialTick, 0.0F, 1.0F))
                * DEGREES_TO_RADIANS;
    }

    private static final class ProgressSample {
        private HydraSkill skill = HydraSkill.NONE;
        private int headIndex = -1;
        private float previous;
        private float current;
    }
}