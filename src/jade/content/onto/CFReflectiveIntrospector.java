package jade.content.onto;

import jade.content.abs.AbsAggregate;
import jade.content.abs.AbsTerm;
import jade.content.lang.sl.SL0Vocabulary;
import jade.content.schema.ObjectSchema;

import java.util.*;

public class CFReflectiveIntrospector extends ReflectiveIntrospector {

    public AbsAggregate externalizeAggregate(String slotName, Object obj, ObjectSchema schema, Ontology referenceOnto) throws OntologyException {
        if (!(obj instanceof Collection<?> c)) {
            throw new NotAnAggregate();
        }

        AbsAggregate absAggregate = null;
        if (!c.isEmpty() || schema == null || schema.isMandatory(slotName)) {
            // Note 1: schema is null when we are externalizing a first-level object and not the value of a slot
            // Note 2: we ignore the aggregateType specified in the slot schema and we use SET for java.util.Set and SEQUENCE for java.util.List
            String aggregateType;
            if (obj instanceof List) {
                aggregateType = SL0Vocabulary.SEQUENCE;
            } else if (obj instanceof Set) {
                aggregateType = SL0Vocabulary.SET;
            } else {
                throw new NotAnAggregate();
            }
            absAggregate = externaliseCollection(c, referenceOnto, aggregateType);
        }
        return absAggregate;
    }

    public Object internalizeAggregate(String slotName, AbsAggregate absAggregate, ObjectSchema schema, Ontology referenceOnto) throws OntologyException {
        // FIXME: Here we should check for Long --> Integer casting, but how?
        return internaliseCollection(absAggregate, referenceOnto);
    }

    private AbsAggregate externaliseCollection(Collection<?> c, Ontology referenceOnto, String aggregateType) throws OntologyException {
        AbsAggregate ret = new AbsAggregate(aggregateType);

        try {
            for (Object o : c) {
                ret.add((AbsTerm) Ontology.externalizeSlotValue(o, this, referenceOnto));
            }
        } catch (ClassCastException cce) {
            throw new OntologyException("Non term object in aggregate");
        }

        return ret;
    }

    private Collection<Object> internaliseCollection(AbsAggregate absAggregate, Ontology referenceOnto) throws OntologyException {
        Collection<Object> ret;
        if (absAggregate.getTypeName().equals(SL0Vocabulary.SET)) {
            ret = new HashSet<>(absAggregate.size());
        } else {
            ret = new ArrayList<>(absAggregate.size());
        }

        for (int i = 0; i < absAggregate.size(); i++) {
            Object element = Ontology.internalizeSlotValue(absAggregate.get(i), this, referenceOnto);
            // Check if the element is a Term, a primitive an AID or a List
            Ontology.checkIsTerm(element);
            ret.add(element);
        }

        return ret;
    }
}
