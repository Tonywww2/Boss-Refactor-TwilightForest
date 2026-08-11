package com.tonywww.bossrefactortf.mixin.boss;

import com.tonywww.bossrefactortf.config.BossRefactorTwilightForestConfig;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraph;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphAccess;
import com.tonywww.bossrefactortf.telegraph.AttackTelegraphShape;
import net.minecraft.world.entity.projectile.LargeFireball;
import org.spongepowered.asm.mixin.Mixin;
import twilightforest.entity.boss.UrGhast;

@Mixin(LargeFireball.class)
public abstract class LargeFireballTelegraphMixin
        implements AttackTelegraphAccess {
    @Override
    public AttackTelegraph bossRefactorTF$getAttackTelegraph() {
        LargeFireball fireball = (LargeFireball) (Object) this;
        if (!BossRefactorTwilightForestConfig.UR_GHAST.enabled.get()
                || !(fireball.getOwner() instanceof UrGhast)) {
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
}