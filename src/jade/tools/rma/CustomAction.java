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

import jade.core.AID;
import jade.gui.AclGui;
import jade.gui.AgentTree;
import jade.lang.acl.ACLMessage;

import java.awt.*;

/**
 * @author Francisco Regi, Andrea Soracchi - Universita` di Parma
 * @version $Date: 2002-12-13 12:40:04 +0100 (ven, 13 dic 2002) $ $Revision: 3524 $
 */
class CustomAction extends AgentAction {

    private final rma myRMA;
    private final Frame mainWnd;

    public CustomAction(rma anRMA, Frame f, ActionProcessor actPro) {
        super("CustomActionIcon", "Send Message", actPro);
        myRMA = anRMA;
        mainWnd = f;
    }

    public void doAction(AgentTree.AgentNode nod) {

        ACLMessage msg2 = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
        AID rec;

        if (nod instanceof AgentTree.RemoteAgentNode agent) {
            rec = agent.getAMSDescription().getName();
        } else {
            // msg2.addDest(node.getName());
            rec = new AID();
            rec.setName(nod.getName());
        }

        msg2.addReceiver(rec);
        ACLMessage msg = AclGui.editMsgInDialog(msg2, mainWnd);
        if (msg != null)
            myRMA.send(msg);

    }

} 
