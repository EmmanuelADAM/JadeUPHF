package jade.tools.logging.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

class StopManagingLogAction extends AbstractAction {
    private final LogManagerGUI gui;

    public StopManagingLogAction(LogManagerGUI gui) {
        super("Stop Managing Log");
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent e) {
        gui.stopManagingLog();
    }
}
