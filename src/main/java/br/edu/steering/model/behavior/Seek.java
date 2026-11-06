package br.edu.steering.model.behavior;

import br.edu.steering.model.Agent;
import br.edu.steering.model.World;
import br.edu.steering.model.math.Vec2;

public final class Seek implements SteeringBehavior {
    @Override
    public Vec2 calculate(Agent agent, World world, double dt) {
        Vec2 desired =
                agent.target().subtract(agent.position()).normalized().multiply(agent.maxSpeed());
        return SteeringBehavior.towardVelocity(agent, desired);
    }
}
