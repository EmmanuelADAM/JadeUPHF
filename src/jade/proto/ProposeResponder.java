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

//#CUSTOM_EXCLUDE_FILE

import jade.core.Agent;
import jade.core.CaseInsensitiveString;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import jade.proto.states.ReplySender;

import java.util.HashMap;
import java.util.List;

/**
 * Behaviour class for  fipa-propose  
 * <em>Responder</em> role. This  behaviour implements the
 *  fipa-propose   interaction protocol from the point
 * of view of a responder to a propose ( propose  )
 * message.<p>
 * The API of this class is similar and homogeneous to the
 *  AchieveREResponder  .
 * <p>
 * When a message arrives
 * that matches the message template passed to the constructor,
 * the callback method  prepareResponse   is executed
 * that must return the wished response, for instance the
 *  ACCEPT_PROPOSAL  
 * reply message. Any other type of returned communicative act
 * is sent and then closes the
 * protocol.
 * <p>
 * If a message were received, with the same value of this
 *  conversation-id  , but that does not comply with the FIPA
 * protocol, than the method  handleOutOfSequence   would be called.
 * <p>
 * This class can be extended by the programmer by overriding all the needed
 * handle methods or, in alternative, appropriate behaviours can be
 * registered for each handle via the  registerHandle  -type
 * of methods. This last case is more difficult to use and proper
 * care must be taken to properly use the  HashMap   of the
 *  Behaviour   as a shared memory mechanism with the
 * registered behaviour.
 * <p>
 *
 * @author Jerome Picault - Motorola Labs
 * @version $Date: 2005-09-16 15:54:46 +0200 (ven, 16 set 2005) $ $Revision: 5780 $
 * @see ProposeInitiator
 * @see AchieveREResponder
 */

public class ProposeResponder extends FSMBehaviour implements FIPANames.InteractionProtocol {

    // FSM states names
    protected static final String RECEIVE_PROPOSE = "Receive-propose";
    protected static final String PREPARE_RESPONSE = "Prepare-response";
    protected static final String SEND_RESPONSE = "Send-response";
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object sent by the initiator.
     **/
    public final String PROPOSE_KEY = "__propose" + hashCode();
    /**
     * key to retrieve from the HashMap of the behaviour the ACLMessage
     * object sent as a response to the initiator.
     **/
    public final String RESPONSE_KEY = "__response" + hashCode();
    // The MsgReceiver behaviour used to receive propose messages
    MsgReceiver rec = null;


    /**
     * Constructor of the behaviour that creates a new empty HashMap
     *
     * @see #ProposeResponder(Agent a, MessageTemplate mt, HashMap, HashMap)
     **/
    public ProposeResponder(Agent a, MessageTemplate mt) {
        this(a, mt, new HashMap<>(), new HashMap<>());
    }

    /**
     * Constructor.
     *
     * @param a               is the reference to the Agent object
     * @param mt              is the MessageTemplate that must be used to match
     *                        the initiator message. Take care that
     *                        if mt is null every message is consumed by this protocol.
     * @param mapMessagesList the HashMap of messages list for this protocol
     * @param mapMessages     the HashMap of message for this protocol
     **/
    public ProposeResponder(Agent a, MessageTemplate mt, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a);

        setMapMessagesList(mapMessagesList);
        setMapMessages(mapMessages);

        // Register the FSM transitions
        registerDefaultTransition(RECEIVE_PROPOSE, PREPARE_RESPONSE);
        registerDefaultTransition(PREPARE_RESPONSE, SEND_RESPONSE);
        registerDefaultTransition(SEND_RESPONSE, RECEIVE_PROPOSE);

        // Create and register the states that make up the FSM
        Behaviour b;

        // RECEIVE_PROPOSE
        rec = new MsgReceiver(myAgent, mt, -1, mapMessagesList, mapMessages, PROPOSE_KEY);
        registerFirstState(rec, RECEIVE_PROPOSE);

        // PREPARE_RESPONSE
        b = new PrepareResponse(myAgent);
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerState(b, PREPARE_RESPONSE);

        // SEND_RESPONSE
        b = new ReplySender(myAgent, RESPONSE_KEY, PROPOSE_KEY, mapMessagesList, mapMessages);
        b.setMapMessagesList(getMapMessagesList());
        registerState(b, SEND_RESPONSE);
    }

    // For persistence service
    private ProposeResponder() {
    }

    /**
     * Constructor.
     *
     * @param a               is the reference to the Agent object
     * @param mt              is the MessageTemplate that must be used to match
     *                        the initiator message. Take care that
     *                        if mt is null every message is consumed by this protocol.
     * @param mapMessagesList the HashMap for this protocol
     * @deprecated public ProposeResponder(Agent a, MessageTemplate mt, HashMap<String, List<ACLMessage>> mapMessagesList) {
    super(a);

    setMapMessagesList(mapMessagesList);

    // Register the FSM transitions
    registerDefaultTransition(RECEIVE_PROPOSE, PREPARE_RESPONSE);
    registerDefaultTransition(PREPARE_RESPONSE, SEND_RESPONSE);
    registerDefaultTransition(SEND_RESPONSE, RECEIVE_PROPOSE);

    // Create and register the states that make up the FSM
    Behaviour b;

    // RECEIVE_PROPOSE
    rec = new MsgReceiver(myAgent, mt, -1, getMapMessagesList(), PROPOSE_KEY);
    registerFirstState(rec, RECEIVE_PROPOSE);

    // PREPARE_RESPONSE
    b = new PrepareResponse(myAgent);
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, PREPARE_RESPONSE);

    // SEND_RESPONSE
    b = new ReplySender(myAgent, RESPONSE_KEY, PROPOSE_KEY);
    b.setMapMessagesList(getMapMessagesList());
    registerState(b, SEND_RESPONSE);
    }
     */

    /**
     * This static method can be used
     * to set the proper message template (based on the interaction protocol
     * and the performative)
     * into the constructor of this behaviour.
     *
     * @see FIPANames.InteractionProtocol
     **/
    public static MessageTemplate createMessageTemplate(String iprotocol) {

        if (CaseInsensitiveString.equalsIgnoreCase(FIPA_PROPOSE, iprotocol))
            return MessageTemplate.and(MessageTemplate.MatchProtocol(FIPA_PROPOSE), MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        else
            return MessageTemplate.MatchProtocol(iprotocol);
    }

    /**
     * Reset this behaviour.
     */
    public void reset() {
        super.reset();
        var ds = getMapMessagesList();
        ds.remove(PROPOSE_KEY);
        ds.remove(RESPONSE_KEY);
    }

    /**
     * This method allows to change the  MessageTemplate  
     * that defines what messages this ProposeResponder will react to
     * and reset the protocol.
     */
    public void reset(MessageTemplate mt) {
        this.reset();
        rec.reset(mt, -1, getMapMessagesList(), getMapMessages(), PROPOSE_KEY);
    }


    /**
     * This method is called when the initiator's
     * message is received that matches the message template
     * passed in the constructor.
     * This default implementation return null which has
     * the effect of sending no reponse. Programmers should
     * override the method in case they need to react to this event.
     *
     * @param propose the received message
     * @return the ACLMessage to be sent as a response (i.e. one of
     *  accept_proposal, reject_proposal, not-understood  .
     * <b>Remind</b> to use the method createReply of the class ACLMessage
     * in order to create a good reply message
     * @see ACLMessage#createReply()
     **/
    protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {
        return null;
    }

    /**
     * This method allows to register a user defined  Behaviour  
     * in the PREPARE_RESPONSE state.
     * This behaviour would override the homonymous method.
     * This method also set the
     * data store of the registered  Behaviour   to the
     * HashMap of this current behaviour.
     * It is responsibility of the registered behaviour to put the
     * response to be sent into the HashMap at the  RESPONSE_KEY  
     * key.
     *
     * @param b the Behaviour that will handle this state
     */
    public void registerPrepareResponse(Behaviour b) {
        registerState(b, PREPARE_RESPONSE);
        b.setMapMessagesList(getMapMessagesList());
    }

    //#MIDP_EXCLUDE_BEGIN

    // Private inner classes for the FSM states
    private static class PrepareResponse extends OneShotBehaviour {

        public PrepareResponse(Agent a) {
            super(a);
        }

        // For persistence service
        private PrepareResponse() {
        }

        public void action() {
            ProposeResponder fsm = (ProposeResponder) getParent();
            var ds = getMapMessages();
            ACLMessage propose = ds.get(fsm.PROPOSE_KEY);

            ACLMessage response;
            try {
                response = fsm.prepareResponse(propose);
            } catch (NotUnderstoodException | RefuseException nue) {
                response = nue.getACLMessage();
            }
            ds.put(fsm.RESPONSE_KEY, response);
        }

    } // End of PrepareResponse class
    //#MIDP_EXCLUDE_END

}
