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

package jade.core.messaging;

//#MIDP_EXCLUDE_FILE

import jade.core.*;
import jade.core.messaging.MessagingHelper.AliasListener;
import jade.core.messaging.RoutingTable.MTPInfo;
import jade.core.replication.MainReplicationHandle;
import jade.core.sam.AbsoluteCounterValueProvider;
import jade.core.sam.AverageMeasureProviderImpl;
import jade.core.sam.SAMHelper;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ReceivedObject;
import jade.domain.FIPANames;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.LEAPACLCodec;
import jade.lang.acl.StringACLCodec;
import jade.mtp.*;
import jade.security.JADESecurityException;
import jade.util.HashCache;
import jade.util.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Timer;
import java.util.Map.Entry;


/**
 * The JADE service to manage the message passing subsystem installed
 * on the platform.
 *
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 * @author Nicolas Lhuillier - Motorola Labs
 * @author Jerome Picault - Motorola Labs
 */
public class MessagingService extends BaseService implements MessageManager.Channel {
    public static final String NAME = MessagingSlice.NAME;

    public static final String CACHE_SIZE = "jade_core_messaging_MessagingService_cachesize";
    public static final int CACHE_SIZE_DEFAULT = 1000;

    public static final String ATTACH_PLATFORM_INFO = "jade_core_messaging_MessagingService_attachplatforminfo";
    public static final String PLATFORM_IDENTIFIER = "x-sender-platform-identifer";
    public static final String MTP_IDENTIFIER = "x-sender-mtp-identifer";

    /**
     * Defines the maximum number of delivery attempts when an agent is found in the GADT, but not in the container it is supposed to be (see deliverUntilOK())
     */
    public static final String MAX_DELIVERY_RETRY_ATTEMPTS = "jade_core_messaging_MessagingService_maxdeliveryretryattempts";
    public static final int MAX_DELIVERY_RETRY_ATTEMPTS_DEFAULT = 20;

    //#J2ME_EXCLUDE_BEGIN
    // SAM related configurations
    public static final String DELIVERY_TIME_MEASUREMENT_RATE = "jade_core_messaging_MessagingService_deliverytimemeasurementrate";
    public static final int DELIVERY_TIME_MEASUREMENT_RATE_DEFAULT = -1; // Delivery time measurement disabled by default. Set it to N to measure delivery time 1 out of N delivered messages
    public static final String ENABLE_POSTED_MESSAGE_COUNT = "jade_core_messaging_MessagingService_enablepostedmessagecount";
    public static final String ENABLE_MESSAGE_MANAGER_METRICS = "jade_core_messaging_MessagingService_enablemessagemanagermetrics";

    //#J2ME_EXCLUDE_END
    private final static int EXPECTED_ACLENCODINGS_SIZE = 3;
    private static final String[] OWNED_COMMANDS = new String[]{
            MessagingSlice.SEND_MESSAGE,
            MessagingSlice.NOTIFY_FAILURE,
            MessagingSlice.INSTALL_MTP,
            MessagingSlice.UNINSTALL_MTP,
            MessagingSlice.NEW_MTP,
            MessagingSlice.DEAD_MTP,
            MessagingSlice.SET_PLATFORM_ADDRESSES
    };
    // The local slice for this service
    private final ServiceComponent localSlice = new ServiceComponent();
    // The command sink, source side
    private final CommandSourceSink senderSink = new CommandSourceSink();
    // The command sink, target side
    private final CommandTargetSink receiverSink = new CommandTargetSink();
    // The map of local and global (used in the Main Container) aliases
    private final Hashtable<Object, AID> localAliases = new Hashtable<>();
    // The table of the locally installed ACL message encodings
    private final Map<String, ACLCodec> messageEncodings = new HashMap<>(EXPECTED_ACLENCODINGS_SIZE);
    // The handle to the MainReplicationService to keep global aliases info in synch
    MainReplicationHandle replicationHandle;
    // The profile passed to this object
    private Profile myProfile;
    // A flag indicating whether or not we must accept foreign agents
    private boolean acceptForeignAgents = false;
    // The ID of the Platform this service belongs to
    private String platformID;
    // The concrete agent container, providing access to LADT, etc.
    private AgentContainer myContainer;
    // The filter for incoming commands related to ACL encoding
    private OutgoingEncodingFilter encOutFilter;
    // The filter for outgoing commands related to ACL encoding
    private IncomingEncodingFilter encInFilter;
    // The cached AID -> MessagingSlice associations
    private Map<Object, Object> cachedSlices;
    // The routing table mapping MTP addresses to their hosting slice
    private RoutingTable routes;
    private int maxDeliveryRetryAttempts;
    private Hashtable<Object, AID> globalAliases;
    private List<AliasListener> aliasListeners;
    // The platform ID, to be used in inter-platform dispatching
    private String accID;
    // The component managing asynchronous message delivery and retries
    private MessageManager myMessageManager;
    //#J2ME_EXCLUDE_BEGIN
    // SAM related variables
    private boolean samActive;
    private int msgCounter = 0;
    private int deliveryTimeMeasurementRate;
    private AverageMeasureProviderImpl deliveryTimeMeasureProvider;
    private long postedMessageCounter;
    private AverageMeasureProviderImpl avgQueueSizeBytesProvider;
    private AverageMeasureProviderImpl avgQueueSizeMessagesProvider;
    //#J2ME_EXCLUDE_END
    private Timer samTimer;
    // Only for debugging:
    private volatile int traceCnt = 0;

    public MessagingService() {
    }


    /**
     * Performs the passive initialization step of the service. This
     * method is called <b>before</b> activating the service. Its role
     * should be simply the one of a constructor, setting up the
     * internal data as needed.
     * Service implementations should not use the Service Manager and
     * Service Finder facilities from within this method. A
     * distributed initialization protocol, if needed, should be
     * exectuted within the <code>boot()</code> method.
     *
     * @param ac The agent container this service is activated on.
     * @param p  The configuration profile for this service.
     * @throws ProfileException If the given profile is not valid.
     */
    public void init(AgentContainer ac, Profile p) throws ProfileException {
        super.init(ac, p);
        myProfile = p;
        myContainer = ac;

        int size = CACHE_SIZE_DEFAULT;
        try {
            size = Integer.parseInt(myProfile.getParameter(CACHE_SIZE, null));
        } catch (Exception e) {
            // Keep default
        }
        cachedSlices = new HashCache(size);

        routes = new RoutingTable(myProfile.getBooleanProperty(ATTACH_PLATFORM_INFO, false));

        if (myContainer.getMain() != null) {
            globalAliases = new Hashtable<>();
            aliasListeners = new ArrayList<>();
        }

        // Look in the profile and check whether we must accept foreign agents
        acceptForeignAgents = myProfile.getBooleanProperty(Profile.ACCEPT_FOREIGN_AGENTS, false);

        maxDeliveryRetryAttempts = MAX_DELIVERY_RETRY_ATTEMPTS_DEFAULT;
        try {
            maxDeliveryRetryAttempts = Integer.parseInt(myProfile.getParameter(MAX_DELIVERY_RETRY_ATTEMPTS, null));
        } catch (Exception e) {
            // Keep default
        }

        // Initialize its own ID
        platformID = myContainer.getPlatformID();
        accID = "fipa-mts://" + platformID + "/acc";

        // create the command filters related to the encoding of ACL messages
        encOutFilter = new OutgoingEncodingFilter(messageEncodings, myContainer, this);
        encInFilter = new IncomingEncodingFilter(messageEncodings, this);

        myMessageManager = MessageManager.instance(p);
    }

    /**
     * Performs the active initialization step of a kernel-level
     * service: Activates the ACL codecs and MTPs as specified in the given
     * <code>Profile</code> instance.
     *
     * @param myProfile The <code>Profile</code> instance containing
     *                  the list of ACL codecs and MTPs to activate on this node.
     * @throws ServiceException If a problem occurs during service
     *                          initialization.
     */
    public void boot(Profile myProfile) throws ServiceException {
        this.myProfile = myProfile;

        try {
            // Initialize the MainReplicationHandle
            replicationHandle = new MainReplicationHandle(this, myContainer.getServiceFinder());

            // Initialize messaging-related System Activity Monitoring
            initializeSAM();

            // Activate the default ACL String codec anyway
            ACLCodec stringCodec = new StringACLCodec();
            messageEncodings.put(stringCodec.getName().toLowerCase(), stringCodec);

            // Activate the efficient encoding for intra-platform encoding
            ACLCodec efficientCodec = new LEAPACLCodec();
            messageEncodings.put(efficientCodec.getName().toLowerCase(), efficientCodec);

            // Codecs
            List<Specifier> l = myProfile.getSpecifiers(Profile.ACLCODECS);
            for (Specifier value : l) {
                String className = value.getClassName();
                try {
                    Class<?> c = Class.forName(className);
                    ACLCodec codec = (ACLCodec) c.getDeclaredConstructor().newInstance();
                    messageEncodings.put(codec.getName().toLowerCase(), codec);
                    if (myLogger.isLoggable(Logger.CONFIG))
                        myLogger.log(Logger.CONFIG, "Installed " + codec.getName() + " ACLCodec implemented by " + className + "\n");

                    // FIXME: notify the AMS of the new Codec to update the APDescritption.
                } catch (ClassNotFoundException cnfe) {
                    throw new ACLCodec.CodecException("ERROR: The class " + className + " for the ACLCodec not found.", cnfe);
                } catch (InstantiationException | NoSuchMethodException ie) {
                    throw new ACLCodec.CodecException("The class " + className + " raised InstantiationException (see NestedException)", ie);
                } catch (IllegalAccessException iae) {
                    throw new ACLCodec.CodecException("The class " + className + " raised IllegalAccessException (see nested exception)", iae);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            // MTPs
            l = myProfile.getSpecifiers(Profile.MTPS);
            PrintWriter f = null;
            StringBuilder sb = null;

            for (Specifier spec : l) {
                String className = spec.getClassName();
                String addressURL = null;
                Object[] args = spec.getArgs();
                if (args != null && args.length > 0) {
                    addressURL = args[0].toString();
                    if (addressURL.equals("")) {
                        addressURL = null;
                    }
                }

                MessagingSlice s = (MessagingSlice) getSlice(getLocalNode().getName());
                MTPDescriptor mtp = s.installMTP(addressURL, className);
                String[] mtpAddrs = mtp.getAddresses();
                if (f == null) {
                    String fileName = myProfile.getParameter(Profile.FILE_DIR, "") + "MTPs-" + myContainer.getID().getName() + ".txt";
                    f = new PrintWriter(new FileWriter(fileName));
                    sb = new StringBuilder("MTP addresses:");
                }
                f.println(mtpAddrs[0]);
                sb.append("\n");
                sb.append(mtpAddrs[0]);
            }

            if (f != null) {
                myLogger.log(Logger.INFO, sb.toString());
                f.close();
            }
        } catch (ProfileException pe1) {
            myLogger.log(Logger.SEVERE, "Error reading MTPs/Codecs", pe1);
        } catch (ServiceException se) {
            myLogger.log(Logger.SEVERE, "Error installing local MTPs", se);
        } catch (ACLCodec.CodecException ce) {
            myLogger.log(Logger.SEVERE, "Error installing ACL Codec", ce);
        } catch (MTPException me) {
            myLogger.log(Logger.SEVERE, "Error installing MTP", me);
        } catch (IOException ioe) {
            myLogger.log(Logger.SEVERE, "Error writing platform address", ioe);
        } catch (IMTPException imtpe) {
            // Should never happen as this is a local call
            imtpe.printStackTrace();
        }
    }

    private void initializeSAM() {
        //#J2ME_EXCLUDE_BEGIN
        //#DOTNET_EXCLUDE_BEGIN
        try {
            Service sam = myContainer.getServiceFinder().findService(SAMHelper.SERVICE_NAME);
            if (sam != null) {
                SAMHelper samHelper = (SAMHelper) sam.getHelper(null);
                samActive = true;

                // DELIVERY TIME
                deliveryTimeMeasurementRate = DELIVERY_TIME_MEASUREMENT_RATE_DEFAULT;
                try {
                    deliveryTimeMeasurementRate = Integer.parseInt(myProfile.getParameter(DELIVERY_TIME_MEASUREMENT_RATE, null));
                } catch (Exception e) {
                    // Keep default
                }
                if (deliveryTimeMeasurementRate > 0) {
                    deliveryTimeMeasureProvider = new AverageMeasureProviderImpl();
                    samHelper.addEntityMeasureProvider("Message-Delivery-Time", deliveryTimeMeasureProvider);
                }

                // POSTED MESSAGE COUNT
                boolean enablePostedMessageCount = "true".equalsIgnoreCase(myProfile.getParameter(ENABLE_POSTED_MESSAGE_COUNT, "false"));
                if (enablePostedMessageCount) {
                    samHelper.addCounterValueProvider("Posted-message-count#" + myContainer.getID().getName(), new AbsoluteCounterValueProvider() {
                        public long getValue() {
                            return postedMessageCounter;
                        }
                    });
                }

                // MESSAGE MANAGER METRICS
                boolean enableMessageManagerMetrics = "true".equalsIgnoreCase(myProfile.getParameter(ENABLE_MESSAGE_MANAGER_METRICS, "false"));
                if (enableMessageManagerMetrics) {
                    avgQueueSizeBytesProvider = new AverageMeasureProviderImpl();
                    samHelper.addEntityMeasureProvider("Message-Manager-avg-queue-size-bytes#" + myContainer.getID().getName(), avgQueueSizeBytesProvider);
                    avgQueueSizeMessagesProvider = new AverageMeasureProviderImpl();
                    samHelper.addEntityMeasureProvider("Message-Manager-avg-queue-size-messages#" + myContainer.getID().getName(), avgQueueSizeMessagesProvider);

                    samTimer = new Timer();
                    samTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            avgQueueSizeBytesProvider.addSample(myMessageManager.getSize());
                            avgQueueSizeMessagesProvider.addSample(myMessageManager.getPendingCnt());
                        }
                    }, 0, 30000);

                    samHelper.addCounterValueProvider("Message-Manager-submitted-count#" + myContainer.getID().getName(), new AbsoluteCounterValueProvider() {
                        @Override
                        public long getValue() {
                            return myMessageManager.getSubmittedCnt();
                        }
                    });
                    samHelper.addCounterValueProvider("Message-Manager-served-count#" + myContainer.getID().getName(), new AbsoluteCounterValueProvider() {
                        @Override
                        public long getValue() {
                            return myMessageManager.getServedCnt();
                        }
                    });
                    samHelper.addCounterValueProvider("Message-Manager-discarded-count#" + myContainer.getID().getName(), new AbsoluteCounterValueProvider() {
                        @Override
                        public long getValue() {
                            return myMessageManager.getDiscardedCnt();
                        }
                    });
                    samHelper.addCounterValueProvider("Message-Manager-slow-delivery-count#" + myContainer.getID().getName(), new AbsoluteCounterValueProvider() {
                        @Override
                        public long getValue() {
                            return myMessageManager.getSlowDeliveryCnt();
                        }
                    });
                    samHelper.addCounterValueProvider("Message-Manager-veryslow-delivery-count#" + myContainer.getID().getName(), new AbsoluteCounterValueProvider() {
                        @Override
                        public long getValue() {
                            return myMessageManager.getVerySlowDeliveryCnt();
                        }
                    });
                    samHelper.addCounterValueProvider("Message-Manager-multiple-delivery-count#" + myContainer.getID().getName(), new AbsoluteCounterValueProvider() {
                        @Override
                        public long getValue() {
                            return myMessageManager.getMultipleDeliveryCnt();
                        }
                    });
                    samHelper.addEntityMeasureProvider("Message-Manager-avg-msg-count-per-multiple-delivery#" + myContainer.getID().getName(), myMessageManager.getAvgMsgCountPerMultipleDeliveryProvider());
                }
            }
        } catch (ServiceNotActiveException snae) {
            // SAMService not active --> just do nothing
        } catch (Exception e) {
            // Should never happen
            myLogger.log(Logger.WARNING, "Error accessing the local SAMService.", e);
        }
        //#DOTNET_EXCLUDE_END
        //#J2ME_EXCLUDE_END
    }

    private void stopSAM() {
        //#J2ME_EXCLUDE_BEGIN
        //#DOTNET_EXCLUDE_BEGIN
        if (samTimer != null) {
            samTimer.cancel();
        }
        //#DOTNET_EXCLUDE_END
        //#J2ME_EXCLUDE_END
    }

    // kindly provided by David Bernstein, 15/6/2005
    public void shutdown() {
        stopSAM();
        // clone addresses (externally because leap list doesn't
        // implement Cloneable) so don't get concurrent modification
        // exception on the list as the MTPs are being uninstalled
        List<String> platformAddresses = new ArrayList<>();
        Iterator<String> routeIterator = routes.getAddresses();
        while (routeIterator.hasNext()) {
            platformAddresses.add(routeIterator.next());
        }
        // make an uninstall-mtp command to re-use for each MTP installed
        GenericCommand cmd = new GenericCommand(MessagingSlice.UNINSTALL_MTP, getName(), null);
        // for each platform address, uninstall the MTP it represents
        routeIterator = platformAddresses.iterator();
        while (routeIterator.hasNext()) {
            String route = routeIterator.next();
            try {
                cmd.addParam(route);
                receiverSink.consume(cmd);
                cmd.removeParam(route);
                if (myLogger.isLoggable(Logger.FINER)) {
                    myLogger.log(Logger.FINER, "uninstalled MTP " + route);
                }
            } catch (Exception e) {
                if (myLogger.isLoggable(Logger.SEVERE)) {
                    myLogger.log(Logger.SEVERE, "Exception uninstalling MTP " + route + ". " + e);
                }
            }
        }
    }


    /**
     * Retrieve the name of this service, that can be used to look up
     * its slices in the Service Finder.
     *
     * @return The name of this service.
     * @see ServiceFinder
     */
    public String getName() {
        return MessagingSlice.NAME;
    }

    /**
     * Retrieve the interface through which the different service
     * slices will communicate, that is, the service <i>Horizontal
     * Interface</i>.
     *
     * @return A <code>Class</code> object, representing the interface
     * that is implemented by the slices of this service.
     */
    public Class<MessagingSlice> getHorizontalInterface() {
        try {
            return (Class<MessagingSlice>) Class.forName(MessagingSlice.NAME + "Slice");
        } catch (ClassNotFoundException cnfe) {
            return null;
        }
    }

    public ServiceHelper getHelper(Agent a) throws ServiceException {
        return new MessagingHelper() {
            private Agent myAgent;

            public void init(Agent a) {
                myAgent = a;
            }

            public void createAlias(String alias) throws IMTPException, ServiceException {
                myLogger.log(Logger.INFO, "Creating Alias " + alias + "-->" + myAgent.getLocalName());
                AID aliasAID = new AID(AID.createGUID(alias, myContainer.getPlatformID()), AID.ISGUID);
                AID id = myAgent.getAID();
                localAliases.put(aliasAID, id);
                notifyNewAlias(aliasAID, id);
            }

            public void deleteAlias(String alias) throws IMTPException, ServiceException {
                myLogger.log(Logger.INFO, "Deleting Alias " + alias + "-->" + myAgent.getLocalName());
                AID aliasAID = new AID(AID.createGUID(alias, myContainer.getPlatformID()), AID.ISGUID);
                AID id = localAliases.remove(aliasAID);
                if (id != null) {
                    if (id.equals(myAgent.getAID())) {
                        // Alias actually removed --> notify the Main
                        notifyDeadAlias(aliasAID);
                    } else {
                        // An agent can delete its aliases only --> restore the alias
                        localAliases.put(aliasAID, id);
                    }
                }
            }

            public void registerAliasListener(AliasListener l) throws ServiceException {
                if (myContainer.getMain() != null) {
                    synchronized (aliasListeners) {
                        if (!aliasListeners.contains(l)) {
                            aliasListeners.add(l);
                        }
                    }
                } else {
                    throw new ServiceException("Cannot register AliasListener on a non-Main container");
                }
            }

            public void deregisterAliasListener(AliasListener l) throws ServiceException {
                if (myContainer.getMain() != null) {
                    synchronized (aliasListeners) {
                        aliasListeners.remove(l);
                    }
                } else {
                    throw new ServiceException("Cannot register AliasListener on a non-Main container");
                }
            }
        };
    }

    // Notify the Main Container about a new alias
    private void notifyNewAlias(AID alias, AID agent) throws ServiceException, IMTPException {
        MessagingSlice mainSlice = (MessagingSlice) getSlice(MAIN_SLICE);
        try {
            mainSlice.newAlias(alias, agent);
        } catch (IMTPException imtpe) {
            // Try to get a newer slice and repeat...
            mainSlice = (MessagingSlice) getFreshSlice(MAIN_SLICE);
            mainSlice.newAlias(alias, agent);
        }
    }

    // Notify the Main Container about a dead alias
    private void notifyDeadAlias(AID alias) throws ServiceException, IMTPException {
        MessagingSlice mainSlice = (MessagingSlice) getSlice(MAIN_SLICE);
        try {
            mainSlice.deadAlias(alias);
        } catch (IMTPException imtpe) {
            // Try to get a newer slice and repeat...
            mainSlice = (MessagingSlice) getFreshSlice(MAIN_SLICE);
            mainSlice.deadAlias(alias);
        }
    }

    // Add a new Global Alias entry
    // Public since it is replicated by the MainReplicationService
    public void newAlias(AID alias, AID agent) {
        myLogger.log(Logger.INFO, "Adding global alias entry: " + alias.getLocalName() + "-->" + agent.getLocalName());
        globalAliases.put(alias, agent);
        // Notify listeners
        notifyAliasListeners(alias, agent, true);
    }

    // Remove a dead Global Alias entry
    // Public since it is replicated by the MainReplicationService
    public void deadAlias(AID alias) {
        myLogger.log(Logger.INFO, "Removing global alias entry: " + alias.getLocalName());
        AID agent = globalAliases.remove(alias);
        if (agent != null) {
            // Notify listeners
            notifyAliasListeners(alias, agent, false);
        }
    }

    private void notifyAliasListeners(AID alias, AID agent, boolean added) {
        synchronized (aliasListeners) {
            for (AliasListener aliasListener : aliasListeners) {
                try {
                    if (added) {
                        // Alias added
                        aliasListener.handleNewAlias(alias, agent);
                    } else {
                        // Alias removed
                        aliasListener.handleDeadAlias(alias, agent);
                    }
                } catch (Exception e) {
                    myLogger.log(Logger.WARNING, "Error notifying listener " + aliasListener + " about dead alias " + alias.getLocalName() + "-->" + agent.getLocalName());
                }
            }
        }
    }

    private AID resolveLocalAlias(AID id) {
        AID mappedId = localAliases.get(id);
        return mappedId != null ? mappedId : id;
    }

    private AID resolveGlobalAlias(AID id) {
        AID mappedId = globalAliases.get(id);
        return mappedId != null ? mappedId : id;
    }

    // Remove all local alias entries for a given agent
    List<Object> removeLocalAliases(AID agent) {
        myLogger.log(Logger.FINE, "Removing all local alias entries for agent " + agent.getLocalName());
        return removeEntriesFor(localAliases, agent);
    }

    // Transfer all local alias entries for a given agent on a remote container
    // This is executed when an agent moves
    void transferLocalAliases(AID agent, ContainerID dest) {
        myLogger.log(Logger.FINE, "Transferring all local alias entries for agent " + agent.getLocalName() + " to " + dest.getName());
        List<Object> aliases = removeLocalAliases(agent);
        if (aliases.size() > 0) {
            try {
                MessagingSlice destSlice = (MessagingSlice) getSlice(dest.getName());
                destSlice.transferLocalAliases(agent, aliases);
            } catch (Exception e) {
                myLogger.log(Logger.SEVERE, "Error transferring local aliases for migrated agent " + agent.getName() + " to new location " + dest.getName());
            }
        }
    }

    // Remove all global alias entries for a given agent
    // Public since it is replicated by the MainReplicationService
    public void removeGlobalAliases(AID agent) {
        myLogger.log(Logger.FINE, "Removing all global alias entries for agent " + agent.getLocalName());
        List<Object> removedAliases = removeEntriesFor(globalAliases, agent);
        // Notify listeners
        for (Object removedAlias : removedAliases) {
            AID alias = (AID) removedAlias;
            notifyAliasListeners(alias, agent, false);
        }
    }

    // Inform the Main Container about all local aliases. This is called after a fault-&-recover
    // of the Main Container when using the FaultRecoveryService
    void notifyLocalAliases() {
        // We do not scan the localAliases table directly since this would require a potentially long
        // synchronized block
        Hashtable<Object, AID> cloned = (Hashtable<Object, AID>) localAliases.clone();
        for (Entry<Object, AID> o : cloned.entrySet()) {
            AID alias = (AID) o.getKey();
            AID agent = o.getValue();
            try {
                notifyNewAlias(alias, agent);
            } catch (Exception e) {
                myLogger.log(Logger.SEVERE, "Error informing recovered Main Container about alias " + alias.getLocalName() + "-->" + agent.getLocalName(), e);
            }
        }
    }

    // Remove all entries that maps to a given target
    private List<Object> removeEntriesFor(Hashtable<Object, AID> table, Object target) {
        List<Object> removedKeys = new ArrayList<>();
        synchronized (table) {
            Iterator<Entry<Object, AID>> it = table.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Object, AID> entry = it.next();
                if (entry.getValue().equals(target)) {
                    removedKeys.add(entry.getKey());
                    it.remove();
                }
            }
        }
        return removedKeys;
    }

    /**
     * Retrieve the locally installed slice of this service.
     */
    public Slice getLocalSlice() {
        return localSlice;
    }


    /**
     * Access the command filter this service needs to perform its
     * tasks. This filter will be installed within the local command
     * processing engine.
     *
     * @param direction One of the two constants
     *                  <code>Filter.INCOMING</code> and <code>Filter.OUTGOING</code>,
     *                  distinguishing between the two filter chains managed by the
     *                  command processor.
     * @return A <code>Filter</code> object, used by this service to
     * intercept and process kernel-level commands.
     */
    public Filter getCommandFilter(boolean direction) {
        if (direction == Filter.OUTGOING) {
            return encOutFilter;
        } else {
            return encInFilter;
        }
    }

    /**
     * Access the command sink this service uses to handle its own
     * vertical commands.
     */
    public Sink getCommandSink(boolean side) {
        if (side == Sink.COMMAND_SOURCE) {
            return senderSink;
        } else {
            return receiverSink;
        }
    }

    /**
     * Access the names of the vertical commands this service wants to
     * handle as their final destination. This set must not overlap
     * with the owned commands set of any previously installed
     * service, or an exception will be raised and service
     * activation will fail.
     * <p>
     * see Service#getCommandSink()
     */
    public String[] getOwnedCommands() {
        return OWNED_COMMANDS;
    }

    void notifyLocalMTPs() {
        Iterator<MTPInfo> it = routes.getLocalMTPs();
        while (it.hasNext()) {
            MTPInfo info = it.next();
            MTPDescriptor mtp = info.getDescriptor();
            ContainerID cid = myContainer.getID();

            try {
                MessagingSlice mainSlice = (MessagingSlice) getSlice(MAIN_SLICE);
                try {
                    mainSlice.newMTP(mtp, cid);
                } catch (IMTPException imtpe) {
                    mainSlice = (MessagingSlice) getFreshSlice(MAIN_SLICE);
                    mainSlice.newMTP(mtp, cid);
                }
            } catch (Exception e) {
                myLogger.log(Logger.WARNING, "Error notifying local MTP " + mtp.getName() + " to Main Container.", e);
            }
        }
    }

    private ContainerID getAgentLocation(AID agentID) throws IMTPException, NotFoundException {
        MainContainer impl = myContainer.getMain();
        if (impl != null) {
            agentID = resolveGlobalAlias(agentID);
            return impl.getContainerID(agentID);
        } else {
            // Should never happen
            throw new NotFoundException("getAgentLocation() invoked on a non-main container");
        }
    }

    void stamp(GenericMessage gmsg) {
        //#J2ME_EXCLUDE_BEGIN
        if (samActive) {
            synchronized (this) {
                msgCounter++;
                if (msgCounter == deliveryTimeMeasurementRate) {
                    gmsg.setTimeStamp(System.currentTimeMillis());
                    msgCounter = 0;
                }
            }
        }
        //#J2ME_EXCLUDE_END
    }

    private void updateDeliveryTimeMeasurement(GenericMessage gmsg) {
        //#J2ME_EXCLUDE_BEGIN
        if (samActive) {
            long timeStamp = gmsg.getTimeStamp();
            if (timeStamp > 0 && deliveryTimeMeasureProvider != null) {
                long deliveryTime = System.currentTimeMillis() - timeStamp;
                if (myLogger.isLoggable(Logger.FINER)) {
                    myLogger.log(Logger.FINER, "Delivery time = " + deliveryTime);
                }
                deliveryTimeMeasureProvider.addSample(deliveryTime);
            }
        }
        //#J2ME_EXCLUDE_END
    }

    // Entry point for the ACL message delivery
    public void deliverNow(GenericMessage msg, AID receiverID) {
        if (msg.getTraceID() != null) {
            myLogger.log(Logger.INFO, msg.getTraceID() + " - Serving message delivery");
        }
        try {
            if (!msg.hasForeignReceiver()) {
                deliverInLocalPlatfrom(msg, receiverID);
            } else {
                // Dispatch it through the ACC
                if (msg.getTraceID() != null) {
                    myLogger.log(Logger.INFO, msg.getTraceID() + " - Activating ACC delivery");
                }
                Iterator<String> addresses = receiverID.getAllAddresses();
                if (addresses.hasNext()) {
                    while (addresses.hasNext()) {
                        String address = addresses.next();
                        try {
                            forwardMessage(msg, receiverID, address);
                            return;
                        } catch (MTPException mtpe) {
                            if (myLogger.isLoggable(Logger.WARNING) && !isPersistentDeliveryRetry(msg))
                                myLogger.log(Logger.WARNING, "Cannot deliver message to address: " + address + " [" + mtpe + "]. Trying the next one...");
                        }
                    }
                    notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_FOREIGN_AGENT_UNREACHABLE + ": " + "No valid address contained within the AID " + receiverID.getName()));
                } else {
                    notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_FOREIGN_AGENT_NO_ADDRESS));
                }
            }
        } catch (NotFoundException nfe) {
            // The receiver does not exist --> Send a FAILURE message
            if (msg.getTraceID() != null) {
                myLogger.log(Logger.WARNING, msg.getTraceID() + " - Receiver " + receiverID.getLocalName() + " does not exist.", nfe);
            }
            notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_AGENT_NOT_FOUND + ": " + nfe.getMessage()));
        } catch (IMTPException imtpe) {
            // Can't reach the destination container --> Send a FAILURE message
            String id = (msg.getTraceID() != null ? msg.getTraceID() : MessageManager.stringify(msg));
            myLogger.log(Logger.WARNING, id + " - Receiver " + receiverID.getLocalName() + " unreachable.", imtpe);
            notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_AGENT_UNREACHABLE + ": " + imtpe.getMessage()));
        } catch (ServiceException se) {
            // Service error during delivery --> Send a FAILURE message
            String id = (msg.getTraceID() != null ? msg.getTraceID() : MessageManager.stringify(msg));
            myLogger.log(Logger.WARNING, id + " - Service error delivering message.", se);
            notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_SERVICE_ERROR + ": " + se.getMessage()));
        } catch (JADESecurityException jse) {
            // Delivery not authorized--> Send a FAILURE message
            if (msg.getTraceID() != null) {
                myLogger.log(Logger.WARNING, msg.getTraceID() + " - Not authorized.", jse);
            }
            notifyFailureToSender(msg, receiverID, new InternalError(ACLMessage.AMS_FAILURE_UNAUTHORIZED + ": " + jse.getMessage()));
        }
    }

    private boolean isPersistentDeliveryRetry(GenericMessage msg) {
        boolean ret = false;
        //#J2ME_EXCLUDE_BEGIN
        ACLMessage acl = msg.getACLMessage();
        if (acl != null) {
            ret = acl.getAllUserDefinedParameters().containsKey(PersistentDeliveryService.ACL_USERDEF_DUE_DATE);
        }
        //#J2ME_EXCLUDE_END
        return ret;
    }

    void deliverInLocalPlatfrom(GenericMessage msg, AID receiverID) throws IMTPException, ServiceException, NotFoundException, JADESecurityException {
        if (msg.getTraceID() != null) {
            myLogger.log(Logger.INFO, msg.getTraceID() + " - Activating local-platform delivery");
        }

        MainContainer impl = myContainer.getMain();
        if (impl != null) {
            // Directly use the GADT on the main container
            int attemptsCnt = 0;
            while (true) {
                ContainerID cid = getAgentLocation(receiverID);
                MessagingSlice targetSlice = oneShotDeliver(cid, msg, receiverID);
                if (targetSlice != null) {
                    // Success --> Done
                    //#J2ME_EXCLUDE_BEGIN
                    DeliveryTracing.setTracingInfo("Target-node", targetSlice.getNode().getName());
                    //#J2ME_EXCLUDE_END
                    return;
                }
                checkRetry(receiverID, cid, attemptsCnt);
                attemptsCnt++;
            }
        } else {
            // Try first with the cached <AgentID;MessagingSlice> pairs
            MessagingSlice cachedSlice = (MessagingSlice) cachedSlices.get(receiverID);
            if (cachedSlice != null) { // Cache hit :-)
                try {
                    if (msg.getTraceID() != null) {
                        myLogger.log(Logger.INFO, msg.getTraceID() + " - Delivering message to cached slice " + cachedSlice.getNode().getName());
                    }
                    cachedSlice.dispatchLocally(msg.getSender(), msg, receiverID);
                    if (msg.getTraceID() != null) {
                        myLogger.log(Logger.INFO, msg.getTraceID() + " - Delivery OK.");
                    }
                    //#J2ME_EXCLUDE_BEGIN
                    DeliveryTracing.setTracingInfo("Target-node", cachedSlice.getNode().getName());
                    //#J2ME_EXCLUDE_END
                    return;
                } catch (IMTPException imtpe) {
                    if (msg.getTraceID() != null) {
                        myLogger.log(Logger.INFO, msg.getTraceID() + " - Cached slice for receiver " + receiverID.getName() + " unreachable.");
                    }
                } catch (NotFoundException nfe) {
                    if (msg.getTraceID() != null) {
                        myLogger.log(Logger.INFO, msg.getTraceID() + " - Receiver " + receiverID.getName() + " not found on cached slice container.");
                    }
                }
                // Eliminate stale cache entry
                cachedSlices.remove(receiverID);
            }

            // Either the receiver was not found in cache or the cache entry was no longer valid
            //#J2ME_EXCLUDE_BEGIN
            DeliveryTracing.setTracingInfo("Messaging-cache-miss", true);
            //#J2ME_EXCLUDE_END
            deliverUntilOK(msg, receiverID);
        }
    }


    ///////////////////////////////////////////////
    // Message delivery
    ///////////////////////////////////////////////

    private void deliverUntilOK(GenericMessage msg, AID receiverID) throws IMTPException, NotFoundException, ServiceException, JADESecurityException {
        int attemptsCnt = 0;
        while (true) {
            ContainerID cid;
            try {
                MessagingSlice mainSlice = (MessagingSlice) getSlice(MAIN_SLICE);
                try {
                    cid = mainSlice.getAgentLocation(receiverID);
                } catch (IMTPException imtpe) {
                    // Try to get a newer slice and repeat...
                    mainSlice = (MessagingSlice) getFreshSlice(MAIN_SLICE);
                    cid = mainSlice.getAgentLocation(receiverID);
                }
            } catch (ServiceException se) {
                // This container is no longer able to access the Main --> before propagating the exception
                // try to see if the receiver lives locally
                if (myContainer.isLocalAgent(receiverID)) {
                    MessagingSlice localSlice = (MessagingSlice) getIMTPManager().createSliceProxy(getName(), getHorizontalInterface(), getLocalNode());
                    localSlice.dispatchLocally(msg.getSender(), msg, receiverID);
                    //#J2ME_EXCLUDE_BEGIN
                    DeliveryTracing.setTracingInfo("Target-node", localSlice.getNode().getName());
                    //#J2ME_EXCLUDE_END
                    return;
                } else {
                    throw se;
                }
            }

            MessagingSlice targetSlice = oneShotDeliver(cid, msg, receiverID);
            if (targetSlice != null) {
                // Success --> Done
                // On successful message dispatch, put the slice into the slice cache
                cachedSlices.put(receiverID, targetSlice);
                //#J2ME_EXCLUDE_BEGIN
                DeliveryTracing.setTracingInfo("Target-node", targetSlice.getNode().getName());
                //#J2ME_EXCLUDE_END
                return;
            }
            checkRetry(receiverID, cid, attemptsCnt);
            attemptsCnt++;
        }
    }

    private MessagingSlice oneShotDeliver(ContainerID cid, GenericMessage msg, AID receiverID) throws IMTPException, ServiceException, JADESecurityException {
        if (msg.getTraceID() != null) {
            myLogger.log(Logger.FINER, msg.getTraceID() + " - Receiver " + receiverID.getLocalName() + " lives on container " + cid.getName());
        }

        MessagingSlice targetSlice = (MessagingSlice) getSlice(cid.getName());
        try {
            try {
                if (msg.getTraceID() != null) {
                    myLogger.log(Logger.INFO, msg.getTraceID() + " - Delivering message to slice " + targetSlice.getNode().getName());
                }
                targetSlice.dispatchLocally(msg.getSender(), msg, receiverID);
            } catch (IMTPException imtpe) {
                // Try to get a newer slice and repeat...
                if (msg.getTraceID() != null) {
                    myLogger.log(Logger.FINER, msg.getTraceID() + " - Messaging slice on container " + cid.getName() + " unreachable. Try to get a fresh one.");
                }

                targetSlice = (MessagingSlice) getFreshSlice(cid.getName());
                if (msg.getTraceID() != null && (targetSlice != null)) {
                    myLogger.log(Logger.FINER, msg.getTraceID() + " - Fresh slice for container " + cid.getName() + " found.");
                }

                targetSlice.dispatchLocally(msg.getSender(), msg, receiverID);
            }
            if (msg.getTraceID() != null) {
                myLogger.log(Logger.INFO, msg.getTraceID() + " - Delivery OK");
            }
            return targetSlice;
        } catch (NotFoundException nfe) {
            // The agent was found in the GADT, but not on the container where it is supposed to
            // be. Possibly it moved elsewhere in the meanwhile. ==> Try again.
            if (msg.getTraceID() != null) {
                myLogger.log(Logger.FINER, msg.getTraceID() + " - Receiver " + receiverID.getLocalName() + " not found on container " + cid.getName() + ". Possibly he moved elsewhere --> Retry");
            }
        } catch (NullPointerException npe) {
            // This is thrown if targetSlice is null: The agent was found in the GADT,
            // but his container does not exist anymore. Possibly the agent moved elsewhere in
            // the meanwhile ==> Try again.
            if (msg.getTraceID() != null) {
                myLogger.log(Logger.FINER, msg.getTraceID() + " - Container " + cid.getName() + " for receiver " + receiverID.getLocalName() + " does not exist anymore. Possibly the receiver moved elsewhere --> Retry");
            }
        }

        return null;
    }

    private void checkRetry(AID receiver, ContainerID cid, int attemptsCnt) throws NotFoundException {
        if (maxDeliveryRetryAttempts >= 0 && attemptsCnt >= maxDeliveryRetryAttempts) {
            throw new NotFoundException("Agent " + receiver.getLocalName() + " not found in container " + cid.getName() + " where it was supposed to be");
        }
        // Wait a bit before enabling next delivery attempt
        try {
            Thread.sleep(200);
        } catch (InterruptedException ie) {
        }
    }

    private void forwardMessage(GenericMessage msg, AID receiver, String address) throws MTPException {
        // FIXME what if there is no envelope?
        AID aid = msg.getEnvelope().getFrom();

        if (aid == null) {
            //System.err.println("ERROR: null message sender. Aborting message dispatch...");
            if (myLogger.isLoggable(Logger.SEVERE))
                myLogger.log(Logger.SEVERE, "ERROR: null message sender. Aborting message dispatch...");
            return;
        }

        // FIXME The message can no longer be updated
        // if has no address set, then adds the addresses of this platform
        if (!aid.getAllAddresses().hasNext())
            addPlatformAddresses(aid);

        try {
            if (msg.getTraceID() != null) {
                myLogger.log(Logger.INFO, msg.getTraceID() + " - Routing message out to address " + address);
                msg.getEnvelope().addProperties(new Property(ACLMessage.TRACE, msg.getTraceID()));
            }
            localSlice.routeOut(msg.getEnvelope(), msg.getPayload(), receiver, address);
        } catch (IMTPException imtpe) {
            throw new MTPException("Error during message routing", imtpe);
        }

    }

    /**
     * This method is used internally by the platform in order
     * to notify the sender of a message that a failure was reported by
     * the Message Transport Service.
     */
    public void notifyFailureToSender(GenericMessage msg, AID receiver, InternalError ie) {
        //#J2ME_EXCLUDE_BEGIN
        // If msg represents a MultipleGenericMessage, recursive call notifyFailureToSender() for each msg
        if (msg instanceof MultipleGenericMessage) {
            for (GenericMessage g : ((MultipleGenericMessage) msg).getMessages()) {
                notifyFailureToSender(g, receiver, ie);
            }
            return;
        }
        //#J2ME_EXCLUDE_END

        ACLMessage acl = msg.getACLMessage();
        if (acl == null) {
            // ACLMessage can be null in case we get a failure delivering a message coming from an external platform (received by a local MTP).
            // In this case in fact the message is encoded. Try to decode it so that a suitable FAILURE response can be sent back.
            // If the payload is mangled in some way (e.g. encrypted) decoding will fail and no suitable FAILURE response will be sent
            try {
                acl = encInFilter.decodeMessage(msg.getEnvelope(), msg.getPayload());
                acl.setEnvelope(msg.getEnvelope());
                msg.setACLMessage(acl);
            } catch (Exception e) {
                // Just do nothing
                e.printStackTrace();
            }
        }
        if (acl == null) {
            myLogger.log(Logger.WARNING, "Cannot notify failure to sender: GenericMessage contains no ACLMessage");
            return;
        }
        if ("true".equals(acl.getUserDefinedParameter(ACLMessage.IGNORE_FAILURE))) {
            // Ignore the failure
            return;
        }

        GenericCommand cmd = new GenericCommand(MessagingSlice.NOTIFY_FAILURE, MessagingSlice.NAME, null);
        cmd.addParam(msg);
        cmd.addParam(receiver);
        cmd.addParam(ie);

        try {
            submit(cmd);
        } catch (ServiceException se) {
            // It should never happen
            se.printStackTrace();
        }
    }

    /*
     * This method is called before preparing the Envelope of an outgoing message.
     * It checks for all the AIDs present in the message and adds the addresses, if not present
     **/
    private void addPlatformAddresses(AID id) {
        Iterator<String> it = routes.getAddresses();
        while (it.hasNext()) {
            String addr = it.next();
            id.addAddresses(addr);
        }
    }

    // Package scoped since it is accessed by the OutgoingEncoding filter
    final boolean livesHere(AID id) {
        if (!acceptForeignAgents) {
            // All agents in the platform must have a name with the form
            // <local-name>@<platform-name>
            String hap = id.getHap();
            return CaseInsensitiveString.equalsIgnoreCase(hap, platformID);
        } else {
            String[] addresses = id.getAddressesArray();
            if (addresses.length == 0) {
                return true;
            } else {
                boolean allLocalAddresses = true;
                for (String address : addresses) {
                    if (!isPlatformAddress(address)) {
                        allLocalAddresses = false;
                        break;
                    }
                }
                if (allLocalAddresses) {
                    return true;
                } else {
                    // Check in the GADT
                    try {
                        MainContainer impl = myContainer.getMain();
                        if (impl != null) {
                            // Directly use the GADT on the main container
                            getAgentLocation(id);
                        } else {
                            // Use the main slice
                            MessagingSlice mainSlice = (MessagingSlice) getSlice(MAIN_SLICE);
                            try {
                                mainSlice.getAgentLocation(id);
                            } catch (IMTPException imtpe) {
                                // Try to get a newer slice and repeat...
                                mainSlice = (MessagingSlice) getFreshSlice(MAIN_SLICE);
                                mainSlice.getAgentLocation(id);
                            }
                        }
                        return true;
                    } catch (Exception nfe) {
                        // The agent does not live in the platform
                        return false;
                    }// Intra-platform delivery would fail, so try inter-platform

                }
            }
        }
    }

    private boolean isPlatformAddress(String addr) {
        Iterator<String> it = routes.getAddresses();
        while (it.hasNext()) {
            String ad = it.next();
            if (CaseInsensitiveString.equalsIgnoreCase(ad, addr)) {
                return true;
            }
        }
        return false;
    }

    // Work-around for PJAVA compilation
    protected Slice getFreshSlice(String name) throws ServiceException {
        return super.getFreshSlice(name);
    }

    private boolean needSynchDelivery(GenericMessage gMsg) {
        ACLMessage acl = gMsg.getACLMessage();
        if (acl != null) {
            return "true".equals(acl.clearUserDefinedParameter(ACLMessage.SYNCH_DELIVERY));
        }
        return false;
    }

    private void checkTracing(GenericMessage msg) {
        ACLMessage acl = msg.getACLMessage();
        if (acl != null) {
            if (myLogger.isLoggable(Logger.FINE) || "true".equals(acl.getUserDefinedParameter(ACLMessage.TRACE))) {
                msg.setTraceID(ACLMessage.getPerformative(acl.getPerformative()) + "-" + msg.getSender().getLocalName() + "-" + traceCnt);
                traceCnt++;
            }
        }
    }

    private String getTraceId(Envelope env) {
        Iterator<Property> it = env.getAllProperties();
        while (it.hasNext()) {
            Property p = it.next();
            if (p.getName().equals(ACLMessage.TRACE)) {
                return (String) p.getValue();
            }
        }
        return null;
    }

    // For debugging purpose
    public String[] getMessageManagerQueueStatus() {
        return myMessageManager.getQueueStatus();
    }

    // For debugging purpose
    public String[] getMessageManagerThreadPoolStatus() {
        return myMessageManager.getThreadPoolStatus();
    }

    // For debugging purpose
    public String getMessageManagerGlobalInfo() {
        return myMessageManager.getGlobalInfo();
    }

    // For debugging purpose
    public Thread[] getMessageManagerThreadPool() {
        return myMessageManager.getThreadPool();
    }

    protected void clearCachedSlice(String name) {
        if (cachedSlices != null) {
            cachedSlices.clear();
            myLogger.log(Logger.CONFIG, "Clearing cache");
        }
        super.clearCachedSlice(name);
    }

    public String dump(String key) {
        StringBuilder sb = new StringBuilder("LOCAL ALIASES:\n");
        if (localAliases.size() > 0) {
            sb.append(stringifyAliasesMap(localAliases));
        } else {
            sb.append("---\n");
        }

        if (globalAliases != null) {
            sb.append("GLOBAL ALIASES:\n");
            if (globalAliases.size() > 0) {
                sb.append(stringifyAliasesMap(globalAliases));
            } else {
                sb.append("---\n");
            }
        }
        return sb.toString();
    }

    private String stringifyAliasesMap(Hashtable<Object, AID> aliases) {
        StringBuilder sb = new StringBuilder();
        for (Object o : aliases.keySet()) {
            AID alias = (AID) o;
            AID agent = aliases.get(alias);
            sb.append("- ").append(alias.getLocalName()).append(" --> ").append(agent.getLocalName()).append("\n");
        }
        return sb.toString();
    }

    public static class UnknownACLEncodingException extends NotFoundException {
        UnknownACLEncodingException(String msg) {
            super(msg);
        }
    } // End of UnknownACLEncodingException class

    /**
     * Inner class CommandSourceSink
     * This inner class handles the messaging commands on the command
     * issuer side, turning them into horizontal commands and
     * forwarding them to remote slices when necessary.
     */
    private class CommandSourceSink implements Sink {

        public void consume(VerticalCommand cmd) {

            try {
                String name = cmd.getName();

                switch (name) {
                    case MessagingSlice.SEND_MESSAGE -> handleSendMessage(cmd);
                    case MessagingSlice.NOTIFY_FAILURE -> handleNotifyFailure(cmd);
                    case MessagingSlice.INSTALL_MTP -> {
                        Object result = handleInstallMTP(cmd);
                        cmd.setReturnValue(result);
                    }
                    case MessagingSlice.UNINSTALL_MTP -> handleUninstallMTP(cmd);
                    case MessagingSlice.NEW_MTP -> handleNewMTP(cmd);
                    case MessagingSlice.DEAD_MTP -> handleDeadMTP(cmd);
                    case MessagingSlice.SET_PLATFORM_ADDRESSES -> handleSetPlatformAddresses(cmd);
                }
            } catch (IMTPException | NotFoundException | ServiceException | MTPException imtpe) {
                cmd.setReturnValue(imtpe);
            } catch (Throwable t) {
                t.printStackTrace();
                cmd.setReturnValue(t);
            }
        }

        // Vertical command handler methods

        private void handleSendMessage(VerticalCommand cmd) {
            Object[] params = cmd.getParams();
            AID sender = (AID) params[0];
            GenericMessage msg = (GenericMessage) params[1];
            AID dest = (AID) params[2];
            // Since message delivery is asynchronous we use the GenericMessage
            // as a temporary holder for the sender principal and credentials
            msg.setSenderPrincipal(cmd.getPrincipal());
            msg.setSenderCredentials(cmd.getCredentials());
            msg.setSender(sender);
            checkTracing(msg);
            if (msg.getTraceID() != null) {
                myLogger.log(Logger.INFO, "MessagingService source sink handling message " + MessageManager.stringify(msg) + " for receiver " + dest.getName() + ". TraceID = " + msg.getTraceID());
            }
            if (needSynchDelivery(msg)) {
                // Synchronous delivery: skip the MessageManager
                deliverNow(msg, dest);
            } else {
                // Normal (asynchronous) delivery
                myMessageManager.deliver(msg, dest, MessagingService.this);
                if (msg.getTraceID() != null) {
                    myLogger.log(Logger.INFO, msg.getTraceID() + " - Message enqueued to MessageManager.");
                }
            }
        }

        private void handleNotifyFailure(VerticalCommand cmd) {
            Object[] params = cmd.getParams();
            GenericMessage msg = (GenericMessage) params[0];
            AID receiver = (AID) params[1];
            InternalError ie = (InternalError) params[2];

            // The acl message contained inside the GenericMessage cannot be null; the notifyFailureToSender() method already checks that
            ACLMessage aclmsg = msg.getACLMessage();
            // Sanity check to avoid infinite loops
            if ((aclmsg.getSender() == null) || (aclmsg.getSender().equals(myContainer.getAMS()))) {
                return;
            }
            if ("true".equals(aclmsg.getUserDefinedParameter(ACLMessage.DONT_NOTIFY_FAILURE))) {
                return;
            }

            // Send back a failure message
            final ACLMessage failure = aclmsg.createReply();
            failure.setPerformative(ACLMessage.FAILURE);
            final AID theAMS = myContainer.getAMS();
            failure.setSender(theAMS);
            failure.setLanguage(FIPANames.ContentLanguage.FIPA_SL);

            // FIXME: the content is not completely correct, but that should
            // also avoid creating wrong content
            String content = "( (action " + msg.getSender().toString();
            content = content + " (ACLMessage) ) (MTS-error " + receiver + " " + ie.getMessage() + ") )";
            failure.setContent(content);

            try {
                GenericCommand command = new GenericCommand(MessagingSlice.SEND_MESSAGE, MessagingSlice.NAME, null);
                command.addParam(theAMS);
                GenericMessage gm = new GenericMessage(failure);
                gm.setAMSFailure(true);
                command.addParam(gm);
                command.addParam(failure.getAllReceiver().next());
                // FIXME: We should set the AMS principal and credentials

                submit(command);
            } catch (ServiceException se) {
                // It should never happen
                se.printStackTrace();
            }
        }

        private MTPDescriptor handleInstallMTP(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException, MTPException {
            Object[] params = cmd.getParams();
            String address = (String) params[0];
            ContainerID cid = (ContainerID) params[1];
            String className = (String) params[2];

            MessagingSlice targetSlice = (MessagingSlice) getSlice(cid.getName());
            try {
                return targetSlice.installMTP(address, className);
            } catch (IMTPException imtpe) {
                targetSlice = (MessagingSlice) getFreshSlice(cid.getName());
                return targetSlice.installMTP(address, className);
            }
        }

        private void handleUninstallMTP(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException, MTPException {
            Object[] params = cmd.getParams();
            String address = (String) params[0];
            ContainerID cid = (ContainerID) params[1];

            MessagingSlice targetSlice = (MessagingSlice) getSlice(cid.getName());
            try {
                targetSlice.uninstallMTP(address);
            } catch (IMTPException imtpe) {
                targetSlice = (MessagingSlice) getFreshSlice(cid.getName());
                targetSlice.uninstallMTP(address);
            }
        }

        private void handleNewMTP(VerticalCommand cmd) throws IMTPException, ServiceException {
            Object[] params = cmd.getParams();
            MTPDescriptor mtp = (MTPDescriptor) params[0];
            ContainerID cid = (ContainerID) params[1];

            MessagingSlice mainSlice = (MessagingSlice) getSlice(MAIN_SLICE);
            try {
                mainSlice.newMTP(mtp, cid);
            } catch (IMTPException imtpe) {
                mainSlice = (MessagingSlice) getFreshSlice(MAIN_SLICE);
                mainSlice.newMTP(mtp, cid);
            }
        }

        private void handleDeadMTP(VerticalCommand cmd) throws IMTPException, ServiceException {
            Object[] params = cmd.getParams();
            MTPDescriptor mtp = (MTPDescriptor) params[0];
            ContainerID cid = (ContainerID) params[1];

            MessagingSlice mainSlice = (MessagingSlice) getSlice(MAIN_SLICE);
            try {
                mainSlice.deadMTP(mtp, cid);
            } catch (IMTPException imtpe) {
                mainSlice = (MessagingSlice) getFreshSlice(MAIN_SLICE);
                mainSlice.deadMTP(mtp, cid);
            }

        }

        private void handleSetPlatformAddresses(VerticalCommand cmd) {
            Object[] params = cmd.getParams();
            AID id = (AID) params[0];
            id.clearAllAddresses();
            addPlatformAddresses(id);
        }

    } // END of inner class CommandSourceSink

    /**
     * Inner class CommandTargetSink
     */
    private class CommandTargetSink implements Sink {

        public void consume(VerticalCommand cmd) {

            try {
                String name = cmd.getName();
                switch (name) {
                    case MessagingSlice.SEND_MESSAGE -> handleSendMessage(cmd);
                    case MessagingSlice.INSTALL_MTP -> {
                        Object result = handleInstallMTP(cmd);
                        cmd.setReturnValue(result);
                    }
                    case MessagingSlice.UNINSTALL_MTP -> handleUninstallMTP(cmd);
                    case MessagingSlice.NEW_MTP -> handleNewMTP(cmd);
                    case MessagingSlice.DEAD_MTP -> handleDeadMTP(cmd);
                    case MessagingSlice.SET_PLATFORM_ADDRESSES -> handleSetPlatformAddresses(cmd);
                    case Service.NEW_SLICE -> handleNewSlice(cmd);
                }
            } catch (IMTPException | NotFoundException | ServiceException | MTPException imtpe) {
                cmd.setReturnValue(imtpe);
            }
        }

        private void handleSendMessage(VerticalCommand cmd) throws NotFoundException {
            Object[] params = cmd.getParams();
            AID senderID = (AID) params[0];
            GenericMessage gMsg = (GenericMessage) params[1];
            AID receiverID = (AID) params[2];
            receiverID = resolveLocalAlias(receiverID);

            //#J2ME_EXCLUDE_BEGIN
            if (gMsg instanceof MultipleGenericMessage) {
                // Multiple-message delivery case.
                List<GenericMessage> gmm = ((MultipleGenericMessage) gMsg).getMessages();
                ACLMessage[] mm = new ACLMessage[gmm.size()];
                int k = 0;
                for (GenericMessage g : gmm) {
                    if (g.getTraceID() != null) {
                        myLogger.log(Logger.INFO, g.getTraceID() + " - MessagingService target sink posting message to receiver " + receiverID.getLocalName());
                    }
                    ACLMessage msg = g.getACLMessage();
                    if (!msg.getSender().equals(g.getSender()) && g.getSender() != null) {
                        myLogger.log(Logger.FINE, "Attaching real-sender user defined parameter: " + g.getSender().getName());
                        // Sender indicated in the message different than the real sender --> store the latter in the REAL_SENDER user defined param
                        msg.addUserDefinedParameter(ACLMessage.REAL_SENDER, g.getSender().getName());
                    }
                    mm[k] = msg;
                    k++;
                }

                boolean found = myContainer.postMessagesBlockToLocalAgent(mm, receiverID);
                if (!found) {
                    throw new NotFoundException("Messaging service slice failed to find " + receiverID);
                }

                for (GenericMessage g : gmm) {
                    postedMessageCounter++;
                    updateDeliveryTimeMeasurement(g);
                    if (g.getTraceID() != null) {
                        myLogger.log(Logger.INFO, g.getTraceID() + " - Message posted");
                    }
                }
                return;
            }
            //#J2ME_EXCLUDE_END

            // Normal (single-message delivery) case
            if (gMsg.getTraceID() != null) {
                myLogger.log(Logger.INFO, gMsg.getTraceID() + " - MessagingService target sink posting message to receiver " + receiverID.getLocalName());
            }
            postMessage(senderID, gMsg.getACLMessage(), receiverID);
            //#J2ME_EXCLUDE_BEGIN
            postedMessageCounter++;
            updateDeliveryTimeMeasurement(gMsg);
            //#J2ME_EXCLUDE_END
            if (gMsg.getTraceID() != null) {
                myLogger.log(Logger.INFO, gMsg.getTraceID() + " - Message posted");
            }
        }

        private void manageMessage(AID senderID, GenericMessage msg, AID receiverID) throws NotFoundException {
            //#J2ME_EXCLUDE_BEGIN
            // If gmsg represents a MultipleGenericMessage recursively call restore() for each message
            if (msg instanceof MultipleGenericMessage) {
                System.out.println("Multiple delivery: posting " + ((MultipleGenericMessage) msg).getMessages().size() + " messages to receiver " + receiverID.getLocalName());
                for (GenericMessage g : ((MultipleGenericMessage) msg).getMessages()) {
                    manageMessage(g.getSender(), g, receiverID);
                }
                return;
            }
            //#J2ME_EXCLUDE_END

            if (msg.getTraceID() != null) {
                myLogger.log(Logger.INFO, msg.getTraceID() + " - MessagingService target sink posting message to receiver " + receiverID.getLocalName());
            }
            postMessage(senderID, msg.getACLMessage(), receiverID);
            //#J2ME_EXCLUDE_BEGIN
            postedMessageCounter++;
            updateDeliveryTimeMeasurement(msg);
            //#J2ME_EXCLUDE_END
            if (msg.getTraceID() != null) {
                myLogger.log(Logger.INFO, msg.getTraceID() + " - Message posted");

            }
        }

        private MTPDescriptor handleInstallMTP(VerticalCommand cmd) throws IMTPException, ServiceException, MTPException {
            Object[] params = cmd.getParams();
            String address = (String) params[0];
            String className = (String) params[1];

            return installMTP(address, className);
        }

        private void handleUninstallMTP(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException, MTPException {
            Object[] params = cmd.getParams();
            String address = (String) params[0];

            uninstallMTP(address);
        }

        private void handleNewMTP(VerticalCommand cmd) throws IMTPException, ServiceException {
            Object[] params = cmd.getParams();
            MTPDescriptor mtp = (MTPDescriptor) params[0];
            ContainerID cid = (ContainerID) params[1];

            newMTP(mtp, cid);
        }

        private void handleDeadMTP(VerticalCommand cmd) throws IMTPException, ServiceException {
            Object[] params = cmd.getParams();
            MTPDescriptor mtp = (MTPDescriptor) params[0];
            ContainerID cid = (ContainerID) params[1];

            deadMTP(mtp, cid);
        }

        private void handleSetPlatformAddresses(VerticalCommand cmd) {
        }

        private void handleNewSlice(VerticalCommand cmd) {
            MainContainer impl = myContainer.getMain();
            if (impl != null) {
                Object[] params = cmd.getParams();
                String newSliceName = (String) params[0];
                try {
                    // Be sure to get the new (fresh) slice --> Bypass the service cache
                    MessagingSlice newSlice = (MessagingSlice) getFreshSlice(newSliceName);

                    // Send all possible routes to the new slice
                    ContainerID[] cids = impl.containerIDs();
                    for (ContainerID cid : cids) {
                        try {
                            List<MTPDescriptor> mtps = impl.containerMTPs(cid);
                            for (MTPDescriptor o : mtps) {
                                newSlice.addRoute(o, cid.getName());
                            }
                        } catch (NotFoundException nfe) {
                            // Should never happen
                            nfe.printStackTrace();
                        }
                    }

                    // If the new slice is on a replicated Main Container send it all current aliases
                    if (newSlice.getNode().hasPlatformManager()) {
                        newSlice.currentAliases(globalAliases);
                    }
                } catch (ServiceException se) {
                    // Should never happen since getSlice() should always work on the Main container
                    se.printStackTrace();
                } catch (IMTPException imtpe) {
                    myLogger.log(Logger.WARNING, "Error notifying current information to new Messaging-Slice " + newSliceName, imtpe);
                }
            }
        }

        private void postMessage(AID senderID, ACLMessage msg, AID receiverID) throws NotFoundException {
            if (!msg.getSender().equals(senderID)) {
                myLogger.log(Logger.FINE, "Attaching real-sender user defined parameter: " + senderID.getName());
                // Sender indicated in the message different than the real sender --> store the latter in the REAL_SENDER user defined param
                msg.addUserDefinedParameter(ACLMessage.REAL_SENDER, senderID.getName());
            }
            boolean found = myContainer.postMessageToLocalAgent(msg, receiverID);
            if (!found) {
                throw new NotFoundException("Messaging service slice failed to find " + receiverID);
            }
        }

        private MTPDescriptor installMTP(String address, String className) throws ServiceException, MTPException {

            try {
                // Create the MTP
                Class<?> c = Class.forName(className);
                MTP proto = null;
                try {
                    proto = (MTP) c.getDeclaredConstructor().newInstance();
                } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                        | SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                InChannel.Dispatcher dispatcher = (env, payload) -> {
                    //log("Message from remote platform received", 2);

                    if (myLogger.isLoggable(Logger.FINE))
                        myLogger.log(Logger.FINE, "Message from remote platform received");

                    // To avoid message loops, make sure that the ID of this ACC does
                    // not appear in a previous 'received' stamp

                    ReceivedObject[] stamps = env.getStamps();
                    for (ReceivedObject stamp : stamps) {
                        String id = stamp.getBy();
                        if (CaseInsensitiveString.equalsIgnoreCase(id, accID)) {
                            System.err.println("ERROR: Message loop detected !!!");
                            System.err.println("Route is: ");
                            for (int j = 0; j < stamps.length; j++)
                                System.err.println("[" + j + "]" + stamps[j].getBy());
                            System.err.println("Message dispatch aborted.");
                            return;
                        }
                    }

                    // Put a 'received-object' stamp in the envelope
                    ReceivedObject ro = new ReceivedObject();
                    ro.setBy(accID);
                    ro.setDate(new Date());
                    env.setReceived(ro);

                    Iterator<AID> it = env.getAllIntendedReceiver();
                    // FIXME: There is a problem if no 'intended-receiver' is present,
                    // but this should not happen
                    while (it.hasNext()) {
                        AID rcv = it.next();
                        GenericMessage msg = new GenericMessage(env, payload);
                        String traceId = getTraceId(env);
                        if (traceId != null) {
                            myLogger.log(Logger.INFO, "MTP In-Channel handling message from the outside for receiver " + rcv.getName() + ". TraceID = " + traceId);
                            msg.setTraceID(traceId);
                        }
                        myMessageManager.deliver(msg, rcv, MessagingService.this);
                    }
                };

                if (address == null) {
                    // Let the MTP choose the address
                    TransportAddress ta = proto.activate(dispatcher, myProfile);
                    address = proto.addrToStr(ta);
                } else {
                    // Convert the given string into a TransportAddress object and use it
                    TransportAddress ta = proto.strToAddr(address);
                    proto.activate(dispatcher, ta, myProfile);
                }
                MTPDescriptor result = new MTPDescriptor(proto.getName(), className, new String[]{address}, proto.getSupportedProtocols());
                routes.addLocalMTP(address, proto, result);

                String[] pp = result.getSupportedProtocols();
                for (String value : pp) {
                    //log("Added Route-Via-MTP for protocol "+pp[i], 1);
                    if (myLogger.isLoggable(Logger.CONFIG))
                        myLogger.log(Logger.CONFIG, "Added Route-Via-MTP for protocol " + value);

                }

                String[] addresses = result.getAddresses();
                for (String s : addresses) {
                    myContainer.addAddressToLocalAgents(s);
                }

                GenericCommand gCmd = new GenericCommand(MessagingSlice.NEW_MTP, MessagingSlice.NAME, null);
                gCmd.addParam(result);
                gCmd.addParam(myContainer.getID());
                submit(gCmd);

                return result;
            }
			/*#DOTNET_INCLUDE_BEGIN
			 catch(System.TypeLoadException tle)
			 {
			 ClassNotFoundException cnfe = new ClassNotFoundException(tle.get_Message());
			 throw new MTPException("The class " + className  + " raised IllegalAccessException (see nested exception)", cnfe);
			 }
			 catch(System.TypeInitializationException tie)
			 {
			 InstantiationException ie = new InstantiationException(tie.get_Message());
			 throw new MTPException("The class " + className + " raised InstantiationException (see nested exception)", ie);
			 }
			 #DOTNET_INCLUDE_END*/ catch (ClassNotFoundException cnfe) {
                throw new MTPException("ERROR: The class " + className + " for the " + address + " MTP was not found");
            } catch (InstantiationException ie) {
                throw new MTPException("The class " + className + " raised InstantiationException (see nested exception)", ie);
            } catch (IllegalAccessException iae) {
                throw new MTPException("The class " + className + " raised IllegalAccessException (see nested exception)", iae);
            }
        }

        private void uninstallMTP(String address) throws ServiceException, MTPException {

            MTPInfo info = routes.removeLocalMTP(address);
            if (info != null) {
                MTP proto = info.getMTP();
                TransportAddress ta = proto.strToAddr(address);
                proto.deactivate(ta);
                MTPDescriptor desc = info.getDescriptor();
                //MTPDescriptor desc = new MTPDescriptor(proto.getName(), proto.getClass().getName(), new String[] {address}, proto.getSupportedProtocols());

                String[] addresses = desc.getAddresses();
                for (String s : addresses) {
                    myContainer.removeAddressFromLocalAgents(s);
                }

                GenericCommand gCmd = new GenericCommand(MessagingSlice.DEAD_MTP, MessagingSlice.NAME, null);
                gCmd.addParam(desc);
                gCmd.addParam(myContainer.getID());
                submit(gCmd);
            } else {
                throw new MTPException("No such address was found on this container: " + address);
            }
        }

        private void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException, ServiceException {
            MainContainer impl = myContainer.getMain();

            if (impl != null) {

                // Update the routing tables of all the other slices
                Slice[] slices = getAllSlices();
                for (Slice value : slices) {
                    try {
                        MessagingSlice slice = (MessagingSlice) value;
                        String sliceName = slice.getNode().getName();
                        if (!sliceName.equals(cid.getName())) {
                            slice.addRoute(mtp, cid.getName());
                        }
                    } catch (Throwable t) {
                        // Re-throw allowed exceptions
                        if (t instanceof IMTPException) {
                            throw (IMTPException) t;
                        }
                        if (t instanceof ServiceException) {
                            throw (ServiceException) t;
                        }
                        //System.err.println("### addRoute() threw " + t.getClass().getName() + " ###");
                        myLogger.log(Logger.WARNING, "### addRoute() threw " + t + " ###");
                    }
                }
                impl.newMTP(mtp, cid);
            } else {
                // Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
            }
        }

        private void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException, ServiceException {
            MainContainer impl = myContainer.getMain();

            if (impl != null) {

                // Update the routing tables of all the other slices
                Slice[] slices = getAllSlices();
                for (Slice value : slices) {
                    try {
                        MessagingSlice slice = (MessagingSlice) value;
                        String sliceName = slice.getNode().getName();
                        if (!sliceName.equals(cid.getName())) {
                            slice.removeRoute(mtp, cid.getName());
                        }
                    } catch (Throwable t) {
                        // Re-throw allowed exceptions
                        if (t instanceof IMTPException) {
                            throw (IMTPException) t;
                        }
                        if (t instanceof ServiceException) {
                            throw (ServiceException) t;
                        }

                        myLogger.log(Logger.WARNING, "### removeRoute() threw " + t + " ###");
                    }
                }
                impl.deadMTP(mtp, cid);
            } else {
                // Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
            }
        }
    } // END of inner class CommandTargetSink

    /**
     * Inner class for this service: this class receives commands from
     * service <code>Sink</code> and serves them, coordinating with
     * remote parts of this service through the <code>Slice</code>
     * interface (that extends the <code>Service.Slice</code>
     * interface).
     */
    private class ServiceComponent implements Slice {
        // Implementation of the Service.Slice interface
        public Service getService() {
            return MessagingService.this;
        }

        public Node getNode() throws ServiceException {
            try {
                return MessagingService.this.getLocalNode();
            } catch (IMTPException imtpe) {
                throw new ServiceException("Problem in contacting the IMTP Manager", imtpe);
            }
        }

        public VerticalCommand serve(HorizontalCommand cmd) {
            VerticalCommand result = null;
            try {
                String cmdName = cmd.getName();
                Object[] params = cmd.getParams();

                switch (cmdName) {
                    case MessagingSlice.H_DISPATCHLOCALLY -> {
                        GenericCommand gCmd = new GenericCommand(MessagingSlice.SEND_MESSAGE, MessagingSlice.NAME, null);
                        AID senderAID = (AID) params[0];
                        GenericMessage msg = (GenericMessage) params[1];
                        AID receiverID = (AID) params[2];
                        if (params.length == 4) {
                            msg.setTimeStamp((Long) params[3]);
                        }
                        if (msg.getTraceID() != null) {
                            myLogger.log(Logger.INFO, "MessagingService-slice: received message " + MessageManager.stringify(msg) + " for receiver " + receiverID.getLocalName() + ". Trace ID = " + msg.getTraceID());
                        }
                        gCmd.addParam(senderAID);
                        gCmd.addParam(msg);
                        gCmd.addParam(receiverID);
                        result = gCmd;
                    }
                    case MessagingSlice.H_GETAGENTLOCATION -> {
                        AID agentID = (AID) params[0];
                        cmd.setReturnValue(getAgentLocation(agentID));
                    }
                    case MessagingSlice.H_ROUTEOUT -> {
                        Envelope env = (Envelope) params[0];
                        byte[] payload = (byte[]) params[1];
                        AID receiverID = (AID) params[2];
                        String address = (String) params[3];

                        routeOut(env, payload, receiverID, address);
                    }
                    case MessagingSlice.H_INSTALLMTP -> {
                        GenericCommand gCmd = new GenericCommand(MessagingSlice.INSTALL_MTP, MessagingSlice.NAME, null);
                        String address = (String) params[0];
                        String className = (String) params[1];
                        gCmd.addParam(address);
                        gCmd.addParam(className);

                        result = gCmd;
                    }
                    case MessagingSlice.H_UNINSTALLMTP -> {
                        GenericCommand gCmd = new GenericCommand(MessagingSlice.UNINSTALL_MTP, MessagingSlice.NAME, null);
                        String address = (String) params[0];
                        gCmd.addParam(address);

                        result = gCmd;
                    }
                    case MessagingSlice.H_NEWMTP -> {
                        MTPDescriptor mtp = (MTPDescriptor) params[0];
                        ContainerID cid = (ContainerID) params[1];

                        GenericCommand gCmd = new GenericCommand(MessagingSlice.NEW_MTP, MessagingSlice.NAME, null);
                        gCmd.addParam(mtp);
                        gCmd.addParam(cid);

                        result = gCmd;
                    }
                    case MessagingSlice.H_DEADMTP -> {
                        MTPDescriptor mtp = (MTPDescriptor) params[0];
                        ContainerID cid = (ContainerID) params[1];

                        GenericCommand gCmd = new GenericCommand(MessagingSlice.DEAD_MTP, MessagingSlice.NAME, null);
                        gCmd.addParam(mtp);
                        gCmd.addParam(cid);

                        result = gCmd;
                    }
                    case MessagingSlice.H_ADDROUTE -> {
                        MTPDescriptor mtp = (MTPDescriptor) params[0];
                        String sliceName = (String) params[1];

                        addRoute(mtp, sliceName);
                    }
                    case MessagingSlice.H_REMOVEROUTE -> {
                        MTPDescriptor mtp = (MTPDescriptor) params[0];
                        String sliceName = (String) params[1];

                        removeRoute(mtp, sliceName);
                    }
                    case MessagingSlice.H_NEWALIAS -> {
                        AID alias = (AID) params[0];
                        AID name = (AID) params[1];

                        newAlias(alias, name);
                        replicationHandle.invokeReplicatedMethod("newAlias", params);
                    }
                    case MessagingSlice.H_DEADALIAS -> {
                        AID alias = (AID) params[0];

                        deadAlias(alias);
                        replicationHandle.invokeReplicatedMethod("deadAlias", params);
                    }
                    case MessagingSlice.H_CURRENTALIASES -> globalAliases = (Hashtable<Object, AID>) params[0];
                    case MessagingSlice.H_TRANSFERLOCALALIASES -> {
                        AID agent = (AID) params[0];
                        List<?> aliases = (List<?>) params[1];
                        for (Object alias : aliases) {
                            localAliases.put(alias, agent);
                        }
                    }
                }
            } catch (Throwable t) {
                cmd.setReturnValue(t);
            }
            return result;
        }

        // Private methods
        private void routeOut(Envelope env, byte[] payload, AID receiverID, String address) throws IMTPException, MTPException {
            RoutingTable.OutPort out = routes.lookup(address);
            //log("Routing message to "+receiverID.getName()+" towards port "+out, 2);
            if (myLogger.isLoggable(Logger.FINE))
                myLogger.log(Logger.FINE, "Routing message to " + receiverID.getName() + " towards port " + out);

            if (out != null)
                out.route(env, payload, receiverID, address);
            else
                throw new MTPException("No suitable route found for address " + address + ".");
        }

        private void addRoute(MTPDescriptor mtp, String sliceName) throws ServiceException {
            // Be sure the slice is fresh --> bypass the service cache
            MessagingSlice slice = (MessagingSlice) getFreshSlice(sliceName);
            if (routes.addRemoteMTP(mtp, sliceName, slice)) {
                // This is actually a new MTP --> Add the new address to all local agents.
                // NOTE that a notification about a remote MTP can be received more than once in case of fault and successive recovery of the Main Container
                String[] pp = mtp.getSupportedProtocols();
                for (String s : pp) {
                    if (myLogger.isLoggable(Logger.CONFIG))
                        myLogger.log(Logger.CONFIG, "Added Route-Via-Slice(" + sliceName + ") for protocol " + s);
                }

                String[] addresses = mtp.getAddresses();
                for (String address : addresses) {
                    myContainer.addAddressToLocalAgents(address);
                }
            }
        }

        private void removeRoute(MTPDescriptor mtp, String sliceName) throws ServiceException {
            // Don't care about whether or not the slice is fresh. Only the name matters.
            MessagingSlice slice = (MessagingSlice) getSlice(sliceName);
            routes.removeRemoteMTP(mtp, sliceName, slice);

            String[] pp = mtp.getSupportedProtocols();
            for (String s : pp) {
                if (myLogger.isLoggable(Logger.CONFIG))
                    myLogger.log(Logger.CONFIG, "Removed Route-Via-Slice(" + sliceName + ") for protocol " + s);

            }

            String[] addresses = mtp.getAddresses();
            for (String address : addresses) {
                myContainer.removeAddressFromLocalAgents(address);
            }
        }

    } // End of ServiceComponent class
}
