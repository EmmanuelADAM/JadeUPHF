/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */


package jade.imtp.leap;

/**
 * Serializer interface.
 * This interface must be implemented to allow custom classes
 * to be automatically serialized by LEAP IMTP.
 *
 * @author Giovanni Caire (TILAB)
 * @author Jerome Picault (Motorola Labs)
 */
interface Serializer {
    byte ACL_ID = 0;
    byte AID_ID = 1;
    byte AIDARRAY_ID = 2;
    byte BOOLEAN_ID = 3;
    byte COMMAND_ID = 4;
    byte CONTAINERID_ID = 5;
    byte CONTAINERIDARRAY_ID = 6;
    byte DATE_ID = 7;
    byte DEFAULT_ID = 8;
    byte INTEGER_ID = 9;
    byte NODEDESCRIPTOR_ID = 10;
    byte STRING_ID = 11;
    byte STRINGARRAY_ID = 12;
    byte VECTOR_ID = 13;
    byte MTPDESCRIPTOR_ID = 14;
    byte NODE_ID = 15;
    byte NODEARRAY_ID = 16;
    byte ENVELOPE_ID = 17;
    byte ARRAYLIST_ID = 18;
    byte BYTEARRAY_ID = 19;
    byte PROPERTIES_ID = 20;
    byte RECEIVEDOBJECT_ID = 21;
    byte JICPADDRESS_ID = 22;
    byte HTTPADDRESS_ID = 23;
    byte DUMMYCERTIFICATE_ID = 24;
    byte DUMMYPRINCIPAL_ID = 25;
    byte NODESTUB_ID = 27;
    byte HORIZONTALCOMMAND_ID = 28;
    byte THROWABLE_ID = 29;
    byte PROPERTY_ID = 30;
    byte SERIALIZABLE_ID = 31;
    byte SERVICEDESCRIPTOR_ID = 32;
    byte SERVICESLICEPROXY_ID = 33;
    byte SLICEPROXY_ID = 36;
    byte PLATFORMMANAGER_ID = 34;
    byte GENERICMESSAGE_ID = 35;
    byte MULTIPLEGENERICMESSAGE_ID = 37;

    /**
     * This method serializes an object according to the LEAP
     * serialization mechanism.
     *
     * @param obj   the object to be serialized
     * @param ddout the output stream where to serialize the object into
     * @throws LEAPSerializationException if an error occurs during
     *                                    serialization
     */
    void serialize(Object obj, DeliverableDataOutputStream ddout) throws LEAPSerializationException;

    /**
     * This method deserializes an object according to the LEAP
     * serialization mechanism.
     *
     * @param ddin the input stream wher to deserialize the object from
     * @return the deserialized object
     * @throws LEAPSerializationException if an error occurs during
     *                                    deserialization
     */
    Object deserialize(DeliverableDataInputStream ddin) throws LEAPSerializationException;
}

