package br.edu.steering.view;

import br.edu.steering.model.*;
import br.edu.steering.model.AgentProfile;

import java.awt.*;
import java.util.EnumMap;
import java.util.Locale;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;

/** Componentes e apresentação; comandos são delegados ao Controller por Actions. */
public final class MainView extends JPanel {
    public enum Tool {
        TARGET("Alvo"),
        OBSTACLE("Obstáculo"),
        ROUTE("Rota");
        private final String label;

        Tool(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public interface Actions {
        void selectScenario(AgentProfile scenario);

        void selectTool(Tool tool);

        void togglePause();

        void reset();

        void stepOnce();

        void changeSpeed(double value);

        void changeForce(double value);

        void changeWeight(BehaviorType type, double value);

        void changeRate(double value);

        void showHelp();
    }

    private Actions actions;
    private boolean syncing;
    private final MapPanel map;
    private final EnumMap<AgentProfile, ScenarioButton> scenarioButtons =
            new EnumMap<>(AgentProfile.class);
    private final EnumMap<Tool, PillButton> toolButtons = new EnumMap<>(Tool.class);
    private final EnumMap<BehaviorType, JSlider> weightSliders = new EnumMap<>(BehaviorType.class);
    private final EnumMap<BehaviorType, JLabel> weightValues = new EnumMap<>(BehaviorType.class);
    private final EnumMap<BehaviorType, JLabel> weightTitles = new EnumMap<>(BehaviorType.class);
    private final JLabel title = Theme.heading("", 23);
    private final JLabel eyebrow = Theme.label("", 10, Theme.GREEN);
    private final JTextArea description = textArea(13, Theme.MUTED);
    private final JTextArea hint = textArea(12, Theme.MUTED);
    private final JLabel name = Theme.heading("Lia", 22);
    private final JLabel motion = Theme.label("", 12, Theme.MUTED);
    private final JLabel speedMetric = Theme.heading("0", 20);
    private final JLabel distanceMetric = Theme.heading("0", 20);
    private final JLabel forceMetric = Theme.heading("0", 20);
    private final JLabel contactMetric = Theme.heading("0", 20);
    private final JLabel speedValue = Theme.label("", 12, Theme.GREEN);
    private final JLabel forceValue = Theme.label("", 12, Theme.GREEN);
    private final JLabel elapsed = Theme.label("00:00", 12, Theme.MUTED);
    private final JSlider speedSlider = slider(40, 200, 110);
    private final JSlider forceSlider = slider(80, 600, 280);
    private final JCheckBox vectors = new JCheckBox("Vetores");
    private final JCheckBox trails = new JCheckBox("Rastros", true);
    private final PillButton pause = new PillButton("Pausar");
    private final PillButton step = new PillButton("1 passo");
    private final JComboBox<String> rate = new JComboBox<>(new String[] {"0,5×", "1×", "2×"});

    public MainView(World world) {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setPreferredSize(new Dimension(1460, 900));
        map = new MapPanel(world);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        add(buildHeader(), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        body.setBorder(Theme.padding(20, 20, 16, 20));
        body.add(buildSidebar(), BorderLayout.WEST);
        body.add(buildInspector(), BorderLayout.EAST);
        body.add(buildCenter(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        setName("mainView");
        speedSlider.setName("parameter.speed");
        forceSlider.setName("parameter.force");
        speedSlider.addChangeListener(
                e -> {
                    if (!syncing && actions != null) {
                        actions.changeSpeed(speedSlider.getValue());
                        speedValue.setText(speedSlider.getValue() + " u/s");
                    }
                });
        forceSlider.addChangeListener(
                e -> {
                    if (!syncing && actions != null) {
                        actions.changeForce(forceSlider.getValue());
                        forceValue.setText(forceSlider.getValue() + " u/s²");
                    }
                });
        vectors.setName("display.vectors");
        vectors.addActionListener(e -> map.setVectors(vectors.isSelected()));
        trails.setName("display.trails");
        trails.addActionListener(e -> map.setTrails(trails.isSelected()));
        showScenario(world.scenario());
        showAgent(world.firstAgent());
        showTool(Tool.TARGET);
        setPaused(false);
    }

    public void bind(Actions value) {
        actions = value;
    }

    public MapPanel map() {
        return map;
    }

    public JCheckBox vectorsControl() {
        return vectors;
    }

    public void toggleVectors() {
        vectors.doClick();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(Theme.INK);
        header.setBorder(Theme.padding(17, 27, 17, 24));
        JPanel brand = new JPanel(new BorderLayout(13, 0));
        brand.setOpaque(false);
        JComponent emblem =
                new JComponent() {
                    @Override
                    protected void paintComponent(Graphics graphics) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        Theme.quality(g);
                        g.setColor(new Color(0x9BBB94));
                        g.fillRoundRect(0, 0, 44, 44, 14, 14);
                        g.setColor(Theme.INK);
                        g.setStroke(new BasicStroke(2));
                        g.drawLine(13, 31, 31, 13);
                        g.drawArc(10, 8, 25, 25, 0, 180);
                        g.drawArc(8, 10, 25, 25, 180, 180);
                        g.dispose();
                    }
                };
        emblem.setPreferredSize(new Dimension(44, 44));
        brand.add(emblem, BorderLayout.WEST);
        JPanel words = Theme.column();
        JLabel main = Theme.heading("Parque autônomo", 23);
        main.setForeground(Theme.PAPER);
        words.add(main);
        words.add(
                Theme.label(
                        "STEERING BEHAVIORS  /  LABORATÓRIO DE MOVIMENTO",
                        10,
                        new Color(0xA3BDB0)));
        brand.add(words, BorderLayout.CENTER);
        header.add(brand, BorderLayout.WEST);
        JPanel commands = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        commands.setOpaque(false);
        PillButton help = new PillButton("Como demonstrar");
        help.setName("action.help");
        help.addActionListener(
                e -> {
                    if (actions != null) actions.showHelp();
                });
        PillButton reset = new PillButton("Reiniciar");
        reset.setName("action.reset");
        reset.addActionListener(
                e -> {
                    if (actions != null) actions.reset();
                });
        pause.setName("action.pause");
        pause.setPreferredSize(new Dimension(100, 35));
        pause.addActionListener(
                e -> {
                    if (actions != null) actions.togglePause();
                });
        step.setName("action.step");
        step.setToolTipText("Avança um quadro de 1/60 s com a simulação pausada (N)");
        step.addActionListener(
                e -> {
                    if (actions != null) actions.stepOnce();
                });
        rate.setName("parameter.rate");
        rate.setSelectedIndex(1);
        rate.setFont(Theme.font(Font.BOLD, 12));
        rate.setBackground(Theme.PAPER);
        rate.setPreferredSize(new Dimension(65, 34));
        rate.setFocusable(false);
        rate.setToolTipText("Velocidade do tempo da simulação");
        rate.addActionListener(
                e -> {
                    if (actions != null)
                        actions.changeRate(new double[] {.5, 1, 2}[rate.getSelectedIndex()]);
                });
        commands.add(help);
        commands.add(reset);
        commands.add(step);
        commands.add(rate);
        commands.add(pause);
        header.add(commands, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(220, 600));
        JPanel content = Theme.column();
        content.add(Theme.heading("Explore os movimentos", 16));
        content.add(Box.createVerticalStrut(4));
        content.add(Theme.label("6 comportamentos · 2 combinações", 11, Theme.MUTED));
        String group = "";
        for (AgentProfile scenario : AgentProfile.values()) {
            if (!scenario.group().equals(group)) {
                group = scenario.group();
                content.add(Box.createVerticalStrut(21));
                JLabel label = Theme.label(group, 10, Theme.MUTED);
                label.setFont(Theme.font(Font.BOLD, 10));
                content.add(label);
                content.add(Box.createVerticalStrut(7));
            }
            ScenarioButton button = new ScenarioButton(scenario);
            button.setName("scenario." + scenario.name());
            button.addActionListener(
                    e -> {
                        if (actions != null) actions.selectScenario(scenario);
                    });
            scenarioButtons.put(scenario, button);
            content.add(button);
            content.add(Box.createVerticalStrut(5));
        }
        content.add(Box.createVerticalStrut(18));
        JLabel note = Theme.label("1–8  cenários     D  vetores", 11, Theme.MUTED);
        content.add(note);
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        Theme.styleScroll(scroll);
        sidebar.add(scroll);
        return sidebar;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        JPanel heading = Theme.column();
        eyebrow.setFont(Theme.font(Font.BOLD, 10));
        heading.add(eyebrow);
        heading.add(Box.createVerticalStrut(4));
        heading.add(title);
        heading.add(Box.createVerticalStrut(7));
        description.setPreferredSize(new Dimension(450, 48));
        description.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        heading.add(description);
        heading.add(Box.createVerticalStrut(14));
        JPanel toolbar = new JPanel(new BorderLayout(6, 0));
        toolbar.setOpaque(false);
        JPanel tools = Theme.row();
        for (Tool tool : Tool.values()) {
            PillButton button = new PillButton(tool.label());
            button.setName("tool." + tool.name());
            button.addActionListener(
                    e -> {
                        if (actions != null) actions.selectTool(tool);
                    });
            toolButtons.put(tool, button);
            tools.add(button);
        }
        toolbar.add(tools, BorderLayout.WEST);
        JPanel display = Theme.row();
        vectors.setOpaque(false);
        vectors.setForeground(Theme.MUTED);
        trails.setOpaque(false);
        trails.setForeground(Theme.MUTED);
        display.add(vectors);
        display.add(trails);
        toolbar.add(display, BorderLayout.EAST);
        heading.add(toolbar);
        center.add(heading, BorderLayout.NORTH);
        center.add(map, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(8, 7));
        bottom.setOpaque(false);
        hint.setPreferredSize(new Dimension(450, 35));
        bottom.add(hint, BorderLayout.NORTH);
        JPanel legend = Theme.row();
        legend.add(Theme.label("● Velocidade", 11, Theme.BLUE));
        legend.add(Theme.label("● Força resultante", 11, Theme.ORANGE));
        legend.add(Theme.label("● Rota", 11, Theme.GREEN));
        bottom.add(legend, BorderLayout.WEST);
        bottom.add(elapsed, BorderLayout.EAST);
        center.add(bottom, BorderLayout.SOUTH);
        return center;
    }

    private JPanel buildInspector() {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setOpaque(false);
        shell.setPreferredSize(new Dimension(258, 620));
        JPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(Theme.padding(21, 19, 19, 19));
        JPanel content = Theme.column();
        content.setBorder(Theme.padding(0, 0, 0, 8));
        content.add(Theme.label("PEDESTRE SELECIONADO", 10, Theme.MUTED));
        content.add(Box.createVerticalStrut(8));
        content.add(name);
        content.add(motion);
        content.add(Box.createVerticalStrut(18));
        JPanel metrics = new JPanel(new GridLayout(2, 2, 8, 15));
        metrics.setOpaque(false);
        metrics.add(metric(speedMetric, "velocidade · u/s"));
        metrics.add(metric(distanceMetric, "até o destino · u"));
        metrics.add(metric(forceMetric, "força · u/s²"));
        metrics.add(metric(contactMetric, "contatos físicos"));
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 106));
        content.add(metrics);
        content.add(Box.createVerticalStrut(22));
        content.add(sectionLabel("MOVIMENTO", null));
        content.add(Box.createVerticalStrut(12));
        content.add(sectionLabel("Velocidade máxima", speedValue));
        content.add(speedSlider);
        content.add(Box.createVerticalStrut(10));
        content.add(sectionLabel("Força máxima", forceValue));
        content.add(forceSlider);
        content.add(Box.createVerticalStrut(20));
        content.add(sectionLabel("PESOS DOS COMPORTAMENTOS", null));
        content.add(Box.createVerticalStrut(5));
        content.add(Theme.label("Ajustes deste pedestre", 10, Theme.MUTED));
        content.add(Box.createVerticalStrut(12));
        for (BehaviorType type : BehaviorType.values()) {
            JLabel label = Theme.label(type.title(), 11, Theme.INK);
            JLabel value = Theme.label("", 11, Theme.GREEN);
            weightTitles.put(type, label);
            weightValues.put(type, value);
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.add(label, BorderLayout.WEST);
            row.add(value, BorderLayout.EAST);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            content.add(row);
            JSlider slider = slider(0, 40, 10);
            slider.setName("weight." + type.name());
            slider.setToolTipText(type.description() + ". Peso zero desativa a contribuição.");
            weightSliders.put(type, slider);
            slider.addChangeListener(
                    e -> {
                        if (!syncing && actions != null) {
                            double weight = slider.getValue() / 10.0;
                            actions.changeWeight(type, weight);
                            value.setText(decimal(weight));
                        }
                    });
            content.add(slider);
            content.add(Box.createVerticalStrut(7));
        }
        content.add(Box.createVerticalStrut(9));
        JTextArea formula = textArea(11, Theme.MUTED);
        formula.setText(
                "F = Σ (peso × força)\n"
                    + "A soma é limitada pela força máxima.\n"
                    + "Zero retira a contribuição do comportamento.");
        formula.setPreferredSize(new Dimension(205, 62));
        formula.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        content.add(formula);
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.PAPER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        Theme.styleScroll(scroll);
        card.add(scroll);
        shell.add(card);
        return shell;
    }

    public void showScenario(AgentProfile scenario) {
        scenarioButtons.forEach((key, button) -> button.setActive(key == scenario));
        eyebrow.setText(scenario.group() + "  /  CENÁRIO " + scenario.number());
        title.setText(scenario.subtitle());
        description.setText(scenario.description());
        setMessage(scenario.instruction());
    }

    public void showAgent(Agent agent) {
        syncing = true;
        name.setText(agent.name());
        name.setForeground(Theme.AGENTS[agent.id() % Theme.AGENTS.length]);
        map.setSelected(agent);
        speedSlider.setValue((int) agent.maxSpeed());
        forceSlider.setValue((int) agent.maxForce());
        speedValue.setText((int) agent.maxSpeed() + " u/s");
        forceValue.setText((int) agent.maxForce() + " u/s²");
        for (BehaviorType type : BehaviorType.values()) {
            boolean active = agent.hasBehavior(type);
            weightSliders.get(type).setEnabled(active);
            weightSliders.get(type).setValue((int) Math.round(agent.weight(type) * 10));
            weightValues.get(type).setText(active ? decimal(agent.weight(type)) : "—");
            weightTitles.get(type).setForeground(active ? Theme.INK : new Color(0xA5B1A7));
        }
        syncing = false;
    }

    public void showTool(Tool tool) {
        toolButtons.forEach((key, button) -> button.setActive(key == tool));
    }

    public void setMessage(String message) {
        hint.setText(message);
    }

    public void setPaused(boolean value) {
        pause.setText(value ? "Continuar" : "Pausar");
        pause.setActive(!value);
        step.setEnabled(value);
        map.setPaused(value);
    }

    public void updateLive(World world, Agent agent, boolean paused) {
        speedMetric.setText(Integer.toString((int) Math.round(agent.speed())));
        distanceMetric.setText(
                world.scenario() == AgentProfile.WANDER
                                || world.scenario() == AgentProfile.FREE_WALK
                        ? "—"
                        : Integer.toString(
                                (int) Math.round(agent.position().distance(world.target()))));
        forceMetric.setText(Integer.toString((int) Math.round(agent.steering().length())));
        contactMetric.setText(Integer.toString(agent.contacts()));
        motion.setText(
                paused ? "Simulação pausada" : agent.speed() < 1 ? "Em repouso" : "Em movimento");
        int seconds = (int) world.elapsed();
        elapsed.setText(String.format(Locale.ROOT, "TEMPO  %02d:%02d", seconds / 60, seconds % 60));
        map.repaint();
    }

    private static JTextArea textArea(int size, Color color) {
        JTextArea text = new JTextArea();
        text.setFont(Theme.font(Font.PLAIN, size));
        text.setForeground(color);
        text.setOpaque(false);
        text.setEditable(false);
        text.setFocusable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        return text;
    }

    private static JPanel metric(JLabel value, String description) {
        JPanel p = Theme.column();
        p.add(value);
        p.add(Theme.label(description, 10, Theme.MUTED));
        return p;
    }

    private static JPanel sectionLabel(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel label = Theme.label(title, 11, Theme.INK);
        label.setFont(Theme.font(Font.BOLD, 11));
        p.add(label, BorderLayout.WEST);
        if (value != null) p.add(value, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        return p;
    }

    private static String decimal(double value) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.1f", value);
    }

    private static JSlider slider(int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        slider.setOpaque(false);
        slider.setBorder(null);
        slider.setPreferredSize(new Dimension(180, 24));
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        slider.setUI(
                new BasicSliderUI(slider) {
                    @Override
                    public void paintTrack(Graphics graphics) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        Theme.quality(g);
                        int y = trackRect.y + trackRect.height / 2;
                        g.setColor(Theme.LINE);
                        g.fillRoundRect(trackRect.x, y - 2, trackRect.width, 4, 4, 4);
                        g.setColor(slider.isEnabled() ? Theme.GREEN : Theme.LINE);
                        g.fillRoundRect(
                                trackRect.x,
                                y - 2,
                                Math.max(0, thumbRect.x - trackRect.x + thumbRect.width / 2),
                                4,
                                4,
                                4);
                        g.dispose();
                    }

                    @Override
                    public void paintThumb(Graphics graphics) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        Theme.quality(g);
                        g.setColor(slider.isEnabled() ? Theme.GREEN : Theme.LINE);
                        g.fillOval(
                                thumbRect.x + thumbRect.width / 2 - 6,
                                thumbRect.y + thumbRect.height / 2 - 6,
                                12,
                                12);
                        g.dispose();
                    }

                    @Override
                    public void paintFocus(Graphics graphics) {
                        if (slider.hasFocus()) {
                            graphics.setColor(Theme.ORANGE);
                            graphics.drawRoundRect(
                                    0, 0, slider.getWidth() - 1, slider.getHeight() - 1, 6, 6);
                        }
                    }
                });
        return slider;
    }

    private static final class RoundedPanel extends JPanel {
        RoundedPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Theme.quality(g);
            g.setColor(Theme.PAPER);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g.setColor(Theme.LINE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class ScenarioButton extends JButton {
        private final AgentProfile scenario;
        private boolean active;

        ScenarioButton(AgentProfile value) {
            scenario = value;
            setText(value.title());
            setToolTipText(value.subtitle());
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
            setPreferredSize(new Dimension(216, 54));
            setMinimumSize(new Dimension(195, 54));
            setAlignmentX(LEFT_ALIGNMENT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            getAccessibleContext().setAccessibleName(value.number() + " " + value.title());
        }

        void setActive(boolean value) {
            active = value;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            Theme.quality(g);
            g.setColor(
                    active ? Theme.INK : getModel().isRollover() ? Theme.SOFT : Theme.BACKGROUND);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g.setColor(active ? new Color(0xA4C6B2) : Theme.MUTED);
            g.setFont(Theme.font(Font.PLAIN, 11));
            g.drawString(scenario.number(), 13, 23);
            g.setColor(active ? Theme.PAPER : Theme.INK);
            g.setFont(Theme.font(Font.BOLD, 13));
            g.drawString(scenario.title(), 40, 24);
            g.setFont(Theme.font(Font.PLAIN, 10));
            g.setColor(active ? new Color(0xB3CABC) : Theme.MUTED);
            g.drawString(
                    scenario.group().equals("COMBINAÇÕES")
                            ? "Forças combinadas"
                            : "Comportamento individual",
                    40,
                    41);
            if (active) {
                g.setColor(new Color(0x9BBC93));
                g.fillOval(getWidth() - 16, 22, 5, 5);
            }
            if (hasFocus()) {
                g.setColor(Theme.ORANGE);
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            }
            g.dispose();
        }
    }
}
