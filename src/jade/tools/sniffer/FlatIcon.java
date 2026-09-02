package jade.tools.sniffer;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.BiConsumer;

/**
 * Small flat, vector-drawn toolbar/menu icons, replacing the historical bitmap
 * (.gif) icons. Crisp at any resolution, no external image files needed.
 */
final class FlatIcon implements Icon {

    private static final int SIZE = 18;

    private final BiConsumer<Graphics2D, Integer> painter;

    private FlatIcon(BiConsumer<Graphics2D, Integer> painter) {
        this.painter = painter;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        painter.accept(g2, SIZE);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }

    static Icon dot(Color color) {
        return new FlatIcon((g2, s) -> {
            g2.setColor(color);
            g2.fill(new Ellipse2D.Float(3, 3, s - 6, s - 6));
        });
    }

    static Icon ring(Color color) {
        return new FlatIcon((g2, s) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.2f));
            g2.draw(new Ellipse2D.Float(4, 4, s - 8, s - 8));
        });
    }

    static Icon broom() {
        return new FlatIcon((g2, s) -> {
            g2.setColor(SnifferTheme.TEXT_MUTED);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(12, 2, 6, 11);
            g2.setColor(SnifferTheme.AGENT_EXCLUDED);
            int[] xs = {3, 8, 10, 2};
            int[] ys = {16, 9, 11, 17};
            g2.fillPolygon(xs, ys, 4);
        });
    }

    static Icon folderOpen() {
        return new FlatIcon((g2, s) -> {
            g2.setColor(SnifferTheme.AGENT_EXCLUDED);
            g2.fill(new RoundRectangle2D.Float(2, 5, 14, 10, 3, 3));
            g2.setColor(new Color(0xFB, 0xBF, 0x4A));
            g2.fill(new RoundRectangle2D.Float(2, 2, 7, 4, 2, 2));
        });
    }

    static Icon save() {
        return new FlatIcon((g2, s) -> {
            g2.setColor(SnifferTheme.AGENT_ACTIVE);
            g2.fill(new RoundRectangle2D.Float(2, 2, 14, 14, 3, 3));
            g2.setColor(Color.WHITE);
            g2.fillRect(5, 3, 8, 5);
            g2.fillRect(5, 11, 8, 3);
        });
    }

    static Icon saveList() {
        return new FlatIcon((g2, s) -> {
            g2.setColor(new Color(0x10, 0xB9, 0x81));
            g2.fill(new RoundRectangle2D.Float(3, 1, 12, 16, 2, 2));
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawLine(6, 5, 14, 5);
            g2.drawLine(6, 9, 14, 9);
            g2.drawLine(6, 13, 11, 13);
        });
    }

    static Icon exit() {
        return new FlatIcon((g2, s) -> {
            g2.setColor(new Color(0xE1, 0x1D, 0x48));
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Arc2D.Float(2, 2, 14, 14, 50, 260, Arc2D.OPEN));
            g2.drawLine(9, 1, 9, 8);
        });
    }
}
