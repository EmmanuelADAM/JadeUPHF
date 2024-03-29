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

package jade.core.messaging;

import jade.core.AID;
import jade.core.ServiceException;
import jade.core.ServiceHelper;

/**
 * The TopicManagementHelper provides methods that allows creating topic objects and registering/deregistering
 * to topics.
 * Topics are represented by means of  AID   objects so that they can be used as receivers
 * of ACLMessages. In this way sending a message to an agent or sending a message about a topic is
 * completely uniform.
 *
 * @author Giovanni Caire - TILAB
 */
public interface TopicManagementHelper extends ServiceHelper {
    /**
     * This constant represents the name of the Topic Management Service and must be specified
     * in the  getHelper()   method of the  Agent   class to retrieve the helper
     * of the local TopicManagementService.
     */
    String SERVICE_NAME = "jade.core.messaging.TopicManagement";
    String TOPIC_SUFFIX = "TOPIC_";
    String TOPIC_TEMPLATE_WILDCARD = "*";

    /**
     * Create a topic with a given name.
     *
     * @param topicName The name of the topic to be created
     * @return The  AID   object representing the created topic
     */
    AID createTopic(String topicName);

    /**
     * Checks if an  AID   represents a topic
     *
     * @param id The  AID   to be checked
     * @return  true   if the given  AID   represents a topic.  false   otherwise
     */
    boolean isTopic(AID id);

    /**
     * Register the agent associated to this helper to a given topic
     *
     * @param topic The topic to register to
     * @throws ServiceException If some error occurs during the registration
     */
    void register(AID topic) throws ServiceException;

    /**
     * Register a given AID to a given topic. Registering a specific AID
     * instead of the agent AID, allows registering an Alias or a Virtual agent AID
     *
     * @param aid   The AID that is going to be registered
     * @param topic The topic to register to
     * @throws ServiceException If some error occurs during the registration
     */
    void register(AID aid, AID topic) throws ServiceException;

    /**
     * De-register the agent associated to this helper from a given topic
     *
     * @param topic The topic to de-register from
     * @throws ServiceException If some error occurs during the de-registration
     */
    void deregister(AID topic) throws ServiceException;

    /**
     * De-register a given AID from a given topic
     *
     * @param aid   The AID that is going to be de-registered
     * @param topic The topic to de-register from
     * @throws ServiceException If some error occurs during the de-registration
     */
    void deregister(AID aid, AID topic) throws ServiceException;
}
