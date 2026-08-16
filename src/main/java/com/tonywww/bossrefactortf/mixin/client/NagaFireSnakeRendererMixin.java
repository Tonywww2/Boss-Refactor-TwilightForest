package com.tonywww.bossrefactortf.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tonywww.bossrefactortf.boss.minoshroom.MinoshroomCombatAccess;
import com.tonywww.bossrefactortf.boss.naga.NagaCombatAccess;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.entity.boss.Minoshroom;
import twilightforest.entity.boss.Naga;

@Mixin(LivingEntityRenderer.class)
public abstract class NagaFireSnakeRendererMixin {
    @Redirect(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FF"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer("
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void bossRefactorTF$tintNagaFireSnake(
            EntityModel<?> model, PoseStack modelPoseStack,
            VertexConsumer consumer, int modelPackedLight, int packedOverlay,
            float red, float green, float blue, float alpha,
            LivingEntity entity, float entityYaw,
            float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int entityPackedLight) {
        if (entity instanceof Naga naga
                && naga instanceof NagaCombatAccess access
                && access.bossRefactorTF$isFireSnakeVisualActive()) {
            BossRefactorTwilightForestConfig.NagaConfig config =
                    BossRefactorTwilightForestConfig.NAGA;
            red *= config.fireSnakeRedMultiplier.get().floatValue();
            green *= config.fireSnakeGreenMultiplier.get().floatValue();
            blue *= config.fireSnakeBlueMultiplier.get().floatValue();
        } else if (entity instanceof Minoshroom minoshroom
                && minoshroom instanceof MinoshroomCombatAccess access
                && BossRefactorTwilightForestConfig.MINOSHROOM.enabled.get()
                && access.bossRefactorTF$isSecondPhase()) {
            BossRefactorTwilightForestConfig.MinoshroomConfig config =
                    BossRefactorTwilightForestConfig.MINOSHROOM;
            red *= config.secondPhaseRedTint.get().floatValue();
            green *= config.secondPhaseGreenTint.get().floatValue();
            blue *= config.secondPhaseBlueTint.get().floatValue();
        }
        model.renderToBuffer(
                modelPoseStack, consumer, modelPackedLight, packedOverlay,
                red, green, blue, alpha);
    }
}