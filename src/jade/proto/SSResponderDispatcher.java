package jade.proto;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ConversationList;
import jade.lang.acl.MessageTemplate;

import java.io.Serial;

/**
 * This behaviour is designed to be used together with the Single-Session responder protocol classes.
 * More in details it is aimed at dealing with protocol initiation messages and dispatching them to
 * responders. The latter are created by means of the <code>createResponder()</code> abstract method
 * that developers must implement.
 *
 * @author Giovanni Caire
 * @see SSContractNetResponder
 * see SSIteratedContractNetResponder
 * see SSIteratedAchieveREtResponder
 */
public abstract class SSResponderDispatcher extends CyclicBehaviour {
    private final ConversationList activeConversations;
    private final MessageTemplate template;

    public SSResponderDispatcher(Agent a, MessageTemplate tpl) {
        super(a);
        activeConversations = new ConversationList(a);
        template = MessageTemplate.and(
                tpl,
                activeConversations.getMessageTemplate());
    }

    public final void action() {
        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {
            // Be sure a conversation-id is set. If not create a suitable one
            if (msg.getConversationId() == null) {
                msg.setConversationId(createConversationId(myAgent.getLocalName()));
            }
            final String convId = msg.getConversationId();
            Behaviour ssResponder = createResponder(msg);
            if (ssResponder != null) {
                activeConversations.registerConversation(convId);
                SequentialBehaviour sb = new SequentialBehaviour() {
                    @Serial
                    private static final long serialVersionUID = 12345678L;

                    public int onEnd() {
                        activeConversations.deregisterConversation(convId);
                        return super.onEnd();
                    }
                };
                sb.setBehaviourName(convId + "-Responder");
                sb.addSubBehaviour(ssResponder);
                addBehaviour(sb);
            }
        } else {
            block();
        }
    }

    /**
     * This method is responsible for creating a suitable <code>Behaviour</code> acting as responder
     * in the interaction protocol initiated by message <code>initiationMsg</code>.
     *
     * @param initiationMsg The message initiating the interaction protocol
     * @return the respondeur behaviour
     */
    protected abstract Behaviour createResponder(ACLMessage initiationMsg);

    protected void addBehaviour(Behaviour b) {
        myAgent.addBehaviour(b);
    }

    private static long cnt = 0;

    private synchronized static String createConversationId(String name) {
        return "C-" + name + '-' + System.currentTimeMillis() + '-' + (cnt++);
    }
}
