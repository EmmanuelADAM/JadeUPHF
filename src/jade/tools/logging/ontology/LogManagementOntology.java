/*****************************************************************
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

package jade.tools.logging.ontology;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.ReflectiveIntrospector;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PrimitiveSchema;
import jade.domain.FIPAAgentManagement.ExceptionOntology;

/**
 * This class represents the Log-Agent-Management-ontology i.e. the set of
 * concepts, actions and predicates needed by the LogManagerAgent and the
 * LogHelperAgent to manage logs on JADE containers at runtime.
 *
 * @author Giovanni Caire -  TILAB
 */
public class LogManagementOntology extends Ontology implements LogManagementVocabulary {

    // The singleton instance of this ontology
    private static final Ontology theInstance = new LogManagementOntology();

    private LogManagementOntology() {
        super(NAME, ExceptionOntology.getInstance(), new ReflectiveIntrospector());

        try {
            // Concepts definitions
            add(new ConceptSchema(LOGGER_INFO), LoggerInfo.class);

            // AgentActions definitions
            add(new AgentActionSchema(GET_ALL_LOGGERS), GetAllLoggers.class);
            add(new AgentActionSchema(SET_LEVEL), SetLevel.class);
            add(new AgentActionSchema(SET_FILE), SetFile.class);


            // Slots definitions
            ConceptSchema cs = (ConceptSchema) getSchema(LOGGER_INFO);
            cs.add(LOGGER_INFO_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(LOGGER_INFO_LEVEL, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            cs.add(LOGGER_INFO_HANDLERS, (PrimitiveSchema) getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
            cs.add(LOGGER_INFO_FILE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

            AgentActionSchema as = (AgentActionSchema) getSchema(GET_ALL_LOGGERS);
            as.add(GET_ALL_LOGGERS_TYPE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
            as.add(GET_ALL_LOGGERS_FILTER, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

            as = (AgentActionSchema) getSchema(SET_LEVEL);
            as.add(SET_LEVEL_LEVEL, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            as.add(SET_LEVEL_LOGGER, (PrimitiveSchema) getSchema(BasicOntology.STRING));

            as = (AgentActionSchema) getSchema(SET_FILE);
            as.add(SET_FILE_FILE, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            as.add(SET_FILE_LOGGER, (PrimitiveSchema) getSchema(BasicOntology.STRING));

        } catch (OntologyException oe) {
            oe.printStackTrace();
        }
    }

    /**
     * This method returns the unique instance (according to the singleton
     * pattern) of the Log-Management-ontology.
     *
     * @return The singleton  Ontology   object, containing the
     * schemas for the elements of the Log-Management-ontology.
     */
    public static Ontology getInstance() {
        return theInstance;
    }
}
