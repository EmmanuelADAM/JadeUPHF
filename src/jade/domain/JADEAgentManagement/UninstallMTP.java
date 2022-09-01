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

package jade.domain.JADEAgentManagement;

import jade.content.AgentAction;
import jade.core.ContainerID;


/**
 * This class represents the  uninstall-MTP   action of
 * the  JADE-agent-management ontology  .
 * This action can be requested to the JADE AMS to uninstall an MTP from
 * a given container.
 *
 * @author Giovanni Rimassa -  Universita' di Parma
 * @version $Date: 2003-11-24 14:47:00 +0100 (lun, 24 nov 2003) $ $Revision: 4597 $
 */
public class UninstallMTP implements AgentAction {

    private String address;
    private ContainerID container;


    /**
     * Default constructor. A default constructor is necessary for
     * ontological classes.
     */
    public UninstallMTP() {
    }

    /**
     * Retrieve the value of the  address   slot of this
     * action, containing the address URL of the MTP to uninstall.
     *
     * @return The value of the  address   slot, or
     *  null   if no value was set.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set the  address   slot of this action.
     *
     * @param a The address URL of the MTP endpoint to uninstall.
     */
    public void setAddress(String a) {
        address = a;
    }

    /**
     * Retrieve the value of the  container   slot of this
     * action, containing the container identifier of the container
     * where the MTP to uninstall is deployed.
     *
     * @return The value of the  container   slot, or
     *  null   if no value was set.
     */
    public ContainerID getContainer() {
        return container;
    }

    /**
     * Set the  container   slot of this action.
     *
     * @param cid The container identifier of the container where the
     *            MTP to uninstall is deployed.
     */
    public void setContainer(ContainerID cid) {
        container = cid;
    }


}
