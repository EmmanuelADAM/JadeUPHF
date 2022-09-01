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

import jade.core.nodeMonitoring.NodeMonitoringService;

import java.util.HashMap;
import java.util.Map;

/**
 * The abstract class  NodeFailureMonitor   provides a basic implementation for classes
 * that are used to monitor the availability of nodes and detect node failures. In addition to that it provides
 * static methods to initialize and create instances of monitors depending on the current
 * settings in the profile.
 * <p>
 * An instance of a subclass of the  NodeFailureMonitor   can only supervise
 * a single node. If there are additional nodes in the same JVM, you can add these as child nodes.
 * A child node is not supervised directly. Instead it has always the same state than its parent node.
 * So if the parent node gets unreachable automatically all its child nodes will turn to the state unreachable.
 *
 * <p>
 * Since JADE 3.3 there are two different types of failure monitoring available:
 * <ul>
 * <li>Blocking failure monitoring based on RMI (Default)
 * </li>
 * <li>UDP based failure monitoring
 * </li>
 * </ul>
 * <p>
 * <p>
 * The failure monitoring can be configured through the following profile parameter:
 *
 * <p>
 * <table style="border:1; cellspacing:0;">
 *  <tr>
 *    <th>Parameter</th>
 *    <th>Description</th>
 *    <th>PC</th>
 *    <th>MC</th>
 *  </tr>
 *  <tr>
 *    <td> jade_core_NodeFailureMonitor_udp   (*)</td>
 *    <td>If  true  , indicates that the UDP based failure monitoring has to be used.
 *        If  false   or not specified the default RMI based failure monitoring is activated.
 *    </td>
 *    <td>X</td>
 *    <td>X</td>
 *  </tr>
 *  <tr>
 *    <td> jade_core_NodeFailureMonitor_udp-port  </td>
 *    <td>Specifies the port number where the main container will listen for UDP pings.
 *     The default value is  28000  . (This parameter is only used in combination with (*)</td>
 *    <td>X</td>
 *    <td>X</td>
 *  </tr>
 * <tr>
 *    <td> jade_core_NodeFailureMonitor_udp-ping-delay-limit  </td>
 *    <td>Defines the maximum time (in milliseconds) the main container will wait for
 *        incoming ping messages. The default value is  3000  . (This parameter is only used in combination with (*)</td>
 *    <td>&nbsp;</td>
 *    <td>X</td>
 *  </tr>
 *
 * <tr>
 *    <td> jade_core_NodeFailureMonitor_udp-unreachable-limit  </td>
 *    <td>Defines the maximum time (in milliseconds) a node can be temporarily unreachable
 *    until it gets removed from the platform. The default value is  10.000  . (This parameter is only used in combination with (*)</td>
 *    <td>&nbsp;</td>
 *    <td>X</td>
 * </tr>
 * <tr>
 *    <td> jade_core_NodeFailureMonitor_udp-ping-delay  </td>
 *    <td>Defines the time interval (in milliseconds) in which a peripheral
 *        container sends UDP ping messages to the main container.
 *        The default value is  1.000  . (This parameter is only used in combination with (*)</td>
 *    <td>X</td>
 *    <td>&nbsp;</td>
 * </tr>
 * </table>
 * <p>
 * MC ... main container
 * PC ... peripheral container
 *
 * @author Roland Mungenast - Profactor
 * @see NodeEventListener
 */
public abstract class NodeFailureMonitor {

    private static NodeMonitoringService theMonitoringService;

    protected Node target;
    protected NodeEventListener listener;
    protected Map<String, Node> childNodes = new HashMap<>();

    /**
     * Factory method to create NodeFailureMonitor objects
     *
     * @return a new instance of a  NodeFailureMonitor  .
     */
    public static NodeFailureMonitor getFailureMonitor() {
        NodeFailureMonitor nfm = null;
        if (theMonitoringService != null) {
            nfm = theMonitoringService.getFailureMonitor();
        }

        if (nfm == null) {
            // Use the default NodeFailureMonitor
            nfm = getDefaultFailureMonitor();
        }

        return nfm;
    }

    public static NodeFailureMonitor getDefaultFailureMonitor() {
        try {
            return (NodeFailureMonitor) Class.forName("jade.core.nodeMonitoring.BlockingNodeFailureMonitor").getDeclaredConstructor().newInstance();
        } catch (Throwable t) {
            // Should never happen
            t.printStackTrace();
            return null;
        }
    }

    public static void init(NodeMonitoringService nms) {
        theMonitoringService = nms;
    }

    /**
     * Start the monitoring
     *
     * @param n   target node to monitor
     * @param nel listener to inform about new events
     */
    public void start(Node n, NodeEventListener nel) {
        target = n;
        listener = nel;
    }

    /**
     * Stop the monitoring
     */
    public abstract void stop();

    /**
     * Add a child node for monitoring.
     *
     * @param n child node
     */
    public synchronized void addChild(Node n) {
        childNodes.put(n.getName(), n);
        System.out.println("FailureMonitor child added. " + childNodes.size());
    }

    /**
     * Remove a child node from monitoring
     *
     * @param n child node
     */
    public synchronized void removeChild(Node n) {
        childNodes.remove(n.getName());
        System.out.println("FailureMonitor child removed. " + childNodes.size());
    }

    /**
     * Return the monitored target node
     */
    public Node getNode() {
        return target;
    }

    /**
     * Fire a NODE ADDED event
     */
    protected synchronized void fireNodeAdded() {
        listener.nodeAdded(target);
        for (Node o : childNodes.values()) {
            listener.nodeAdded(o);
        }
    }

    /**
     * Fire a NODE REMOVED event
     */
    protected synchronized void fireNodeRemoved() {
        listener.nodeRemoved(target);
        for (Node o : childNodes.values()) {
            listener.nodeRemoved(o);
        }
    }

    /**
     * Fire a NODE REACHABLE event
     */
    protected synchronized void fireNodeReachable() {
        listener.nodeReachable(target);
        issueNodeReachable(target);
        for (Node o : childNodes.values()) {
            listener.nodeReachable(o);
            issueNodeReachable(o);
        }
    }

    /**
     * Fire a NODE UNREACHABLE event
     */
    protected synchronized void fireNodeUnreachable() {
        listener.nodeUnreachable(target);
        issueNodeUnreachable(target);
        for (Node o : childNodes.values()) {
            listener.nodeUnreachable(o);
            issueNodeUnreachable(target);
        }
    }


    /////////////////////////////////////////////////////
    // Static methods
    /////////////////////////////////////////////////////

    private void issueNodeReachable(Node n) {
        if (theMonitoringService != null) {
            GenericCommand cmd = new GenericCommand(NodeMonitoringService.NODE_REACHABLE, theMonitoringService.getName(), null);
            cmd.addParam(n);
            try {
                theMonitoringService.submit(cmd);
            } catch (ServiceException se) {
                // Should never happen
                se.printStackTrace();
            }
        }
    }

    private void issueNodeUnreachable(Node n) {
        if (theMonitoringService != null) {
            GenericCommand cmd = new GenericCommand(NodeMonitoringService.NODE_UNREACHABLE, theMonitoringService.getName(), null);
            cmd.addParam(n);
            try {
                theMonitoringService.submit(cmd);
            } catch (ServiceException se) {
                // Should never happen
                se.printStackTrace();
            }
        }
    }

    public String requireService() {
        return null;
    }
}
