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
package jade.content.frame;

import java.util.Hashtable;

/**
 * Generic class representing all frames (such as concepts and
 * predicates) whose composing elements can be retrieved by a
 * unique name.
 *
 * @author Giovanni Caire - TILAB
 */
public class QualifiedFrame extends Hashtable<Object, Object> implements Frame {
    private final String typeName;

    /**
     * Create a QualifiedFrame with a given type-name.
     *
     * @param typeName The type-name of the QualifiedFrame to be created.
     */
    public QualifiedFrame(String typeName) {
        super();
        this.typeName = typeName;
    }

    /**
     * Retrieve the type-name of this QualifiedFrame.
     *
     * @return the type-name of this QualifiedFrame
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Redefine the put() method so that keys must be String and
     * setting a null value for a given key is interpreted as
     * removing the entry.
     *
     * @throws ClassCastException if  key   is not a String
     */
    public Object put(Object key, Object val) {
        if (val != null) {
            return super.put(key, val);
        } else {
            return remove(key);
        }
    }

    /**
     * Utility method to put a value of type  int   in this
     * Frame.
     */
    public Object putInteger(Object key, int val) {
        return put(key, (long) val);
    }

    /**
     * Utility method to retrieve a value of type  int   from this
     * Frame.
     */
    public int getInteger(Object key) {
        return (int) (((Long) get(key)).longValue());
    }

    /**
     * Utility method to put a value of type  boolean   in this
     * Frame.
     */
    public Object putBoolean(Object key, boolean val) {
        return put(key, val);
    }

    /**
     * Utility method to retrieve a value of type  boolean   from this
     * Frame.
     */
    public boolean getBoolean(Object key) {
        return (Boolean) get(key);
    }
}

