package br.edu.steering.model.behavior;

import br.edu.steering.model.Agent;
import br.edu.steering.model.Obstacle;
import br.edu.steering.model.World;
import br.edu.steering.model.math.Vec2;

/** Desvio por tangentes de círculos expandidos, com espaço de conforto e memória do lado. */
public final class ObstacleAvoidance implements SteeringBehavior {
    public static final double SAFETY_MARGIN = 36;
    private static final double COMFORT_BAND = 24;
    public static final double TARGET_CLEARANCE = SAFETY_MARGIN + COMFORT_BAND + 8;

    @Override
    public Vec2 calculate(Agent agent, World world, double dt) {
        Vec2 preferred = agent.preferredVelocity();
        Vec2 intent = preferred.length() > 1 ? preferred.normalized() : agent.heading();
        Vec2 forward = agent.speed() > 15 ? agent.heading() : intent;
        double lookAhead =
                80 + agent.speed() * .9 + agent.speed() * agent.speed() / (2 * agent.maxForce());
        Obstacle nearest = null;
        double nearestHit = Double.POSITIVE_INFINITY;
        Vec2 separation = Vec2.ZERO;

        for (Obstacle obstacle : world.obstacles()) {
            Vec2 offset = agent.position().subtract(obstacle.position());
            double distance = offset.length();
            double radius = obstacle.radius() + Agent.RADIUS + SAFETY_MARGIN;
            Vec2 normal = distance > 1e-6 ? offset.normalized() : intent.perpendicular();
            double pressure = Vec2.clamp((radius + COMFORT_BAND - distance) / COMFORT_BAND, 0, 2);
            separation =
                    separation.add(normal.multiply(agent.maxForce() * pressure * pressure * .75));

            double hit = Math.min(rayHit(offset, forward, radius), rayHit(offset, intent, radius));
            if (distance < radius + 8 && intent.dot(normal) < .2) hit = 0;
            if (hit <= lookAhead && hit < nearestHit) {
                nearest = obstacle;
                nearestHit = hit;
            }
        }
        agent.setSensor(
                agent.position().add(forward.multiply(lookAhead)),
                nearest == null ? null : nearest.position());
        // Sem intenção de movimento, não inventa uma manobra só pela orientação do corpo.
        // Assim Arrive pode permanecer parado em um destino livre.
        if (preferred.length() < 1 && agent.speed() < 1) {
            agent.clearAvoidance();
            return separation.limit(agent.maxForce());
        }
        if (nearest == null) {
            agent.clearAvoidance();
            return separation.limit(agent.maxForce());
        }

        Vec2 normal = agent.position().subtract(nearest.position()).normalized();
        if (normal.lengthSquared() < 1e-6) normal = intent.perpendicular();
        double radius = nearest.radius() + Agent.RADIUS + SAFETY_MARGIN;
        double distance = agent.position().distance(nearest.position());
        Vec2 positive = passingDirection(normal, distance, radius, 1);
        Vec2 negative = passingDirection(normal, distance, radius, -1);
        double positiveCost = cost(positive, agent, nearest, world, intent);
        double negativeCost = cost(negative, agent, nearest, world, intent);
        double preferredSide = positiveCost < negativeCost ? 1 : -1;
        double side = agent.avoidanceDirection(nearest.position(), preferredSide);
        // Se o lado escolhido levar à borda, permite trocar antes de entrar num canto.
        Vec2 chosen = side > 0 ? positive : negative;
        Vec2 alternative = side > 0 ? negative : positive;
        if (boundaryCost(agent.position().add(chosen.multiply(100))) > .5
                && cost(alternative, agent, nearest, world, intent) + .5
                        < cost(chosen, agent, nearest, world, intent)) {
            side = -side;
            agent.changeAvoidanceSide(side);
            chosen = alternative;
        }
        double passingSpeed =
                Math.min(agent.maxSpeed(), Math.max(agent.maxSpeed() * .75, preferred.length()));
        Vec2 desired = chosen.multiply(passingSpeed);
        return SteeringBehavior.towardVelocity(agent, desired)
                .add(separation)
                .limit(agent.maxForce());
    }

    private static double rayHit(Vec2 offset, Vec2 direction, double radius) {
        Vec2 toCenter = offset.multiply(-1);
        double along = toCenter.dot(direction);
        double acrossSquared = Math.max(0, toCenter.lengthSquared() - along * along);
        if (along <= 0 || acrossSquared >= radius * radius) return Double.POSITIVE_INFINITY;
        return Math.max(0, along - Math.sqrt(radius * radius - acrossSquared));
    }

    /** Direção da tangente externa; perto do círculo acrescenta afastamento radial. */
    private static Vec2 passingDirection(Vec2 normal, double distance, double radius, double side) {
        double sine = Vec2.clamp(radius / Math.max(distance, radius), 0, 1);
        double cosine = Math.sqrt(Math.max(0, 1 - sine * sine));
        Vec2 tangent = normal.multiply(-cosine).add(normal.perpendicular().multiply(side * sine));
        double recovery = Vec2.clamp((radius + COMFORT_BAND - distance) / COMFORT_BAND, 0, 2);
        return tangent.add(normal.multiply(recovery)).normalized();
    }

    private static double cost(
            Vec2 direction, Agent agent, Obstacle current, World world, Vec2 intent) {
        Vec2 ahead = agent.position().add(direction.multiply(110));
        double result = -direction.dot(intent) + boundaryCost(ahead) * 4;
        for (Obstacle other : world.obstacles()) {
            if (other == current) continue;
            double radius = other.radius() + Agent.RADIUS + SAFETY_MARGIN;
            double pressure = Math.max(0, 1 - ahead.distance(other.position()) / radius);
            result += pressure * pressure * 5;
        }
        return result;
    }

    private static double boundaryCost(Vec2 point) {
        double gap =
                Math.min(
                        Math.min(point.x() - 75, World.WIDTH - 75 - point.x()),
                        Math.min(point.y() - 75, World.HEIGHT - 75 - point.y()));
        return Math.max(0, -gap / 50);
    }
}
