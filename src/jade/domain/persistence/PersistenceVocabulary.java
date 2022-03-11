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

package jade.domain.persistence;

import jade.domain.FIPAAgentManagement.ExceptionVocabulary;

/**
 * This interface contains all the string constants for frame and slot
 * names of exceptions defined in the <code>jade-persistence</code>
 * ontology.
 *
 * @author Giovanni Rimassa - FRAMeTech
 */
public interface PersistenceVocabulary extends ExceptionVocabulary {
    /**
     * A symbolic constant, containing the name of this ontology.
     */
    String NAME = "JADE-Persistence";

    // Concepts
    String AGENTGROUP = "agent-group";

    String LOCATION = "location";
    String LOCATION_NAME = "name";
    String LOCATION_ADDRESS = "address";
    String LOCATION_PROTOCOL = "protocol";

    String CONTAINERID = "container-ID";

    // Actions supported by the ams

    String SAVEAGENT = "save-agent";
    String SAVEAGENT_AGENT = "agent";
    String SAVEAGENT_REPOSITORY = "repository";

    String LOADAGENT = "load-agent";
    String LOADAGENT_AGENT = "agent";
    String LOADAGENT_REPOSITORY = "repository";
    String LOADAGENT_WHERE = "where";

    String RELOADAGENT = "reload-agent";
    String RELOADAGENT_AGENT = "agent";
    String RELOADAGENT_REPOSITORY = "repository";

    String DELETEAGENT = "delete-agent";
    String DELETEAGENT_AGENT = "agent";
    String DELETEAGENT_REPOSITORY = "repository";
    String DELETEAGENT_WHERE = "where";

    String FREEZEAGENT = "freeze-agent";
    String FREEZEAGENT_AGENT = "agent";
    String FREEZEAGENT_REPOSITORY = "repository";
    String FREEZEAGENT_BUFFERCONTAINER = "buffer-container";

    String THAWAGENT = "thaw-agent";
    String THAWAGENT_AGENT = "agent";
    String THAWAGENT_REPOSITORY = "repository";
    String THAWAGENT_NEWCONTAINER = "new-container";

    String SAVECONTAINER = "save-container";
    String SAVECONTAINER_CONTAINER = "container";
    String SAVECONTAINER_REPOSITORY = "repository";

    String LOADCONTAINER = "load-container";
    String LOADCONTAINER_CONTAINER = "container";
    String LOADCONTAINER_REPOSITORY = "repository";

    String DELETECONTAINER = "delete-container";
    String DELETECONTAINER_CONTAINER = "container";
    String DELETECONTAINER_REPOSITORY = "repository";
    String DELETECONTAINER_WHERE = "where";

    String SAVEAGENTGROUP = "save-agent-group";
    String SAVEAGENTGROUP_GROUP = "group";
    String SAVEAGENTGROUP_REPOSITORY = "repository";

    String DELETEAGENTGROUP = "delete-agent-group";
    String DELETEAGENTGROUP_GROUP = "group";
    String DELETEAGENTGROUP_REPOSITORY = "repository";

    String LOADAGENTGROUP = "load-agent-group";
    String LOADAGENTGROUP_GROUP = "group";
    String LOADAGENTGROUP_REPOSITORY = "repository";
    // FIXME: More slots are needed (deployment vector for the group)

    // Exception Predicates
    String NOTREGISTERED = jade.domain.FIPAAgentManagement.FIPAManagementVocabulary.NOTREGISTERED;

}
