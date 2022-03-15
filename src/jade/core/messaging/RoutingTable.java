/*
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

import jade.core.AID;
import jade.core.CaseInsensitiveString;
import jade.core.IMTPException;
import jade.core.Runtime;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.Property;
import jade.mtp.MTP;
import jade.mtp.MTPDescriptor;
import jade.mtp.MTPException;
import jade.mtp.OutChannel;

import java.util.*;

class RoutingTable {

    private static final boolean LOCAL = true;
    private static final boolean REMOTE = false;
    private static final int EXPECTED_PLATFORMADDRESSES_SIZE = 2;
    private final Map<CaseInsensitiveString, MTPInfo> inPorts = new HashMap<>(2);
    private final Map<CaseInsensitiveString, OutPortList> outPorts = new HashMap<>(2);
    private final List<MTPDescriptor> remoteMTPs = new ArrayList<>();
    private final List<String> platformAddresses = new ArrayList<>(EXPECTED_PLATFORMADDRESSES_SIZE);
    private String platformInfo = null;
    public RoutingTable(boolean attachPlatformInfo) {
        if (attachPlatformInfo) {
            platformInfo = Runtime.getVersionInfo() + " (" + System.getProperty("java.version") + ", " + System.getProperty("os.name") + " " + System.getProperty("os.version") + ")";
        }
    }

    /**
     * Adds a new locally installed MTP for the URL named
     * <code>url</code>.
     */
    public synchronized void addLocalMTP(String url, MTP proto, MTPDescriptor dsc) {
        CaseInsensitiveString urlTmp = new CaseInsensitiveString(url);
        // A local MTP can receive messages
        inPorts.put(urlTmp, new MTPInfo(proto, dsc));

        // A local MTP can also send messages, over all supported protocols
        OutPort out = new OutViaMTP(proto, platformInfo);
        String[] protoNames = proto.getSupportedProtocols();
        for (String protoName : protoNames) {
            addOutPort(protoName, out, LOCAL);
        }

        // The new MTP is a valid address for the platform
        platformAddresses.add(url);
    }

    /**
     * Removes a locally installed MTP for the URL named
     * <code>url</code>.
     */
    public synchronized MTPInfo removeLocalMTP(String url) {
        // url = url.toLowerCase();
        CaseInsensitiveString urlTmp = new CaseInsensitiveString(url);
        // A local MTP appears both in the input and output port tables
        MTPInfo info = inPorts.remove(urlTmp);
        if (info != null) {
            MTP proto = info.getMTP();
            // Remove all outgoing ports associated with this MTP
            String[] protoNames = proto.getSupportedProtocols();
            for (String protoName : protoNames) {
                OutPort out = new OutViaMTP(proto, platformInfo);
                removeOutPort(protoName, out);
            }
        }

        // The MTP address is not a platform address anymore
        platformAddresses.remove(url);

		/*
		 java.util.Iterator it = outPorts.keySet().iterator();
		 while(it.hasNext()) {
		 String name = (String)it.next();
		 OutPortList l = (OutPortList)outPorts.get(name);
		 System.out.println("<" + name + "> ==> " + l.size());
		 }
		 */

        return info;
    }

    public synchronized boolean addRemoteMTP(MTPDescriptor mtp, String sliceName, MessagingSlice where) {
        if (!remoteMTPs.contains(mtp)) {
            remoteMTPs.add(mtp);
            // A remote MTP can be used only for outgoing messages, through an
            // OutPort that routes messages through a container
            OutPort out = new OutViaSlice(sliceName, where);
            String[] protoNames = mtp.getSupportedProtocols();
            for (String protoName : protoNames) {
                addOutPort(protoName, out, REMOTE);
            }

            // Remote MTPs are valid platform addresses
            String[] mtpAddrs = mtp.getAddresses();
            platformAddresses.add(mtpAddrs[0]);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the MTP for the URL named <code>name</code>.
     */
    public synchronized void removeRemoteMTP(MTPDescriptor mtp, String sliceName, MessagingSlice where) {
        remoteMTPs.remove(mtp);

        OutPort ch = new OutViaSlice(sliceName, where);
        String[] protoNames = mtp.getSupportedProtocols();
        for (String protoName : protoNames) {
            removeOutPort(protoName, ch);
        }

        // Remote MTPs are valid platform addresses
        String[] mtpAddrs = mtp.getAddresses();
        platformAddresses.remove(mtpAddrs[0]);
    }

    /**
     * Retrieves an outgoing channel object suitable for
     * reaching the address <code>url</code>.
     */
    public synchronized OutPort lookup(String url) {
        //url = url.toLowerCase();
        String proto = extractProto(url);
        CaseInsensitiveString protoTmp = new CaseInsensitiveString(proto);
        OutPortList l = outPorts.get(protoTmp);
        if (l != null) {
            return l.get();
        } else {
            return null;
        }
    }

    public synchronized Iterator<String> getAddresses() {
        return platformAddresses.iterator();
    }

    public synchronized Iterator<MTPInfo> getLocalMTPs() {
        return inPorts.values().iterator();
    }

    private void addOutPort(String proto, OutPort port, boolean location) {
        //proto = proto.toLowerCase();
        CaseInsensitiveString protoTmp = new CaseInsensitiveString(proto);
        OutPortList l = outPorts.get(protoTmp);
        if (l != null)
            l.add(port, location);
        else {
            l = new OutPortList();
            l.add(port, location);
            outPorts.put(protoTmp, l);
        }
    }

    private void removeOutPort(String proto, OutPort port) {
        //proto = proto.toLowerCase();
        CaseInsensitiveString protoTmp = new CaseInsensitiveString(proto);
        OutPortList l = outPorts.get(protoTmp);
        if (l != null) {
            l.remove(port);
        }
    }

    private String extractProto(String address) {
        int colonPos = address.indexOf(':');
        if (colonPos == -1)
            return null;
        return address.substring(0, colonPos);
    }

    public interface OutPort {
        void route(Envelope env, byte[] payload, AID receiver, String address) throws MTPException;
    }

    // This class wraps an MTP installed on a remote container, using
    // RMI to forward the deliver() operation
    private static class OutViaSlice implements OutPort {

        private final String sliceName;
        private final MessagingSlice slice;

        public OutViaSlice(String sn, MessagingSlice ms) {
            sliceName = sn;
            slice = ms;
        }

        public void route(Envelope env, byte[] payload, AID receiver, String address) throws MTPException {
            try {
                slice.routeOut(env, payload, receiver, address);
            } catch (IMTPException imtpe) {
                throw new MTPException("Container unreachable during routing", imtpe);
            }
        }

        public boolean equals(Object o) {
            try {
                OutViaSlice rhs = (OutViaSlice) o;
                return sliceName.equals(rhs.sliceName);
            } catch (ClassCastException cce) {
                return false;
            }
        }

    } // End of OutViaContainer class

    // This class wraps an MTP installed locally, using the ACC to encode
    // the message into an MTP payload.
    private static class OutViaMTP implements OutPort {

        private final OutChannel myChannel;
        private final String platformInfo;

        public OutViaMTP(OutChannel proto, String platformInfo) {
            myChannel = proto;
            this.platformInfo = platformInfo;
        }

        public void route(Envelope env, byte[] payload, AID receiver, String address) throws MTPException {
            if (platformInfo != null) {
                env.addProperties(new Property(MessagingService.PLATFORM_IDENTIFIER, platformInfo));
                env.addProperties(new Property(MessagingService.MTP_IDENTIFIER, myChannel.getClass().getName()));
            }
            myChannel.deliver(address, env, payload);
        }

        public boolean equals(Object o) {
            try {
                OutViaMTP rhs = (OutViaMTP) o;
                return myChannel.equals(rhs.myChannel);
            } catch (ClassCastException cce) {
                return false;
            }
        }
    }

    private static class OutPortList {

        private final List<OutPort> local = new ArrayList<>(1);
        private final List<OutPort> remote = new ArrayList<>(1);

        public void add(OutPort port, boolean location) {
            if (location == LOCAL) {
                local.add(port);
            } else {
                remote.add(port);
            }
        }

        public void remove(OutPort port) {
            local.remove(port);
            remote.remove(port);
        }

        public OutPort get() {
            // Look first in the local list
            if (!local.isEmpty())
                return local.get(0);
                // Then look in the remote list
            else if (!remote.isEmpty())
                return remote.get(0);
            return null;
        }

        public boolean isEmpty() {
            return local.isEmpty() && remote.isEmpty();
        }

        public String size() {
            return "[ local: " + local.size() + "  remote: " + remote.size() + " ]";
        }
    } // End of OutPortList class

    /**
     * Inner class MTPInfo
     * This class just provides the association between a local MTP and its descriptor
     */
    static class MTPInfo {
        private final MTP mtp;
        private final MTPDescriptor dsc;

        public MTPInfo(MTP mtp, MTPDescriptor dsc) {
            this.mtp = mtp;
            this.dsc = dsc;
        }

        public MTP getMTP() {
            return mtp;
        }

        public MTPDescriptor getDescriptor() {
            return dsc;
        }
    } // END of inner class MTPInfo
}

