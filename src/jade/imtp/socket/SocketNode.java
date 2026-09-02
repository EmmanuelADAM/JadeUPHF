package jade.imtp.socket;

import jade.core.BaseNode;
import jade.core.HorizontalCommand;
import jade.core.IMTPException;
import jade.core.ServiceException;

import java.io.Serializable;

/**
 * The local representation of a JADE platform node under the socket IMTP.
 * <p>
 * Every node remembers the host:port of the JVM that created it. On that
 * JVM (once bound to a running {@link SocketServer} via
 * {@link #bindLocalServer}), calls are served in-process by the local*
 * methods below. On every other JVM -- which only ever sees this object
 * after it travelled there through plain Java serialization -- the
 * transient  server   link is null, so calls go out over the wire
 * instead. Unlike RMI, no stub-substitution trick is needed to tell the two
 * cases apart: it falls out for free from  server   being transient.
 *
 * @author Claude
 */
public class SocketNode extends BaseNode implements Serializable {

    private final String host;
    private final int port;

    private transient SocketServer server;
    private final transient Object terminationLock = new Object();
    private transient volatile boolean terminating = false;

    public SocketNode(String name, boolean hasPM, String host, int port) {
        super(name, hasPM);
        this.host = host;
        this.port = port;
    }

    void bindLocalServer(SocketServer server) {
        this.server = server;
    }

    public Object accept(HorizontalCommand cmd) throws IMTPException {
        if (server != null) {
            return localAccept(cmd);
        }
        return unwrap(SocketConnection.call(host, port, RpcProtocol.Target.NODE, "accept", cmd));
    }

    public boolean ping(boolean hang) throws IMTPException {
        if (server != null) {
            return localPing(hang);
        }
        return (Boolean) unwrap(SocketConnection.call(host, port, RpcProtocol.Target.NODE, "ping", hang));
    }

    public void exit() throws IMTPException {
        if (server != null) {
            localExit();
            return;
        }
        unwrap(SocketConnection.call(host, port, RpcProtocol.Target.NODE, "exit"));
    }

    public void interrupt() throws IMTPException {
        if (server != null) {
            localInterrupt();
            return;
        }
        unwrap(SocketConnection.call(host, port, RpcProtocol.Target.NODE, "interrupt"));
    }

    public void platformManagerDead(String deadPmAddress, String notifyingPmAddr) throws IMTPException {
        if (server != null) {
            localPlatformManagerDead(deadPmAddress, notifyingPmAddr);
            return;
        }
        unwrap(SocketConnection.call(host, port, RpcProtocol.Target.NODE, "platformManagerDead", deadPmAddress, notifyingPmAddr));
    }

    // -- Served only on the JVM that created this node; invoked directly by
    // SocketServer.serveNode() when a request for this node comes in over
    // the wire, and directly above when the call is already local. --

    Object localAccept(HorizontalCommand cmd) throws IMTPException {
        try {
            return serveHorizontalCommand(cmd);
        } catch (ServiceException se) {
            throw new IMTPException("Service Error", se);
        }
    }

    boolean localPing(boolean hang) {
        if (hang) {
            synchronized (terminationLock) {
                try {
                    terminationLock.wait();
                } catch (InterruptedException ie) {
                    // Fall through and report the current state.
                }
            }
        }
        return terminating;
    }

    void localExit() {
        terminating = true;
        synchronized (terminationLock) {
            terminationLock.notifyAll();
        }
    }

    void localInterrupt() {
        synchronized (terminationLock) {
            terminationLock.notifyAll();
        }
    }

    void localPlatformManagerDead(String deadPmAddress, String notifyingPmAddr) throws IMTPException {
        super.platformManagerDead(deadPmAddress, notifyingPmAddr);
    }

    private static Object unwrap(RpcProtocol.Response resp) throws IMTPException {
        if (resp.exception != null) {
            if (resp.exception instanceof IMTPException imtpe) {
                throw imtpe;
            }
            throw new IMTPException("Remote node error", resp.exception);
        }
        return resp.result;
    }
}