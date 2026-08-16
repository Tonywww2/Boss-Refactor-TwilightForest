package com.tonywww.bossrefactortf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.boss.snowqueen.SnowQueenSummonAccess;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.entity.boss.SnowQueen;

@Mod.EventBusSubscriber(
        modid = BossRefactorTwilightForest.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SnowQueenLinkRenderer {
    private SnowQueenLinkRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        BossRefactorTwilightForestConfig.SnowQueenConfig config =
                BossRefactorTwilightForestConfig.SNOW_QUEEN;
        Camera camera = event.getCamera();
        Vec3 cameraPosition = camera.getPosition();
        double maximumDistance = config.linkMaximumRenderDistance.get();
        double maximumDistanceSquared = maximumDistance * maximumDistance;
        float partialTick = event.getPartialTick();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers =
                minecraft.renderBuffers().bufferSource();
        RenderType lineType = RenderType.debugLineStrip(
                config.linkLineWidth.get());
        float red = config.linkRed.get() / 255.0F;
        float green = config.linkGreen.get() / 255.0F;
        float blue = config.linkBlue.get() / 255.0F;
        float alpha = config.linkAlpha.get() / 255.0F;

        poseStack.pushPose();
        poseStack.translate(
                -cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity summon)
                    || !(entity instanceof SnowQueenSummonAccess access)
                    || !summon.isAlive()
                    || !access.bossRefactorTF$isSnowQueenLinked()
                    || access.bossRefactorTF$getSnowQueenOwnerEntityId() <= 0) {
                continue;
            }

            Entity ownerEntity = minecraft.level.getEntity(
                    access.bossRefactorTF$getSnowQueenOwnerEntityId());
            if (!(ownerEntity instanceof SnowQueen owner) || !owner.isAlive()) {
                continue;
            }

            Vec3 summonPosition = interpolatedCenter(summon, partialTick);
            Vec3 ownerPosition = interpolatedCenter(owner, partialTick);
            if (summonPosition.distanceToSqr(cameraPosition)
                    > maximumDistanceSquared
                    || ownerPosition.distanceToSqr(cameraPosition)
                    > maximumDistanceSquared) {
                continue;
            }

            VertexConsumer consumer = buffers.getBuffer(lineType);
            vertex(poseStack, consumer, ownerPosition, red, green, blue, alpha);
            vertex(poseStack, consumer, summonPosition, red, green, blue, alpha);
            buffers.endBatch(lineType);
        }
        poseStack.popPose();
    }

    private static Vec3 interpolatedCenter(Entity entity, float partialTick) {
        return new Vec3(
                Mth.lerp(partialTick, entity.xo, entity.getX()),
                Mth.lerp(partialTick, entity.yo, entity.getY())
                        + entity.getBbHeight() * 0.5,
                Mth.lerp(partialTick, entity.zo, entity.getZ()));
    }

    private static void vertex(
            PoseStack poseStack, VertexConsumer consumer, Vec3 position,
            float red, float green, float blue, float alpha) {
        consumer.vertex(poseStack.last().pose(),
                        (float) position.x, (float) position.y, (float) position.z)
                .color(red, green, blue, alpha)
                .endVertex();
    }
}