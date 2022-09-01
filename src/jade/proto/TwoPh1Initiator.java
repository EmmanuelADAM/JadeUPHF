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

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class description
 *
 * @author Elena Quarantotto - TILAB
 * @author Giovanni Caire - TILAB
 */
public class TwoPh1Initiator extends Initiator {
    // Data store keys
    // Private data store keys (can't be static since if we register another instance of this class as state of the FSM
    // using the same data store the new values overrides the old one.
    /* FSM states names */
    private static final String HANDLE_CONFIRM = "Handle-Confirm";
    private static final String HANDLE_DISCONFIRM = "Handle-Disconfirm";
    private static final String HANDLE_INFORM = "Handle-Inform";
    private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";
    private static final int ALL_RESPONSES_RECEIVED = 1;
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object passed in the constructor of the class.
     */
    public final String QUERYIF_KEY = INITIATION_K;
    /**
     * key to retrieve from the HashMap of the behaviour the Vector of
     * QUERY_IF messages that have to be sent.
     */
    public final String ALL_QUERYIFS_KEY = ALL_INITIATIONS_K;
    /**
     * key to retrieve from the HashMap of the behaviour the last
     * ACLMessage object that has been received (null if the timeout
     * expired).
     */
    public final String REPLY_KEY = REPLY_K;
    /**
     * key to retrieve from the HashMap of the behaviour the Vector of
     * all messages that have been received as response.
     */
    public final String ALL_RESPONSES_KEY = "__all-responses" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the Vector of
     * CONFIRM messages that have been received as response.
     */
    public final String ALL_CONFIRMS_KEY = "__all-confirms" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the Vector of
     * DISCONFIRM messages that have been received as response.
     */
    public final String ALL_DISCONFIRMS_KEY = "__all-disconfirms" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the Vector of
     * INFORM messages that have been received as response.
     */
    public final String ALL_INFORMS_KEY = "__all-informs" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the Vector of
     * QUERY_IF messages for which a response has not been received yet.
     */
    public final String ALL_PENDINGS_KEY = "__all-pendings" + hashCode();
    /* Data store output key */
    private final String outputKey;

    private int totSessions;
    private String[] toBeReset = null;

    /**
     * Constructs a  TwoPh1Initiator   behaviour.
     *
     * @param a               The agent performing the protocol.
     * @param queryIf         msg
     * @param outputKey       Data store key where the behaviour prepares a vector of
     *                        messages which will be send by a  TwoPh2Initiator   behaviour.
     *                        If phase 1 ends with all confirm or inform than messages prepared are
     *                         accept-proposal  , otherwise they are  reject-proposal  .
     * @param mapMessagesList  HashMap   of messages list that will be used by this  TwoPh1Initiator  .
     * deprecated

    public TwoPh1Initiator(Agent a, ACLMessage queryIf, String outputKey, HashMap<String, List<ACLMessage>> mapMessagesList) {
    super(a, queryIf, mapMessagesList);
    //this.inputKey = inputKey;
    this.outputKey = outputKey;
    //Register the FSM transitions specific to the Two-Phase1-Commit protocol
    registerTransition(CHECK_IN_SEQ, HANDLE_CONFIRM, ACLMessage.CONFIRM);
    registerTransition(CHECK_IN_SEQ, HANDLE_DISCONFIRM, ACLMessage.DISCONFIRM);
    registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);
    registerDefaultTransition(HANDLE_CONFIRM, CHECK_SESSIONS);
    registerDefaultTransition(HANDLE_DISCONFIRM, CHECK_SESSIONS);
    registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
    registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
    /*
    registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, SOME_DISCONFIRM);
    registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, PH1_TIMEOUT_EXPIRED);
    registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_CONFIRM_OR_INFORM);
    registerDefaultTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL);


    // Create and register the states specific to the Two-Phase1-Commit protocol
    Behaviour b;

    // HANDLE_CONFIRM state activated if arrived a confirm message compliant with conversationId and a receiver of one of queryIf messages sent.
    b = new OneShotBehaviour(myAgent) {
    public void action() {
    ACLMessage confirm = getMapMessages().get(REPLY_KEY);
    handleConfirm(confirm);
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, HANDLE_CONFIRM);

    // HANDLE_DISCONFIRM state activated if arrived a disconfirm message
    //compliant with conversationId and a receiver of one of queryIf messages
    //sent.
    b = new OneShotBehaviour(myAgent) {
    public void action() {
    ACLMessage disconfirm = getMapMessages().get(REPLY_KEY);
    handleDisconfirm(disconfirm);
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, HANDLE_DISCONFIRM);

    // HANDLE_INFORM state activated if arrived an inform message
    //compliant with conversationId and a receiver of one of queryIf messages
    //sent.
    b = new OneShotBehaviour(myAgent) {
    public void action() {
    ACLMessage inform = getMapMessages().get(REPLY_KEY);
    handleInform(inform);
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, HANDLE_INFORM);

    // HANDLE_ALL_RESPONSES state activated when timeout is expired or
    //all the answers have been received.
    b = new OneShotBehaviour(myAgent) {
    public void action() {
    var responses = getMapMessagesList().get(ALL_RESPONSES_KEY);
    var confirms = getMapMessagesList().get(ALL_CONFIRMS_KEY);
    var disconfirms = getMapMessagesList().get(ALL_DISCONFIRMS_KEY);
    var informs = getMapMessagesList().get(ALL_INFORMS_KEY);
    var pendings = getMapMessagesList().get(ALL_PENDINGS_KEY);
    var nextPhMsgs = getMapMessagesList().get(TwoPh1Initiator.this.outputKey);
    handleAllResponses(responses, confirms, disconfirms, informs,
    pendings, nextPhMsgs);
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, HANDLE_ALL_RESPONSES);
    }
     */
    /**
     * Constructs a  TwoPh1Initiator   behaviour.
     *
     * @param a         The agent performing the protocol.
     * @param queryIf   msg
     * @param outputKey Data store key where the behaviour prepares a vector of
     *                  messages which will be send by a  TwoPh2Initiator   behaviour.
     *                  If phase 1 ends with all confirm or inform than messages prepared are
     *                   accept-proposal  , otherwise they are  reject-proposal  .
     */
    public TwoPh1Initiator(Agent a, ACLMessage queryIf, String outputKey) {
        this(a, queryIf, outputKey, new HashMap<>(), new HashMap<>());
    }

    /**
     * Constructs a  TwoPh1Initiator   behaviour.
     *
     * @param a               The agent performing the protocol.
     * @param queryIf         msg
     * @param outputKey       Data store key where the behaviour prepares a vector of
     *                        messages which will be send by a  TwoPh2Initiator   behaviour.
     *                        If phase 1 ends with all confirm or inform than messages prepared are
     *                         accept-proposal  , otherwise they are  reject-proposal  .
     * @param mapMessagesList  HashMap   of messages list that will be used by this  TwoPh1Initiator  .
     * @param mapMessages      HashMap   of messages  that will be used by this  TwoPh1Initiator  .
     */
    public TwoPh1Initiator(Agent a, ACLMessage queryIf, String outputKey, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a, queryIf, mapMessagesList, mapMessages);
        //this.inputKey = inputKey;
        this.outputKey = outputKey;
        /* Register the FSM transitions specific to the Two-Phase1-Commit protocol */
        registerTransition(CHECK_IN_SEQ, HANDLE_CONFIRM, ACLMessage.CONFIRM);
        registerTransition(CHECK_IN_SEQ, HANDLE_DISCONFIRM, ACLMessage.DISCONFIRM);
        registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);
        registerDefaultTransition(HANDLE_CONFIRM, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_DISCONFIRM, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
        /*
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, SOME_DISCONFIRM);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, PH1_TIMEOUT_EXPIRED);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_CONFIRM_OR_INFORM);
        registerDefaultTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL);
        */

        /* Create and register the states specific to the Two-Phase1-Commit protocol */
        Behaviour b;

        /* HANDLE_CONFIRM state activated if arrived a confirm message compliant with
        conversationId and a receiver of one of queryIf messages sent. */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                ACLMessage confirm = getMapMessages().get(REPLY_KEY);
                handleConfirm(confirm);
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_CONFIRM);

        /* HANDLE_DISCONFIRM state activated if arrived a disconfirm message
        compliant with conversationId and a receiver of one of queryIf messages
        sent. */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                ACLMessage disconfirm = getMapMessages().get(REPLY_KEY);
                handleDisconfirm(disconfirm);
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_DISCONFIRM);

        /* HANDLE_INFORM state activated if arrived an inform message
        compliant with conversationId and a receiver of one of queryIf messages
        sent. */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                ACLMessage inform = getMapMessages().get(REPLY_KEY);
                handleInform(inform);
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_INFORM);

        /* HANDLE_ALL_RESPONSES state activated when timeout is expired or
        all the answers have been received. */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                var responses = getMapMessagesList().get(ALL_RESPONSES_KEY);
                var confirms = getMapMessagesList().get(ALL_CONFIRMS_KEY);
                var disconfirms = getMapMessagesList().get(ALL_DISCONFIRMS_KEY);
                var informs = getMapMessagesList().get(ALL_INFORMS_KEY);
                var pendings = getMapMessagesList().get(ALL_PENDINGS_KEY);
                var nextPhMsgs = getMapMessagesList().get(TwoPh1Initiator.this.outputKey);
                handleAllResponses(responses, confirms, disconfirms, informs,
                        pendings, nextPhMsgs);
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_ALL_RESPONSES);
    }

    public int onEnd() {
        //TODO: EA. verifier ce que peut etre outputkey
        var nextPhMsgs = getMapMessagesList().get(outputKey);
        if (nextPhMsgs.size() != 0) {
            return (nextPhMsgs.get(0)).getPerformative();
        } else {
            return -1;
        }
    }

    /* User can override these methods */

    /**
     * This method must return the vector of ACLMessage objects to be sent.
     * It is called in the first state of this protocol. This default
     * implementation just returns the ACLMessage object (a QUERY_IF) passed in
     * the constructor. Programmers might prefer to override this method in order
     * to return a vector of QUERY_IF objects for 1:N conversations.
     *
     * @param queryIf the ACLMessage object passed in the constructor
     * @return a Vector of ACLMessage objects. The value of the  reply-with
     * slot is ignored and regenerated automatically by this
     * class. Instead user can specify  reply-by   slot representing phase0
     * timeout.
     */
    protected List<ACLMessage> prepareQueryIfs(ACLMessage queryIf) {
        List<ACLMessage> v = new ArrayList<>(1);
        v.add(queryIf);
        return v;
    }

    /**
     * This method is called every time a  confirm   message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     *
     * @param confirm the received propose message
     */
    protected void handleConfirm(ACLMessage confirm) {
    }

    /**
     * This method is called every time a  disconfirm   message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     *
     * @param disconfirm the received propose message
     */
    protected void handleDisconfirm(ACLMessage disconfirm) {
    }

    /**
     * This method is called every time a  inform   message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     *
     * @param inform the received propose message
     */
    protected void handleInform(ACLMessage inform) {
    }

    /**
     * This method is called when all the responses have been collected or when
     * the timeout is expired. The used timeout is the minimum value of the slot
     *  reply-By   of all the sent messages. By response message we
     * intend here all the  disconfirm, confirm, inform   received messages,
     * which are not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event by analysing all the messages in
     * just one call.
     *
     * @param confirms    all confirms received
     * @param disconfirms all disconfirms received
     * @param pendings    all queryIfs still pending
     * @param responses   prepared responses for next phase:  accept-proposal
     *                    or  reject-proposal
     */
    protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> confirms, List<ACLMessage> disconfirms,
                                      List<ACLMessage> informs, List<ACLMessage> pendings, List<ACLMessage> nextPhMsgs) {
    }

    /**
     * This method allows to register a user-defined  Behaviour   in the
     * PREPARE_QUERYIFS state. This behaviour would override the homonymous method. This
     * method also set the data store of the registered  Behaviour   to the
     * HashMap of this current behaviour. It is responsibility of the registered
     * behaviour to put the  Vector   of ACLMessage objects to be sent into
     * the HashMap at the  ALL_QUERYIFS_KEY   key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerPrepareQueryIfs(Behaviour b) {
        registerPrepareInitiations(b);
    }

    /**
     * This method allows to register a user defined  Behaviour   in the
     * HANDLE_CONFIRM state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered  Behaviour
     * to the HashMap of this current behaviour. The registered behaviour can retrieve
     * the  confirm   ACLMessage object received from the HashMap at the
     *  REPLY_KEY   key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleConfirm(Behaviour b) {
        registerState(b, HANDLE_CONFIRM);
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * This method allows to register a user defined  Behaviour   in the
     * HANDLE_DISCONFIRM state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered  Behaviour
     * to the HashMap of this current behaviour. The registered behaviour can retrieve
     * the  disconfirm   ACLMessage object received from the HashMap at the
     *  REPLY_KEY   key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleDisconfirm(Behaviour b) {
        registerState(b, HANDLE_DISCONFIRM);
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * This method allows to register a user defined  Behaviour   in the
     * HANDLE_INFORM state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered  Behaviour
     * to the HashMap of this current behaviour. The registered behaviour can retrieve
     * the  inform   ACLMessage object received from the HashMap at the
     *  REPLY_KEY   key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleInform(Behaviour b) {
        registerState(b, HANDLE_INFORM);
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * This method allows to register a user defined  Behaviour   in the
     * HANDLE_ALL_RESPONSES state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered  Behaviour   to
     * the HashMap of this current behaviour. The registered behaviour can retrieve
     * the vector of ACLMessage confirms, disconfirms, informs, pending and responses
     * from the HashMap at  ALL_CONFIRMS_KEY  ,  ALL_DISCONFIRMS_KEY  ,
     *  ALL_INFORMS_KEY  ,  ALL_PH1_PENDINGS_KEY   and
     *  output   field.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleAllResponses(Behaviour b) {
        registerState(b, HANDLE_ALL_RESPONSES);
        b.setMapMessagesList(getMapMessagesList());
    }

    /* User CAN'T override these methods */
    //#APIDOC_EXCLUDE_BEGIN

    /**
     *
     */
    protected String[] getToBeReset() {
        if (toBeReset == null) {
            toBeReset = new String[]{
                    HANDLE_CONFIRM,
                    HANDLE_DISCONFIRM,
                    HANDLE_INFORM,
                    HANDLE_NOT_UNDERSTOOD,
                    HANDLE_FAILURE,
                    HANDLE_OUT_OF_SEQ
            };
        }
        return toBeReset;
    }

    /**
     * Prepare vector containing queryIfs.
     *
     * @param initiation queryIf passed in the constructor
     * @return Vector of queryIfs
     */
    protected final List<ACLMessage> prepareInitiations(ACLMessage initiation) {
        return prepareQueryIfs(initiation);
    }

    /**
     * This method sets for all prepared queryIfs  conversation-id   slot (with
     * value passed in the constructor),  protocol   slot and
     *  reply-with   slot with a unique value constructed by concatenating
     * receiver's agent name and phase number (i.e. 1). After that it sends all cfps.
     *
     * @param initiations vector prepared in PREPARE_QUERYIFS state
     */
    protected final void sendInitiations(List<ACLMessage> initiations) {
        getMapMessagesList().put(ALL_PENDINGS_KEY, new ArrayList<>());
        super.sendInitiations(initiations);

        totSessions = mapSessions.size();
    }

    /**
     * Check whether a reply is in-sequence and than update the appropriate Session
     * and removes corresponding queryif from vector of pendings.
     *
     * @param reply message received
     * @return true if reply is compliant with flow of protocol, false otherwise
     */
    protected final boolean checkInSequence(ACLMessage reply) {
        boolean ret = false;
        String inReplyTo = reply.getInReplyTo();
        Session s = (Session) mapSessions.get(inReplyTo);
        if (s != null) {
            int perf = reply.getPerformative();
            if (s.update(perf)) {
                // The reply is compliant to the protocol
                getMapMessagesList().get(ALL_RESPONSES_KEY).add(reply);

                switch (perf) {
                    case ACLMessage.CONFIRM -> getMapMessagesList().get(ALL_CONFIRMS_KEY).add(reply);
                    case ACLMessage.DISCONFIRM -> getMapMessagesList().get(ALL_DISCONFIRMS_KEY).add(reply);
                    case ACLMessage.INFORM -> getMapMessagesList().get(ALL_INFORMS_KEY).add(reply);
                }
                updatePendings(inReplyTo);
                ret = true;
            }
            if (s.isCompleted()) {
                mapSessions.remove(inReplyTo);
            }
        }
        return ret;
    }

    private void updatePendings(String key) {
        var pendings = getMapMessagesList().get(ALL_PENDINGS_KEY);
        pendings.removeIf(pendingMsg -> pendingMsg.getReplyWith().equals(key));
        //TODO: EA. Here, before, only the first msg was removed
    }

    /**
     * Check if there are still active sessions or if timeout is expired.
     *
     * @param reply last message received
     * @return ALL_RESPONSES_RECEIVED or -1 (still active sessions)
     */
    protected final int checkSessions(ACLMessage reply) {
        if (reply == null) {
            // Timeout expired --> clear all sessions
            mapSessions.clear();
        }
        if (mapSessions.size() == 0) {
            // We have finished --> fill the Vector of initiation messages for next
            // phase (unless already filled by the user)
            var ds = getMapMessagesList();
            var nextPhMsgs = ds.get(outputKey);
            if (nextPhMsgs.size() == 0) {
                var confirms = ds.get(ALL_CONFIRMS_KEY);
                var informs = ds.get(ALL_INFORMS_KEY);
                var pendings = ds.get(ALL_PENDINGS_KEY);
                fillNextPhInitiations(nextPhMsgs, confirms, informs, pendings);
            }
            return ALL_RESPONSES_RECEIVED;
        } else {
            // We are still waiting for some responses
            return -1;
        }
    }

    private void fillNextPhInitiations(List<ACLMessage> nextPhMsgs, List<ACLMessage> confirms, List<ACLMessage> informs, List<ACLMessage> pendings) {
        if ((confirms.size() + informs.size()) == totSessions) {
            // All responders replied with CONFIRM or INFORM --> Fill the vector
            // of initiation messages for next phase with ACCEPT_PROPOSAL
            for (ACLMessage confirm : confirms) {
                ACLMessage accept = confirm.createReply();
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                nextPhMsgs.add(accept);
            }
        } else {
            // At least one responder disconfirmed, failed or didn't reply --> Fill the vector
            // of initiation messages for next phase with REJECT_PROPOSALS
            for (ACLMessage confirm : confirms) {
                ACLMessage reject = confirm.createReply();
                reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                nextPhMsgs.add(reject);
            }
            for (ACLMessage pending : pendings) {
                ACLMessage reject = (ACLMessage) pending.clone();
                reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
                nextPhMsgs.add(reject);
            }
        }
    }

    /**
     * Initialize the data store.
     *
     * @param msg Ignored
     */
    protected void initializeHashMap(ACLMessage msg) {
        super.initializeHashMap(msg);
        getMapMessagesList().put(ALL_RESPONSES_KEY, new ArrayList<>());
        getMapMessagesList().put(ALL_CONFIRMS_KEY, new ArrayList<>());
        getMapMessagesList().put(ALL_DISCONFIRMS_KEY, new ArrayList<>());
        getMapMessagesList().put(ALL_INFORMS_KEY, new ArrayList<>());
        getMapMessagesList().put(outputKey, new ArrayList<>());
    }
    //#APIDOC_EXCLUDE_END


    protected ProtocolSession getSession(ACLMessage msg, int sessionIndex) {
        var pendings = getMapMessagesList().get(ALL_PENDINGS_KEY);
        pendings.add(msg);

        return new Session("R" + hashCode() + "_" + sessionIndex + "_" + TwoPhConstants.PH1);
    }

    /**
     * Inner class Session
     */
    class Session implements ProtocolSession, Serializable {
        // Session states 
        static final int INIT = 0;
        static final int REPLY_RECEIVED = 1;
        private final String myId;
        private int state = INIT;

        public Session(String id) {
            myId = id;
        }

        public String getId() {
            return myId;
        }


        /**
         * Return true if received ACLMessage is consistent with the protocol.
         *
         * @param perf performative
         * @return Return true if received ACLMessage is consistent with the protocol
         */
        public boolean update(int perf) {
            if (state == INIT) {
                switch (perf) {
                    case ACLMessage.CONFIRM:
                    case ACLMessage.DISCONFIRM:
                    case ACLMessage.INFORM:
                    case ACLMessage.NOT_UNDERSTOOD:
                    case ACLMessage.FAILURE:
                        state = REPLY_RECEIVED;
                        return true;
                    default:
                        return false;
                }
            } else {
                return false;
            }
        }

        public int getState() {
            return state;
        }

        public boolean isCompleted() {
            return (state == REPLY_RECEIVED);
        }
    }
}


