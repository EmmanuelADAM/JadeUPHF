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

//#CUSTOM_EXCLUDE_FILE

import jade.core.AID;
import jade.core.Agent;
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
import jade.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;


/**
 * This is a single homogeneous and effective implementation of the responder role in
 * all the FIPA-Subscribe-like interaction protocols defined by FIPA,
 * that is all those protocols
 * where the initiator sends a single "subscription" message
 * and receives notifications each time a given condition becomes true.
 *
 * @author Elisabetta Cortese - TILAB
 * @author Giovanni Caire - TILAB
 * @see SubscriptionInitiator
 */
public class SubscriptionResponder extends FSMBehaviour implements FIPANames.InteractionProtocol {

    // FSM states names
    private static final String RECEIVE_SUBSCRIPTION = "Receive-subscription";
    private static final String HANDLE_SUBSCRIPTION = "Handle-subscription";
    private static final String HANDLE_CANCEL = "Handle-cancel";
    private static final String SEND_RESPONSE = "Send-response";
    private static final String SEND_NOTIFICATIONS = "Send-notifications";
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object sent by the initiator as a subscription.
     **/
    public final String SUBSCRIPTION_KEY = "__subs_canc" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object sent by the initiator to cancel a subscription.
     **/
    public final String CANCEL_KEY = SUBSCRIPTION_KEY;
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object sent as a response to the initiator.
     **/
    public final String RESPONSE_KEY = "__response" + hashCode();
    // The MsgReceiver behaviour used to receive subscription messages
    private final MsgReceiver msgRecBehaviour;

    private final Hashtable<String, Subscription> subscriptions = new Hashtable<>();
    private final List<ACLMessage[]> notifications = new ArrayList<>();
    private final Logger myLogger = Logger.getJADELogger(getClass().getName());
    /**
     * The  SubscriptionManager   used by this
     *  SubscriptionResponder   to register subscriptions
     */
    protected SubscriptionManager mySubscriptionManager;

    /**
     * Construct a SubscriptionResponder behaviour that handles subscription messages matching a given template.
     *
     * @see #SubscriptionResponder(Agent, MessageTemplate, SubscriptionManager, HashMap, HashMap)
     **/
    public SubscriptionResponder(Agent a, MessageTemplate mt) {
        this(a, mt, null, new HashMap<>(), new HashMap<>());
    }

    /**
     * Construct a SubscriptionResponder behaviour that handles subscription messages matching a given template and
     * notifies a given SubscriptionManager about subscription/un-subscription events.
     * <p>
     * see #SubscriptionResponder(Agent, MessageTemplate, SubscriptionManager, HashMap)
     **/
    public SubscriptionResponder(Agent a, MessageTemplate mt, SubscriptionManager sm) {
        this(a, mt, sm, new HashMap<>(), new HashMap<>());
    }

    /**
     * Construct a SubscriptionResponder behaviour that handles subscription messages matching a given template,
     * notifies a given SubscriptionManager about subscription/un-subscription events and uses a given HashMap.
     *
     * @param a               is the reference to the Agent performing this behaviour.
     * @param mt              is the MessageTemplate that must be used to match
     *                        subscription messages sent by the initiators. Take care that
     *                        if mt is null every message is consumed by this protocol.
     * @param sm              The  SubscriptionManager   object that is notified about subscription/un-subscription events
     * @param mapMessagesList the HashMap of messages list that will be used by protocol
     * @param mapMessages     the HashMap of messages that will be used by protocol
     **/
    public SubscriptionResponder(Agent a, MessageTemplate mt, SubscriptionManager sm, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a);
        setMapMessagesList(mapMessagesList);
        setMapMessages(mapMessages);
        mySubscriptionManager = sm;

        // Register the FSM transitions
        registerDefaultTransition(RECEIVE_SUBSCRIPTION, HANDLE_SUBSCRIPTION);
        registerTransition(RECEIVE_SUBSCRIPTION, HANDLE_CANCEL, ACLMessage.CANCEL);
        registerTransition(RECEIVE_SUBSCRIPTION, SEND_NOTIFICATIONS, MsgReceiver.INTERRUPTED);
        registerDefaultTransition(HANDLE_SUBSCRIPTION, SEND_RESPONSE);
        registerDefaultTransition(HANDLE_CANCEL, SEND_RESPONSE);
        registerDefaultTransition(SEND_RESPONSE, RECEIVE_SUBSCRIPTION, new String[]{HANDLE_SUBSCRIPTION, HANDLE_CANCEL});
        registerDefaultTransition(SEND_NOTIFICATIONS, RECEIVE_SUBSCRIPTION);

        //***********************************************
        // For each state create and register a behaviour
        //***********************************************
        Behaviour b;

        // RECEIVE_SUBSCRIPTION
        msgRecBehaviour = new MsgReceiver(myAgent, mt, MsgReceiver.INFINITE, getMapMessagesList(), getMapMessages(), SUBSCRIPTION_KEY);
        registerFirstState(msgRecBehaviour, RECEIVE_SUBSCRIPTION);

        // HANDLE_SUBSCRIPTION
        b = new OneShotBehaviour(myAgent) {

            public void action() {
                var ds = getMapMessages();
                ACLMessage subscription = ds.get(SUBSCRIPTION_KEY);
                ACLMessage response;
                try {
                    response = handleSubscription(subscription);
                } catch (NotUnderstoodException | RefuseException nue) {
                    response = nue.getACLMessage();
                }
                ds.put(RESPONSE_KEY, response);
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, HANDLE_SUBSCRIPTION);

        // HANDLE_CANCEL
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                var ds = getMapMessages();
                ACLMessage cancel = ds.get(CANCEL_KEY);
                ACLMessage response;
                try {
                    response = handleCancel(cancel);
                } catch (FailureException fe) {
                    response = fe.getACLMessage();
                }
                ds.put(RESPONSE_KEY, response);
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
        registerState(b, HANDLE_CANCEL);

        // SEND_RESPONSE
        b = new ReplySender(myAgent, RESPONSE_KEY, SUBSCRIPTION_KEY, mapMessagesList, mapMessages);
        registerState(b, SEND_RESPONSE);

        // SEND_NOTIFICATIONS
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                sendNotifications();
            }
        };
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, SEND_NOTIFICATIONS);

    } // End of Constructor

    /**
     * Construct a SubscriptionResponder behaviour that handles subscription messages matching a given template,
     * notifies a given SubscriptionManager about subscription/un-subscription events and uses a given HashMap.
     *
     * @param a               is the reference to the Agent performing this behaviour.
     * @param mt              is the MessageTemplate that must be used to match
     *                        subscription messages sent by the initiators. Take care that
     *                        if mt is null every message is consumed by this protocol.
     * @param sm              The  SubscriptionManager   object that is notified about subscription/un-subscription events
     * @param mapMessagesList the HashMap that will be used by protocol
     * deprecated public SubscriptionResponder(Agent a, MessageTemplate mt, SubscriptionManager sm, HashMap<String, List<ACLMessage>> mapMessagesList) {
    super(a);
    setMapMessagesList(mapMessagesList);
    mySubscriptionManager = sm;

    // Register the FSM transitions
    registerDefaultTransition(RECEIVE_SUBSCRIPTION, HANDLE_SUBSCRIPTION);
    registerTransition(RECEIVE_SUBSCRIPTION, HANDLE_CANCEL, ACLMessage.CANCEL);
    registerTransition(RECEIVE_SUBSCRIPTION, SEND_NOTIFICATIONS, MsgReceiver.INTERRUPTED);
    registerDefaultTransition(HANDLE_SUBSCRIPTION, SEND_RESPONSE);
    registerDefaultTransition(HANDLE_CANCEL, SEND_RESPONSE);
    registerDefaultTransition(SEND_RESPONSE, RECEIVE_SUBSCRIPTION, new String[]{HANDLE_SUBSCRIPTION, HANDLE_CANCEL});
    registerDefaultTransition(SEND_NOTIFICATIONS, RECEIVE_SUBSCRIPTION);

    //***********************************************
    // For each state create and register a behaviour
    //***********************************************
    Behaviour b;

    // RECEIVE_SUBSCRIPTION
    msgRecBehaviour = new MsgReceiver(myAgent, mt, MsgReceiver.INFINITE, getMapMessagesList(), SUBSCRIPTION_KEY);
    registerFirstState(msgRecBehaviour, RECEIVE_SUBSCRIPTION);

    // HANDLE_SUBSCRIPTION
    b = new OneShotBehaviour(myAgent) {

    public void action() {
    var ds = getMapMessages();
    ACLMessage subscription = ds.get(SUBSCRIPTION_KEY);
    ACLMessage response;
    try {
    response = handleSubscription(subscription);
    } catch (NotUnderstoodException | RefuseException nue) {
    response = nue.getACLMessage();
    }
    ds.put(RESPONSE_KEY, response);
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, HANDLE_SUBSCRIPTION);

    // HANDLE_CANCEL
    b = new OneShotBehaviour(myAgent) {
    public void action() {
    var ds = getMapMessages();
    ACLMessage cancel = ds.get(CANCEL_KEY);
    ACLMessage response;
    try {
    response = handleCancel(cancel);
    } catch (FailureException fe) {
    response = fe.getACLMessage();
    }
    ds.put(RESPONSE_KEY, response);
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, HANDLE_CANCEL);

    // SEND_RESPONSE
    b = new ReplySender(myAgent, RESPONSE_KEY, SUBSCRIPTION_KEY, mapMessagesList, null);
    registerState(b, SEND_RESPONSE);

    // SEND_NOTIFICATIONS
    b = new OneShotBehaviour(myAgent) {
    public void action() {
    sendNotifications();
    }
    };
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, SEND_NOTIFICATIONS);

    } // End of Constructor
     */

    /**
     * This static method can be used
     * to set the proper message Template (based on the performative of the
     * subscription message) into the constructor of this behaviour.
     *
     * @param perf The performative of the subscription message
     */
    public static MessageTemplate createMessageTemplate(int perf) {
        return MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPA_SUBSCRIBE),
                MessageTemplate.or(MessageTemplate.MatchPerformative(perf), MessageTemplate.MatchPerformative(ACLMessage.CANCEL)));
    }

    /**
     * Reset this behaviour
     */
    // FIXME: reset deve resettare anche le sottoscrizioni?
    public void reset() {
        super.reset();
        var ds = getMapMessages();
        ds.remove(SUBSCRIPTION_KEY);
        ds.remove(RESPONSE_KEY);
    }

    /**
     * This method resets the protocol and allows to change the
     *  MessageTemplate
     * that defines what messages this SubscriptionResponder
     * will react to.
     */
    public void reset(MessageTemplate mt) {
        this.reset();
        msgRecBehaviour.reset(mt, MsgReceiver.INFINITE, getMapMessagesList(), getMapMessages(), SUBSCRIPTION_KEY);
    }

    /**
     * This method is called when a subscription
     * message is received that matches the message template
     * specified in the constructor.
     * The default implementation creates an new  Subscription
     * object, stores it internally and notify the  SubscriptionManager
     * used by this responder if any. Then it returns null which has
     * the effect of sending no response. Programmers in general do not need
     * to override this method. In case they need to manage Subscription objects in an application specific
     * way they should rather use a  SubscriptionManager   with the  register()   method properly implemented.
     * However they could override it in case they need to react to the reception of a
     * subscription message in a different way, e.g. by sending back an AGREE message.
     *
     * @param subscription the received message
     * @return the ACLMessage to be sent as a response: typically one of
     *  agree, refuse, not-understood   or null if no response must be sent back.
     */
    protected ACLMessage handleSubscription(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
        // Call prepareResponse() for backward compatibility
        return prepareResponse(subscription);
    }

    /**
     * @deprecated Use handleSubscription() instead
     */
    protected ACLMessage prepareResponse(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
        Subscription subs = createSubscription(subscription);
        if (mySubscriptionManager != null) {
            mySubscriptionManager.register(subs);
        }
        return null;
    }

    /**
     * This method is called when a CANCEL message is received for a previous subscription.
     * The default implementation retrieves the  Subscription
     * object the received cancel message refers to, notifies the
     *  SubscriptionManager   used by this responder if any and remove the Subscription from its internal structures.
     * Then it returns null which has the effect of sending no response.
     * Programmers in general do not need
     * to override this method. In case they need to manage Subscription objects in an application specific
     * way they should rather use a  SubscriptionManager   with the  deregister()   method properly implemented.
     * However they could override it in case they need to react to the reception of a
     * cancel message in a different way, e.g. by sending back an INFORM.
     *
     * @param cancel the received CANCEL message
     * @return the ACLMessage to be sent as a response to the
     * cancel operation: typically one of  inform   and  failure   or null if no response must be sent back.
     */
    protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
        Subscription s = getSubscription(cancel);
        if (s != null) {
            if (mySubscriptionManager != null) {
                mySubscriptionManager.deregister(s);
            }
            s.close();
        }
        return null;
    }

    /**
     * This method allows to register a user defined  Behaviour
     * in the HANDLE_SUBSCRIPTION state.
     * This behaviour overrides the homonymous method.
     * This method also sets the
     * data store of the registered  Behaviour   to the
     * HashMap of this current behaviour.
     * It is responsibility of the registered behaviour to put the
     * response (if any) to be sent back into the HashMap at the
     *  RESPONSE_KEY   key.
     * The incoming subscription message can be retrieved from the
     * HashMap at the  SUBSCRIPTION_KEY   key
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleSubscription(Behaviour b) {
        registerState(b, HANDLE_SUBSCRIPTION);
        b.setMapMessagesList(getMapMessagesList());
    }

    /**
     * @deprecated Use registerHandleSubscription() instead.
     */
    public void registerPrepareResponse(Behaviour b) {
        registerHandleSubscription(b);
    }

    /**
     * This method allows to register a user defined  Behaviour
     * in the HANDLE_CANCEL state.
     * This behaviour overrides the homonymous method.
     * This method also sets the
     * data store of the registered  Behaviour   to the
     * HashMap of this current behaviour.
     * It is responsibility of the registered behaviour to put the
     * response (if any) to be sent back into the HashMap at the
     *  RESPONSE_KEY   key.
     * The incoming CANCEL message can be retrieved from the
     * HashMap at the  CANCEL_KEY   key
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleCancel(Behaviour b) {
        registerState(b, HANDLE_CANCEL);
        b.setMapMessagesList(getMapMessagesList());
        b.setMapMessages(getMapMessages());
    }

    /**
     * Utility method to correctly create a new  Subscription   object
     * managed by this  SubscriptionResponder
     */
    public Subscription createSubscription(ACLMessage subsMsg) {
        Subscription s = new Subscription(this, subsMsg);
        String convId = subsMsg.getConversationId();
        if (convId != null) {
            Subscription old = subscriptions.put(convId, s);
            if (old != null) {
                myLogger.log(Logger.WARNING, "Agent " + myAgent.getLocalName() + " - Subscription from agent " + old.getMessage().getSender().getLocalName() + " overridden by agent " + subsMsg.getSender().getLocalName());
            }
        }
        return s;
    }

    /**
     * Utility method to correctly retrieve the
     *  Subscription   object that is related to the conversation
     * message  msg   belongs to.
     *
     * @param msg The message whose  conversation-id   indicates the conversation
     * @return the  Subscription   object related to the conversation the given message belongs to
     */
    public Subscription getSubscription(ACLMessage msg) {
        String convId = msg.getConversationId();
        return getSubscription(convId);
    }

    /**
     * Utility method to correctly retrieve the
     *  Subscription   object that is related a given conversation.
     *
     * @param convId The id of the conversation
     * @return the  Subscription   object related to the given conversation
     */
    public Subscription getSubscription(String convId) {
        Subscription s = null;
        if (convId != null) {
            s = subscriptions.get(convId);
        }
        return s;
    }

    /**
     * Utility method that retrieves all Subscription-s done by a given agent
     *
     * @param subscriber The AID of the agent whose subscriptions must be retrieved
     * @return A  Vector   including all  Subscription  -s made by the given agent
     */
    public List<Subscription> getSubscriptions(AID subscriber) {
        // Synchronization is needed to avoid concurrent modification exception in case this method is
        // invoked from within a separate Thread
        synchronized (subscriptions) {
            List<Subscription> ss = new ArrayList<>();
            var en = subscriptions.elements();
            while (en.hasMoreElements()) {
                Subscription s = en.nextElement();
                if (s.getMessage().getSender().equals(subscriber)) {
                    ss.add(s);
                }
            }
            return ss;
        }
    }

    /**
     * Utility method that retrieves all Subscription-s managed by this  SubscriptionResponder
     *
     * @return A  Vector   including all  Subscription  -s managed by this  SubscriptionResponder
     */
    public List<Subscription> getSubscriptions() {
        // Synchronization is needed to avoid concurrent modification exception in case this method is
        // invoked from within a separate Thread
        synchronized (subscriptions) {
            List<Subscription> ss = new ArrayList<>();
            var en = subscriptions.elements();
            while (en.hasMoreElements()) {
                Subscription s = en.nextElement();
                ss.add(s);
            }
            return ss;
        }
    }

    /**
     * This is called by a Subscription object when a notification has
     * to be sent to the corresponding subscribed agent.
     * Executed in mutual exclusion with sendNotifications(). Note that this
     * synchronization is not needed in general, but we never know how users
     * manages Subscription objects (possibly in another thread)
     */
    private synchronized void addNotification(ACLMessage notification, ACLMessage subscription) {
        ACLMessage[] tmp = new ACLMessage[]{notification, subscription};
        notifications.add(tmp);
        msgRecBehaviour.interrupt();
    }

    /**
     * This is called within the SEND_NOTIFICATIONS state.
     * Executed in mutual exclusion with addNotification(). Note that this
     * synchronization is not needed in general, but we never know how users
     * manages Subscription objects (possibly in another thread)
     */
    private synchronized void sendNotifications() {
        for (ACLMessage[] tabNotifications : notifications) {
            boolean receiversNull = true;
            boolean replyWithNull = true;
            if (tabNotifications[0].getAllReceiver().hasNext()) {
                receiversNull = false;
            }
            if (tabNotifications[0].getReplyWith() != null) {
                replyWithNull = false;
            }
            ReplySender.adjustReply(myAgent, tabNotifications[0], tabNotifications[1]);
            myAgent.send(tabNotifications[0]);
            // If the message was modified --> restore it
            if (receiversNull) {
                tabNotifications[0].clearAllReceiver();
            }
            if (replyWithNull) {
                tabNotifications[0].setReplyWith(null);
            }
        }
        notifications.clear();
    }

    /**
     * Inner interface SubscriptionManager.
     * <p>
     * A  SubscriptionResponder  , besides enforcing and
     * controlling the sequence of messages in a subscription conversation, also stores current subscriptions
     * into an internal table. In many cases however it is desirable to manage Subscription objects in an application specific way
     * (e.g. storing them to a persistent support such as a DB). To enable that, it is possible to pass a
     * SubscriptionManager implementation to the SubscriptionResponder. The SubscriptionManager is notified
     * about subscription and cancellation events by means of the register() and deregister() methods.
     * <p>
     */
    public interface SubscriptionManager {
        /**
         * Register a new Subscription object
         *
         * @param s The Subscription object to be registered
         * @return The boolean value returned by this method provides an
         * indication to the  SubscriptionResponder   about whether
         * or not an AGREE message should be sent back to the initiator. The
         * default implementation of the  handleSubscription()   method
         * of the  SubscriptionResponder   ignores this indication,
         * but programmers can override it.
         */
        boolean register(Subscription s) throws RefuseException, NotUnderstoodException;

        /**
         * Deregister a Subscription object
         *
         * @return The boolean value returned by this method provides an
         * indication to the  SubscriptionResponder   about whether
         * or not an INFORM message should be sent back to the initiator. The
         * default implementation of the  handleCancel()   method
         * of the  SubscriptionResponder   ignores this indication,
         * but programmers can override it.
         */
        boolean deregister(Subscription s) throws FailureException;
    } // END of inner interface SubscriptionManager


    /**
     * Inner calss Subscription
     * <p>
     * This class represents a subscription. When a notification has to
     * be sent to a subscribed agent the notification message should not
     * be directly sent to the subscribed agent, but should be passed to the
     *  Subscription   object representing the subscription of that
     * agent by means of its  notify()   method. This automatically
     * handles sequencing and protocol fields appropriately.
     *  Subscription   objects must be created by means of the
     *  createSubscription()   method.
     */
    public static class Subscription {

        private final ACLMessage subscription;
        private final SubscriptionResponder myResponder;

        /**
         * Private constructor. The  createSubscription()
         * must be used instead.
         *
         * @param r The  SubscriptionResponder   that received
         *          the subscription message corresponding to this
         *           Subscription
         * @param s The subscription message corresponding to this
         *           Subscription
         */
        private Subscription(SubscriptionResponder r, ACLMessage s) {
            myResponder = r;
            subscription = s;
        }

        /**
         * Retrieve the ACL message with which this
         * subscription object was created.
         *
         * @return the subscription message corresponding to this
         *  Subscription
         */
        public ACLMessage getMessage() {
            return subscription;
        }

        /**
         * This method allows sending back a notification message to the subscribed
         * agent associated to this  Subscription   object. The user
         * should call this method, instead of directly using the  send()
         * method of the  Agent   class, as it automatically
         * handles sequencing and protocol fields appropriately.
         */
        public void notify(ACLMessage notification) {
            myResponder.addNotification(notification, subscription);
        }

        /**
         * This method removes the current Subscription object from the SubscriptionResponder internal tables.
         */
        public void close() {
            String convId = subscription.getConversationId();
            if (convId != null) {
                myResponder.subscriptions.remove(convId);
            }
        }

        public boolean equals(Object obj) {
            if (obj instanceof Subscription) {
                // They are equals if they have the same conversation-id
                return subscription.getConversationId().equals(((Subscription) obj).subscription.getConversationId());
            }
            return false;
        }

        public int hashCode() {
            return subscription.getConversationId().hashCode();
        }
    } // END of inner class Subscription

}
