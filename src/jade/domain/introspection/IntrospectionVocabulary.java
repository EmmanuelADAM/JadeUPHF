/*
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by
the IST-1999-10211 LEAP Project

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

package jade.domain.introspection;

/**
 * This interface contains all the string constants for frame and slot
 * names of exceptions defined in the  jade-introspection  
 * ontology.
 */
public interface IntrospectionVocabulary {

// Concepts

    String APDESCRIPTION = "ap-description";
    String APDESCRIPTION_NAME = "name";
    String APDESCRIPTION_SERVICES = "ap-services";

    String APSERVICE = "ap-service";
    String APSERVICE_NAME = "name";
    String APSERVICE_TYPE = "type";
    String APSERVICE_ADDRESSES = "addresses";

    String EVENTRECORD = "event-record";
    String EVENTRECORD_WHAT = "what";
    String EVENTRECORD_WHEN = "when";
    String EVENTRECORD_WHERE = "where";

    String META_RESETEVENTS = "meta_reset-events";

    String ADDEDCONTAINER = "added-container";
    String ADDEDCONTAINER_CONTAINER = "container";
    String ADDEDCONTAINER_OWNERSHIP = "ownership";

    String REMOVEDCONTAINER = "removed-container";
    String REMOVEDCONTAINER_CONTAINER = "container";

    String KILLCONTAINERREQUESTED = "kill-container-requested";
    String KILLCONTAINERREQUESTED_CONTAINER = "container";

    String SHUTDOWNPLATFORMREQUESTED = "shutdown-platform-requested";

    String ADDEDMTP = "added-mtp";
    String ADDEDMTP_ADDRESS = "address";
    String ADDEDMTP_WHERE = "where";

    String REMOVEDMTP = "removed-mtp";
    String REMOVEDMTP_ADDRESS = "address";
    String REMOVEDMTP_WHERE = "where";

    String BORNAGENT = "born-agent";
    String BORNAGENT_AGENT = "agent";
    String BORNAGENT_WHERE = "where";
    String BORNAGENT_STATE = "state";
    String BORNAGENT_OWNERSHIP = "ownership";
    String BORNAGENT_CLASS_NAME = "class-name";

    String DEADAGENT = "dead-agent";
    String DEADAGENT_AGENT = "agent";
    String DEADAGENT_WHERE = "where";
    String DEADAGENT_CONTAINER_REMOVED = "container-removed";

    String SUSPENDEDAGENT = "suspended-agent";
    String SUSPENDEDAGENT_AGENT = "agent";
    String SUSPENDEDAGENT_WHERE = "where";

    String RESUMEDAGENT = "resumed-agent";
    String RESUMEDAGENT_AGENT = "agent";
    String RESUMEDAGENT_WHERE = "where";

    String FROZENAGENT = "frozen-agent";
    String FROZENAGENT_AGENT = "agent";
    String FROZENAGENT_WHERE = "where";
    String FROZENAGENT_BUFFERCONTAINER = "buffer-container";

    String THAWEDAGENT = "thawed-agent";
    String THAWEDAGENT_AGENT = "agent";
    String THAWEDAGENT_WHERE = "where";
    String THAWEDAGENT_BUFFERCONTAINER = "buffer-container";

    String CHANGEDAGENTOWNERSHIP = "changed-agent-ownership";
    String CHANGEDAGENTOWNERSHIP_AGENT = "agent";
    String CHANGEDAGENTOWNERSHIP_FROM = "from";
    String CHANGEDAGENTOWNERSHIP_TO = "to";
    String CHANGEDAGENTOWNERSHIP_WHERE = "where";

    String MOVEDAGENT = "moved-agent";
    String MOVEDAGENT_AGENT = "agent";
    String MOVEDAGENT_TO = "to";
    String MOVEDAGENT_FROM = "from";

    String CHANGEDAGENTSTATE = "changed-agent-state";
    String CHANGEDAGENTSTATE_AGENT = "agent";
    String CHANGEDAGENTSTATE_FROM = "from";
    String CHANGEDAGENTSTATE_TO = "to";


    String ADDEDBEHAVIOUR = "added-behaviour";
    String ADDEDBEHAVIOUR_AGENT = "agent";
    String ADDEDBEHAVIOUR_BEHAVIOUR = "behaviour";

    String REMOVEDBEHAVIOUR = "removed-behaviour";
    String REMOVEDBEHAVIOUR_AGENT = "agent";
    String REMOVEDBEHAVIOUR_BEHAVIOUR = "behaviour";

    String CHANGEDBEHAVIOURSTATE = "changed-behaviour-state";
    String CHANGEDBEHAVIOURSTATE_AGENT = "agent";
    String CHANGEDBEHAVIOURSTATE_BEHAVIOUR = "behaviour";
    String CHANGEDBEHAVIOURSTATE_FROM = "from";
    String CHANGEDBEHAVIOURSTATE_TO = "to";

    String SENTMESSAGE = "sent-message";
    String SENTMESSAGE_SENDER = "sender";
    String SENTMESSAGE_RECEIVER = "receiver";
    String SENTMESSAGE_MESSAGE = "message";

    String RECEIVEDMESSAGE = "received-message";
    String RECEIVEDMESSAGE_SENDER = "sender";
    String RECEIVEDMESSAGE_RECEIVER = "receiver";
    String RECEIVEDMESSAGE_MESSAGE = "message";

    String POSTEDMESSAGE = "posted-message";
    String POSTEDMESSAGE_SENDER = "sender";
    String POSTEDMESSAGE_RECEIVER = "receiver";
    String POSTEDMESSAGE_MESSAGE = "message";

    String ROUTEDMESSAGE = "routed-message";
    String ROUTEDMESSAGE_FROM = "from";
    String ROUTEDMESSAGE_TO = "to";
    String ROUTEDMESSAGE_MESSAGE = "message";

    String CONTAINERID = "container-ID";
    String CONTAINERID_NAME = "name";
    String CONTAINERID_ADDRESS = "address";
    String CONTAINERID_MAIN = "main";
    String CONTAINERID_PORT = "port";
    String CONTAINERID_PROTOCOL = "protocol";


    String AGENTSTATE = "agent-state";
    String AGENTSTATE_NAME = "name";

    String BEHAVIOURID = "behaviour-ID";
    String BEHAVIOURID_NAME = "name";
    String BEHAVIOURID_CLASS_NAME = "class-name";
    String BEHAVIOURID_KIND = "kind";
    String BEHAVIOURID_CHILDREN = "children";
    String BEHAVIOURID_CODE = "code";

    String ACLMESSAGE = "acl-message";
    String ACLMESSAGE_ENVELOPE = "envelope";
    String ACLMESSAGE_PAYLOAD = "payload";
    String ACLMESSAGE_ACLREPRESENTATION = "acl-representation";

    String ENVELOPE = "envelope";
    String ENVELOPE_TO = "to";
    String ENVELOPE_FROM = "from";
    String ENVELOPE_COMMENTS = "comments";
    String ENVELOPE_ACLREPRESENTATION = "acl-representation";
    String ENVELOPE_PAYLOADLENGTH = "payload-length";
    String ENVELOPE_PAYLOADENCODING = "payload-encoding";
    String ENVELOPE_DATE = "date";
    String ENVELOPE_INTENDEDRECEIVER = "intended-receiver";
    String ENVELOPE_RECEIVED = "received";

    String RECEIVEDOBJECT = "received-object";
    String RECEIVEDOBJECT_BY = "by";
    String RECEIVEDOBJECT_FROM = "from";
    String RECEIVEDOBJECT_DATE = "date";
    String RECEIVEDOBJECT_ID = "id";
    String RECEIVEDOBJECT_VIA = "via";

    String CHANNEL = "channel";
    String CHANNEL_NAME = "name";
    String CHANNEL_PROTOCOL = "protocol";
    String CHANNEL_ADDRESS = "address";


    String PLATFORMDESCRIPTION = "platform-description";
    String PLATFORMDESCRIPTION_PLATFORM = "platform";

    // Actions
    String STARTNOTIFY = "start-notify";
    String STARTNOTIFY_OBSERVED = "observed";
    String STARTNOTIFY_EVENTS = "events";

    String STOPNOTIFY = "stop-notify";
    String STOPNOTIFY_OBSERVED = "observed";
    String STOPNOTIFY_EVENTS = "events";

    String GETKEYS = "get-keys";

    String GETVALUE = "get-value";
    String GETVALUE_KEY = "key";

    // Predicates
    String OCCURRED = "occurred";
    String OCCURRED_WHAT = "what";

}
