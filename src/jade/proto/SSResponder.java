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
 * Common base class for all classes implementing the Single Session
 * version of the Responder
 * role in interaction protocols where the responder is expected
 * to receive more than one message from the initiator and reply
 * to each of them.
 *
 * @author Giovanni Caire - TILAB
 */
abstract class SSResponder extends FSMBehaviour {
    //#APIDOC_EXCLUDE_BEGIN
    // FSM states names
    protected static final String RECEIVE_NEXT = "Receive-Next";
    protected static final String CHECK_IN_SEQ = "Check-In-seq";
    protected static final String HANDLE_OUT_OF_SEQUENCE = "Handle-Out-of-seq";
    protected static final String SEND_REPLY = "Send-Reply";
    protected static final String DUMMY_FINAL = "Dummy-Final";
    private static final int OUT_OF_SEQUENCE_EXIT_CODE = -98765; // Very strange number
    /**
     * Key to retrieve from the HashMap of the behaviour the initiation
     * ACLMessage that triggered this responder session
     */
    public final String INITIATION_KEY = "__Initiation_key" + hashCode();
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
    private final ACLMessage initiation;
    private final String initiationKey;


    /*
     * @deprecated
    public SSResponder(Agent a, ACLMessage initiation, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages, boolean useInitiationKey) {
        super(a);
        setMapMessagesList(mapMessagesList);
        this.initiation = initiation;
        initiationKey = (useInitiationKey ? INITIATION_KEY : RECEIVED_KEY);

        registerDefaultTransition(RECEIVE_NEXT, CHECK_IN_SEQ);
        registerTransition(CHECK_IN_SEQ, HANDLE_OUT_OF_SEQUENCE, OUT_OF_SEQUENCE_EXIT_CODE);
        registerDefaultTransition(HANDLE_OUT_OF_SEQUENCE, RECEIVE_NEXT, new String[]{HANDLE_OUT_OF_SEQUENCE});
        registerDefaultTransition(SEND_REPLY, DUMMY_FINAL);


        Behaviour b;

        // RECEIVE_NEXT
        b = new NextMsgReceiver(myAgent, getMapMessagesList(), RECEIVED_KEY);
        registerState(b, RECEIVE_NEXT);

        // CHECK_IN_SEQ
        b = new SeqChecker(myAgent);
        registerDSState(b, CHECK_IN_SEQ);

        // HANDLE_OUT_OF_SEQUENCE
        b = new OutOfSeqHandler(myAgent);
        registerDSState(b, HANDLE_OUT_OF_SEQUENCE);

        // SEND_REPLY
        b = new NextReplySender(myAgent, REPLY_KEY, initiationKey, mapMessagesList, mapMessages);
        registerDSState(b, SEND_REPLY);

        // DUMMY_FINAL
        b = new DummyFinal(myAgent);
        registerLastState(b, DUMMY_FINAL);
        b.setMapMessagesList(getMapMessagesList());
    }
     */

    /**
     *
     */
    public SSResponder(Agent a, ACLMessage initiation, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages, boolean useInitiationKey) {
        super(a);
        setMapMessagesList(mapMessagesList);
        setMapMessages(mapMessages);
        this.initiation = initiation;
        initiationKey = (useInitiationKey ? INITIATION_KEY : RECEIVED_KEY);

        registerDefaultTransition(RECEIVE_NEXT, CHECK_IN_SEQ);
        registerTransition(CHECK_IN_SEQ, HANDLE_OUT_OF_SEQUENCE, OUT_OF_SEQUENCE_EXIT_CODE);
        registerDefaultTransition(HANDLE_OUT_OF_SEQUENCE, RECEIVE_NEXT, new String[]{HANDLE_OUT_OF_SEQUENCE});
        registerDefaultTransition(SEND_REPLY, DUMMY_FINAL);


        Behaviour b;

        // RECEIVE_NEXT
        b = new NextMsgReceiver(myAgent, mapMessagesList, mapMessages, RECEIVED_KEY);
        registerState(b, RECEIVE_NEXT);

        // CHECK_IN_SEQ
        b = new SeqChecker(myAgent);
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerDSState(b, CHECK_IN_SEQ);

        // HANDLE_OUT_OF_SEQUENCE
        b = new OutOfSeqHandler(myAgent);
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerDSState(b, HANDLE_OUT_OF_SEQUENCE);

        // SEND_REPLY
        b = new NextReplySender(myAgent, REPLY_KEY, initiationKey, mapMessagesList, mapMessages);
        registerDSState(b, SEND_REPLY);

        // DUMMY_FINAL
        b = new DummyFinal(myAgent);
        registerLastState(b, DUMMY_FINAL);
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
    }

    public void onStart() {
        getMapMessages().put(initiationKey, initiation);
        super.onStart();
    }
    //#APIDOC_EXCLUDE_END


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
     * This method allows to register a user defined  Behaviour  
     * in the HANDLE_OUT_OF_SEQ state.
     * This behaviour would override the homonymous method.
     * This method also sets the
     * data store of the registered  Behaviour   to the
     * HashMap of this current behaviour.
     * The registered behaviour can retrieve
     * the  out of sequence   ACLMessage object received
     * from the HashMap at the  RECEIVED_KEY  
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
        reinit();
        super.reset();
    }

    /**
     * Re-initialize the internal state without performing a complete reset.
     */
    protected void reinit() {
        var ds = getMapMessagesList();
        ds.remove(RECEIVED_KEY);
        ds.remove(REPLY_KEY);

        setMessageToReplyKey(initiationKey);
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * Check whether a received message complies with the protocol rules.
     */
    protected boolean checkInSequence(ACLMessage received) {
        return false;
    }

    /**
     * This method can be redefined by protocol specific implementations
     * to customize a reply that is going to be sent back to the initiator.
     * This default implementation does nothing.
     */
    protected void beforeReply(ACLMessage reply) {
    }

    /**
     * This method can be redefined by protocol specific implementations
     * to update the status of the protocol just after a reply has been sent.
     * This default implementation does nothing.
     */
    protected void afterReply(ACLMessage reply) {
    }

    /**
     * This method can be redefined by protocol specific implementations
     * to take proper actions after the completion of the current protocol
     * session.
     */
    protected void sessionTerminated() {
    }

    /**
     * Utility method to register a behaviour in a state of the
     * protocol and set the HashMap appropriately
     */
    protected void registerDSState(Behaviour b, String name) {
        b.setMapMessagesList(getMapMessagesList());
        registerState(b, name);
    }
    //#APIDOC_EXCLUDE_END

    private void setMessageToReplyKey(String key) {
        ReplySender rs = (ReplySender) getState(SEND_REPLY);
        rs.setMsgKey(key);
    }


    /**
     * Inner class NextMsgReceiver
     */
    private static class NextMsgReceiver extends MsgReceiver {
        @Serial
        private static final long serialVersionUID = 4487495895818001L;

        public NextMsgReceiver(Agent a, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages, String key) {
            super(a, null, INFINITE, mapMessagesList, mapMessages, key);
        }

        public int onEnd() {
            // The next reply (if any) will be a reply to the received message
            SSResponder parent = (SSResponder) getParent();
            parent.setMessageToReplyKey(receivedMsgKey);

            return super.onEnd();
        }
    } // End of inner class NextMsgReceiver


    /**
     * Inner class SeqChecker
     */
    private static class SeqChecker extends OneShotBehaviour {
        @Serial
        private static final long serialVersionUID = 4487495895818002L;
        private int ret;

        public SeqChecker(Agent a) {
            super(a);
        }

        public void action() {
            SSResponder parent = (SSResponder) getParent();
            ACLMessage received = getMapMessages().get(parent.RECEIVED_KEY);
            if (received != null && parent.checkInSequence(received)) {
                ret = received.getPerformative();
            } else {
                ret = OUT_OF_SEQUENCE_EXIT_CODE;
            }
        }

        public int onEnd() {
            return ret;
        }
    } // End of inner class SeqChecker


    /**
     * Inner class OutOfSeqHandler
     */
    private static class OutOfSeqHandler extends OneShotBehaviour {
        @Serial
        private static final long serialVersionUID = 4487495895818003L;

        public OutOfSeqHandler(Agent a) {
            super(a);
        }

        public void action() {
            SSResponder parent = (SSResponder) getParent();
            parent.handleOutOfSequence(getMapMessages().get(parent.RECEIVED_KEY));
        }
    } // End of inner class OutOfSeqHandler


    /**
     * Inner class NextReplySender
     */
    private static class NextReplySender extends ReplySender {
        @Serial
        private static final long serialVersionUID = 4487495895818004L;

        public NextReplySender(Agent a, String replyKey, String msgKey, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
            super(a, replyKey, msgKey, mapMessagesList, mapMessages);
        }

        public void onStart() {
            SSResponder parent = (SSResponder) getParent();
            ACLMessage reply = getMapMessages().get(parent.REPLY_KEY);
            parent.beforeReply(reply);
        }

        public int onEnd() {
            int ret = super.onEnd();
            SSResponder parent = (SSResponder) getParent();

            // If a reply was sent back, adjust the template and deadline of the
            // RECEIVE_NEXT state
            ACLMessage reply = getMapMessages().get(parent.REPLY_KEY);
            if (reply != null) {
                MsgReceiver mr = (MsgReceiver) parent.getState(RECEIVE_NEXT);
                mr.setTemplate(createNextMsgTemplate(reply));

                Date d = reply.getReplyByDate();
                if (d != null && d.getTime() > System.currentTimeMillis()) {
                    mr.setDeadline(d.getTime());
                } else {
                    mr.setDeadline(MsgReceiver.INFINITE);
                }
            }

            parent.afterReply(reply);
            return ret;
        }

        private MessageTemplate createNextMsgTemplate(ACLMessage reply) {
            return MessageTemplate.and(
                    MessageTemplate.MatchConversationId(reply.getConversationId()),
                    MessageTemplate.not(MessageTemplate.MatchCustom(reply, true)));
        }
    } // End of inner class NextReplySender


    /**
     * Inner class DummyFinal
     */
    private static class DummyFinal extends OneShotBehaviour {
        @Serial
        private static final long serialVersionUID = 4487495895818005L;

        public DummyFinal(Agent a) {
            super(a);
        }

        public void action() {
            SSResponder parent = (SSResponder) getParent();
            parent.sessionTerminated();
        }
    } // End of inner class DummyFinal
}
