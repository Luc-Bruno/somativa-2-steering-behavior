package br.edu.steering.model;

import br.edu.steering.model.math.Vec2;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Testes prolongados: progresso, permanência local, distância e contatos. */
public final class AvoidanceStressTest {
    private record Case(
            String name,
            AgentProfile profile,
            Vec2 start,
            Vec2 target,
            double speed,
            double force,
            double seconds,
            boolean extra,
            boolean verticalRoute,
            double dt) {
        Case(
                String name,
                AgentProfile profile,
                Vec2 start,
                Vec2 target,
                double speed,
                double force,
                double seconds,
                boolean extra,
                boolean verticalRoute) {
            this(
                    name,
                    profile,
                    start,
                    target,
                    speed,
                    force,
                    seconds,
                    extra,
                    verticalRoute,
                    1.0 / 60);
        }
    }

    private record Result(
            String name,
            double reachedAt,
            double residence,
            double lowSpeed,
            double clearance,
            int contacts,
            double finalDistance,
            boolean passed) {}

    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));
        Path output = Path.of(args.length > 0 ? args[0] : "output/stress");
        Files.createDirectories(output);
        boolean diagnose = Arrays.asList(args).contains("--diagnose");
        List<Case> cases =
                List.of(
                        new Case(
                                "desvio-padrao",
                                AgentProfile.AVOIDANCE,
                                null,
                                null,
                                110,
                                280,
                                60,
                                false,
                                false),
                        new Case(
                                "passeio-10-minutos",
                                AgentProfile.FREE_WALK,
                                null,
                                null,
                                92,
                                280,
                                600,
                                false,
                                false),
                        new Case(
                                "rota-segura",
                                AgentProfile.SAFE_ROUTE,
                                null,
                                null,
                                110,
                                280,
                                70,
                                false,
                                false),
                        new Case(
                                "fonte-direita-esquerda",
                                AgentProfile.AVOIDANCE,
                                new Vec2(900, 350),
                                new Vec2(230, 350),
                                110,
                                280,
                                60,
                                false,
                                false),
                        new Case(
                                "fonte-sul-norte",
                                AgentProfile.AVOIDANCE,
                                new Vec2(550, 590),
                                new Vec2(550, 155),
                                110,
                                280,
                                60,
                                false,
                                false),
                        new Case(
                                "fonte-norte-sul",
                                AgentProfile.AVOIDANCE,
                                new Vec2(550, 165),
                                new Vec2(550, 590),
                                110,
                                280,
                                60,
                                false,
                                false),
                        new Case(
                                "desvio-rapido",
                                AgentProfile.AVOIDANCE,
                                null,
                                null,
                                160,
                                280,
                                60,
                                false,
                                false),
                        new Case(
                                "desvio-forca-menor",
                                AgentProfile.AVOIDANCE,
                                null,
                                null,
                                90,
                                140,
                                90,
                                false,
                                false),
                        new Case(
                                "obstaculo-adicionado",
                                AgentProfile.AVOIDANCE,
                                null,
                                null,
                                110,
                                280,
                                70,
                                true,
                                false),
                        new Case(
                                "rota-vertical",
                                AgentProfile.SAFE_ROUTE,
                                new Vec2(550, 590),
                                new Vec2(550, 155),
                                110,
                                280,
                                80,
                                false,
                                true),
                        new Case(
                                "passeio-30fps",
                                AgentProfile.FREE_WALK,
                                null,
                                null,
                                92,
                                280,
                                300,
                                false,
                                false,
                                1.0 / 30),
                        new Case(
                                "passeio-144fps",
                                AgentProfile.FREE_WALK,
                                null,
                                null,
                                92,
                                280,
                                300,
                                false,
                                false,
                                1.0 / 144));
        List<Result> results = new ArrayList<>();
        for (Case test : cases) results.add(run(test, output));
        int failures = 0;
        try (PrintWriter report =
                new PrintWriter(
                        Files.newBufferedWriter(
                                output.resolve("resumo.txt"), StandardCharsets.UTF_8))) {
            for (Result result : results) {
                String line =
                        String.format(
                                Locale.ROOT,
                                "%s %s | chegada=%.2fs | permanência local=%.2fs | baixa"
                                    + " velocidade=%.2fs | folga=%.2f | contatos=%d | distância"
                                    + " final=%.2f",
                                result.passed() ? "PASS" : "FAIL",
                                result.name(),
                                result.reachedAt(),
                                result.residence(),
                                result.lowSpeed(),
                                result.clearance(),
                                result.contacts(),
                                result.finalDistance());
                System.out.println(line);
                report.println(line);
                if (!result.passed()) failures++;
            }
            String summary =
                    "RESULTADO: "
                            + (results.size() - failures)
                            + "/"
                            + results.size()
                            + " testes prolongados aprovados.";
            System.out.println(summary);
            report.println(summary);
        }
        if (!diagnose && failures > 0) System.exit(1);
    }

    private static Result run(Case test, Path output) throws Exception {
        World world = new World();
        world.load(test.profile());
        Agent agent = world.firstAgent();
        if (test.start() != null) agent.resolvePosition(test.start(), Vec2.ZERO);
        if (test.verticalRoute())
            agent.setRoute(new Route(List.of(test.start(), new Vec2(550, 350), test.target())));
        else if (test.target() != null && !world.setTarget(test.target()))
            throw new IllegalStateException("Alvo inválido no teste " + test.name());
        agent.setMaxSpeed(test.speed());
        agent.setMaxForce(test.force());
        if (test.extra() && !world.addObstacle(new Vec2(390, 350)))
            throw new IllegalStateException("Obstáculo de teste inválido");
        Vec2 anchor = agent.position();
        double local = 0,
                maximumLocal = 0,
                slow = 0,
                maximumSlow = 0,
                reached = -1,
                clearance = Double.POSITIVE_INFINITY;
        try (PrintWriter trace =
                new PrintWriter(
                        Files.newBufferedWriter(
                                output.resolve(test.name() + ".csv"), StandardCharsets.UTF_8))) {
            trace.println("time,x,y,speed,goal_distance,clearance");
            for (int i = 0; i < (int) Math.round(test.seconds() / test.dt()); i++) {
                world.update(test.dt());
                double distance = agent.position().distance(agent.target());
                boolean active = World.isWandering(agent) || distance > 35;
                if (!World.isWandering(agent) && distance < 12 && reached < 0)
                    reached = world.elapsed();
                if (active && world.elapsed() > 2) {
                    if (agent.position().distance(anchor) > 20) {
                        anchor = agent.position();
                        local = 0;
                    } else local += test.dt();
                    slow = agent.speed() < 8 ? slow + test.dt() : 0;
                    maximumLocal = Math.max(maximumLocal, local);
                    maximumSlow = Math.max(maximumSlow, slow);
                } else {
                    local = 0;
                    slow = 0;
                    anchor = agent.position();
                }
                clearance = Math.min(clearance, world.clearance(agent));
                if (i % 6 == 0)
                    trace.printf(
                            Locale.ROOT,
                            "%.3f,%.3f,%.3f,%.3f,%.3f,%.3f%n",
                            world.elapsed(),
                            agent.position().x(),
                            agent.position().y(),
                            agent.speed(),
                            distance,
                            world.clearance(agent));
            }
        }
        boolean passed =
                (World.isWandering(agent) || reached >= 0)
                        && agent.contacts() == 0
                        && maximumLocal < 4
                        && maximumSlow < 3
                        && clearance >= 32
                        && (!agent.profile().followsPath()
                                || (agent.position().distance(agent.target()) < 8
                                        && agent.speed() < 1));
        return new Result(
                test.name(),
                reached,
                maximumLocal,
                maximumSlow,
                clearance,
                agent.contacts(),
                agent.position().distance(agent.target()),
                passed);
    }
}
