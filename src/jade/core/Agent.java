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

package jade.core;

import jade.core.behaviours.Behaviour;
import jade.core.mobility.AgentMobilityHelper;
import jade.core.mobility.Movable;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.security.JADESecurityException;
import jade.util.Event;
import jade.util.Logger;
import static java.lang.System.out;
import java.io.*;
import java.util.*;

//#MIDP_EXCLUDE_END

/*#MIDP_INCLUDE_BEGIN
 import javax.microedition.midlet.*;
 #MIDP_INCLUDE_END*/

/**
 * The  Agent   class is the common superclass for user
 * defined software agents. It provides methods to perform basic agent
 * tasks, such as:
 * <ul>
 * <li> <b> Message passing using  ACLMessage   objects,
 * both unicast and multicast with optional pattern matching. </b></li>
 * <li> <b> Complete Agent Platform life cycle support, including
 * starting, suspending and killing an agent. </b></li>
 * <li> <b> Scheduling and execution of multiple concurrent activities. </b></li>
 * </ul>
 * <p>
 * Application programmers must write their own agents as
 *  Agent   subclasses, adding specific behaviours as needed
 * and exploiting  Agent   class capabilities.
 *
 * @author Giovanni Rimassa - Universita' di Parma
 * @author Giovanni Caire - TILAB
 * @version $Date: 2017-05-23 10:41:19 +0200 (mar, 23 mag 2017) $ $Revision: 6826 $
 */
public class Agent implements Runnable, Serializable, TimerListener {
    /**
     * Out of band value for Agent Platform Life Cycle states.
     */
    public static final int AP_MIN = 0;   // Hand-made type checking
    /**
     * Represents the <em>initiated</em> agent state.
     */
    public static final int AP_INITIATED = 1;
    /**
     * Represents the <em>active</em> agent state.
     */
    public static final int AP_ACTIVE = 2;
    /**
     * Represents the <em>idle</em> agent state.
     */
    public static final int AP_IDLE = 3;
    /**
     * Represents the <em>suspended</em> agent state.
     */
    public static final int AP_SUSPENDED = 4;


    //#MIDP_EXCLUDE_BEGIN
    /**
     * Represents the <em>waiting</em> agent state.
     */
    public static final int AP_WAITING = 5;
    //#MIDP_EXCLUDE_END


    //#APIDOC_EXCLUDE_BEGIN
    /**
     * Represents the <em>deleted</em> agent state.
     */
    public static final int AP_DELETED = 6;
    /**
     * Out of band value for Agent Platform Life Cycle states.
     */
    public static final int AP_MAX = 13;    // Hand-made type checking
    /**
     * Out of band value for Domain Life Cycle states.
     */
    public static final int D_MIN = 9;     // Hand-made type checking
    /**
     * Represents the <em>active</em> agent state.
     */
    public static final int D_ACTIVE = 10;
    /**
     * Represents the <em>suspended</em> agent state.
     */
    public static final int D_SUSPENDED = 20;
    /**
     * Represents the <em>retired</em> agent state.
     */
    public static final int D_RETIRED = 30;
    /**
     * Represents the <em>unknown</em> agent state.
     */
    public static final int D_UNKNOWN = 40;
    /**
     * Out of band value for Domain Life Cycle states.
     */
    public static final int D_MAX = 41;    // Hand-made type checking
    public static final String MSG_QUEUE_CLASS = "jade_core_Agent_msgQueueClass";
    @Serial
    private static final long serialVersionUID = 3487495895819000L;
    private final Logger log = Logger.getJADELogger(this.getClass().getName());
    //#MIDP_EXCLUDE_END
    //E.ADAM map service name, service description
    protected DFAgentDescription servicesList;

    //#MIDP_EXCLUDE_BEGIN
    private transient AgentToolkit myToolkit;
    /**
     * These constants represent the various Domain Life Cycle states
     */
    private transient MessageQueue msgQueue;
    private int msgQueueMaxSize = 0;
    //#MIDP_EXCLUDE_BEGIN
    private transient boolean temporaryMessageQueue;
    private transient List<Event> o2aQueue;
    private int o2aQueueSize = 0;
    private transient Map<Event, CondVar> o2aLocks;
    //#MIDP_EXCLUDE_END
    //#APIDOC_EXCLUDE_END
    private Behaviour o2aManager = null;
    private transient Object suspendLock;
    //#J2ME_EXCLUDE_BEGIN
    private Map<Class<?>, Object> o2aInterfaces;
    private String myName = null;
    private AID myAID = null;
    private String myHap = null;
    private transient Object stateLock;
    private transient Thread myThread;
    private transient TimerDispatcher theDispatcher;
    private Scheduler myScheduler;
    //#MIDP_EXCLUDE_END
    private transient AssociationTB pendingTimers;
    //#J2ME_EXCLUDE_END
    private boolean restarting = false;
    private LifeCycle myLifeCycle;
    private LifeCycle myBufferedLifeCycle;
    private LifeCycle myActiveLifeCycle;
    private transient LifeCycle myDeletedLifeCycle;
    //#MIDP_EXCLUDE_BEGIN
    private transient LifeCycle mySuspendedLifeCycle;
    /**
     * This flag is used to distinguish the normal AP_ACTIVE state from
     * the particular case in which the agent state is set to AP_ACTIVE
     * during agent termination (takeDown()) to allow it to clean-up properly.
     * In this case in fact a call to  doDelete()  ,
     *  doMove()  ,  doClone()   and  doSuspend()
     * should have no effect.
     */
    private boolean terminating = false;
    /**
     * When set to false (default) all behaviour-related events (such as ADDED_BEHAVIOUR
     * or CHANGED_BEHAVIOUR_STATE) are not generated in order to improve performances.
     * These events in facts are very frequent.
     */
    private boolean generateBehaviourEvents = false;
    /**
     * Declared transient because the container changes in case
     * of agent migration.
     */
    private transient jade.wrapper.AgentContainer myContainer = null;
    private transient Object[] arguments = null;  // array of arguments
    /////////////////////////////
    // Mobility related code
    /////////////////////////////
    private transient AgentMobilityHelper mobHelper;
    //#CUSTOM_EXCLUDE_BEGIN
    private jade.content.ContentManager theContentManager = null;
    // All the agent's service helper
    private transient Hashtable<String, ServiceHelper> helpersTable;
    // For persistence service -- Hibernate needs java.util collections
    private transient Set<TBPair> persistentPendingTimers = new HashSet<>();

    /**
     * Default constructor.
     */
    public Agent() {
        //#MIDP_EXCLUDE_BEGIN
        myToolkit = DummyToolkit.instance();
        o2aLocks = new HashMap<>();
        suspendLock = new Object();
        temporaryMessageQueue = true;
        //#MIDP_EXCLUDE_END
        msgQueue = new InternalMessageQueue(msgQueueMaxSize, this);
        stateLock = new Object();
        pendingTimers = new AssociationTB();
        myActiveLifeCycle = new ActiveLifeCycle();
        myLifeCycle = myActiveLifeCycle;
        myScheduler = new Scheduler(this);
        theDispatcher = TimerDispatcher.getTimerDispatcher();
        //#J2ME_EXCLUDE_BEGIN
        o2aInterfaces = new Hashtable<>();
        //#J2ME_EXCLUDE_END
    }

    //#MIDP_EXCLUDE_BEGIN

    /**
     * Constructor to be used by special "agents" that will never powerUp.
     */
    Agent(AID id) {
        setAID(id);
    }
    //#MIDP_EXCLUDE_END

	/*#MIDP_INCLUDE_BEGIN
	public static MIDlet midlet;

	// Flag for agent interruption (necessary as Thread.interrupt() is not available in MIDP)
	private boolean isInterrupted = false;
	#MIDP_INCLUDE_END*/

    /**
     * Schedules a restart for a behaviour, after a certain amount of
     * time has passed.
     *
     * @param b      The behaviour to restart later.
     * @param millis The amount of time to wait before restarting
     *                b  .
     * @see Behaviour#block(long millis)
     */
    public void restartLater(Behaviour b, long millis) {
        if (millis <= 0)
            return;
        Timer t = new Timer(System.currentTimeMillis() + millis, this);
        pendingTimers.addPair(b, t);
    }

    //#MIDP_EXCLUDE_BEGIN

    /**
     * Restarts the behaviour associated with t.
     * This method runs within the time-critical Timer Dispatcher thread and
     * is not intended to be called by users. It is defined public only because
     * is part of the  TimerListener   interface.
     */
    public void doTimeOut(Timer t) {
        Behaviour b;
        // This synchronized block avoids that if a behaviour is blocked
        // again just after pendingTimers.getPeer(t) is called, a new mapping
        // is added before the old one is removed --> The new mapping is
        // removed instead of the old one.
        // In any case b.restart() must be called outside the synchronized
        // block to avoid a deadlock between the TimerDispatcher and the Scheduler.
        synchronized (theDispatcher) {
            b = pendingTimers.getPeer(t);
            if (b != null) {
                pendingTimers.removeMapping(b);
            }
        }
        if (b != null) {
            b.restart();
        } else {
            System.out.println("Warning: No mapping found for expired timer " + t.expirationTime());
        }
    }

    /**
     * Notifies this agent that one of its behaviours has been restarted
     * for some reason. This method clears any timer associated with
     * behaviour object  b  , and it is unneeded by
     * application level code. To explicitly schedule behaviours, use
     *  block()   and  restart()   methods.
     *
     * @param b The behaviour object which was restarted.
     * @see Behaviour#restart()
     */
    public void notifyRestarted(Behaviour b) {
        // Did this restart() cause the root behaviour to become runnable ?
        // If so, put the root behaviour back into the ready queue.
        Behaviour root = b.root();
        if (root.isRunnable()) {
            myScheduler.restart(root);
        }
    }

    public void removeTimer(Behaviour b) {
        // The mapping for b in general has already been removed in doTimeOut().
        // There is however a case related to ParallelBehaviours where
        // notifyRestarted() is not called as a consequence of a timer
        // expiration --> doTimeOut() is not called in this case -->
        // We remove the mapping in any case.
        Timer t = pendingTimers.getPeer(b);
        if (t != null) {
            pendingTimers.removeMapping(b);
        }
    }

    /**
     * Developer can override this method to provide an alternative message queue creation mechanism
     *
     * @return The MessageQueue to be used by this agent or null if the internal message queue must be used
     */
    protected MessageQueue createMessageQueue() {
        String msgQueueClass = getProperty(MSG_QUEUE_CLASS, null);
        if (msgQueueClass != null) {
            try {
                return (MessageQueue) Class.forName(msgQueueClass).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                System.out.println("Error loading MessageQueue of class " + msgQueueClass + " [" + e + "]");
            }
        }
        return null;
    }

    /**
     * If the agent still has a temporary message queue, create the real one and copy messages if any
     */
    void initMessageQueue() {
        if (temporaryMessageQueue) {
            temporaryMessageQueue = false;
            MessageQueue queue = createMessageQueue();
            if (queue != null) {
                queue.setMaxSize(msgQueueMaxSize);
                // Copy messages (if any) from the old message queue to the new one
                synchronized (msgQueue) {
                    int size = msgQueue.size();
                    if (size > 0) {
                        List<ACLMessage> l = new ArrayList<>(size);
                        msgQueue.copyTo(l);
                        for (ACLMessage acl : l) {
                            queue.addLast(acl);
                        }
                    }
                    msgQueue = queue;
                }
            }
        }
    }

    /**
     * This is only called by AgentContainerImpl
     */
    MessageQueue getMessageQueue() {
        return msgQueue;
    }

    // For persistence service
    private void setMessageQueue(MessageQueue mq) {
        msgQueue = mq;
    }
    //#MIDP_EXCLUDE_END

    /**
     * Return a controller for the container this agent lives in.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     *
     * @return jade.wrapper.AgentContainer a controller for the container this agent lives in.
     */
    public jade.wrapper.AgentContainer getContainerController() {
        if (myContainer == null) {  // first time called
            try {
                jade.security.JADEPrincipal principal = null;
                jade.security.Credentials credentials = null;
                try {
                    jade.security.CredentialsHelper ch = (jade.security.CredentialsHelper) getHelper("jade.core.security.Security");
                    principal = ch.getPrincipal();
                    credentials = ch.getCredentials();
                } catch (ServiceException se) {
                    // Security plug-in not present. Ignore it
                }
                myContainer = myToolkit.getContainerController(principal, credentials);
            } catch (Exception e) {
                throw new IllegalStateException("A ContainerController cannot be got for this agent. Probably the method has been called at an appropriate time before the complete initialization of the agent.");
            }
        }
        return myContainer;
    }
    //#APIDOC_EXCLUDE_BEGIN

    /**
     * Get the array of arguments passed to this agent.
     * <p> Take care that the arguments are transient and they do not
     * migrate with the agent neither are cloned with the agent!
     *
     * @return the array of arguments passed to this agent.
     * @see <a href=../../../tutorials/ArgsAndPropsPassing.htm>How to use arguments or properties to configure your agent.</a>
     **/
    public Object[] getArguments() {
        return arguments;
    }
    //#APIDOC_EXCLUDE_END

    /**
     * Called by AgentContainerImpl in order to pass arguments to a
     * just created Agent.
     * <p>Usually, programmers do not need to call this method in their code.
     *
     * @see #getArguments() how to get the arguments passed to an agent
     **/
    public final void setArguments(Object[] args) {
        // I have declared the method final otherwise getArguments would not work!
        arguments = args;
    }

    /**
     * This method returns  true   when this agent is restarting after a crash.
     * The restarting indication is automatically reset as soon as the  setup()   method of
     * this agent terminates.
     *
     * @return  true   when this agent is restarting after a crash.  false   otherwise.
     */
    public final boolean isRestarting() {
        return restarting;
    }

    void setRestarting(boolean restarting) {
        this.restarting = restarting;
    }

    /**
     * Get the Agent ID for the platform AMS.
     *
     * @return An  AID   object, that can be used to contact
     * the AMS of this platform.
     */
    public final AID getAMS() {
        return myToolkit.getAMS();
    }

    /**
     * Get the Agent ID for the platform default DF.
     *
     * @return An  AID   object, that can be used to contact
     * the default DF of this platform.
     */
    public AID getDefaultDF() {
        return myToolkit.getDefaultDF();
    }


    /**
     * Method to query the agent local name.
     *
     * @return A  String   containing the local agent name
     * (e.g. <em>peter</em>).
     */
    public final String getLocalName() {
        return myName;
    }

    /**
     * Method to query the agent complete name (<em><b>GUID</b></em>).
     *
     * @return A  String   containing the complete agent name
     * (e.g. <em>peter@fipa.org:50</em>).
     */
    public final String getName() {
        if (myHap != null) {
            return myName + '@' + myHap;
        } else {
            return myName;
        }
    }

    /**
     * Method to query the Home Agent Platform. This is the name of
     * the platform where the agent has been created, therefore it will
     * never change during the entire lifetime of the agent.
     * In JADE the name of an agent by default is composed by the
     * concatenation (using '@') of the agent local name and the Home
     * Agent Platform name
     *
     * @return A  String   containing the name of the home agent platform
     * (e.g. <em>myComputerName:1099/JADE</em>).
     */
    public final String getHap() {
        return myHap;
    }

    /**
     * Method to query the private Agent ID. Note that this Agent ID is
     * <b>different</b> from the one that is registered with the
     * platform AMS.
     *
     * @return An  Agent ID   object, containing the complete
     * agent GUID, addresses and resolvers.
     */
    public final AID getAID() {
        return myAID;
    }

    void setAID(AID id) {
        myName = id.getLocalName();
        myHap = id.getHap();
        myAID = id;
    }

    /**
     * Method to build a complete agent GUID belonging to the same platform of the current agent
     *
     * @param name Agent local name.
     * @return An  Agent ID   object, containing the complete
     * agent GUID, addresses and resolvers.
     */
    public final AID getAID(String name) {
        String guid = AID.createGUID(name, getHap());
        AID result = new AID(guid, AID.ISGUID);
        Iterator<String> it = myAID.getAllAddresses();
        while (it.hasNext()) {
            result.addAddresses(it.next());
        }
        Iterator<AID> it2 = myAID.getAllResolvers();
        while (it2.hasNext()) {
            result.addResolvers(it2.next());
        }
        return result;
    }

    /**
     * Returns true if the agent implemented by this Agent object is alive (regardless of the
     * actual agent internal state).
     * This means that the internal Thread of this Agent object exists and is alive.
     *
     * @return true if the agent implemented by this Agent object is alive
     */
    public boolean isAlive() {
        if (myThread != null) {
            return myThread.isAlive();
        } else {
            return false;
        }
    }

    /**
     * This method adds a new platform address to the AID of this Agent.
     * It is called by the container when a new MTP is activated
     * in the platform (in the local container - installMTP() -
     * or in a remote container - updateRoutingTable()) to keep the
     * Agent AID updated.
     */
    synchronized void addPlatformAddress(String address) { // Mutual exclusion with Agent.powerUp()
        if (myAID != null) {
            // Cloning the AID is necessary as the agent may be using its AID.
            // If this is the case a ConcurrentModificationException would be thrown
            myAID = (AID) myAID.clone();
            myAID.addAddresses(address);
        }
    }

    /**
     * This method removes an old platform address from the AID of this Agent.
     * It is called by the container when a new MTP is deactivated
     * in the platform (in the local container - uninstallMTP() -
     * or in a remote container - updateRoutingTable()) to keep the
     * Agent AID updated.
     */
    synchronized void removePlatformAddress(String address) { // Mutual exclusion with Agent.powerUp()
        if (myAID != null) {
            // Cloning the AID is necessary as the agent may be using its AID.
            // If this is the case a ConcurrentModificationException would be thrown
            myAID = (AID) myAID.clone();
            myAID.removeAddresses(address);
        }
    }

    /**
     * Method to retrieve the location this agent is currently at.
     *
     * @return A  Location   object, describing the location
     * where this agent is currently running.
     */
    public Location here() {
        return myToolkit.here();
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * This method is used internally by the framework and should NOT be used by programmers.
     * This is used by the agent container to wait for agent termination.
     * We have already called doDelete on the thread which would have
     * issued an interrupt on it. However, it still may decide not to exit.
     * So we will wait no longer than 5 seconds for it to exit and we
     * do not care of this zombie agent.
     * FIXME: we must further isolate container and agents, for instance
     * by using custom class loader and dynamic proxies and JDK 1.3.
     * FIXME: the timeout value should be got by Profile
     */
    public boolean join() {
        //#MIDP_EXCLUDE_BEGIN
        try {
            if (myThread == null) {
                return true;
            }
            myThread.join(5000);
            if (!myThread.isAlive()) {
                return true;
            } else {
                StringBuilder sb = new StringBuilder("*** Agent " + myName + " did not terminate when requested to do so.");
                //#J2ME_EXCLUDE_BEGIN
                StackTraceElement[] ss = myThread.getStackTrace();
                if (ss != null && ss.length > 0) {
                    sb.append(" Agent Thread is in method ").append(ss[0]).append("\n");
                    sb.append("*** Full stack trace is\n");
                    for (StackTraceElement s : ss) {
                        sb.append("*** \t at ").append(s).append("\n");
                    }
                }
                //#J2ME_EXCLUDE_END
                log.log(Logger.WARNING, sb.toString());
                if (!myThread.equals(Thread.currentThread())) {
                    myThread.interrupt();
                    log.log(Logger.WARNING, "*** Second interrupt issued.");
                }
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        //#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 if (myThread != null && myThread.isAlive()) {
		 try {
		 myThread.join();
		 return true;
		 } 
		 catch (InterruptedException ie) {
		 ie.printStackTrace();
		 } 
		 } 
		 #MIDP_INCLUDE_END*/
        return false;
    }
    //#APIDOC_EXCLUDE_END

    /**
     * This method retrieves the current length of the message queue
     * of this agent.
     *
     * @return The number of messages that are currently stored into the
     * message queue.
     **/
    public int getCurQueueSize() {
        return msgQueue.size();
    }

    /**
     * Reads message queue size. A zero value means that the message
     * queue is unbounded (its size is limited only by amount of
     * available memory).
     *
     * @return The actual size of the message queue (i.e. the max number
     * of messages that can be stored into the queue)
     * @see Agent#setQueueSize(int newSize)
     * @see Agent#getCurQueueSize()
     */
    public int getQueueSize() {
        return msgQueue.getMaxSize();
    }

    /**
     * Set message queue size. This method allows to change the number
     * of ACL messages that can be buffered before being actually read
     * by the agent or discarded.
     *
     * @param newSize A non negative integer value to set message queue
     *                size to. Passing 0 means unlimited message queue.  When the number of
     *                buffered
     *                messages exceeds this value, older messages are discarded
     *                according to a <b><em>FIFO</em></b> replacement policy.
     * @throws IllegalArgumentException If  newSize   is negative.
     * @see Agent#getQueueSize()
     */
    public void setQueueSize(int newSize) throws IllegalArgumentException {
        msgQueue.setMaxSize(newSize);
        msgQueueMaxSize = newSize;
    }

    /////////////////////////////////
    // Agent state management
    /////////////////////////////////
    public void changeStateTo(LifeCycle newLifeCycle) {
        boolean changed = false;
        newLifeCycle.setAgent(this);
        synchronized (stateLock) {
            if (!myLifeCycle.equals(newLifeCycle)) {
                // The new state is actually different from the current one
                if (myLifeCycle.transitionTo(newLifeCycle)) {
                    myBufferedLifeCycle = myLifeCycle;
                    myLifeCycle = newLifeCycle;
                    changed = true;
                    //#MIDP_EXCLUDE_BEGIN
                    notifyChangedAgentState(myBufferedLifeCycle.getState(), myLifeCycle.getState());
                    //#MIDP_EXCLUDE_END
                }
            }
        }
        if (changed) {
            myLifeCycle.transitionFrom(myBufferedLifeCycle);
            if (!Thread.currentThread().equals(myThread)) {
                // If the state-change is forced from the outside, interrupt
                // the agent thread to allow the state change to take place
                interruptThread();
            }
        }
    }

    public void restoreBufferedState() {
        changeStateTo(myBufferedLifeCycle);
    }

    /**
     * The ActiveLifeCycle handles different internal states (INITIATED,
     * ACTIVE, WAITING, IDLE). This method switches between them.
     */
    private void setActiveState(int newState) {
        synchronized (stateLock) {
            if (myLifeCycle == myActiveLifeCycle) {
                int oldState = myLifeCycle.getState();
                if (newState != oldState) {
                    ((ActiveLifeCycle) myLifeCycle).setState(newState);
                    //#MIDP_EXCLUDE_BEGIN
                    notifyChangedAgentState(oldState, newState);
                    //#MIDP_EXCLUDE_END
                }
            } else {
                // A change state request arrived in the meanwhile.
                // Let it take place.
                throw new Interrupted();
            }
        }
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * Read current agent state. This method can be used to query an
     * agent for its state from the outside.
     *
     * @return the Agent Platform Life Cycle state this agent is currently in.
     */
    public int getState() {
        return myLifeCycle.getState();
    }
    //#APIDOC_EXCLUDE_END


    //#MIDP_EXCLUDE_BEGIN
    public AgentState getAgentState() {
        return AgentState.getInstance(getState());
    }

    /**
     * This is only called by the NotificationService to provide the Introspector
     * agent with a snapshot of the behaviours currently loaded in the agent
     */
    Scheduler getScheduler() {
        return myScheduler;
    }

    private void initMobHelper() throws ServiceException {
        if (mobHelper == null) {
            mobHelper = (AgentMobilityHelper) getHelper(AgentMobilityHelper.NAME);
            mobHelper.registerMovable(new Movable() {
                public void beforeMove() {
                    Agent.this.beforeMove();
                }

                public void afterMove() {
                    Agent.this.afterMove();
                }

                public void beforeClone() {
                    Agent.this.beforeClone();
                }

                public void afterClone() {
                    Agent.this.afterClone();
                }
            });
        }
    }

    /**
     * Make this agent move to a remote location. This method
     * is intended to support agent mobility and is called either by the
     * Agent Platform or by the agent itself to start a migration process.
     * It should be noted that this method just changes the agent
     * state to  AP_TRANSIT  . The actual migration takes
     * place asynchronously.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     *
     * @param destination The  Location   to migrate to.
     */
    public void doMove(Location destination) {
        // Do nothing if the mobility service is not installed
        try {
            initMobHelper();
            mobHelper.move(destination);
        } catch (ServiceException se) {
            // FIXME: Log a proper warning
        }
    }

    /**
     * Make this agent be cloned on another location. This method
     * is intended to support agent mobility and is called either by the
     * Agent Platform or by the agent itself to start a clonation process.
     * It should be noted that this method just changes the agent
     * state to  AP_COPY  . The actual clonation takes
     * place asynchronously.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     *
     * @param destination The  Location   where the copy agent will start.
     * @param newName     The name that will be given to the copy agent.
     */
    public void doClone(Location destination, String newName) {
        // Do nothing if the mobility service is not installed
        try {
            initMobHelper();
            mobHelper.clone(destination, newName);
        } catch (ServiceException se) {
            // FIXME: Log a proper warning
        }
    }

    /**
     * Make a state transition from <em>active</em> or <em>waiting</em>
     * to <em>suspended</em> within Agent Platform Life Cycle; the
     * original agent state is saved and will be restored by a
     *  doActivate()   call. This method can be called from
     * the Agent Platform or from the agent itself and stops all agent
     * activities. Incoming messages for a suspended agent are buffered
     * by the Agent Platform and are delivered as soon as the agent
     * resumes. Calling  doSuspend()   on a suspended agent
     * has no effect.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     *
     * @see Agent#doActivate()
     */
    public void doSuspend() {
        //#MIDP_EXCLUDE_BEGIN
        if (mySuspendedLifeCycle == null) {
            mySuspendedLifeCycle = new SuspendedLifeCycle();
        }
        changeStateTo(mySuspendedLifeCycle);
        //#MIDP_EXCLUDE_END
    }
    //#MIDP_EXCLUDE_END

    /**
     * Make a state transition from <em>suspended</em> to
     * <em>active</em> or <em>waiting</em> (whichever state the agent
     * was in when  doSuspend()   was called) within Agent
     * Platform Life Cycle. This method is called from the Agent
     * Platform and resumes agent execution. Calling
     *  doActivate()   when the agent is not suspended has no
     * effect.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     *
     * @see Agent#doSuspend()
     */
    public void doActivate() {
        //#MIDP_EXCLUDE_BEGIN
        //doExecute();
        restoreBufferedState();
        //#MIDP_EXCLUDE_END
    }

    /**
     * Make a state transition from <em>active</em> to <em>waiting</em>
     * within Agent Platform Life Cycle. This method has only effect
     * when called by the agent thread and causes the agent to
     * block, stopping all its activities until
     * a message arrives or 	the
     *  doWake()   method is called.
     *
     * @see Agent#doWake()
     */
    public void doWait() {
        doWait(0);
    }

    /**
     * Make a state transition from <em>active</em> to <em>waiting</em>
     * within Agent Platform Life Cycle. This method adds a timeout to
     * the other  doWait()   version.
     *
     * @param millis The timeout value, in milliseconds.
     * @see Agent#doWait()
     */
    public void doWait(long millis) {
        if (Thread.currentThread().equals(myThread)) {
            setActiveState(AP_WAITING);

            synchronized (msgQueue) {
                try {
                    // Blocks on msgQueue monitor for a while
                    waitOn(msgQueue, millis);
                } catch (InterruptedException ie) {
                    if (myLifeCycle != myActiveLifeCycle && !terminating) {
                        // Change state request from the outside
                        throw new Interrupted();
                    } else {
                        // Spurious wake up. Just print a warning
                        System.out.println("Agent " + getName() + " interrupted while waiting");
                    }
                }
                setActiveState(AP_ACTIVE);
            }
        }
    }

    /**
     * Make a state transition from <em>waiting</em> to <em>active</em>
     * within Agent Platform Life Cycle. This method is called from
     * Agent Platform and resumes agent execution. Calling
     *  doWake()   when an agent is not waiting has no effect.
     *
     * @see Agent#doWait()
     */
    public void doWake() {
        synchronized (stateLock) {
            int previous = myLifeCycle.getState();
            if ((previous == AP_WAITING) || (previous == AP_IDLE)) {
                setActiveState(AP_ACTIVE);
            }
        }
        if (myLifeCycle.isMessageAware()) {
            activateAllBehaviours();
            synchronized (msgQueue) {
                msgQueue.notifyAll(); // Wakes up the embedded thread
            }
        }
    }

    /**
     * Make a state transition from <em>active</em>, <em>suspended</em>
     * or <em>waiting</em> to <em>deleted</em> state within Agent
     * Platform Life Cycle, thereby destroying the agent. This method
     * can be called either from the Agent Platform or from the agent
     * itself. Calling  doDelete()   on an already deleted
     * agent has no effect.
     */
    public void doDelete() {
        if (myDeletedLifeCycle == null) {
            myDeletedLifeCycle = new DeletedLifeCycle();
        }
        changeStateTo(myDeletedLifeCycle);
    }

    // This is called only by the scheduler
    void idle() throws InterruptedException {
        setActiveState(AP_IDLE);
        // No need for synchronized block since this is only called by the
        // scheduler in the synchronized schedule() method
        waitOn(myScheduler, 0);
        setActiveState(AP_ACTIVE);
    }

    /**
     * Write this agent to an output stream; this method can be used to
     * record a snapshot of the agent state on a file or to send it
     * through a network connection. Of course, the whole agent must
     * be serializable in order to be written successfully.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     *
     * @param s The stream this agent will be sent to. The stream is
     *          <em>not</em> closed on exit.
     * @throws IOException Thrown if some I/O error occurs during
     *                     writing.
     *                     see Agent#read(InputStream s)
     */
    public void write(OutputStream s) throws IOException {
        ObjectOutput out = new ObjectOutputStream(s);
        out.writeUTF(myName);
        out.writeObject(this);
    }

    //#MIDP_EXCLUDE_BEGIN

    /**
     * This method reads a previously saved agent, replacing the current
     * state of this agent with the one previously saved. The stream
     * must contain the saved state of <b>the same agent</b> that it is
     * trying to restore itself; that is, <em>both</em> the Java object
     * <em>and</em> the agent name must be the same.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     *
     * @param s The input stream the agent state will be read from.
     * @throws IOException Thrown if some I/O error occurs during
     *                     stream reading.
     *                     <em>Note: This method is currently not implemented</em>
     */
    public void restore(InputStream s) throws IOException {
        // FIXME: Not implemented
    }

    /**
     Read a previously saved agent from an input stream and restarts
     it under its former name. This method can realize some sort of
     mobility through time, where an agent is saved, then destroyed
     and then restarted from the saved copy.
     <br>
     <b>NOT available in MIDP</b>
     <br>
     @param s The stream the agent will be read from. The stream is
     <em>not</em> closed on exit.
     @exception IOException Thrown if some I/O error occurs during
     stream reading.
     @see Agent#write(OutputStream s)
      *
     public static void read(InputStream s) throws IOException {
     try {
     ObjectInput in = new ObjectInputStream(s);
     String name = in.readUTF();
     Agent a = (Agent)in.readObject();
     a.doStart(name);
     }
     catch(ClassNotFoundException cnfe) {
     cnfe.printStackTrace();
     }
     }*/

    /**
     Read a previously saved agent from an input stream and restarts
     it under a different name. This method can realize agent cloning
     through streams, where an agent is saved, then an exact copy of
     it is restarted as a completely separated agent, with the same
     state but with different identity and address.
     <br>
     <b>NOT available in MIDP</b>
     <br>
     @param s The stream the agent will be read from. The stream is
     <em>not</em> closed on exit.
     @param agentName The name of the new agent, copy of the saved
     original one.
     @exception IOException Thrown if some I/O error occurs during
     stream reading.
     @see Agent#write(OutputStream s)
      *
     public static void read(InputStream s, String agentName) throws IOException {
     try {
     ObjectInput in = new ObjectInputStream(s);
     String oldName = in.readUTF();
     Agent a = (Agent)in.readObject();
     a.doStart(agentName);
     }
     catch(ClassNotFoundException cnfe) {
     cnfe.printStackTrace();
     }
     }*/

    /**
     * This method should not be used by application code. Use the
     * same-named method of  jade.wrapper.Agent   instead.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     *
     * @see jade.wrapper.AgentController#putO2AObject(Event o, boolean blocking)
     */
    public void putO2AObject(Event o, boolean blocking) throws InterruptedException {
        // Drop object on the floor if object-to-agent communication is
        // disabled.
        if (o2aQueue == null)
            return;

        CondVar cond = null;
        // the following block is synchronized because o2aQueue.add and o2aLocks.put must be
        // an atomic operation
        synchronized (o2aQueue) {
            // If the queue has a limited capacity and it is full, discard the
            // first element
            if ((o2aQueueSize != 0) && (o2aQueue.size() == o2aQueueSize))
                o2aQueue.remove(0);

            o2aQueue.add(o);

            // If we are going to block, then activate behaviours after storing the CondVar object
            if (blocking) {
                cond = new CondVar();

                // Store lock for later, when getO2AObject will be called
                o2aLocks.put(o, cond);
            }
        } // end synchronization
        // Reactivate the O2AManager if any or the whole agent if no O2AManager is set.
        if (o2aManager == null) {
            // This method is synchronized on the scheduler
            activateAllBehaviours();
        } else {
            o2aManager.restart();
        }
        if (blocking)
            // Sleep on the condition. This method is synchronized on the condvar
            cond.waitOn();
    }

    /**
     * This method picks an object (if present) from the internal
     * object-to-agent communication queue. In order for this method to
     * work, the agent must have declared its will to accept objects
     * from other software components running within its JVM. This can
     * be achieved by calling the
     *  jade.core.Agent.setEnabledO2ACommunication()   method.
     * If the retrieved object was originally inserted by an external
     * component using a blocking call, that call will return during the
     * execution of this method.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     *
     * @return the first object in the queue, or  null   if
     * the queue is empty.
     * @see jade.wrapper.AgentController#putO2AObject(Event o, boolean blocking)
     * @see Agent#setEnabledO2ACommunication(boolean enabled, int queueSize)
     */
    public Object getO2AObject() {

        // Return 'null' if object-to-agent communication is disabled
        if (o2aQueue == null)
            return null;

        CondVar cond;
        Event result;
        synchronized (o2aQueue) {
            if (o2aQueue.isEmpty())
                return null;

            // Retrieve the first object from the object-to-agent
            // communication queue
            result = o2aQueue.remove(0);

            // If some thread issued a blocking putO2AObject() call with this
            // object, wake it up. cond.set is synchronized on CondVar object
            cond = o2aLocks.remove(result);
        }

        if (cond != null) {
            cond.set();
        }

        return result;

    }

    /**
     * This method declares this agent attitude towards object-to-agent
     * communication, that is, whether the agent accepts to communicate
     * with other non-JADE components living within the same JVM.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     *
     * @param enabled   Tells whether Java objects inserted with
     *                   putO2AObject()   will be accepted.
     * @param queueSize If the object-to-agent communication is enabled,
     *                  this parameter specifiies the maximum number of Java objects that
     *                  will be queued. If the passed value is 0, no maximum limit is set
     *                  up for the queue.
     * @see jade.wrapper.AgentController#putO2AObject(Event o, boolean blocking)
     * see getO2AObject()
     */
    public void setEnabledO2ACommunication(boolean enabled, int queueSize) {
        if (enabled) {
            if (o2aQueue == null)
                o2aQueue = new ArrayList<>(queueSize);

            // Ignore a negative value
            if (queueSize >= 0)
                o2aQueueSize = queueSize;
        } else {

            // Wake up all threads blocked in putO2AObject() calls
            for (CondVar cv : o2aLocks.values()) {
                if (cv != null) cv.set();
            }

            o2aQueue = null;
        }

    }

    /**
     * Sets the behaviour responsible for managing objects passed to the agent by
     * means of the Object-To-Agent (O2A) communication mechanism.
     * If the O2A manager behaviour is set, whenever an object is inserted in the
     * O2A queue by means of the  putO2AObject()   method, only the manager
     * is waken up. This improves the efficiency since all behaviours not interested in
     * O2A communication remain sleeping. <br>
     * NOTE that this method only declares a behaviour as being responsible for managing
     * objects received by the agent by means of the O2A communication channel; in order to
     * correctly run, the behaviour must be added to the agent by means of the
     *  addBehaviour()  method as usual.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     *
     * @param b The behaviour that will act as O2A manager.
     * @see jade.wrapper.AgentController#putO2AObject(Event o, boolean blocking)
     * see getO2AObject()
     */
    public void setO2AManager(Behaviour b) {
        o2aManager = b;
    }

    /**
     * Used internally by the framework
     */
    @SuppressWarnings("unchecked")
    public <T> T getO2AInterface(Class<T> theInterface) {
        return (T) o2aInterfaces.get(theInterface);
    }
    //#MIDP_EXCLUDE_END

    //#J2ME_EXCLUDE_BEGIN

    /**
     * Registers an implementation for a given O2A interface. All invocations
     * to methods of the O2A interface will be redirected to the registered implementation object.
     *
     * @param theInterface   The O2A interface the implementation is registered for.
     * @param implementation The object providing an implementation for the given O2A interface.
     */
    public <T> void registerO2AInterface(Class<T> theInterface, T implementation) {
        o2aInterfaces.put(theInterface, implementation);
    }

    /**
     * This method is the main body of every agent. It
     * provides startup and cleanup hooks for application
     * programmers to put their specific code into.
     *
     * @see Agent#setup()
     * @see Agent#takeDown()
     */
    public final void run() {
        try {
            myLifeCycle.init();
            while (myLifeCycle.alive()) {
                try {
                    myLifeCycle.execute();
                    // Let other agents go on
                    Thread.yield();
                } catch (JADESecurityException jse) {
                    // FIXME: maybe we should send a message to the agent
                    System.out.println("JADESecurityException: " + jse.getMessage());
                } catch (InterruptedException | Interrupted | InterruptedIOException ie) {
                    // Change LC state request from the outside. Just do nothing
                    // and let the new LC state do its job
                }
            }
        } catch (Throwable t) {
            System.err.println("***  Uncaught Exception for agent " + myName + "  ***");
            t.printStackTrace();
        }
        terminating = true;
        myLifeCycle.end();
    }
    //#J2ME_EXCLUDE_END


    //#APIDOC_EXCLUDE_BEGIN

    //#APIDOC_EXCLUDE_BEGIN
    public void clean(boolean ok) {
        if (!ok) {
            System.out.println("ERROR: Agent " + myName + " died without being properly terminated !!!");
            System.out.println("State was " + myLifeCycle.getState());
        }
        //#MIDP_EXCLUDE_BEGIN
        // Reset the interrupted state of the Agent Thread
        Thread.interrupted();
        //#MIDP_EXCLUDE_END

        myBufferedLifeCycle = myLifeCycle;
        myLifeCycle = myActiveLifeCycle;
        takeDown();
        pendingTimers.clear();
        myToolkit.handleEnd(myAID);
        myLifeCycle = myBufferedLifeCycle;
    }
    //#APIDOC_EXCLUDE_END

    /**
     * This protected method is an empty placeholder for application
     * specific startup code. Agent developers can override it to
     * provide necessary behaviour. When this method is called the agent
     * has been already registered with the Agent Platform <b>AMS</b>
     * and is able to send and receive messages. However, the agent
     * execution model is still sequential and no behaviour scheduling
     * is active yet.
     * <p>
     * This method can be used for ordinary startup tasks such as
     * <b>DF</b> registration, but is essential to add at least a
     *  Behaviour   object to the agent, in order for it to be
     * able to do anything.
     *
     * @see Agent#addBehaviour(Behaviour b)
     * @see Behaviour
     */
    protected void setup() {
    }

    /**
     * This protected method is an empty placeholder for application
     * specific cleanup code. Agent developers can override it to
     * provide necessary behaviour. When this method is called the agent
     * has not deregistered itself with the Agent Platform <b>AMS</b>
     * and is still able to exchange messages with other
     * agents. However, no behaviour scheduling is active anymore and
     * the Agent Platform Life Cycle state is already set to
     * <em>deleted</em>.
     * <p>
     * This method can be used for ordinary cleanup tasks such as
     * <b>DF</b> deregistration, but explicit removal of all agent
     * behaviours is not needed.
     */
    protected void takeDown() {
    }

    /**print a text on the console*/
    protected void println(String text) {out.println(text);}

    /**print a formatted text with values on the console*/
    protected void printf(String format, Object[] tabO) {out.printf(format, tabO);}

    //#MIDP_EXCLUDE_BEGIN

    /**
     * This empty placeholder shall be overridden by user defined agents
     * to execute some actions before the original agent instance on the
     * source container is stopped (e.g. releasing local resources such
     * as a GUI).<br>
     * <b>IMPORTANT:</b> At this point, it is ensured that the move process
     * is successful and that a moved agent instance has been created on the
     * destination container
     * Therefore setting the value of a class field in this method will have
     * no impact on the moved agent instance. Such parameters must indeed be
     * set <b>before</b> the  doMove()   method is called.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     */
    protected void beforeMove() {
    }

    //#MIDP_EXCLUDE_END

    /**
     * Actions to perform after moving. This empty placeholder method can be
     * overridden by user defined agents to execute some actions just after
     * arriving to the destination agent container for a migration.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     */
    protected void afterMove() {
    }
    //#APIDOC_EXCLUDE_END

    /**
     * This empty placeholder method shall be overridden by user defined agents
     * to execute some actions before copying an agent to another agent container.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     * see beforeMove()
     * see afterClone()
     */
    protected void beforeClone() {
    }

    /**
     * Actions to perform after cloning. This empty placeholder method can be
     * overridden by user defined agents to execute some actions just after
     * creating an agent copy to the destination agent container.
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     */
    protected void afterClone() {
    }

    //#MIDP_EXCLUDE_BEGIN

    // This method is used by the Agent Container to fire up a new agent for the first time
    // Mutual exclusion with itself and Agent.addPlatformAddress()
    synchronized void powerUp(AID id, Thread t) {
        if (myThread == null) {
            // Set this agent's name and address and start its embedded thread
            myName = id.getLocalName();
            myHap = id.getHap();

            myAID = id;
            myToolkit.setPlatformAddresses(myAID);

            myThread = t;
            myThread.start();
        }
    }

    //#J2ME_EXCLUDE_BEGIN
    // Return agent thread
    // Package scooped as it is called by JadeMisc add-on for container monitor purpose
    Thread getThread() {
        return myThread;
    }

    //#MIDP_EXCLUDE_BEGIN
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        // Updates the queue maximum size field, before serialising
        msgQueueMaxSize = msgQueue.getMaxSize();

        out.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // Restore transient fields apart from myThread, that will be set when the agent will be powered up)
        stateLock = new Object();
        suspendLock = new Object();
        pendingTimers = new AssociationTB();
        theDispatcher = TimerDispatcher.getTimerDispatcher();
        // restore O2AQueue
        if (o2aQueueSize > 0)
            o2aQueue = new ArrayList<>(o2aQueueSize);
        o2aLocks = new HashMap<>();
        myToolkit = DummyToolkit.instance();
        temporaryMessageQueue = true;
        msgQueue = new InternalMessageQueue(msgQueueMaxSize, this);

        //#PJAVA_EXCLUDE_BEGIN
        //For persistence service
        persistentPendingTimers = new HashSet<>();
        //#PJAVA_EXCLUDE_END
    }
    //#MIDP_EXCLUDE_END

    /**
     * This method is executed when blockingReceive() is called
     * from a separate Thread.
     * It does not affect the agent state.
     */
    private void waitUntilWake(long millis) {
        synchronized (msgQueue) {
            try {
                // Blocks on msgQueue monitor for a while
                waitOn(msgQueue, millis);
            } catch (InterruptedException ie) {
                throw new Interrupted();
            }
        }
    }

    //#MIDP_EXCLUDE_BEGIN
    private void waitUntilActivate() throws InterruptedException {
        synchronized (suspendLock) {
            waitOn(suspendLock, 0);
        }
    }
    //#J2ME_EXCLUDE_END

    /**
     * This method adds a new behaviour to the agent. This behaviour
     * will be executed concurrently with all the others, using a
     * cooperative round robin scheduling.  This method is typically
     * called from an agent  setup()   to fire off some
     * initial behaviour, but can also be used to spawn new behaviours
     * dynamically.
     *
     * @param b The new behaviour to add to the agent.
     * @see Agent#setup()
     * @see Behaviour
     */
    public void addBehaviour(Behaviour b) {
        b.setAgent(this);
        myScheduler.add(b);
    }


    /**
     * This method removes a given behaviour from the agent. This method
     * is called automatically when a top level behaviour terminates,
     * but can also be called from a behaviour to terminate itself or
     * some other behaviour.
     *
     * @param b The behaviour to remove.
     * @see Behaviour
     */
    public void removeBehaviour(Behaviour b) {
        myScheduler.remove(b);
        b.setAgent(null);
    }
    //#MIDP_EXCLUDE_END

    /**
     * Send an <b>ACL</b> message to another agent. This methods sends
     * a message to the agent specified in  :receiver
     * message field (more than one agent can be specified as message
     * receiver).
     *
     * @param msg An ACL message object containing the actual message to
     *            send.
     * @see ACLMessage
     */
    public final void send(ACLMessage msg) {
        // Set the sender of the message if not yet set
        // FIXME. Probably we should always set the sender of the message!
        try {
            msg.getSender().getName().charAt(0);
        } catch (Exception e) {
            msg.setSender(myAID);
        }
        boolean cloneMessage = !("true".equals(msg.clearUserDefinedParameter(ACLMessage.NO_CLONE)));
        myToolkit.handleSend(msg, myAID, cloneMessage);
    }

    /**
     * Receives an <b>ACL</b> message from the agent message
     * queue. This method is non-blocking and returns the first message
     * in the queue, if any. Therefore, polling and busy waiting is
     * required to wait for the next message sent using this method.
     *
     * @return A new ACL message, or  null   if no message is
     * present.
     * @see ACLMessage
     */
    public final ACLMessage receive() {
        return receive(null);
    }
    //#MIDP_EXCLUDE_END

    /**
     * Receives an <b>ACL</b> message matching a given template. This
     * method is non-blocking and returns the first matching message in
     * the queue, if any. Therefore, polling and busy waiting is
     * required to wait for a specific kind of message using this method.
     *
     * @param pattern A message template to match received messages
     *                against.
     * @return A new ACL message matching the given template, or
     *  null   if no such message is present.
     * @see ACLMessage
     * @see MessageTemplate
     */
    public final ACLMessage receive(MessageTemplate pattern) {
        ACLMessage msg = null;
        synchronized (msgQueue) {
            msg = msgQueue.receive(pattern);
            //#MIDP_EXCLUDE_BEGIN
            if (msg != null) {
                myToolkit.handleReceived(myAID, msg);
            }
            //#MIDP_EXCLUDE_END
        }
        return msg;
    }

    /**
     * Receives an <b>ACL</b> message from the agent message
     * queue. This method is blocking and suspends the whole agent until
     * a message is available in the queue.
     *
     * @return A new ACL message, blocking the agent until one is
     * available.
     * @see Agent#receive()
     * @see ACLMessage
     */
    public final ACLMessage blockingReceive() {
        return blockingReceive(null, 0);
    }

    /**
     * Receives an <b>ACL</b> message from the agent message queue,
     * waiting at most a specified amount of time.
     *
     * @param millis The maximum amount of time to wait for the message.
     * @return A new ACL message, or  null   if the specified
     * amount of time passes without any message reception.
     */
    public final ACLMessage blockingReceive(long millis) {
        return blockingReceive(null, millis);
    }

    /**
     * Receives an <b>ACL</b> message matching a given message
     * template. This method is blocking and suspends the whole agent
     * until a message is available in the queue.
     *
     * @param pattern A message template to match received messages
     *                against.
     * @return A new ACL message matching the given template, blocking
     * until such a message is available.
     * @see Agent#receive(MessageTemplate)
     * @see ACLMessage
     * @see MessageTemplate
     */
    public final ACLMessage blockingReceive(MessageTemplate pattern) {
        return blockingReceive(pattern, 0);
    }

    /**
     * Receives an <b>ACL</b> message matching a given message template,
     * waiting at most a specified time.
     *
     * @param pattern A message template to match received messages
     *                against.
     * @param millis  The amount of time to wait for the message, in
     *                milliseconds.
     * @return A new ACL message matching the given template, or
     *  null   if no suitable message was received within
     *  millis   milliseconds.
     * @see Agent#blockingReceive()
     */
    public final ACLMessage blockingReceive(MessageTemplate pattern, long millis) {
        ACLMessage msg;
        synchronized (msgQueue) {
            msg = receive(pattern);
            long timeToWait = millis;
            while (msg == null) {
                long startTime = System.currentTimeMillis();
                if (Thread.currentThread().equals(myThread)) {
                    doWait(timeToWait);
                } else {
                    // blockingReceive() called from an external thread --> Do not change the agent state
                    waitUntilWake(timeToWait);
                }
                long elapsedTime = System.currentTimeMillis() - startTime;

                msg = receive(pattern);

                if (millis != 0) {
                    timeToWait -= elapsedTime;
                    if (timeToWait <= 0)
                        break;
                }
            }
        }
        return msg;
    }

    /**
     * Puts a received <b>ACL</b> message back into the message
     * queue. This method can be used from an agent behaviour when it
     * realizes it read a message of interest for some other
     * behaviour. The message is put in front of the message queue, so
     * it will be the first returned by a  receive()   call.
     *
     * @see Agent#receive()
     */
    public final void putBack(ACLMessage msg) {
        synchronized (msgQueue) {
            msgQueue.addFirst(msg);
        }
    }

    final void setToolkit(AgentToolkit at) {
        myToolkit = at;
    }

    final void resetToolkit() {
        //#MIDP_EXCLUDE_BEGIN
        myToolkit = DummyToolkit.instance();
        //#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 myToolkit = null;
		 #MIDP_INCLUDE_END*/
    }

    /**
     * This method blocks until the agent has finished its start-up phase
     * (i.e. until just before its setup() method is called.
     * When this method returns, the target agent is registered with the
     * AMS and the JADE platform is aware of it.
     */
    public synchronized void waitUntilStarted() {
        while (myLifeCycle.getState() == AP_INITIATED) {
            try {
                wait();
            } catch (InterruptedException ie) {
                // Do nothing...
            }
        }
    }

    // Notify creator that the start-up phase has completed
    private synchronized void notifyStarted() {
        notifyAll();
    }

    // Notify toolkit of the added behaviour
    // Package scooped as it is called by the Scheduler
    void notifyAddBehaviour(Behaviour b) {
        if (generateBehaviourEvents) {
            myToolkit.handleBehaviourAdded(myAID, b);
        }
    }

    // Notify the toolkit of the removed behaviour
    // Package scooped as it is called by the Scheduler
    void notifyRemoveBehaviour(Behaviour b) {
        if (generateBehaviourEvents) {
            myToolkit.handleBehaviourRemoved(myAID, b);
        }
    }


    //#MIDP_EXCLUDE_BEGIN
    //#APIDOC_EXCLUDE_BEGIN

    // Notify the toolkit of the change in behaviour state
    // Public as it is called by the Scheduler and by the Behaviour class
    public void notifyChangeBehaviourState(Behaviour b, String from, String to) {
        b.setExecutionState(to);
        if (generateBehaviourEvents) {
            myToolkit.handleChangeBehaviourState(myAID, b, from, to);
        }
    }
    //#APIDOC_EXCLUDE_END

    // For persistence service
    private boolean getGenerateBehaviourEvents() {
        return generateBehaviourEvents;
    }

    public void setGenerateBehaviourEvents(boolean b) {
        generateBehaviourEvents = b;
    }

    // Notify toolkit that the current agent has changed its state
    private void notifyChangedAgentState(int oldState, int newState) {
        myToolkit.handleChangedAgentState(myAID, oldState, newState);
    }


    //#APIDOC_EXCLUDE_BEGIN

    private void activateAllBehaviours() {
        myScheduler.restartAll();
    }

    /**
     * Put a received message into the agent message queue. The message
     * is put at the back end of the queue. This method is called by
     * JADE runtime system when a message arrives, but can also be used
     * by an agent, and is just the same as sending a message to oneself
     * (though slightly faster).
     *
     * @param msg The ACL message to put in the queue.
     * @see Agent#send(ACLMessage msg)
     */
    public final void postMessage(final ACLMessage msg) {
        msg.setPostTimeStamp(System.currentTimeMillis());
        synchronized (msgQueue) {
            if (msg != null) {
                //#MIDP_EXCLUDE_BEGIN
                myToolkit.handlePosted(myAID, msg);
                //#MIDP_EXCLUDE_END
                msgQueue.addLast(msg);
                doWake();
            }
        }
    }
    //#APIDOC_EXCLUDE_END

    final void postMessagesBlock(ACLMessage[] mm) {
        long time = System.currentTimeMillis();
        synchronized (msgQueue) {
            for (ACLMessage msg : mm) {
                msg.setPostTimeStamp(time);
                //#MIDP_EXCLUDE_BEGIN
                myToolkit.handlePosted(myAID, msg);
                //#MIDP_EXCLUDE_END
                msgQueue.addLast(msg);
            }
            doWake();
        }
    }

    /**
     * Retrieves the agent's content manager
     *
     * @return The content manager.
     */
    public jade.content.ContentManager getContentManager() {
        if (theContentManager == null) {
            theContentManager = new jade.content.ContentManager();
        }
        return theContentManager;
    }

    //#MIDP_EXCLUDE_END

    /**
     * Retrieves the agent's service helper
     *
     * @return The service helper.
     */
    public ServiceHelper getHelper(String serviceName) throws ServiceException {
        ServiceHelper se = null;
        if (helpersTable == null) {
            helpersTable = new Hashtable<>();
        }

        se = helpersTable.get(serviceName);
        // is the helper already into the agent's helpersTable ?
        if (se == null) {
            // there isn't, request its creation
            se = myToolkit.getHelper(this, serviceName);
            if (se != null) {
                se.init(this);
                helpersTable.put(serviceName, se);
            } else {
                throw new ServiceException("Null helper");
            }
        }
        return se;
    }

    /**
     * Retrieve a configuration property set in the  Profile
     * of the local container (first) or as a System property.
     *
     * @param key      the key that maps to the property that has to be
     *                 retrieved.
     * @param aDefault a default value to be returned if there is no mapping
     *                 for  key
     */
    public String getProperty(String key, String aDefault) {
        String val = myToolkit.getProperty(key, aDefault);
        if (val == null || val.equals(aDefault)) {
            // Try among the System properties
            String sval = System.getProperty(key);
            if (sval != null) {
                val = sval;
            }
        }
        return val;
    }

    /**
     * Return the configuration properties exactly as they were passed to the Profile before
     * starting the local JADE container.
     */
    public Properties getBootProperties() {
        return myToolkit.getBootProperties();
    }

    /**
     * This method is used to interrupt the agent's thread.
     * In J2SE/PJAVA it just calls myThread.interrupt(). In MIDP,
     * where interrupt() is not supported the thread interruption is
     * simulated as described below.
     * The agent thread can be in one of the following three states:
     * 1) Running a behaviour.
     * 2) Sleeping on msgQueue due to a doWait()
     * 3) Sleeping on myScheduler due to a schedule() with no active behaviours
     * Note that in MIDP the suspended state is not supported
     * The idea is: set the 'isInterrupted' flag, then wake up the
     * thread wherever it may be
     */
    private void interruptThread() {
        //#MIDP_EXCLUDE_BEGIN
        myThread.interrupt();
        //#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 synchronized (this) {
		 isInterrupted = true;

		 // case 1: Nothing to do.
		  // case 2: Signal on msgQueue.
		   synchronized (msgQueue) {msgQueue.notifyAll();}
		   // case 3: Signal on the Scheduler
		    synchronized (myScheduler) {myScheduler.notifyAll();}
		    }
		    #MIDP_INCLUDE_END*/
    }

    /**
     * Since in MIDP Thread.interrupt() does not exist and a simulated
     * interruption is used to "interrupt" the agent's thread, we must
     * check whether the simulated interruption happened just before and
     * after going to sleep.
     */
    void waitOn(Object lock, long millis) throws InterruptedException {
		/*#MIDP_INCLUDE_BEGIN
		 synchronized (this) {
		 if (isInterrupted) {
		 isInterrupted = false;
		 throw new InterruptedException();
		 }
		 }
		 #MIDP_INCLUDE_END*/
        lock.wait(millis);
		/*#MIDP_INCLUDE_BEGIN
		 synchronized (this) {
		 if (isInterrupted) {
		 isInterrupted = false;
		 throw new InterruptedException();
		 }
		 }
		 #MIDP_INCLUDE_END*/
    }

    //#J2ME_EXCLUDE_BEGIN
    // For persistence service -- Hibernate needs java.util collections
    private Set<Behaviour> getBehaviours() {
        Behaviour[] behaviours = myScheduler.getBehaviours();

        return new HashSet<>(Arrays.asList(behaviours));
    }

    // For persistence service -- Hibernate needs java.util collections
    private void setBehaviours(Set<?> behaviours) {
        Behaviour[] arr = new Behaviour[behaviours.size()];
        arr = behaviours.toArray(arr);

        // Reconnect all the behaviour -> agent pointers
        for (Behaviour behaviour : arr) {
            behaviour.setAgent(this);
        }

        myScheduler.setBehaviours(arr);
    }
    //#CUSTOM_EXCLUDE_END

    // For persistence service -- Hibernate needs java.util collections
    private Set<TBPair> getPendingTimers() {
        return persistentPendingTimers;
    }

    //#MIDP_EXCLUDE_BEGIN

    // For persistence service -- Hibernate needs java.util collections
    private void setPendingTimers(Set<TBPair> timers) {

        if (!persistentPendingTimers.equals(timers)) {
            // Clear the timers table, and install the new timers.
            pendingTimers.clear();

            for (TBPair timer : timers) {
                pendingTimers.addPair(timer);
            }
        }

        persistentPendingTimers = timers;

    }
    //#MIDP_EXCLUDE_END

    /**
     * add a service description into the agent description
     *
     * @param service description of the service
     * @return the new agent description with this service
     * @author Emmanuel Adam
     * @since 2022
     */
    public DFAgentDescription addService(ServiceDescription service) {
        if (servicesList == null) servicesList = new DFAgentDescription();
        servicesList.addServices(service);
        return servicesList;
    }

    /**
     * @return collection of the service descriptions of the agent
     * @author Emmanuel Adam
     * @since 2022
     */
    public List<ServiceDescription> getServices() {
//        var l = new ArrayList<ServiceDescription>();
//        servicesList.getAllServices().forEachRemaining(l::add);
        return servicesList.getAllServices();
    }

    /**
     * @return the agent description
     * @author Emmanuel Adam
     * @since 2022
     */
    public DFAgentDescription getServicesList() {
        return servicesList;
    }

    /**
     * @return the agent description
     * @author Emmanuel Adam
     * @since 2022
     */
    public void setServicesList(DFAgentDescription servicesList) {
        this.servicesList = servicesList;
    }

    /**
     * Inner class Interrupted.
     * This class is used to handle change state requests that occur
     * in particular situations such as when the agent thread is
     * blocked in the doWait() method.
     */
    public static class Interrupted extends Error {
        public Interrupted() {
            super();
        }
    }  // END of inner class Interrupted

    /**
     * Inner class TBPair
     */
    private static class TBPair {

        private Timer myTimer;
        private long expirationTime;
        private Behaviour myBehaviour;
        private Agent owner;

        public TBPair() {
            expirationTime = -1;
        }

        public TBPair(Agent a, Timer t, Behaviour b) {
            owner = a;
            myTimer = t;
            expirationTime = t.expirationTime();
            myBehaviour = b;
        }

        public Timer getTimer() {
            return myTimer;
        }

        public void setTimer(Timer t) {
            myTimer = t;
        }

        public Behaviour getBehaviour() {
            return myBehaviour;
        }

        public void setBehaviour(Behaviour b) {
            myBehaviour = b;
        }

        public Agent getOwner() {
            return owner;
        }

        public void setOwner(Agent o) {
            owner = o;
            createTimerIfNeeded();
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(long when) {
            expirationTime = when;
            createTimerIfNeeded();
        }

        // If both the owner and the expiration time have been set,
        // but the Timer object is still null, create one
        private void createTimerIfNeeded() {
            if (myTimer == null) {
                if ((owner != null) && (expirationTime > 0)) {
                    myTimer = new Timer(expirationTime, owner);
                }
            }
        }

    } // End of inner class TBPair

    /**
     * Inner class CondVar
     * A simple class for a boolean condition variable
     */
    private static class CondVar {
        private boolean value = false;

        public synchronized void waitOn() throws InterruptedException {
            while (!value) {
                wait();
            }
        }

        public synchronized void set() {
            value = true;
            notifyAll();
        }

    } // End of inner class CondVar

    //#J2ME_EXCLUDE_END

    /**
     * Inner class AssociationTB.
     * This class manages bidirectional associations between Timer and
     * Behaviour objects, using hash tables. This class is
     * synchronized with the operations
     * carried out by the TimerDispatcher. It allows also to avoid a deadlock when:
     * 1) A behaviour blocks for a very short time --> A Timer is added
     * to the TimerDispatcher
     * 2) The Timer immediately expires and the TimerDispatcher try to
     * restart the behaviour before the pair (b, t) is added to the
     * pendingTimers of this agent.
     */
    private class AssociationTB {
        private final Hashtable<Behaviour, TBPair> BtoT = new Hashtable<>();
        private final Hashtable<Timer, TBPair> TtoB = new Hashtable<>();

        public void clear() {
            synchronized (theDispatcher) {
                Enumeration<?> e = timers();
                while (e.hasMoreElements()) {
                    Timer t = (Timer) e.nextElement();
                    theDispatcher.remove(t);
                }

                BtoT.clear();
                TtoB.clear();

                //#J2ME_EXCLUDE_BEGIN

                // For persistence service
                persistentPendingTimers.clear();

                //#J2ME_EXCLUDE_END
            } //end synch
        }

        public void addPair(Behaviour b, Timer t) {
            TBPair pair = new TBPair(Agent.this, t, b);
            addPair(pair);
        }

        public void addPair(TBPair pair) {
            synchronized (theDispatcher) {
                if (pair.getOwner() == null) {
                    pair.setOwner(Agent.this);
                }

                pair.setTimer(theDispatcher.add(pair.getTimer()));
                TBPair old = BtoT.put(pair.getBehaviour(), pair);
                if (old != null) {
                    theDispatcher.remove(old.getTimer());
                    //#J2ME_EXCLUDE_BEGIN
                    persistentPendingTimers.remove(old);
                    //#J2ME_EXCLUDE_END
                    TtoB.remove(old.getTimer());
                }
                // Note that timers added to the TimerDispatcher are unique --> there
                // can't be an old value to handle
                TtoB.put(pair.getTimer(), pair);

                //#J2ME_EXCLUDE_BEGIN
                // For persistence service
                persistentPendingTimers.add(pair);
                //#J2ME_EXCLUDE_END
            } //end synch
        }

        public void removeMapping(Behaviour b) {
            synchronized (theDispatcher) {
                TBPair pair = BtoT.remove(b);
                if (pair != null) {
                    TtoB.remove(pair.getTimer());

                    //#J2ME_EXCLUDE_BEGIN
                    // For persistence service
                    persistentPendingTimers.remove(pair);
                    //#J2ME_EXCLUDE_END

                    theDispatcher.remove(pair.getTimer());
                }
            } //end synch
        }


        public Timer getPeer(Behaviour b) {
            // this is not synchronized because BtoT is an Hashtable (that is already synch!)
            TBPair pair = BtoT.get(b);
            if (pair != null) {
                return pair.getTimer();
            } else {
                return null;
            }
        }

        public Behaviour getPeer(Timer t) {
            // this is not synchronized because BtoT is an Hashtable (that is already synch!)
            TBPair pair = TtoB.get(t);
            if (pair != null) {
                return pair.getBehaviour();
            } else {
                return null;
            }
        }

        private Enumeration<Timer> timers() {
            return TtoB.keys();
        }


    } // End of inner class AssociationTB

    /**
     * Inner class ActiveLifeCycle
     */
    private class ActiveLifeCycle extends LifeCycle {
        @Serial
        private static final long serialVersionUID = 11111;

        private ActiveLifeCycle() {
            super(AP_INITIATED);
        }

        public void setState(int s) {
            myState = s;
        }

        public void init() {
            setActiveState(AP_ACTIVE);
            //#MIDP_EXCLUDE_BEGIN
            notifyStarted();
            //#MIDP_EXCLUDE_END
            setup();
            restarting = false;
        }

        public void execute() throws JADESecurityException, InterruptedException, InterruptedIOException {
            // Select the next behaviour to execute
            Behaviour currentBehaviour = myScheduler.schedule();
            long oldRestartCounter = currentBehaviour.getRestartCounter();

            // Just do it!
            currentBehaviour.actionWrapper();

            // When it is needed no more, delete it from the behaviours queue
            if (currentBehaviour.done()) {
                currentBehaviour.onEnd();
                myScheduler.remove(currentBehaviour);
                currentBehaviour = null;
            } else {
                synchronized (myScheduler) {
                    // If the current Behaviour has blocked and it was restarted in the meanwhile
                    // (e.g. because a message arrived), restart the behaviour to give it another chance.
                    // Furthermore restart it even if it appears to be runnable since, due to the fact that block/restart
                    // events are managed in an un-synchronized way, we may end up in a situation where the root is runnable,
                    // but some of its childern are not.
                    if (oldRestartCounter != currentBehaviour.getRestartCounter()) {
                        currentBehaviour.handleRestartEvent();
                    }

                    // Need synchronized block (Crais Sayers, HP): What if
                    // 1) it checks to see if its runnable, sees its not,
                    //    so it begins to enter the body of the if clause
                    // 2) meanwhile, in another thread, a message arrives, so
                    //    the behaviour is restarted and moved to the ready list.
                    // 3) now back in the first thread, the agent executes the
                    //    body of the if clause and, by calling block(), moves
                    //   the behaviour back to the blocked list.
                    if (!currentBehaviour.isRunnable()) {
                        // Remove blocked behaviour from ready behaviours queue
                        // and put it in blocked behaviours queue
                        myScheduler.block(currentBehaviour);
                        currentBehaviour = null;
                    }
                }
            }
        }

        public void end() {
            clean(false);
        }

        public boolean transitionTo(LifeCycle to) {
            // We can go to whatever state unless we are terminating
            if (!terminating) {
                // The agent is going to leave this state. When
                // the agent will enter this state again it must be
                // in AP_ACTIVE
                myState = AP_ACTIVE;
                return true;
            } else {
                return false;
            }
        }

        public void transitionFrom(LifeCycle from) {
            activateAllBehaviours();
        }

        public boolean isMessageAware() {
            return true;
        }
    } // END of inner class ActiveLifeCycle

    /**
     * Inner class DeletedLifeCycle
     */
    private class DeletedLifeCycle extends LifeCycle {
        @Serial
        private static final long serialVersionUID = 11112;

        private DeletedLifeCycle() {
            super(AP_DELETED);
        }

        public void end() {
            clean(true);
        }

        public boolean alive() {
            return false;
        }
    } // END of inner class DeletedLifeCycle

    /**
     * Inner class SuspendedLifeCycle
     */
    private class SuspendedLifeCycle extends LifeCycle {
        @Serial
        private static final long serialVersionUID = 11113;

        private SuspendedLifeCycle() {
            super(AP_SUSPENDED);
        }

        public void execute() throws JADESecurityException, InterruptedException, InterruptedIOException {
            waitUntilActivate();
        }

        public void end() {
            clean(false);
        }

        public boolean transitionTo(LifeCycle to) {
            // We can only die or resume
            return (to.getState() == AP_ACTIVE || to.getState() == AP_DELETED);
        }
    } // END of inner class SuspendedLifeCycle

}
