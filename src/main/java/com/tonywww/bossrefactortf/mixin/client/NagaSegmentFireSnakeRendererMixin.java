package com.tonywww.bossrefactortf.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tonywww.bossrefactortf.boss.naga.NagaCombatAccess;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.client.renderer.entity.TFPartRenderer;
import twilightforest.entity.TFPart;
import twilightforest.entity.boss.Naga;
import twilightforest.entity.boss.NagaSegment;

@Mixin(value = TFPartRenderer.class, remap = false)
public abstract class NagaSegmentFireSnakeRendererMixin {
        @Redirect(
            method = "render(Ltwilightforest/entity/TFPart;FF"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/ListModel;renderToBuffer("
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
                    remap = true))
    private void bossRefactorTF$tintNagaSegmentFireSnake(
            ListModel<?> model, PoseStack modelPoseStack,
            VertexConsumer consumer, int modelPackedLight, int packedOverlay,
            float red, float green, float blue, float alpha,
            TFPart<?> part, float entityYaw,
            float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int entityPackedLight) {
        if (part instanceof NagaSegment segment) {
            Naga parent = segment.getParent();
            if (parent instanceof NagaCombatAccess access
                    && access.bossRefactorTF$isFireSnakeVisualActive()) {
                BossRefactorTwilightForestConfig.NagaConfig config =
                        BossRefactorTwilightForestConfig.NAGA;
                red *= config.fireSnakeRedMultiplier.get().floatValue();
                green *= config.fireSnakeGreenMultiplier.get().floatValue();
                blue *= config.fireSnakeBlueMultiplier.get().floatValue();
            }
        }
        model.renderToBuffer(
                modelPoseStack, consumer, modelPackedLight, packedOverlay,
                red, green, blue, alpha);
    }
}