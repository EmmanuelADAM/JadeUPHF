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
package jade.content.schema;

import jade.content.abs.AbsObject;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.facets.CardinalityFacet;
import jade.content.schema.facets.TypedAggregateFacet;
import jade.core.CaseInsensitiveString;
import jade.util.Logger;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Giovanni Caire - TILAB
 */
class ObjectSchemaImpl extends ObjectSchema {
    static final String RESULT_SLOT_NAME = "__Result_SLOT_123";

    static {
        baseSchema = new ObjectSchemaImpl();
    }

    private final Logger logger = Logger.getMyLogger(this.getClass().getName());
    private String typeName = null;
    private Hashtable<CaseInsensitiveString, SlotDescriptor> slots;
    private Vector<CaseInsensitiveString> slotNames;
    private Vector<ObjectSchema> superSchemas;

    // Note that the list of facets for a given slot cannot be included in the slot descriptor.
    // In fact facets are associated to the a slot in a given schema, not to the slot itself.
    // For instance if we have
    // - schema A that extends schema B (B is a super-schema of A)
    // - B defines slot S and associates facet f1 to it
    // - A associates facet f2 to slot S
    // When validating an instance of B only facet f1 must be applied, while when validating
    // an instance of A both f1 and f2 must be applied.

    private Hashtable<CaseInsensitiveString, Vector<Facet>> facets;

    /**
     * Construct a schema that vinculates an entity to be a generic
     * object (i.e. no constraints at all)
     */
    private ObjectSchemaImpl() {
        this(BASE_NAME);
    }

    /**
     * Creates an  ObjectSchema   with a given type-name.
     *
     * @param typeName The name of this  ObjectSchema  .
     */
    protected ObjectSchemaImpl(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Add a slot to the schema.
     *
     * @param name        The name of the slot.
     * @param slotSchema  The schema defining the type of the slot.
     * @param optionality The optionality, i.e.,  OPTIONAL
     *                    or  MANDATORY
     */
    protected void add(String name, ObjectSchema slotSchema, int optionality) {
        CaseInsensitiveString ciName = new CaseInsensitiveString(name);
        if (slots == null) {
            slots = new Hashtable<>();
            slotNames = new Vector<>();
        }
        if (slots.put(ciName, new SlotDescriptor(name, slotSchema, optionality)) == null) {
            // We treat Action results as if they were slots. However we don't want the special
            // RESULT_SLOT_NAME to be included among slot names
            if (!name.equals(RESULT_SLOT_NAME)) {
                slotNames.addElement(ciName);
            }
        }
    }

    /**
     * Add a mandatory slot to the schema.
     *
     * @param name       name of the slot.
     * @param slotSchema schema of the slot.
     */
    protected void add(String name, ObjectSchema slotSchema) {
        add(name, slotSchema, MANDATORY);
    }

    /**
     * Add a slot with cardinality between  cardMin
     * and  cardMax   to this schema.
     * Adding such a slot corresponds to add a slot
     * of type Aggregate and then to add proper facets (constraints)
     * to check that the type of the elements in the aggregate are
     * compatible with  elementsSchema   and that the
     * aggregate contains at least  cardMin   elements and
     * at most  cardMax   elements. By default the Aggregate
     * is of type  BasicOntology.SEQUENCE  .
     *
     * @param name           The name of the slot.
     * @param elementsSchema The schema for the elements of this slot.
     * @param cardMin        This slot must get at least  cardMin
     *                       values
     * @param cardMax        This slot can get at most  cardMax
     *                       values
     */
    protected void add(String name, ObjectSchema elementsSchema, int cardMin, int cardMax) {
        add(name, elementsSchema, cardMin, cardMax, BasicOntology.SEQUENCE);
    }

    /**
     * Add a slot with cardinality between  cardMin
     * and  cardMax   to this schema and allow specifying the type
     * of Aggregate to be used for this slot.
     *
     * @param name           The name of the slot.
     * @param elementsSchema The schema for the elements of this slot.
     * @param cardMin        This slot must get at least  cardMin
     *                       values
     * @param cardMax        This slot can get at most  cardMax
     *                       values
     * @param aggType        The type of Aggregate to be used
     * @see #add(String, ObjectSchema, int, int)
     */
    protected void add(String name, ObjectSchema elementsSchema, int cardMin, int cardMax, String aggType) {
        int optionality = (cardMin == 0 ? OPTIONAL : MANDATORY);
        add(name, elementsSchema, cardMin, cardMax, aggType, optionality);
    }

    /**
     * Add a slot with optionality and cardinality between  cardMin
     * and  cardMax   to this schema and allow specifying the type
     * of Aggregate to be used for this slot.
     *
     * @param name           The name of the slot.
     * @param elementsSchema The schema for the elements of this slot.
     * @param cardMin        This slot must get at least  cardMin
     *                       values
     * @param cardMax        This slot can get at most  cardMax
     *                       values
     * @param aggType        The type of Aggregate to be used
     * @param optionality    The optionality, i.e.,  OPTIONAL
     * @see #add(String, ObjectSchema, int, int)
     */
    protected void add(String name, ObjectSchema elementsSchema, int cardMin, int cardMax, String aggType, int optionality) {
        try {
            // If the aggregate type is not yet present in the BasicOntology, add it (without elements type specification)
            ObjectSchema aggTypeSchema = BasicOntology.getInstance().getSchema(aggType);
            if (aggTypeSchema == null) {
                // Create a new aggregate schema and add it to BasicOntology
                aggTypeSchema = new AggregateSchema(aggType);
                BasicOntology.getInstance().add(aggTypeSchema);
            }

            // If elements are typed, we need an ad-hoc schema to hold the elements type specification
            ObjectSchema schema;
            if (elementsSchema != null) {
                schema = new AggregateSchema(aggType, (TermSchema) elementsSchema);
            } else {
                schema = aggTypeSchema;
            }

            add(name, schema, optionality);

            // Add proper facets
            if (elementsSchema != null) {
                addFacet(name, new TypedAggregateFacet(elementsSchema));
            }
            addFacet(name, new CardinalityFacet(cardMin, cardMax));
        } catch (OntologyException oe) {
            // Should never happen
            oe.printStackTrace();
        }
    }

    /**
     * Add a super schema to this schema, i.e. this schema will
     * inherit all characteristics from the super schema
     *
     * @param superSchema the super schema.
     */
    protected void addSuperSchema(ObjectSchema superSchema) {
        if (superSchema != null) {
            if (superSchemas == null) {
                superSchemas = new Vector<>();
            }
            superSchemas.addElement(superSchema);
        }
    }

    /**
     * Add a  Facet   on a slot of this schema
     *
     * @param slotName the name of the slot the  Facet
     *                 must be added to.
     * @param f        the  Facet   to be added.
     * @throws OntologyException if slotName does not identify
     *                           a valid slot in this schema
     */
    protected void addFacet(String slotName, Facet f) throws OntologyException {
        if (containsSlot(slotName)) {
            CaseInsensitiveString ciName = new CaseInsensitiveString(slotName);
            if (facets == null) {
                facets = new Hashtable<>();
            }
            Vector<Facet> v = facets.get(ciName);
            if (v == null) {
                v = new Vector<>();
                facets.put(ciName, v);
                //DEBUG
                if (logger.isLoggable(Logger.CONFIG))
                    logger.log(Logger.CONFIG, "Added facet " + f + " to slot " + slotName);
            }
            v.addElement(f);
        } else {
            throw new OntologyException(slotName + " is not a valid slot in this schema");
        }
    }

    /**
     * Retrieves the name of the type of this schema.
     *
     * @return the name of the type of this schema.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Returns the names of all the slots in this  Schema
     * (including slots defined in super schemas).
     *
     * @return the names of all slots.
     */
    public String[] getNames() {
        Vector<CaseInsensitiveString> allSlotNames = new Vector<>();

        fillAllSlotNames(allSlotNames);

        String[] names = new String[allSlotNames.size()];
        int counter = 0;
        for (Enumeration<CaseInsensitiveString> e = allSlotNames.elements(); e.hasMoreElements(); ) {
            names[counter++] = e.nextElement().toString();
        }

        return names;
    }

    /**
     * Returns the names of the slots defined in this  Schema
     * (excluding slots defined in super schemas).
     *
     * @return the names of the slots defined in this  Schema  .
     */
    public String[] getOwnNames() {
        if (slotNames != null) {
            String[] ownNames = new String[slotNames.size()];
            int counter = 0;
            for (Enumeration<CaseInsensitiveString> e = slotNames.elements(); e.hasMoreElements(); ) {
                ownNames[counter++] = e.nextElement().toString();
            }
            return ownNames;
        } else {
            return new String[0];
        }
    }

    /**
     * Retrieves the schema of a slot of this  Schema  .
     *
     * @param name The name of the slot.
     * @return the  Schema   of slot  name
     * @throws OntologyException If no slot with this name is present
     *                           in this schema.
     */
    public ObjectSchema getSchema(String name) throws OntologyException {
        SlotDescriptor slot = getSlot(new CaseInsensitiveString(name));
        if (slot == null) {
            throw new OntologyException("No slot named: " + name + " in schema " + typeName);
        }
        return slot.schema;
    }

    /**
     * Indicate whether a given  String   is the name of a
     * slot defined in this  Schema
     *
     * @param name The  String   to test.
     * @return  true   if  name   is the name of a
     * slot defined in this  Schema  .
     */
    public boolean containsSlot(String name) {
        SlotDescriptor slot = getSlot(new CaseInsensitiveString(name));
        return (slot != null);
    }

    /**
     * Indicate whether a given  String   is the name of a
     * slot actually defined in this  Schema   (excluding super-schemas)
     *
     * @param name The  String   to test.
     * @return  true   if  name   is the name of a
     * slot actually defined in this  Schema   (excluding super-schemas).
     */
    public boolean isOwnSlot(String name) {
        SlotDescriptor slot = getOwnSlot(new CaseInsensitiveString(name));
        return (slot != null);
    }

    /**
     * Indicate whether a slot of this schema is mandatory
     *
     * @param name The name of the slot.
     * @return  true   if the slot is mandatory.
     * @throws OntologyException If no slot with this name is present
     *                           in this schema.
     */
    public boolean isMandatory(String name) throws OntologyException {
        SlotDescriptor slot = getSlot(new CaseInsensitiveString(name));
        if (slot == null) {
            throw new OntologyException("No slot named: " + name);
        }
        return slot.optionality == MANDATORY;
    }

    /**
     * Creates an Abstract descriptor to hold an object compliant to
     * this  Schema  .
     */
    public AbsObject newInstance() throws OntologyException {
        throw new OntologyException("AbsObject cannot be instantiated");
    }

    private void fillAllSlotNames(Vector<CaseInsensitiveString> v) {
        // Get slot names of super schemas (if any) first
        if (superSchemas != null) {
            for (Enumeration<ObjectSchema> e = superSchemas.elements(); e.hasMoreElements(); ) {
                ObjectSchemaImpl superSchema = (ObjectSchemaImpl) e.nextElement();

                superSchema.fillAllSlotNames(v);
            }
        }

        // Then add slot names of this schema
        if (slotNames != null) {
            for (Enumeration<CaseInsensitiveString> e = slotNames.elements(); e.hasMoreElements(); ) {
                v.addElement(e.nextElement());
            }
        }
    }

    /**
     * Check whether a given abstract descriptor complies with this
     * schema.
     *
     * @param abs The abstract descriptor to be checked
     * @throws OntologyException If the abstract descriptor does not
     *                           complies with this schema
     */
    public void validate(AbsObject abs, Ontology onto) throws OntologyException {
        validateSlots(abs, onto);
    }

    /**
     * For each slot
     * - get the corresponding attribute value from the abstract descriptor
     * abs
     * - Check that it is not null if the slot is mandatory
     * - Check that its schema is compatible with the schema of the slot
     * - Check that it is a correct abstract descriptor by validating it
     * against its schema.
     */
    protected void validateSlots(AbsObject abs, Ontology onto) throws OntologyException {
        // Validate all the attributes in the abstract descriptor
        String[] slotNames = getNames();
        for (String slotName : slotNames) {
            AbsObject slotValue = abs.getAbsObject(slotName);
            CaseInsensitiveString ciName = new CaseInsensitiveString(slotName);
            validate(ciName, slotValue, onto);
        }
    }

    /**
     * Validate a given abstract descriptor as a value for a slot
     * defined in this schema
     *
     * @param slotName The name of the slot
     * @param value    The abstract descriptor to be validated
     * @return true if the slot is defined in this schema (or in
     * one of its super schemas). false otherwise
     * @throws OntologyException If the abstract descriptor is not a
     *                           valid value
     */
    private boolean validate(CaseInsensitiveString slotName, AbsObject value, Ontology onto) throws OntologyException {
        // DEBUG
        if (logger.isLoggable(Logger.FINE))
            logger.log(Logger.FINE, "Validating " + (value != null ? value.toString() : "null") + " as a value for slot " + slotName);
        // NOTE: for performance reasons we don't want to scan the schema
        // to check if slotValue is a valid slot and THEN to scan again
        // the schema to validate value. This is the reason for the
        // boolean return value of this method
        boolean slotFound = false;

        // If the slot is defined in this schema --> check the value
        // against the schema of the slot. Otherwise let the super-schema
        // where the slot is defined validate the value
        SlotDescriptor dsc = getOwnSlot(slotName);
        if (dsc != null) {
            // DEBUG
            if (logger.isLoggable(Logger.CONFIG))
                logger.log(Logger.CONFIG, "Slot " + slotName + " is defined in schema " + this);
            if (value == null) {
                // Check optionality
                if (dsc.optionality == MANDATORY) {
                    throw new OntologyException("Missing value for mandatory slot " + slotName + ". Schema is " + this);
                }
                // Don't need to check facets on a null value for an optional slot
                return true;
            } else {
                // - Get from the ontology the schema s that defines the type
                // of the abstract descriptor value.
                // - Check if this schema is compatible with the schema for
                // slot slotName
                // - Finally check value against s
                ObjectSchema s = onto.getSchema(value.getTypeName());
                //DEBUG
                if (logger.isLoggable(Logger.CONFIG))
                    logger.log(Logger.CONFIG, "Actual schema for " + value + " is " + s);
                if (s == null) {
                    throw new OntologyException("No schema found for type " + value.getTypeName() + ". Ontology is " + onto.getName());
                }
                if (!s.isCompatibleWith(dsc.schema)) {
                    throw new OntologyException("Schema " + s + " for element " + value + " is not compatible with schema " + dsc.schema + " for slot " + slotName);
                }
                //DEBUG
                if (logger.isLoggable(Logger.CONFIG))
                    logger.log(Logger.CONFIG, "Schema " + s + " for type " + value + " is compatible with schema " + dsc.schema + " for slot " + slotName);
                s.validate(value, onto);
            }
            slotFound = true;
        } else {
            if (superSchemas != null) {
                Enumeration<ObjectSchema> e = superSchemas.elements();
                while (e.hasMoreElements()) {
                    ObjectSchemaImpl s = (ObjectSchemaImpl) e.nextElement();
                    if (s.validate(slotName, value, onto)) {
                        slotFound = true;
                        // Don't need to check other super-schemas
                        break;
                    }
                }
            }
        }

        if (slotFound && facets != null) {
            // Check value against the facets (if any) defined for the
            // slot in this schema
            Vector<Facet> ff = facets.get(slotName);
            if (ff != null) {
                Enumeration<Facet> e = ff.elements();
                while (e.hasMoreElements()) {
                    Facet f = e.nextElement();
                    //DEBUG
                    if (logger.isLoggable(Logger.CONFIG))
                        logger.log(Logger.CONFIG, "Checking facet " + f + " defined on slot " + slotName);
                    f.validate(value, onto);
                }
            } else {
                //DEBUG
                if (logger.isLoggable(Logger.CONFIG))
                    logger.log(Logger.CONFIG, "No facets for slot " + slotName);
            }
        }

        return slotFound;
    }

    /**
     * Check if this schema is compatible with a given schema s.
     * This is the case if
     * 1) This schema is equals to s
     * 2) s is one of the super-schemas of this schema
     * 3) This schema descends from s i.e.
     * - s is the base schema for the XXXSchema class this schema is
     * an instance of (e.g. s is ConceptSchema.getBaseSchema() and this
     * schema is an instance of ConceptSchema)
     * - s is the base schema for a super-class of the XXXSchema class
     * this schema is an instance of (e.g. s is TermSchema.getBaseSchema()
     * and this schema is an instance of ConceptSchema)
     */
    public boolean isCompatibleWith(ObjectSchema s) {
        if (equals(s)) {
            return true;
        }
        if (isSubSchemaOf(s)) {
            return true;
        }
        return descendsFrom(s);
    }

    /**
     * Return true if
     * - s is the base schema for the XXXSchema class this schema is
     * an instance of (e.g. s is ConceptSchema.getBaseSchema() and this
     * schema is an instance of ConceptSchema)
     * - s is the base schema for a super-class of the XXXSchema class
     * this schema is an instance of (e.g. s is TermSchema.getBaseSchema()
     * and this schema is an instance of ConceptSchema)
     */
    protected boolean descendsFrom(ObjectSchema s) {
        // The base schema for the ObjectSchema class descends only
        // from itself
        if (s != null) {
            return s.equals(getBaseSchema());
        } else {
            return false;
        }
    }

    /**
     * Return true if s is a super-schema (directly or indirectly)
     * of this schema
     */
    private boolean isSubSchemaOf(ObjectSchema s) {
        if (superSchemas != null) {
            Enumeration<ObjectSchema> e = superSchemas.elements();
            while (e.hasMoreElements()) {
                ObjectSchemaImpl s1 = (ObjectSchemaImpl) e.nextElement();
                if (s1.equals(s)) {
                    return true;
                }
                if (s1.isSubSchemaOf(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String toString() {
        return getClass().getName() + "-" + getTypeName();
    }

    public boolean equals(Object o) {
        if (o != null) {
            return toString().equals(o.toString());
        } else {
            return false;
        }
    }

    public ObjectSchema[] getSuperSchemas() {
        ObjectSchema[] ss;
        if (superSchemas != null) {
            ss = new ObjectSchema[superSchemas.size()];
            for (int i = 0; i < ss.length; ++i) {
                ss[i] = superSchemas.elementAt(i);
            }
        } else {
            ss = new ObjectSchema[0];
        }
        return ss;
    }

    public Facet[] getFacets(String slotName) {
        Vector<Facet> v = getAllFacets(slotName);
        Facet[] ff = null;
        if (v != null) {
            ff = new Facet[v.size()];
            for (int i = 0; i < v.size(); ++i) {
                ff[i] = v.elementAt(i);
            }
        }
        return ff;
    }

    /**
     * Get the facets defined upon a slot. More in details this method returns
     * all facets defined in this schema plus all facets defined in super-schemas
     * up to the schema actually declaring the given slot.
     *
     * @return the facets defined upon a slot or null if the specified slot is not found.
     */
    Vector<Facet> getAllFacets(String slotName) { // Package-scoped for testing purposes only
        Vector<Facet> allFacets = new Vector<>();
        CaseInsensitiveString caseInsensitiveSlotName = new CaseInsensitiveString(slotName);
        if (facets != null) {
            Vector<Facet> v = facets.get(caseInsensitiveSlotName);
            if (v != null) {
                // We don't use Vector.addAll() for MIDP compatibility
                addAll(allFacets, v);
            }
        }

        boolean found = false;
        if (getOwnSlot(caseInsensitiveSlotName) == null) {
            // The slot must be defined in one of the super-schema
            if (superSchemas != null) {
                for (int i = 0; i < superSchemas.size(); i++) {
                    ObjectSchemaImpl superSchema = (ObjectSchemaImpl) superSchemas.elementAt(i);
                    if (superSchema.containsSlot(slotName)) {
                        found = true;
                        // We don't use Vector.addAll() for MIDP compatibility
                        addAll(allFacets, superSchema.getAllFacets(slotName));
                    }
                }
            }
        } else {
            found = true;
        }

        return (found ? allFacets : null);
    }

    private void addAll(Vector<Facet> v1, Vector<Facet> v2) {
        for (int i = 0; i < v2.size(); ++i) {
            v1.addElement(v2.elementAt(i));
        }
    }

    private SlotDescriptor getOwnSlot(CaseInsensitiveString ciName) {
        return (slots != null ? slots.get(ciName) : null);
    }

    private SlotDescriptor getSlot(CaseInsensitiveString ciName) {
        SlotDescriptor dsc = getOwnSlot(ciName);
        if (dsc == null) {
            if (superSchemas != null) {
                for (int i = 0; i < superSchemas.size(); ++i) {
                    ObjectSchemaImpl sc = (ObjectSchemaImpl) superSchemas.elementAt(i);
                    dsc = sc.getSlot(ciName);
                    if (dsc != null) {
                        break;
                    }
                }
            }
        }
        return dsc;
    }

    public boolean isAssignableFrom(ObjectSchema s) {
        // This = destination schema
        // s = source schema

        // Type names must be equals or
        // the source schema must have a super-schema with the same type name of the destination type name
        // e.g.
        // - source schema = LivingBeing
        // - destination = Person and Person has a super-schema called LivingBeing
        if (s.isCompatibleWith(this)) {
            return true;
        }

        try {
            // All slots of the destination schema that are present in the source schema too must be assignable.
            // Slots of the destination schema not present in the source schema must be OPTIONAL.
            ObjectSchema destSchema;
            ObjectSchema srcSchema;
            String[] destSlotNames = getNames();
            for (String destSlotName : destSlotNames) {
                destSchema = getSchema(destSlotName);

                try {
                    srcSchema = s.getSchema(destSlotName);

                    // The slot is present in source and destination schema -> check compatibility
                    if (!destSchema.isAssignableFrom(srcSchema)) {
                        return false;
                    }
                } catch (OntologyException e) {
                    // The slot is present only in destination schema -> must be not mandatory
                    if (isMandatory(destSlotName)) {
                        return false;
                    }
                }
            }

            return true;

        } catch (OntologyException e) {
            return false;
        }
    }

    private class SlotDescriptor implements Serializable {
        private final String name;
        private final ObjectSchema schema;
        private final int optionality;

        /**
         * Construct a SlotDescriptor
         */
        private SlotDescriptor(String name, ObjectSchema schema, int optionality) {
            this.name = name;
            this.schema = schema;
            this.optionality = optionality;
        }

    }

}

