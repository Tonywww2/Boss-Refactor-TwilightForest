package com.tonywww.bossrefactortf.telegraph;

public final class AttackTelegraphSelfTest {
    private AttackTelegraphSelfTest() {
    }

    public static void main(String[] args) {
        checkClose(AttackTelegraph.windupProgress(0, 20), 0.0,
                "windup must start at zero");
        checkClose(AttackTelegraph.windupProgress(10, 20), 0.5,
                "windup must report fractional progress");
        checkClose(AttackTelegraph.windupProgress(30, 20), 1.0,
                "windup progress must clamp at one");
        checkClose(AttackTelegraph.windupProgress(0, 0), 1.0,
                "zero-tick windup must complete immediately");

        AttackTelegraph low = new AttackTelegraph(
                AttackTelegraphShape.CIRCLE,
                0.0F, 0.0F, 0.0F, 0.0F, 3.0F, -1.0F);
        AttackTelegraph high = low.withProgress(2.0F);
        checkClose(low.progress(), 0.0, "record construction must clamp low progress");
        checkClose(high.progress(), 1.0, "withProgress must clamp high progress");

        AttackTelegraph locked = new AttackTelegraph(
                AttackTelegraphShape.ARC,
                1.0F, 2.0F, 3.0F,
                0.0F, 1.0F, 8.0F, 45.0F, 0.0F, 0.25F);
        check(locked.hasLockedOrigin(), "finite coordinates must lock the origin");
        check(!AttackTelegraph.NONE.hasLockedOrigin(),
                "the NONE telegraph must follow its entity");
        check(AttackTelegraphShape.byId(-1) == AttackTelegraphShape.NONE,
                "negative shape ids must fall back to NONE");
        check(AttackTelegraphShape.byId(999) == AttackTelegraphShape.NONE,
                "large shape ids must fall back to NONE");
        check(AttackTelegraphShape.byId(AttackTelegraphShape.ARC.ordinal())
                        == AttackTelegraphShape.ARC,
                "valid shape ids must round-trip");
        check(AttackTelegraphShape.byId(AttackTelegraphShape.RING.ordinal())
                        == AttackTelegraphShape.RING,
                "ring shape ids must round-trip");
        check(AttackTelegraphShape.byId(AttackTelegraphShape.BOX.ordinal())
                        == AttackTelegraphShape.BOX,
                "box shape ids must round-trip");
        check(AttackTelegraphShape.byId(AttackTelegraphShape.CYLINDER.ordinal())
                        == AttackTelegraphShape.CYLINDER,
                "cylinder shape ids must round-trip");

        AttackTelegraph ring = new AttackTelegraph(
                AttackTelegraphShape.RING,
                0.0F, 0.0F, 0.0F,
                0.0F, 1.0F, 0.0F, 1.5F, 12.0F, 0.5F);
        check(AttackTelegraphNbt.decode(AttackTelegraphNbt.encode(ring)).equals(ring),
                "telegraphs must round-trip through synchronized NBT");
        AttackTelegraph cylinder = new AttackTelegraph(
                AttackTelegraphShape.CYLINDER,
                4.0F, 5.0F, 6.0F,
                0.0F, 1.0F, 24.0F, 0.0F, 6.0F, 0.5F);
        check(AttackTelegraphNbt.decode(
                        AttackTelegraphNbt.encode(cylinder)).equals(cylinder),
                "three-dimensional telegraphs must round-trip through NBT");

        check(!ParryIndicatorStyle.isVisible(false, locked),
                "non-parryable attacks must not show the SenDimS warning");
        check(!ParryIndicatorStyle.isVisible(true, AttackTelegraph.NONE),
                "missing telegraphs must not show the SenDimS warning");
        check(ParryIndicatorStyle.isVisible(true, locked),
                "parryable telegraphed attacks must show the SenDimS warning");
        checkClose(ParryIndicatorStyle.red(-1.0F), 1.0,
                "warning red must clamp low progress");
        checkClose(ParryIndicatorStyle.red(2.0F), 0.3,
                "warning red must clamp high progress");
        checkClose(ParryIndicatorStyle.greenBlue(2.0F), 0.1,
                "warning green and blue must clamp high progress");
    }

    private static void checkClose(double actual, double expected, String message) {
        check(Math.abs(actual - expected) < 1.0E-6,
                message + ": expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}