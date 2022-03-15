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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * This class implements the initiator role in a Fipa-Contract-Net or Iterated-Fipa-Contract-Net
 * interaction protocol.<br>
 * This implementation works both for 1:1 and 1:N conversation.
 * <p>
 * The following is a brief description of the protocol. The programmer
 * should however refer to the
 * <a href=http://www.fipa.org/specs/fipa00061/XC00061D.html>FIPA Spec</a>
 * for a complete description.
 * <p>
 * The initiator solicits proposals from other agents by sending
 * a <code>CFP</code> message that specifies the action to be performed
 * and, if needed, conditions upon its execution. The implementation of
 * the callback method <code>prepareCfps</code> must return the Vector of
 * messages to be sent (eventually a single message with multiple receivers).
 * <p>
 * The responders can then reply by sending a <code>PROPOSE</code> message
 * including the preconditions that they set out for the action, for instance
 * the price or the time.
 * Alternatively, responders may send a <code>REFUSE</code>, to refuse
 * the proposal or, eventually, a <code>NOT-UNDERSTOOD</code> to communicate
 * communication problems.
 * This first category of reply messages has been here identified as a
 * responses and can be handled via the <code>handleAllResponses()</code>
 * callback method.
 * Specific handle callback methods for each type of communicative act are also
 * available when the programmer wishes to handle them separately:
 * <code>handlePropose(), handleRefuse(), handleNotUnderstood()</code>.
 * <p>
 * The initiator can evaluate all the received proposals
 * and make its choice of which agent proposals will be accepted and
 * which will be rejected.
 * This class provides two ways for this evaluation. It can be done
 * progressively each time a new <code>PROPOSE</code> message is
 * received and a new call to the <code>handlePropose()</code> callback
 * method is executed
 * or,
 * in alternative, it can be done just once when all the <code>PROPOSE</code>
 * messages have been collected (or the <code>reply-by</code> deadline has
 * expired) and a single call to the
 * <code>handleAllResponses()</code> callback method is executed.
 * In both cases, the second parameter of the method, i.e. the Vector
 * <code>acceptances</code>, must be filled with the appropriate
 * <code>ACCEPT/REJECT-PROPOSAL</code> messages.
 * Notice that, for the first case, the method <code>skipNextResponses()</code>
 * has been provided that, if called by the programmer
 * when waiting for <code>PROPOSE</code>
 * messages, allows to skip to the next state and ignore all the
 * responses and proposals that have not yet been received.
 * <p>
 * Once the responders whose proposal has been accepted (i.e. those that have
 * received a <code>ACCEPT-PROPOSAL</code> message) have completed
 * the task, they can, finally,
 * respond with an
 * <code>INFORM</code> of the result of the action (eventually just that the
 * action has been done) or with a <code>FAILURE</code> if anything went wrong.
 * This second category of reply messages has been here identified as a
 * result notifications and can be handled via the
 * <code>handleAllResultNotifications()</code> callback method.
 * Again, specific handle callback
 * methods for each type of communicative act are also
 * available when the programmer wishes to handle them separately:
 * <code>handleInform(), handleFailure()</code>.
 * <p>
 * If a message were received, with the same value of this
 * <code>conversation-id</code>, but that does not comply with the FIPA
 * protocol, than the method <code>handleOutOfSequence()</code> would be called.
 * <p>
 * This class can be extended by the programmer by overriding all the needed
 * handle methods or, in alternative, appropriate behaviours can be
 * registered for each handle via the <code>registerHandle</code>-type
 * of methods. This last case is more difficult to use and proper
 * care must be taken to properly use the <code>HashMap</code> of the
 * <code>Behaviour</code> as a shared memory mechanism with the
 * registered behaviour.
 * <p>
 * When needed this class can also be used to play the initiator role in an
 * Iterated-Fipa-Contract-Net protocol. To activate a new CFP-PROPOSE iteration it is
 * sufficient to invoke the <code>newIteration()</code> method from within the
 * <code>handleAllResponses()</code> method.
 *
 * @author Giovanni Caire - TILab
 * @author Fabio Bellifemine - TILab
 * @author Tiziana Trucco - TILab
 * @author Marco Monticone - TILab
 * @version $Date: 2016-03-28 17:07:43 +0200 (lun, 28 mar 2016) $ $Revision: 6795 $
 * @see AchieveREInitiator
 * @see <a href=http://www.fipa.org/specs/fipa00029/XC00029F.html>FIPA Spec</a>
 * @since JADE2.5
 * see ContractNetResponder
 **/
public class ContractNetInitiator extends Initiator {

    // Private data store keys (can't be static since if we register another instance of this class as state of the FSM
    // using the same data store the new values overrides the old one.
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object passed in the constructor of the class.
     **/
    public final String CFP_KEY = INITIATION_K;
    /**
     * key to retrieve from the HashMap of the behaviour the vector of
     * CFP ACLMessage objects that have to be sent.
     **/
    public final String ALL_CFPS_KEY = ALL_INITIATIONS_K;
    /**
     * key to retrieve from the HashMap of the behaviour the vector of
     * ACCEPT/REJECT_PROPOSAL ACLMessage objects that have to be sent
     **/
    public final String ALL_ACCEPTANCES_KEY = "__all-acceptances" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the last
     * ACLMessage object that has been received (null if the timeout
     * expired).
     **/
    public final String REPLY_KEY = REPLY_K;
    /**
     * key to retrieve from the HashMap of the behaviour the vector of
     * ACLMessage objects that have been received as response.
     **/
    public final String ALL_RESPONSES_KEY = "__all-responses" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the vector of
     * ACLMessage objects that have been received as result notifications.
     **/
    public final String ALL_RESULT_NOTIFICATIONS_KEY = "__all-result-notifications" + hashCode();

    // FSM states names
    private static final String HANDLE_PROPOSE = "Handle-propose";
    private static final String HANDLE_REFUSE = "Handle-refuse";
    private static final String HANDLE_INFORM = "Handle-inform";
    private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";
    private static final String HANDLE_ALL_RESULT_NOTIFICATIONS = "Handle-all-result-notifications";

    // States exit values
    private static final int ALL_RESPONSES_RECEIVED = 1;
    private static final int ALL_RESULT_NOTIFICATIONS_RECEIVED = 2;
    private static final int MORE_ACCEPTANCES = 3;

    // When step == 1 we deal with CFP and responses
    // When step == 2 we deal with ACCEPT/REJECT_PROPOSAL and result notifications
    private int step = 1;
    // If set to true all responses not yet received are skipped
    private boolean skipNextRespFlag = false;
    // Indicates that a new tranche of acceptances must be sent after all result notifications
    // have been received
    private boolean moreAcceptancesToSend = false;

    /**
     * Constructor for the class that creates a new empty HashMap
     *
     * @see #ContractNetInitiator(Agent, ACLMessage, HashMap)
     **/
    public ContractNetInitiator(Agent a, ACLMessage cfp) {
        this(a, cfp, new HashMap<>(), new HashMap<>());
    }

    /**
     * Constructs a <code>ContractNetInitiator</code> behaviour
     *
     * @param a               The agent performing the protocol
     * @param cfp             The message that must be used to initiate the protocol.
     *                        Notice that the default implementation of the
     *                        <code>prepareCfps</code>
     *                        method returns
     *                        an array composed of that message only.
     * @param mapMessagesList The <code>HashMap</code> that will be used by this
     *                        <code>ContractNetInitiator</code>
     * deprecated

    public ContractNetInitiator(Agent a, ACLMessage cfp, HashMap<String, List<ACLMessage>> mapMessagesList) {
        super(a, cfp, mapMessagesList);
        // Register the FSM transitions specific to the ContractNet protocol
        registerTransition(CHECK_IN_SEQ, HANDLE_PROPOSE, ACLMessage.PROPOSE);
        registerTransition(CHECK_IN_SEQ, HANDLE_REFUSE, ACLMessage.REFUSE);
        registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);
        registerDefaultTransition(HANDLE_PROPOSE, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_REFUSE, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESULT_NOTIFICATIONS, ALL_RESULT_NOTIFICATIONS_RECEIVED);
        registerDefaultTransition(HANDLE_ALL_RESPONSES, SEND_INITIATIONS, getToBeReset());
        registerTransition(HANDLE_ALL_RESULT_NOTIFICATIONS, SEND_INITIATIONS, MORE_ACCEPTANCES, getToBeReset());
        registerDefaultTransition(HANDLE_ALL_RESULT_NOTIFICATIONS, DUMMY_FINAL);

        // Create and register the states specific to the ContractNet protocol
        Behaviour b;
        // HANDLE_PROPOSE
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895819003L;

            public void action() {
                var acceptances = getMapMessagesList().get(ALL_ACCEPTANCES_KEY);
                ACLMessage propose = getMapMessages().get(REPLY_K);
                handlePropose(propose, acceptances);
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, HANDLE_PROPOSE);

        // HANDLE_REFUSE
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895819004L;

            public void action() {
                handleRefuse(getMapMessages().get(REPLY_K));
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, HANDLE_REFUSE);

        // HANDLE_INFORM
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818006L;

            public void action() {
                handleInform(getMapMessages().get(REPLY_K));
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, HANDLE_INFORM);

        // HANDLE_ALL_RESPONSES
        b = new OneShotBehaviour(myAgent) {

            public void action() {
                var responses = getMapMessagesList().get(ALL_RESPONSES_KEY);
                var acceptances = getMapMessagesList().get(ALL_ACCEPTANCES_KEY);
                handleAllResponses(responses, acceptances);
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, HANDLE_ALL_RESPONSES);

        // HANDLE_ALL_RESULT_NOTIFICATIONS
        b = new OneShotBehaviour(myAgent) {

            public void action() {
                handleAllResultNotifications(getMapMessagesList().get(ALL_RESULT_NOTIFICATIONS_KEY));
            }

            public int onEnd() {
                return moreAcceptancesToSend ? MORE_ACCEPTANCES : super.onEnd();
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);
    }
*/
    /**
     * Constructs a <code>ContractNetInitiator</code> behaviour
     *
     * @param a               The agent performing the protocol
     * @param cfp             The message that must be used to initiate the protocol.
     *                        Notice that the default implementation of the
     *                        <code>prepareCfps</code>
     *                        method returns
     *                        an array composed of that message only.
     * @param mapMessagesList The <code>HashMap</code> of messages list that will be used by this
     * @param mapMessages     The <code>HashMap</code> of messages  that will be used by this
     *                        <code>ContractNetInitiator</code>
     */
    public ContractNetInitiator(Agent a, ACLMessage cfp, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a, cfp, mapMessagesList, mapMessages);

        // Register the FSM transitions specific to the ContractNet protocol
        registerTransition(CHECK_IN_SEQ, HANDLE_PROPOSE, ACLMessage.PROPOSE);
        registerTransition(CHECK_IN_SEQ, HANDLE_REFUSE, ACLMessage.REFUSE);
        registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);
        registerDefaultTransition(HANDLE_PROPOSE, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_REFUSE, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESULT_NOTIFICATIONS, ALL_RESULT_NOTIFICATIONS_RECEIVED);
        registerDefaultTransition(HANDLE_ALL_RESPONSES, SEND_INITIATIONS, getToBeReset());
        registerTransition(HANDLE_ALL_RESULT_NOTIFICATIONS, SEND_INITIATIONS, MORE_ACCEPTANCES, getToBeReset());
        registerDefaultTransition(HANDLE_ALL_RESULT_NOTIFICATIONS, DUMMY_FINAL);

        // Create and register the states specific to the ContractNet protocol
        Behaviour b;
        // HANDLE_PROPOSE
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895819003L;

            public void action() {
                var acceptances = getMapMessagesList().get(ALL_ACCEPTANCES_KEY);
                ACLMessage propose = getMapMessages().get(REPLY_K);
                handlePropose(propose, acceptances);
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_PROPOSE);

        // HANDLE_REFUSE
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895819004L;

            public void action() {
                handleRefuse(getMapMessages().get(REPLY_K));
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_REFUSE);

        // HANDLE_INFORM
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818006L;

            public void action() {
                handleInform(getMapMessages().get(REPLY_K));
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_INFORM);

        // HANDLE_ALL_RESPONSES
        b = new OneShotBehaviour(myAgent) {

            public void action() {
                var responses = getMapMessagesList().get(ALL_RESPONSES_KEY);
                var acceptances = getMapMessagesList().get(ALL_ACCEPTANCES_KEY);
                handleAllResponses(responses, acceptances);
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_ALL_RESPONSES);

        // HANDLE_ALL_RESULT_NOTIFICATIONS
        b = new OneShotBehaviour(myAgent) {

            public void action() {
                handleAllResultNotifications(getMapMessagesList().get(ALL_RESULT_NOTIFICATIONS_KEY));
            }

            public int onEnd() {
                return moreAcceptancesToSend ? MORE_ACCEPTANCES : super.onEnd();
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     *
     */
    protected List<ACLMessage> prepareInitiations(ACLMessage initiation) {
        return prepareCfps(initiation);
    }

    /**
     * Create and initialize the Sessions and sends the initiation messages
     */
    protected void sendInitiations(List<ACLMessage> initiations) {
        // By default the initiations parameter points to the Vector of the CFPs.
        // However at step 2 we need to deal with the acceptances
        if (step >= 2) {
            initiations = getMapMessagesList().get(ALL_ACCEPTANCES_KEY);
            if (moreAcceptancesToSend) {
                moreAcceptancesToSend = false;
                getMapMessagesList().put(ALL_RESULT_NOTIFICATIONS_KEY, new ArrayList<>());
            }
        }

        super.sendInitiations(initiations);
    }

    /**
     * Check whether a reply is in-sequence and update the appropriate Session
     */
    protected boolean checkInSequence(ACLMessage reply) {
        boolean ret = false;
        String inReplyTo = reply.getInReplyTo();
        Session s = (Session) mapSessions.get(inReplyTo);
        if (s != null) {
            int perf = reply.getPerformative();
            if (s.update(perf)) {
                // The reply is compliant to the protocol
                var all = getMapMessagesList().get(step == 1 ? ALL_RESPONSES_KEY : ALL_RESULT_NOTIFICATIONS_KEY);
                all.add(reply);
                ret = true;
            }
            if (s.isCompleted()) {
                mapSessions.remove(inReplyTo);
            }
        }
        return ret;
    }

    /**
     * Check the status of the sessions after the reception of the last reply
     * or the expiration of the timeout
     */
    protected int checkSessions(ACLMessage reply) {
        if (skipNextRespFlag) {
            mapSessions.clear();
        }

        int ret = (step == 1 ? ALL_RESPONSES_RECEIVED : ALL_RESULT_NOTIFICATIONS_RECEIVED);
        if (reply != null) {
            if (mapSessions.size() > 0) {
                // If there are still active sessions we haven't received
                // all responses/result_notifications yet
                ret = -1;
            }
        } else {
            // Timeout has expired or we were interrupted --> clear all remaining sessions
            mapSessions.clear();
        }
        if (ret != -1) {
            step++;
        }
        return ret;
    }

    private String[] toBeReset = null;

    /**
     *
     */
    protected String[] getToBeReset() {
        if (toBeReset == null) {
            toBeReset = new String[]{
                    HANDLE_PROPOSE,
                    HANDLE_REFUSE,
                    HANDLE_NOT_UNDERSTOOD,
                    HANDLE_INFORM,
                    HANDLE_FAILURE,
                    HANDLE_OUT_OF_SEQ
            };
        }
        return toBeReset;
    }
    //#APIDOC_EXCLUDE_END

    /**
     * This method must return the vector of ACLMessage objects to be
     * sent. It is called in the first state of this protocol.
     * This default implementation just returns the ACLMessage object (a CFP)
     * passed in the constructor. Programmers might prefer to override
     * this method in order to return a vector of CFP objects for 1:N conversations
     * or also to prepare the messages during the execution of the behaviour.
     *
     * @param cfp the ACLMessage object passed in the constructor
     * @return a Vector of ACLMessage objects. The value of the slot
     * <code>reply-with</code> is ignored and regenerated automatically
     * by this class.
     **/
    protected List<ACLMessage> prepareCfps(ACLMessage cfp) {
        List<ACLMessage> v = new ArrayList<>(1);
        v.add(cfp);
        return v;
    }

    /**
     * This method is called every time a <code>propose</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     *
     * @param propose     the received propose message
     * @param acceptances the list of ACCEPT/REJECT_PROPOSAL to be sent back.
     *                    This list can be filled step by step redefining this method, or
     *                    it can be filled at once
     *                    redefining the handleAllResponses method.
     **/
    protected void handlePropose(ACLMessage propose, List<ACLMessage> acceptances) {
    }

    /**
     * This method is called every time a <code>refuse</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     *
     * @param refuse the received refuse message
     **/
    protected void handleRefuse(ACLMessage refuse) {
    }

    /**
     * This method is called every time a <code>inform</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     *
     * @param inform the received inform message
     **/
    protected void handleInform(ACLMessage inform) {
    }

    /**
     * This method is called when all the responses have been
     * collected or when the timeout is expired.
     * The used timeout is the minimum value of the slot <code>replyBy</code>
     * of all the sent messages.
     * By response message we intend here all the <code>propose, not-understood,
     * refuse</code> received messages, which are not
     * not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event
     * by analysing all the messages in just one call.
     *
     * @param responses   the Vector of ACLMessage objects that have been received
     * @param acceptances the list of ACCEPT/REJECT_PROPOSAL to be sent back.
     *                    This list can be filled at once redefining this method, or step by step
     *                    redefining the handlePropose method.
     **/
    protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> acceptances) {
    }

    /**
     * This method is called when all the result notification messages
     * have been
     * collected.
     * By result notification message we intend here all the <code>inform,
     * failure</code> received messages, which are not
     * not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event
     * by analysing all the messages in just one call.
     *
     * @param resultNotifications the Vector of ACLMessage object received
     **/
    protected void handleAllResultNotifications(List<ACLMessage> resultNotifications) {
    }


    /**
     * This method allows to register a user-defined <code>Behaviour</code>
     * in the PREPARE_CFPS state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * It is responsibility of the registered behaviour to put the
     * Vector of ACLMessage objects to be sent
     * into the HashMap at the <code>ALL_CFPS_KEY</code>
     * key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerPrepareCfps(Behaviour b) {
        registerPrepareInitiations(b);
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_PROPOSE state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve
     * the <code>propose</code> ACLMessage object received
     * from the HashMap at the <code>REPLY_KEY</code>
     * key and the <code>Vector</code> of ACCEPT/REJECT_PROPOSAL to be
     * sent back at the <code>ALL_ACCEPTANCES_KEY</code>
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandlePropose(Behaviour b) {
        registerState(b, HANDLE_PROPOSE);
        b.setMapMessages(getMapMessages());
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_REFUSE state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve
     * the <code>refuse</code> ACLMessage object received
     * from the HashMap at the <code>REPLY_KEY</code>
     * key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleRefuse(Behaviour b) {
        registerState(b, HANDLE_REFUSE);
        b.setMapMessages(getMapMessages());
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_INFORM state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve
     * the <code>inform</code> ACLMessage object received
     * from the HashMap at the <code>REPLY_KEY</code>
     * key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleInform(Behaviour b) {
        registerState(b, HANDLE_INFORM);
        b.setMapMessages(getMapMessages());
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_ALL_RESPONSES state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve
     * the vector of ACLMessage objects, received as a response,
     * from the HashMap at the <code>ALL_RESPONSES_KEY</code>
     * key and the <code>Vector</code> of ACCEPT/REJECT_PROPOSAL to be
     * sent back at the <code>ALL_ACCEPTANCES_KEY</code>
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleAllResponses(Behaviour b) {
        registerState(b, HANDLE_ALL_RESPONSES);
        b.setMapMessages(getMapMessages());
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_ALL_RESULT_NOTIFICATIONS state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve
     * the Vector of ACLMessage objects, received as a result notification,
     * from the HashMap at the <code>ALL_RESULT_NOTIFICATIONS_KEY</code>
     * key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleAllResultNotifications(Behaviour b) {
        registerLastState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);
        b.setMapMessages(getMapMessages());
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * This method can be called (typically within the handlePropose() method)
     * to skip all responses that have not been received yet.
     */
    public void skipNextResponses() {
        skipNextRespFlag = true;
    }

    /**
     * This method can be called (typically within the handleAllResponses() method) to
     * activate a new iteration (this means we are implementing an Iterated-Contract-Net
     * protocol).
     *
     * @param nextMessages The messages to be sent to responders at next iteration. Such messages
     *                     can be CFPs (for responders actually involved in the next iteration) or REJECT_PROPOSALs
     *                     (for responders no longer involved in the next iteration).
     *                     see SSIteratedContractNetResponder
     */
    public void newIteration(List<ACLMessage> nextMessages) {
        reset();
        getMapMessagesList().put(ALL_CFPS_KEY, nextMessages);
    }

    /**
     * This method can be called to send acceptances to responder agents that replied with
     * PROPOSE not all together but in more tranches. This is useful, for instance, to send
     * an ACCEPT_PROPOSAL to the best proposer immediately and the REJECT_PROPOSALs to the
     * other proposers only after the reception of the final INFORM from the best proposer.
     * To achieve that the first tranche of acceptances is managed normally. However by
     * calling the moreAcceptances() method in one of the result notifications handler methods
     * (i.e. handleInform(), handleFailure(), handleAllResultNotifications()) an additional
     * tranche of acceptances is specified and the protocol goes on.
     *
     * @param nextAcceptances An additional tranche of acceptance messages
     */
    public void moreAcceptances(List<ACLMessage> nextAcceptances) {
        if (nextAcceptances != null) {
            getMapMessagesList().put(ALL_ACCEPTANCES_KEY, nextAcceptances);
            moreAcceptancesToSend = true;
        }
    }

    protected void reinit() {
        step = 1;
        skipNextRespFlag = false;
        super.reinit();
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * Initialize the data store.
     **/
    protected void initializeHashMap(ACLMessage msg) {
        super.initializeHashMap(msg);
        var ds = getMapMessagesList();
        List<ACLMessage> l = new ArrayList<>();
        ds.put(ALL_RESPONSES_KEY, l);
        l = new ArrayList<>();
        ds.put(ALL_RESULT_NOTIFICATIONS_KEY, l);
        l = new ArrayList<>();
        ds.put(ALL_ACCEPTANCES_KEY, l);
    }
    //#APIDOC_EXCLUDE_END


    protected ProtocolSession getSession(ACLMessage msg, int sessionIndex) {
        if (msg.getPerformative() == ACLMessage.CFP) {
            return new Session(1);
        } else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
            return new Session(2);
        } else {
            return null;
        }
    }

    /**
     * Inner class Session
     */
    class Session implements ProtocolSession, Serializable {
        // Session states
        static final int INIT = 0;
        static final int REPLY_RECEIVED = 1;

        private int state = INIT;
        private final int sessionStep;

        public Session(int s) {
            this.sessionStep = s;
        }

        public String getId() {
            return null;
        }

        /**
         * Return true if received ACLMessage is consistent with the protocol
         */
        public boolean update(int perf) {
            if (state == INIT) {
                if (sessionStep == 1) {
                    switch (perf) {
                        case ACLMessage.PROPOSE:
                        case ACLMessage.REFUSE:
                        case ACLMessage.NOT_UNDERSTOOD:
                        case ACLMessage.FAILURE:
                            state = REPLY_RECEIVED;
                            return true;
                        default:
                            return false;
                    }
                } else {
                    switch (perf) {
                        case ACLMessage.INFORM:
                        case ACLMessage.NOT_UNDERSTOOD:
                        case ACLMessage.FAILURE:
                            state = REPLY_RECEIVED;
                            return true;
                        default:
                            return false;
                    }
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
    } // End of inner class Session

}


