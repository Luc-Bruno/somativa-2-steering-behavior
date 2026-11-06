package br.edu.steering.model.behavior;

import br.edu.steering.model.Agent;
import br.edu.steering.model.World;
import br.edu.steering.model.math.Vec2;

@FunctionalInterface
public interface SteeringBehavior {
    Vec2 calculate(Agent agent, World world, double dt);

    /** Tempo de resposta impede que a mudança de velocidade seja instantânea. */
    static Vec2 towardVelocity(Agent agent, Vec2 desired) {
        return desired.subtract(agent.velocity())
                .multiply(agent.mass() / 0.45)
                .limit(agent.maxForce());
    }

    static Vec2 arriveAt(Agent agent, Vec2 target) {
        Vec2 offset = target.subtract(agent.position());
        double distance = offset.length();
        double speed = distance < 3 ? 0 : agent.maxSpeed() * Math.min(1, distance / 125);
        return towardVelocity(agent, offset.normalized().multiply(speed));
    }
}
