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
import jade.proto.states.MsgReceiver;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * This is a single homogeneous and effective implementation of
 * all the FIPA-Request-like interaction protocols defined by FIPA,
 * that is all those protocols where the initiator sends a single message
 * (i.e. it performs a single communicative act) within the scope
 * of an interaction protocol in order to verify if the RE (Rational
 * Effect) of the communicative act has been achieved or not.
 * This implementation works both for 1:1 and 1:N conversation.
 * <p>
 * FIPA has already specified a number of these interaction protocols, like
 * FIPA-Request, FIPA-query, FIPA-Request-When, FIPA-recruiting,
 * FIPA-brokering, that allows the initiator to verify if the
 * expected rational effect of a single communicative act has been achieved.
 * <p>
 * The structure of these protocols is equal.
 * The initiator sends a message (in general it performs a communicative act).
 * <p>
 * The responder can then reply by sending a <code>not-understood</code>, or a
 * <code>refuse</code> to
 * achieve the rational effect of the communicative act, or also
 * an <code>agree</code> message to communicate the agreement to perform
 * (possibly in the future) the communicative act.  This first category
 * of reply messages has been here identified as a response.
 * <p> The responder performs the action and, finally, must respond with an
 * <code>inform</code> of the result of the action (eventually just that the
 * action has been done) or with a <code>failure</code> if anything went wrong.
 * This second category of reply messages has been here identified as a
 * result notification.
 * <p> Notice that we have extended the protocol to make optional the
 * transmission of the agree message. Infact, in most cases performing the
 * action takes so short time that sending the agree message is just an
 * useless and uneffective overhead; in such cases, the agree to perform the
 * communicative act is subsumed by the reception of the following message in
 * the protocol.
 * <p>
 * Read carefully the section of the
 * <a href="..\..\..\programmersguide.pdf"> JADE programmer's guide </a>
 * that describes
 * the usage of this class.
 * <p> <b>Known bugs:</b>
 * <i> The handler <code>handleAllResponses</code> is not called if the <code>
 * agree</code> message is skipped and the <code>inform</code> message
 * is received instead.
 * <br> One message for every receiver is sent instead of a single
 * message for all the receivers. </i>
 *
 * @author Giovanni Caire - TILab
 * @author Fabio Bellifemine - TILab
 * @author Tiziana Trucco - TILab
 * @version $Date: 2005-12-01 14:09:42 +0100 (gio, 01 dic 2005) $ $Revision: 5839 $
 **/
public class AchieveREInitiator extends Initiator {

    // Private data store keys (can't be static since if we register another instance of this class as state of the FSM
    // using the same data store the new values overrides the old one.
    // FSM states names specific to the Achieve-RE protocol
    protected static final String HANDLE_AGREE = "Handle-agree";
    protected static final String HANDLE_REFUSE = "Handle-refuse";
    protected static final String HANDLE_INFORM = "Handle-inform";
    protected static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";
    protected static final String HANDLE_ALL_RESULT_NOTIFICATIONS = "Handle-all-result-notifications";
    protected static final String CHECK_AGAIN = "Check-again";
    // States exit values
    private static final int ALL_RESPONSES_RECEIVED = 1;
    private static final int ALL_RESULT_NOTIFICATIONS_RECEIVED = 2;
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object passed in the constructor of the class.
     **/
    public final String REQUEST_KEY = INITIATION_K;
    /**
     * key to retrieve from the HashMap of the behaviour the vector of
     * ACLMessage objects that have been sent.
     **/
    public final String ALL_REQUESTS_KEY = ALL_INITIATIONS_K;
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
    // If set to true all expected responses have been received
    private boolean allResponsesReceived = false;
    private String[] toBeReset = null;

    /**
     * Construct an <code>AchieveREInitiator</code> with an empty HashMap
     * see #AchieveREInitiator(Agent, ACLMessage, HashMap)
     **/
    public AchieveREInitiator(Agent a, ACLMessage msg) {
        this(a, msg, new HashMap<>(), new HashMap<>());
    }

    /**
     * Construct an <code>AchieveREInitiator</code> with a given HashMap
     *
     * @param a     The agent performing the protocol
     * @param msg   The message that must be used to initiate the protocol.
     *              Notice that the default implementation of the
     *              <code>prepareRequest()</code>
     *              method returns
     *              an array composed of only this message.
     *              The values of the slot
     *              <code>reply-with</code> is ignored and a different value is assigned
     *              automatically by this class for each receiver.
     * @param store The <code>HashMap</code> that will be used by this
     *              <code>AchieveREInitiator</code>
     * @deprecated
     */
    public AchieveREInitiator(Agent a, ACLMessage msg, HashMap<String, List<ACLMessage>> store) {
        super(a, msg, store);

        // Register the FSM transitions specific to the Achieve-RE protocol
        registerTransition(CHECK_IN_SEQ, HANDLE_AGREE, ACLMessage.AGREE);
        registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);
        registerTransition(CHECK_IN_SEQ, HANDLE_REFUSE, ACLMessage.REFUSE);
        registerDefaultTransition(HANDLE_AGREE, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_REFUSE, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESULT_NOTIFICATIONS, ALL_RESULT_NOTIFICATIONS_RECEIVED);
        registerDefaultTransition(HANDLE_ALL_RESPONSES, CHECK_AGAIN);
        registerTransition(CHECK_AGAIN, HANDLE_ALL_RESULT_NOTIFICATIONS, 0);
        registerDefaultTransition(CHECK_AGAIN, RECEIVE_REPLY, toBeReset);

        // Create and register the states specific to the Achieve-RE protocol
        Behaviour b;
        // HANDLE_AGREE
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818003L;

            public void action() {
                handleAgree(getMapMessages().get(REPLY_K));
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, HANDLE_AGREE);

        // HANDLE_REFUSE
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818004L;

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
                handleAllResponses(getMapMessagesList().get(ALL_RESPONSES_KEY));
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
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerLastState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);

        // CHECK_AGAIN
        b = new OneShotBehaviour(myAgent) {
            public void action() {
            }

            public int onEnd() {
                return mapSessions.size();
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, CHECK_AGAIN);
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * Construct an <code>AchieveREInitiator</code> with a given HashMap
     *
     * @param a               The agent performing the protocol
     * @param msg             The message that must be used to initiate the protocol.
     *                        Notice that the default implementation of the
     *                        <code>prepareRequest()</code>
     *                        method returns
     *                        an array composed of only this message.
     *                        The values of the slot
     *                        <code>reply-with</code> is ignored and a different value is assigned
     *                        automatically by this class for each receiver.
     * @param mapMessagesList The <code>HashMap</code> that will be used by this <code>AchieveREInitiator</code> to store messages list
     * @param mapMessages     The <code>HashMap</code> that will be used by this <code>AchieveREInitiator</code> to store messages
     * @deprecated
     */
    public AchieveREInitiator(Agent a, ACLMessage msg, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a, msg, mapMessagesList, mapMessages);

        // Register the FSM transitions specific to the Achieve-RE protocol
        registerTransition(CHECK_IN_SEQ, HANDLE_AGREE, ACLMessage.AGREE);
        registerTransition(CHECK_IN_SEQ, HANDLE_INFORM, ACLMessage.INFORM);
        registerTransition(CHECK_IN_SEQ, HANDLE_REFUSE, ACLMessage.REFUSE);
        registerDefaultTransition(HANDLE_AGREE, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_INFORM, CHECK_SESSIONS);
        registerDefaultTransition(HANDLE_REFUSE, CHECK_SESSIONS);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES, ALL_RESPONSES_RECEIVED);
        registerTransition(CHECK_SESSIONS, HANDLE_ALL_RESULT_NOTIFICATIONS, ALL_RESULT_NOTIFICATIONS_RECEIVED);
        registerDefaultTransition(HANDLE_ALL_RESPONSES, CHECK_AGAIN);
        registerTransition(CHECK_AGAIN, HANDLE_ALL_RESULT_NOTIFICATIONS, 0);
        registerDefaultTransition(CHECK_AGAIN, RECEIVE_REPLY, toBeReset);

        // Create and register the states specific to the Achieve-RE protocol
        Behaviour b;
        // HANDLE_AGREE
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818003L;

            public void action() {
                handleAgree(getMapMessages().get(REPLY_K));
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, HANDLE_AGREE);

        // HANDLE_REFUSE
        b = new OneShotBehaviour(myAgent) {
            @Serial
            private static final long serialVersionUID = 3487495895818004L;

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
                handleAllResponses(getMapMessagesList().get(ALL_RESPONSES_KEY));
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
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerLastState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);

        // CHECK_AGAIN
        b = new OneShotBehaviour(myAgent) {
            public void action() {
            }

            public int onEnd() {
                return mapSessions.size();
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, CHECK_AGAIN);
    }

    /**
     *
     */
    protected List<ACLMessage> prepareInitiations(ACLMessage initiation) {
        return prepareRequests(initiation);
    }

    /**
     * Check whether a reply is in-sequence and update the appropriate Session
     */
    protected boolean checkInSequence(ACLMessage reply) {
        String inReplyTo = reply.getInReplyTo();
        var s = mapSessions.get(inReplyTo);
        if (s != null) {
            int perf = reply.getPerformative();
            if (s.update(perf)) {
                // The reply is compliant to the protocol
                switch (s.getState()) {
                    case Session.POSITIVE_RESPONSE_RECEIVED, Session.NEGATIVE_RESPONSE_RECEIVED -> {
                        // The reply is a response
                        var allRsp = getMapMessagesList().get(ALL_RESPONSES_KEY);
                        allRsp.add(reply);
                    }
                    case Session.RESULT_NOTIFICATION_RECEIVED -> {
                        // The reply is a resultNotification
                        var allNotif = getMapMessagesList().get(ALL_RESULT_NOTIFICATIONS_KEY);
                        allNotif.add(reply);
                    }
                    default -> {
                        // Something went wrong. Return false --> we will go to the HANDLE_OUT_OF_SEQ state
                        return false;
                    }
                }
                // If the session is completed then remove it.
                if (s.isCompleted()) {
                    mapSessions.remove(inReplyTo);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Check the status of the sessions after the reception of the last reply
     * or the expiration of the timeout
     */
    protected int checkSessions(ACLMessage reply) {
        int ret = -1;
        if (getLastExitValue() == MsgReceiver.TIMEOUT_EXPIRED && !allResponsesReceived) {
            // Special case 1: Timeout has expired
            // Remove all the sessions for which no response has been received yet
            var keysToRemove = new ArrayList<String>();
            mapSessions.forEach((k, v) -> {
                if (v.getState() == Session.INIT) keysToRemove.add(k);
            });
            keysToRemove.forEach(k -> mapSessions.remove(k));
        } else if (reply == null) {
            // Special case 2: We were interrupted (or an additional timeout expired)
            // Remove all sessions
            mapSessions.clear();
        }

        if (!allResponsesReceived) {
            // Check whether all responses have been received (this is the
            // case when no active session is still in the INIT state).
            var currentSessions = mapSessions.values();
            allResponsesReceived = currentSessions.stream().noneMatch(s -> s.getState() == Session.INIT);

            if (allResponsesReceived) {
                // Set an infite timeout to the replyReceiver.
                replyReceiver.setDeadline(MsgReceiver.INFINITE);
                ret = ALL_RESPONSES_RECEIVED;
            }
        } else {
            // Check whether all result notifications have been received
            // (this is the case when there are no active sessions).
            if (mapSessions.size() == 0) {
                ret = ALL_RESULT_NOTIFICATIONS_RECEIVED;
            }
        }
        return ret;
    }

    /**
     *
     */
    protected String[] getToBeReset() {
        if (toBeReset == null) {
            toBeReset = new String[]{
                    HANDLE_AGREE,
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
     * This default implementation just returns the ACLMessage object
     * passed in the constructor. Programmers might prefer to override
     * this method in order to return a vector of objects for 1:N conversations
     * or also to prepare the messages during the execution of the behaviour.
     *
     * @param request the ACLMessage object passed in the constructor
     * @return a Vector of ACLMessage objects.
     * The values of the slot
     * <code>reply-with</code> is ignored and a different value is assigned
     * automatically by this class for each receiver.
     **/
    protected List<ACLMessage> prepareRequests(ACLMessage request) {
        List<ACLMessage> l = new ArrayList<>(1);
        l.add(request);
        return l;
    }

    /**
     * This method is called every time an <code>agree</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     *
     * @param agree the received agree message
     **/
    protected void handleAgree(ACLMessage agree) {
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
     * By response message we intend here all the <code>agree, not-understood,
     * refuse</code> received messages, which are not
     * not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event
     * by analysing all the messages in just one call.
     *
     * @param responses the Vector of ACLMessage objects that have been received
     **/
    protected void handleAllResponses(List<ACLMessage> responses) {
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
     * This method allows to register a user defined <code>Behaviour</code>
     * in the PREPARE_REQUESTS state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * It is responsibility of the registered behaviour to put the
     * Vector of ACLMessage objects to be sent
     * into the HashMap at the <code>ALL_REQUESTS_KEY</code>
     * key.
     * The values of the slot
     * <code>reply-with</code> is ignored and a different value is assigned
     * automatically by this class for each receiver.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerPrepareRequests(Behaviour b) {
        registerPrepareInitiations(b);
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_AGREE state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve
     * the <code>agree</code> ACLMessage object received
     * from the HashMap at the <code>REPLY_KEY</code>
     * key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleAgree(Behaviour b) {
        registerState(b, HANDLE_AGREE);
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
     * in the HANDLE_ALL_RESPONSES state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve
     * the vector of ACLMessage objects, received as a response,
     * from the HashMap at the <code>ALL_RESPONSES_KEY</code>
     * key.
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
     * reset this behaviour
     **/
    protected void reinit() {
        allResponsesReceived = false;
        super.reinit();
    }


    //#APIDOC_EXCLUDE_BEGIN

    /**
     * Initialize the data store.
     **/
    protected void initializeHashMap(ACLMessage msg) {
        super.initializeHashMap(msg);
        List<ACLMessage> l = new ArrayList<>();
        getMapMessagesList().put(ALL_RESPONSES_KEY, l);
        l = new ArrayList<>();
        getMapMessagesList().put(ALL_RESULT_NOTIFICATIONS_KEY, l);
    }
    //#APIDOC_EXCLUDE_END

    protected ProtocolSession getSession(ACLMessage msg, int sessionIndex) {
        return new Session();
    }

    /**
     * Inner class Session
     */
    private static class Session implements ProtocolSession, Serializable {
        // Session states
        static final int INIT = 0;
        static final int POSITIVE_RESPONSE_RECEIVED = 1;
        static final int NEGATIVE_RESPONSE_RECEIVED = 2;
        static final int RESULT_NOTIFICATION_RECEIVED = 3;

        private int state = INIT;

        public String getId() {
            return null;
        }

        /**
         * return true if the received performative is valid with respect to
         * the current session state.
         */
        public boolean update(int perf) {
            switch (state) {
                case INIT:
                    switch (perf) {
                        case ACLMessage.AGREE:
                            state = POSITIVE_RESPONSE_RECEIVED;
                            return true;
                        case ACLMessage.REFUSE:
                        case ACLMessage.NOT_UNDERSTOOD:
                            state = NEGATIVE_RESPONSE_RECEIVED;
                            return true;
                        case ACLMessage.INFORM:
                        case ACLMessage.FAILURE:
                            state = RESULT_NOTIFICATION_RECEIVED;
                            return true;
                        default:
                            return false;
                    }
                case POSITIVE_RESPONSE_RECEIVED:
                    switch (perf) {
                        case ACLMessage.INFORM:
                        case ACLMessage.FAILURE:
                            state = RESULT_NOTIFICATION_RECEIVED;
                            return true;
                        default:
                            return false;
                    }
                default:
                    return false;
            }
        }

        public int getState() {
            return state;
        }

        public boolean isCompleted() {
            return (state == NEGATIVE_RESPONSE_RECEIVED || state == RESULT_NOTIFICATION_RECEIVED);
        }

    } // End of inner class Session

}



