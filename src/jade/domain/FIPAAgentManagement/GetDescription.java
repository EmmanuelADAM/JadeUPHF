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


package jade.domain.FIPAAgentManagement;

import jade.content.AgentAction;

/**
 * This class implements the  get-description   action of the
 *  fipa-agent-management ontology  .
 * This action is supported by (and can be requested to) the AMS.
 *
 * @author Fabio Bellifemine - CSELT S.p.A.
 * @version $Date: 2003-11-24 14:47:00 +0100 (lun, 24 nov 2003) $ $Revision: 4597 $
 * @see FIPAManagementOntology
 */
public class GetDescription implements AgentAction {

    /**
     * Default constructor. A default constructor is needed for
     * ontological classes.
     */
    public GetDescription() {
    }

}
