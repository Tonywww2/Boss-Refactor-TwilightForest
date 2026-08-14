package com.tonywww.bossrefactortf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.boss.naga.NagaCombatAccess;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.entity.boss.Naga;

@Mod.EventBusSubscriber(
        modid = BossRefactorTwilightForest.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NagaIndicatorRenderer {
    private static final double MAXIMUM_DISTANCE_SQUARED = 64.0 * 64.0;

    private NagaIndicatorRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (!BossRefactorTwilightForestConfig.NAGA.enabled.get()
                || !(event.getEntity() instanceof Naga naga)
                || !(naga instanceof NagaCombatAccess access)
                || !access.bossRefactorTF$isStunImmunityVisible()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.player.distanceToSqr(naga)
                        > MAXIMUM_DISTANCE_SQUARED) {
            return;
        }

        Component label = Component.translatable(
                "label.bossrefactortf.naga.stun_immunity")
                .withStyle(ChatFormatting.GOLD);
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(0.0, naga.getBbHeight() + 0.75, 0.0);
        poseStack.mulPose(
                minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        Font font = minecraft.font;
        float x = -font.width(label) * 0.5F;
        int background = minecraft.options.getBackgroundColor(0.35F);
        font.drawInBatch(
                label, x, 0.0F, 0x55FFFFFF, false,
                poseStack.last().pose(), event.getMultiBufferSource(),
                Font.DisplayMode.SEE_THROUGH, background,
                event.getPackedLight());
        font.drawInBatch(
                label, x, 0.0F, 0xFFFFFFFF, false,
                poseStack.last().pose(), event.getMultiBufferSource(),
                Font.DisplayMode.NORMAL, 0,
                event.getPackedLight());
        poseStack.popPose();
    }
}