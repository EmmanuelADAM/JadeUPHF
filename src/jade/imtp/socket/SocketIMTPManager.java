package jade.imtp.socket;

import jade.core.IMTPException;
import jade.core.IMTPManager;
import jade.core.Node;
import jade.core.PlatformManager;
import jade.core.PlatformManagerImpl;
import jade.core.Profile;
import jade.core.Service;
import jade.core.ServiceException;
import jade.core.SliceProxy;
import jade.mtp.TransportAddress;
import jade.util.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * A lightweight Internal Message Transport Protocol that replaces
 * {@code jade.imtp.rmi}: plain TCP sockets and virtual threads instead of
 * java.rmi. Each JVM opens a single listening socket (instead of RMI's
 * registry + Node + Service Manager ports), each remote call is a
 * short-lived connection carrying one request and one response by standard
 * Java serialization, and there is no RMI registry, stub classes, or
 * distributed garbage collection to keep alive in the background.
 * <p>
 * The address of the platform's Main Container does not need to be
 * discovered dynamically: as with RMI, it is already known statically at
 * launch time (the  -host  /  -port   options, i.e.
 * {@link Profile#MAIN_HOST}/{@link Profile#MAIN_PORT}), so connecting to it
 * is a direct socket connect -- no naming service involved.
 *
 * @author Claude
 */
public class SocketIMTPManager implements IMTPManager {

    /**
     * Default TCP port used when none is configured, distinct from RMI's
     * traditional 1099 so both IMTPs can be experimented with side by side.
     */
    public static final int DEFAULT_PORT = 7099;

    private static final String PROTO_PREFIX = "socket://";

    private final Logger myLogger = Logger.getMyLogger(getClass().getName());
    private Profile myProfile;
    private String mainHost;
    private int mainPort;
    private String localHost;
    private int localPort;
    private String localAddr;
    private String originalPMAddr;
    private SocketNode localNode;
    private SocketServer server;

    public void initialize(Profile p) throws IMTPException {
        myProfile = p;

        mainHost = myProfile.getParameter(Profile.MAIN_HOST, null);
        mainPort = DEFAULT_PORT;
        try {
            mainPort = Integer.parseInt(myProfile.getParameter(Profile.MAIN_PORT, null));
        } catch (Exception e) {
            // Use default
        }

        if (myProfile.isMasterMain()) {
            localHost = myProfile.getParameter(Profile.LOCAL_HOST, mainHost);
            localPort = mainPort;
        } else {
            localHost = myProfile.getParameter(Profile.LOCAL_HOST, null);
            localPort = DEFAULT_PORT;
        }
        try {
            localPort = Integer.parseInt(myProfile.getParameter(Profile.LOCAL_PORT, null));
        } catch (Exception e) {
            // Use default
        }

        if (myProfile.isBackupMain()) {
            originalPMAddr = PROTO_PREFIX + mainHost + ":" + mainPort + "/";
        }

        if (myLogger.isLoggable(Logger.CONFIG)) {
            myLogger.log(Logger.CONFIG, "IMTP parameters: main-host=" + mainHost + " main-port=" + mainPort + " local-host=" + localHost + " local-port=" + localPort);
        }

        localNode = new SocketNode(PlatformManager.NO_NAME, myProfile.getBooleanProperty(Profile.MAIN, true), localHost, localPort);
        try {
            server = new SocketServer(localHost, localPort, localNode);
        } catch (IOException ioe) {
            throw new IMTPException("Cannot open the local IMTP listening socket on " + localHost + ":" + localPort, ioe);
        }
        localNode.bindLocalServer(server);

        // A configured port of 0 means "pick any free port": now that the
        // server is actually bound, remember the port it really got so
        // other containers can be told the true address to connect back to.
        localPort = server.getLocalPort();
        localAddr = PROTO_PREFIX + localHost + ":" + localPort + "/";
    }

    public void exportPlatformManager(PlatformManager mgr) throws IMTPException {
        mgr.setLocalAddress(localAddr);
        server.exportPlatformManager(mgr);

        // Attach to the original Platform manager, if any (fault-tolerant deployment)
        if (originalPMAddr != null) {
            try {
                PlatformManager originalPM = getPlatformManagerProxy(originalPMAddr);
                ((PlatformManagerImpl) mgr).setPlatformName(originalPM.getPlatformName());
                myLogger.log(Logger.INFO, "Connecting to master Main Container at address " + originalPMAddr);
                mgr.addReplica(originalPMAddr, true); // Do as if it was a propagated info
                originalPM.addReplica(localAddr, false);
            } catch (ServiceException se) {
                throw new IMTPException("Cannot attach to the original PlatformManager.", se);
            } catch (IMTPException imtpe) {
                Throwable t = imtpe.getNested();
                if (t instanceof ConnectException) {
                    // The master main container does not exist. Become the leader
                    myLogger.log(Logger.INFO, "No master Main Container found at address " + originalPMAddr + ". Take the leadership");
                    originalPMAddr = null;
                    myProfile.setParameter(Profile.LOCAL_SERVICE_MANAGER, "false");
                } else {
                    throw imtpe;
                }
            }
        }
    }

    public void unexportPlatformManager(PlatformManager mgr) throws IMTPException {
        server.unexportPlatformManager();
    }

    public PlatformManager getPlatformManagerProxy() throws IMTPException {
        return getPlatformManagerProxy(PROTO_PREFIX + mainHost + ":" + mainPort + "/");
    }

    public PlatformManager getPlatformManagerProxy(String addr) throws IMTPException {
        TransportAddress ta = stringToAddr(addr);
        return new SocketPlatformManagerProxy(ta.getHost(), Integer.parseInt(ta.getPort()), addr);
    }

    public void reconnected(PlatformManager pm) {
        // Nothing to do: connections are opened on demand, per call, not kept alive.
    }

    public Service.Slice createSliceProxy(String serviceName, Class<?> itf, Node where) throws IMTPException {
        try {
            Class<?> proxyClass = Class.forName(serviceName + "Proxy");
            Service.Slice proxy = (Service.Slice) proxyClass.getDeclaredConstructor().newInstance();
            if (proxy instanceof SliceProxy) {
                ((SliceProxy) proxy).setNode(where);
            } else if (proxy instanceof Service.SliceProxy) {
                ((Service.SliceProxy) proxy).setNode(where);
            } else {
                throw new IMTPException("Class " + proxyClass.getName() + " is not a slice proxy.");
            }
            return proxy;
        } catch (Exception e) {
            throw new IMTPException("Error creating a slice proxy", e);
        }
    }

    public Node getLocalNode() throws IMTPException {
        return localNode;
    }

    public void shutDown() {
        try {
            if (localNode != null) {
                localNode.exit();
            }
        } catch (IMTPException imtpe) {
            // Should never happen since this is a local call
            imtpe.printStackTrace();
        }
        if (server != null) {
            server.shutDown();
        }
    }

    public List<TransportAddress> getLocalAddresses() throws IMTPException {
        try {
            List<TransportAddress> l = new LinkedList<>();
            l.add(new SocketAddress(InetAddress.getLocalHost().getHostName(), String.valueOf(localPort)));
            return l;
        } catch (Exception e) {
            throw new IMTPException("Exception reading local addresses", e);
        }
    }

    public TransportAddress stringToAddr(String url) throws IMTPException {
        if (url == null) {
            throw new IMTPException("Null URL");
        }
        int protoEnd = url.indexOf("://");
        if (protoEnd < 0) {
            throw new IMTPException("Invalid URL (missing protocol): " + url);
        }
        String rest = url.substring(protoEnd + 3);
        int slash = rest.indexOf('/');
        String hostPort = (slash >= 0) ? rest.substring(0, slash) : rest;
        int colon = hostPort.indexOf(':');
        if (colon < 0) {
            throw new IMTPException("Invalid URL (missing port): " + url);
        }
        return new SocketAddress(hostPort.substring(0, colon), hostPort.substring(colon + 1));
    }
}