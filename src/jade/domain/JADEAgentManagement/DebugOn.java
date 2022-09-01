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


package jade.domain.JADEAgentManagement;

import jade.content.AgentAction;
import jade.core.AID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents the  debug-on   action, requesting
 * a debugger to start observing a set of agents in the platform.
 *
 * @author Giovanni Rimassa -  Universita' di Parma
 * @version $Date: 2003-11-24 14:47:00 +0100 (lun, 24 nov 2003) $ $Revision: 4597 $
 */
public class DebugOn implements AgentAction {

    private final List<AID> debuggedAgents = new ArrayList<>();
    private AID debugger;
    private String password;


    /**
     * Default constructor. A default constructor is necessary for
     * ontological classes.
     */
    public DebugOn() {
    }

    /**
     * Retrieve the value of the  debugger   slot of this
     * action, containing the agent identifier of the debugger agent.
     *
     * @return The value of the  debugger   slot, or
     *  null   if no value was set.
     */
    public AID getDebugger() {
        return debugger;
    }

    /**
     * Set the  debugger   slot of this action.
     *
     * @param id The agent identifier of the debugger agent.
     */
    public void setDebugger(AID id) {
        debugger = id;
    }

    /**
     * Remove all agent identifiers from the
     *  debugged-agents   slot collection of this object.
     */
    public void clearAllDebuggedAgents() {
        debuggedAgents.clear();
    }

    /**
     * Add an agent identifier to the  debugged-agents
     * slot collection of this object.
     *
     * @param id The agent identifier to add to the collection.
     */
    public void addDebuggedAgents(AID id) {
        debuggedAgents.add(id);
    }

    /**
     * Remove an agent identifier from the
     *  debugged-agents   slot collection of this object.
     *
     * @param id The agent identifier to remove from the collection.
     * @return A boolean, telling whether the element was present in
     * the collection or not.
     */
    public boolean removeDebuggedAgents(AID id) {
        return debuggedAgents.remove(id);
    }

    /**
     * Access all agent identifiers from the
     *  debugged-agents   slot collection of this object.
     *
     * @return An iterator over the properties collection.
     */
    public Iterator<AID> getAllDebuggedAgents() {
        return debuggedAgents.iterator();
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * This method is called by the AMS in order to prepare an RMI call.
     * The  getAllDebuggedAgents()   cannot be used as it returns
     * an  Iterator   that is not serializable.
     */
    public ArrayList<AID> getCloneOfDebuggedAgents() {
        return (ArrayList<AID>) ((ArrayList<AID>) debuggedAgents).clone();
    }

    //#APIDOC_EXCLUDE_END

    /**
     * Retrieve the value of the  password   slot of this
     * action, containing the password used to authenticate the
     * principal requesting this action.
     *
     * @return The value of the  password   slot, or
     *  null   if no value was set.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the  password   slot of this action.
     *
     * @param p The password used to authenticate the principal
     *          requesting this action.
     */
    public void setPassword(String p) {
        password = p;
    }

}
