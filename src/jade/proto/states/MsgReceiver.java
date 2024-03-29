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

package jade.proto.states;

//#CUSTOM_EXCLUDE_FILE

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.List;

/**
 * This behaviour is a simple implementation of a message receiver.
 * It puts into the given key of the given HashMap the received message
 * according to the given message template and timeout. All these
 * data must be passed in the constructor.
 * If the timeout expires before any message arrives, the behaviour
 * terminates and put null into the HashMap.
 *
 * @author Tiziana Trucco - TILab
 * @version $Date: 2011-01-19 11:09:42 +0100(mer, 19 gen 2011) $ $Revision: 6386 $
 **/
public class MsgReceiver extends SimpleBehaviour {

    /**
     * A numeric constant to mean that a timeout expired.
     */
    public static final int TIMEOUT_EXPIRED = -1001;

    /**
     * A numeric constant to mean that the receive operation was
     * interrupted.
     */
    public static final int INTERRUPTED = -1002;

    /**
     * A numeric constant to mean that the deadline for the receive
     * operation will never expire.
     */
    public static final int INFINITE = -1;

    protected MessageTemplate template;
    protected long deadline;
    protected String receivedMsgKey;

    private boolean received;
    private boolean expired;
    private boolean interrupted;
    private int ret;

    /**
     * Constructor.
     *
     * @param a               a reference to the Agent
     * @param mt              the MessageTemplate of the message to be received, if null
     *                        the first received message is returned by this behaviour
     * @param deadline        a timeout for waiting until a message arrives. It must
     *                        be expressed as an absolute time, as it would be returned by
     *                         System.currentTimeMillisec()  
     * @param mapMessagesList the HashMap for this bheaviour
     * @param msgKey          the key where the behaviour must put the received  message into the HashMap.
     * @deprecated
     **/
    public MsgReceiver(Agent a, MessageTemplate mt, long deadline, HashMap<String, List<ACLMessage>> mapMessagesList, String msgKey) {
        super(a);
        setMapMessagesList(mapMessagesList);
        template = mt;
        this.deadline = deadline;
        receivedMsgKey = msgKey;
        received = false;
        expired = false;
        interrupted = false;
    }

    /**
     * Constructor.
     *
     * @param a               a reference to the Agent
     * @param mt              the MessageTemplate of the message to be received, if null
     *                        the first received message is returned by this behaviour
     * @param deadline        a timeout for waiting until a message arrives. It must
     *                        be expressed as an absolute time, as it would be returned by
     *                         System.currentTimeMillisec()  
     * @param mapMessagesList the HashMap of messages list for this bheaviour
     * @param mapMessages     the HashMap of messages  for this bheaviour
     * @param msgKey          the key where the beahviour must put the received
     *                        message into the HashMap.
     **/
    public MsgReceiver(Agent a, MessageTemplate mt, long deadline, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages, String msgKey) {
        this(a, mt, deadline, mapMessagesList, msgKey);
        setMapMessages(mapMessages);
    }

    //#APIDOC_EXCLUDE_BEGIN

    // For persistence service
    protected MsgReceiver() {
    }

    public void action() {
        if (interrupted) {
            //TODO: here, verifier le code de la cle pour choisir la bonne hashmap !!
            if (receivedMsgKey != null) {
                var map = getMapMessagesList();
                map.put(receivedMsgKey, null);
            }
            ret = INTERRUPTED;
            return;
        }

        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {

            if (receivedMsgKey != null) {
                var map = getMapMessages();
                map.put(receivedMsgKey, msg);
            }
            received = true;
            ret = msg.getPerformative();
            handleMessage(msg);
        } else {
            if (deadline >= 0) {
                // If a timeout was set, then check if it is expired
                long blockTime = deadline - System.currentTimeMillis();
                if (blockTime <= 0) {
                    //timeout expired
                    if (receivedMsgKey != null) {
                        var map = getMapMessages();
                        map.put(receivedMsgKey, null);
                    }
                    expired = true;
                    ret = TIMEOUT_EXPIRED;
                    handleMessage(null);
                } else {
                    block(blockTime);
                }
            } else {
                block();
            }
        }
    }

    public boolean done() {
        return received || expired || interrupted;
    }

    /**
     * @return the performative if a message arrived,
     *  TIMEOUT_EXPIRED   if the timeout expired or
     *  INTERRUPTED   if this  MsgReceiver  
     * was interrupted calling the  interrupt()   method.
     **/
    public int onEnd() {
        received = false;
        expired = false;
        interrupted = false;
        return ret;
    }
    //#APIDOC_EXCLUDE_END

    /**
     * This is invoked when a message matching the specified template
     * is received or the timeout has expired (the  msg  
     * parameter is null in this case). Users may redefine this method
     * to react to this event. The default implementation of does nothing.
     */
    protected void handleMessage(ACLMessage msg) {
    }

    /**
     * Reset this behaviour, possibly replacing the receive templatt
     * and other data.
     *
     * @param mt              The template to match ACL messages against during the
     *                        receive operation.
     * @param deadline        The relative timeout of the receive
     *                        operation. If the  INFINITE   constant is used, then
     *                        no deadline is set and the operation will wait until a matching
     *                        ACL message arrives.
     * @param mapMessagesList The HashMap where the received ACL message is to be put.
     * @param msgKey          The key to use to put the received message into the selected HashMap.
     * @deprecated
     */
    public void reset(MessageTemplate mt, long deadline, HashMap<String, List<ACLMessage>> mapMessagesList, String msgKey) {
        super.reset();
        received = false;
        expired = false;
        interrupted = false;
        setTemplate(mt);
        setDeadline(deadline);
        setMapMessagesList(mapMessagesList);
        setReceivedKey(msgKey);
    }


    /**
     * Reset this behaviour, possibly replacing the receive templatt
     * and other data.
     *
     * @param mt              The template to match ACL messages against during the
     *                        receive operation.
     * @param deadline        The relative timeout of the receive
     *                        operation. If the  INFINITE   constant is used, then
     *                        no deadline is set and the operation will wait until a matching
     *                        ACL message arrives.
     * @param mapMessagesList The HashMap where the received ACL message list is to be put.
     * @param mapMessages     The HashMap where the received ACL message is to be put.
     * @param msgKey          The key to use to put the received message into the selected HashMap.
     */
    public void reset(MessageTemplate mt, long deadline, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages, String msgKey) {
        super.reset();
        received = false;
        expired = false;
        interrupted = false;
        setTemplate(mt);
        setDeadline(deadline);
        setMapMessagesList(mapMessagesList);
        setMapMessages(mapMessages);
        setReceivedKey(msgKey);
    }

    /**
     * This method allows modifying the deadline
     **/
    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    /**
     * This method allows modifying the template
     **/
    public void setTemplate(MessageTemplate mt) {
        template = mt;
    }

    /**
     * This method allows modifying the key in the DS where to put the
     * received message
     **/
    public void setReceivedKey(String key) {
        receivedMsgKey = key;
    }

    /**
     * Signal an interruption to this receiver, and cause the ongoing
     * receive operation to abort.
     */
    public void interrupt() {
        interrupted = true;
        restart();
    }
}

