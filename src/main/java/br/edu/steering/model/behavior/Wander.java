package br.edu.steering.model.behavior;

import br.edu.steering.model.Agent;
import br.edu.steering.model.World;
import br.edu.steering.model.math.Vec2;

public final class Wander implements SteeringBehavior {
    @Override
    public Vec2 calculate(Agent agent, World world, double dt) {
        double angle = agent.advanceWander(dt);
        Vec2 circle = agent.position().add(agent.heading().multiply(65));
        Vec2 target = circle.add(Vec2.fromAngle(agent.heading().angle() + angle).multiply(38));
        agent.setPathDebug(circle, target);
        return SteeringBehavior.towardVelocity(
                agent, target.subtract(agent.position()).normalized().multiply(agent.maxSpeed()));
    }
}
