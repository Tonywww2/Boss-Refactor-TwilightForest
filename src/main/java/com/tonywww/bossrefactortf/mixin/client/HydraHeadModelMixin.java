package com.tonywww.bossrefactortf.mixin.client;

import com.tonywww.bossrefactortf.client.HydraHeadAnimation;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.model.entity.HydraHeadModel;
import twilightforest.entity.boss.HydraHead;

@Mixin(value = HydraHeadModel.class, remap = false)
public abstract class HydraHeadModelMixin {
    @Shadow
    @Final
    private ModelPart head;
    @Shadow
    @Final
    private ModelPart jaw;
    @Unique
    private float bossRefactorTF$baseHeadX;
    @Unique
    private float bossRefactorTF$baseHeadY;
    @Unique
    private float bossRefactorTF$baseHeadZ;
    @Unique
    private float bossRefactorTF$baseHeadZRot;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bossRefactorTF$captureHydraHeadPose(
            ModelPart root, CallbackInfo callback) {
        bossRefactorTF$baseHeadX = head.x;
        bossRefactorTF$baseHeadY = head.y;
        bossRefactorTF$baseHeadZ = head.z;
        bossRefactorTF$baseHeadZRot = head.zRot;
    }

    @Inject(
            method = "prepareMobModel(Ltwilightforest/entity/boss/HydraHead;FFF)V",
            at = @At("TAIL"))
    private void bossRefactorTF$animateHydraHead(
            HydraHead headEntity, float limbSwing, float limbSwingAmount,
            float partialTick, CallbackInfo callback) {
        HydraHeadAnimation.apply(
                headEntity, head, jaw,
                bossRefactorTF$baseHeadX,
                bossRefactorTF$baseHeadY,
                bossRefactorTF$baseHeadZ,
                bossRefactorTF$baseHeadZRot,
                partialTick);
    }
}