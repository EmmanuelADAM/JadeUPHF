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

package jade.core.replication;

//#MIDP_EXCLUDE_FILE

import jade.core.*;

import java.util.Vector;


/**
 * The horizontal interface for the JADE kernel-level service managing
 * the main-container replication subsystem installed in the platform.
 *
 * @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
public interface MainReplicationSlice extends Service.Slice {

    // Constants for the names of the service vertical commands

    /**
     * The name of this service.
     */
    String NAME = "jade.core.replication.MainReplication";

    /**
     * The vertical command issued on the new master main container just after leadership acquisition
     */
    String LEADERSHIP_ACQUIRED = "Leadership-Acquired";

    // Constants for the names of horizontal commands associated to methods
    String H_GETLABEL = "1";
    String H_GETPLATFORMMANAGERADDRESS = "2";
    String H_ADDREPLICA = "3";
    String H_REMOVEREPLICA = "4";
    String H_FILLGADT = "5";
    String H_BORNAGENT = "6";
    String H_DEADAGENT = "7";
    String H_SUSPENDEDAGENT = "8";
    String H_RESUMEDAGENT = "9";
    String H_NEWMTP = "10";
    String H_DEADMTP = "11";
    String H_NEWTOOL = "12";
    String H_DEADTOOL = "13";
    String H_INVOKESERVICEMETHOD = "14";


    // NOTE that some horizontal command do not have a corresponding
    // method in the SliceProxy since they are always sent in 
    // broadcast and therefore the serve() method of the SliceProxy
    // is called directly.
    int getLabel() throws IMTPException;

    String getPlatformManagerAddress() throws IMTPException;

    void addReplica(String sliceName, String smAddr, int sliceIndex, NodeDescriptor dsc, Vector<ServiceDescriptor> services) throws IMTPException;

    void removeReplica(String smAddr, int sliceIndex) throws IMTPException;

    void fillGADT(AID[] agents, ContainerID[] containers) throws IMTPException;

    void suspendedAgent(AID name) throws IMTPException, NotFoundException;

    void newTool(AID tool) throws IMTPException;
}
