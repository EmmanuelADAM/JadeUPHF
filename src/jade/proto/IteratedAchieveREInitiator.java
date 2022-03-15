/*****************************************************************
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class implements the initiator role in the iterated version of
 * fipa-request like interaction protocols. In the iterated version, having
 * received all the result notifications from the responders, the initiator
 * may send further initiation messages.
 * The session of such a protocol with a given responder terminates when
 * one of the followings occurs:
 * i) The initiator sends an explicit CANCEL message instead of the next
 * initiation message to the responder.
 * ii) The responder replies with a negative reply i.e. REFUSE, NOT_UNDERSTOOD
 * or FAILURE
 * ii) The responder attaches a termination flag to an INFORM result notification.
 * That termination flag can be detected using the
 * <code>isSessionTerminated()</code> method.
 *
 * @author Giovanni Caire - TILab
 */
public class IteratedAchieveREInitiator extends AchieveREInitiator {
    public static final String REINIT = "Reinit";
    /**
     * Key to retrieve from the HashMap of the behaviour the vector of
     * ACLMessage objects that will be sent at next round.
     */
    public final String ALL_NEXT_REQUESTS_KEY = "__all-next-requests" + hashCode();

    /**
     * Construct an <code>IteratedAchieveREInitiator</code> with an empty HashMap
     */
    public IteratedAchieveREInitiator(Agent a, ACLMessage msg) {
        this(a, msg, new HashMap<>(), new HashMap<>());
    }

    /**
     * Construct an <code>IteratedAchieveREInitiator</code> with a given HashMap
     *
     * @param a               The agent performing the protocol
     * @param msg             The message that must be used to initiate the protocol.
     *                        Notice that the default implementation of the
     *                        <code>prepareRequest()</code> method returns an array composed of
     *                        only this message. The values of the slot
     *                        <code>reply-with</code> is ignored and a different value is assigned
     *                        automatically by this class for each receiver.
     * @param mapMessagesList The <code>HashMap</code> that will be used by this <code>AchieveREInitiator</code>
     * deprecated use the constructor with 2 hashmap

    public IteratedAchieveREInitiator(Agent a, ACLMessage msg, HashMap<String, List<ACLMessage>> mapMessagesList) {
    super(a, msg, mapMessagesList);

    // The HANDLE_ALL_RESULT_NOTIFICATIONS state must no longer be final
    Behaviour b = deregisterState(HANDLE_ALL_RESULT_NOTIFICATIONS);
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);

    // REINIT
    b = new OneShotBehaviour(myAgent) {
    public void action() {
    prepareForNextRound();
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, REINIT);

    // Register the FSM transitions specific to the Iterated-Achieve-RE protocol
    registerDefaultTransition(HANDLE_ALL_RESULT_NOTIFICATIONS, REINIT);
    registerDefaultTransition(REINIT, SEND_INITIATIONS);
    }*/

    /**
     * Construct an <code>IteratedAchieveREInitiator</code> with a given HashMap
     *
     * @param a               The agent performing the protocol
     * @param msg             The message that must be used to initiate the protocol.
     *                        Notice that the default implementation of the
     *                        <code>prepareRequest()</code> method returns an array composed of
     *                        only this message. The values of the slot
     *                        <code>reply-with</code> is ignored and a different value is assigned
     *                        automatically by this class for each receiver.
     * @param mapMessagesList The <code>HashMap</code> of messages list that will be used by this <code>AchieveREInitiator</code>
     * @param mapMessages     The <code>HashMap</code> of messages that will be used by this <code>AchieveREInitiator</code>
     */
    public IteratedAchieveREInitiator(Agent a, ACLMessage msg, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a, msg, mapMessagesList, mapMessages);

        // The HANDLE_ALL_RESULT_NOTIFICATIONS state must no longer be final
        Behaviour b = deregisterState(HANDLE_ALL_RESULT_NOTIFICATIONS);
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);

        // REINIT
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                prepareForNextRound();
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, REINIT);

        // Register the FSM transitions specific to the Iterated-Achieve-RE protocol
        registerDefaultTransition(HANDLE_ALL_RESULT_NOTIFICATIONS, REINIT);
        registerDefaultTransition(REINIT, SEND_INITIATIONS);
    }

    /**
     * Check if the responder has closed the session just after sending this <code>inform</code> message.
     */
    public static boolean isSessionTerminated(ACLMessage inform) {
        String terminatedStr = inform.getUserDefinedParameter(SSIteratedAchieveREResponder.ACL_USERDEF_TERMINATED_SESSION);
        return "true".equals(terminatedStr);
    }

    //#APIDOC_EXCLUDE_BEGIN
    protected void prepareForNextRound() {
        // Reset local variables, clean data store, reset children and copy the
        // "next-requests" of previous round to the "requests" of the next round.
        var v = getMapMessagesList().get(ALL_NEXT_REQUESTS_KEY);
        reinit();
        resetChildren();
        initializeHashMap(null);
        getMapMessagesList().put(ALL_REQUESTS_KEY, v);
    }

    protected void initializeHashMap(ACLMessage msg) {
        super.initializeHashMap(msg);
        List<ACLMessage> v = new ArrayList<>();
        getMapMessagesList().put(ALL_NEXT_REQUESTS_KEY, v);
    }
    //#APIDOC_EXCLUDE_END

    protected ProtocolSession getSession(ACLMessage msg, int sessionIndex) {
        if (msg.getPerformative() == ACLMessage.CANCEL) {
            return null;
        } else {
            return super.getSession(msg, sessionIndex);
        }
    }

    /**
     * This method is called every time an <code>inform</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override this method in case they need to react to this event.
     *
     * @param inform       the received inform message
     * @param nextRequests the Vector of ACLMessage objects to be sent at
     *                     next round
     */
    protected void handleInform(ACLMessage inform, List<ACLMessage> nextRequests) {
    }

    /**
     * This method is redefined to call the proper overloaded method
     */
    protected final void handleInform(ACLMessage inform) {
        var v = getMapMessagesList().get(ALL_NEXT_REQUESTS_KEY);
        handleInform(inform, v);
    }

    /**
     * This method is called when all the result notification messages
     * of the current round have been collected.
     * By result notification message we intend here all the <code>inform,
     * failure</code> received messages, which are not out-of-sequence
     * according to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override this method in case they need to react to this event
     * by analysing all the messages in just one call.
     *
     * @param resultNotifications the Vector of ACLMessage object received
     * @param nextRequests        the Vector of ACLMessage objects to be sent at
     *                            next round
     */
    protected void handleAllResultNotifications(List<ACLMessage> resultNotifications, List<ACLMessage> nextRequests) {
    }

    /**
     * This method is redefined to call the proper overloaded method
     */
    protected final void handleAllResultNotifications(List<ACLMessage> resultNotifications) {
        var v = getMapMessagesList().get(ALL_NEXT_REQUESTS_KEY);
        handleAllResultNotifications(resultNotifications, v);
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_REFUSE state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve the received <code>inform</code>
     * ACLMessage object from the HashMap at the <code>REPLY_KEY</code>
     * key and the Vector of ACLMessage objects to be sent at next round
     * at the <code>ALL_NEXT_REQUESTS_KEY</code>.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleInform(Behaviour b) {
        // This is redefined for Javadoc purposes only.
        super.registerHandleInform(b);
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
     * key and the Vector of ACLMessage objects to be sent at next round
     * at the <code>ALL_NEXT_REQUESTS_KEY</code>.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleAllResultNotifications(Behaviour b) {
        // Method redefined since HANDLE_ALL_RESULT_NOTIFICATION must not be
        // registered as a final state
        registerState(b, HANDLE_ALL_RESULT_NOTIFICATIONS);
        b.setMapMessagesList(getMapMessagesList());
    }
}



