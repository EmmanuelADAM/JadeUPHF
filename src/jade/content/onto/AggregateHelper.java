/*
 WADE - Workflow and Agent Development Environment is a framework to develop 
 multi-agent systems able to execute tasks defined according to the workflow
 metaphor.
 Copyright (C) 2008 Telecom Italia S.p.A. 

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

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.content.schema.AggregateSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.TermSchema;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class AggregateHelper {

    private static final int ACC_ABSTRACT = 0x0400;
    private static final int ACC_INTERFACE = 0x0200;

    /**
     * Get ontology schema associated to class
     * Try to manage as aggregate
     *
     * @param clazz         class to get schema
     * @param elementSchema aggregate element schema
     * @return associated class schema
     */
    public static ObjectSchema getSchema(Class<?> clazz, TermSchema elementSchema) {
        ObjectSchema schema = null;

        // Sequence type
        if (List.class.isAssignableFrom(clazz) ||
                List.class.isAssignableFrom(clazz) ||
                (clazz.isArray() && clazz != byte[].class)) {

            schema = new AggregateSchema(BasicOntology.SEQUENCE, elementSchema);
        }

        // Set type
        else if (Set.class.isAssignableFrom(clazz) ||
                Set.class.isAssignableFrom(clazz)) {

            schema = new AggregateSchema(BasicOntology.SET, elementSchema);
        }

        return schema;
    }

    /**
     * Try to convert, if possible, the aggregate value srcValue into an instance of destClass
     * Possible source and destination classes are java array, java collection and jade collection
     * //* @throws Exception
     */
    public static Object adjustAggregateValue(Object srcValue, Class<?> destClass) throws Exception {
        Object destValue = srcValue;
        if (srcValue != null) {
            Class<?> srcClass = srcValue.getClass();
            if (srcClass != destClass) {

                // Destination is an array
                if (destClass.isArray()) {

                    // Source is a java collection
                    if (Collection.class.isAssignableFrom(srcClass)) {
                        Collection<Object> javaCollection = (Collection<Object>) srcValue;
                        destValue = collectionToArray(javaCollection.iterator(), destClass.getComponentType(), javaCollection.size());
                    }

                    // Source is a jade collection
                    else if (Collection.class.isAssignableFrom(srcClass)) {
                        Collection<Object> jadeCollection = (Collection<Object>) srcValue;
                        destValue = collectionToArray(jadeCollection.iterator(), destClass.getComponentType(), jadeCollection.size());
                    }
                }

                // Destination is a java collection
                else if (Collection.class.isAssignableFrom(destClass)) {

                    // Source is an array
                    if (srcClass.isArray()) {
                        Collection<Object> javaCollection = createConcreteJavaCollection(destClass);
                        int size = Array.getLength(srcValue);
                        for (int index = 0; index < size; index++) {
                            javaCollection.add(Array.get(srcValue, index));
                        }
                        destValue = javaCollection;
                    }

                    // Source is a jade collection
                    else if (Collection.class.isAssignableFrom(srcClass)) {
                        Collection<Object> javaCollection = createConcreteJavaCollection(destClass);
                        Collection<?> jadeCollection = (Collection<?>) srcValue;
                        for (Collection<?> collection : (Iterable<Collection<?>>) jadeCollection) {
                            javaCollection.add(collection);
                        }
                        destValue = javaCollection;
                    }
                }

                // Destination is a jade collection
                else if (Collection.class.isAssignableFrom(destClass)) {

                    // Source is an array
                    if (srcClass.isArray()) {
                        Collection<Object> jadeCollection = createConcreteJadeCollection(destClass);
                        int size = Array.getLength(srcValue);
                        for (int index = 0; index < size; index++) {
                            jadeCollection.add(Array.get(srcValue, index));
                        }
                        destValue = jadeCollection;
                    }

                    // Source is a java collection
                    else if (Collection.class.isAssignableFrom(srcClass)) {
                        Collection<Object> jadeCollection = createConcreteJadeCollection(destClass);
                        Collection<?> javaCollection = (Collection<?>) srcValue;
                        jadeCollection.addAll(javaCollection);
                        destValue = jadeCollection;
                    }
                }
            }
        }
        return destValue;
    }

    private static Object collectionToArray(Iterator<Object> it, Class<?> componentTypeClass, int size) {
        int index = 0;
        Object array = Array.newInstance(componentTypeClass, size);
        while (it.hasNext()) {
            Object item = it.next();
            Array.set(array, index, item);
            index++;
        }
        return array;
    }

    static Collection<Object> createConcreteJavaCollection(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        int modifiers = clazz.getModifiers();
        Collection<Object> result = null;
        if ((modifiers & ACC_ABSTRACT) == 0 && (modifiers & ACC_INTERFACE) == 0) {
            // class is concrete, we can instantiate it directly
            try {
                result = (Collection<Object>) clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        } else {
            // class is either abstract or an interface, we have to somehow choose a concrete collection :-(
            if (List.class.isAssignableFrom(clazz)) {
                result = new ArrayList<>();
            } else if (Set.class.isAssignableFrom(clazz)) {
                result = new HashSet<>();
            }
        }
        return result;
    }

    static Collection<Object> createConcreteJadeCollection(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        int modifiers = clazz.getModifiers();
        Collection<Object> result = null;
        if ((modifiers & ACC_ABSTRACT) == 0 && (modifiers & ACC_INTERFACE) == 0) {
            // class is concrete, we can instantiate it directly
            try {
                result = (Collection<Object>) clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // class is either abstract or an interface, we have to somehow choose a concrete collection :-(
            if (List.class.isAssignableFrom(clazz)) {
                result = new ArrayList<>();
            } else if (Set.class.isAssignableFrom(clazz)) {
                result = new HashSet<>();
            }
        }
        return result;
    }
}
