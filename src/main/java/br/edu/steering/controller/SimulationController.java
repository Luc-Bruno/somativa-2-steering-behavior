package br.edu.steering.controller;

import br.edu.steering.model.*;
import br.edu.steering.model.AgentProfile;
import br.edu.steering.model.math.Vec2;
import br.edu.steering.view.MainView;

import java.awt.event.*;

import javax.swing.*;

/** Coordena o Model e a View; todos os eventos e atualizações ocorrem na EDT. */
public final class SimulationController implements MainView.Actions {
    private final World world;
    private final MainView view;
    private final Timer timer;
    private Agent selected;
    private MainView.Tool tool = MainView.Tool.TARGET;
    private boolean paused;
    private double rate = 1;
    private long previousNanos;

    public SimulationController(World world, MainView view) {
        this.world = world;
        this.view = view;
        selected = world.firstAgent();
        view.bind(this);
        timer =
                new Timer(
                        16,
                        e -> {
                            long now = System.nanoTime();
                            double dt = Math.min(.05, (now - previousNanos) / 1_000_000_000.0);
                            previousNanos = now;
                            advance(dt);
                        });
        timer.setCoalesce(true);
        view.map()
                .addMouseListener(
                        new MouseAdapter() {
                            @Override
                            public void mousePressed(MouseEvent e) {
                                handleMapClick(
                                        view.map().toWorld(e.getPoint()),
                                        SwingUtilities.isRightMouseButton(e));
                            }
                        });
        bindKey("SPACE", "pause", this::togglePause);
        bindKey("R", "reset", this::reset);
        bindKey("D", "vectors", view::toggleVectors);
        bindKey("N", "step", this::stepOnce);
        AgentProfile[] scenarios = AgentProfile.values();
        for (int i = 0; i < scenarios.length; i++) {
            AgentProfile scenario = scenarios[i];
            bindKey(Integer.toString(i + 1), "scenario" + i, () -> selectScenario(scenario));
        }
        refresh();
    }

    public void start() {
        previousNanos = System.nanoTime();
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public boolean isPaused() {
        return paused;
    }

    public Agent selected() {
        return selected;
    }

    public MainView.Tool tool() {
        return tool;
    }

    public void advance(double seconds) {
        if (!paused) world.update(seconds * rate);
        refresh();
    }

    @Override
    public void selectScenario(AgentProfile scenario) {
        world.load(scenario);
        selected = world.firstAgent();
        tool = MainView.Tool.TARGET;
        paused = false;
        view.showScenario(scenario);
        view.showAgent(selected);
        view.showTool(tool);
        view.setPaused(false);
        refresh();
    }

    @Override
    public void selectTool(MainView.Tool value) {
        if (value == MainView.Tool.ROUTE) {
            if (!world.scenario().followsPath()) selectScenario(AgentProfile.SAFE_ROUTE);
            world.beginRoute();
            paused = true;
            view.setPaused(true);
            view.setMessage(
                    "Desenhe a rota com cliques: mínimo de 2 pontos. Depois use Continuar ou"
                            + " Espaço. Selecionar Rota novamente inicia outro desenho.");
        } else if (value == MainView.Tool.OBSTACLE) {
            view.setMessage(
                    "Clique para adicionar um canteiro. Botão direito remove um obstáculo."
                            + " Reiniciar restaura o mapa original.");
        } else view.setMessage(world.scenario().instruction());
        tool = value;
        view.showTool(tool);
        refresh();
    }

    public void handleMapClick(Vec2 point, boolean rightButton) {
        if (point == null) return;
        if (rightButton) {
            view.setMessage(
                    world.removeObstacle(point)
                            ? "Obstáculo removido. Reiniciar restaura o cenário."
                            : "Clique com o botão direito sobre um obstáculo para removê-lo.");
            refresh();
            return;
        }
        if (tool == MainView.Tool.ROUTE) {
            paused = true;
            view.setPaused(true);
            boolean accepted = world.addRoutePoint(point);
            view.setMessage(
                    !accepted
                            ? "Escolha pontos livres, separados por pelo menos 30 unidades (máximo"
                                    + " de 18 pontos)."
                            : world.draftRoute().size() < 2
                                    ? "Ponto inicial definido. Adicione pelo menos mais um ponto."
                                    : "Rota atualizada. Adicione outros pontos ou use Continuar /"
                                            + " Espaço para testar.");
        } else {
            Agent clicked = world.agentAt(point);
            if (clicked != null) {
                selected = clicked;
                view.showAgent(selected);
                view.setMessage("Ajustes agora aplicados a " + selected.name() + ".");
            } else if (tool == MainView.Tool.OBSTACLE)
                view.setMessage(
                        world.addObstacle(point)
                                ? "Canteiro adicionado. Observe como o pedestre reage."
                                : "Escolha uma área livre e distante dos pedestres e do alvo."
                                        + " Limite: 30 obstáculos.");
            else if (world.scenario() == AgentProfile.WANDER
                    || world.scenario() == AgentProfile.FREE_WALK)
                view.setMessage(
                        "Wander escolhe seu próprio alvo. Use Obstáculo para alterar o ambiente.");
            else
                view.setMessage(
                        world.setTarget(point)
                                ? world.scenario().instruction()
                                : "Deixe espaço entre o alvo e os obstáculos para a chegada.");
        }
        refresh();
    }

    @Override
    public void togglePause() {
        if (paused && tool == MainView.Tool.ROUTE && world.draftRoute().size() == 1) {
            view.setMessage("A rota precisa de pelo menos dois pontos antes de continuar.");
            return;
        }
        paused = !paused;
        view.setPaused(paused);
        previousNanos = System.nanoTime();
        refresh();
    }

    @Override
    public void reset() {
        selectScenario(world.scenario());
    }

    @Override
    public void stepOnce() {
        if (paused && !(tool == MainView.Tool.ROUTE && world.draftRoute().size() == 1)) {
            world.update(1.0 / 60);
            refresh();
        }
    }

    @Override
    public void changeSpeed(double value) {
        selected.setMaxSpeed(value);
        refresh();
    }

    @Override
    public void changeForce(double value) {
        selected.setMaxForce(value);
        refresh();
    }

    @Override
    public void changeWeight(BehaviorType type, double value) {
        if (selected.hasBehavior(type)) selected.setWeight(type, value);
        refresh();
    }

    @Override
    public void changeRate(double value) {
        rate = value;
    }

    @Override
    public void showHelp() {
        boolean previous = paused;
        paused = true;
        view.setPaused(true);
        JOptionPane.showMessageDialog(
                view,
                "COMO DEMONSTRAR\n\n"
                    + "1. Use os cenários 1 a 6 para apresentar os comportamentos.\n"
                    + "2. Compare Seek e Arrive mudando o alvo com um clique.\n"
                    + "3. Em Flee, coloque a ameaça perto do pedestre.\n"
                    + "4. Ative Vetores para ver velocidade (azul), força (laranja) e sensores.\n"
                    + "5. Em Obstacle Avoidance, compare o peso de desvio 2,6 com 0.\n"
                    + "6. Apresente as combinações Passeio livre e Rota segura.\n"
                    + "7. Em Rota segura, desenhe um percurso e adicione obstáculos.\n\n"
                    + "CONTROLES\n"
                    + "Espaço: pausar / continuar     R: reiniciar     D: vetores\n"
                    + "N: avançar um quadro quando pausado     1–8: selecionar cenário\n"
                    + "Clique em um pedestre: selecionar     Botão direito: remover obstáculo\n\n"
                    + "Os pesos atuam somente nos comportamentos ativos do pedestre selecionado.\n"
                    + "Reiniciar restaura os pesos, o mapa e a semente aleatória do cenário.\n"
                    + "Contatos físicos são a salvaguarda de colisão, não o desvio antecipatório.",
                "Guia de demonstração",
                JOptionPane.INFORMATION_MESSAGE);
        paused = previous;
        view.setPaused(paused);
        previousNanos = System.nanoTime();
        refresh();
    }

    private void refresh() {
        view.updateLive(world, selected, paused);
    }

    private void bindKey(String key, String name, Runnable runnable) {
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), name);
        view.getActionMap()
                .put(
                        name,
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                runnable.run();
                            }
                        });
    }
}
