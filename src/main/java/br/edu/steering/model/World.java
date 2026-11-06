package br.edu.steering.model;

import br.edu.steering.model.behavior.SteeringMixer;
import br.edu.steering.model.math.Vec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Ambiente de testes. Cada cenário configura um agente com estado e alvo próprios. */
public final class World {
    public static final double WIDTH = 1080;
    public static final double HEIGHT = 720;
    private final List<Agent> agents = new ArrayList<>();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<Vec2> draftRoute = new ArrayList<>();
    private final SteeringMixer mixer = new SteeringMixer();
    private Agent routeEditor;
    private double elapsed;

    public World() {
        load(AgentProfile.SAFE_ROUTE);
    }

    public List<Agent> agents() {
        return Collections.unmodifiableList(agents);
    }

    public List<Obstacle> obstacles() {
        return Collections.unmodifiableList(obstacles);
    }

    public List<Vec2> draftRoute() {
        return Collections.unmodifiableList(draftRoute);
    }

    public Agent routeEditor() {
        return routeEditor;
    }

    public double elapsed() {
        return elapsed;
    }

    public Agent agent(AgentProfile profile) {
        return agents.stream().filter(a -> a.profile() == profile).findFirst().orElseThrow();
    }

    public int totalContacts() {
        return agents.stream().mapToInt(Agent::contacts).sum();
    }

    public Agent firstAgent() {
        return agents.get(0);
    }

    public AgentProfile scenario() {
        return firstAgent().profile();
    }

    public Vec2 target() {
        return firstAgent().target();
    }

    public Route route() {
        return firstAgent().route();
    }

    public void load(AgentProfile profile) {
        createPark();
        agents.add(createAgent(profile));
    }

    private void createPark() {
        elapsed = 0;
        agents.clear();
        obstacles.clear();
        draftRoute.clear();
        routeEditor = null;
        double[][] trees = {{110, 120, 27}, {955, 95, 29}, {75, 640, 25}, {955, 605, 30}};
        for (double[] tree : trees)
            obstacles.add(new Obstacle(new Vec2(tree[0], tree[1]), tree[2], Obstacle.Kind.TREE));
        obstacles.add(new Obstacle(new Vec2(340, 170), 23, Obstacle.Kind.BENCH));
        obstacles.add(new Obstacle(new Vec2(550, 350), 52, Obstacle.Kind.FOUNTAIN));
        obstacles.add(new Obstacle(new Vec2(780, 535), 30, Obstacle.Kind.GARDEN));
    }

    private Agent createAgent(AgentProfile profile) {
        Vec2 position, target;
        Route route = null;
        double heading = 0;
        switch (profile) {
            case SEEK -> {
                position = new Vec2(230, 275);
                target = new Vec2(425, 335);
            }
            case FLEE -> {
                position = new Vec2(300, 465);
                target = new Vec2(400, 465);
                heading = Math.PI;
            }
            case ARRIVE -> {
                position = new Vec2(855, 540);
                target = new Vec2(930, 290);
            }
            case WANDER -> {
                position = new Vec2(750, 230);
                target = position;
                heading = -.5;
            }
            case AVOIDANCE -> {
                position = new Vec2(270, 350);
                target = new Vec2(930, 350);
            }
            case PATH -> {
                route =
                        new Route(
                                List.of(
                                        new Vec2(165, 550),
                                        new Vec2(165, 260),
                                        new Vec2(520, 260),
                                        new Vec2(700, 190)));
                position = route.points().get(0);
                target = route.end();
                heading = -Math.PI / 2;
            }
            case FREE_WALK -> {
                position = new Vec2(630, 540);
                target = position;
                heading = -Math.PI / 2;
            }
            case SAFE_ROUTE -> {
                route =
                        new Route(
                                List.of(
                                        new Vec2(165, 600),
                                        new Vec2(165, 350),
                                        new Vec2(550, 350),
                                        new Vec2(820, 350),
                                        new Vec2(900, 190)));
                position = route.points().get(0);
                target = route.end();
                heading = -Math.PI / 2;
            }
            default -> throw new IllegalStateException();
        }
        Agent agent = new Agent(profile, position, heading, target, route);
        switch (profile) {
            case SEEK -> agent.setWeight(BehaviorType.SEEK, 1);
            case FLEE -> agent.setWeight(BehaviorType.FLEE, 1);
            case ARRIVE -> agent.setWeight(BehaviorType.ARRIVE, 1);
            case WANDER -> {
                agent.setWeight(BehaviorType.WANDER, 1);
                agent.setMaxSpeed(78);
            }
            case AVOIDANCE -> {
                agent.setWeight(BehaviorType.SEEK, .8);
                agent.setWeight(BehaviorType.AVOIDANCE, 2.6);
            }
            case PATH -> agent.setWeight(BehaviorType.PATH, 1);
            case FREE_WALK -> {
                agent.setWeight(BehaviorType.WANDER, .9);
                agent.setWeight(BehaviorType.AVOIDANCE, 2.6);
                agent.setMaxSpeed(92);
            }
            case SAFE_ROUTE -> {
                agent.setWeight(BehaviorType.PATH, 1);
                agent.setWeight(BehaviorType.AVOIDANCE, 2.8);
                agent.setWeight(BehaviorType.ARRIVE, .9);
            }
        }
        return agent;
    }

    public Agent resetAgent(AgentProfile profile) {
        Agent replacement = createAgent(profile);
        int index = agents.indexOf(agent(profile));
        agents.set(index, replacement);
        clearDraft();
        return replacement;
    }

    public void update(double dt) {
        if (!Double.isFinite(dt) || dt <= 0) return;
        double duration = Math.min(dt, .25);
        int steps = Math.max(1, (int) Math.ceil(duration / (1.0 / 120)));
        double step = duration / steps;
        for (int i = 0; i < steps; i++) {
            for (Agent agent : agents) {
                agent.integrate(mixer.calculate(agent, this, step), step);
                resolveContacts(agent);
            }
            elapsed += step;
        }
    }

    /** Salvaguarda física, separada do desvio antecipatório; contatos ficam contabilizados. */
    private void resolveContacts(Agent agent) {
        boolean touching = false;
        for (Obstacle obstacle : obstacles) {
            Vec2 offset = agent.position().subtract(obstacle.position());
            double minimum = obstacle.radius() + Agent.RADIUS;
            if (offset.lengthSquared() < minimum * minimum) {
                touching = true;
                Vec2 normal =
                        offset.lengthSquared() < 1e-9
                                ? agent.heading().perpendicular()
                                : offset.normalized();
                Vec2 velocity = agent.velocity();
                double inward = velocity.dot(normal);
                if (inward < 0) velocity = velocity.subtract(normal.multiply(inward));
                agent.resolvePosition(
                        obstacle.position().add(normal.multiply(minimum + .01)), velocity);
            }
        }
        Vec2 p = agent.position();
        Vec2 bounded =
                new Vec2(Vec2.clamp(p.x(), 25, WIDTH - 25), Vec2.clamp(p.y(), 25, HEIGHT - 25));
        if (!bounded.equals(p)) {
            Vec2 v = agent.velocity();
            agent.resolvePosition(
                    bounded,
                    new Vec2(bounded.x() == p.x() ? v.x() : 0, bounded.y() == p.y() ? v.y() : 0));
        }
        agent.registerContact(touching);
    }

    public boolean setTarget(Vec2 point) {
        return setTarget(firstAgent(), point);
    }

    public boolean setTarget(Agent agent, Vec2 point) {
        Vec2 p = inside(point);
        double comfort =
                agent.hasBehavior(BehaviorType.AVOIDANCE)
                        ? br.edu.steering.model.behavior.ObstacleAvoidance.TARGET_CLEARANCE
                        : 3;
        if (!isFree(p, Agent.RADIUS + comfort)) return false;
        if (agent.profile().followsPath()) {
            List<Vec2> points = new ArrayList<>(agent.route().points());
            if (p.distance(points.get(points.size() - 2)) < 20) return false;
            points.set(points.size() - 1, p);
            agent.setRoute(new Route(points));
        } else agent.setTarget(p);
        return true;
    }

    public Agent agentAt(Vec2 point) {
        for (Agent agent : agents) if (agent.position().distance(point) <= 27) return agent;
        return null;
    }

    public boolean addObstacle(Vec2 point) {
        if (obstacles.size() >= 30) return false;
        Vec2 p = inside(point);
        if (!isFree(p, 30)) return false;
        for (Agent agent : agents)
            if (p.distance(agent.position()) < 95
                    || (!isWandering(agent) && p.distance(agent.target()) < 110)) return false;
        obstacles.add(new Obstacle(p, 27, Obstacle.Kind.GARDEN));
        return true;
    }

    public boolean removeObstacle(Vec2 point) {
        return obstacles.removeIf(o -> point.distance(o.position()) <= o.radius() + 8);
    }

    public void beginRoute() {
        beginRoute(firstAgent());
    }

    public void beginRoute(Agent agent) {
        draftRoute.clear();
        routeEditor = agent;
    }

    public void clearDraft() {
        draftRoute.clear();
        routeEditor = null;
    }

    public boolean addRoutePoint(Vec2 point) {
        if (routeEditor == null) return false;
        Vec2 p = inside(point);
        double comfort =
                routeEditor.hasBehavior(BehaviorType.AVOIDANCE)
                        ? br.edu.steering.model.behavior.ObstacleAvoidance.TARGET_CLEARANCE
                        : 2;
        if (!isFree(p, Agent.RADIUS + comfort) || draftRoute.size() >= 18) return false;
        if (!draftRoute.isEmpty() && p.distance(draftRoute.get(draftRoute.size() - 1)) < 30)
            return false;
        draftRoute.add(p);
        if (draftRoute.size() >= 2) {
            routeEditor.setRoute(new Route(draftRoute));
            routeEditor.resolvePosition(draftRoute.get(0), Vec2.ZERO);
        }
        return true;
    }

    public double clearance(Agent agent) {
        double min = Double.POSITIVE_INFINITY;
        for (Obstacle o : obstacles)
            min =
                    Math.min(
                            min,
                            agent.position().distance(o.position()) - o.radius() - Agent.RADIUS);
        return min;
    }

    public static boolean isWandering(Agent agent) {
        return agent.profile() == AgentProfile.WANDER || agent.profile() == AgentProfile.FREE_WALK;
    }

    private boolean isFree(Vec2 p, double radius) {
        for (Obstacle o : obstacles)
            if (p.distance(o.position()) < o.radius() + radius) return false;
        return true;
    }

    private Vec2 inside(Vec2 p) {
        return new Vec2(Vec2.clamp(p.x(), 85, WIDTH - 85), Vec2.clamp(p.y(), 85, HEIGHT - 85));
    }
}
