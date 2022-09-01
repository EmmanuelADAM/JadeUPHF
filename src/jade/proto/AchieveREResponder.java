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

//#CUSTOM_EXCLUDE_FILE

import jade.core.Agent;
import jade.core.CaseInsensitiveString;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import jade.proto.states.ReplySender;

import java.util.HashMap;
import java.util.List;

/**
 * This is a single homogeneous and effective implementation of
 * all the FIPA-Request-like interaction protocols defined by FIPA,
 * that is all those protocols where the initiator sends a single message
 * (i.e. it performs a single communicative act) within the scope
 * of an interaction protocol in order to verify if the RE (Rational
 * Effect) of the communicative act has been achieved or not.
 *
 * @author Giovanni Caire - TILab
 * @author Fabio Bellifemine - TILab
 * @author Tiziana Trucco - TILab
 * @version $Date: 2006-05-25 15:29:42 +0200 (gio, 25 mag 2006) $ $Revision: 5884 $
 * @see AchieveREInitiator
 **/
public class AchieveREResponder extends FSMBehaviour implements FIPANames.InteractionProtocol {

    // FSM states names
    private static final String RECEIVE_REQUEST = "Receive-request";
    private static final String HANDLE_REQUEST = "Handle-request";
    private static final String SEND_RESPONSE = "Send-response";
    private static final String PREPARE_RESULT_NOTIFICATION = "Prepare-result-notification";
    private static final String SEND_RESULT_NOTIFICATION = "Send-result-notification";
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object sent by the initiator.
     **/
    public final String REQUEST_KEY = "__request" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object sent as a response to the initiator.
     **/
    public final String RESPONSE_KEY = "__response" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object sent as a result notification to the initiator.
     **/
    public final String RESULT_NOTIFICATION_KEY = "__result-notification" + hashCode();
    // The MsgReceiver behaviour used to receive request messages
    MsgReceiver rec = null;

    /**
     * Constructor of the behaviour that creates a new empty HashMap
     *
     * @see #AchieveREResponder(Agent a, MessageTemplate mt, HashMap mapmessageslist, HashMap mapmessages)
     **/
    public AchieveREResponder(Agent a, MessageTemplate mt) {
        this(a, mt, new HashMap<>(), new HashMap<>());
    }


    // Inner classes for the FSM states

    /**
     * Constructor.
     *
     * @param a               is the reference to the Agent object
     * @param mt              is the MessageTemplate that must be used to match
     *                        the initiator message. Take care that
     *                        if mt is null every message is consumed by this protocol.
     * @param mapMessagesList the HashMap of messages list for this protocol
     * @param mapMessages     the HashMap of messages  for this protocol
     **/
    public AchieveREResponder(Agent a, MessageTemplate mt, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {

        setMapMessagesList(mapMessagesList);
        setMapMessages(mapMessages);

        // Register the FSM transitions
        registerDefaultTransition(RECEIVE_REQUEST, HANDLE_REQUEST);
        registerDefaultTransition(HANDLE_REQUEST, SEND_RESPONSE);
        registerTransition(SEND_RESPONSE, PREPARE_RESULT_NOTIFICATION, ACLMessage.AGREE);
        registerTransition(SEND_RESPONSE, PREPARE_RESULT_NOTIFICATION, ReplySender.NO_REPLY_SENT);
        registerDefaultTransition(SEND_RESPONSE, RECEIVE_REQUEST);
        registerDefaultTransition(PREPARE_RESULT_NOTIFICATION, SEND_RESULT_NOTIFICATION);
        registerDefaultTransition(SEND_RESULT_NOTIFICATION, RECEIVE_REQUEST);

        // Create and register the states that make up the FSM
        Behaviour b;

        // RECEIVE_REQUEST
        rec = new MsgReceiver(myAgent, mt, -1, mapMessagesList, mapMessages, REQUEST_KEY);
        registerFirstState(rec, RECEIVE_REQUEST);

        // HANDLE_REQUEST
        b = new HandleRequest(myAgent);
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, HANDLE_REQUEST);

        // SEND_RESPONSE
        b = new SendResponse(myAgent, RESPONSE_KEY, REQUEST_KEY, mapMessagesList, mapMessages);
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, SEND_RESPONSE);

        // PREPARE_RESULT_NOTIFICATION
        b = new PrepareResult(myAgent);
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, PREPARE_RESULT_NOTIFICATION);

        // SEND_RESULT_NOTIFICATION
        b = new SendResult(myAgent, RESULT_NOTIFICATION_KEY, REQUEST_KEY, mapMessagesList, mapMessages);
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, SEND_RESULT_NOTIFICATION);
    }


    // For persistence service
    private AchieveREResponder() {
    }

    /**
     * This static method can be used
     * to set the proper message Template (based on the interaction protocol
     * and the performative)
     * into the constructor of this behaviour.
     *
     * @see FIPANames.InteractionProtocol
     **/
    public static MessageTemplate createMessageTemplate(String iprotocol) {

        if (CaseInsensitiveString.equalsIgnoreCase(FIPA_REQUEST, iprotocol))
            return MessageTemplate.and(MessageTemplate.MatchProtocol(FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        else if (CaseInsensitiveString.equalsIgnoreCase(FIPA_QUERY, iprotocol))
            return MessageTemplate.and(MessageTemplate.MatchProtocol(FIPA_QUERY), MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF), MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)));
        else
            return MessageTemplate.MatchProtocol(iprotocol);
    }

    /**
     * Reset this behaviour using the same MessageTemplate.
     */
    public void reset() {
        super.reset();
        var ds = getMapMessages();
        ds.remove(REQUEST_KEY);
        ds.remove(RESPONSE_KEY);
        ds.remove(RESULT_NOTIFICATION_KEY);
    }

    /**
     * This method allows to change the  MessageTemplate  
     * that defines what messages this FIPARequestResponder will react to and reset the protocol.
     */
    public void reset(MessageTemplate mt) {
        this.reset();
        rec.reset(mt, -1, getMapMessagesList(), getMapMessages(), REQUEST_KEY);
    }

    /**
     * Constructor.
     *
     * @param a               is the reference to the Agent object
     * @param mt              is the MessageTemplate that must be used to match
     *                        the initiator message. Take care that
     *                        if mt is null every message is consumed by this protocol.
     * @param mapMessagesList the HashMap of messages list for this protocol
     * deprecated use the constructor with 2 hashmap

    public AchieveREResponder(Agent a, MessageTemplate mt, HashMap<String, List<ACLMessage>> mapMessagesList) {
    super(a);

    setMapMessagesList(mapMessagesList);

    // Register the FSM transitions
    registerDefaultTransition(RECEIVE_REQUEST, HANDLE_REQUEST);
    registerDefaultTransition(HANDLE_REQUEST, SEND_RESPONSE);
    registerTransition(SEND_RESPONSE, PREPARE_RESULT_NOTIFICATION, ACLMessage.AGREE);
    registerTransition(SEND_RESPONSE, PREPARE_RESULT_NOTIFICATION, ReplySender.NO_REPLY_SENT);
    registerDefaultTransition(SEND_RESPONSE, RECEIVE_REQUEST);
    registerDefaultTransition(PREPARE_RESULT_NOTIFICATION, SEND_RESULT_NOTIFICATION);
    registerDefaultTransition(SEND_RESULT_NOTIFICATION, RECEIVE_REQUEST);

    // Create and register the states that make up the FSM
    Behaviour b;

    // RECEIVE_REQUEST
    rec = new MsgReceiver(myAgent, mt, -1, getMapMessagesList(), getMapMessages(), REQUEST_KEY);
    registerFirstState(rec, RECEIVE_REQUEST);

    // HANDLE_REQUEST
    b = new HandleRequest(myAgent);
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, HANDLE_REQUEST);

    // SEND_RESPONSE
    b = new SendResponse(myAgent, RESPONSE_KEY, REQUEST_KEY);
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, SEND_RESPONSE);

    // PREPARE_RESULT_NOTIFICATION
    b = new PrepareResult(myAgent);
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, PREPARE_RESULT_NOTIFICATION);

    // SEND_RESULT_NOTIFICATION
    b = new SendResult(myAgent, RESULT_NOTIFICATION_KEY, REQUEST_KEY);
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, SEND_RESULT_NOTIFICATION);
    }
     */

    /**
     * This method is called when the protocol initiation message (matching the
     * MessageTemplate specified in the constructor) is received.
     * This default implementation returns null which has
     * the effect of sending no reponse. Programmers should
     * override this method in case they need to react to this event.
     *
     * @param request the received message
     * @return the ACLMessage to be sent as a response (i.e. one of
     *  AGREE, REFUSE, NOT_UNDERSTOOD, INFORM  .
     **/
    protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
        // Call prepareResponse() for backward compatibility
        return prepareResponse(request);
    }

    /**
     * @deprecated Use handleRequest() instead
     */
    protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
        System.out.println("prepareResponse() method not re-defined");
        return null;
    }

    /**
     * This method is called after the execution of the handleRequest() method if
     * no response was sent or the response was an  AGREE   message.
     * This default implementation returns null which has
     * the effect of sending no result notification. Programmers should
     * override the method in case they need to react to this event.
     *
     * @param request  the received message
     * @param response the previously sent response message
     * @return the ACLMessage to be sent as a result notification (i.e. one of
     *  INFORM, FAILURE  .
     * @see #handleRequest(ACLMessage)
     **/
    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
        System.out.println("prepareResultNotification() method not re-defined");
        return null;
    }

    /**
     * This method allows to register a user defined  Behaviour  
     * in the HANDLE_REQUEST state.
     * This behaviour would override the homonymous method.
     * This method also set the HashMap of the registered  Behaviour   to the
     * HashMap of this AchieveREResponder.
     * It is responsibility of the registered behaviour to put the
     * response to be sent into the HashMap at the  RESPONSE_KEY  
     * key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleRequest(Behaviour b) {
        registerState(b, HANDLE_REQUEST);
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
    }

    /**
     * @deprecated Use registerHandleRequest() instead.
     */
    public void registerPrepareResponse(Behaviour b) {
        registerHandleRequest(b);
    }

    /**
     * This method allows to register a user defined  Behaviour  
     * in the PREPARE_RESULT_NOTIFICATION state.
     * This behaviour would override the homonymous method.
     * This method also set the HashMap of the registered  Behaviour   to the
     * HashMap of this AchieveREResponder.
     * It is responsibility of the registered behaviour to put the
     * result notification message to be sent into the HashMap at the
     *  RESULT_NOTIFICATION_KEY  
     * key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerPrepareResultNotification(Behaviour b) {
        registerState(b, PREPARE_RESULT_NOTIFICATION);
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
    }

    private static class HandleRequest extends OneShotBehaviour {

        public HandleRequest(Agent a) {
            super(a);
        }

        public void action() {
            var ds = getMapMessages();
            AchieveREResponder fsm = (AchieveREResponder) getParent();
            ACLMessage request = ds.get(fsm.REQUEST_KEY);

            ACLMessage response;
            try {
                response = fsm.handleRequest(request);
            } catch (NotUnderstoodException | RefuseException nue) {
                response = nue.getACLMessage();
            }
            ds.put(fsm.RESPONSE_KEY, response);
        }
    } // End of HandleRequest class

    private static class SendResponse extends ReplySender {

        public SendResponse(Agent a, String replyKey, String msgKey, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
            super(a, replyKey, msgKey, mapMessagesList, mapMessages);
        }

        // For persistence service
        private SendResponse() {
        }

        public int onEnd() {
            int ret = super.onEnd();
            if (ret != ACLMessage.AGREE && ret != ReplySender.NO_REPLY_SENT) {
                AchieveREResponder fsm = (AchieveREResponder) getParent();
                fsm.reset();
            }
            return ret;
        }

    } // End of SendResponse class

    private static class PrepareResult extends OneShotBehaviour {

        public PrepareResult(Agent a) {
            super(a);
        }

        // For persistence service
        private PrepareResult() {
        }

        public void action() {
            var ds = getMapMessages();
            AchieveREResponder fsm = (AchieveREResponder) getParent();
            ACLMessage request = ds.get(fsm.REQUEST_KEY);
            ACLMessage response = ds.get(fsm.RESPONSE_KEY);
            ACLMessage resNotification;
            try {
                resNotification = fsm.prepareResultNotification(request, response);
            } catch (FailureException fe) {
                resNotification = fe.getACLMessage();
            }
            ds.put(fsm.RESULT_NOTIFICATION_KEY, resNotification);
        }

    } // End of PrepareResult class

    private static class SendResult extends ReplySender {

        public SendResult(Agent a, String replyKey, String msgKey, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
            super(a, replyKey, msgKey, mapMessagesList, mapMessages);
        }

        // For persistence service
        private SendResult() {
        }

        public int onEnd() {
            AchieveREResponder fsm = (AchieveREResponder) getParent();
            fsm.reset();
            return super.onEnd();
        }

    } // End of SendResult class

}



