package jade.imtp.socket;

import jade.core.IMTPException;
import jade.core.Node;
import jade.core.NodeDescriptor;
import jade.core.PlatformManager;
import jade.core.Service;
import jade.core.ServiceDescriptor;
import jade.core.ServiceException;
import jade.security.JADESecurityException;

import java.util.Vector;

/**
 * A proxy implementing {@link PlatformManager} by forwarding every call to
 * the real PlatformManager over a socket IMTP connection. The socket
 * counterpart of RMI's  PlatformManagerAdapter  , minus the RMI
 * registry lookup: the target host:port is already known from the address
 * this proxy was built from.
 *
 * @author Claude
 */
class SocketPlatformManagerProxy implements PlatformManager {
    private final String host;
    private final int port;
    private final String localAddress;

    SocketPlatformManagerProxy(String host, int port, String localAddress) {
        this.host = host;
        this.port = port;
        this.localAddress = localAddress;
    }

    public String getPlatformName() throws IMTPException {
        return (String) unwrap(call("getPlatformName"));
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String addr) {
        // Never called on a remote proxy: the real PlatformManager sets its own address.
    }

    public String addNode(NodeDescriptor dsc, Vector<ServiceDescriptor> nodeServices, boolean propagated) throws IMTPException, ServiceException, JADESecurityException {
        RpcProtocol.Response resp = call("addNode", dsc, nodeServices, propagated);
        if (resp.exception instanceof ServiceException se) throw se;
        if (resp.exception instanceof JADESecurityException jse) throw jse;
        return (String) unwrap(resp);
    }

    public void removeNode(NodeDescriptor dsc, boolean propagated) throws IMTPException, ServiceException {
        RpcProtocol.Response resp = call("removeNode", dsc, propagated);
        if (resp.exception instanceof ServiceException se) throw se;
        unwrap(resp);
    }

    public void addSlice(ServiceDescriptor service, NodeDescriptor dsc, boolean propagated) throws IMTPException, ServiceException {
        RpcProtocol.Response resp = call("addSlice", service, dsc, propagated);
        if (resp.exception instanceof ServiceException se) throw se;
        unwrap(resp);
    }

    public void removeSlice(String serviceKey, String sliceKey, boolean propagated) throws IMTPException, ServiceException {
        RpcProtocol.Response resp = call("removeSlice", serviceKey, sliceKey, propagated);
        if (resp.exception instanceof ServiceException se) throw se;
        unwrap(resp);
    }

    public void addReplica(String newAddr, boolean propagated) throws IMTPException, ServiceException {
        RpcProtocol.Response resp = call("addReplica", newAddr, propagated);
        if (resp.exception instanceof ServiceException se) throw se;
        unwrap(resp);
    }

    public void removeReplica(String address, boolean propagated) throws IMTPException, ServiceException {
        RpcProtocol.Response resp = call("removeReplica", address, propagated);
        if (resp.exception instanceof ServiceException se) throw se;
        unwrap(resp);
    }

    @SuppressWarnings("unchecked")
    public Service.Slice findSlice(String serviceKey, String sliceKey) throws IMTPException, ServiceException {
        RpcProtocol.Response resp = call("findSlice", serviceKey, sliceKey);
        if (resp.exception instanceof ServiceException se) throw se;
        return (Service.Slice) unwrap(resp);
    }

    @SuppressWarnings("unchecked")
    public Vector<Service.Slice> findAllSlices(String serviceKey) throws IMTPException, ServiceException {
        RpcProtocol.Response resp = call("findAllSlices", serviceKey);
        if (resp.exception instanceof ServiceException se) throw se;
        return (Vector<Service.Slice>) unwrap(resp);
    }

    public void adopt(Node n, Node[] children) throws IMTPException {
        unwrap(call("adopt", n, children));
    }

    public void ping() throws IMTPException {
        unwrap(call("ping"));
    }

    private RpcProtocol.Response call(String method, Object... args) throws IMTPException {
        return SocketConnection.call(host, port, RpcProtocol.Target.PLATFORM_MANAGER, method, args);
    }

    private static Object unwrap(RpcProtocol.Response resp) throws IMTPException {
        if (resp.exception != null) {
            throw new IMTPException("Remote PlatformManager error", resp.exception);
        }
        return resp.result;
    }

    public String toString() {
        return "SocketPlatformManagerProxy: local-address=" + localAddress;
    }
}