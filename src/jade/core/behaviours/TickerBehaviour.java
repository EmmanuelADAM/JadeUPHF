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

import jade.core.Agent;

import java.util.function.Consumer;

/**
 * This abstract class implements a  Behaviour   that
 * periodically executes a user-defined piece of code.
 * <p>
 *     <ul>
 * <li>The user can extend this class re-defining the method
 *  onTick()   and including the piece of code that
 * must be periodically executed into it.</li>
 * <li>or pass in the constructor a Consumer Agent that will be activated at each tick. <br/>
 *  
 *     addBehaviour(new TickerBehaviour(this, 300, a->System.out.println("Hello, my name is " + a.getLocalName()));
 *   
 * </li>
 * </ul>
 * @author Giovanni Caire - TILAB
 * @author emmanuel adam - 22.07
 * @version $Date: 2022-07-13  $
 */
public  class TickerBehaviour extends SimpleBehaviour {
    private long wakeupTime, period;
    private boolean finished;
    private int tickCount = 0;

    private boolean fixedPeriod = false;
    private long startTime;

    /**
     * Construct a  TickerBehaviour   that call its
     *  onTick()   method every  period   ms.
     *
     * @param a      is the pointer to the agent
     * @param period the tick period in ms
     */
    public TickerBehaviour(Agent a, long period) {
        super(a);
        if (period <= 0) {
            throw new IllegalArgumentException("Period must be greater than 0");
        }
        this.period = period;
    }

    /**
     * Construct a  TickerBehaviour   that call its
     *  onTick()   method every  period   ms.
     *
     * @param a      is the pointer to the agent
     * @param period the tick period in ms
     * @param fAction consumer that take the Agent; automatically launch by tick()
     * @author E.ADAM
     * @since 22.07
     */
    public TickerBehaviour(Agent a, long period, Consumer<Agent> fAction) {
        this(a, period);
        this.fAction = fAction;
    }

    public void onStart() {
        startTime = System.currentTimeMillis();
        wakeupTime = startTime + period;
    }

    public final void action() {
        // Someone else may have stopped us in the meanwhile
        if (!finished) {
            long blockTime = wakeupTime - System.currentTimeMillis();
            if (blockTime <= 0) {
                // Timeout is expired --> execute the user defined action and
                // re-initialize wakeupTime
                tickCount++;
                onTick();

                long currentTime = System.currentTimeMillis();
                if (fixedPeriod) {
                    wakeupTime = startTime + (tickCount + 1) * period;
                } else {
                    wakeupTime = currentTime + period;
                }
                blockTime = wakeupTime - currentTime;
            }
            // Maybe this behaviour has been removed within the onTick() method
            if (myAgent != null && !finished && blockTime > 0) {
                block(blockTime);
            }
        }
    }

    public final boolean done() {
        return finished;
    }

    /**
     * This method is invoked periodically with the period defined in the
     * constructor.
     * Subclasses are expected to define this method specifying the action
     * that must be performed at every tick.
     * if not overrided, execute fAction
     * @author E.ADAM
     * @since 22.07
     */
    protected void onTick()
    {
        if (fAction!=null) fAction.accept(myAgent);
    }

    /**
     * Turn on/off the "fixed period" mode. Given a period P, when fixed period mode is off (default),
     * this behaviour will wait for P milliseconds from the end of the n-th execution of the onTick() method
     * to the beginning of the n+1-th execution.
     * When fixed period is on, this behaviour will execute the onTick() method exactly every P milliseconds.
     *
     * @param fixedPeriod A boolean value indicating whether the fixed period mode must be turned on or off.
     */
    public void setFixedPeriod(boolean fixedPeriod) {
        this.fixedPeriod = fixedPeriod;
    }

    /**
     * This method must be called to reset the behaviour and starts again
     *
     * @param period the new tick time
     */
    public void reset(long period) {
        this.reset();
        if (period <= 0) {
            throw new IllegalArgumentException("Period must be greater than 0");
        }
        this.period = period;
    }

    /**
     * This method must be called to reset the behaviour and starts again
     */
    public void reset() {
        super.reset();
        finished = false;
        tickCount = 0;
    }

    /**
     * Make this  TickerBehaviour   terminate.
     * Calling stop() has the same effect as removing this TickerBehaviour, but is Thread safe
     */
    public void stop() {
        finished = true;
        restart();
    }

    /**
     * Retrieve how many ticks were done (i.e. how many times this
     * behaviour was executed) since the last reset.
     *
     * @return The number of ticks since the last reset
     */
    public final int getTickCount() {
        return tickCount;
    }

    // For persistence service
    private void setTickCount(int tc) {
        tickCount = tc;
    }
    //#MIDP_EXCLUDE_BEGIN

    protected long getPeriod() {
        return period;
    }

    // For persistence service
    private void setPeriod(long p) {
        period = p;
    }

    // For persistence service
    private long getWakeupTime() {
        return wakeupTime;
    }

    // For persistence service
    private void setWakeupTime(long wt) {
        wakeupTime = wt;
    }


    //#MIDP_EXCLUDE_END


}
