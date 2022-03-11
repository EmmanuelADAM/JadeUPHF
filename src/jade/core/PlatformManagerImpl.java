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
import jade.core.behaviours.Behaviour;
import jade.mtp.TransportAddress;
import jade.security.JADESecurityException;
import jade.util.Logger;
import jade.util.TransportAddressWrapper;

import java.util.*;

/**
 * The <code>ServiceManagerImpl</code> class is the actual
 * implementation of JADE platform <i>Service Manager</i> and
 * <i>Service Finder</i> components. It holds a set of services and
 * manages them.
 *
 * @author Giovanni Caire - TILAB
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
public class PlatformManagerImpl implements PlatformManager {

    private final IMTPManager myIMTPManager;
    private final CommandProcessor myCommandProcessor;

    // FIXME: The association between MainContainer and PlatformManagerImpl need be clarified...
    private final MainContainerImpl myMain;
    private final Map<String, NodeDescriptor> nodes;
    private final Map<String, ServiceEntry> services;
    private final Map<TransportAddressWrapper, PlatformManager> replicas;
    private final Map<String, NodeFailureMonitor> monitors;
    private String localAddr;
    private String platformID;

    // These variables hold progressive numbers just used to name new nodes.
    // By convention, nodes hosting containers with a local copy of the Platform Manager are called
    // Main-Container-<N>, hosting containers whereas nodes without their own Platform Manager are
    // called Container-<M>.
    // Nodes not hosting containers are called Aux-Node-<K>
    private int containerNo = 1;
    private int mainContainerNo = 0;
    private int nodeNo = 1;

    private final Logger myLogger = Logger.getMyLogger(getClass().getName());

    /**
     * Inner class ServiceEntry (package scoped for debugging purpose)
     */
    class ServiceEntry {

        public ServiceEntry(Service s) {
            myService = s;
            slices = new HashMap<>();
        }

        public void addSlice(String name, Slice s, Node n, boolean childNode) {
            SliceEntry e = new SliceEntry(s, n, childNode);
            slices.put(name, e);
        }

        public Slice removeSlice(String name) {
            SliceEntry e = slices.remove(name);
            if (e == null) {
                return null;
            } else {
                return e.getSlice();
            }
        }

        public Vector<Slice> getSlices() {
            Iterator<SliceEntry> sliceEntries = slices.values().iterator();
            Vector<Slice> result = new Vector<>();

            while (sliceEntries.hasNext()) {
                SliceEntry e = sliceEntries.next();
                result.addElement(e.getSlice());
            }

            return result;
        }

        public Slice getSlice(String name) {
            SliceEntry e = slices.get(name);
            if (e == null) {
                return null;
            } else {
                return e.getSlice();
            }
        }

        public Node[] getNodes() {
            Object[] sliceEntries = slices.values().toArray();
            Node[] result = new Node[sliceEntries.length];

            for (int i = 0; i < result.length; i++) {
                SliceEntry e = (SliceEntry) sliceEntries[i];
                result[i] = e.getNode();
            }

            return result;
        }

        public Node getNode(String name) {
            SliceEntry e = slices.get(name);
            if (e == null) {
                return null;
            } else {
                return e.getNode();
            }
        }

        public void setService(Service svc) {
            myService = svc;
        }

        public Service getService() {
            return myService;
        }

        // For debugging purpose only
        Map<String, SliceEntry> getSlicesMap() {
            return slices;
        }

        private Service myService;
        private final Map<String, SliceEntry> slices;

    } // End of inner class ServiceEntry class

    /**
     * Inner class SliceEntry (package-scoped for debugging purpose)
     */
    class SliceEntry {

        public SliceEntry(Slice s, Node n, boolean c) {
            mySlice = s;
            myNode = n;
            childNode = c;
        }

        public Slice getSlice() {
            return mySlice;
        }

        public Node getNode() {
            return myNode;
        }

        public boolean isInChildNode() {
            return childNode;
        }

        private final Slice mySlice;
        private final Node myNode;
        private final boolean childNode;

    } // End of inner class SliceEntry class

    /**
     * Constructs a new Service Manager implementation complying with
     * a given JADE profile. This constructor is package-scoped, so
     * that only the JADE kernel is allowed to create a new Service
     * Manager implementation.
     *
     * @param p The platform profile describing how the JADE platform
     *          is to be configured.
     */
    PlatformManagerImpl(Profile p) throws ProfileException {
        myCommandProcessor = p.getCommandProcessor();
        myIMTPManager = p.getIMTPManager();
        myMain = new MainContainerImpl(p, this);

        nodes = new HashMap<>();
        services = new HashMap<>();
        replicas = new HashMap<>();
        monitors = new HashMap<>();

        platformID = p.getParameter(Profile.PLATFORM_ID, null);
        if (platformID == null || platformID.equals("")) {
            try {
                // Build the PlatformID using the local host and port
                List<TransportAddress> l = myIMTPManager.getLocalAddresses();
                TransportAddress localAddr = l.get(0);
                platformID = localAddr.getHost() + ":" + localAddr.getPort() + "/JADE";
            } catch (Exception e) {
                throw new ProfileException("Can't set PlatformID", e);
            }
        }
    }

    MainContainerImpl getMain() {
        return myMain;
    }

    public void setPlatformName(String name) throws IMTPException {
        platformID = name;
    }

    // Implementation of the PlatformManager interface

    public String getPlatformName() throws IMTPException {
        return platformID;
    }

    public String getLocalAddress() {
        return localAddr;
    }

    public void setLocalAddress(String addr) {
        localAddr = addr;
    }

    /**
     * @param dsc          The Descriptor of the new Node
     * @param nodeServices The services currently installed on the new Node
     * @param propagated   Flag indicating whether the new-node event
     *                     was a propagated event within the replication mechanism
     */
    public String addNode(NodeDescriptor dsc, Vector<ServiceDescriptor> nodeServices, boolean propagated) throws IMTPException, ServiceException, JADESecurityException {
        String newName = localAddNode(dsc, nodeServices, propagated);
        if (!propagated) {
            broadcastAddNode(dsc, nodeServices);
        }
        return newName;
    }

    // This may throw IMTPException since localAddSlice() throws IMTPException
    private String localAddNode(NodeDescriptor dsc, Vector<ServiceDescriptor> nodeServices, boolean propagated) throws IMTPException, JADESecurityException {
        Node node = dsc.getNode();

        // If the node hosts a Container, adjust node name
        adjustName(dsc, node);

        // Check if the newly starting node is reachable and, in case, issue a NEW_NODE vertical command
        if (!propagated) {
            checkReachability(dsc);
            // In this case we issue the command before adding the node for authorization purposes
            issueNewNodeCommand(dsc);
        }

        // Add the new node
        if (isLocalNode(node)) {
            // If this is the local node it surely host a container (the main). Remove it
            myMain.addLocalContainer(dsc.getContainer());
        } else {
            if (myLogger.isLoggable(Logger.INFO)) {
                myLogger.log(Logger.INFO, "Adding node <" + dsc.getName() + "> to the platform");
            }

            // If the node hosts a container add it as a remote container
            if (dsc.getContainer() != null) {
                myMain.addRemoteContainer(dsc.getContainer());
            }
        }


        // Add the node to the global node list
        nodes.put(dsc.getName(), dsc);

        // Add all service slices
        // Do not broadcast since this information is already conveied when broadcasting the add-node event
        if (nodeServices != null) {
            for (int i = 0; i < nodeServices.size(); ++i) {
                ServiceDescriptor service = nodeServices.elementAt(i);
                localAddSlice(service, dsc, propagated);
            }
        }

        // Start monitoring the new node if necessary
        if (!isLocalNode(node) && !propagated) {
            // Start monitoring
            monitor(dsc);
        }

        // Return the name given to the new node
        return node.getName();
    }

    private void broadcastAddNode(NodeDescriptor dsc, Vector<ServiceDescriptor> nodeServices) throws ServiceException {
        // Avoid concurrent modification exception
        Object[] rr = replicas.values().toArray();
        for (Object o : rr) {
            PlatformManager replica = (PlatformManager) o;
            try {
                replica.addNode(dsc, nodeServices, true);
            } catch (IMTPException imtpe) {
                // Zombie replica. Will be removed soon. Just ignore it
            } catch (JADESecurityException ae) {
                // Should never happen since this is a propagated info
                ae.printStackTrace();
            }
        }
    }

    public void removeNode(NodeDescriptor dsc, boolean propagated) throws IMTPException, ServiceException {
        localRemoveNode(dsc, propagated);
        // If this is the local node the node termination will cause the deregistration from replicas...
        if (!propagated && !isLocalNode(dsc.getNode())) {
            broadcastRemoveNode(dsc);
        }
    }

    private void localRemoveNode(NodeDescriptor dsc, boolean propagated) {
        dsc = adjustDescriptor(dsc);
        Node node = dsc.getNode();

        // Remove all the slices corresponding to the removed node
        // Avoid concurrent modification exception
        Object[] allServiceKeys = services.keySet().toArray();
        for (Object allServiceKey : allServiceKeys) {
            String serviceKey = (String) allServiceKey;
            localRemoveSlice(serviceKey, dsc.getName(), propagated);
        }

        // Remove the node
        if (isLocalNode(node)) {
            // If it is the local node it surely hosts a container (the main). Remove it
            myMain.removeLocalContainer(dsc.getContainer());
        } else {
            if (myLogger.isLoggable(Logger.INFO)) {
                myLogger.log(Logger.INFO, "Removing node <" + dsc.getName() + "> from the platform");
            }

            // If the node hosted a container remove it as a remote container
            if (dsc.getContainer() != null) {
                myMain.removeRemoteContainer(dsc.getContainer());
            }
        }

        // Remove the node from the global node list
        nodes.remove(dsc.getName());

        // Stop monitoring (this has no effect if we were not monitoring the dead node)
        Node parent = dsc.getParentNode();
        if (parent != null) {
            // If the dead node had a parent, notify the failure-monitor monitoring the parent
            NodeFailureMonitor failureMonitor = monitors.get(parent.getName());
            if (failureMonitor != null) {
                failureMonitor.removeChild(node);
            }
        }
        monitors.remove(node.getName());

        // Issue a DEAD_NODE vertical command
        if (!propagated) {
            GenericCommand gCmd = new GenericCommand(Service.DEAD_NODE, null, null);
            gCmd.addParam(dsc);
            Object result = myCommandProcessor.processIncoming(gCmd);
            if (result instanceof Throwable) {
                myLogger.log(Logger.WARNING, "Unexpected error processing DEAD_NODE command. Node is " + dsc.getName(), (Throwable) result);
            }
        }
    }

    private void broadcastRemoveNode(NodeDescriptor dsc) throws ServiceException {
        // Avoid concurrent modification exception
        Object[] rr = replicas.values().toArray();
        for (Object o : rr) {
            PlatformManager replica = (PlatformManager) o;
            try {
                replica.removeNode(dsc, true);
            } catch (IMTPException imtpe) {
                // Zombie replica. Will be removed soon. Just ignore it
            }
        }
    }

    public void addSlice(ServiceDescriptor service, NodeDescriptor dsc, boolean propagated) throws IMTPException, ServiceException {
        localAddSlice(service, dsc, propagated);
        if (!propagated) {
            broadcastAddSlice(service, dsc);
        }
    }

    // This may throw IMTPException since IMTPManager.createSliceProxy throws IMTPException
    private void localAddSlice(ServiceDescriptor serviceDsc, NodeDescriptor dsc, boolean propagated) throws IMTPException {
        Service service = serviceDsc.getService();

        String serviceKey = service.getName();
        ServiceEntry e = services.get(serviceKey);

        if (e == null) {
            if (myLogger.isLoggable(Logger.CONFIG)) {
                myLogger.log(Logger.CONFIG, "Adding entry for service <" + serviceKey + ">");
            }

            e = new ServiceEntry(service);
            services.put(serviceKey, e);
        }
        if (myLogger.isLoggable(Logger.CONFIG)) {
            myLogger.log(Logger.CONFIG, "Adding slice for service <" + serviceKey + "> on node <" + dsc.getName() + ">");
        }

        Node node = dsc.getNode();
        Slice slice = null;
        if (service.getHorizontalInterface() != null) {
            // Create a real SliceProxy
            slice = myIMTPManager.createSliceProxy(serviceKey, service.getHorizontalInterface(), node);
        } else {
            // Create a dummy SliceProxy (it will never be used)
            slice = new SliceProxy(service, node);
        }

        String sliceKey = node.getName();
        boolean childNode = dsc.getParentNode() != null;
        e.addSlice(sliceKey, slice, node, childNode);

        if (isLocalNode(node)) {
            // The service is just started on this main container
            // Register the service-specific behaviour (if any) within the AMS
            Behaviour b = service.getAMSBehaviour();
            if (b != null) {
                myMain.installAMSBehaviour(b);
            }
        }

        if (!propagated) {
            issueNewSliceCommand(serviceKey, sliceKey);
        }
    }

    private void broadcastAddSlice(ServiceDescriptor service, NodeDescriptor dsc) throws ServiceException {
        // Avoid concurrent modification exception
        Object[] rr = replicas.values().toArray();
        for (Object o : rr) {
            PlatformManager replica = (PlatformManager) o;
            try {
                replica.addSlice(service, dsc, true);
            } catch (IMTPException imtpe) {
                // Zombie replica. Will be removed soon. Just ignore it
            }
        }
    }

    public void removeSlice(String serviceKey, String sliceKey, boolean propagated) throws ServiceException {
        localRemoveSlice(serviceKey, sliceKey, propagated);
        if (!propagated) {
            broadcastRemoveSlice(serviceKey, sliceKey);
        }
    }

    private void localRemoveSlice(String serviceKey, String sliceKey, boolean propagated) {
        ServiceEntry e = services.get(serviceKey);

        if (e != null) {
            if (e.removeSlice(sliceKey) != null) {
                if (myLogger.isLoggable(Logger.CONFIG)) {
                    myLogger.log(Logger.CONFIG, "Removing slice for service <" + serviceKey + "> on node <" + sliceKey + ">");
                }
            }
            // Clear the cache
            Service svc = e.getService();
            if (svc instanceof BaseService) {
                ((BaseService) svc).clearCachedSlice(sliceKey);
            }

            NodeDescriptor dsc = getDescriptor(sliceKey);
            if (dsc != null && isLocalNode(dsc.getNode())) {
                // The service slice was removed on this node
                // Deregister the service-specific behaviour (if any) within the AMS
                Behaviour b = e.getService().getAMSBehaviour();
                if (b != null) {
                    myMain.uninstallAMSBehaviour(b);
                }
            }

            if (!propagated) {
                GenericCommand gCmd = new GenericCommand(Service.DEAD_SLICE, serviceKey, null);
                gCmd.addParam(sliceKey);
                Object result = myCommandProcessor.processIncoming(gCmd);
                if (result instanceof Throwable) {
                    myLogger.log(Logger.WARNING, "Unexpected error processing DEAD_SLICE command. Service is " + serviceKey + " node is " + sliceKey, (Throwable) result);
                }
            }
        }
    }

    private void broadcastRemoveSlice(String serviceKey, String sliceKey) throws ServiceException {
        // Avoid concurrent modification exception
        Object[] rr = replicas.values().toArray();
        for (Object o : rr) {
            PlatformManager replica = (PlatformManager) o;
            try {
                replica.removeSlice(serviceKey, sliceKey, true);
            } catch (IMTPException imtpe) {
                // Zombie replica. Will be removed soon. Just ignore it
            }
        }
    }

    public void addReplica(String newAddr, boolean propagated) throws IMTPException, ServiceException {
        PlatformManager newReplica = myIMTPManager.getPlatformManagerProxy(newAddr);
        localAddReplica(newReplica, propagated);
        if (!propagated) {
            broadcastAddReplica(newAddr);
        }
        // Actually add the new replica only after broadcasting
        replicas.put(TransportAddressWrapper.getWrapper(newReplica.getLocalAddress(), myIMTPManager), newReplica);
    }


    // This may throw IMTPException since the new replica must be informed about the platform status
    private void localAddReplica(PlatformManager newReplica, boolean propagated) throws IMTPException, ServiceException {
        if (myLogger.isLoggable(Logger.INFO)) {
            myLogger.log(Logger.INFO, "Adding replica <" + newReplica.getLocalAddress() + "> to the platform");
        }

        if (!propagated) {
            // Inform the new replica about existing nodes and their installed services...
            List<NodeInfo> infos = getAllNodesInfo();

            for (NodeInfo info : infos) {
                try {
                    newReplica.addNode(info.getNodeDescriptor(), info.getServices(), true);
                } catch (JADESecurityException ae) {
                    // Should never happen since this is a propagated info
                    ae.printStackTrace();
                }
            }

            // Inform the new replica about other replicas
            // Avoid concurrent modification exception
            Object[] rr = replicas.values().toArray();
            for (Object o : rr) {
                PlatformManager replica = (PlatformManager) o;
                newReplica.addReplica(replica.getLocalAddress(), true);
            }
        }

        // Issue a NEW_REPLICA command
        GenericCommand gCmd = new GenericCommand(Service.NEW_REPLICA, null, null);
        gCmd.addParam(newReplica.getLocalAddress());
        Object result = myCommandProcessor.processIncoming(gCmd);
        if (result instanceof Throwable) {
            myLogger.log(Logger.WARNING, "Unexpected error processing NEW_REPLICA command. Replica address is " + newReplica.getLocalAddress(), (Throwable) result);
        }
    }

    private void broadcastAddReplica(String newAddr) throws ServiceException {
        // Avoid concurrent modification exception
        Object[] rr = replicas.values().toArray();
        for (Object o : rr) {
            PlatformManager replica = (PlatformManager) o;
            try {
                replica.addReplica(newAddr, true);
            } catch (IMTPException imtpe) {
                // Zombie replica. Will be removed soon. Just ignore it
            }
        }
    }

    public void removeReplica(String address, boolean propagated) throws IMTPException, ServiceException {
        localRemoveReplica(address, propagated);
        if (!propagated) {
            broadcastRemoveReplica(address);
        }
    }

    private void localRemoveReplica(String address, boolean propagated) throws ServiceException {
        if (myLogger.isLoggable(Logger.INFO)) {
            myLogger.log(Logger.INFO, "Removing replica <" + address + "> from the platform");
        }

        // Remove the old replica
        try {
            TransportAddressWrapper key = TransportAddressWrapper.getWrapper(address, myIMTPManager);
            if (replicas.remove(key) == null) {
                // The replica to remove is not among the known replicas --> Print a warning since this should never happen
                StringBuilder sb = new StringBuilder("Replica " + key.hashCode() + ", " + key.getAddress() + "not found! Known replicas are: {");
                Iterator<TransportAddressWrapper> it = replicas.keySet().iterator();
                while (it.hasNext()) {
                    TransportAddressWrapper taw = it.next();
                    sb.append(taw.hashCode()).append(", ").append(taw.getAddress());
                    if (it.hasNext()) {
                        sb.append("; ");
                    }
                }
                sb.append("}");
                myLogger.log(Logger.WARNING, sb.toString());
            }
        } catch (IMTPException imtpe) {
            // This should never happen as our IMTPManager must be able to deal with a replica address
            throw new ServiceException("Error parsing PlatformManager replica transport address");
        }

        if (!propagated) {
            // Notify first all non-child and non-main nodes.
            Object[] allNodes = nodes.values().toArray();
            for (Object node : allNodes) {
                NodeDescriptor dsc = (NodeDescriptor) node;
                if (dsc.getParentNode() == null) {
                    Node n = dsc.getNode();
                    if (!n.hasPlatformManager()) {
                        try {
                            n.platformManagerDead(address, getLocalAddress());

                        } catch (IMTPException imtpe) {
                            // The node died while no one was monitoring it
                            removeTerminatedNode(n);
                        }
                    }
                }
            }
            // Then notify all child nodes.
            for (Object allNode : allNodes) {
                NodeDescriptor dsc = (NodeDescriptor) allNode;
                if (dsc.getParentNode() != null) {
                    Node n = dsc.getNode();
                    try {
                        n.platformManagerDead(address, getLocalAddress());
                    } catch (IMTPException imtpe) {
                        // The node daid while no one was monitoring it
                        removeTerminatedNode(n);
                    }
                }
            }
        }

        // Issue a DEAD_REPLICA command
        GenericCommand gCmd = new GenericCommand(Service.DEAD_REPLICA, null, null);
        gCmd.addParam(address);
        Object result = myCommandProcessor.processIncoming(gCmd);
        if (result instanceof Throwable) {
            myLogger.log(Logger.WARNING, "Unexpected error processing DEAD_REPLICA command. Replica address is " + address, (Throwable) result);
        }
    }

    private void broadcastRemoveReplica(String address) throws ServiceException {
        // Avoid concurrent modification exception
        Object[] rr = replicas.values().toArray();
        for (Object o : rr) {
            PlatformManager replica = (PlatformManager) o;
            try {
                replica.removeReplica(address, true);
            } catch (IMTPException imtpe) {
                // Zombie replica. Will be removed soon. Just ignore it
            }
        }
    }

    public void adopt(Node n, Node[] children) throws IMTPException {
        String name = n.getName();
        NodeDescriptor dsc = getDescriptor(name);
        if (dsc != null) {
            monitor(dsc);
            // Issue an ADOPTED_NODE Vertical command
            GenericCommand gCmd = new GenericCommand(Service.ADOPTED_NODE, null, null);
            gCmd.addParam(dsc);
            Object result = myCommandProcessor.processIncoming(gCmd);
            if (result instanceof Throwable) {
                myLogger.log(Logger.WARNING, "Unexpected error processing ADOPTED_NODE command. Node is " + dsc.getName(), (Throwable) result);
            }
            myLogger.log(Logger.INFO, "Node <" + n.getName() + "> adopted");
        } else {
            myLogger.log(Logger.WARNING, "NO descriptor found for node <" + n.getName() + "> requesting adoption. Ignore...");
        }
    }

    public void ping() throws IMTPException {
        // Just do nothing
    }

    /**
     * This method is invoked by the MainReplicationService when a new MainReplication slice is added
     * and has the effect of issuing a NEW_NODE VCommand and one NEW_SLICE VCommand for each service
     * installed in the node hosting the new MainReplication slice. This allows services to propagate
     * service specific information to their new slice
     */
    public void addMainContainerNode(NodeDescriptor dsc, Vector<ServiceDescriptor> services) throws JADESecurityException {
        issueNewNodeCommand(dsc);
        for (ServiceDescriptor srvDsc : services) {
            issueNewSliceCommand(srvDsc.getService().getName(), dsc.getNode().getName());
        }
    }

    ////////////////////////////////
    // Service finding methods
    ////////////////////////////////
    public Slice findSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
        ServiceEntry e = services.get(serviceKey);

        if (e == null) {
            return null;
        } else {
            // If the special MAIN_SLICE name is used, return the local slice
            if (CaseInsensitiveString.equalsIgnoreCase(sliceKey, ServiceFinder.MAIN_SLICE)) {
                sliceKey = myIMTPManager.getLocalNode().getName();
            }

            return e.getSlice(sliceKey);
        }
    }

    public Vector<Slice> findAllSlices(String serviceKey) throws IMTPException, ServiceException {
        ServiceEntry e = services.get(serviceKey);
        if (e == null) {
            return null;
        } else {
            return e.getSlices();
        }
    }

    //////////////////////////////////////////////////////////////
    // Package-scoped methods called by the MainContainer only
    //////////////////////////////////////////////////////////////
    NodeDescriptor getDescriptor(String name) {
        return nodes.get(name);
    }

    /**
     * Kill all auxiliary nodes not holding any container
     */
    void shutdown() {
        // FIXME: to be implemented
    }

    //////////////////////////////////
    // Private methods
    //////////////////////////////////
    private void issueNewNodeCommand(NodeDescriptor dsc) throws JADESecurityException {
        GenericCommand gCmd = new GenericCommand(Service.NEW_NODE, null, null);
        gCmd.addParam(dsc);
        Object result = myCommandProcessor.processIncoming(gCmd);
        if (result instanceof JADESecurityException) {
            throw (JADESecurityException) result;
        } else if (result instanceof Throwable) {
            myLogger.log(Logger.WARNING, "Unexpected error processing NEW_NODE command. Node is " + dsc.getName(), (Throwable) result);
        }
    }

    private void issueNewSliceCommand(String serviceName, String sliceKey) {
        GenericCommand gCmd = new GenericCommand(Service.NEW_SLICE, serviceName, null);
        gCmd.addParam(sliceKey);
        Object result = myCommandProcessor.processIncoming(gCmd);
        if (result instanceof Throwable) {
            myLogger.log(Logger.WARNING, "Unexpected error processing NEW_SLICE command. Service is " + serviceName + " node is " + sliceKey, (Throwable) result);
        }
    }

    private boolean isLocalNode(Node n) {
        try {
            return myIMTPManager.getLocalNode().equals(n);
        } catch (IMTPException imtpe) {
            // Should never happen
            imtpe.printStackTrace();
            return false;
        }
    }

    private NodeDescriptor adjustDescriptor(NodeDescriptor dsc) {
        NodeDescriptor originalDsc = nodes.get(dsc.getName());
        if (originalDsc != null) {
            ContainerID cid = originalDsc.getContainer();
            if (cid != null) {
                dsc = new NodeDescriptor(cid, dsc.getNode());
            }
            dsc.setParentNode(originalDsc.getParentNode());
        }
        return dsc;
    }

    private List<NodeInfo> getAllNodesInfo() {
        // Get all node descriptors and build the list of NodeInfo
        Object[] allNodes = nodes.values().toArray();
        List<NodeInfo> infos = new ArrayList<>(allNodes.length);
        for (Object allNode : allNodes) {
            NodeDescriptor nodeDsc = (NodeDescriptor) allNode;
            infos.add(new NodeInfo(nodeDsc));
        }

        // Build the map of services for each node
        Map<String, Vector<ServiceDescriptor>> nodeServices = new HashMap<>();
        // Avoid concurrent modification exception
        Object[] allServices = services.values().toArray();
        for (Object allService : allServices) {
            ServiceEntry e = (ServiceEntry) allService;
            Node[] serviceNodes = e.getNodes();

            for (Node serviceNode : serviceNodes) {
                String nodeName = serviceNode.getName();

                Vector<ServiceDescriptor> v = nodeServices.get(nodeName);
                if (v == null) {
                    v = new Vector<>();
                    nodeServices.put(nodeName, v);
                }
                Service svc = e.getService();
                v.addElement(new ServiceDescriptor(svc.getName(), svc));
            }
        }

        // Now fill the services in the list of NodeInfo
        for (NodeInfo ni : infos) {
            Vector<ServiceDescriptor> v = nodeServices.get(ni.getNodeDescriptor().getName());
            ni.setServices(v);
        }
        return infos;
    }

    private Node[] getChildren(String name) {
        List<Node> children = new ArrayList<>();
        Object[] allNodes = nodes.values().toArray();
        for (Object allNode : allNodes) {
            NodeDescriptor dsc = (NodeDescriptor) allNode;
            Node parent = dsc.getParentNode();
            if (parent != null && name.equals(parent.getName())) {
                children.add(dsc.getNode());
            }
        }
        Node[] childrenArray = new Node[children.size()];
        for (int i = 0; i < childrenArray.length; ++i) {
            childrenArray[i] = children.get(i);
        }
        return childrenArray;
    }

    private void checkReachability(NodeDescriptor dsc) throws IMTPException {
        if (dsc.getParentNode() == null) {
            // Check reachability of stand-alone nodes only
            Node node = dsc.getNode();
            if (!isLocalNode(node)) {
                try {
                    node.ping(false);
                } catch (IMTPException imtpe) {
                    myLogger.log(Logger.WARNING, "Node <" + dsc.getName() + "> is trying to join the platform but is not reachable. Node details: " + node + ". Exception message: " + imtpe.getMessage());
                    // The following exception will be displayed in the joining node output
                    throw new IMTPException("Main container cannot reach newly starting node " + node + ": " + imtpe.getMessage());
                }
            }
        }
    }

    private void adjustName(NodeDescriptor dsc, Node node) {
        ContainerID cid = dsc.getContainer();
        if (cid != null) {
            // If the node hosts a container, use the container naming convention
            adjustContainerName(node, cid);
            node.setName(cid.getName());
            dsc.setName(cid.getName());
        } else {
            // Otherwise use the node naming convention unless a custom name is specified
            if (node.getName() == null || node.getName().equals(NO_NAME)) {
                String name = null;
                NodeDescriptor old = null;
                do {
                    name = AUX_NODE_NAME + '-' + nodeNo;
                    nodeNo++;
                    old = nodes.get(name);
                } while (old != null);
                node.setName(name);
            }
            dsc.setName(node.getName());
        }
    }

    private void adjustContainerName(Node n, ContainerID cid) {
        String name = null;
        NodeDescriptor old = null;
        cid.setMain(n.hasPlatformManager());

        if (cid.getName() == null || cid.getName().equals(NO_NAME)) {
            if (n.hasPlatformManager()) {
                // Use the Main-Container-<N> name schema
                do {
                    name = AgentContainer.MAIN_CONTAINER_NAME + (mainContainerNo == 0 ? "" : "-" + mainContainerNo);
                    mainContainerNo++;
                    old = nodes.get(name);

                } while (old != null);
            } else {
                do {
                    name = AgentContainer.AUX_CONTAINER_NAME + '-' + containerNo;
                    containerNo++;
                    old = nodes.get(name);
                } while (old != null);
            }
        } else {
            // The new container comes with a user defined name.
            // If it is already in use add a progressive number

            name = cid.getName();
            int cnt = 1;
            old = nodes.get(name);
            while (old != null) {
                name = cid.getName() + '-' + cnt;
                cnt++;
                old = nodes.get(name);
            }
        }
        cid.setName(name);
    }

    private void monitor(NodeDescriptor dsc) {
        boolean needMonitor = true;
        Node node = dsc.getNode();
        Node parent = dsc.getParentNode();
        if (parent != null) {
            // This is a child node --> Do not monitor it directly if the parent is already monitored or is the local node
            if (isLocalNode(parent)) {
                needMonitor = false;
            } else {
                NodeFailureMonitor failureMonitor = monitors.get(parent.getName());
                if (failureMonitor != null) {
                    failureMonitor.addChild(node);
                    if (myLogger.isLoggable(Logger.CONFIG)) {
                        myLogger.log(Logger.INFO, "Node <" + node.getName() + "> added as child of node " + parent.getName());
                    }
                    needMonitor = false;
                }
            }
        }
        if (needMonitor) {
            monitor(node);
        }
    }

    private NodeFailureMonitor monitor(Node target) {

        NodeEventListener listener = new NodeEventListener() {

            public void nodeAdded(Node n) {
                myLogger.log(Logger.INFO, "--- Node <" + n.getName() + "> ALIVE ---");
            }

            public void nodeRemoved(Node n) {
                removeTerminatedNode(n);
            }

            public void nodeUnreachable(Node n) {
                myLogger.log(Logger.WARNING, "--- Node <" + n.getName() + "> UNREACHABLE ---");
            }

            public void nodeReachable(Node n) {
                myLogger.log(Logger.INFO, "--- Node <" + n.getName() + "> REACHABLE ---");
            }

        };

        // Start a new node failure monitor
        NodeFailureMonitor failureMonitor = NodeFailureMonitor.getFailureMonitor();
        // If this failure monitor require a specific service, check that service is
        // actually installed in the target node
        String requiredService = failureMonitor.requireService();
        if (requiredService != null) {
            Slice s = null;
            try {
                s = findSlice(requiredService, target.getName());
            } catch (Exception ignored) {
            }
            if (s == null) {
                // Required service not installed in the target node --> Use default monitoring
                myLogger.log(Logger.WARNING, "Service " + requiredService + " not found in node " + target.getName() + " --> Use default node monitoring");
                failureMonitor = NodeFailureMonitor.getDefaultFailureMonitor();
            }
        }
        failureMonitor.start(target, listener);
        monitors.put(target.getName(), failureMonitor);

        return failureMonitor;
    }

    private void removeTerminatedNode(Node n) {
        if (myLogger.isLoggable(Logger.INFO)) {
            myLogger.log(Logger.INFO, "--- Node <" + n.getName() + "> TERMINATED ---");
        }

        try {
            removeNode(new NodeDescriptor(n), false);
        } catch (IMTPException | ServiceException imtpe) {
            // Should never happen since this is a local call
            imtpe.printStackTrace();
        } // There is nothing we can do

    }

    /**
     * Inner class NodeInfo.
     * Embeds the node descriptor and the services currently installed
     * on the node
     */
    private class NodeInfo {
        private final NodeDescriptor nodeDsc;

        private Vector<ServiceDescriptor> services;

        private NodeInfo(NodeDescriptor nd) {
            nodeDsc = nd;
        }

        public NodeDescriptor getNodeDescriptor() {
            return nodeDsc;
        }

        public Vector<ServiceDescriptor> getServices() {
            return services;
        }

        public void setServices(Vector<ServiceDescriptor> ss) {
            services = ss;
        }
    } // END of inner class NodeInfo


    // For debugging purpose only
    Map<String, ServiceEntry> getServicesMap() {
        return services;
    }

    Map<TransportAddressWrapper, PlatformManager> getReplicasMap() {
        return replicas;
    }

    Map<String, NodeDescriptor> getNodesMap() {
        return nodes;
    }

    Map<String, NodeFailureMonitor> getMonitorsMap() {
        return monitors;
    }
}
