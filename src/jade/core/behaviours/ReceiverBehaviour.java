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

package jade.core.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * Behaviour for receiving an ACL message. This class encapsulates a
 *  receive()   as an atomic operation. This behaviour
 * terminates when an ACL message is received.
 * The method  getMessage()   allows to get the received message.
 * <p>Otherwise;
 * if provided in the constructor, the {@code BiConsumer<Agent, ACLMessage> fActionMessage}
 * is activated when a message is received : <br/>
 *  <i>add a behaviour that wait intinitely for the next msg, without template</i>
 *
 *     addBehaviour(new ReceiverBehaviour(this, -1, null, (a, m)->{
 *             println(getLocalName() + ", I received " + m.getContent() + " from " + m.getSender());}));
 *
 * </p>
 *
 * @author Giovanni Rimassa - Universita' di Parma $Date: 2003-11-25 09:24:45 +0100 (mar, 25 nov 2003) $ $Revision:
 * 4601 $
 * @author Emmanuel Adam
 * @version 2022/07/13
 * @see SenderBehaviour
 * @see Agent#receive()
 * @see ACLMessage
 */
public final class ReceiverBehaviour extends Behaviour {

    /**
     * @serial
     */
    private final MessageTemplate template;
    /**
     * @serial
     */
    private final MessageFuture future;
    /**
     * @serial
     */
    private final long timeOut;
    /**
     * @serial
     */
    private long timeToWait;
    /**
     * @serial
     */
    private long blockingTime = 0;

    /**message to launch when a message corresponding to a template is received*/
    private BiConsumer<Agent, ACLMessage> fActionMessage;

    /**continuous = true means a cyclic behaviour, each message that corresponds to the template are managed.
     * continuous = false to stop after the first message or a delay
     */
    boolean continuous = false;


    // The pattern to match incoming messages against
    /**
     * @serial
     */
    private boolean finished;

    // A future for the ACL message, used when a timeout was specified
    /**
     * This constructor creates a
     *  ReceiverBehaviour   object that ends as soon as an ACL
     * message matching a given  MessageTemplate   arrives or
     * the passed  millis  timeout expires.
     * The received message can then be got via the method
     *  getMessage  .
     *
     * @param a      The agent this behaviour belongs to, and that will
     *                receive()   the message.
     * @param millis The timeout expressed in milliseconds, an infinite timeout
     *               can be expressed by a value < 0.
     * @param mt     A Message template to match incoming messages against, null to
     *               indicate no template and receive any message that arrives.
     */
    public ReceiverBehaviour(Agent a, long millis, MessageTemplate mt) {
        this(a, newHandle(), millis, mt);
    }

    // A time out value, when present
    /**
     * Receive any ACL message, waiting at most  millis
     * milliseconds (infinite time if  millis < 1  ).
     * When calling this constructor, a suitable  Handle
     * must be created and passed to it. When this behaviour ends, some
     * other behaviour will try to get the ACL message out of the
     * handle, and an exception will be thrown in case of a time out.
     * The following example code explains this:
     *
     *  <pre>
     * // ReceiverBehaviour creation, e.g. in agent setup() method
     * h = ReceiverBehaviour.newHandle(); // h is an agent instance variable
     * addBehaviour(new ReceiverBehaviour(this, h, 10000); // Wait 10 seconds
     *
     * ...
     *
     * // Some other behaviour, later, tries to read the ACL message
     * // in its action() method
     * try {
     * ACLMessage msg = h.getMessage();
     * // OK. Message received within timeout.
     * }
     * catch(ReceiverBehaviour.TimedOut rbte) {
     * // Receive timed out
     * }
     * catch(ReceiverBehaviour.NotYetReady rbnyr) {
     * // Message not yet ready, but timeout still active
     * }
     * </pre>
     *
     * @param a      The agent this behaviour belongs to.
     * @param h      An <em>Handle</em> representing the message to receive.
     * @param millis The timeout expressed in milliseconds, an infinite timeout
     *               can be expressed by a value < 0.
     * @see Handle
     * @see ReceiverBehaviour#newHandle()
     */
    public ReceiverBehaviour(Agent a, Handle h, long millis) {
        this(a, h, millis, null);
    }

    /**
     * This constructor creates a
     *  ReceiverBehaviour   object that ends as soon as an ACL
     * message matching a given  MessageTemplate   arrives or
     * the passed  millis  timeout expires.
     * The received message can then be got via the method
     *  getMessage  .
     *
     * @param a      The agent this behaviour belongs to, and that will
     *                receive()   the message.
     * @param millis The timeout expressed in milliseconds, an infinite timeout
     *               can be expressed by a value < 0.
     * @param mt     A Message template to match incoming messages against, null to
     *               indicate no template and receive any message that arrives.
     * @param fActionMessage bi consumer that takes an agent and a message, launch when a message corresponding to the
     *                      template is received
     */
    public ReceiverBehaviour(Agent a, long millis, MessageTemplate mt,
                             BiConsumer<Agent, ACLMessage> fActionMessage) {
        this(a, newHandle(), millis, mt);
        this.fActionMessage = fActionMessage;
    }

    /**
     * This constructor creates a
     *  ReceiverBehaviour   object that ends as soon as an ACL
     * message matching a given  MessageTemplate   arrives or
     * the passed  millis  timeout expires.
     * The received message can then be got via the method
     *  getMessage  .
     *
     * @param a      The agent this behaviour belongs to, and that will
     *                receive()   the message.
     * @param millis The timeout expressed in milliseconds, an infinite timeout
     *               can be expressed by a value < 0.
     * @param mt     A Message template to match incoming messages against, null to
     *               indicate no template and receive any message that arrives.
     * @param continuous false to stop after the first message, true to stay and deal each message corresponding to
     *                   the template
     * @param fActionMessage bi consumer that takes an agent and a message, launch when a message corresponding to the
     *                      template is received
     *  <pre>
     * // ReceiverBehaviour creation with lambda expression
     * // exemple : cyclic reception from  a topic
     * var topic = AgentServicesTools.generateTopicAID(this, "InfoRadio");
     * final MessageTemplate mt = MessageTemplate.MatchTopic(topic);
     * addBehaviour(new ReceiverBehaviour(this, -1, mt, true, (a,msg)-> {
     * println("received " + msg.getContent() + " from " + topic.getLocalName() + ", sent by " + msg.getSender().getLocalName());}));
     * </pre>
     */
    public ReceiverBehaviour(Agent a, long millis, MessageTemplate mt, boolean continuous,
                             BiConsumer<Agent, ACLMessage> fActionMessage) {
        this(a, millis, mt,fActionMessage);
        this.continuous = continuous;
    }


    // A running counter for calling block(millis) until 'timeOut' milliseconds pass.
    /**
     * Receive any ACL message matching the given template, witing at
     * most  millis   milliseconds (infinite time if
     *  millis < 1  . When calling this constructor, a
     * suitable  Handle   must be created and passed to it.
     *
     * @param a      The agent this behaviour belongs to.
     * @param h      An <em>Handle</em> representing the message to receive.
     * @param millis The maximum amount of time to wait for the message,
     *               in milliseconds (infinite if <0).
     * @param mt     A Message template to match incoming messages against, null to
     *               indicate no template and receive any message that arrives.
     * @see ReceiverBehaviour#ReceiverBehaviour(Agent a, Handle h, long millis)
     */
    public ReceiverBehaviour(Agent a, Handle h, long millis, MessageTemplate mt) {
        super(a);
        future = (MessageFuture) h;
        timeOut = millis;
        timeToWait = timeOut;
        template = mt;
    }

    // Timestamp holder, used when calling block(millis) many times.

    /**
     * Factory method for message handles. This method returns a new
     *  Handle   object, which can be used to retrieve an ACL
     * message out of a  ReceiverBehaviour   object.
     *
     * @return A new  Handle   object.
     * @see Handle
     */
    public static Handle newHandle() {
        return new MessageFuture();
    }

    /**
     * Actual behaviour implementation. This method receives a suitable
     * ACL message and copies it into the message provided by the
     * behaviour creator. It blocks the current behaviour if no suitable
     * message is available.
     */
    public void action() {
        ACLMessage msg;
        if (template == null)
            msg = myAgent.receive();
        else
            msg = myAgent.receive(template);

        if (msg == null) {
            if (timeOut < 0) {
                block();
                finished = false;
            } else {
                long elapsedTime = 0;
                if (blockingTime != 0)
                    elapsedTime = System.currentTimeMillis() - blockingTime;
                timeToWait -= elapsedTime;
                if (timeToWait > 0) {
                    blockingTime = System.currentTimeMillis();
                    // System.out.println("Waiting for " + timeToWait + " ms.");
                    block(timeToWait);
                } else {
                    future.setMessage(msg);
                    finished = true;
                }
            }
        } else {
            future.setMessage(msg);
            if(fActionMessage!=null) fActionMessage.accept(myAgent, msg);
            finished = true;
        }
    }

    /**
     * Checks whether this behaviour ended.
     *
     * @return  true   when an ACL message has been received.
     */
    public boolean done() {
        return finished && !continuous;
    }

    /**
     * Resets this behaviour. This method allows to receive another
     *  ACLMessage   with the same
     *  ReceiverBehaviour   without creating a new object.
     */
    public void reset() {
        finished = false;
        future.reset();
        timeToWait = timeOut;
        blockingTime = 0;
        super.reset();
    }

    /**
     * This method allows the caller to get the received message.
     *
     * @return the received message
     * @throws TimedOut    if the timeout passed in the constructor of this
     *                     class expired before any message (that eventually matched the passed
     *                     message template) arrived
     * @throws NotYetReady if the message is not yet arrived and the
     *                     timeout is not yet expired.
     **/
    public ACLMessage getMessage() throws TimedOut, NotYetReady {
        return future.getMessage();
    }

    /**
     * An interface representing ACL messages due to arrive within a time
     * limit. This interface is used to create a
     *  ReceiverBehaviour   object to receive an ACL message
     * within a user specified time limit. When the user tries to read the
     * message represented by the handle, either gets it or gets an
     * exception.
     *
     * @see ReceiverBehaviour#newHandle()
     * @see ReceiverBehaviour#ReceiverBehaviour(Agent
     * a, Handle h, long millis)
     */
    public interface Handle {

        /**
         * Tries to retrieve the  ACLMessage   object represented
         * by this handle.
         *
         * @return The ACL message, received by the associated
         *  ReceiverBehaviour  , if any.
         * @throws TimedOut    If the associated
         *                      ReceiverBehaviour   did not receive a suitable ACL
         *                     message within the time limit.
         * @throws NotYetReady If the associated
         *                      ReceiverBehaviour   is still waiting for a suitable
         *                     ACL message to arrive.
         * @see ReceiverBehaviour#ReceiverBehaviour(Agent
         * a, Handle h, long millis)
         */
        ACLMessage getMessage() throws TimedOut, NotYetReady;

    }

    /**
     * Exception class for timeouts. This exception is thrown when trying
     * to obtain an  ACLMessage   object from an
     *  Handle  , but no message was received within a specified
     * timeout.
     *
     * @see Handle#getMessage()
     */
    public static class TimedOut extends Exception {
        TimedOut() {
            super("No message was received before time limit.");
        }
    }

    /**
     * Exception class for timeouts. This exception is thrown when trying
     * to obtain an  ACLMessage   from an  Handle
     * and no message was received so far, but the time limit is not yet
     * reached.
     *
     * @see Handle#getMessage()
     */
    public static class NotYetReady extends Exception {
        NotYetReady() {
            super("Requested message is not ready yet.");
        }
    }

    private static class MessageFuture implements Handle, Serializable {

        private static final int OK = 0;
        private static final int NOT_YET = 1;
        private static final int TIMED_OUT = 2;

        private int state = NOT_YET;
        private ACLMessage message;

        public void reset() {
            message = null;
            state = NOT_YET;
        }

        public ACLMessage getMessage() throws TimedOut, NotYetReady {
            return switch (state) {
                case NOT_YET -> throw new NotYetReady();
                case TIMED_OUT -> throw new TimedOut();
                default -> message;
            };
        }

        public void setMessage(ACLMessage msg) {
            message = msg;
            if (message != null)
                state = OK;
            else
                state = TIMED_OUT;
        }
    }
} 
