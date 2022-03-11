/* ***************************************************************
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


package jade.tools.sniffer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

  /*
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma, E.ADAM (2021)
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2021-06-02 11:00:36 +0200  $ $Revision: 2021-1 $
   before : Date: 2001-10-09 17:15:36 +0200 (mar, 09 ott 2001) $ $Revision: 2768

  */

/**
 * The List for the agents on the Agent Canvas. Implements Serializable for saving
 * data to the binary snapshot file.
 */


public class AgentList implements Serializable {

    public List<Agent> agents;
    public List<String> agentNames;

    /**
     * Default constructor for the class <em>AgentList</em>
     */
    public AgentList() {
        agents = new ArrayList<>(50);
        String n = "";

        /* First we put a dummy agent called "Other" */
        agents.add(new Agent());

    }

    /**
     * Add an agent to the list.
     *
     * @param agent the agent to add
     */
    public void addAgent(Agent agent) {
        agents.add(agent);
    }

    /**
     * Removes an agent from the list
     *
     * @param agentName name of the agent to remove
     */
    public void removeAgent(String agentName) {
        agents.removeIf(o -> o.equals(agentName) && o.onCanv);
    }

    /**
     * Clears the agent list
     */
    public void removeAllAgents() {
        agents.clear();
    }

    /**
     * Verifies if an agent is present on the canvas
     *
     * @param agName name of the agent to check for
     */
    public boolean isPresent(String agName) {
        return agents.stream().anyMatch(a -> a.equals(agName));
    }


    /**
     * Gives back the position inside the agents
     *
     * @param agName name of the agent for its position to search
     */
    public int getPos(String agName) {
        int i = 0;
        boolean found = false;
        int nb = agents.size();
        while (i < nb && !found) {
            found = agents.get(i).equals(agName);
            if (!found) i++;
        }
        return i;
    }

    public Iterator<Agent> getAgents() {
        return agents.iterator();
    }

    public int size() {
        return agents.size();
    }

}  // End class AgentList
