package br.edu.steering.model.behavior;

import br.edu.steering.model.Agent;
import br.edu.steering.model.World;
import br.edu.steering.model.math.Vec2;

public final class Flee implements SteeringBehavior {
    public static final double PERCEPTION_RADIUS = 190;

    @Override
    public Vec2 calculate(Agent agent, World world, double dt) {
        Vec2 away = agent.position().subtract(agent.target());
        if (away.length() >= PERCEPTION_RADIUS)
            return SteeringBehavior.towardVelocity(agent, Vec2.ZERO);
        if (away.lengthSquared() < 1e-9) away = agent.heading();
        return SteeringBehavior.towardVelocity(agent, away.normalized().multiply(agent.maxSpeed()));
    }
}
