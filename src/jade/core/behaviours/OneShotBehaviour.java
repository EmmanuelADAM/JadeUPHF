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

import java.util.function.Consumer;

/**
 * Atomic behaviour that executes just once. This class can
 * be extended by application programmers to create behaviours for
 * operations that need to be done just one time.<br/>
 * Otherwise, the Agent Consumer fAction will be executed.
 *  
 *     addBehaviour(new OneShotBehaviour(this, 300, a->System.out.println("Just one hello from " + a.getLocalName()));
 *   
 * @author Giovanni Rimassa - Universita` di Parma $Date: 2000-09-12 15:24:08 +0200 (mar, 12 set 2000) $ $Revision: 1857 $
 * @author Emmanuel Adam
 * @version Date: 2022-07-13
 */
public  class OneShotBehaviour extends SimpleBehaviour {

    /**
     * Default constructor. It does not set the owner agent.
     */
    public OneShotBehaviour() {
        super();
    }

    /**
     * This constructor sets the owner agent for this
     *  OneShotBehaviour  .
     *
     * @param a The agent this behaviour belongs to.
     */
    public OneShotBehaviour(Agent a) {
        super(a);
    }

    public OneShotBehaviour(Agent a, Consumer<Agent> fAction) {
        super(a, fAction);
    }

    /**
     * This is the method that makes  OneShotBehaviour  
     * one-shot, because it always returns  true  .
     *
     * @return Always  true  .
     */
    public final boolean done() {
        return true;
    }

}
