package jade.tools.logging.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

class SetDefaultLoggingSystemAction extends AbstractAction {
    private final LogManagerGUI gui;

    public SetDefaultLoggingSystemAction(LogManagerGUI gui) {
        super("Set default logging system");
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent e) {
        gui.setDefaultLoggingSystem();
    }
}
