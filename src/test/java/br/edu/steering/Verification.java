package br.edu.steering;

import br.edu.steering.controller.SimulationController;
import br.edu.steering.model.*;
import br.edu.steering.model.AgentProfile;
import br.edu.steering.model.math.Vec2;
import br.edu.steering.view.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.swing.*;

/** Verificações de resultado e integração, sem JUnit ou qualquer dependência externa. */
public final class Verification {
    private static int passed, failed;
    private static final StringBuilder report = new StringBuilder();

    private Verification() {}

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.setOut(
                new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        testMath();
        testBehaviors();
        Path output = Path.of(args.length > 0 ? args[0] : "output");
        Files.createDirectories(output);
        AtomicReference<Throwable> uiFailure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(
                () -> {
                    try {
                        testInterface(output);
                    } catch (Throwable e) {
                        uiFailure.set(e);
                    }
                });
        if (uiFailure.get() != null) {
            uiFailure.get().printStackTrace();
            check(false, "Integração da interface");
        }
        String summary = "RESULTADO: " + passed + " verificações aprovadas; " + failed + " falhas.";
        System.out.println(summary);
        report.append(summary).append("\n");
        Files.writeString(
                output.resolve("verificacao.txt"),
                report.toString(),
                java.nio.charset.StandardCharsets.UTF_8);
        if (failed > 0) System.exit(1);
    }

    private static void testMath() {
        check(Vec2.ZERO.normalized().equals(Vec2.ZERO), "Normalização do vetor zero é segura");
        check(
                Math.abs(new Vec2(3, 4).limit(2).length() - 2) < 1e-9,
                "Limite de magnitude preserva a direção");
        Route route = new Route(List.of(new Vec2(0, 0), new Vec2(100, 0), new Vec2(100, 100)));
        Route.Projection p = route.project(new Vec2(120, 80));
        check(
                p.point().distance(new Vec2(100, 80)) < 1e-9
                        && Math.abs(p.distanceAlong() - 180) < 1e-9,
                "Projeção e distância acumulada em rota com curva");
    }

    private static void testBehaviors() {
        World world = new World();
        world.load(AgentProfile.SEEK);
        double initial = world.firstAgent().position().distance(world.target());
        simulate(world, 3);
        check(
                world.firstAgent().position().distance(world.target()) < initial - 80,
                "Seek reduz a distância até o alvo");
        world.load(AgentProfile.ARRIVE);
        simulate(world, 22);
        check(
                world.firstAgent().position().distance(world.target()) < 5
                        && world.firstAgent().speed() < .7,
                "Arrive alcança o destino e para");
        world.load(AgentProfile.ARRIVE);
        world.setTarget(new Vec2(1080, 350));
        simulate(world, 24);
        check(
                world.target().x() <= World.WIDTH - 75
                        && world.firstAgent().position().distance(world.target()) < 5,
                "Alvo perto da borda é mantido em região alcançável");
        world.load(AgentProfile.FLEE);
        simulate(world, 7);
        check(
                world.firstAgent().position().distance(world.target()) > 190
                        && world.firstAgent().speed() < 2,
                "Flee aumenta a distância e repousa fora do raio");
        world.load(AgentProfile.WANDER);
        Vec2 start = world.firstAgent().position();
        simulate(world, 12);
        Vec2 end = world.firstAgent().position();
        World replay = new World();
        replay.load(AgentProfile.WANDER);
        simulate(replay, 12);
        check(start.distance(end) > 60, "Wander produz exploração contínua");
        check(
                end.distance(replay.firstAgent().position()) < 1e-9,
                "Reinício com semente fixa reproduz Wander");
        world.load(AgentProfile.AVOIDANCE);
        double maxX = 0, minClearance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < 60 * 25; i++) {
            world.update(1.0 / 60);
            maxX = Math.max(maxX, world.firstAgent().position().x());
            minClearance = Math.min(minClearance, world.clearance(world.firstAgent()));
        }
        System.out.printf(
                java.util.Locale.ROOT,
                "  Desvio: x máximo=%.1f, folga mínima=%.2f, contatos=%d%n",
                maxX,
                minClearance,
                world.totalContacts());
        check(
                maxX > 800 && world.totalContacts() == 0,
                "Obstacle Avoidance contorna a fonte sem contatos físicos");
        World noAvoid = new World();
        noAvoid.load(AgentProfile.AVOIDANCE);
        noAvoid.firstAgent().setWeight(BehaviorType.AVOIDANCE, 0);
        simulate(noAvoid, 12);
        check(
                noAvoid.totalContacts() > 0,
                "Peso zero no desvio revela contato com o obstáculo (controle)");
        world.load(AgentProfile.PATH);
        simulate(world, 38);
        System.out.printf(
                java.util.Locale.ROOT,
                "  Rota: distância final=%.2f, velocidade=%.2f%n",
                world.firstAgent().position().distance(world.target()),
                world.firstAgent().speed());
        check(
                world.firstAgent().position().distance(world.route().end()) < 6
                        && world.firstAgent().speed() < 1,
                "Path Following percorre os segmentos e encerra o trajeto");
        world.load(AgentProfile.SAFE_ROUTE);
        simulate(world, 42);
        System.out.printf(
                java.util.Locale.ROOT,
                "  Rota segura: distância final=%.2f, contatos=%d%n",
                world.firstAgent().position().distance(world.target()),
                world.totalContacts());
        check(
                world.firstAgent().position().distance(world.route().end()) < 7
                        && world.firstAgent().speed() < 1
                        && world.totalContacts() == 0,
                "Rota + desvio + chegada concluem o percurso sem contato");
        Agent resting =
                new Agent(
                        AgentProfile.SAFE_ROUTE,
                        world.target(),
                        -Math.PI / 2,
                        world.target(),
                        world.route());
        resting.setPathProgress(world.route().length());
        resting.setWeight(BehaviorType.PATH, 1);
        resting.setWeight(BehaviorType.ARRIVE, .9);
        resting.setWeight(BehaviorType.AVOIDANCE, 2.8);
        br.edu.steering.model.behavior.SteeringMixer restingMixer =
                new br.edu.steering.model.behavior.SteeringMixer();
        double drift = 0;
        for (int i = 0; i < 300; i++) {
            resting.integrate(restingMixer.calculate(resting, world, 1.0 / 60), 1.0 / 60);
            drift = Math.max(drift, resting.position().distance(world.target()));
        }
        check(
                drift < .1 && resting.speed() < .1,
                "Desvio não inicia uma manobra quando o agente já está parado no destino");
        world.load(AgentProfile.FREE_WALK);
        simulate(world, 60);
        check(
                world.totalContacts() == 0,
                "Wander + desvio mantém exploração sem contatos por 60 s");
        boolean limits = true;
        for (AgentProfile scenario : AgentProfile.values()) {
            world.load(scenario);
            for (int i = 0; i < 60 * 20; i++) {
                world.update(1.0 / 60);
                for (Agent a : world.agents())
                    limits &=
                            a.speed() <= a.maxSpeed() + 1e-7
                                    && a.steering().length() <= a.maxForce() + 1e-7
                                    && world.clearance(a) >= -1e-6
                                    && a.position().x() >= 25
                                    && a.position().x() <= World.WIDTH - 25
                                    && a.position().y() >= 25
                                    && a.position().y() <= World.HEIGHT - 25;
            }
        }
        check(limits, "Limites físicos e posições válidas nos oito cenários");
        double elapsed = world.elapsed();
        world.update(Double.NaN);
        world.update(-1);
        check(world.elapsed() == elapsed, "Intervalos de tempo inválidos não alteram a simulação");
    }

    private static void testInterface(Path output) throws Exception {
        Theme.install();
        World world = new World();
        world.load(AgentProfile.SAFE_ROUTE);
        MainView view = new MainView(world);
        SimulationController controller = new SimulationController(world, view);
        button(view, "action.pause").doClick();
        double time = world.elapsed();
        controller.advance(.1);
        check(
                controller.isPaused() && world.elapsed() == time,
                "Botão Pausar interrompe a evolução do Model");
        button(view, "action.step").doClick();
        check(
                Math.abs(world.elapsed() - 1.0 / 60) < 1e-9,
                "Avanço de um quadro funciona durante a pausa");
        button(view, "scenario.ARRIVE").doClick();
        ((JSlider) find(view, "parameter.speed")).setValue(160);
        check(
                world.scenario() == AgentProfile.ARRIVE && controller.selected().maxSpeed() == 160,
                "Seleção de cenário e slider chegam ao Model pelo Controller");
        button(view, "scenario.FREE_WALK").doClick();
        ((JSlider) find(view, "weight.WANDER")).setValue(12);
        check(
                controller.selected().weight(BehaviorType.WANDER) == 1.2,
                "Slider de peso altera a contribuição da combinação ativa");
        int count = world.obstacles().size();
        button(view, "tool.OBSTACLE").doClick();
        controller.handleMapClick(new Vec2(920, 490), false);
        check(world.obstacles().size() == count + 1, "Ferramenta adiciona obstáculo ao mapa");
        controller.handleMapClick(new Vec2(920, 490), true);
        check(world.obstacles().size() == count, "Botão direito remove o obstáculo");
        button(view, "tool.ROUTE").doClick();
        controller.handleMapClick(new Vec2(210, 500), false);
        controller.togglePause();
        check(controller.isPaused(), "Editor exige pelo menos dois pontos para continuar");
        controller.handleMapClick(new Vec2(400, 500), false);
        check(
                world.route().points().size() == 2
                        && world.route().end().equals(new Vec2(400, 500)),
                "Editor substitui a rota por pontos da interface");
        view.getActionMap().get("vectors").actionPerformed(new ActionEvent(view, 0, "vectors"));
        check(view.map().vectorsVisible(), "Atalho D alterna a visualização de vetores");
        controller.selectScenario(AgentProfile.SAFE_ROUTE);
        for (int i = 0; i < 60 * 6; i++) controller.advance(1.0 / 60);
        writeSnapshot(view, output.resolve("preview.png"), 1536, 960);
        writeSnapshot(view, output.resolve("preview-compact.png"), 1180, 760);
        controller.selectScenario(AgentProfile.FREE_WALK);
        for (int i = 0; i < 60 * 5; i++) controller.advance(1.0 / 60);
        writeSnapshot(view, output.resolve("preview-passeio.png"), 1536, 960);
        controller.stop();
        check(true, "Interface renderizada nos tamanhos 1536×960 e 1180×760");
    }

    private static void writeSnapshot(MainView view, Path path, int width, int height)
            throws Exception {
        view.setSize(width, height);
        layout(view);
        layout(view);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        view.printAll(g);
        g.dispose();
        ImageIO.write(image, "png", path.toFile());
        System.out.println("  Imagem: " + path.toAbsolutePath());
    }

    private static void layout(Container container) {
        container.invalidate();
        container.doLayout();
        for (Component child : container.getComponents())
            if (child instanceof Container nested) layout(nested);
    }

    private static Component find(Container parent, String name) {
        for (Component child : parent.getComponents()) {
            if (name.equals(child.getName())) return child;
            if (child instanceof Container nested) {
                Component found = find(nested, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static AbstractButton button(Container parent, String name) {
        Component found = find(parent, name);
        if (!(found instanceof AbstractButton button))
            throw new IllegalStateException("Botão ausente: " + name);
        return button;
    }

    private static void simulate(World world, double seconds) {
        for (int i = 0; i < (int) Math.round(seconds * 60); i++) world.update(1.0 / 60);
    }

    private static void check(boolean condition, String title) {
        String line = (condition ? "PASS " : "FAIL ") + title;
        System.out.println(line);
        report.append(line).append("\n");
        if (condition) passed++;
        else failed++;
    }
}
