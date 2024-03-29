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

import jade.core.Service.Slice;
import jade.util.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a partial implementation of the
 *  Node   interface. Concrete IMTPs will have to provide a
 * full implementation of the  Node   interface, possibly by
 * subclassing this class.
 *
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
public abstract class BaseNode implements Node, Serializable {

    private final transient Logger myLogger;
    // A map, indexed by service name, of all the local slices of this
    // node. This map is used to dispatch incoming commands to the
    // service they belong to.
    private final transient Map<String, Slice> localSlices;
    protected transient ServiceManager myServiceManager;
    private transient CommandProcessor processor;
    // The name of this node
    private String myName;
    // True if a local copy of the Platform Manager is deployed at this Node
    private boolean hasLocalPM = false;

    public BaseNode(String name, boolean hasPM) {
        myName = name;
        hasLocalPM = hasPM;
        localSlices = new HashMap<>(5);
        myLogger = Logger.getMyLogger(getClass().getName());
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public boolean hasPlatformManager() {
        return hasLocalPM;
    }

    public void exportSlice(String serviceName, Slice localSlice) {
        localSlices.put(serviceName, localSlice);
    }

    public void unexportSlice(String serviceName) {
        localSlices.remove(serviceName);
    }

    protected Slice getSlice(String serviceName) {
        return localSlices.get(serviceName);
    }

    /**
     * Provides an IMTP independent implementation for the
     * horizontal command serving mechanism. IMTP dependent implementations
     * of the accept() method should invoke this method.
     *
     * @param cmd The horizontal command to process.
     * @return The object that is the result of processing the command.
     * @throws ServiceException If the service the command belongs to
     *                          is not present on this node.
     */
    public Object serveHorizontalCommand(HorizontalCommand cmd) throws ServiceException {

        String serviceName = cmd.getService();
        String commandName = cmd.getName();
        Object[] commandParams = cmd.getParams();

        if (myLogger.isLoggable(Logger.FINE)) {
            myLogger.log(Logger.FINE, "Node " + myName + " serving incoming H-Command " + commandName + " of Service " + serviceName);
        }

        // Look up in the local slices table and find the slice to dispatch to
        Slice slice = getSlice(serviceName);

        if (slice != null) {
            Object ret = null;
            VerticalCommand vCmd = slice.serve(cmd);

            if (vCmd != null) {
                vCmd.setPrincipal(cmd.getPrincipal());
                vCmd.setCredentials(cmd.getCredentials());
                // Hand it to the command processor
                if (myLogger.isLoggable(Logger.FINE)) {
                    myLogger.log(Logger.FINE, "Node " + myName + " issuing incoming V-Command " + vCmd.getName() + " of Service " + vCmd.getService());
                }
                serveVerticalCommand(vCmd);
                ret = vCmd.getReturnValue();
            } else {
                ret = cmd.getReturnValue();
            }

            if (ret != null) {
                if (myLogger.isLoggable(Logger.FINE)) {
                    myLogger.log(Logger.FINE, "Node " + myName + " return value for incoming H-Command " + commandName + " of Service " + serviceName + " = " + ret);
                }
            }
            return ret;
        } else {
            String s = "Node " + myName + ": Service " + serviceName + " Unknown. Command = " + commandName;
            throw new ServiceException("-- " + s + " --");
        }
    }

    public void setCommandProcessor(CommandProcessor cp) {
        processor = cp;
    }

    public void setServiceManager(ServiceManager mgr) {
        myServiceManager = mgr;
    }

    public void platformManagerDead(String deadPMAddr, String notifyingPMAddr) throws IMTPException {
        ((ServiceManagerImpl) myServiceManager).platformManagerDead(deadPMAddr, notifyingPMAddr);
    }

    /**
     * Serves an incoming vertical command, locally. This method is
     * invoked if a new  VerticalCommand   object is
     * generated by a slice targetted by a former
     *  HorizontalCommand  , which happens if the
     *  Slice.serve()   yields a non-null result.
     * <p>
     * This method makes it so that the newly created vertical command
     * is handed to the command processor to first pass through all
     * incoming filters and then to be dispatched to its proper
     * incoming command sink.
     *
     * @param cmd The vertical command to process.
     * @return The object that is the result of processing the command.
     * @throws ServiceException If some problem occurs.
     */
    private Object serveVerticalCommand(VerticalCommand cmd) throws ServiceException {
        if (processor == null) {
            throw new ServiceException("No command processor for node <" + getName() + ">");
        }

        return processor.processIncoming(cmd);
    }

    public String toString() {
        return myName;
    }
}
