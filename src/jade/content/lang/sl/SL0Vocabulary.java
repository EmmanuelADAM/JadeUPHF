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
package jade.content.lang.sl;

/**
 * The vocabulary of the simbols used in the FIPA SL0 language
 *
 * @author Giovanni Caire - TILAB
 */
public interface SL0Vocabulary {
    // Aggregate operators
    String SEQUENCE = "sequence";
    String SET = "set";

    // Generic concepts: AID and ACLMessage
    String AID = "agent-identifier";
    String AID_NAME = "name";
    String AID_ADDRESSES = "addresses";
    String AID_RESOLVERS = "resolvers";

    String ACLMSG = "fipa-acl-message";
    String ACLMSG_PERFORMATIVE = "performative";
    String ACLMSG_SENDER = "sender";
    String ACLMSG_RECEIVERS = "receivers";
    String ACLMSG_REPLY_TO = "reply-to";
    String ACLMSG_LANGUAGE = "language";
    String ACLMSG_ONTOLOGY = "ontology";
    String ACLMSG_PROTOCOL = "protocol";
    String ACLMSG_IN_REPLY_TO = "in-reply-to";
    String ACLMSG_REPLY_WITH = "reply-with";
    String ACLMSG_CONVERSATION_ID = "conversation-id";
    String ACLMSG_REPLY_BY = "reply-by";
    String ACLMSG_CONTENT = "content";
    String ACLMSG_BYTE_SEQUENCE_CONTENT = "bs-content";
    String ACLMSG_ENCODING = "encoding";

    // Generic propositions:
    // TRUE_PROP (i.e. the proposition that is true under whatever condition)
    // FALSE_PROP (i.e. the proposition that is false under whatever condition)
    String TRUE_PROPOSITION = "true";
    String FALSE_PROPOSITION = "false";

    // Action operators
    String DONE = "done";
    String DONE_ACTION = "action";
    String DONE_CONDITION = "condition";

    String RESULT = "result";
    String RESULT_ACTION = "action";
    String RESULT_VALUE = "value";
    /**
     * @deprecated Use <code>RESULT_VALUE</code> instead
     */
    String RESULT_ITEMS = RESULT_VALUE;

    String ACTION = "action";
    String ACTION_ACTOR = "actor";
    String ACTION_ACTION = "action";

    String EQUALS = "=";
    String EQUALS_LEFT = "left";
    String EQUALS_RIGHT = "right";
}
