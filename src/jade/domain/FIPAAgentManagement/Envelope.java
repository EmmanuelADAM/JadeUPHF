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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;


/**
 * This class models an envelope.
 *
 * @author Fabio Bellifemine - CSELT S.p.A.
 * @version $Date: 2009-08-26 08:56:09 +0200 (mer, 26 ago 2009) $ $Revision: 6183 $
 * @see FIPAManagementOntology
 */
public class Envelope implements Concept, Serializable {

    private final static int EXPECTED_LIST_SIZE = 2;
    /**
     * @serial
     */
    private ArrayList<AID> to = new ArrayList<>(EXPECTED_LIST_SIZE);
    /**
     * @serial
     */
    private AID from;
    /**
     * @serial
     */
    private String comments;
    /**
     * @serial
     */
    private String aclRepresentation;
    /**
     * @serial
     */
    private Long payloadLength;
    /**
     * @serial
     */
    private String payloadEncoding;
    /**
     * @serial
     */
    private Date date;

    /**
     * @serial
     */
    private ArrayList<AID> intendedReceiver = new ArrayList<>(EXPECTED_LIST_SIZE);
    /**
     * @serial
     */
    private Properties transportBehaviour;

    /**
     * @serial
     */
    private ArrayList<ReceivedObject> stamps = new ArrayList<>(EXPECTED_LIST_SIZE);

    /**
     * @serial
     */
    private ArrayList<Property> properties = new ArrayList<>(EXPECTED_LIST_SIZE);

    /**
     * Default constructor. Initializes the payloadLength to -1.
     **/
    public Envelope() {
        payloadLength = (long) -1;
    }


    /**
     * Add an agent identifier to the  to   slot collection
     * of this object.
     *
     * @param id The agent identifier to add to the collection.
     */
    public void addTo(AID id) {
        to.add(id);
    }

    /**
     * Remove an agent identifier from the  to   slot
     * collection of this object.
     *
     * @param id The agent identifierto remove from the collection.
     * @return A boolean, telling whether the element was present in
     * the collection or not.
     */
    public boolean removeTo(AID id) {
        return to.remove(id);
    }

    /**
     * Remove all agent identifiers from the  to   slot
     * collection of this object.
     */
    public void clearAllTo() {
        to.clear();
    }

    /**
     * Access all agent identifiers from the  to   slot
     * collection of this object.
     *
     * @return An iterator over the agent identifiers collection.
     */
    public Iterator<AID> getAllTo() {
        return to.iterator();
    }

    /**
     * Retrieve the  from   slot of this object.
     *
     * @return The value of the  from   slot of this
     * envelope, or  null   if no value was set.
     */
    public AID getFrom() {
        return from;
    }

    /**
     * Set the  from   slot of this object.
     *
     * @param id The agent identifier for the envelope sender.
     */
    public void setFrom(AID id) {
        from = id;
    }

    /**
     * Retrieve the  comments   slot of this object.
     *
     * @return The value of the  comments   slot of this
     * envelope, or  null   if no value was set.
     */
    public String getComments() {
        return comments;
    }

    /**
     * Set the  comments   slot of this object.
     *
     * @param c The string for the envelope comments.
     */
    public void setComments(String c) {
        comments = c;
    }

    /**
     * Retrieve the  acl-representation   slot of this
     * object.
     *
     * @return The value of the  acl-representation   slot
     * of this envelope, or  null   if no value was set.
     */
    public String getAclRepresentation() {
        return aclRepresentation;
    }

    /**
     * Set the  acl-representation   slot of this object.
     *
     * @param r The string for the ACL representation.
     */
    public void setAclRepresentation(String r) {
        aclRepresentation = r;
    }

    /**
     * Retrieve the  payload-length   slot of this object.
     *
     * @return The value of the  payload-length   slot of
     * this envelope, or  null   or a negative value if no value was set.
     */
    public Long getPayloadLength() {
        return payloadLength;
    }

    /**
     * Set the  payload-length   slot of this object.
     *
     * @param l The payload length, in bytes.
     */
    public void setPayloadLength(Long l) {
        payloadLength = l;
    }

    /**
     * Retrieve the  payload-encoding   slot of this object.
     *
     * @return The value of the  payload-encoding   slot of
     * this envelope, or  null   if no value was set.
     */
    public String getPayloadEncoding() {
        return payloadEncoding;
    }

    /**
     * Set the  payload-encoding   slot of this object.
     * This slot can be used to specify a different charset than
     * the standard one (US-ASCII) in order for instance to support
     * accentuated characters in the content slot of the ACL message
     * (e.g. setPayloadEncoding("UTF-8")).
     *
     * @param e The string for the payload encoding.
     */
    public void setPayloadEncoding(String e) {
        payloadEncoding = e;
    }

    /**
     * Retrieve the  date   slot of this object.
     *
     * @return The value of the  date   slot of this
     * envelope, or  null   if no value was set.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Set the  date   slot of this object.
     *
     * @param d The envelope date.
     */
    public void setDate(Date d) {
        date = d;
    }

    /**
     * Add an agent identifier to the  intended-receiver
     * slot collection of this object.
     *
     * @param id The agent identifier to add to the collection.
     */
    public void addIntendedReceiver(AID id) {
        intendedReceiver.add(id);
    }

    /**
     * Remove an agent identifier from the
     *  intended-receiver   slot collection of this object.
     *
     * @param id The agent identifier to remove from the collection.
     * @return A boolean, telling whether the element was present in
     * the collection or not.
     */
    public boolean removeIntendedReceiver(AID id) {
        return intendedReceiver.remove(id);
    }

    /**
     * Remove all agent identifiers from the
     *  intended-receiver   slot collection of this object.
     */
    public void clearAllIntendedReceiver() {
        intendedReceiver.clear();
    }

    /**
     * Access all agent identifiers from the  intended
     * receiver   slot collection of this object.
     *
     * @return An iterator over the agent identifiers collection.
     */
    public Iterator<AID> getAllIntendedReceiver() {
        return intendedReceiver.iterator();
    }

    /**
     * Retrieve the  received   slot of this object.
     *
     * @return The value of the  received   slot of this
     * envelope, or  null   if no value was set.
     */
    public ReceivedObject getReceived() {
        if (stamps.isEmpty())
            return null;
        else
            return stamps.get(stamps.size() - 1);
    }

    /**
     * Set the  received   slot of this object.
     *
     * @param ro The received object for the  received
     *           slot.
     */
    public void setReceived(ReceivedObject ro) {
        addStamp(ro);
    }

    /**
     * Add a  received-object   stamp to this message
     * envelope. This method is used by the ACC to add a new stamp to
     * the envelope at every routing hop.
     *
     * @param ro The  received-object   to add.
     */
    public void addStamp(ReceivedObject ro) {
        stamps.add(ro);
    }

    /**
     * Access the list of all the stamps. The
     *  received-object   stamps are sorted according to the
     * routing path, from the oldest to the newest.
     */
    public ReceivedObject[] getStamps() {
        ReceivedObject[] ret = new ReceivedObject[stamps.size()];
        int counter = 0;

        for (ReceivedObject stamp : stamps) ret[counter++] = stamp;

        return ret;
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

    //#MIDP_EXCLUDE_BEGIN

    /**
     * Retrieve a string representation for this platform description.
     *
     * @return an SL0-like String representation of this object
     **/
    public String toString() {
        StringBuilder s = new StringBuilder("(Envelope ");
        Iterator<AID> i = getAllTo();
        if (i.hasNext()) {
            s.append(" :to (sequence ");
            while (i.hasNext()) {
                s.append(" ").append(i.next().toString());
            }
            s.append(") ");
        }
        if (getFrom() != null)
            s.append(" :from ").append(getFrom().toString());
        if (getComments() != null)
            s.append(" :comments ").append(getComments());
        if (getAclRepresentation() != null)
            s.append(" :acl-representation ").append(getAclRepresentation());
        if (getPayloadLength() != null)
            s.append(" :payload-length ").append(getPayloadLength().toString());
        if (getPayloadEncoding() != null)
            s.append(" :payload-encoding ").append(getPayloadEncoding());
        if (getDate() != null)
            s.append(" :date ").append(getDate().toString());
        i = getAllIntendedReceiver();
        if (i.hasNext()) {
            s.append(" :intended-receiver (sequence ");
            while (i.hasNext()) {
                s.append(" ").append(i.next().toString());
            }
            s.append(") ");
        }
        ReceivedObject[] ro = getStamps();
        if (ro.length > 0) {
            s.append(" :received-object (sequence ");
            for (ReceivedObject receivedObject : ro) {
                if (receivedObject != null) {
                    s.append(" ").append(receivedObject);
                }
            }
            s.append(") ");
        }
        if (properties.size() > 0) {
            s.append(" :properties (set");
            for (Property property : properties) {
                s.append(" ").append(property.getName()).append(" ").append(property.getValue());
            }
            s.append(")");
        }
        return s + ")";
    }
    //#MIDP_EXCLUDE_END

    //#APIDOC_EXCLUDE_BEGIN
    public Object clone() {
        Envelope env = new Envelope();

        // Deep clone
        env.to = new ArrayList<>(to.size());
        for (int i = 0; i < to.size(); i++) {
            AID id = to.get(i);
            env.to.add((AID) id.clone());
        }

        // Deep clone
        env.intendedReceiver = new ArrayList<>(intendedReceiver.size());
        for (int i = 0; i < intendedReceiver.size(); i++) {
            AID id = intendedReceiver.get(i);
            env.intendedReceiver.add((AID) id.clone());
        }

        env.stamps = (ArrayList<ReceivedObject>) stamps.clone();

        if (from != null) {
            env.from = (AID) from.clone();
        }
        env.comments = comments;
        env.aclRepresentation = aclRepresentation;
        env.payloadLength = payloadLength;
        env.payloadEncoding = payloadEncoding;
        env.date = date;
        env.transportBehaviour = transportBehaviour;

        // Deep clone. Particularly important when security is enabled as SecurityObject-s (that are stored as
        // Envelope properties) are modified in the encryption process
        env.properties = new ArrayList<>(properties.size());
        for (int i = 0; i < properties.size(); i++) {
            Property p = properties.get(i);
            env.properties.add((Property) p.clone());
        }

        return env;
    }
    //#APIDOC_EXCLUDE_END


    //#MIDP_EXCLUDE_BEGIN

    // For persistence service
    private ArrayList<AID> getTo() {
        return to;
    }

    // For persistence service
    private void setTo(ArrayList<AID> al) {
        to = al;
    }

    // For persistence service
    private ArrayList<AID> getIntendedReceivers() {
        return intendedReceiver;
    }

    // For persistence service
    private void setIntendedReceivers(ArrayList<AID> al) {
        intendedReceiver = al;
    }

    // For persistence service
    private ArrayList<Property> getProperties() {
        return properties;
    }

    // For persistence service
    private void setProperties(ArrayList<Property> al) {
        properties = al;
    }

    //#MIDP_EXCLUDE_END

}
