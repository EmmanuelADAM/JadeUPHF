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

import java.io.Serializable;
import java.util.Date;

/**
 * This class implements the  received-object   object from
 * the FIPA Agent Management ontology.
 *
 * @author Fabio Bellifemine - CSELT S.p.A.
 * @version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $
 * @see FIPAManagementOntology
 */
public class ReceivedObject implements Concept, Serializable {

    private String by;
    private String from;
    private Date date;
    private String id;
    private String via;


    /**
     * The constructor initializes the date to current time and all
     * the Strings to empty strings.
     **/
    public ReceivedObject() {
        date = new Date();
        by = "";
        from = "";
        id = "";
        via = "";
    }

    /**
     * Retrieve the  by   slot of this object. This slot
     * identifies the ACC that received the envelope containing this
     * object.
     *
     * @return The value of the  by   slot of this object,
     * or  null   if no value was set.
     */
    public String getBy() {
        return by;
    }

    /**
     * Set the  by   slot of this object.
     *
     * @param b The identifier for the ACC that received the envelope
     *          containing this object.
     */
    public void setBy(String b) {
        by = b;
    }

    /**
     * Retrieve the  from   slot of this object. This slot
     * identifies the ACC that sent the envelope containing this
     * object.
     *
     * @return The value of the  from   slot of this object,
     * or  null   if no value was set.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Set the  from   slot of this object.
     *
     * @param f The identifier for the ACC that sent the envelope
     *          containing this object.
     */
    public void setFrom(String f) {
        from = f;
    }

    /**
     * Retrieve the  date   slot of this object. This slot
     * identifies the date when the envelope containing this object
     * was sent.
     *
     * @return The value of the  date   slot of this object,
     * or  null   if no value was set.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Set the  date   slot of this object.
     *
     * @param d The date when the envelope containing this object was
     *          sent.
     */
    public void setDate(Date d) {
        date = d;
    }

    /**
     * Retrieve the  id   slot of this object. This slot
     * uniquely identifies the envelope containing this object.
     *
     * @return The value of the  id   slot of this object,
     * or  null   if no value was set.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the  id   slot of this object.
     *
     * @param i A unique id for the envelope containing this object.
     */
    public void setId(String i) {
        id = i;
    }

    /**
     * Retrieve the  via   slot of this object. This slot
     * describes the MTP over which the envelope containing this
     * object was sent.
     *
     * @return The value of the  via   slot of this
     * envelope, or  null   if no value was set.
     */
    public String getVia() {
        return via;
    }

    /**
     * Set the  via   slot of this object.
     *
     * @param v The name of the MTP over which the envelope containing
     *          this object was sent.
     */
    public void setVia(String v) {
        via = v;
    }

    /**
     * Retrieve a string representation for this received object.
     *
     * @return an SL0-like String representation of this object
     **/
    public String toString() {
        String s = "(ReceivedObject ";
        if (date != null)
            s = s + " :date " + date;
        if ((by != null) && (by.trim().length() > 0))
            s = s + " :by " + by;
        if ((from != null) && (from.trim().length() > 0))
            s = s + " :from " + from;
        if ((id != null) && (id.trim().length() > 0))
            s = s + " :id " + id;
        if ((via != null) && (via.trim().length() > 0))
            s = s + " :via " + via;
        return s;
    }


}