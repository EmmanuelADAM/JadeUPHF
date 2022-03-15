package jade.domain.introspection;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class IntrospectionServer extends CyclicBehaviour {
    private static Class<Serializable> serializableClass;

    static {
        try {
            serializableClass = (Class<Serializable>) Class.forName("java.io.Serializable");
        } catch (Exception ignored) {
        }
    }

    private Codec codec;
    private Ontology onto;
    private MessageTemplate template;

    public IntrospectionServer(Agent a) {
        super(a);
    }

    public void onStart() {
        ContentManager cm = myAgent.getContentManager();

        onto = IntrospectionOntology.getInstance();
        cm.registerOntology(onto);

        codec = cm.lookupLanguage(FIPANames.ContentLanguage.FIPA_SL);
        if (codec == null) {
            codec = cm.lookupLanguage(FIPANames.ContentLanguage.FIPA_SL2);
            if (codec == null) {
                codec = cm.lookupLanguage(FIPANames.ContentLanguage.FIPA_SL1);
                if (codec == null) {
                    codec = cm.lookupLanguage(FIPANames.ContentLanguage.FIPA_SL0);
                }
            }
        }
        if (codec == null) {
            codec = new SLCodec();
            cm.registerLanguage(codec);
        }

        template = MessageTemplate.and(
                MessageTemplate.MatchOntology(onto.getName()),
                MessageTemplate.MatchPerformative(jade.lang.acl.ACLMessage.REQUEST));
    }

    public void action() {
        jade.lang.acl.ACLMessage request = myAgent.receive(template);
        if (request != null) {
            try {
                ContentManager cm = myAgent.getContentManager();
                Action actionExpr = (Action) cm.extractContent(request);
                Object act = actionExpr.getAction();
                if (act instanceof GetKeys) {
                    serveGetKeys(request, actionExpr, (GetKeys) act);
                } else if (act instanceof GetValue) {
                    serveGetValue(request, actionExpr, (GetValue) act);
                } else {
                    serveUnknownAction(request, actionExpr, act);
                }
            } catch (OntologyException | CodecException oe) {
                reply(request, jade.lang.acl.ACLMessage.NOT_UNDERSTOOD);
                oe.printStackTrace();
            } catch (ValueEncodingException vee) {
                jade.lang.acl.ACLMessage msg = request.createReply();
                msg.setPerformative(jade.lang.acl.ACLMessage.FAILURE);
                msg.setContent("VALUE_NOT_ENCODABLE");
                myAgent.send(msg);
            } catch (Throwable t) {
                reply(request, jade.lang.acl.ACLMessage.FAILURE);
                t.printStackTrace();
            }
        } else {
            block();
        }
    }

    protected void reply(jade.lang.acl.ACLMessage request, int performative) {
        jade.lang.acl.ACLMessage msg = request.createReply();
        msg.setPerformative(performative);
        myAgent.send(msg);
    }

    protected void serveGetKeys(jade.lang.acl.ACLMessage request, Action aExpr, GetKeys action) throws Exception {
        List<String> keys = new ArrayList<>();
        Method[] mm = myAgent.getClass().getMethods();
        for (Method method : mm) {
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                Class<?> retType = method.getReturnType();
                if (retType.isPrimitive() || (serializableClass != null && serializableClass.isAssignableFrom(retType))) {
                    String key = method.getName().substring(3);
                    keys.add(key);
                }
            }
        }
        Result r = new Result(aExpr, keys);
        jade.lang.acl.ACLMessage reply = request.createReply();
        myAgent.getContentManager().fillContent(reply, r);
        reply.setPerformative(jade.lang.acl.ACLMessage.INFORM);
        myAgent.send(reply);
    }

    protected void serveGetValue(jade.lang.acl.ACLMessage request, Action aExpr, GetValue action) throws Exception {
        Method method = myAgent.getClass().getMethod("get" + action.getKey());
        Object value = method.invoke(myAgent, (Object[]) null);
        if (value == null) {
            value = "null";
        }
        Result r = new Result(aExpr, value);
        jade.lang.acl.ACLMessage reply = request.createReply();
        try {
            myAgent.getContentManager().fillContent(reply, r);
            reply.setPerformative(jade.lang.acl.ACLMessage.INFORM);
            myAgent.send(reply);
        } catch (OntologyException | CodecException oe) {
            throw new ValueEncodingException();
        }
    }

    protected void serveUnknownAction(jade.lang.acl.ACLMessage request, Action aExpr, Object action) {
        reply(request, jade.lang.acl.ACLMessage.REFUSE);
    }

    private class ValueEncodingException extends Exception {
    }
}
