package br.edu.steering.model.behavior;

import br.edu.steering.model.Agent;
import br.edu.steering.model.World;
import br.edu.steering.model.math.Vec2;

public final class Arrive implements SteeringBehavior {
    @Override
    public Vec2 calculate(Agent agent, World world, double dt) {
        if (agent.hasBehavior(br.edu.steering.model.BehaviorType.PATH)) {
            if (agent.route().length() - agent.pathProgress() > 150) return Vec2.ZERO;
            return SteeringBehavior.arriveAt(agent, agent.route().end());
        }
        return SteeringBehavior.arriveAt(agent, agent.target());
    }
}
