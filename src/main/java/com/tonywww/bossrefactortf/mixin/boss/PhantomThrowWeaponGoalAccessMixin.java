package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.knightphantom.PhantomThrowWeaponGoalAccess;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import twilightforest.entity.ai.goal.PhantomThrowWeaponGoal;

@Mixin(value = PhantomThrowWeaponGoal.class, remap = false)
public interface PhantomThrowWeaponGoalAccessMixin
        extends PhantomThrowWeaponGoalAccess {
    @Override
    @Invoker("launchAxeAt")
    void bossRefactorTF$launchAxeAt(Entity target);

    @Override
    @Invoker("launchPicks")
    void bossRefactorTF$launchPicks();
}