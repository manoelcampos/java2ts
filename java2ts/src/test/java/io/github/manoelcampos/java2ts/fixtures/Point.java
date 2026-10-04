package io.github.manoelcampos.java2ts.fixtures;

/** A class with final fields (like a Lombok {@code @Value} class), which become read-only properties. */
public class Point {
    private final int x;
    private int y;

    /** A public final field. */
    public final String label;

    public Point(final int x, final int y, final String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    /** A getter without backing field is not read-only. */
    public double getDistance() { return Math.sqrt(x * x + y * y); }
}
