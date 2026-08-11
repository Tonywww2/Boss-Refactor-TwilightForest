package com.tonywww.bossrefactortf.telegraph;

import net.minecraft.nbt.CompoundTag;

public final class AttackTelegraphNbt {
    private AttackTelegraphNbt() {
    }

    public static CompoundTag encode(AttackTelegraph telegraph) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Shape", telegraph.shape().ordinal());
        tag.putFloat("OriginX", telegraph.originX());
        tag.putFloat("OriginY", telegraph.originY());
        tag.putFloat("OriginZ", telegraph.originZ());
        tag.putFloat("DirectionX", telegraph.directionX());
        tag.putFloat("DirectionZ", telegraph.directionZ());
        tag.putFloat("Length", telegraph.length());
        tag.putFloat("Width", telegraph.width());
        tag.putFloat("Radius", telegraph.radius());
        tag.putFloat("Progress", telegraph.progress());
        return tag;
    }

    public static AttackTelegraph decode(CompoundTag tag) {
        AttackTelegraphShape shape = AttackTelegraphShape.byId(tag.getInt("Shape"));
        if (shape == AttackTelegraphShape.NONE) {
            return AttackTelegraph.NONE;
        }
        return new AttackTelegraph(
                shape,
                tag.getFloat("OriginX"),
                tag.getFloat("OriginY"),
                tag.getFloat("OriginZ"),
                tag.getFloat("DirectionX"),
                tag.getFloat("DirectionZ"),
                tag.getFloat("Length"),
                tag.getFloat("Width"),
                tag.getFloat("Radius"),
                tag.getFloat("Progress"));
    }
}