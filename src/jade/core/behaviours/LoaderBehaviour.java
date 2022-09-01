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

package jade.core.behaviours;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.leap.LEAPCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.ExceptionVocabulary;
import jade.domain.mobility.BehaviourLoadingOntology;
import jade.domain.mobility.LoadBehaviour;
import jade.domain.mobility.Parameter;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This behaviour serves behaviour-loading requests
 * according to the Behaviour-loading ontology.
 * When an agent runs an instance of this behaviour it becomes able
 * to load and execute completely new behaviours, i.e. behaviours
 * whose code is not included in the classpath of the JVM where the
 * agent lives.
 * Loading behaviour requests must have the  ACLMessage.REQUEST  
 * performative and must use the BehaviourLoading ontology and the
 * LEAP language.
 *
 * <br>
 * <b>NOT available in MIDP</b>
 * <br>
 * <p>
 * see LoadBehaviour
 *
 * @author Giovanni Caire - TILAB
 * @see BehaviourLoadingOntology
 * @see LEAPCodec
 */
public class LoaderBehaviour extends Behaviour {
    private final Codec codec = new LEAPCodec();
    private final Ontology onto = BehaviourLoadingOntology.getInstance();
    private final ContentManager myContentManager = new ContentManager();
    private final MessageTemplate myTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.and(
                    MessageTemplate.MatchLanguage(codec.getName()),
                    MessageTemplate.MatchOntology(onto.getName())
            )
    );
    private ClassLoader localLoader;
    private boolean finished = false;

    /**
     * Construct a LoaderBehaviour.
     */
    public LoaderBehaviour() {
        super();
        init();
    }

    /**
     * Construct a LoaderBehaviour to be executed by a given agent.
     */
    public LoaderBehaviour(Agent a) {
        super(a);
        init();
    }

    /**
     * Construct a LoaderBehaviour to be executed by a given agent and that will use a given class loader to load behaviours whose code
     * is not embedded in the LoadBehaviour request.
     */
    public LoaderBehaviour(Agent a, ClassLoader cl) {
        super(a);
        init();
        localLoader = cl;
    }

    /**
     * The action() method is redefined to serve behaviour loading requests
     */
    public final void action() {
        if (!finished) {
            ACLMessage msg = myAgent.receive(myTemplate);
            if (msg != null) {
                ACLMessage reply = msg.createReply();

                if (accept(msg)) {
                    try {
                        Action actionExpr = (Action) myContentManager.extractContent(msg);
                        LoadBehaviour lb = (LoadBehaviour) actionExpr.getAction();

                        // Load the behaviour
                        String className = lb.getClassName();
                        Behaviour b;
                        byte[] code = lb.getCode();
                        byte[] zip = lb.getZip();
                        if (code != null) {
                            // Try from the class byte code
                            b = loadFromCode(className, code);
                        } else if (zip != null) {
                            // Try from the zip
                            b = loadFromZip(className, zip);
                        } else {
                            //#J2ME_EXCLUDE_BEGIN
                            // Try using the local loader if any. Otherwise load from the classpath
                            if (localLoader != null) {
                                b = (Behaviour) Class.forName(className, true, localLoader).getDeclaredConstructor().newInstance();
                            } else {
                                //#J2ME_EXCLUDE_END
                                b = (Behaviour) Class.forName(className).getDeclaredConstructor().newInstance();
                                //#J2ME_EXCLUDE_BEGIN
                            }
                            //#J2ME_EXCLUDE_END
                        }

                        // Set parameters
                        List<Parameter> params = lb.getParameters();
                        setInputParameters(b, params);

                        // Start the behaviour and prepare a positive reply
                        SequentialBehaviour sb = new SequentialBehaviour(myAgent);
                        sb.addSubBehaviour(b);
                        sb.addSubBehaviour(new ResultCollector(b, params, actionExpr, msg));
                        myAgent.addBehaviour(sb);
                        reply.setPerformative(ACLMessage.AGREE);
                    } catch (Codec.CodecException | OntologyException ce) {
                        ce.printStackTrace();
                        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                        reply.setContent("((" + ExceptionVocabulary.UNRECOGNISEDVALUE + " content))");
                    } catch (Exception e) {
                        e.printStackTrace();
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("((internal-error \"" + e + "\"))");
                    }
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    /**
     * The done() method is redefined to make this behaviour terminate
     * when its  stop()   method is called.
     */
    public boolean done() {
        return finished;
    }

    /**
     * Make this behaviour terminate.
     */
    public void stop() {
        finished = true;
        restart();
    }

    /**
     * Add a loaded behaviour to the agent.
     * Subclasses may redefine this method to handle the behaviour
     * addition operation in an application specific way.
     *
     * @param b       The  Behaviour   to be added.
     * @param request The  ACLMessage   carrying the
     *                 LoadBehaviour   request.
     */
    protected void addBehaviour(Behaviour b, ACLMessage request) {
        myAgent.addBehaviour(b);
    }

    /**
     * Suclasses may redefine this method to prevent the behaviour
     * loading operation under specific conditions.
     * This default implementation always returns  true  
     */
    protected boolean accept(ACLMessage msg) {
        return true;
    }

    ///////////////////////////
    // Private utility methods
    ///////////////////////////
    private void init() {
        // Register LEAP language and BehaviourLoading ontology in a
        // local ContentManager
        myContentManager.registerLanguage(codec);
        myContentManager.registerOntology(onto);

        localLoader = getClass().getClassLoader();
    }

    private Behaviour loadFromCode(String className, final byte[] code) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Hashtable<String, byte[]> classes = new Hashtable<>(1);
        classes.put(className, code);
        return load(className, classes);
    }

    private Behaviour loadFromZip(String className, byte[] zip) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Hashtable<String, byte[]> classes = new Hashtable<>();
        try {
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] tmp = new byte[1024];
                int k = zis.read(tmp, 0, tmp.length);
                while (k > 0) {
                    baos.write(tmp, 0, k);
                    k = zis.read(tmp, 0, tmp.length);
                }
                classes.put(ze.getName(), baos.toByteArray());
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            return load(className, classes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException("Error reading zip for class " + className + ". " + e);
        }
    }

    private Behaviour load(String className, Hashtable<String, byte[]> classes) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ClassLoader loader = new HashClassLoader(classes, getClass().getClassLoader());

        //#J2ME_EXCLUDE_BEGIN
        Class<?> c = Class.forName(className, true, loader);
        //#J2ME_EXCLUDE_END
		/*#J2ME_INCLUDE_BEGIN
		Class c = loader.loadClass(className);
		#J2ME_INCLUDE_END*/
        Behaviour b = null;
        try {
            b = (Behaviour) c.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException e) {
            System.out.println(e);
        }
        return b;
    }

    protected void setInputParameters(Behaviour b, List<Parameter> params) {
        var ds = b.getMapMessages();
        if (params != null) {
            for (Parameter param : params) {
                if (param.getMode() == Parameter.IN_MODE || param.getMode() == Parameter.INOUT_MODE) {
                    ds.put(param.getName(), (ACLMessage)param.getValue());
                }
            }
        }
    }

    protected void getOutputParameters(Behaviour b, List<Parameter> params) {
        var ds = b.getMapMessages();
        if (params != null) {
            for (Parameter param : params) {
                if (param.getMode() == Parameter.OUT_MODE || param.getMode() == Parameter.INOUT_MODE) {
                    param.setValue(ds.get(param.getName()));
                }
            }
        }
    }

    /**
     * Inner class HashClassLoader
     */
    private class HashClassLoader extends ClassLoader {
        private final Hashtable<String, byte[]> classes;

        public HashClassLoader(Hashtable<String, byte[]> ht, ClassLoader parent) {
            //#PJAVA_EXCLUDE_BEGIN
            super(parent);
            //#PJAVA_EXCLUDE_END
			/*#PJAVA_INCLUDE_BEGIN
			super();
			#PJAVA_INCLUDE_END*/
            classes = ht;
        }

        protected Class<?> findClass(String name) throws ClassNotFoundException {
            String fileName = name.replace('.', '/') + ".class";
            byte[] code = classes.get(fileName);
            if (code != null) {
                return defineClass(name, code, 0, code.length);
            } else {
                throw new ClassNotFoundException("Class " + name + " does not exist");
            }
        }

		/*#PJAVA_INCLUDE_BEGIN In PersonalJava loadClass(String, boolean) is abstract --> we must implement it
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
	  	// 1) Try to see if the class has already been loaded
	  	Class c = findLoadedClass(name);

			// 2) Try to load the class using the system class loader
	  	if(c == null) {
		    try {
		        c = findSystemClass(name);
		    }
		    catch (ClassNotFoundException cnfe) {
		    }
			}

	  	// 3) If still not found, try to load the class from the code
	  	if(c == null) {
  	    c = findClass(name);
	  	}

	  	if(resolve) {
	  	    resolveClass(c);
	  	}

	  	return c;
    }
		#PJAVA_INCLUDE_END*/
    }  // END of inner class HashClassLoader

    /**
     * Inner class ResultCollector.
     * This behaviour restores the output parameters at the end of
     * the execution of the loaded behaviour and sends back the
     * result notification to the requester.
     */
    private class ResultCollector extends OneShotBehaviour {
        private final Behaviour myBehaviour;
        private final Action actionExpr;
        private final ACLMessage request;
        private List<Parameter> myParams;

        ResultCollector(Behaviour b, List<Parameter> l, Action a, ACLMessage m) {
            super();
            myBehaviour = b;
            myParams = l;
            actionExpr = a;
            request = m;
        }

        public void action() {
            // Avoid sending back the behaviour code
            LoadBehaviour lb = (LoadBehaviour) actionExpr.getAction();
            lb.setCode(null);
            lb.setZip(null);

            // Restore output parameters
            getOutputParameters(myBehaviour, myParams);

            if (myParams == null) {
                // Result cannot be null
                myParams = new ArrayList<>();
            }
            Result r = new Result(actionExpr, myParams);
            ACLMessage notification = request.createReply();
            try {
                myContentManager.fillContent(notification, r);
                notification.setPerformative(ACLMessage.INFORM);
            } catch (Exception e) {
                e.printStackTrace();
                notification.setPerformative(ACLMessage.FAILURE);
                notification.setContent("((internal-error \"" + e + "\"))");
            }
            myAgent.send(notification);
        }
    } // END of inner class ResultCollector
}