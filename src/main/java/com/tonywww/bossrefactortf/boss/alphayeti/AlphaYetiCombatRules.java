package com.tonywww.bossrefactortf.boss.alphayeti;

public final class AlphaYetiCombatRules {
    public static final int FATIGUE_THRESHOLD = 3;
    public static final int IMBALANCE_THRESHOLD = 3;
    private static final double FALLING_ICE_GRAVITY = 0.04;
    private static final double FALLING_ICE_DRAG = 0.98;
    private static final AlphaYetiSkill[] ALL_SKILLS = {
            AlphaYetiSkill.RAMPAGE,
            AlphaYetiSkill.PURSUIT,
            AlphaYetiSkill.STOMP
    };

    private AlphaYetiCombatRules() {
    }

    public static int fatigueAfterSkill(int currentFatigue) {
        return Math.max(0, currentFatigue) + 1;
    }

    public static int fatigueAfterPursuit(
            int currentFatigue, boolean stoppedByImbalance) {
        int updatedFatigue = fatigueAfterSkill(currentFatigue);
        return stoppedByImbalance ? updatedFatigue + 1 : updatedFatigue;
    }

    public static boolean shouldRest(int fatigue) {
        return fatigue >= FATIGUE_THRESHOLD;
    }

    public static boolean reachesImbalanceThreshold(int imbalance) {
        return imbalance >= IMBALANCE_THRESHOLD;
    }

    public static boolean isSecondPhase(float health, float maximumHealth) {
        return maximumHealth > 0.0F && health < maximumHealth * 0.5F;
    }

    public static int restTicks(int configuredTicks, boolean secondPhase) {
        int nonNegativeTicks = Math.max(0, configuredTicks);
        return secondPhase ? nonNegativeTicks / 2 : nonNegativeTicks;
    }

    public static double pursuitDamage(double attackDamage) {
        return Math.max(0.0, attackDamage);
    }

    public static double stompDamage(double attackDamage) {
        return Math.max(0.0, attackDamage) * 1.5;
    }

    public static double fallingIceDamage(double attackDamage) {
        return Math.max(0.0, attackDamage);
    }

    public static int cruiseTicks(int minimumTicks, int maximumTicks, double roll) {
        int minimum = Math.max(0, minimumTicks);
        int maximum = Math.max(minimum, maximumTicks);
        double clampedRoll = Math.max(0.0, Math.min(Math.nextDown(1.0), roll));
        return minimum + (int) Math.floor(clampedRoll * (maximum - minimum + 1));
    }

    public static AlphaYetiSkill nextSkill(
            AlphaYetiSkill previousSkill, int randomIndex) {
        AlphaYetiSkill previous = previousSkill == null
                ? AlphaYetiSkill.NONE
                : previousSkill;
        int candidateCount = previous == AlphaYetiSkill.NONE ? 3 : 2;
        int selected = Math.floorMod(randomIndex, candidateCount);
        for (AlphaYetiSkill skill : ALL_SKILLS) {
            if (skill == previous) {
                continue;
            }
            if (selected-- == 0) {
                return skill;
            }
        }
        return AlphaYetiSkill.RAMPAGE;
    }

    public static double fallingIceRingRadius(
            int ringIndex, int ringCount, double maximumRadius) {
        int count = Math.max(1, ringCount);
        int index = Math.max(1, Math.min(count, ringIndex));
        return Math.max(0.0, maximumRadius) * index / count;
    }

    public static double fallingIceRingHalfWidth(
            int ringCount, double maximumRadius) {
        return Math.max(0.0, maximumRadius) / Math.max(1, ringCount) * 0.5;
    }

    public static boolean isInsideRing(
            double distance, double radius, double halfWidth) {
        double nonNegativeDistance = Math.max(0.0, distance);
        double inner = Math.max(0.0, radius - Math.max(0.0, halfWidth));
        double outer = Math.max(inner, radius + Math.max(0.0, halfWidth));
        return nonNegativeDistance >= inner && nonNegativeDistance <= outer;
    }

    public static boolean isIntervalPulse(
            int elapsedTicks, int intervalTicks) {
        int elapsed = Math.max(0, elapsedTicks);
        int interval = Math.max(1, intervalTicks);
        return (elapsed + 1) % interval == 0;
    }

    public static double fallingIceInitialVelocity(
            double fallHeight, int impactTicks) {
        double height = Math.max(0.0, fallHeight);
        int ticks = Math.max(1, impactTicks);
        double dragPower = Math.pow(FALLING_ICE_DRAG, ticks);
        double terminalPostTickVelocity = FALLING_ICE_GRAVITY
                * FALLING_ICE_DRAG / (1.0 - FALLING_ICE_DRAG);
        double terminalMovement = FALLING_ICE_GRAVITY
                / (1.0 - FALLING_ICE_DRAG);
        return (terminalMovement * ticks - height)
                * (1.0 - FALLING_ICE_DRAG) / (1.0 - dragPower)
                - terminalPostTickVelocity;
    }

    public static double fallingIceTravelDistance(
            double initialVelocity, int ticks) {
        double velocity = initialVelocity;
        double distance = 0.0;
        for (int tick = 0; tick < Math.max(0, ticks); tick++) {
            velocity -= FALLING_ICE_GRAVITY;
            distance -= velocity;
            velocity *= FALLING_ICE_DRAG;
        }
        return distance;
    }
}