/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * <p>
 * GNU Lesser General Public License
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content.abs;


import java.io.Serializable;

/**
 * The common ancestor of all abstract descriptors
 * @author Federico Bergenti - Universita` di Parma
 * @author Giovanni Caire - TILAB
 */
public interface AbsObject extends Serializable {
    int UNKNOWN = -1;
    int ABS_PREDICATE = 1;
    int ABS_CONCEPT = 2;
    int ABS_AGENT_ACTION = 3;
    int ABS_PRIMITIVE = 4;
    int ABS_AGGREGATE = 5;
    int ABS_IRE = 6;
    int ABS_VARIABLE = 7;
    int ABS_CONTENT_ELEMENT_LIST = 8;
    int ABS_CONCEPT_SLOT_FUNCTION = 9;

    /**
     * @return The name of the type of the object held by this
     * abstract descriptor.
     */
    String getTypeName();

    /**
     * Gets the value of an attribute of the object held by this
     * abstract descriptor.
     * @param name The name of the attribute.
     * @return value The value of the attribute.
     */
    AbsObject getAbsObject(String name);

    /**
     * @return the name of all attributes.
     */
    String[] getNames();

    /**
     * Tests if the object is grounded, i.e., if no one of its attributes 
     * is associated with a variable
     * @return <code>true</code> if the object is grounded.
     */
    boolean isGrounded();

    /**
     * Gets the number of attributes.
     * @return the number of attributes.
     */
    int getCount();

    int getAbsType();
}

