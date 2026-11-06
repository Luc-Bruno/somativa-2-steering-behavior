package br.edu.steering.model;

import br.edu.steering.model.math.Vec2;

/** O raio é o volume físico usado na detecção e no desvio. */
public record Obstacle(Vec2 position, double radius, Kind kind) {
    public enum Kind {
        TREE,
        GARDEN,
        FOUNTAIN,
        BENCH
    }

    public Obstacle {
        if (position == null || kind == null || radius <= 0 || !Double.isFinite(radius)) {
            throw new IllegalArgumentException("Obstáculo inválido");
        }
    }
}
