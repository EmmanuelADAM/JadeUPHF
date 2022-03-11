/*
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
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


package jade.tools.rma;

import jade.gui.AboutJadeAction;

import javax.swing.*;
import java.awt.*;

/**
 * @author Francisco Regi, Andrea Soracchi - Universita` di Parma
 * @version $Date: 2004-07-19 14:49:58 +0200 (lun, 19 lug 2004) $ $Revision: 5214 $
 */
class MainMenu extends JMenuBar {

    private final ActionProcessor actPro;
    private RMAAction obj;
    private JMenu menu;
    private JMenuItem tmp;


    void paintM(boolean enable, RMAAction obj) {
        tmp = menu.add(obj);
        tmp.setEnabled(enable);
    }


    public MainMenu(Frame mainWnd, ActionProcessor actPro) {

        super();
        this.actPro = actPro;

        menu = new JMenu("File");

        paintM(true, ActionProcessor.actions.get(ActionProcessor.CLOSE_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.EXIT_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.SHUTDOWN_ACTION));
        add(menu);

        menu = new JMenu("Actions");

        paintM(true, ActionProcessor.actions.get(ActionProcessor.START_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.KILL_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.SUSPEND_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.RESUME_ACTION));

        // AR: removed for JADE 3.2
        // paintM(true,(RMAAction)actPro.actions.get(actPro.CHANGE_AGENT_OWNERSHIP_ACTION));

        paintM(true, ActionProcessor.actions.get(ActionProcessor.CUSTOM_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.MOVEAGENT_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.CLONEAGENT_ACTION));
        menu.addSeparator();
        paintM(true, ActionProcessor.actions.get(ActionProcessor.LOADAGENT_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.SAVEAGENT_ACTION));
        menu.addSeparator();
        paintM(true, ActionProcessor.actions.get(ActionProcessor.FREEZEAGENT_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.THAWAGENT_ACTION));
        add(menu);

        menu = new JMenu("Tools");

        paintM(true, ActionProcessor.actions.get(ActionProcessor.SNIFFER_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.DUMMYAG_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.SHOWDF_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.INTROSPECTOR_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.LOGGERAG_ACTION));
        add(menu);

        menu = new JMenu("Remote Platforms");
        paintM(true, ActionProcessor.actions.get(ActionProcessor.ADDREMOTEPLATFORM_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.ADDREMOTEPLATFORMFROMURL_ACTION));
        menu.addSeparator();
        paintM(true, ActionProcessor.actions.get(ActionProcessor.VIEWPLATFORM_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.REFRESHAPDESCRIPTION_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.REMOVEREMOTEAMS_ACTION));
        paintM(true, ActionProcessor.actions.get(ActionProcessor.REFRESHAMSAGENT_ACTION));
        add(menu);
        menu = new JMenu("Help");
        tmp = menu.add(new AboutJadeAction((JFrame) mainWnd));
        add(menu);

        // builds the popupmenu

    } // End Builder
}
