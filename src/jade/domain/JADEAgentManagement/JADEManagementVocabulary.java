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

import jade.domain.FIPAAgentManagement.ExceptionVocabulary;

/**
 * This interface contains all the string constants for frame and slot
 * names of exceptions defined in the
 * <code>jade-agent-management</code> ontology.
 */
public interface JADEManagementVocabulary extends ExceptionVocabulary {
    /**
     * A symbolic constant, containing the name of this ontology.
     */
    String NAME = "JADE-Agent-Management";

    // Concepts
    String LOCATION = "location";
    String LOCATION_NAME = "name";
    String LOCATION_ADDRESS = "address";
    String LOCATION_PROTOCOL = "protocol";

    String CONTAINERID = "container-ID";
    String CONTAINERID_MAIN = "main";
    String CONTAINERID_PORT = "port";
    String CONTAINERID_PROTOCOL = "protocol";


    String PLATFORMID = "platform-ID";

    // Actions supported by the ams
    String QUERYAGENTSONLOCATION = "query-agents-on-location";
    String QUERYAGENTSONLOCATION_LOCATION = "location";

    String SHUTDOWNPLATFORM = "shutdown-platform";
    String KILLCONTAINER = "kill-container";
    String KILLCONTAINER_CONTAINER = "container";
    String KILLCONTAINER_PASSWORD = "password";

    String CREATEAGENT = "create-agent";
    String CREATEAGENT_AGENT_NAME = "agent-name";
    String CREATEAGENT_CLASS_NAME = "class-name";
    String CREATEAGENT_ARGUMENTS = "arguments";
    String CREATEAGENT_CONTAINER = "container";
    //#MIDP_EXCLUDE_BEGIN
    String CREATEAGENT_OWNER = "owner";
    String CREATEAGENT_INITIAL_CREDENTIALS = "initial-credentials";
    //#MIDP_EXCLUDE_END

    String KILLAGENT = "kill-agent";
    String KILLAGENT_AGENT = "agent";
    String KILLAGENT_PASSWORD = "password";

    String INSTALLMTP = "install-mtp";
    String INSTALLMTP_ADDRESS = "address";
    String INSTALLMTP_CONTAINER = "container";
    String INSTALLMTP_CLASS_NAME = "class-name";

    String UNINSTALLMTP = "uninstall-mtp";
    String UNINSTALLMTP_ADDRESS = "address";
    String UNINSTALLMTP_CONTAINER = "container";

    String SNIFFON = "sniff-on";
    String SNIFFON_SNIFFER = "sniffer";
    String SNIFFON_SNIFFED_AGENTS = "sniffed-agents";
    String SNIFFON_PASSWORD = "password";

    String SNIFFOFF = "sniff-off";
    String SNIFFOFF_SNIFFER = "sniffer";
    String SNIFFOFF_SNIFFED_AGENTS = "sniffed-agents";
    String SNIFFOFF_PASSWORD = "password";

    String DEBUGON = "debug-on";
    String DEBUGON_DEBUGGER = "debugger";
    String DEBUGON_DEBUGGED_AGENTS = "debugged-agents";
    String DEBUGON_PASSWORD = "password";

    String DEBUGOFF = "debug-off";
    String DEBUGOFF_DEBUGGER = "debugger";
    String DEBUGOFF_DEBUGGED_AGENTS = "debugged-agents";
    String DEBUGOFF_PASSWORD = "password";

    String WHEREISAGENT = "where-is-agent";
    String WHEREISAGENT_AGENTIDENTIFIER = "agent-identifier";

    String QUERY_PLATFORM_LOCATIONS = "query-platform-locations";

    // actions supported by the DF
    String SHOWGUI = "showgui";

    // Exception Predicates
    String NOTREGISTERED = jade.domain.FIPAAgentManagement.FIPAManagementVocabulary.NOTREGISTERED;
    String ALREADYREGISTERED = jade.domain.FIPAAgentManagement.FIPAManagementVocabulary.ALREADYREGISTERED;

    // additional constants.
    String CONTAINER_WILDCARD = "%C";
    String AGENT_TAG_WILDCARD = "%A";
}
