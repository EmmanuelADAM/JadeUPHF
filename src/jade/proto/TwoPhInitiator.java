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
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class description
 *
 * @author Elena Quarantotto - TILAB
 * @author Giovanni Caire - TILAB
 */
public class TwoPhInitiator extends FSMBehaviour {
    /* FSM states names */
    public static final String PH0_STATE = "Ph0";
    public static final String PH1_STATE = "Ph1";
    public static final String PH2_STATE = "Ph2";
    private static final String DUMMY_FINAL = "Dummy-final";
    private static final String TEMP = "__temp";

    private final boolean logging = true; // @todo REMOVE IT!!!!!!!!!!!
    private final boolean currentLogging = true; // @todo REMOVE IT!!!!!!!!!!!

    /**
     * Constructs a  TwoPhInitiator   behaviour.
     *
     * @param a   The agent performing the protocol.
     * @param cfp The message that must be used to initiate the protocol.
     *            Notice that the default implementation of the  prepareCfps   method
     *            returns an array composed of that message only.
     */
    public TwoPhInitiator(Agent a, ACLMessage cfp) {
        this(a, cfp, new HashMap<>(), new HashMap<>());
    }

    /**
     * Constructs a  TwoPhInitiator   behaviour.
     *
     * @param a               The agent performing the protocol.
     * @param cfp             The message that must be used to initiate the protocol.
     *                        Notice that the default implementation of the  prepareCfps   method
     *                        returns an array composed of that message only.
     * @param mapMessagesList  HashMap   of messages list that will be used by this  TwoPhInitiator  .
     * deprecated

    public TwoPhInitiator(Agent a, ACLMessage cfp, HashMap<String, List<ACLMessage>> mapMessagesList) {
    super(a);
    setMapMessagesList(mapMessagesList);
    // Register the FSM transitions specific to the Two-Phase-Commit protocol
    registerTransition(PH0_STATE, PH1_STATE, ACLMessage.QUERY_IF);
    registerTransition(PH0_STATE, PH2_STATE, ACLMessage.REJECT_PROPOSAL);
    registerTransition(PH0_STATE, DUMMY_FINAL, -1);
    registerTransition(PH0_STATE, PH0_STATE, ACLMessage.CFP, new String[]{PH0_STATE});

    registerTransition(PH1_STATE, PH2_STATE, ACLMessage.ACCEPT_PROPOSAL);
    registerTransition(PH1_STATE, PH2_STATE, ACLMessage.REJECT_PROPOSAL);
    registerTransition(PH1_STATE, DUMMY_FINAL, -1); // fix

    // Create and register the states specific to the Two-Phase-Commit protocol
    Behaviour b;

    // PH0_STATE activated for the first time. It sends cfps messages and wait
    // for a propose (operation completed), a failure (operation failed) or
    // expiration of timeout.
    b = new TwoPh0Initiator(myAgent, cfp, TEMP, mapMessagesList) {

    protected List<ACLMessage> prepareCfps(ACLMessage cfp) {
    return TwoPhInitiator.this.prepareCfps(cfp);
    }

    protected void handlePropose(ACLMessage propose) {
    TwoPhInitiator.this.handlePropose(propose);
    }

    protected void handleFailure(ACLMessage failure) {
    TwoPhInitiator.this.handleFailure(failure);
    }

    protected void handleNotUnderstood(ACLMessage notUnderstood) {
    TwoPhInitiator.this.handleNotUnderstood(notUnderstood);
    }

    protected void handleOutOfSequence(ACLMessage msg) {
    TwoPhInitiator.this.handleOutOfSequence(msg);
    }

    protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> proposes, List<ACLMessage> pendings, List<ACLMessage> nextPhMsgs) {
    TwoPhInitiator.this.handleAllPh0Responses(responses, proposes, pendings, nextPhMsgs);
    }
    };
    registerFirstState(b, PH0_STATE);

    // PH1_STATE activated if phase 0 succeded (all propose in phase 0). It
    // sends queryIf messages and wait for a confirm (receiver prepared), a
    // disconfirm (receiver aborted), an inform (receiver not changed) or
    // expiration of timeout.
    b = new TwoPh1Initiator(myAgent, null, TEMP, mapMessagesList) {
    protected void initializeHashMap(ACLMessage msg) {
    // Use the QUERY_IF messages prepared in previous phase
    var v = getMapMessagesList().get(TEMP);
    getMapMessagesList().put(ALL_QUERYIFS_KEY, v);
    super.initializeHashMap(msg);
    }

    protected void handleConfirm(ACLMessage confirm) {
    TwoPhInitiator.this.handleConfirm(confirm);
    }

    protected void handleDisconfirm(ACLMessage disconfirm) {
    TwoPhInitiator.this.handleDisconfirm(disconfirm);
    }

    protected void handleInform(ACLMessage inform) {
    TwoPhInitiator.this.handlePh1Inform(inform);
    }

    protected void handleFailure(ACLMessage failure) {
    TwoPhInitiator.this.handleFailure(failure);
    }

    protected void handleNotUnderstood(ACLMessage notUnderstood) {
    TwoPhInitiator.this.handleNotUnderstood(notUnderstood);
    }

    protected void handleOutOfSequence(ACLMessage msg) {
    TwoPhInitiator.this.handleOutOfSequence(msg);
    }

    protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> confirms, List<ACLMessage> disconfirms,
    List<ACLMessage> informs, List<ACLMessage> pendings, List<ACLMessage> nextPhMsgs) {
    TwoPhInitiator.this.handleAllPh1Responses(responses, confirms, disconfirms, informs, pendings, nextPhMsgs);
    }
    };
    registerState(b, PH1_STATE);

    // PH2_STATE activated when phase 0 fails (some failure or expiration
    // of timeout), phase 1 fails (some disconfirm or expiration of timeout) or
    // phase 1 succeds (no disconfirms). In the first and third case it sends
    // reject-proposal; in the second case it sends accept-proposal.
    b = new TwoPh2Initiator(myAgent, null, mapMessagesList) {
    protected void initializeHashMap(ACLMessage msg) {
    // Use the acceptance messages prepared in previous phase
    var v = getMapMessagesList().get(TEMP);
    getMapMessagesList().put(ALL_ACCEPTANCES_KEY, v);
    super.initializeHashMap(msg);
    }

    protected void handleInform(ACLMessage inform) {
    TwoPhInitiator.this.handlePh2Inform(inform);
    }

    protected void handleOldResponse(ACLMessage old) {
    TwoPhInitiator.this.handleOldResponse(old);
    }

    protected void handleFailure(ACLMessage failure) {
    TwoPhInitiator.this.handleFailure(failure);
    }

    protected void handleNotUnderstood(ACLMessage notUnderstood) {
    TwoPhInitiator.this.handleNotUnderstood(notUnderstood);
    }

    protected void handleOutOfSequence(ACLMessage msg) {
    TwoPhInitiator.this.handleOutOfSequence(msg);
    }

    protected void handleAllResponses(List<ACLMessage> responses) {
    TwoPhInitiator.this.handleAllPh2Responses(responses);
    }
    };
    registerLastState(b, PH2_STATE);

    // DUMMY_FINAL
    b = new OneShotBehaviour(myAgent) {
    public void action() {
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerLastState(b, DUMMY_FINAL);
    }
     */
    /**
     * Constructs a  TwoPhInitiator   behaviour.
     *
     * @param a               The agent performing the protocol.
     * @param cfp             The message that must be used to initiate the protocol.
     *                        Notice that the default implementation of the  prepareCfps   method
     *                        returns an array composed of that message only.
     * @param mapMessagesList  HashMap   of messages list that will be used by this  TwoPhInitiator  .
     * @param mapMessages      HashMap   of messages  that will be used by this  TwoPhInitiator  .
     */
    public TwoPhInitiator(Agent a, ACLMessage cfp, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a);
        setMapMessagesList(mapMessagesList);
        setMapMessages(mapMessages);
        // Register the FSM transitions specific to the Two-Phase-Commit protocol
        registerTransition(PH0_STATE, PH1_STATE, ACLMessage.QUERY_IF);
        registerTransition(PH0_STATE, PH2_STATE, ACLMessage.REJECT_PROPOSAL);
        registerTransition(PH0_STATE, DUMMY_FINAL, -1);
        registerTransition(PH0_STATE, PH0_STATE, ACLMessage.CFP, new String[]{PH0_STATE});

        registerTransition(PH1_STATE, PH2_STATE, ACLMessage.ACCEPT_PROPOSAL);
        registerTransition(PH1_STATE, PH2_STATE, ACLMessage.REJECT_PROPOSAL);
        registerTransition(PH1_STATE, DUMMY_FINAL, -1); // fix

        // Create and register the states specific to the Two-Phase-Commit protocol
        Behaviour b;

        /* PH0_STATE activated for the first time. It sends cfps messages and wait
        for a propose (operation completed), a failure (operation failed) or
        expiration of timeout. */
        b = new TwoPh0Initiator(myAgent, cfp, TEMP, mapMessagesList, mapMessages) {

            protected List<ACLMessage> prepareCfps(ACLMessage cfp) {
                return TwoPhInitiator.this.prepareCfps(cfp);
            }

            protected void handlePropose(ACLMessage propose) {
                TwoPhInitiator.this.handlePropose(propose);
            }

            protected void handleFailure(ACLMessage failure) {
                TwoPhInitiator.this.handleFailure(failure);
            }

            protected void handleNotUnderstood(ACLMessage notUnderstood) {
                TwoPhInitiator.this.handleNotUnderstood(notUnderstood);
            }

            protected void handleOutOfSequence(ACLMessage msg) {
                TwoPhInitiator.this.handleOutOfSequence(msg);
            }

            protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> proposes, List<ACLMessage> pendings, List<ACLMessage> nextPhMsgs) {
                TwoPhInitiator.this.handleAllPh0Responses(responses, proposes, pendings, nextPhMsgs);
            }
        };
        registerFirstState(b, PH0_STATE);

        /* PH1_STATE activated if phase 0 succeded (all propose in phase 0). It
        sends queryIf messages and wait for a confirm (receiver prepared), a
        disconfirm (receiver aborted), an inform (receiver not changed) or
        expiration of timeout. */
        b = new TwoPh1Initiator(myAgent, null, TEMP, mapMessagesList, mapMessages) {
            protected void initializeHashMap(ACLMessage msg) {
                // Use the QUERY_IF messages prepared in previous phase
                var v = getMapMessagesList().get(TEMP);
                getMapMessagesList().put(ALL_QUERYIFS_KEY, v);
                super.initializeHashMap(msg);
            }

            protected void handleConfirm(ACLMessage confirm) {
                TwoPhInitiator.this.handleConfirm(confirm);
            }

            protected void handleDisconfirm(ACLMessage disconfirm) {
                TwoPhInitiator.this.handleDisconfirm(disconfirm);
            }

            protected void handleInform(ACLMessage inform) {
                TwoPhInitiator.this.handlePh1Inform(inform);
            }

            protected void handleFailure(ACLMessage failure) {
                TwoPhInitiator.this.handleFailure(failure);
            }

            protected void handleNotUnderstood(ACLMessage notUnderstood) {
                TwoPhInitiator.this.handleNotUnderstood(notUnderstood);
            }

            protected void handleOutOfSequence(ACLMessage msg) {
                TwoPhInitiator.this.handleOutOfSequence(msg);
            }

            protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> confirms, List<ACLMessage> disconfirms,
                                              List<ACLMessage> informs, List<ACLMessage> pendings, List<ACLMessage> nextPhMsgs) {
                TwoPhInitiator.this.handleAllPh1Responses(responses, confirms, disconfirms, informs, pendings, nextPhMsgs);
            }
        };
        registerState(b, PH1_STATE);

        /* PH2_STATE activated when phase 0 fails (some failure or expiration
        of timeout), phase 1 fails (some disconfirm or expiration of timeout) or
        phase 1 succeds (no disconfirms). In the first and third case it sends
        reject-proposal; in the second case it sends accept-proposal. */
        b = new TwoPh2Initiator(myAgent, null, mapMessagesList, mapMessages) {
            protected void initializeHashMap(ACLMessage msg) {
                // Use the acceptance messages prepared in previous phase
                var v = getMapMessagesList().get(TEMP);
                getMapMessagesList().put(ALL_ACCEPTANCES_KEY, v);
                super.initializeHashMap(msg);
            }

            protected void handleInform(ACLMessage inform) {
                TwoPhInitiator.this.handlePh2Inform(inform);
            }

            protected void handleOldResponse(ACLMessage old) {
                TwoPhInitiator.this.handleOldResponse(old);
            }

            protected void handleFailure(ACLMessage failure) {
                TwoPhInitiator.this.handleFailure(failure);
            }

            protected void handleNotUnderstood(ACLMessage notUnderstood) {
                TwoPhInitiator.this.handleNotUnderstood(notUnderstood);
            }

            protected void handleOutOfSequence(ACLMessage msg) {
                TwoPhInitiator.this.handleOutOfSequence(msg);
            }

            protected void handleAllResponses(List<ACLMessage> responses) {
                TwoPhInitiator.this.handleAllPh2Responses(responses);
            }
        };
        registerLastState(b, PH2_STATE);

        /* DUMMY_FINAL */
        b = new OneShotBehaviour(myAgent) {
            public void action() {
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerLastState(b, DUMMY_FINAL);
    }

    /**
     * This method must return the vector of ACLMessage objects to be sent.
     * It is called in the first state of this protocol. This default
     * implementation just returns the ACLMessage object (a CFP) passed in
     * the constructor. Programmers might prefer to override this method in order
     * to return a vector of CFP objects for 1:N conversations.
     *
     * @param cfp the ACLMessage object passed in the constructor
     * @return a vector of ACLMessage objects. The values of the slot  reply-with
     * and  conversation-id   are ignored and regenerated automatically by this
     * class. Instead user can specify  reply-by   slot representing phase0
     * timeout.
     */
    protected List<ACLMessage> prepareCfps(ACLMessage cfp) {
        List<ACLMessage> v = new ArrayList<>(1);
        v.add(cfp);
        return v;
    }

    /**
     * This method is called every time a  propose   message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     *
     * @param propose the received propose message
     */
    protected void handlePropose(ACLMessage propose) {
    }

    /**
     * This method is called every time a  failure   message is received,
     * which is not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event.
     *
     * @param failure the received propose message
     */
    protected void handleFailure(ACLMessage failure) {
    }

    /**
     * This method is called when all the responses of phase 0 have been collected or when
     * the timeout is expired. The used timeout is the minimum value of the slot
     *  reply-By   of all the CFP messages sent.By response message we
     * intend here all the  propose, failure, not-understood   received messages, which
     * are not out-of-sequence according to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override this method to modify the Vector of initiation messages
     * ( nextPhMsgs  ) for next phase. More in details this Vector
     * already includes messages with the performative set according to the
     * default protocol rules i.e. QUERY_IF (if all responders replied with
     * PROPOSE) or REJECT_PROPOSAL (if at least one responder failed or didn't reply).
     * In particular, by setting the  reply-by   slot, users can
     * specify a timeout for next phase.
     *
     * @param responses  The Vector of all messages received as response in phase 0
     * @param proposes   The Vector of PROPOSE messages received as response in phase 0
     * @param pendings   The Vector of CFP messages for which a response has not
     *                   been received yet.
     * @param nextPhMsgs The Vector of initiation messages for next phase already
     *                   filled with  QUERY_IF   messages (if all responders replied with
     *                    PROPOSE  ) or  REJECT_PROPOSAL   (if at least one
     *                   responder failed or didn't reply).
     */
    protected void handleAllPh0Responses(List<ACLMessage> responses, List<ACLMessage> proposes, List<ACLMessage> pendings, List<ACLMessage> nextPhMsgs) {
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
     * This method is called every time an  inform   message in phase 1
     * is received, which is not out-of-sequence according to the protocol rules.
     * This default implementation does nothing; programmers might wish to override
     * the method in case they need to react to this event.
     *
     * @param inform the received propose message
     */
    protected void handlePh1Inform(ACLMessage inform) {
    }

    /**
     * This method is called in phase 1 when all the responses have been collected or when
     * the timeout is expired. The used timeout is the minimum value of the slot
     *  reply-By   of all the sent messages. By response message we
     * intend here all the  disconfirm, confirm, inform   received messages,
     * which are not out-of-sequence according to the protocol rules. This default
     * implementation does nothing; programmers might wish to override the method
     * in case they need to react to this event by analysing all the messages in
     * just one call.
     *
     * @param responses   The Vector of all messages received as response in phase 1
     * @param confirms    all confirms received
     * @param disconfirms all disconfirms received
     * @param pendings    all queryIfs still pending
     * @param nextPhMsgs  prepared responses for next phase:  accept-proposal
     *                    or  reject-proposal
     */
    protected void handleAllPh1Responses(List<ACLMessage> responses, List<ACLMessage> confirms, List<ACLMessage> disconfirms, List<ACLMessage> informs, List<ACLMessage> pendings, List<ACLMessage> nextPhMsgs) {
    }

    /**
     * This method is called every time an  inform   message in phase 2
     * is received, which is not out-of-sequence according to the protocol rules.
     * This default implementation does nothing; programmers might wish to override
     * the method in case they need to react to this event.
     *
     * @param inform the received propose message
     */
    protected void handlePh2Inform(ACLMessage inform) {
    }

    /**
     * This method is called every time a  failure  , a  disconfirm
     * or an  inform   message is received in phase 2, which is not out-of-sequence
     * according to the protocol rules. This default implementation does nothing;
     * programmers might wish to override the method in case they need to react
     * to this event.
     *
     * @param old the received propose message
     */
    protected void handleOldResponse(ACLMessage old) {
    }

    /**
     * This method is called in phase 2 when all the responses have been collected.
     * By response message we intend here all the  inform   (phase 2),
     *  failure   (phase 0),  disconfirm   (phase 1) and
     *  inform   (phase 1) received messages, which are not out-of-sequence
     * according to the protocol rules. This default implementation does nothing;
     * programmers might wish to override the method in case they need to react to
     * this event by analysing all the messages in just one call.
     *
     * @param responses all responses received in phase 2
     */
    protected void handleAllPh2Responses(List<ACLMessage> responses) {
    }

    /**
     * This method is called every time a message is received in phase n (use
     *  getCurrentPhase   method to know the phase), which is
     * out-of-sequence according to the protocol rules. This default implementation
     * does nothing; programmers might wish to override the method in case they need
     * to react to this event.
     *
     * @param msg the received message
     */
    protected void handleOutOfSequence(ACLMessage msg) {
    }

    protected void handleNotUnderstood(ACLMessage notUnderstood) {
    }

    public String getCurrentPhase() {
        return getCurrent().getBehaviourName();
    }

    public Behaviour getPhase(String name) {
        return getState(name);
    }
}
