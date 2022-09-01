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
package jade.content.onto;

//#J2ME_EXCLUDE_FILE

import jade.content.abs.AbsAggregate;
import jade.content.abs.AbsObject;


public class JavaCollectionOntology extends Ontology {

    // The singleton instance of this ontology
    private static final JavaCollectionOntology theInstance = new JavaCollectionOntology();

    private final Introspector introspector = new CFReflectiveIntrospector();

    private JavaCollectionOntology() {
        super("Java-Collection-ontology", (Ontology) null, null);
    }

    /**
     * Returns the singleton instance of the  JavaCollectionOntology  .
     *
     * @return the singleton instance of the  JavaCollectionOntology  
     */
    public static Ontology getInstance() {
        return theInstance;
    }

    //#APIDOC_EXCLUDE_BEGIN

    /**
     *
     */
    protected Object toObject(AbsObject abs, String lcType, Ontology globalOnto) throws OntologyException {
        if (abs instanceof AbsAggregate) {
            return introspector.internalizeAggregate(null, (AbsAggregate) abs, null, globalOnto);
        }
        throw new UnknownSchemaException(false);
    }

    /**
     *
     */
    protected AbsObject fromObject(Object obj, Ontology globalOnto) throws OntologyException {
        try {
            return introspector.externalizeAggregate(null, obj, null, globalOnto);
        } catch (NotAnAggregate naa) {
            throw new UnknownSchemaException(false);
        }
    }
    //#APIDOC_EXCLUDE_END
}
