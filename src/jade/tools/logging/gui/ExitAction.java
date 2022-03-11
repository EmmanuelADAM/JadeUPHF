package jade.tools.logging.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

class ExitAction extends AbstractAction {
    private final LogManagerGUI gui;

    public ExitAction(LogManagerGUI gui) {
        super("Exit");
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent e) {
        gui.exit();
    }
}
