package com.tonywww.bossrefactortf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphAccess;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.client.renderer.entity.ShieldLayer;
import twilightforest.entity.boss.Lich;

@Mod.EventBusSubscriber(
        modid = BossRefactorTwilightForest.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LichShieldBashRenderer {
    private static final float BASE_FORWARD_OFFSET = 1.15F;
    private static final float FORWARD_PUSH = 0.10F;
    private static final float BASE_SCALE = 0.75F;
    private static final float SCALE_RANGE = 0.25F;
    private static final float SHIELD_WIDTH = 5.0F;
    private static final float SHIELD_HEIGHT = 4.0F;

    private LichShieldBashRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (!BossRefactorTwilightForestConfig.LICH.enabled.get()
                || !(event.getEntity() instanceof Lich lich)
                || !(lich instanceof AttackTelegraphAccess access)) {
            return;
        }

        AttackTelegraph telegraph = access.bossRefactorTF$getAttackTelegraph();
        if (telegraph.shape() != AttackTelegraphShape.CORRIDOR
                || !telegraph.hasLockedOrigin()) {
            return;
        }

        float directionLength = Mth.sqrt(
                telegraph.directionX() * telegraph.directionX()
                        + telegraph.directionZ() * telegraph.directionZ());
        if (directionLength < 1.0E-4F) {
            return;
        }

        float directionX = telegraph.directionX() / directionLength;
        float directionZ = telegraph.directionZ() / directionLength;
        float progress = telegraph.progress();
        float growth = BASE_SCALE + SCALE_RANGE * progress;
        float forwardOffset = BASE_FORWARD_OFFSET + FORWARD_PUSH * progress;
        double lichX = Mth.lerp(event.getPartialTick(), lich.xOld, lich.getX());
        double lichY = Mth.lerp(event.getPartialTick(), lich.yOld, lich.getY());
        double lichZ = Mth.lerp(event.getPartialTick(), lich.zOld, lich.getZ());
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(
                telegraph.originX() - lichX + directionX * forwardOffset,
                telegraph.originY() - lichY + lich.getBbHeight() * 0.5F,
                telegraph.originZ() - lichZ + directionZ * forwardOffset);
        poseStack.mulPose(Axis.YP.rotationDegrees(
                (float) (Mth.atan2(directionX, -directionZ) * Mth.RAD_TO_DEG)));
        poseStack.translate(0.5F, 0.5F, 0.0F);
        poseStack.scale(SHIELD_WIDTH * growth, -SHIELD_HEIGHT * growth, growth);
        poseStack.translate(-0.5F, -0.5F, -0.7F);

        BakedModel model = Minecraft.getInstance().getModelManager()
                .getModel(ShieldLayer.LOC);
        VertexConsumer consumer = event.getMultiBufferSource()
                .getBuffer(Sheets.translucentCullBlockSheet());
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        renderShieldQuads(itemRenderer, poseStack, consumer, model, lich, null);
        for (Direction direction : Direction.values()) {
            renderShieldQuads(itemRenderer, poseStack, consumer, model, lich, direction);
        }
        poseStack.popPose();
    }

    private static void renderShieldQuads(
            ItemRenderer itemRenderer, PoseStack poseStack, VertexConsumer consumer,
            BakedModel model, Lich lich, Direction direction) {
        itemRenderer.renderQuadList(
                poseStack,
                consumer,
                model.getQuads(
                        null,
                        direction,
                        lich.getRandom(),
                        ModelData.EMPTY,
                        Sheets.translucentCullBlockSheet()),
                ItemStack.EMPTY,
                15728880,
                OverlayTexture.NO_OVERLAY);
    }
}