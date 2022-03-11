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


/**
 * This interface contains all the string constants for frame and slot
 * names defined in the
 * <code>fipa-agent-management</code> ontology.
 */
public interface FIPAManagementVocabulary extends ExceptionVocabulary {

    /**
     * A symbolic constant, containing the name of this ontology.
     */
    String NAME = "FIPA-Agent-Management";

    // Concepts
    String DFAGENTDESCRIPTION = "df-agent-description";
    String DFAGENTDESCRIPTION_NAME = "name";
    String DFAGENTDESCRIPTION_SERVICES = "services";
    String DFAGENTDESCRIPTION_PROTOCOLS = "protocols";
    String DFAGENTDESCRIPTION_ONTOLOGIES = "ontologies";
    String DFAGENTDESCRIPTION_LANGUAGES = "languages";
    String DFAGENTDESCRIPTION_LEASE_TIME = "lease-time";

    String SERVICEDESCRIPTION = "service-description";
    String SERVICEDESCRIPTION_NAME = "name";
    String SERVICEDESCRIPTION_TYPE = "type";
    String SERVICEDESCRIPTION_OWNERSHIP = "ownership";
    String SERVICEDESCRIPTION_PROTOCOLS = "protocols";
    String SERVICEDESCRIPTION_ONTOLOGIES = "ontologies";
    String SERVICEDESCRIPTION_LANGUAGES = "languages";
    String SERVICEDESCRIPTION_PROPERTIES = "properties";

    String SEARCHCONSTRAINTS = "search-constraints";
    String SEARCHCONSTRAINTS_MAX_DEPTH = "max-depth";
    String SEARCHCONSTRAINTS_MAX_RESULTS = "max-results";
    String SEARCHCONSTRAINTS_SEARCH_ID = "search-id";

    String AMSAGENTDESCRIPTION = "ams-agent-description";
    String AMSAGENTDESCRIPTION_NAME = "name";
    String AMSAGENTDESCRIPTION_OWNERSHIP = "ownership";
    String AMSAGENTDESCRIPTION_STATE = "state";

    String PROPERTY = "property";
    String PROPERTY_NAME = "name";
    String PROPERTY_VALUE = "value";

    String MULTI_VALUE_PROPERTY = "multi-value-property";

    String ENVELOPE = "envelope";
    String ENVELOPE_TO = "to";
    String ENVELOPE_FROM = "from";
    String ENVELOPE_COMMENTS = "comments";
    String ENVELOPE_ACLREPRESENTATION = "acl-representation";
    String ENVELOPE_PAYLOADLENGTH = "payload-length";
    String ENVELOPE_PAYLOADENCODING = "payload-encoding";
    String ENVELOPE_DATE = "date";
    String ENVELOPE_INTENDEDRECEIVER = "intended-receiver";
    String ENVELOPE_TRANSPORTBEHAVIOUR = "transport-behaviour";
    String ENVELOPE_RECEIVED = "received";
    String ENVELOPE_PROPERTIES = "properties";

    String RECEIVEDOBJECT = "received-object";
    String RECEIVEDOBJECT_BY = "by";
    String RECEIVEDOBJECT_FROM = "from";
    String RECEIVEDOBJECT_DATE = "date";
    String RECEIVEDOBJECT_ID = "id";
    String RECEIVEDOBJECT_VIA = "via";

    String APDESCRIPTION = "ap-description";
    String APDESCRIPTION_NAME = "name";
    String APDESCRIPTION_SERVICES = "ap-services";

    String APSERVICE = "ap-service";
    String APSERVICE_NAME = "name";
    String APSERVICE_TYPE = "type";
    String APSERVICE_ADDRESSES = "addresses";

    // Actions
    String REGISTER = "register";
    String REGISTER_DESCRIPTION = "description";

    String DEREGISTER = "deregister";
    String DEREGISTER_DESCRIPTION = "description";

    String MODIFY = "modify";
    String MODIFY_DESCRIPTION = "description";

    String SEARCH = "search";
    String SEARCH_DESCRIPTION = "description";
    String SEARCH_CONSTRAINTS = "constraints";

    String GETDESCRIPTION = "get-description";

    // Predicates
    String ALREADYREGISTERED = "already-registered";
    String NOTREGISTERED = "not-registered";

}
