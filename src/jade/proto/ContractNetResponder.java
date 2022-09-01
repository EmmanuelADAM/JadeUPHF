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

import jade.core.Agent;
import jade.core.CaseInsensitiveString;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;

import java.util.HashMap;
import java.util.List;

/**
 * Behaviour class for  fipa-contract-net  
 * <em>Responder</em> role. This  behaviour implements the
 *  fipa-contract-net   interaction protocol from the point
 * of view of a responder to a call for proposal ( cfp  )
 * message.<p>
 * The API of this class is similar and homogeneous to the
 *  AchieveREResponder  .
 * <p>
 * Read also the introduction to
 * <a href="ContractNetInitiator.html">ContractNetInitiator</a>
 * for a description of the protocol.
 * <p>
 * When a message arrives
 * that matches the message template passed to the constructor,
 * the callback method  prepareResponse   is executed
 * that must return the wished response, for instance the  PROPOSE  
 * reply message. Any other type of returned communicative act
 * is sent and then closes the
 * protocol.
 * <p>
 * Then, if the initiator accepted the proposal, i.e. if
 * an  ACCEPT-PROPOSAL   message was received, the callback
 * method  prepareResultNotification   would be executed that
 * must return the message with the result notification, i.e.
 *  INFORM   or  FAILURE  .
 * <br>
 * In alternative, if the initiator rejected the proposal, i.e. if
 * an  REJECT-PROPOSAL   message was received, the callback
 * method  handleRejectProposal   would be executed and
 * the protocol terminated.
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
 * @author Fabio Bellifemine - TILAB
 * @author Giovanni Caire - TILAB
 * @author Marco Monticone - TILAB
 * @version $Date: 2006-05-25 15:29:42 +0200 (gio, 25 mag 2006) $ $Revision: 5884 $
 * @see ContractNetInitiator
 * @see AchieveREResponder
 */

public class ContractNetResponder extends SSContractNetResponder {
    public static final String RECEIVE_CFP = "Receive-Cfp";
    /**
     * @deprecated Use  REPLY_KEY  
     */
    public final String RESPONSE_KEY = REPLY_KEY;
    /**
     * @deprecated Use either  ACCEPT_PROPOSAL_KEY   or
     *  REJECT_PROPOSAL_KEY   according to the message
     * that has been received
     */
    public final String PROPOSE_ACCEPTANCE_KEY = RECEIVED_KEY;
    /**
     * @deprecated Use  REPLY_KEY  
     */
    public final String RESULT_NOTIFICATION_KEY = REPLY_KEY;

    /**
     * Constructor of the behaviour that creates a new empty HashMap
     *
     * @see #ContractNetResponder(Agent a, MessageTemplate mt, HashMap mapmessageslist, HashMap mapmessage)
     **/
    public ContractNetResponder(Agent a, MessageTemplate mt) {
        this(a, mt, new HashMap<>(), new HashMap<>());
    }


    /**
     * Constructor of the behaviour.
     *
     * @param a               is the reference to the Agent object
     * @param mt              is the MessageTemplate that must be used to match
     *                        the initiator message. Take care that
     *                        if mt is null every message is consumed by this protocol.
     *                        The best practice is to have a MessageTemplate that matches
     *                        the protocol slot; the static method  createMessageTemplate  
     *                        might be usefull.
     * @param mapMessagesList the HashMap of messages list for this protocol behaviour
     * @param mapMessages     the HashMap of messages  for this protocol behaviour
     **/
    public ContractNetResponder(Agent a, MessageTemplate mt, HashMap<String, List<ACLMessage>> mapMessagesList, HashMap<String, ACLMessage> mapMessages) {
        super(a, null, mapMessagesList, mapMessages);

        Behaviour b;

        // RECEIVE_CFP
        b = new MsgReceiver(myAgent, mt, -1, getMapMessagesList(), getMapMessages(), CFP_KEY);
        registerFirstState(b, RECEIVE_CFP);

        // The DUMMY_FINAL state must no longer be final
        b = deregisterState(DUMMY_FINAL);
        b.setMapMessagesList(mapMessagesList);
        b.setMapMessages(mapMessages);
        registerDSState(b, DUMMY_FINAL);

        registerDefaultTransition(RECEIVE_CFP, HANDLE_CFP);
        registerDefaultTransition(DUMMY_FINAL, RECEIVE_CFP);
    }

    /**
     * This static method can be used
     * to set the proper message Template (based on the interaction protocol
     * and the performative) to be passed to the constructor of this behaviour.
     *
     * @see FIPANames.InteractionProtocol
     */
    public static MessageTemplate createMessageTemplate(String iprotocol) {
        if (CaseInsensitiveString.equalsIgnoreCase(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET, iprotocol)) {
            return MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET), MessageTemplate.MatchPerformative(ACLMessage.CFP));
        } else if (CaseInsensitiveString.equalsIgnoreCase(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET, iprotocol)) {
            return MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET), MessageTemplate.MatchPerformative(ACLMessage.CFP));
        } else {
            return MessageTemplate.MatchProtocol(iprotocol);
        }
    }

    /**
     * @deprecated Use  handleCfp()   instead
     */
    protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        return null;
    }

    /**
     * @deprecated Use  handleAcceptProposal()   instead.
     */
    protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        return null;
    }

    /**
     * @deprecated Use  registerHandleCfp()   instead.
     */
    public void registerPrepareResponse(Behaviour b) {
        registerHandleCfp(b);
    }


    //#APIDOC_EXCLUDE_BEGIN

    /**
     * @deprecated Use  registerHandleAcceptProposal()   instead.
     */
    public void registerPrepareResultNotification(Behaviour b) {
        registerHandleAcceptProposal(b);
    }

    /**
     * Redefine this method to call prepareResponse()
     */
    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
        return prepareResponse(cfp);
    }

    /**
     * Redefine this method to call prepareResultNotification()
     */
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        return prepareResultNotification(cfp, propose, accept);
    }

    /**
     * Redefine this method so that the HANDLE_CFP state is not registered
     * as first state
     */
    public void registerHandleCfp(Behaviour b) {
        registerDSState(b, HANDLE_CFP);
    }
    //#APIDOC_EXCLUDE_END

    protected void sessionTerminated() {
        // Once the current session is terminated reinit the
        // internal state to handle the next one
        reinit();

        // Be sure all children can be correctly re-executed
        resetChildren();
    }
}
