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

package jade.wrapper;

import jade.core.Location;
import jade.util.Event;

/**
 * This interface is a Proxy, allowing access to a JADE agent.
 * Invoking methods on instances of this class, it is possible to
 * trigger state transition of the agent life cycle.  This class must
 * not be instantiated by applications. Instead, use the
 * <code>createAgent()</code> method in class
 * <code>AgentContainer</code>.
 * <br>
 * <b>NOT available in MIDP</b>
 * <br>
 *
 * @author Giovanni Rimassa - Universita' di Parma
 * @see AgentContainer#createNewAgent(String, String, Object[])
 */
public interface AgentController {

    /**
     * Constant representing an asynchronous rendez-vous policy.
     */
    boolean ASYNC = false;
    /**
     * Constant representing a synchronous rendez-vous policy.
     */
    boolean SYNC = true;

    /**
     * Get the platforms name of the agent.
     * This name would be what the platform would use to uniquely reference this agent.
     *
     * @return The agents name.
     */
    String getName() throws StaleProxyException;

    /**
     * Triggers a state transition from <b>INITIATED</b> to
     * <b>ACTIVE</b>. This call also starts the internal agent
     * thread. If this call is performed on an already started agent,
     * nothing happens.
     *
     * @throws StaleProxyException If the underlying agent is dead or
     *                             gone.
     */
    void start() throws StaleProxyException;

    /**
     * Triggers a state transition from <b>ACTIVE</b> to
     * <b>SUSPENDED</b>.
     *
     * @throws StaleProxyException If the underlying agent is dead or
     *                             gone.
     */
    void suspend() throws StaleProxyException;

    /**
     * Triggers a state transition from <b>SUSPENDED</b> to
     * <b>ACTIVE</b>.
     *
     * @throws StaleProxyException If the underlying agent is dead or
     *                             gone.
     */
    void activate() throws StaleProxyException;

    /**
     * Triggers a state transition from <b>ACTIVE</b> to
     * <b>DELETED</b>. This call also stops the internal agent thread
     * and fully terminates the agent. If this call is performed on an
     * already terminated agent, nothing happens.
     *
     * @throws StaleProxyException If the underlying agent is dead or
     *                             gone.
     */
    void kill() throws StaleProxyException;

    /**
     * Triggers a state transition from <b>ACTIVE</b> to
     * <b>TRANSIT</b>. This call also moves the agent code and data to
     * another container. This calls terminates the locally running
     * agent, so that this proxy object becomes detached from the moved
     * agent that keeps on executing elsewhere (i.e., no proxy
     * remotization is performed).
     *
     * @param where A <code>Location</code> object, representing the
     *              container the agent should move to.
     * @throws StaleProxyException If the underlying agent is dead or
     *                             gone.
     */
    void move(Location where) throws StaleProxyException;

    /**
     * Clones the current agent. Calling this method does not really
     * trigger a state transition in the current agent
     * lifecycle. Rather, it creates another agent on the given
     * location, that is just a copy of this agent.
     *
     * @param where   The <code>Location</code> object, representing the
     *                container where the new agent copy will start.
     * @param newName The new nickname to give to the copy.
     * @throws StaleProxyException If the underlying agent is dead or
     *                             gone.
     */
    void clone(Location where, String newName) throws StaleProxyException;

    /**
     * Passes an application-specific object to a local agent, created
     * using JADE In-Process Interface. The object will be put into an
     * internal agent queue, from where it can be picked using the
     * <code>jade.core.Agent.getO2AObject()</code> method. The agent
     * must first declare its will to accept passed objects, using the
     * <code>jade.core.Agent.setEnabledO2ACommunication()</code> method.
     *
     * @param o        The object to put in the private agent queue.
     * @param blocking A flag, stating the desired rendez-vous policy;
     *                 it can be <code>ASYNC</code>, for a non-blocking call, returning
     *                 right after putting the object in the quque, or
     *                 <code>SYNC</code>, for a blocking call that does not return until
     *                 the agent picks the object from the private queue.
     * @see jade.core.Agent#getO2AObject()
     * @see jade.core.Agent#setEnabledO2ACommunication(boolean enabled, int queueSize)
     */
    void putO2AObject(Event o, boolean blocking) throws StaleProxyException;

    //#J2ME_EXCLUDE_BEGIN

    /**
     * Retrieve an O2A (Object-to-Agent) interface to interact with the controlled
     * agent.
     *
     * @param theInterface The O2A interface that must be retrieved
     * @return An implementation of the indicated O2A interface
     * @throws StaleProxyException If the underlying agent is dead or gone.
     */
    <T> T getO2AInterface(Class<T> theInterface) throws StaleProxyException;
    //#J2ME_EXCLUDE_END

    /**
     * Read current agent state. This method can be used to query an
     * agent for its state from the outside.
     *
     * @return the Agent Platform Life Cycle state this agent is currently in.
     */
    State getState() throws StaleProxyException;

}