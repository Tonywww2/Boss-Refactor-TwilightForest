package com.tonywww.bossrefactortf.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import twilightforest.client.renderer.entity.newmodels.NewHydraHeadRenderer;

@Mixin(value = NewHydraHeadRenderer.class, remap = false)
public abstract class NewHydraHeadRendererMixin {
    @ModifyConstant(
            method = "getHeadObject",
            constant = @Constant(intValue = 7),
            require = 1,
            remap = false)
    private static int bossRefactorTF$findExpandedHead(int original) {
        return 9;
    }
}