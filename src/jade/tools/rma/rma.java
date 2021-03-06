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

package jade.tools.rma;

import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.AgentContainer;
import jade.core.ContainerID;
import jade.core.ServiceException;
import jade.core.behaviours.SenderBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.*;
import jade.domain.introspection.*;
import jade.domain.mobility.CloneAction;
import jade.domain.mobility.MobileAgentDescription;
import jade.domain.mobility.MobilityOntology;
import jade.domain.mobility.MoveAction;
import jade.domain.persistence.*;
import jade.gui.AgentTreeModel;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;
import jade.security.JADEPrincipal;
import jade.tools.ToolAgent;
import jade.util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;


/**
 * <em>Remote Management Agent</em> agent. This class implements
 * <b>JADE</b> <em>RMA</em> agent. <b>JADE</b> applications cannot use
 * this class directly, but interact with it through <em>ACL</em>
 * message passing. Besides, this agent has a <em>GUI</em> through
 * which <b>JADE</b> Agent Platform can be administered.
 *
 * @author Giovanni Rimassa - Universita' di Parma
 * @version $Date: 2013-01-07 13:03:08 +0100 (lun, 07 gen 2013) $ $Revision: 6621 $
 */
public class rma extends ToolAgent {

    private final SequentialBehaviour AMSSubscribe = new SequentialBehaviour();
    private APDescription myPlatformProfile;
    private transient MainWindow myGUI;
    private String myContainerName;

    /**
     * This method starts the <em>RMA</em> behaviours to allow the agent
     * to carry on its duties within <em><b>JADE</b></em> agent platform.
     */
    protected void toolSetup() {
        logger = Logger.getMyLogger(getName());

        // Register the supported ontologies
        getContentManager().registerOntology(MobilityOntology.getInstance());
        getContentManager().registerOntology(PersistenceOntology.getInstance());

        // Send 'subscribe' message to the AMS
        AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, getSubscribe()));

        // Handle incoming 'inform' messages
        AMSSubscribe.addSubBehaviour(new RMAAMSListenerBehaviour());

        // Schedule Behaviour for execution
        addBehaviour(AMSSubscribe);

        // Show Graphical User Interface
        myGUI = new MainWindow(this);
        myGUI.ShowCorrect();
    }

    /**
     * Cleanup during agent shutdown. This method cleans things up when
     * <em>RMA</em> agent is destroyed, disconnecting from <em>AMS</em>
     * agent and closing down the platform administration <em>GUI</em>.
     */
    protected void toolTakeDown() {
        send(getCancel());
        if (myGUI != null) {
            // The following call was removed as it causes a threading
            // deadlock with join. Its also not needed as the async
            // dispose will do it.
            // myGUI.setVisible(false);
            myGUI.disposeAsync();
        }
    }

    protected void beforeMove() {
        super.beforeMove();

        myGUI.disposeAsync();
        send(getCancel());
    }

    protected void afterMove() {
        super.afterMove();

        getContentManager().registerOntology(MobilityOntology.getInstance());
        getContentManager().registerOntology(PersistenceOntology.getInstance());

        myGUI = new MainWindow(this);
        myGUI.ShowCorrect();

        // Make the AMS send back the whole container list
        send(getSubscribe());

    }

    protected void afterClone() {
        super.afterClone();

        getContentManager().registerOntology(MobilityOntology.getInstance());
        getContentManager().registerOntology(PersistenceOntology.getInstance());

        // Add yourself to the RMA list
        ACLMessage AMSSubscription = getSubscribe();
        send(AMSSubscription);
        myGUI = new MainWindow(this);
        myGUI.ShowCorrect();
    }

    public void afterLoad() {
        super.afterLoad();

        getContentManager().registerOntology(MobilityOntology.getInstance());
        getContentManager().registerOntology(PersistenceOntology.getInstance());

        // Add yourself to the RMA list
        ACLMessage AMSSubscription = getSubscribe();
        send(AMSSubscription);
        myGUI = new MainWindow(this);
        myGUI.ShowCorrect();
    }

    public void beforeReload() {
        super.beforeReload();

        myGUI.disposeAsync();
        send(getCancel());
    }

    public void afterReload() {
        super.afterReload();

        getContentManager().registerOntology(MobilityOntology.getInstance());
        getContentManager().registerOntology(PersistenceOntology.getInstance());

        myGUI = new MainWindow(this);
        myGUI.ShowCorrect();

        // Make the AMS send back the whole container list
        send(getSubscribe());
    }

    public void beforeFreeze() {
        super.beforeFreeze();

        myGUI.disposeAsync();
        send(getCancel());
    }

    public void afterThaw() {
        super.afterThaw();

        getContentManager().registerOntology(MobilityOntology.getInstance());
        getContentManager().registerOntology(PersistenceOntology.getInstance());

        myGUI = new MainWindow(this);
        myGUI.ShowCorrect();

        // Make the AMS send back the whole container list
        send(getSubscribe());
    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public AgentTreeModel getModel() {
        return myGUI.getModel();
    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void newAgent(String agentName, String className, Object[] arg, String containerName) {
        newAgent(agentName, className, arg, null, containerName);
    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void newAgent(String agentName, String className, Object[] arg, String ownerName, String containerName) {

        CreateAgent ca = new CreateAgent();

        if (containerName.equals(""))
            containerName = AgentContainer.MAIN_CONTAINER_NAME;

        // fill the create action with the intended agent owner
        JADEPrincipal intendedOwner;
        jade.security.Credentials initialCredentials = null;

        if ((ownerName == null) || (ownerName.trim().length() == 0)) {
            // it is requested the creation of an agent
            // with the same owner of the RMA
            try {
                JADEPrincipal rmaOwner = null;
                jade.security.Credentials rmaCredentials = null;
                jade.security.CredentialsHelper ch = (jade.security.CredentialsHelper) getHelper("jade.core.security.Security");
                // get RMA's owner
                if (ch != null) {
                    rmaCredentials = ch.getCredentials();
                }
                if (rmaCredentials != null) {
                    rmaOwner = rmaCredentials.getOwner();
                }
                intendedOwner = rmaOwner;
            } catch (ServiceException se) { // Security service not present. Owner is null.
                intendedOwner = null;
                initialCredentials = null;
            }

        } else {
            // it is requested the creation of an agent
            // with a specified owner name
            try {
                Class<?> c = Class.forName("jade.security.impl.JADEPrincipalImpl");
                intendedOwner = (JADEPrincipal) c.getDeclaredConstructor().newInstance();
                java.lang.reflect.Method setName = c.getDeclaredMethod("setName", String.class);
                setName.invoke(intendedOwner, ownerName);
            } catch (Exception e) {
                //e.printStackTrace();
                // Security service not present. Owner is null.
                intendedOwner = null;
                initialCredentials = null;
            }
        }

        ca.setOwner(intendedOwner);
        ca.setInitialCredentials(initialCredentials);

        ca.setAgentName(agentName);
        ca.setClassName(className);
        ca.setContainer(new ContainerID(containerName, null));
        if (arg != null) {
            for (Object o : arg) {
                ca.addArguments(o);
            }
        }

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(ca);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(JADEManagementOntology.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("CreateAgent", requestMsg));
        } catch (Exception fe) {
            fe.printStackTrace();
        }

    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void suspendAgent(AID name) {
        AMSAgentDescription amsd = new AMSAgentDescription();
        amsd.setName(name);
        amsd.setState(AMSAgentDescription.SUSPENDED);
        Modify m = new Modify();
        m.setDescription(amsd);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(m);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(FIPAManagementOntology.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("SuspendAgent", requestMsg));
        } catch (Exception fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void suspendContainer(String name) {
        // FIXME: Not implemented
    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void resumeAgent(AID name) {
        AMSAgentDescription amsd = new AMSAgentDescription();
        amsd.setName(name);
        amsd.setState(AMSAgentDescription.ACTIVE);
        Modify m = new Modify();
        m.setDescription(amsd);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(m);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(FIPAManagementOntology.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("ResumeAgent", requestMsg));
        } catch (Exception fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void changeAgentOwnership(AID name, String ownership) {
        AMSAgentDescription amsd = new AMSAgentDescription();
        amsd.setName(name);
        amsd.setState(AMSAgentDescription.ACTIVE);//SUSPENDED);
        amsd.setOwnership(ownership);
        Modify m = new Modify();
        m.setDescription(amsd);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(m);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(FIPAManagementOntology.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("ChangeAgentOwnership", requestMsg));
        } catch (Exception fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void resumeContainer(String name) {
        // FIXME: Not implemented
    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void killAgent(AID name) {

        KillAgent ka = new KillAgent();

        ka.setAgent(name);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(ka);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(JADEManagementOntology.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("KillAgent", requestMsg));
        } catch (Exception fe) {
            fe.printStackTrace();
        }

    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void saveContainer(String name, String repository) {
        SaveContainer saveAct = new SaveContainer();
        saveAct.setContainer(new ContainerID(name, null));
        saveAct.setRepository(repository);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(saveAct);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(PersistenceVocabulary.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("SaveContainer", requestMsg));
        } catch (Exception fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void loadContainer(String name, String repository) {
        LoadContainer saveAct = new LoadContainer();
        saveAct.setContainer(new ContainerID(name, null));
        saveAct.setRepository(repository);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(saveAct);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(PersistenceVocabulary.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("LoadContainer", requestMsg));
        } catch (Exception fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void killContainer(String name) {

        KillContainer kc = new KillContainer();

        kc.setContainer(new ContainerID(name, null));

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(kc);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(JADEManagementOntology.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("KillContainer", requestMsg));
        } catch (Exception fe) {
            fe.printStackTrace();
        }

    }

    /**
     * Callback method for platform management
     */
    public void moveAgent(AID name, String container) {
        MoveAction moveAct = new MoveAction();
        MobileAgentDescription desc = new MobileAgentDescription();
        desc.setName(name);
        ContainerID dest = new ContainerID(container, null);

        desc.setDestination(dest);
        moveAct.setMobileAgentDescription(desc);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(moveAct);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(MobilityOntology.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("MoveAgent", requestMsg));

        } catch (Exception fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Callback method for platform management
     */
    public void cloneAgent(AID name, String newName, String container) {
        CloneAction cloneAct = new CloneAction();
        MobileAgentDescription desc = new MobileAgentDescription();
        desc.setName(name);
        ContainerID dest = new ContainerID(container, null);
        desc.setDestination(dest);
        cloneAct.setMobileAgentDescription(desc);
        cloneAct.setNewName(newName);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(cloneAct);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(MobilityOntology.NAME);
            getContentManager().fillContent(requestMsg, a);

            addBehaviour(new AMSClientBehaviour("CloneAgent", requestMsg));

        } catch (Exception fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Callback method for platform management
     */
    public void saveAgent(AID name, String repository) {
        SaveAgent saveAct = new SaveAgent();
        saveAct.setAgent(name);
        saveAct.setRepository(repository);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(saveAct);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(PersistenceVocabulary.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("SaveAgent", requestMsg));
        } catch (Exception fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Callback method for platform management
     */
    public void loadAgent(AID name, String repository, String container) {
        LoadAgent loadAct = new LoadAgent();
        loadAct.setAgent(name);
        loadAct.setRepository(repository);
        ContainerID where = new ContainerID(container, null);
        loadAct.setWhere(where);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(loadAct);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(PersistenceVocabulary.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("LoadAgent", requestMsg));

        } catch (Exception fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Callback method for platform management
     */
    public void freezeAgent(AID name, String repository) {
        FreezeAgent freezeAct = new FreezeAgent();
        freezeAct.setAgent(name);
        freezeAct.setRepository(repository);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(freezeAct);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(PersistenceVocabulary.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("FreezeAgent", requestMsg));
        } catch (Exception fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Callback method for platform management
     */
    public void thawAgent(AID name, String repository, ContainerID newContainer) {
        ThawAgent thawAct = new ThawAgent();
        thawAct.setAgent(name);
        thawAct.setRepository(repository);
        thawAct.setNewContainer(newContainer);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(thawAct);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(PersistenceVocabulary.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("ThawAgent", requestMsg));
        } catch (Exception fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void exit() {
        if (myGUI.showExitDialog("Exit this container"))
            killContainer(myContainerName);
    }

    /**
     * Callback method for platform management <em>GUI</em>.
     */
    public void shutDownPlatform() {
        if (myGUI.showExitDialog("Shut down the platform")) {

            ShutdownPlatform sp = new ShutdownPlatform();
            try {
                Action a = new Action();
                a.setActor(getAMS());
                a.setAction(sp);

                ACLMessage requestMsg = getRequest();
                requestMsg.setOntology(JADEManagementOntology.NAME);
                getContentManager().fillContent(requestMsg, a);
                addBehaviour(new AMSClientBehaviour("ShutdownPlatform", requestMsg));
            } catch (Exception fe) {
                fe.printStackTrace();
            }
        }

    }

    public void installMTP(String containerName) {
        InstallMTP imtp = new InstallMTP();
        imtp.setContainer(new ContainerID(containerName, null));
        if (myGUI.showInstallMTPDialog(imtp)) {
            try {
                Action a = new Action();
                a.setActor(getAMS());
                a.setAction(imtp);

                ACLMessage requestMsg = getRequest();
                requestMsg.setOntology(JADEManagementOntology.NAME);
                getContentManager().fillContent(requestMsg, a);
                addBehaviour(new AMSClientBehaviour("InstallMTP", requestMsg));
            } catch (Exception fe) {
                fe.printStackTrace();
            }

        }
    }

    public void uninstallMTP(String containerName) {
        UninstallMTP umtp = new UninstallMTP();
        umtp.setContainer(new ContainerID(containerName, null));
        if (myGUI.showUninstallMTPDialog(umtp)) {
            uninstallMTP(umtp.getContainer().getName(), umtp.getAddress());
        }
    }

    public void uninstallMTP(String containerName, String address) {
        UninstallMTP umtp = new UninstallMTP();
        umtp.setContainer(new ContainerID(containerName, null));
        umtp.setAddress(address);
        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(umtp);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(JADEManagementOntology.NAME);
            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new AMSClientBehaviour("UninstallMTP", requestMsg));
        } catch (Exception fe) {
            fe.printStackTrace();
        }
    }

    //this method sends a request to a remote AMS to know the APDescription of a remote Platform
    public void addRemotePlatform(AID remoteAMS) {
        if (logger.isLoggable(Logger.FINE))
            logger.log(Logger.FINE, "AddRemotePlatform" + remoteAMS.toString());
        try {

            ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
            requestMsg.setSender(getAID());
            requestMsg.clearAllReceiver();
            requestMsg.addReceiver(remoteAMS);
            requestMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            requestMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
            requestMsg.setOntology(FIPAManagementOntology.NAME);

            GetDescription action = new GetDescription();
            Action a = new Action();
            a.setActor(remoteAMS);
            a.setAction(action);

            getContentManager().fillContent(requestMsg, a);
            addBehaviour(new handleAddRemotePlatformBehaviour("GetDescription", requestMsg));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addRemotePlatformFromURL(String url) {

        try {
            URL AP_URL = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(AP_URL.openStream()));

            StringBuilder buf = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (!inputLine.equals("")) {
                    buf.append(inputLine);
                    buf.append(" ");
                }
            }
            //to parse the APDescription it is put in a Dummy ACLMessage
            ACLMessage dummyMsg = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
            dummyMsg.setOntology(FIPAManagementOntology.NAME);
            dummyMsg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
            String content = "(( result (action ( agent-identifier :name ams :addresses (sequence IOR:00000000000000) :resolvers (sequence ) ) (get-description ) ) (sequence " + buf + " ) ) )";
            dummyMsg.setContent(content);
            try {

                Result r = (Result) getContentManager().extractContent(dummyMsg);

                Iterator<Object> i = r.getItems().iterator();
                Object obj = null;
                if (i.hasNext()) {
                    obj = i.next();
                }
                APDescription APDesc = (APDescription) obj;

                while (i.hasNext() && (APDesc != null)) {
                    String amsName = "ams@" + APDesc.getName();
                    if (amsName.equalsIgnoreCase(getAMS().getName())) {
                        if (logger.isLoggable(Logger.WARNING))
                            logger.log(Logger.WARNING, "ERROR: Action not allowed.");
                    } else {
                        // create the proper AID for AMS by adding all available addresses
                        AID ams = new AID(amsName, AID.ISGUID);
                        for (Iterator<APService> is = APDesc.getAllAPServices(); is.hasNext(); ) {
                            APService s = is.next();
                            for (Iterator<String> js = s.getAllAddresses(); js.hasNext(); )
                                ams.addAddresses(js.next());
                        }

                        myGUI.addRemotePlatformFolder();
                        myGUI.addRemotePlatform(ams, APDesc);
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void viewAPDescription(String title) {
        myGUI.viewAPDescriptionDialog(myPlatformProfile, title);
    }

    public void viewAPDescription(APDescription remoteAP, String title) {
        myGUI.viewAPDescriptionDialog(remoteAP, title);
    }

    public void removeRemotePlatform(APDescription platform) {
        myGUI.removeRemotePlatform(platform.getName());
    }

    //make a search on a specified ams in order to return
    //all the agents registered with that ams.
    public void refreshRemoteAgent(APDescription platform, AID ams) {
        try {
            // FIXME. Move all this block into the constructor for better performance
            // because it is invariant to the method parameters
            ACLMessage request; // variable that keeps the request search message
            Action act;  // variable that keeps the search action
            request = new ACLMessage(ACLMessage.REQUEST);
            request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            request.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
            request.setOntology(FIPAManagementOntology.NAME);
            AMSAgentDescription amsd = new AMSAgentDescription();
            SearchConstraints constraints = new SearchConstraints();
            constraints.setMaxResults(-1L); // all results back
            // Build a AMS action object for the request
            Search s = new Search();
            s.setDescription(amsd);
            s.setConstraints(constraints);
            act = new Action();
            act.setAction(s);

            // request has been already initialized in the constructor
            request.clearAllReceiver();
            request.addReceiver(ams);
            act.setActor(ams); // set the actor of this search action
            getContentManager().fillContent(request, act);

            addBehaviour(new handleRefreshRemoteAgentBehaviour("search", request, platform));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ask the local AMS to register a remote Agent.
    public void registerRemoteAgentWithAMS(AMSAgentDescription amsd) {

        Register register_act = new Register();
        register_act.setDescription(amsd);

        try {
            Action a = new Action();
            a.setActor(getAMS());
            a.setAction(register_act);

            ACLMessage requestMsg = getRequest();
            requestMsg.setOntology(FIPAManagementOntology.NAME);
            getContentManager().fillContent(requestMsg, a);

            addBehaviour(new AMSClientBehaviour("Register", requestMsg));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Sends requests to the AMS
    private class AMSClientBehaviour extends SimpleAchieveREInitiator {

        private final String actionName;


        public AMSClientBehaviour(String an, ACLMessage request) {
            super(rma.this, request);
            actionName = an;
        }


        protected void handleNotUnderstood(ACLMessage reply) {
            myGUI.showErrorDialog("NOT-UNDERSTOOD received by RMA during " + actionName, reply);
        }

        protected void handleRefuse(ACLMessage reply) {
            myGUI.showErrorDialog("REFUSE received during " + actionName, reply);
        }

        protected void handleAgree(ACLMessage reply) {
            if (logger.isLoggable(Logger.FINE))
                logger.log(Logger.FINE, "AGREE received" + reply);
        }

        protected void handleFailure(ACLMessage reply) {
            myGUI.showErrorDialog("FAILURE received during " + actionName, reply);
        }

        protected void handleInform(ACLMessage reply) {
            if (logger.isLoggable(Logger.FINE))
                logger.log(Logger.FINE, "INFORM received" + reply);
        }

    } // End of AMSClientBehaviour class

    private class handleAddRemotePlatformBehaviour extends AMSClientBehaviour {

        public handleAddRemotePlatformBehaviour(String an, ACLMessage request) {
            super(an, request);

        }

        protected void handleInform(ACLMessage msg) {
            if (logger.isLoggable(Logger.FINE))
                logger.log(Logger.FINE, "arrived a new APDescription");
            try {
                AID sender = msg.getSender();
                Result r = (Result) getContentManager().extractContent(msg);
                Iterator<Object> i = r.getItems().iterator();
                Object APDesc = i.next();
                if (APDesc != null) {
                    myGUI.addRemotePlatformFolder();
                    myGUI.addRemotePlatform(sender, (APDescription) APDesc);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }//end handleAddRemotePlatformBehaviour

    private class handleRefreshRemoteAgentBehaviour extends AMSClientBehaviour {

        private final APDescription platform;

        public handleRefreshRemoteAgentBehaviour(String an, ACLMessage request, APDescription ap) {
            super(an, request);
            platform = ap;

        }

        protected void handleInform(ACLMessage msg) {
            if (logger.isLoggable(Logger.FINE))
                logger.log(Logger.FINE, "arrived a new agents from a remote platform");
            try {
                AID sender = msg.getSender();
                Result r = (Result) getContentManager().extractContent(msg);
                Iterator<Object> i = r.getItems().iterator();
                myGUI.refreshRemoteAgentsInRemotePlatform(platform, i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }//end handleAddRemotePlatformBehaviour

    class RMAAMSListenerBehaviour extends AMSListenerBehaviour {
        protected void installHandlers(Map<String, EventHandler> handlersTable) {

            // Fill the event handler table.

            handlersTable.put(IntrospectionVocabulary.META_RESETEVENTS, (EventHandler) ev -> {
                ResetEvents re = (ResetEvents) ev;
                myGUI.resetTree();
            });

            handlersTable.put(IntrospectionVocabulary.ADDEDCONTAINER, (EventHandler) ev -> {
                AddedContainer ac = (AddedContainer) ev;
                ContainerID cid = ac.getContainer();
                String name = cid.getName();
                String address = cid.getAddress();
                try {
                    InetAddress addr = InetAddress.getByName(address);
                    myGUI.addContainer(name, addr);
                } catch (UnknownHostException uhe) {
                    myGUI.addContainer(name, null);
                }
            });


            handlersTable.put(IntrospectionVocabulary.REMOVEDCONTAINER, (EventHandler) ev -> {
                RemovedContainer rc = (RemovedContainer) ev;
                ContainerID cid = rc.getContainer();
                String name = cid.getName();
                myGUI.removeContainer(name);
            });

            handlersTable.put(IntrospectionVocabulary.BORNAGENT, (EventHandler) ev -> {
                BornAgent ba = (BornAgent) ev;
                ContainerID cid = ba.getWhere();
                // ContainerID is null in case of foreign agents registered with the local AMS or virtual agents
                // FIXME: Such agents should be shown somewhere
                if (cid != null) {
                    String container = cid.getName();
                    AID agent = ba.getAgent();
                    myGUI.addAgent(container, agent, ba.getState(), ba.getOwnership());
                    if (agent.equals(getAID()))
                        myContainerName = container;
                }
            });

            handlersTable.put(IntrospectionVocabulary.DEADAGENT, (EventHandler) ev -> {
                DeadAgent da = (DeadAgent) ev;
                ContainerID cid = da.getWhere();
                // ContainerID is null in case of foreign agents registered with the local AMS or virtual agents
                if (cid != null) {
                    String container = cid.getName();
                    AID agent = da.getAgent();
                    myGUI.removeAgent(container, agent);
                }
            });

            handlersTable.put(IntrospectionVocabulary.SUSPENDEDAGENT, (EventHandler) ev -> {
                SuspendedAgent sa = (SuspendedAgent) ev;
                ContainerID cid = sa.getWhere();
                String container = cid.getName();
                AID agent = sa.getAgent();
                myGUI.modifyAgent(container, agent, AMSAgentDescription.SUSPENDED, null);
            });

            handlersTable.put(IntrospectionVocabulary.RESUMEDAGENT, (EventHandler) ev -> {
                ResumedAgent ra = (ResumedAgent) ev;
                ContainerID cid = ra.getWhere();
                String container = cid.getName();
                AID agent = ra.getAgent();
                myGUI.modifyAgent(container, agent, AMSAgentDescription.ACTIVE, null);
            });

            handlersTable.put(IntrospectionVocabulary.FROZENAGENT, (EventHandler) ev -> {
                FrozenAgent fa = (FrozenAgent) ev;
                String oldContainer = fa.getWhere().getName();
                String newContainer = fa.getBufferContainer().getName();
                AID agent = fa.getAgent();
                myGUI.modifyFrozenAgent(oldContainer, newContainer, agent);
            });

            handlersTable.put(IntrospectionVocabulary.THAWEDAGENT, (EventHandler) ev -> {
                ThawedAgent ta = (ThawedAgent) ev;
                String oldContainer = ta.getWhere().getName();
                String newContainer = ta.getBufferContainer().getName();
                AID agent = ta.getAgent();
                myGUI.modifyThawedAgent(oldContainer, newContainer, agent);
            });

            handlersTable.put(IntrospectionVocabulary.CHANGEDAGENTOWNERSHIP, (EventHandler) ev -> {
                ChangedAgentOwnership cao = (ChangedAgentOwnership) ev;
                ContainerID cid = cao.getWhere();
                String container = cid.getName();
                AID agent = cao.getAgent();
                myGUI.modifyAgent(container, agent, null, cao.getTo());
            });

            handlersTable.put(IntrospectionVocabulary.MOVEDAGENT, (EventHandler) ev -> {
                MovedAgent ma = (MovedAgent) ev;
                AID agent = ma.getAgent();
                ContainerID from = ma.getFrom();
                ContainerID to = ma.getTo();
                myGUI.moveAgent(from.getName(), to.getName(), agent);
            });

            handlersTable.put(IntrospectionVocabulary.ADDEDMTP, (EventHandler) ev -> {
                AddedMTP amtp = (AddedMTP) ev;
                String address = amtp.getAddress();
                ContainerID where = amtp.getWhere();
                myGUI.addAddress(address, where.getName());
            });

            handlersTable.put(IntrospectionVocabulary.REMOVEDMTP, (EventHandler) ev -> {
                RemovedMTP rmtp = (RemovedMTP) ev;
                String address = rmtp.getAddress();
                ContainerID where = rmtp.getWhere();
                myGUI.removeAddress(address, where.getName());
            });

            //handle the APDescription provided by the AMS
            handlersTable.put(IntrospectionVocabulary.PLATFORMDESCRIPTION, (EventHandler) ev -> {
                PlatformDescription pd = (PlatformDescription) ev;
                myPlatformProfile = pd.getPlatform();
                myGUI.refreshLocalPlatformName(myPlatformProfile.getName());
            });

        }
    } // END of inner class RMAAMSListenerBehaviour
}
