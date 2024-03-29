/* ****************************************************************
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
import jade.lang.acl.ACLMessage;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Abstract base class for <b><em>JADE</em></b> behaviours.  Extending
 * this class directly should only be needed for particular behaviours
 * with special synchronization needs; this is because event based
 * notification used for blocking and restarting behaviours is
 * directly accessible at this level.
 *
 * @author Giovanni Rimassa - Universita' di Parma
 * @version $Date: 2013-03-11 09:55:05 +0100 (lun, 11 mar 2013) $ $Revision: 6645 $
 */
public abstract class Behaviour implements Serializable {
    /**
     * A constant identifying the runnable state.
     *
     * @serial
     */
    public static final String STATE_READY = "READY";

    //#APIDOC_EXCLUDE_BEGIN
    /**
     * A constant identifying the blocked state.
     *
     * @serial
     */
    public static final String STATE_BLOCKED = "BLOCKED";
    /**
     * A constant identifying the running state.
     *
     * @serial
     */
    public static final String STATE_RUNNING = "RUNNING";
    /**
     * A constant for child-to-parent notifications.
     *
     * @serial
     */
    protected static final int NOTIFY_UP = -1;
    /**
     * A constant for parent-to-child notifications.
     *
     * @serial
     */
    protected static final int NOTIFY_DOWN = 1;
    @Serial
    private static final long serialVersionUID = 3487495895819001L;
    /**
     * The agent this behaviour belongs to.
     * <p>
     * This is an instance variable that holds a reference to the Agent
     * object and allows the usage of its methods within the body of the
     * behaviour. As the class  Behaviour   is the superclass
     * of all the other behaviour classes, this variable is always
     * available. Of course, remind to use the appropriate constructor,
     * i.e. the one that accepts an agent object as argument; otherwise,
     * this variable is set to  null  .
     */
    protected Agent myAgent;


    //#APIDOC_EXCLUDE_END
    /**
     * This event object will be re-used for every state change
     * notification.
     */
    protected RunnableChangedEvent myEvent = new RunnableChangedEvent();
    //#APIDOC_EXCLUDE_BEGIN
    protected CompositeBehaviour parent;
    private String myName;
    private boolean startFlag = true;
    /**
     * Flag indicating whether this Behaviour is runnable or not
     */
    private volatile boolean runnableState = true;
    private volatile long restartCounter = 0;

    //#APIDOC_EXCLUDE_BEGIN
    private volatile String executionState = STATE_READY;

    //#APIDOC_EXCLUDE_END

    //#CUSTOM_EXCLUDE_BEGIN
    /**
     * map key - list of messages
     */
    private HashMap<String, List<ACLMessage>> mapMessagesList;
    /**
     * map key - message
     */
    private HashMap<String, ACLMessage> mapMessages;
    private CompositeBehaviour wrappedParent;

    /**  an agent consumer called in action() by default*/
    Consumer<Agent> fAction;

    /**
     * Default constructor. It does not set the agent owning this
     * behaviour object.
     */
    public Behaviour() {
        // Construct a default name
        myName = getClass().getName();
        // Remove the class name and the '$' characters from
        // the class name for readability.
        int dotIndex = myName.lastIndexOf('.');
        int dollarIndex = myName.lastIndexOf('$');
        int lastIndex = (Math.max(dotIndex, dollarIndex));

        if (lastIndex != -1) {
            myName = myName.substring(lastIndex + 1);
        }
    }

    /**
     * Constructor with owner agent.
     *
     * @param a The agent owning this behaviour.
     */
    public Behaviour(Agent a) {
        this();
        myAgent = a;
    }


    /**
     * Constructor with owner agent.
     *
     * @param a The agent owning this behaviour.
     * @param fAction The action launch by default by the agent.
     * @author E. ADAM
     * @since 22.07     */
    public Behaviour(Agent a, Consumer<Agent> fAction) {
        this();
        myAgent = a;
        this.fAction = fAction;
    }

    void setWrappedParent(CompositeBehaviour cb) {
        wrappedParent = cb;
    }
    //#APIDOC_EXCLUDE_END

    /**
     * Retrieve the enclosing CompositeBehaviour (if present). In order to access the parent behaviour
     * it is strongly suggested to use this method rather than the <core>parent   member variable
     * directly. In case of threaded or wrapped behaviour in facts the latter may have unexpected values.
     *
     * @return The enclosing CompositeBehaviour (if present).
     * see CompositeBehaviour
     */
    protected CompositeBehaviour getParent() {
        if (wrappedParent != null) {
            return wrappedParent;
        } else {
            return parent;
        }
    }
    //#CUSTOM_EXCLUDE_END

    void setParent(CompositeBehaviour cb) {
        parent = cb;
        if (parent != null) {
            myAgent = parent.myAgent;
        }
        wrappedParent = null;
    }

    /**
     * Retrieve the name of this behaviour object. If no explicit name
     * was set, a default one is given, based on the behaviour class
     * name.
     *
     * @return The name of this behaviour.
     */
    public final String getBehaviourName() {
        return myName;
    }

    /**
     * Give a name to this behaviour object.
     *
     * @param name The name to give to this behaviour.
     */
    public final void setBehaviourName(String name) {
        myName = name;
    }

    /**
     * Runs the behaviour. This abstract method must be implemented by
     *  Behaviour  subclasses to perform ordinary behaviour
     * duty. An agent schedules its behaviours calling their
     *  action()   method; since all the behaviours belonging
     * to the same agent are scheduled cooperatively, this method
     * <b>must not</b> enter in an endless loop and should return as
     * soon as possible to preserve agent responsiveness. To split a
     * long and slow task into smaller section, recursive behaviour
     * aggregation may be used.
     * see CompositeBehaviour
     */
    public  void action()
    {
        if (fAction!=null) fAction.accept(myAgent);
    }

    /**
     * Check if this behaviour is done. The agent scheduler calls this
     * method to see whether a  Behaviour   still need to be
     * run or it has completed its task. Concrete behaviours must
     * implement this method to return their completion state. Finished
     * behaviours are removed from the scheduling queue, while others
     * are kept within to be run again when their turn comes again.
     *
     * @return  true   if the behaviour has completely executed.
     */
    public abstract boolean done();

    /**
     * This method is just an empty placeholder for subclasses. It is
     * invoked just once after this behaviour has ended. Therefore,
     * it acts as an epilog for the task represented by this
     *  Behaviour  .
     * <br>
     * Note that  onEnd   is called after the behaviour has been
     * removed from the pool of behaviours to be executed by an agent.
     * Therefore calling
     *  reset()   is not sufficient to cyclically repeat the task
     * represented by this  Behaviour  . In order to achieve that,
     * this  Behaviour   must be added again to the agent
     * (using  myAgent.addBehaviour(this)  ). The same applies to
     * in the case of a  Behaviour   that is a child of a
     *  ParallelBehaviour  .
     *
     * @return an integer code representing the termination value of
     * the behaviour.
     */
    public int onEnd() {
        return 0;
    }

    /**
     * This method is just an empty placeholders for subclasses. It is
     * executed just once before starting behaviour execution.
     * Therefore, it acts as a prolog to the task
     * represented by this  Behaviour  .
     */
    public void onStart() {
    }

    /**
     * This method is called internally by the JADE framework
     * and should not be called by the user.
     */
    public final void actionWrapper() {
        if (startFlag) {
            onStart();
            startFlag = false;
        }

        //#MIDP_EXCLUDE_BEGIN
        // Maybe the behaviour was removed from another thread
        if (myAgent != null) {
            myAgent.notifyChangeBehaviourState(this, Behaviour.STATE_READY, Behaviour.STATE_RUNNING);
        }
        //#MIDP_EXCLUDE_END
        action();
        //#MIDP_EXCLUDE_BEGIN
        if (myAgent != null) {
            myAgent.notifyChangeBehaviourState(this, Behaviour.STATE_RUNNING, Behaviour.STATE_READY);
        }
        //#MIDP_EXCLUDE_END
    }

    //#APIDOC_EXCLUDE_BEGIN

    public final String getExecutionState() {
        return executionState;
    }

    public final void setExecutionState(String s) {
        executionState = s;
    }

    /**
     * Restores behaviour initial state. This method must be implemented
     * by concrete subclasses in such a way that calling
     *  reset()   on a behaviour object is equivalent to
     * destroying it and recreating it back. The main purpose for this
     * method is to realize multistep cyclic behaviours without needing
     * expensive constructions an deletion of objects at each loop
     * iteration.
     * Remind to call super.reset() from the sub-classes.
     */
    public void reset() {
        startFlag = true;
        restart();
    }
    //#APIDOC_EXCLUDE_END

    /**
     * Handler for block/restart events. This method handles
     * notification by copying its runnable state and then by simply
     * forwarding the event when it is traveling upwards and by doing
     * nothing when it is traveling downwards, since an ordinary
     * behaviour has no children.
     *
     * @param rce The event to handle
     */
    protected void handle(RunnableChangedEvent rce) {
        // Set the new runnable state
        setRunnable(rce.isRunnable());
        //#CUSTOM_EXCLUDE_BEGIN
        // If the notification is upwords and a parent exists -->
        // Notify the parent
        if ((parent != null) && (rce.isUpwards())) {
            parent.handle(rce);
        }
        //#CUSTOM_EXCLUDE_END
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * Returns the root for this  Behaviour   object. That is,
     * the top-level behaviour this one is a part of. Agents apply
     * scheduling only to top-level behaviour objects, so they just call
     *  restart()   on root behaviours.
     *
     * @return The top-level behaviour this behaviour is a part of. If
     * this one is a top level behaviour itself, then simply
     *  this   is returned.
     * @see Behaviour#restart()
     */
    public Behaviour root() {
        //#CUSTOM_EXCLUDE_BEGIN
        Behaviour p = getParent();
        if (p != null) {
            return p.root();
        }
        //#CUSTOM_EXCLUDE_END
        return this;
    }
    //#APIDOC_EXCLUDE_END

    /**
     * Returns whether this  Behaviour   object is blocked or
     * not.
     *
     * @return  true   when this behaviour is not blocked,
     *  false   when it is.
     */
    public boolean isRunnable() {
        return runnableState;
    }

    // Sets the runnable/not-runnable state
    void setRunnable(boolean runnable) {
        runnableState = runnable;
        if (runnableState) {
            restartCounter++;
        }
    }

    /**
     * This method is used internally by the framework. Developer should not call or redefine it.
     */
    public final long getRestartCounter() {
        return restartCounter;
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * Blocks this behaviour. It should be noticed that this method is NOT a
     * blocking call: when it is invoked, the internal behaviour
     * state is set to <em>Blocked</em> so that, as soon as the  action()
     * method returns, the behaviour is put into a blocked behaviours queue so that it will
     * not be scheduled anymore.<br>
     * The behaviour is moved back in the pool of active behaviours when either
     * a message is received or the behaviour is explicitly restarted by means of its
     *  restart()   method.<br>
     * If this behaviour is a child of a  CompositeBehaviour   a suitable event is fired to
     * notify its parent behaviour up to the behaviour composition hierarchy root.
     *
     * @see Behaviour#restart()
     */
    public void block() {
        handleBlockEvent();
    }
    //#APIDOC_EXCLUDE_END

    /**
     * This method is used internally by the framework. Developer should not call or redefine it.
     */
    protected void handleBlockEvent() {
        myEvent.init(false, NOTIFY_UP);
        handle(myEvent);
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * Blocks this behaviour for a specified amount of time. The
     * behaviour will be restarted when among the three following
     * events happens.
     * <ul>
     * <li> <em>A time of  millis   milliseconds has passed
     * since the call to  block()  .</em>
     * <li> <em>An ACL message is received by the agent this behaviour
     * belongs to.</em>
     * <li> <em>Method  restart()   is called explicitly on
     * this behaviour object.</em>
     * </ul>
     *
     * @param millis The amount of time to block, in
     *               milliseconds. <em><b>Notice:</b> a value of 0 for
     *                millis   is equivalent to a call to
     *                block()   without arguments.</em>
     * @see Behaviour#block()
     */
    public void block(long millis) {
        // Note that it is important to block the behaviour before
        // adding a Timer to restart it in a millis time. In fact if
        // the two operations are cerried out the other way around, it
        // could happen that the Timer expires before the block()
        // operation is executed --> The TimerDispatcher thread restarts
        // the behaviour (that has not blocked yet) and just after the
        // behaviour blocks.
        block();
        if (myAgent != null) {
            myAgent.restartLater(this, millis);
        }
    }
    //#APIDOC_EXCLUDE_END

    /**
     * Restarts a blocked behaviour. This method fires a suitable event
     * to notify this behaviour's parent. When the agent scheduler
     * inserts a blocked event back into the agent ready queue, it
     * restarts it automatically. When this method is called, any timer
     * associated with this behaviour object is cleared.
     *
     * @see Behaviour#block()
     */
    public void restart() {
        if (myAgent != null) {
            myAgent.removeTimer(this);
        }

        handleRestartEvent();

        if (myAgent != null) {
            myAgent.notifyRestarted(this);
        }
    }

    /**
     * This method is used internally by the framework. Developer should not call or redefine it.
     */
    public void handleRestartEvent() {
        myEvent.init(true, NOTIFY_UP);
        handle(myEvent);
    }

    //#APIDOC_EXCLUDE_BEGIN

    public Agent getAgent() {
        return myAgent;
    }
    //#APIDOC_EXCLUDE_END

    /**
     * Associates this behaviour with the agent it belongs to. There is
     * no need to call this method explicitly, since the
     *  addBehaviour()   call takes care of the association
     * transparently.
     *
     * @param a The agent this behaviour belongs to.
     * @see Agent#addBehaviour(Behaviour b)
     */
    public void setAgent(Agent a) {
        myAgent = a;
    }

    /**
     * Return the private map of list of messages  of this  Behaviour  .
     * If it was null, a new HashMap is created and returned.
     *
     * @return The private map of list of messages of this  Behaviour
     */
    public HashMap<String, List<ACLMessage>> getMapMessagesList() {
        if (mapMessagesList == null) {
            mapMessagesList = new HashMap<>();
        }
        return mapMessagesList;
    }


    //#CUSTOM_EXCLUDE_BEGIN

    /**
     * Set the private map of list of messages of this  Behaviour
     *
     * @param map the  HashMap   that this  Behaviour
     *            will use as its private map of list of messages
     */
    public void setMapMessagesList(HashMap<String, List<ACLMessage>> map) {
        mapMessagesList = map;
    }

    /**
     * Return the private map of messages  of this  Behaviour  .
     * If it was null, a new HashMap is created and returned.
     *
     * @return The private map of  messages of this  Behaviour
     */
    public HashMap<String, ACLMessage> getMapMessages() {
        if (mapMessages == null) {
            mapMessages = new HashMap<>();
        }
        return mapMessages;
    }

    /**
     * Set the private map of  messages of this  Behaviour
     *
     * @param map the  HashMap   that this  Behaviour
     *            will use as its private map of  messages
     */
    public void setMapMessages(HashMap<String, ACLMessage> map) {
        mapMessages = map;
    }

    /**
     * Event class for notifying blocked and restarted behaviours.
     * This class is used to notify interested behaviours when a
     * Behaviour changes its runnable state. It may be sent to
     * behaviour's parent (<em>upward notification</em> or to behaviour's
     * children (<em>downward notification</em>).
     */
    protected class RunnableChangedEvent implements Serializable {
        @Serial
        private static final long serialVersionUID = 3487495895819002L;
        /**
         * @serial
         */
        private boolean runnable;

        /**
         * @serial
         */
        private int direction;

        /**
         * Re-init event content. This method can be used to rewrite an
         * existing event with new data (much cheaper than making a new
         * object).
         *
         * @param b A  boolean   flag; when  false
         *          it means that a behaviour passed from <em>Ready</em> to
         *          <em>Blocked</em> state. When  true   it means that a
         *          behaviour passed from <em>Blocked</em> to <em>Ready</em> (this
         *          flag is the truth value of the predicate <em><b>'The behaviour
         *          has now become runnable'</b></em>.
         * @param d A notification direction: when direction is
         *           NOTIFY_UP  , the event travels upwards the behaviour
         *          containment hierarchy; when it is  NOTIFY_DOWN  , the
         *          event travels downwards.
         */
        public void init(boolean b, int d) {
            runnable = b;
            direction = d;
        }


        /**
         * Read event source.
         *
         * @return The  Behaviour   object which generated this event.
         */
        public Behaviour getSource() {
            return Behaviour.this;
        }

        /**
         * Check whether the event is runnable.
         *
         * @return  true   when the behaviour generating this
         * event has become <em>Ready</em>,  false   when it has
         * become <em>Blocked</em>.
         */
        public boolean isRunnable() {
            return runnable;
        }

        /**
         * Check which direction this event is travelling.
         *
         * @return  true   when the event is a notification
         * going from a child behaviour to its parent;  false
         * otherwise.
         */
        public boolean isUpwards() {
            return direction == NOTIFY_UP;
        }

    } // End of RunnableChangedEvent class

    //#CUSTOM_EXCLUDE_END
}
