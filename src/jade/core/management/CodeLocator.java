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

package jade.core.management;

//#J2ME_EXCLUDE_FILE

import jade.core.AID;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

/**
 * Class that maps agents and classloaders.
 * <p>
 * The access to the methods should be made inside a synchronized
 * block against the CodeLocator instance.
 *
 * @author <a href="mailto:jcucurull@deic.uab.cat">Jordi Cucurull Juan</a>
 * @version 2.0
 */

public class CodeLocator {

    public CodeLocator() {
        _agents = new HashMap<>();
        _subscriptions = new Vector<>();
    }

    /**
     * Register an agent to the CodeLocator.
     *
     * @param name Agent name.
     * @param cl   Agent associated classloader.
     */
    public synchronized void registerAgent(AID name, ClassLoader cl) {

        _agents.put(name, cl);

        // Notify listeners.
        Enumeration<CodeLocatorListener> subs = _subscriptions.elements();
        while (subs.hasMoreElements()) {
            (subs.nextElement()).handleRegisterAgent(name, cl);
        }

    }

    /**
     * Update an agent ClassLoader in the CodeLocator.
     *
     * @param name Agent name.
     * @param cl   Agent associated classloader.
     * @return True - Agent is updated.
     * False - Agent cannot be updated.
     * //* @throws Exception
     */

    public synchronized boolean updateAgent(AID name, ClassLoader cl) {

        if (_agents.containsKey(name)) {
            ClassLoader clOld = _agents.get(name);
            _agents.put(name, cl);

            // Notify listeners.
            Enumeration<CodeLocatorListener> subs = _subscriptions.elements();
            while (subs.hasMoreElements()) {
                (subs.nextElement()).handleUpdateAgent(name, clOld, cl);
            }

            return true;

        } else {
            return false;
        }
    }

    /**
     * Remove an agent fromt the list.
     *
     * @param name Agent name.
     */
    public synchronized void removeAgent(AID name) {

        ClassLoader cl = _agents.remove(name);

        // If agent use JarClassLoader close it.
        if (cl instanceof JarClassLoader) {
            ((JarClassLoader) cl).close();
        }

        // Notify listeners.
        Enumeration<CodeLocatorListener> subs = _subscriptions.elements();
        while (subs.hasMoreElements()) {
            (subs.nextElement()).handleRemoveAgent(name, cl);
        }

    }

    /**
     * clone an agent .
     *
     * @param oldName AID of the agent to clone.
     * @param newName AID of the clone agent
     */
    public synchronized void cloneAgent(AID oldName, AID newName) {

        if (_agents.containsKey(oldName)) {

            ClassLoader cl = _agents.get(oldName);
            ClassLoader clNew = null;
            ClassLoader clNewTemp;

            // JarClassLoader clonning.
            if (cl instanceof JarClassLoader jcl) {
                try {
                    clNew = new JarClassLoader(new File(jcl.getJarFileName()), jcl.getParent());
                } catch (IOException ioe) {
                    System.out.println("CodeLocator: Error clonning JarClassLoader.");
                }
            }

            // Notify listeners.
            Enumeration<CodeLocatorListener> subs = _subscriptions.elements();
            while (subs.hasMoreElements()) {
                clNewTemp = (subs.nextElement()).handleCloneAgent(oldName, newName, cl);
                if (clNewTemp != null) clNew = clNewTemp;
            }

            // Assign the new classloader if returned.
            if (clNew == null) _agents.put(newName, cl);
            else _agents.put(newName, clNew);
        }

    }

    /**
     * Check if the agent is registered.
     *
     * @param name Agent name.
     * @return True - The agent is registered.
     * False - The agent is not registered.
     */
    public synchronized boolean isRegistered(AID name) {

        return _agents.containsKey(name);

    }

    /**
     * Get the agent associated classloader.
     *
     * @param name Agent name.
     * @return The agent ClassLoader.
     * //* @throws Throws and exception if the agent is not found.
     */
    public synchronized ClassLoader getAgentClassLoader(AID name) {

        return _agents.get(name);

    }

    /**
     * Change the agent name.
     *
     * @param oldName Old agent name.
     * @param newName New agent name.
     */
    public synchronized void changeAgentName(AID oldName, AID newName) {

        ClassLoader cl = _agents.remove(oldName);
        if (cl != null) _agents.put(newName, cl);
    }

    /**
     * Method to subscribe to CodeLocator basic events from
     * an external class.
     *
     * @param cle Class with a method per event which should
     *            be overloaded by users.
     */
    public synchronized void subscribeToEvents(CodeLocatorListener cle) {
        _subscriptions.add(cle);
    }

    /**
     * Method to unsubscribe to CodeLocator events.
     *
     * @param cle CodeLocatorListener class to unsubscribe.
     * @return True - Unsubscription ok.
     * False - Subscription not found.
     */
    public synchronized boolean unSubscribeToEvents(CodeLocatorListener cle) {
        return _subscriptions.remove(cle);
    }

    private final HashMap<AID, ClassLoader> _agents;
    private final Vector<CodeLocatorListener> _subscriptions;


}
