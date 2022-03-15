/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jade.imtp.leap.nio;

import jade.imtp.leap.ICPException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a nio based server connection for which ssl can be configured. A {@link SSLEngine} is used
 * for dealing with handshaking and encrypting/decrypting application data. The superclass does the actual
 * (also ),
 * and {@link #writeToChannel(java.nio.ByteBuffer) writing} from the SocketChannel and handles the application data.
 *
 * @author eduard
 * @see SSLContext
 */
public class NIOJICPSConnection extends NIOJICPConnection {

    private static final Logger log = Logger.getLogger(NIOJICPSConnection.class.getName());
    private SSLEngineHelper helper = null;

    public NIOJICPSConnection() {
    }

    /**
     * Initializes this connection by setting a {@link SSLEngineHelper } and calling the super.
     *
     * @param channel the Selection key provided by the {@link Selector}.
     * @see SSLEngineHelper
     */
    @Override
    void init(SocketChannel channel) throws ICPException {
        super.init(channel);
        if (log.isLoggable(Level.FINE)) {
            log.fine("initialize ssl tooling");
        }
        helper = new SSLEngineHelper(getRemoteHost(), channel.socket().getPort(), this);
        addBufferTransformer(helper);
    }

    /**
     * first try to send ssl close packet, then close channel
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {

        try {
            helper.close();
        } catch (IOException ex) {
        } catch (Exception e) {
            log.log(Level.WARNING, "Unexpected error closing SSLHelper.", e);
        }

        super.close();
    }

    @Override
    public String toString() {
        return super.toString() + "-CH=" + getChannel() + "-SSLEH=" + helper;
    }
}
