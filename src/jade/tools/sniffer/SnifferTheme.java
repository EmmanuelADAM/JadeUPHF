package jade.tools.sniffer;

import java.awt.Color;
import java.awt.Font;

/**
 * Shared flat colour palette and fonts for the Sniffer GUI.
 */
final class SnifferTheme {

    private SnifferTheme() {
    }

    static final Color WINDOW_BACKGROUND = new Color(0xF4, 0xF6, 0xF8);
    static final Color CANVAS_BACKGROUND = Color.WHITE;
    static final Color BORDER = new Color(0xDC, 0xE1, 0xE7);

    static final Color AGENT_ACTIVE = new Color(0x2F, 0x6F, 0xED);
    static final Color AGENT_INACTIVE = new Color(0xAF, 0xB8, 0xC3);
    static final Color AGENT_EXCLUDED = new Color(0xF5, 0x9E, 0x0B);

    static final Color TEXT_DARK = new Color(0x1F, 0x29, 0x37);
    static final Color TEXT_LIGHT = Color.WHITE;
    static final Color TEXT_MUTED = new Color(0x6B, 0x72, 0x80);

    static final Color TIMELINE = new Color(0xC7, 0xCE, 0xD6);
    static final Color TIMELINE_INDEX = new Color(0x8A, 0x93, 0x9E);
    static final Color ROW_STRIPE = new Color(0xFA, 0xFA, 0xFC);

    static final Color NO_CONVERSATION = new Color(0x9C, 0xA3, 0xAF);

    static final Color[] CONVERSATION_COLORS = {
            new Color(0x2F, 0x6F, 0xED), // blue
            new Color(0xE1, 0x1D, 0x48), // red
            new Color(0x10, 0xB9, 0x81), // green
            new Color(0xF5, 0x9E, 0x0B), // amber
            new Color(0x8B, 0x5C, 0xF6), // violet
            new Color(0x06, 0xB6, 0xD4), // cyan
            new Color(0xEC, 0x48, 0x99), // pink
            new Color(0x8D, 0x6E, 0x4C), // brown
    };

    static final Font FONT_AGENT_LABEL = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    static final Font FONT_PERFORMATIVE = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    static final Font FONT_TIMELINE_INDEX = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    static final Font FONT_STATUS_BAR = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
}
