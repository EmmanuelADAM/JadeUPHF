/*
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
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
package jade.content.onto;

//#J2ME_EXCLUDE_BEGIN

import jade.content.Concept;
import jade.content.abs.AbsAggregate;
import jade.content.abs.AbsHelper;
import jade.content.abs.AbsObject;
import jade.content.schema.*;
import jade.content.schema.facets.CardinalityFacet;
import jade.content.schema.facets.DefaultValueFacet;
import jade.content.schema.facets.PermittedValuesFacet;
import jade.content.schema.facets.RegexFacet;
import jade.util.Logger;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;

/**
 * An application-specific ontology describes the elements that agents
 * can use within content of messages. It defines a vocabulary and
 * relationships between the elements in such a vocabulary.
 * The relationships can be:
 * <ul>
 * <li>structural, e.g., the predicate  fatherOf   accepts two
 *     parameters, a father and a set of children;
 * <li>semantic, e.g., a concept of class  Man   is also of class
 *      Person  .
 * </ul>
 * Application-specific ontologies are implemented through objects
 * of class  Ontology  .<br>
 * An ontology is characterized by:
 * <ul>
 * <li>one name;
 * <li>one (or more) base ontology that it extends;
 * <li>a set of <i>element schemas</i>.
 * </ul>
 * Element schemas are objects describing the structure of concepts, actions,
 * and predicates that are allowed in messages. For example,
 *  People   ontology contains an element schema called
 *  Person  . This schema states that a  Person   is
 * characterized by a  name   and by an  address  :
 *
 * ConceptSchema personSchema = new ConceptSchema(PERSON);
 * personSchema.addSlot(NAME,    stringSchema);
 * personSchema.addSlot(ADDRESS, addressSchema, ObjectSchema.OPTIONAL);
 *   
 * where  PERSON  ,  NAME   and  ADDRESS   are
 * string constants. When you register your schema with the ontology, such
 * constants become part of the vocabulary of the ontology.<br>
 * Schemas that describe concepts support inheritance. You can define the
 * concept  Man   as a refinement of the concept  Person  :
 *
 * ConceptSchema manSchema = new ConceptSchema(MAN);
 * manSchema.addSuperSchema(personSchema);
 *   
 * Each element schema can be associated with a Java class to map elements of
 * the ontology that comply with a schema with Java objects of that class. The
 * following is a class that might be associated with the  Person  
 * schema:
 *
 * public class Person extends Concept {
 *       private String  name    = null;
 *       private Address address =  null;
 *       public void setName(String name) {
 *               this.name = name;
 *       }
 *       public void setAddress(Address address) {
 *               this.address = address;
 *       }
 *       public String getName() {
 *               return name;
 *       }
 *       public Address getAddress() {
 *               return address;
 *       }
 * }
 *   
 * When sending/receiving messages you can represent your content in terms of
 * objects belonging to classes that the ontology associates with schemas.<br>
 * As the previous example suggests, you cannot use objects of class
 *  Person   when asking for the value of some attribute, e.g., when
 * asking for the value of  address  . Basically, the problem is that
 * you cannot 'assign' a variable to an attribute of an object, i.e.
 * you cannot write something like:
 *  person.setName(new Variable("X"))  .<br>
 * In order to solve this problem, you can describe your content in terms of
 * <i>abstract descriptors</i>. An abstract descriptor is an
 * object that reifies an element of the ontology.
 * The following is the creation of an abstract
 * descriptor for a concept of type  Man  :
 *
 * AbsConcept absMan = new AbsConcept(MAN);
 * absMan.setSlot(NAME,    "John");
 * absMan.setSlot(ADDRESS, absAddress);
 *   
 * where  absAddress   is the abstract descriptor for John's
 * address:
 *
 * AbsConcept absAddress = new AbsConcept(ADDRESS);
 * absAddress.setSlot(CITY, "London");
 *   
 * Objects of class  Ontology   allows you to:
 * <ul>
 * <li>register schemas with associated (i) a mandatory term of the
 *     vocabulary e.g. `NAME` and (ii) an optional Java class,
 *     e.g. `Person`;
 * <li>retrieve the registered information through various keys.
 * </ul>
 * The framework already provides the  BasicOntology   ontology
 * that provides all basic elements, i.e. primitive data types, aggregate
 * types, etc.
 * Application-specific ontologies should be implemented extending it.
 *
 * @author Federico Bergenti - Universita` di Parma
 * @author Giovanni Caire - TILAB
 * @see Concept
 * @see jade.content.abs.AbsConcept
 * @see ConceptSchema
 * @see BasicOntology
 */
public class Ontology implements Serializable {
    private static final String DEFAULT_INTROSPECTOR_CLASS = "jade.content.onto.ReflectiveIntrospector";
    // This is required for compatibility with CLDC MIDP where XXX.class
    // is not supported
    private static Class<?> absObjectClass = null;

    static {
        try {
            absObjectClass = Class.forName("jade.content.abs.AbsObject");
        } catch (Exception e) {
            // Should never happen
            e.printStackTrace();
        }
    }

    private final Ontology[] base;
    private final String name;
    private final Hashtable<String, ObjectSchema> elements = new Hashtable<>(); // Maps type-names to schemas
    private final Hashtable<String, Class<?>> classes = new Hashtable<>(); // Maps type-names to java classes
    private final Hashtable<Class<?>, ObjectSchema> schemas = new Hashtable<>(); // Maps java classes to schemas
    private final Logger logger = Logger.getMyLogger(this.getClass().getName());
    private Introspector introspector;
    // We use an Hashtable as if it was a Set
    private Hashtable<String, String> conceptSlots;

    /**
     * Construct an Ontology object with a given  name  
     * that extends a given ontology.
     * The  ReflectiveIntrospector   is used by default to
     * convert between Java objects and abstract descriptors.
     *
     * @param name The identifier of the ontology.
     * @param base The base ontology.
     */
    public Ontology(String name, Ontology base) {
        this(name, base, null);
        try {
            introspector = (Introspector) Class.forName(DEFAULT_INTROSPECTOR_CLASS).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Class " + DEFAULT_INTROSPECTOR_CLASS + "for default Introspector not found");
        }
    }

    /**
     * Construct an Ontology object with a given  name  
     * that uses a given Introspector to
     * convert between Java objects and abstract descriptors.
     *
     * @param name         The identifier of the ontology.
     * @param introspector The introspector.
     */
    public Ontology(String name, Introspector introspector) {
        this(name, new Ontology[0], introspector);
    }

    /**
     * Construct an Ontology object with a given  name  
     * that extends a given ontology and that uses a given Introspector to
     * convert between Java objects and abstract descriptors.
     *
     * @param name         The identifier of the ontology.
     * @param base         The base ontology.
     * @param introspector The introspector.
     */
    public Ontology(String name, Ontology base, Introspector introspector) {
        this(name, (base != null ? new Ontology[]{base} : new Ontology[0]), introspector);
    }

    /**
     * Construct an Ontology object with a given  name  
     * that extends a given set of ontologies and that uses a given Introspector to
     * convert between Java objects and abstract descriptors.
     *
     * @param name         The identifier of the ontology.
     * @param base         The base ontology.
     * @param introspector The introspector.
     */
    public Ontology(String name, Ontology[] base, Introspector introspector) {
        this.name = name;
        this.introspector = introspector;
        this.base = (base != null ? base : new Ontology[0]);
    }

    /**
     * Check whether a given object is a valid term.
     * If it is an Aggregate (i.e. a  List  ) it also check
     * the elements.
     *
     * @throws OntologyException if the given object is not a valid term
     */
    public static void checkIsTerm(Object obj) throws OntologyException {
        // FIXME: This method is likely to be removed as it does not add any value and creates problems
        // when using the Serializable Ontology
		/*if (obj instanceof String ||
    		  obj instanceof Boolean ||
    		  obj instanceof Integer ||
    		  obj instanceof Long ||
    		  //#MIDP_EXCLUDE_BEGIN
    		  obj instanceof Float ||
    		  obj instanceof Double ||
    		  //#MIDP_EXCLUDE_END
    		  obj instanceof Date ||
    		  obj instanceof Term) {
    		return;
    	}
    	if (obj instanceof List) {
    		Iterator it = ((List) obj).iterator();
    		while (it.hasNext()) {
    			checkIsTerm(it.next());
    		}
    		return;
    	}

    	// If we reach this point the object is not a term
    	throw new OntologyException("Object "+obj+" of class "+obj.getClass().getName()+" is not a term");
		 */
    }

    public static AbsObject externalizeSlotValue(Object obj, Introspector introspector, Ontology referenceOnto) throws OntologyException {
        try {
            return introspector.externalizeAggregate(null, obj, null, referenceOnto);
        } catch (NotAnAggregate nan) {

            return referenceOnto.fromObject(obj);
        }
    }

    public static Object internalizeSlotValue(AbsObject abs, Introspector introspector, Ontology referenceOnto) throws OntologyException {
        if (abs.getAbsType() == AbsObject.ABS_AGGREGATE) {
            return introspector.internalizeAggregate(null, (AbsAggregate) abs, null, referenceOnto);
        }

        return referenceOnto.toObject(abs);
    }

    //#J2ME_EXCLUDE_BEGIN
    private static void addReferencedSchemas(ObjectSchema schema, List<ObjectSchema> schemas) throws OntologyException {
        ObjectSchema[] superSchemas = schema.getSuperSchemas();
        for (ObjectSchema superSchema : superSchemas) {
            addReferencedSchemas(superSchema, schemas);
        }

        if (schema instanceof AggregateSchema) {
            ObjectSchema elementsSchema = ((AggregateSchema) schema).getElementsSchema();
            if (elementsSchema != null) {
                addReferencedSchemas(elementsSchema, schemas);
            }
        } else if (schema instanceof ConceptSchema) {
            if (!schemas.contains(schema)) {
                schemas.add(schema);
            }

            for (String slotName : schema.getNames()) {
                ObjectSchema slotSchema = schema.getSchema(slotName);
                addReferencedSchemas(slotSchema, schemas);
            }
        }
    }

    public static List<ObjectSchema> getReferencedSchemas(ObjectSchema rootSchema) throws OntologyException {
        List<ObjectSchema> schemas = new ArrayList<>();
        addReferencedSchemas(rootSchema, schemas);
        return schemas;
    }

    public static boolean isBaseOntology(Ontology[] oo, String name) {
        if (oo != null) {
            for (Ontology o : oo) {
                if (o.getName().equals(name)) {
                    return true;
                } else if (isBaseOntology(o.base, name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves the name of this ontology.
     *
     * @return the name of this ontology.
     */
    public String getName() {
        return name;
    }

    public Introspector getIntrospector() {
        return introspector;
    }

    /**
     * Adds a schema to this ontology
     *
     * @param schema The schema to add
     */
    public void add(ObjectSchema schema) throws OntologyException {
        add(schema, null);
    }

    /**
     * Adds a schema to the ontology and associates it to the class
     *  javaClass  
     *
     * @param schema    the schema.
     * @param javaClass the concrete class.
     */
    public void add(ObjectSchema schema, Class<?> javaClass) throws OntologyException {
        if (schema.getTypeName() == null) {
            throw new OntologyException("Invalid schema identifier");
        }

        String s = schema.getTypeName().toLowerCase();
        elements.put(s, schema);

        if (javaClass != null) {
            classes.put(s, javaClass);
            if (!absObjectClass.isAssignableFrom(javaClass)) {
                if (introspector != null) {
                    introspector.checkClass(schema, javaClass, this);
                }
                schemas.put(javaClass, schema);
            } else {
                // If the java class is an abstract descriptor check the
                // coherence between the schema and the abstract descriptor
                if (!javaClass.isInstance(schema.newInstance())) {
                    throw new OntologyException("Java class " + javaClass.getName() + " can't represent instances of schema " + schema);
                }
            }
        }
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     * Retrieves the schema of element  name   in this ontology.
     * The search is extended to the base ontologies if the schema is not
     * found.
     *
     * @param name the name of the schema in the vocabulary.
     * @return the schema or  null   if the schema is not found.
     */
    public ObjectSchema getSchema(String name) throws OntologyException {
        if (name == null) {
            throw new OntologyException("Null schema identifier");
        }

        ObjectSchema ret = elements.get(name.toLowerCase());

        if (ret == null) {
            // Check if a ConceptSlotFunctionSchema must be returned
            if ("BC-Ontology".equals(getName())) {
                System.out.println("Searching for schema " + name);
            }
            if (conceptSlots != null && conceptSlots.containsKey(name)) {
                return new ConceptSlotFunctionSchema(name);
            }

            if (logger.isLoggable(Logger.FINE))
                logger.log(Logger.FINE, "Ontology " + getName() + ". Schema for " + name + " not found");
            for (int i = 0; i < base.length; ++i) {
                if (base[i] == null) {
                    if (logger.isLoggable(Logger.FINE))
                        logger.log(Logger.FINE, "Base ontology # " + i + " for ontology " + getName() + " is null");
                } else {
                    ret = base[i].getSchema(name);
                    if (ret != null) {
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Retrieves the schema associated to a given class in this ontology.
     * The search is extended to the base ontologies if the schema is not
     * found.
     *
     * @param clazz the class whose associated schema must be retrieved.
     * @return the schema associated to the given class or  null   if the schema is not found.
     */
    public ObjectSchema getSchema(Class<?> clazz) throws OntologyException {
        if (clazz == null) {
            throw new OntologyException("Null class");
        }
        ObjectSchema ret = schemas.get(clazz);
        if (ret == null) {
            if (logger.isLoggable(Logger.FINE))
                logger.log(Logger.FINE, "Ontology " + getName() + ". Schema for class " + clazz + " not found");
            for (int i = 0; i < base.length; ++i) {
                if (base[i] == null) {
                    if (logger.isLoggable(Logger.FINE))
                        logger.log(Logger.FINE, "Base ontology # " + i + " for ontology " + getName() + " is null");
                } else {
                    ret = base[i].getSchema(clazz);
                    if (ret != null) {
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Converts an abstract descriptor to a Java object of the proper class.
     *
     * @param abs the abstract descriptor.
     * @return the object
     * @throws UngroundedException if the abstract descriptor contains a
     *                             variable
     * @throws OntologyException   if some mismatch with the schema is found
     * @see #fromObject(Object)
     */
    public Object toObject(AbsObject abs) throws OntologyException, UngroundedException {
        if (abs == null) {
            return null;
        }

        try {
            return toObject(abs, abs.getTypeName().toLowerCase(), this);
        } catch (UnknownSchemaException use) {
            // If we get this exception here, the schema is globally unknown
            // (i.e. is unknown in the reference ontology and all its base
            // ontologies) --> throw a generic OntologyException
            throw new OntologyException("No schema found for type " + abs.getTypeName());
        } catch (OntologyException oe) {
            // This exception may have been thrown due to the fact that the Abs descriptor is
            // ungrounded. In this case an UngroundedException must be thrown.
            // Note that we don't check ungrouding before to speed up performances
            if (!abs.isGrounded()) {
                throw new UngroundedException();
            } else {
                throw oe;
            }
        }
    }

    /**
     * Converts a Java object into a proper abstract descriptor.
     *
     * @param obj the object
     * @return the abstract descriptor.
     * @throws OntologyException if some mismatch with the schema is found
     * @see #toObject(AbsObject)
     */
    public AbsObject fromObject(Object obj) throws OntologyException {
        if (obj == null) {
            return null;
        }

        try {
            return fromObject(obj, this);
        } catch (UnknownSchemaException use) {
            // If we get this exception here, the schema is globally unknown
            // (i.e. is unknown in the reference ontology and all its base
            // ontologies) --> throw a generic OntologyException
            throw new OntologyException("No schema found for class " + obj.getClass().getName());
        }

    }

    /**
     * Retrieves the concrete class associated with element  name  
     * in this ontology. The search is extended to the base ontologies
     *
     * @param name the name of the schema.
     * @return the Java class or null if no schema called  name  
     * is found or if no class is associated to that schema.
     * @throws OntologyException if name is null
     */
    public Class<?> getClassForElement(String name) throws OntologyException {
        if (name == null) {
            throw new OntologyException("Null schema identifier");
        }

        Class<?> ret = classes.get(name.toLowerCase());

        if (ret == null) {
            for (Ontology ontology : base) {
                ret = ontology.getClassForElement(name);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return ret;
    }

    /**
     * Retrieves the ontology actually containing the definition of a given schema.
     * This can be the ontology itself or one of its super-ontologies.
     *
     * @param lcName The lower-case version of the name of the schema whose defining ontology must be retrieved
     * @return The ontology actually containing the definition of schema  lcName   or null if such schema
     * is not defined neither in this ontology nor in one of its super-ontologies
     */
    private Ontology getDefiningOntology(String lcName) {
        Ontology definingOntology = null;
        if (elements.containsKey(lcName)) {
            definingOntology = this;
        } else {
            for (Ontology ontology : base) {
                definingOntology = ontology.getDefiningOntology(lcName);
                if (definingOntology != null) {
                    break;
                }
            }
        }
        return definingOntology;
    }

    /**
     * Converts an abstract descriptor to a Java object of the proper class.
     *
     * @param abs        the abstract descriptor.
     * @param lcType     the type of the abstract descriptor to be translated
     *                   aconverted into lower case. This is passed as parameters to avoid
     *                   making the conversion to lower case for each base ontology.
     * @param globalOnto The ontology this ontology is part of (i.e. the
     *                   ontology that extends this ontology).
     * @return the object
     * @throws UnknownSchemaException If no schema for the abs descriptor
     *                                to be translated is defined in this ontology.
     * @throws UngroundedException    if the abstract descriptor contains a
     *                                variable
     * @throws OntologyException      if some mismatch with the schema is found      * ontology. In this case UnknownSchema
     */
    protected Object toObject(AbsObject abs, String lcType, Ontology globalOnto) throws UnknownSchemaException, UngroundedException, OntologyException {
        if (logger.isLoggable(Logger.FINE))
            logger.log(Logger.FINE, "Ontology " + getName() + ". Translating ABS descriptor " + abs);

        // Retrieve the schema
        ObjectSchema schema = elements.get(lcType);
        if (schema != null) {
            if (logger.isLoggable(Logger.FINE))
                logger.log(Logger.FINE, "Ontology " + getName() + ". Schema for type " + abs.getTypeName() + " found locally: " + schema);

            // Retrieve the java class
            Class<?> javaClass = classes.get(lcType);
            if (javaClass == null) {
                throw new OntologyException("No java class associated to type " + abs.getTypeName());
            }
            if (logger.isLoggable(Logger.FINE))
                logger.log(Logger.FINE, "Ontology " + getName() + ". Class for type " + abs.getTypeName() + " = " + javaClass.getName());

            // If the Java class is an Abstract descriptor --> just return abs
            if (absObjectClass.isAssignableFrom(javaClass)) {
                return abs;
            }

            try {
                // Try to manage as special type
                Object obj = null;
                try {
                    obj = internalizeSpecialType(abs, schema, javaClass, globalOnto);
                } catch (NotASpecialType nasp) {
                    // Manage as structure slot
                    obj = javaClass.newInstance();
                    internalize(abs, obj, schema, globalOnto);
                }
                return obj;
            } catch (OntologyException oe) {
                // Let the exception pass through
                throw oe;
            } catch (InstantiationException ie) {
                throw new OntologyException("Class " + javaClass + " can't be instantiated", ie);
            } catch (IllegalAccessException iae) {
                throw new OntologyException("Class " + javaClass + " does not have an accessible constructor", iae);
            }
        }

        // If we get here --> This ontology is not able to translate abs
        // --> Try to convert it using the base ontologies
        for (Ontology ontology : base) {
            try {
                return ontology.toObject(abs, lcType, globalOnto);
            } catch (UnknownSchemaException use) {
                // Try the next one
            }
        }

        throw new UnknownSchemaException();
    }

    private Object internalizeSpecialType(AbsObject abs, ObjectSchema schema, Class<?> javaClass, Ontology globalOnto) throws OntologyException {
        if (introspector == null) {
            throw new NotASpecialType();
        }
        return introspector.internalizeSpecialType(abs, schema, javaClass, globalOnto);
    }

    /**
     * Internalize (abs --> obj) the slots defined in  schema   and its super-schemas
     */
    protected void internalize(AbsObject abs, Object obj, ObjectSchema schema, Ontology globalOnto) throws OntologyException {
        // Let the proper ontology manage slots defined in super schemas if any
        ObjectSchema[] superSchemas = schema.getSuperSchemas();
        for (ObjectSchema superSchema : superSchemas) {
            Ontology definingOntology = getDefiningOntology(superSchema.getTypeName().toLowerCase());
            if (definingOntology != null) {
                definingOntology.internalize(abs, obj, superSchema, globalOnto);
            }
        }

        // Finally manage "local" slots through the introspector
        if (introspector != null) {
            String[] names = schema.getOwnNames();
            for (String slotName : names) {
                AbsObject absSlotValue = abs.getAbsObject(slotName);
                if (absSlotValue != null) {
                    Object slotValue = null;
                    if (absSlotValue.getAbsType() == AbsObject.ABS_AGGREGATE) {
                        // Manage as aggregate
                        slotValue = introspector.internalizeAggregate(slotName, (AbsAggregate) absSlotValue, schema, globalOnto);
                    } else {
                        // Manage as normal slot
                        slotValue = globalOnto.toObject(absSlotValue);
                    }

                    if (slotValue != null) {
                        introspector.setSlotValue(slotName, slotValue, obj, schema);
                    }
                }
            }
        }
    }

    /**
     * Converts a Java object into a proper abstract descriptor.
     *
     * @param obj        the object
     * @param globalOnto The ontology this ontology is part of (i.e. the
     *                   ontology that extends this ontology).
     * @return the abstract descriptor.
     * @throws UnknownSchemaException If no schema for the object to be
     *                                translated is defined in this ontology.
     * @throws OntologyException      if some mismatch with the schema is found
     */
    protected AbsObject fromObject(Object obj, Ontology globalOnto) throws UnknownSchemaException, OntologyException {

        // If obj is already an abstract descriptor --> just return it
        if (obj instanceof AbsObject) {
            return (AbsObject) obj;
        }

        // Retrieve the Java class
        Class<?> javaClass = obj.getClass();
        if (logger.isLoggable(Logger.FINE))
            logger.log(Logger.FINE, "Ontology " + getName() + ". Translating object of class " + javaClass);

        // Retrieve the schema
        ObjectSchema schema = schemas.get(javaClass);
        if (schema != null) {
            if (logger.isLoggable(Logger.FINE))
                logger.log(Logger.FINE, "Ontology " + getName() + ". Schema for class " + javaClass + " found locally: " + schema);

            // Try to manage as special type (i.e. types that need special handling such as enum)
            AbsObject abs = null;
            try {
                abs = externalizeSpecialType(obj, schema, javaClass, globalOnto);
            } catch (NotASpecialType nasp) {
                // Manage as structure slot
                abs = schema.newInstance();
                externalize(obj, abs, schema, globalOnto);
            }
            return abs;
        }

        // If we get here --> This ontology is not able to translate obj
        // --> Try to convert it using the base ontologies
        for (Ontology ontology : base) {
            try {
                return ontology.fromObject(obj, globalOnto);
            } catch (UnknownSchemaException use) {
                // Try the next one
            }
        }

        throw new UnknownSchemaException();
    }
    //#APIDOC_EXCLUDE_END


    /////////////////////////
    // Utility static methods
    /////////////////////////

    private AbsObject externalizeSpecialType(Object obj, ObjectSchema schema, Class<?> javaClass, Ontology globalOnto) throws OntologyException {
        if (introspector == null) {
            throw new NotASpecialType();
        }
        return introspector.externalizeSpecialType(obj, schema, javaClass, globalOnto);
    }

    /**
     * Externalize (obj --> abs) the slots defined in  schema   and its super-schemas
     */
    protected void externalize(Object obj, AbsObject abs, ObjectSchema schema, Ontology globalOnto) throws OntologyException {
        // Let the proper ontology manage slots defined in super schemas if any
        ObjectSchema[] superSchemas = schema.getSuperSchemas();
        for (ObjectSchema superSchema : superSchemas) {
            Ontology definingOntology = getDefiningOntology(superSchema.getTypeName().toLowerCase());
            if (definingOntology != null) {
                definingOntology.externalize(obj, abs, superSchema, globalOnto);
            }
        }

        // Finally manage "local" slots through the introspector
        if (introspector != null) {
            String[] names = schema.getOwnNames();
            for (String slotName : names) {
                Object slotValue = introspector.getSlotValue(slotName, obj, schema);
                if (slotValue != null) {
                    // Try to manage as aggregate
                    AbsObject absSlotValue = null;
                    try {
                        absSlotValue = introspector.externalizeAggregate(slotName, slotValue, schema, globalOnto);
                    } catch (NotAnAggregate naa) {
                        // Manage as normal slot
                        absSlotValue = globalOnto.fromObject(slotValue);
                    }

                    if (absSlotValue != null) {
                        AbsHelper.setAttribute(abs, slotName, absSlotValue);
                    }
                }
            }
        }
    }


    //#J2ME_EXCLUDE_BEGIN

    /**
     * Set the value of slot  slotName   as  slotValue   to object  obj  
     */
    public void setSlotValue(String slotName, Object slotValue, Object obj) throws OntologyException {
        Class<?> javaClass = obj.getClass();
        ObjectSchema schema = schemas.get(javaClass);
        if (schema != null) {
            setSlotValue(slotName, slotValue, obj, schema);
            return;
        }

        // The schema must be defined in a super-ontology --> let it do the job
        for (Ontology ontology : base) {
            try {
                ontology.setSlotValue(slotName, slotValue, obj);
                return;
            } catch (UnknownSchemaException use) {
                // Try the next one
            }
        }

        throw new UnknownSchemaException();
    }

    private void setSlotValue(String slotName, Object slotValue, Object obj, ObjectSchema schema) throws OntologyException {
        if (schema.isOwnSlot(slotName)) {
            // The slot is defined in "schema" --> use the Introspector of the ontology actually defining "schema"
            Ontology definingOntology = getDefiningOntology(schema.getTypeName().toLowerCase());
            definingOntology.introspector.setSlotValue(slotName, slotValue, obj, schema);
            return;
        } else {
            // The slot must be defined in a super-schema
            ObjectSchema[] superSchemas = schema.getSuperSchemas();
            for (ObjectSchema superSchema : superSchemas) {
                try {
                    setSlotValue(slotName, slotValue, obj, superSchema);
                    return;
                } catch (UnknownSlotException use) {
                    // Try next super-schema
                }
            }
        }
        throw new UnknownSlotException(slotName);
    }

    /**
     * Retrieve the value of slot  slotName   from object  obj  
     */
    public Object getSlotValue(String slotName, Object obj) throws OntologyException {
        Class<?> javaClass = obj.getClass();
        ObjectSchema schema = schemas.get(javaClass);
        if (schema != null) {
            return getSlotValue(slotName, obj, schema);
        }
        // The schema must be defined in a super-ontology --> let it do the job
        for (Ontology ontology : base) {
            try {
                return ontology.getSlotValue(slotName, obj);
            } catch (UnknownSchemaException use) {
                // Try the next one
            }
        }

        throw new UnknownSchemaException();
    }

    private Object getSlotValue(String slotName, Object obj, ObjectSchema schema) throws OntologyException {
        if (schema.isOwnSlot(slotName)) {
            // The slot is defined in "schema" --> use the Introspector of the ontology actually defining "schema"
            Ontology definingOntology = getDefiningOntology(schema.getTypeName().toLowerCase());
            return definingOntology.introspector.getSlotValue(slotName, obj, schema);
        } else {
            // The slot must be defined in a super-schema
            ObjectSchema[] superSchemas = schema.getSuperSchemas();
            for (ObjectSchema superSchema : superSchemas) {
                try {
                    return getSlotValue(slotName, obj, superSchema);
                } catch (UnknownSlotException use) {
                    // Try next super-schema
                }
            }
        }
        throw new UnknownSlotException(slotName);
    }

    public String toString() {
        return getClass().getName() + "-" + name;
    }

    /**
     * Retrieve the names of the concepts defined in this ontology only (excluding extended ontologies).
     * It should be noticed that an agent-action is itself a concept and therefore the returned list
     * also includes names of agent-actions defined in this ontology.
     * <br>
     * <b>NOT available in J2ME</b>
     * <br>
     *
     * @return the names of the concepts defined in this ontology only (excluding extended ontologies)
     */
    public List<String> getOwnConceptNames() {
        return getOwnElementNames(ConceptSchema.class);
    }

    /**
     * Retrieve the names of all concepts defined in this ontology (including extended ontologies).
     * It should be noticed that an agent-action is itself a concept and therefore the returned list
     * also includes names of agent-actions defined in this ontology.
     * <br>
     * <b>NOT available in J2ME</b>
     * <br>
     *
     * @return the names of all concepts defined in this ontology (including extended ontologies)
     */
    public List<String> getConceptNames() {
        Set<String> names = getElementNames(ConceptSchema.class);
        return new ArrayList<>(names);
    }

    /**
     * Retrieve the names of the agent actions defined in this ontology only (excluding extended ontologies).
     * <br>
     * <b>NOT available in J2ME</b>
     * <br>
     *
     * @return the names of the agent actions defined in this ontology only (excluding extended ontologies)
     */
    public List<String> getOwnActionNames() {
        return getOwnElementNames(AgentActionSchema.class);
    }
    //#J2ME_EXCLUDE_END

    //#MIDP_EXCLUDE_BEGIN

    /**
     * Retrieve the names of all agent actions defined in this ontology (including extended ontologies).
     * <br>
     * <b>NOT available in J2ME</b>
     * <br>
     *
     * @return the names of all agent actions defined in this ontology (including extended ontologies)
     */
    public List<String> getActionNames() {
        Set<String> names = getElementNames(AgentActionSchema.class);
        return new ArrayList<>(names);
    }

    /**
     * Retrieve the names of the predicates defined in this ontology only (excluding extended ontologies).
     * <br>
     * <b>NOT available in J2ME</b>
     * <br>
     *
     * @return the names of the predicates defined in this ontology only (excluding extended ontologies)
     */
    public List<String> getOwnPredicateNames() {
        return getOwnElementNames(PredicateSchema.class);
    }
    //#MIDP_EXCLUDE_END

    /**
     * Retrieve the names of all predicatess defined in this ontology (including extended ontologies).
     * <br>
     * <b>NOT available in J2ME</b>
     * <br>
     *
     * @return the names of all predicatess defined in this ontology (including extended ontologies)
     */
    public List<String> getPredicateNames() {
        Set<String> names = getElementNames(PredicateSchema.class);
        return new ArrayList<>(names);
    }

    private List<String> getOwnElementNames(Class<?> c) {
        List<String> names = new ArrayList<>();
        for (Enumeration<String> e = elements.keys(); e.hasMoreElements(); ) {
            String key = e.nextElement();
            ObjectSchema objSchema = elements.get(key);
            if (c.isAssignableFrom(objSchema.getClass())) {
                names.add(objSchema.getTypeName());
            }
        }
        return names;
    }

    private Set<String> getElementNames(Class<?> c) {
        // We use a Set to avoid duplicating names in case an element is overridden
        Set<String> names = new HashSet<>(getOwnElementNames(c));
        for (Ontology o : base) {
            // Do not consider elements defined in the BasicOntology and in the SerializableOntology
            if ((o != null) && (o.getClass() != BasicOntology.class) && (o.getClass() != SerializableOntology.class)) {
                names.addAll(o.getElementNames(c));
            }
        }
        return names;
    }

    /**
     * Create a ConceptSlotFunction for a given slot of a given Concept.
     * The ConceptSlotFunction class allows treating the slots of an ontological concept as functions.
     * For instance, if an ontology defines a concept  Person   with a slot  name   and a slot  age  ,
     * it is possible to create expression such as<br>
     * (= (age (Person :name John)) 41) <br>
     * (> (age (Person :name John)) (age (Person :name Bill)))<br>
     * (iota ?x (= (age (Person :name John)) ?x))
     * <br>
     * <b>NOT available in MIDP</b>
     * <br>
     *
     * @param slotName The name of the slot
     * @param c        The concept a ConceptSlotFunction must be created for. This concept must have a slot called  slotName  
     * @return A ConceptSlotFunction for the given slotName of the given Concept
     * @see ConceptSlotFunction
     * see useConceptSlotsAsFunctions()
     * @since JADE 3.7
     */
    public ConceptSlotFunction createConceptSlotFunction(String slotName, Concept c) throws OntologyException {
        // Scan the ontology hierarchy and get the ontology where concept c is defined.
        // Then create a ConceptSlotFunction refering to that ontology
        ObjectSchema schema = schemas.get(c.getClass());
        if (schema != null) {
            if (conceptSlots != null) {
                if (schema.containsSlot(slotName)) {
                    return new ConceptSlotFunction(slotName, c, this);
                } else {
                    throw new OntologyException("Schema " + schema.getTypeName() + " for class " + c.getClass() + " does not contain a slot called " + slotName);
                }
            } else {
                throw new OntologyException("Ontology " + name + " does not support usage of concept slots as functions");
            }
        } else {
            for (Ontology ontology : base) {
                try {
                    return ontology.createConceptSlotFunction(slotName, c);
                } catch (UnknownSchemaException use) {
                    // Try the next one
                }
            }
        }
        throw new UnknownSchemaException();
    }

    /**
     * Instruct this ontology to support usage of concept slots as functions.
     * This method must be invoked after all schemas have been completely defined and added to this ontology.
     *
     * @see ConceptSlotFunction
     * see createConceptSlotFunction(String, Concept)
     * @since JADE 3.7
     */
    protected void useConceptSlotsAsFunctions() {
        conceptSlots = new Hashtable<>();
        Enumeration<ObjectSchema> en = schemas.elements();
        while (en.hasMoreElements()) {
            ObjectSchema schema = en.nextElement();
            String[] slotNames = schema.getNames();
            for (String slotName : slotNames) {
                System.out.println("Concept-slot-function: " + slotName);
                conceptSlots.put(slotName, slotName);
            }
        }
    }

    /**
     * Dump ontology to default output stream
     */
    public void dump() {
        dump(System.out);
    }

    /**
     * Dump ontology to specified PrintStream
     */
    public void dump(PrintStream ps) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Ontology ").append(name).append("\n");

            dump(getConceptNames(), "concept", sb);
            dump(getPredicateNames(), "predicate", sb);
            dump(getActionNames(), "action", sb);

            ps.println(sb);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dump(List<String> schemaNames, String label, StringBuilder sb) throws Exception {

        Iterator<String> iter = schemaNames.iterator();
        String conceptName;
        ObjectSchema os;
        while (iter.hasNext()) {
            conceptName = iter.next();
            os = getSchema(conceptName);

            StringBuilder sbsc = new StringBuilder();
            boolean first = true;
            ObjectSchema[] superSchemas = os.getSuperSchemas();
            for (ObjectSchema superSchema : superSchemas) {
                if (!first) {
                    sbsc.append(" ");
                }
                sbsc.append(superSchema.getTypeName());
                first = false;
            }

            sb.append("  ").append(label).append(" ").append(conceptName).append(" (").append(sbsc).append(") {\n");
            String[] names = os.getOwnNames();
            for (String s : names) {
                sb.append("    ").append(s).append(": ");
                boolean mandatory = os.isMandatory(s);
                ObjectSchema schema = os.getSchema(s);
                if (schema == null) {
                    sb.append("ERROR: no schema!\n");
                } else {
                    Object defaultValue = null;
                    Object regex = null;
                    String pValues = null;
                    Integer cardMin = null;
                    Integer cardMax = null;
                    Facet[] facets = os.getFacets(s);
                    if (facets != null) {
                        for (Facet facet : facets) {
                            if (facet instanceof DefaultValueFacet dvf) {
                                defaultValue = dvf.getDefaultValue();
                            } else if (facet instanceof RegexFacet rf) {
                                regex = rf.getRegex();
                            } else if (facet instanceof PermittedValuesFacet pvf) {
                                pValues = pvf.getPermittedValuesAsString();
                            } else if (facet instanceof CardinalityFacet cf) {
                                cardMin = cf.getCardMin();
                                cardMax = cf.getCardMax();
                            }
                        }
                    }

                    sb.append(schema.getTypeName()).append(!mandatory ? " (OPTIONAL)" : "");
                    if (defaultValue != null) {
                        sb.append(" (DEFAULT=").append(defaultValue).append(")");
                    }
                    if (regex != null) {
                        sb.append(" (REGEX=").append(regex).append(")");
                    }
                    if (pValues != null && pValues.length() > 0) {
                        sb.append(" (VALUES=").append(pValues).append(")");
                    }
                    if (cardMin != null && cardMax != null) {
                        sb.append(" ([").append(cardMin).append(",").append(cardMax != -1 ? cardMax : "unbounded").append("])");
                    }
                    sb.append("\n");
                }
            }
            sb.append("  } -> ").append(getClassForElement(os.getTypeName()).getName()).append("\n\n");
        }
    }
    //#J2ME_EXCLUDE_END
}
