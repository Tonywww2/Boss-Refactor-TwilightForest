package com.tonywww.bossrefactortf.boss.naga;

import java.util.List;

public final class NagaBodyRules {
    private static final double EPSILON = 1.0E-9;

    private NagaBodyRules() {
    }

    public static boolean shouldResetTrail(
            double distanceSquared, double resetDistance) {
        return distanceSquared > resetDistance * resetDistance;
    }

    public static int trailCapacity(
            int segmentCount, double segmentSpacing,
            double sampleSpacing) {
        double requiredLength = Math.max(0, segmentCount) * segmentSpacing;
        return Math.max(2,
                (int) Math.ceil(requiredLength / sampleSpacing) + 3);
    }

    public static Point sampleTrail(
            Point head, List<Point> newestFirst, double distance) {
        if (newestFirst.isEmpty() || distance <= 0.0) {
            return head;
        }
        Point previous = head;
        double remaining = distance;
        for (Point point : newestFirst) {
            double sectionLength = previous.distanceTo(point);
            if (sectionLength > EPSILON && remaining <= sectionLength) {
                return previous.lerp(point, remaining / sectionLength);
            }
            remaining -= sectionLength;
            previous = point;
        }
        return previous;
    }

    public record Point(double x, double y, double z) {
        public double distanceTo(Point other) {
            double deltaX = other.x - x;
            double deltaY = other.y - y;
            double deltaZ = other.z - z;
            return Math.sqrt(deltaX * deltaX
                    + deltaY * deltaY
                    + deltaZ * deltaZ);
        }

        public Point lerp(Point other, double progress) {
            return new Point(
                    x + (other.x - x) * progress,
                    y + (other.y - y) * progress,
                    z + (other.z - z) * progress);
        }
    }
}