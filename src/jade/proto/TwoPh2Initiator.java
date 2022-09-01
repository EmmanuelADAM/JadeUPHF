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
public class TwoPh2Initiator extends Initiator {
    // Data store keys
    // Private data store keys (can't be static since if we register another instance of this class as state of the FSM
    // using the same data store the new values overrides the old one.
    /* FSM states names */
    private static final String HANDLE_INFORM = "Handle-Inform";
    private static final String HANDLE_OLD_RESPONSE = "Handle-old-response";
    private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";
    /* Possible TwoPh2Initiator's returned values */
    private static final int OLD_RESPONSE = 1000;
    private static final int ALL_RESPONSES_RECEIVED = 1;
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object passed in the constructor of the class.
     */
    public final String ACCEPTANCE_KEY = INITIATION_K;
    /**
     * key to retrieve from the HashMap of the behaviour the vector of
     * ACCEPT_PROPOSAL or REJECT_PROPOSAL messages that have to be sent.
     */
    public final String ALL_ACCEPTANCES_KEY = ALL_INITIATIONS_K;
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
     * key to retrieve from the HashMap of the behaviour the vector of
     * INFORM messages that have been received as response.
     */
    public final String ALL_INFORMS_KEY = "__all-informs" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the vector of
     * ACCEPT_PROPOSAL or REJECT_PROPOSAL messages for which a response
     * has not been received yet.
     */
    public final String ALL_PENDINGS_KEY = "__all-pendings" + hashCode();
    private String[] toBeReset = null;

    /**
     * Constructs a  TwoPh2Initiator   behaviour.
     *
     * @param a               The agent performing the protocol.
     * @param acceptance      msg
     * @param mapMessagesList  HashMap   of messages list that will be used by this  TwoPh2Initiator  .
     * deprecated

    public TwoPh2Initiator(Agent a, ACLMessage acceptance, HashMap<String, List<ACLMessage>> mapMessagesList) {
    super(a, acceptance, mapMessagesList);
    // Register the FSM transitions specific to the Two-Phase2-Commit protocol
    registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);
    registerTransition(CHECK_IN_SEQ, HANDLE_OLD_RESPONSE, OLD_RESPONSE);
    registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
    registerDefaultTransition(HANDLE_OLD_RESPONSE, CHECK_SESSIONS);
    registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
    registerDefaultTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL);

    // Create and register the states specific to the Two-Phase2-Commit protocol
    Behaviour b;

    // CHECK_IN_SEQ
    // We must override this state to distinguish the case in which
    // a response belonging to a previous phase is received (e.g. due
    // to network delay).
    b = new OneShotBehaviour(myAgent) {
    int ret;

    public void action() {
    ACLMessage reply = (ACLMessage) getMapMessagesList().get(REPLY_K);
    String inReplyTo = reply.getInReplyTo();
    String phase = inReplyTo.substring(inReplyTo.length() - 3);
    if (phase.equals(TwoPhConstants.PH0) || phase.equals(TwoPhConstants.PH1)) {
    // The reply belongs to a previous phase
    oldResponse(reply);
    ret = OLD_RESPONSE;
    } else {
    if (checkInSequence(reply)) {
    ret = reply.getPerformative();
    } else {
    ret = -1;
    }
    }
    }

    public int onEnd() {
    return ret;
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, CHECK_IN_SEQ);

    // HANDLE_INFORM state activated if arrived an inform message compliant with
    //conversationId and a receiver of one of accept/reject-proposal messages sent.
    b = new OneShotBehaviour(myAgent) {
    final int ret = -1;

    public void action() {
    ACLMessage inform = (ACLMessage) (getMapMessagesList().get(REPLY_KEY));
    handleInform(inform);
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, HANDLE_INFORM);

    // HANDLE_OLD_RESPONSE state activate if arrived a failure message coming
    //from phase 0 (timeout expired), a disconfirm or inform message coming from phase 1
    //(timeout expired).
    b = new OneShotBehaviour(myAgent) {
    public void action() {
    ACLMessage old = (ACLMessage) (getMapMessagesList().get(REPLY_KEY));
    handleOldResponse(old);
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, HANDLE_OLD_RESPONSE);

    // HANDLE_ALL_RESPONSES state activated when all the answers have been received.
    b = new OneShotBehaviour(myAgent) {
    public void action() {
    var responses = getMapMessagesList().get(ALL_RESPONSES_KEY);
    handleAllResponses(responses);
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, HANDLE_ALL_RESPONSES);

    }
     */

    /**
     * Constructs a  TwoPh2Initiator   behaviour.
     *
     * @param a          The agent performing the protocol.
     * @param acceptance msg
     */
    public TwoPh2Initiator(Agent a, ACLMessage acceptance) {
        this(a, acceptance, new HashMap<>(), new HashMap<>());
    }

    /* User can override these methods */

    /**
     * Constructs a  TwoPh2Initiator   behaviour.
     *
     * @param a               The agent performing the protocol.
     * @param acceptance      msg
     * @param mapMessagesList  HashMap   of messages list that will be used by this  TwoPh2Initiator  .
     * @param mapMessages      HashMap   of messages  that will be used by this  TwoPh2Initiator  .
     */
    public TwoPh2Initiator(Agent a, ACLMessage acceptance, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a, acceptance, mapMessagesList, mapMessages);
        /* Register the FSM transitions specific to the Two-Phase2-Commit protocol */
        registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);
        registerTransition(CHECK_IN_SEQ, HANDLE_OLD_RESPONSE, OLD_RESPONSE);
        registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_OLD_RESPONSE, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
        registerDefaultTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL);

        /* Create and register the states specific to the Two-Phase2-Commit protocol */
        Behaviour b;

        // CHECK_IN_SEQ
        // We must override this state to distinguish the case in which
        // a response belonging to a previous phase is received (e.g. due
        // to network delay).
        b = new OneShotBehaviour(myAgent) {
            int ret;

            public void action() {
                ACLMessage reply = (ACLMessage) getMapMessagesList().get(REPLY_K);
                String inReplyTo = reply.getInReplyTo();
                String phase = inReplyTo.substring(inReplyTo.length() - 3);
                if (phase.equals(TwoPhConstants.PH0) || phase.equals(TwoPhConstants.PH1)) {
                    // The reply belongs to a previous phase
                    oldResponse(reply);
                    ret = OLD_RESPONSE;
                } else {
                    if (checkInSequence(reply)) {
                        ret = reply.getPerformative();
                    } else {
                        ret = -1;
                    }
                }
            }

            public int onEnd() {
                return ret;
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, CHECK_IN_SEQ);

        /* HANDLE_INFORM state activated if arrived an inform message compliant with
        conversationId and a receiver of one of accept/reject-proposal messages sent. */
        b = new OneShotBehaviour(myAgent) {
            final int ret = -1;

            public void action() {
                ACLMessage inform = (ACLMessage) (getMapMessagesList().get(REPLY_KEY));
                handleInform(inform);
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_INFORM);

        /* HANDLE_OLD_RESPONSE state activate if arrived a failure message coming
        from phase 0 (timeout expired), a disconfirm or inform message coming from phase 1
        (timeout expired). */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                ACLMessage old = (ACLMessage) (getMapMessagesList().get(REPLY_KEY));
                handleOldResponse(old);
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_OLD_RESPONSE);

        /* HANDLE_ALL_RESPONSES state activated when all the answers have been received. */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                var responses = getMapMessagesList().get(ALL_RESPONSES_KEY);
                handleAllResponses(responses);
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_ALL_RESPONSES);

    }

    /**
     * This method must return the vector of ACLMessage objects to be sent.
     * It is called in the first state of this protocol. This default
     * implementation just returns the ACLMessage object passed in
     * the constructor. Programmers might prefer to override this method in order
     * to return a vector of ACCEPT_PROPOSAL or REJECT_PROPOSAL objects for 1:N
     * conversations.
     *
     * @param acceptance the ACLMessage object passed in the constructor
     * @return a Vector of ACLMessage objects. The value of the  reply-with
     * slot is ignored and regenerated automatically by this
     * class. Instead user can specify the  reply-by   slot representing phase2
     * timeout.
     */
    protected List<ACLMessage> prepareAcceptances(ACLMessage acceptance) {
        List<ACLMessage> v = new ArrayList<>(1);
        v.add(acceptance);
        return v;
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
     * This method is called every time a  failure  , a  disconfirm
     * or an  inform   message is received, which is not out-of-sequence
     * according to the protocol rules. This default implementation does nothing;
     * programmers might wish to override the method in case they need to react
     * to this event.
     *
     * @param old the received propose message
     */
    protected void handleOldResponse(ACLMessage old) {
    }

    /**
     * This method is called when all the responses have been collected. By response
     * message we intend here all the  inform   (phase 2),  failure
     * (phase 0),  disconfirm   (phase 1) and  inform   (phase 1)
     * received messages, which are not out-of-sequence according to the protocol rules.
     * This default implementation does nothing; programmers might wish to override the
     * method in case they need to react to this event by analysing all the messages in
     * just one call.
     *
     * @param responses all responses received
     */
    protected void handleAllResponses(List<ACLMessage> responses) {
    }

    /**
     * This method allows to register a user-defined  Behaviour   in the
     * PREPARE_ACCEPTANCES state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered  Behaviour   to the
     * HashMap of this current behaviour. It is responsibility of the registered
     * behaviour to put the  Vector   of ACLMessage objects to be sent into
     * the HashMap at the  ALL_ACCEPTANCES_KEY   key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerPrepareProposals(Behaviour b) {
        registerPrepareInitiations(b);
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
     * HANDLE_OLD_RESPONSE state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered  Behaviour
     * to the HashMap of this current behaviour. The registered behaviour can retrieve
     * the  failure, disconfirm or inform   ACLMessage object received
     * from the HashMap at the  REPLY_KEY   key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleOldResponse(Behaviour b) {
        registerState(b, HANDLE_OLD_RESPONSE);
        b.setMapMessagesList(getMapMessagesList());
    }

    /* User CAN'T override these methods */
    //#APIDOC_EXCLUDE_BEGIN

    /**
     * This method allows to register a user defined  Behaviour   in the
     * HANDLE_ALL_RESPONSES state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered  Behaviour   to
     * the HashMap of this current behaviour. The registered behaviour can retrieve
     * the vector of ACLMessage received from the HashMap at
     *  ALL_RESPONSES_RECEIVED_KEY  .
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleAllResponses(Behaviour b) {
        registerState(b, HANDLE_ALL_RESPONSES);
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     *
     */
    protected String[] getToBeReset() {
        if (toBeReset == null) {
            toBeReset = new String[]{
                    HANDLE_INFORM,
                    HANDLE_OLD_RESPONSE,
                    HANDLE_NOT_UNDERSTOOD,
                    HANDLE_FAILURE,
                    HANDLE_OUT_OF_SEQ
            };
        }
        return toBeReset;
    }

    /**
     * Returns vector of accept/reject-proposal stored in the data store at
     * key  inputKey   from previouse phase.
     *
     * @param initiation ignored
     * @return Vector of accept/reject-proposal
     */
    protected final List<ACLMessage> prepareInitiations(ACLMessage initiation) {
        return prepareAcceptances(initiation);
    }

    /**
     * This method sets for all prepared accept/reject-proposal
     *  conversation-id   slot (with value passed in the constructor),
     *  protocol   slot and  reply-with   slot with a unique
     * value constructed by concatenating receiver's agent name and phase number
     * (i.e. 2). After that it sends all accept/reject-proposal.
     *
     * @param initiations vector prepared in PREPARE_ACCEPTANCES state
     */
    protected final void sendInitiations(List<ACLMessage> initiations) {
        getMapMessagesList().put(ALL_PENDINGS_KEY, new ArrayList<>());

        super.sendInitiations(initiations);
    }

    /**
     * Check whether a reply is in-sequence and than update the appropriate Session
     * and removes corresponding accept/reject-proposal from vector of pendings.
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
                if (perf == ACLMessage.INFORM) {
                    getMapMessagesList().get(ALL_INFORMS_KEY).add(reply);
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
        //TODO: E.A. Here, before, only the first msg that corresponds to the condition was removed
    }

    private void oldResponse(ACLMessage reply) {
        String inReplyTo = reply.getInReplyTo();
        String sessionKey = inReplyTo.substring(0, inReplyTo.length() - 3) + "PH2";
        int perf = reply.getPerformative();
        if (perf == ACLMessage.FAILURE || perf == ACLMessage.NOT_UNDERSTOOD || perf == ACLMessage.DISCONFIRM) {
            mapSessions.remove(sessionKey);
            updatePendings(sessionKey);
        }
    }

    /**
     * Check if there are still active sessions or if timeout is expired.
     *
     * @param reply last message received
     * @return ALL_RESPONSES_RECEIVED, -1 (still active sessions)
     */
    protected final int checkSessions(ACLMessage reply) {
        if (reply == null) {
            // Timeout expired --> clear all sessions
            mapSessions.clear();
        }
        if (mapSessions.size() == 0) {
            // We have finished
            return ALL_RESPONSES_RECEIVED;
        } else {
            // We are still waiting for some responses
            return -1;
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
        getMapMessagesList().put(ALL_INFORMS_KEY, new ArrayList<>());
    }
    //#APIDOC_EXCLUDE_END


    protected ProtocolSession getSession(ACLMessage msg, int sessionIndex) {
        var pendings = getMapMessagesList().get(ALL_PENDINGS_KEY);
        pendings.add(msg);

        return new Session("R" + hashCode() + "_" + sessionIndex + "_" + TwoPhConstants.PH2);
    }

    /**
     * Inner class Session
     */
    class Session implements ProtocolSession, Serializable {
        // Possible Session states 
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
                    case ACLMessage.INFORM:
                    case ACLMessage.FAILURE:
                    case ACLMessage.NOT_UNDERSTOOD:
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


