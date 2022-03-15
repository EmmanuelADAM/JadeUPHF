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

package jade.domain.FIPAAgentManagement;

import jade.content.Concept;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class implements the concept of the fipa-agent-management ontology
 * representing the description of an agent platform as it can be retrieved
 * from the AMS.
 *
 * @author Giovanni Rimassa - Universita' di Parma
 * @author Alessandro Chiarotto - TILAB
 * @version $Date: 2003-11-24 14:47:00 +0100 (lun, 24 nov 2003) $ $Revision: 4597 $
 * @see FIPAManagementOntology
 */
public class APDescription implements Concept {

    private final List<APService> services = new ArrayList<>(1);
    private String name;


    /**
     * Default constructor.
     */
    public APDescription() {
    }

    /**
     * Retrieve the <code>name</code> slot of this object.
     *
     * @return The value of the <code>name</code> slot of this
     * platform description, or <code>null</code> if no value was set.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the <code>name</code> slot of this object.
     *
     * @param n The string for the platform name.
     */
    public void setName(String n) {
        name = n;
    }

    /**
     * Add a service to the <code>ap-services</code> slot collection
     * of this object.
     *
     * @param a The platform service to add to the collection.
     */
    public void addAPServices(APService a) {
        services.add(a);
    }

    /**
     * Remove a service from the <code>ap-services</code> slot
     * collection of this object.
     *
     * @param a The platform service to remove from the collection.
     * @return A boolean, telling whether the element was present in
     * the collection or not.
     */
    public boolean removeAPServices(APService a) {
        return services.remove(a);
    }

    /**
     * Remove all services from the <code>ap-services</code> slot
     * collection of this object.
     */
    public void clearAllAPServices() {
        services.clear();
    }

    /**
     * Access all services from the <code>ap-services</code> slot
     * collection of this object.
     *
     * @return An iterator over the services collection.
     */
    public Iterator<APService> getAllAPServices() {
        return services.iterator();
    }

    /**
     * Retrieve a string representation for this platform description.
     *
     * @return an SL0-like String representation of this object
     **/
    public String toString() {
        StringBuilder str = new StringBuilder("( ap-description ");
        if ((name != null) && (name.length() > 0))
            str.append(" :name ").append(name);
        APService s;
        str.append(" :ap-services (set");
        for (APService service : services) {
            s = service;
            str.append(" ").append(s.toString());
        }
        str.append("))");
        return str.toString();
    }


}
