package jade.imtp.socket;

import jade.core.HorizontalCommand;
import jade.core.IMTPException;
import jade.core.Node;
import jade.core.NodeDescriptor;
import jade.core.PlatformManager;
import jade.core.ServiceDescriptor;
import jade.util.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * The single listening socket a JVM opens under the socket IMTP. It serves
 * calls targeting this JVM's local Node and -- when this JVM hosts the
 * platform's PlatformManager -- calls targeting that too, so a JVM never
 * needs more than one listening port (RMI needed up to three: the registry,
 * the Node, and the Service Manager). Each accepted connection is handled
 * on its own virtual thread, so holding thousands of them open (including
 * long-lived blocking ping(true) connections used for failure detection)
 * costs negligible memory.
 *
 * @author Claude
 */
final class SocketServer {

    private final Logger myLogger = Logger.getMyLogger(getClass().getName());
    private final ServerSocket serverSocket;
    private final SocketNode node;
    private volatile PlatformManager platformManager;
    private volatile boolean running = true;

    SocketServer(String host, int port, SocketNode node) throws IOException {
        this.node = node;
        serverSocket = new ServerSocket();
        InetAddress addr = (host != null) ? InetAddress.getByName(host) : null;
        serverSocket.bind(new InetSocketAddress(addr, port));
        Thread.ofVirtual().name("jade-socket-imtp-acceptor").start(this::acceptLoop);
    }

    int getLocalPort() {
        return serverSocket.getLocalPort();
    }

    void exportPlatformManager(PlatformManager pm) {
        platformManager = pm;
    }

    void unexportPlatformManager() {
        platformManager = null;
    }

    void shutDown() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException ignored) {
            // Already closed or closing -- nothing to do.
        }
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                Thread.ofVirtual().name("jade-socket-imtp-conn").start(() -> handle(socket));
            } catch (IOException ioe) {
                if (running && myLogger.isLoggable(Logger.WARNING)) {
                    myLogger.log(Logger.WARNING, "Socket IMTP accept loop error", ioe);
                }
            }
        }
    }

    private void handle(Socket socket) {
        try (socket;
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            RpcProtocol.Request req = (RpcProtocol.Request) in.readObject();
            RpcProtocol.Response resp = dispatch(req);
            out.writeObject(resp);
            out.flush();
        } catch (IOException | ClassNotFoundException e) {
            if (myLogger.isLoggable(Logger.FINE)) {
                myLogger.log(Logger.FINE, "Socket IMTP connection dropped", e);
            }
        }
    }

    private RpcProtocol.Response dispatch(RpcProtocol.Request req) {
        try {
            Object result = switch (req.target) {
                case NODE -> serveNode(req.method, req.args);
                case PLATFORM_MANAGER -> servePlatformManager(req.method, req.args);
            };
            return new RpcProtocol.Response(result, null);
        } catch (Throwable t) {
            return new RpcProtocol.Response(null, t);
        }
    }

    private Object serveNode(String method, Object[] args) throws Exception {
        return switch (method) {
            case "accept" -> node.localAccept((HorizontalCommand) args[0]);
            case "ping" -> node.localPing((Boolean) args[0]);
            case "exit" -> {
                node.localExit();
                yield null;
            }
            case "interrupt" -> {
                node.localInterrupt();
                yield null;
            }
            case "platformManagerDead" -> {
                node.localPlatformManagerDead((String) args[0], (String) args[1]);
                yield null;
            }
            default -> throw new IMTPException("Unknown Node method: " + method);
        };
    }

    @SuppressWarnings("unchecked")
    private Object servePlatformManager(String method, Object[] args) throws Exception {
        PlatformManager pm = platformManager;
        if (pm == null) {
            throw new IMTPException("No PlatformManager exported on this node");
        }
        return switch (method) {
            case "getPlatformName" -> pm.getPlatformName();
            case "addNode" -> pm.addNode((NodeDescriptor) args[0], (Vector<ServiceDescriptor>) args[1], (Boolean) args[2]);
            case "removeNode" -> {
                pm.removeNode((NodeDescriptor) args[0], (Boolean) args[1]);
                yield null;
            }
            case "addSlice" -> {
                pm.addSlice((ServiceDescriptor) args[0], (NodeDescriptor) args[1], (Boolean) args[2]);
                yield null;
            }
            case "removeSlice" -> {
                pm.removeSlice((String) args[0], (String) args[1], (Boolean) args[2]);
                yield null;
            }
            case "addReplica" -> {
                pm.addReplica((String) args[0], (Boolean) args[1]);
                yield null;
            }
            case "removeReplica" -> {
                pm.removeReplica((String) args[0], (Boolean) args[1]);
                yield null;
            }
            case "findSlice" -> pm.findSlice((String) args[0], (String) args[1]);
            case "findAllSlices" -> pm.findAllSlices((String) args[0]);
            case "adopt" -> {
                pm.adopt((Node) args[0], (Node[]) args[1]);
                yield null;
            }
            case "ping" -> {
                pm.ping();
                yield null;
            }
            default -> throw new IMTPException("Unknown PlatformManager method: " + method);
        };
    }
}