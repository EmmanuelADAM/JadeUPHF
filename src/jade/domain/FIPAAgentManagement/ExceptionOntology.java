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


package jade.domain.FIPAAgentManagement;

import jade.content.onto.BCReflectiveIntrospector;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PredicateSchema;

/**
 * This class groups into a separated ontology the elements of the
 * FIPA-Agent-Management-ontology (see FIPA specification document no. 23)
 * representing generic exceptions. This allows reusing these
 * elements into other ontologies simply including tis Exception-ontology
 * <p>
 * The actual  Ontology   object representing the
 * Exception-ontology is a singleton and is accessible through
 * the static method  getInstance()
 *
 * @author Giovanni Caire - CSELT S.p.A.
 * @version $Date: 2003-02-20 11:54:12 +0100 (gio, 20 feb 2003) $ $Revision: 3675 $
 */
public class ExceptionOntology extends Ontology implements ExceptionVocabulary {

    private static final Ontology theInstance = new ExceptionOntology();

    private ExceptionOntology() {
        //#MIDP_EXCLUDE_BEGIN
        super(NAME, BasicOntology.getInstance(), new BCReflectiveIntrospector());
        //#MIDP_EXCLUDE_END

		/*#MIDP_INCLUDE_BEGIN
  	super(NAME, BasicOntology.getInstance(), null);
   	#MIDP_INCLUDE_END*/


        try {
            //#MIDP_EXCLUDE_BEGIN
            add(new PredicateSchema(UNAUTHORISED), Unauthorised.class);
            add(new PredicateSchema(UNSUPPORTEDACT), UnsupportedAct.class);
            add(new PredicateSchema(UNEXPECTEDACT), UnexpectedAct.class);
            add(new PredicateSchema(UNSUPPORTEDVALUE), UnsupportedValue.class);
            add(new PredicateSchema(UNRECOGNISEDVALUE), UnrecognisedValue.class);
            add(new PredicateSchema(MISSINGARGUMENT), MissingArgument.class);
            add(new PredicateSchema(UNEXPECTEDARGUMENT), UnexpectedArgument.class);
            add(new PredicateSchema(UNEXPECTEDARGUMENTCOUNT), UnexpectedArgumentCount.class);
            add(new PredicateSchema(UNSUPPORTEDFUNCTION), UnsupportedFunction.class);
            add(new PredicateSchema(MISSINGPARAMETER), MissingParameter.class);
            add(new PredicateSchema(UNEXPECTEDPARAMETER), UnexpectedParameter.class);
            add(new PredicateSchema(UNRECOGNISEDPARAMETERVALUE), UnrecognisedParameterValue.class);
            add(new PredicateSchema(INTERNALERROR), InternalError.class);
            //#MIDP_EXCLUDE_END

			/*#MIDP_INCLUDE_BEGIN
			add(new PredicateSchema(UNAUTHORISED));
	  	add(new PredicateSchema(UNSUPPORTEDACT));
	  	add(new PredicateSchema(UNEXPECTEDACT));
	  	add(new PredicateSchema(UNSUPPORTEDVALUE));
	  	add(new PredicateSchema(UNRECOGNISEDVALUE));
	  	add(new PredicateSchema(UNSUPPORTEDFUNCTION));
	  	add(new PredicateSchema(MISSINGARGUMENT));
	  	add(new PredicateSchema(UNEXPECTEDARGUMENT));
	  	add(new PredicateSchema(UNEXPECTEDARGUMENTCOUNT));
	  	add(new PredicateSchema(MISSINGPARAMETER));
	  	add(new PredicateSchema(UNEXPECTEDPARAMETER));
	  	add(new PredicateSchema(UNRECOGNISEDPARAMETERVALUE));
	  	add(new PredicateSchema(INTERNALERROR));
   		#MIDP_INCLUDE_END*/

            PredicateSchema ps = (PredicateSchema) getSchema(UNSUPPORTEDACT);
            ps.add(UNSUPPORTEDACT_ACT, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            ps = (PredicateSchema) getSchema(UNEXPECTEDACT);
            ps.add(UNEXPECTEDACT_ACT, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            ps = (PredicateSchema) getSchema(UNSUPPORTEDVALUE);
            ps.add(UNSUPPORTEDVALUE_VALUE, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            ps = (PredicateSchema) getSchema(UNRECOGNISEDVALUE);
            ps.add(UNRECOGNISEDVALUE_VALUE, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            ps = (PredicateSchema) getSchema(UNAUTHORISED);

            ps = (PredicateSchema) getSchema(UNSUPPORTEDFUNCTION);
            ps.add(UNSUPPORTEDFUNCTION_FUNCTION, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            ps = (PredicateSchema) getSchema(MISSINGARGUMENT);
            ps.add(MISSINGARGUMENT_ARGUMENT, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            ps = (PredicateSchema) getSchema(UNEXPECTEDARGUMENT);
            ps.add(UNEXPECTEDARGUMENT_ARGUMENT, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            ps = (PredicateSchema) getSchema(MISSINGPARAMETER);
            ps.add(MISSINGPARAMETER_OBJECT_NAME, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
            ps.add(MISSINGPARAMETER_PARAMETER_NAME, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            ps = (PredicateSchema) getSchema(UNEXPECTEDPARAMETER);
            ps.add(UNEXPECTEDPARAMETER_OBJECT_NAME, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
            ps.add(UNEXPECTEDPARAMETER_PARAMETER_NAME, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            ps = (PredicateSchema) getSchema(UNRECOGNISEDPARAMETERVALUE);
            ps.add(UNRECOGNISEDPARAMETERVALUE_PARAMETER_NAME, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
            ps.add(UNRECOGNISEDPARAMETERVALUE_PARAMETER_VALUE, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);

            ps = (PredicateSchema) getSchema(INTERNALERROR);
            ps.add(INTERNALERROR_MESSAGE, getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
        } catch (OntologyException oe) {
            oe.printStackTrace();
        }
    }

    /**
     * This method returns the unique instance (according to the singleton
     * pattern) of the Exception-ontology.
     *
     * @return The singleton  Ontology   object, containing the
     * schemas for the elements of the Exception-ontology.
     */
    public static Ontology getInstance() {
        return theInstance;
    }


}
