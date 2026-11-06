package br.edu.steering.view;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Identidade visual centralizada; somente a View conhece cores e tipografia. */
public final class Theme {
    public static final Color BACKGROUND = new Color(0xF3F4EF);
    public static final Color PAPER = new Color(0xFFFFFF);
    public static final Color INK = new Color(0x183C35);
    public static final Color MUTED = new Color(0x6D8077);
    public static final Color GREEN = new Color(0x257A63);
    public static final Color SOFT = new Color(0xE7EFE8);
    public static final Color LINE = new Color(0xDFE6DE);
    public static final Color ORANGE = new Color(0xC57449);
    public static final Color BLUE = new Color(0x397FBC);
    public static final Color[] AGENTS = {
        new Color(0xD77C4C), new Color(0x4C8CBA), new Color(0x9273B5)
    };

    private Theme() {}

    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    public static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    public static JLabel heading(String text, int size) {
        JLabel label = label(text, size, INK);
        label.setFont(font(Font.BOLD, size));
        return label;
    }

    public static void install() {
        UIManager.put("Label.font", font(Font.PLAIN, 13));
        UIManager.put("Button.font", font(Font.BOLD, 12));
        UIManager.put("CheckBox.font", font(Font.PLAIN, 12));
        UIManager.put("Slider.background", PAPER);
        UIManager.put("Slider.foreground", GREEN);
        UIManager.put("ToolTip.font", font(Font.PLAIN, 12));
        UIManager.put("ToolTip.background", INK);
        UIManager.put("ToolTip.foreground", PAPER);
    }

    public static JPanel column() {
        JPanel p = new ColumnPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        return p;
    }

    private static final class ColumnPanel extends JPanel implements Scrollable {
        @Override
        protected void addImpl(Component child, Object constraints, int index) {
            if (child instanceof JComponent component) component.setAlignmentX(LEFT_ALIGNMENT);
            super.addImpl(child, constraints, index);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            return Math.max(18, visible.height - 30);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    public static void styleScroll(JScrollPane pane) {
        JScrollBar bar = pane.getVerticalScrollBar();
        bar.setPreferredSize(new Dimension(8, 0));
        bar.setOpaque(false);
        bar.setUI(
                new javax.swing.plaf.basic.BasicScrollBarUI() {
                    @Override
                    protected JButton createDecreaseButton(int orientation) {
                        JButton button = new JButton();
                        button.setPreferredSize(new Dimension(0, 0));
                        return button;
                    }

                    @Override
                    protected JButton createIncreaseButton(int orientation) {
                        return createDecreaseButton(orientation);
                    }

                    @Override
                    protected void paintTrack(Graphics graphics, JComponent c, Rectangle bounds) {}

                    @Override
                    protected void paintThumb(Graphics graphics, JComponent c, Rectangle bounds) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        quality(g);
                        g.setColor(new Color(0xC5D1C6));
                        g.fillRoundRect(
                                bounds.x + 1,
                                bounds.y,
                                Math.max(3, bounds.width - 2),
                                bounds.height,
                                6,
                                6);
                        g.dispose();
                    }
                });
    }

    public static JPanel row() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        return p;
    }

    public static EmptyBorder padding(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    public static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}
