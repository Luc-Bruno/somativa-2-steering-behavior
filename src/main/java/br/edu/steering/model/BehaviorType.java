package br.edu.steering.model;

public enum BehaviorType {
    SEEK("Seek", "Buscar o alvo"),
    FLEE("Flee", "Fugir da ameaça"),
    ARRIVE("Arrive", "Chegar e desacelerar"),
    WANDER("Wander", "Explorar com suavidade"),
    AVOIDANCE("Obstacle Avoidance", "Antecipar obstáculos"),
    PATH("Path Following", "Acompanhar a rota");

    private final String title;
    private final String description;

    BehaviorType(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }
}
