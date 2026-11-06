package br.edu.steering.model.math;

/** Vetor imutável: operações nunca alteram os operandos. */
public record Vec2(double x, double y) {
    public static final Vec2 ZERO = new Vec2(0, 0);

    public Vec2 {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Componentes do vetor devem ser finitos");
        }
    }

    public Vec2 add(Vec2 other) {
        return new Vec2(x + other.x, y + other.y);
    }

    public Vec2 subtract(Vec2 other) {
        return new Vec2(x - other.x, y - other.y);
    }

    public Vec2 multiply(double scalar) {
        return new Vec2(x * scalar, y * scalar);
    }

    public double dot(Vec2 other) {
        return x * other.x + y * other.y;
    }

    public double lengthSquared() {
        return dot(this);
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double distance(Vec2 other) {
        return subtract(other).length();
    }

    public Vec2 perpendicular() {
        return new Vec2(-y, x);
    }

    public double angle() {
        return Math.atan2(y, x);
    }

    public Vec2 normalized() {
        double n = length();
        return n < 1e-9 ? ZERO : multiply(1 / n);
    }

    public Vec2 limit(double maximum) {
        return lengthSquared() > maximum * maximum ? normalized().multiply(maximum) : this;
    }

    public static Vec2 fromAngle(double angle) {
        return new Vec2(Math.cos(angle), Math.sin(angle));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
