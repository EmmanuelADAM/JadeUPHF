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
import jade.core.AID;

import java.util.*;


/**
 * This class implements the concept of the fipa-agent-management ontology
 * representing the description of an agent in the DF catalogue.
 *
 * @author Fabio Bellifemine - CSELT S.p.A.
 * @version $Date: 2006-12-14 17:26:48 +0100 (gio, 14 dic 2006) $ $Revision: 5916 $
 * @see FIPAManagementOntology
 * @see jade.domain.AMSService
 */
public class DFAgentDescription implements Concept {

    private final List<ServiceDescription> services = new ArrayList<>();
    private final List<String> interactionProtocols = new ArrayList<>();
    private final List<String> ontology = new ArrayList<>();
    private final List<String> language = new ArrayList<>();
    private AID name;
    // Added lease default value -1
    //private Date leaseTime = new Date(-1);
    private Date leaseTime;


    /**
     * Default constructor.
     */
    public DFAgentDescription() {
    }

    /**
     * Retrieve the agent identifier for the described agent.
     *
     * @return The identifier of the agent
     */
    public AID getName() {
        return name;
    }

    /**
     * Set the identifier of the agent
     *
     * @param n the identifier of the agent
     */
    public void setName(AID n) {
        name = n;
    }

    /**
     * Retrieve the lease time for the registration of this
     * description (i.e., how long it will be kept within the DF agent
     * knowledge base).
     *
     * @return The lease time for the registration of this
     *  DFAgentDescription   as an absolute time. A
     *  null   value indicates an infinite time.
     */
    public Date getLeaseTime() {
        return leaseTime;
    }

    /**
     * Set the lease time for the registration of this DFAgentDescription
     * as an absolute time.
     *
     * @param absoluteTime The lease time for the registration of this
     *                     DFAgentDescription as an absolute time. Use  null
     *                     (default) to indicate an infinite lease time
     */
    public void setLeaseTime(Date absoluteTime) {
        leaseTime = absoluteTime;
    }

    /**
     * Set the lease time for the registration of this
     *  DFAgentDescription   as a relative time.
     *
     * @param relativeTime The lease time for the registration of this
     *                     DFAgentDescription as a relative time.
     */
    public void setRelativeLeaseTime(long relativeTime) {
        leaseTime = new Date(System.currentTimeMillis() + relativeTime);
    }

    /**
     * Indicates whether the lease time for the registration of this
     *  DFAgentDescription   expired.
     *
     * @return If the lease time expired,  true   is
     * returned, and  false   otherwise.
     */
    public boolean checkLeaseTimeExpired() {
        if (leaseTime == null) {
            return false;
        } else {
            return (System.currentTimeMillis() > leaseTime.getTime());
        }
    }

    /**
     * Add a service description to the  service   slot
     * collection of this object.
     *
     * @param a The service description to add to the collection.
     */
    public void addServices(ServiceDescription a) {
        services.add(a);
    }

    /**
     * Remove a service description from the  services
     * slot collection of this object.
     *
     * @param a The service description to remove from the collection.
     * @return A boolean, telling whether the element was present in
     * the collection or not.
     */
    public boolean removeServices(ServiceDescription a) {
        return services.remove(a);
    }

    /**
     * Remove all service descriptions from the  services
     * slot collection of this object.
     */
    public void clearAllServices() {
        services.clear();
    }

    /**
     * Access all service descriptions from the  services
     * slot collection of this object.
     *
     * @return An iterator over the service descriptions collection.
     */
    public List<ServiceDescription> getAllServices() {
        return services;
    }

    /**
     * Add a protocol name to the  protocols   slot
     * collection of this object.
     *
     * @param ip The protocol name to add to the collection.
     */
    public void addProtocols(String ip) {
        interactionProtocols.add(ip);
    }

    /**
     * Remove a protocol name from the  protocols   slot
     * collection of this object.
     *
     * @param ip The protocol name to remove from the collection.
     * @return A boolean, telling whether the element was present in
     * the collection or not.
     */
    public boolean removeProtocols(String ip) {
        return interactionProtocols.remove(ip);
    }

    /**
     * Remove all protocol names from the  protocols   slot
     * collection of this object.
     */
    public void clearAllProtocols() {
        interactionProtocols.clear();
    }

    /**
     * Access all protocol names from the  protocols   slot
     * collection of this object.
     *
     * @return An iterator over the protocol names collection.
     */
    public Iterator<String> getAllProtocols() {
        return interactionProtocols.iterator();
    }

    /**
     * Add an ontology name to the  ontologies   slot
     * collection of this object.
     *
     * @param o The ontology name to add to the collection.
     */
    public void addOntologies(String o) {
        ontology.add(o);
    }

    /**
     * Remove an ontology name from the  ontologies   slot
     * collection of this object.
     *
     * @param o The ontology name to remove from the collection.
     * @return A boolean, telling whether the element was present in
     * the collection or not.
     */
    public boolean removeOntologies(String o) {
        return ontology.remove(o);
    }

    /**
     * Remove all ontology names from the  ontologies   slot
     * collection of this object.
     */
    public void clearAllOntologies() {
        ontology.clear();
    }

    /**
     * Access all ontology names from the  ontologies   slot
     * collection of this object.
     *
     * @return An iterator over the ontology names collection.
     */
    public Iterator<String> getAllOntologies() {
        return ontology.iterator();
    }

    /**
     * Add a content language name to the  languages   slot
     * collection of this object.
     *
     * @param l The content language name to add to the collection.
     */
    public void addLanguages(String l) {
        language.add(l);
    }

    /**
     * Remove a content language name from the  languages
     * slot collection of this object.
     *
     * @param l The content language name to remove from the
     *          collection.
     * @return A boolean, telling whether the element was present in
     * the collection or not.
     */
    public boolean removeLanguages(String l) {
        return language.remove(l);
    }

    /**
     * Remove all content language names from the
     *  languages   slot collection of this object.
     */
    public void clearAllLanguages() {
        language.clear();
    }

    /**
     * Access all content language names from the
     *  languages   slot collection of this object.
     *
     * @return An iterator over the content language names collection.
     */
    public Iterator<String> getAllLanguages() {
        return language.iterator();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DFAgentDescription that = (DFAgentDescription) o;

        return services != null ? services.equals(that.services) : that.services == null;
    }

    @Override
    public String toString() {
        return "DFAgentDescription{" +
                "services=" + Arrays.toString(services.toArray()) +
                '}';
    }
}
