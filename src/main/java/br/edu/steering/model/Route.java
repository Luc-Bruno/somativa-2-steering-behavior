package br.edu.steering.model;

import br.edu.steering.model.math.Vec2;

import java.util.List;

/** Polilinha aberta, com projeção no segmento mais próximo e distância acumulada. */
public final class Route {
    public record Projection(Vec2 point, double distanceAlong, double distance) {}

    private final List<Vec2> points;
    private final double[] accumulated;

    public Route(List<Vec2> points) {
        if (points == null || points.size() < 2)
            throw new IllegalArgumentException("A rota exige dois pontos");
        this.points = List.copyOf(points);
        accumulated = new double[points.size()];
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).distance(points.get(i - 1)) < 1) {
                throw new IllegalArgumentException("Pontos consecutivos devem ser distintos");
            }
            accumulated[i] = accumulated[i - 1] + points.get(i).distance(points.get(i - 1));
        }
    }

    public List<Vec2> points() {
        return points;
    }

    public double length() {
        return accumulated[accumulated.length - 1];
    }

    public Vec2 end() {
        return points.get(points.size() - 1);
    }

    public Vec2 pointAt(double distance) {
        double d = Vec2.clamp(distance, 0, length());
        for (int i = 1; i < points.size(); i++) {
            if (d <= accumulated[i]) {
                double fraction = (d - accumulated[i - 1]) / (accumulated[i] - accumulated[i - 1]);
                return points.get(i - 1)
                        .add(points.get(i).subtract(points.get(i - 1)).multiply(fraction));
            }
        }
        return end();
    }

    public Projection project(Vec2 position) {
        Projection nearest = null;
        for (int i = 1; i < points.size(); i++) {
            Vec2 start = points.get(i - 1);
            Vec2 segment = points.get(i).subtract(start);
            double t =
                    Vec2.clamp(
                            position.subtract(start).dot(segment) / segment.lengthSquared(), 0, 1);
            Vec2 point = start.add(segment.multiply(t));
            double distance = position.distance(point);
            if (nearest == null || distance < nearest.distance()) {
                nearest =
                        new Projection(point, accumulated[i - 1] + segment.length() * t, distance);
            }
        }
        return nearest;
    }
}
