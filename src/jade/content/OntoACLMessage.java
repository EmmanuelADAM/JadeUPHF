/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * <p>
 * GNU Lesser General Public License
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.Iterator;

/**
 * Utility class that allow using an  ACLMessage   object
 * as an ontological agent action.
 *
 * @author Giovanni Caire - TILAB
 */
public class OntoACLMessage extends ACLMessage implements AgentAction {

    /**
     * Construct an ontological ACL message whose performative
     * is ACLMessage.NOT_UNDERSTOOD
     */
    public OntoACLMessage() {
        super(ACLMessage.NOT_UNDERSTOOD);
    }

    /**
     * Construct an ontological ACL message with a given
     * performative
     *
     * @param performative the performative of this ACL message.
     * @see ACLMessage#ACLMessage(int)
     */
    public OntoACLMessage(int performative) {
        super(performative);
    }

    /**
     * Create an ontological ACL message that wraps an existing
     *  ACLMessage  .
     *
     * @param msg the  ACLMessage  to be wrapped. If
     *             msg
     *            is already an ontological ACL message no new object is
     *            created and  msg   is returned with the sender
     *            and receivers properly wrapped if necessary.
     */
    public static OntoACLMessage wrap(ACLMessage msg) {
        OntoACLMessage wrapper = null;
        if (msg != null) {
            if (msg instanceof OntoACLMessage) {
                wrapper = (OntoACLMessage) msg;
            } else {
                wrapper = new OntoACLMessage(msg.getPerformative());
                // This automatically performs the wrapping
                wrapper.setSender(msg.getSender());
                Iterator<AID> it = msg.getAllReceiver();
                while (it.hasNext()) {
                    // This automatically performs the wrapping
                    wrapper.addReceiver(it.next());
                }

                it = msg.getAllReplyTo();
                while (it.hasNext()) {
                    // This automatically performs the wrapping
                    wrapper.addReplyTo(it.next());
                }

                wrapper.setLanguage(msg.getLanguage());
                wrapper.setOntology(msg.getOntology());
                wrapper.setProtocol(msg.getProtocol());
                wrapper.setInReplyTo(msg.getInReplyTo());
                wrapper.setReplyWith(msg.getReplyWith());
                wrapper.setConversationId(msg.getConversationId());
                wrapper.setReplyByDate(msg.getReplyByDate());
                if (msg.hasByteSequenceContent()) {
                    wrapper.setByteSequenceContent(msg.getByteSequenceContent());
                } else {
                    wrapper.setContent(msg.getContent());
                }
                wrapper.setEncoding(msg.getEncoding());

                //FIXME: Message Envelope is missing
            }
        }
        return wrapper;
    }

    /**
     * This method is redefined so that the sender AID is automatically
     * wrapped into an OntoAID
     */
    public void setSender(AID aid) {
        super.setSender(OntoAID.wrap(aid));
    }

    /**
     * This method is redefined so that the receiver AID is automatically
     * wrapped into an OntoAID
     */
    public void addReceiver(AID aid) {
        super.addReceiver(OntoAID.wrap(aid));
    }

    /**
     * This method is redefined so that the replyTo AID is automatically
     * wrapped into an OntoAID
     */
    public void addReplyTo(AID aid) {
        super.addReplyTo(OntoAID.wrap(aid));
    }

    // FIXME: clone method should be redefined too

}

