/*
 JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent
 systems in compliance with the FIPA specifications.
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

import java.util.Collection;
import java.util.Hashtable;

/**
 * Composite behaviour with concurrent children scheduling.
 * It is a  CompositeBehaviour   that executes its children
 * behaviours concurrently, and it terminates when a
 * particular condition on its sub-behaviours is met i.e. when
 * all children are done, <em>N</em> children are done or any
 * child is done.
 *
 * @author Giovanni Rimassa - Universita` di Parma
 * @author Giovanni Caire - Telecom Italia Lab
 * @version $Date: 2010-10-14 15:27:14 +0200(gio, 14 ott 2010) $ $Revision: 6375 $
 */
public class ParallelBehaviour extends CompositeBehaviour {

    /**
     * Predefined constant to be used in the constructor to create
     * a  ParallelBehaviour   that terminates when all its
     * children are done.
     */
    public static final int WHEN_ALL = 0;
    /**
     * Predefined constant to be used in the constructor to create
     * a  ParallelBehaviour   that terminates when any of
     * its child is done.
     */
    public static final int WHEN_ANY = 1;

    private final int whenToStop;
    private final BehaviourList subBehaviours = new BehaviourList();
    private final Hashtable<Behaviour, Behaviour> blockedChildren = new Hashtable<>();
    private final BehaviourList terminatedChildren = new BehaviourList();

    /**
     * Construct a  ParallelBehaviour   without setting the
     * owner agent, and using the default termination condition
     * (i.e. the parallel behaviour terminates as soon as all its
     * children behaviours terminate.
     */
    public ParallelBehaviour() {
        whenToStop = WHEN_ALL;
    }

    /**
     * Construct a  ParallelBehaviour   without setting the
     * owner agent.
     *
     * @param endCondition this value defines the termination condition
     *                     for this  ParallelBehaviour  . Use
     *                     <ol>
     *                     <li>
     *                      WHEN_ALL   to terminate this  ParallelBehaviour  
     *                     when all its children are done.
     *                     </li>
     *                     <li>
     *                      WHEN_ANY   to terminate this  ParallelBehaviour  
     *                     when any of its child is done.
     *                     </li>
     *                     <li>
     *                     a positive  int   value n to terminate this
     *                      ParallelBehaviour   when n of its children are done.
     *                     </li>
     *                     </ol>
     */
    public ParallelBehaviour(int endCondition) {
        whenToStop = endCondition;
    }

    /**
     * Construct a  ParallelBehaviour   setting the
     * owner agent.
     *
     * @param a            the agent this  ParallelBehaviour  
     *                     belongs to.
     * @param endCondition this value defines the termination condition
     *                     for this  ParallelBehaviour  . Use
     *                     <ol>
     *                     <li>
     *                      WHEN_ALL   to terminate this  ParallelBehaviour  
     *                     when all its children are done.
     *                     </li>
     *                     <li>
     *                      WHEN_ANY   to terminate this  ParallelBehaviour  
     *                     when any of its child is done.
     *                     </li>
     *                     <li>
     *                     a positive  int   value n to terminate this
     *                      ParallelBehaviour   when n of its children are done.
     *                     </li>
     *                     </ol>
     */
    public ParallelBehaviour(Agent a, int endCondition) {
        super(a);
        whenToStop = endCondition;
    }

    /**
     * Prepare the first child for execution
     *
     * @see CompositeBehaviour#scheduleFirst
     */
    protected void scheduleFirst() {
        // Schedule the first child
        subBehaviours.begin();
        Behaviour b = subBehaviours.getCurrent();

        // If there are no children just do nothing
        if (b != null) {
            // If there is at least one runnable child, then schedule
            // the first runnable child, else just do nothing.
            if (blockedChildren.size() < subBehaviours.size()) {
                while (!b.isRunnable()) {
                    b = subBehaviours.next();
                }
            }
        }
    }

    /**
     * This method
     * schedules children behaviours one at a time, in a round robin
     * fashion.
     *
     * @see CompositeBehaviour#scheduleNext(boolean, int)
     */
    protected void scheduleNext(boolean currentDone, int currentResult) {
        // Regardless of whether the current child is terminated, schedule
        // the next one;
        Behaviour b = subBehaviours.next();

        // If there are no children just do nothing (this can happen
        // if there was just one child and someone suddenly removed it)
        if (b != null) {
            // If there is at least one runnable child, then schedule
            // the first runnable child, else just do nothing.
            if (blockedChildren.size() < subBehaviours.size()) {
                while (!b.isRunnable()) {
                    b = subBehaviours.next();
                }
            }
        }
    }

    /**
     * Check whether this  ParallelBehaviour   must terminate.
     *
     * @see CompositeBehaviour#checkTermination
     */
    protected boolean checkTermination(boolean currentDone, int currentResult) {
        if (currentDone) {
            // If the current child is terminated --> remove it from
            // the list of sub-behaviours
            Behaviour b = subBehaviours.getCurrent();
            subBehaviours.removeElement(b);
            b.setParent(null);
            terminatedChildren.addElement(b);
        }

        if (!evalCondition()) {
            // The following check must be done regardless of the fact
            // that the current child is done or not, but provided that
            // this ParallelBehaviour is not terminated
            if (blockedChildren.size() == subBehaviours.size()) {
                // If all children are blocked --> this
                // ParallelBehaviour must block too and notify upwards
                myEvent.init(false, NOTIFY_UP);
                super.handle(myEvent);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get the current child
     *
     * @see CompositeBehaviour#getCurrent
     */
    protected Behaviour getCurrent() {
        return subBehaviours.getCurrent();
    }

    /**
     * Return a Collection view of the children of
     * this  ParallelBehaviour  
     *
     * @return
     * @see CompositeBehaviour#getChildren
     */
    public Collection<Behaviour> getChildren() {
        return subBehaviours;
    }

    /**
     * Return a Collection view of the children of
     * this  ParallelBehaviour   that have already completed.
     */
    public BehaviourList getTerminatedChildren() {
        return terminatedChildren;
    }

    /**
     * Add a sub behaviour to this  ParallelBehaviour  
     */
    public void addSubBehaviour(Behaviour b) {
        subBehaviours.addElement(b);

        b.setParent(this);
        b.setAgent(myAgent);

        if (b.isRunnable()) {
            // If all previous children were blocked (this Parallel Behaviour
            // was blocked too), restart this ParallelBehaviour and notify
            // upwards
            if (!isRunnable()) {
                if (myAgent != null)
                    myAgent.removeTimer(this);
                myEvent.init(true, NOTIFY_UP);
                super.handle(myEvent);
                if (myAgent != null)
                    myAgent.notifyRestarted(this);
                // Also reset the currentExecuted flag so that a runnable
                // child will be scheduled for execution
                currentExecuted = true;
            }
        } else {
            blockedChildren.put(b, b);
        }
    }

    /**
     * Remove a sub behaviour from this  ParallelBehaviour  
     */
    public void removeSubBehaviour(Behaviour b) {
        terminatedChildren.removeElement(b);
        boolean rc = subBehaviours.removeElement(b);

        if (rc) {
            b.setParent(null);
        }

        if (!b.isRunnable()) {
            blockedChildren.remove(b);
        } else {
            // If some children still exist and they are all blocked,
            // block this ParallelBehaviour and notify upwards
            if ((!subBehaviours.isEmpty()) &&
                    (blockedChildren.size() == subBehaviours.size())) {
                myEvent.init(false, NOTIFY_UP);
                super.handle(myEvent);
            }
        }
    }

    /**
     * Resets this behaviour. This methods puts a
     *  ParallelBehaviour   back in initial state,
     * besides calling  reset()   on each child behaviour
     * recursively.
     */
    public void reset() {
        blockedChildren.clear();

        terminatedChildren.begin();
        Behaviour b = terminatedChildren.getCurrent();

        // Restore all terminated sub-behaviours
        while (b != null) {
            terminatedChildren.removeElement(b);
            b.setParent(this);
            subBehaviours.addElement(b);
            b = terminatedChildren.next();
        }

        subBehaviours.begin();

        super.reset();

    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * Handle block/restart notifications. A
     *  ParallelBehaviour   object is blocked
     * <em>only</em> when all its children behaviours are blocked and
     * becomes ready to run as soon as any of its children is
     * runnable. This method takes care of the various possibilities.
     *
     * @param rce The event to handle.
     */
    protected void handle(RunnableChangedEvent rce) {
        // This method may be executed by an auxiliary thread posting
        // a message into the Agent's message queue --> it must be
        // synchronized with sub-behaviour additions/removal.
        synchronized (subBehaviours) {
            if (rce.isUpwards()) {
                // Upwards notification
                Behaviour b = rce.getSource();

                if (b == this) {
                    // If the event is from this behaviour, set the new
                    // runnable state and notify upwards.
                    super.handle(rce);
                } else {
                    // If the event is from a child -->
                    if (rce.isRunnable()) {
                        // If this is a restart, remove the child from the
                        // list of blocked children
                        Object child = blockedChildren.remove(b);

                        // Only if all children were blocked (this ParallelBehaviour was
                        // blocked too), restart this ParallelBehaviour and notify upwards
                        if ((child != null) && !isRunnable()) {
                            myEvent.init(true, NOTIFY_UP);
                            super.handle(myEvent);
                            // Also reset the currentExecuted flag so that a runnable
                            // child will be scheduled for execution
                            currentExecuted = true;
                        }
                    } else {
                        // If this is a block, put the child in the list of
                        // blocked children
                        Object child = blockedChildren.put(b, b);

                        // Only if, with the addition of this child all sub-behaviours
                        // are now blocked, block this ParallelBehaviour and notify upwards
                        if ((child == null) && (blockedChildren.size() == subBehaviours.size())) {
                            myEvent.init(false, NOTIFY_UP);
                            super.handle(myEvent);
                        }
                    }
                } // END of upwards notification from children

            } // END of upwards notification
            else {
                // Downwards notification	(from parent)
                boolean r = rce.isRunnable();

                // Set the new runnable state
                setRunnable(r);
                // Notify all children
                getChildren().forEach(b -> b.handle(rce));
                // Clear or completely fill the list of blocked children
                // according to whether this is a block or restart
                if (r) {
                    blockedChildren.clear();
                } else {
                    getChildren().forEach(b -> blockedChildren.put(b, b));
                }
            }  // END of downwards notification
        }
    }

    //#APIDOC_EXCLUDE_END

    private boolean evalCondition() {

        return switch (whenToStop) {
            case WHEN_ALL -> subBehaviours.isEmpty();
            case WHEN_ANY -> (terminatedChildren.size() > 0);
            default -> (terminatedChildren.size() >= whenToStop);
        };
    }
}
