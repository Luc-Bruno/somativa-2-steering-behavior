package br.edu.steering.view;

import br.edu.steering.model.*;
import br.edu.steering.model.AgentProfile;
import br.edu.steering.model.behavior.Flee;
import br.edu.steering.model.math.Vec2;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.*;

/** Desenha apenas o estado do Model; não executa física nem altera os agentes. */
public final class MapPanel extends JPanel {
    private final World world;
    private Agent selected;
    private boolean vectors;
    private boolean trails = true;
    private boolean paused;
    private double scale = 1, offsetX, offsetY;
    private final List<Vec2> grass = new ArrayList<>();

    public MapPanel(World world) {
        this.world = world;
        selected = world.firstAgent();
        setOpaque(false);
        setPreferredSize(new Dimension(860, 650));
        setMinimumSize(new Dimension(300, 300));
        setName("map");
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        Random random = new Random(42);
        for (int i = 0; i < 220; i++)
            grass.add(new Vec2(35 + random.nextDouble() * 1010, 35 + random.nextDouble() * 650));
    }

    public void setSelected(Agent agent) {
        selected = agent;
        repaint();
    }

    public void setVectors(boolean value) {
        vectors = value;
        repaint();
    }

    public void setTrails(boolean value) {
        trails = value;
        repaint();
    }

    public void setPaused(boolean value) {
        paused = value;
        repaint();
    }

    public boolean vectorsVisible() {
        return vectors;
    }

    private void transform() {
        scale =
                Math.max(
                        0.01,
                        Math.min(
                                (getWidth() - 28) / World.WIDTH,
                                (getHeight() - 28) / World.HEIGHT));
        offsetX = (getWidth() - World.WIDTH * scale) / 2;
        offsetY = (getHeight() - World.HEIGHT * scale) / 2;
    }

    public Vec2 toWorld(Point point) {
        transform();
        double x = (point.x - offsetX) / scale, y = (point.y - offsetY) / scale;
        return x < 0 || y < 0 || x > World.WIDTH || y > World.HEIGHT ? null : new Vec2(x, y);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D outer = (Graphics2D) graphics.create();
        Theme.quality(outer);
        outer.setColor(Theme.PAPER);
        outer.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
        transform();
        Graphics2D g = (Graphics2D) outer.create();
        g.translate(offsetX, offsetY);
        g.scale(scale, scale);
        g.clip(new RoundRectangle2D.Double(0, 0, World.WIDTH, World.HEIGHT, 20, 20));
        drawPark(g);
        if (world.scenario().followsPath()) drawRoute(g);
        drawTarget(g);
        if (trails) drawTrails(g);
        for (Obstacle obstacle : world.obstacles()) drawObstacle(g, obstacle);
        if (vectors) drawDebug(g);
        for (Agent agent : world.agents()) drawPerson(g, agent);
        if (!world.draftRoute().isEmpty()) drawDraft(g);
        g.dispose();
        outer.setColor(Theme.LINE);
        outer.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
        badge(outer, 22, 22, "MAPA DE TESTES", Theme.INK, Theme.PAPER);
        String state = paused ? "PAUSADO" : "EM MOVIMENTO";
        badge(outer, getWidth() - 151, 22, state, paused ? Theme.ORANGE : Theme.GREEN, Theme.PAPER);
        outer.dispose();
    }

    private void drawPark(Graphics2D g) {
        g.setColor(new Color(0xE5ECD8));
        g.fillRect(0, 0, 1080, 720);
        g.setColor(new Color(0xD1DEC2));
        g.setStroke(new BasicStroke(1.2f));
        for (Vec2 p : grass) {
            g.draw(new Line2D.Double(p.x(), p.y(), p.x() + 2, p.y() - 4));
            g.draw(new Line2D.Double(p.x() + 3, p.y(), p.x() + 5, p.y() - 3));
        }
        Path2D walk = new Path2D.Double();
        walk.moveTo(-10, 350);
        walk.lineTo(1090, 350);
        walk.moveTo(165, 0);
        walk.lineTo(165, 720);
        walk.moveTo(165, 550);
        walk.lineTo(350, 550);
        walk.curveTo(470, 550, 570, 490, 760, 550);
        walk.moveTo(820, 350);
        walk.lineTo(900, 190);
        walk.lineTo(1085, 190);
        g.setColor(new Color(0xD9D6BC));
        g.setStroke(new BasicStroke(65, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(walk);
        g.setColor(new Color(0xF1E7D1));
        g.setStroke(new BasicStroke(59, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(walk);
        g.setColor(new Color(255, 255, 255, 65));
        g.setStroke(new BasicStroke(2));
        g.draw(walk);
        g.setColor(new Color(0xC6D3B9));
        g.setStroke(new BasicStroke(2));
        g.draw(new RoundRectangle2D.Double(20, 20, 1040, 680, 26, 26));
        g.setFont(Theme.font(Font.BOLD, 12));
        g.setColor(new Color(0x7F997F));
        g.drawString("PRAÇA DAS ACÁCIAS", 425, 51);
        g.setFont(Theme.font(Font.PLAIN, 11));
        g.setColor(new Color(0x8D9C82));
        g.drawString("JARDIM NORTE", 575, 165);
        g.drawString("PASSEIO SUL", 440, 592);
        // Escala gráfica em unidades do mundo, independente do tamanho da janela.
        g.setStroke(new BasicStroke(2));
        g.drawLine(45, 678, 145, 678);
        g.drawLine(45, 673, 45, 683);
        g.drawLine(145, 673, 145, 683);
        g.setFont(Theme.font(Font.PLAIN, 11));
        g.drawString("100 unidades", 57, 698);
    }

    private void drawRoute(Graphics2D g) {
        Path2D path = new Path2D.Double();
        boolean first = true;
        for (Vec2 p : world.route().points()) {
            if (first) {
                path.moveTo(p.x(), p.y());
                first = false;
            } else path.lineTo(p.x(), p.y());
        }
        g.setColor(new Color(37, 122, 99, 21));
        g.setStroke(new BasicStroke(48, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(path);
        g.setColor(new Color(0x4B9987));
        g.setStroke(
                new BasicStroke(
                        2.5f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND,
                        10,
                        new float[] {7, 8},
                        0));
        g.draw(path);
        int i = 1;
        for (Vec2 p : world.route().points()) {
            g.setColor(Theme.PAPER);
            g.fill(new Ellipse2D.Double(p.x() - 10, p.y() - 10, 20, 20));
            g.setColor(Theme.GREEN);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new Ellipse2D.Double(p.x() - 10, p.y() - 10, 20, 20));
            g.setFont(Theme.font(Font.BOLD, 10));
            g.drawString(Integer.toString(i++), (float) p.x() - 3, (float) p.y() + 4);
        }
    }

    private void drawTarget(Graphics2D g) {
        AgentProfile scenario = world.scenario();
        if (scenario == AgentProfile.WANDER || scenario == AgentProfile.FREE_WALK) return;
        Vec2 p = world.target();
        boolean threat = scenario == AgentProfile.FLEE;
        Color color = threat ? Theme.ORANGE : Theme.GREEN;
        if (scenario == AgentProfile.ARRIVE || scenario.followsPath() || threat) {
            double radius = threat ? Flee.PERCEPTION_RADIUS : 125;
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 13));
            g.fill(new Ellipse2D.Double(p.x() - radius, p.y() - radius, radius * 2, radius * 2));
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 65));
            g.setStroke(
                    new BasicStroke(
                            1.3f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND,
                            10,
                            new float[] {4, 7},
                            0));
            g.draw(new Ellipse2D.Double(p.x() - radius, p.y() - radius, radius * 2, radius * 2));
        }
        double pulse = 18 + Math.sin(world.elapsed() * 3) * 2;
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        g.fill(new Ellipse2D.Double(p.x() - pulse, p.y() - pulse, pulse * 2, pulse * 2));
        g.setColor(Theme.PAPER);
        g.fill(new Ellipse2D.Double(p.x() - 12, p.y() - 12, 24, 24));
        g.setColor(color);
        g.setStroke(new BasicStroke(2));
        g.draw(new Ellipse2D.Double(p.x() - 12, p.y() - 12, 24, 24));
        if (threat) {
            Path2D triangle = new Path2D.Double();
            triangle.moveTo(p.x(), p.y() - 7);
            triangle.lineTo(p.x() + 6, p.y() + 5);
            triangle.lineTo(p.x() - 6, p.y() + 5);
            triangle.closePath();
            g.fill(triangle);
        } else {
            g.fill(new Ellipse2D.Double(p.x() - 4, p.y() - 4, 8, 8));
        }
        String label = threat ? "AMEAÇA" : scenario.followsPath() ? "DESTINO" : "ALVO";
        g.setFont(Theme.font(Font.BOLD, 10));
        g.drawString(label, (float) p.x() - 20, (float) p.y() + 31);
    }

    private void drawTrails(Graphics2D g) {
        for (Agent agent : world.agents()) {
            Color color = Theme.AGENTS[agent.id() % Theme.AGENTS.length];
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 85));
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D line = new Path2D.Double();
            boolean first = true;
            for (Vec2 p : agent.trail()) {
                if (first) {
                    line.moveTo(p.x(), p.y());
                    first = false;
                } else line.lineTo(p.x(), p.y());
            }
            g.draw(line);
        }
    }

    private void drawObstacle(Graphics2D g, Obstacle obstacle) {
        Vec2 p = obstacle.position();
        double r = obstacle.radius();
        g.setColor(new Color(36, 64, 42, 25));
        g.fill(new Ellipse2D.Double(p.x() - r + 7, p.y() - r + 10, r * 2, r * 2));
        switch (obstacle.kind()) {
            case TREE -> {
                g.setColor(new Color(0x436D4C));
                g.fill(new Ellipse2D.Double(p.x() - r, p.y() - r, r * 2, r * 2));
                g.setColor(new Color(0x5F8957));
                g.fill(new Ellipse2D.Double(p.x() - r * .95, p.y() - r * .95, r * 1.5, r * 1.5));
                g.setColor(new Color(0x7B9F65));
                g.fill(new Ellipse2D.Double(p.x() - r * .65, p.y() - r * .9, r * 1.05, r * 1.05));
                g.setColor(new Color(0x73975D));
                g.fill(new Ellipse2D.Double(p.x() - r * .1, p.y() - r * .2, r, r));
                g.setColor(new Color(255, 255, 255, 32));
                g.fill(new Ellipse2D.Double(p.x() - r * .55, p.y() - r * .65, r * .5, r * .3));
            }
            case GARDEN -> {
                g.setColor(new Color(0xAEBB8E));
                g.fill(new Ellipse2D.Double(p.x() - r, p.y() - r, r * 2, r * 2));
                g.setColor(new Color(0x73966A));
                g.fill(new Ellipse2D.Double(p.x() - r + 5, p.y() - r + 5, r * 2 - 10, r * 2 - 10));
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    double x = p.x() + Math.cos(a) * r * .5, y = p.y() + Math.sin(a) * r * .5;
                    g.setColor(i % 2 == 0 ? new Color(0xDBA47D) : new Color(0xE9D8A8));
                    g.fill(new Ellipse2D.Double(x - 4, y - 4, 8, 8));
                }
            }
            case FOUNTAIN -> {
                g.setColor(new Color(0xBEC6BA));
                g.fill(new Ellipse2D.Double(p.x() - r, p.y() - r, r * 2, r * 2));
                g.setColor(new Color(0xF4F1E4));
                g.fill(new Ellipse2D.Double(p.x() - r + 5, p.y() - r + 5, r * 2 - 10, r * 2 - 10));
                g.setColor(new Color(0xA9CCC5));
                g.fill(
                        new Ellipse2D.Double(
                                p.x() - r + 10, p.y() - r + 10, r * 2 - 20, r * 2 - 20));
                g.setColor(new Color(0xD1E7DE));
                g.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < 3; i++) {
                    double ring = 15 + i * 10 + Math.sin(world.elapsed() * 2) * 2;
                    g.draw(new Ellipse2D.Double(p.x() - ring, p.y() - ring, ring * 2, ring * 2));
                }
                g.setColor(new Color(0xEEF3E9));
                g.fill(new Ellipse2D.Double(p.x() - 8, p.y() - 8, 16, 16));
            }
            case BENCH -> {
                g.setColor(new Color(0x566A5B));
                g.fill(new RoundRectangle2D.Double(p.x() - 17, p.y() - 17, 5, 34, 2, 2));
                g.fill(new RoundRectangle2D.Double(p.x() + 12, p.y() - 17, 5, 34, 2, 2));
                g.setColor(new Color(0xB68D62));
                for (int i = 0; i < 4; i++)
                    g.fill(
                            new RoundRectangle2D.Double(
                                    p.x() - 23, p.y() - 14 + i * 8, 46, 5, 2, 2));
            }
        }
        if (vectors) {
            g.setColor(new Color(197, 116, 73, 105));
            g.setStroke(
                    new BasicStroke(
                            1,
                            BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND,
                            10,
                            new float[] {3, 5},
                            0));
            g.draw(new Ellipse2D.Double(p.x() - r, p.y() - r, r * 2, r * 2));
            if (world.firstAgent().hasBehavior(BehaviorType.AVOIDANCE)) {
                double buffer =
                        r
                                + Agent.RADIUS
                                + br.edu.steering.model.behavior.ObstacleAvoidance.SAFETY_MARGIN;
                g.setColor(new Color(197, 116, 73, 48));
                g.draw(
                        new Ellipse2D.Double(
                                p.x() - buffer, p.y() - buffer, buffer * 2, buffer * 2));
            }
        }
    }

    private void drawPerson(Graphics2D g, Agent agent) {
        Vec2 p = agent.position();
        Color coat = Theme.AGENTS[agent.id() % Theme.AGENTS.length];
        boolean active = agent == selected;
        if (active) {
            g.setColor(new Color(coat.getRed(), coat.getGreen(), coat.getBlue(), 28));
            g.fill(new Ellipse2D.Double(p.x() - 26, p.y() - 26, 52, 52));
            g.setColor(coat);
            g.setStroke(new BasicStroke(1.7f));
            g.draw(new Ellipse2D.Double(p.x() - 25, p.y() - 25, 50, 50));
        }
        Graphics2D person = (Graphics2D) g.create();
        person.translate(p.x(), p.y());
        person.rotate(agent.heading().angle() + Math.PI / 2);
        double step = Math.sin(agent.walkingPhase()) * Math.min(5, agent.speed() / 20);
        person.setColor(new Color(32, 48, 38, 25));
        person.fill(new Ellipse2D.Double(-13, -16, 30, 39));
        person.setColor(new Color(0x3D4F51));
        person.fill(new RoundRectangle2D.Double(-8, 7 + step, 6, 14, 5, 5));
        person.fill(new RoundRectangle2D.Double(2, 7 - step, 6, 14, 5, 5));
        person.setColor(new Color(0x263C3E));
        person.fill(new RoundRectangle2D.Double(-8, 17 + step, 7, 6, 3, 3));
        person.fill(new RoundRectangle2D.Double(2, 17 - step, 7, 6, 3, 3));
        person.setColor(coat.darker());
        person.fill(new RoundRectangle2D.Double(-15, -7 - step * .5, 7, 19, 6, 6));
        person.fill(new RoundRectangle2D.Double(8, -7 + step * .5, 7, 19, 6, 6));
        person.setColor(coat);
        person.fill(new RoundRectangle2D.Double(-10, -9, 20, 23, 10, 10));
        person.setColor(new Color(255, 255, 255, 40));
        person.fill(new RoundRectangle2D.Double(-8, -7, 6, 15, 5, 5));
        person.setColor(new Color(0xD8B18B));
        person.fill(new Ellipse2D.Double(-7, -19, 14, 15));
        person.setColor(new Color(0x4C3F35));
        person.fill(new Arc2D.Double(-8, -21, 16, 17, 10, 230, Arc2D.CHORD));
        if (active) {
            person.setColor(coat);
            Path2D arrow = new Path2D.Double();
            arrow.moveTo(0, -34);
            arrow.lineTo(-4, -28);
            arrow.lineTo(4, -28);
            arrow.closePath();
            person.fill(arrow);
        }
        person.dispose();
        g.setFont(Theme.font(active ? Font.BOLD : Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        int width = fm.stringWidth(agent.name()) + 14;
        g.setColor(new Color(255, 255, 255, 230));
        g.fill(new RoundRectangle2D.Double(p.x() - width / 2.0, p.y() + 30, width, 21, 8, 8));
        g.setColor(Theme.INK);
        g.drawString(
                agent.name(),
                (float) p.x() - fm.stringWidth(agent.name()) / 2f,
                (float) p.y() + 45);
    }

    private void drawDebug(Graphics2D g) {
        Agent a = selected;
        if (a == null) return;
        if (a.sensorEnd() != null) {
            g.setColor(new Color(197, 116, 73, 18));
            g.setStroke(
                    new BasicStroke(
                            (float) (Agent.RADIUS * 2 + 20),
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND));
            g.draw(
                    new Line2D.Double(
                            a.position().x(),
                            a.position().y(),
                            a.sensorEnd().x(),
                            a.sensorEnd().y()));
            g.setColor(Theme.ORANGE);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(
                    new Line2D.Double(
                            a.position().x(),
                            a.position().y(),
                            a.sensorEnd().x(),
                            a.sensorEnd().y()));
            if (a.avoidedObstacle() != null) {
                Vec2 p = a.avoidedObstacle();
                g.setStroke(new BasicStroke(2));
                g.draw(new Ellipse2D.Double(p.x() - 8, p.y() - 8, 16, 16));
            }
        }
        if (a.pathProjection() != null && a.steeringTarget() != null) {
            Vec2 projection = a.pathProjection(), target = a.steeringTarget();
            g.setColor(new Color(0x8E76A6));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(
                    new Line2D.Double(
                            a.position().x(), a.position().y(), projection.x(), projection.y()));
            g.draw(new Ellipse2D.Double(target.x() - 5, target.y() - 5, 10, 10));
            if (world.scenario() == AgentProfile.WANDER
                    || world.scenario() == AgentProfile.FREE_WALK)
                g.draw(new Ellipse2D.Double(projection.x() - 38, projection.y() - 38, 76, 76));
        }
        arrow(g, a.position(), a.velocity().multiply(.58), Theme.BLUE);
        arrow(g, a.position(), a.steering().multiply(.21), Theme.ORANGE);
    }

    private void drawDraft(Graphics2D g) {
        g.setColor(Theme.ORANGE);
        g.setFont(Theme.font(Font.BOLD, 11));
        for (Vec2 p : world.draftRoute()) {
            g.fill(new Ellipse2D.Double(p.x() - 4, p.y() - 4, 8, 8));
        }
        if (world.draftRoute().size() == 1) {
            Vec2 p = world.draftRoute().get(0);
            g.drawString("Adicione o próximo ponto", (float) p.x() + 12, (float) p.y() - 12);
        }
    }

    private void arrow(Graphics2D g, Vec2 origin, Vec2 vector, Color color) {
        if (vector.length() < 1) return;
        Vec2 end = origin.add(vector);
        g.setColor(color);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(origin.x(), origin.y(), end.x(), end.y()));
        Vec2 back = vector.normalized().multiply(-8),
                side = vector.normalized().perpendicular().multiply(4);
        Path2D head = new Path2D.Double();
        head.moveTo(end.x(), end.y());
        head.lineTo(end.add(back).add(side).x(), end.add(back).add(side).y());
        head.lineTo(end.add(back).subtract(side).x(), end.add(back).subtract(side).y());
        head.closePath();
        g.fill(head);
    }

    private void badge(
            Graphics2D g, int x, int y, String text, Color foreground, Color background) {
        g.setFont(Theme.font(Font.BOLD, 10));
        int width = g.getFontMetrics().stringWidth(text) + 20;
        g.setColor(
                new Color(background.getRed(), background.getGreen(), background.getBlue(), 225));
        g.fillRoundRect(x, y, width, 27, 9, 9);
        g.setColor(foreground);
        g.drawString(text, x + 10, y + 18);
    }
}
