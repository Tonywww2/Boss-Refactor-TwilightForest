package com.tonywww.bossrefactortf.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tonywww.bossrefactortf.boss.naga.NagaCombatAccess;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.entity.boss.Naga;

@Mixin(LivingEntityRenderer.class)
public abstract class NagaFireSnakeRendererMixin {
    @Redirect(
            method = "render",
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
        }
        model.renderToBuffer(
                modelPoseStack, consumer, modelPackedLight, packedOverlay,
                red, green, blue, alpha);
    }
}