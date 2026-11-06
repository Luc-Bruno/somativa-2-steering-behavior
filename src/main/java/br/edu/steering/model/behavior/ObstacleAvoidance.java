package br.edu.steering.model.behavior;

import br.edu.steering.model.Agent;
import br.edu.steering.model.Obstacle;
import br.edu.steering.model.World;
import br.edu.steering.model.math.Vec2;

/** Sensor com volume: interseção de um corredor à frente com círculos expandidos. */
public final class ObstacleAvoidance implements SteeringBehavior {
    @Override
    public Vec2 calculate(Agent agent, World world, double dt) {
        Vec2 forward = agent.heading();
        Vec2 side = forward.perpendicular();
        double lookAhead = 55 + agent.speed() * 0.95;
        Obstacle nearest = null;
        double nearestHit = Double.POSITIVE_INFINITY;
        double nearestSide = 0;
        for (Obstacle obstacle : world.obstacles()) {
            Vec2 relative = obstacle.position().subtract(agent.position());
            double along = relative.dot(forward);
            double across = relative.dot(side);
            double expanded = obstacle.radius() + Agent.RADIUS + 18;
            if (along < -expanded * 0.3
                    || along > lookAhead + expanded
                    || Math.abs(across) >= expanded) continue;
            double hit = along - Math.sqrt(expanded * expanded - across * across);
            if (hit <= lookAhead && hit < nearestHit) {
                nearest = obstacle;
                nearestHit = Math.max(0, hit);
                nearestSide = across;
            }
        }
        agent.setSensor(
                agent.position().add(forward.multiply(lookAhead)),
                nearest == null ? null : nearest.position());
        if (nearest == null) return Vec2.ZERO;
        double direction = agent.avoidanceDirection(nearest.position(), nearestSide);
        double urgency = 1 - Vec2.clamp(nearestHit / lookAhead, 0, 1);
        Vec2 lateral = side.multiply(direction * agent.maxForce() * (0.18 + urgency * 0.95));
        Vec2 braking = forward.multiply(-agent.maxForce() * urgency * 0.4);
        return lateral.add(braking).limit(agent.maxForce());
    }
}
