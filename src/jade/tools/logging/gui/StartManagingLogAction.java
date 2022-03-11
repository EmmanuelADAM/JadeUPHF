package jade.tools.logging.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

class StartManagingLogAction extends AbstractAction {
    private final LogManagerGUI gui;

    public StartManagingLogAction(LogManagerGUI gui) {
        super("Start Managing Log");
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent e) {
        gui.startManagingLog();
    }
}
