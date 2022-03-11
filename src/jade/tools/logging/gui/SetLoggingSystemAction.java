package jade.tools.logging.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

class SetLoggingSystemAction extends AbstractAction {
    private final ContainerLogWindow gui;

    public SetLoggingSystemAction(ContainerLogWindow gui) {
        super("Set logging system");
        this.gui = gui;
    }

    public void actionPerformed(ActionEvent e) {
        gui.setLoggingSystem();
    }
}
