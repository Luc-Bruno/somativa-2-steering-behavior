package br.edu.steering.model;

import br.edu.steering.model.math.Vec2;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public final class Agent {
    public static final double RADIUS = 12;
    private final AgentProfile profile;
    private Vec2 target;
    private Route route;
    private Vec2 preferredVelocity = Vec2.ZERO;
    private Vec2 committedObstacle;
    private double avoidanceSide;
    private final int id;
    private final String name;
    private final Random random;
    private final EnumMap<BehaviorType, Double> weights = new EnumMap<>(BehaviorType.class);
    private final EnumMap<BehaviorType, Vec2> forces = new EnumMap<>(BehaviorType.class);
    private final Deque<Vec2> trail = new ArrayDeque<>();
    private Vec2 position;
    private Vec2 velocity = Vec2.ZERO;
    private Vec2 steering = Vec2.ZERO;
    private Vec2 heading = new Vec2(1, 0);
    private double maxSpeed = 110;
    private double maxForce = 280;
    private double wanderAngle;
    private double pathProgress;
    private double walkingPhase;
    private double trailClock;
    private boolean touching;
    private int contacts;
    private Vec2 sensorEnd;
    private Vec2 avoidedObstacle;
    private Vec2 pathProjection;
    private Vec2 steeringTarget;

    public Agent(
            AgentProfile profile, Vec2 position, double orientation, Vec2 target, Route route) {
        this.profile = profile;
        this.id = profile.ordinal();
        this.name = profile.pedestrianName();
        this.position = position;
        this.target = target;
        this.route = route;
        heading = Vec2.fromAngle(orientation);
        random = new Random(731L + id * 101L);
        trail.add(position);
    }

    public AgentProfile profile() {
        return profile;
    }

    public Vec2 target() {
        return target;
    }

    public Route route() {
        return route;
    }

    public void setTarget(Vec2 value) {
        target = value;
    }

    public void setRoute(Route value) {
        route = value;
        target = value.end();
        pathProgress = 0;
    }

    public double avoidanceDirection(Vec2 obstacle, double preferredSide) {
        if (!obstacle.equals(committedObstacle)) {
            committedObstacle = obstacle;
            avoidanceSide = preferredSide;
        }
        return avoidanceSide;
    }

    public void clearAvoidance() {
        committedObstacle = null;
    }

    public void changeAvoidanceSide(double side) {
        avoidanceSide = side;
    }

    public Vec2 preferredVelocity() {
        return preferredVelocity;
    }

    public void setPreferredVelocity(Vec2 value) {
        preferredVelocity = value;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Vec2 position() {
        return position;
    }

    public Vec2 velocity() {
        return velocity;
    }

    public Vec2 steering() {
        return steering;
    }

    public Vec2 heading() {
        return heading;
    }

    public double speed() {
        return velocity.length();
    }

    public double maxSpeed() {
        return maxSpeed;
    }

    public double maxForce() {
        return maxForce;
    }

    public double mass() {
        return 1;
    }

    public double walkingPhase() {
        return walkingPhase;
    }

    public double pathProgress() {
        return pathProgress;
    }

    public int contacts() {
        return contacts;
    }

    public Map<BehaviorType, Double> weights() {
        return Collections.unmodifiableMap(weights);
    }

    public Map<BehaviorType, Vec2> forces() {
        return Collections.unmodifiableMap(forces);
    }

    public Iterable<Vec2> trail() {
        return Collections.unmodifiableCollection(trail);
    }

    public double weight(BehaviorType type) {
        return weights.getOrDefault(type, 0.0);
    }

    public boolean hasBehavior(BehaviorType type) {
        return weights.containsKey(type);
    }

    public Vec2 sensorEnd() {
        return sensorEnd;
    }

    public Vec2 avoidedObstacle() {
        return avoidedObstacle;
    }

    public Vec2 pathProjection() {
        return pathProjection;
    }

    public Vec2 steeringTarget() {
        return steeringTarget;
    }

    public void setMaxSpeed(double value) {
        maxSpeed = Vec2.clamp(value, 40, 200);
    }

    public void setMaxForce(double value) {
        maxForce = Vec2.clamp(value, 80, 600);
    }

    public void setWeight(BehaviorType type, double value) {
        weights.put(type, Vec2.clamp(value, 0, 4));
    }

    public void setPathProgress(double value) {
        pathProgress = Math.max(pathProgress, value);
    }

    public void resetPathProgress() {
        pathProgress = 0;
    }

    public void setSensor(Vec2 end, Vec2 obstacle) {
        sensorEnd = end;
        avoidedObstacle = obstacle;
    }

    public void setPathDebug(Vec2 projection, Vec2 target) {
        pathProjection = projection;
        steeringTarget = target;
    }

    public double advanceWander(double dt) {
        wanderAngle += (random.nextDouble() * 2 - 1) * 1.9 * Math.sqrt(dt);
        return wanderAngle;
    }

    public void beginForces() {
        forces.clear();
        sensorEnd = null;
        avoidedObstacle = null;
        pathProjection = null;
        steeringTarget = null;
    }

    public void recordForce(BehaviorType type, Vec2 value) {
        forces.put(type, value);
    }

    /** Euler semi-implícito: atualiza primeiro a velocidade, depois a posição. */
    public void integrate(Vec2 force, double dt) {
        steering = force.limit(maxForce);
        velocity = velocity.add(steering.multiply(dt / mass())).limit(maxSpeed);
        position = position.add(velocity.multiply(dt));
        if (speed() > 0.5) heading = velocity.normalized();
        walkingPhase += speed() * dt * 0.14;
        trailClock += dt;
        if (trailClock >= 0.07) {
            trailClock = 0;
            trail.addLast(position);
            while (trail.size() > 150) trail.removeFirst();
        }
    }

    void resolvePosition(Vec2 newPosition, Vec2 newVelocity) {
        position = newPosition;
        velocity = newVelocity;
    }

    void registerContact(boolean current) {
        if (current && !touching) contacts++;
        touching = current;
    }
}
