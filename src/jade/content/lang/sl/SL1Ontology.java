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
package jade.content.lang.sl;

import jade.content.abs.AbsPredicate;
import jade.content.onto.BasicOntology;
import jade.content.onto.Introspector;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.PredicateSchema;
import jade.core.CaseInsensitiveString;

/**
 * Ontology containing schemas for the SL1 language operators.
 * see jade.content.Ontology
 *
 * @author Giovanni Caire - TILAB
 */
class SL1Ontology extends SL0Ontology implements SL1Vocabulary {
    // NAME
    public static final String ONTOLOGY_NAME = jade.domain.FIPANames.Ontology.SL1_ONTOLOGY;

    // The singleton instance of this ontology
    // Note that the SL0Ontology does not add any schema to the BasicOntology
    private static final Ontology theInstance = new SL1Ontology(ONTOLOGY_NAME, BasicOntology.getInstance(), null);

    /**
     * Constructor
     */
    protected SL1Ontology(String name, Ontology base, Introspector intro) {
        super(name, base, intro);

        try {
            add(new PredicateSchema(AND), AbsPredicate.getJavaClass());
            add(new PredicateSchema(OR), AbsPredicate.getJavaClass());
            add(new PredicateSchema(NOT), AbsPredicate.getJavaClass());

            PredicateSchema ps = (PredicateSchema) getSchema(AND);
            ps.add(AND_LEFT, PredicateSchema.getBaseSchema());
            ps.add(AND_RIGHT, PredicateSchema.getBaseSchema());

            ps = (PredicateSchema) getSchema(OR);
            ps.add(OR_LEFT, PredicateSchema.getBaseSchema());
            ps.add(OR_RIGHT, PredicateSchema.getBaseSchema());

            ps = (PredicateSchema) getSchema(NOT);
            ps.add(NOT_WHAT, PredicateSchema.getBaseSchema());
        } catch (OntologyException oe) {
            oe.printStackTrace();
        }

    }

    public static Ontology getInstance() {
        return theInstance;
    }

    boolean isUnaryLogicalOp(String symbol) {
        return (CaseInsensitiveString.equalsIgnoreCase(NOT, symbol));
    }

    boolean isBinaryLogicalOp(String symbol) {
        return (CaseInsensitiveString.equalsIgnoreCase(AND, symbol) ||
                CaseInsensitiveString.equalsIgnoreCase(OR, symbol));
    }

}
