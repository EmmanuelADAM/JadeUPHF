/*
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package jade.tools.sniffer;

import jade.gui.JadeLogoButton;

import javax.swing.*;
import java.awt.*;

/**
 * @author Francisco Regi, Andrea Soracchi - Universita` di Parma
 * <Br>
 * <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
 * @version $Date: 2014-04-03 09:02:19 +0200 (gio, 03 apr 2014) $ $Revision: 6710 $
 */

/**
 * Sets up the toolbar for the main Sniffer Gui
 *
 * @see JToolBar
 */

final public class ToolBar extends JToolBar {

    private final ActionProcessor actPro;
    private SnifferAction obj;

    public ToolBar(ActionProcessor actPro) {
        setBorderPainted(true);
        this.actPro = actPro;
        addSeparator();
        addAction();
        add(Box.createHorizontalGlue());
        JadeLogoButton logo = new JadeLogoButton();
        add(logo);
    }

    private void setButton(JButton b) {
        b.setToolTipText(obj.getActionName());
        b.setText("");
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1, 1, 1, 1));
    }

    private void addAction() {

        obj = actPro.actions.get(ActionProcessor.CLEARCANVAS_ACTION);
        setButton(add(obj));

        addSeparator();

        obj = actPro.actions.get(ActionProcessor.DISPLAYLOGFILE_ACTION);
        setButton(add(obj));

        obj = actPro.actions.get(ActionProcessor.WRITELOGFILE_ACTION);
        setButton(add(obj));

        obj = actPro.actions.get(ActionProcessor.WRITEMESSAGELIST_ACTION);
        setButton(add(obj));

        addSeparator();

        obj = actPro.actions.get(ActionProcessor.DO_SNIFFER_ACTION);
        setButton(add(obj));

        obj = actPro.actions.get(ActionProcessor.DO_NOT_SNIFFER_ACTION);
        setButton(add(obj));

        obj = actPro.actions.get(ActionProcessor.SWOW_ONLY_ACTION);
        setButton(add(obj));


        addSeparator();

        obj = actPro.actions.get(ActionProcessor.EXIT_SNIFFER_ACTION);
        setButton(add(obj));
    }

}
