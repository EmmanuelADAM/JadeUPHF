package jade.core;

import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * tools to help the use of the Directory Facilitator (registration to a
 * service, find agents that have declared a service)
 *
 * @author revised by Emmanuel ADAM
 */
public final class AgentServicesTools {

    /**
     * create an Agent Description (model of a service)
     *
     * @param typeService type of the service
     * @param nameService name of the service (can  be null)
     * @return the model of the service
     */
    public static DFAgentDescription createAgentDescription(final String typeService, final String nameService) {
        var model = new DFAgentDescription();
        var service = new ServiceDescription();
        service.setType(typeService);
        service.setName(nameService);
        model.addServices(service);
        return model;
    }

    /**@return the AID relative to the topic topicName for manage 'radio', broadcast message
     * @param agent the agent that create or retrieve the topic
     * @param topicName the topic name*/
    public static AID generateTopicAID(Agent agent, String topicName)
    {
        AID topic=null;
        TopicManagementHelper topicHelper;
        try {
            topicHelper = (TopicManagementHelper) agent.getHelper(TopicManagementHelper.SERVICE_NAME);
            topic = topicHelper.createTopic(topicName);
            topicHelper.register(topic);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return topic;
    }


    /**
     * deregister an agent from a service with the Directory Facilitator
     *
     * @param myAgent     agent that have to be registered
     * @param model       description of the service to deregister for this agent
     */
    public synchronized static void deregisterService(final Agent myAgent, final DFAgentDescription model) {
        if(model!=null){
            try { DFService.deregister(myAgent, model); }
            catch (FIPAException fe) { fe.printStackTrace();}
        }
    }

    /**
     * deregister an agent from a list of services with the Directory Facilitator
     *
     * @param myAgent     agent that have to be registered
     * @param models      List of description of the service to deregister for this agent
     */
    public synchronized static void deregisterServices(final Agent myAgent, final Collection<DFAgentDescription> models) {
            models.forEach(model-> {
                try { DFService.deregister(myAgent, model);}
                catch (FIPAException e) { e.printStackTrace();}
            });
    }

    /**
     * deregister an agent from all its services with the Directory Facilitator
     *
     * @param myAgent     agent that have to be registered
     */
    public synchronized static void deregisterAll(final Agent myAgent) {
        try { DFService.deregister(myAgent); }
        catch (FIPAException fe) { fe.printStackTrace();}
    }

    /**
     * register an agent to a service with the Directory Facilitator
     *
     * @param myAgent     agent that have to be registered
     * @param typeService type of the service
     * @param nameService name of the service (could be null but it is prefered to use a
     *                    name (like the agent name)
     */
    public synchronized static DFAgentDescription register(final Agent myAgent, final String typeService, final String nameService) {
        var model = createAgentDescription(typeService, nameService);
        try {
            DFService.register(myAgent, model);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return model;
    }

    /***
     * search agents that are registered to a typical service
     *
     * @param myAgent
     *            agent that asks for the search (it will be omitted from the
     *            result)
     * @param typeService
     *            type of the service
     * @param nameService
     *            name of the service (can be null)
     * @return AIDs of the agents that are registered to (_typeService, _nameService),
     *			do not include the AID of myAgent
     */
    public synchronized static AID[] searchAgents(final Agent myAgent, final String typeService, final String nameService) {
        var model = createAgentDescription(typeService, nameService);
        AID[] result = null;
        try {
            DFAgentDescription[] agentsDescription = DFService.search(myAgent, model);
            ArrayList<DFAgentDescription> list = new ArrayList<>(Arrays.asList(agentsDescription));
            AID myAID = myAgent.getAID();
            list.removeIf(e -> e.getName().equals(myAID));
            result = list.stream().map(DFAgentDescription::getName).toArray(AID[]::new);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return result;
    }

}