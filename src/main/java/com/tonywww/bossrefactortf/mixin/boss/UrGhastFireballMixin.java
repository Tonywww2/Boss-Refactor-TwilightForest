package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.boss.urghast.UrGhastCombat;
import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphAccess;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.projectile.UrGhastFireball;

@Mixin(value = UrGhastFireball.class, remap = false)
public abstract class UrGhastFireballMixin extends LargeFireball
        implements AttackTelegraphAccess {
    protected UrGhastFireballMixin(
            EntityType<? extends LargeFireball> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = {"onHit", "m_6532_"},
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void bossRefactorTF$replaceExplosion(
            HitResult hitResult, CallbackInfo callback) {
        if (UrGhastCombat.explodeFireball(
                bossRefactorTF$self(), hitResult)) {
            callback.cancel();
        }
    }

    @Override
    public AttackTelegraph bossRefactorTF$getAttackTelegraph() {
        if (!BossRefactorTwilightForestConfig.UR_GHAST.enabled.get()) {
            return AttackTelegraph.NONE;
        }
        return new AttackTelegraph(
                AttackTelegraphShape.BOX,
                0.0F, 1.0F, 0.0F, 0.0F,
                BossRefactorTwilightForestConfig.UR_GHAST
                        .fireballExplosionRadius.get().floatValue(),
                1.0F);
    }

    @Override
    public void bossRefactorTF$setAttackTelegraph(AttackTelegraph telegraph) {
    }

    @Unique
    private UrGhastFireball bossRefactorTF$self() {
        return (UrGhastFireball) (Object) this;
    }
}