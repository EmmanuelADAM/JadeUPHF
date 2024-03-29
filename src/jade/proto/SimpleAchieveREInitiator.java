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

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

import java.util.*;

/**
 * This is  simple implementation of the AchieveREInitiator.
 * This implementation in particular is 1:1 and does not allow
 * the possibility to add Handler.
 * <p>
 * This is a single homogeneous and effective implementation of
 * all the FIPA-Request-like interaction protocols defined by FIPA,
 * that is all those protocols where the initiator sends a single message
 * (i.e. it performs a single communicative act) within the scope
 * of an interaction protocol in order to verify if the RE (Rational
 * Effect) of the communicative act has been achieved or not.
 *
 * <p>
 * FIPA has already specified a number of these interaction protocols, like
 * FIPA-Request, FIPA-query, FIPA-Request-When, FIPA-recruiting,
 * FIPA-brokering, FIPA-subscribe, that allows the initiator to verify if the
 * expected rational effect of a single communicative act has been achieved.
 * <p>
 * The structure of these protocols is equal.
 * The initiator sends a message (in general it performs a communicative act).
 * <p>
 * The responder can then reply by sending a  not-understood  , or a
 *  refuse   to
 * achieve the rational effect of the communicative act, or also
 * an  agree   message to communicate the agreement to perform
 * (possibly in the future) the communicative act.  This first category
 * of reply messages has been here identified as a response.
 * <p> The responder performs the action and, finally, must respond with an
 *  inform   of the result of the action (eventually just that the
 * action has been done) or with a  failure   if anything went wrong.
 * This second category of reply messages has been here identified as a
 * result notification.
 * <p> Notice that we have extended the protocol to make optional the
 * transmission of the agree message. Infact, in most cases performing the
 * action takes so short time that sending the agree message is just an
 * useless and uneffective overhead; in such cases, the agree to perform the
 * communicative act is subsumed by the reception of the following message in
 * the protocol.
 * <p>
 * <p> <b>Known bugs:</b>
 * <i> The handler  handleAllResponses   is not called if the
 * agree   message is skipped and the  inform   message
 * is received instead.</i>
 * <p>
 * see SimpleAchieveREResponder
 *
 * @author Tiziana Trucco - TILab
 * @version $Date: 2005-09-16 15:54:46 +0200 (ven, 16 set 2005) $ $Revision: 5780 $
 * @see AchieveREInitiator
 * @see AchieveREResponder
 **/


public class SimpleAchieveREInitiator extends SimpleBehaviour {

    private final static int PREPARE_MSG_STATE = 0;
    private final static int SEND_MSG_STATE = 1;
    private final static int RECEIVE_REPLY_STATE = 2;
    private final static int RECEIVE_2ND_REPLY_STATE = 3;
    private final static int ALL_REPLIES_RECEIVED_STATE = 4;
    private final static int ALL_RESULT_NOTIFICATION_RECEIVED_STATE = 5;


    /**
     * key to retrive from the HashMap the ACLMessage passed in the constructor
     **/
    public final String REQUEST_KEY = "_request" + hashCode();

    /**
     * key to retrive from the HashMap the ACLMessage that has been sent.
     **/
    public final String REQUEST_SENT_KEY = "_request_sent" + hashCode();

    /**
     * key to retrive the second reply received.
     **/
    public final String SECOND_REPLY_KEY = "_2nd_reply" + hashCode();

    /**
     * key to retrive all the responses received.
     **/
    public final String ALL_RESPONSES_KEY = "_all-responses" + hashCode();

    /**
     * key to retrive the result notification received.
     **/
    public final String ALL_RESULT_NOTIFICATIONS_KEY = "_all-result-notification" + hashCode();
    private final Logger logger = Logger.getMyLogger(this.getClass().getName());
    //private ACLMessage request = null;
    private MessageTemplate mt = null;
    private int state = PREPARE_MSG_STATE;
    private boolean finished;
    private long timeout = -1;
    private long endingTime = 0;

    /**
     * Construct for the class by creating a new empty HashMap
     *
     * @see #SimpleAchieveREInitiator(Agent, ACLMessage, HashMap, HashMap)
     **/
    public SimpleAchieveREInitiator(Agent a, ACLMessage msg) {
        this(a, msg, new HashMap<>(), new HashMap<>());
    }

    /**
     * Constructs a  SimpleAchieveREInitiator   behaviour
     *
     * @param a     The agent performing the protocol
     * @param msg   The message that must be used to initiate the protocol.
     *              Notice that in this simple implementation, the
     *               prepareMessage
     *              method returns a single message.
     * @param store The  HashMap   that will be used by this
     *               SimpleAchieveREInitiator
     * @deprecated cf. constructor with 2 maps
     */

    public SimpleAchieveREInitiator(Agent a, ACLMessage msg, HashMap<String, List<ACLMessage>> store) {
        super(a);
        //TODO: verifier les appels car ils sont obsoletes
        setMapMessagesList(store);
        //	request = msg;
        getMapMessagesList().put(REQUEST_KEY, new ArrayList<>(List.of(msg)));
        finished = false;
    }

    /**
     * Constructs a  SimpleAchieveREInitiator   behaviour
     *
     * @param a               The agent performing the protocol
     * @param msg             The message that must be used to initiate the protocol.
     *                        Notice that in this simple implementation, the
     *                         prepareMessage
     *                        method returns a single message.
     * @param mapMessagesList The  HashMap   that will be used by this  SimpleAchieveREInitiator
     * @param mapMessages     The  HashMap   that will be used by this  SimpleAchieveREInitiator
     * @deprecated cf. constructor with 2 maps
     */

    public SimpleAchieveREInitiator(Agent a, ACLMessage msg, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a);
        setMapMessagesList(mapMessagesList);
        setMapMessages(mapMessages);
        //	request = msg;
        mapMessages.put(REQUEST_KEY, msg);
        finished = false;
    }

    public final void action() {

        switch (state) {
            case PREPARE_MSG_STATE: {
                //retrive the message to send
                ACLMessage msg = prepareRequest(getMapMessages().get(REQUEST_KEY));
                getMapMessages().put(REQUEST_SENT_KEY, msg);
                state = SEND_MSG_STATE;
            }
            case SEND_MSG_STATE: {
                //send the message. If there is more than one receiver only the first will be taken into account.
                var ds = getMapMessages();
                String conversationID;
                ACLMessage request = ds.get(REQUEST_SENT_KEY);
                if (request == null) { //no message to send --> protocol finished; //state = FINAL_STATE;
                    finished = true;
                } else {
                    if (request.getConversationId() == null) {
                        conversationID = "C" + hashCode() + "_" + System.currentTimeMillis();
                        request.setConversationId(conversationID);
                    } else {
                        conversationID = request.getConversationId();
                    }
                    mt = MessageTemplate.MatchConversationId(conversationID);

                    //send the message only to the first receiver.
                    Iterator<AID> receivers = request.getAllReceiver();
                    AID r = receivers.next();
                    request.clearAllReceiver();
                    request.addReceiver(r);
                    if (receivers.hasNext())
                        if (logger.isLoggable(Logger.WARNING))
                            logger.log(Logger.WARNING, "The message you are sending has more than one receivers. The message will be sent only to the first one !!");
                    if (r.equals(myAgent.getAID())) {
                        //if myAgent is the receiver then modify the messageTemplate
                        //to avoid intercepting the request as it was a reply.
                        mt = MessageTemplate.and(mt, MessageTemplate.not(MessageTemplate.MatchCustom(request, true)));
                    }
                    //set the timeout
                    //FIXME: if the Timeout is already expired before the message will be sent, it will be considered a infinite timeout
                    Date d = request.getReplyByDate();
                    if (d != null)
                        timeout = d.getTime() - (new Date()).getTime();
                    else
                        timeout = -1;
                    endingTime = System.currentTimeMillis() + timeout;

                    myAgent.send(request);
                    state = RECEIVE_REPLY_STATE;
                }
                break;
            }
            case RECEIVE_REPLY_STATE: {
                ACLMessage firstReply = myAgent.receive(mt);
                if (firstReply != null) {
                    var ds = getMapMessagesList();
                    switch (firstReply.getPerformative()) {
                        case ACLMessage.AGREE -> {
                            state = RECEIVE_2ND_REPLY_STATE;
                            var allResp = ds.get(ALL_RESPONSES_KEY);
                            allResp.add(firstReply);
                            handleAgree(firstReply);
                            //all the responses have been collected.
                            handleAllResponses(getMapMessagesList().get(ALL_RESPONSES_KEY));
                        }
                        case ACLMessage.REFUSE -> {
                            var allResp = ds.get(ALL_RESPONSES_KEY);
                            allResp.add(firstReply);
                            state = ALL_REPLIES_RECEIVED_STATE;
                            handleRefuse(firstReply);
                        }
                        case ACLMessage.NOT_UNDERSTOOD -> {
                            var allResp = ds.get(ALL_RESPONSES_KEY);
                            allResp.add(firstReply);
                            state = ALL_REPLIES_RECEIVED_STATE;
                            handleNotUnderstood(firstReply);
                        }
                        case ACLMessage.FAILURE -> {
                            var allResNot = ds.get(ALL_RESULT_NOTIFICATIONS_KEY);
                            allResNot.add(firstReply);
                            state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
                            handleFailure(firstReply);
                        }
                        case ACLMessage.INFORM -> {
                            var allResNot = ds.get(ALL_RESULT_NOTIFICATIONS_KEY);
                            allResNot.add(firstReply);
                            state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
                            handleInform(firstReply);
                        }
                        default -> {
                            state = RECEIVE_REPLY_STATE;
                            handleOutOfSequence(firstReply);
                        }
                    }
                } else {
                    if (timeout > 0) {
                        long blockTime = endingTime - System.currentTimeMillis();
                        if (blockTime <= 0) {  //timeout Expired
                            state = ALL_REPLIES_RECEIVED_STATE;
                        } else {//timeout not yet expired.
                            block(blockTime);
                        }
                    } else {//request without timeout.
                        block();
                    }
                    break;
                }

            }
            case RECEIVE_2ND_REPLY_STATE: {
                //after received an AGREE message. Wait for the second message.

                ACLMessage secondReply = myAgent.receive(mt);
                if (secondReply != null) {
                    var ds = getMapMessagesList();
                    switch (secondReply.getPerformative()) {
                        case ACLMessage.INFORM -> {
                            //call the method handleAllResponses since if an agree was arrived it was not called.
                            state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;

                            var allResNot = ds.get(ALL_RESULT_NOTIFICATIONS_KEY);
                            allResNot.add(secondReply);
                            handleInform(secondReply);
                        }
                        case ACLMessage.FAILURE -> {
                            state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;

                            var allResNot = ds.get(ALL_RESULT_NOTIFICATIONS_KEY);
                            allResNot.add(secondReply);
                            handleFailure(secondReply);
                        }
                        default -> {
                            state = RECEIVE_REPLY_STATE;
                            handleOutOfSequence(secondReply);
                        }
                    }
                } else {
                    block();
                }
            }
            case ALL_REPLIES_RECEIVED_STATE: {
                //after received a NOT-UNDERSTOOD and REFUSE message.
                //call the handleAllResponses and then the handleAllResultNotification without any message.
                state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
                handleAllResponses(getMapMessagesList().get(ALL_RESPONSES_KEY));
            }
            case ALL_RESULT_NOTIFICATION_RECEIVED_STATE: {
                //after an INFORM or FAILURE message arrived.
                finished = true;
                handleAllResultNotifications(getMapMessagesList().get(ALL_RESULT_NOTIFICATIONS_KEY));
            }
            default: {
            }
        }
    }

    public void onStart() {
        initializeHashMap();
    }

    public boolean done() {
        return finished;
    }

    /**
     * This method must return the ACLMessage to be sent.
     * This default implementation just return the ACLMessage object passed in the constructor.
     * Programmer might override the method in order to return a different ACLMessage.
     * Note that for this simple version of protocol, the message will be just send to the first receiver set.
     *
     * @param msg the ACLMessage object passed in the constructor.
     * @return a ACLMessage.
     **/
    protected ACLMessage prepareRequest(ACLMessage msg) {
        return msg;
    }

    /**
     * This method is called every time an  agree
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     *
     * @param agree the received agree message
     **/
    protected void handleAgree(ACLMessage agree) {
        if (logger.isLoggable(Logger.FINE))
            logger.log(Logger.FINE, "in HandleAgree: " + agree.toString());

    }

    /**
     * This method is called every time a  refuse
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     *
     * @param refuse the received refuse message
     **/
    protected void handleRefuse(ACLMessage refuse) {
        if (logger.isLoggable(Logger.FINE))
            logger.log(Logger.FINE, "in HandleRefuse: " + refuse.toString());
    }

    /**
     * This method is called every time a  not-understood
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     *
     * @param notUnderstood the received not-understood message
     **/
    protected void handleNotUnderstood(ACLMessage notUnderstood) {
        if (logger.isLoggable(Logger.FINE))
            logger.log(Logger.FINE, "in HandleNotUnderstood: " + notUnderstood.toString());
    }

    /**
     * This method is called every time a  inform
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     *
     * @param inform the received inform message
     **/
    protected void handleInform(ACLMessage inform) {
        if (logger.isLoggable(Logger.FINE))
            logger.log(Logger.FINE, "in HandleInform: " + inform.toString());
    }

    /**
     * This method is called every time a  failure
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     *
     * @param failure the received failure message
     **/
    protected void handleFailure(ACLMessage failure) {
        if (logger.isLoggable(Logger.FINEST))
            logger.log(Logger.FINEST, "in HandleFailure: " + failure.toString());
    }

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
        if (logger.isLoggable(Logger.FINEST))
            logger.log(Logger.FINEST, "in HandleOutOfSequence: " + msg.toString());
    }

    /**
     * This method is called when all the responses have been
     * collected or when the timeout is expired.
     * By response message we intend here all the  agree, not-understood,
     * refuse   received messages, which are not
     * not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event
     * by analysing all the messages in just one call.
     *
     * @param responses the Vector of ACLMessage objects that have been received
     **/
    protected void handleAllResponses(List<ACLMessage> responses) {
        if (logger.isLoggable(Logger.FINEST))
            logger.log(Logger.FINEST, myAgent.getName() + "in handleAllResponses: ");
    }

    /**
     * This method is called when all the result notification messages
     * have been collected.
     * By result notification message we intend here all the  inform,
     * failure   received messages, which are not
     * not out-of-sequence according to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event
     * by analysing all the messages in just one call.
     *
     * @param resultNotifications the Vector of ACLMessage object received
     **/
    protected void handleAllResultNotifications(List<ACLMessage> resultNotifications) {
        if (logger.isLoggable(Logger.FINEST))
            logger.log(Logger.FINEST, myAgent.getName() + "in HandleAllResultNotification: ");
    }

    /**
     * This method resets this behaviour so that it restarts from the initial
     * state of the protocol with a null message.
     */
    public void reset() {
        reset(null);
    }

    /**
     * This method resets this behaviour so that it restarts the protocol with
     * another request message.
     *
     * @param msg updates message to be sent.
     */
    public void reset(ACLMessage msg) {
        finished = false;
        state = PREPARE_MSG_STATE;
        getMapMessages().put(REQUEST_KEY, msg);
        initializeHashMap();
        super.reset();
    }

    private void initializeHashMap() {

        List<ACLMessage> l = new ArrayList<>();
        getMapMessagesList().put(ALL_RESPONSES_KEY, l);
        l = new ArrayList<>();
        getMapMessagesList().put(ALL_RESULT_NOTIFICATIONS_KEY, l);
    }

}//end class SimpleAchieveREInitiator
 
