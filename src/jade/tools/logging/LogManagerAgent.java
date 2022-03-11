package jade.tools.logging;

//#ANDROID_EXCLUDE_FILE

import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.introspection.*;
import jade.tools.logging.gui.LogManagerGUI;
import jade.tools.logging.ontology.LogManagementOntology;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * This tool agent supports local and remote management of logs in JADE containers.
 *
 * @author Giovanni Caire - TILAB
 * @author Rosalba Bochicchio - TILAB
 */
public class LogManagerAgent extends Agent {
    private LogManagerGUI myGui;
    private APDescription myPlatformProfile;

    private AMSSubscriber myAMSSubscriber;

    protected void setup() {
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(JADEManagementOntology.getInstance());
        getContentManager().registerOntology(LogManagementOntology.getInstance());

        myAMSSubscriber = new AMSSubscriber() {
            protected void installHandlers(Map<String, EventHandler> handlersTable) {
                handlersTable.put(IntrospectionVocabulary.META_RESETEVENTS, (EventHandler) ev -> myGui.resetTree());

                handlersTable.put(IntrospectionVocabulary.ADDEDCONTAINER, (EventHandler) ev -> {
                    AddedContainer ac = (AddedContainer) ev;
                    ContainerID cid = ac.getContainer();
                    String name = cid.getName();
                    String address = cid.getAddress();
                    try {
                        InetAddress addr = InetAddress.getByName(address);
                        myGui.addContainer(name, addr);
                    } catch (UnknownHostException uhe) {
                        myGui.addContainer(name, null);
                    }
                });

                handlersTable.put(IntrospectionVocabulary.REMOVEDCONTAINER, (EventHandler) ev -> {
                    RemovedContainer rc = (RemovedContainer) ev;
                    ContainerID cid = rc.getContainer();
                    String name = cid.getName();
                    myGui.removeContainer(name);
                });

                //handle the APDescription provided by the AMS
                handlersTable.put(IntrospectionVocabulary.PLATFORMDESCRIPTION, (EventHandler) ev -> {
                    PlatformDescription pd = (PlatformDescription) ev;
                    myPlatformProfile = pd.getPlatform();
                    myGui.refreshLocalPlatformName(myPlatformProfile.getName());
                });

            }
        };

        addBehaviour(myAMSSubscriber);

        myGui = new LogManagerGUI(this);
        myGui.showCorrect();
    }

    protected void takeDown() {
        myGui.dispose();
        send(myAMSSubscriber.getCancel());
    }
}
