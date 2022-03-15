package jade.domain;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.util.Logger;

import java.io.Serial;
import java.util.Iterator;
import java.util.List;

//#PJAVA_EXCLUDE_FILE
//#MIDP_EXCLUDE_FILE

public abstract class DFSubscriber extends SubscriptionInitiator {
    @Serial
    private static final long serialVersionUID = -5741304962740821073L;

    private static final Logger logger = Logger.getJADELogger(DFSubscriber.class.getName());

    private boolean firstNotificationReceived = false;

    private  DFAgentDescription template;

    public DFSubscriber(Agent a, DFAgentDescription template) {
        super(a, DFService.createSubscriptionMessage(a, a.getDefaultDF(), template, null));
        this.template = template;
    }

    public abstract void onRegister(DFAgentDescription dfad);

    public abstract void onDeregister(DFAgentDescription dfad);

    public void afterFirstNotification(DFAgentDescription[] dfds) {
        // default: nothing to do
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        try {
            DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
            var l = template.getAllServices();
            for (DFAgentDescription dfad : dfds) {
                if (dfad.getAllServices().containsAll(l)) {
                    onRegister(dfad);
                } else {
                    onDeregister(dfad);
                }
            }
            if (!firstNotificationReceived) {
                firstNotificationReceived = true;
                afterFirstNotification(dfds);
            }
        } catch (Exception e) {
            logger.log(Logger.SEVERE, "Agent " + myAgent.getName() + ": Error decoding DF notification", e);
        }
    }
}