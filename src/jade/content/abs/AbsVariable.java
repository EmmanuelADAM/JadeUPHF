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

import jade.content.schema.VariableSchema;

/**
 * An abstract descriptor that can hold a variable expression, i.e. an
 * entity that is not known yet.
 *
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsVariable extends AbsObjectImpl implements AbsTerm {
    // Easy way to access the Java class representing AbsVariable.
    // Useful in MIDP where XXX.class is not available
    private static Class<AbsVariable> absVariableClass = null;
    private boolean isMeta = false;

    /**
     * Construct an Abstract descriptor to hold a variable
     */
    public AbsVariable() {
        super(VariableSchema.BASE_NAME);
    }

    /**
     * Construct an AbsVariable with the given name and value type
     *
     * @param name      The name of the variable.
     * @param valueType The type of values that can be assigned to
     *                  this variable.
     */
    public AbsVariable(String name, String valueType) {
        super(VariableSchema.BASE_NAME);

        setName(name);
        setType(valueType);
    }

    public static Class<AbsVariable> getJavaClass() {
        if (absVariableClass == null) {
            try {
                absVariableClass = (Class<AbsVariable>) Class.forName("jade.content.abs.AbsVariable");
            } catch (Exception e) {
                // Should never happen
                e.printStackTrace();
            }
        }
        return absVariableClass;
    }

    /**
     * Gets the name of this variable.
     *
     * @return The name of this variable.
     */
    public String getName() {
        AbsPrimitive abs = (AbsPrimitive) getAbsObject(VariableSchema.NAME);
        if (abs != null) {
            return abs.getString();
        } else {
            return null;
        }
    }

    /**
     * Sets the name of this variable.
     *
     * @param name The new name of this variable.
     */
    public void setName(String name) {
        set(VariableSchema.NAME, AbsPrimitive.wrap(name));
    }

    /**
     * Gets the value type of this variable.
     *
     * @return The type of values that can be assigned to
     * this variable.
     */
    public String getType() {
        AbsPrimitive abs = (AbsPrimitive) getAbsObject(VariableSchema.VALUE_TYPE);
        if (abs != null) {
            return abs.getString();
        } else {
            return null;
        }
    }

    /**
     * Sets the value type of this variable.
     *
     * @param valueType The type of values that can be assigned to
     *                  this variable.
     */
    public void setType(String valueType) {
        set(VariableSchema.VALUE_TYPE, AbsPrimitive.wrap(valueType));
    }

    /**
     * Redefine the <code>isGrounded()</code> method in order to
     * always return <code>false</code>.
     */
    public boolean isGrounded() {
        return false;
    }

    /**
     * Return true if this object represents a meta term (i.e. symbol ??x) rather than a concrete variable.
     * This method is currently used only by the semantics framework.
     *
     * @return true if this object represents a meta term
     * @since JADE3.4
     **/
    public final boolean isMetaTerm() {
        return isMeta;
    }

    /**
     * Sets the value of isMetaTerm, where the default is false.
     * This method is currently used only by the semantics framework.
     *
     * @since JADE3.4
     **/
    public final void setIsMetaTerm(boolean isMeta) {
        this.isMeta = isMeta;
    }

    public int getAbsType() {
        return ABS_VARIABLE;
    }
}

