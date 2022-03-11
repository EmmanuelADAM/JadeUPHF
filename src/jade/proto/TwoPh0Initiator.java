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
public class TwoPh0Initiator extends Initiator {
    // Data store keys
    // Private data store keys (can't be static since if we register another instance of this class as state of the FSM
    // using the same data store the new values overrides the old one.
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object passed in the constructor of the class.
     */
    public final String CFP_KEY = INITIATION_K;
    /**
     * key to retrieve from the HashMap of the behaviour the vector of
     * CFP messages that have to be sent.
     */
    public final String ALL_CFPS_KEY = ALL_INITIATIONS_K;
    /**
     * key to retrieve from the HashMap of the behaviour the last
     * ACLMessage object that has been received (null if the timeout
     * expired).
     */
    public final String REPLY_KEY = REPLY_K;
    /**
     * key to retrieve from the HashMap of the behaviour the vector of
     * all messages that have been received as response.
     */
    public final String ALL_RESPONSES_KEY = "__all-responses" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the vector of
     * PROPOSE messages that have been received as response.
     */
    public final String ALL_PROPOSES_KEY = "__all-proposes" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the vector of
     * CFP messages for which a response has not been received yet.
     */
    public final String ALL_PENDINGS_KEY = "__all-pendings" + hashCode();

    /* FSM states names */
    private static final String HANDLE_PROPOSE = "Handle-Propose";
    private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";

    private static final int ALL_RESPONSES_RECEIVED = 1;

    /* Data store output key */
    private final String outputKey;

    private int totSessions;

    /**
     * Constructs a <code>TwoPh0Initiator</code> behaviour.
     *
     * @param a         The agent performing the protocol.
     * @param cfp       The message that must be used to initiate the protocol.
     *                  Notice that the default implementation of the <code>prepareCfps</code> method
     *                  returns an array composed of that message only.
     * @param outputKey Data store key where the behaviour will store the Vector
     *                  of messages to be sent to initiate the successive phase.
     */
    public TwoPh0Initiator(Agent a, ACLMessage cfp, String outputKey) {
        this(a, cfp, outputKey, new HashMap<>(), new HashMap<>());
    }

    /**
     * Constructs a <code>TwoPh0Initiator</code> behaviour.
     *
     * @param a               The agent performing the protocol.
     * @param cfp             The message that must be used to initiate the protocol.
     *                        Notice that the default implementation of the <code>prepareCfps</code> method
     *                        returns an array composed of that message only.
     * @param outputKey       Data store key where the behaviour will store the Vector
     *                        of messages to be sent to initiate the successive phase.
     * @param mapMessagesList <code>HashMap</code> of messages list that will be used by this <code>TwoPh0Initiator</code>.
     * @deprecated
     */
    public TwoPh0Initiator(Agent a, ACLMessage cfp, String outputKey, HashMap<String, List<ACLMessage>> mapMessagesList) {
        super(a, cfp, mapMessagesList);
        //this.conversationId = conversationId;
        this.outputKey = outputKey;
        // Register the FSM transitions specific to the Two-Phase0-Commit protocol
        registerTransition(CHECK_IN_SEQ, HANDLE_PROPOSE, ACLMessage.PROPOSE);
        registerDefaultTransition(HANDLE_PROPOSE, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED); // update1
        registerDefaultTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL);

        // Create and register the states specific to the Two-Phase0-Commit protocol */
        Behaviour b;

        // HANDLE_PROPOSE 
        // This state is activated when a propose message is received as a reply
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                ACLMessage propose = getMapMessages().get(REPLY_KEY);
                handlePropose(propose);
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        registerState(b, HANDLE_PROPOSE);

        // HANDLE_ALL_RESPONSES 
        // This state is activated when all the responsess have been 
        // received or the specified timeout has expired.
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                var responses = getMapMessagesList().get(ALL_RESPONSES_KEY);
                var proposes = getMapMessagesList().get(ALL_PROPOSES_KEY);
                var pendings = getMapMessagesList().get(ALL_PENDINGS_KEY);
                var nextPhMsgs = getMapMessagesList().get(TwoPh0Initiator.this.outputKey);
                handleAllResponses(responses, proposes, pendings, nextPhMsgs);
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        registerState(b, HANDLE_ALL_RESPONSES);
    }

    /**
     * Constructs a <code>TwoPh0Initiator</code> behaviour.
     *
     * @param a               The agent performing the protocol.
     * @param cfp             The message that must be used to initiate the protocol.
     *                        Notice that the default implementation of the <code>prepareCfps</code> method
     *                        returns an array composed of that message only.
     * @param outputKey       Data store key where the behaviour will store the Vector
     *                        of messages to be sent to initiate the successive phase.
     * @param mapMessagesList <code>HashMap</code> of messages list that will be used by this <code>TwoPh0Initiator</code>.
     * @param mapMessages     <code>HashMap</code> of messages  that will be used by this <code>TwoPh0Initiator</code>.
     */
    public TwoPh0Initiator(Agent a, ACLMessage cfp, String outputKey, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a, cfp, mapMessagesList, mapMessages);
        //this.conversationId = conversationId;
        this.outputKey = outputKey;
        // Register the FSM transitions specific to the Two-Phase0-Commit protocol
        registerTransition(CHECK_IN_SEQ, HANDLE_PROPOSE, ACLMessage.PROPOSE);
        registerDefaultTransition(HANDLE_PROPOSE, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED); // update1
        registerDefaultTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL);

        // Create and register the states specific to the Two-Phase0-Commit protocol */
        Behaviour b;

        // HANDLE_PROPOSE
        // This state is activated when a propose message is received as a reply
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                ACLMessage propose = getMapMessages().get(REPLY_KEY);
                handlePropose(propose);
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        registerState(b, HANDLE_PROPOSE);

        // HANDLE_ALL_RESPONSES
        // This state is activated when all the responsess have been
        // received or the specified timeout has expired.
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                var responses = getMapMessagesList().get(ALL_RESPONSES_KEY);
                var proposes = getMapMessagesList().get(ALL_PROPOSES_KEY);
                var pendings = getMapMessagesList().get(ALL_PENDINGS_KEY);
                var nextPhMsgs = getMapMessagesList().get(TwoPh0Initiator.this.outputKey);
                handleAllResponses(responses, proposes, pendings, nextPhMsgs);
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        registerState(b, HANDLE_ALL_RESPONSES);
    }

    public int onEnd() {
        //TODO: EA. VERIFIER ce que peut être outpukey
        var nextPhMsgs = getMapMessagesList().get(outputKey);
        if (nextPhMsgs.size() != 0) {
            return (nextPhMsgs.get(0)).getPerformative();
        } else {
            return -1;
        }
    }

    private String[] toBeReset = null;

    /* User can override these methods */

    /**
     * This method must return the vector of ACLMessage objects to be sent.
     * It is called in the first state of this protocol. This default
     * implementation just returns the ACLMessage object (a CFP) passed in
     * the constructor. Programmers might prefer to override this method in order
     * to return a vector of CFP objects for 1:N conversations.
     *
     * @param cfp the ACLMessage object passed in the constructor
     * @return a vector of ACLMessage objects. The value of the <code>reply-with</code>
     * slot is ignored and regenerated automatically by this
     * class. Instead user can specify <code>reply-by</code> slot representing phase0
     * timeout.
     */
    protected List<ACLMessage> prepareCfps(ACLMessage cfp) {
        List<ACLMessage> v = new ArrayList<>(1);
        v.add(cfp);
        return v;
    }

    /**
     * This method is called every time a <code>propose</code> message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     *
     * @param propose the received propose message
     */
    protected void handlePropose(ACLMessage propose) {
    }

    /**
     * This method is called when all the responses have been collected or when
     * the timeout is expired. The used timeout is the minimum value of the slot
     * <code>reply-By</code> of all the sent messages. By response message we
     * intend here all the <code>propose, failure, not-understood</code> received messages, which
     * are not out-of-sequence according to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override this method to modify the Vector of initiation messages
     * (<code>nextPhMsgs</code>) for next phase. More in details this Vector
     * already includes messages with the performative set according to the
     * default protocol rules i.e. QUERY_IF (if all responders replied with
     * PROPOSE) or REJECT_PROPOSAL (if at least one responder failed or didn't reply).
     * In particular, by setting the <code>reply-by</code> slot, users can
     * specify a timeout for next phase.
     *
     * @param responses  The Vector of all messages received as response
     * @param proposes   The Vector of PROPOSE messages received as response
     * @param pendings   The Vector of CFP messages for which a response has not
     *                   been received yet
     * @param nextPhMsgs The Vector of initiation messages for next phase already
     *                   filled with <code>QUERY_IF</code> messages (if all responders replied with
     *                   <code>PROPOSE</code>) or <code>REJECT_PROPOSAL</code> (if at least one
     *                   responder failed or didn't reply).
     */
    protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> proposes,
                                      List<ACLMessage> pendings, List<ACLMessage> nextPhMsgs) {
    }

    /**
     * This method allows to register a user-defined <code>Behaviour</code> in the
     * PREPARE_CFPS state. This behaviour would override the homonymous method. This
     * method also set the data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour. It is responsibility of the registered
     * behaviour to put the <code>Vector</code> of ACLMessage objects to be sent into
     * the HashMap at the <code>ALL_CFPS_KEY</code> key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerPrepareCfps(Behaviour b) {
        registerPrepareInitiations(b);
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_PROPOSE state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code>
     * to the HashMap of this current behaviour. The registered behaviour can retrieve
     * the <code>propose</code> ACLMessage object received from the HashMap at the
     * <code>REPLY_KEY</code> key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandlePropose(Behaviour b) {
        registerState(b, HANDLE_PROPOSE);
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code> in the
     * HANDLE_ALL_RESPONSES state. This behaviour would override the homonymous method.
     * This method also set the data store of the registered <code>Behaviour</code> to
     * the HashMap of this current behaviour. The registered behaviour can retrieve
     * the vector of ACLMessage proposes, failures, pending and responses from the
     * HashMap at <code>ALL_PROPOSES_KEY</code>, <code>ALL_FAILURES_KEY</code>,
     * <code>ALL_PH0_PENDINGS_KEY</code> and <code>output</code> field.
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
                    HANDLE_PROPOSE,
                    HANDLE_NOT_UNDERSTOOD,
                    HANDLE_FAILURE,
                    HANDLE_OUT_OF_SEQ
            };
        }
        return toBeReset;
    }

    /**
     * Prepare vector containing cfps.
     *
     * @param initiation cfp passed in the constructor
     * @return Vector of cfps
     */
    protected final List<ACLMessage> prepareInitiations(ACLMessage initiation) {
        return prepareCfps(initiation);
    }

    /**
     * This method sets for all prepared cfps <code>conversation-id</code> slot (with
     * value passed in the constructor), <code>protocol</code> slot and
     * <code>reply-with</code> slot with a unique value
     * constructed by concatenating receiver's agent name and phase number (i.e. 0).
     * After that it sends all cfps.
     *
     * @param initiations vector prepared in PREPARE_CFPS state
     */
    protected final void sendInitiations(List<ACLMessage> initiations) {
        getMapMessagesList().put(ALL_PENDINGS_KEY, new ArrayList<>());

        super.sendInitiations(initiations);

        totSessions = mapSessions.size();
    }

    /**
     * Check whether a reply is in-sequence and than update the appropriate Session
     * and removes corresponding cfp from vector of pendings.
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
                if (perf == ACLMessage.PROPOSE) {
                    getMapMessagesList().get(ALL_PROPOSES_KEY).add(reply);
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
        pendings.removeIf(msg -> msg.getReplyWith().equals(key));
        //TODO: in fact remove the first that corresponds to the condition
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
                var proposes = ds.get(ALL_PROPOSES_KEY);
                var pendings = ds.get(ALL_PENDINGS_KEY);
                fillNextPhInitiations(nextPhMsgs, proposes, pendings);
            }
            return ALL_RESPONSES_RECEIVED;
        } else {
            // We are still waiting for some responses
            return -1;
        }
    }

    private void fillNextPhInitiations(List<ACLMessage> nextPhMsgs, List<ACLMessage> proposes, List<ACLMessage> pendings) {
        if (proposes.size() == totSessions) {
            // All responders replied with PROPOSE --> Fill the vector
            // of initiation messages for next phase with QUERY_IF
            for (ACLMessage propose : proposes) {
                ACLMessage queryIf = propose.createReply();
                queryIf.setPerformative(ACLMessage.QUERY_IF);
                nextPhMsgs.add(queryIf);
            }
        } else {
            // At least one responder failed or didn't reply --> Fill the vector
            // of initiation messages for next phase with REJECT_PROPOSALS
            for (ACLMessage propose : proposes) {
                ACLMessage reject = propose.createReply();
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
     * @param msg Message passed in the constructor
     */
    protected void initializeHashMap(ACLMessage msg) {
        super.initializeHashMap(msg);
        getMapMessagesList().put(ALL_RESPONSES_KEY, new ArrayList<>());
        getMapMessagesList().put(ALL_PROPOSES_KEY, new ArrayList<>());
        getMapMessagesList().put(outputKey, new ArrayList<>());
    }
    //#APIDOC_EXCLUDE_END


    protected ProtocolSession getSession(ACLMessage msg, int sessionIndex) {
        var pendings = getMapMessagesList().get(ALL_PENDINGS_KEY);
        pendings.add(msg);

        return new Session("R" + hashCode() + "_" + sessionIndex + "_" + TwoPhConstants.PH0);
    }

    /**
     * Inner class Session
     */
    class Session implements ProtocolSession, Serializable {
        // Session states 
        static final int INIT = 0;
        static final int REPLY_RECEIVED = 1;

        private int state = INIT;
        private final String myId;

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
                    case ACLMessage.PROPOSE:
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


