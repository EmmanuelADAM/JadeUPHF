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


package jade.domain.mobility;

import jade.domain.JADEAgentManagement.JADEManagementVocabulary;

/**
 * This interface contains all the string constants for frame and slot
 * names of exceptions defined in the
 *  jade-mobility-ontology   ontology.
 */
public interface MobilityVocabulary extends JADEManagementVocabulary {

    String MOBILE_AGENT_DESCRIPTION = "mobile-agent-description";
    String MOBILE_AGENT_DESCRIPTION_NAME = "name";
    String MOBILE_AGENT_DESCRIPTION_DESTINATION = "destination";
    String MOBILE_AGENT_DESCRIPTION_AGENT_PROFILE = "agent-profile";
    String MOBILE_AGENT_DESCRIPTION_AGENT_VERSION = "agent-version";
    String MOBILE_AGENT_DESCRIPTION_SIGNATURE = "signature";

    String MOBILE_AGENT_PROFILE = "mobile-agent-profile";
    String MOBILE_AGENT_PROFILE_SYSTEM = "system";
    String MOBILE_AGENT_PROFILE_LANGUAGE = "language";
    String MOBILE_AGENT_PROFILE_OS = "os";

    String MOBILE_AGENT_SYSTEM = "mobile-agent-system";
    String MOBILE_AGENT_SYSTEM_NAME = "name";
    String MOBILE_AGENT_SYSTEM_MAJOR_VERSION = "major-version";
    String MOBILE_AGENT_SYSTEM_MINOR_VERSION = "minor-version";
    String MOBILE_AGENT_SYSTEM_DEPENDENCIES = "dependencies";

    String MOBILE_AGENT_LANGUAGE = "mobile-agent-language";
    String MOBILE_AGENT_LANGUAGE_NAME = "name";
    String MOBILE_AGENT_LANGUAGE_MAJOR_VERSION = "major-version";
    String MOBILE_AGENT_LANGUAGE_MINOR_VERSION = "minor-version";
    String MOBILE_AGENT_LANGUAGE_DEPENDENCIES = "dependencies";

    String MOBILE_AGENT_OS = "mobile-agent-os";
    String MOBILE_AGENT_OS_NAME = "name";
    String MOBILE_AGENT_OS_MAJOR_VERSION = "major-version";
    String MOBILE_AGENT_OS_MINOR_VERSION = "minor-version";
    String MOBILE_AGENT_OS_DEPENDENCIES = "dependencies";

    String MOVE = "move-agent";
    String MOVE_MOBILE_AGENT_DESCRIPTION = "mobile-agent-description";

    String CLONE = "clone-agent";
    String CLONE_MOBILE_AGENT_DESCRIPTION = "mobile-agent-description";
    String CLONE_NEW_NAME = "new-name";


}