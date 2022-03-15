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
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import jade.proto.states.ReplySender;

import java.io.Serial;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Common base class for all classes implementing the Responder
 * role in interaction protocols where the responder is expected
 * to receive more than one message from the initiator and reply
 * to them.
 *
 * @author Elena Quarantotto - TILAB
 * @author Giovanni Caire - TILAB
 */
abstract class Responder extends FSMBehaviour {


    // Data store keys
    //#APIDOC_EXCLUDE_BEGIN
    // FSM states names
    protected static final String RECEIVE_INITIATION = "Receive-Initiation";
    protected static final String RECEIVE_NEXT = "Receive-Next";


    // private inner classes for the FSM states
    protected static final String HANDLE_OUT_OF_SEQUENCE = "Handle-Out-of-seq";
    protected static final String CHECK_IN_SEQ = "Check-In-seq";
    protected static final String SEND_REPLY = "Send-Reply";
    /**
     * Key to retrieve from the HashMap of the behaviour the last received
     * ACLMessage
     */
    public final String RECEIVED_KEY = "__Received_key" + hashCode();
    /**
     * Key to set into the HashMap of the behaviour the new ACLMessage
     * to be sent back to the initiator as a reply.
     */
    public final String REPLY_KEY = "__Reply_key" + hashCode();


    /**
     * Constructor of the behaviour that creates a new empty HashMap
     *
     * @see #Responder(Agent a, MessageTemplate mt, HashMap, HashMap)
     **/
    public Responder(Agent a, MessageTemplate mt) {
        this(a, mt, new HashMap<>(), new HashMap<>());
    }
    /**
     * Constructor of the behaviour.
     *
     * @param a               is the reference to the Agent object
     * @param mt              is the MessageTemplate that must be used to match
     *                        the initiation message. Take care that if mt is null every message is
     *                        consumed by this protocol.
     * @param mapMessagesList the HashMap of messages list  for this protocol behaviour
     * @deprecated
     **/
    public Responder(Agent a, MessageTemplate mt, HashMap<String, List<ACLMessage>> mapMessagesList) {
        super(a);
        setMapMessagesList(mapMessagesList);

        registerDefaultTransition(RECEIVE_INITIATION, CHECK_IN_SEQ);
        registerDefaultTransition(RECEIVE_NEXT, CHECK_IN_SEQ);

        registerDefaultTransition(CHECK_IN_SEQ, HANDLE_OUT_OF_SEQUENCE);
        registerDefaultTransition(HANDLE_OUT_OF_SEQUENCE, RECEIVE_NEXT, new String[]{HANDLE_OUT_OF_SEQUENCE});


        Behaviour b;

        // RECEIVE_INITIATION
        b = new CfpReceiver(myAgent, mt, -1, getMapMessagesList(), getMapMessages(), RECEIVED_KEY);
        registerFirstState(b, RECEIVE_INITIATION);

        // RECEIVE_NEXT
        b = new NextReceiver(myAgent, null, -1, getMapMessagesList(), getMapMessages(), RECEIVED_KEY);
        registerState(b, RECEIVE_NEXT);

        // CHECK_IN_SEQ
        b = new CheckInSeq(myAgent);
        registerDSState(b, CHECK_IN_SEQ);

        // HANDLE_OUT_OF_SEQUENCE
        b = new HandleOutOfSeq(myAgent);
        registerDSState(b, HANDLE_OUT_OF_SEQUENCE);

        // SEND_REPLY
        b = new SendReply(myAgent, REPLY_KEY, RECEIVED_KEY, getMapMessagesList(), getMapMessages());
        registerDSState(b, SEND_REPLY);
    }
    /**
     * Constructor of the behaviour.
     *
     * @param a               is the reference to the Agent object
     * @param mt              is the MessageTemplate that must be used to match
     *                        the initiation message. Take care that if mt is null every message is
     *                        consumed by this protocol.
     * @param mapMessagesList the HashMap of messages list  for this protocol behaviour
     * @param mapMessages     the HashMap of messages   for this protocol behaviour
     **/
    public Responder(Agent a, MessageTemplate mt, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a);
        setMapMessagesList(mapMessagesList);
        setMapMessages(mapMessages);

        registerDefaultTransition(RECEIVE_INITIATION, CHECK_IN_SEQ);
        registerDefaultTransition(RECEIVE_NEXT, CHECK_IN_SEQ);

        registerDefaultTransition(CHECK_IN_SEQ, HANDLE_OUT_OF_SEQUENCE);
        registerDefaultTransition(HANDLE_OUT_OF_SEQUENCE, RECEIVE_NEXT, new String[]{HANDLE_OUT_OF_SEQUENCE});


        Behaviour b;

        // RECEIVE_INITIATION
        b = new CfpReceiver(myAgent, mt, -1, getMapMessagesList(), getMapMessages(), RECEIVED_KEY);
        registerFirstState(b, RECEIVE_INITIATION);

        // RECEIVE_NEXT
        b = new NextReceiver(myAgent, null, -1, getMapMessagesList(), getMapMessages(), RECEIVED_KEY);
        registerState(b, RECEIVE_NEXT);

        // CHECK_IN_SEQ
        b = new CheckInSeq(myAgent);
        registerDSState(b, CHECK_IN_SEQ);

        // HANDLE_OUT_OF_SEQUENCE
        b = new HandleOutOfSeq(myAgent);
        registerDSState(b, HANDLE_OUT_OF_SEQUENCE);

        // SEND_REPLY
        b = new SendReply(myAgent, REPLY_KEY, RECEIVED_KEY, getMapMessagesList(), getMapMessages());
        registerDSState(b, SEND_REPLY);
    }
    // For persistence service
    private Responder() {
    }

    /**
     * This method is called whenever a message is received that does
     * not comply to the protocol rules.
     * This default implementation does nothing.
     * Programmers may override it in case they need to react to this event.
     *
     * @param msg the received out-of-sequence message.
     */
    protected void handleOutOfSequence(ACLMessage msg) {
    }

    /**
     * This method allows to register a user defined <code>Behaviour</code>
     * in the HANDLE_OUT_OF_SEQ state.
     * This behaviour would override the homonymous method.
     * This method also sets the
     * data store of the registered <code>Behaviour</code> to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve
     * the <code>out of sequence</code> ACLMessage object received
     * from the HashMap at the <code>RECEIVED_KEY</code>
     * key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerHandleOutOfSequence(Behaviour b) {
        registerDSState(b, HANDLE_OUT_OF_SEQUENCE);
    }

    /**
     * Reset this behaviour.
     */
    public void reset() {
        super.reset();
        var ds = getMapMessagesList();
        ds.remove(RECEIVED_KEY);
        ds.remove(REPLY_KEY);
    }

    /**
     * Check whether a received message complies with the protocol rules.
     */
    protected abstract boolean checkInSequence(ACLMessage received);

    /**
     * This method can be redefined by protocol specific implementations
     * to update the status of the protocol after a reply has been sent.
     * This default implementation does nothing.
     */
    protected void replySent(int exitValue) {
    }

    //#APIDOC_EXCLUDE_END

    /**
     * Utility method to register a behaviour in a state of the
     * protocol and set the HashMap appropriately
     */
    protected void registerDSState(Behaviour b, String name) {
        b.setMapMessagesList(getMapMessagesList());
        registerState(b, name);
    }

    private static class CfpReceiver extends MsgReceiver {

        /**
         * @deprecated
         */
        public CfpReceiver(Agent myAgent, MessageTemplate mt, long deadline, HashMap<String, List<ACLMessage>> s, String msgKey) {
            super(myAgent, mt, deadline, s, msgKey);
        }

        public CfpReceiver(Agent myAgent, MessageTemplate mt, long deadline, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages, String msgKey) {
            super(myAgent, mt, deadline, mapMessagesList, mapMessages, msgKey);
        }

        // For persistence service
        private CfpReceiver() {
        }

        public int onEnd() {
            Responder fsm = (Responder) getParent();
            MsgReceiver nextRecv = (MsgReceiver) fsm.getState(RECEIVE_NEXT);

            // Set the template to receive next messages
            ACLMessage received = getMapMessages().get(fsm.RECEIVED_KEY);
            nextRecv.setTemplate(MessageTemplate.MatchConversationId(received.getConversationId()));
            return super.onEnd();
        }

    } // End of CfpReceiver class

    private static class NextReceiver extends MsgReceiver {
        /**
         * @deprecated
         */
        public NextReceiver(Agent myAgent, MessageTemplate mt, long deadline, HashMap<String, List<ACLMessage>> s, String msgKey) {
            super(myAgent, mt, deadline, s, msgKey);
        }

        public NextReceiver(Agent myAgent, MessageTemplate mt, long deadline, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages, String msgKey) {
            super(myAgent, mt, deadline, mapMessagesList, mapMessages, msgKey);
        }

        // For persistence service
        private NextReceiver() {
        }

        public void onStart() {
            // Set the deadline for receiving the next message on the basis
            // of the last reply sent
            Responder fsm = (Responder) getParent();
            ACLMessage reply = getMapMessages().get(fsm.REPLY_KEY);
            if (reply != null) {
                Date d = reply.getReplyByDate();
                if (d != null && d.getTime() > System.currentTimeMillis()) {
                    setDeadline(d.getTime());
                }
            }
        }

    } // End of NextReceiver class

    //#APIDOC_EXCLUDE_BEGIN

    private static class CheckInSeq extends OneShotBehaviour {

        @Serial
        private static final long serialVersionUID = 4487495895818000L;
        private int ret;

        public CheckInSeq(Agent a) {
            super(a);
        }

        // For persistence service
        private CheckInSeq() {
        }

        public void action() {
            Responder fsm = (Responder) getParent();
            ACLMessage received = getMapMessages().get(fsm.RECEIVED_KEY);
            if (fsm.checkInSequence(received)) {
                ret = received.getPerformative();
            } else {
                ret = -1;
            }
        }

        public int onEnd() {
            return ret;
        }

    } // End of CheckInSeq class

    private static class HandleOutOfSeq extends OneShotBehaviour {

        @Serial
        private static final long serialVersionUID = 4487495895818005L;

        public HandleOutOfSeq(Agent a) {
            super(a);
        }

        // For persistence service
        private HandleOutOfSeq() {
        }

        public void action() {
            Responder fsm = (Responder) getParent();
            fsm.handleOutOfSequence(getMapMessages().get(fsm.RECEIVED_KEY));
        }

    } // End of HandleOutOfSeq class

    private static class SendReply extends ReplySender {

        public SendReply(Agent a, String replyKey, String msgKey, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
            super(a, replyKey, msgKey, mapMessagesList, mapMessages);
        }

        // For persistence service
        private SendReply() {
        }

        public int onEnd() {
            int ret = super.onEnd();
            Responder fsm = (Responder) getParent();
            fsm.replySent(ret);
            return ret;
        }

    } // End of SendReply class
    //#APIDOC_EXCLUDE_END
}
