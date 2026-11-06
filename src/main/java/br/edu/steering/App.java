package br.edu.steering;

import br.edu.steering.controller.SimulationController;
import br.edu.steering.model.AgentProfile;
import br.edu.steering.model.World;
import br.edu.steering.view.MainView;
import br.edu.steering.view.Theme;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

/** Ponto de entrada: apenas cria e conecta os componentes MVC. */
public final class App {
    private App() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                () -> {
                    Theme.install();
                    World world = new World();
                    world.load(AgentProfile.SAFE_ROUTE);
                    MainView view = new MainView(world);
                    SimulationController controller = new SimulationController(world, view);
                    JFrame frame = new JFrame("Parque autônomo | Somativa 2 - Steering Behavior");
                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    frame.setContentPane(view);
                    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                    frame.setSize(
                            Math.min(1500, screen.width - 70), Math.min(940, screen.height - 80));
                    frame.setMinimumSize(new Dimension(1180, 760));
                    frame.setLocationRelativeTo(null);
                    frame.addWindowListener(
                            new WindowAdapter() {
                                @Override
                                public void windowClosed(WindowEvent e) {
                                    controller.stop();
                                }
                            });
                    frame.setVisible(true);
                    controller.start();
                });
    }
}
