package com.tonywww.bossrefactortf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphAccess;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(
        modid = BossRefactorTwilightForest.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AttackTelegraphRenderer {
    private AttackTelegraphRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES
                || !BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH.enabled.get()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 cameraPosition = camera.getPosition();
        double maximumDistance = BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH
                .maxRenderDistance.get();
        double maximumDistanceSquared = maximumDistance * maximumDistance;
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        List<VisibleTelegraph> visibleTelegraphs = new ArrayList<>();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof AttackTelegraphAccess access)
                    || entity.distanceToSqr(cameraPosition) > maximumDistanceSquared) {
                continue;
            }
                        for (AttackTelegraph telegraph
                                        : access.bossRefactorTF$getAttackTelegraphs()) {
                                if (telegraph.shape() != AttackTelegraphShape.NONE) {
                                        visibleTelegraphs.add(new VisibleTelegraph(entity, telegraph));
                                }
            }
        }
        if (visibleTelegraphs.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        if (BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH.fillEnabled.get()) {
            VertexConsumer fill = buffers.getBuffer(RenderType.debugQuads());
            for (VisibleTelegraph visible : visibleTelegraphs) {
                renderTelegraphFill(
                        poseStack, fill, visible.entity(), visible.telegraph());
            }
            buffers.endBatch(RenderType.debugQuads());
        }

        RenderType outlineType = RenderType.debugLineStrip(
                BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH.lineWidth.get());
        for (VisibleTelegraph visible : visibleTelegraphs) {
            renderTelegraphOutline(
                    poseStack, buffers, outlineType,
                    visible.entity(), visible.telegraph());
        }
        poseStack.popPose();
    }

    private static void renderTelegraphOutline(
            PoseStack poseStack, MultiBufferSource.BufferSource buffers,
            RenderType outlineType, Entity entity, AttackTelegraph telegraph) {
        Vec3 direction = horizontalDirection(telegraph);
        Vec3 origin = telegraphOrigin(entity, telegraph);
        switch (telegraph.shape()) {
            case ARC -> renderArc(poseStack, buffers, outlineType, origin, direction,
                    telegraph.length(), telegraph.width());
            case CIRCLE -> renderCircle(
                    poseStack, buffers, outlineType, origin, telegraph.radius());
            case RING -> renderRing(
                    poseStack, buffers, outlineType, origin,
                    telegraph.radius(), telegraph.width());
            case CORRIDOR -> renderCorridor(
                    poseStack, buffers, outlineType, origin, direction,
                    telegraph.length(), telegraph.width());
            case CORRIDOR_WITH_END_CIRCLE -> {
                renderCorridor(poseStack, buffers, outlineType, origin, direction,
                        telegraph.length(), telegraph.width());
                renderCircle(poseStack, buffers, outlineType,
                        origin.add(direction.scale(telegraph.length())),
                        telegraph.radius());
            }
            case BOX -> renderBox(
                    poseStack, buffers, outlineType, origin, telegraph.radius());
            case CYLINDER -> renderCylinder(
                    poseStack, buffers, outlineType, origin,
                    telegraph.radius(), telegraph.length());
            case NONE -> {
            }
        }
    }

    private static void renderTelegraphFill(
            PoseStack poseStack, VertexConsumer consumer,
            Entity entity, AttackTelegraph telegraph) {
        Vec3 direction = horizontalDirection(telegraph);
        Vec3 origin = telegraphOrigin(entity, telegraph);
        int alpha = fillAlpha(telegraph.progress());
        switch (telegraph.shape()) {
            case ARC -> fillArc(poseStack, consumer, origin, direction,
                    telegraph.length(), telegraph.width(), alpha);
            case CIRCLE -> fillCircle(
                    poseStack, consumer, origin, telegraph.radius(), alpha);
            case RING -> fillRing(
                    poseStack, consumer, origin,
                    telegraph.radius(), telegraph.width(), alpha);
            case CORRIDOR -> fillCorridor(
                    poseStack, consumer, origin, direction,
                    telegraph.length(), telegraph.width(), alpha);
            case CORRIDOR_WITH_END_CIRCLE -> {
                fillCorridor(poseStack, consumer, origin, direction,
                        telegraph.length(), telegraph.width(), alpha);
                fillCircle(poseStack, consumer,
                        origin.add(direction.scale(telegraph.length())),
                        telegraph.radius(), alpha);
            }
            case BOX -> fillBox(
                    poseStack, consumer, origin, telegraph.radius(), alpha);
            case CYLINDER -> fillCylinder(
                    poseStack, consumer, origin,
                    telegraph.radius(), telegraph.length(), alpha);
            case NONE -> {
            }
        }
    }

    private static void renderArc(
            PoseStack poseStack, MultiBufferSource.BufferSource buffers,
            RenderType outlineType, Vec3 origin, Vec3 direction,
            double radius, double halfAngleDegrees) {
        VertexConsumer consumer = buffers.getBuffer(outlineType);
        int segments = segmentCount(halfAngleDegrees * 2.0 / 360.0);
        double baseAngle = Math.atan2(direction.z, direction.x);
        double halfAngle = Math.toRadians(halfAngleDegrees);
        stripVertex(poseStack, consumer, origin);
        stripVertex(poseStack, consumer,
                pointOnCircle(origin, radius, baseAngle - halfAngle));
        for (int index = 1; index <= segments; index++) {
            double progress = index / (double) segments;
            stripVertex(poseStack, consumer, pointOnCircle(
                    origin, radius,
                    baseAngle - halfAngle + halfAngle * 2.0 * progress));
        }
        stripVertex(poseStack, consumer, origin);
        buffers.endBatch(outlineType);
    }

    private static void renderCircle(
            PoseStack poseStack, MultiBufferSource.BufferSource buffers,
            RenderType outlineType, Vec3 center, double radius) {
        VertexConsumer consumer = buffers.getBuffer(outlineType);
        int segments = segmentCount(1.0);
        for (int index = 0; index <= segments; index++) {
            double angle = Math.PI * 2.0 * index / segments;
            stripVertex(poseStack, consumer, pointOnCircle(center, radius, angle));
        }
        buffers.endBatch(outlineType);
    }

    private static void renderCorridor(
            PoseStack poseStack, MultiBufferSource.BufferSource buffers,
            RenderType outlineType, Vec3 origin, Vec3 direction,
            double length, double halfWidth) {
        VertexConsumer consumer = buffers.getBuffer(outlineType);
        Vec3 perpendicular = new Vec3(-direction.z, 0.0, direction.x)
                .scale(Math.max(0.0, halfWidth));
        Vec3 end = origin.add(direction.scale(Math.max(0.0, length)));
        Vec3 startLeft = origin.add(perpendicular);
        Vec3 startRight = origin.subtract(perpendicular);
        Vec3 endLeft = end.add(perpendicular);
        Vec3 endRight = end.subtract(perpendicular);
        stripVertex(poseStack, consumer, startLeft);
        stripVertex(poseStack, consumer, endLeft);
        stripVertex(poseStack, consumer, endRight);
        stripVertex(poseStack, consumer, startRight);
        stripVertex(poseStack, consumer, startLeft);
        buffers.endBatch(outlineType);
    }

        private static void renderRing(
                        PoseStack poseStack, MultiBufferSource.BufferSource buffers,
                        RenderType outlineType, Vec3 center,
                        double radius, double halfWidth) {
                double innerRadius = Math.max(0.0, radius - halfWidth);
                double outerRadius = Math.max(innerRadius, radius + halfWidth);
                renderCircle(poseStack, buffers, outlineType, center, innerRadius);
                renderCircle(poseStack, buffers, outlineType, center, outerRadius);
        }

        private static void renderBox(
                        PoseStack poseStack, MultiBufferSource.BufferSource buffers,
                        RenderType outlineType, Vec3 center, double radius) {
                double halfSize = Math.max(0.0, radius);
                Vec3 bottomNorthWest = center.add(-halfSize, -halfSize, -halfSize);
                Vec3 bottomNorthEast = center.add(halfSize, -halfSize, -halfSize);
                Vec3 bottomSouthEast = center.add(halfSize, -halfSize, halfSize);
                Vec3 bottomSouthWest = center.add(-halfSize, -halfSize, halfSize);
                Vec3 topNorthWest = center.add(-halfSize, halfSize, -halfSize);
                Vec3 topNorthEast = center.add(halfSize, halfSize, -halfSize);
                Vec3 topSouthEast = center.add(halfSize, halfSize, halfSize);
                Vec3 topSouthWest = center.add(-halfSize, halfSize, halfSize);
                VertexConsumer consumer = buffers.getBuffer(outlineType);
                for (Vec3 point : new Vec3[] {
                                bottomNorthWest, bottomNorthEast,
                                bottomSouthEast, bottomSouthWest, bottomNorthWest,
                                topNorthWest, topNorthEast,
                                topSouthEast, topSouthWest, topNorthWest,
                                topSouthWest, bottomSouthWest,
                                bottomSouthEast, topSouthEast,
                                topNorthEast, bottomNorthEast}) {
                        stripVertex(poseStack, consumer, point);
                }
                buffers.endBatch(outlineType);
        }

        private static void renderCylinder(
                        PoseStack poseStack, MultiBufferSource.BufferSource buffers,
                        RenderType outlineType, Vec3 baseCenter,
                        double radius, double height) {
                Vec3 topCenter = baseCenter.add(0.0, Math.max(0.0, height), 0.0);
                renderCircle(poseStack, buffers, outlineType, baseCenter, radius);
                renderCircle(poseStack, buffers, outlineType, topCenter, radius);
                for (int index = 0; index < 4; index++) {
                        double angle = Math.PI * 0.5 * index;
                        renderLine(poseStack, buffers, outlineType,
                                        pointOnCircle(baseCenter, radius, angle),
                                        pointOnCircle(topCenter, radius, angle));
                }
        }

        private static void renderLine(
                        PoseStack poseStack, MultiBufferSource.BufferSource buffers,
                        RenderType outlineType, Vec3 start, Vec3 end) {
                VertexConsumer consumer = buffers.getBuffer(outlineType);
                stripVertex(poseStack, consumer, start);
                stripVertex(poseStack, consumer, end);
                buffers.endBatch(outlineType);
        }

    private static void fillArc(
            PoseStack poseStack, VertexConsumer consumer,
            Vec3 origin, Vec3 direction,
            double radius, double halfAngleDegrees, int alpha) {
        int segments = segmentCount(halfAngleDegrees * 2.0 / 360.0);
        double baseAngle = Math.atan2(direction.z, direction.x);
        double halfAngle = Math.toRadians(halfAngleDegrees);
        Vec3 previous = pointOnCircle(origin, radius, baseAngle - halfAngle);
        for (int index = 1; index <= segments; index++) {
            double progress = index / (double) segments;
            Vec3 current = pointOnCircle(
                    origin, radius,
                    baseAngle - halfAngle + halfAngle * 2.0 * progress);
            triangle(poseStack, consumer, origin, previous, current, alpha);
            previous = current;
        }
    }

    private static void fillCircle(
            PoseStack poseStack, VertexConsumer consumer,
            Vec3 center, double radius, int alpha) {
        int segments = segmentCount(1.0);
        Vec3 previous = pointOnCircle(center, radius, 0.0);
        for (int index = 1; index <= segments; index++) {
            double angle = Math.PI * 2.0 * index / segments;
            Vec3 current = pointOnCircle(center, radius, angle);
            triangle(poseStack, consumer, center, previous, current, alpha);
            previous = current;
        }
    }

    private static void fillCorridor(
            PoseStack poseStack, VertexConsumer consumer,
            Vec3 origin, Vec3 direction,
            double length, double halfWidth, int alpha) {
        Vec3 perpendicular = new Vec3(-direction.z, 0.0, direction.x)
                .scale(Math.max(0.0, halfWidth));
        Vec3 end = origin.add(direction.scale(Math.max(0.0, length)));
        quad(poseStack, consumer,
                origin.add(perpendicular),
                end.add(perpendicular),
                end.subtract(perpendicular),
                origin.subtract(perpendicular), alpha);
    }

        private static void fillRing(
                        PoseStack poseStack, VertexConsumer consumer,
                        Vec3 center, double radius, double halfWidth, int alpha) {
                double innerRadius = Math.max(0.0, radius - halfWidth);
                double outerRadius = Math.max(innerRadius, radius + halfWidth);
                int segments = segmentCount(1.0);
                Vec3 previousInner = pointOnCircle(center, innerRadius, 0.0);
                Vec3 previousOuter = pointOnCircle(center, outerRadius, 0.0);
                for (int index = 1; index <= segments; index++) {
                        double angle = Math.PI * 2.0 * index / segments;
                        Vec3 currentInner = pointOnCircle(center, innerRadius, angle);
                        Vec3 currentOuter = pointOnCircle(center, outerRadius, angle);
                        quad(poseStack, consumer,
                                        previousInner, previousOuter,
                                        currentOuter, currentInner, alpha);
                        previousInner = currentInner;
                        previousOuter = currentOuter;
                }
        }

                    private static void fillBox(
                            PoseStack poseStack, VertexConsumer consumer,
                            Vec3 center, double radius, int alpha) {
                        double halfSize = Math.max(0.0, radius);
                        Vec3 bottomNorthWest = center.add(-halfSize, -halfSize, -halfSize);
                        Vec3 bottomNorthEast = center.add(halfSize, -halfSize, -halfSize);
                        Vec3 bottomSouthEast = center.add(halfSize, -halfSize, halfSize);
                        Vec3 bottomSouthWest = center.add(-halfSize, -halfSize, halfSize);
                        Vec3 topNorthWest = center.add(-halfSize, halfSize, -halfSize);
                        Vec3 topNorthEast = center.add(halfSize, halfSize, -halfSize);
                        Vec3 topSouthEast = center.add(halfSize, halfSize, halfSize);
                        Vec3 topSouthWest = center.add(-halfSize, halfSize, halfSize);
                        quad(poseStack, consumer,
                                bottomNorthWest, bottomSouthWest,
                                bottomSouthEast, bottomNorthEast, alpha);
                        quad(poseStack, consumer,
                                topNorthWest, topNorthEast, topSouthEast, topSouthWest, alpha);
                        quad(poseStack, consumer,
                                bottomNorthWest, bottomNorthEast,
                                topNorthEast, topNorthWest, alpha);
                        quad(poseStack, consumer,
                                bottomSouthWest, topSouthWest,
                                topSouthEast, bottomSouthEast, alpha);
                        quad(poseStack, consumer,
                                bottomNorthWest, topNorthWest,
                                topSouthWest, bottomSouthWest, alpha);
                        quad(poseStack, consumer,
                                bottomNorthEast, bottomSouthEast,
                                topSouthEast, topNorthEast, alpha);
                    }

                    private static void fillCylinder(
                            PoseStack poseStack, VertexConsumer consumer,
                            Vec3 baseCenter, double radius, double height, int alpha) {
                        Vec3 topCenter = baseCenter.add(0.0, Math.max(0.0, height), 0.0);
                        int segments = segmentCount(1.0);
                        Vec3 previousBottom = pointOnCircle(baseCenter, radius, 0.0);
                        Vec3 previousTop = pointOnCircle(topCenter, radius, 0.0);
                        for (int index = 1; index <= segments; index++) {
                            double angle = Math.PI * 2.0 * index / segments;
                            Vec3 currentBottom = pointOnCircle(baseCenter, radius, angle);
                            Vec3 currentTop = pointOnCircle(topCenter, radius, angle);
                            quad(poseStack, consumer,
                                    previousBottom, currentBottom,
                                    currentTop, previousTop, alpha);
                            triangle(poseStack, consumer,
                                    baseCenter, currentBottom, previousBottom, alpha);
                            triangle(poseStack, consumer,
                                    topCenter, previousTop, currentTop, alpha);
                            previousBottom = currentBottom;
                            previousTop = currentTop;
                        }
                    }

    private static int segmentCount(double circumferenceFraction) {
        int configured = BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH
                .curveSegments.get();
        return Math.max(2, (int) Math.ceil(
                configured * Math.max(0.05, circumferenceFraction)));
    }

    private static Vec3 pointOnCircle(Vec3 center, double radius, double angle) {
        return center.add(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
    }

    private static Vec3 horizontalDirection(AttackTelegraph telegraph) {
        Vec3 direction = new Vec3(
                telegraph.directionX(), 0.0, telegraph.directionZ());
        return direction.lengthSqr() < 1.0E-8
                ? new Vec3(0.0, 0.0, 1.0)
                : direction.normalize();
    }

    private static Vec3 telegraphOrigin(Entity entity, AttackTelegraph telegraph) {
        double heightOffset = BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH
                .heightOffset.get();
        if (telegraph.hasLockedOrigin()) {
            return new Vec3(
                    telegraph.originX(), telegraph.originY() + heightOffset,
                    telegraph.originZ());
        }
        return new Vec3(entity.getX(), entity.getY() + heightOffset, entity.getZ());
    }

    private static int fillAlpha(float progress) {
        int start = BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH
                .fillStartAlpha.get();
        int end = BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH
                .fillEndAlpha.get();
        double clamped = Math.max(0.0, Math.min(1.0, progress));
        return Math.max(0, Math.min(255,
                (int) Math.round(start + (end - start) * clamped)));
    }

    private static void triangle(
            PoseStack poseStack, VertexConsumer consumer,
            Vec3 first, Vec3 second, Vec3 third, int alpha) {
        quad(poseStack, consumer, first, second, third, first, alpha);
    }

    private static void quad(
            PoseStack poseStack, VertexConsumer consumer,
            Vec3 first, Vec3 second, Vec3 third, Vec3 fourth, int alpha) {
        int red = BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH.red.get();
        int green = BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH.green.get();
        int blue = BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH.blue.get();
        PoseStack.Pose pose = poseStack.last();
        consumer.vertex(pose.pose(), (float) first.x, (float) first.y, (float) first.z)
                .color(red, green, blue, alpha).endVertex();
        consumer.vertex(pose.pose(), (float) second.x, (float) second.y, (float) second.z)
                .color(red, green, blue, alpha).endVertex();
        consumer.vertex(pose.pose(), (float) third.x, (float) third.y, (float) third.z)
                .color(red, green, blue, alpha).endVertex();
        consumer.vertex(pose.pose(), (float) fourth.x, (float) fourth.y, (float) fourth.z)
                .color(red, green, blue, alpha).endVertex();
    }

    private static void stripVertex(
            PoseStack poseStack, VertexConsumer consumer, Vec3 position) {
        int red = BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH.red.get();
        int green = BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH.green.get();
        int blue = BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH.blue.get();
        int alpha = BossRefactorTwilightForestConfig.ATTACK_TELEGRAPH.alpha.get();
        PoseStack.Pose pose = poseStack.last();
        consumer.vertex(pose.pose(),
                        (float) position.x, (float) position.y, (float) position.z)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private record VisibleTelegraph(Entity entity, AttackTelegraph telegraph) {
    }
}