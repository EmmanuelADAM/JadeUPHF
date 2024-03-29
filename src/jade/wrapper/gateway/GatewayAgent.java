package jade.wrapper.gateway;

//#J2ME_EXCLUDE_FILE

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.util.Logger;

/**
 * This agent is the gateway able to execute all commands requests received via JadeGateway.
 * <p>
 *  JadeGateway   enables two alternative ways to implement a gateway
 * that allows non-JADE code to communicate with JADE agents.
 * <br> The first one is to extend the  GatewayAgent
 * <br> The second one is to extend this  GatewayBehaviour   and add an instance
 * of this Behaviour to your own agent that will have to function as a gateway (see its javadoc for reference).
 *
 * @author Fabio Bellifemine, Telecom Italia LAB
 * @version $Date: 2015-03-10 12:58:25 +0100 (mar, 10 mar 2015) $ $Revision: 6749 $
 * @see JadeGateway
 * @see GatewayBehaviour
 **/
public class GatewayAgent extends Agent {

    private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
    private GatewayBehaviour myB = null;
    private GatewayListener listener;

    public GatewayAgent() {
        // enable object2agent communication with queue of infinite length
        setEnabledO2ACommunication(true, 0);
    }

    /**
     * subclasses may implement this method.
     * The method is called each time a request to process a command
     * is received from the JSP Gateway.
     * <p> The recommended pattern is the following implementation:
     *
     * if (c instanceof Command1)
     * exexCommand1(c);
     * else if (c instanceof Command2)
     * exexCommand2(c);
     *
     * </p>
     * <b> REMIND THAT WHEN THE COMMAND HAS BEEN PROCESSED,
     * YOU MUST CALL THE METHOD  releaseCommand  .
     * <br>Sometimes, you might prefer launching a new Behaviour that processes
     * this command and release the command just when the Behaviour terminates,
     * i.e. in its  onEnd()   method.
     **/
    protected void processCommand(final Object command) {
        if (command instanceof Behaviour) {
            SequentialBehaviour sb = new SequentialBehaviour(this);
            sb.addSubBehaviour((Behaviour) command);
            sb.addSubBehaviour(new OneShotBehaviour(this) {
                public void action() {
                    GatewayAgent.this.releaseCommand(command);
                }
            });
            addBehaviour(sb);
        } else {
            myLogger.log(Logger.WARNING, "Unknown command " + command);
        }
    }

    /**
     * notify that the command has been processed and remove the command from the queue
     *
     * @param command is the same object that was passed in the processCommand method
     **/
    final public void releaseCommand(Object command) {
        myB.releaseCommand(command);
    }

    /*
     * Those classes that extends this setup method of the GatewayAgent
     * MUST absolutely call  super.setup()   otherwise this
     * method is not executed and the system would not work.
     * @see jade.core.Agent#setup()
     */
    protected void setup() {
        myLogger.log(Logger.INFO, "Started GatewayAgent " + getLocalName());
        myB = new GatewayBehaviour() {
            protected void processCommand(Object command) {
                ((GatewayAgent) myAgent).processCommand(command);
            }
        };
        addBehaviour(myB);
        setO2AManager(myB);

        // Check if the listener is passed as agent argument
        if (listener == null) {
            Object[] args = getArguments();
            if (args != null) {
                for (Object arg : args) {
                    if (arg instanceof GatewayListener) {
                        listener = (GatewayListener) arg;
                        break;
                    }
                }
            }
        }

        if (listener != null) {
            listener.handleGatewayConnected();
        }
    }

    protected void takeDown() {
        if (listener != null) {
            listener.handleGatewayDisconnected();
        }
    }

    // No need for synchronizations since this is only called when the Agent Thread has not started yet
    void setListener(GatewayListener listener) {
        this.listener = listener;
    }

}

