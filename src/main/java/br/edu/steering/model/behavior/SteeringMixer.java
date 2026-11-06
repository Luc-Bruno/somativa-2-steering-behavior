package br.edu.steering.model.behavior;

import br.edu.steering.model.Agent;
import br.edu.steering.model.BehaviorType;
import br.edu.steering.model.World;
import br.edu.steering.model.math.Vec2;

import java.util.EnumMap;
import java.util.Map;

/** Soma ponderada das forças, limitada uma única vez na integração do agente. */
public final class SteeringMixer {
    private final Map<BehaviorType, SteeringBehavior> behaviors = new EnumMap<>(BehaviorType.class);

    public SteeringMixer() {
        behaviors.put(BehaviorType.SEEK, new Seek());
        behaviors.put(BehaviorType.FLEE, new Flee());
        behaviors.put(BehaviorType.ARRIVE, new Arrive());
        behaviors.put(BehaviorType.WANDER, new Wander());
        behaviors.put(BehaviorType.AVOIDANCE, new ObstacleAvoidance());
        behaviors.put(BehaviorType.PATH, new PathFollowing());
    }

    public Vec2 calculate(Agent agent, World world, double dt) {
        agent.beginForces();
        Vec2 total = boundaryForce(agent);
        // Calcula primeiro a intenção completa, incluindo Path Following e a borda.
        for (BehaviorType type : BehaviorType.values()) {
            if (type == BehaviorType.AVOIDANCE || agent.weight(type) <= 0) continue;
            Vec2 weighted =
                    behaviors.get(type).calculate(agent, world, dt).multiply(agent.weight(type));
            agent.recordForce(type, weighted);
            total = total.add(weighted);
        }
        agent.setPreferredVelocity(
                agent.velocity().add(total.multiply(.45 / agent.mass())).limit(agent.maxSpeed()));
        if (agent.weight(BehaviorType.AVOIDANCE) > 0) {
            Vec2 avoid =
                    behaviors
                            .get(BehaviorType.AVOIDANCE)
                            .calculate(agent, world, dt)
                            .multiply(agent.weight(BehaviorType.AVOIDANCE));
            agent.recordForce(BehaviorType.AVOIDANCE, avoid);
            total = total.add(avoid);
        }
        return total;
    }

    /** Contenção suave do mapa, auxiliar e independente dos seis comportamentos. */
    private Vec2 boundaryForce(Agent agent) {
        double margin = 75;
        Vec2 p = agent.position();
        double x =
                p.x() < margin
                        ? (margin - p.x()) / margin
                        : p.x() > World.WIDTH - margin
                                ? (World.WIDTH - margin - p.x()) / margin
                                : 0;
        double y =
                p.y() < margin
                        ? (margin - p.y()) / margin
                        : p.y() > World.HEIGHT - margin
                                ? (World.HEIGHT - margin - p.y()) / margin
                                : 0;
        return new Vec2(x, y).multiply(agent.maxForce() * 5);
    }
}
