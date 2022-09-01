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
import java.util.Objects;

/**
 * This class models a service data type.
 *
 * @author Fabio Bellifemine - CSELT S.p.A.
 * @version $Date: 2006-12-14 17:26:48 +0100 (gio, 14 dic 2006) $ $Revision: 5916 $
 */
public class ServiceDescription implements Concept {

    private final List<String> interactionProtocols = new ArrayList<>();
    private final List<String> ontology = new ArrayList<>();
    private final List<String> language = new ArrayList<>();
    private final List<Property> properties = new ArrayList<>();
    private String name;
    private String type;
    private String ownership;

    /**
     * Default constructor. A default constructor is necessary for
     * JADE ontological classes.
     */
    public ServiceDescription() {
    }

    /**
     * Retrieve the  name   slot of this object.
     *
     * @return The value of the  name   slot of this service
     * description, or  null   if no value was set.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the  name   slot of this object.
     *
     * @param n The name of the described service.
     */
    public void setName(String n) {
        name = n;
    }

    /**
     * Retrieve the  type   slot of this object.
     *
     * @return The value of the  type   slot of this service
     * description, or  null   if no value was set.
     */
    public String getType() {
        return type;
    }

    /**
     * Set the  type   slot of this object.
     *
     * @param t The type of the described service.
     */
    public void setType(String t) {
        type = t;
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

    /**
     * Retrieve the  ownership   slot of this object.
     *
     * @return The value of the  ownership   slot of this
     * service description, or  null   if no value was set.
     */
    public String getOwnership() {
        return ownership;
    }

    /**
     * Set the  ownership   slot of this object.
     *
     * @param o The name of the entity owning the described service.
     */
    public void setOwnership(String o) {
        ownership = o;
    }

    /**
     * Add a property to the  properties   slot collection
     * of this object.
     *
     * @param p The property to add to the collection.
     */
    public void addProperties(Property p) {
        properties.add(p);
    }

    /**
     * Remove a property from the  properties   slot
     * collection of this object.
     *
     * @param p The property to remove from the collection.
     * @return A boolean, telling whether the element was present in
     * the collection or not.
     */
    public boolean removeProperties(Property p) {
        return properties.remove(p);
    }

    /**
     * Remove all properties from the  properties   slot
     * collection of this object.
     */
    public void clearAllProperties() {
        properties.clear();
    }

    /**
     * Access all properties from the  properties   slot
     * collection of this object.
     *
     * @return An iterator over the properties collection.
     */
    public Iterator<Property> getAllProperties() {
        return properties.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceDescription that = (ServiceDescription) o;

        if (!Objects.equals(name, that.name)) return false;
        return Objects.equals(type, that.type);
    }

    @Override
    public String toString() {
        return "Service{" + name + '\'' + type + '}';
    }
}
