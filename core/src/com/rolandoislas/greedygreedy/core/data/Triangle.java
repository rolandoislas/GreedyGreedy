package com.rolandoislas.greedygreedy.core.data;

/**
 * Barycentric coordinate - https://stackoverflow.com/a/25346777
 */
public class Triangle {
    private Triangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        this.x3 = x3;
        this.y3 = y3;
        y23 = y2 - y3;
        x32 = x3 - x2;
        y31 = y3 - y1;
        x13 = x1 - x3;
        det = y23 * x13 - x32 * y31;
        minD = Math.min(det, 0);
        maxD = Math.max(det, 0);
    }

    public Triangle(float[] size) {
        this(size[0], size[1], size[2], size[3], size[4], size[5]);
    }

    public boolean contains(double x, double y) {
        double dx = x - x3;
        double dy = y - y3;
        double a = y23 * dx + x32 * dy;
        if (a < minD || a > maxD)
            return false;
        double b = y31 * dx + x13 * dy;
        if (b < minD || b > maxD)
            return false;
        double c = det - a - b;
        return !(c < minD) && !(c > maxD);
    }

    private final double x3, y3;
    private final double y23, x32, y31, x13;
    private final double det, minD, maxD;
}
