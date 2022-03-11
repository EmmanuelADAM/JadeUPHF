package jade.imtp.leap.http;

import jade.imtp.leap.SSLHelper;
import jade.mtp.TransportAddress;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Eduard Drenth: Logica, 30-sep-2009
 */
class HTTPSClientConnection extends HTTPClientConnection {

    {
        System.setProperty("https.cipherSuites", SSLHelper.supportedKeys.get(0));
    }

    public HTTPSClientConnection(TransportAddress ta) {
        super(ta);
    }

    protected String getProtocol() {
        return "https://";
    }

    protected HttpURLConnection open(String url) throws IOException {

        return (HttpsURLConnection) new URL(url).openConnection();
    }

}
