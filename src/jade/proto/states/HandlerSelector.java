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

package jade.proto.states;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a selector of handler
 * (i.e.  jade.core.behaviours.Behaviour  )
 * A number of handlers can be registered with
 * this  HandlerSelector  , each handler bound to a different key.
 * The abstract method  getSelectionKey   is then called that
 * must return the key to select one of the registered handlers.
 * The selected handler is finally scheduled for execution.
 *
 * @author Giovanni Caire - TILab Torino
 * @version $Date: 2005-09-16 15:54:46 +0200 (ven, 16 set 2005) $ $Revision: 5780 $
 **/
public abstract class HandlerSelector extends FSMBehaviour {
    /**
     * Value returned by  onEnd   method if
     * an handler was found mapped to the key
     **/
    public static final int SELECTION_OK = 1;
    /**
     * Value returned by  onEnd   method if
     * no handler was found mapped to the key
     **/
    public static final int SELECTION_NOK = 0;
    // FSM states names
    private static final String SELECT = "Select";
    private static final String HANDLE = "Handle";
    private static final String DUMMY = "Dummy";

    // States exit values
    private final Map<Object, Behaviour> handlers = new HashMap<>();
    private final Object accesKey;


    /**
     * Constructor for this HandlerSelector.
     *
     * @param a         is a reference to the Agent object
     * @param s         is the HashMap where the object can be retrieved from
     * @param accessKey is the key to get the proper object from the HashMap,
     *                  this is the object that will be later passed as argument to the
     *                  method  getSelectionKey
     * @see #getSelectionKey(Object)
     **/
    public HandlerSelector(Agent a, HashMap s, Object accessKey) {
        super(a);

        setMapMessagesList(s);
        this.accesKey = accessKey;

        Behaviour b;
        // Create and register the states that make up the FSM
        // SELECT
        b = new OneShotBehaviour(myAgent) {
            int ret;

            public void action() {
                ret = SELECTION_NOK;
                Object key = getSelectionKey(getMapMessagesList().get(accesKey));
                if (key != null) {
                    Behaviour b1 = handlers.get(key);
                    if (b1 != null) {
                        // The HANDLE state is registered on the fly
                        registerLastState(b1, HANDLE);
                        ret = SELECTION_OK;
                    }
                }
            }

            public int onEnd() {
                return ret;
            }
        };
        b.setMapMessagesList(getMapMessagesList());
        registerFirstState(b, SELECT);

        // DUMMY
        b = new OneShotBehaviour(myAgent) {
            public void action() {
            }
        };
        registerLastState(b, DUMMY);

        // Register the FSM transitions
        registerTransition(SELECT, HANDLE, SELECTION_OK);
        registerDefaultTransition(SELECT, DUMMY);
    }

    /**
     * Subclasses must provide a concrete implementation for this method.
     * It must return the key to which the handler was bound.
     *
     * @param selectionVar the object retrieved from the HashMap at
     *                     the  accessKey   passed in the constructor
     * @return the key to which the handler was bound
     **/
    protected abstract Object getSelectionKey(Object selectionVar);

    /**
     * Register the bounding between an handler and a key.
     *
     * @param key this is the key that must be later returned by
     *            the method  getSelectionKey   when the passed
     *            Behaviour must be selected
     * @param h   the handler for this key
     **/
    public void registerHandler(Object key, Behaviour h) {
        handlers.put(key, h);
    }
}
		
