package jade.core.nodeMonitoring;

import jade.core.IMTPException;
import jade.core.Service;
import jade.core.ServiceException;

public interface UDPNodeMonitoringSlice extends Service.Slice {
    String H_ACTIVATEUDP = "H-ACTIVATEUDP";
    String H_DEACTIVATEUDP = "H-DEACTIVATEUDP";

    /*
     * Request a given node to start sending UDP packets
     */
    void activateUDP(String label, String host, int port, int pingDelay, long key) throws IMTPException, ServiceException;

    /*
     * Request a given node to stop sending UDP packets
     */
    void deactivateUDP(String label, long key) throws IMTPException;
}
