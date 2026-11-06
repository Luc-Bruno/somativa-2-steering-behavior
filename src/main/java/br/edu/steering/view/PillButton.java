package br.edu.steering.view;

import java.awt.*;

import javax.swing.*;

public class PillButton extends JButton {
    private boolean active;

    public PillButton(String title) {
        super(title);
        setFont(Theme.font(Font.BOLD, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);
        setBorder(Theme.padding(9, 14, 9, 14));
        setFocusPainted(false);
        setOpaque(false);
    }

    public void setActive(boolean value) {
        active = value;
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.quality(g);
        g.setColor(active ? Theme.GREEN : getModel().isRollover() ? Theme.SOFT : Theme.PAPER);
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        g.setColor(active ? Theme.GREEN : Theme.LINE);
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        g.dispose();
        setForeground(active ? Theme.PAPER : Theme.INK);
        super.paintComponent(graphics);
        if (hasFocus()) {
            Graphics2D focus = (Graphics2D) graphics.create();
            focus.setColor(Theme.ORANGE);
            focus.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 12, 12);
            focus.dispose();
        }
    }
}
