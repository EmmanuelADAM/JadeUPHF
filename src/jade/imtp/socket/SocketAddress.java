package jade.imtp.socket;

import jade.mtp.TransportAddress;

/**
 * A socket IMTP address: just a host and a port. Unlike RMIAddress there is
 * no registry-bound "file" name to resolve -- the host:port is directly the
 * listening socket of the target JVM.
 *
 * @author Claude
 */
public class SocketAddress implements TransportAddress {
    private final String host;
    private final String port;

    public SocketAddress() {
        this(null, null);
    }

    public SocketAddress(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public String getProto() {
        return "socket";
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getFile() {
        return null;
    }

    public String getAnchor() {
        return null;
    }
}