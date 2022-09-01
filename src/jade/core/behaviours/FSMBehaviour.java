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

package jade.core.behaviours;

//#CUSTOM_EXCLUDE_FILE

import jade.core.Agent;
import jade.util.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Composite behaviour with Finite State Machine based children scheduling.
 * It is a  CompositeBehaviour   that executes its children
 * behaviours according to a FSM defined by the user. More specifically
 * each child represents a state in the FSM.
 * The class provides methods to register states (sub-behaviours) and
 * transitions that defines how sub-behaviours will be scheduled.
 * <p> At a minimum, the following steps are needed in order to properly
 * define a  FSMBehaviour  :
 * <ul>
 * <li> register a single Behaviour as the initial state of the FSM by calling
 * the method  registerFirstState  ;
 * <li> register one or more Behaviours as the final states of the FSM
 * by calling the method  registerLastState  ;
 * <li> register one or more Behaviours as the intermediate states of the FSM
 * by calling the method  registerState  ;
 * <li> for each state of the FSM, register the transitions to the other
 * states by calling the method  registerTransition  ;
 * <li> the method  registerDefaultTransition   is also useful
 * in order to register a default transition from a state to another state
 * independently on the termination event of the source state.
 * </ul>
 * A number of other methods are available in this class for generic
 * tasks, such as getting the current state or the name of a state, ...
 *
 * @author Giovanni Caire - CSELT
 * @version $Date: 2008-04-15 11:09:57 +0200 (mar, 15 apr 2008) $ $Revision: 6029 $
 * @see SequentialBehaviour
 * @see ParallelBehaviour
 */
public class FSMBehaviour extends SerialBehaviour {

    private final Map<String, Behaviour> mapStates = new HashMap<>();
    private final TransitionTable theTransitionTable = new TransitionTable();
    //#J2ME_EXCLUDE_BEGIN
    private final Logger myLogger = Logger.getMyLogger(FSMBehaviour.class.getName());
    // Protected for debugging purposes only
    protected List<String> lastStates = new ArrayList<>();
    protected String currentName = null;
    private Behaviour current = null;
    private String previousName = null;
    private String firstName = null;
    private int lastExitValue;
    // These variables are used to force a transition on a given state at runtime
    private boolean transitionForced = false;
    private String forcedTransitionDest = null;
    //#J2ME_EXCLUDE_END

    /**
     * Default constructor, does not set the owner agent.
     */
    public FSMBehaviour() {
        super();
    }

    /**
     * This constructor sets the owner agent.
     *
     * @param a The agent this behaviour belongs to.
     */
    public FSMBehaviour(Agent a) {
        super(a);
    }

    /**
     * Register a  Behaviour   as a state of this
     *  FSMBehaviour  . When the FSM reaches this state
     * the registered  Behaviour   will be executed.
     *
     * @param state The  Behaviour   representing the state
     * @param name  The name identifying the state.
     */
    public void registerState(Behaviour state, String name) {
        state.setBehaviourName(name);
        state.setParent(this);
        state.setAgent(myAgent);
        mapStates.put(name, state);

        // Maybe we are over-writing the state that is currently in execution
        if (name.equals(currentName)) {
            current = state;
        }
    }

    /**
     * Register a  Behaviour   as the initial state of this
     *  FSMBehaviour  .
     *
     * @param state The  Behaviour   representing the state
     * @param name  The name identifying the state.
     */
    public void registerFirstState(Behaviour state, String name) {
        registerState(state, name);
        firstName = name;
    }

    /**
     * Register a  Behaviour   as a final state of this
     *  FSMBehaviour  . When the FSM reaches this state
     * the registered  Behaviour   will be executed and,
     * when completed, the  FSMBehaviour   will terminate too.
     *
     * @param state The  Behaviour   representing the state
     * @param name  The name identifying the state.
     */
    public void registerLastState(Behaviour state, String name) {
        registerState(state, name);
        if (!lastStates.contains(name)) {
            lastStates.add(name);
        }
    }

    /**
     * Deregister a state of this  FSMBehaviour  .
     *
     * @param name The name of the state to be deregistered.
     * @return the Behaviour if any that was registered as the
     * deregistered state.
     */
    public Behaviour deregisterState(String name) {
        Behaviour b = mapStates.remove(name);
        if (b != null) {
            b.setParent(null);
        }
        theTransitionTable.removeTransitionsFromState(name);
        if (name.equals(firstName)) {
            firstName = null;
        }
        lastStates.remove(name);
        return b;
    }

    /**
     * Register a transition in the FSM defining the policy for
     * children scheduling of this  FSMBehaviour  .
     *
     * @param s1    The name of the state this transition starts from
     * @param s2    The name of the state this transition leads to
     * @param event The termination event that fires this transition
     *              as returned by the  onEnd()   method of the
     *               Behaviour   representing state s1.
     * @see Behaviour#onEnd()
     */
    public void registerTransition(String s1, String s2, int event) {
        registerTransition(s1, s2, event, null);
    }

    /**
     * Register a transition in the FSM defining the policy for
     * children scheduling of this  FSMBehaviour  .
     * When this transition is fired the states indicated in the
     *  toBeReset   parameter are reset. This is
     * particularly useful for transitions that lead to states that
     * have already been visited.
     *
     * @param s1        The name of the state this transition starts from
     * @param s2        The name of the state this transition leads to
     * @param event     The termination event that fires this transition
     *                  as returned by the  onEnd()   method of the
     *                   Behaviour   representing state s1.
     * @param toBeReset An array of strings including the names of
     *                  the states to be reset.
     *                  see Behaviour#onEnd()
     */
    public void registerTransition(String s1, String s2, int event, String[] toBeReset) {
        Transition t = new Transition(this, s1, s2, event, toBeReset);
        theTransitionTable.addTransition(t);
    }

    /**
     * Register a default transition in the FSM defining the policy for
     * children scheduling of this  FSMBehaviour  .
     * This transition will be fired when state s1 terminates with
     * an event that is not explicitly associated to any transition.
     *
     * @param s1 The name of the state this transition starts from
     * @param s2 The name of the state this transition leads to
     */
    public void registerDefaultTransition(String s1, String s2) {
        registerDefaultTransition(s1, s2, null);
    }

    /**
     * Register a default transition in the FSM defining the policy for
     * children scheduling of this  FSMBehaviour  .
     * This transition will be fired when state s1 terminates with
     * an event that is not explicitly associated to any transition.
     * When this transition is fired the states indicated in the
     *  toBeReset   parameter are reset. This is
     * particularly useful for transitions that lead to states that
     * have already been visited.
     *
     * @param s1        The name of the state this transition starts from
     * @param s2        The name of the state this transition leads to
     * @param toBeReset An array of strings including the names of
     *                  the states to be reset.
     */
    public void registerDefaultTransition(String s1, String s2, String[] toBeReset) {
        Transition t = new Transition(this, s1, s2, toBeReset);
        theTransitionTable.addTransition(t);
    }

    /**
     * Deregister the transition from a given source state and identfied by a
     * given termination event.
     *
     * @param source The name of the source state
     * @param event  The termination event that identifies the transition to be removed
     */
    public void deregisterTransition(String source, int event) {
        theTransitionTable.removeTransition(source, event);
    }

    /**
     * Deregister the default transition from a given source state.
     *
     * @param source The name of the source state
     */
    public void deregisterDefaultTransition(String source) {
        theTransitionTable.removeTransition(source);
    }

    /**
     * Check if a default transition exits from a given source state.
     *
     * @param source The name of the source state
     * @return  true   if a default transition exits from the given source state.  false   otherwise.
     */
    public boolean hasDefaultTransition(String source) {
        return (theTransitionTable.getTransition(source) != null);
    }

    /**
     * Retrieve the child behaviour associated to the FSM state with
     * the given name.
     *
     * @return the  Behaviour   representing the state whose
     * name is  name  , or  null   if no such
     * behaviour exists.
     */
    public Behaviour getState(String name) {
        Behaviour b = null;
        if (name != null) {
            b = mapStates.get(name);
        }
        return b;
    }

    /**
     * Retrieve the name of the FSM state associated to the given child
     * behaviour.
     *
     * @return the name of the state represented by
     *  Behaviour   state, or  null   if the given
     * behaviour is not a child of this FSM behaviour.
     */
    public String getName(Behaviour state) {
        for (String o : mapStates.keySet()) {
            Behaviour s = mapStates.get(o);
            if (state == s) {
                return o;
            }
        }
        return null;
    }

    /**
     * Retrieve the exit value of the most recently executed
     * child. This is also the trigger value that selects the next FSM
     * transition.
     *
     * @return the exit value of the last executed state.
     */
    public int getLastExitValue() {
        return lastExitValue;
    }

    /**
     * Override the onEnd() method to return the exit value of the
     * last executed state.
     */
    public int onEnd() {
        return getLastExitValue();
    }

    /**
     * Prepare the first child for execution. The first child is the
     *  Behaviour   registered as the first state of this
     *  FSMBehaviour
     * see CompositeBehaviour#scheduleFirst
     */
    protected void scheduleFirst() {
        if (transitionForced) {
            currentName = forcedTransitionDest;
            transitionForced = false;
        } else {
            // Normal case: go to the first state
            currentName = firstName;
        }
        current = getState(currentName);
        handleStateEntered(current);
        // DEBUG
        //System.out.println(myAgent.getLocalName()+" is Executing state "+currentName);
    }

    /**
     * This method schedules the next child to be executed. It checks
     * whether the current child is completed and, in this case, fires
     * a suitable transition (according to the termination event of
     * the current child) and schedules the child representing the
     * new state.
     *
     * @param currentDone   a flag indicating whether the just executed
     *                      child has completed or not.
     * @param currentResult the termination value (as returned by
     *                       onEnd()  ) of the just executed child in the case this
     *                      child has completed (otherwise this parameter is meaningless)
     *                      see CompositeBehaviour#scheduleNext(boolean, int)
     */
    protected void scheduleNext(boolean currentDone, int currentResult) {
        if (currentDone) {
            try {
                previousName = currentName;
                if (transitionForced) {
                    currentName = forcedTransitionDest;
                    transitionForced = false;
                } else {
                    // Normal case: use the TransitionTable to select the next state
                    Transition t = theTransitionTable.getTransition(currentName, currentResult);
                    resetStates(t.toBeReset);
                    currentName = t.dest;
                }
                current = getState(currentName);
                if (current == null) {
                    throw new NullPointerException();
                } else {
                    handleStateEntered(current);
                }
            } catch (NullPointerException npe) {
                current = null;
                handleInconsistentFSM(previousName, currentResult);
            }
            // DEBUG
            //System.out.println(myAgent.getLocalName()+ " is Executing state "+currentName);
        }
    }

    protected void handleInconsistentFSM(String current, int event) {
        throw new RuntimeException("Inconsistent FSM. State: " + current + " event: " + event);
    }

    protected void handleStateEntered(Behaviour state) {
    }

    /**
     * Check whether this  FSMBehaviour   must terminate.
     *
     * @return true when the last child has terminated and it
     * represents a final state. false otherwise
     * see CompositeBehaviour#checkTermination
     */
    protected boolean checkTermination(boolean currentDone, int currentResult) {
        boolean ret = false;
        if (currentDone) {
            lastExitValue = currentResult;
            ret = lastStates.contains(currentName);
        }
        //#J2ME_EXCLUDE_BEGIN
        if (myLogger.isLoggable(Logger.FINE)) {
            myLogger.log(Logger.FINE, "FSM-Behaviour " + getBehaviourName() + ": checkTermination() returning " + ret);
        }
        //#J2ME_EXCLUDE_END
        return ret;
    }

    /**
     * Get the current child
     *
     * @see CompositeBehaviour#getCurrent
     */
    protected Behaviour getCurrent() {
        return current;
    }

    /**
     * Return a Collection view of the children of
     * this  SequentialBehaviour
     * see CompositeBehaviour#getChildren
     *
     * @return
     */
    public Collection<Behaviour> getChildren() {
        return mapStates.values();
    }

    /**
     * Temporarily disregards the FSM structure, and jumps to the given
     * state. This method acts as a sort of  GOTO   statement
     * between states, and replaces the currently active state without
     * considering the trigger event or whether a transition was
     * registered. It should be used only to handle exceptional
     * conditions, if default transitions are not effective enough.
     *
     * @param next The name of the state to jump to at the next FSM
     *             cheduling quantum. If the FSM has no state with the given name,
     *             this method does nothing.
     */
    protected void forceTransitionTo(String next) {
        // Just check that the forced transition leads into a valid state
        Behaviour b = getState(next);
        if (b != null) {
            transitionForced = true;
            forcedTransitionDest = next;
        }
    }

    /**
     * Get the previously executed child
     * see CompositeBehaviour#getCurrent
     */
    protected Behaviour getPrevious() {
        return getState(previousName);
    }

    /**
     * Put this FSMBehaviour back in the initial condition.
     */
    public void reset() {
        super.reset();
        transitionForced = false;
        forcedTransitionDest = null;
    }

    /**
     * Reset the children behaviours registered in the states indicated in
     * the  states   parameter.
     *
     * @param states the names of the states that have to be reset
     */
    public void resetStates(String[] states) {
        if (states != null) {
            for (String state : states) {
                Behaviour b = getState(state);
                b.reset();
            }
        }
    }

    //#MIDP_EXCLUDE_BEGIN
    public String stringifyTransitionTable() {
        return theTransitionTable.transitions.toString();
    }

    /**
     * Inner class Transition
     */
    static class Transition implements Serializable {

        @Serial
        private static final long serialVersionUID = 3487495895819004L;
        private FSMBehaviour fsm;
        private String src;
        private String dest;
        private int trigger;
        private boolean def;
        private String[] toBeReset;

        public Transition() {
        }

        public Transition(FSMBehaviour f, String s, String d, int t, String[] rs) {
            fsm = f;
            src = s;
            dest = d;
            trigger = t;
            def = false;
            toBeReset = rs;
        }

        public Transition(FSMBehaviour f, String s, String d, String[] rs) {
            fsm = f;
            src = s;
            dest = d;
            trigger = 0;
            def = true;
            toBeReset = rs;
        }

        public FSMBehaviour getFSM() {
            return fsm;
        }

        public void setFSM(FSMBehaviour f) {
            fsm = f;
        }

        public String getFromState() {
            return src;
        }

        public void setFromState(String f) {
            src = f;
        }

        public String getToState() {
            return dest;
        }

        public void setToState(String t) {
            dest = t;
        }

        public int getTrigger() {
            return trigger;
        }

        public void setTrigger(int t) {
            trigger = t;
        }

        public boolean isDefault() {
            return def;
        }

        public void setDefault(boolean d) {
            def = d;
        }

        public String[] getStatesToReset() {
            return toBeReset;
        }

        public void setStatesToReset(String[] ss) {
            toBeReset = ss;
        }

        //#MIDP_EXCLUDE_BEGIN
        public String toString() {
            return "(TRANSITION trigger=" + trigger + ", source=" + src + ", dest=" + dest + ")";
        }
        //#MIDP_EXCLUDE_END
    } // END of inner class Transition

    /**
     * Inner class implementing the FSM transition table
     */
    class TransitionTable implements Serializable {
        @Serial
        private static final long serialVersionUID = 3487495895819003L;
        private final Hashtable<String, TransitionsFromState> transitions = new Hashtable<>();

        void clear() {
            transitions.clear();
        }

        void addTransition(Transition t) {
            String key1 = t.getFromState();

            TransitionsFromState tfs = transitions.get(key1);

            if (tfs == null) {
                tfs = new TransitionsFromState();
                transitions.put(key1, tfs);
            }

            if (t.isDefault()) {
                tfs.setDefaultTransition(t);
            } else {
                Integer key2 = t.getTrigger();
                tfs.put(key2, t);
            }
        }

        void removeTransition(String s1, int event) {
            TransitionsFromState tfs = transitions.get(s1);
            if (tfs != null) {
                Transition t = (Transition) tfs.remove(event);
                if (t != null) {

                    if ((tfs.isEmpty() && (tfs.getDefaultTransition() == null))) {
                        transitions.remove(s1);
                    }
                }
            }
        }

        void removeTransition(String s1) {
            TransitionsFromState tfs = transitions.get(s1);
            if (tfs != null) {
                tfs.setDefaultTransition(null);

                if (tfs.isEmpty()) {
                    transitions.remove(s1);
                }
            }
        }

        Transition getTransition(String s, int event) {
            TransitionsFromState tfs = transitions.get(s);
            if (tfs != null) {
                return (Transition) tfs.get(event);
            } else {
                return null;
            }
        }

        Transition getTransition(String s) {
            TransitionsFromState tfs = transitions.get(s);
            if (tfs != null) {
                return tfs.getDefaultTransition();
            } else {
                return null;
            }
        }

        void removeTransitionsFromState(String stateName) {
            transitions.remove(stateName);
        }
    }

    /**
     * Inner class TransitionsFromState
     */
    class TransitionsFromState extends Hashtable<Object, Object> {
        @Serial
        private static final long serialVersionUID = 3487495895819005L;
        private Transition defaultTransition = null;

        Transition getDefaultTransition() {
            return defaultTransition;
        }

        void setDefaultTransition(Transition dt) {
            defaultTransition = dt;
        }

        public Object get(Object key) {
            Transition t = (Transition) super.get(key);
            if (t == null) {
                t = defaultTransition;
            }
            return t;
        }

        //#MIDP_EXCLUDE_BEGIN
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Transitions: ");
            sb.append(super.toString());
            if (defaultTransition != null) {
                sb.append(" defaultTransition: ").append(defaultTransition);
            }
            return sb.toString();
        }
        //#MIDP_EXCLUDE_END
    } // END of inner class TransitionsFromState
    //#MIDP_EXCLUDE_END
}
