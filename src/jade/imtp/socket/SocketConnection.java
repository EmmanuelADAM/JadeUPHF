package jade.imtp.socket;

import jade.core.IMTPException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Opens one short-lived TCP connection per remote call: connect, write the
 * request, read the response, close. No connection pooling and no RMI-style
 * persistent JRMP connections to manage -- a plain blocking call is cheap
 * enough since every caller and every server-side handler runs on a virtual
 * thread. The one call that legitimately keeps its connection open for a
 * long time is a blocking Node.ping(true), used for failure detection; that
 * is just as cheap on a virtual thread.
 *
 * @author Claude
 */
final class SocketConnection {

    private SocketConnection() {
    }

    static RpcProtocol.Response call(String host, int port, RpcProtocol.Target target, String method, Object... args) throws IMTPException {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(new RpcProtocol.Request(target, method, args));
            out.flush();
            return (RpcProtocol.Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IMTPException("Socket IMTP communication error calling " + method + " on " + host + ":" + port, e);
        }
    }
}