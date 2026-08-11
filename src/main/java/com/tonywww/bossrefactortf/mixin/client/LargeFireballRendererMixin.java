package com.tonywww.bossrefactortf.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.LargeFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.boss.UrGhast;

@Mixin(ThrownItemRenderer.class)
@SuppressWarnings("target")
public abstract class LargeFireballRendererMixin {
    private static final float VANILLA_LARGE_FIREBALL_SCALE = 3.0F;

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;FF"
                + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                + "Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    shift = At.Shift.AFTER))
    private void bossRefactorTF$scaleUrGhastFireball(
            Entity entity, float yaw, float partialTick,
            PoseStack poseStack, MultiBufferSource buffers,
            int packedLight, CallbackInfo callback) {
        if (!(entity instanceof LargeFireball fireball)
                || !(fireball.getOwner() instanceof UrGhast)
                || !BossRefactorTwilightForestConfig.UR_GHAST.enabled.get()) {
            return;
        }
        float configuredScale = BossRefactorTwilightForestConfig.UR_GHAST
                .fireballVisualScale.get().floatValue();
        float multiplier = configuredScale / VANILLA_LARGE_FIREBALL_SCALE;
        poseStack.scale(multiplier, multiplier, multiplier);
    }
}