package br.edu.steering.model.behavior;

import br.edu.steering.model.Agent;
import br.edu.steering.model.BehaviorType;
import br.edu.steering.model.Route;
import br.edu.steering.model.World;
import br.edu.steering.model.math.Vec2;

public final class PathFollowing implements SteeringBehavior {
    @Override
    public Vec2 calculate(Agent agent, World world, double dt) {
        Route route = agent.route();
        Route.Projection current = route.project(agent.position());
        Route.Projection predicted =
                route.project(agent.position().add(agent.velocity().multiply(0.35)));
        agent.setPathProgress(current.distanceAlong());
        double lookAhead = 42 + agent.speed() * 0.25;
        double distance =
                Math.max(agent.pathProgress() + lookAhead, predicted.distanceAlong() + 20);
        Vec2 target = route.pointAt(distance);
        agent.setPathDebug(predicted.point(), target);
        double remaining = route.length() - agent.pathProgress();
        if (remaining < 150) {
            // Na combinação, transfere progressivamente a chegada para Arrive.
            if (agent.hasBehavior(BehaviorType.ARRIVE)) {
                double fade = Vec2.clamp(remaining / 150, 0, 1);
                return SteeringBehavior.arriveAt(agent, route.end()).multiply(fade);
            }
            return SteeringBehavior.arriveAt(agent, route.end());
        }
        return SteeringBehavior.towardVelocity(
                agent, target.subtract(agent.position()).normalized().multiply(agent.maxSpeed()));
    }
}
