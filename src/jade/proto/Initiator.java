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

package jade.proto;

//import java.util.Iterator;
//import java.util.Map;
//import java.util.HashMap;
//import java.util.List;
//import java.util.ArrayList;
//import java.io.Serializable;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;

import java.io.Serial;
import java.util.*;

/**
 * @author Giovanni Caire - TILab
 **/
abstract class Initiator extends FSMBehaviour {
    // FSM states names
    protected static final String PREPARE_INITIATIONS = "Prepare-initiations";
    protected static final String SEND_INITIATIONS = "Send-initiations";
    protected static final String RECEIVE_REPLY = "Receive-reply";
    protected static final String CHECK_IN_SEQ = "Check-in-seq";
    protected static final String HANDLE_NOT_UNDERSTOOD = "Handle-not-understood";
    protected static final String HANDLE_FAILURE = "Handle-failure";
    protected static final String HANDLE_OUT_OF_SEQ = "Handle-out-of-seq";
    protected static final String CHECK_SESSIONS = "Check-sessions";
    protected static final String DUMMY_FINAL = "Dummy-final";
    private static int cnt = 0;
    //#APIDOC_EXCLUDE_BEGIN
    protected final String INITIATION_K = "__initiation" + hashCode();
    protected final String ALL_INITIATIONS_K = "__all-initiations" + hashCode();
    protected final String REPLY_K = "__reply" + hashCode();
    // This maps the AID of each responder to a Session object
    // holding the status of the protocol as far as that responder
    // is concerned. Sessions are protocol-specific
    protected Map<String, ProtocolSession> mapSessions = new HashMap<>();
    // The MsgReceiver behaviour used to receive replies
    protected MsgReceiver replyReceiver;
    // The MessageTemplate used by the replyReceiver
    protected MessageTemplate replyTemplate = null;
    private ACLMessage initiation;

    /**
     * Constructs an <code>Initiator</code> behaviour
     *
     * @param a               The agent performing the protocol
     * @param initiation      The message that must be used to initiate the protocol.
     * @param mapMessagesList The <code>HashMap</code> of list of messages that will be used by this
     *                        <code>Initiator</code>
     * deprecated

    protected Initiator(Agent a, ACLMessage initiation, HashMap<String, List<ACLMessage>> mapMessagesList) {
    super(a);

    setMapMessagesList(mapMessagesList);
    this.initiation = initiation;

    // Register the FSM transitions
    registerDefaultTransition(PREPARE_INITIATIONS, SEND_INITIATIONS);
    registerTransition(SEND_INITIATIONS, DUMMY_FINAL, 0); // Exit the protocol if no initiation message is sent
    registerDefaultTransition(SEND_INITIATIONS, RECEIVE_REPLY);
    registerTransition(RECEIVE_REPLY, CHECK_SESSIONS, MsgReceiver.TIMEOUT_EXPIRED);
    registerTransition(RECEIVE_REPLY, CHECK_SESSIONS, MsgReceiver.INTERRUPTED);
    registerDefaultTransition(RECEIVE_REPLY, CHECK_IN_SEQ);
    registerTransition(CHECK_IN_SEQ, HANDLE_NOT_UNDERSTOOD, ACLMessage.NOT_UNDERSTOOD);
    registerTransition(CHECK_IN_SEQ, HANDLE_FAILURE, ACLMessage.FAILURE);
    registerDefaultTransition(CHECK_IN_SEQ, HANDLE_OUT_OF_SEQ);
    registerDefaultTransition(HANDLE_NOT_UNDERSTOOD, CHECK_SESSIONS);
    registerDefaultTransition(HANDLE_FAILURE, CHECK_SESSIONS);
    registerDefaultTransition(HANDLE_OUT_OF_SEQ, RECEIVE_REPLY);
    registerDefaultTransition(CHECK_SESSIONS, RECEIVE_REPLY, getToBeReset());

    // Create and register the states that make up the FSM
    Behaviour b;
    // PREPARE_INITIATIONS
    b = new OneShotBehaviour(myAgent) {
    @Serial private static final long serialVersionUID = 3487495895818000L;

    public void action() {
    var mapMessages = getMapMessagesList();
    var allInitiations = mapMessages.get(ALL_INITIATIONS_K);
    if (allInitiations == null || allInitiations.size() == 0) {
    allInitiations = prepareInitiations(getMapMessages().get(INITIATION_K));
    mapMessages.put(ALL_INITIATIONS_K, allInitiations);
    }
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerFirstState(b, PREPARE_INITIATIONS);

    // SEND_INITIATIONS
    b = new OneShotBehaviour(myAgent) {
    @Serial private static final long serialVersionUID = 3487495895818001L;

    public void action() {
    var allInitiations = getMapMessagesList().get(ALL_INITIATIONS_K);
    if (allInitiations != null) {
    sendInitiations(allInitiations);
    }
    }

    public int onEnd() {
    return mapSessions.size();
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, SEND_INITIATIONS);

    // RECEIVE_REPLY
    //TODO: THE BUG IS HERE !!!!! 2022-02-28
    replyReceiver = new MsgReceiver(myAgent, null, MsgReceiver.INFINITE, getMapMessagesList(), REPLY_K);
    registerState(replyReceiver, RECEIVE_REPLY);

    // CHECK_IN_SEQ
    b = new OneShotBehaviour(myAgent) {
    int ret;
     @Serial private static final long serialVersionUID = 3487495895818002L;

     public void action() {
     ACLMessage reply = getMapMessages().get(REPLY_K);
     if (checkInSequence(reply)) {
     ret = reply.getPerformative();
     } else {
     ret = -1;
     }
     }

     public int onEnd() {
     return ret;
     }
     };
     b.setMapMessagesList(getMapMessagesList());
     registerState(b, CHECK_IN_SEQ);

     // HANDLE_NOT_UNDERSTOOD
     b = new OneShotBehaviour(myAgent) {
     @Serial private static final long serialVersionUID = 3487495895818005L;

     public void action() {
     handleNotUnderstood(getMapMessages().get(REPLY_K));
     }
     };
     b.setMapMessagesList(getMapMessagesList());
     registerState(b, HANDLE_NOT_UNDERSTOOD);

     // HANDLE_FAILURE
     b = new OneShotBehaviour(myAgent) {
     @Serial private static final long serialVersionUID = 3487495895818007L;

     public void action() {
     handleFailure(getMapMessages().get(REPLY_K));
     }
     };
     b.setMapMessagesList(getMapMessagesList());
     registerState(b, HANDLE_FAILURE);

     // HANDLE_OUT_OF_SEQ
     b = new OneShotBehaviour(myAgent) {
     @Serial private static final long serialVersionUID = 3487495895818008L;

     public void action() {
     handleOutOfSequence(getMapMessages().get(REPLY_K));
     }
     };
     b.setMapMessagesList(getMapMessagesList());
     registerState(b, HANDLE_OUT_OF_SEQ);

     // CHECK_SESSIONS
     b = new OneShotBehaviour(myAgent) {
     int ret;
     @Serial private static final long serialVersionUID = 3487495895818009L;

     public void action() {
     ACLMessage reply = getMapMessages().get(REPLY_K);
     ret = checkSessions(reply);
     }

     public int onEnd() {
     return ret;
     }
     };
     b.setMapMessagesList(getMapMessagesList());
     registerState(b, CHECK_SESSIONS);

     // DUMMY_FINAL
     b = new OneShotBehaviour(myAgent) {
     @Serial private static final long serialVersionUID = 3487495895818010L;

     public void action() {
     }
     };
     registerLastState(b, DUMMY_FINAL);
     }
     */
    /**
     * Constructs an <code>Initiator</code> behaviour
     * see #AchieveREInitiator(Agent, ACLMessage, HashMap)
     **/
    protected Initiator(Agent a, ACLMessage initiation) {
        this(a, initiation, new HashMap<>(), new HashMap<>());
    }

    /**
     * Constructs an <code>Initiator</code> behaviour
     *
     * @param a               The agent performing the protocol
     * @param initiation      The message that must be used to initiate the protocol.
     * @param mapMessagesList The <code>HashMap</code> of list of messages that will be used by this
     * @param mapMessages     The <code>HashMap</code> of messages that will be used by this
     *                        <code>Initiator</code>
     */
    protected Initiator(Agent a, ACLMessage initiation, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a);

        setMapMessagesList(mapMessagesList);
        setMapMessages(mapMessages);
        this.initiation = initiation;

        // Register the FSM transitions
        registerDefaultTransition(PREPARE_INITIATIONS, SEND_INITIATIONS);
        registerTransition(SEND_INITIATIONS, DUMMY_FINAL, 0); // Exit the protocol if no initiation message is sent
        registerDefaultTransition(SEND_INITIATIONS, RECEIVE_REPLY);
        registerTransition(RECEIVE_REPLY, CHECK_SESSIONS, MsgReceiver.TIMEOUT_EXPIRED);
        registerTransition(RECEIVE_REPLY, CHECK_SESSIONS, MsgReceiver.INTERRUPTED);
        registerDefaultTransition(RECEIVE_REPLY, CHECK_IN_SEQ);
        registerTransition(CHECK_IN_SEQ, HANDLE_NOT_UNDERSTOOD, ACLMessage.NOT_UNDERSTOOD);
        registerTransition(CHECK_IN_SEQ, HANDLE_FAILURE, ACLMessage.FAILURE);
        registerDefaultTransition(CHECK_IN_SEQ, HANDLE_OUT_OF_SEQ);
        registerDefaultTransition(HANDLE_NOT_UNDERSTOOD, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_FAILURE, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_OUT_OF_SEQ, RECEIVE_REPLY);
        registerDefaultTransition(CHECK_SESSIONS, RECEIVE_REPLY, getToBeReset());

        // Create and register the states that make up the FSM
        Behaviour b;
        // PREPARE_INITIATIONS
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818000L;

            public void action() {
                var mapMessages = getMapMessagesList();
                var allInitiations = mapMessages.get(ALL_INITIATIONS_K);
                if (allInitiations == null || allInitiations.size() == 0) {
                    allInitiations = prepareInitiations(getMapMessages().get(INITIATION_K));
                    mapMessages.put(ALL_INITIATIONS_K, allInitiations);
                }
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerFirstState(b, PREPARE_INITIATIONS);

        // SEND_INITIATIONS
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818001L;

            public void action() {
                var allInitiations = getMapMessagesList().get(ALL_INITIATIONS_K);
                if (allInitiations != null) {
                    sendInitiations(allInitiations);
                }
            }

            public int onEnd() {
                return mapSessions.size();
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, SEND_INITIATIONS);

        // RECEIVE_REPLY
        replyReceiver = new MsgReceiver(myAgent, null, MsgReceiver.INFINITE, getMapMessagesList(), getMapMessages(), REPLY_K);
        registerState(replyReceiver, RECEIVE_REPLY);

        // CHECK_IN_SEQ
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818002L;
            int ret;

            public void action() {
                ACLMessage reply = getMapMessages().get(REPLY_K);
                if (checkInSequence(reply)) {
                    ret = reply.getPerformative();
                } else {
                    ret = -1;
                }
            }

            public int onEnd() {
                return ret;
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, CHECK_IN_SEQ);

        // HANDLE_NOT_UNDERSTOOD
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818005L;

            public void action() {
                handleNotUnderstood(getMapMessages().get(REPLY_K));
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, HANDLE_NOT_UNDERSTOOD);

        // HANDLE_FAILURE
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818007L;

            public void action() {
                handleFailure(getMapMessages().get(REPLY_K));
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, HANDLE_FAILURE);

        // HANDLE_OUT_OF_SEQ
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818008L;

            public void action() {
                handleOutOfSequence(getMapMessages().get(REPLY_K));
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, HANDLE_OUT_OF_SEQ);

        // CHECK_SESSIONS
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818009L;
            int ret;

            public void action() {
                ACLMessage reply = getMapMessages().get(REPLY_K);
                ret = checkSessions(reply);
            }

            public int onEnd() {
                return ret;
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, CHECK_SESSIONS);

        // DUMMY_FINAL
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818010L;

            public void action() {
            }
        };
        registerLastState(b, DUMMY_FINAL);
    }

    private synchronized static int getCnt() {
        int k = cnt;
        cnt++;
        return k;
    }

    /**
     * Specialize (if necessary) the initiation message for each receiver
     */
    protected abstract List<ACLMessage> prepareInitiations(ACLMessage initiation);

    /**
     * Check whether a reply is in-sequence and update the appropriate Session
     */
    protected abstract boolean checkInSequence(ACLMessage reply);

    /**
     * Check the global status of the sessions after the reception of the last reply
     * or the expiration of the timeout
     */
    protected abstract int checkSessions(ACLMessage reply);

    /**
     * Return the states that must be reset before they are visited again.
     * Note that resetting a state before visiting it again is required
     * only if
     * - The onStart() method is redefined
     * - The state has an "internal memory"
     */
    protected abstract String[] getToBeReset();
    //#APIDOC_EXCLUDE_END

    /**
     * Return a ProtocolSession object to manage replies to a given
     * initiation message
     */
    protected abstract ProtocolSession getSession(ACLMessage msg, int sessionIndex);

    /**
     * Create and initialize the Sessions and sends the initiation messages
     */
    protected void sendInitiations(List<ACLMessage> initiations) {
        long currentTime = System.currentTimeMillis();
        long minTimeout = -1;
        long deadline = -1;

        String conversationID = createConvId(initiations);
        replyTemplate = MessageTemplate.MatchConversationId(conversationID);
        int cnt = 0; // counter of sessions
        List<ACLMessage> sentMessages = new ArrayList<>();
        for (ACLMessage initiation : initiations) {
            if (initiation != null) {
                // Update the list of sessions on the basis of the receivers
                // FIXME: Maybe this should take the envelope into account first

                for (Iterator<AID> receivers = initiation.getAllReceiver(); receivers.hasNext(); ) {
                    ACLMessage toSend = (ACLMessage) initiation.clone();
                    toSend.setConversationId(conversationID);
                    toSend.clearAllReceiver();
                    AID r = receivers.next();
                    toSend.addReceiver(r);
                    ProtocolSession ps = getSession(toSend, cnt);
                    if (ps != null) {
                        String sessionKey = ps.getId();
                        if (sessionKey == null) {
                            sessionKey = "R" + System.currentTimeMillis() + "_" + cnt;
                        }
                        toSend.setReplyWith(sessionKey);
                        mapSessions.put(sessionKey, ps);
                        adjustReplyTemplate(toSend);
                        cnt++;
                    }
                    myAgent.send(toSend);
                    sentMessages.add(toSend);
                }

                // Update the timeout (if any) used to wait for replies according
                // to the reply-by field: get the miminum.
                Date d = initiation.getReplyByDate();
                if (d != null) {
                    long timeout = d.getTime() - currentTime;
                    if (timeout > 0 && (timeout < minTimeout || minTimeout <= 0)) {
                        minTimeout = timeout;
                        deadline = d.getTime();
                    }
                }
            }
        }
        // Replace the initiations list with that of actually sent messages
        var map = getMapMessagesList();
        map.put(ALL_INITIATIONS_K, sentMessages);

        // Finally set the MessageTemplate and timeout used in the RECEIVE_REPLY
        // state to accept replies
        replyReceiver.setTemplate(replyTemplate);
        replyReceiver.setDeadline(deadline);
    }

    /**
     * This method is called every time a <code>not-understood</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     *
     * @param notUnderstood the received not-understood message
     **/
    protected void handleNotUnderstood(ACLMessage notUnderstood) {
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * This method is called every time a <code>failure</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     *
     * @param failure the received failure message
     **/
    protected void handleFailure(ACLMessage failure) {
    }
    //#APIDOC_EXCLUDE_END

    /**
     * This method is called every time a
     * message is received, which is out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     *
     * @param msg the received message
     **/
    protected void handleOutOfSequence(ACLMessage msg) {
    }

    /**
     * Attach a behaviour to the <code>Prepare-initiations</code>
     * protocol state.
     *
     * @param b The behaviour object to be executed in the
     *          <code>Prepare-initiations</code> state.
     */
    protected void registerPrepareInitiations(Behaviour b) {
        registerState(b, PREPARE_INITIATIONS);
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_NOT_UNDERSTOOD state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve
     * the <code>not-understood</code> ACLMessage object received
     * from the HashMap at the <code>REPLY_KEY</code>
     * key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleNotUnderstood(Behaviour b) {
        registerState(b, HANDLE_NOT_UNDERSTOOD);
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_FAILURE state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve
     * the <code>failure</code> ACLMessage object received
     * from the HashMap at the <code>REPLY_KEY</code>
     * key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleFailure(Behaviour b) {
        registerState(b, HANDLE_FAILURE);
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_OUT_OF_SEQ state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve
     * the <code>out of sequence</code> ACLMessage object received
     * from the HashMap at the <code>REPLY_KEY</code>
     * key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleOutOfSequence(Behaviour b) {
        registerState(b, HANDLE_OUT_OF_SEQ);
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * reset this behaviour by putting a null ACLMessage as message
     * to be sent
     **/
    public void reset() {
        reset(null);
    }

    /**
     * reset this behaviour
     *
     * @param msg is the ACLMessage to be sent
     **/
    public void reset(ACLMessage msg) {
        initiation = msg;
        reinit();
        super.reset();
    }

    /**
     * Re-initialize the internal state without performing a complete reset.
     */
    protected void reinit() {
        replyReceiver.reset(null, MsgReceiver.INFINITE, getMapMessagesList(), getMapMessages(), REPLY_K);
        mapSessions.clear();
        var mapall = getMapMessagesList();
        var map = getMapMessages();
        map.remove(INITIATION_K);
        mapall.remove(ALL_INITIATIONS_K);
        map.remove(REPLY_K);
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * Override the onStart() method to initialize the lists that
     * will keep all the replies in the map store.
     */
    public void onStart() {
        initializeHashMap(initiation);
    }
    //#APIDOC_EXCLUDE_END

    /**
     * Override the setHashMap() method to propagate this
     * setting to all children.
     */
    public void setMapMessagesList(HashMap<String, List<ACLMessage>> ds) {
        super.setMapMessagesList(ds);
        getChildren().forEach(child -> child.setMapMessagesList(ds));
    }

    /**
     * Initialize the data store.
     **/
    protected void initializeHashMap(ACLMessage initiation) {
        var map = getMapMessages();
        map.put(INITIATION_K, initiation);
    }

    /**
     * Create a new conversation identifier to begin a new
     * interaction.
     *
     * @param msgs A list of ACL messages. If the first one has a
     *             non-empty <code>:conversation-id</code> slot, its value is
     *             used, else a new conversation identifier is generated.
     */
    protected String createConvId(List<ACLMessage> msgs) {
        // If the conversation-id of the first message is set -->
        // use it. Otherwise create a default one
        String convId = null;
        if (msgs.size() > 0) {
            var msg = msgs.get(0);
            if ((msg == null) || (msg.getConversationId() == null)) {
                convId = "C" + hashCode() + "_" + myAgent.getLocalName() + "_" + System.currentTimeMillis() + "_" + getCnt();
            } else {
                convId = msg.getConversationId();
            }
        }
        return convId;
    }

    //#APIDOC_EXCLUDE_BEGIN
    protected void adjustReplyTemplate(ACLMessage msg) {
        // If myAgent is among the receivers (strange case, but can happen)
        // then modify the replyTemplate to avoid intercepting the initiation
        // message msg as if it was a reply
        AID r = msg.getAllReceiver().next();
        if (myAgent.getAID().equals(r)) {
            replyTemplate = MessageTemplate.and(
                    replyTemplate,
                    MessageTemplate.not(MessageTemplate.MatchCustom(msg, true)));
        }
    }


    /**
     * Inner interface Session
     */
    protected interface ProtocolSession {
        String getId();

        boolean update(int perf);

        int getState();

        boolean isCompleted();
    }
    //#APIDOC_EXCLUDE_END
}
