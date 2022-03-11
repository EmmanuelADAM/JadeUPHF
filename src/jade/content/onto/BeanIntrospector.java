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

package jade.content.onto;

import jade.content.Term;
import jade.content.abs.*;
import jade.content.schema.ObjectSchema;

import java.io.Serial;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

class BeanIntrospector implements Introspector {

    @Serial
    private static final long serialVersionUID = 6896919407513408002L;

    private static final String ENUM_SLOT_NAME = BeanOntologyBuilder.ENUM_SLOT_NAME;

    private final Map<SlotKey, SlotAccessData> accessors;

    BeanIntrospector() {
        accessors = new HashMap<>();
    }

    void addAccessors(Map<SlotKey, SlotAccessData> accessors) {
        this.accessors.putAll(accessors);
    }

    private Object invokeGetterMethod(Method method, Object obj) throws OntologyException {
        Object result;
        try {
            result = method.invoke(obj, (Object[]) null);
            if (result != null && Calendar.class.isAssignableFrom(result.getClass())) {
                // ontologically, Calendar is translated into a Date => convert the Calendar into a Date
                result = ((Calendar) result).getTime();
            }
        } catch (IllegalArgumentException iae) {
            result = new Object();
        } catch (Exception e) {
            throw new OntologyException("Error invoking getter method " + method.getName() + " on object " + obj, e);
        }
        return result;
    }

    private void invokeSetterMethod(Method method, Object obj, Object value, Class<?> clazz) throws OntologyException {
        try {
            if (Calendar.class.isAssignableFrom(clazz)) {
                // ontologically, Calendar is translated into a Date => convert the date back into a Calendar
                Calendar calendar = new GregorianCalendar();
                calendar.setTime((Date) value);
                value = calendar;
            } else {
                value = BasicOntology.adjustPrimitiveValue(value, clazz);
            }
            Object[] params = new Object[]{value};
            method.invoke(obj, params);
        } catch (Exception e) {
            throw new OntologyException("Error invoking setter method " + method.getName() + " on object " + obj + " with parameter " + value, e);
        }
    }

    public void checkClass(ObjectSchema schema, Class<?> javaClass, Ontology onto) {
        // FIXME not implemented yet
    }

    public Object getSlotValue(String slotName, Object obj, ObjectSchema schema) throws OntologyException {
        SlotAccessData slotAccessData = accessors.get(new SlotKey(schema.getTypeName(), slotName));
        if (slotAccessData == null) {
            throw new OntologyException("cannot retrieve a getter for slot " + slotName + ", class " + obj.getClass());
        }

        return invokeGetterMethod(slotAccessData.getter, obj);
    }

    public void setSlotValue(String slotName, Object slotValue, Object obj, ObjectSchema schema) throws OntologyException {
        SlotAccessData slotAccessData = accessors.get(new SlotKey(schema.getTypeName(), slotName));
        if (slotAccessData == null) {
            throw new OntologyException("cannot retrieve a setter for slot " + slotName + ", class " + obj.getClass());
        }

        invokeSetterMethod(slotAccessData.setter, obj, slotValue, slotAccessData.type);
    }

    public AbsAggregate externalizeAggregate(String slotName, Object slotValue, ObjectSchema schema, Ontology referenceOnto) throws OntologyException {
        if (slotValue == null) {
            // This slot isn't an aggregate
            throw new NotAnAggregate();
        }

        AbsAggregate absAggregate = null;
        Class<?> valueClass = slotValue.getClass();

        // Check if slot is typized
        boolean slotTypized = false;
        if (schema != null) {
            SlotAccessData slotAccessData = accessors.get(new SlotKey(schema.getTypeName(), slotName));
            slotTypized = slotAccessData.isTypized();
        }

        // Try to manage as array
        if (valueClass.isArray() && valueClass != byte[].class) {
            // In the case of array and slot not typized --> throw exception
            // (Only java collection are permitted)
            if (!slotTypized) {
                throw new OntologyException("Impossible manage array into a not typized slot");
            }

            absAggregate = new AbsAggregate(BasicOntology.SEQUENCE);
            for (int i = 0; i < Array.getLength(slotValue); i++) {
                Object object = Array.get(slotValue, i);
                absAggregate.add((AbsTerm) Ontology.externalizeSlotValue(object, this, referenceOnto));
            }
        } else {
            Iterator<?> iter;
            String aggregateType;
            if (slotValue instanceof Collection) {
                iter = ((Collection<?>) slotValue).iterator();
                if (slotValue instanceof List) {
                    aggregateType = BasicOntology.SEQUENCE;
                } else {
                    aggregateType = BasicOntology.SET;
                }
            } else {
                // This slot isn't an aggregate
                throw new NotAnAggregate();
            }
            if (iter.hasNext() || Objects.requireNonNull(schema).isMandatory(slotName)) {
                absAggregate = new AbsAggregate(aggregateType);
                try {
                    while (iter.hasNext()) {
                        Object object = iter.next();
                        // Do not call Ontology.fromObject directly since each element in the aggregate may be an aggregate itself
                        absAggregate.add((AbsTerm) Ontology.externalizeSlotValue(object, this, referenceOnto));
                    }
                } catch (ClassCastException cce) {
                    throw new OntologyException("Non term object in aggregate");
                }
            }
        }

        return absAggregate;
    }

    public Object internalizeAggregate(String slotName, AbsAggregate absAggregate, ObjectSchema schema, Ontology referenceOnto) throws OntologyException {
        SlotAccessData slotAccessData = accessors.get(new SlotKey(schema.getTypeName(), slotName));
        if (slotAccessData == null) {
            Class<?> containerClazz = referenceOnto.getClassForElement(schema.getTypeName());
            throw new OntologyException("cannot retrieve a setter for slot " + slotName + ", class " + containerClazz);
        }

        Class<?> elementClazz = slotAccessData.aggregateClass;
        Class<?> aggregateClass = slotAccessData.type;

        if (aggregateClass == null ||
                aggregateClass == Object.class ||
                aggregateClass == Term.class ||
                aggregateClass == Collection.class) {
            // Aggregate not typized -> use default class

            if (BasicOntology.SEQUENCE.equals(absAggregate.getTypeName())) {
                aggregateClass = ArrayList.class;
            } else {
                aggregateClass = HashSet.class;
            }
        }

        Object result;
        Iterator<AbsTerm> iterator = absAggregate.iterator();
        try {
            if (aggregateClass.isArray()) {
                int index = 0;
                result = Array.newInstance(elementClazz, absAggregate.size());
                while (iterator.hasNext()) {
                    Array.set(result, index, Ontology.internalizeSlotValue(iterator.next(), this, referenceOnto));
                    index++;
                }
            } else if (Collection.class.isAssignableFrom(aggregateClass)) {
                Collection<Object> javaCollection = AggregateHelper.createConcreteJavaCollection(aggregateClass);
                if (javaCollection == null) {
                    throw new OntologyException("cannot create a concrete collection for class " + aggregateClass.getName());
                }
                result = javaCollection;
                while (iterator.hasNext()) {
                    javaCollection.add(Ontology.internalizeSlotValue(iterator.next(), this, referenceOnto));
                }
            } else if (Collection.class.isAssignableFrom(aggregateClass)) {
                Collection<Object> jadeCollection = AggregateHelper.createConcreteJadeCollection(aggregateClass);
                result = jadeCollection;
                while (iterator.hasNext()) {
                    jadeCollection.add(Ontology.internalizeSlotValue(iterator.next(), this, referenceOnto));
                }
            } else {
                throw new OntologyException("don't know how to handle aggregate slot of class " + aggregateClass.getName());
            }
        } catch (InstantiationException ie) {
            throw new OntologyException("cannot instantiate aggregate slot of non-concrete class " + aggregateClass.getName(), ie);
        } catch (IllegalAccessException iae) {
            throw new OntologyException("cannot instantiate aggregate slot through unaccessible default constructor of class " + aggregateClass.getName(), iae);
        }
        return result;
    }

    public AbsObject externalizeSpecialType(Object obj, ObjectSchema schema, Class<?> javaClass, Ontology referenceOnto) throws OntologyException {
        if (!javaClass.isEnum()) {
            throw new NotASpecialType();
        }

        AbsObject abs = schema.newInstance();
        AbsHelper.setAttribute(abs, ENUM_SLOT_NAME, AbsPrimitive.wrap(obj.toString()));
        return abs;
    }

    public Enum internalizeSpecialType(AbsObject abs, ObjectSchema schema, Class javaClass, Ontology referenceOnto) throws OntologyException {
        if (!javaClass.isEnum()) {
            throw new NotASpecialType();
        }

        AbsPrimitive absEnumValue = (AbsPrimitive) abs.getAbsObject(ENUM_SLOT_NAME);
        String strEnumValue = absEnumValue.getString();
        return Enum.valueOf(javaClass, strEnumValue);
    }
}
