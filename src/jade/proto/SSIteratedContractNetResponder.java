package jade.proto;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.List;

public class SSIteratedContractNetResponder extends SSContractNetResponder {

    public SSIteratedContractNetResponder(Agent a, ACLMessage cfp) {
        this(a, cfp, new HashMap<>(), new HashMap<>());
    }

    /**
     * Construct a SSIteratedContractNetResponder that is activated
     * by the reception of a given initiation CFP message and uses
     * a given HashMap of messages list.
     *
     * @deprecated public SSIteratedContractNetResponder(Agent a, ACLMessage cfp, HashMap<String, List<ACLMessage>> mapMessagesList) {
    super(a, cfp, mapMessagesList);

    registerTransition(CHECK_IN_SEQ, HANDLE_CFP, ACLMessage.CFP, new String[]{HANDLE_CFP, SEND_REPLY, RECEIVE_NEXT});
    }
     */
    /**
     * Construct a SSIteratedContractNetResponder that is activated
     * by the reception of a given initiation CFP message and uses
     * a given HashMap of messages list and a given HashMap of messages
     */
    public SSIteratedContractNetResponder(Agent a, ACLMessage cfp, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a, cfp, mapMessagesList, mapMessages);

        registerTransition(CHECK_IN_SEQ, HANDLE_CFP, ACLMessage.CFP, new String[]{HANDLE_CFP, SEND_REPLY, RECEIVE_NEXT});
    }

    protected boolean checkInSequence(ACLMessage received) {
        if (received.getPerformative() == ACLMessage.CFP) {
            // New iteration --> Move the received message to the CFP_KEY and return true
            getMapMessages().put(this.CFP_KEY, received);
            return true;
        } else {
            return super.checkInSequence(received);
        }
    }

    protected void beforeReply(ACLMessage reply) {
        ACLMessage lastReceivedMsg = getMapMessages().get(RECEIVED_KEY);
        if (lastReceivedMsg != null && lastReceivedMsg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
            // We are sending the reply to the ACCEPT_PROPOSAL --> Jump out and terminate just after sending this reply
            forceTransitionTo(DUMMY_FINAL);
        }
    }

    protected void afterReply(ACLMessage reply) {
    }
}
