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
**************************************************************/

package jade.tools.applet;

import jade.core.AID;
import jade.domain.DFGUIAdapter;
import jade.domain.DFGUIManagement.DFAppletVocabulary;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FIPAManagementVocabulary;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLParser;
import jade.tools.dfgui.DFGUI;
import jade.util.Logger;

import java.applet.Applet;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is used by DFApplet in order to communicate with the DF agent
 * via a socket connection. The socket server is implemented by the agent
 * jade.tools.SocketProxyAgent.
 *
 * @author Fabio Bellifemine - CSELT - 25/8/1999
 * @version $Date: 2004-07-19 17:54:06 +0200 (lun, 19 lug 2004) $ $Revision: 5217 $
 * @see jade.tools.SocketProxyAgent.SocketProxyAgent
 */


public class DFAppletCommunicator implements DFGUIAdapter {

    private Applet a;
    private DataInputStream in;
    private PrintStream out;
    private final static int DEFAULT_PORT = 6789;
    private ACLParser parser;
    private DFGUI gui;
    private String address;
    private String hap;


    //default description of the df.
    private DFAgentDescription thisDF = null;

    //logging
    private final Logger logger = Logger.getMyLogger(this.getClass().getName());

    /**
     * Create a socket to communicate with a server on port 6789 of the
     * host that the applet's code is on. Create streams to use with the socket.
     * Finally, gets the value of the parameter <code>JADEAddress</code>
     * from the HTML file.
     */
    public DFAppletCommunicator(Applet applet) {
        try {

            a = applet;
            //retrive the HAP from the html file.
            hap = a.getParameter("HAP");
            if (logger.isLoggable(Logger.FINEST))
                logger.log(Logger.FINEST, "HAP:" + hap);

            Socket s = new Socket(a.getCodeBase().getHost(), DEFAULT_PORT);
            if (logger.isLoggable(Logger.CONFIG))
                logger.log(Logger.CONFIG, "DFAppletClient connected to local port " + s.getLocalPort() + " and remote port " + s.getPort());
            in = new DataInputStream(s.getInputStream());
            parser = new ACLParser(in);
            out = new PrintStream(s.getOutputStream(), true);


        } catch (IOException e) {
            e.printStackTrace();
            a.stop();
        }
    }

    /**
     * This method allows this class to call the method showStatusMsg implemented
     * by DFGUI
     */
    void setGUI(DFGUI g) {
        gui = g;
    }

    //return the gui for this applet
    DFGUI getGUI() {
        return gui;
    }

    /**
     * shows the message not authorized and does nothing.
     */
    public void doDelete() {
        gui.showStatusMsg("Operation not authorized");
    }

    /**
     * returns "df" that is the name of the  default DF.
     * In fact, so far this applet can be used only to interact
     * with the default DF.
     */
    public String getName() {
        return "df" + "@" + hap;
    }


    /**
     * According to the event generatated by the applet,
     * this method performes the needed actions.
     */

    public void postGuiEvent(GuiEvent event) {
        switch (event.getType()) {
            case DFGUIAdapter.EXIT, DFGUIAdapter.CLOSEGUI -> {
                gui.dispose();
                a.destroy();
            }
            case DFGUIAdapter.REFRESHAPPLET -> refreshDFGUI();
            case DFGUIAdapter.REGISTER -> RegisterNewAgent(event);
            case DFGUIAdapter.DEREGISTER -> DeregisterAgent(event);
            case DFGUIAdapter.SEARCH -> SearchAgents(event);
            case DFGUIAdapter.MODIFY -> Modify(event);
            case DFGUIAdapter.FEDERATE -> Federate(event);
        }
    }


    /**
     * Refresh the gui of the applet.
     * First of all makes a search for all agent registered with the df,
     * then updates the federate view, requesting the parents to the df.
     */

    public void refreshDFGUI() {
        //first: make a search on the df of all the agents registered.
        AID df = getDescriptionOfThisDF().getName();
        DFAgentDescription dfd = new DFAgentDescription();
        SearchConstraints sc = new SearchConstraints();
        try {

            FIPAAppletRequestProto arp = new FIPAAppletRequestProto(this, df, FIPAManagementVocabulary.SEARCH, dfd, sc);
            arp.doProto();
            Iterator<DFAgentDescription> result = arp.getSearchResult().iterator();
            ArrayList<AID> listOfAID = new ArrayList<>();
            ArrayList<AID> listOfChildren = new ArrayList<>();
            while (result.hasNext()) {
                DFAgentDescription next = result.next();
                listOfAID.add(next.getName());
                if (isADF(next))
                    listOfChildren.add(next.getName());
            }

            //second request the df the parent
            JADEAppletRequestProto getParent = new JADEAppletRequestProto(this, getDescriptionOfThisDF().getName(), DFAppletVocabulary.GETPARENTS, null, null);
            getParent.doProto();
            Iterator<DFAgentDescription> parents = getParent.getResult().iterator();

            ArrayList<AID> alparents2 = new ArrayList<>();
            while (parents.hasNext()) {
                DFAgentDescription dfAgentDescription = parents.next();
                alparents2.add(dfAgentDescription.getName());
            }
            Iterator<AID> parents2 = alparents2.iterator();

            gui.refresh(listOfAID.iterator(), parents2, listOfChildren.iterator());
        } catch (FIPAAppletRequestProto.NotYetReady | JADEAppletRequestProto.NotYetReady | FIPAException nyr) {
            nyr.printStackTrace();
        }


    }

    /**
     * Register an agent with a given df.
     */
    private void RegisterNewAgent(GuiEvent event) {

        AID df = (AID) event.getParameter(0);
        DFAgentDescription dfd = (DFAgentDescription) event.getParameter(1);

        if (df.getName().equalsIgnoreCase(thisDF.getName().getName()))
            try {
                //register an agent with this df.
                FIPAAppletRequestProto rf = new FIPAAppletRequestProto(this, df, FIPAManagementVocabulary.REGISTER, dfd, null);
                rf.doProto();
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        else
            //request the df to register an agent with another df.
            try {
                JADEAppletRequestProto requestBehav = new JADEAppletRequestProto(this, getDescriptionOfThisDF().getName(), DFAppletVocabulary.REGISTERWITH, dfd, df);
                requestBehav.doProto();
            } catch (FIPAException e) {
                e.printStackTrace();
            }
    }

    /**
     * Deregister an agent with a df.
     */
    private void DeregisterAgent(GuiEvent event) {
        AID df = (AID) event.getParameter(0);
        DFAgentDescription dfd = (DFAgentDescription) event.getParameter(1);
        if (df.getName().equalsIgnoreCase(thisDF.getName().getName()))
            try {
                FIPAAppletRequestProto rf = new FIPAAppletRequestProto(this, df, FIPAManagementVocabulary.DEREGISTER, dfd, null);
                rf.doProto();
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        else
            //deregister the df from a parent
            try {
                JADEAppletRequestProto rf = new JADEAppletRequestProto(this, getDescriptionOfThisDF().getName(), DFAppletVocabulary.DEREGISTERFROM, dfd, df);
                rf.doProto();
            } catch (FIPAException e) {
                e.printStackTrace();
            }

    }

    /**
     * Finds all the agent descriptors that match the given agent descriptor
     */
    private void SearchAgents(GuiEvent event) {

        AID df = (AID) event.getParameter(0);
        DFAgentDescription dfd = (DFAgentDescription) event.getParameter(1);
        SearchConstraints sc = (SearchConstraints) event.getParameter(2);
        if (df.getName().equalsIgnoreCase(thisDF.getName().getName()))
            try {
                FIPAAppletRequestProto rf = new FIPAAppletRequestProto(this, df, FIPAManagementVocabulary.SEARCH, dfd, sc);
                rf.doProto();
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        else
            try {
                JADEAppletRequestProto rf = new JADEAppletRequestProto(this, getDescriptionOfThisDF().getName(), DFAppletVocabulary.SEARCHON, dfd, df, sc);
                rf.doProto();
            } catch (FIPAException e) {
                e.printStackTrace();
            }
    }

    /**
     * Modifies the DFAgent description of an agent.
     */
    private void Modify(GuiEvent event) {
        AID df = (AID) event.getParameter(0);
        DFAgentDescription dfd = (DFAgentDescription) event.getParameter(1);

        if (df.equals(thisDF.getName()))
            try {
                FIPAAppletRequestProto rf = new FIPAAppletRequestProto(this, df, FIPAManagementVocabulary.MODIFY, dfd, null);
                rf.doProto();
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        else
            try {
                JADEAppletRequestProto rf = new JADEAppletRequestProto(this, thisDF.getName(), DFAppletVocabulary.MODIFYON, dfd, df);
                rf.doProto();
            } catch (FIPAException e) {
                e.printStackTrace();
            }

    }

    /**
     * This method requests the df to perform a federate action.
     */
    private void Federate(GuiEvent event) {
        AID parentDF = (AID) event.getParameter(0);
        if (parentDF.equals(thisDF.getName())) {
            gui.showStatusMsg("Self federation not allowed.");
        } else {
            DFAgentDescription dfd = (DFAgentDescription) event.getParameter(1);
            try {
                JADEAppletRequestProto rf = new JADEAppletRequestProto(this, getDescriptionOfThisDF().getName(), DFAppletVocabulary.FEDERATE, dfd, parentDF);
                rf.doProto();
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * This method requests the df the DFAgentDescription of a specific agent.
     *
     * @param name The AID of the agent.
     */
    public DFAgentDescription getDFAgentDsc(AID name) throws FIPAException {

        DFAgentDescription outDesc = null;

        AID df = getDescriptionOfThisDF().getName();
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(name);
        SearchConstraints sc = new SearchConstraints();

        try {
            FIPAAppletRequestProto arp = new FIPAAppletRequestProto(this, df, FIPAManagementVocabulary.SEARCH, dfd, sc);
            arp.doProto();
            Iterator<DFAgentDescription> result = arp.getSearchResult().iterator();
            if (result.hasNext())
                outDesc = result.next();
        } catch (FIPAAppletRequestProto.NotYetReady | FIPAException nyr) {
            nyr.printStackTrace();
        }

        return outDesc;
    }


    /*
    This method requests the df its default description.
    */
    public DFAgentDescription getDescriptionOfThisDF() {
        if (thisDF == null) {
            AID df = new AID(getName(), AID.ISGUID);

            System.out.println(df.getName());
            try {
                JADEAppletRequestProto rf = new JADEAppletRequestProto(this, df, DFAppletVocabulary.GETDESCRIPTION, null, null);
                rf.doProto();
            } catch (FIPAException e) {
                e.printStackTrace();
            }

        }

        return thisDF;
    }

    /*
    This method requests the df the DFAgentDescription used to federate with a parent df.
    @param df  The AID of the parent df.
    */
    public DFAgentDescription getDescriptionOfThisDF(AID df) {
        if (logger.isLoggable(Logger.FINEST))
            logger.log(Logger.FINEST, "CALLED METHOD: getDescriptionOfThisDF(aid) into DFAppletCommunicator");
        DFAgentDescription output = null;
        try {
            JADEAppletRequestProto rf = new JADEAppletRequestProto(this, getDescriptionOfThisDF().getName(), DFAppletVocabulary.GETDESCRIPTIONUSED, null, df);
            rf.doProto();
            ArrayList<DFAgentDescription> result = (ArrayList<DFAgentDescription>) rf.getResult();

            output = result.get(0);

        } catch (FIPAException | JADEAppletRequestProto.NotYetReady e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * Verifies if an agent is a DF.
     */
    boolean isADF(DFAgentDescription dfd) {
        try {
            ServiceDescription sd = dfd.getAllServices().next();
            return (sd.getType().equalsIgnoreCase("fipa-df"));
        } catch (Exception e) {
            return false;
        }
    }

    /*
    set the description of the df.
    */
    void setDescription(DFAgentDescription dfd) {
        thisDF = dfd;
    }

    //get the stream used to communicate with the dfproxy
    PrintStream getStream() {
        return out;
    }

    //get the parser used to communicate to the dfproxy
    ACLParser getParser() {
        return parser;
    }

}
